# Roadmap científico y técnico

Este orden evita añadir espectáculo visual a costa de exactitud o estabilidad.

## Academia Continua 1.1 — fuente integrada, cierre Android pendiente

- Seis rutas, tres niveles, 34 guías, 20 prácticas y 66 Dudas: integrados y validados
  por manifiesto; pendiente compilación/lint en Android.
- Autosave de paso, predicción, evidencia, análisis y reflexión: integrado en DataStore.
- Cuaderno, dudas contextuales, asignación local y feedback: integrados.
- Paquete/entrega/feedback por SAF con esquema y límite de tamaño: integrado; pendiente
  prueba round trip en dispositivo.
- Pendiente para una fase posterior: constructor libre de secuencias, comparación de
  entradas, backup/restore completo, evaluadores numéricos tipados y auditoría Compose.

## Cymatics Lab — núcleo 1.0 implementado

- Selector Aire/Placa, estado y renderer separados: implementado.
- Cuatro geometrías, dos bordes, 24 modos por banco y generador reproducible:
  implementado con validación numérica documentada.
- Placa Científica/Espectacular, sonda, contornos y arena cualitativa GPU: implementado.
- Cuatro prácticas Estudiante/Docente, bloqueos y exportación: implementado.
- Morph interpolado, cámara inercial, luz reactiva y arena orgánica GPU: implementado
  en 0.9 con ruta accesible de movimiento reducido.
- Reloj perceptivo, placa multicapas, escenario, pulsos, corrientes nodales, cámara Show
  y bloom opcional por FBO: implementado en 1.0 con respaldo directo adaptativo.
- Pendiente: campaña Android instrumentada, comparación con placa física, transform
  feedback opcional y exportación CSV de barrido.

## Fase 1 — Validación de campo

- Probar en varios SoC Adreno, Mali y Xclipse, a 60/90/120 Hz.
- Registrar FPS, percentiles de frame time, consumo de memoria y thermal throttling.
- Añadir pruebas instrumentadas de gestos, rotación, pausa, audio y recreación de la
  Activity.
- Comparar presión, fase e interferencia con soluciones analíticas de referencia:
  suite cuantitativa aprobada en Gradle/JVM; pendiente verificación GPU y hardware.
- Crear un modo demostración reproducible para clases y ferias.

## Fase 2 — Herramientas científicas · parcialmente completada en 0.4

- Sondas espaciales múltiples y línea temporal medible: implementado.
- Exportación CSV/JSON con parámetros, unidades, modelo y tiempo: implementado.
- Exportación PNG de alta resolución: pendiente.
- Incertidumbre, cifras significativas y panel de supuestos del experimento.
- Prácticas con perfiles Estudiante/Docente, progreso, rúbricas y bloqueos locales:
  implementado en 0.7; aula remota y manual docente independiente pendientes.
- Paletas aptas para daltonismo: implementado; auditoría completa de lector de pantalla pendiente.
- Localización completa y manual del docente.

## Fase 3 — Representaciones del campo · parcialmente completada en 0.4

- Frentes y cortes 2D/3D de presión: implementado.
- Flechas de velocidad/desplazamiento con muestreo espacial controlado.
- Volumen de densidad mediante texturas 3D o ray marching escalable.
- Selección GPU precisa y sondas que no dependan de la densidad visible.
- FBO con render scale 50/75/100/125 %, MSAA y captura de alta resolución.

## Fase 4 — Recintos y ondas estacionarias · base analítica completada

- Recinto rectangular con modos analíticos y condiciones declaradas: implementado.
- Nodos, antinodos y frecuencias propias: implementado.
- Reflexiones por imagen-fuente para demostraciones simples.
- Después, solver FDTD/elementos finitos GPU con capa absorbente y pruebas de
  convergencia; nunca etiquetarlo como exacto sin validación.
- Mantener separada la acústica del aire de la cimática de placas.

## Fase 5 — Medición y laboratorio real · calibración por referencia implementada

- Entrada de micrófono opcional con consentimiento: implementado.
- FFT Hann y nivel digital relativo RMS: implementado; incertidumbre/saturación avanzada pendiente.
- Calibración por referencia externa con captura estable de 5 s y offset: implementado.
- Perfiles persistentes y estrictos por dispositivo, ruta y configuración: implementado.
- Invalidación ante ruta no coincidente y gestión de perfiles: implementado.
- Ensayos físicos multipunto, incertidumbre trazable y conformidad normativa: pendientes.
- Comparación señal sintetizada/medida y respuesta impulsional.
- Compatibilidad con micrófonos USB calibrados y exportación de metadatos.
- No mostrar dB SPL absolutos sin una calibración válida.

## Fase 6 — Motor visual avanzado · parcialmente completada en 1.0

- Morph modal, cuatro coreografías de cámara, iluminación orbital, aura de resonancia,
  partículas, escenario y corrientes separadas del dato científico: implementado.
- Bloom y composición con tone mapping mediante FBO: implementados como ruta opcional
  con desactivación térmica/FPS y respaldo directo. HDR real en punto flotante y trails
  temporales permanecen pendientes.
- Backend Vulkan o compute cuando el solver/volumen lo requiera y exista una ruta
  OpenGL estable de compatibilidad.
- Escalado dinámico por tiempos de GPU cuando la API del dispositivo lo exponga de
  forma fiable.
- Realidad aumentada para colocar cortes del campo en un aula o laboratorio.

## Requisitos para uso universitario

- Revisión por un especialista externo en acústica; checklist disponible, revisión y firma pendientes.
- Documento de validación con errores frente a casos analíticos.
- Versionado del modelo y resultados reproducibles.
- Licencia, política de privacidad, accesibilidad y mantenimiento definidos.
- Material didáctico que diferencie observación, aproximación y medición.
