# Informe de entrega · SonicLab 3D 0.8 Cymatics Lab

Fecha de cierre técnico: 2026-08-13  
Versión: `versionCode 8` / `0.8.0-cymatics-lab`  
Paquete: `com.soniclab3d`

## Resultado

La aplicación incorpora el dominio **Placa** detrás de un selector visible
**Aire | Placa**. Aire sigue siendo el dominio inicial y conserva su estado al cambiar.
Placa usa física, estado, repositorio, renderer y exportador separados; mantiene los
espacios Explorar, Aprender y Laboratorio y extiende el mismo sistema académico local.

## Implementación

- Modelo de placa delgada Kirchhoff–Love y respuesta modal amortiguada.
- Ocho bancos reproducibles: cuadrado, círculo, triángulo y hexágono con borde apoyado
  y sujeto; malla 64 × 64 y 24 modos por banco.
- Malla OpenGL ES 3.0 de 128 × 128, deformación y normales GPU.
- Estilos Científico y Espectacular sobre el mismo campo; tres paletas, cuatro variables,
  nodos, antinodos, contornos, retícula, excitador y sonda.
- Cámara orbital, pinza, pan, toque para excitador, pulsación larga para sonda y cinco
  presets específicos.
- Arena de 30k/60k/120k/200k con destinos nodales deterministas fuera del hilo UI y
  animación GPU; etiquetada como visualización cualitativa.
- Parámetros ideales configurables de material, espesor, dimensión, amortiguamiento y
  excitación; ajuste exacto a resonancia y selección manual/automática de modo.
- Cuatro prácticas nuevas de Cimática para Estudiante/Docente, bloqueos y persistencia
  compatible con las claves de v0.7.
- Exportación JSON autocontenida con versión, fecha, modelo, física, banco, modo,
  excitador, sonda, arena, visuales, rendimiento y advertencias.

## Archivos creados principales

- `cymatics/CymaticsState.kt`, `CymaticsPhysics.kt`, `CymaticsController.kt`,
  `CymaticsMode.kt`, `CymaticsModeRepository.kt`, `CymaticsExporter.kt`.
- `renderer/CymaticsRenderer.kt`, `CymaticsGLSurfaceView.kt`.
- `ui/CymaticsPanel.kt`.
- cuatro shaders `cymatics_surface.*` y `cymatics_sand.*`.
- ocho activos `.cym` y `mode_manifest.json`.
- `tools/generate_cymatics_modes.py`.
- pruebas `CymaticsPhysicsTest.kt` y extensiones académicas.
- cuatro documentos específicos de Cymatics Lab.

## Archivos existentes modificados

- `SonicLabApp.kt`: selector de dominio y composición contextual.
- `AcademicModels.kt`, `AcademicExperienceRepository.kt` y
  `AcademicExperienceExporter.kt`: prácticas, bloqueos, codec y exportación 0.8.
- `CameraController.kt`: presets exclusivos de Placa; la ruta de presets de Aire no
  cambió.
- `AndroidManifest.xml`: conserva requisito OpenGL ES 3.0 y elimina la declaración
  opcional de Vulkan.
- `app/build.gradle.kts`: versión 8.
- README y documentación de estado, roadmap y modelo.

## Protección de Aire

Antes de implementar se ejecutaron las 33 pruebas, lint y ensamblado de v0.7. En 0.8
se compararon hashes de física acústica, estado acústico, partícula de referencia,
recinto, audio, analizador, calibración, renderer de partículas, vista GL, programa de
shaders y shaders de Aire. Todos permanecen idénticos. Solo `CameraController.kt`
cambió para añadir un método separado de presets de Placa; `setPreset` de Aire conserva
su implementación.

## Verificación final

- `testDebugUnitTest`: **47 pruebas, 0 fallos**.
- Las 33 pruebas heredadas continúan aprobando; se añadieron 14 controles de física,
  activos, manifiesto, codecs y academia de Cimática.
- `lintDebug`: **0 incidencias**.
- `assembleDebug`: correcto.
- Ocho bancos regenerados dos veces con SHA-256 idénticos.
- Cuadrado apoyado: error del primer modo 2,1383 %; residual máximo 1,68 × 10⁻¹⁰.
- APK debug: **11.879.573 bytes**.
- APK SHA-256: `4357bf06c679d5f61ad377ddbddd5b2d6ec44ddf2097568fce714e6ff19be88b`.
- `zipalign -c -P 16 -v 4`: correcto.
- Firma: APK Signature Scheme v2, un firmante debug RSA 2048.
- Certificado SHA-256:
  `ba179e9879e4c85a84dc22f136ed38f4d46034cbd29a7d8f27c6416566c59af1`,
  igual a v0.7/v0.6 y apto para actualización directa de esas APK debug.
- El SHA-256 del ZIP fuente se entrega en `SHA256SUMS.txt` junto a los artefactos para
  evitar incluir un checksum autorreferencial dentro del propio ZIP.

## Limitaciones e incidencias abiertas

- No había dispositivo/emulador Android con GPU: shaders, ciclo de vida, gestos y
  rendimiento quedan pendientes de prueba instrumentada real.
- La arena usa la ruta fallback determinista animada en GPU; no implementa transform
  feedback ni dinámica granular predictiva.
- El borde sujeto es una penalización de pendiente en malla finita.
- No hay bloom, sombras físicas, síntesis de sonido de placa ni comparación por cámara.
- No se dispone de ensayo modal físico, incertidumbre o validación metrológica.

## Siguiente paso recomendado

Instalar como actualización sobre la APK 0.7 en el Xiaomi ya usado, repetir 20 ciclos
Aire/Placa, rotación y pausa/reanudación, y registrar FPS/P90/P99 en Normal 60k y 120k.
Luego comparar las primeras frecuencias de una placa identificada mediante acelerómetro
o vibrómetro; no usar la arena como variable cuantitativa.
