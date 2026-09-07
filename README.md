# SonicLab 3D 1.1 · Academia Continua

SonicLab 3D es un laboratorio científico Android nativo. La versión 1.1 amplía la base
**Cymatics Stage 1.0** con una experiencia académica local y progresiva para varios años
de estudio. Conserva Aire, Placa, Explorar, Aprender, Laboratorio, Compose y OpenGL ES
3.0. En Aire, su idea central se mantiene
físicamente explícita: **la onda se propaga, pero cada partícula del aire oscila
alrededor de su posición de equilibrio**. La escala física y la exageración visual
se muestran por separado para evitar confundir una representación educativa con una
medición literal.

## Academia Continua 1.1

- Seis rutas editables y tres niveles: Fundamentos, Aplicación y Proyecto.
- 34 guías revisadas, 20 prácticas —incluidos los ocho IDs de v1.0— y 66 respuestas
  locales en Dudas frecuentes.
- Aprender se organiza en **Rutas | Guías | Prácticas | Dudas | Mi cuaderno**.
- Docente se organiza en **Contenido | Asignaciones | Entregas | Dudas | Progreso**.
- Las prácticas recorren Orientar, Predecir, Preparar, Experimentar, Registrar,
  Analizar, Concluir y Reflexionar, con paso y borrador persistentes en DataStore.
- Los presets pueden abrir Aire o Placa y permiten restaurar la escena anterior.
- Mis dudas conserva contexto, expectativa, observación, estado y respuesta docente.
- Cuaderno por periodo/ruta y exportación de portafolio académico local.
- Intercambio mediante `.soniclabpack.json`, `.soniclabsubmission.json` y
  `.soniclabfeedback.json` usando Storage Access Framework, sin cuentas ni servidor.
- Paquetes `core_acoustics_es` y `unap_2026`; la alineación UNAP es orientativa,
  fechada y declara `officialEndorsement = false`.
- El validador comprueba recuentos, IDs heredados, manifiestos, cobertura y marcadores
  editoriales: `python3 tools/validate_learning_content.py`.

## Cymatics Stage 1.0

- Reloj de oscilación perceptivo desacoplado de la frecuencia física. A 120 Hz, el ajuste
  inicial se muestra aproximadamente a 0,42 ciclos/s en vez de parpadear a 9,6 ciclos/s.
- Placa renderizada en tres capas: espesor inferior, metal modal y filamentos emisivos.
  Las líneas nodales ya no se mezclan como una franja plana sobre el material.
- Escenario profundo bajo la placa con retícula, anillos y pulsos que nacen en la posición
  real del excitador, todos identificados como recursos visuales.
- Arena con asentamiento nodal, microcorrientes permanentes y una pasada adicional de
  hasta 16.000 granos luminosos que recorren tangencialmente los nodos.
- Cámara con inercia y cuatro coreografías —Fija, Flotante, Órbita y Show— que se suspenden
  después de una interacción táctil para respetar el encuadre del usuario.
- Postprocesado por FBO: extracción luminosa, bloom separable a media resolución,
  composición, tone mapping, viñeta y aberración cromática muy contenida.
- Paletas nuevas **Aurora eléctrica** y **Solar dorada**, además de las tres anteriores.
- Ruta directa automática si el framebuffer no está disponible, el dispositivo entra en
  presión térmica o la calidad adaptativa detecta una caída sostenida de rendimiento.
- Perfiles Preciso, Fluido y Cinemático actualizados con escena, cámara, pulsos, corrientes
  y bloom coherentes; cada capa también puede controlarse por separado.
- **Reducir movimiento** detiene oscilación y efectos decorativos y aplica transiciones
  inmediatas sin perder controles ni lectura modal.
- La exportación identifica cámara, luces, escenario, pulsos, corrientes y postprocesado
  como capas visuales; ninguna modifica autovalores, frecuencias, respuesta o acoplamiento.

## Núcleo Cymatics Lab conservado

- Selector visible **Aire | Placa**; Aire continúa siendo el dominio inicial y conserva
  su estado al entrar y salir de Placa.
