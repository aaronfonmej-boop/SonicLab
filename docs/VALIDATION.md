# Validación cuantitativa del modelo analítico

Documento asociado a `AcousticValidationTest.kt`  
Fecha de casos analíticos: 2026-08-10  
Última verificación Android: 2026-08-11  
Estado: **verificación automática aprobada; no equivale a validación metrológica**

## Objetivo y alcance

Se compara el resultado de `AcousticPhysics.sampleAt()` y de los modos rectangulares
con soluciones analíticas calculadas independientemente. La comprobación evalúa el
modelo matemático implementado; no valida el micrófono, el altavoz, el render visual,
la cadena Android ni la conformidad con un sonómetro IEC.

Los puntos de la nube representan volúmenes elementales de aire. La escala visual no
participa en estas ecuaciones.

## Convenciones y ecuaciones de referencia

Para una fuente saliente y una señal sinusoidal:

```text
c = 331,3 + 0,606 T_C
lambda = c / f
omega = 2 pi f
k = omega / c
theta = k r - omega t + phi

p_rms = 20e-6 10^(L_p/20)
p_peak = sqrt(2) p_rms
rho = p_d/(R_d T) + p_v/(R_v T)

r_eff = sqrt(r^2 + a^2)
G(r) = sqrt(1^2 + a^2) / r_eff
       * 10^[-alpha_dB max(r - 1, 0) / 20]

p(r,t) = p_peak G(r) sin(theta)
u_r(r,t) = p_peak G(r) sin(theta) / (rho c)
xi_r(r,t) = p_peak G(r) cos(theta) / (rho c omega)
```

La relación entre `u_r` y `xi_r` es coherente porque la derivada temporal de
`cos(k r - omega t + phi)` es `omega sin(k r - omega t + phi)`. La aproximación
de velocidad es la de una onda progresiva local; no contiene el campo cercano
reactivo completo de un transductor real.

Para paredes rígidas ideales en un recinto rectangular:

```text
f_n = c/2 sqrt[(n_x/L_x)^2 + (n_y/L_y)^2 + (n_z/L_z)^2]
p_n = cos(n_x pi x/L_x) cos(n_y pi y/L_y) cos(n_z pi z/L_z)
```

## Parámetros obligatorios y constantes

| Magnitud | Valor | Unidad o referencia |
|---|---:|---|
| Temperatura | 20 | °C |
| Frecuencia | 120 | Hz |
| Humedad relativa | 50 | % |
| Presión atmosférica | 101,325 | kPa |
| Nivel de fuente | 70 | dB SPL referido a 20 µPa a 1 m dentro del modelo |
| Radio regularizador `a` | 0,12 | m |
| Velocidad esperada `c` | 343,42 | m/s |
| Longitud de onda esperada | 2,8618333333333337 | m |
| Periodo esperado | 8,333333333333334 | ms |
| Densidad de referencia | 1,1988441854083347 | kg/m³ |
| Presión RMS de referencia | 0,0632455532033676 | Pa |
| Presión pico de referencia | 0,0894427190999916 | Pa |
| Absorción de referencia a 120 Hz | 0,0004090616115276836 | dB/m, expresión ISO 9613-1 implementada |

## Casos y tolerancias

1. **Condición 20 °C / 120 Hz:** comprueba `c`, `lambda` y periodo.
2. **Onda progresiva local a 1 m:** fija `theta = pi/4`. En esa distancia la
   envolvente vale uno y se prueban presión, velocidad, desplazamiento y fase.
3. **Fuente esférica regularizada a 0,25 m:** fija `theta = pi/2` y comprueba
   divergencia finita sin absorción adicional antes de 1 m.
4. **Fuente esférica a 4 m:** fija `theta = pi/2` y combina divergencia con
   atenuación de amplitud expresada directamente en dB.
5. **Centro y distancia casi nula:** exige valores finitos; la dirección radial
   indefinida se representa con vectores de velocidad y desplazamiento nulos.
6. **Modo rectangular (1,1,0):** sala de 6 x 4 x 3 m y muestra en
   `(1,5; 1,0; 1,5) m`.

Las tolerancias relativas de `1e-12` se reservan para aritmética cerrada con
constantes `Double`. Para estados almacenados como `Float`, funciones trigonométricas
y absorción se usan tolerancias entre `2e-7` y `8e-6`. Son suficientemente menores
que el error visual y no ocultan discrepancias de signo, fase, referencia de nivel o
envolvente.

## Resultados cuantitativos del núcleo puro

Ejecución independiente: Kotlin/JS 2.4.10, compatibilidad de lenguaje 2.0,
OpenJDK 17.0.19, 2026-08-10. Los valores esperados están incrustados en la prueba y
no se obtienen llamando a las funciones productivas que se evalúan.

