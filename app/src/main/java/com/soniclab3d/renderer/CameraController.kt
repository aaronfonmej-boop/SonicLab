package com.soniclab3d.renderer

import android.opengl.Matrix
import com.soniclab3d.cymatics.CymaticsCameraMotion
import com.soniclab3d.cymatics.CymaticsCameraPreset
import com.soniclab3d.simulation.CameraPreset
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

data class CameraFrame(
    val mvp: FloatArray,
    val position: FloatArray
)

class CameraController {
    private var yawDegrees = 35f
    private var pitchDegrees = 22f
    private var distance = 12f
    private var panX = 0f
    private var panY = 0f
    private var animatedYawDegrees = yawDegrees
    private var animatedPitchDegrees = pitchDegrees
    private var animatedDistance = distance
    private var animatedPanX = panX
    private var animatedPanY = panY
    private var previousCinematicFrameNanos = 0L
    private var lastInteractionNanos = 0L

    fun rotate(deltaX: Float, deltaY: Float) {
        yawDegrees = (yawDegrees + deltaX) % 360f
        pitchDegrees = (pitchDegrees + deltaY).coerceIn(-85f, 85f)
        noteInteraction()
    }

    fun zoom(scaleFactor: Float) {
        distance = (distance / scaleFactor).coerceIn(2.5f, 35f)
        noteInteraction()
    }

    fun pan(deltaX: Float, deltaY: Float) {
        val scale = distance * 0.0016f
        panX -= deltaX * scale
        panY += deltaY * scale
        noteInteraction()
    }

    fun reset() {
        yawDegrees = 35f
        pitchDegrees = 22f
        distance = 12f
        panX = 0f
        panY = 0f
        noteInteraction()
    }

    fun setPreset(preset: CameraPreset) {
        when (preset) {
            CameraPreset.FREE -> {
                yawDegrees = 35f
                pitchDegrees = 22f
            }
            CameraPreset.TOP -> {
                yawDegrees = 0f
                pitchDegrees = 89f
            }
            CameraPreset.FRONT -> {
                yawDegrees = 0f
                pitchDegrees = 0f
            }
            CameraPreset.SIDE -> {
                yawDegrees = 90f
                pitchDegrees = 0f
            }
        }
        distance = 12f
        panX = 0f
        panY = 0f
        noteInteraction()
    }

    fun setCymaticsPreset(preset: CymaticsCameraPreset) {
        when (preset) {
            CymaticsCameraPreset.FREE -> {
                yawDegrees = 35f
                pitchDegrees = 22f
                distance = 12f
            }
            CymaticsCameraPreset.TOP -> {
                yawDegrees = 0f
                pitchDegrees = 85f
                distance = 11f
            }
            CymaticsCameraPreset.GRAZING -> {
                yawDegrees = -28f
                pitchDegrees = 8f
                distance = 9f
            }
            CymaticsCameraPreset.FRONT -> {
                yawDegrees = 0f
                pitchDegrees = 2f
                distance = 10f
            }
            CymaticsCameraPreset.MACRO_NODE -> {
                yawDegrees = 42f
                pitchDegrees = 34f
                distance = 5.2f
            }
        }
        panX = 0f
        panY = 0f
        noteInteraction()
    }

    fun buildMvp(width: Int, height: Int): FloatArray {
        animatedYawDegrees = yawDegrees
        animatedPitchDegrees = pitchDegrees
        animatedDistance = distance
        animatedPanX = panX
        animatedPanY = panY
        previousCinematicFrameNanos = 0L
        return buildFrame(
            width = width,
            height = height,
            yaw = yawDegrees,
            pitch = pitchDegrees,
            cameraDistance = distance,
            targetX = panX,
            targetY = panY
        ).mvp
    }

