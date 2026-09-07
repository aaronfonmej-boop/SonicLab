# Informe de entrega · SonicLab 3D 1.0 Cymatics Stage

Fecha de cierre: 2026-08-13  
Versión: `versionCode 10` / `1.0.0-cymatics-stage`  
Paquete: `com.soniclab3d`

## Resultado

Se reconstruyó la presentación de Placa como una escena continua. El cambio elimina la
fase visual ligada directamente a la frecuencia audible, separa metal y filamentos,
mantiene la arena activa después del asentamiento y añade profundidad, pulsos, cámara
coreografiada y bloom opcional. La física modal y los ocho bancos no se sustituyeron.

## Cambios principales

- reloj visual logarítmico, aproximadamente 0,42 ciclos/s a 120 Hz con el ajuste base;
- cara inferior, material modal y filamentos nodales en pasadas separadas;
- escenario bajo la placa con retícula, anillos y pulsos desde el excitador;
- microcorrientes de arena y hasta 16.000 granos tangenciales luminosos;
- cámaras Fija, Flotante, Órbita y Show con prioridad temporal para gestos;
- paletas Aurora eléctrica y Solar dorada;
- FBO de escena, extracción luminosa, bloom a media resolución y composición final;
- respaldo directo por framebuffer incompleto, presión térmica o FPS bajos;
- controles individuales, presets coherentes, movimiento reducido y codec 5 compatible
  con escenas académicas 2/3/4;
- exportación que marca escenario, pulsos, corrientes, cámara y bloom como visuales.

## Verificación ejecutada

- Construcción limpia: `clean testDebugUnitTest lintDebug assembleDebug`.
- Pruebas JVM: **54**, 0 fallos y 0 errores.
- Android Lint: 0 incidencias.
- GLSL ES 3.00: diez shaders compilados individualmente y seis pares enlazados con
  glslang: superficie, arena, escenario, extracción, blur y composición.
- `zipalign -c -P 16 -v 4`: verificación correcta.
- `apksigner`: firma v2 correcta, un firmante.
- `aapt`: paquete, versión y SDK verificados; `minSdk 26`, `targetSdk 35`.
- Los ocho bancos `.cym`, el manifiesto y los diez shaders están dentro del APK.

## APK

- Tamaño: **11.431.950 bytes**.
- SHA-256: `f44748254f6e8d9cda964895a831bd4c3c5a380b0d0f44438fc2f92d1f79fc15`.
- Certificado debug SHA-256:
  `ba179e9879e4c85a84dc22f136ed38f4d46034cbd29a7d8f27c6416566c59af1`.

El certificado coincide con las entregas previas, por lo que la APK debug admite una
actualización directa sobre ellas. Para distribución pública se requiere un keystore de
release controlado por el propietario.

## Límite de validación

El entorno no dispone de EGL/GLES ni de una GPU Android. Hubo compilación y enlace
estáticos, pero no captura visual ni medición de FPS del nuevo motor. Deben revisarse en
el teléfono objetivo el balance de bloom, clipping, grosor nodal, pausa de cámara tras
gestos, rotación y activación de la ruta directa adaptativa. Esto no se presenta como
validación de hardware, perceptual ni experimental.

El SHA-256 del ZIP fuente se registra fuera del propio ZIP en `SHA256SUMS-v1.0.txt` para
evitar una autorreferencia imposible.