| Magnitud | Esperado | Calculado | Error absoluto | Error relativo | Tolerancia relativa | Resultado |
|---|---:|---:|---:|---:|---:|---|
| `c` (m/s) | 343,42 | 343,42 | 0 | 0 % | 1e-12 | Aprobado |
| `lambda` (m) | 2,8618333333333337 | 2,8618333333333337 | 0 | 0 % | 1e-12 | Aprobado |
| `T` (ms) | 8,333333333333334 | 8,333333333333334 | 0 | 0 % | 1e-12 | Aprobado |
| Onda `p` (Pa) | 0,0632455532033676 | 0,0632455532033676 | 0 | 0 % | 2e-7 | Aprobado |
| Onda `u_x` (m/s) | 1,5361784576561864e-4 | 1,5361784576561864e-4 | 0 | 0 % | 5e-7 | Aprobado |
| Onda `xi_x` (m) | 2,0374199583938832e-7 | 2,0374199583938832e-7 | 0 | 0 % | 5e-7 | Aprobado |
| Onda fase (rad) | 0,7853981633974483 | 0,7853981633974483 | 0 | 0 % | 1e-10 | Aprobado |
| Esférica 0,25 m `p` (Pa) | 0,3248526724187956 | 0,3248526724187956 | 0 | 0 % | 5e-7 | Aprobado |
| Esférica 0,25 m `u_x` (m/s) | 7,890383623922895e-4 | 7,890383623922895e-4 | 0 | 0 % | 8e-7 | Aprobado |
| Esférica 4 m `p` (Pa) | 0,0225077933210979 | 0,022507793321097876 | 2,43e-17 | 1,08e-13 % | 8e-6 | Aprobado |
| Centro `p` (Pa) | 0,7507033738804934 | 0,7507033738804932 | 1,11e-16 | 1,48e-14 % | 5e-7 | Aprobado |
| Centro `|u|` (m/s) | 0 | 0 | 0 | no aplica | exacta | Aprobado |
| Centro `|xi|` (m) | 0 | 0 | 0 | no aplica | exacta | Aprobado |
| Modo (1,1,0) (Hz) | 51,592434125826806 | 51,592434125826806 | 0 | 0 % | 1e-12 | Aprobado |
| Presión modal normalizada | 0,5 | 0,5000000000000001 | 1,11e-16 | 2,22e-14 % | 1e-12 | Aprobado |

Resultado parcial: **15 magnitudes aprobadas de 15** en el núcleo puro.

## Coherencia Kotlin y GLSL inspeccionada

`AcousticPhysics.kt` y `particle.vert` comparten:

- fase `omega r/c - omega t + phi`;
- referencia a 1 m y radio efectivo `sqrt(r^2 + a^2)`;
- conversión de absorción a nepers antes de la exponencial;
- presión y velocidad con seno, desplazamiento con coseno;
- superposición lineal de la segunda fuente;
- escala visual aplicada solo después del desplazamiento físico.

Esta es una inspección estática, no una validación de GPU. Las diferencias de
precisión `Double`/`float`, compilador GLSL y dispositivo quedan pendientes de pruebas
instrumentadas.

## Resultado reproducido en Android

El 2026-08-11 se ejecutó la suite real del proyecto con Gradle 8.10.2, JDK 17 y
Android SDK 35:

| Suite | Pruebas | Fallos | Errores | Resultado |
|---|---:|---:|---:|---|
| `AcousticPhysicsTest` | 13 | 0 | 0 | Aprobado |
| `AcousticValidationTest` | 6 | 0 | 0 | Aprobado |
| `SplCalibrationTest` | 6 | 0 | 0 | Aprobado |
| **Total** | **25** | **0** | **0** | **Aprobado** |

`lintDebug` terminó con **0 incidencias**. `assembleDebug` produjo un APK de
10.080.663 bytes, paquete `com.soniclab3d`, `versionCode 5` y
`versionName 0.5.0-calibration-lab`. `apksigner` verificó la firma debug mediante el
esquema v2. SHA-256 del APK:

```text
a7ad0f9a503080b22d6e7270848e35893b05d31e99caaa45981925feccceca86
```

La inspección del DEX confirmó la presencia de `AudioInputRoute`,
`SplCalibrationProfile`, `SplCalibrationRepository`,
`SplCalibrationCaptureController` y el campo `calibratedSplDb` del analizador. Esto
demuestra que el paquete compilado contiene la mejora y no es una copia del APK 0.4.

## Casos automáticos de calibración SPL

La suite `SplCalibrationTest` comprueba:

1. Una captura estable de cinco segundos calcula `offset = L_ref - promedio(dBFS)`.
2. El perfil solo se aplica a una ruta y configuración exactamente coincidentes.
3. Un cambio de ruta durante la captura cancela el proceso y no genera perfil.
4. Una fuente con variación superior a 1,5 dB se rechaza.
5. La serialización y recuperación conserva todos los metadatos del perfil.
6. Un nivel de referencia fuera de 20–140 dB SPL no inicia la calibración.

Estas pruebas usan rutas y muestras sintéticas. No caracterizan la respuesta acústica
de un teléfono, la referencia externa, el sistema operativo ni el procesamiento del
fabricante.

## Reproducción

En Android Studio o un entorno con SDK 35 y acceso a dependencias:

```bash
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
```

Se debe archivar el informe JUnit, el informe lint, el hash y tamaño del APK y el
entorno de ejecución. Si una tolerancia falla, no debe ampliarse sin una justificación
física y numérica registrada.

## Limitaciones abiertas

- No se ha realizado revisión externa por un especialista.
- La absorción requiere contraste adicional con una fuente primaria autorizada.
- El modelo no incluye campo cercano reactivo completo, directividad, reflexiones,
  difracción, turbulencia ni no linealidad.
- La cadena de micrófono y el SPL calibrado no se han validado con hardware real.
- No se afirma conformidad con ISO, IEC ni equivalencia a un sonómetro.
