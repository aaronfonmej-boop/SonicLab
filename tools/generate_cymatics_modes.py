#!/usr/bin/env python3
"""Generate deterministic modal banks for SonicLab 3D Cymatics Lab.

The simply-supported banks use eigenvectors of a Dirichlet finite-difference
Laplacian. Those eigenvectors are also modes of the corresponding discrete
biharmonic operator K=L². Clamped banks use K plus a documented boundary-slope
penalty on the first interior ring. This is a finite-grid approximation, not an
experimental characterization of a real plate.

Runtime Android code has no NumPy/SciPy dependency: it only reads the generated
big-endian binary banks committed under app/src/main/assets/cymatics.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import struct
from dataclasses import dataclass
from pathlib import Path

import numpy as np
from scipy import sparse
from scipy.sparse.linalg import eigsh


GENERATOR_VERSION = "0.8.0"
MODEL_ID = "cymatics-kirchhoff-love-modal-0.8"
MAGIC = b"CYM8"
FORMAT_VERSION = 1


@dataclass(frozen=True)
class Geometry:
    name: str
    ordinal: int


@dataclass(frozen=True)
class Boundary:
    name: str
    ordinal: int


GEOMETRIES = (
    Geometry("square", 0),
    Geometry("circle", 1),
    Geometry("triangle", 2),
    Geometry("hexagon", 3),
)
BOUNDARIES = (
    Boundary("simply_supported", 0),
    Boundary("clamped", 1),
)


def geometry_mask(name: str, xx: np.ndarray, yy: np.ndarray) -> np.ndarray:
    if name == "square":
        return (np.abs(xx) <= 0.90) & (np.abs(yy) <= 0.90)
    if name == "circle":
        return xx * xx + yy * yy <= 0.90**2
    if name == "triangle":
        radius = 0.96
        half_side = math.sqrt(3.0) * radius / 2.0
        vertices = np.array(
            [[-half_side, -radius / 2.0], [half_side, -radius / 2.0], [0.0, radius]]
        )
        return points_in_polygon(xx, yy, vertices)
    if name == "hexagon":
        radius = 0.92
        return (
            (np.abs(xx) <= radius)
            & (np.abs(yy) <= math.sqrt(3.0) * radius / 2.0)
            & (math.sqrt(3.0) * np.abs(xx) + np.abs(yy) <= math.sqrt(3.0) * radius)
        )
    raise ValueError(f"Unknown geometry: {name}")


def points_in_polygon(xx: np.ndarray, yy: np.ndarray, vertices: np.ndarray) -> np.ndarray:
    inside = np.zeros_like(xx, dtype=bool)
    j = len(vertices) - 1
    for i in range(len(vertices)):
        xi, yi = vertices[i]
        xj, yj = vertices[j]
        crossing = ((yi > yy) != (yj > yy)) & (
            xx < (xj - xi) * (yy - yi) / ((yj - yi) + 1.0e-15) + xi
        )
        inside ^= crossing
        j = i
    return inside


def build_laplacian(mask: np.ndarray, spacing: float) -> tuple[sparse.csr_matrix, np.ndarray, np.ndarray]:
    grid = mask.shape[0]
    indices = -np.ones(mask.shape, dtype=np.int32)
    active = np.argwhere(mask)
    for index, (row, column) in enumerate(active):
        indices[row, column] = index

    rows: list[int] = []
    columns: list[int] = []
    values: list[float] = []
    edge_ring = np.zeros(len(active), dtype=bool)
    inverse_h2 = 1.0 / (spacing * spacing)
    neighbors = ((-1, 0), (1, 0), (0, -1), (0, 1))
    for index, (row, column) in enumerate(active):
        rows.append(index)
        columns.append(index)
        values.append(4.0 * inverse_h2)
        for dr, dc in neighbors:
            rr, cc = row + dr, column + dc
            if 0 <= rr < grid and 0 <= cc < grid and mask[rr, cc]:
                rows.append(index)
                columns.append(int(indices[rr, cc]))
                values.append(-inverse_h2)
            else:
                edge_ring[index] = True
    matrix = sparse.coo_matrix((values, (rows, columns)), shape=(len(active), len(active)))
    return matrix.tocsr(), active, edge_ring


def solve_modes(
    mask: np.ndarray,
    boundary: Boundary,
    mode_count: int,
    spacing: float,
) -> tuple[np.ndarray, np.ndarray, dict[str, object]]:
    laplacian, active, edge_ring = build_laplacian(mask, spacing)
    biharmonic = (laplacian.T @ laplacian).tocsr()
    penalty = 0.0
    if boundary.name == "clamped":
        penalty = 18.0 / spacing**4
        biharmonic = biharmonic + sparse.diags(edge_ring.astype(np.float64) * penalty)

    count = min(mode_count + 4, biharmonic.shape[0] - 2)
    initial = np.linspace(0.25, 1.0, biharmonic.shape[0], dtype=np.float64)
    values, vectors = eigsh(
        biharmonic,
        k=count,
        sigma=0.0,
        which="LM",
        v0=initial,
        tol=1.0e-9,
        maxiter=100_000,
    )
    order = np.argsort(values)
    values = values[order]
    vectors = vectors[:, order]

    valid_values: list[float] = []
    valid_fields: list[np.ndarray] = []
    residuals: list[float] = []
    for value, vector in zip(values, vectors.T):
        if not np.isfinite(value) or value <= 1.0e-9:
            continue
        norm = float(np.max(np.abs(vector)))
        if norm <= 1.0e-12:
            continue
        vector = vector / norm
        max_index = int(np.argmax(np.abs(vector)))
        if vector[max_index] < 0.0:
            vector = -vector
        residual = np.linalg.norm(biharmonic @ vector - value * vector) / (
            max(np.linalg.norm(value * vector), 1.0e-12)
        )
        field = np.zeros(mask.shape, dtype=np.float32)
        field[active[:, 0], active[:, 1]] = vector.astype(np.float32)
        valid_values.append(float(value))
        valid_fields.append(field)
        residuals.append(float(residual))
        if len(valid_values) == mode_count:
            break
    if len(valid_values) != mode_count:
        raise RuntimeError(f"Only {len(valid_values)} valid modes found")

    modes = np.stack(valid_fields)
    gram = vectors[:, :mode_count].T @ vectors[:, :mode_count]
    off_diagonal = gram - np.diag(np.diag(gram))
    diagnostics: dict[str, object] = {
        "max_relative_residual": max(residuals),
        "max_orthogonality_error": float(np.max(np.abs(off_diagonal))),
        "clamped_penalty": penalty,
        "relative_residuals": residuals,
    }
    return np.asarray(valid_values, dtype=np.float32), modes, diagnostics


def write_bank(
    path: Path,
    grid: int,
    geometry: Geometry,
    boundary: Boundary,
    eigenvalues: np.ndarray,
    mask: np.ndarray,
    modes: np.ndarray,
) -> str:
    with path.open("wb") as output:
        output.write(
            struct.pack(
                ">4s5i",
                MAGIC,
                FORMAT_VERSION,
                grid,
                len(eigenvalues),
                geometry.ordinal,
                boundary.ordinal,
            )
        )
        output.write(eigenvalues.astype(">f4", copy=False).tobytes(order="C"))
        output.write(mask.astype(">f4", copy=False).tobytes(order="C"))
        output.write(modes.astype(">f4", copy=False).tobytes(order="C"))
    return hashlib.sha256(path.read_bytes()).hexdigest()


def square_analytic_check(eigenvalues: np.ndarray) -> dict[str, float]:
    # Domain is [-0.90, 0.90], so its dimensionless side is 1.8.
    side = 1.8
    analytical = []
    for m in range(1, 10):
        for n in range(1, 10):
            mu = (math.pi / side) ** 2 * (m * m + n * n)
            analytical.append(mu * mu)
    analytical = np.sort(np.asarray(analytical))[: len(eigenvalues)]
    errors = np.abs(eigenvalues - analytical) / analytical
    return {
        "first_mode_relative_error": float(errors[0]),
        "median_first_12_relative_error": float(np.median(errors[:12])),
    }


def generate(output_dir: Path, grid: int, mode_count: int) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)
    axis = np.linspace(-1.0, 1.0, grid)
    xx, yy = np.meshgrid(axis, axis)
    spacing = float(axis[1] - axis[0])
    manifest: dict[str, object] = {
        "modelId": MODEL_ID,
        "generatorVersion": GENERATOR_VERSION,
        "formatVersion": FORMAT_VERSION,
        "gridSize": grid,
        "modeCount": mode_count,
        "method": "finite-difference biharmonic eigenproblem",
        "simplySupported": "K=L_D^T L_D with zero displacement outside the domain",
        "clamped": "K plus first-interior-ring zero-slope penalty; finite-grid approximation",
        "coordinateDomain": "[-1,1] x [-1,1]",
        "normalization": "each field uses max(abs(phi)) = 1; sign fixed at its largest-magnitude cell",
        "ordering": "ascending positive eigenvalue",
        "banks": [],
    }
    for geometry in GEOMETRIES:
        mask = geometry_mask(geometry.name, xx, yy)
        for boundary in BOUNDARIES:
            print(f"Solving {geometry.name}/{boundary.name}...", flush=True)
            eigenvalues, modes, diagnostics = solve_modes(
                mask, boundary, mode_count, spacing
            )
            filename = f"{geometry.name}_{boundary.name}.cym"
            path = output_dir / filename
            digest = write_bank(
                path, grid, geometry, boundary, eigenvalues, mask, modes
            )
            entry: dict[str, object] = {
                "geometry": geometry.name,
                "geometryOrdinal": geometry.ordinal,
                "boundary": boundary.name,
                "boundaryOrdinal": boundary.ordinal,
                "file": filename,
                "sha256": digest,
                "activeGridPoints": int(mask.sum()),
                "firstEigenvalue": float(eigenvalues[0]),
                "lastEigenvalue": float(eigenvalues[-1]),
                "modes": [
                    {
                        "index": index + 1,
                        "eigenvalue": float(eigenvalue),
                        "relativeResidual": diagnostics["relative_residuals"][index],
                    }
                    for index, eigenvalue in enumerate(eigenvalues)
                ],
                **diagnostics,
            }
            if geometry.name == "square" and boundary.name == "simply_supported":
                entry["analyticalValidation"] = square_analytic_check(eigenvalues)
            manifest["banks"].append(entry)
    manifest_path = output_dir / "mode_manifest.json"
    manifest_path.write_text(
        json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )
    print(f"Wrote {manifest_path}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("app/src/main/assets/cymatics"),
    )
    parser.add_argument("--grid", type=int, default=64)
    parser.add_argument("--modes", type=int, default=24)
    arguments = parser.parse_args()
    if arguments.grid < 32 or arguments.modes < 8:
        raise SystemExit("Use at least a 32x32 grid and eight modes")
    generate(arguments.output, arguments.grid, arguments.modes)


if __name__ == "__main__":
    main()
