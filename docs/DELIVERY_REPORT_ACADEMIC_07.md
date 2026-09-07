# Informe de entrega · SonicLab 3D Academic Experience 0.7

Fecha: 2026-08-12  
Paquete: `com.soniclab3d`  
Versión: `7` / `0.7.0-academic-experience`

## Alcance implementado

- Perfil Estudiante con selección o recepción de una práctica, objetivo, procedimiento,
  predicción, pista, respuesta y contraste posterior.
- Progreso y respuestas persistentes en DataStore, locales y sin cuenta de usuario.
- Perfil Docente con selección de actividad, respuesta esperada, rúbrica y lectura de
  resultados guardados en el dispositivo.
- Publicación local de una actividad y bloqueos independientes de frecuencia, nivel,
  ambiente y fuentes. La cámara, el tiempo, la calidad GPU y las variables visuales no
  quedan bloqueadas.
- Cuatro prácticas centrales reutilizadas por ambos perfiles; cada una configura el
  único `AcousticState` de la aplicación.
- Exportación JSON académica con progreso, actividad, bloqueos y parámetros de escena.
  El archivo declara `calibratedMeasurement: false` y `analytical-simulation`.
- Confirmación en dos pasos antes de borrar el progreso local.

## Aislamiento del núcleo

No se modificaron los archivos de física, audio, renderizador OpenGL ni shaders. Se
registró un SHA-256 de cada archivo antes de la implementación y se verificó después de
compilar; todos conservaron el mismo hash. Academic 0.7 añade una capa de orquestación y
persistencia, no un segundo motor de simulación.

## Verificación ejecutada

| Control | Resultado |
|---|---|
| `testDebugUnitTest` | 33 pruebas, 0 fallos |
| Pruebas académicas nuevas | 8: prácticas, bloqueos, codecs y exportación |
| `lintDebug` | 0 incidencias |
| `assembleDebug` | correcto |
| ZIP alignment | correcto a 4 bytes |
| Firma | APK Signature Scheme v2 válida |
| `minSdk` / `targetSdk` | 26 / 35 |
| Tamaño APK | 10.191.051 bytes |
| SHA-256 APK | `ff97cef110f1b5a87d9fd7df0c7bbb2c998d6e0321659f383c96e51d21252e5f` |

Certificado debug SHA-256:
`ba179e9879e4c85a84dc22f136ed38f4d46034cbd29a7d8f27c6416566c59af1`.
Coincide con la APK 0.6 instalada y validada por el usuario, por lo que Android puede
aplicar 0.7 como actualización directa al tener además un `versionCode` mayor.

## Límites de esta entrega

- La asignación y los resultados son locales al dispositivo; no existe aula remota,
  autenticación, sincronización ni backend.
- Una entrega registrada no equivale a calificación automática. La respuesta esperada
  y la rúbrica sirven para contraste y revisión docente.
- Los niveles de la escena son parámetros simulados. El micrófono solo muestra SPL
  absoluto cuando existe un perfil de calibración aplicable, como en 0.6.
- Las pruebas JVM no sustituyen una revisión táctil de 0.7 en teléfono ni una validación
  metrológica con instrumental externo.
