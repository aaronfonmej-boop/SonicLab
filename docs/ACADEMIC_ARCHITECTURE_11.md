# Arquitectura académica · SonicLab 3D 1.1

## Alcance

Academia Continua extiende la única aplicación `com.soniclab3d`; no crea otro motor,
otra Activity ni una copia de Aire/Placa. Compose consume el mismo `AcousticState` y
`CymaticsState`, y los presets son transformaciones controladas sobre esos estados.

## Componentes

| Componente | Responsabilidad |
| --- | --- |
| `LearningModels.kt` | Rutas, niveles, guías, prácticas, Dudas, borradores, cuaderno, asignaciones, entregas y feedback. |
| `LearningCatalog.kt` | Catálogo tipado de 34 guías y 20 prácticas; conserva los ocho IDs v1.0. |
| `LearningFaqCatalog.kt` | Biblioteca local de 66 respuestas curadas. |
| `LearningPresetResolver.kt` | Enrutamiento separado de presets de Aire y Placa. |
| `LearningPersistenceCodecs.kt` | Codecs puros, limitados y versionados para DataStore. |
| `LearningExchangeCodec.kt` | Pack, entrega y feedback JSON; valida tamaño, esquema, IDs y campos prohibidos. |
| `AcademicExperienceRepository.kt` | Único repositorio de progreso; lee claves v1/v2 y añade claves v3. |
| `ContinuousLearningPanel.kt` | Cinco destinos de Estudiante y cinco de Docente. |
| `content_manifest.json` | Índice versionado, recuentos, rutas e IDs estables. |
| `validate_learning_content.py` | Verificación independiente de manifiestos y catálogo fuente. |

## Flujo de datos

1. La UI observa un `Flow<AcademicExperienceState>` de DataStore.
2. Cada edición de práctica se conserva como `PracticeDraft` después de una pausa breve.
3. El paso activo se guarda por ID; una recreación vuelve al mismo paso y al mismo intento.
4. Una entrega marca el ID heredado/nuevo como completado y mantiene el borrador asociado.
5. Los presets cambian la escena real; antes se guarda una instantánea en memoria y se
   ofrece **Volver a mi escena**.
6. Los intercambios usan Storage Access Framework y nunca rutas de almacenamiento amplias.

## Persistencia y migración

Las claves v1 (`role_v1`, `completed_practices_v1`, `student_answers_v1` y actividad de
Aire) y v2 (actividad de Cimática) se mantienen. v3 añade rutas, guías, fases,
borradores, Dudas, cuaderno, asignaciones, entregas importadas y feedback. No se
reutiliza una clave antigua con otro significado.

Los IDs `wave_particle`, `frequency_wavelength`, `interference`, `harmonics`,
`cymatics_nodes`, `cymatics_resonance`, `cymatics_geometry` y `cymatics_coupling`
siguen siendo las claves de progreso originales.

## Decisiones y desviaciones pendientes

El contenido completo permanece tipado en Kotlin para evitar incorporar una dependencia
de serialización no verificable en este entorno. Los activos JSON funcionan como
manifiesto y paquetes curriculares, no como repositorio de cada párrafo. Una futura
migración a JSON por guía debe conservar exactamente los IDs, pasar el mismo validador y
probar equivalencia antes de retirar el catálogo Kotlin.

La instantánea para restaurar la escena sobrevive recomposiciones, pero no una muerte de
proceso. Los textos de práctica sí están en DataStore. No se guardan imágenes grandes en
DataStore.

## Seguridad de archivos

Los documentos académicos se limitan a 1 MB, esquema 1, `appId=com.soniclab3d`, IDs con
alfabeto restringido y prácticas incorporadas conocidas. Se rechazan `script`,
`expectedAnswer` y rúbricas privadas en paquetes de estudiante. La importación nunca
ejecuta contenido.
