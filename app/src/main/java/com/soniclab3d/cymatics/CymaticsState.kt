package com.soniclab3d.cymatics

import com.soniclab3d.particles.ParticleQuality

enum class SceneDomain(val label: String) {
    AIR("Aire"),
    PLATE("Placa")
}

enum class PlateGeometry(val label: String, val assetStem: String) {
    SQUARE("Cuadrada", "square"),
    CIRCLE("Circular", "circle"),
    TRIANGLE("Triangular", "triangle"),
    HEXAGON("Hexagonal", "hexagon")
}

enum class PlateBoundary(val label: String, val assetStem: String) {
    SIMPLY_SUPPORTED("Simplemente apoyada", "simply_supported"),
    CLAMPED("Borde sujeto", "clamped")
}

enum class CymaticsVisualStyle(val label: String) {
    SCIENTIFIC("Científico"),
    SPECTACULAR("Espectacular")
}

enum class CymaticsMotionPreset(val label: String, val description: String) {
    PRECISE(
        "Preciso",
        "Transiciones breves y sin movimiento ambiental para lectura técnica."
    ),
    FLUID(
        "Fluido",
        "Interpolación suave, luz reactiva y arena orgánica con cámara estable."
    ),
    CINEMATIC(
        "Cinemático",
        "Escena completa: órbita coreografiada, escenario, filamentos, corrientes y bloom."
    )
}

enum class CymaticsCameraMotion(val label: String) {
    STATIC("Fija"),
    FLOAT("Flotante"),
    ORBIT("Órbita"),
    SHOW("Show")
}

enum class CymaticsPalette(val label: String) {
    CYAN_MAGENTA("Cian / magenta"),
    COLORBLIND("Naranja / azul"),
    MONOCHROME("Monocromática"),
    AURORA("Aurora eléctrica"),
    SOLAR("Solar dorada")
}

enum class CymaticsVariable(val label: String) {
    DISPLACEMENT("Desplazamiento"),
    VELOCITY("Velocidad transversal"),
    PHASE("Fase"),
    ENERGY("Energía modal")
}

enum class CymaticsCameraPreset(val label: String) {
    FREE("Libre"),
    TOP("Superior"),
    GRAZING("Rasante"),
    FRONT("Frontal"),
    MACRO_NODE("Macro nodo")
}

