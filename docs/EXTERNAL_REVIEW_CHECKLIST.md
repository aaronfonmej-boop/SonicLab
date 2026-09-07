# Lista de revisión externa por especialista en acústica

Documento: `EXTERNAL_REVIEW_CHECKLIST.md`  
Aplicación: SonicLab 3D Science 0.5  
Versión candidata: `0.5.0-calibration-lab` (`versionCode 5`)  
Estado: **pendiente de revisión externa; no aprobado**  

Esta lista permite documentar una revisión técnica independiente. Su existencia no
equivale a una aprobación, certificación ni conformidad normativa. Todas las casillas
se entregan sin marcar para que las complete la persona revisora.

## Identificación del material revisado

- Commit o identificador del código: __________________________________________
- Hash SHA-256 del paquete fuente: ____________________________________________
- Hash SHA-256 del APK: _______________________________________________________
- Dispositivo(s) y ruta(s) de audio revisados: ________________________________
- Documentos y casos de prueba incluidos: ____________________________________

Escala recomendada para observaciones: **C** = conforme con el alcance declarado;
**NC** = no conforme; **NA** = no aplica; **PV** = pendiente de verificar.

## 1. Ecuaciones y convenciones

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| La convención temporal y el signo de fase están declarados y se aplican de forma coherente. | ☐ | ☐ | ☐ | ☐ | |
| Las relaciones entre frecuencia, periodo, longitud de onda, número de onda y velocidad son dimensionalmente correctas. | ☐ | ☐ | ☐ | ☐ | |
| La conversión entre nivel de presión y amplitud de presión utiliza una referencia explícita. | ☐ | ☐ | ☐ | ☐ | |
| La superposición de fuentes respeta amplitud y fase. | ☐ | ☐ | ☐ | ☐ | |
| El modelo modal rectangular y sus índices se corresponden con la ecuación declarada. | ☐ | ☐ | ☐ | ☐ | |

## 2. Unidades, signos y magnitudes

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| Las unidades SI aparecen en fórmulas, interfaz y exportaciones. | ☐ | ☐ | ☐ | ☐ | |
| Se distinguen presión instantánea, amplitud pico, RMS y nivel logarítmico. | ☐ | ☐ | ☐ | ☐ | |
| Se distinguen desplazamiento físico y exageración visual. | ☐ | ☐ | ☐ | ☐ | |
| El signo de velocidad y desplazamiento es coherente con la dirección de propagación adoptada. | ☐ | ☐ | ☐ | ☐ | |
| dBFS relativo y dB SPL calibrado se muestran como magnitudes diferentes. | ☐ | ☐ | ☐ | ☐ | |

## 3. Condiciones iniciales y de frontera

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| Las condiciones iniciales del campo y del reloj de simulación están declaradas. | ☐ | ☐ | ☐ | ☐ | |
| Las condiciones de frontera del recinto rectangular ideal están declaradas. | ☐ | ☐ | ☐ | ☐ | |
| No se atribuyen al modelo reflexiones, difracción o absorción de materiales que no calcula. | ☐ | ☐ | ☐ | ☐ | |
| Pausa, reanudación y búsqueda temporal preservan el estado físico esperado. | ☐ | ☐ | ☐ | ☐ | |

## 4. Regularización de la fuente

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| El radio regularizador y su motivación numérica están documentados. | ☐ | ☐ | ☐ | ☐ | |
| El comportamiento en el centro y cerca de la fuente es finito y está probado. | ☐ | ☐ | ☐ | ☐ | |
| La interfaz no presenta la zona regularizada como campo cercano acústico exacto. | ☐ | ☐ | ☐ | ☐ | |

## 5. Atenuación y absorción

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| La divergencia geométrica aplicada corresponde al tipo de fuente declarado. | ☐ | ☐ | ☐ | ☐ | |
| La absorción atmosférica distingue pérdida de amplitud y pérdida de intensidad. | ☐ | ☐ | ☐ | ☐ | |
| Los parámetros ambientales usados por la aproximación están visibles o documentados. | ☐ | ☐ | ☐ | ☐ | |
| El dominio de validez y las simplificaciones de la absorción están declarados. | ☐ | ☐ | ☐ | ☐ | |

## 6. Coherencia Kotlin / GLSL

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| CPU y shader usan la misma convención de fase, distancia y regularización. | ☐ | ☐ | ☐ | ☐ | |
| Las magnitudes visualizadas por color corresponden a la variable seleccionada. | ☐ | ☐ | ☐ | ☐ | |
| La escala visual no modifica los resultados físicos o exportados. | ☐ | ☐ | ☐ | ☐ | |
| Los límites de precisión de coma flotante y muestreo GPU están documentados. | ☐ | ☐ | ☐ | ☐ | |

## 7. Presión, velocidad y desplazamiento

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| Se interpreta correctamente la presión acústica instantánea. | ☐ | ☐ | ☐ | ☐ | |
| Se diferencia velocidad de partícula de velocidad de propagación. | ☐ | ☐ | ☐ | ☐ | |
| Se diferencia desplazamiento de partícula de trayectoria de la onda. | ☐ | ☐ | ☐ | ☐ | |
| Las relaciones de onda progresiva se limitan al dominio en que son aplicables. | ☐ | ☐ | ☐ | ☐ | |

