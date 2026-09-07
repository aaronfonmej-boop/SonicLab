package com.soniclab3d.cymatics

import kotlin.math.ln

object CymaticsMotion {
    fun applyPreset(state: CymaticsState, preset: CymaticsMotionPreset): CymaticsState = when (preset) {
        CymaticsMotionPreset.PRECISE -> state.copy(
            motionPreset = preset,
            motionIntensity = 0.18f,
            modeTransitionSeconds = 0.28f,
            cameraDriftEnabled = false,
            cameraMotion = CymaticsCameraMotion.STATIC,
            orbitingLightEnabled = false,
            resonanceAuraEnabled = false,
            atmosphericDustEnabled = false,
            sandFlowIntensity = 0.20f,
            stageEnabled = true,
            nodalStreamsEnabled = false,
            energyPulsesEnabled = false,
            postProcessingEnabled = false,
            bloomIntensity = 0f
        )
        CymaticsMotionPreset.FLUID -> state.copy(
            motionPreset = preset,
            motionIntensity = 0.58f,
            modeTransitionSeconds = 0.68f,
            cameraDriftEnabled = false,
            cameraMotion = CymaticsCameraMotion.FLOAT,
            orbitingLightEnabled = true,
            resonanceAuraEnabled = true,
            atmosphericDustEnabled = false,
            sandFlowIntensity = 0.58f,
            stageEnabled = true,
            nodalStreamsEnabled = true,
            energyPulsesEnabled = true,
            postProcessingEnabled = true,
            bloomIntensity = 0.62f
        )
        CymaticsMotionPreset.CINEMATIC -> state.copy(
            motionPreset = preset,
            motionIntensity = 0.88f,
            modeTransitionSeconds = 0.95f,
            cameraDriftEnabled = true,
            cameraMotion = CymaticsCameraMotion.SHOW,
            orbitingLightEnabled = true,
            resonanceAuraEnabled = true,
            atmosphericDustEnabled = true,
            sandFlowIntensity = 0.84f,
            stageEnabled = true,
            nodalStreamsEnabled = true,
            energyPulsesEnabled = true,
            postProcessingEnabled = true,
            bloomIntensity = 0.92f
        )
    }.sanitized()

    fun effectiveIntensity(state: CymaticsState): Float =
        if (state.reduceMotion) 0f else state.motionIntensity.coerceIn(0f, 1f)

    fun visualCyclesPerSecond(state: CymaticsState): Float {
        if (state.reduceMotion || state.paused) return 0f
        val clean = state.sanitized()
        val logPosition = (
            ln(clean.driveFrequencyHz / 20.0) / ln(20_000.0 / 20.0)
            ).toFloat().coerceIn(0f, 1f)
        val perceptualBase = 0.34f + 0.32f * logPosition
        val speedFactor = (clean.visualTimeScale / 0.08f).coerceIn(0.08f, 3.2f)
        return (perceptualBase * speedFactor).coerceIn(0.03f, 2.15f)
    }

    fun transitionProgress(elapsedSeconds: Double, state: CymaticsState): Float {
        if (state.reduceMotion) return 1f
        val duration = state.modeTransitionSeconds.coerceIn(0.12f, 2.4f)
        val linear = (elapsedSeconds / duration).toFloat().coerceIn(0f, 1f)
        return smootherStep(linear)
    }

    fun sandTransitionProgress(elapsedSeconds: Double, state: CymaticsState): Float {
        if (state.reduceMotion) return 1f
        val duration = (state.modeTransitionSeconds * 0.62f).coerceIn(0.16f, 0.8f)
        val linear = (elapsedSeconds / duration).toFloat().coerceIn(0f, 1f)
        return smootherStep(linear)
    }

    fun resonanceEnergy(metrics: CymaticsMetrics): Float =
        (metrics.normalizedAmplitude * 0.72 + metrics.response * metrics.coupling * 0.28)
            .toFloat()
            .coerceIn(0f, 1f)

    fun blendModalData(previous: FloatArray, current: FloatArray, progress: Float): FloatArray {
        require(previous.size == current.size) { "Los campos modales deben tener el mismo tamaño" }
        val blend = progress.coerceIn(0f, 1f)
        return FloatArray(current.size) { index ->
            previous[index] + (current[index] - previous[index]) * blend
        }
    }

    private fun smootherStep(value: Float): Float =
        value * value * value * (value * (value * 6f - 15f) + 10f)
}
