package com.soniclab3d.cymatics

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class CymaticsMetrics(
    val modeIndex: Int,
    val naturalFrequencyHz: Double,
    val response: Double,
    val coupling: Double,
    val relativeDetuning: Double,
    val flexuralRigidityNm: Double,
    val normalizedAmplitude: Double,
    val firstFrequencyHz: Double,
    val lastFrequencyHz: Double
)

object CymaticsPhysics {
    fun flexuralRigidity(state: CymaticsState): Double {
        val clean = state.sanitized()
        return clean.youngModulusPa * clean.thicknessM.pow(3.0) /
            (12.0 * (1.0 - clean.poissonRatio * clean.poissonRatio))
    }

    fun naturalFrequencyHz(state: CymaticsState, eigenvalue: Double): Double {
        val clean = state.sanitized()
        require(eigenvalue.isFinite() && eigenvalue > 0.0)
        val rigidity = flexuralRigidity(clean)
        val arealMass = clean.densityKgM3 * clean.thicknessM
        val angular = 4.0 / (clean.characteristicSizeM * clean.characteristicSizeM) *
            sqrt(rigidity / arealMass) * sqrt(eigenvalue)
        return angular / (2.0 * PI)
    }

    fun responseMagnitude(driveFrequencyHz: Double, naturalFrequencyHz: Double, dampingRatio: Double): Double {
        if (!driveFrequencyHz.isFinite() || !naturalFrequencyHz.isFinite() || naturalFrequencyHz <= 0.0) return 0.0
        val ratio = driveFrequencyHz / naturalFrequencyHz
        val damping = dampingRatio.coerceIn(0.002, 0.30)
        return 1.0 / sqrt((1.0 - ratio * ratio).pow(2.0) + (2.0 * damping * ratio).pow(2.0))
    }

    fun metrics(state: CymaticsState, bank: CymaticsModeBank): CymaticsMetrics {
        val clean = state.sanitized()
        val frequencies = DoubleArray(bank.eigenvalues.size) { index ->
            naturalFrequencyHz(clean, bank.eigenvalues[index].toDouble())
        }
        val mode = if (clean.automaticMode) {
            frequencies.indices.maxByOrNull { index ->
                val coupling = abs(bank.sample(index, clean.driveX, clean.driveY)).toDouble()
                responseMagnitude(clean.driveFrequencyHz, frequencies[index], clean.dampingRatio) * coupling
            } ?: 0
        } else {
            clean.modeIndex.coerceIn(frequencies.indices)
        }
        val natural = frequencies[mode]
        val coupling = abs(bank.sample(mode, clean.driveX, clean.driveY)).toDouble().coerceIn(0.0, 1.0)
        val rawResponse = responseMagnitude(clean.driveFrequencyHz, natural, clean.dampingRatio)
        val normalizedResponse = (2.0 * clean.dampingRatio * rawResponse).coerceIn(0.0, 1.0)
        val amplitude = (normalizedResponse * coupling * clean.normalizedDrive).coerceIn(0.0, 1.0)
        return CymaticsMetrics(
            modeIndex = mode,
            naturalFrequencyHz = natural,
            response = normalizedResponse,
            coupling = coupling,
            relativeDetuning = abs(clean.driveFrequencyHz - natural) / natural,
            flexuralRigidityNm = flexuralRigidity(clean),
            normalizedAmplitude = amplitude,
            firstFrequencyHz = frequencies.first(),
            lastFrequencyHz = frequencies.last()
        )
    }

    fun squareSimplySupportedAnalyticalEigenvalue(m: Int, n: Int, sideInNormalizedCoordinates: Double = 1.8): Double {
        require(m >= 1 && n >= 1 && sideInNormalizedCoordinates > 0.0)
        val laplacianEigenvalue = (PI / sideInNormalizedCoordinates).pow(2.0) * (m * m + n * n)
        return laplacianEigenvalue * laplacianEigenvalue
    }
}
