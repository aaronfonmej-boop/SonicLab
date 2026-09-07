# Estado de implementación universidad/laboratorio

Última actualización: 2026-08-15  
Versión candidata de fuente: `1.1.0-academia-continua`  
Último APK verificado: `1.0.0-cymatics-stage`

Estados permitidos: **pendiente**, **en curso**, **bloqueado**, **implementado** y
**validado**. Un archivo creado o una función diseñada no se marca como implementada
si todavía faltan compilación, pruebas aplicables o APK debug.

| N.º | Mejora | Estado | Evidencia y siguiente control |
|---:|---|---|---|
| 1 | Documento de validación cuantitativa | implementado | Documento y 19 pruebas analíticas aprobadas en Gradle/JVM; lint y APK correctos. Validación GPU y física queda en la mejora 4. |
| 2 | Revisión externa por especialista | en curso | Checklist firmable implementado y entregado sin marcas. La revisión, identidad y firma de un especialista real continúan pendientes; no se atribuye aprobación. |
| 3 | Calibración real de SPL | implementado | Flujo visible de 5 s, offset, DataStore, perfiles exactos por ruta/configuración, advertencia de cambio, invalidación/borrado y 6 pruebas aprobadas. Falta validación metrológica con hardware real. |
| 4 | Pruebas instrumentadas y matriz de hardware | pendiente | Requiere Android SDK y equipos reales o servicio autorizado. |
| 5 | Incertidumbre y cifras significativas | pendiente | El exportador actual no modela incertidumbre. |
| 6 | Solver FDTD 2D validado | pendiente | No iniciado; el modelo analítico sigue siendo la ruta activa. |
| 7 | Accesibilidad y localización | pendiente | Existe soporte visual parcial; falta auditoría integral. |
| 8 | Privacidad y licencia | pendiente | No se ha elegido licencia en nombre del propietario. |
| 9 | Exportación de imagen y video | pendiente | No iniciado. |
| 10 | Manual docente y prácticas exportables | en curso | Las cuatro prácticas incorporan objetivo, pasos, pregunta, pista, respuesta esperada y rúbrica editable; el progreso se exporta a JSON. Falta el manual docente independiente. |
| 11 | Cadena profesional de adquisición | en curso | Se registra la ruta efectiva y se intenta `UNPROCESSED` con alternativa `DEFAULT`; faltan selección manual y compatibilidad USB avanzada. |
| 12 | Analizador acústico científico | pendiente | FFT Hann fija de 2.048 muestras en la versión heredada. |
| 13 | Laboratorio de acústica de salas | pendiente | No iniciado. |
| 14 | Gemelo acústico simulación/medición | pendiente | No iniciado. |
| 15 | Geometría, materiales y fuentes | pendiente | Solo campo libre ideal y recinto rectangular analítico. |
| 16 | Paquete reproducible `.soniclab` | pendiente | No se ha definido el esquema. |
| 17 | Tres niveles de interfaz y modos académicos | implementado | Los tres espacios de 0.6 fueron validados en un Xiaomi. 0.7 añade perfiles Estudiante/Docente, progreso persistente, entregas, rúbricas, bloqueos y exportación sobre el mismo estado; APK y pruebas correctos. Falta auditoría táctil específica de 0.7 para declararlo validado. |
| 18 | Gestor de rendimiento y estabilidad térmica | pendiente | Hay métricas base; falta el gestor objetivo y su validación. |
| 19 | API científica e integración externa | pendiente | El núcleo puro existe dentro de `:app`; no se ha extraído una API estable. |
| 20 | Calidad institucional y ciencia abierta | pendiente | No iniciado; la licencia y firma siguen reservadas al propietario. |
| 21 | Cymatics Lab modal | implementado | Ocho bancos reproducibles, cuatro geometrías, dos bordes, 24 modos, placa OpenGL, sonda, arena cualitativa y exportación. Validación física/hardware pendiente. |
| 22 | Academia de Cimática | implementado | Cuatro prácticas, perfiles compartidos Estudiante/Docente, bloqueos, respuestas y codec compatible. Auditoría táctil pendiente. |
| 23 | Movimiento cinematográfico de Placa | implementado | Morph modal continuo, doble VBO de arena, cámara inercial, luz reactiva, ambiente, perfiles y movimiento reducido: 52 pruebas, lint, shaders y APK correctos. Falta validación táctil y de rendimiento en GPU Android. |
| 24 | Cymatics Stage 1.0 | implementado | Reloj perceptivo, placa multicapas, escenario, pulsos, corrientes nodales, cuatro cámaras, bloom por FBO y fallback adaptativo: 54 pruebas, lint, seis programas GLSL y APK correctos. Falta validación visual y de rendimiento en GPU Android. |
| 25 | Academia Continua 1.1 | en curso | Fuente con seis rutas, 34 guías, 20 prácticas y 66 Dudas. Validador de contenido correcto; faltan Gradle, lint y APK 1.1 en este entorno. |
| 26 | Persistencia y migración académica v3 | en curso | Se preservan claves v1/v2 y los ocho IDs; se añadieron fases, borradores, cuaderno, dudas, asignaciones, entregas y feedback. Falta prueba de actualización en dispositivo. |
| 27 | Paquetes académicos SAF | en curso | Pack, submission y feedback JSON limitados/validados e importables localmente. Falta round trip Android y revisión de archivos hostiles. |
| 28 | Alineación UNAP 2026 | en curso | Paquete orientativo con fuentes, fecha y `officialEndorsement=false`; falta revalidación editorial externa previa a publicación. |
| 29 | Build y APK 1.1 | bloqueado | El entorno actual no contiene distribución Gradle ni Android SDK y no puede descargar Gradle; no se generó una APK 1.1 falsa. |

