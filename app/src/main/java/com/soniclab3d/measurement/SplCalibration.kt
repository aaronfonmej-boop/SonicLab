package com.soniclab3d.measurement

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.soniclab3d.audio.AudioInputRoute
import java.io.IOException
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.math.sqrt

private val Context.splCalibrationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spl_calibration_profiles"
)

data class SplCalibrationProfile(
    val id: String,
    val routeKey: String,
    val route: AudioInputRoute,
    val referenceSplDb: Double,
    val capturedDbFs: Double,
    val offsetDb: Double,
    val method: String,
    val createdAtEpochMs: Long,
    val valid: Boolean = true,
    val invalidatedAtEpochMs: Long? = null
) {
    fun appliesTo(currentRoute: AudioInputRoute): Boolean =
        valid && routeKey == currentRoute.calibrationKey

    fun calibratedSplDb(relativeDbFs: Double, currentRoute: AudioInputRoute): Double? =
        if (appliesTo(currentRoute) && relativeDbFs.isFinite()) relativeDbFs + offsetDb else null
}

class SplCalibrationRepository(context: Context) {
    private val dataStore = context.applicationContext.splCalibrationDataStore
    private val profilesKey = stringPreferencesKey("profiles_v1")

    @Volatile
    private var cachedProfiles: List<SplCalibrationProfile> = emptyList()

    val profiles: Flow<List<SplCalibrationProfile>> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> SplCalibrationCodec.decode(preferences[profilesKey].orEmpty()) }
        .onEach { cachedProfiles = it }
        .distinctUntilChanged()

    fun applicableProfile(route: AudioInputRoute): SplCalibrationProfile? = cachedProfiles
        .asSequence()
        .filter { it.appliesTo(route) }
        .maxByOrNull { it.createdAtEpochMs }

    suspend fun save(profile: SplCalibrationProfile) {
        dataStore.edit { preferences ->
            val current = SplCalibrationCodec.decode(preferences[profilesKey].orEmpty())
            val updated = (current.filterNot { it.routeKey == profile.routeKey } + profile)
                .sortedByDescending { it.createdAtEpochMs }
            preferences[profilesKey] = SplCalibrationCodec.encode(updated)
            cachedProfiles = updated
        }
    }

    suspend fun invalidate(profileId: String, nowEpochMs: Long = System.currentTimeMillis()) {
        dataStore.edit { preferences ->
            val updated = SplCalibrationCodec.decode(preferences[profilesKey].orEmpty()).map { profile ->
                if (profile.id == profileId) {
                    profile.copy(valid = false, invalidatedAtEpochMs = nowEpochMs)
                } else {
                    profile
                }
            }
            preferences[profilesKey] = SplCalibrationCodec.encode(updated)
            cachedProfiles = updated
        }
    }

    suspend fun delete(profileId: String) {
        dataStore.edit { preferences ->
            val updated = SplCalibrationCodec.decode(preferences[profilesKey].orEmpty())
                .filterNot { it.id == profileId }
            preferences[profilesKey] = SplCalibrationCodec.encode(updated)
            cachedProfiles = updated
        }
    }
}

data class SplCalibrationCaptureState(
    val active: Boolean = false,
    val progress: Float = 0f,
    val durationMs: Long = SplCalibrationCaptureController.DEFAULT_DURATION_MS,
    val sampleCount: Int = 0,
    val message: String = "Sin captura activa",
    val error: String? = null
)

data class SplCalibrationCaptureUpdate(
    val state: SplCalibrationCaptureState,
    val completedProfile: SplCalibrationProfile? = null
)

