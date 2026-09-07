# Modelo científico de SonicLab 3D Science 0.9

## Propósito

El modelo está diseñado para explicar propagación acústica lineal en aire de forma
interactiva. Distingue siempre entre cantidades físicas y su representación visual.
No debe usarse como sustituto de un sonómetro calibrado, software de predicción de
salas o un solver numérico validado.

Desde 0.8, el selector **Aire | Placa** separa dos fenómenos: este documento conserva
el detalle de propagación longitudinal en aire; Placa representa vibración transversal
mediante un modelo modal de Kirchhoff–Love. La formulación, validación y límites de
Placa están en [CYMATICS_MODEL.md](CYMATICS_MODEL.md) y
[CYMATICS_VALIDATION.md](CYMATICS_VALIDATION.md).

## Relaciones básicas

Para frecuencia `f`, temperatura `T_C` y velocidad del sonido `c`:

```text
c = 331,3 + 0,606 T_C
λ = c / f
T = 1 / f
ω = 2πf
k = 2π / λ
```

La onda saliente de una fuente puntual en el origen se representa mediante una fase
`kr - ωt + φ`. La singularidad en el origen se evita con un radio efectivo:

```text
r_eff = sqrt(r² + a²)
G(r) = sqrt(1² + a²) / r_eff · exp[-α max(r - 1, 0)]
p(r,t) = p_peak · G(r) · sin(kr - ωt + φ)
```

`a` es el radio regularizador de la fuente y `α` es la absorción atmosférica convertida
de dB/m a nepers/m. El nivel configurado se interpreta a una distancia de referencia
de un metro.

## Nivel de presión sonora

La referencia usada es `p0 = 20 µPa`:

```text
p_rms = p0 · 10^(L_p/20)
p_peak = sqrt(2) · p_rms
L_p = 20 log10(p_rms/p0)
```

Para onda progresiva plana local, la aproximación de baja amplitud es:

```text
u_peak = p_peak / (ρc)
ξ_peak = u_peak / ω
I = p_rms² / (ρc)
```

La app usa estas relaciones localmente con la envolvente esférica. Son útiles para la
enseñanza, pero no incluyen campo cercano reactivo completo de una fuente real.

## Densidad, humedad y absorción

La densidad combina presiones parciales de aire seco y vapor de agua usando la
aproximación de gas ideal. La absorción atmosférica sigue una implementación aproximada
de las expresiones de ISO 9613-1 para tonos puros. La humedad y presión afectan densidad
y absorción; la fórmula de `c` de esta versión depende solo de temperatura, y la
interfaz lo declara explícitamente.

## Superposición y armónicos

Dos fuentes se suman linealmente:

```text
p_total(x,t) = p_A(x,t) + p_B(x,t)
```

El modo voz/AUM suma hasta dieciséis componentes:

```text
p_total(r,t) = Σ A_n p_peak G_n(r) sin(k_n r - ω_n t + φ)
f_n = n f_1
```

Las formas triangular, cuadrada y pulso se aproximan mediante series finitas. El
espectro de síntesis y el analizador de micrófono son rutas separadas.

## Recinto rectangular ideal

El panel Recinto usa modos propios analíticos con paredes rígidas ideales:

```text
f_n = c/2 · sqrt[(n_x/L_x)² + (n_y/L_y)² + (n_z/L_z)²]
p_n(x,y,z) ∝ cos(n_xπx/L_x) cos(n_yπy/L_y) cos(n_zπz/L_z)
```

Este modelo permite estudiar nodos y antinodos, pero no calcula absorción de paredes,
geometrías irregulares, difracción ni respuesta impulsional de una sala real.

## Micrófono, FFT y calibración por referencia externa

La entrada opcional usa una ventana Hann y una FFT de 2.048 muestras a 48 kHz. El
nivel digital se obtiene como RMS de las muestras después de retirar la componente
continua y se expresa en dBFS. Sin un perfil aplicable, esta es la única lectura de
nivel que se muestra.

Durante una calibración, la app registra cinco segundos de una fuente estable cuyo
nivel `L_ref` es introducido por el usuario a partir de un equipo externo. Si la señal
no está saturada, reúne al menos 20 análisis y su desviación estándar no supera
1,5 dB, se calcula:

```text
L_dBFS,cal = promedio de las lecturas dBFS de la captura
Delta_cal = L_ref - L_dBFS,cal
L_SPL,estimado = L_dBFS,RMS + Delta_cal
```

El perfil conserva referencia, lectura capturada, offset, método, fecha y metadatos
de entrada. Solo se considera aplicable cuando coinciden identificador y tipo del
dispositivo, nombre/dirección informados por Android, frecuencia de muestreo, canales,
codificación y fuente de audio. Al cambiar la ruta, la app retira el valor SPL anterior
y mantiene dBFS hasta encontrar o crear un perfil exacto y válido.

Esta calibración de un solo punto no caracteriza la respuesta en frecuencia, la
orientación, el ruido propio ni el procesamiento del dispositivo; tampoco estima una
incertidumbre trazable. No demuestra conformidad IEC 61672 ni equivalencia con un
sonómetro clase 1 o clase 2.

## Partículas y escala visual

Cada posición de equilibrio `x0` permanece almacenada en GPU. El shader calcula:

```text
x_física(t) = x0 + ξ(t)
x_dibujada(t) = x0 + clamp(S_visual · ξ(t))
```

`S_visual` puede ser cientos de miles de veces mayor que uno, porque el desplazamiento
acústico ordinario suele ser microscópico. La ficha de la partícula informa la posición
física sin esa amplificación; el render informa por separado la escala visual.

## Supuestos y exclusiones actuales

- Medio homogéneo, isotrópico, lineal y en reposo.
- Escena de fuente idealizada y campo libre sin obstáculos.
- Recinto modal rectangular ideal separado de la escena de campo libre.
- Sin reflexión arbitraria, difracción, convección, turbulencia ni no linealidad.
- Sin directividad ni impedancia compleja de transductores reales.
- Sin calibración del altavoz ni caracterización multipunto del micrófono/dispositivo.
- El SPL estimado depende de un perfil externo válido para la ruta exacta.
- Los puntos representan volúmenes elementales, no moléculas individuales a escala.
- La calidad adaptativa modifica densidad gráfica, no la física simulada.
- Los patrones de Placa no son geometrías universales por frecuencia: dependen de
  forma, borde, dimensiones, propiedades, amortiguamiento, modo y excitador.
- La arena de Placa es acumulación nodal cualitativa, no dinámica granular predictiva.

## Pruebas automáticas

Las pruebas cubren las cuatro relaciones fundamentales, el caso 20 °C/120 Hz,
conversiones SPL, rango de densidad, tendencia de absorción, dependencia del
desplazamiento con la frecuencia, cancelación de dos fuentes simétricas, rechazo de
frecuencia cero, modos rectangulares y formato reproducible de exportación.

Seis pruebas adicionales cubren captura estable y cálculo de offset, rechazo de una
captura inestable, rango de referencia, aplicación solo a ruta/configuración exacta,
cancelación por cambio de ruta y persistencia reversible del perfil mediante codec.

`AcousticValidationTest.kt` añade referencias numéricas independientes para una onda
progresiva local a 1 m, fuente esférica regularizada, atenuación a 4 m, distancia casi
nula y un modo tangencial rectangular. Ecuaciones, tolerancias, resultados parciales
y controles Android se documentan en [VALIDATION.md](VALIDATION.md).
