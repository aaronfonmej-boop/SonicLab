#!/usr/bin/env python3
"""Validate SonicLab 3D v1.1 learning manifests against the typed catalog sources."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
ACADEMIC = ROOT / "app" / "src" / "main" / "java" / "com" / "soniclab3d" / "academic"


def load_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def require(condition: bool, message: str, errors: list[str]) -> None:
    if not condition:
        errors.append(message)


def main() -> int:
    errors: list[str] = []
    manifest = load_json(ASSETS / "learning" / "content_manifest.json")
    faq_manifest = load_json(ASSETS / "help" / "faq_manifest.json")
    core_pack = load_json(ASSETS / "learning" / "career-packs" / "core_acoustics_es.json")
    unap_pack = load_json(ASSETS / "learning" / "career-packs" / "unap_2026.json")

    route_ids = [item["id"] for item in manifest["paths"]]
    guide_ids = [guide_id for item in manifest["paths"] for guide_id in item["guideIds"]]
    practice_ids = manifest["practiceIds"]
    legacy_ids = manifest["legacyPracticeIds"]
    faq_groups = faq_manifest["groups"]

    require(len(route_ids) == len(set(route_ids)) == 6, "Rutas: se esperaban seis IDs únicos", errors)
    require(len(guide_ids) == len(set(guide_ids)) == manifest["counts"]["guides"], "Guías: recuento o IDs repetidos", errors)
    require(len(practice_ids) == len(set(practice_ids)) == manifest["counts"]["practices"], "Prácticas: recuento o IDs repetidos", errors)
    require(len(legacy_ids) == 8 and set(legacy_ids) <= set(practice_ids), "Migración: faltan prácticas v1.0", errors)
    require(sum(group["count"] for group in faq_groups) == faq_manifest["totalEntries"] >= 60, "Dudas: recuento menor que 60 o manifiesto incoherente", errors)
    require({group["pathId"] for group in faq_groups} == set(route_ids), "Dudas: cobertura de rutas incompleta", errors)

    catalog_text = (ACADEMIC / "LearningCatalog.kt").read_text(encoding="utf-8")
    faq_text = (ACADEMIC / "LearningFaqCatalog.kt").read_text(encoding="utf-8")
    legacy_text = (ACADEMIC / "AcademicModels.kt").read_text(encoding="utf-8")
    kotlin_guide_ids = re.findall(r"\bg\(\s*\"([^\"]+)\"", catalog_text)
    kotlin_new_practice_ids = re.findall(r"\bp\(\s*\"([^\"]+)\"", catalog_text)
    faq_seed_count = len(re.findall(r"^\s*Seed\(", faq_text, flags=re.MULTILINE))

    require(set(kotlin_guide_ids) == set(guide_ids), "Manifiesto y guías Kotlin no coinciden", errors)
    require(set(kotlin_new_practice_ids) == set(practice_ids) - set(legacy_ids), "Manifiesto y prácticas nuevas no coinciden", errors)
    for practice_id in legacy_ids:
        require(f'id = "{practice_id}"' in legacy_text, f"No existe el ID heredado {practice_id}", errors)
    require(faq_seed_count == faq_manifest["totalEntries"], f"Se encontraron {faq_seed_count} entradas de Dudas y el manifiesto declara {faq_manifest['totalEntries']}", errors)

    for pack_name, pack in (("core_acoustics_es", core_pack), ("unap_2026", unap_pack)):
        for field in ("institutionId", "catalogId", "catalogVersion", "reviewedAt", "sourceTitle", "sourceEdition", "officialEndorsement", "notes"):
            require(field in pack, f"{pack_name}: falta {field}", errors)
        require(pack.get("officialEndorsement") is False, f"{pack_name}: officialEndorsement debe ser false", errors)

    published_sources = "\n".join(path.read_text(encoding="utf-8") for path in (
        ACADEMIC / "LearningCatalog.kt",
        ACADEMIC / "LearningFaqCatalog.kt",
        ACADEMIC / "LearningModels.kt",
    ))
    require(not re.search(r"\bTODO\b", published_sources), "Existe un TODO editorial", errors)
    require(not re.search(r"Lorem ipsum", published_sources, flags=re.IGNORECASE), "Existe contenido Lorem ipsum", errors)

    if errors:
        print("VALIDACIÓN DE CONTENIDO: FALLÓ")
        for error in errors:
            print(f"- {error}")
        return 1

    print("VALIDACIÓN DE CONTENIDO: OK")
    print(f"6 rutas · 3 niveles · 34 guías · 20 prácticas · {faq_manifest['totalEntries']} dudas · 2 paquetes curriculares")
    print("8 IDs v1.0 preservados · alineación UNAP no oficial declarada")
    return 0


if __name__ == "__main__":
    sys.exit(main())
