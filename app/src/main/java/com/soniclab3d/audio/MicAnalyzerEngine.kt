package com.soniclab3d.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.soniclab3d.measurement.SplCalibrationProfile
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class MicAnalysis(
    val fundamentalHz: Double = 0.0,
    val relativeDbFs: Double = -120.0,
    val calibratedSplDb: Double? = null,
    val calibrationProfileId: String? = null,
    val spectrum: List<Float> = List(64) { 0f },
    val route: AudioInputRoute? = null,
    val routeChanged: Boolean = false,
    val running: Boolean = false,
    val error: String? = null
)

class MicAnalyzerEngine {
    private data class RecorderSetup(val recorder: AudioRecord, val audioSource: Int)

    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var running = false
    private var worker: Thread? = null
    private var recorder: AudioRecord? = null

    @SuppressLint("MissingPermission")
    @Synchronized
    fun start(
        calibrationResolver: (AudioInputRoute) -> SplCalibrationProfile? = { null },
        onAnalysis: (MicAnalysis) -> Unit
    ): Boolean {
        if (running) return true
        val sampleRate = 48_000
        val fftSize = 2_048
        val minimumBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(fftSize * 4)
        val setup = runCatching {
            val unprocessedRecorder = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.UNPROCESSED)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minimumBuffer)
                .build()
            if (unprocessedRecorder.state != AudioRecord.STATE_INITIALIZED) {
                unprocessedRecorder.release()
                error("UNPROCESSED no está disponible")
            }
            RecorderSetup(
                recorder = unprocessedRecorder,
                audioSource = MediaRecorder.AudioSource.UNPROCESSED
            )
        }.recoverCatching {
            RecorderSetup(
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minimumBuffer
                ),
                audioSource = MediaRecorder.AudioSource.DEFAULT
            )
        }.getOrElse {
            onAnalysis(MicAnalysis(error = "No fue posible abrir el micrófono"))
            return false
        }
        val audioRecord = setup.recorder
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            onAnalysis(MicAnalysis(error = "Micrófono no disponible"))
            return false
        }
        recorder = audioRecord
        running = true
        worker = thread(name = "SonicLabMicAnalyzer", isDaemon = true) {
            analyzeLoop(
                audioRecord = audioRecord,
                requestedSampleRate = sampleRate,
                fftSize = fftSize,
                audioSource = setup.audioSource,
                bufferSizeBytes = minimumBuffer,
                calibrationResolver = calibrationResolver,
                onAnalysis = onAnalysis
            )
        }
        return true
    }

    @Synchronized
    fun stop() {
        running = false
        runCatching { recorder?.stop() }
        worker?.interrupt()
        worker = null
        runCatching { recorder?.release() }
        recorder = null
    }

    private fun analyzeLoop(
        audioRecord: AudioRecord,
        requestedSampleRate: Int,
        fftSize: Int,
        audioSource: Int,
        bufferSizeBytes: Int,
        calibrationResolver: (AudioInputRoute) -> SplCalibrationProfile?,
        onAnalysis: (MicAnalysis) -> Unit
    ) {
        val input = ShortArray(fftSize)
        var frame = 0
        var previousRouteKey: String? = null
        var routeChanged = false
        try {
            audioRecord.startRecording()
            while (running && !Thread.currentThread().isInterrupted) {
                var offset = 0
                while (offset < fftSize && running) {
                    val read = audioRecord.read(input, offset, fftSize - offset, AudioRecord.READ_BLOCKING)
                    if (read <= 0) break
                    offset += read
                }
                if (offset < fftSize) continue
                val effectiveSampleRate = audioRecord.sampleRate.takeIf { it > 0 } ?: requestedSampleRate
                val analysis = analyze(input, effectiveSampleRate)
                frame++
                if (frame % 3 == 0) {
                    val route = currentRoute(audioRecord, audioSource, bufferSizeBytes)
                    val currentRouteKey = route?.calibrationKey
                    if (previousRouteKey != null && currentRouteKey != null && previousRouteKey != currentRouteKey) {
                        routeChanged = true
                    }
                    if (currentRouteKey != null) previousRouteKey = currentRouteKey
                    val profile = route?.let(calibrationResolver)
                    val calibratedSplDb = if (route != null) {
                        profile?.calibratedSplDb(analysis.relativeDbFs, route)
                    } else {
                        null
                    }
                    val routedAnalysis = analysis.copy(
                        calibratedSplDb = calibratedSplDb,
                        calibrationProfileId = profile?.takeIf { calibratedSplDb != null }?.id,
                        route = route,
                        routeChanged = routeChanged,
                        running = true
                    )
                    mainHandler.post { onAnalysis(routedAnalysis) }
                }
            }
        } catch (_: Throwable) {
            if (running) mainHandler.post { onAnalysis(MicAnalysis(error = "La captura de audio se interrumpió")) }
        } finally {
            runCatching { audioRecord.stop() }
            runCatching { audioRecord.release() }
            if (recorder === audioRecord) recorder = null
            running = false
        }
    }

    private fun analyze(input: ShortArray, sampleRate: Int): MicAnalysis {
        val size = input.size
        val real = DoubleArray(size)
        val imaginary = DoubleArray(size)
        val dcOffset = input.average() / 32768.0
        var sumSquares = 0.0
        for (index in input.indices) {
            val normalizedSample = input[index] / 32768.0 - dcOffset
            sumSquares += normalizedSample * normalizedSample
            val window = 0.5 - 0.5 * cos(2.0 * PI * index / (size - 1))
            real[index] = normalizedSample * window
        }
        fft(real, imaginary)
        val minBin = (20.0 * size / sampleRate).toInt().coerceAtLeast(1)
        val maxBin = min(size / 2 - 1, (20_000.0 * size / sampleRate).toInt())
        var peakBin = minBin
        var peakMagnitude = 0.0
        for (bin in minBin..maxBin) {
            val magnitude = sqrt(real[bin] * real[bin] + imaginary[bin] * imaginary[bin])
            if (magnitude > peakMagnitude) {
                peakMagnitude = magnitude
                peakBin = bin
            }
        }
        val interpolatedBin = if (peakBin in (minBin + 1) until maxBin) {
            val left = magnitude(real, imaginary, peakBin - 1)
            val center = magnitude(real, imaginary, peakBin)
            val right = magnitude(real, imaginary, peakBin + 1)
            val denominator = left - 2.0 * center + right
            peakBin + if (kotlin.math.abs(denominator) > 1e-12) 0.5 * (left - right) / denominator else 0.0
        } else peakBin.toDouble()
        val spectrum = List(64) { band ->
            val start = max(minBin, band * maxBin / 64)
            val end = max(start + 1, (band + 1) * maxBin / 64)
            var bandPeak = 0.0
            for (bin in start until min(end, maxBin + 1)) {
                bandPeak = max(bandPeak, magnitude(real, imaginary, bin))
            }
            (bandPeak / max(peakMagnitude, 1e-12)).toFloat().coerceIn(0f, 1f)
        }
        val rms = sqrt(sumSquares / size)
        val relativeDbFs = (20.0 * log10(max(rms, 1e-12))).coerceIn(-120.0, 0.0)
        return MicAnalysis(
            fundamentalHz = if (relativeDbFs > -80.0) interpolatedBin * sampleRate / size else 0.0,
            relativeDbFs = relativeDbFs,
            spectrum = spectrum,
            running = true
        )
    }

    private fun currentRoute(
        audioRecord: AudioRecord,
        audioSource: Int,
        bufferSizeBytes: Int
    ): AudioInputRoute? {
        val device = audioRecord.routedDevice ?: return null
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) device.address.orEmpty() else ""
        return AudioInputRoute(
            deviceId = device.id,
            deviceType = device.type,
            productName = device.productName?.toString().orEmpty(),
            address = address,
            sampleRateHz = audioRecord.sampleRate,
            channelCount = audioRecord.channelCount,
            encoding = audioRecord.audioFormat,
            audioSource = audioSource,
            bufferSizeBytes = bufferSizeBytes
        )
    }

    private fun magnitude(real: DoubleArray, imaginary: DoubleArray, index: Int): Double =
        sqrt(real[index] * real[index] + imaginary[index] * imaginary[index])

    private fun fft(real: DoubleArray, imaginary: DoubleArray) {
        val size = real.size
        var j = 0
        for (index in 1 until size) {
            var bit = size shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (index < j) {
                val realTemp = real[index]
                real[index] = real[j]
                real[j] = realTemp
                val imaginaryTemp = imaginary[index]
                imaginary[index] = imaginary[j]
                imaginary[j] = imaginaryTemp
            }
        }
        var length = 2
        while (length <= size) {
            val angle = -2.0 * PI / length
            val stepReal = cos(angle)
            val stepImaginary = sin(angle)
            var start = 0
            while (start < size) {
                var weightReal = 1.0
                var weightImaginary = 0.0
                for (offset in 0 until length / 2) {
                    val even = start + offset
                    val odd = even + length / 2
                    val oddReal = real[odd] * weightReal - imaginary[odd] * weightImaginary
                    val oddImaginary = real[odd] * weightImaginary + imaginary[odd] * weightReal
                    real[odd] = real[even] - oddReal
                    imaginary[odd] = imaginary[even] - oddImaginary
                    real[even] += oddReal
                    imaginary[even] += oddImaginary
                    val nextWeightReal = weightReal * stepReal - weightImaginary * stepImaginary
                    weightImaginary = weightReal * stepImaginary + weightImaginary * stepReal
                    weightReal = nextWeightReal
                }
                start += length
            }
            length = length shl 1
        }
    }
}