## 8. FFT y nivel digital

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| Tamaño de FFT, frecuencia de muestreo y ventana Hann están declarados. | ☐ | ☐ | ☐ | ☐ | |
| La frecuencia dominante se contrasta con tonos de referencia y tolerancias explícitas. | ☐ | ☐ | ☐ | ☐ | |
| El cálculo de dBFS RMS, el rechazo de continua y el umbral de señal se verifican. | ☐ | ☐ | ☐ | ☐ | |
| Se evalúan fuga espectral, resolución en frecuencia, ruido y saturación. | ☐ | ☐ | ☐ | ☐ | |
| La ruta de captura `UNPROCESSED` y su alternativa `DEFAULT` se informan correctamente. | ☐ | ☐ | ☐ | ☐ | |

## 9. Calibración SPL

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| El nivel de referencia procede de un equipo externo identificado por el usuario. | ☐ | ☐ | ☐ | ☐ | |
| La captura dura 5 s y rechaza señal insuficiente, saturada o inestable. | ☐ | ☐ | ☐ | ☐ | |
| El offset se calcula a partir del promedio dBFS de la captura estable. | ☐ | ☐ | ☐ | ☐ | |
| El perfil conserva fecha, método, nivel de referencia y configuración de entrada. | ☐ | ☐ | ☐ | ☐ | |
| El perfil solo se aplica a dispositivo, ruta, muestreo y configuración coincidentes. | ☐ | ☐ | ☐ | ☐ | |
| Un cambio de ruta provoca advertencia y retira el dB SPL no aplicable. | ☐ | ☐ | ☐ | ☐ | |
| Invalidar o borrar un perfil impide su uso posterior. | ☐ | ☐ | ☐ | ☐ | |
| Reiniciar la aplicación conserva los perfiles mediante DataStore. | ☐ | ☐ | ☐ | ☐ | |

Equipo de referencia (fabricante, modelo, serie): ______________________________

Fecha de su última calibración trazable: ______________________________________

Método, señal, frecuencia y acoplamiento utilizados: __________________________

## 10. Incertidumbre

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| Se identifican por separado incertidumbre del sensor, variabilidad experimental y error numérico. | ☐ | ☐ | ☐ | ☐ | |
| No se inventa una incertidumbre cuando no hay datos suficientes. | ☐ | ☐ | ☐ | ☐ | |
| La repetibilidad de la calibración se evalúa en más de una captura. | ☐ | ☐ | ☐ | ☐ | |
| Se evalúa la dependencia con frecuencia, orientación, carcasa y ruta de audio. | ☐ | ☐ | ☐ | ☐ | |
| Las cifras significativas son coherentes con la resolución y el método. | ☐ | ☐ | ☐ | ☐ | |

## 11. Afirmaciones permitidas y prohibidas

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| Se permite describir la app como visualización educativa y laboratorio experimental. | ☐ | ☐ | ☐ | ☐ | |
| Se permite mostrar SPL solo con un perfil válido para la ruta activa. | ☐ | ☐ | ☐ | ☐ | |
| Se prohíbe afirmar exactitud, certificación o conformidad IEC sin evaluación correspondiente. | ☐ | ☐ | ☐ | ☐ | |
| Se prohíbe afirmar equivalencia con sonómetro clase 1 o clase 2 por usar un offset. | ☐ | ☐ | ☐ | ☐ | |
| Se prohíbe confundir observación visual, aproximación analítica y medición real. | ☐ | ☐ | ☐ | ☐ | |

## 12. Reproducibilidad

| Control | C | NC | NA | PV | Observaciones / evidencia |
|---|:---:|:---:|:---:|:---:|---|
| La versión del modelo, app, sistema y hardware queda identificada. | ☐ | ☐ | ☐ | ☐ | |
| Los parámetros y unidades necesarios para repetir cada caso se exportan. | ☐ | ☐ | ☐ | ☐ | |
| Las pruebas de referencia no repiten literalmente la implementación productiva. | ☐ | ☐ | ☐ | ☐ | |
| Los resultados fallidos y limitaciones se conservan en el informe. | ☐ | ☐ | ☐ | ☐ | |
| Los hashes permiten relacionar revisión, fuente y APK. | ☐ | ☐ | ☐ | ☐ | |

## Resultado y recomendaciones de la revisión

Resultado seleccionado por la persona revisora:

- ☐ Conforme con el alcance y las limitaciones declaradas.
- ☐ Conforme con observaciones que deben registrarse.
- ☐ Requiere correcciones antes de una nueva revisión.
- ☐ Revisión incompleta; no emitir conclusión.

Recomendaciones obligatorias:

______________________________________________________________________________

______________________________________________________________________________

Observaciones adicionales:

______________________________________________________________________________

______________________________________________________________________________

## Identificación y firma de la persona revisora

- Nombre completo: ___________________________________________________________
- Título / especialidad: _____________________________________________________
- Institución: _______________________________________________________________
- Correo institucional o contacto: ___________________________________________
- Alcance y posibles conflictos de interés: __________________________________
- Lugar y fecha: _____________________________________________________________
- Firma: ____________________________________________________________________

La firma documenta únicamente la revisión del material identificado al inicio y el
resultado marcado por la persona revisora. No extiende el alcance a versiones,
dispositivos, rutas o configuraciones que no hayan sido examinados.
