# Informe de entrega · mejoras 2 y 3

Fecha: 2026-08-11  
Aplicación: SonicLab 3D Science 0.5 · Calibration Lab  
Versión: `5` / `0.5.0-calibration-lab`

## Resultado

La mejora 2 entrega una lista de revisión externa completa y firmable. El documento
permanece deliberadamente sin marcar: no se inventó el nombre, institución, firma ni
aprobación de un especialista.

La mejora 3 incorpora un flujo funcional y visible de calibración SPL por referencia
externa. La lectura principal del Analizador permanece en dBFS salvo que exista un
perfil válido para la ruta y configuración activas.

## Implementación visible

- Cabecera identificada como `SCIENCE 0.5 · CALIBRACIÓN SPL`.
- Panel Analizador con dBFS o dB SPL calibrado claramente diferenciados.
- Tarjeta de ruta con dispositivo, identificador, muestreo, formato, canales y fuente.
- Nivel de referencia y método/equipo externo introducidos por el usuario.
- Captura guiada de cinco segundos con progreso y opción de cancelar.
- Rechazo de señal insuficiente, cercana a saturación o inestable.
- Perfil con fecha, método, referencia, promedio dBFS y offset.
- Acciones para invalidar y borrar el perfil.
- Advertencia persistente al cambiar de ruta durante la sesión.
- Aviso explícito de que el offset no demuestra conformidad IEC 61672 ni clase 1/2.

## Archivos principales

| Archivo | Responsabilidad |
|---|---|
| `audio/AudioInputRoute.kt` | Identidad conservadora de dispositivo, ruta y configuración. |
| `audio/MicAnalyzerEngine.kt` | RMS dBFS, ruta efectiva y aplicación condicional del perfil. |
| `measurement/SplCalibration.kt` | Captura, offset, perfil, DataStore, invalidación y codec. |
| `ui/AdvancedPanels.kt` | Flujo completo y estados visibles en el Analizador. |
| `ui/SonicLabApp.kt` | Integración de motor, repositorio, permisos y ciclo de interfaz. |
| `SplCalibrationTest.kt` | Seis pruebas de lógica, estabilidad, ruta y persistencia. |
| `EXTERNAL_REVIEW_CHECKLIST.md` | Lista independiente, firmable y sin aprobación fabricada. |

## Criterio de aplicabilidad

Un perfil solo se aplica si está vigente y coincide exactamente con:

- identificador y tipo de dispositivo de entrada;
- nombre y dirección informados por Android;
- frecuencia de muestreo y cantidad de canales;
- codificación PCM;
- fuente `UNPROCESSED` o alternativa `DEFAULT`.

El tamaño de buffer queda registrado como metadato, pero no participa en la clave
porque no modifica por sí mismo la sensibilidad digital. Si la ruta cambia, el valor
`calibratedSplDb` vuelve a `null` salvo que exista un perfil válido para la nueva clave.

## Verificación reproducida

| Control | Resultado |
|---|---|
| Pruebas unitarias | 25/25 aprobadas; 0 fallos y 0 errores |
| Pruebas específicas de calibración | 6/6 aprobadas |
| `lintDebug` | 0 incidencias |
| `assembleDebug` | Correcto |
| Metadatos APK | `com.soniclab3d`, versión 5 / `0.5.0-calibration-lab` |
| Firma APK | Certificado Android Debug, esquema v2 verificado |
| Tamaño APK | 10.080.663 bytes |
| SHA-256 APK | `a7ad0f9a503080b22d6e7270848e35893b05d31e99caaa45981925feccceca86` |

## Límites y trabajo pendiente

- No se realizó una revisión externa: el checklist es el instrumento para hacerla.
- No se ensayó esta entrega en un teléfono físico ni con un calibrador/sonómetro real.
- Un único offset no caracteriza respuesta en frecuencia, orientación, ruido propio,
  compresión, AGC residual ni incertidumbre.
- Los metadatos de ruta dependen de lo que Android y el fabricante informen.
- El APK entregado es una compilación debug; una distribución pública requiere una
  clave release controlada por el propietario.
- No se afirma certificación, conformidad IEC 61672 ni equivalencia a sonómetro clase 1/2.

La siguiente evidencia necesaria para elevar la mejora 3 de **implementada** a
**validada** es una campaña documentada con al menos un equipo de referencia
identificado, varias frecuencias/niveles, rutas representativas y estimación de
incertidumbre.
