package com.soniclab3d.cymatics

import com.soniclab3d.BuildConfig
import com.soniclab3d.performance.PerformanceStats
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object CymaticsExporter {
    fun json(
        state: CymaticsState,
        metrics: CymaticsMetrics,
        bank: CymaticsModeBank,
        stats: PerformanceStats
    ): String = buildString {
        appendLine("{")
        appendLine("  \"format\": \"SonicLab Cymatics experiment 1.0\",")
        appendLine("  \"appVersion\": \"${BuildConfig.VERSION_NAME}\",")
        appendLine("  \"exportedAtLocal\": \"${OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}\",")
        appendLine("  \"modelId\": \"${CymaticsModeBank.MODEL_ID}\",")
        appendLine("  \"classification\": \"numerical-simulation\",")
        appendLine("  \"experimentalMeasurement\": false,")
        appendLine("  \"granularDynamics\": \"qualitative-nodal-relaxation\",")
        appendLine("  \"geometry\": \"${state.geometry.name.lowercase()}\",")
        appendLine("  \"boundary\": \"${state.boundary.name.lowercase()}\",")
        appendLine("  \"gridSize\": ${bank.gridSize},")
        appendLine("  \"modeCount\": ${bank.eigenvalues.size},")
        appendLine("  \"activeModeIndex\": ${metrics.modeIndex + 1},")
        appendLine("  \"eigenvalue\": ${format(bank.eigenvalues[metrics.modeIndex].toDouble())},")
        appendLine("  \"driveFrequencyHz\": ${format(state.driveFrequencyHz)},")
        appendLine("  \"naturalFrequencyHz\": ${format(metrics.naturalFrequencyHz)},")
        appendLine("  \"relativeDetuning\": ${format(metrics.relativeDetuning)},")
        appendLine("  \"normalizedResponse\": ${format(metrics.response)},")
        appendLine("  \"normalizedCoupling\": ${format(metrics.coupling)},")
        appendLine("  \"normalizedAmplitude\": ${format(metrics.normalizedAmplitude)},")
        appendLine("  \"youngModulusPa\": ${format(state.youngModulusPa)},")
        appendLine("  \"materialPreset\": \"ideal-configurable\",")
        appendLine("  \"materialPropertySource\": \"user-configured; no material grade asserted\",")
        appendLine("  \"densityKgM3\": ${format(state.densityKgM3)},")
        appendLine("  \"poissonRatio\": ${format(state.poissonRatio)},")
        appendLine("  \"thicknessM\": ${format(state.thicknessM)},")
        appendLine("  \"characteristicSizeM\": ${format(state.characteristicSizeM)},")
        appendLine("  \"dampingRatio\": ${format(state.dampingRatio)},")
        appendLine("  \"drivePositionNormalized\": [${format(state.driveX.toDouble())}, ${format(state.driveY.toDouble())}],")
        appendLine("  \"probeEnabled\": ${state.probeEnabled},")
        appendLine("  \"probePositionNormalized\": [${format(state.probeX.toDouble())}, ${format(state.probeY.toDouble())}],")
        appendLine("  \"probeModalValueAu\": ${format(if (state.probeEnabled) bank.sample(metrics.modeIndex, state.probeX, state.probeY).toDouble() else 0.0)},")
        appendLine("  \"visualScale\": ${format(state.visualScale.toDouble())},")
        appendLine("  \"visualStyle\": \"${state.visualStyle.name.lowercase()}\",")
        appendLine("  \"palette\": \"${state.palette.name.lowercase()}\",")
        appendLine("  \"visualVariable\": \"${state.variable.name.lowercase()}\",")
        appendLine("  \"motionPreset\": \"${state.motionPreset.name.lowercase()}\",")
        appendLine("  \"motionIntensity\": ${format(state.motionIntensity.toDouble())},")
        appendLine("  \"visualCyclesPerSecond\": ${format(CymaticsMotion.visualCyclesPerSecond(state).toDouble())},")
        appendLine("  \"modeTransitionSeconds\": ${format(state.modeTransitionSeconds.toDouble())},")
        appendLine("  \"cameraDriftVisualOnly\": ${state.cameraDriftEnabled},")
        appendLine("  \"cameraChoreographyVisualOnly\": \"${state.cameraMotion.name.lowercase()}\",")
        appendLine("  \"orbitingLightVisualOnly\": ${state.orbitingLightEnabled},")
        appendLine("  \"resonanceAuraVisualOnly\": ${state.resonanceAuraEnabled},")
        appendLine("  \"atmosphericParticlesVisualOnly\": ${state.atmosphericDustEnabled},")
        appendLine("  \"stageVisualOnly\": ${state.stageEnabled},")
        appendLine("  \"nodalStreamsVisualOnly\": ${state.nodalStreamsEnabled},")
        appendLine("  \"energyPulsesVisualOnly\": ${state.energyPulsesEnabled},")
        appendLine("  \"postProcessingVisualOnly\": ${state.postProcessingEnabled},")
        appendLine("  \"bloomIntensityVisualOnly\": ${format(state.bloomIntensity.toDouble())},")
        appendLine("  \"sandFlowIntensity\": ${format(state.sandFlowIntensity.toDouble())},")
        appendLine("  \"reduceMotion\": ${state.reduceMotion},")
        appendLine("  \"showNodes\": ${state.showNodes},")
        appendLine("  \"showAntinodes\": ${state.showAntinodes},")
        appendLine("  \"showModalContours\": ${state.showPhaseLines},")
        appendLine("  \"visualExaggeration\": ${format(state.visualScale.toDouble())},")
        appendLine("  \"sandParticles\": ${state.sandParticleCount},")
        appendLine("  \"sandPath\": \"deterministic-nodal-targets-with-organic-gpu-interpolation\",")
        appendLine("  \"qualityProfile\": \"${state.quality.name.lowercase()}\",")
        appendLine("  \"adaptiveQuality\": ${state.autoQuality},")
        appendLine("  \"fps\": ${format(stats.fps.toDouble())},")
        appendLine("  \"frameTimeMs\": ${format(stats.frameTimeMs.toDouble())},")
        appendLine("  \"notice\": \"La amplitud está normalizada (a.u.); no es desplazamiento medido. La arena, las corrientes nodales, los pulsos, la cámara, las luces, el escenario y el bloom son capas visuales cualitativas.\"")
        appendLine("}")
    }

    private fun format(value: Double): String = String.format(Locale.US, "%.10g", value)
}
