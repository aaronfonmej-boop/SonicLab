@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.soniclab3d.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.soniclab3d.academic.AcademicExperienceExporter
import com.soniclab3d.academic.AcademicExperienceRepository
import com.soniclab3d.academic.AcademicExperienceState
import com.soniclab3d.academic.AcademicLocks
import com.soniclab3d.academic.AcademicPractices
import com.soniclab3d.academic.AcademicRole
import com.soniclab3d.academic.AcademicSceneBaseline
import com.soniclab3d.academic.CymaticsAcademicLocks
import com.soniclab3d.academic.CymaticsAcademicPractices
import com.soniclab3d.academic.CymaticsSceneBaseline
import com.soniclab3d.academic.CymaticsTeacherActivity
import com.soniclab3d.academic.LearningAssignment
import com.soniclab3d.academic.LearningCatalog
import com.soniclab3d.academic.LearningDomain
import com.soniclab3d.academic.LearningExchangeCodec
import com.soniclab3d.academic.LearningPath
import com.soniclab3d.academic.LearningPresetResolver
import com.soniclab3d.academic.NotebookEntry
import com.soniclab3d.academic.PersonalDoubt
import com.soniclab3d.academic.PracticeDraft
import com.soniclab3d.academic.PracticePhase
import com.soniclab3d.academic.TeacherActivity
import com.soniclab3d.academic.TeacherFeedback
import com.soniclab3d.particles.ParticleQuality
import com.soniclab3d.audio.MicAnalysis
import com.soniclab3d.audio.MicAnalyzerEngine
import com.soniclab3d.audio.SynthAudioEngine
import com.soniclab3d.measurement.SplCalibrationCaptureController
import com.soniclab3d.measurement.SplCalibrationCaptureState
import com.soniclab3d.measurement.SplCalibrationProfile
import com.soniclab3d.measurement.SplCalibrationRepository
import com.soniclab3d.cymatics.CymaticsController
import com.soniclab3d.cymatics.CymaticsCameraPreset
import com.soniclab3d.cymatics.CymaticsExporter
import com.soniclab3d.cymatics.CymaticsMetrics
import com.soniclab3d.cymatics.CymaticsModeBank
import com.soniclab3d.cymatics.CymaticsRenderInfo
import com.soniclab3d.cymatics.CymaticsState
import com.soniclab3d.cymatics.SceneDomain
import com.soniclab3d.performance.PerformanceStats
import com.soniclab3d.performance.ThermalMonitor
import com.soniclab3d.physics.AcousticPhysics
import com.soniclab3d.physics.AcousticState
import com.soniclab3d.physics.ColorMode
import com.soniclab3d.physics.ReferenceParticle
import com.soniclab3d.physics.VisualVariable
import com.soniclab3d.physics.Waveform
import com.soniclab3d.renderer.ParticleGLSurfaceView
import com.soniclab3d.renderer.CymaticsGLSurfaceView
import com.soniclab3d.simulation.SimulationController
import com.soniclab3d.simulation.CameraPreset
import com.soniclab3d.scientific.ExperimentExporter
import com.soniclab3d.ui.theme.SonicPalette
import java.util.Locale
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.pow

private val frequencyPresets = listOf(20, 40, 60, 80, 100, 120, 150, 200, 432, 440, 528, 1000, 5000, 10000, 15000, 20000)
private val timePresets = listOf(
    "Real" to 1f,
    "Lenta ×2" to 0.5f,
    "×10" to 0.1f,
    "×100" to 0.01f,
    "×1000" to 0.001f
)

private fun readUtf8Limited(input: InputStream, maxBytes: Int): String {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8_192)
    var total = 0
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        total += read
        require(total <= maxBytes) { "Archivo mayor que ${maxBytes / 1_000} kB" }
        output.write(buffer, 0, read)
    }
    return output.toString(Charsets.UTF_8.name())
}

private enum class PanelTab(val label: String, val glyph: String) {
    CONTROL("Control", "⌁"),
    LAB("Prácticas", "◇"),
    SCIENCE("Ciencia", "λ"),
    REFERENCE("Partícula", "◎"),
    SOURCES("Fuentes", "≋"),
    FIELD("Campo", "▦"),
    ROOM("Recinto", "⌂"),
    ANALYZER("Analizador", "⌇"),
    TOOLS("Herramientas", "↗")
}

private enum class ExperienceMode(
    val label: String,
    val glyph: String,
    val description: String,
    val accent: Color,
    val tabs: List<PanelTab>
) {
    EXPLORE(
        "Explorar",
        "✦",
        "Controles esenciales para ver, tocar y comprender la onda.",
        SonicPalette.Cyan,
        listOf(PanelTab.CONTROL, PanelTab.FIELD, PanelTab.SOURCES)
    ),
    LEARN(
        "Aprender",
        "◇",
        "Prácticas guiadas, ecuaciones y lectura de una partícula.",
        SonicPalette.Violet,
        listOf(PanelTab.LAB, PanelTab.SCIENCE, PanelTab.REFERENCE, PanelTab.ROOM)
    ),
    LAB(
        "Laboratorio",
        "⌇",
        "Medición, calibración y herramientas reproducibles.",
        SonicPalette.Green,
        listOf(PanelTab.ANALYZER, PanelTab.TOOLS, PanelTab.FIELD, PanelTab.ROOM, PanelTab.SOURCES)
    );

    val startTab: PanelTab get() = tabs.first()
}

private data class CymaticsUiBindings(
    val controller: CymaticsController,
    val state: CymaticsState,
    val stats: PerformanceStats,
    val renderInfo: CymaticsRenderInfo,
    val update: ((CymaticsState) -> CymaticsState) -> Unit,
    val onStats: (PerformanceStats) -> Unit,
    val onInfo: (CymaticsRenderInfo) -> Unit,
    val onCamera: (CymaticsCameraPreset) -> Unit,
    val onResetSand: () -> Unit,
    val onResetScene: () -> Unit,
    val onPrepare: (String) -> Unit,
    val onPublish: (String, CymaticsAcademicLocks, String, String) -> Unit,
    val onCloseActivity: () -> Unit,
    val onExportExperiment: (CymaticsModeBank, CymaticsMetrics) -> Unit
)

