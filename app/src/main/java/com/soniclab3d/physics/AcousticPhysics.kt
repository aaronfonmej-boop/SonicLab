package com.soniclab3d.physics

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object AcousticPhysics {
    const val REFERENCE_PRESSURE_PA = 20e-6
    private const val REFERENCE_DISTANCE_M = 1.0
    private const val REFERENCE_PRESSURE_KPA = 101.325

    fun soundSpeed(temperatureC: Double): Double = 331.3 + 0.606 * temperatureC

    fun wavelength(soundSpeedMps: Double, frequencyHz: Double): Double {
        require(frequencyHz > 0.0) { "La frecuencia debe ser positiva" }
        return soundSpeedMps / frequencyHz
    }

    fun periodSeconds(frequencyHz: Double): Double {
        require(frequencyHz > 0.0) { "La frecuencia debe ser positiva" }
        return 1.0 / frequencyHz
    }

    fun angularFrequency(frequencyHz: Double): Double = 2.0 * PI * frequencyHz

    fun waveNumber(wavelengthM: Double): Double {
        require(wavelengthM > 0.0) { "La longitud de onda debe ser positiva" }
        return 2.0 * PI / wavelengthM
    }

    fun soundPressureRmsPa(levelDbSpl: Double): Double =
        REFERENCE_PRESSURE_PA * 10.0.pow(levelDbSpl / 20.0)

    fun soundPressurePeakPa(levelDbSpl: Double): Double = soundPressureRmsPa(levelDbSpl) * sqrt(2.0)

    fun soundPressureLevelDb(pressureRmsPa: Double): Double {
        require(pressureRmsPa > 0.0) { "La presión RMS debe ser positiva" }
        return 20.0 * log10(pressureRmsPa / REFERENCE_PRESSURE_PA)
    }

    fun saturationVaporPressurePa(temperatureC: Double): Double =
        610.94 * exp(17.625 * temperatureC / (temperatureC + 243.04))

    fun airDensity(temperatureC: Double, humidityPercent: Double, pressureKPa: Double): Double {
        val temperatureK = temperatureC + 273.15
        val totalPressurePa = pressureKPa * 1_000.0
        val vaporPressurePa = (humidityPercent.coerceIn(0.0, 100.0) / 100.0) *
            saturationVaporPressurePa(temperatureC).coerceAtMost(totalPressurePa * 0.2)
        val dryPressurePa = totalPressurePa - vaporPressurePa
        return dryPressurePa / (287.058 * temperatureK) + vaporPressurePa / (461.495 * temperatureK)
    }

    /**
     * Aproximación de ISO 9613-1 para absorción atmosférica de tonos puros.
     * El resultado es atenuación de amplitud en dB por metro.
     */
    fun atmosphericAbsorptionDbPerM(
        frequencyHz: Double,
        temperatureC: Double,
        humidityPercent: Double,
        pressureKPa: Double
    ): Double {
        require(frequencyHz > 0.0)
        val temperatureK = temperatureC + 273.15
        val referenceTemperatureK = 293.15
        val pressureRatio = (pressureKPa / REFERENCE_PRESSURE_KPA).coerceAtLeast(0.2)
        val saturationExponent = -6.8346 * (273.16 / temperatureK).pow(1.261) + 4.6151
        val saturationRatio = 10.0.pow(saturationExponent)
        val molarWaterPercent = humidityPercent.coerceIn(0.0, 100.0) * saturationRatio / pressureRatio
        val oxygenRelaxation = pressureRatio * (
            24.0 + 4.04e4 * molarWaterPercent * (0.02 + molarWaterPercent) /
                (0.391 + molarWaterPercent)
            )
        val nitrogenRelaxation = pressureRatio * (temperatureK / referenceTemperatureK).pow(-0.5) * (
            9.0 + 280.0 * molarWaterPercent *
                exp(-4.17 * ((temperatureK / referenceTemperatureK).pow(-1.0 / 3.0) - 1.0))
            )
        val f2 = frequencyHz * frequencyHz
        val classical = 1.84e-11 * pressureRatio.pow(-1.0) *
            sqrt(temperatureK / referenceTemperatureK)
        val relaxation = (temperatureK / referenceTemperatureK).pow(-2.5) * (
            0.01275 * exp(-2239.1 / temperatureK) /
                (oxygenRelaxation + f2 / oxygenRelaxation) +
                0.1068 * exp(-3352.0 / temperatureK) /
                (nitrogenRelaxation + f2 / nitrogenRelaxation)
            )
        return (8.686 * f2 * (classical + relaxation)).coerceAtLeast(0.0)
    }

    fun metrics(state: AcousticState): AcousticMetrics {
        val speed = state.soundSpeedMps
        val wavelength = wavelength(speed, state.frequencyHz)
        val density = state.airDensityKgM3
        val pressureRms = soundPressureRmsPa(state.levelDbSpl.toDouble())
        val pressurePeak = pressureRms * sqrt(2.0)
        val particleVelocityPeak = pressurePeak / (density * speed)
        val displacementPeak = particleVelocityPeak / angularFrequency(state.frequencyHz)
        return AcousticMetrics(
            soundSpeedMps = speed,
            airDensityKgM3 = density,
            wavelengthM = wavelength,
            periodMs = periodSeconds(state.frequencyHz) * 1_000.0,
            angularFrequencyRadS = angularFrequency(state.frequencyHz),
            waveNumberRadM = waveNumber(wavelength),
            pressureRmsPa = pressureRms,
            pressurePeakPa = pressurePeak,
            particleVelocityPeakMps = particleVelocityPeak,
            displacementPeakM = displacementPeak,
            intensityWm2 = pressureRms * pressureRms / (density * speed),
            absorptionDbPerM = atmosphericAbsorptionDbPerM(
                state.frequencyHz,
                state.temperatureC,
                state.humidityPercent.toDouble(),
                state.atmosphericPressureKPa.toDouble()
            )
        )
    }

    /**
     * Muestrea el modelo analítico en coordenadas cartesianas expresadas en metros.
     *
     * La fase de una fuente saliente sigue `k r - omega t + phi`. La presión y la
     * velocidad de partícula radial están en fase para la aproximación progresiva
     * local; el desplazamiento usa el coseno para que su derivada temporal sea la
     * velocidad. El nivel de la fuente se referencia a 1 m y la singularidad se
     * regulariza con [AcousticState.sourceRadiusM]. Ninguna escala visual interviene
     * en este cálculo.
     */
    fun sampleAt(state: AcousticState, xM: Double, yM: Double, zM: Double, timeS: Double): AcousticSample {
        val metrics = metrics(state)
        var pressure = 0.0
        var velocityX = 0.0
        var velocityY = 0.0
        var velocityZ = 0.0
        var displacementX = 0.0
        var displacementY = 0.0
        var displacementZ = 0.0

        fun addSource(
            sourceX: Double,
            sourceY: Double,
            sourceZ: Double,
            frequencyHz: Double,
            levelDbSpl: Double,
            phaseOffset: Double,
            harmonicAmplitudes: List<Float>?,
            waveform: Waveform
        ) {
            val dx = xM - sourceX
            val dy = yM - sourceY
            val dz = zM - sourceZ
            val radius = sqrt(dx * dx + dy * dy + dz * dz)
            val regularizedRadius = sqrt(radius * radius + state.sourceRadiusM * state.sourceRadiusM)
            val referenceRadius = sqrt(REFERENCE_DISTANCE_M * REFERENCE_DISTANCE_M + state.sourceRadiusM * state.sourceRadiusM)
            val dirScale = if (radius > 1e-9) 1.0 / radius else 0.0
            val dirX = dx * dirScale
            val dirY = dy * dirScale
            val dirZ = dz * dirScale
            val absorptionDb = atmosphericAbsorptionDbPerM(
                frequencyHz,
                state.temperatureC,
                state.humidityPercent.toDouble(),
                state.atmosphericPressureKPa.toDouble()
            )
            val absorptionNepers = absorptionDb / 8.685889638
            val envelope = referenceRadius / regularizedRadius *
                exp(-absorptionNepers * max(radius - REFERENCE_DISTANCE_M, 0.0))
            val pressurePeak = soundPressurePeakPa(levelDbSpl)
            val omega = angularFrequency(frequencyHz)
            val basePhase = omega / metrics.soundSpeedMps * radius - omega * timeS + phaseOffset

            var pressureNorm = 0.0
            var velocityNorm = 0.0
            var displacementNorm = 0.0
            if (harmonicAmplitudes != null) {
                harmonicAmplitudes.take(16).forEachIndexed { index, amplitude ->
                    val order = index + 1.0
                    val harmonicPhase = order * (basePhase - phaseOffset) + phaseOffset
                    pressureNorm += amplitude * sin(harmonicPhase)
                    velocityNorm += amplitude * sin(harmonicPhase)
                    displacementNorm += amplitude / order * cos(harmonicPhase)
                }
            } else {
                val components = waveformComponents(basePhase, waveform)
                pressureNorm = components.pressure
                velocityNorm = components.pressure
                displacementNorm = components.displacement
            }

            val localPressure = pressurePeak * envelope * pressureNorm
            val velocity = pressurePeak / (metrics.airDensityKgM3 * metrics.soundSpeedMps) * envelope * velocityNorm
            val displacement = pressurePeak /
                (metrics.airDensityKgM3 * metrics.soundSpeedMps * omega) * envelope * displacementNorm
            pressure += localPressure
            velocityX += dirX * velocity
            velocityY += dirY * velocity
            velocityZ += dirZ * velocity
            displacementX += dirX * displacement
            displacementY += dirY * displacement
            displacementZ += dirZ * displacement
        }

        addSource(
            0.0, 0.0, 0.0,
            state.frequencyHz,
            state.levelDbSpl.toDouble(),
            state.phaseRadians.toDouble(),
            if (state.harmonicMode) state.harmonicAmplitudes else null,
            state.waveform
        )
        state.secondarySource.takeIf { it.enabled }?.let { source ->
            addSource(
                source.xM.toDouble(), source.yM.toDouble(), source.zM.toDouble(),
                source.frequencyHz, source.levelDbSpl.toDouble(), source.phaseRadians.toDouble(),
                null, Waveform.SINE
            )
        }

        val baseRadius = sqrt(xM * xM + yM * yM + zM * zM)
        val basePhase = metrics.waveNumberRadM * baseRadius - metrics.angularFrequencyRadS * timeS + state.phaseRadians
        return AcousticSample(
            pressurePa = pressure,
            particleVelocityXMps = velocityX,
            particleVelocityYMps = velocityY,
            particleVelocityZMps = velocityZ,
            displacementXM = displacementX,
            displacementYM = displacementY,
            displacementZM = displacementZ,
            phaseRadians = wrapPhase(basePhase)
        )
    }

    private fun waveformComponents(phase: Double, waveform: Waveform): WaveComponents = when (waveform) {
        Waveform.SINE -> WaveComponents(sin(phase), cos(phase))
        Waveform.TRIANGLE -> fourierOddComponents(phase, triangle = true)
        Waveform.SQUARE -> fourierOddComponents(phase, triangle = false)
        Waveform.PULSE -> {
            var pressure = 0.0
            var displacement = 0.0
            for (order in 1..8) {
                val coefficient = 0.24
                pressure += coefficient * sin(order * phase)
                displacement += coefficient / order * cos(order * phase)
            }
            WaveComponents(pressure, displacement)
        }
    }

    private fun fourierOddComponents(phase: Double, triangle: Boolean): WaveComponents {
        var pressure = 0.0
        var displacement = 0.0
        for (order in intArrayOf(1, 3, 5, 7)) {
            val n = order.toDouble()
            val coefficient = if (triangle) {
                val sign = if (((order - 1) / 2) % 2 == 0) 1.0 else -1.0
                sign * 8.0 / (PI * PI * n * n)
            } else {
                4.0 / (PI * n)
            }
            pressure += coefficient * sin(n * phase)
            displacement += coefficient / n * cos(n * phase)
        }
        return WaveComponents(pressure, displacement)
    }

    private fun wrapPhase(value: Double): Double {
        val wrapped = value % (2.0 * PI)
        return if (wrapped < 0.0) wrapped + 2.0 * PI else wrapped
    }
}

private data class WaveComponents(val pressure: Double, val displacement: Double)

data class AcousticMetrics(
    val soundSpeedMps: Double,
    val airDensityKgM3: Double,
    val wavelengthM: Double,
    val periodMs: Double,
    val angularFrequencyRadS: Double,
    val waveNumberRadM: Double,
    val pressureRmsPa: Double,
    val pressurePeakPa: Double,
    val particleVelocityPeakMps: Double,
    val displacementPeakM: Double,
    val intensityWm2: Double,
    val absorptionDbPerM: Double
)

data class AcousticSample(
    val pressurePa: Double,
    val particleVelocityXMps: Double,
    val particleVelocityYMps: Double,
    val particleVelocityZMps: Double,
    val displacementXM: Double,
    val displacementYM: Double,
    val displacementZM: Double,
    val phaseRadians: Double
) {
    val particleVelocityMagnitudeMps: Double
        get() = sqrt(
            particleVelocityXMps * particleVelocityXMps +
                particleVelocityYMps * particleVelocityYMps +
                particleVelocityZMps * particleVelocityZMps
        )
    val displacementMagnitudeM: Double
        get() = sqrt(displacementXM * displacementXM + displacementYM * displacementYM + displacementZM * displacementZM)
}
