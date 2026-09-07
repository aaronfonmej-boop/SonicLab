# Informe de entrega · Mejora 01

Fecha: 2026-08-10  
Objetivo: incorporar una base cuantitativa independiente para evaluar el modelo
analítico antes de iniciar calibración, FDTD u otras mejoras físicas de mayor riesgo.

## Estado de la entrega

La mejora queda **en curso**. El núcleo puro aprobó los casos numéricos, pero el
entorno no permitió ejecutar Gradle/JVM, lint ni generar el APK. Por esa razón se
conservan `versionCode 4` y `versionName 0.4.0-field-lab`.

El ZIP de origen no incluía `.git`; no se crearon ni se inventaron commits.

## Archivos incorporados

- `app/src/test/java/com/soniclab3d/physics/AcousticValidationTest.kt`
- `docs/BASELINE_AUDIT.md`
- `docs/DELIVERY_REPORT_VALIDATION_01.md`
- `docs/IMPLEMENTATION_STATUS.md`
- `docs/VALIDATION.md`

## Archivos actualizados

- `app/src/main/java/com/soniclab3d/physics/AcousticPhysics.kt`
- `README.md`
- `docs/ROADMAP.md`
- `docs/SCIENTIFIC_MODEL.md`

## Decisiones técnicas

- No se cambiaron las ecuaciones productivas ni el shader. Se documentó de forma
  explícita la convención de fase y la relación presión–velocidad–desplazamiento.
- Los valores esperados de la nueva suite están precomputados y no llaman a
  `metrics()`, `soundPressurePeakPa()` ni
  `atmosphericAbsorptionDbPerM()` para construir el resultado esperado.
- Se mantuvieron separados el desplazamiento físico y la escala visual.
- No se modificó el perfil Normal 60k, el render, el audio, los permisos, el paquete,
  la firma ni la licencia.
- La versión no se incrementó porque no se obtuvo un APK verificable.

## Ecuaciones y métodos afectados

No hubo cambios numéricos en el modelo. La nueva validación cubre:

- `c`, longitud de onda, periodo, `omega` y `k` a 20 °C y 120 Hz;
- onda progresiva local a 1 m;
- fuente esférica con radio regularizador;
- absorción de amplitud después de 1 m;
- presión, velocidad y desplazamiento de partícula;
- centro de fuente y distancia casi nula;
- frecuencia y forma espacial del modo rectangular `(1,1,0)`.

## Pruebas y resultados reales

| Control | Resultado |
|---|---|
| Compilación del núcleo puro con Kotlin/JS 2.4.10, lenguaje 2.0 | Correcta |
| Comparación numérica independiente | 15/15 magnitudes dentro de tolerancia |
| Compilación sintáctica de `AcousticValidationTest.kt` con API JUnit sustituta | Correcta; no equivale a ejecución JUnit |
| `testDebugUnitTest` | No ejecutado: Gradle 8.10.2 no disponible ni descargable |
| `lintDebug` | No ejecutado por el mismo bloqueo |
| `assembleDebug` | No ejecutado; APK no generado |
| `androidTest` | No ejecutado: sin SDK, emulador ni dispositivo |

## Rendimiento y cambios visuales

- No se modificaron `ParticleRenderer`, VBO, shader, perfiles de partículas ni rutas
  por frame; no se espera una regresión de Normal 60k por estos cambios de pruebas y
  documentación.
- No se informa FPS antes/después porque no hubo dispositivo real.
- `metrics()` no cambió y sus valores de referencia permanecen iguales.
- No hubo cambios de interfaz; las capturas antes/después no aplican.

## Riesgos y limitaciones

- Falta confirmar el comportamiento con el backend Kotlin/JVM de la app.
- Falta una prueba automatizada de equivalencia Kotlin/GLSL y ejecución en GPU real.
- El caso de absorción comprueba la integración del coeficiente actual, no reemplaza
  una revisión normativa externa.
- La fuente sigue siendo idealizada y no modela el campo cercano reactivo completo.
- No se ha validado SPL absoluto ni hardware de audio.

## Pasos manuales pendientes

1. Abrir el proyecto con Android Studio, JDK 17 y SDK 35.
2. Ejecutar `./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug`.
3. Archivar informes, hash y tamaño del APK.
4. Probar Normal 60k en hardware identificado y registrar FPS y frame time.
5. Revisar ecuaciones y convenciones con un especialista sin marcar una aprobación
   antes de recibirla.
