package com.soniclab3d.audio

import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.MediaRecorder
import java.util.Locale

/**
 * Identifica la entrada y la configuración que afectan a una calibración SPL.
 * El identificador de ruta es deliberadamente conservador: si Android informa
 * un dispositivo distinto, la app exige una calibración nueva.
 */
data class AudioInputRoute(
    val deviceId: Int,
    val deviceType: Int,
    val productName: String,
    val address: String,
    val sampleRateHz: Int,
    val channelCount: Int,
    val encoding: Int,
    val audioSource: Int,
    val bufferSizeBytes: Int
) {
    val calibrationKey: String
        get() = listOf(
            deviceId.toString(),
            deviceType.toString(),
            normalized(productName),
            normalized(address),
            sampleRateHz.toString(),
            channelCount.toString(),
            encoding.toString(),
            audioSource.toString()
        ).joinToString("|")

    fun displayName(): String = productName.ifBlank { deviceTypeLabel() }

    fun deviceTypeLabel(): String = when (deviceType) {
        AudioDeviceInfo.TYPE_BUILTIN_MIC -> "Micrófono integrado"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "Audio USB"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "Micrófono USB"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Auriculares con micrófono"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
        AudioDeviceInfo.TYPE_BLE_HEADSET -> "Auriculares Bluetooth LE"
        AudioDeviceInfo.TYPE_BLE_BROADCAST -> "Bluetooth LE Broadcast"
        AudioDeviceInfo.TYPE_TELEPHONY -> "Telefonía"
        else -> "Entrada de audio tipo $deviceType"
    }

    fun encodingLabel(): String = when (encoding) {
        AudioFormat.ENCODING_PCM_16BIT -> "PCM 16 bit"
        AudioFormat.ENCODING_PCM_FLOAT -> "PCM float"
        AudioFormat.ENCODING_PCM_8BIT -> "PCM 8 bit"
        else -> "Formato $encoding"
    }

    fun sourceLabel(): String = when (audioSource) {
        MediaRecorder.AudioSource.UNPROCESSED -> "UNPROCESSED"
        MediaRecorder.AudioSource.DEFAULT -> "DEFAULT"
        MediaRecorder.AudioSource.MIC -> "MIC"
        else -> "Fuente $audioSource"
    }

    fun configurationLabel(): String =
        "$sampleRateHz Hz · ${encodingLabel()} · ${channelCount} canal${if (channelCount == 1) "" else "es"} · ${sourceLabel()}"

    private fun normalized(value: String): String = value
        .trim()
        .lowercase(Locale.ROOT)
        .replace('|', '_')
}
