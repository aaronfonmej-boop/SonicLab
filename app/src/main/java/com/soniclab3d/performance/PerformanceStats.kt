package com.soniclab3d.performance

import android.content.Context
import android.os.Build
import android.os.PowerManager

data class PerformanceStats(
    val fps: Float = 0f,
    val frameTimeMs: Float = 0f,
    val particleCount: Int = 0,
    val configuredParticleCount: Int = 0,
    val adaptiveFraction: Float = 1f,
    val width: Int = 0,
    val height: Int = 0,
    val usedMemoryMb: Long = 0,
    val simulationTimeS: Double = 0.0,
    val thermalStatus: Int = -1
)

class FpsCounter {
    private var windowStart = System.nanoTime()
    private var frames = 0

    fun frame(
        particleCount: Int,
        configuredParticleCount: Int,
        adaptiveFraction: Float,
        width: Int,
        height: Int,
        simulationTimeS: Double,
        thermalStatus: Int
    ): PerformanceStats? {
        frames++
        val now = System.nanoTime()
        val elapsed = (now - windowStart) / 1_000_000_000.0
        if (elapsed < 0.75) return null
        val fps = (frames / elapsed).toFloat()
        val runtime = Runtime.getRuntime()
        val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L)
        val result = PerformanceStats(
            fps = fps,
            frameTimeMs = if (fps > 0f) 1_000f / fps else 0f,
            particleCount = particleCount,
            configuredParticleCount = configuredParticleCount,
            adaptiveFraction = adaptiveFraction,
            width = width,
            height = height,
            usedMemoryMb = usedMb,
            simulationTimeS = simulationTimeS,
            thermalStatus = thermalStatus
        )
        frames = 0
        windowStart = now
        return result
    }
}

class ThermalMonitor(context: Context) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    fun currentStatus(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        powerManager.currentThermalStatus
    } else {
        -1
    }

    companion object {
        fun label(status: Int): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (status) {
                PowerManager.THERMAL_STATUS_NONE -> "nominal"
                PowerManager.THERMAL_STATUS_LIGHT -> "ligero"
                PowerManager.THERMAL_STATUS_MODERATE -> "moderado"
                PowerManager.THERMAL_STATUS_SEVERE -> "alto"
                PowerManager.THERMAL_STATUS_CRITICAL -> "crítico"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "emergencia"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "apagado"
                else -> "no disponible"
            }
        } else {
            "no disponible"
        }
    }
}