- Cuatro geometrías modales: cuadrada, circular, triangular equilátera y hexagonal
  regular, con borde simplemente apoyado o sujeto.
- 24 modos por combinación, generados en malla 64 × 64 mediante un operador biarmónico
  reproducible. El manifiesto incluye autovalores, residuales y SHA-256.
- Modelo de placa delgada Kirchhoff–Love con material ideal, espesor, dimensión,
  amortiguamiento, frecuencia y excitador configurables.
- Ajuste a resonancia, modo manual/automático, lectura de sonda y gráfica de respuesta.
- Escena Científica/Espectacular con placa y normales GPU, nodos, antinodos, contornos,
  retícula, tres paletas, cuatro variables y cinco cámaras.
- Arena de 30k, 60k, 120k o 200k animada en GPU hacia destinos nodales deterministas;
  se identifica como visualización cualitativa, no solver granular.
- Cuatro prácticas nuevas dentro de los mismos perfiles Estudiante/Docente.
- Exportación JSON que declara simulación numérica, amplitud `a.u.` y limitaciones.

## Experiencia académica heredada de 0.7

- Perfil **Estudiante** con cuatro actividades guiadas, predicción, pista contextual,
  respuesta, contraste posterior y progreso persistente en el dispositivo.
- Perfil **Docente** con selección de práctica, respuesta esperada, rúbrica editable y
  resultados locales de las entregas.
- Actividad asignada localmente con bloqueos independientes para frecuencia, nivel,
  ambiente y fuentes. Cámara, pausa, calidad GPU y lectura visual siguen disponibles.
- Preparación reproducible de cada práctica sobre el mismo `AcousticState`; no existe
  un motor académico paralelo ni una copia de la escena.
- Exportación JSON del progreso, configuración docente y escena simulada, identificada
  expresamente como simulación analítica y no como medición calibrada.
- Persistencia mediante DataStore, funcionamiento sin cuentas y sin conexión.

## Experiencia heredada de 0.6

- Tres espacios de trabajo con complejidad progresiva: **Explorar**, **Aprender** y
  **Laboratorio**. Cada uno muestra únicamente las secciones pertinentes y conserva
  acceso a todas las herramientas originales.
- Navegación y espacio seleccionados se conservan al rotar la pantalla o recrearse la
  actividad.
- Modo **Enfoque** para ampliar la escena 3D a toda el área útil sin perder los
  controles para volver al laboratorio.
- Barra rápida sobre la escena con Pausar/Continuar, Centrar cámara y Enfoque/Controles.
- Objetivos táctiles de navegación de al menos 48 dp y tipografía base más legible.
- Diseño adaptable a orientación vertical y horizontal, incluido el modo de enfoque.

## Núcleo científico heredado de Science 0.5

- Escena OpenGL ES 3.0 con hasta 600.000 posiciones de equilibrio en VBO y movimiento
  longitudinal calculado por el vertex shader; Kotlin no actualiza cada partícula.
- Corte de presión 3D GPU orientable en XY, XZ o YZ, corte 2D sincronizado y frentes de
  onda resaltables.
- Paletas científica rojo/azul, naranja/cian apta para daltonismo y monocromática.
- Microscopio de partícula con comparación explícita entre desplazamiento físico con
  autozoom y posición dibujada con la amplificación seleccionada.
- Línea temporal precisa con búsqueda por fase, avances de 1° y T/4, pausa y paso por
  fotograma.
- Tres sondas espaciales ajustables con presión, fase, desplazamiento y tiempo ideal de
  propagación entre posiciones.
- Recinto rectangular analítico configurable, modos `(nₓ,nᵧ,n_z)`, nodos, antinodos y
  cálculo de frecuencia modal. No se presenta como un solver de sala arbitraria.
- Dos fuentes para interferencia, sincronización de frecuencia, inversión de fase y
  mapa de nodos mediante el corte de presión.
- Síntesis y visualización de hasta 16 armónicos con preset educativo de voz/AUM.
- Analizador de micrófono opcional con FFT local de 2.048 muestras, frecuencia dominante
  y nivel digital relativo dBFS calculado como RMS después de retirar continua.
