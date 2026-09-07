package com.soniclab3d.cymatics

import kotlin.math.floor

data class CymaticsModeBank(
    val gridSize: Int,
    val geometry: PlateGeometry,
    val boundary: PlateBoundary,
    val eigenvalues: FloatArray,
    val mask: FloatArray,
    val fields: FloatArray
) {
    init {
        require(gridSize >= 16)
        require(eigenvalues.size == MODE_COUNT)
        require(mask.size == gridSize * gridSize)
        require(fields.size == MODE_COUNT * gridSize * gridSize)
    }

    fun field(modeIndex: Int): FloatArray {
        val mode = modeIndex.coerceIn(0, MODE_COUNT - 1)
        val cells = gridSize * gridSize
        return fields.copyOfRange(mode * cells, (mode + 1) * cells)
    }

    fun sample(modeIndex: Int, normalizedX: Float, normalizedY: Float): Float {
        val mode = modeIndex.coerceIn(0, MODE_COUNT - 1)
        val x = ((normalizedX + 1f) * 0.5f * (gridSize - 1)).coerceIn(0f, gridSize - 1f)
        val y = ((normalizedY + 1f) * 0.5f * (gridSize - 1)).coerceIn(0f, gridSize - 1f)
        val x0 = floor(x).toInt()
        val y0 = floor(y).toInt()
        val x1 = (x0 + 1).coerceAtMost(gridSize - 1)
        val y1 = (y0 + 1).coerceAtMost(gridSize - 1)
        val tx = x - x0
        val ty = y - y0
        val offset = mode * gridSize * gridSize
        fun at(column: Int, row: Int): Float = fields[offset + row * gridSize + column]
        val a = at(x0, y0) * (1f - tx) + at(x1, y0) * tx
        val b = at(x0, y1) * (1f - tx) + at(x1, y1) * tx
        return a * (1f - ty) + b * ty
    }

    fun isInside(normalizedX: Float, normalizedY: Float): Boolean {
        val x = (((normalizedX + 1f) * 0.5f * (gridSize - 1)).toInt()).coerceIn(0, gridSize - 1)
        val y = (((normalizedY + 1f) * 0.5f * (gridSize - 1)).toInt()).coerceIn(0, gridSize - 1)
        return mask[y * gridSize + x] > 0.5f
    }

    companion object {
        const val MODE_COUNT = 24
        const val FORMAT_VERSION = 1
        const val MODEL_ID = "cymatics-kirchhoff-love-modal-0.8"
    }
}
