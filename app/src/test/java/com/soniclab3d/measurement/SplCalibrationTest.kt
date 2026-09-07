package com.soniclab3d.measurement

import com.soniclab3d.audio.AudioInputRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SplCalibrationTest {
    private val builtInRoute = AudioInputRoute(
        deviceId = 7,
        deviceType = 15,
        productName = "Micrófono del teléfono",
        address = "bottom",
        sampleRateHz = 48_000,
        channelCount = 1,
        encoding = 2,
        audioSource = 9,
        bufferSizeBytes = 8_192
    )

    @Test
    fun stableFiveSecondCaptureCreatesExpectedOffset() {
        var now = 1_000L
        val controller = SplCalibrationCaptureController { now }
        controller.start(builtInRoute, 94.0, "Calibrador externo", now)

        var finalUpdate = SplCalibrationCaptureUpdate(controller.state)
        for (index in 0..25) {
            now = 1_000L + index * 200L
            val sample = -30.0 + if (index % 2 == 0) 0.1 else -0.1
            finalUpdate = controller.addSample(sample, builtInRoute, now)
        }

        val profile = finalUpdate.completedProfile
        assertNotNull(profile)
        assertFalse(finalUpdate.state.active)
        assertEquals(-30.0, profile!!.capturedDbFs, 0.05)
        assertEquals(124.0, profile.offsetDb, 0.05)
        assertEquals(94.0, profile.calibratedSplDb(-30.0, builtInRoute)!!, 0.05)
    }

    @Test
    fun profileOnlyAppliesToExactCurrentRouteAndConfiguration() {
        val profile = profileFor(builtInRoute)
        val changedRate = builtInRoute.copy(sampleRateHz = 44_100)
        val changedDevice = builtInRoute.copy(deviceId = 8)

        assertNotNull(profile.calibratedSplDb(-30.0, builtInRoute))
        assertNull(profile.calibratedSplDb(-30.0, changedRate))
        assertNull(profile.calibratedSplDb(-30.0, changedDevice))
        assertNull(profile.copy(valid = false).calibratedSplDb(-30.0, builtInRoute))
    }

    @Test
    fun routeChangeCancelsCaptureWithoutCreatingProfile() {
        val controller = SplCalibrationCaptureController { 1_000L }
        controller.start(builtInRoute, 94.0, "Referencia externa", 1_000L)

        val update = controller.addSample(
            relativeDbFs = -30.0,
            route = builtInRoute.copy(deviceId = 99),
            nowEpochMs = 1_200L
        )

        assertNull(update.completedProfile)
        assertFalse(update.state.active)
        assertTrue(update.state.error.orEmpty().contains("ruta de audio cambió"))
    }

    @Test
    fun unstableSignalIsRejected() {
        val controller = SplCalibrationCaptureController { 1_000L }
        controller.start(builtInRoute, 94.0, "Referencia externa", 1_000L)

        var update = SplCalibrationCaptureUpdate(controller.state)
        for (index in 0..25) {
            update = controller.addSample(
                relativeDbFs = if (index % 2 == 0) -20.0 else -40.0,
                route = builtInRoute,
                nowEpochMs = 1_000L + index * 200L
            )
        }

        assertNull(update.completedProfile)
        assertTrue(update.state.error.orEmpty().contains("señal varió"))
    }

    @Test
    fun profileCodecPreservesTraceabilityAndValidity() {
        val original = profileFor(builtInRoute).copy(
            method = "Sonómetro USB / 1 kHz",
            valid = false,
            invalidatedAtEpochMs = 1_700_000_100_000L
        )

        val decoded = SplCalibrationCodec.decode(SplCalibrationCodec.encode(listOf(original)))

        assertEquals(listOf(original), decoded)
    }

    @Test
    fun invalidReferenceDoesNotStartCapture() {
        val controller = SplCalibrationCaptureController { 1_000L }

        val state = controller.start(builtInRoute, 180.0, "Referencia", 1_000L)

        assertFalse(state.active)
        assertTrue(state.error.orEmpty().contains("20 y 140"))
    }

    private fun profileFor(route: AudioInputRoute) = SplCalibrationProfile(
        id = "profile-1",
        routeKey = route.calibrationKey,
        route = route,
        referenceSplDb = 94.0,
        capturedDbFs = -30.0,
        offsetDb = 124.0,
        method = "Calibrador externo",
        createdAtEpochMs = 1_700_000_000_000L
    )
}
