package com.soniclab3d.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.soniclab3d.physics.AcousticState
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class SynthAudioEngine {
    @Volatile
    private var config = AudioConfig()
    @Volatile
    private var running = false
    private var worker: Thread? = null

    fun update(state: AcousticState) {
        config = AudioConfig(
            frequencyHz = state.frequencyHz.coerceIn(20.0, 20_000.0),
            volume = state.audioVolume.coerceIn(0f, 0.08f),
            harmonics = if (state.harmonicMode) {
                state.harmonicAmplitudes.take(16).toFloatArray()
            } else {
                FloatArray(16) { index -> if (index == 0) 1f else 0f }
            }
        )
        if (state.audioEnabled && !running) start()
        if (!state.audioEnabled && running) stop()
    }

    @Synchronized
    private fun start() {
        if (running) return
        running = true
        worker = thread(name = "SonicLabAudio", isDaemon = true) { renderLoop() }
    }

    @Synchronized
    fun stop() {
        running = false
        worker?.interrupt()
        worker = null
    }

    private fun renderLoop() {
        val sampleRate = 48_000
        val minimum = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        ).coerceAtLeast(2_048)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minimum * Float.SIZE_BYTES)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        val buffer = FloatArray(1_024)
        var phase = 0.0
        try {
            track.play()
            while (running && !Thread.currentThread().isInterrupted) {
                val current = config
                val amplitudes = current.harmonics
                val normalization = amplitudes.sumOf { abs(it.toDouble()) }.coerceAtLeast(1.0)
                val phaseStep = 2.0 * PI * current.frequencyHz / sampleRate
                for (sampleIndex in buffer.indices) {
                    var value = 0.0
                    amplitudes.forEachIndexed { index, amplitude ->
                        val order = index + 1
                        if (current.frequencyHz * order < sampleRate * 0.48) {
                            value += amplitude * sin(phase * order)
                        }
                    }
                    buffer[sampleIndex] = (value / normalization * current.volume).toFloat()
                    phase += phaseStep
                    if (phase > 2.0 * PI) phase -= 2.0 * PI
                }
                val written = track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
                if (written < 0) break
            }
        } finally {
            runCatching { track.pause() }
            runCatching { track.flush() }
            runCatching { track.release() }
            running = false
        }
    }
}

private data class AudioConfig(
    val frequencyHz: Double = 120.0,
    val volume: Float = 0.025f,
    val harmonics: FloatArray = FloatArray(16) { index -> if (index == 0) 1f else 0f }
)