class SplCalibrationCaptureController(
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private data class Session(
        val route: AudioInputRoute,
        val referenceSplDb: Double,
        val method: String,
        val startedAtEpochMs: Long,
        val samples: MutableList<Double> = mutableListOf()
    )

    private var session: Session? = null
    var state: SplCalibrationCaptureState = SplCalibrationCaptureState()
        private set

    @Synchronized
    fun start(
        route: AudioInputRoute,
        referenceSplDb: Double,
        method: String,
        nowEpochMs: Long = clock()
    ): SplCalibrationCaptureState {
        val cleanMethod = method.trim()
        val validationError = when {
            !referenceSplDb.isFinite() || referenceSplDb !in 20.0..140.0 ->
                "La referencia debe estar entre 20 y 140 dB SPL"
            cleanMethod.isBlank() -> "Indica el método o equipo de referencia"
            else -> null
        }
        if (validationError != null) {
            session = null
            state = SplCalibrationCaptureState(error = validationError, message = "Calibración no iniciada")
            return state
        }
        session = Session(route, referenceSplDb, cleanMethod, nowEpochMs)
        state = SplCalibrationCaptureState(
            active = true,
            message = "Mantén estable la fuente y ambos micrófonos durante 5,0 s"
        )
        return state
    }

    @Synchronized
    fun addSample(
        relativeDbFs: Double,
        route: AudioInputRoute?,
        nowEpochMs: Long = clock()
    ): SplCalibrationCaptureUpdate {
        val activeSession = session ?: return SplCalibrationCaptureUpdate(state)
        if (route == null || route.calibrationKey != activeSession.route.calibrationKey) {
            session = null
            state = SplCalibrationCaptureState(
                message = "Calibración cancelada",
                error = "La ruta de audio cambió durante la captura; vuelve a calibrar"
            )
            return SplCalibrationCaptureUpdate(state)
        }

        if (relativeDbFs.isFinite() && relativeDbFs in MIN_CAPTURE_DBFS..MAX_CAPTURE_DBFS) {
            activeSession.samples += relativeDbFs
        }
        val elapsedMs = (nowEpochMs - activeSession.startedAtEpochMs).coerceAtLeast(0L)
        val progress = (elapsedMs.toDouble() / DEFAULT_DURATION_MS).toFloat().coerceIn(0f, 1f)
        if (elapsedMs < DEFAULT_DURATION_MS) {
            state = SplCalibrationCaptureState(
                active = true,
                progress = progress,
                sampleCount = activeSession.samples.size,
                message = "Capturando referencia estable: ${elapsedMs / 100L / 10.0} / 5,0 s"
            )
            return SplCalibrationCaptureUpdate(state)
        }

        session = null
        if (activeSession.samples.size < MIN_SAMPLES) {
            state = SplCalibrationCaptureState(
                progress = 1f,
                sampleCount = activeSession.samples.size,
                message = "Captura rechazada",
                error = "Señal insuficiente o saturada; ajusta el nivel y repite"
            )
            return SplCalibrationCaptureUpdate(state)
        }

        val meanDbFs = activeSession.samples.average()
        val standardDeviationDb = sqrt(
            activeSession.samples.sumOf { sample ->
                val difference = sample - meanDbFs
                difference * difference
            } / activeSession.samples.size
        )
        if (standardDeviationDb > MAX_STANDARD_DEVIATION_DB) {
            state = SplCalibrationCaptureState(
                progress = 1f,
                sampleCount = activeSession.samples.size,
                message = "Captura rechazada",
                error = "La señal varió ${"%.2f".format(standardDeviationDb)} dB; estabilízala y repite"
            )
            return SplCalibrationCaptureUpdate(state)
        }

        val profile = SplCalibrationProfile(
            id = UUID.randomUUID().toString(),
            routeKey = activeSession.route.calibrationKey,
            route = activeSession.route,
            referenceSplDb = activeSession.referenceSplDb,
            capturedDbFs = meanDbFs,
            offsetDb = activeSession.referenceSplDb - meanDbFs,
            method = activeSession.method,
            createdAtEpochMs = nowEpochMs
        )
        state = SplCalibrationCaptureState(
            progress = 1f,
            sampleCount = activeSession.samples.size,
            message = "Captura estable completada · σ = ${"%.2f".format(standardDeviationDb)} dB"
        )
        return SplCalibrationCaptureUpdate(state, completedProfile = profile)
    }

    @Synchronized
    fun cancel() {
        session = null
        state = SplCalibrationCaptureState(message = "Captura cancelada")
    }

    companion object {
        const val DEFAULT_DURATION_MS = 5_000L
        const val MIN_SAMPLES = 20
        const val MAX_STANDARD_DEVIATION_DB = 1.5
        const val MIN_CAPTURE_DBFS = -100.0
        const val MAX_CAPTURE_DBFS = -0.5
    }
}

internal object SplCalibrationCodec {
    fun encode(profiles: List<SplCalibrationProfile>): String = profiles.joinToString("\n") { profile ->
        listOf(
            "1",
            encoded(profile.id),
            encoded(profile.routeKey),
            profile.route.deviceId,
            profile.route.deviceType,
            encoded(profile.route.productName),
            encoded(profile.route.address),
            profile.route.sampleRateHz,
            profile.route.channelCount,
            profile.route.encoding,
            profile.route.audioSource,
            profile.route.bufferSizeBytes,
            profile.referenceSplDb,
            profile.capturedDbFs,
            profile.offsetDb,
            encoded(profile.method),
            profile.createdAtEpochMs,
            profile.valid,
            profile.invalidatedAtEpochMs?.toString().orEmpty()
        ).joinToString("\t")
    }

    fun decode(serialized: String): List<SplCalibrationProfile> = serialized
        .lineSequence()
        .filter { it.isNotBlank() }
        .mapNotNull(::decodeLine)
        .toList()

    private fun decodeLine(line: String): SplCalibrationProfile? = runCatching {
        val fields = line.split('\t')
        require(fields.size == 19 && fields[0] == "1")
        val route = AudioInputRoute(
            deviceId = fields[3].toInt(),
            deviceType = fields[4].toInt(),
            productName = decoded(fields[5]),
            address = decoded(fields[6]),
            sampleRateHz = fields[7].toInt(),
            channelCount = fields[8].toInt(),
            encoding = fields[9].toInt(),
            audioSource = fields[10].toInt(),
            bufferSizeBytes = fields[11].toInt()
        )
        SplCalibrationProfile(
            id = decoded(fields[1]),
            routeKey = decoded(fields[2]),
            route = route,
            referenceSplDb = fields[12].toDouble(),
            capturedDbFs = fields[13].toDouble(),
            offsetDb = fields[14].toDouble(),
            method = decoded(fields[15]),
            createdAtEpochMs = fields[16].toLong(),
            valid = fields[17].toBooleanStrict(),
            invalidatedAtEpochMs = fields[18].takeIf { it.isNotBlank() }?.toLong()
        )
    }.getOrNull()

    private fun encoded(value: Any): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toString().toByteArray(Charsets.UTF_8))

    private fun decoded(value: String): String = if (value.isBlank()) {
        ""
    } else {
        String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
    }
}
