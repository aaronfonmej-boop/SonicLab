# Auditoría de línea base

Fecha de auditoría: 2026-08-10  
Fuente: `SonicLab3D-Science-v0.4-field-lab-Android.zip`  
Versión declarada: `0.4.0-field-lab` (`versionCode 4`)

## Estado encontrado

- Proyecto Android de un módulo (`:app`), paquete `com.soniclab3d`.
- `minSdk 26`, `targetSdk 35`, Java 17, Kotlin 2.0.21 y Compose.
- El ZIP no contiene metadatos Git; por ello no fue posible registrar ramas,
  commits ni cambios previos. Ningún commit se atribuye a esta entrega.
- El archivo original ocupa 114.192 bytes y no incluye un APK precompilado.
- No se encontraron `AGENTS.md` ni `CONTRIBUTING.md` dentro del proyecto.
- Se revisaron README, modelo científico, roadmap, Gradle, manifiesto, física,
  shader de partículas, exportador y pruebas existentes.

## Compilación y pruebas de referencia

Se solicitó la ejecución conjunta de:

```bash
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
```

| Comprobación | Resultado real | Evidencia o causa |
|---|---|---|
| Wrapper en caché predeterminada | Bloqueado antes de Gradle | El entorno no permite crear `/root/.gradle/...zip.lck`. |
| Wrapper con caché local | Bloqueado antes de Gradle | Gradle 8.10.2 no estaba instalado y `services.gradle.org` no era accesible. |
| `testDebugUnitTest` | No ejecutado | El wrapper no pudo iniciar. |
| `lintDebug` | No ejecutado | El wrapper no pudo iniciar. |
| `assembleDebug` | No ejecutado | El wrapper no pudo iniciar. |
| Pruebas instrumentadas | No ejecutadas | No había Android SDK, emulador ni dispositivo autorizado. |
| APK debug y tamaño | No generado | `assembleDebug` no llegó a iniciarse. |
| Perfil Normal 60k | No medido | Requiere ejecutar la app en hardware o emulador gráfico representativo. |

El entorno sí disponía de OpenJDK `17.0.19`. La imposibilidad de iniciar
Gradle es una limitación de infraestructura y no demuestra éxito ni fallo del
código Android.

## Verificación alternativa del núcleo puro

Como control parcial, los archivos Kotlin puros de partículas, estado, física y
modos de sala se compilaron con el compilador Kotlin/JS 2.4.10 en modo de lenguaje
2.0. Un ejecutable independiente comparó 15 magnitudes con valores analíticos
precalculados: 15 aprobadas y 0 fuera de tolerancia. Los resultados están en
[VALIDATION.md](VALIDATION.md).

Esta comprobación detecta errores de sintaxis y de cálculo en el núcleo común,
pero no sustituye la compilación Kotlin/JVM del proyecto, JUnit, lint, el empaquetado
Android ni las pruebas del shader.

## Diferencias frente al prompt maestro

Al iniciar la mejora 1 no existían:

- `docs/IMPLEMENTATION_STATUS.md`;
- `docs/VALIDATION.md`;
- `AcousticValidationTest.kt`;
- resultados cuantitativos independientes para `sampleAt()`.

Las 13 pruebas heredadas cubrían relaciones fundamentales, conversiones, tendencias
y algunos casos de sala, pero solo una comprobaba directamente `sampleAt()`.

## Riesgos abiertos

1. Falta confirmar las nuevas pruebas con Kotlin/JVM 2.0.21 y JUnit.
2. La coherencia Kotlin/GLSL fue inspeccionada, pero aún no tiene una prueba GPU
   automatizada ni mediciones en una GPU real.
3. La absorción atmosférica necesita comparación externa con datos de referencia
   trazables; el caso actual comprueba la integración dentro de `sampleAt()`.
4. No existen métricas reales de rendimiento, memoria o estado térmico para Normal
   60k en esta auditoría.

## Plan incremental

1. Completar la mejora 1: casos analíticos, tolerancias y documentación.
2. Repetir `testDebugUnitTest`, `lintDebug` y `assembleDebug` en un entorno Android.
3. Continuar con la lista de revisión externa sin atribuir una aprobación inexistente.
4. Implementar calibración SPL solo después de cerrar la base de validación.
5. Mantener una sola mejora física de alto riesgo en curso.