- Calibración SPL por comparación con una referencia externa: captura estable de cinco
  segundos, cálculo del offset y perfil persistente en DataStore.
- Perfiles separados por dispositivo, ruta, frecuencia de muestreo, canales, formato y
  fuente de audio. Un cambio de ruta retira el SPL no aplicable y muestra una advertencia.
- Gestión visible para invalidar o borrar perfiles, con fecha, método, referencia, captura
  dBFS y offset conservados como trazabilidad.
- Exportación de experimentos a CSV y JSON mediante el selector de archivos de Android;
  incluye unidades, parámetros, tiempo y nombre del modelo.
- Cuatro prácticas guiadas evaluables con objetivo, procedimiento, pregunta y respuesta.
- Presentación automática para clases y ferias: propagación, corte, interferencia y
  armónicos con cámaras sincronizadas.
- Gráficas de presión-tiempo, desplazamiento-tiempo, presión-distancia y espectro.
- HUD con frecuencia, longitud de onda, nivel, FPS, frame time, partículas y estado
  térmico oficial de Android. No inventa temperatura ni carga de GPU.
- Calidad adaptativa y perfiles Ahorro 30k, Normal 60k, Ultra 120k, Extremo 200k y
  Experimental 600k.
- Audio sintetizado opt-in, apagado al iniciar y con volumen digital bajo.

## Arquitectura

| Área | Implementación |
|---|---|
| Interfaz | Kotlin + Jetpack Compose, adaptable a vertical y horizontal |
| Render 3D | OpenGL ES 3.0, `GL_POINTS`, VAO/VBO y shaders GLSL ES 3.00 |
| Física | Funciones Kotlin puras y muestreo CPU equivalente al shader |
| Simulación | Controlador independiente, reloj visual desacoplado y búsqueda temporal |
| Partículas/corte | Nube generada fuera del hilo GL; desplazamiento y corte masivo en GPU |
| Gráficas | Canvas de Compose sincronizado con el estado físico |
| Audio | `AudioTrack` para síntesis y `AudioRecord` + FFT/RMS para análisis local |
| Calibración | Referencia externa, perfiles estrictos por ruta y persistencia DataStore |
| Cimática | Bancos modales versionados, placa multicapas, arena cualitativa GPU, escenario y bloom por FBO |
| Academia | Seis rutas, 34 guías, 20 prácticas, 66 Dudas, autosave, cuaderno y perfiles Estudiante/Docente |
| Datos | DataStore compatible, portafolio y archivos pack/submission/feedback por SAF; sin servidor |
| Rendimiento | Métricas disponibles, estado térmico oficial y ajuste adaptativo conservador |

OpenGL ES 3.0 continúa siendo el único backend gráfico. Esta versión no usa WebView,
servicios remotos ni Vulkan.

## Modelo y límites científicos

El dominio Aire combina un campo libre analítico regularizado, superposición lineal y un
modelo modal rectangular ideal. No es CFD/FDTD, no calcula difracción compleja,
turbulencia, materiales arbitrarios ni una respuesta real de sala. El micrófono entrega
dBFS por defecto. Solo muestra un valor calibrado en dB SPL cuando existe un perfil
válido que coincide exactamente con la entrada y configuración activas. Ese offset no
certifica el teléfono, no aporta por sí solo incertidumbre trazable y no implica
conformidad IEC 61672 ni equivalencia con un sonómetro de clase 1 o 2. Los puntos
representan volúmenes elementales de aire, no moléculas individuales a escala. Véase
[docs/SCIENTIFIC_MODEL.md](docs/SCIENTIFIC_MODEL.md).

Placa usa un modelo modal numérico de placa delgada idealizada. No mide desplazamiento,
no simula granos de forma predictiva y no asigna una figura universal a una frecuencia.
Véanse [docs/CYMATICS_MODEL.md](docs/CYMATICS_MODEL.md) y
[docs/CYMATICS_VALIDATION.md](docs/CYMATICS_VALIDATION.md).

