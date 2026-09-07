# Rendimiento de Cymatics Motion 0.9

## Ruta gráfica

- OpenGL ES 3.0 y GLSL ES 3.00; sin WebView, Vulkan, FBO ni posprocesado.
- Placa 128 × 128 con deformación y normales en vertex shader.
- Dos texturas modales `RG32F` de 64 × 64. La textura anterior solo se muestrea
  mientras `uModeBlend` está entre 0 y 1; terminada la transición, el shader toma la
  ruta de una textura.
- Dos VAO/VBO de arena permiten fundir una nube saliente y una entrante. El doble draw
  termina en un máximo configurable de 0,8 s.
- Hasta 12.000 puntos ambientales reutilizan el VBO activo y se dibujan en una pasada
  aditiva. No existe una simulación ni una asignación por partícula adicional.
- Cámara, luz, rutas de arena, brillo y partículas se resuelven en GPU o con un número
  constante de operaciones por frame; Kotlin no recorre la nube durante la animación.

## Presupuesto estático aproximado

| Recurso | Tamaño máximo aproximado |
|---|---:|
| Malla de placa + índices | 506 KiB |
| Dos texturas modales RG32F | 64 KiB |
| Banco modal activo | 400 KiB |
| Dos VBO de arena Ahorro 30k | 1,37 MiB |
| Dos VBO de arena Normal 60k | 2,75 MiB |
| Dos VBO de arena Ultra 120k | 5,49 MiB |
| Dos VBO de arena 200k | 9,16 MiB |

Durante la generación existe temporalmente una nube JVM y un buffer directo de
transferencia. Al interrumpir un morph se crea un arreglo de solo 32 KiB para fijar el
campo visible intermedio; no ocurre por frame.

## Contención de coste

- La calidad adaptativa conserva el mínimo de 25 % y reduce arena por FPS o estado
  térmico oficial de Android.
- La pasada ambiental sigue la misma fracción adaptativa y nunca supera 12k puntos.
- El perfil Preciso desactiva cámara flotante, luz orbital, aura y ambiente.
- `Reducir movimiento` fija intensidad decorativa en cero, omite ambiente y completa
  inmediatamente los fundidos.
- La máscara, resolución modal y malla no se degradan; solo cambia densidad visual de
  puntos.

## Verificación disponible y pendiente

Los cuatro shaders compilan y enlazan con glslang para GLSL ES 3.00. Gradle verifica
Kotlin, empaquetado, pruebas JVM y lint. El entorno no expone una GPU Android, por lo
que no se publican cifras inventadas de FPS, memoria de driver, consumo o temperatura.

La campaña pendiente debe medir perfiles Preciso/Cinemático con 60k, 120k y 200k,
cambios rápidos de modo, orientación, pausa/reanudación y 60/90/120 Hz en el Xiaomi
objetivo, seguida de al menos un Mali y un Xclipse desde API 26.