data class CymaticsState(
    val geometry: PlateGeometry = PlateGeometry.SQUARE,
    val boundary: PlateBoundary = PlateBoundary.SIMPLY_SUPPORTED,
    val driveFrequencyHz: Double = 120.0,
    val modeIndex: Int = 3,
    val automaticMode: Boolean = true,
    val youngModulusPa: Double = 69.0e9,
    val densityKgM3: Double = 2_700.0,
    val poissonRatio: Double = 0.33,
    val thicknessM: Double = 0.001,
    val characteristicSizeM: Double = 0.35,
    val dampingRatio: Double = 0.025,
    val driveX: Float = 0.22f,
    val driveY: Float = 0.16f,
    val probeEnabled: Boolean = false,
    val probeX: Float = -0.28f,
    val probeY: Float = 0.22f,
    val normalizedDrive: Float = 0.85f,
    val visualScale: Float = 0.64f,
    val visualTimeScale: Float = 0.08f,
    val paused: Boolean = false,
    val visualStyle: CymaticsVisualStyle = CymaticsVisualStyle.SPECTACULAR,
    val palette: CymaticsPalette = CymaticsPalette.AURORA,
    val variable: CymaticsVariable = CymaticsVariable.DISPLACEMENT,
    val showNodes: Boolean = true,
    val showAntinodes: Boolean = true,
    val showPhaseLines: Boolean = false,
    val contourDensity: Float = 12f,
    val showGrid: Boolean = false,
    val showExciter: Boolean = true,
    val sandEnabled: Boolean = true,
    val sandPointSizePx: Float = 2.5f,
    val sandRelaxation: Float = 0.55f,
    val motionPreset: CymaticsMotionPreset = CymaticsMotionPreset.CINEMATIC,
    val motionIntensity: Float = 0.88f,
    val modeTransitionSeconds: Float = 0.95f,
    val cameraDriftEnabled: Boolean = true,
    val cameraMotion: CymaticsCameraMotion = CymaticsCameraMotion.SHOW,
    val orbitingLightEnabled: Boolean = true,
    val resonanceAuraEnabled: Boolean = true,
    val atmosphericDustEnabled: Boolean = true,
    val sandFlowIntensity: Float = 0.84f,
    val stageEnabled: Boolean = true,
    val nodalStreamsEnabled: Boolean = true,
    val energyPulsesEnabled: Boolean = true,
    val postProcessingEnabled: Boolean = true,
    val bloomIntensity: Float = 0.92f,
    val lightHueDegrees: Float = 188f,
    val reduceMotion: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val quality: ParticleQuality = ParticleQuality.ULTRA,
    val autoQuality: Boolean = true
) {
    val sandParticleCount: Int
        get() = quality.particleCount.coerceAtMost(200_000)

    fun sanitized(): CymaticsState = copy(
        driveFrequencyHz = driveFrequencyHz.takeIf { it.isFinite() }?.coerceIn(20.0, 20_000.0) ?: 120.0,
        modeIndex = modeIndex.coerceIn(0, CymaticsModeBank.MODE_COUNT - 1),
        youngModulusPa = youngModulusPa.takeIf { it.isFinite() }?.coerceIn(1.0e6, 500.0e9) ?: 69.0e9,
        densityKgM3 = densityKgM3.takeIf { it.isFinite() }?.coerceIn(50.0, 25_000.0) ?: 2_700.0,
        poissonRatio = poissonRatio.takeIf { it.isFinite() }?.coerceIn(0.0, 0.49) ?: 0.33,
        thicknessM = thicknessM.takeIf { it.isFinite() }?.coerceIn(0.0002, 0.02) ?: 0.001,
        characteristicSizeM = characteristicSizeM.takeIf { it.isFinite() }?.coerceIn(0.08, 2.0) ?: 0.35,
        dampingRatio = dampingRatio.takeIf { it.isFinite() }?.coerceIn(0.002, 0.30) ?: 0.025,
        driveX = driveX.takeIf { it.isFinite() }?.coerceIn(-0.95f, 0.95f) ?: 0.22f,
        driveY = driveY.takeIf { it.isFinite() }?.coerceIn(-0.95f, 0.95f) ?: 0.16f,
        probeX = probeX.takeIf { it.isFinite() }?.coerceIn(-0.95f, 0.95f) ?: -0.28f,
        probeY = probeY.takeIf { it.isFinite() }?.coerceIn(-0.95f, 0.95f) ?: 0.22f,
        normalizedDrive = normalizedDrive.takeIf { it.isFinite() }?.coerceIn(0.05f, 1f) ?: 0.85f,
        visualScale = visualScale.takeIf { it.isFinite() }?.coerceIn(0.02f, 1.2f) ?: 0.64f,
        visualTimeScale = visualTimeScale.takeIf { it.isFinite() }?.coerceIn(0.005f, 1f) ?: 0.08f,
        sandPointSizePx = sandPointSizePx.takeIf { it.isFinite() }?.coerceIn(1f, 7f) ?: 2.5f,
        sandRelaxation = sandRelaxation.takeIf { it.isFinite() }?.coerceIn(0.08f, 1.5f) ?: 0.55f,
        motionIntensity = motionIntensity.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.88f,
        modeTransitionSeconds = modeTransitionSeconds.takeIf { it.isFinite() }
            ?.coerceIn(0.12f, 2.4f) ?: 0.95f,
        sandFlowIntensity = sandFlowIntensity.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.84f,
        bloomIntensity = bloomIntensity.takeIf { it.isFinite() }?.coerceIn(0f, 1.5f) ?: 0.92f,
        contourDensity = contourDensity.takeIf { it.isFinite() }?.coerceIn(4f, 32f) ?: 12f,
        lightHueDegrees = lightHueDegrees.takeIf { it.isFinite() }
            ?.let { ((it % 360f) + 360f) % 360f } ?: 188f
    )
}

data class CymaticsRenderInfo(
    val bankReady: Boolean = false,
    val activeModeIndex: Int = 0,
    val naturalFrequencyHz: Double = 0.0,
    val response: Double = 0.0,
    val coupling: Double = 0.0,
    val probeValue: Double = 0.0,
    val relativeDetuning: Double = 0.0,
    val sandParticleCount: Int = 0,
    val modeTransitionProgress: Float = 1f,
    val visualCyclesPerSecond: Float = 0f,
    val postProcessingActive: Boolean = false,
    val motionProfile: String = "Cargando movimiento",
    val rendererPath: String = "Cargando banco modal",
    val warning: String? = null
)
