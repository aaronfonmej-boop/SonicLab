package com.soniclab3d.cymatics

import com.soniclab3d.performance.PerformanceStats
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import kotlin.math.abs
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CymaticsPhysicsTest {
    @Test
    fun naturalFrequencyFollowsThinPlateScaling() {
        val base = CymaticsState()
        val eigenvalue = 120.0
        val reference = CymaticsPhysics.naturalFrequencyHz(base, eigenvalue)

        val stiffer = CymaticsPhysics.naturalFrequencyHz(
            base.copy(youngModulusPa = base.youngModulusPa * 4.0),
            eigenvalue
        )
        val denser = CymaticsPhysics.naturalFrequencyHz(
            base.copy(densityKgM3 = base.densityKgM3 * 4.0),
            eigenvalue
        )
        val thicker = CymaticsPhysics.naturalFrequencyHz(
            base.copy(thicknessM = base.thicknessM * 2.0),
            eigenvalue
        )
        val larger = CymaticsPhysics.naturalFrequencyHz(
            base.copy(characteristicSizeM = base.characteristicSizeM * 2.0),
            eigenvalue
        )

        assertEquals(reference * 2.0, stiffer, reference * 1.0e-9)
        assertEquals(reference / 2.0, denser, reference * 1.0e-9)
        assertEquals(reference * 2.0, thicker, reference * 1.0e-9)
        assertEquals(reference / 4.0, larger, reference * 1.0e-9)
    }

    @Test
    fun dampedResponsePeaksAtTheNaturalFrequency() {
        val natural = 240.0
        val atResonance = CymaticsPhysics.responseMagnitude(natural, natural, 0.025)
        val below = CymaticsPhysics.responseMagnitude(natural * 0.5, natural, 0.025)
        val above = CymaticsPhysics.responseMagnitude(natural * 2.0, natural, 0.025)

        assertTrue(atResonance > below * 10.0)
        assertTrue(atResonance > above * 10.0)
    }

    @Test
    fun automaticModeUsesFrequencyResponseAndExciterCoupling() {
        val bank = syntheticBank()
        val base = CymaticsState(automaticMode = true, driveX = 0f, driveY = 0f)
        val targetFrequency = CymaticsPhysics.naturalFrequencyHz(base, bank.eigenvalues[7].toDouble())

        val metrics = CymaticsPhysics.metrics(base.copy(driveFrequencyHz = targetFrequency), bank)

        assertEquals(7, metrics.modeIndex)
        assertTrue(metrics.coupling > 0.9)
        assertTrue(metrics.normalizedAmplitude > 0.75)
    }

    @Test
    fun manualModeHasNoCouplingWhenExciterIsOnItsNode() {
        val base = syntheticBank()
        val fields = base.fields.copyOf()
        val mode = 7
        val cells = base.gridSize * base.gridSize
        listOf(7 to 7, 7 to 8, 8 to 7, 8 to 8).forEach { (row, column) ->
            fields[mode * cells + row * base.gridSize + column] = 0f
        }
        val bank = base.copy(fields = fields)
        val state = CymaticsState(
            automaticMode = false,
            modeIndex = mode,
            driveX = 0f,
            driveY = 0f,
            driveFrequencyHz = CymaticsPhysics.naturalFrequencyHz(CymaticsState(), bank.eigenvalues[mode].toDouble())
        )

        val metrics = CymaticsPhysics.metrics(state, bank)

        assertEquals(0.0, metrics.coupling, 1.0e-8)
        assertEquals(0.0, metrics.normalizedAmplitude, 1.0e-8)
    }

    @Test
    fun sanitizedStateProducesFinitePositiveMetrics() {
        val dirty = CymaticsState(
            driveFrequencyHz = Double.NaN,
            youngModulusPa = Double.POSITIVE_INFINITY,
            densityKgM3 = -1.0,
            thicknessM = 0.0,
            characteristicSizeM = Double.NaN,
            dampingRatio = -5.0
        )

        val metrics = CymaticsPhysics.metrics(dirty, syntheticBank())

        assertTrue(metrics.naturalFrequencyHz.isFinite() && metrics.naturalFrequencyHz > 0.0)
        assertTrue(metrics.flexuralRigidityNm.isFinite() && metrics.flexuralRigidityNm > 0.0)
        assertTrue(metrics.normalizedAmplitude in 0.0..1.0)
    }

    @Test
    fun cinematicMotionPresetAppliesACompleteCoherentProfile() {
        val precise = CymaticsMotion.applyPreset(CymaticsState(), CymaticsMotionPreset.PRECISE)
        val cinematic = CymaticsMotion.applyPreset(precise, CymaticsMotionPreset.CINEMATIC)

        assertFalse(precise.cameraDriftEnabled)
        assertFalse(precise.atmosphericDustEnabled)
        assertTrue(cinematic.cameraDriftEnabled)
        assertTrue(cinematic.orbitingLightEnabled)
        assertTrue(cinematic.resonanceAuraEnabled)
        assertTrue(cinematic.atmosphericDustEnabled)
        assertEquals(CymaticsCameraMotion.STATIC, precise.cameraMotion)
        assertEquals(CymaticsCameraMotion.SHOW, cinematic.cameraMotion)
        assertFalse(precise.postProcessingEnabled)
        assertTrue(cinematic.postProcessingEnabled)
        assertTrue(cinematic.stageEnabled)
        assertTrue(cinematic.nodalStreamsEnabled)
        assertTrue(cinematic.energyPulsesEnabled)
        assertTrue(cinematic.bloomIntensity > precise.bloomIntensity)
        assertTrue(cinematic.motionIntensity > precise.motionIntensity)
        assertTrue(cinematic.sandFlowIntensity > precise.sandFlowIntensity)
    }

    @Test
    fun visualClockIsPerceptualSlowAndIndependentOfAudioRate() {
        val low = CymaticsMotion.visualCyclesPerSecond(CymaticsState(driveFrequencyHz = 20.0))
        val base = CymaticsMotion.visualCyclesPerSecond(CymaticsState(driveFrequencyHz = 120.0))
        val high = CymaticsMotion.visualCyclesPerSecond(CymaticsState(driveFrequencyHz = 20_000.0))

        assertTrue(low in 0.30f..0.38f)
        assertTrue(base in 0.38f..0.50f)
        assertTrue(high in 0.60f..0.70f)
        assertTrue(low < base && base < high)
        assertEquals(0f, CymaticsMotion.visualCyclesPerSecond(CymaticsState(paused = true)), 0f)
        assertEquals(0f, CymaticsMotion.visualCyclesPerSecond(CymaticsState(reduceMotion = true)), 0f)
    }

    @Test
    fun modalMorphUsesSmoothBoundedProgressAndHonorsReducedMotion() {
        val state = CymaticsState(modeTransitionSeconds = 1f)
        val start = CymaticsMotion.transitionProgress(0.0, state)
        val quarter = CymaticsMotion.transitionProgress(0.25, state)
        val middle = CymaticsMotion.transitionProgress(0.5, state)
        val end = CymaticsMotion.transitionProgress(1.0, state)

        assertEquals(0f, start, 0f)
        assertTrue(quarter in 0f..middle)
        assertEquals(0.5f, middle, 1.0e-6f)
        assertEquals(1f, end, 0f)
        assertEquals(1f, CymaticsMotion.transitionProgress(0.0, state.copy(reduceMotion = true)), 0f)
        assertEquals(0f, CymaticsMotion.effectiveIntensity(state.copy(reduceMotion = true)), 0f)
    }

    @Test
    fun motionParametersRejectNonFiniteAndOutOfRangeValues() {
        val clean = CymaticsState(
            motionIntensity = Float.NaN,
            modeTransitionSeconds = Float.POSITIVE_INFINITY,
            sandFlowIntensity = -4f,
            bloomIntensity = Float.NaN,
            visualTimeScale = Float.NaN,
            lightHueDegrees = Float.NaN
        ).sanitized()

        assertTrue(clean.motionIntensity.isFinite() && clean.motionIntensity in 0f..1f)
        assertTrue(clean.modeTransitionSeconds.isFinite() && clean.modeTransitionSeconds in 0.12f..2.4f)
        assertEquals(0f, clean.sandFlowIntensity, 0f)
        assertEquals(0.92f, clean.bloomIntensity, 0f)
        assertEquals(0.08f, clean.visualTimeScale, 0f)
        assertEquals(188f, clean.lightHueDegrees, 0f)
    }

    @Test
    fun interruptedMorphCanContinueFromItsVisibleIntermediateField() {
        val previous = floatArrayOf(-1f, 0f, 1f, 1f)
        val current = floatArrayOf(1f, 1f, -1f, 0f)

        val visible = CymaticsMotion.blendModalData(previous, current, 0.25f)

        assertEquals(-0.5f, visible[0], 0f)
        assertEquals(0.25f, visible[1], 0f)
        assertEquals(0.5f, visible[2], 0f)
        assertEquals(0.75f, visible[3], 0f)
        assertTrue(runCatching {
            CymaticsMotion.blendModalData(floatArrayOf(0f), floatArrayOf(0f, 1f), 0.5f)
        }.isFailure)
    }

    @Test
    fun squareAnalyticalEigenvalueUsesExpectedDegeneracy() {
        val mode12 = CymaticsPhysics.squareSimplySupportedAnalyticalEigenvalue(1, 2)
        val mode21 = CymaticsPhysics.squareSimplySupportedAnalyticalEigenvalue(2, 1)
        val mode11 = CymaticsPhysics.squareSimplySupportedAnalyticalEigenvalue(1, 1)

        assertEquals(mode12, mode21, 0.0)
        assertEquals(mode11 * 6.25, mode12, mode11 * 1.0e-12)
    }

    @Test
    fun committedBanksLoadWithVersionedShapeAndNormalizedModes() {
        PlateGeometry.entries.forEach { geometry ->
            PlateBoundary.entries.forEach { boundary ->
                val bank = File(
                    "src/main/assets/cymatics/${geometry.assetStem}_${boundary.assetStem}.cym"
                ).inputStream().use { CymaticsModeCodec.decode(it, geometry, boundary) }

                assertEquals(64, bank.gridSize)
                assertEquals(CymaticsModeBank.MODE_COUNT, bank.eigenvalues.size)
                assertTrue((1 until bank.eigenvalues.size).all { index ->
                    bank.eigenvalues[index] >= bank.eigenvalues[index - 1]
                })
                repeat(CymaticsModeBank.MODE_COUNT) { mode ->
                    val maximum = bank.field(mode).maxOf { abs(it) }
                    assertEquals(1.0f, maximum, 2.0e-5f)
                }
                bank.mask.indices.filter { bank.mask[it] < 0.5f }.forEach { cell ->
                    repeat(CymaticsModeBank.MODE_COUNT) { mode ->
                        assertEquals(0f, bank.fields[mode * bank.mask.size + cell], 0f)
                    }
                }
            }
        }
    }

    @Test
    fun committedSquareBankMatchesAnalyticalReferenceAndOrthogonality() {
        val bank = File("src/main/assets/cymatics/square_simply_supported.cym")
            .inputStream().use {
                CymaticsModeCodec.decode(it, PlateGeometry.SQUARE, PlateBoundary.SIMPLY_SUPPORTED)
            }
        val analytical = CymaticsPhysics.squareSimplySupportedAnalyticalEigenvalue(1, 1)
        val relativeError = abs(bank.eigenvalues.first() - analytical) / analytical
        val first = bank.field(0)
        val second = bank.field(1)
        val dot = first.indices.sumOf { index -> (first[index] * second[index]).toDouble() }
        val normA = sqrt(first.sumOf { (it * it).toDouble() })
        val normB = sqrt(second.sumOf { (it * it).toDouble() })

        assertTrue("First-mode relative error was $relativeError", relativeError < 0.03)
        assertTrue(abs(dot / (normA * normB)) < 1.0e-5)
    }

    @Test
    fun clampedPenaltyProducesSmallerBoundarySlopeRingThanSupportedBank() {
        fun bank(boundary: PlateBoundary) = File(
            "src/main/assets/cymatics/square_${boundary.assetStem}.cym"
        ).inputStream().use { CymaticsModeCodec.decode(it, PlateGeometry.SQUARE, boundary) }
        val supported = bank(PlateBoundary.SIMPLY_SUPPORTED)
        val clamped = bank(PlateBoundary.CLAMPED)
        val ring = boundaryRing(supported)
        fun meanRing(value: CymaticsModeBank): Double {
            val field = value.field(0)
            return ring.map { abs(field[it]).toDouble() }.average()
        }

        assertTrue(meanRing(clamped) < meanRing(supported) * 0.05)
        assertTrue(clamped.eigenvalues.first() > supported.eigenvalues.first())
    }

    @Test
    fun codecRejectsBadMagicAndUnsupportedVersion() {
        val valid = File("src/main/assets/cymatics/circle_clamped.cym").readBytes()
        val badMagic = valid.copyOf().also { it[0] = 'X'.code.toByte() }
        val badVersion = valid.copyOf().also {
            ByteBuffer.wrap(it).order(ByteOrder.BIG_ENDIAN).putInt(4, 99)
        }

        assertTrue(runCatching {
            CymaticsModeCodec.decode(ByteArrayInputStream(badMagic), PlateGeometry.CIRCLE, PlateBoundary.CLAMPED)
        }.isFailure)
        assertTrue(runCatching {
            CymaticsModeCodec.decode(ByteArrayInputStream(badVersion), PlateGeometry.CIRCLE, PlateBoundary.CLAMPED)
        }.isFailure)
    }

    @Test
    fun manifestIdentifiesEveryBankModeAndMatchingAssetHash() {
        val assetDirectory = File("src/main/assets/cymatics")
        val manifest = File(assetDirectory, "mode_manifest.json").readText()

        assertTrue(manifest.contains("\"modelId\": \"${CymaticsModeBank.MODEL_ID}\""))
        assertTrue(manifest.contains("\"generatorVersion\": \"0.8.0\""))
        assertTrue(manifest.contains("\"modeCount\": ${CymaticsModeBank.MODE_COUNT}"))
        assertEquals(PlateGeometry.entries.size * PlateBoundary.entries.size * CymaticsModeBank.MODE_COUNT,
            Regex("\"index\":").findAll(manifest).count())

        PlateGeometry.entries.forEach { geometry ->
            PlateBoundary.entries.forEach { boundary ->
                val filename = "${geometry.assetStem}_${boundary.assetStem}.cym"
                val block = Regex(
                    "\\\"file\\\": \\\"${Regex.escape(filename)}\\\"[\\s\\S]*?\\\"sha256\\\": \\\"([0-9a-f]{64})\\\""
                ).find(manifest) ?: error("Missing manifest entry for $filename")
                val expected = block.groupValues[1]
                val digest = MessageDigest.getInstance("SHA-256")
                    .digest(File(assetDirectory, filename).readBytes())
                    .joinToString("") { "%02x".format(it) }
                assertEquals(expected, digest)
            }
        }
    }

    @Test
    fun exportIsExplicitlySimulationAndQualitativeSand() {
        val state = CymaticsState()
        val bank = syntheticBank()
        val metrics = CymaticsPhysics.metrics(state, bank)

        val json = CymaticsExporter.json(state, metrics, bank, PerformanceStats())

        assertTrue(json.contains("\"classification\": \"numerical-simulation\""))
        assertTrue(json.contains("\"experimentalMeasurement\": false"))
        assertTrue(json.contains("\"granularDynamics\": \"qualitative-nodal-relaxation\""))
        assertTrue(json.contains("\"cameraDriftVisualOnly\""))
        assertTrue(json.contains("\"atmosphericParticlesVisualOnly\""))
        assertTrue(json.contains("\"nodalStreamsVisualOnly\""))
        assertTrue(json.contains("\"energyPulsesVisualOnly\""))
        assertTrue(json.contains("\"postProcessingVisualOnly\""))
        assertTrue(json.contains("\"bloomIntensityVisualOnly\""))
        assertTrue(json.contains("a.u."))
        assertFalse(json.contains("\"experimentalMeasurement\": true"))
    }

    private fun syntheticBank(): CymaticsModeBank {
        val grid = 16
        val cells = grid * grid
        val fields = FloatArray(CymaticsModeBank.MODE_COUNT * cells)
        repeat(CymaticsModeBank.MODE_COUNT) { mode ->
            repeat(cells) { cell ->
                fields[mode * cells + cell] = if (mode == 7) 1f else 0.1f
            }
        }
        return CymaticsModeBank(
            gridSize = grid,
            geometry = PlateGeometry.SQUARE,
            boundary = PlateBoundary.SIMPLY_SUPPORTED,
            eigenvalues = FloatArray(CymaticsModeBank.MODE_COUNT) { index ->
                ((index + 1) * (index + 1)).toFloat()
            },
            mask = FloatArray(cells) { 1f },
            fields = fields
        )
    }

    private fun boundaryRing(bank: CymaticsModeBank): List<Int> {
        val grid = bank.gridSize
        return bank.mask.indices.filter { index ->
            if (bank.mask[index] < 0.5f) return@filter false
            val row = index / grid
            val column = index % grid
            listOf(row - 1 to column, row + 1 to column, row to column - 1, row to column + 1)
                .any { (candidateRow, candidateColumn) ->
                    candidateRow !in 0 until grid || candidateColumn !in 0 until grid ||
                        bank.mask[candidateRow * grid + candidateColumn] < 0.5f
                }
        }
    }
}
