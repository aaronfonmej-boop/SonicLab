# Informe de entrega · SonicLab 3D 1.1 Academia Continua

Fecha: 2026-08-15  
Fuente base: `SonicLab3D-Science-v1.0-cymatics-stage-Android.zip`  
SHA-256 base verificado: `5434e361036e75a533f33bbf66b7376f9c92e2f478062e8c48343e9b53616ad2`  
Versión fuente: `versionCode 11` / `1.1.0-academia-continua`  
Paquete: `com.soniclab3d`

## Implementación

- 6 rutas, 3 niveles, 34 guías, 20 prácticas y 66 Dudas frecuentes.
- 8 IDs históricos conservados y claves DataStore v1/v2 todavía legibles.
- Prácticas con ocho etapas, paso persistente, autosave de cuatro campos y reinicio aislado.
- Favoritos, progreso de guías, Dudas personales, cuaderno, asignaciones y feedback.
- Presets Aire/Placa y restauración de escena anterior.
- Pack, submission y feedback por SAF con validación de tipo, esquema, tamaño e IDs.
- Manifiestos, paquetes curriculares, validador y documentación de usuario/docente.

## Verificación ejecutada

```text
python3 tools/validate_learning_content.py
VALIDACIÓN DE CONTENIDO: OK
6 rutas · 3 niveles · 34 guías · 20 prácticas · 66 dudas · 2 paquetes curriculares
8 IDs v1.0 preservados · alineación UNAP no oficial declarada
```

También se validó la sintaxis de los cinco activos JSON con `python3 -m json.tool` y se
realizó revisión de delimitadores de los archivos Kotlin modificados. El árbol contiene
63 métodos `@Test` —54 heredados y 9 nuevos—, pero ese recuento de fuente no se presenta
como ejecución. Una comparación binaria de directorios confirmó que física, audio,
renderers, shaders y bancos modales permanecen idénticos a la base.

## Bloqueo de build

`./gradlew` requiere descargar Gradle 8.10.2. El entorno actual no contiene distribución
Gradle, Android SDK 35, `android.jar` ni cachés reutilizables, y la descarga de Gradle no
es accesible (`java.net.SocketException: Network is unreachable`). JDK 17 sí está
disponible. Por ello no se ejecutaron pruebas JVM, lint, ensamblado, firma ni análisis
de APK 1.1. No se entrega una APK renombrada o reconstruida falsamente.

## Pendientes frente al prompt maestro

- Compilar y corregir cualquier error que revele Kotlin/Compose/Android lint.
- Ejecutar pruebas nuevas y las 54 heredadas; realizar round trip SAF en Android.
- Probar instalación limpia y actualización firmada desde v1.0 con datos representativos.
- Auditar UI compacta, horizontal, tableta, texto ampliado y lector de pantalla.
- Completar backup/restore, comparación del cuaderno, evaluadores tipados y constructor
  docente de secuencias si se exige aceptación total de esos puntos.
- Migrar el texto completo del catálogo desde Kotlin a activos JSON sin perder IDs.

## Criterio científico

No se modificaron deliberadamente física, audio, renderers, shaders ni bancos modales.
Las nuevas pantallas mantienen etiquetas y límites para simulación, medición,
visualización y contenido educativo. No se afirma capacidad clínica o metrológica.
