# Motor visual Cymatics Stage 1.0

## Objetivo

Esta revisión cambia la lectura visual de Placa sin sustituir el modelo modal. El banco,
los autovalores, las frecuencias propias, el acoplamiento y la respuesta normalizada se
calculan igual que antes. Escenario, pulsos, corrientes, cámara y bloom son capas de
presentación explícitamente cualitativas.

## Relojes separados

La frecuencia de excitación conserva su valor físico para seleccionar el modo y calcular
la respuesta. El desplazamiento dibujado usa un reloj perceptivo logarítmico de 0,34 a
0,66 ciclos/s a velocidad visual nominal. Esto evita alias temporal y parpadeo cuando la
excitación está en el rango audible. El control Ritmo visual escala ese reloj y el HUD
muestra los ciclos/s efectivos.

## Orden de render

1. Escenario inferior con retícula, anillos y ondas radiales desde el excitador.
2. Cara inferior desplazada 0,15 unidades para dar espesor a la placa.
3. Superficie modal metálica con iluminación primaria y secundaria.
4. Pasada aditiva elevada para filamentos nodales y pulsos de energía.
5. Polvo atmosférico, arena principal y corrientes nodales tangenciales.
6. Extracción luminosa, dos desenfoques separables y composición final.

La pasada de corrientes evalúa el gradiente modal alrededor de cada objetivo nodal. Su
tangente se obtiene como el vector perpendicular al gradiente; el movimiento queda
acotado a una vecindad pequeña de la línea. La arena principal conserva los objetivos
deterministas y añade únicamente microcorrientes y saltos visuales después del
asentamiento.

## Postprocesado y respaldo

La escena usa una textura RGBA8 de resolución completa y un depth renderbuffer de 24 bits.
El bloom usa dos texturas RGBA8 a media resolución. Si algún framebuffer queda incompleto,
el renderizador vuelve a la ruta directa. Con calidad adaptativa, bloom se desactiva bajo
44 FPS o presión térmica y vuelve cuando el rendimiento se estabiliza sobre 57 FPS con
presupuesto de partículas suficiente.

## Accesibilidad

Reducir movimiento congela los relojes de oscilación, cámara y decoración, y completa
los morph inmediatamente. La cámara también suspende su coreografía después de tocar,
rotar, desplazar o ampliar la escena. El perfil Preciso usa cámara fija y desactiva
corrientes, pulsos y postprocesado.

## Alcance científico

Los filamentos muestran ceros del campo modal interpolado. Los pulsos no son una
simulación de propagación elástica transitoria, las partículas no constituyen un solver
granular y el bloom no representa energía medida. La exportación JSON marca cada uno de
estos controles con el sufijo `VisualOnly`.
