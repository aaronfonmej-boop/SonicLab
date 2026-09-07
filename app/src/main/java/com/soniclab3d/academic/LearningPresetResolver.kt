package com.soniclab3d.academic

import com.soniclab3d.cymatics.CymaticsMotionPreset
import com.soniclab3d.cymatics.CymaticsPalette
import com.soniclab3d.cymatics.CymaticsState
import com.soniclab3d.cymatics.CymaticsVariable
import com.soniclab3d.cymatics.CymaticsVisualStyle
import com.soniclab3d.cymatics.PlateBoundary
import com.soniclab3d.cymatics.PlateGeometry
import com.soniclab3d.physics.AcousticState
import com.soniclab3d.physics.RectangularRoom
import com.soniclab3d.physics.ScientificPalette
import com.soniclab3d.physics.SecondarySourceSettings
import com.soniclab3d.physics.VisualVariable
import com.soniclab3d.physics.Waveform
import kotlin.math.PI

object LearningPresetResolver {
    fun presetForContent(contentId: String): String? =
        LearningCatalog.guide(contentId)?.presetId ?: LearningCatalog.practice(contentId)?.presetId

    fun domainForContent(contentId: String): LearningDomain =
        LearningCatalog.guide(contentId)?.domain ?: LearningCatalog.practice(contentId)?.domain ?: LearningDomain.AIR

    fun applyAir(state: AcousticState, presetId: String): AcousticState {
        val clean = state.copy(audioEnabled = false, paused = false)
        return when (presetId) {
            "wave_particle", "air_oscillation_120" -> clean.copy(
                frequencyHz = 120.0,
                temperatureC = 20.0,
                visualTimeScale = 0.01f,
                visualVariable = VisualVariable.DISPLACEMENT,
                showWavefronts = true,
                showPressureSlice = false,
                harmonicMode = false,
                secondarySource = clean.secondarySource.copy(enabled = false)
            )
            "frequency_wavelength", "air_frequency_120" -> clean.copy(
                frequencyHz = 120.0,
                temperatureC = 20.0,
                visualVariable = VisualVariable.PRESSURE,
                showWavefronts = true,
                harmonicMode = false,
                secondarySource = clean.secondarySource.copy(enabled = false)
            )
            "interference", "phase_map", "air_interference_phase" -> clean.copy(
                frequencyHz = 120.0,
                levelDbSpl = 70f,
                showPressureSlice = true,
                showWavefronts = false,
                harmonicMode = false,
                waveform = Waveform.SINE,
                secondarySource = SecondarySourceSettings(
                    enabled = true,
                    frequencyHz = 120.0,
                    levelDbSpl = 70f,
                    phaseRadians = PI.toFloat(),
                    xM = 2f,
                    yM = 0f,
                    zM = 0f
                )
            )
            "harmonics", "signal_harmonics", "voice_harmonics", "voice_source_filter" -> clean.copy(
                frequencyHz = 120.0,
                harmonicMode = true,
                harmonicAmplitudes = listOf(1f, 0.55f, 0.34f, 0.22f, 0.12f) + List(11) { 0f },
                waveform = Waveform.SINE,
                secondarySource = clean.secondarySource.copy(enabled = false),
                showPressureSlice = false
            )
            "air_temperature" -> clean.copy(
                frequencyHz = 500.0,
                temperatureC = 0.0,
                showWavefronts = true,
                harmonicMode = false,
                secondarySource = clean.secondarySource.copy(enabled = false)
            )
            "air_beats" -> clean.copy(
                frequencyHz = 120.0,
                levelDbSpl = 64f,
                waveform = Waveform.SINE,
                harmonicMode = false,
                showPressureSlice = true,
                secondarySource = SecondarySourceSettings(
                    enabled = true,
                    frequencyHz = 124.0,
                    levelDbSpl = 64f,
                    phaseRadians = 0f,
                    xM = 0.6f,
                    yM = 0f,
                    zM = 0f
                )
            )
            "air_room_mode", "room_resonance" -> clean.copy(
                frequencyHz = 28.6,
                showPressureSlice = true,
                showWavefronts = false,
                secondarySource = clean.secondarySource.copy(enabled = false),
                room = RectangularRoom(lengthM = 6.0, widthM = 4.0, heightM = 2.8, modeX = 1, modeY = 0, modeZ = 0)
            )
            "room_dimensions" -> clean.copy(
                frequencyHz = 34.3,
                showPressureSlice = true,
                room = RectangularRoom(lengthM = 5.0, widthM = 4.0, heightM = 2.8, modeX = 1, modeY = 0, modeZ = 0)
            )
            "air_level_relative" -> clean.copy(
                frequencyHz = 1_000.0,
                levelDbSpl = 60f,
                displacementVisualScale = 500_000f,
                showPressureSlice = true,
                visualVariable = VisualVariable.PRESSURE,
                scientificPalette = ScientificPalette.COLORBLIND,
                secondarySource = clean.secondarySource.copy(enabled = false)
            )
            "signal_time_frequency", "signal_spectrum", "mic_signal_chain", "level_calibration",
            "mic_distance", "voice_spectrum", "data_sampling", "data_export", "noise_metrics",
            "room_reverb", "room_reflection", "room_compare", "method_control", "method_uncertainty",
            "method_sweep", "method_report" -> clean.copy(
                frequencyHz = 440.0,
                levelDbSpl = 58f,
                harmonicMode = false,
                waveform = Waveform.SINE,
                showPressureSlice = false,
                showWavefronts = false,
                secondarySource = clean.secondarySource.copy(enabled = false)
            )
            else -> AcademicPractices.all.firstOrNull { it.id == presetId }
                ?.let { AcademicPractices.prepare(clean, it.id) }
                ?: clean
        }
    }

