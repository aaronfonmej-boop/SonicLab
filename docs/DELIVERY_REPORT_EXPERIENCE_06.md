# Informe de entrega · Experience 0.6

Fecha: 2026-08-12  
Versión candidata: `6` / `0.6.0-experience-lab`  
Base: SonicLab 3D Science `0.5.0-calibration-lab`

## Objetivo

Reducir la carga cognitiva de una interfaz con nueve secciones contiguas y mejorar el
uso táctil en teléfono, conservando toda la funcionalidad científica existente.

## Cambios implementados

1. **Explorar** agrupa Control, Campo y Fuentes para manipular la simulación con la
   menor cantidad de decisiones iniciales.
2. **Aprender** agrupa Prácticas, Ciencia, Partícula y Recinto para estudiar el fenómeno
   mediante una secuencia pedagógica.
3. **Laboratorio** agrupa Analizador, Herramientas, Campo, Recinto y Fuentes para
   medición, calibración, exportación y trabajo reproducible.
4. El espacio y la sección activos usan `rememberSaveable`, por lo que sobreviven a
   rotaciones y recreaciones de la actividad.
5. El modo Enfoque amplía la escena OpenGL al área completa y permite regresar mediante
   el mismo control contextual.
6. La barra rápida de escena expone Pausar/Continuar, Centrar y Enfoque sin obligar al
   usuario a desplazarse por el panel.
7. Las pestañas tienen una altura táctil mínima de 48 dp y la escala tipográfica base
   aumenta para facilitar la lectura.

## Compatibilidad y alcance

- No se cambiaron `AcousticPhysics`, `AcousticState`, `RoomAcoustics`, audio, medición,
  exportación, renderer, shaders ni archivos de pruebas.
- Los nueve paneles anteriores permanecen disponibles; solo cambia su organización.
- El modo Enfoque reutiliza el mismo `ParticleGLSurfaceView` y el mismo
  `SimulationController`.
- El modelo científico continúa siendo Science 0.5; Experience 0.6 identifica la capa
  de interacción y navegación.

## Verificación

- Análisis sintáctico Kotlin de `SonicLabApp.kt`: sin nodos de error.
- Inspección estructural de todas las rutas de navegación: los nueve paneles están
  asignados al menos a un espacio.
- Compilación Gradle, lint y APK 0.6: pendientes por falta local de Gradle 8.10.2 y
  Android SDK 35 en el entorno de edición sin acceso de descarga.

La candidata no debe presentarse como APK verificado hasta aprobar
`testDebugUnitTest`, `lintDebug` y `assembleDebug`.
