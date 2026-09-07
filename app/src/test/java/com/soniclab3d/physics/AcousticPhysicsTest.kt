package com.soniclab3d.physics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import com.soniclab3d.scientific.ExperimentExporter

class AcousticPhysicsTest {
    @Test
    fun `caso obligatorio 20 C y 120 Hz`() {
        val speed = AcousticPhysics.soundSpeed(20.0)
        val wavelength = AcousticPhysics.wavelength(speed, 120.0)
        val periodMs = AcousticPhysics.periodSeconds(120.0) * 1_000.0

        assertEquals(343.42, speed, 0.001)
        assertEquals(2.861833, wavelength, 0.00001)
        assertEquals(8.333333, periodMs, 0.00001)
    }

    @Test
    fun `omega es 2 pi f`() {
        assertEquals(2.0 * PI * 120.0, AcousticPhysics.angularFrequency(120.0), 1e-9)
    }

    @Test
    fun `k es 2 pi sobre lambda`() {
        val lambda = 343.42 / 120.0
        assertEquals(2.0 * PI / lambda, AcousticPhysics.waveNumber(lambda), 1e-9)
    }

    @Test
    fun `94 dB SPL equivale aproximadamente a un pascal RMS`() {
        assertEquals(1.00237, AcousticPhysics.soundPressureRmsPa(94.0), 0.001)
    }

    @Test
    fun `conversion SPL ida y vuelta`() {
        val pressure = AcousticPhysics.soundPressureRmsPa(70.0)
        assertEquals(70.0, AcousticPhysics.soundPressureLevelDb(pressure), 1e-9)
    }

    @Test
    fun `densidad de aire humedo esta en rango fisico`() {
        val density = AcousticPhysics.airDensity(20.0, 50.0, 101.325)
        assertTrue(density in 1.18..1.22)
    }

    @Test
    fun `absorcion atmosferica es positiva y aumenta con frecuencia`() {
        val low = AcousticPhysics.atmosphericAbsorptionDbPerM(120.0, 20.0, 50.0, 101.325)
        val high = AcousticPhysics.atmosphericAbsorptionDbPerM(10_000.0, 20.0, 50.0, 101.325)
        assertTrue(low >= 0.0)
        assertTrue(high > low)
    }

    @Test
    fun `desplazamiento fisico disminuye al aumentar frecuencia a igual presion`() {
        val low = AcousticPhysics.metrics(AcousticState(frequencyHz = 120.0)).displacementPeakM
        val high = AcousticPhysics.metrics(AcousticState(frequencyHz = 1_200.0)).displacementPeakM
        assertEquals(10.0, low / high, 1e-6)
    }

    @Test
    fun `dos fuentes en oposicion cancelan en el punto medio simetrico`() {
        val state = AcousticState(
            frequencyHz = 120.0,
            phaseRadians = 0f,
            secondarySource = SecondarySourceSettings(
                enabled = true,
                frequencyHz = 120.0,
                levelDbSpl = 70f,
                phaseRadians = PI.toFloat(),
                xM = 2f
            )
        )
        val sample = AcousticPhysics.sampleAt(state, 1.0, 0.0, 0.0, 0.001)
        assertEquals(0.0, sample.pressurePa, 1e-6)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `frecuencia cero se rechaza`() {
        AcousticPhysics.periodSeconds(0.0)
    }

    @Test
    fun `primer modo axial de sala usa c sobre dos L`() {
        val room = RectangularRoom(lengthM = 6.0, widthM = 4.0, heightM = 3.0, modeX = 1)
        assertEquals(343.42 / 12.0, RoomAcoustics.modeFrequencyHz(room, 343.42), 1e-9)
        assertEquals("Axial", RoomAcoustics.modeType(room))
    }

    @Test
    fun `modo axial tiene signos opuestos en paredes enfrentadas`() {
        val room = RectangularRoom(lengthM = 6.0, widthM = 4.0, heightM = 3.0, modeX = 1)
        assertEquals(1.0, RoomAcoustics.normalizedPressure(room, 0.0, 2.0, 1.5), 1e-9)
        assertEquals(-1.0, RoomAcoustics.normalizedPressure(room, 6.0, 2.0, 1.5), 1e-9)
    }

    @Test
    fun `exportacion CSV declara unidades y modelo`() {
        val csv = ExperimentExporter.csv(AcousticState(), 0.0, samples = 3)
        assertTrue(csv.contains("distancia_m,presion_Pa"))
        assertTrue(csv.contains("campo libre analítico"))
        assertEquals(9, csv.lineSequence().filter { it.isNotBlank() }.count())
    }
}
