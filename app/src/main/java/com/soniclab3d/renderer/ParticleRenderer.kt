package com.soniclab3d.renderer

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import com.soniclab3d.performance.FpsCounter
import com.soniclab3d.performance.PerformanceStats
import com.soniclab3d.performance.ThermalMonitor
import com.soniclab3d.physics.AcousticPhysics
import com.soniclab3d.physics.ColorMode
import com.soniclab3d.physics.ReferenceParticle
import com.soniclab3d.simulation.SimulationController
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin

class ParticleRenderer(
    private val context: Context,
    private val controller: SimulationController,
    private val statsCallback: (PerformanceStats) -> Unit,
    private val referenceCallback: (ReferenceParticle) -> Unit
) : GLSurfaceView.Renderer {
    val camera = CameraController()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val fpsCounter = FpsCounter()
    private val thermalMonitor = ThermalMonitor(context)
    private val particleExecutor = Executors.newSingleThreadExecutor()
    private val uniformLocations = mutableMapOf<String, Int>()
    private var particleFuture: Future<ParticleCloud>? = null
    private var requestedParticleKey: ParticleKey? = null
    private var program = 0
    private var vao = 0
    private var vbo = 0
    private var sliceVao = 0
    private var sliceVbo = 0
    private var slicePointCount = 0
    private var activeParticleCount = 0
    private var uploadedParticleKey: ParticleKey? = null
    private var equilibriumPositions = FloatArray(0)
    private var selectedParticleIndex = -1
    private var width = 1
    private var height = 1
    private var previousFrameNanos = 0L
    private var simulationTimeS = 0.0
    private var adaptiveFraction = 1f
    private var lastDrawCount = 0
    private var lastMvp = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vertex = context.assets.open("shaders/particle.vert").bufferedReader().use { it.readText() }
        val fragment = context.assets.open("shaders/particle.frag").bufferedReader().use { it.readText() }
        program = ShaderProgram.create(vertex, fragment)
        cacheUniformLocations()

        val ids = IntArray(1)
        GLES30.glGenVertexArrays(1, ids, 0)
        vao = ids[0]
        GLES30.glGenBuffers(1, ids, 0)
        vbo = ids[0]
        GLES30.glGenVertexArrays(1, ids, 0)
        sliceVao = ids[0]
        GLES30.glGenBuffers(1, ids, 0)
        sliceVbo = ids[0]
        uploadSliceGrid()

        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glClearColor(0.008f, 0.018f, 0.036f, 1f)
        previousFrameNanos = System.nanoTime()
        uploadedParticleKey = null
        requestParticleCloud(controller.state.quality.particleCount, controller.state.maxDistanceM)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        val state = controller.state
        controller.consumeCameraPreset()?.let(camera::setPreset)
        controller.consumeSimulationTime()?.let { simulationTimeS = it }
        ensureParticleCloud(state.quality.particleCount, state.maxDistanceM)

        val now = System.nanoTime()
        val elapsed = if (previousFrameNanos == 0L) 0.0 else {
            ((now - previousFrameNanos) / 1_000_000_000.0).coerceAtMost(0.05)
        }
        previousFrameNanos = now
        if (!state.paused) {
            simulationTimeS += elapsed * state.visualTimeScale
        } else if (controller.consumeFrameStep()) {
            simulationTimeS += (1.0 / 60.0) * state.visualTimeScale
        }

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        if (activeParticleCount <= 0) return

        val metrics = AcousticPhysics.metrics(state)
        val pressurePeakA = metrics.pressurePeakPa.toFloat()
        val sourceB = state.secondarySource
        val pressurePeakB = if (sourceB.enabled) {
            AcousticPhysics.soundPressurePeakPa(sourceB.levelDbSpl.toDouble()).toFloat()
        } else {
            0f
        }
        val absorptionA = (metrics.absorptionDbPerM / 8.685889638).toFloat()
        val absorptionB = if (sourceB.enabled) {
            (AcousticPhysics.atmosphericAbsorptionDbPerM(
                sourceB.frequencyHz,
                state.temperatureC,
                state.humidityPercent.toDouble(),
                state.atmosphericPressureKPa.toDouble()
            ) / 8.685889638).toFloat()
        } else {
            0f
        }

        val epoch = floor(simulationTimeS)
        val localTime = (simulationTimeS - epoch).toFloat()
        val phaseA = wrapPhase(
            state.phaseRadians - (2.0 * PI * state.frequencyHz * epoch).toFloat()
        )
        val phaseB = wrapPhase(
            sourceB.phaseRadians - (2.0 * PI * sourceB.frequencyHz * epoch).toFloat()
        )
        val harmonicGain = if (state.harmonicMode) {
            state.harmonicAmplitudes.sumOf { kotlin.math.abs(it.toDouble()) }.toFloat().coerceAtLeast(1f)
        } else {
            1f
        }
        val pressureScale = max(pressurePeakA * harmonicGain + pressurePeakB, 1e-6f)
        val velocityScale = pressureScale /
            max((metrics.airDensityKgM3 * metrics.soundSpeedMps).toFloat(), 1e-6f)
        val displacementScale = max(
            metrics.displacementPeakM.toFloat() * harmonicGain,
            if (sourceB.enabled) {
                pressurePeakB /
                    max(
                        (metrics.airDensityKgM3 * metrics.soundSpeedMps *
                            AcousticPhysics.angularFrequency(sourceB.frequencyHz)).toFloat(),
                        1e-9f
                    )
            } else 0f
        ).coerceAtLeast(1e-12f)

        GLES30.glUseProgram(program)
        lastMvp = camera.buildMvp(width, height)
        uniformMatrix("uMvp", lastMvp)
        uniform1f("uTime", localTime)
        uniform1f("uSoundSpeed", metrics.soundSpeedMps.toFloat())
        uniform1f("uAirDensity", metrics.airDensityKgM3.toFloat())
        uniform1f("uSourceRadius", state.sourceRadiusM)
        uniform1f("uVisualScale", state.displacementVisualScale)
        uniform1f("uMaxVisualDisplacement", state.maxVisualDisplacementM)
        uniform1f("uPointSize", state.pointSizePx)
        uniform1f("uArtisticHue", state.artisticHueDegrees)
        uniform1i("uColorMode", if (state.colorMode == ColorMode.SCIENTIFIC) 0 else 1)
        uniform1i("uVisualVariable", state.visualVariable.ordinal)
        uniform1i("uWaveform", state.waveform.ordinal)
        uniform1i("uScientificPalette", state.scientificPalette.ordinal)
        uniform1i("uShowWavefronts", if (state.showWavefronts) 1 else 0)
        uniform1f("uMaxDistance", state.maxDistanceM)
        uniform1i("uSliceAxis", state.sliceAxis.ordinal)
        uniform1f("uSliceOffset", state.sliceOffsetM)
        uniform1i("uSelectedIndex", selectedParticleIndex)
        uniform1i("uMarkerMode", 0)

        uniform1f("uFrequencyA", state.frequencyHz.toFloat())
        uniform1f("uPressurePeakA", pressurePeakA)
        uniform1f("uPhaseA", phaseA)
        uniform1f("uAbsorptionA", absorptionA)
        uniform1i("uHarmonicMode", if (state.harmonicMode) 1 else 0)
        uniformFloatArray("uHarmonics", state.harmonicAmplitudes.take(16).toFloatArray().paddedToSixteen())

        uniform1i("uSourceBEnabled", if (sourceB.enabled) 1 else 0)
        uniform3f("uSourceBPosition", sourceB.xM, sourceB.yM, sourceB.zM)
        uniform1f("uFrequencyB", sourceB.frequencyHz.toFloat())
        uniform1f("uPressurePeakB", pressurePeakB)
        uniform1f("uPhaseB", phaseB)
        uniform1f("uAbsorptionB", absorptionB)
        uniform1f("uPressureColorScale", pressureScale)
        uniform1f("uVelocityColorScale", velocityScale)
        uniform1f("uDisplacementColorScale", displacementScale)

        val drawCount = (activeParticleCount * adaptiveFraction).toInt()
            .coerceIn(minOf(30_000, activeParticleCount), activeParticleCount)
        lastDrawCount = drawCount
        GLES30.glBindVertexArray(vao)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, drawCount)
        if (sourceB.enabled) {
            uniform1i("uMarkerMode", 1)
            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1)
        }
        if (state.showPressureSlice) {
            uniform1i("uMarkerMode", 2)
            GLES30.glDepthMask(false)
            GLES30.glBindVertexArray(sliceVao)
            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, slicePointCount)
            GLES30.glDepthMask(true)
        }
        GLES30.glBindVertexArray(0)

        val thermalStatus = thermalMonitor.currentStatus()
        fpsCounter.frame(
            drawCount,
            activeParticleCount,
            adaptiveFraction,
            width,
            height,
            simulationTimeS,
            thermalStatus
        )?.let { stats ->
            updateAdaptiveQuality(state.autoQuality, stats)
            publishReference()
            mainHandler.post { statsCallback(stats) }
        }
    }

    fun selectParticle(screenX: Float, screenY: Float) {
        if (equilibriumPositions.isEmpty() || width <= 0 || height <= 0) return
        val selectableCount = lastDrawCount.coerceIn(0, activeParticleCount)
        if (selectableCount <= 1) return
        val sampleStep = max(1, selectableCount / 160_000)
        var bestIndex = -1
        var bestDistanceSquared = 52f * 52f
        var index = 1
        while (index < selectableCount) {
            val base = index * 3
            val x = equilibriumPositions[base]
            val y = equilibriumPositions[base + 1]
            val z = equilibriumPositions[base + 2]
            val clipX = lastMvp[0] * x + lastMvp[4] * y + lastMvp[8] * z + lastMvp[12]
            val clipY = lastMvp[1] * x + lastMvp[5] * y + lastMvp[9] * z + lastMvp[13]
            val clipW = lastMvp[3] * x + lastMvp[7] * y + lastMvp[11] * z + lastMvp[15]
            if (clipW > 0.0001f) {
                val projectedX = (clipX / clipW * 0.5f + 0.5f) * width
                val projectedY = (0.5f - clipY / clipW * 0.5f) * height
                val dx = projectedX - screenX
                val dy = projectedY - screenY
                val distanceSquared = dx * dx + dy * dy
                if (distanceSquared < bestDistanceSquared) {
                    bestDistanceSquared = distanceSquared
                    bestIndex = index
                }
            }
            index += sampleStep
        }
        selectedParticleIndex = bestIndex
        publishReference()
    }

    private fun publishReference() {
        val index = selectedParticleIndex
        val reference = if (index in 1 until activeParticleCount) {
            val base = index * 3
            val x = equilibriumPositions[base].toDouble()
            val y = equilibriumPositions[base + 1].toDouble()
            val z = equilibriumPositions[base + 2].toDouble()
            ReferenceParticle(
                particleIndex = index,
                equilibriumX = x,
                equilibriumY = y,
                equilibriumZ = z,
                sample = AcousticPhysics.sampleAt(controller.state, x, y, z, simulationTimeS),
                simulationTimeS = simulationTimeS
            )
        } else {
            val x = 2.0
            ReferenceParticle(
                equilibriumX = x,
                sample = AcousticPhysics.sampleAt(controller.state, x, 0.0, 0.0, simulationTimeS),
                simulationTimeS = simulationTimeS
            )
        }
        mainHandler.post { referenceCallback(reference) }
    }

    private fun updateAdaptiveQuality(enabled: Boolean, stats: PerformanceStats) {
        if (!enabled) {
            adaptiveFraction = 1f
            return
        }
        adaptiveFraction = when {
            stats.thermalStatus >= 3 -> (adaptiveFraction * 0.78f).coerceAtLeast(0.25f)
            stats.fps in 1f..53.9f -> (adaptiveFraction * 0.86f).coerceAtLeast(0.25f)
            stats.fps >= 59f -> (adaptiveFraction + 0.06f).coerceAtMost(1f)
            else -> adaptiveFraction
        }
    }

    private fun ensureParticleCloud(count: Int, radius: Float) {
        val desired = ParticleKey(count, radius)
        val future = particleFuture
        if (future != null && future.isDone) {
            val cloud = runCatching { future.get() }.getOrNull()
            particleFuture = null
            if (cloud != null && cloud.key == desired) uploadParticles(cloud)
        }
        if (uploadedParticleKey != desired && requestedParticleKey != desired) {
            requestParticleCloud(count, radius)
        }
    }

    private fun requestParticleCloud(count: Int, radius: Float) {
        val key = ParticleKey(count, radius)
        particleFuture?.cancel(true)
        requestedParticleKey = key
        particleFuture = particleExecutor.submit<ParticleCloud> { generateParticleCloud(key) }
    }

    private fun generateParticleCloud(key: ParticleKey): ParticleCloud {
        val positions = FloatArray(key.count * 3)
        var randomState = 0x6D2B79F5u
        fun nextFloat(): Float {
            randomState = randomState xor (randomState shl 13)
            randomState = randomState xor (randomState shr 17)
            randomState = randomState xor (randomState shl 5)
            return (randomState.toLong() and 0xFFFFFFFFL).toFloat() / 4294967295f
        }
        for (index in 1 until key.count) {
            val radial = key.radius * cbrt(nextFloat().toDouble()).toFloat()
            val theta = acos((1f - 2f * nextFloat()).coerceIn(-1f, 1f).toDouble())
            val phi = 2.0 * PI * nextFloat()
            val sinTheta = sin(theta).toFloat()
            val base = index * 3
            positions[base] = radial * sinTheta * cos(phi).toFloat()
            positions[base + 1] = radial * cos(theta).toFloat()
            positions[base + 2] = radial * sinTheta * sin(phi).toFloat()
        }
        return ParticleCloud(key, positions)
    }

    private fun uploadParticles(cloud: ParticleCloud) {
        val data = ByteBuffer.allocateDirect(cloud.positions.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        data.put(cloud.positions).position(0)
        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            cloud.positions.size * Float.SIZE_BYTES,
            data,
            GLES30.GL_STATIC_DRAW
        )
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        GLES30.glBindVertexArray(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        equilibriumPositions = cloud.positions
        activeParticleCount = cloud.key.count
        uploadedParticleKey = cloud.key
        requestedParticleKey = cloud.key
        if (selectedParticleIndex >= activeParticleCount) selectedParticleIndex = -1
    }

    private fun uploadSliceGrid() {
        val side = 144
        val positions = FloatArray(side * side * 3)
        var cursor = 0
        for (row in 0 until side) {
            val y = -1f + 2f * row / (side - 1f)
            for (column in 0 until side) {
                val x = -1f + 2f * column / (side - 1f)
                positions[cursor++] = x
                positions[cursor++] = y
                positions[cursor++] = 0f
            }
        }
        val data = ByteBuffer.allocateDirect(positions.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        data.put(positions).position(0)
        GLES30.glBindVertexArray(sliceVao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, sliceVbo)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            positions.size * Float.SIZE_BYTES,
            data,
            GLES30.GL_STATIC_DRAW
        )
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        GLES30.glBindVertexArray(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        slicePointCount = side * side
    }

    private fun cacheUniformLocations() {
        val names = listOf(
            "uMvp", "uTime", "uSoundSpeed", "uAirDensity", "uSourceRadius", "uVisualScale",
            "uMaxVisualDisplacement", "uPointSize", "uArtisticHue", "uColorMode", "uVisualVariable",
            "uWaveform", "uScientificPalette", "uShowWavefronts", "uMaxDistance", "uSliceAxis", "uSliceOffset",
            "uSelectedIndex", "uMarkerMode", "uFrequencyA", "uPressurePeakA", "uPhaseA", "uAbsorptionA",
            "uHarmonicMode", "uHarmonics", "uSourceBEnabled", "uSourceBPosition", "uFrequencyB",
            "uPressurePeakB", "uPhaseB", "uAbsorptionB", "uPressureColorScale", "uVelocityColorScale",
            "uDisplacementColorScale"
        )
        names.forEach { name ->
            val shaderName = if (name == "uHarmonics") "uHarmonics[0]" else name
            uniformLocations[name] = GLES30.glGetUniformLocation(program, shaderName)
        }
    }

    private fun uniform1f(name: String, value: Float) = GLES30.glUniform1f(uniformLocations.getValue(name), value)
    private fun uniform1i(name: String, value: Int) = GLES30.glUniform1i(uniformLocations.getValue(name), value)
    private fun uniform3f(name: String, x: Float, y: Float, z: Float) =
        GLES30.glUniform3f(uniformLocations.getValue(name), x, y, z)
    private fun uniformFloatArray(name: String, values: FloatArray) =
        GLES30.glUniform1fv(uniformLocations.getValue(name), values.size, values, 0)
    private fun uniformMatrix(name: String, value: FloatArray) =
        GLES30.glUniformMatrix4fv(uniformLocations.getValue(name), 1, false, value, 0)

    private fun wrapPhase(value: Float): Float {
        val period = (2.0 * PI).toFloat()
        val wrapped = value % period
        return if (wrapped < 0f) wrapped + period else wrapped
    }

    private fun FloatArray.paddedToSixteen(): FloatArray =
        if (size == 16) this else FloatArray(16) { index -> getOrElse(index) { 0f } }
}

private data class ParticleKey(val count: Int, val radius: Float)
private data class ParticleCloud(val key: ParticleKey, val positions: FloatArray)