@Composable
fun SonicLabApp() {
    val controller = remember { SimulationController() }
    val cymaticsController = remember { CymaticsController() }
    val audioEngine = remember { SynthAudioEngine() }
    val micEngine = remember { MicAnalyzerEngine() }
    val context = LocalContext.current
    val calibrationRepository = remember(context.applicationContext) {
        SplCalibrationRepository(context.applicationContext)
    }
    val calibrationProfiles by calibrationRepository.profiles.collectAsState(initial = emptyList())
    val academicRepository = remember(context.applicationContext) {
        AcademicExperienceRepository(context.applicationContext)
    }
    val academicExperience by academicRepository.state.collectAsState(
        initial = AcademicExperienceState()
    )
    val calibrationController = remember { SplCalibrationCaptureController() }
    val coroutineScope = rememberCoroutineScope()
    var acousticState by remember { mutableStateOf(controller.state) }
    var stats by remember { mutableStateOf(PerformanceStats()) }
    var cymaticsState by remember { mutableStateOf(cymaticsController.state) }
    var cymaticsStats by remember { mutableStateOf(PerformanceStats()) }
    var cymaticsInfo by remember { mutableStateOf(CymaticsRenderInfo()) }
    var reference by remember { mutableStateOf(ReferenceParticle()) }
    var micAnalysis by remember { mutableStateOf(MicAnalysis()) }
    var micEnabled by remember { mutableStateOf(false) }
    var calibrationCapture by remember { mutableStateOf(calibrationController.state) }
    var presentationRunning by remember { mutableStateOf(false) }
    var pendingCsv by remember { mutableStateOf("") }
    var pendingJson by remember { mutableStateOf("") }
    var pendingAcademicJson by remember { mutableStateOf("") }
    var pendingCymaticsJson by remember { mutableStateOf("") }
    var pendingLearningExchangeJson by remember { mutableStateOf("") }
    var sceneDomainName by rememberSaveable { mutableStateOf(SceneDomain.AIR.name) }
    var focusMode by rememberSaveable { mutableStateOf(false) }
    var learningAirSnapshot by remember { mutableStateOf<AcousticState?>(null) }
    var learningPlateSnapshot by remember { mutableStateOf<CymaticsState?>(null) }
    var learningSnapshotDomainName by remember { mutableStateOf<String?>(null) }
    val sceneDomain = SceneDomain.entries.firstOrNull { it.name == sceneDomainName } ?: SceneDomain.AIR

    fun update(transform: (AcousticState) -> AcousticState) {
        controller.update { current ->
            academicExperience.teacherActivity.enforce(transform(current))
        }
        acousticState = controller.state
    }

    fun updateAcademicScene(transform: (AcousticState) -> AcousticState) {
        controller.update(transform)
        acousticState = controller.state
    }

    fun updateCymatics(transform: (CymaticsState) -> CymaticsState) {
        cymaticsController.update { current ->
            academicExperience.cymaticsTeacherActivity.enforce(transform(current))
        }
        cymaticsState = cymaticsController.state
    }

    fun updateCymaticsAcademicScene(transform: (CymaticsState) -> CymaticsState) {
        cymaticsController.update(transform)
        cymaticsState = cymaticsController.state
    }

    fun receiveMicAnalysis(nextAnalysis: MicAnalysis) {
        micAnalysis = nextAnalysis
        if (!calibrationController.state.active) return
        val update = calibrationController.addSample(
            relativeDbFs = nextAnalysis.relativeDbFs,
            route = nextAnalysis.route
        )
        calibrationCapture = update.state
        update.completedProfile?.let { profile ->
            coroutineScope.launch {
                val result = runCatching { calibrationRepository.save(profile) }
                if (result.isSuccess) {
                    Toast.makeText(context, "Perfil SPL guardado para esta entrada", Toast.LENGTH_SHORT).show()
                } else {
                    calibrationCapture = calibrationCapture.copy(
                        message = "No se guardó la calibración",
                        error = "DataStore no pudo persistir el perfil"
                    )
                }
            }
        }
    }

    fun startMicrophoneEngine(): Boolean = micEngine.start(
        calibrationResolver = calibrationRepository::applicableProfile,
        onAnalysis = ::receiveMicAnalysis
    )

    LaunchedEffect(
        acousticState.audioEnabled,
        acousticState.audioVolume,
        acousticState.frequencyHz,
        acousticState.harmonicMode,
        acousticState.harmonicAmplitudes
    ) {
        audioEngine.update(acousticState)
    }
    DisposableEffect(audioEngine, micEngine) {
        onDispose {
            audioEngine.stop()
            micEngine.stop()
        }
    }

    val microphonePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            micEnabled = startMicrophoneEngine()
        } else {
            micEnabled = false
            micAnalysis = MicAnalysis(error = "Permiso de micrófono denegado")
        }
    }
    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val saved = runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(pendingCsv) }
                    ?: error("No se pudo abrir el archivo")
            }.isSuccess
            Toast.makeText(context, if (saved) "Experimento CSV guardado" else "No se pudo guardar el CSV", Toast.LENGTH_SHORT).show()
        }
    }
    val jsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val saved = runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(pendingJson) }
                    ?: error("No se pudo abrir el archivo")
            }.isSuccess
            Toast.makeText(context, if (saved) "Experimento JSON guardado" else "No se pudo guardar el JSON", Toast.LENGTH_SHORT).show()
        }
    }
    val academicJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val saved = runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                    it.write(pendingAcademicJson)
                } ?: error("No se pudo abrir el archivo")
            }.isSuccess
            Toast.makeText(
                context,
                if (saved) "Progreso académico guardado" else "No se pudo guardar el progreso",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    val cymaticsJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val saved = runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                    it.write(pendingCymaticsJson)
                } ?: error("No se pudo abrir el archivo")
            }.isSuccess
            Toast.makeText(
                context,
                if (saved) "Experimento de cimática guardado" else "No se pudo guardar el experimento",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    val learningExchangeExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val saved = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                            it.write(pendingLearningExchangeJson)
                        } ?: error("No se pudo abrir el archivo")
                    }
                }.isSuccess
                Toast.makeText(context, if (saved) "Archivo académico guardado" else "No se pudo guardar el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val learningExchangeImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        val json = context.contentResolver.openInputStream(uri)?.use {
                            readUtf8Limited(it, LearningExchangeCodec.MAX_DOCUMENT_BYTES)
                        } ?: error("No se pudo abrir el archivo")
                        when (LearningExchangeCodec.documentType(json)) {
                            "soniclabpack" -> {
                                academicRepository.publishLearningAssignment(LearningExchangeCodec.parseStudentPack(json))
                                "Paquete importado y asignación activada"
                            }
                            "soniclabsubmission" -> {
                                academicRepository.importSubmission(LearningExchangeCodec.parseSubmission(json))
                                "Entrega importada para revisión"
                            }
                            "soniclabfeedback" -> {
                                academicRepository.saveFeedback(LearningExchangeCodec.parseFeedback(json))
                                "Retroalimentación importada"
                            }
                            else -> error("Tipo de archivo SonicLab no compatible")
                        }
                    }
                }
                Toast.makeText(
                    context,
                    result.getOrElse { "Importación rechazada: ${it.message ?: "archivo inválido"}" },
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun changeMicrophone(enabled: Boolean) {
        if (!enabled) {
            micEngine.stop()
            calibrationController.cancel()
            calibrationCapture = calibrationController.state
            micEnabled = false
            micAnalysis = micAnalysis.copy(running = false)
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            micEnabled = startMicrophoneEngine()
        } else {
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun startSplCalibration(referenceSplDb: Double, method: String) {
        val route = micAnalysis.route
        if (!micEnabled || !micAnalysis.running || route == null) {
            calibrationCapture = SplCalibrationCaptureState(
                message = "Calibración no iniciada",
                error = "Activa el micrófono y espera a que Android identifique la ruta"
            )
            return
        }
        calibrationCapture = calibrationController.start(route, referenceSplDb, method)
    }

    fun cancelSplCalibration() {
        calibrationController.cancel()
        calibrationCapture = calibrationController.state
    }

    fun invalidateSplCalibration(profileId: String) {
        coroutineScope.launch {
            runCatching { calibrationRepository.invalidate(profileId) }
                .onSuccess {
                    if (micAnalysis.calibrationProfileId == profileId) {
                        micAnalysis = micAnalysis.copy(calibratedSplDb = null, calibrationProfileId = null)
                    }
                    Toast.makeText(context, "Perfil marcado como no válido", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(context, "No se pudo invalidar el perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun deleteSplCalibration(profileId: String) {
        coroutineScope.launch {
            runCatching { calibrationRepository.delete(profileId) }
                .onSuccess {
                    if (micAnalysis.calibrationProfileId == profileId) {
                        micAnalysis = micAnalysis.copy(calibratedSplDb = null, calibrationProfileId = null)
                    }
                    Toast.makeText(context, "Perfil de calibración borrado", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(context, "No se pudo borrar el perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun exportCsv() {
        pendingCsv = ExperimentExporter.csv(acousticState, stats.simulationTimeS)
        csvLauncher.launch("SonicLab-${acousticState.frequencyHz.toInt()}Hz.csv")
    }

    fun exportJson() {
        pendingJson = ExperimentExporter.json(acousticState, stats.simulationTimeS, acousticState.probes)
        jsonLauncher.launch("SonicLab-experimento.json")
    }

    fun setAcademicRole(role: AcademicRole) {
        coroutineScope.launch {
            runCatching { academicRepository.setRole(role) }
                .onFailure {
                    Toast.makeText(context, "No se pudo guardar el perfil académico", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun selectLearningPath(path: LearningPath) {
        coroutineScope.launch {
            runCatching { academicRepository.selectLearningPath(path) }
                .onFailure { Toast.makeText(context, "No se pudo guardar la ruta", Toast.LENGTH_SHORT).show() }
        }
    }

    fun setGuideCompleted(guideId: String, completed: Boolean) {
        coroutineScope.launch {
            runCatching { academicRepository.setGuideCompleted(guideId, completed) }
                .onFailure { Toast.makeText(context, "No se pudo actualizar la guía", Toast.LENGTH_SHORT).show() }
        }
    }

    fun toggleGuideFavorite(guideId: String) {
        coroutineScope.launch {
            runCatching { academicRepository.toggleGuideFavorite(guideId) }
                .onFailure { Toast.makeText(context, "No se pudo guardar el favorito", Toast.LENGTH_SHORT).show() }
        }
    }

    fun openLearningPreset(contentOrPresetId: String) {
        val presetId = LearningPresetResolver.presetForContent(contentOrPresetId) ?: contentOrPresetId
        val knownContent = LearningCatalog.guide(contentOrPresetId) != null || LearningCatalog.practice(contentOrPresetId) != null
        val domain = if (knownContent) LearningPresetResolver.domainForContent(contentOrPresetId) else when {
            presetId.startsWith("plate_") || presetId.startsWith("cymatics_") -> LearningDomain.PLATE
            else -> LearningDomain.AIR
        }
        val usePlate = domain == LearningDomain.PLATE ||
            (domain == LearningDomain.MIXED && (presetId.startsWith("plate_") || presetId.startsWith("cymatics_")))
        if (learningAirSnapshot == null && learningPlateSnapshot == null) {
            learningAirSnapshot = acousticState
            learningPlateSnapshot = cymaticsState
            learningSnapshotDomainName = sceneDomainName
        }
        if (usePlate) {
            sceneDomainName = SceneDomain.PLATE.name
            updateCymaticsAcademicScene { LearningPresetResolver.applyPlate(it, presetId) }
            cymaticsController.requestCameraPreset(CymaticsCameraPreset.FREE)
            cymaticsController.resetSand()
        } else {
            sceneDomainName = SceneDomain.AIR.name
            updateAcademicScene { LearningPresetResolver.applyAir(it, presetId) }
            controller.requestCameraPreset(CameraPreset.FREE)
        }
        Toast.makeText(context, "Demostración preparada · puedes volver a Aprender sin perder el paso", Toast.LENGTH_SHORT).show()
    }

    fun restoreLearningScene() {
        val air = learningAirSnapshot
        val plate = learningPlateSnapshot
        if (air != null) updateAcademicScene { air }
        if (plate != null) updateCymaticsAcademicScene { plate }
        sceneDomainName = learningSnapshotDomainName ?: sceneDomainName
        learningAirSnapshot = null
        learningPlateSnapshot = null
        learningSnapshotDomainName = null
        Toast.makeText(context, "Escena anterior restaurada", Toast.LENGTH_SHORT).show()
    }

    fun setLearningPracticePhase(practiceId: String, phase: PracticePhase) {
        coroutineScope.launch {
            runCatching { academicRepository.setPracticePhase(practiceId, phase) }
                .onFailure { Toast.makeText(context, "No se pudo guardar el paso", Toast.LENGTH_SHORT).show() }
        }
    }

    fun saveLearningPracticeDraft(draft: PracticeDraft) {
        coroutineScope.launch {
            runCatching { academicRepository.savePracticeDraft(draft) }
                .onFailure { Toast.makeText(context, "No se pudo guardar el borrador", Toast.LENGTH_SHORT).show() }
        }
    }

    fun resetLearningPractice(practiceId: String) {
        coroutineScope.launch {
            runCatching { academicRepository.resetLearningPractice(practiceId) }
                .onSuccess { Toast.makeText(context, "Práctica reiniciada", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "No se pudo reiniciar la práctica", Toast.LENGTH_SHORT).show() }
        }
    }

    fun savePersonalDoubt(doubt: PersonalDoubt) {
        coroutineScope.launch {
            runCatching { academicRepository.savePersonalDoubt(doubt) }
                .onSuccess { Toast.makeText(context, "Duda guardada localmente", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "Escribe una pregunta antes de guardar", Toast.LENGTH_SHORT).show() }
        }
    }

    fun answerPersonalDoubt(doubtId: String, answer: String) {
        coroutineScope.launch {
            runCatching { academicRepository.answerPersonalDoubt(doubtId, answer) }
                .onSuccess { Toast.makeText(context, "Respuesta docente guardada", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "No se pudo responder la duda", Toast.LENGTH_SHORT).show() }
        }
    }

    fun archivePersonalDoubt(doubtId: String) {
        coroutineScope.launch {
            runCatching { academicRepository.archivePersonalDoubt(doubtId) }
                .onFailure { Toast.makeText(context, "No se pudo archivar la duda", Toast.LENGTH_SHORT).show() }
        }
    }

    fun saveNotebookEntry(entry: NotebookEntry) {
        coroutineScope.launch {
            runCatching { academicRepository.saveNotebookEntry(entry) }
                .onSuccess { Toast.makeText(context, "Entrada añadida al cuaderno", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "La entrada necesita título y evidencia", Toast.LENGTH_SHORT).show() }
        }
    }

    fun publishLearningAssignment(assignment: LearningAssignment) {
        coroutineScope.launch {
            runCatching { academicRepository.publishLearningAssignment(assignment) }
                .onSuccess { Toast.makeText(context, "Asignación publicada en este dispositivo", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "Revisa curso, práctica y rúbrica", Toast.LENGTH_SHORT).show() }
        }
    }

    fun closeLearningAssignment(assignmentId: String) {
        coroutineScope.launch {
            runCatching { academicRepository.closeLearningAssignment(assignmentId) }
                .onSuccess { Toast.makeText(context, "Asignación cerrada", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "No se pudo cerrar la asignación", Toast.LENGTH_SHORT).show() }
        }
    }

    fun importLearningExchange() {
        learningExchangeImportLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
    }

    fun exportLearningAssignment(assignmentId: String) {
        val assignment = academicExperience.learning.assignments.firstOrNull { it.id == assignmentId }
        if (assignment == null) {
            Toast.makeText(context, "No existe la asignación", Toast.LENGTH_SHORT).show()
            return
        }
        pendingLearningExchangeJson = LearningExchangeCodec.studentPack(assignment)
        learningExchangeExportLauncher.launch("${assignment.id}.soniclabpack.json")
    }

    fun exportLearningSubmission() {
        val assignment = academicExperience.learning.activeAssignment
        if (assignment == null) {
            Toast.makeText(context, "Importa o activa una asignación primero", Toast.LENGTH_SHORT).show()
            return
        }
        pendingLearningExchangeJson = LearningExchangeCodec.submission(academicExperience, assignment)
        learningExchangeExportLauncher.launch("${assignment.id}.soniclabsubmission.json")
    }

    fun exportTeacherFeedback(feedback: TeacherFeedback) {
        coroutineScope.launch {
            runCatching { academicRepository.saveFeedback(feedback) }
                .onFailure { Toast.makeText(context, "No se pudo guardar el feedback", Toast.LENGTH_SHORT).show() }
        }
        pendingLearningExchangeJson = LearningExchangeCodec.feedback(feedback)
        learningExchangeExportLauncher.launch("${feedback.id}.soniclabfeedback.json")
    }

    fun prepareAcademicPractice(practiceId: String) {
        val prepared = AcademicPractices.prepare(controller.state, practiceId)
        val activity = academicExperience.teacherActivity
        val next = if (activity.active && activity.practiceId == practiceId) {
            activity.baseline.applyTo(prepared)
        } else {
            prepared
        }
        updateAcademicScene { next }
        controller.requestCameraPreset(CameraPreset.FREE)
    }

    fun submitAcademicPractice(practiceId: String, answer: String) {
        coroutineScope.launch {
            runCatching { academicRepository.submit(practiceId, answer) }
                .onSuccess {
                    Toast.makeText(context, "Entrega guardada en el dispositivo", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(context, "Escribe una respuesta antes de entregar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun publishAcademicActivity(
        practiceId: String,
        locks: AcademicLocks,
        expectedAnswer: String,
        rubric: String
    ) {
        val prepared = AcademicPractices.prepare(controller.state, practiceId)
        updateAcademicScene { prepared }
        val activity = TeacherActivity(
            active = true,
            practiceId = practiceId,
            locks = locks,
            expectedAnswer = expectedAnswer,
            rubric = rubric,
            baseline = AcademicSceneBaseline.from(prepared)
        )
        coroutineScope.launch {
            runCatching { academicRepository.publish(activity) }
                .onSuccess {
                    Toast.makeText(context, "Actividad publicada localmente", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(context, "Revisa la respuesta esperada y la rúbrica", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun closeAcademicActivity() {
        coroutineScope.launch {
            runCatching { academicRepository.closeActivity() }
                .onSuccess {
                    Toast.makeText(context, "Actividad cerrada; controles liberados", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(context, "No se pudo cerrar la actividad", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun prepareCymaticsPractice(practiceId: String) {
        val prepared = CymaticsAcademicPractices.prepare(cymaticsController.state, practiceId)
        val activity = academicExperience.cymaticsTeacherActivity
        val next = if (activity.active && activity.practiceId == practiceId) {
            activity.baseline.state
        } else {
            prepared
        }
        updateCymaticsAcademicScene { next }
        cymaticsController.requestCameraPreset(CymaticsCameraPreset.FREE)
        cymaticsController.resetSand()
    }

    fun publishCymaticsActivity(
        practiceId: String,
        locks: CymaticsAcademicLocks,
        expectedAnswer: String,
        rubric: String
    ) {
        val prepared = CymaticsAcademicPractices.prepare(cymaticsController.state, practiceId)
        updateCymaticsAcademicScene { prepared }
        val activity = CymaticsTeacherActivity(
            active = true,
            practiceId = practiceId,
            locks = locks,
            expectedAnswer = expectedAnswer,
            rubric = rubric,
            baseline = CymaticsSceneBaseline(prepared)
        )
        coroutineScope.launch {
            runCatching { academicRepository.publishCymatics(activity) }
                .onSuccess {
                    Toast.makeText(context, "Actividad de cimática publicada localmente", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(context, "Revisa la respuesta esperada y la rúbrica", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun closeCymaticsActivity() {
        coroutineScope.launch {
            runCatching { academicRepository.closeCymaticsActivity() }
                .onSuccess {
                    Toast.makeText(context, "Actividad de cimática cerrada", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(context, "No se pudo cerrar la actividad", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun resetAcademicProgress() {
        coroutineScope.launch {
            runCatching { academicRepository.resetStudentProgress() }
                .onSuccess {
                    Toast.makeText(context, "Progreso local borrado", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(context, "No se pudo borrar el progreso", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun exportAcademicProgress() {
        pendingAcademicJson = AcademicExperienceExporter.json(
            academicExperience,
            acousticState,
            stats.simulationTimeS,
            cymaticsState,
            cymaticsInfo
        )
        academicJsonLauncher.launch("SonicLab-progreso-academico.json")
    }

    fun exportCymaticsExperiment(bank: CymaticsModeBank, metrics: CymaticsMetrics) {
        pendingCymaticsJson = CymaticsExporter.json(cymaticsState, metrics, bank, cymaticsStats)
        cymaticsJsonLauncher.launch("SonicLab-cimatica-${cymaticsState.geometry.name.lowercase()}.json")
    }

    LaunchedEffect(presentationRunning) {
        if (!presentationRunning) return@LaunchedEffect
        update {
            it.copy(
                frequencyHz = 120.0,
                paused = false,
                visualTimeScale = 0.01f,
                showWavefronts = true,
                showPressureSlice = false,
                harmonicMode = false,
                secondarySource = it.secondarySource.copy(enabled = false)
            )
        }
        controller.requestCameraPreset(CameraPreset.FREE)
        delay(3_200)
        update { it.copy(showPressureSlice = true, sliceAxis = com.soniclab3d.physics.SliceAxis.XY, sliceOffsetM = 0f) }
        controller.requestCameraPreset(CameraPreset.TOP)
        delay(3_200)
        update {
            it.copy(
                showPressureSlice = true,
                showWavefronts = false,
                secondarySource = it.secondarySource.copy(
                    enabled = true,
                    frequencyHz = 120.0,
                    levelDbSpl = it.levelDbSpl,
                    phaseRadians = PI.toFloat(),
                    xM = 2f
                )
            )
        }
        delay(3_200)
        update {
            it.copy(
                showPressureSlice = false,
                harmonicMode = true,
                harmonicAmplitudes = listOf(1f, 0.50f, 0.30f, 0.18f, 0.10f, 0.06f) + List(10) { 0f },
                secondarySource = it.secondarySource.copy(enabled = false)
            )
        }
        controller.requestCameraPreset(CameraPreset.FRONT)
        delay(3_200)
        presentationRunning = false
    }

    val cymaticsUi = CymaticsUiBindings(
        controller = cymaticsController,
        state = cymaticsState,
        stats = cymaticsStats,
        renderInfo = cymaticsInfo,
        update = ::updateCymatics,
        onStats = { cymaticsStats = it },
        onInfo = {
            cymaticsInfo = it
            cymaticsState = cymaticsController.state
        },
        onCamera = cymaticsController::requestCameraPreset,
        onResetSand = cymaticsController::resetSand,
        onResetScene = {
            updateCymatics { CymaticsState() }
            cymaticsController.requestCameraPreset(CymaticsCameraPreset.FREE)
            cymaticsController.resetSand()
        },
        onPrepare = ::prepareCymaticsPractice,
        onPublish = ::publishCymaticsActivity,
        onCloseActivity = ::closeCymaticsActivity,
        onExportExperiment = ::exportCymaticsExperiment
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0B1C2D), SonicPalette.DeepSpace, SonicPalette.Void),
                    radius = 1500f
                )
            )
    ) {
        val landscape = maxWidth > maxHeight
        val safePadding = WindowInsets.safeDrawing.asPaddingValues()
        if (focusMode) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(safePadding)
                    .padding(6.dp)
            ) {
                UnifiedScenePanel(
                    domain = sceneDomain,
                    airController = controller,
                    airState = acousticState,
                    airStats = stats,
                    onAirStats = { stats = it },
                    onReference = { reference = it },
                    onAirPauseToggle = { update { it.copy(paused = !it.paused) } },
                    onAirResetView = { controller.requestCameraPreset(CameraPreset.FREE) },
                    cymatics = cymaticsUi,
                    focusMode = true,
                    onFocusToggle = { focusMode = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else if (landscape) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(safePadding)
                    .padding(7.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnifiedScenePanel(
                    domain = sceneDomain,
                    airController = controller,
                    airState = acousticState,
                    airStats = stats,
                    onAirStats = { stats = it },
                    onReference = { reference = it },
                    onAirPauseToggle = { update { it.copy(paused = !it.paused) } },
                    onAirResetView = { controller.requestCameraPreset(CameraPreset.FREE) },
                    cymatics = cymaticsUi,
                    focusMode = false,
                    onFocusToggle = { focusMode = true },
                    modifier = Modifier.weight(0.69f)
                )
                ControlsPanel(
                    sceneDomain = sceneDomain,
                    onSceneDomainChange = { sceneDomainName = it.name },
                    cymatics = cymaticsUi,
                    state = acousticState,
                    stats = stats,
                    reference = reference,
                    update = ::update,
                    controller = controller,
                    micAnalysis = micAnalysis,
                    micEnabled = micEnabled,
                    onMicEnabledChange = ::changeMicrophone,
                    calibrationProfiles = calibrationProfiles,
                    calibrationCapture = calibrationCapture,
                    onStartCalibration = ::startSplCalibration,
                    onCancelCalibration = ::cancelSplCalibration,
                    onInvalidateCalibration = ::invalidateSplCalibration,
                    onDeleteCalibration = ::deleteSplCalibration,
                    presentationRunning = presentationRunning,
                    onPresentation = { presentationRunning = !presentationRunning },
                    onExportCsv = ::exportCsv,
                    onExportJson = ::exportJson,
                    academicExperience = academicExperience,
                    onAcademicRoleChange = ::setAcademicRole,
                    onPrepareAcademic = ::prepareAcademicPractice,
                    onSubmitAcademic = ::submitAcademicPractice,
                    onResetAcademicProgress = ::resetAcademicProgress,
                    onPublishAcademic = ::publishAcademicActivity,
                    onCloseAcademicActivity = ::closeAcademicActivity,
                    onExportAcademic = ::exportAcademicProgress,
                    onSelectLearningPath = ::selectLearningPath,
                    onGuideCompleted = ::setGuideCompleted,
                    onToggleGuideFavorite = ::toggleGuideFavorite,
                    onOpenLearningPreset = ::openLearningPreset,
                    onLearningPracticePhase = ::setLearningPracticePhase,
                    onSaveLearningPracticeDraft = ::saveLearningPracticeDraft,
                    onResetLearningPractice = ::resetLearningPractice,
                    onSavePersonalDoubt = ::savePersonalDoubt,
                    onAnswerPersonalDoubt = ::answerPersonalDoubt,
                    onArchivePersonalDoubt = ::archivePersonalDoubt,
                    onSaveNotebookEntry = ::saveNotebookEntry,
                    onPublishLearningAssignment = ::publishLearningAssignment,
                    onCloseLearningAssignment = ::closeLearningAssignment,
                    onImportLearningExchange = ::importLearningExchange,
                    onExportLearningAssignment = ::exportLearningAssignment,
                    onExportLearningSubmission = ::exportLearningSubmission,
                    onExportTeacherFeedback = ::exportTeacherFeedback,
                    hasLearningSceneSnapshot = learningAirSnapshot != null || learningPlateSnapshot != null,
                    onRestoreLearningScene = ::restoreLearningScene,
                    modifier = Modifier.weight(0.31f)
                )
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(safePadding)
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                UnifiedScenePanel(
                    domain = sceneDomain,
                    airController = controller,
                    airState = acousticState,
                    airStats = stats,
                    onAirStats = { stats = it },
                    onReference = { reference = it },
                    onAirPauseToggle = { update { it.copy(paused = !it.paused) } },
                    onAirResetView = { controller.requestCameraPreset(CameraPreset.FREE) },
                    cymatics = cymaticsUi,
                    focusMode = false,
                    onFocusToggle = { focusMode = true },
                    modifier = Modifier.weight(0.58f)
                )
                ControlsPanel(
                    sceneDomain = sceneDomain,
                    onSceneDomainChange = { sceneDomainName = it.name },
                    cymatics = cymaticsUi,
                    state = acousticState,
                    stats = stats,
                    reference = reference,
                    update = ::update,
                    controller = controller,
                    micAnalysis = micAnalysis,
                    micEnabled = micEnabled,
                    onMicEnabledChange = ::changeMicrophone,
                    calibrationProfiles = calibrationProfiles,
                    calibrationCapture = calibrationCapture,
                    onStartCalibration = ::startSplCalibration,
                    onCancelCalibration = ::cancelSplCalibration,
                    onInvalidateCalibration = ::invalidateSplCalibration,
                    onDeleteCalibration = ::deleteSplCalibration,
                    presentationRunning = presentationRunning,
                    onPresentation = { presentationRunning = !presentationRunning },
                    onExportCsv = ::exportCsv,
                    onExportJson = ::exportJson,
                    academicExperience = academicExperience,
                    onAcademicRoleChange = ::setAcademicRole,
                    onPrepareAcademic = ::prepareAcademicPractice,
                    onSubmitAcademic = ::submitAcademicPractice,
                    onResetAcademicProgress = ::resetAcademicProgress,
                    onPublishAcademic = ::publishAcademicActivity,
                    onCloseAcademicActivity = ::closeAcademicActivity,
                    onExportAcademic = ::exportAcademicProgress,
                    onSelectLearningPath = ::selectLearningPath,
                    onGuideCompleted = ::setGuideCompleted,
                    onToggleGuideFavorite = ::toggleGuideFavorite,
                    onOpenLearningPreset = ::openLearningPreset,
                    onLearningPracticePhase = ::setLearningPracticePhase,
                    onSaveLearningPracticeDraft = ::saveLearningPracticeDraft,
                    onResetLearningPractice = ::resetLearningPractice,
                    onSavePersonalDoubt = ::savePersonalDoubt,
                    onAnswerPersonalDoubt = ::answerPersonalDoubt,
                    onArchivePersonalDoubt = ::archivePersonalDoubt,
                    onSaveNotebookEntry = ::saveNotebookEntry,
                    onPublishLearningAssignment = ::publishLearningAssignment,
                    onCloseLearningAssignment = ::closeLearningAssignment,
                    onImportLearningExchange = ::importLearningExchange,
                    onExportLearningAssignment = ::exportLearningAssignment,
                    onExportLearningSubmission = ::exportLearningSubmission,
                    onExportTeacherFeedback = ::exportTeacherFeedback,
                    hasLearningSceneSnapshot = learningAirSnapshot != null || learningPlateSnapshot != null,
                    onRestoreLearningScene = ::restoreLearningScene,
                    modifier = Modifier.weight(0.42f)
                )
            }
        }
    }
}

@Composable
private fun UnifiedScenePanel(
    domain: SceneDomain,
    airController: SimulationController,
    airState: AcousticState,
    airStats: PerformanceStats,
    onAirStats: (PerformanceStats) -> Unit,
    onReference: (ReferenceParticle) -> Unit,
    onAirPauseToggle: () -> Unit,
    onAirResetView: () -> Unit,
    cymatics: CymaticsUiBindings,
    focusMode: Boolean,
    onFocusToggle: () -> Unit,
    modifier: Modifier
) {
    if (domain == SceneDomain.AIR) {
        ScenePanel(
            controller = airController,
            state = airState,
            stats = airStats,
            onStats = onAirStats,
            onReference = onReference,
            onPauseToggle = onAirPauseToggle,
            onResetView = onAirResetView,
            focusMode = focusMode,
            onFocusToggle = onFocusToggle,
            modifier = modifier
        )
    } else {
        CymaticsScenePanel(
            bindings = cymatics,
            focusMode = focusMode,
            onFocusToggle = onFocusToggle,
            modifier = modifier
        )
    }
}

@Composable
private fun CymaticsScenePanel(
    bindings: CymaticsUiBindings,
    focusMode: Boolean,
    onFocusToggle: () -> Unit,
    modifier: Modifier
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var surfaceView by remember { mutableStateOf<CymaticsGLSurfaceView?>(null) }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> surfaceView?.onResume()
                Lifecycle.Event.ON_PAUSE -> surfaceView?.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            surfaceView?.releaseRenderer()
        }
    }

    val sceneShape = RoundedCornerShape(24.dp)
    Box(
        modifier
            .fillMaxHeight()
            .shadow(18.dp, sceneShape)
            .clip(sceneShape)
            .border(1.dp, SonicPalette.Magenta.copy(alpha = 0.25f), sceneShape)
            .background(SonicPalette.Void)
    ) {
        AndroidView(
            factory = { context ->
                CymaticsGLSurfaceView(
                    context,
                    bindings.controller,
                    bindings.onStats,
                    bindings.onInfo
                ).also { surfaceView = it }
            },
            modifier = Modifier.fillMaxSize()
        )
        CymaticsHud(bindings.state, bindings.renderInfo, bindings.stats, Modifier.align(Alignment.TopStart))
        StatusPill(
            text = when {
                bindings.renderInfo.modeTransitionProgress < 0.995f ->
                    "MORPH ${(bindings.renderInfo.modeTransitionProgress * 100).toInt()} %"
                bindings.state.reduceMotion -> "MOVIMIENTO ↓"
                bindings.state.visualStyle == com.soniclab3d.cymatics.CymaticsVisualStyle.SPECTACULAR ->
                    "${bindings.state.motionPreset.label.uppercase()} ✦"
                else -> "CIMÁTICA λ"
            },
            accent = SonicPalette.Magenta,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
        )
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SceneAction(
                if (bindings.state.paused) "▶  Continuar" else "Ⅱ  Pausar",
                SonicPalette.Green
            ) { bindings.update { it.copy(paused = !it.paused) } }
            SceneAction("◎  Centrar", SonicPalette.Cyan) {
                bindings.onCamera(CymaticsCameraPreset.FREE)
            }
            SceneAction(
                if (focusMode) "↙  Controles" else "↗  Enfoque",
                SonicPalette.Violet,
                onFocusToggle
            )
        }
    }
}

@Composable
private fun CymaticsHud(
    state: CymaticsState,
    info: CymaticsRenderInfo,
    stats: PerformanceStats,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xEE161026), Color(0xDA07101C))))
            .border(1.dp, SonicPalette.Magenta.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(7.dp).height(7.dp).background(SonicPalette.Magenta, RoundedCornerShape(50)))
                Spacer(Modifier.width(7.dp))
                Text("CYMATICS LAB", color = SonicPalette.Magenta, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("SCIENCE 1.1", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Muted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HudValue("PLACA", state.geometry.label.uppercase())
                HudValue("MODO", if (info.bankReady) "${info.activeModeIndex + 1}" else "…")
                HudValue("f PROPIA", if (info.bankReady) formatHz(info.naturalFrequencyHz) else "…")
                HudValue("FPS", format(stats.fps.toDouble(), 0))
            }
            Text(
                "${info.motionProfile.uppercase()} · ${format(info.visualCyclesPerSecond.toDouble(), 2)} ciclos/s · " +
                    "ARENA ${info.sandParticleCount / 1000}k · ${if (info.postProcessingActive) "BLOOM ON" else "DIRECTO"} · " +
                    "${info.rendererPath}",
                color = SonicPalette.Muted,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ScenePanel(
    controller: SimulationController,
    state: AcousticState,
    stats: PerformanceStats,
    onStats: (PerformanceStats) -> Unit,
    onReference: (ReferenceParticle) -> Unit,
    onPauseToggle: () -> Unit,
    onResetView: () -> Unit,
    focusMode: Boolean,
    onFocusToggle: () -> Unit,
    modifier: Modifier
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var surfaceView by remember { mutableStateOf<ParticleGLSurfaceView?>(null) }

    DisposableEffect(lifecycle, surfaceView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> surfaceView?.onResume()
                Lifecycle.Event.ON_PAUSE -> surfaceView?.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            surfaceView?.onPause()
        }
    }

    val sceneShape = RoundedCornerShape(24.dp)
    Box(
        modifier
            .fillMaxHeight()
            .shadow(18.dp, sceneShape)
            .clip(sceneShape)
            .border(1.dp, SonicPalette.Cyan.copy(alpha = 0.22f), sceneShape)
            .background(SonicPalette.Void)
    ) {
        AndroidView(
            factory = { context ->
                ParticleGLSurfaceView(context, controller, onStats, onReference).also { surfaceView = it }
            },
            modifier = Modifier.fillMaxSize()
        )
        ScientificHud(state, stats, Modifier.align(Alignment.TopStart))
        StatusPill(
            text = visualVariableLabel(state.visualVariable).uppercase(),
            accent = when (state.visualVariable) {
                VisualVariable.PRESSURE -> SonicPalette.Magenta
                VisualVariable.DISPLACEMENT -> SonicPalette.Violet
                VisualVariable.VELOCITY -> SonicPalette.Green
                VisualVariable.PHASE -> SonicPalette.Cyan
            },
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SceneAction(if (state.paused) "▶  Continuar" else "Ⅱ  Pausar", SonicPalette.Green, onPauseToggle)
            SceneAction("◎  Centrar", SonicPalette.Cyan, onResetView)
            SceneAction(if (focusMode) "↙  Controles" else "↗  Enfoque", SonicPalette.Violet, onFocusToggle)
        }
    }
}

@Composable
private fun SceneAction(label: String, accent: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xE607111E),
        contentColor = accent,
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ScientificHud(state: AcousticState, stats: PerformanceStats, modifier: Modifier = Modifier) {
    val metrics = AcousticPhysics.metrics(state)
    Box(
        modifier = modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xEE091725), Color(0xD907101C))
                )
            )
            .border(1.dp, SonicPalette.Cyan.copy(alpha = 0.20f), RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(7.dp)
                        .height(7.dp)
                        .background(SonicPalette.Cyan, RoundedCornerShape(50))
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    "SONICLAB 3D",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text("SCIENCE 1.1", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Muted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HudValue("FRECUENCIA", formatHz(state.frequencyHz))
                HudValue("LONGITUD", "${format(metrics.wavelengthM, 3)} m")
                HudValue("NIVEL", "${state.levelDbSpl.toInt()} dB")
                HudValue("FPS", format(stats.fps.toDouble(), 0))
            }
            Text(
                "GPU ${stats.particleCount / 1000}k/${state.quality.particleCount / 1000}k  ·  ${stats.frameTimeMs.toInt()} ms  ·  TÉRMICO ${ThermalMonitor.label(stats.thermalStatus).uppercase()}",
                color = SonicPalette.Muted,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun HudValue(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.035f), RoundedCornerShape(9.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = SonicPalette.Muted)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SonicPalette.Ice)
    }
}

@Composable
private fun ControlsPanel(
    sceneDomain: SceneDomain,
    onSceneDomainChange: (SceneDomain) -> Unit,
    cymatics: CymaticsUiBindings,
    state: AcousticState,
    stats: PerformanceStats,
    reference: ReferenceParticle,
    update: ((AcousticState) -> AcousticState) -> Unit,
    controller: SimulationController,
    micAnalysis: MicAnalysis,
    micEnabled: Boolean,
    onMicEnabledChange: (Boolean) -> Unit,
    calibrationProfiles: List<SplCalibrationProfile>,
    calibrationCapture: SplCalibrationCaptureState,
    onStartCalibration: (Double, String) -> Unit,
    onCancelCalibration: () -> Unit,
    onInvalidateCalibration: (String) -> Unit,
    onDeleteCalibration: (String) -> Unit,
    presentationRunning: Boolean,
    onPresentation: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    academicExperience: AcademicExperienceState,
    onAcademicRoleChange: (AcademicRole) -> Unit,
    onPrepareAcademic: (String) -> Unit,
    onSubmitAcademic: (String, String) -> Unit,
    onResetAcademicProgress: () -> Unit,
    onPublishAcademic: (String, AcademicLocks, String, String) -> Unit,
    onCloseAcademicActivity: () -> Unit,
    onExportAcademic: () -> Unit,
    onSelectLearningPath: (LearningPath) -> Unit,
    onGuideCompleted: (String, Boolean) -> Unit,
    onToggleGuideFavorite: (String) -> Unit,
    onOpenLearningPreset: (String) -> Unit,
    onLearningPracticePhase: (String, PracticePhase) -> Unit,
    onSaveLearningPracticeDraft: (PracticeDraft) -> Unit,
    onResetLearningPractice: (String) -> Unit,
    onSavePersonalDoubt: (PersonalDoubt) -> Unit,
    onAnswerPersonalDoubt: (String, String) -> Unit,
    onArchivePersonalDoubt: (String) -> Unit,
    onSaveNotebookEntry: (NotebookEntry) -> Unit,
    onPublishLearningAssignment: (LearningAssignment) -> Unit,
    onCloseLearningAssignment: (String) -> Unit,
    onImportLearningExchange: () -> Unit,
    onExportLearningAssignment: (String) -> Unit,
    onExportLearningSubmission: () -> Unit,
    onExportTeacherFeedback: (TeacherFeedback) -> Unit,
    hasLearningSceneSnapshot: Boolean,
    onRestoreLearningScene: () -> Unit,
    modifier: Modifier = Modifier
) {
    var modeName by rememberSaveable { mutableStateOf(ExperienceMode.EXPLORE.name) }
    var tabName by rememberSaveable { mutableStateOf(PanelTab.CONTROL.name) }
    val mode = ExperienceMode.entries.firstOrNull { it.name == modeName } ?: ExperienceMode.EXPLORE
    val tab = PanelTab.entries.firstOrNull { it.name == tabName }?.takeIf { it in mode.tabs } ?: mode.startTab
    val haptic = LocalHapticFeedback.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B1724), Color(0xFF06101A))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(38.dp)
                        .height(38.dp)
                        .background(
                            Brush.linearGradient(listOf(SonicPalette.Cyan, SonicPalette.Violet)),
                            RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("S", color = SonicPalette.Void, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (sceneDomain == SceneDomain.AIR) "Laboratorio acústico" else "Laboratorio de cimática", style = MaterialTheme.typography.titleLarge)
                    Text("SCIENCE 1.1 · ${academicExperience.role.label.uppercase()}", style = MaterialTheme.typography.labelSmall, color = mode.accent)
                }
                val activePaused = if (sceneDomain == SceneDomain.AIR) state.paused else cymatics.state.paused
                StatusPill(if (activePaused) "PAUSA" else "LIVE", if (activePaused) SonicPalette.Amber else SonicPalette.Green)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (sceneDomain == SceneDomain.AIR) {
                    "Visualización educativa · no sustituye un sonómetro calibrado"
                } else {
                    "Simulación modal educativa · amplitud a.u. y arena cualitativa"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = SonicPalette.Muted
            )
        }
        item {
            DomainSelector(sceneDomain) { selected ->
                onSceneDomainChange(selected)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
        item {
            ExperienceModeSelector(mode) { selected ->
                modeName = selected.name
                if (tab !in selected.tabs) tabName = selected.startTab.name
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
        if (sceneDomain == SceneDomain.AIR && mode != ExperienceMode.LEARN) {
            item {
                PanelTabBar(tab, mode.tabs) { selected ->
                    tabName = selected.name
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
        item {
            if (mode == ExperienceMode.LEARN) {
                ContinuousLearningPanel(
                    experience = academicExperience,
                    onRoleChange = onAcademicRoleChange,
                    onSelectPath = onSelectLearningPath,
                    onGuideCompleted = onGuideCompleted,
                    onToggleFavorite = onToggleGuideFavorite,
                    onOpenPreset = onOpenLearningPreset,
                    onPracticePhase = onLearningPracticePhase,
                    onSavePracticeDraft = onSaveLearningPracticeDraft,
                    onResetPractice = onResetLearningPractice,
                    onSubmitPractice = onSubmitAcademic,
                    onSaveDoubt = onSavePersonalDoubt,
                    onAnswerDoubt = onAnswerPersonalDoubt,
                    onArchiveDoubt = onArchivePersonalDoubt,
                    onSaveNotebook = onSaveNotebookEntry,
                    onPublishAssignment = onPublishLearningAssignment,
                    onCloseAssignment = onCloseLearningAssignment,
                    onImportExchange = onImportLearningExchange,
                    onExportAssignment = onExportLearningAssignment,
                    onExportSubmission = onExportLearningSubmission,
                    onExportFeedback = onExportTeacherFeedback,
                    hasSceneSnapshot = hasLearningSceneSnapshot,
                    onRestoreScene = onRestoreLearningScene,
                    onExport = onExportAcademic
                )
            } else if (sceneDomain == SceneDomain.PLATE) {
                CymaticsPanel(
                    workspace = when (mode) {
                        ExperienceMode.EXPLORE -> CymaticsWorkspace.EXPLORE
                        ExperienceMode.LEARN -> CymaticsWorkspace.LEARN
                        ExperienceMode.LAB -> CymaticsWorkspace.LAB
                    },
                    state = cymatics.state,
                    renderInfo = cymatics.renderInfo,
                    stats = cymatics.stats,
                    experience = academicExperience,
                    update = cymatics.update,
                    onCamera = cymatics.onCamera,
                    onResetSand = cymatics.onResetSand,
                    onResetScene = cymatics.onResetScene,
                    onRoleChange = onAcademicRoleChange,
                    onPrepare = cymatics.onPrepare,
                    onSubmit = onSubmitAcademic,
                    onPublish = cymatics.onPublish,
                    onCloseActivity = cymatics.onCloseActivity,
                    onResetProgress = onResetAcademicProgress,
                    onExportAcademic = onExportAcademic,
                    onExportExperiment = cymatics.onExportExperiment
                )
            } else when (tab) {
                PanelTab.CONTROL -> ControlTab(state, update, controller)
                PanelTab.LAB -> AcademicPanel(
                    sceneState = state,
                    experience = academicExperience,
                    onRoleChange = onAcademicRoleChange,
                    onPrepare = onPrepareAcademic,
                    onSubmit = onSubmitAcademic,
                    onResetProgress = onResetAcademicProgress,
                    onPublish = onPublishAcademic,
                    onCloseActivity = onCloseAcademicActivity,
                    onExport = onExportAcademic
                )
                PanelTab.SCIENCE -> ScienceTab(state, stats, reference)
                PanelTab.REFERENCE -> ReferenceTab(state, reference)
                PanelTab.SOURCES -> SourcesTab(state, update)
                PanelTab.FIELD -> FieldPanel(state, stats.simulationTimeS, update, controller::requestSimulationTime)
                PanelTab.ROOM -> RoomPanel(state, update)
                PanelTab.ANALYZER -> AnalyzerPanel(
                    analysis = micAnalysis,
                    enabled = micEnabled,
                    onEnabledChange = onMicEnabledChange,
                    profiles = calibrationProfiles,
                    captureState = calibrationCapture,
                    onStartCalibration = onStartCalibration,
                    onCancelCalibration = onCancelCalibration,
                    onInvalidateCalibration = onInvalidateCalibration,
                    onDeleteCalibration = onDeleteCalibration
                )
                PanelTab.TOOLS -> ToolsPanel(
                    presentationRunning,
                    onPresentation,
                    onExportCsv,
                    onExportJson
                )
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun DomainSelector(selected: SceneDomain, onSelected: (SceneDomain) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.025f), RoundedCornerShape(17.dp))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(17.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        SceneDomain.entries.forEach { domain ->
            val active = domain == selected
            val accent = if (domain == SceneDomain.AIR) SonicPalette.Cyan else SonicPalette.Magenta
            Surface(
                onClick = { onSelected(domain) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                color = if (active) accent.copy(alpha = 0.15f) else Color.Transparent,
                contentColor = if (active) accent else SonicPalette.Muted,
                shape = RoundedCornerShape(13.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (active) accent.copy(alpha = 0.46f) else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (domain == SceneDomain.AIR) "≋" else "✦", fontSize = 15.sp)
                    Spacer(Modifier.width(7.dp))
                    Text(domain.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ExperienceModeSelector(selected: ExperienceMode, onSelected: (ExperienceMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.025f), RoundedCornerShape(17.dp))
                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(17.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExperienceMode.entries.forEach { mode ->
                val active = selected == mode
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (active) mode.accent.copy(alpha = 0.14f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (active) mode.accent.copy(alpha = 0.42f) else Color.Transparent,
                            RoundedCornerShape(13.dp)
                        )
                        .clickable { onSelected(mode) }
                        .padding(horizontal = 4.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(mode.glyph, color = if (active) mode.accent else SonicPalette.Muted, fontSize = 15.sp)
                    Text(
                        mode.label,
                        color = if (active) mode.accent else SonicPalette.Muted,
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
        }
        Surface(
            color = selected.accent.copy(alpha = 0.065f),
            shape = RoundedCornerShape(13.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, selected.accent.copy(alpha = 0.18f))
        ) {
            Text(
                selected.description,
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                color = SonicPalette.Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PanelTabBar(selected: PanelTab, tabs: List<PanelTab>, onSelected: (PanelTab) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        items(tabs) { tab ->
            val active = selected == tab
            Surface(
                onClick = { onSelected(tab) },
                color = if (active) SonicPalette.Cyan.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.025f),
                contentColor = if (active) SonicPalette.Cyan else SonicPalette.Muted,
                shape = RoundedCornerShape(13.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (active) SonicPalette.Cyan.copy(alpha = 0.48f) else Color.White.copy(alpha = 0.07f)
                )
            ) {
                Row(
                    modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 11.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(tab.glyph, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(tab.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.11f),
        contentColor = accent,
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(Modifier.width(5.dp).height(5.dp).background(accent, RoundedCornerShape(50)))
            Text(text, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp)
        }
    }
}

@Composable
private fun LabTab(
    state: AcousticState,
    update: ((AcousticState) -> AcousticState) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Laboratorio guiado")
        Text(
            "Cada práctica configura un caso reproducible. Observa la escena, abre Ciencia para revisar las ecuaciones y toca una partícula para comprobar su movimiento local.",
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.64f)
        )
        LabCard(
            title = "1 · La onda avanza; el aire oscila",
            objective = "Distinguir propagación de la perturbación y transporte local de partículas.",
            steps = "Configura 120 Hz y 20 °C. Usa ×100, toca una partícula y compara equilibrio con posición instantánea.",
            question = "¿Una partícula seleccionada debería alejarse continuamente de la fuente?",
            answer = "No. Oscila alrededor de su equilibrio; lo que avanza es la fase de la perturbación.",
            actionLabel = "Preparar 120 Hz"
        ) {
            update {
                it.copy(
                    frequencyHz = 120.0,
                    temperatureC = 20.0,
                    visualTimeScale = 0.01f,
                    paused = false,
                    harmonicMode = false,
                    secondarySource = it.secondarySource.copy(enabled = false),
                    waveform = Waveform.SINE
                )
            }
        }
        LabCard(
            title = "2 · Frecuencia y longitud de onda",
            objective = "Comprobar que λ disminuye cuando f aumenta si la velocidad se mantiene.",
            steps = "Observa λ a 120 Hz. Cambia a 1.000 Hz sin modificar la temperatura y compara el resultado en Ciencia.",
            question = "¿Qué ocurre con λ si multiplicamos la frecuencia y mantenemos c?",
            answer = "Disminuye en la misma proporción porque λ = c/f.",
            actionLabel = "Comparar a 1 kHz"
        ) {
            update {
                it.copy(
                    frequencyHz = 1_000.0,
                    temperatureC = 20.0,
                    harmonicMode = false,
                    secondarySource = it.secondarySource.copy(enabled = false),
                    waveform = Waveform.SINE
                )
            }
        }
        LabCard(
            title = "3 · Interferencia",
            objective = "Identificar zonas donde dos presiones se refuerzan o se cancelan.",
            steps = "Activa dos fuentes de 120 Hz con igual nivel. Cambia la fase B entre 0 y π y observa cómo se desplazan los nodos.",
            question = "¿Qué debe ocurrir en un punto donde llegan presiones iguales y opuestas?",
            answer = "La suma instantánea se aproxima a cero: interferencia destructiva.",
            actionLabel = "Preparar dos fuentes"
        ) {
            update {
                it.copy(
                    frequencyHz = 120.0,
                    levelDbSpl = 70f,
                    harmonicMode = false,
                    waveform = Waveform.SINE,
                    secondarySource = it.secondarySource.copy(
                        enabled = true,
                        frequencyHz = 120.0,
                        levelDbSpl = 70f,
                        phaseRadians = 0f,
                        xM = 2f,
                        yM = 0f,
                        zM = 0f
                    )
                )
            }
        }
        LabCard(
            title = "4 · Una voz no es una sola frecuencia",
            objective = "Relacionar una fundamental con sus armónicos.",
            steps = "Usa 120 Hz como fundamental. Modifica las amplitudes 2f, 3f y 4f y observa el espectro sintetizado.",
            question = "¿Dos señales con la misma fundamental necesariamente tienen el mismo timbre?",
            answer = "No. Sus armónicos y envolventes pueden ser diferentes aunque compartan la fundamental.",
            actionLabel = "Preparar voz 120 Hz"
        ) {
            update {
                it.copy(
                    frequencyHz = 120.0,
                    harmonicMode = true,
                    harmonicAmplitudes = listOf(1f, 0.45f, 0.25f, 0.15f) + List(12) { 0f },
                    secondarySource = it.secondarySource.copy(enabled = false),
                    waveform = Waveform.SINE
                )
            }
        }
        Text(
            "Caso actual: ${formatHz(state.frequencyHz)} · ${state.levelDbSpl.toInt()} dB SPL · ${if (state.secondarySource.enabled) "dos fuentes" else "una fuente"}.",
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun LabCard(
    title: String,
    objective: String,
    steps: String,
    question: String,
    answer: String,
    actionLabel: String,
    onPrepare: () -> Unit
) {
    var revealAnswer by remember(title) { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1A29)),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SonicPalette.Cyan.copy(alpha = 0.14f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(28.dp)
                        .background(
                            Brush.verticalGradient(listOf(SonicPalette.Cyan, SonicPalette.Violet)),
                            RoundedCornerShape(50)
                        )
                )
                Spacer(Modifier.width(9.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = SonicPalette.Cyan)
            }
            Text("OBJETIVO", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Violet)
            Text(objective, style = MaterialTheme.typography.bodyMedium, color = SonicPalette.Ice)
            Text(steps, style = MaterialTheme.typography.bodyMedium, fontSize = 10.sp, color = SonicPalette.Muted)
            Surface(color = SonicPalette.Violet.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("PREGUNTA", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Violet)
                    Text(question, fontSize = 10.sp)
                    if (revealAnswer) Text(answer, fontSize = 10.sp, color = SonicPalette.Green)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Button(
                    onClick = onPrepare,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SonicPalette.Cyan,
                        contentColor = SonicPalette.Void
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(actionLabel, style = MaterialTheme.typography.labelLarge, fontSize = 10.sp)
                }
                Button(
                    onClick = { revealAnswer = !revealAnswer },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SonicPalette.PanelBright,
                        contentColor = SonicPalette.Violet
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (revealAnswer) "Ocultar" else "Respuesta", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun ControlTab(
    state: AcousticState,
    update: ((AcousticState) -> AcousticState) -> Unit,
    controller: SimulationController
) {
    val haptic = LocalHapticFeedback.current
    var frequencyText by remember(state.frequencyHz) { mutableStateOf(state.frequencyHz.toInt().toString()) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Frecuencia física")
        Surface(
            color = Color(0xFF0C1B2A),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SonicPalette.Cyan.copy(alpha = 0.20f))
        ) {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text("OSCILADOR PRINCIPAL", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Muted)
                        Text(formatHz(state.frequencyHz), style = MaterialTheme.typography.displaySmall, color = SonicPalette.Cyan)
                    }
                    OutlinedTextField(
                        value = frequencyText,
                        onValueChange = { text ->
                            frequencyText = text.filter { it.isDigit() }.take(5)
                            frequencyText.toDoubleOrNull()?.coerceIn(20.0, 20_000.0)?.let { hz ->
                                update { it.copy(frequencyHz = hz) }
                            }
                        },
                        suffix = { Text("Hz") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(104.dp)
                    )
                }
                Slider(
                    value = frequencyToSlider(state.frequencyHz),
                    onValueChange = { value ->
                        val hz = sliderToFrequency(value)
                        update { it.copy(frequencyHz = hz) }
                        frequencyText = hz.toInt().toString()
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = SonicPalette.Cyan,
                        activeTrackColor = SonicPalette.Cyan,
                        inactiveTrackColor = SonicPalette.Grid
                    )
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(frequencyPresets) { hz ->
                        AssistChip(
                            onClick = {
                                update { it.copy(frequencyHz = hz.toDouble()) }
                                frequencyText = hz.toString()
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            label = { Text(if (hz >= 1000) "${hz / 1000}k" else "$hz") }
                        )
                    }
                }
            }
        }

        ParameterSlider("Nivel a 1 m", state.levelDbSpl, 20f..110f, "${state.levelDbSpl.toInt()} dB SPL") {
            update { current -> current.copy(levelDbSpl = it) }
        }
        ParameterSlider("Temperatura", state.temperatureC.toFloat(), -10f..45f, "${format(state.temperatureC, 1)} °C") {
            update { current -> current.copy(temperatureC = it.toDouble()) }
        }
        ParameterSlider("Humedad relativa", state.humidityPercent, 10f..100f, "${state.humidityPercent.toInt()} %") {
            update { current -> current.copy(humidityPercent = it) }
        }
        ParameterSlider("Presión atmosférica", state.atmosphericPressureKPa, 80f..105f, "${format(state.atmosphericPressureKPa.toDouble(), 1)} kPa") {
            update { current -> current.copy(atmosphericPressureKPa = it) }
        }
        ParameterSlider("Fase", state.phaseRadians, 0f..(2f * PI.toFloat()), "${format(state.phaseRadians.toDouble(), 2)} rad") {
            update { current -> current.copy(phaseRadians = it) }
        }
        ParameterSlider(
            "Exageración visual",
            kotlin.math.log10(state.displacementVisualScale),
            3f..7f,
            "×${compactScale(state.displacementVisualScale)}"
        ) { logarithm ->
            update { current -> current.copy(displacementVisualScale = 10f.pow(logarithm)) }
        }
        ScienceNotice(
            "ESCALA FÍSICA ≠ ESCALA VISUAL",
            "El desplazamiento se amplifica únicamente para hacerlo observable.",
            SonicPalette.Magenta
        )

        SectionTitle("Tiempo visual")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(timePresets) { preset ->
                FilterChip(
                    selected = state.visualTimeScale == preset.second && !state.paused,
                    onClick = {
                        update { it.copy(visualTimeScale = preset.second, paused = false) }
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    label = { Text(preset.first) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    update { it.copy(paused = !it.paused) }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.paused) SonicPalette.Green else SonicPalette.Cyan,
                    contentColor = SonicPalette.Void
                ),
                modifier = Modifier.weight(1f)
            ) { Text(if (state.paused) "▶  Reanudar" else "Ⅱ  Pausa") }
            Button(
                onClick = {
                    update { it.copy(paused = true) }
                    controller.requestFrameStep()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SonicPalette.PanelBright,
                    contentColor = SonicPalette.Cyan
                ),
                modifier = Modifier.weight(1f)
            ) { Text("+1 frame") }
        }

        SectionTitle("Calidad GPU")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(ParticleQuality.entries) { quality ->
                FilterChip(
                    selected = state.quality == quality,
                    onClick = {
                        update { it.copy(quality = quality) }
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    label = { Text("${quality.label} ${quality.particleCount / 1000}k") }
                )
            }
        }
        ToggleRow("Calidad adaptativa ~60 FPS", state.autoQuality) { enabled ->
            update { it.copy(autoQuality = enabled) }
        }
        ParameterSlider("Tamaño de partícula", state.pointSizePx, 1f..8f, "${format(state.pointSizePx.toDouble(), 1)} px") {
            update { current -> current.copy(pointSizePx = it) }
        }

        SectionTitle("Audio opcional")
        ToggleRow("Reproducir tono", state.audioEnabled) { enabled ->
            update { it.copy(audioEnabled = enabled) }
        }
        ParameterSlider("Volumen digital", state.audioVolume, 0.005f..0.08f, "${(state.audioVolume * 100).toInt()} %") { value ->
            update { it.copy(audioVolume = value) }
        }
        ScienceNotice(
            if (state.frequencyHz >= 10_000.0) "FRECUENCIA AGUDA" else "AUDIO SEGURO POR DEFECTO",
            if (state.frequencyHz >= 10_000.0) {
                "La reproducción es opcional. Utiliza un volumen bajo y detén el tono si resulta molesto."
            } else {
                "Comienza desactivado y con amplitud baja. La simulación funciona sin reproducir sonido."
            },
            if (state.frequencyHz >= 10_000.0) SonicPalette.Magenta else SonicPalette.Green
        )

        SectionTitle("Cámara")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(CameraPreset.entries) { preset ->
                AssistChip(
                    onClick = { controller.requestCameraPreset(preset) },
                    label = { Text(cameraPresetLabel(preset)) }
                )
            }
        }

        SectionTitle("Variable visualizada")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(VisualVariable.entries) { variable ->
                FilterChip(
                    selected = state.visualVariable == variable,
                    onClick = { update { it.copy(visualVariable = variable) } },
                    label = { Text(visualVariableLabel(variable)) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterChip(
                selected = state.colorMode == ColorMode.SCIENTIFIC,
                onClick = { update { it.copy(colorMode = ColorMode.SCIENTIFIC) } },
                label = { Text("Científico") }
            )
            FilterChip(
                selected = state.colorMode == ColorMode.ARTISTIC,
                onClick = { update { it.copy(colorMode = ColorMode.ARTISTIC) } },
                label = { Text("Artístico") }
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(Waveform.entries) { waveform ->
                FilterChip(
                    selected = state.waveform == waveform,
                    onClick = { update { it.copy(waveform = waveform) } },
                    label = { Text(waveformLabel(waveform)) }
                )
            }
        }
    }
}

@Composable
private fun ScienceTab(state: AcousticState, stats: PerformanceStats, reference: ReferenceParticle) {
    val metrics = AcousticPhysics.metrics(state)
    val time = stats.simulationTimeS
    val x = reference.equilibriumX
    val y = reference.equilibriumY
    val z = reference.equilibriumZ
    val period = AcousticPhysics.periodSeconds(state.frequencyHz)
    val pressureTime = remember(state, time, x, y, z) {
        sampleRange(161) { fraction ->
            val sampleTime = time + (fraction - 0.5) * 2.0 * period
            AcousticPhysics.sampleAt(state, x, y, z, sampleTime).pressurePa
        }
    }
    val displacementTime = remember(state, time, x, y, z) {
        sampleRange(161) { fraction ->
            val sampleTime = time + (fraction - 0.5) * 2.0 * period
            AcousticPhysics.sampleAt(state, x, y, z, sampleTime).displacementMagnitudeM
        }
    }
    val pressureDistance = remember(state, time) {
        sampleRange(161) { fraction ->
            val distance = state.sourceRadiusM + fraction * (state.maxDistanceM - state.sourceRadiusM)
            AcousticPhysics.sampleAt(state, distance, 0.0, 0.0, time).pressurePa
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Modelo y ecuaciones")
        EquationCard(state, metrics)
        ScientificTable(
            listOf(
                "Velocidad del sonido c" to "${format(metrics.soundSpeedMps, 3)} m/s",
                "Densidad del aire ρ" to "${format(metrics.airDensityKgM3, 4)} kg/m³",
                "Longitud de onda λ" to "${format(metrics.wavelengthM, 5)} m",
                "Período T" to "${format(metrics.periodMs, 5)} ms",
                "Frecuencia angular ω" to "${format(metrics.angularFrequencyRadS, 2)} rad/s",
                "Número de onda k" to "${format(metrics.waveNumberRadM, 5)} rad/m",
                "Presión RMS a 1 m" to "${scientific(metrics.pressureRmsPa)} Pa",
                "Velocidad de partícula pico" to "${scientific(metrics.particleVelocityPeakMps)} m/s",
                "Desplazamiento físico pico" to "${scientific(metrics.displacementPeakM)} m",
                "Intensidad a 1 m" to "${scientific(metrics.intensityWm2)} W/m²",
                "Absorción atmosférica" to "${scientific(metrics.absorptionDbPerM)} dB/m"
            )
        )
        ScientificGraph("Presión frente al tiempo · 2 períodos", pressureTime, "Pa", 0.5f)
        ScientificGraph("Desplazamiento físico frente al tiempo", displacementTime, "m", 0.5f, lineColor = Color(0xFF9B8CFF))
        ScientificGraph("Presión frente a distancia", pressureDistance, "Pa", null, lineColor = Color(0xFFFF3C6F))
        SpectrumGraph(
            state.frequencyHz,
            if (state.harmonicMode) state.harmonicAmplitudes else List(16) { index -> if (index == 0) 1f else 0f }
        )
        ScienceNotice(
            "ALCANCE DEL MODELO",
            "La escena 3D usa campo libre analítico con fuente esférica regularizada. El panel Recinto añade modos rectangulares ideales; no simula todavía difracción ni materiales arbitrarios.",
            SonicPalette.Violet
        )
    }
}

@Composable
private fun ReferenceTab(state: AcousticState, reference: ReferenceParticle) {
    val sample = reference.sample
    val period = AcousticPhysics.periodSeconds(state.frequencyHz)
    val pressureSeries = remember(state, reference) {
        sampleRange(161) { fraction ->
            val time = reference.simulationTimeS + (fraction - 0.5) * 2.0 * period
            AcousticPhysics.sampleAt(
                state,
                reference.equilibriumX,
                reference.equilibriumY,
                reference.equilibriumZ,
                time
            ).pressurePa
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Partícula de referencia")
        Text(
            if (reference.isSelected) "Partícula GPU #${reference.particleIndex} seleccionada" else "Toca una partícula en la escena; mientras tanto se usa una sonda en x=2 m.",
            color = if (reference.isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.62f),
            fontSize = 11.sp
        )
        ScientificTable(
            listOf(
                "Equilibrio" to vector(reference.equilibriumX, reference.equilibriumY, reference.equilibriumZ, "m"),
                "Posición física instantánea" to vector(reference.instantaneousX, reference.instantaneousY, reference.instantaneousZ, "m"),
                "Desplazamiento" to "${scientific(sample.displacementMagnitudeM)} m",
                "Velocidad de partícula" to "${scientific(sample.particleVelocityMagnitudeMps)} m/s",
                "Presión local" to "${scientific(sample.pressurePa)} Pa",
                "Fase" to "${format(sample.phaseRadians, 4)} rad",
                "Tiempo simulado" to "${format(reference.simulationTimeS, 4)} s"
            )
        )
        ParticleMicroscope(state, reference)
        ScientificGraph("Presión temporal de la partícula", pressureSeries, "Pa", 0.5f)
        ScienceNotice(
            "PARTÍCULA FÍSICA",
            "La posición usa el desplazamiento real. La escena lo multiplica por ×${compactScale(state.displacementVisualScale)} para hacerlo visible.",
            SonicPalette.Cyan
        )
    }
}

@Composable
private fun SourcesTab(
    state: AcousticState,
    update: ((AcousticState) -> AcousticState) -> Unit
) {
    val sourceB = state.secondarySource
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Fuente A · origen")
        Text("${formatHz(state.frequencyHz)} · ${state.levelDbSpl.toInt()} dB SPL a 1 m · fase ${format(state.phaseRadians.toDouble(), 2)} rad", fontSize = 11.sp)
        ToggleRow("Modo voz / armónicos", state.harmonicMode) { enabled ->
            update { it.copy(harmonicMode = enabled, waveform = Waveform.SINE) }
        }
        if (state.harmonicMode) {
            state.harmonicAmplitudes.take(16).forEachIndexed { index, amplitude ->
                ParameterSlider(
                    "Armónico ${index + 1} · ${(state.frequencyHz * (index + 1)).toInt()} Hz",
                    amplitude,
                    0f..1f,
                    "${(amplitude * 100).toInt()} %"
                ) { value ->
                    update { current ->
                        val amplitudes = current.harmonicAmplitudes.toMutableList()
                        while (amplitudes.size < 16) amplitudes += 0f
                        amplitudes[index] = value
                        current.copy(harmonicAmplitudes = amplitudes)
                    }
                }
            }
            SpectrumGraph(state.frequencyHz, state.harmonicAmplitudes)
            Text("Las frecuencias 432 y 528 Hz se tratan solamente como valores seleccionables; no se atribuyen efectos curativos.", fontSize = 9.sp, color = Color.White.copy(alpha = 0.52f))
        }

        SectionTitle("Fuente B · interferencia")
        ToggleRow("Activar segunda fuente", sourceB.enabled) { enabled ->
            update { it.copy(secondarySource = it.secondarySource.copy(enabled = enabled)) }
        }
        if (sourceB.enabled) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Button(
                    onClick = {
                        update {
                            it.copy(secondarySource = it.secondarySource.copy(frequencyHz = it.frequencyHz))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.PanelBright, contentColor = SonicPalette.Cyan)
                ) { Text("Sincronizar f", fontSize = 10.sp) }
                Button(
                    onClick = {
                        update {
                            it.copy(secondarySource = it.secondarySource.copy(
                                frequencyHz = it.frequencyHz,
                                phaseRadians = ((it.phaseRadians + PI.toFloat()) % (2f * PI.toFloat()))
                            ))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.PanelBright, contentColor = SonicPalette.Violet)
                ) { Text("Invertir fase π", fontSize = 10.sp) }
            }
            ParameterSlider("Frecuencia B", sourceB.frequencyHz.toFloat(), 20f..2_000f, "${formatHz(sourceB.frequencyHz)}") { value ->
                update { it.copy(secondarySource = it.secondarySource.copy(frequencyHz = value.toDouble())) }
            }
            ParameterSlider("Nivel B", sourceB.levelDbSpl, 20f..110f, "${sourceB.levelDbSpl.toInt()} dB SPL") { value ->
                update { it.copy(secondarySource = it.secondarySource.copy(levelDbSpl = value)) }
            }
            ParameterSlider("Fase B", sourceB.phaseRadians, 0f..(2f * PI.toFloat()), "${format(sourceB.phaseRadians.toDouble(), 2)} rad") { value ->
                update { it.copy(secondarySource = it.secondarySource.copy(phaseRadians = value)) }
            }
            ParameterSlider("Posición X", sourceB.xM, -4f..4f, "${format(sourceB.xM.toDouble(), 2)} m") { value ->
                update { it.copy(secondarySource = it.secondarySource.copy(xM = value)) }
            }
            ParameterSlider("Posición Y", sourceB.yM, -4f..4f, "${format(sourceB.yM.toDouble(), 2)} m") { value ->
                update { it.copy(secondarySource = it.secondarySource.copy(yM = value)) }
            }
            ParameterSlider("Posición Z", sourceB.zM, -4f..4f, "${format(sourceB.zM.toDouble(), 2)} m") { value ->
                update { it.copy(secondarySource = it.secondarySource.copy(zM = value)) }
            }
            ToggleRow("Mostrar mapa de nodos", state.showPressureSlice) { enabled ->
                update { it.copy(showPressureSlice = enabled) }
            }
            ScienceNotice(
                "SUPERPOSICIÓN LINEAL",
                "Con igual frecuencia aparecen refuerzo y cancelación. Con frecuencias cercanas pueden observarse batidos.",
                SonicPalette.Green
            )
        }
    }
}

@Composable
private fun EquationCard(state: AcousticState, metrics: com.soniclab3d.physics.AcousticMetrics) {
    Surface(
        color = Color(0xFF07131F),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SonicPalette.Violet.copy(alpha = 0.26f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("MODELO DE CAMPO LIBRE", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Violet)
            Text(
                "p(r,t) = p̂ · R/rₑ · e⁻ᵅʳ · sin(kr − ωt + φ)",
                color = SonicPalette.Cyan,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
            EquationRow("λ = c/f", "${format(metrics.soundSpeedMps, 2)}/${format(state.frequencyHz, 2)} = ${format(metrics.wavelengthM, 5)} m")
            EquationRow("T = 1/f", "${format(metrics.periodMs, 5)} ms")
            EquationRow("ω = 2πf", "${format(metrics.angularFrequencyRadS, 2)} rad/s")
            EquationRow("k = 2π/λ", "${format(metrics.waveNumberRadM, 5)} rad/m")
            Text("REFERENCIA SPL · p₀ = 20 µPa · nivel definido a 1 m", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Muted)
        }
    }
}

@Composable
private fun EquationRow(equation: String, result: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            equation,
            modifier = Modifier.weight(0.34f),
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 10.sp,
            color = SonicPalette.Violet
        )
        Text(result, modifier = Modifier.weight(0.66f), fontSize = 10.sp, color = SonicPalette.Ice)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        color = if (checked) SonicPalette.Cyan.copy(alpha = 0.075f) else Color.White.copy(alpha = 0.025f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked) SonicPalette.Cyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f)
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = SonicPalette.Ice)
                Text(
                    if (checked) "ACTIVO" else "INACTIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (checked) SonicPalette.Green else SonicPalette.Muted
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    shownValue: String = format(value.toDouble(), 2),
    onChange: (Float) -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.025f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.055f))
    ) {
        Column(Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = SonicPalette.Ice)
                Surface(
                    color = SonicPalette.Cyan.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        shownValue,
                        color = SonicPalette.Cyan,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Slider(
                value = value.coerceIn(range.start, range.endInclusive),
                onValueChange = onChange,
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = SonicPalette.Cyan,
                    activeTrackColor = SonicPalette.Cyan,
                    inactiveTrackColor = SonicPalette.Grid
                )
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(3.dp)
                .height(14.dp)
                .background(SonicPalette.Cyan, RoundedCornerShape(50))
        )
        Spacer(Modifier.width(7.dp))
        Text(text.uppercase(), color = SonicPalette.Cyan, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(Modifier.weight(1f), color = SonicPalette.Grid.copy(alpha = 0.75f))
    }
}

@Composable
private fun ScientificTable(rows: List<Pair<String, String>>) {
    Surface(
        color = Color(0xFF091725),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
            rows.forEachIndexed { index, row ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(row.first, modifier = Modifier.weight(1f), fontSize = 10.sp, color = SonicPalette.Muted)
                    Text(
                        row.second,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SonicPalette.Ice,
                        textAlign = TextAlign.End
                    )
                }
                if (index != rows.lastIndex) HorizontalDivider(color = Color.White.copy(alpha = 0.045f))
            }
        }
    }
}

@Composable
private fun ScienceNotice(title: String, message: String, accent: Color) {
    Surface(
        color = accent.copy(alpha = 0.08f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.25f))
    ) {
        Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(28.dp)
                    .height(28.dp)
                    .background(accent.copy(alpha = 0.16f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("!", color = accent, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(9.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Bold)
                Text(message, style = MaterialTheme.typography.bodyMedium, fontSize = 10.sp, color = SonicPalette.Muted)
            }
        }
    }
}

private fun sampleRange(count: Int, sample: (Double) -> Double): List<Double> =
    List(count) { index -> sample(index.toDouble() / (count - 1).coerceAtLeast(1)) }

private fun frequencyToSlider(frequency: Double): Float =
    (ln(frequency.coerceIn(20.0, 20_000.0) / 20.0) / ln(1000.0)).toFloat()

private fun sliderToFrequency(value: Float): Double =
    (20.0 * 1000.0.pow(value.toDouble())).coerceIn(20.0, 20_000.0)

private fun format(value: Double, decimals: Int): String =
    String.format(Locale.US, "%.${decimals}f", value)

private fun scientific(value: Double): String = String.format(Locale.US, "%.3e", value)

private fun formatHz(value: Double): String =
    if (value >= 1000.0) "${format(value / 1000.0, 2)} kHz" else "${format(value, 0)} Hz"

private fun compactScale(value: Float): String = when {
    value >= 1_000_000f -> "${format((value / 1_000_000f).toDouble(), 1)}M"
    value >= 1_000f -> "${format((value / 1_000f).toDouble(), 0)}k"
    else -> value.toInt().toString()
}

private fun vector(x: Double, y: Double, z: Double, unit: String): String =
    "(${format(x, 4)}, ${format(y, 4)}, ${format(z, 4)}) $unit"

private fun waveformLabel(waveform: Waveform): String = when (waveform) {
    Waveform.SINE -> "Seno"
    Waveform.TRIANGLE -> "Triangular"
    Waveform.SQUARE -> "Cuadrada"
    Waveform.PULSE -> "Pulso"
}

private fun visualVariableLabel(variable: VisualVariable): String = when (variable) {
    VisualVariable.PRESSURE -> "Presión"
    VisualVariable.DISPLACEMENT -> "Desplazamiento"
    VisualVariable.VELOCITY -> "Velocidad"
    VisualVariable.PHASE -> "Fase"
}

private fun cameraPresetLabel(preset: CameraPreset): String = when (preset) {
    CameraPreset.FREE -> "Libre"
    CameraPreset.TOP -> "Superior"
    CameraPreset.FRONT -> "Frontal"
    CameraPreset.SIDE -> "Lateral"
}
