# Informe de entrega · SonicLab 3D 0.9 Cymatics Motion

Fecha de cierre técnico: 2026-08-13  
Versión: `versionCode 9` / `0.9.0-cymatics-motion`  
Paquete: `com.soniclab3d`

## Resultado

El modo **Placa** recibe un sistema de movimiento cinematográfico configurable sin
cambiar el modelo modal de 0.8. La escena predeterminada usa el perfil Cinemático y
ofrece perfiles Preciso y Fluido, control manual y una activación **Escena show** de
un toque. Aire y la identidad instalable de la aplicación se conservan.

## Implementación visual

- Morph `smootherstep` entre dos texturas modales `RG32F`; también interpola la máscara
  al cambiar geometría.
- Continuidad ante cambios rápidos: el campo visible intermedio pasa a ser el origen
  del siguiente morph, sin volver bruscamente al modo anterior.
- Dos VAO/VBO de arena con fundido cruzado; la nube anterior permanece visible hasta
  que la nueva está cargada.
- Trayectorias de arena curvas, acotadas por máscara, salto granular visual, brillo y
  pulso ligado a la resonancia. Los destinos nodales siguen siendo deterministas.
- Cámara inercial con deriva lenta. Cualquier gesto suspende la deriva y da prioridad
  temporal al usuario; los presets ahora llegan con transición suave.
- Luz principal orbital, luz secundaria, Fresnel, halo nodal, cintas iso-modales,
  brillo de borde y respiración del excitador.
- Hasta 12k partículas ambientales aditivas reutilizando el VBO activo.
- Perfiles Preciso/Fluido/Cinemático, intensidad, duración de morph, flujo de arena y
  conmutadores independientes.
- `Reducir movimiento` fija intensidad decorativa en cero, omite ambiente y completa
  inmediatamente los fundidos.

## Integridad científica y académica

- Autovalores, bancos, frecuencias, respuesta, acoplamiento y sonda no fueron
  reemplazados ni recalculados por el sistema visual.
- El brillo de resonancia se deriva de amplitud, respuesta y acoplamiento normalizados.
- Cámara, luces, aura, ambiente y flujo se exportan explícitamente como capas visuales.
- La arena continúa etiquetada como relajación nodal cualitativa, no solver granular.
- El codec académico 4 persiste los ocho parámetros nuevos y conserva compatibilidad
  de lectura con las versiones 2 y 3.
- El bloqueo docente de ayudas visuales incluye ahora el perfil de movimiento.

## Rendimiento diseñado

- Terminada una transición, los shaders omiten por rama uniforme la textura modal
  anterior y vuelve la ruta de un campo.
- El doble draw de arena solo vive durante un máximo de 0,8 s.
- La pasada ambiental se limita a 12k puntos y sigue la fracción adaptativa.
- Dos VBO de 200k ocupan aproximadamente 9,16 MiB; dos texturas modales, 64 KiB.
- La calidad adaptativa conserva su mínimo de 25 % y responde a FPS/estado térmico.

## Verificación final

- Construcción completa desde `clean` con Gradle 8.10.2, JDK 17 y Android SDK 35.
- `testDebugUnitTest`: **52 pruebas, 0 fallos y 0 errores**.
- `lintDebug`: **0 incidencias**.
- `assembleDebug`: correcto.
- Cuatro shaders compilados y enlazados estáticamente con glslang para GLSL ES 3.00.
- APK debug: **11.410.995 bytes**.
- APK SHA-256:
  `eec4af819b03326f4e872fb0f6197e894f09aaf8d227ac57679ca117b2cd0f18`.
- `aapt`: paquete `com.soniclab3d`, `versionCode 9`,
  `versionName 0.9.0-cymatics-motion`, `minSdk 26`, `targetSdk 35`.
- `zipalign -c -P 16 4`: correcto.
- Firma: APK Signature Scheme v2, un firmante debug RSA 2048.
- Certificado SHA-256:
  `ba179e9879e4c85a84dc22f136ed38f4d46034cbd29a7d8f27c6416566c59af1`,
  igual a v0.8/v0.7/v0.6 y compatible con actualización directa de esas APK debug.
- El APK contiene los ocho bancos, el manifiesto modal y los cuatro shaders 0.9.

El primer cierre `clean` encontró un archivo temporal ausente en el caché incremental
de Kotlin. Se descartó esa ruta y se repitió la compilación completa con el compilador
en proceso; el segundo cierre terminó correctamente. No se ocultó ni se aceptó una
salida parcial.

## Limitaciones abiertas

- No había dispositivo/emulador con GPU Android. El enlace del driver, la experiencia
  táctil, FPS/P90/P99, memoria del driver y comportamiento térmico siguen pendientes.
- Las capas visuales no son bloom HDR, trazado de rayos ni iluminación físicamente
  calibrada.
- No hay dinámica granular predictiva, colisiones, fricción ni fuerzas calibradas.
- El modelo de placa ideal y las limitaciones de 0.8 permanecen vigentes.

## Prueba recomendada en el teléfono

1. Instalar sobre v0.8 y confirmar que Android ofrece actualización, no otra app.
2. Abrir Placa y pulsar **Activar escena show**.
3. Recorrer modos manuales y mover frecuencia en automático; observar continuidad.
4. Cambiar cuadrada/circular/triangular/hexagonal durante una transición.
5. Rotar, ampliar y desplazar; comprobar que la cámara flotante cede al gesto.
6. Activar **Reducir movimiento** y verificar que desaparecen deriva, ambiente y morph.
7. Probar 60k, 120k y 200k durante varios minutos, con pausa, rotación y Aire/Placa.

El SHA-256 del ZIP fuente se entrega en `SHA256SUMS-v0.9.txt` fuera del ZIP para evitar
un checksum autorreferencial.
