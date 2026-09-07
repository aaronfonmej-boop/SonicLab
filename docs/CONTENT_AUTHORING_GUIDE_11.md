# Guía de autoría de contenido 1.1

## Reglas mínimas

Cada ID es estable, único y ASCII. Una publicación incluye versión de esquema/contenido,
idioma, ruta, nivel, dominio, semestres orientativos, objetivo observable, duración,
tipo científico, limitación, referencias, revisión y versión mínima de app.

Una guía debe ofrecer propósito, idea central, conceptos, ecuación cuando corresponde,
pasos, comprobación, respuesta razonada, error común, limitación y referencias. Una
práctica debe incluir preset, ocho etapas, evidencia, pregunta, pista, solución docente y
rúbrica. Una Duda debe responder de forma directa, ampliar sin diagnosticar, declarar
límites y vincular contenido cuando exista.

## Procedimiento

1. Añadir o editar el elemento tipado sin cambiar IDs publicados.
2. Actualizar `content_manifest.json` o `faq_manifest.json`.
3. Revisar que el preset exista y abra el dominio correcto.
4. Confirmar que Medición, Simulación, Visualización y Educativo no se confundan.
5. Ejecutar:

```bash
python3 tools/validate_learning_content.py
./gradlew testDebugUnitTest lintDebug assembleDebug
```

6. Revisar en teléfono compacto, horizontal y tableta. No publicar un contenido por
   alcanzar un recuento si repite el objetivo de otro.

## Cambios incompatibles

No renombrar prácticas heredadas, filtrar claves de respuesta a estudiantes, aceptar
scripts, rutas de archivo o expresiones arbitrarias. Un cambio editorial sustantivo
incrementa `contentVersion`; los intentos previos conservan su ID y número de intento.
