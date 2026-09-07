package com.soniclab3d.renderer

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import com.soniclab3d.cymatics.CymaticsController
import com.soniclab3d.cymatics.CymaticsMetrics
import com.soniclab3d.cymatics.CymaticsModeBank
import com.soniclab3d.cymatics.CymaticsModeRepository
import com.soniclab3d.cymatics.CymaticsMotion
import com.soniclab3d.cymatics.CymaticsPhysics
import com.soniclab3d.cymatics.CymaticsRenderInfo
import com.soniclab3d.cymatics.CymaticsState
import com.soniclab3d.cymatics.CymaticsVisualStyle
import com.soniclab3d.performance.FpsCounter
import com.soniclab3d.performance.PerformanceStats
import com.soniclab3d.performance.ThermalMonitor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp

class CymaticsRenderer(
    context: Context,
    private val controller: CymaticsController,
    private val statsCallback: (PerformanceStats) -> Unit,
    private val infoCallback: (CymaticsRenderInfo) -> Unit
) : GLSurfaceView.Renderer {
    val camera = CameraController()

    private val appContext = context.applicationContext
    private val repository = CymaticsModeRepository(appContext)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val fpsCounter = FpsCounter()
    private val thermalMonitor = ThermalMonitor(appContext)
    private val worker = Executors.newSingleThreadExecutor()
    private val surfaceUniforms = mutableMapOf<String, Int>()
    private val sandUniforms = mutableMapOf<String, Int>()

    private var surfaceProgram = 0
    private var sandProgram = 0
    private var stageProgram = 0
    private var brightProgram = 0
    private var blurProgram = 0
    private var compositeProgram = 0
    private var surfaceVao = 0
    private var surfaceVbo = 0
    private var surfaceEbo = 0
    private var surfaceIndexCount = 0
    private val sandVaos = IntArray(2)
    private val sandVbos = IntArray(2)
    private val modeTextures = IntArray(2)
    private var fullscreenVao = 0
    private var sceneFramebuffer = 0
    private var sceneTexture = 0
    private var sceneDepthBuffer = 0
    private val bloomFramebuffers = IntArray(2)
    private val bloomTextures = IntArray(2)
    private var bloomWidth = 1
    private var bloomHeight = 1
    private var postProcessingReady = false
    private var width = 1
    private var height = 1
    private var previousFrameNanos = 0L
    private var simulationTimeS = 0.0
    private var visualPhaseCycles = 0.0
    private var animationTimeS = 0.0

    private var bankFuture: Future<CymaticsModeBank>? = null
    private var requestedBankKey = ""
    private var activeBank: CymaticsModeBank? = null

    private var uploadedModeKey = ""
    private var currentModeSlot = 0
    private var hasModeTexture = false
    private var previousModeData: FloatArray? = null
    private var currentModeData: FloatArray? = null
    private var currentModeGridSize = 0
    private var modeTransitionStartedNanos = 0L

    private var sandFuture: Future<SandCloud>? = null
    private var requestedSandKey = ""
    private var uploadedSandKey = ""
    private var currentSandSlot = 0
    private var hasSandCloud = false
    private var activeSandCount = 0
    private var previousSandCount = 0
    private var sandStartedNanos = 0L
    private var sandTransitionStartedNanos = 0L
    private var adaptiveFraction = 1f
    private var postProcessingBudgetEnabled = true

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val surfaceVertex = assetText("shaders/cymatics_surface.vert")
        val surfaceFragment = assetText("shaders/cymatics_surface.frag")
        val sandVertex = assetText("shaders/cymatics_sand.vert")
        val sandFragment = assetText("shaders/cymatics_sand.frag")
        val stageVertex = assetText("shaders/cymatics_stage.vert")
        val stageFragment = assetText("shaders/cymatics_stage.frag")
        val postVertex = assetText("shaders/cymatics_post.vert")
        surfaceProgram = ShaderProgram.create(surfaceVertex, surfaceFragment)
        sandProgram = ShaderProgram.create(sandVertex, sandFragment)
        stageProgram = ShaderProgram.create(stageVertex, stageFragment)
        brightProgram = ShaderProgram.create(postVertex, assetText("shaders/cymatics_bright.frag"))
        blurProgram = ShaderProgram.create(postVertex, assetText("shaders/cymatics_blur.frag"))
        compositeProgram = ShaderProgram.create(postVertex, assetText("shaders/cymatics_composite.frag"))
        createSurfaceMesh()
        createSandBuffers()
        createModeTextures()
        createFullscreenVao()
        cacheUniforms()
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLES30.glClearColor(0.0015f, 0.004f, 0.014f, 1f)
        previousFrameNanos = System.nanoTime()
        uploadedModeKey = ""
        uploadedSandKey = ""
        requestedSandKey = ""
        hasModeTexture = false
        hasSandCloud = false
        activeSandCount = 0
        previousSandCount = 0
        requestBank(controller.state)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height
        GLES30.glViewport(0, 0, width, height)
        createPostProcessingTargets()
    }

    override fun onDrawFrame(gl: GL10?) {
        var state = controller.state.sanitized()
        controller.consumeCameraPreset()?.let(camera::setCymaticsPreset)
        ensureBank(state)

        val now = System.nanoTime()
        val elapsed = if (previousFrameNanos == 0L) {
            0.0
        } else {
            ((now - previousFrameNanos) / 1_000_000_000.0).coerceAtMost(0.05)
        }
        previousFrameNanos = now
        val visualCyclesPerSecond = CymaticsMotion.visualCyclesPerSecond(state)
        if (!state.paused && !state.reduceMotion) {
            simulationTimeS += elapsed * state.visualTimeScale
            visualPhaseCycles += elapsed * visualCyclesPerSecond
            animationTimeS += elapsed
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glViewport(0, 0, width, height)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        val bank = activeBank
        if (bank == null) {
            publishLoadingStats()
            return
        }
        state = controller.state.sanitized()
        val metrics = CymaticsPhysics.metrics(state, bank)
        val modeKey = bank.geometry.name + ":" + bank.boundary.name + ":" + metrics.modeIndex
        val previousModeBlend = modeTransitionProgress(now, state)
        if (uploadedModeKey != modeKey) {
            uploadModeTexture(bank, metrics.modeIndex, previousModeBlend, now)
            uploadedModeKey = modeKey
            requestedSandKey = ""
        }
        val modeBlend = modeTransitionProgress(now, state)
        ensureSand(bank, state, metrics.modeIndex)

        val phase = wrapPhase(2.0 * PI * visualPhaseCycles).toFloat()
        val motionIntensity = CymaticsMotion.effectiveIntensity(state)
        val resonance = CymaticsMotion.resonanceEnergy(metrics)
        val cameraFrame = if (state.reduceMotion) {
            CameraFrame(camera.buildMvp(width, height), camera.cameraPosition())
        } else {
            camera.buildCinematicFrame(
                width = width,
                height = height,
                animationTimeSeconds = animationTimeS,
                driftStrength = if (state.cameraDriftEnabled) motionIntensity else 0f,
                motion = state.cameraMotion
            )
        }
        val usePostProcessing = state.visualStyle == CymaticsVisualStyle.SPECTACULAR &&
            state.postProcessingEnabled && postProcessingReady &&
            (!state.autoQuality || postProcessingBudgetEnabled)
        beginScene(usePostProcessing)
        if (state.stageEnabled && state.visualStyle == CymaticsVisualStyle.SPECTACULAR) {
            drawStage(state, resonance, motionIntensity, cameraFrame.mvp)
        }
        drawSurface(
            state = state,
            metrics = metrics,
            bank = bank,
            phase = phase,
            modeBlend = modeBlend,
            motionIntensity = motionIntensity,
            resonance = resonance,
            frame = cameraFrame,
            renderPass = 0,
            verticalOffset = -0.15f
        )
        drawSurface(
            state = state,
            metrics = metrics,
            bank = bank,
            phase = phase,
            modeBlend = modeBlend,
            motionIntensity = motionIntensity,
            resonance = resonance,
            frame = cameraFrame,
            renderPass = 1,
            verticalOffset = 0f
        )
        if (state.visualStyle == CymaticsVisualStyle.SPECTACULAR &&
            (state.showNodes || state.energyPulsesEnabled)
        ) {
            GLES30.glDepthMask(false)
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE)
            drawSurface(
                state = state,
                metrics = metrics,
                bank = bank,
                phase = phase,
                modeBlend = modeBlend,
                motionIntensity = motionIntensity,
                resonance = resonance,
                frame = cameraFrame,
                renderPass = 2,
                verticalOffset = 0.025f
            )
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
            GLES30.glDepthMask(true)
        }

        var atmosphereDrawCount = 0
        if (
            state.atmosphericDustEnabled &&
            state.visualStyle == CymaticsVisualStyle.SPECTACULAR &&
            motionIntensity > 0f &&
            hasSandCloud
        ) {
            atmosphereDrawCount = drawAtmosphere(
                state = state,
                metrics = metrics,
                phase = phase,
                modeBlend = modeBlend,
                motionIntensity = motionIntensity,
                resonance = resonance,
                mvp = cameraFrame.mvp
            )
        }

        var sandDrawCount = 0
        if (state.sandEnabled && hasSandCloud && activeSandCount > 0) {
            val sandBlend = sandTransitionProgress(now, state)
            if (previousSandCount > 0 && sandBlend < 0.999f) {
                sandDrawCount += drawSandCloud(
                    slot = 1 - currentSandSlot,
                    sourceCount = previousSandCount,
                    state = state,
                    metrics = metrics,
                    phase = phase,
                    modeBlend = modeBlend,
                    motionIntensity = motionIntensity,
                    resonance = resonance,
                    mvp = cameraFrame.mvp,
                    progress = 1f,
                    alpha = 1f - sandBlend
                )
            }
            val seconds = elapsedSeconds(sandStartedNanos, now)
            val relaxationRate = state.sandRelaxation *
                (0.35 + metrics.normalizedAmplitude * 3.65)
            val progress = (1.0 - exp(-seconds * relaxationRate)).toFloat().coerceIn(0f, 1f)
            sandDrawCount += drawSandCloud(
                slot = currentSandSlot,
                sourceCount = activeSandCount,
                state = state,
                metrics = metrics,
                phase = phase,
                modeBlend = modeBlend,
                motionIntensity = motionIntensity,
                resonance = resonance,
                mvp = cameraFrame.mvp,
                progress = progress,
                alpha = sandBlend
            )
            if (
                state.nodalStreamsEnabled &&
                state.visualStyle == CymaticsVisualStyle.SPECTACULAR &&
                motionIntensity > 0f
            ) {
                sandDrawCount += drawNodalStreams(
                    state = state,
                    metrics = metrics,
                    phase = phase,
                    modeBlend = modeBlend,
                    motionIntensity = motionIntensity,
                    resonance = resonance,
                    mvp = cameraFrame.mvp,
                    alpha = sandBlend
                )
            }
        }

        if (usePostProcessing) {
            applyPostProcessing(state, resonance, motionIntensity)
        }

        fpsCounter.frame(
            particleCount = sandDrawCount + atmosphereDrawCount,
            configuredParticleCount = state.sandParticleCount,
            adaptiveFraction = adaptiveFraction,
            width = width,
            height = height,
            simulationTimeS = simulationTimeS,
            thermalStatus = thermalMonitor.currentStatus()
        )?.let { stats ->
            updateAdaptiveQuality(state, stats)
            val warning = when {
                state.driveFrequencyHz < metrics.firstFrequencyHz ||
                    state.driveFrequencyHz > metrics.lastFrequencyHz ->
                    "La excitación está fuera del rango del banco modal cargado"
                metrics.coupling < 0.04 -> "El excitador está cerca de un nodo del modo dominante"
                else -> null
            }
            val info = CymaticsRenderInfo(
                bankReady = true,
                activeModeIndex = metrics.modeIndex,
                naturalFrequencyHz = metrics.naturalFrequencyHz,
                response = metrics.response,
                coupling = metrics.coupling,
                probeValue = if (state.probeEnabled) {
                    bank.sample(metrics.modeIndex, state.probeX, state.probeY).toDouble()
                } else {
                    0.0
                },
                relativeDetuning = metrics.relativeDetuning,
                sandParticleCount = if (state.sandEnabled) {
                    adaptedCount(activeSandCount)
                } else {
                    0
                },
                modeTransitionProgress = modeBlend,
                visualCyclesPerSecond = visualCyclesPerSecond,
                postProcessingActive = usePostProcessing,
                motionProfile = if (state.reduceMotion) {
                    "Movimiento reducido"
                } else {
                    state.motionPreset.label
                },
                rendererPath = if (usePostProcessing) {
                    "OpenGL ES 3.0 · escenario HDR simulado + bloom GPU"
                } else if (state.postProcessingEnabled && state.autoQuality &&
                    !postProcessingBudgetEnabled
                ) {
                    "OpenGL ES 3.0 · ruta directa adaptativa"
                } else {
                    "OpenGL ES 3.0 · escenario modal directo"
                },
                warning = warning
            )
            mainHandler.post {
                statsCallback(stats)
                infoCallback(info)
            }
        }
    }

    fun resetSandAnimation() {
        sandStartedNanos = System.nanoTime()
        requestedSandKey = ""
        uploadedSandKey = ""
    }

    fun release() {
        bankFuture?.cancel(true)
        sandFuture?.cancel(true)
        worker.shutdownNow()
    }

    fun releaseGl() {
        if (surfaceProgram != 0) GLES30.glDeleteProgram(surfaceProgram)
        if (sandProgram != 0) GLES30.glDeleteProgram(sandProgram)
        if (stageProgram != 0) GLES30.glDeleteProgram(stageProgram)
        if (brightProgram != 0) GLES30.glDeleteProgram(brightProgram)
        if (blurProgram != 0) GLES30.glDeleteProgram(blurProgram)
        if (compositeProgram != 0) GLES30.glDeleteProgram(compositeProgram)
        if (surfaceVao != 0) GLES30.glDeleteVertexArrays(1, intArrayOf(surfaceVao), 0)
        if (fullscreenVao != 0) GLES30.glDeleteVertexArrays(1, intArrayOf(fullscreenVao), 0)
        GLES30.glDeleteVertexArrays(sandVaos.size, sandVaos, 0)
        if (surfaceVbo != 0) GLES30.glDeleteBuffers(1, intArrayOf(surfaceVbo), 0)
        if (surfaceEbo != 0) GLES30.glDeleteBuffers(1, intArrayOf(surfaceEbo), 0)
        GLES30.glDeleteBuffers(sandVbos.size, sandVbos, 0)
        GLES30.glDeleteTextures(modeTextures.size, modeTextures, 0)
        deletePostProcessingTargets()
    }

    private fun drawSurface(
        state: CymaticsState,
        metrics: CymaticsMetrics,
        bank: CymaticsModeBank,
        phase: Float,
        modeBlend: Float,
        motionIntensity: Float,
        resonance: Float,
        frame: CameraFrame,
        renderPass: Int,
        verticalOffset: Float
    ) {
        GLES30.glUseProgram(surfaceProgram)
        bindSurfaceModeTextures()
        surfaceMatrix("uMvp", frame.mvp)
        surface1f("uModeBlend", modeBlend)
        surface1f("uPhase", phase)
        surface1f("uAmplitude", metrics.normalizedAmplitude.toFloat())
        surface1f("uVisualScale", state.visualScale)
        surface1f("uGridSize", bank.gridSize.toFloat())
        surface1f("uVerticalOffset", verticalOffset)
        surface1f("uAnimationTime", animationTimeS.toFloat())
        surface1f("uMotionIntensity", motionIntensity)
        surface1f("uResonance", resonance)
        surface1i("uVariable", state.variable.ordinal)
        surface1i("uStyle", if (state.visualStyle == CymaticsVisualStyle.SCIENTIFIC) 0 else 1)
        surface1i("uPalette", state.palette.ordinal)
        surface1i("uRenderPass", renderPass)
        surface1i("uShowNodes", if (state.showNodes) 1 else 0)
        surface1i("uShowAntinodes", if (state.showAntinodes) 1 else 0)
        surface1i("uShowPhaseLines", if (state.showPhaseLines) 1 else 0)
        surface1i("uShowGrid", if (state.showGrid) 1 else 0)
        surface1i("uShowExciter", if (state.showExciter) 1 else 0)
        surface1i("uShowProbe", if (state.probeEnabled) 1 else 0)
        surface1i("uOrbitingLight", if (state.orbitingLightEnabled) 1 else 0)
        surface1i("uResonanceAura", if (state.resonanceAuraEnabled) 1 else 0)
        surface1i("uEnergyPulses", if (state.energyPulsesEnabled) 1 else 0)
        surface2f("uDrivePosition", state.driveX, state.driveY)
        surface2f("uProbePosition", state.probeX, state.probeY)
        surface1f("uLightHue", state.lightHueDegrees)
        surface1f("uContourDensity", state.contourDensity)
        surface3f(
            "uCameraPosition",
            frame.position[0],
            frame.position[1],
            frame.position[2]
        )
        GLES30.glBindVertexArray(surfaceVao)
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            surfaceIndexCount,
            GLES30.GL_UNSIGNED_INT,
            0
        )
        GLES30.glBindVertexArray(0)
    }

    private fun beginScene(usePostProcessing: Boolean) {
        GLES30.glBindFramebuffer(
            GLES30.GL_FRAMEBUFFER,
            if (usePostProcessing) sceneFramebuffer else 0
        )
        GLES30.glViewport(0, 0, width, height)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDepthMask(true)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
    }

    private fun drawStage(
        state: CymaticsState,
        resonance: Float,
        motionIntensity: Float,
        mvp: FloatArray
    ) {
        GLES30.glUseProgram(stageProgram)
        programMatrix(stageProgram, "uMvp", mvp)
        program2f(stageProgram, "uDrivePosition", state.driveX, state.driveY)
        program1f(stageProgram, "uAnimationTime", animationTimeS.toFloat())
        program1f(stageProgram, "uMotionIntensity", motionIntensity)
        program1f(stageProgram, "uResonance", resonance)
        program1i(stageProgram, "uEnergyPulses", if (state.energyPulsesEnabled) 1 else 0)
        program1i(stageProgram, "uPalette", state.palette.ordinal)
        GLES30.glBindVertexArray(fullscreenVao)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)
        GLES30.glBindVertexArray(0)
    }

    private fun drawNodalStreams(
        state: CymaticsState,
        metrics: CymaticsMetrics,
        phase: Float,
        modeBlend: Float,
        motionIntensity: Float,
        resonance: Float,
        mvp: FloatArray,
        alpha: Float
    ): Int {
        val count = adaptedCount(activeSandCount).coerceAtMost(16_000)
        if (count <= 0 || alpha <= 0.001f) return 0
        prepareSandProgram(
            state = state,
            metrics = metrics,
            phase = phase,
            modeBlend = modeBlend,
            motionIntensity = motionIntensity,
            resonance = resonance,
            mvp = mvp,
            progress = 1f,
            pointSize = state.sandPointSizePx * 1.18f,
            layer = 2,
            alpha = alpha
        )
        GLES30.glDepthMask(false)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE)
        GLES30.glBindVertexArray(sandVaos[currentSandSlot])
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, count)
        GLES30.glBindVertexArray(0)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDepthMask(true)
        return count
    }

    private fun applyPostProcessing(
        state: CymaticsState,
        resonance: Float,
        motionIntensity: Float
    ) {
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glBindVertexArray(fullscreenVao)

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, bloomFramebuffers[0])
        GLES30.glViewport(0, 0, bloomWidth, bloomHeight)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(brightProgram)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, sceneTexture)
        program1i(brightProgram, "uScene", 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

        repeat(2) {
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, bloomFramebuffers[1])
            GLES30.glUseProgram(blurProgram)
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, bloomTextures[0])
            program1i(blurProgram, "uImage", 0)
            program2f(blurProgram, "uDirection", 1f / bloomWidth, 0f)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, bloomFramebuffers[0])
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, bloomTextures[1])
            program2f(blurProgram, "uDirection", 0f, 1f / bloomHeight)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glViewport(0, 0, width, height)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(compositeProgram)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, sceneTexture)
        program1i(compositeProgram, "uScene", 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, bloomTextures[0])
        program1i(compositeProgram, "uBloom", 1)
        program1f(compositeProgram, "uBloomIntensity", state.bloomIntensity)
        program1f(compositeProgram, "uMotionIntensity", motionIntensity)
        program1f(compositeProgram, "uResonance", resonance)
        program1f(compositeProgram, "uAnimationTime", animationTimeS.toFloat())
        program2f(compositeProgram, "uResolution", width.toFloat(), height.toFloat())
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
        GLES30.glBindVertexArray(0)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDepthMask(true)
    }

    private fun drawAtmosphere(
        state: CymaticsState,
        metrics: CymaticsMetrics,
        phase: Float,
        modeBlend: Float,
        motionIntensity: Float,
        resonance: Float,
        mvp: FloatArray
    ): Int {
        val count = adaptedCount(activeSandCount).coerceAtMost(12_000)
        if (count <= 0) return 0
        prepareSandProgram(
            state = state,
            metrics = metrics,
            phase = phase,
            modeBlend = modeBlend,
            motionIntensity = motionIntensity,
            resonance = resonance,
            mvp = mvp,
            progress = 1f,
            pointSize = state.sandPointSizePx * 1.12f,
            layer = 1,
            alpha = 1f
        )
        GLES30.glDepthMask(false)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE)
        GLES30.glBindVertexArray(sandVaos[currentSandSlot])
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, count)
        GLES30.glBindVertexArray(0)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDepthMask(true)
        return count
    }

    private fun drawSandCloud(
        slot: Int,
        sourceCount: Int,
        state: CymaticsState,
        metrics: CymaticsMetrics,
        phase: Float,
        modeBlend: Float,
        motionIntensity: Float,
        resonance: Float,
        mvp: FloatArray,
        progress: Float,
        alpha: Float
    ): Int {
        if (alpha <= 0.001f) return 0
        val count = adaptedCount(sourceCount)
        if (count <= 0) return 0
        prepareSandProgram(
            state = state,
            metrics = metrics,
            phase = phase,
            modeBlend = modeBlend,
            motionIntensity = motionIntensity,
            resonance = resonance,
            mvp = mvp,
            progress = progress,
            pointSize = state.sandPointSizePx,
            layer = 0,
            alpha = alpha
        )
        GLES30.glDepthMask(false)
        GLES30.glBindVertexArray(sandVaos[slot])
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, count)
        GLES30.glBindVertexArray(0)
        GLES30.glDepthMask(true)
        return count
    }

    private fun prepareSandProgram(
        state: CymaticsState,
        metrics: CymaticsMetrics,
        phase: Float,
        modeBlend: Float,
        motionIntensity: Float,
        resonance: Float,
        mvp: FloatArray,
        progress: Float,
        pointSize: Float,
        layer: Int,
        alpha: Float
    ) {
        GLES30.glUseProgram(sandProgram)
        bindSandModeTextures()
        sandMatrix("uMvp", mvp)
        sand1f("uModeBlend", modeBlend)
        sand1f("uProgress", progress)
        sand1f("uPhase", phase)
        sand1f("uAmplitude", metrics.normalizedAmplitude.toFloat())
        sand1f("uVisualScale", state.visualScale)
        sand1f("uPointSize", pointSize)
        sand1f("uAnimationTime", animationTimeS.toFloat())
        sand1f("uMotionIntensity", motionIntensity)
        sand1f("uResonance", resonance)
        sand1f("uFlowIntensity", state.sandFlowIntensity)
        sand1f("uLayerAlpha", alpha)
        sand1i("uLayer", layer)
        sand1i("uStyle", if (state.visualStyle == CymaticsVisualStyle.SCIENTIFIC) 0 else 1)
        sand1i("uPalette", state.palette.ordinal)
    }

    private fun bindSurfaceModeTextures() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, modeTextures[currentModeSlot])
        surface1i("uModeTexture", 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, modeTextures[1 - currentModeSlot])
        surface1i("uPreviousModeTexture", 1)
    }

    private fun bindSandModeTextures() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, modeTextures[currentModeSlot])
        sand1i("uModeTexture", 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, modeTextures[1 - currentModeSlot])
        sand1i("uPreviousModeTexture", 1)
    }

    private fun ensureBank(state: CymaticsState) {
        val key = state.geometry.name + ":" + state.boundary.name
        val future = bankFuture
        if (future != null && future.isDone) {
            val result = runCatching { future.get() }.getOrNull()
            bankFuture = null
            if (result != null && requestedBankKey == key) {
                activeBank = result
                uploadedModeKey = ""
                requestedSandKey = ""
            }
        }
        if (
            activeBank?.let { it.geometry.name + ":" + it.boundary.name } != key &&
            requestedBankKey != key
        ) {
            requestBank(state)
        }
    }

    private fun requestBank(state: CymaticsState) {
        val key = state.geometry.name + ":" + state.boundary.name
        bankFuture?.cancel(true)
        requestedBankKey = key
        bankFuture = worker.submit<CymaticsModeBank> {
            repository.load(state.geometry, state.boundary)
        }
    }

    private fun ensureSand(bank: CymaticsModeBank, state: CymaticsState, modeIndex: Int) {
        if (!state.sandEnabled && !state.atmosphericDustEnabled) return
        val key = bank.geometry.name + ":" + bank.boundary.name + ":" + modeIndex + ":" +
            state.sandParticleCount + ":" + controller.sandGeneration()
        val future = sandFuture
        if (future != null && future.isDone) {
            val result = runCatching { future.get() }.getOrNull()
            sandFuture = null
            if (result != null && result.key == key) uploadSand(result)
        }
        if (uploadedSandKey != key && requestedSandKey != key) {
            sandFuture?.cancel(true)
            requestedSandKey = key
            val field = bank.field(modeIndex)
            val mask = bank.mask.copyOf()
            val grid = bank.gridSize
            val count = state.sandParticleCount
            sandFuture = worker.submit<SandCloud> {
                generateSandCloud(key, grid, count, field, mask)
            }
        }
    }

    private fun generateSandCloud(
        key: String,
        grid: Int,
        count: Int,
        field: FloatArray,
        mask: FloatArray
    ): SandCloud {
        val activeCells = IntArray(mask.count { it > 0.5f })
        var cursor = 0
        mask.indices.forEach { index ->
            if (mask[index] > 0.5f) activeCells[cursor++] = index
        }
        require(activeCells.isNotEmpty()) { "La máscara modal no contiene celdas activas" }
        val values = FloatArray(count * 6)
        var randomState = (0x6D2B79F5u xor key.hashCode().toUInt())
        fun nextFloat(): Float {
            randomState = randomState xor (randomState shl 13)
            randomState = randomState xor (randomState shr 17)
            randomState = randomState xor (randomState shl 5)
            return (randomState.toLong() and 0xFFFFFFFFL).toFloat() / 4294967295f
        }
        val neighbors = intArrayOf(
            -grid - 1,
            -grid,
            -grid + 1,
            -1,
            1,
            grid - 1,
            grid,
            grid + 1
        )
        repeat(count) { particle ->
            if ((particle and 2047) == 0 && Thread.currentThread().isInterrupted) {
                throw InterruptedException("Generación de arena reemplazada por un estado más reciente")
            }
            val seedCell = activeCells[
                (nextFloat() * activeCells.size).toInt().coerceIn(0, activeCells.lastIndex)
            ]
            var target = seedCell
            repeat(13) {
                var best = target
                var bestValue = abs(field[target])
                neighbors.forEach { offset ->
                    val candidate = target + offset
                    if (candidate in field.indices && mask[candidate] > 0.5f) {
                        val rowDistance = abs(candidate / grid - target / grid)
                        if (rowDistance <= 1 && abs(field[candidate]) < bestValue) {
                            best = candidate
                            bestValue = abs(field[candidate])
                        }
                    }
                }
                target = best
            }
            fun uvX(cell: Int, jitter: Float): Float =
                ((cell % grid + jitter) / (grid - 1f)).coerceIn(0f, 1f)
            fun uvY(cell: Int, jitter: Float): Float =
                ((cell / grid + jitter) / (grid - 1f)).coerceIn(0f, 1f)
            val base = particle * 6
            values[base] = uvX(seedCell, nextFloat() - 0.5f)
            values[base + 1] = uvY(seedCell, nextFloat() - 0.5f)
            values[base + 2] = uvX(target, (nextFloat() - 0.5f) * 0.72f)
            values[base + 3] = uvY(target, (nextFloat() - 0.5f) * 0.72f)
            values[base + 4] = nextFloat()
            values[base + 5] = nextFloat()
        }
        return SandCloud(key, count, values)
    }

    private fun uploadSand(cloud: SandCloud) {
        val now = System.nanoTime()
        if (hasSandCloud) {
            previousSandCount = activeSandCount
            currentSandSlot = 1 - currentSandSlot
            sandTransitionStartedNanos = now
        } else {
            currentSandSlot = 0
            previousSandCount = 0
            sandTransitionStartedNanos = 0L
            hasSandCloud = true
        }
        val buffer = ByteBuffer.allocateDirect(cloud.values.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(cloud.values).position(0)
        GLES30.glBindVertexArray(sandVaos[currentSandSlot])
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, sandVbos[currentSandSlot])
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            cloud.values.size * Float.SIZE_BYTES,
            buffer,
            GLES30.GL_STATIC_DRAW
        )
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(
            0,
            4,
            GLES30.GL_FLOAT,
            false,
            6 * Float.SIZE_BYTES,
            0
        )
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(
            1,
            2,
            GLES30.GL_FLOAT,
            false,
            6 * Float.SIZE_BYTES,
            4 * Float.SIZE_BYTES
        )
        GLES30.glBindVertexArray(0)
        activeSandCount = cloud.count
        uploadedSandKey = cloud.key
        requestedSandKey = cloud.key
        sandStartedNanos = now
    }

    private fun uploadModeTexture(
        bank: CymaticsModeBank,
        modeIndex: Int,
        existingBlend: Float,
        now: Long
    ) {
        val newData = interleavedModeData(bank, modeIndex)
        if (!hasModeTexture) {
            currentModeSlot = 0
            uploadModeTextureData(modeTextures[0], newData, bank.gridSize)
            uploadModeTextureData(modeTextures[1], newData, bank.gridSize)
            previousModeData = newData
            currentModeData = newData
            currentModeGridSize = bank.gridSize
            modeTransitionStartedNanos = 0L
            hasModeTexture = true
            return
        }

        val oldCurrent = requireNotNull(currentModeData)
        val oldPrevious = requireNotNull(previousModeData)
        val displayedData = if (oldCurrent.size == oldPrevious.size && existingBlend < 0.999f) {
            CymaticsMotion.blendModalData(oldPrevious, oldCurrent, existingBlend)
        } else {
            oldCurrent
        }
        val previousSlot = currentModeSlot
        val newCurrentSlot = 1 - currentModeSlot
        uploadModeTextureData(modeTextures[previousSlot], displayedData, currentModeGridSize)
        uploadModeTextureData(modeTextures[newCurrentSlot], newData, bank.gridSize)
        currentModeSlot = newCurrentSlot
        previousModeData = displayedData
        currentModeData = newData
        currentModeGridSize = bank.gridSize
        modeTransitionStartedNanos = now
    }

    private fun interleavedModeData(bank: CymaticsModeBank, modeIndex: Int): FloatArray {
        val field = bank.field(modeIndex)
        return FloatArray(field.size * 2).also { interleaved ->
            field.indices.forEach { index ->
                interleaved[index * 2] = field[index]
                interleaved[index * 2 + 1] = bank.mask[index]
            }
        }
    }

    private fun uploadModeTextureData(texture: Int, data: FloatArray, gridSize: Int) {
        val buffer = ByteBuffer.allocateDirect(data.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(data).position(0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RG32F,
            gridSize,
            gridSize,
            0,
            GLES30.GL_RG,
            GLES30.GL_FLOAT,
            buffer
        )
        // RG32F linear filtering is not guaranteed on every GLES 3.0 device.
        // Both shaders perform explicit bilinear interpolation with texelFetch.
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_NEAREST
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_NEAREST
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE
        )
    }

    private fun createSurfaceMesh() {
        val side = 128
        val vertices = FloatArray(side * side * 2)
        var cursor = 0
        repeat(side) { row ->
            repeat(side) { column ->
                vertices[cursor++] = column / (side - 1f)
                vertices[cursor++] = row / (side - 1f)
            }
        }
        val indices = IntArray((side - 1) * (side - 1) * 6)
        cursor = 0
        repeat(side - 1) { row ->
            repeat(side - 1) { column ->
                val topLeft = row * side + column
                val bottomLeft = (row + 1) * side + column
                indices[cursor++] = topLeft
                indices[cursor++] = bottomLeft
                indices[cursor++] = topLeft + 1
                indices[cursor++] = topLeft + 1
                indices[cursor++] = bottomLeft
                indices[cursor++] = bottomLeft + 1
            }
        }
        val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }
        val indexBuffer = ByteBuffer.allocateDirect(indices.size * Int.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .apply {
                put(indices)
                position(0)
            }
        val ids = IntArray(1)
        GLES30.glGenVertexArrays(1, ids, 0)
        surfaceVao = ids[0]
        GLES30.glGenBuffers(1, ids, 0)
        surfaceVbo = ids[0]
        GLES30.glGenBuffers(1, ids, 0)
        surfaceEbo = ids[0]
        GLES30.glBindVertexArray(surfaceVao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, surfaceVbo)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertices.size * Float.SIZE_BYTES,
            vertexBuffer,
            GLES30.GL_STATIC_DRAW
        )
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(
            0,
            2,
            GLES30.GL_FLOAT,
            false,
            2 * Float.SIZE_BYTES,
            0
        )
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, surfaceEbo)
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            indices.size * Int.SIZE_BYTES,
            indexBuffer,
            GLES30.GL_STATIC_DRAW
        )
        GLES30.glBindVertexArray(0)
        surfaceIndexCount = indices.size
    }

    private fun createSandBuffers() {
        GLES30.glGenVertexArrays(sandVaos.size, sandVaos, 0)
        GLES30.glGenBuffers(sandVbos.size, sandVbos, 0)
    }

    private fun createModeTextures() {
        GLES30.glGenTextures(modeTextures.size, modeTextures, 0)
    }

    private fun createFullscreenVao() {
        val ids = IntArray(1)
        GLES30.glGenVertexArrays(1, ids, 0)
        fullscreenVao = ids[0]
    }

    private fun createPostProcessingTargets() {
        deletePostProcessingTargets()
        if (width <= 1 || height <= 1) return

        val id = IntArray(1)
        GLES30.glGenTextures(1, id, 0)
        sceneTexture = id[0]
        configureColorTexture(sceneTexture, width, height)

        GLES30.glGenFramebuffers(1, id, 0)
        sceneFramebuffer = id[0]
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, sceneFramebuffer)
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D,
            sceneTexture,
            0
        )
        GLES30.glGenRenderbuffers(1, id, 0)
        sceneDepthBuffer = id[0]
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, sceneDepthBuffer)
        GLES30.glRenderbufferStorage(
            GLES30.GL_RENDERBUFFER,
            GLES30.GL_DEPTH_COMPONENT24,
            width,
            height
        )
        GLES30.glFramebufferRenderbuffer(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_DEPTH_ATTACHMENT,
            GLES30.GL_RENDERBUFFER,
            sceneDepthBuffer
        )
        var complete = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) ==
            GLES30.GL_FRAMEBUFFER_COMPLETE

        bloomWidth = (width / 2).coerceAtLeast(1)
        bloomHeight = (height / 2).coerceAtLeast(1)
        GLES30.glGenTextures(bloomTextures.size, bloomTextures, 0)
        GLES30.glGenFramebuffers(bloomFramebuffers.size, bloomFramebuffers, 0)
        bloomTextures.indices.forEach { index ->
            configureColorTexture(bloomTextures[index], bloomWidth, bloomHeight)
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, bloomFramebuffers[index])
            GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER,
                GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D,
                bloomTextures[index],
                0
            )
            complete = complete && GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) ==
                GLES30.GL_FRAMEBUFFER_COMPLETE
        }
        postProcessingReady = complete
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        if (!complete) deletePostProcessingTargets()
    }

    private fun configureColorTexture(texture: Int, textureWidth: Int, textureHeight: Int) {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA8,
            textureWidth,
            textureHeight,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            null
        )
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
    }

    private fun deletePostProcessingTargets() {
        postProcessingReady = false
        if (sceneFramebuffer != 0) {
            GLES30.glDeleteFramebuffers(1, intArrayOf(sceneFramebuffer), 0)
            sceneFramebuffer = 0
        }
        if (sceneDepthBuffer != 0) {
            GLES30.glDeleteRenderbuffers(1, intArrayOf(sceneDepthBuffer), 0)
            sceneDepthBuffer = 0
        }
        if (sceneTexture != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(sceneTexture), 0)
            sceneTexture = 0
        }
        if (bloomFramebuffers.any { it != 0 }) {
            GLES30.glDeleteFramebuffers(bloomFramebuffers.size, bloomFramebuffers, 0)
            bloomFramebuffers.fill(0)
        }
        if (bloomTextures.any { it != 0 }) {
            GLES30.glDeleteTextures(bloomTextures.size, bloomTextures, 0)
            bloomTextures.fill(0)
        }
    }

    private fun cacheUniforms() {
        listOf(
            "uMvp",
            "uModeTexture",
            "uPreviousModeTexture",
            "uModeBlend",
            "uPhase",
            "uAmplitude",
            "uVisualScale",
            "uGridSize",
            "uVerticalOffset",
            "uVariable",
            "uStyle",
            "uPalette",
            "uRenderPass",
            "uShowNodes",
            "uShowAntinodes",
            "uShowPhaseLines",
            "uShowGrid",
            "uShowExciter",
            "uShowProbe",
            "uOrbitingLight",
            "uResonanceAura",
            "uEnergyPulses",
            "uDrivePosition",
            "uProbePosition",
            "uLightHue",
            "uContourDensity",
            "uAnimationTime",
            "uMotionIntensity",
            "uResonance",
            "uCameraPosition"
        ).forEach { surfaceUniforms[it] = GLES30.glGetUniformLocation(surfaceProgram, it) }
        listOf(
            "uMvp",
            "uModeTexture",
            "uPreviousModeTexture",
            "uModeBlend",
            "uProgress",
            "uPhase",
            "uAmplitude",
            "uVisualScale",
            "uPointSize",
            "uAnimationTime",
            "uMotionIntensity",
            "uResonance",
            "uFlowIntensity",
            "uLayerAlpha",
            "uLayer",
            "uStyle",
            "uPalette"
        ).forEach { sandUniforms[it] = GLES30.glGetUniformLocation(sandProgram, it) }
    }

    private fun updateAdaptiveQuality(state: CymaticsState, stats: PerformanceStats) {
        if (!state.autoQuality) {
            adaptiveFraction = 1f
            postProcessingBudgetEnabled = true
            return
        }
        if (stats.thermalStatus >= 3 || stats.fps in 1f..43.9f) {
            postProcessingBudgetEnabled = false
        } else if (stats.fps >= 57f && adaptiveFraction >= 0.72f) {
            postProcessingBudgetEnabled = true
        }
        adaptiveFraction = when {
            stats.thermalStatus >= 3 -> (adaptiveFraction * 0.76f).coerceAtLeast(0.25f)
            stats.fps in 1f..53.9f -> (adaptiveFraction * 0.86f).coerceAtLeast(0.25f)
            stats.fps >= 59f -> (adaptiveFraction + 0.05f).coerceAtMost(1f)
            else -> adaptiveFraction
        }
    }

    private fun publishLoadingStats() {
        fpsCounter.frame(
            0,
            controller.state.sandParticleCount,
            1f,
            width,
            height,
            simulationTimeS,
            thermalMonitor.currentStatus()
        )?.let { stats ->
            mainHandler.post {
                statsCallback(stats)
                infoCallback(
                    CymaticsRenderInfo(
                        motionProfile = controller.state.motionPreset.label,
                        rendererPath = "Cargando banco modal reproducible"
                    )
                )
            }
        }
    }

    private fun modeTransitionProgress(now: Long, state: CymaticsState): Float =
        if (modeTransitionStartedNanos == 0L) {
            1f
        } else {
            CymaticsMotion.transitionProgress(
                elapsedSeconds(modeTransitionStartedNanos, now),
                state
            )
        }

    private fun sandTransitionProgress(now: Long, state: CymaticsState): Float =
        if (sandTransitionStartedNanos == 0L) {
            1f
        } else {
            CymaticsMotion.sandTransitionProgress(
                elapsedSeconds(sandTransitionStartedNanos, now),
                state
            )
        }

    private fun adaptedCount(sourceCount: Int): Int =
        (sourceCount * adaptiveFraction).toInt().coerceIn(0, sourceCount)

    private fun elapsedSeconds(startNanos: Long, nowNanos: Long): Double =
        (nowNanos - startNanos).coerceAtLeast(0L) / 1_000_000_000.0

    private fun assetText(path: String): String =
        appContext.assets.open(path).bufferedReader().use { it.readText() }

    private fun wrapPhase(value: Double): Double {
        val period = 2.0 * PI
        val wrapped = value % period
        return if (wrapped < 0.0) wrapped + period else wrapped
    }

    private fun surface1f(name: String, value: Float) =
        GLES30.glUniform1f(surfaceUniforms.getValue(name), value)

    private fun surface1i(name: String, value: Int) =
        GLES30.glUniform1i(surfaceUniforms.getValue(name), value)

    private fun surface2f(name: String, x: Float, y: Float) =
        GLES30.glUniform2f(surfaceUniforms.getValue(name), x, y)

    private fun surface3f(name: String, x: Float, y: Float, z: Float) =
        GLES30.glUniform3f(surfaceUniforms.getValue(name), x, y, z)

    private fun surfaceMatrix(name: String, value: FloatArray) =
        GLES30.glUniformMatrix4fv(surfaceUniforms.getValue(name), 1, false, value, 0)

    private fun sand1f(name: String, value: Float) =
        GLES30.glUniform1f(sandUniforms.getValue(name), value)

    private fun sand1i(name: String, value: Int) =
        GLES30.glUniform1i(sandUniforms.getValue(name), value)

    private fun sandMatrix(name: String, value: FloatArray) =
        GLES30.glUniformMatrix4fv(sandUniforms.getValue(name), 1, false, value, 0)

    private fun program1f(program: Int, name: String, value: Float) {
        val location = GLES30.glGetUniformLocation(program, name)
        if (location >= 0) GLES30.glUniform1f(location, value)
    }

    private fun program1i(program: Int, name: String, value: Int) {
        val location = GLES30.glGetUniformLocation(program, name)
        if (location >= 0) GLES30.glUniform1i(location, value)
    }

    private fun program2f(program: Int, name: String, x: Float, y: Float) {
        val location = GLES30.glGetUniformLocation(program, name)
        if (location >= 0) GLES30.glUniform2f(location, x, y)
    }

    private fun programMatrix(program: Int, name: String, value: FloatArray) {
        val location = GLES30.glGetUniformLocation(program, name)
        if (location >= 0) GLES30.glUniformMatrix4fv(location, 1, false, value, 0)
    }
}

private data class SandCloud(
    val key: String,
    val count: Int,
    val values: FloatArray
)
