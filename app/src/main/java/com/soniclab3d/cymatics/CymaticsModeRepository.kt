package com.soniclab3d.cymatics

import android.content.Context
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

class CymaticsModeRepository(context: Context) {
    private val assets = context.applicationContext.assets

    fun load(geometry: PlateGeometry, boundary: PlateBoundary): CymaticsModeBank {
        val key = "${geometry.assetStem}_${boundary.assetStem}"
        cache[key]?.let { return it }
        return synchronized(cache) {
            cache[key] ?: readBank(geometry, boundary).also { cache[key] = it }
        }
    }

    private fun readBank(
        expectedGeometry: PlateGeometry,
        expectedBoundary: PlateBoundary
    ): CymaticsModeBank {
        val filename = "cymatics/${expectedGeometry.assetStem}_${expectedBoundary.assetStem}.cym"
        return assets.open(filename).use { input ->
            CymaticsModeCodec.decode(input, expectedGeometry, expectedBoundary)
        }
    }

    private companion object {
        val cache = ConcurrentHashMap<String, CymaticsModeBank>()
    }
}

internal object CymaticsModeCodec {
    fun decode(
        source: InputStream,
        expectedGeometry: PlateGeometry,
        expectedBoundary: PlateBoundary
    ): CymaticsModeBank {
        DataInputStream(BufferedInputStream(source)).use { input ->
            val magic = ByteArray(4)
            input.readFully(magic)
            require(magic.contentEquals(byteArrayOf('C'.code.toByte(), 'Y'.code.toByte(), 'M'.code.toByte(), '8'.code.toByte()))) {
                "Banco modal con cabecera inválida"
            }
            val version = input.readInt()
            val gridSize = input.readInt()
            val modeCount = input.readInt()
            val geometryOrdinal = input.readInt()
            val boundaryOrdinal = input.readInt()
            require(version == CymaticsModeBank.FORMAT_VERSION) { "Versión de banco modal no compatible" }
            require(gridSize in 16..512) { "Resolución de banco modal inválida" }
            require(modeCount == CymaticsModeBank.MODE_COUNT) { "Cantidad de modos inesperada" }
            require(geometryOrdinal == expectedGeometry.ordinal && boundaryOrdinal == expectedBoundary.ordinal) {
                "El banco modal no coincide con la geometría solicitada"
            }
            val eigenvalues = FloatArray(modeCount) { input.readFloat() }
            val cells = gridSize * gridSize
            val mask = FloatArray(cells) { input.readFloat() }
            val fields = FloatArray(modeCount * cells) { input.readFloat() }
            require(eigenvalues.all { it.isFinite() && it > 0f }) { "Autovalores inválidos" }
            require(mask.all { it.isFinite() }) { "Máscara inválida" }
            require(fields.all { it.isFinite() }) { "Campo modal inválido" }
            return CymaticsModeBank(
                gridSize,
                expectedGeometry,
                expectedBoundary,
                eigenvalues,
                mask,
                fields
            )
        }
    }
}
