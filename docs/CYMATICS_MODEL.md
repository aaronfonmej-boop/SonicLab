# Modelo científico de Cymatics Lab 1.0

## Alcance

Cymatics Lab representa la vibración transversal de una placa delgada idealizada. Es
un dominio separado de la propagación longitudinal en aire de SonicLab. Los resultados
son una **simulación numérica modal**, no una medición de una placa real.

La interfaz usa `a.u.` para amplitud, acoplamiento y lectura de sonda porque no existe
una fuerza excitadora calibrada ni una cadena trazable de desplazamiento. La escala de
relieve modifica únicamente el dibujo.

## Formulación

La referencia conceptual es la placa de Kirchhoff–Love homogénea, isotrópica, lineal y
de espesor constante:

```text
D ∇⁴w(x,y,t) + cᵈ ∂w/∂t + ρh ∂²w/∂t² = q(x,y,t)
D = E h³ / [12 (1 − ν²)]
```

`E` es el módulo de Young, `ν` la razón de Poisson, `ρ` la densidad, `h` el espesor,
`cᵈ` un amortiguamiento equivalente y `q` la excitación externa. La app no afirma que
el material configurable corresponda a un grado comercial concreto.

La respuesta mostrada utiliza una forma modal a la vez:

```text
w(x,y,t) = Aᵢ φᵢ(x,y) sin(2π f_drive t)
Hᵢ(r,ζ) = 1 / sqrt[(1 − r²)² + (2ζr)²]
r = f_drive / f_mode
Aᵢ,normalizada = clamp(2ζ Hᵢ · |φᵢ(x_drive,y_drive)| · F_normalizada, 0, 1)
```

En selección automática se maximiza `Hᵢ · |φᵢ(x_drive,y_drive)|` entre los 24 modos
del banco. En selección manual se conserva el modo elegido aunque la respuesta sea
débil. Si la excitación queda fuera del intervalo modal, la interfaz lo advierte; no
sustituye el campo por una figura ornamental.

La conversión del autovalor adimensional `λᵢ` a frecuencia usa:

```text
fᵢ = [4 / (2π L²)] sqrt[D/(ρh)] sqrt(λᵢ)
```

El factor 4 corresponde al mapeo de la coordenada normalizada `[-1,1]` a la dimensión
característica `L`. Las máscaras geométricas ocupan una fracción documentada de ese
dominio; por ello `L` debe interpretarse como dimensión característica del modelo y no
como una medición automática de una placa física.

## Bancos modales

`tools/generate_cymatics_modes.py` genera ocho bancos deterministas:

| Geometría | Condiciones de borde | Modos | Malla |
|---|---|---:|---:|
| Cuadrada | apoyada / sujeta | 24 por borde | 64 × 64 |
| Circular | apoyada / sujeta | 24 por borde | 64 × 64 |
| Triangular equilátera | apoyada / sujeta | 24 por borde | 64 × 64 |
| Hexagonal regular | apoyada / sujeta | 24 por borde | 64 × 64 |

Para borde simplemente apoyado se usa `K = L_DᵀL_D`, con desplazamiento cero fuera
del dominio discreto. Para borde sujeto se añade una penalización de pendiente en el
primer anillo interior. Esta última es una aproximación de malla finita declarada, no
una caracterización experimental de una mordaza real.

Cada campo se normaliza con `max(abs(φ)) = 1`; el signo se fija en la celda de mayor
magnitud. El formato binario big-endian `CYM8` guarda versión, malla, cantidad de modos,
geometría, borde, autovalores, máscara y campos. `mode_manifest.json` incluye modelo,
generador, método, normalización, residuales, autovalor de cada modo y SHA-256.

Regeneración:

```bash
python3 tools/generate_cymatics_modes.py \
  --output app/src/main/assets/cymatics --grid 64 --modes 24
```

NumPy y SciPy solo se usan fuera de Android para crear los activos. El APK no contiene
ni ejecuta esos solvers.

## Render y codificación visual

La malla 128 × 128 se deforma en el vertex shader a partir de la textura modal. Las
normales también se derivan en GPU. Los estilos Científico y Espectacular comparten el
mismo campo y cambian solo paleta, iluminación y material visual.

Variables disponibles:

- desplazamiento transversal normalizado;
- velocidad transversal normalizada;
- fase modal;
- energía modal relativa.

Nodos, antinodos, contornos, malla, excitador y sonda son capas independientes. Las
paletas cian/magenta, naranja/azul y monocromática no atribuyen un color físico a una
frecuencia.

## Sistema de movimiento y escena 1.0

Al cambiar de modo, el renderer conserva dos texturas `RG32F` y mezcla campo y máscara
con una curva `smootherstep`. Si llega otro modo antes de terminar, calcula una única
vez el campo intermedio visible de 64 × 64 y lo usa como origen del siguiente morph.
Las métricas CPU pasan inmediatamente al modo seleccionado; la interpolación es una
transición de presentación, no una combinación física de autovectores.

La fase dibujada usa un reloj perceptivo logarítmico desacoplado de la frecuencia física.
La frecuencia audible sigue seleccionando el modo y calculando respuesta/acoplamiento;
el reloj visual evita alias y parpadeo y se exporta por separado en ciclos/s.

La intensidad de resonancia que alimenta brillo y partículas se limita a `[0,1]` y se
deriva exclusivamente de respuesta modal, acoplamiento y amplitud normalizada. La
cámara, las luces orbitales, el halo, el escenario, los pulsos, las corrientes, el bloom
y las partículas ambientales se declaran **solo visuales**. No alteran `φ`, `λ`, `fᵢ`,
`Hᵢ` ni la sonda.

Los perfiles Preciso, Fluido y Cinemático configuran estas capas. `Reducir movimiento`
fija su intensidad efectiva en cero y hace inmediato el morph, incluida la transición
de arena. La cámara manual, las capas científicas y los datos continúan disponibles.

## Arena

La ruta estable conserva el fallback permitido por el diseño: al cambiar modo o
calidad se calculan fuera del hilo UI destinos nodales deterministas; cada grano se
anima y se desplaza en el vertex shader. 1.0 conserva la curva lateral acotada, el salto
granular visual y el fundido entre dos VBO durante la sustitución del patrón, y añade
microcorrientes permanentes. Una pasada limitada a 16.000 puntos estima el gradiente
modal y desplaza granos luminosos por su tangente cerca de los nodos. No hay
objetos Kotlin ni actualizaciones CPU por partícula en cada frame. La velocidad de acumulación depende de la amplitud modal,
pero mantiene una relajación visual mínima para evitar una escena inmóvil.

La etiqueta normativa de la app y del JSON es:

> Visualización cualitativa de acumulación nodal; no es un solver granular predictivo.

No se modelan contactos, fricción granular, rebote físico, masa de grano ni fuerzas en
newtons. Las posiciones objetivo no deben usarse para predecir un experimento real.

## Límites

- Placa delgada, lineal, homogénea e isotrópica; sin tensiones iniciales ni no linealidad.
- 24 modos por banco; no hay geometría arbitraria ni refinamiento adaptativo.
- Condición sujeta aproximada mediante penalización de pendiente.
- Sin fuerza, desplazamiento, incertidumbre ni calibración experimental trazables.
- Sin solver granular, colisiones, acoplamiento fluido-estructura o radiación acústica.
- Sin afirmaciones terapéuticas, biológicas o espirituales asociadas a frecuencias.

Los resultados cuantitativos y tolerancias están en
[CYMATICS_VALIDATION.md](CYMATICS_VALIDATION.md).
