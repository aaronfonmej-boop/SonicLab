package com.soniclab3d.scientific

import com.soniclab3d.physics.AcousticPhysics
import com.soniclab3d.physics.AcousticProbe
import com.soniclab3d.physics.AcousticState
import java.util.Locale

object ExperimentExporter {
    fun csv(state: AcousticState, simulationTimeS: Double, samples: Int = 241): String = buildString {
        appendLine("# SonicLab 3D Science 0.5")
        appendLine("# Modelo: campo libre analítico; no es una medición calibrada")
        appendLine("# frecuencia_Hz,${format(state.frequencyHz)}")
        appendLine("# temperatura_C,${format(state.temperatureC)}")
        appendLine("# tiempo_s,${format(simulationTimeS)}")
        appendLine("distancia_m,presion_Pa,desplazamiento_m,velocidad_m_s,fase_rad")
        repeat(samples.coerceAtLeast(2)) { index ->
            val distance = state.sourceRadiusM +
                (state.maxDistanceM - state.sourceRadiusM) * index / (samples - 1.0)
            val sample = AcousticPhysics.sampleAt(state, distance, 0.0, 0.0, simulationTimeS)
            appendLine(
                listOf(
                    distance,
                    sample.pressurePa,
                    sample.displacementMagnitudeM,
                    sample.particleVelocityMagnitudeMps,
                    sample.phaseRadians
                ).joinToString(",") { format(it) }
            )
        }
    }

    fun json(state: AcousticState, simulationTimeS: Double, probes: List<AcousticProbe>): String = buildString {
        appendLine("{")
        appendLine("  \"format\": \"SonicLab experiment 0.5\",")
        appendLine("  \"model\": \"analytical-free-field\",")
        appendLine("  \"calibratedMeasurement\": false,")
        appendLine("  \"frequencyHz\": ${format(state.frequencyHz)},")
        appendLine("  \"levelDbSplAtOneMeter\": ${format(state.levelDbSpl.toDouble())},")
        appendLine("  \"temperatureC\": ${format(state.temperatureC)},")
        appendLine("  \"humidityPercent\": ${format(state.humidityPercent.toDouble())},")
        appendLine("  \"atmosphericPressureKPa\": ${format(state.atmosphericPressureKPa.toDouble())},")
        appendLine("  \"simulationTimeS\": ${format(simulationTimeS)},")
        appendLine("  \"probes\": [")
        probes.forEachIndexed { index, probe ->
            val sample = AcousticPhysics.sampleAt(state, probe.xM, probe.yM, probe.zM, simulationTimeS)
            append("    {\"label\": \"${probe.label.replace("\"", "")}\", \"xM\": ${format(probe.xM)}, \"yM\": ${format(probe.yM)}, \"zM\": ${format(probe.zM)}, \"pressurePa\": ${format(sample.pressurePa)}, \"phaseRad\": ${format(sample.phaseRadians)}}")
            appendLine(if (index == probes.lastIndex) "" else ",")
        }
        appendLine("  ]")
        appendLine("}")
    }

    private fun format(value: Double): String = String.format(Locale.US, "%.10g", value)
}
