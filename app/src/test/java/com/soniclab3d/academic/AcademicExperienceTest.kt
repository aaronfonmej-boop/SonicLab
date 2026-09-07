package com.soniclab3d.academic

import com.soniclab3d.particles.ParticleQuality
import com.soniclab3d.physics.AcousticState
import com.soniclab3d.physics.VisualVariable
import com.soniclab3d.cymatics.CymaticsState
import com.soniclab3d.cymatics.CymaticsCameraMotion
import com.soniclab3d.cymatics.CymaticsMotionPreset
import com.soniclab3d.cymatics.PlateBoundary
import com.soniclab3d.cymatics.PlateGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademicExperienceTest {
    @Test
    fun practicesHaveStableUniqueIdentifiers() {
        assertEquals(4, AcademicPractices.all.size)
        assertEquals(4, AcademicPractices.all.map { it.id }.toSet().size)
        assertTrue(AcademicPractices.all.all { it.question.isNotBlank() && it.defaultRubric.isNotBlank() })
        assertEquals(4, CymaticsAcademicPractices.all.size)
        assertEquals(4, CymaticsAcademicPractices.all.map { it.id }.toSet().size)
        assertTrue(CymaticsAcademicPractices.all.all { it.id.startsWith("cymatics_") })
    }

    @Test
    fun interferencePracticePreparesTwoCoherentSources() {
        val prepared = AcademicPractices.prepare(AcousticState(), "interference")

        assertEquals(120.0, prepared.frequencyHz, 0.0)
        assertTrue(prepared.secondarySource.enabled)
        assertEquals(prepared.frequencyHz, prepared.secondarySource.frequencyHz, 0.0)
        assertEquals(prepared.levelDbSpl, prepared.secondarySource.levelDbSpl, 0f)
        assertTrue(prepared.showPressureSlice)
        assertFalse(prepared.harmonicMode)
    }

    @Test
    fun harmonicPracticeKeepsFundamentalAndSixteenAmplitudes() {
        val prepared = AcademicPractices.prepare(AcousticState(), "harmonics")

        assertEquals(120.0, prepared.frequencyHz, 0.0)
        assertTrue(prepared.harmonicMode)
        assertEquals(16, prepared.harmonicAmplitudes.size)
        assertEquals(1f, prepared.harmonicAmplitudes.first(), 0f)
        assertFalse(prepared.secondarySource.enabled)
    }

    @Test
    fun teacherLocksOnlyConfiguredAcademicParameters() {
        val baselineState = AcademicPractices.prepare(AcousticState(), "frequency_wavelength")
        val activity = TeacherActivity(
            active = true,
            practiceId = "frequency_wavelength",
            locks = AcademicLocks(frequency = true, level = false, environment = true, sources = false),
            baseline = AcademicSceneBaseline.from(baselineState)
        )
        val candidate = baselineState.copy(
            frequencyHz = 440.0,
            levelDbSpl = 84f,
            temperatureC = 35.0,
            visualVariable = VisualVariable.VELOCITY,
            quality = ParticleQuality.NORMAL
        )

        val enforced = activity.enforce(candidate)

        assertEquals(1_000.0, enforced.frequencyHz, 0.0)
        assertEquals(20.0, enforced.temperatureC, 0.0)
        assertEquals(84f, enforced.levelDbSpl, 0f)
        assertEquals(VisualVariable.VELOCITY, enforced.visualVariable)
        assertEquals(ParticleQuality.NORMAL, enforced.quality)
    }

    @Test
    fun inactiveTeacherActivityDoesNotModifyScene() {
        val candidate = AcousticState(frequencyHz = 528.0, levelDbSpl = 63f)
        assertEquals(candidate, TeacherActivity(active = false).enforce(candidate))
    }

    @Test
    fun cymaticsTeacherLocksOnlyConfiguredPlateParameters() {
        val baseline = CymaticsAcademicPractices.prepare(CymaticsState(), "cymatics_geometry")
        val activity = CymaticsTeacherActivity(
            active = true,
            practiceId = "cymatics_geometry",
            locks = CymaticsAcademicLocks(
                geometryAndBoundary = true,
                materialAndDimensions = false,
                frequencyAndMode = true,
                exciter = false,
                visualAids = false
            ),
            baseline = CymaticsSceneBaseline(baseline)
        )
        val candidate = baseline.copy(
            geometry = PlateGeometry.CIRCLE,
            boundary = PlateBoundary.CLAMPED,
            driveFrequencyHz = 528.0,
            driveX = -0.7f,
            youngModulusPa = 3.0e9,
            showGrid = true
        )

        val enforced = activity.enforce(candidate)

        assertEquals(baseline.geometry, enforced.geometry)
        assertEquals(baseline.boundary, enforced.boundary)
        assertEquals(baseline.driveFrequencyHz, enforced.driveFrequencyHz, 0.0)
        assertEquals(-0.7f, enforced.driveX, 0f)
        assertEquals(3.0e9, enforced.youngModulusPa, 0.0)
        assertTrue(enforced.showGrid)
    }

    @Test
    fun cymaticsBaselineCodecRoundTripsAllLockableValues() {
        val baseline = CymaticsSceneBaseline(
            CymaticsAcademicPractices.prepare(CymaticsState(), "cymatics_coupling").copy(
                motionPreset = CymaticsMotionPreset.FLUID,
                motionIntensity = 0.47f,
                modeTransitionSeconds = 1.37f,
                cameraDriftEnabled = false,
                atmosphericDustEnabled = false,
                sandFlowIntensity = 0.63f,
                cameraMotion = CymaticsCameraMotion.ORBIT,
                stageEnabled = false,
                nodalStreamsEnabled = false,
                energyPulsesEnabled = false,
                postProcessingEnabled = false,
                bloomIntensity = 0.37f
            )
        )

        val decoded = CymaticsBaselineCodec.decode(CymaticsBaselineCodec.encode(baseline))

        assertEquals(baseline, decoded)
        assertEquals(null, CymaticsBaselineCodec.decode("corrupt"))
    }

    @Test
    fun cymaticsBaselineCodecKeepsVersionFourScenesCompatible() {
        val currentFields = CymaticsBaselineCodec.encode(CymaticsSceneBaseline()).split('\t')
        val legacy = currentFields.take(46).toMutableList().also { it[0] = "4" }.joinToString("\t")

        val decoded = CymaticsBaselineCodec.decode(legacy)

        assertTrue(decoded != null)
        assertEquals(CymaticsCameraMotion.SHOW, decoded?.state?.cameraMotion)
        assertTrue(decoded?.state?.stageEnabled == true)
        assertTrue(decoded?.state?.postProcessingEnabled == true)
        assertEquals(0.92f, decoded?.state?.bloomIntensity ?: -1f, 0f)
    }

    @Test
    fun cymaticsVisualLockIncludesCinematicMotionConfiguration() {
        val baseline = CymaticsState(
            motionPreset = CymaticsMotionPreset.PRECISE,
            motionIntensity = 0.18f,
            modeTransitionSeconds = 0.28f,
            cameraDriftEnabled = false,
            cameraMotion = CymaticsCameraMotion.STATIC,
            atmosphericDustEnabled = false,
            stageEnabled = false,
            nodalStreamsEnabled = false,
            energyPulsesEnabled = false,
            postProcessingEnabled = false,
            bloomIntensity = 0.11f
        )
        val activity = CymaticsTeacherActivity(
            active = true,
            locks = CymaticsAcademicLocks(
                geometryAndBoundary = false,
                materialAndDimensions = false,
                frequencyAndMode = false,
                visualAids = true
            ),
            baseline = CymaticsSceneBaseline(baseline)
        )

        val enforced = activity.enforce(CymaticsState())

        assertEquals(CymaticsMotionPreset.PRECISE, enforced.motionPreset)
        assertEquals(0.18f, enforced.motionIntensity, 0f)
        assertFalse(enforced.cameraDriftEnabled)
        assertFalse(enforced.atmosphericDustEnabled)
        assertEquals(CymaticsCameraMotion.STATIC, enforced.cameraMotion)
        assertFalse(enforced.stageEnabled)
        assertFalse(enforced.nodalStreamsEnabled)
        assertFalse(enforced.energyPulsesEnabled)
        assertFalse(enforced.postProcessingEnabled)
        assertEquals(0.11f, enforced.bloomIntensity, 0f)
    }

    @Test
    fun baselineCodecRoundTripsAllLockableValues() {
        val baseline = AcademicSceneBaseline.from(
            AcademicPractices.prepare(
                AcousticState(frequencyHz = 432.0, temperatureC = 26.5),
                "interference"
            )
        )

        val decoded = AcademicBaselineCodec.decode(AcademicBaselineCodec.encode(baseline))

        assertEquals(baseline, decoded)
        assertEquals(null, AcademicBaselineCodec.decode("corrupt"))
    }

    @Test
    fun answerCodecPreservesUnicodeAndLineBreaks() {
        val answers = mapOf(
            "wave_particle" to "La partícula oscila alrededor del equilibrio.\nλ no es trayectoria.",
            "harmonics" to "Armónicos 2f, 3f y 4f"
        )

        val decoded = AcademicTextMapCodec.decode(AcademicTextMapCodec.encode(answers))

        assertEquals(answers, decoded)
        assertNotEquals(answers["wave_particle"], AcademicTextMapCodec.encode(answers))
    }

    @Test
    fun academicExportLabelsSceneAsSimulationNotMeasurement() {
        val experience = AcademicExperienceState(
            completedPracticeIds = setOf("wave_particle"),
            answers = mapOf("wave_particle" to "Respuesta con \"evidencia\"")
        )

        val json = AcademicExperienceExporter.json(experience, AcousticState(), 1.25)

        assertTrue(json.contains("\"calibratedMeasurement\": false"))
        assertTrue(json.contains("\"format\": \"SonicLab academic experience 1.1\""))
        assertTrue(json.contains("\"schemaVersion\": 3"))
        assertTrue(json.contains("\"continuousLearning\""))
        assertTrue(json.contains("\"model\": \"analytical-simulation\""))
        assertTrue(json.contains("Respuesta con \\\"evidencia\\\""))
        assertFalse(json.contains("calibratedMeasurement\": true"))
        assertFalse(json.contains("\"expectedAnswer\""))
        assertFalse(json.contains("\"rubric\""))
    }

    @Test
    fun teacherExportKeepsPrivateEvaluationInTeacherRoleOnly() {
        val json = AcademicExperienceExporter.json(
            AcademicExperienceState(role = AcademicRole.TEACHER),
            AcousticState(),
            0.0
        )

        assertTrue(json.contains("\"expectedAnswer\""))
        assertTrue(json.contains("\"rubric\""))
    }
}
