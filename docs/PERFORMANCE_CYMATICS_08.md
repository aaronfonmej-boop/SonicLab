# Rendimiento de Cymatics Lab 0.8

## Ruta de render

- OpenGL ES 3.0, sin WebView, motor remoto ni Vulkan.
- Malla de placa 128 × 128; deformación y normales en vertex shader.
- Campo modal `RG32F` de 64 × 64 con interpolación bilineal explícita mediante
  `texelFetch`. El filtro GL se mantiene en `NEAREST` para no depender de extensiones
  de filtrado lineal de float.
- Arena como puntos GPU. Kotlin genera destinos solo cuando cambia banco, modo, calidad
  o reinicio; la animación masiva ocurre en el vertex shader.
- Un worker serial carga bancos y genera nubes fuera de UI/GL.
- Caché compartida de bancos para evitar duplicar los activos entre panel y renderer.
- Ajuste adaptativo por FPS real y estado térmico oficial de Android.

La ruta informada por el HUD es `fallback nodal determinista + animación GPU`. No se
afirma que exista transform feedback en esta versión.

## Presupuesto estático aproximado

| Recurso | Tamaño aproximado |
|---|---:|
| VBO de malla, 16.384 UV | 128 KiB |
| EBO de malla, 96.774 índices | 378 KiB |
| Textura modal RG32F 64 × 64 | 32 KiB |
| Banco activo: 24 campos + máscara | 400 KiB |
| Arena Ahorro 30k, 24 bytes/grano | 0,69 MiB |
| Arena Normal 60k | 1,37 MiB |
| Arena Ultra 120k | 2,75 MiB |
| Arena Extremo/Experimental limitada a 200k | 4,58 MiB |

Durante una regeneración de arena existe temporalmente una copia JVM y un buffer
directo de transferencia. No se crean objetos por grano. La calidad Experimental sigue
siendo 600k en Aire; en Placa se limita honestamente a 200k.

## Calidad adaptativa

Si el estado térmico de Android alcanza nivel 3 o los FPS bajan de 54, el renderer
reduce gradualmente la fracción de arena hasta un mínimo de 25 %. Si supera 59 FPS la
recupera por pasos. La malla y el campo modal no pierden resolución; cambia solo la
densidad visual de arena. El HUD informa cantidad dibujada/configurada, FPS, frame time
y estado térmico, sin inventar porcentaje de GPU ni temperatura interna.

## Ciclo de vida

Al abandonar Placa se encola la eliminación de programas, VAO, VBO, EBO y textura en
el hilo GL, se pausa `GLSurfaceView` y se detiene el worker. Al volver se crea un nuevo
contexto de Cimática y se reutiliza el estado controlado. Aire conserva su propio estado
y renderer; los shaders y el movimiento de partículas de Aire no fueron modificados.

## Resultados disponibles

La construcción local verificó compilación Kotlin/Java, empaquetado de shaders y activos,
tests JVM y lint. El APK incluye los ocho bancos y cuatro shaders de Cimática.

No se registran cifras inventadas de rendimiento: el entorno no expuso una GPU Android
ni el Xiaomi objetivo. Quedan pendientes FPS promedio, P90/P99, memoria de driver,
temperatura, throttling, pausa/reanudación y pruebas a 60/90/120 Hz en hardware. La
primera campaña recomendada debe usar Normal 60k y Extremo 200k en un Xiaomi 15T, y
después cubrir al menos un Mali y un Xclipse desde API 26.
