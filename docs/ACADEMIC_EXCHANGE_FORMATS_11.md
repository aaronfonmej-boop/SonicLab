# Formatos académicos 1.1

| Tipo | `documentType` | Uso |
| --- | --- | --- |
| `.soniclabpack.json` | `soniclabpack` | Asignación para estudiante; sin solución ni rúbrica privada. |
| `.soniclabsubmission.json` | `soniclabsubmission` | Respuesta, predicción, evidencia, reflexión y Dudas de una práctica. |
| `.soniclabfeedback.json` | `soniclabfeedback` | Puntaje/estado y comentario para una entrega. |
| Portafolio JSON | `SonicLab academic experience 1.1` | Resumen local de progreso, cuaderno, dudas y estados. |

Todos declaran esquema, app, versión, fecha y advertencia científica. El importador
acepta como máximo 1 MB, IDs restringidos y prácticas incorporadas conocidas. Rechaza
esquema/tipo/app incompatibles y campos `script` o `expectedAnswer` donde no corresponden.
La transferencia usa URIs SAF. En esta versión no existe checksum interno, cifrado ni
firma de contenido; comparte archivos solo por canales apropiados.

Pendientes explícitos: `.soniclabportfolio.json` con importación, `.soniclabbackup.json`
con restauración transaccional, historial de versiones y comentario por criterio.
