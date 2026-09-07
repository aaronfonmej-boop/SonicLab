package com.soniclab3d.renderer

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.soniclab3d.cymatics.CymaticsController
import com.soniclab3d.cymatics.CymaticsRenderInfo
import com.soniclab3d.performance.PerformanceStats

@SuppressLint("ViewConstructor")
class CymaticsGLSurfaceView(
    context: Context,
    private val controller: CymaticsController,
    onStats: (PerformanceStats) -> Unit,
    onInfo: (CymaticsRenderInfo) -> Unit
) : GLSurfaceView(context) {
    private val cymaticsRenderer = CymaticsRenderer(context, controller, onStats, onInfo)
    private var lastX = 0f
    private var lastY = 0f
    private var lastCentroidX = 0f
    private var lastCentroidY = 0f

    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                queueEvent { cymaticsRenderer.camera.zoom(detector.scaleFactor) }
                return true
            }
        }
    )

    private val gestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                val normalizedX = (e.x / width.coerceAtLeast(1) * 2f - 1f).coerceIn(-0.95f, 0.95f)
                val normalizedY = (1f - e.y / height.coerceAtLeast(1) * 2f).coerceIn(-0.95f, 0.95f)
                controller.update { it.copy(driveX = normalizedX, driveY = normalizedY) }
                if (controller.state.hapticsEnabled) performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val normalizedX = (e.x / width.coerceAtLeast(1) * 2f - 1f).coerceIn(-0.95f, 0.95f)
                val normalizedY = (1f - e.y / height.coerceAtLeast(1) * 2f).coerceIn(-0.95f, 0.95f)
                controller.update {
                    it.copy(probeEnabled = true, probeX = normalizedX, probeY = normalizedY)
                }
                if (controller.state.hapticsEnabled) performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                queueEvent { cymaticsRenderer.camera.reset() }
                if (controller.state.hapticsEnabled) performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                return true
            }
        }
    )

    init {
        contentDescription = "Placa modal 3D interactiva; toca para mover el excitador y mantén pulsado para colocar una sonda"
        setEGLContextClientVersion(3)
        preserveEGLContextOnPause = true
        setRenderer(cymaticsRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun resetSand() {
        controller.resetSand()
        queueEvent { cymaticsRenderer.resetSandAnimation() }
    }

    fun releaseRenderer() {
        queueEvent { cymaticsRenderer.releaseGl() }
        onPause()
        cymaticsRenderer.release()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                lastCentroidX = centroidX(event)
                lastCentroidY = centroidY(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount >= 2) {
                    val cx = centroidX(event)
                    val cy = centroidY(event)
                    if (!scaleDetector.isInProgress) {
                        val dx = cx - lastCentroidX
                        val dy = cy - lastCentroidY
                        queueEvent { cymaticsRenderer.camera.pan(dx, dy) }
                    }
                    lastCentroidX = cx
                    lastCentroidY = cy
                } else if (!scaleDetector.isInProgress) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    queueEvent { cymaticsRenderer.camera.rotate(dx * 0.28f, dy * 0.28f) }
                    lastX = event.x
                    lastY = event.y
                }
            }
            MotionEvent.ACTION_UP -> performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun centroidX(event: MotionEvent): Float =
        (0 until event.pointerCount).sumOf { event.getX(it).toDouble() }.toFloat() / event.pointerCount

    private fun centroidY(event: MotionEvent): Float =
        (0 until event.pointerCount).sumOf { event.getY(it).toDouble() }.toFloat() / event.pointerCount
}
