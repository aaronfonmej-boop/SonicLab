package com.soniclab3d.physics

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

data class RectangularRoom(
    val lengthM: Double = 6.0,
    val widthM: Double = 4.0,
    val heightM: Double = 2.8,
    val modeX: Int = 1,
    val modeY: Int = 0,
    val modeZ: Int = 0
) {
    init {
        require(lengthM > 0.0 && widthM > 0.0 && heightM > 0.0)
        require(modeX >= 0 && modeY >= 0 && modeZ >= 0)
        require(modeX + modeY + modeZ > 0)
    }
}

object RoomAcoustics {
    fun modeFrequencyHz(room: RectangularRoom, soundSpeedMps: Double): Double {
        val x = room.modeX / room.lengthM
        val y = room.modeY / room.widthM
        val z = room.modeZ / room.heightM
        return soundSpeedMps * 0.5 * sqrt(x * x + y * y + z * z)
    }

    fun normalizedPressure(room: RectangularRoom, xM: Double, yM: Double, zM: Double): Double =
        cos(room.modeX * PI * xM.coerceIn(0.0, room.lengthM) / room.lengthM) *
            cos(room.modeY * PI * yM.coerceIn(0.0, room.widthM) / room.widthM) *
            cos(room.modeZ * PI * zM.coerceIn(0.0, room.heightM) / room.heightM)

    fun modeType(room: RectangularRoom): String = when (
        listOf(room.modeX, room.modeY, room.modeZ).count { it > 0 }
    ) {
        1 -> "Axial"
        2 -> "Tangencial"
        else -> "Oblicuo"
    }
}

data class AcousticProbe(
    val label: String,
    val xM: Double,
    val yM: Double = 0.0,
    val zM: Double = 0.0
)
