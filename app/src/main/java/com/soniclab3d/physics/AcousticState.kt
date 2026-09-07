package com.soniclab3d.physics

import com.soniclab3d.particles.ParticleQuality

enum class ColorMode { SCIENTIFIC, ARTISTIC }
enum class Waveform { SINE, TRIANGLE, SQUARE, PULSE }
enum class VisualVariable { PRESSURE, DISPLACEMENT, VELOCITY, PHASE }
enum class SliceAxis { XY, XZ, YZ }
enum class ScientificPalette { RED_BLUE, COLORBLIND, MONOCHROME }

data class SecondarySourceSettings(
    val enabled: Boolean = false,
    val frequencyHz: Double = 120.0,
    val levelDbSpl: Float = 70f,
    val phaseRadians: Float = 0f,
    val xM: Float = 2.0f,
    val yM: Float = 0f,
    val zM: Float = 0f
)

data class AcousticState(
    val frequencyHz: Double = 120.0,
    val levelDbSpl: Float = 70f,
    val phaseRadians: Float = 0f,
    val temperatureC: Double = 20.0,
    val humidityPercent: Float = 50f,
    val atmosphericPressureKPa: Float = 101.325f,
    val sourceRadiusM: Float = 0.12f,
    val maxDistanceM: Float = 8f,
    val pointSizePx: Float = 3.0f,
    val displacementVisualScale: Float = 500_000f,
    val maxVisualDisplacementM: Float = 0.38f,
    val visualTimeScale: Float = 0.01f,
    val paused: Boolean = false,
    val audioEnabled: Boolean = false,
    val audioVolume: Float = 0.025f,
    val quality: ParticleQuality = ParticleQuality.ULTRA,
    val autoQuality: Boolean = true,
    val colorMode: ColorMode = ColorMode.SCIENTIFIC,
    val scientificPalette: ScientificPalette = ScientificPalette.RED_BLUE,
    val visualVariable: VisualVariable = VisualVariable.PRESSURE,
    val artisticHueDegrees: Float = 215f,
    val showPressureSlice: Boolean = false,
    val sliceAxis: SliceAxis = SliceAxis.XY,
    val sliceOffsetM: Float = 0f,
    val showWavefronts: Boolean = false,
    val waveform: Waveform = Waveform.SINE,
    val harmonicMode: Boolean = false,
    val harmonicAmplitudes: List<Float> = listOf(1f, 0.45f, 0.25f, 0.15f) + List(12) { 0f },
    val secondarySource: SecondarySourceSettings = SecondarySourceSettings(),
    val probes: List<AcousticProbe> = listOf(
        AcousticProbe("A", 1.0),
        AcousticProbe("B", 2.0),
        AcousticProbe("C", 3.0)
    ),
    val room: RectangularRoom = RectangularRoom()
) {
    val soundSpeedMps: Double get() = AcousticPhysics.soundSpeed(temperatureC)
    val airDensityKgM3: Double
        get() = AcousticPhysics.airDensity(temperatureC, humidityPercent.toDouble(), atmosphericPressureKPa.toDouble())
}