    fun buildCinematicFrame(
        width: Int,
        height: Int,
        animationTimeSeconds: Double,
        driftStrength: Float,
        motion: CymaticsCameraMotion
    ): CameraFrame {
        val now = System.nanoTime()
        val deltaSeconds = if (previousCinematicFrameNanos == 0L) {
            0.0
        } else {
            ((now - previousCinematicFrameNanos) / 1_000_000_000.0).coerceIn(0.0, 0.05)
        }
        previousCinematicFrameNanos = now
        val follow = if (deltaSeconds == 0.0) 1f else {
            (1.0 - exp(-deltaSeconds * 9.5)).toFloat()
        }
        animatedYawDegrees = approachAngle(animatedYawDegrees, yawDegrees, follow)
        animatedPitchDegrees += (pitchDegrees - animatedPitchDegrees) * follow
        animatedDistance += (distance - animatedDistance) * follow
        animatedPanX += (panX - animatedPanX) * follow
        animatedPanY += (panY - animatedPanY) * follow

        val secondsSinceInteraction = if (lastInteractionNanos == 0L) {
            Double.POSITIVE_INFINITY
        } else {
            (now - lastInteractionNanos).coerceAtLeast(0L) / 1_000_000_000.0
        }
        val interactionFade = ((secondsSinceInteraction - 0.7) / 1.6).toFloat().coerceIn(0f, 1f)
        val drift = if (motion == CymaticsCameraMotion.STATIC) {
            0f
        } else {
            driftStrength.coerceIn(0f, 1f) * interactionFade
        }
        val time = animationTimeSeconds.toFloat()
        val choreography = when (motion) {
            CymaticsCameraMotion.STATIC -> floatArrayOf(0f, 0f, 0f, 0f, 0f)
            CymaticsCameraMotion.FLOAT -> floatArrayOf(
                sin(time * 0.16f) * 4.2f,
                sin(time * 0.12f + 1.4f) * 1.7f,
                sin(time * 0.10f + 2.1f) * 0.28f,
                sin(time * 0.14f + 0.6f) * 0.07f,
                sin(time * 0.11f + 2.7f) * 0.045f
            )
            CymaticsCameraMotion.ORBIT -> floatArrayOf(
                time * 5.5f,
                sin(time * 0.18f + 0.8f) * 2.2f,
                sin(time * 0.12f + 2.4f) * 0.34f,
                sin(time * 0.17f) * 0.08f,
                sin(time * 0.13f + 1.5f) * 0.05f
            )
            CymaticsCameraMotion.SHOW -> floatArrayOf(
                sin(time * 0.15f) * 10.5f + sin(time * 0.071f + 1.2f) * 3.4f,
                sin(time * 0.11f + 1.4f) * 3.8f,
                sin(time * 0.095f + 2.1f) * 0.56f,
                sin(time * 0.13f + 0.6f) * 0.12f,
                sin(time * 0.10f + 2.7f) * 0.075f
            )
        }
        val driftYaw = choreography[0] * drift
        val driftPitch = choreography[1] * drift
        val driftDistance = choreography[2] * drift
        val driftPanX = choreography[3] * drift
        val driftPanY = choreography[4] * drift

        return buildFrame(
            width = width,
            height = height,
            yaw = animatedYawDegrees + driftYaw,
            pitch = animatedPitchDegrees + driftPitch,
            cameraDistance = animatedDistance + driftDistance,
            targetX = animatedPanX + driftPanX,
            targetY = animatedPanY + driftPanY
        )
    }

    private fun buildFrame(
        width: Int,
        height: Int,
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        targetX: Float,
        targetY: Float
    ): CameraFrame {
        val projection = FloatArray(16)
        val view = FloatArray(16)
        val result = FloatArray(16)
        val aspect = width.toFloat() / height.coerceAtLeast(1).toFloat()
        Matrix.perspectiveM(projection, 0, 48f, aspect, 0.1f, 80f)

        val yawRadians = Math.toRadians(yaw.toDouble())
        val pitchRadians = Math.toRadians(pitch.toDouble())
        val horizontal = cameraDistance * cos(pitchRadians).toFloat()
        val eyeX = targetX + horizontal * sin(yawRadians).toFloat()
        val eyeY = targetY + cameraDistance * sin(pitchRadians).toFloat()
        val eyeZ = horizontal * cos(yawRadians).toFloat()
        Matrix.setLookAtM(
            view, 0,
            eyeX, eyeY, eyeZ,
            targetX, targetY, 0f,
            0f, 1f, 0f
        )
        Matrix.multiplyMM(result, 0, projection, 0, view, 0)
        return CameraFrame(result, floatArrayOf(eyeX, eyeY, eyeZ))
    }

    fun cameraPosition(): FloatArray {
        val yaw = Math.toRadians(yawDegrees.toDouble())
        val pitch = Math.toRadians(pitchDegrees.toDouble())
        val horizontal = distance * cos(pitch).toFloat()
        return floatArrayOf(
            panX + horizontal * sin(yaw).toFloat(),
            panY + distance * sin(pitch).toFloat(),
            horizontal * cos(yaw).toFloat()
        )
    }

    private fun noteInteraction() {
        lastInteractionNanos = System.nanoTime()
    }

    private fun approachAngle(current: Float, target: Float, amount: Float): Float {
        val delta = ((target - current + 540f) % 360f) - 180f
        return current + delta * amount
    }
}
