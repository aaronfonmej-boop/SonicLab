package com.soniclab3d.simulation

import com.soniclab3d.physics.AcousticState
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

enum class CameraPreset { FREE, TOP, FRONT, SIDE }

class SimulationController(initial: AcousticState = AcousticState()) {
    @Volatile
    var state: AcousticState = initial
        private set

    private val frameSteps = AtomicInteger(0)
    private val cameraPreset = AtomicReference<CameraPreset?>(null)
    private val requestedSimulationTime = AtomicReference<Double?>(null)

    @Synchronized
    fun update(transform: (AcousticState) -> AcousticState) {
        state = transform(state)
    }

    fun requestFrameStep() {
        frameSteps.incrementAndGet()
    }

    fun consumeFrameStep(): Boolean {
        while (true) {
            val value = frameSteps.get()
            if (value <= 0) return false
            if (frameSteps.compareAndSet(value, value - 1)) return true
        }
    }

    fun requestCameraPreset(preset: CameraPreset) {
        cameraPreset.set(preset)
    }

    fun consumeCameraPreset(): CameraPreset? = cameraPreset.getAndSet(null)

    fun requestSimulationTime(timeSeconds: Double) {
        requestedSimulationTime.set(timeSeconds.coerceAtLeast(0.0))
    }

    fun consumeSimulationTime(): Double? = requestedSimulationTime.getAndSet(null)
}
