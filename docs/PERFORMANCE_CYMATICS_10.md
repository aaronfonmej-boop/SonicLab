# Rendimiento de Cymatics Stage 1.0

## Presupuesto gráfico

- Malla de placa: 128 × 128 vértices y 96.774 índices, dibujada en dos o tres pasadas.
- Arena: 30k–200k puntos según calidad, con fracción adaptativa entre 25 % y 100 %.
- Polvo: hasta 12.000 puntos aditivos.
- Corrientes nodales: hasta 16.000 puntos aditivos; solo esta pasada evalúa cuatro
  muestras adicionales del campo para estimar la tangente.
- Postprocesado: una escena RGBA8 + profundidad 24 bits a resolución completa y dos
  buffers RGBA8 a media resolución; extracción, cuatro desenfoques y composición.

En una superficie 1080 × 2400, los destinos de postprocesado requieren aproximadamente
26 MB: 10,4 MB para color, 10,4 MB para profundidad y 5,2 MB para los dos buffers de bloom.
Los controladores pueden reservar memoria adicional internamente.

## Degradación controlada

Si el framebuffer no se completa, se usa render directo. Con calidad adaptativa, una
lectura térmica Android de nivel 3 o superior, o FPS sostenidos por debajo de 44, apaga
temporalmente el postprocesado y sigue reduciendo partículas. Bloom vuelve a habilitarse
sobre 57 FPS cuando la fracción adaptativa alcanza al menos 72 %. Al desactivar calidad
adaptativa se respeta el control manual del usuario.

## Verificación disponible

Los diez shaders de Cimática se compilan individualmente y los seis pares de programa se
enlazan con glslang para GLSL ES 3.00. Las pruebas JVM y Android Lint cubren estado,
persistencia, exportación y física, pero no miden una GPU Android real.

## Campaña pendiente en dispositivo

Medir en orientación vertical y horizontal los perfiles Normal, Ultra y Extremo durante
cinco minutos, con bloom activado y desactivado. Registrar FPS, frame time, estado térmico,
resolución, GPU/SoC y cualquier cambio a ruta directa. También revisar visualmente líneas
nodales finas, halos sin clipping, legibilidad del HUD y pausa de cámara tras gestos.
