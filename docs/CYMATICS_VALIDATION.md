# Validación numérica y visual de Cymatics Lab 1.0

## Método y entorno

Los bancos entregados se generaron con Python 3, NumPy 2.3 y SciPy 1.17. El operador
biarmónico discreto se resuelve con `scipy.sparse.linalg.eigsh`, vector inicial fijo,
tolerancia `1e-9` y orden ascendente. No existe aleatoriedad en los autovectores; el
signo de cada campo se normaliza de forma canónica.

Se ejecutó dos veces el comando completo de generación y se compararon los nueve
archivos resultantes. Los ocho binarios y el manifiesto conservaron exactamente su
SHA-256.

## Comparación analítica del cuadrado apoyado

Para un cuadrado simplemente apoyado de lado adimensional `a = 1,8`:

```text
λ_mn = {[(mπ/a)² + (nπ/a)²]}²
```

La malla 64 × 64 obtiene:

| Control | Resultado | Tolerancia documentada |
|---|---:|---:|
| Error relativo del primer modo | 2,1383 % | < 3 % |
| Mediana del error, primeros 12 modos | 2,4762 % | < 4 % |
| Máximo residual relativo, banco cuadrado apoyado | 1,68 × 10⁻¹⁰ | < 1 × 10⁻⁸ |
| Máximo error de ortogonalidad | 8,45 × 10⁻¹⁶ | < 1 × 10⁻⁵ |

La tolerancia de 3 % corresponde al error observado de discretización de segundo orden
en esta resolución, con margen inferior a un punto porcentual. Aumentar la resolución
reduce el error, a costa de activos y tiempo de generación mayores.

## Controles de todos los bancos

El manifiesto registra para cada modo su autovalor y residual. En los ocho bancos:

- el máximo residual relativo es del orden de `10⁻¹⁰`;
- el máximo error de ortogonalidad es del orden de `10⁻¹⁵`;
- todos los autovalores son positivos, finitos y no decrecientes;
- cada campo cumple `max(abs(φ)) = 1` dentro de `2 × 10⁻⁵`;
- todo valor fuera de la máscara geométrica es exactamente cero;
- el primer anillo del cuadrado sujeto tiene amplitud media menor que 5 % de la del
  cuadrado apoyado, coherente con la penalización de pendiente;
- la cabecera, versión, geometría y borde son validados antes de aceptar un activo.

## Frecuencias del estado predeterminado

Con `E = 69 GPa`, `ρ = 2700 kg/m³`, `ν = 0,33`, `h = 1 mm` y `L = 0,35 m`:

| Geometría | Borde | Primer modo | Modo 24 |
|---|---|---:|---:|
| Cuadrada | apoyado | 48,4 Hz | 888,1 Hz |
| Cuadrada | sujeto | 91,1 Hz | 1072,3 Hz |
| Circular | apoyado | 56,2 Hz | 1177,3 Hz |
| Circular | sujeto | 102,1 Hz | 1384,9 Hz |
| Triangular | apoyado | 145,9 Hz | 2340,8 Hz |
| Triangular | sujeto | 289,3 Hz | 2934,1 Hz |
| Hexagonal | apoyado | 66,1 Hz | 1327,1 Hz |
| Hexagonal | sujeto | 122,1 Hz | 1575,7 Hz |

El rango cambia de forma físicamente coherente al editar rigidez, densidad, espesor y
dimensión; no es un límite universal de la geometría.

## Pruebas JVM

La suite final cubre:

- escalamiento con `E`, `ρ`, `h` y `L`;
- máximo de respuesta amortiguada en resonancia;
- selección automática y acoplamiento del excitador;
- acoplamiento nulo sobre un nodo;
- sanitización sin NaN ni infinito;
- referencia analítica y degeneración del cuadrado;
- lectura real de los ocho activos, 24 modos por banco;
- normalización, máscara, ortogonalidad y borde sujeto;
- rechazo de magic y versión corruptos;
- lectura del manifiesto y cotejo de sus ocho SHA-256;
- exportación etiquetada como simulación y arena cualitativa;
- perfiles de movimiento coherentes, reloj perceptivo y sanitización de parámetros no finitos;
- progreso acotado del morph, anulación por movimiento reducido y continuidad desde
  un campo intermedio;
- bloqueos académicos y round trip del codec 5, compatible con codecs 2, 3 y 4.

La suite completa también conserva las pruebas acústicas, de calibración y academia de
v0.7. El conteo exacto ejecutado se registra en
[DELIVERY_REPORT_CYMATICS_10.md](DELIVERY_REPORT_CYMATICS_10.md).

## Validación pendiente

No hubo dispositivo o emulador con GPU disponible en el entorno de construcción. Los
los diez shaders 1.0 sí se compilaron y sus seis programas se enlazaron estáticamente con glslang para GLSL ES
3.00. Continúan pendientes el enlace real del driver en Adreno/Mali/Xclipse, cambio
repetido Aire/Placa, rotación, pausa/reanudación, percentiles de frame time y comparación
con una placa física. Estas ausencias no se presentan como aprobación experimental.