## Última verificación cerrada de la base 1.0

El 2026-08-13 se ejecutaron desde limpio `testDebugUnitTest`, `lintDebug` y
`assembleDebug` con Gradle 8.10.2, JDK 17 y Android SDK 35 y compilación Kotlin en
proceso: 54/54 pruebas aprobadas, cero incidencias lint y APK debug versión 10. Los diez
shaders de Placa compilaron y sus seis programas enlazaron como GLSL ES 3.00.
El control pendiente más importante es una campaña instrumentada en hardware y una
comparación modal física.

## Academic Experience 0.7

La capa académica usa el mismo `AcousticState` y prepara casos mediante transformaciones
puras. Los bloqueos docentes se aplican al único flujo de actualización; no duplican el
motor ni impiden cámara, pausa o calidad GPU. DataStore conserva perfil, actividad,
progreso y respuestas en el dispositivo. La APK mantiene el paquete y certificado de
0.6, por lo que admite una actualización directa. La prueba táctil específica de 0.7
en teléfono continúa pendiente.

## Cymatics Lab 0.8

Placa usa `CymaticsState` y un controlador independientes de `AcousticState`. Los
bancos se generan fuera del runtime, se validan por cabecera/versión/hash y se cargan
fuera del hilo UI. La placa y hasta 200k granos se dibujan en GPU. La arena es el
fallback nodal determinista animado en shader y se etiqueta como cualitativa. No hubo
GPU Android disponible durante el cierre; por ello el núcleo se declara implementado,
no validado en hardware ni experimentalmente.

## Cymatics Motion 0.9

La transición usa dos texturas modales y dos VBO de arena únicamente durante el
fundido. Los efectos decorativos consumen el campo modal como entrada y no modifican
la física exportada. El codec académico sube a versión 4 y mantiene lectura de 2/3.
La APK conserva paquete y certificado debug de 0.6–0.8, por lo que admite actualización
directa. El estado es implementado, no validado en hardware.

## Cymatics Stage 1.0

La fase visible se desacopla de la frecuencia audible mediante un reloj perceptivo. La
placa se dibuja con espesor, material y filamentos; el escenario, los pulsos, las
corrientes nodales y el postprocesado son capas visuales declaradas. El codec académico
sube a versión 5 y conserva lectura de 2/3/4. La ruta FBO vuelve a render directo si no
se completa o si el presupuesto adaptativo detecta presión térmica/FPS. La APK conserva
paquete y certificado debug, por lo que admite actualización directa. El estado es
implementado y verificado estáticamente, no validado visualmente en una GPU Android.
