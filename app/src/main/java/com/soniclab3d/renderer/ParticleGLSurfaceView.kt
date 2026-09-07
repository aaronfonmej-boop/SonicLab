package com.soniclab3d.renderer

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.soniclab3d.performance.PerformanceStats
import com.soniclab3d.physics.ReferenceParticle
import com.soniclab3d.simulation.SimulationController

@SuppressLint("ViewConstructor")
class ParticleGLSurfaceView(
    context: Context,
    controller: SimulationController,
    onStats: (PerformanceStats) -> Unit,
    onReference: (ReferenceParticle) -> Unit
) : GLSurfaceView(context) {
    private val particleRenderer = ParticleRenderer(context, controller, onStats, onReference)
    private var lastX = 0f
    private var lastY = 0f
    private var lastCentroidX = 0f
    private var lastCentroidY = 0f

    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                queueEvent { particleRenderer.camera.zoom(detector.scaleFactor) }
                return true
            }
        }
    )

    private val gestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                queueEvent { particleRenderer.selectParticle(e.x, e.y) }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                queueEvent { particleRenderer.camera.reset() }
                return true
            }
        }
    )

    init {
        setEGLContextClientVersion(3)
        preserveEGLContextOnPause = true
        setRenderer(particleRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
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
                        queueEvent { particleRenderer.camera.pan(dx, dy) }
                    }
                    lastCentroidX = cx
                    lastCentroidY = cy
                } else if (!scaleDetector.isInProgress) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    queueEvent { particleRenderer.camera.rotate(dx * 0.28f, dy * 0.28f) }
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
