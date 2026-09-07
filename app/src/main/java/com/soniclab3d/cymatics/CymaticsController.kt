package com.soniclab3d.cymatics

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class CymaticsController(initial: CymaticsState = CymaticsState()) {
    @Volatile
    var state: CymaticsState = initial.sanitized()
        private set

    private val cameraPreset = AtomicReference<CymaticsCameraPreset?>(null)
    private val sandGeneration = AtomicInteger(0)

    @Synchronized
    fun update(transform: (CymaticsState) -> CymaticsState) {
        state = transform(state).sanitized()
    }

    fun requestCameraPreset(preset: CymaticsCameraPreset) {
        cameraPreset.set(preset)
    }

    fun consumeCameraPreset(): CymaticsCameraPreset? = cameraPreset.getAndSet(null)

    fun resetSand() {
        sandGeneration.incrementAndGet()
    }

    fun sandGeneration(): Int = sandGeneration.get()
}
