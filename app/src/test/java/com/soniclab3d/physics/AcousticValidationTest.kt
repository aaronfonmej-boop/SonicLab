package com.soniclab3d.physics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max

/**
 * Casos de referencia independientes para el modelo analítico.
 *
 * Los valores esperados se calcularon fuera del código productivo a partir de las
 * ecuaciones publicadas en docs/VALIDATION.md. Estas pruebas no llaman a metrics(),
 * soundPressurePeakPa() ni atmosphericAbsorptionDbPerM() para construir el resultado
 * esperado de sampleAt().
 */
class AcousticValidationTest {
    @Test
    fun `condicion obligatoria de 20 C y 120 Hz`() {
        val state = referenceState()

        assertRelative(343.42, state.soundSpeedMps, relativeTolerance = 1e-12)
        assertRelative(2.8618333333333337, state.soundSpeedMps / state.frequencyHz, relativeTolerance = 1e-12)
        assertRelative(8.333333333333334, 1_000.0 / state.frequencyHz, relativeTolerance = 1e-12)
    }

    @Test
    fun `onda armonica progresiva local a un metro`() {
        val state = referenceState()
        val targetPhase = PI / 4.0
        val timeS = (REFERENCE_WAVE_NUMBER_RAD_M - targetPhase) / REFERENCE_OMEGA_RAD_S

        val sample = AcousticPhysics.sampleAt(state, 1.0, 0.0, 0.0, timeS)

        assertRelative(0.0632455532033676, sample.pressurePa, relativeTolerance = 2e-7)
        assertRelative(0.00015361784576561864, sample.particleVelocityXMps, relativeTolerance = 5e-7)
        assertRelative(2.0374199583938832e-7, sample.displacementXM, relativeTolerance = 5e-7)
        assertEquals(0.0, sample.particleVelocityYMps, 0.0)
        assertEquals(0.0, sample.particleVelocityZMps, 0.0)
        assertRelative(targetPhase, sample.phaseRadians, relativeTolerance = 1e-10)
    }

    @Test
    fun `fuente esferica usa radio regularizado antes de un metro`() {
        val radiusM = 0.25
        val state = referenceState(
            phaseRadians = (PI / 2.0 - REFERENCE_WAVE_NUMBER_RAD_M * radiusM).toFloat()
        )

        val sample = AcousticPhysics.sampleAt(state, radiusM, 0.0, 0.0, 0.0)

        assertRelative(0.3248526724187956, sample.pressurePa, relativeTolerance = 5e-7)
        assertRelative(0.0007890383623922895, sample.particleVelocityXMps, relativeTolerance = 8e-7)
        assertEquals(0.0, sample.displacementXM, 2e-13)
    }

    @Test
    fun `fuente esferica combina divergencia y absorcion despues de un metro`() {
        val radiusM = 4.0
        val state = referenceState(
            phaseRadians = (PI / 2.0 - REFERENCE_WAVE_NUMBER_RAD_M * radiusM).toFloat()
        )

        val sample = AcousticPhysics.sampleAt(state, radiusM, 0.0, 0.0, 0.0)

        assertRelative(0.0225077933210979, sample.pressurePa, relativeTolerance = 8e-6)
    }

    @Test
    fun `la regularizacion mantiene finito el centro de la fuente`() {
        val state = referenceState(phaseRadians = (PI / 2.0).toFloat())

        val center = AcousticPhysics.sampleAt(state, 0.0, 0.0, 0.0, 0.0)
        val nearCenter = AcousticPhysics.sampleAt(state, 1e-9, 0.0, 0.0, 0.0)

        assertTrue(center.pressurePa.isFinite())
        assertTrue(nearCenter.pressurePa.isFinite())
        assertRelative(0.7507033738804934, center.pressurePa, relativeTolerance = 5e-7)
        assertEquals(0.0, center.particleVelocityMagnitudeMps, 0.0)
        assertEquals(0.0, center.displacementMagnitudeM, 0.0)
        assertRelative(center.pressurePa, nearCenter.pressurePa, relativeTolerance = 1e-9)
    }

    @Test
    fun `modo rectangular tangencial coincide con la solucion cerrada`() {
        val room = RectangularRoom(
            lengthM = 6.0,
            widthM = 4.0,
            heightM = 3.0,
            modeX = 1,
            modeY = 1,
            modeZ = 0
        )

        val frequencyHz = RoomAcoustics.modeFrequencyHz(room, REFERENCE_SOUND_SPEED_M_S)
        val normalizedPressure = RoomAcoustics.normalizedPressure(room, 1.5, 1.0, 1.5)

        assertRelative(51.592434125826806, frequencyHz, relativeTolerance = 1e-12)
        assertRelative(0.5, normalizedPressure, relativeTolerance = 1e-12)
        assertEquals("Tangencial", RoomAcoustics.modeType(room))
    }

    private fun referenceState(phaseRadians: Float = 0f): AcousticState = AcousticState(
        frequencyHz = 120.0,
        levelDbSpl = 70f,
        phaseRadians = phaseRadians,
        temperatureC = 20.0,
        humidityPercent = 50f,
        atmosphericPressureKPa = 101.325f,
        sourceRadiusM = 0.12f,
        waveform = Waveform.SINE,
        harmonicMode = false,
        secondarySource = SecondarySourceSettings(enabled = false)
    )

    private fun assertRelative(
        expected: Double,
        actual: Double,
        relativeTolerance: Double,
        absoluteTolerance: Double = 1e-15
    ) {
        val allowedError = max(abs(expected) * relativeTolerance, absoluteTolerance)
        assertEquals(
            "Esperado=$expected, calculado=$actual, tolerancia_absoluta=$allowedError",
            expected,
            actual,
            allowedError
        )
    }

    private companion object {
        const val REFERENCE_SOUND_SPEED_M_S = 343.42
        const val REFERENCE_OMEGA_RAD_S = 753.9822368615503
        const val REFERENCE_WAVE_NUMBER_RAD_M = 2.1955105610085326
    }
}