    fun applyPlate(state: CymaticsState, presetId: String): CymaticsState {
        val clean = state.copy(paused = false, visualStyle = CymaticsVisualStyle.SCIENTIFIC)
        return when (presetId) {
            "cymatics_nodes", "plate_sand" -> clean.copy(
                geometry = PlateGeometry.SQUARE,
                boundary = PlateBoundary.SIMPLY_SUPPORTED,
                automaticMode = false,
                modeIndex = 4,
                showNodes = true,
                showAntinodes = true,
                sandEnabled = true,
                variable = CymaticsVariable.DISPLACEMENT,
                palette = CymaticsPalette.COLORBLIND,
                motionPreset = CymaticsMotionPreset.PRECISE
            )
            "cymatics_resonance", "plate_resonance" -> clean.copy(
                geometry = PlateGeometry.CIRCLE,
                boundary = PlateBoundary.CLAMPED,
                automaticMode = false,
                modeIndex = 2,
                dampingRatio = 0.018,
                sandEnabled = false,
                showNodes = true,
                variable = CymaticsVariable.ENERGY,
                motionPreset = CymaticsMotionPreset.PRECISE
            )
            "cymatics_geometry", "plate_modes" -> clean.copy(
                geometry = PlateGeometry.HEXAGON,
                boundary = PlateBoundary.SIMPLY_SUPPORTED,
                automaticMode = false,
                modeIndex = 5,
                showNodes = true,
                showGrid = true,
                sandEnabled = false,
                motionPreset = CymaticsMotionPreset.PRECISE
            )
            "cymatics_coupling", "plate_coupling" -> clean.copy(
                geometry = PlateGeometry.SQUARE,
                boundary = PlateBoundary.SIMPLY_SUPPORTED,
                automaticMode = false,
                modeIndex = 6,
                driveX = 0.32f,
                driveY = 0.26f,
                showNodes = true,
                showExciter = true,
                sandEnabled = false,
                motionPreset = CymaticsMotionPreset.PRECISE
            )
            "plate_scaling" -> clean.copy(
                geometry = PlateGeometry.SQUARE,
                boundary = PlateBoundary.CLAMPED,
                automaticMode = false,
                modeIndex = 3,
                thicknessM = 0.001,
                characteristicSizeM = 0.35,
                showNodes = true,
                sandEnabled = false,
                motionPreset = CymaticsMotionPreset.PRECISE
            )
            "mixed_air_plate" -> clean.copy(
                geometry = PlateGeometry.SQUARE,
                boundary = PlateBoundary.SIMPLY_SUPPORTED,
                automaticMode = false,
                modeIndex = 3,
                showNodes = true,
                showAntinodes = true,
                sandEnabled = false,
                variable = CymaticsVariable.DISPLACEMENT,
                motionPreset = CymaticsMotionPreset.PRECISE
            )
            else -> CymaticsAcademicPractices.all.firstOrNull { it.id == presetId }
                ?.let { CymaticsAcademicPractices.prepare(clean, it.id) }
                ?: clean
        }.sanitized()
    }
}