La evolución hacia uso universitario se registra sin adelantar estados en
[docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md). La auditoría de la
línea base y los casos cuantitativos están en
[docs/BASELINE_AUDIT.md](docs/BASELINE_AUDIT.md) y
[docs/VALIDATION.md](docs/VALIDATION.md). El detalle del motor visual se explica en
[docs/CYMATICS_STAGE_10.md](docs/CYMATICS_STAGE_10.md) y su cierre de entrega en
[docs/DELIVERY_REPORT_CYMATICS_10.md](docs/DELIVERY_REPORT_CYMATICS_10.md).
La lista firmable para una revisión independiente se entrega en
[docs/EXTERNAL_REVIEW_CHECKLIST.md](docs/EXTERNAL_REVIEW_CHECKLIST.md); continúa sin
marcas y no representa una aprobación externa.

## Abrir y compilar en Android Studio

1. Instala Android Studio con JDK 17 y Android SDK 35.
2. Abre la carpeta raíz `SonicLab3D`.
3. Espera a que Gradle termine la sincronización.
4. Selecciona **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. El APK debug queda en `app/build/outputs/apk/debug/app-debug.apk`.

Para distribución usa **Build → Generate Signed Bundle / APK** y firma con tu propio
keystore. La construcción release sin firma queda en
`app/build/outputs/apk/release/app-release-unsigned.apk`.

Desde consola:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
./gradlew assembleRelease
python3 tools/validate_learning_content.py
```

## Estado de verificación

**Estado de la fuente 1.1 en este cierre:** el manifiesto y los activos JSON fueron
validados con el script local: 6 rutas, 34 guías, 20 prácticas, 66 Dudas y los 8 IDs
heredados. La compilación Gradle, pruebas JVM, lint y APK 1.1 todavía deben ejecutarse
en un entorno con Gradle 8.10.2 y Android SDK 35; no se reutilizan como si fueran
resultados de 1.1 las cifras históricas siguientes.

### Última verificación histórica de la base 1.0

La base de Aire conserva su verificación. 1.0 añadió pruebas del reloj perceptivo,
perfiles escénicos, sanitización, migración del codec, accesibilidad y persistencia.

- `testDebugUnitTest`: 54 pruebas, 0 fallos.
- `lintDebug`: correcto, 0 incidencias.
- `assembleDebug`: correcto; APK debug alineada y firmada mediante esquema v2.
- Paquete `com.soniclab3d`, `minSdk 26`, `targetSdk 35`.
- Caso 20 °C / 120 Hz: `c = 343,42 m/s`, `λ = 2,861833 m`,
  `T = 8,333333 ms`.
- Último APK verificado: `10` / `1.0.0-cymatics-stage`.
- Tamaño y SHA-256 finales se registran en
  [docs/DELIVERY_REPORT_CYMATICS_10.md](docs/DELIVERY_REPORT_CYMATICS_10.md).
- Certificado debug SHA-256 `ba179e9879e4c85a84dc22f136ed38f4d46034cbd29a7d8f27c6416566c59af1`,
  igual al APK 0.6 validado, con actualización directa compatible.

La auditoría incremental del 2026-08-10 había quedado pendiente por falta de entorno
Android. El 2026-08-11 se reprodujo con Gradle 8.10.2, JDK 17 y SDK 35: las 19 pruebas
analíticas existentes y las 6 nuevas pruebas de calibración aprobaron, lint no informó
incidencias y el APK se compiló y verificó. Todavía falta una campaña instrumentada en
teléfonos y referencias acústicas físicas; no se confunde una prueba unitaria con una
validación metrológica.

El rendimiento exacto de Placa debe medirse en el teléfono objetivo. No hubo GPU
Android en el entorno de construcción; el presupuesto estático, la ruta adaptativa y
la campaña pendiente se documentan en
[docs/PERFORMANCE_CYMATICS_10.md](docs/PERFORMANCE_CYMATICS_10.md).

## Próximos pasos declarados

Quedan fuera de esta versión: solver FDTD/elementos finitos validado, backend Vulkan,
materiales y geometrías arbitrarias, calibración multipunto en frecuencia con
incertidumbre trazable, conformidad metrológica, exportación de vídeo/PNG, escala de
render dinámica y realidad aumentada. Véase [docs/ROADMAP.md](docs/ROADMAP.md).
