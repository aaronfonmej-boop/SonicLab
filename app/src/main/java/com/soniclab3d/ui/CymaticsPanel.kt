package com.soniclab3d.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soniclab3d.academic.AcademicExperienceState
import com.soniclab3d.academic.AcademicPractice
import com.soniclab3d.academic.AcademicRole
import com.soniclab3d.academic.CymaticsAcademicLocks
import com.soniclab3d.academic.CymaticsAcademicPractices
import com.soniclab3d.cymatics.CymaticsCameraMotion
import com.soniclab3d.cymatics.CymaticsMetrics
import com.soniclab3d.cymatics.CymaticsCameraPreset
import com.soniclab3d.cymatics.CymaticsModeBank
import com.soniclab3d.cymatics.CymaticsModeRepository
import com.soniclab3d.cymatics.CymaticsMotion
import com.soniclab3d.cymatics.CymaticsMotionPreset
import com.soniclab3d.cymatics.CymaticsPalette
import com.soniclab3d.cymatics.CymaticsPhysics
import com.soniclab3d.cymatics.CymaticsRenderInfo
import com.soniclab3d.cymatics.CymaticsState
import com.soniclab3d.cymatics.CymaticsVariable
import com.soniclab3d.cymatics.CymaticsVisualStyle
import com.soniclab3d.cymatics.PlateBoundary
import com.soniclab3d.cymatics.PlateGeometry
import com.soniclab3d.particles.ParticleQuality
import com.soniclab3d.performance.PerformanceStats
import com.soniclab3d.ui.theme.SonicPalette
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ln
import kotlin.math.pow

internal enum class CymaticsWorkspace {
    EXPLORE,
    LEARN,
    LAB
}

@Composable
internal fun CymaticsPanel(
    workspace: CymaticsWorkspace,
    state: CymaticsState,
    renderInfo: CymaticsRenderInfo,
    stats: PerformanceStats,
    experience: AcademicExperienceState,
    update: ((CymaticsState) -> CymaticsState) -> Unit,
    onCamera: (CymaticsCameraPreset) -> Unit,
    onResetSand: () -> Unit,
    onResetScene: () -> Unit,
    onRoleChange: (AcademicRole) -> Unit,
    onPrepare: (String) -> Unit,
    onSubmit: (String, String) -> Unit,
    onPublish: (String, CymaticsAcademicLocks, String, String) -> Unit,
    onCloseActivity: () -> Unit,
    onResetProgress: () -> Unit,
    onExportAcademic: () -> Unit,
    onExportExperiment: (CymaticsModeBank, CymaticsMetrics) -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context.applicationContext) {
        CymaticsModeRepository(context.applicationContext)
    }
    var bank by remember { mutableStateOf<CymaticsModeBank?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.geometry, state.boundary) {
        bank = null
        loadError = null
        runCatching {
            withContext(Dispatchers.IO) { repository.load(state.geometry, state.boundary) }
        }.onSuccess { bank = it }
            .onFailure { loadError = "No se pudo cargar el banco modal" }
    }
    val metrics = remember(state, bank) { bank?.let { CymaticsPhysics.metrics(state, it) } }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (workspace) {
            CymaticsWorkspace.EXPLORE -> CymaticsExplore(
                state = state,
                metrics = metrics,
                renderInfo = renderInfo,
                stats = stats,
                update = update,
                onCamera = onCamera,
                onResetSand = onResetSand,
                onResetScene = onResetScene
            )
            CymaticsWorkspace.LEARN -> CymaticsLearn(
                state = state,
                metrics = metrics,
                bank = bank,
                renderInfo = renderInfo,
                experience = experience,
                onRoleChange = onRoleChange,
                onPrepare = onPrepare,
                onSubmit = onSubmit,
                onPublish = onPublish,
                onCloseActivity = onCloseActivity,
                onResetProgress = onResetProgress,
                onExportAcademic = onExportAcademic
            )
            CymaticsWorkspace.LAB -> CymaticsLab(
                state = state,
                metrics = metrics,
                bank = bank,
                renderInfo = renderInfo,
                stats = stats,
                update = update,
                onExport = onExportExperiment
            )
        }
        loadError?.let { CymNotice("BANCO MODAL", it, SonicPalette.Magenta) }
        if (bank == null && loadError == null) {
            CymNotice("CARGANDO MODELO", "Preparando 24 modos propios reproducibles…", SonicPalette.Cyan)
        }
    }
}

@Composable
private fun CymaticsExplore(
    state: CymaticsState,
    metrics: CymaticsMetrics?,
    renderInfo: CymaticsRenderInfo,
    stats: PerformanceStats,
    update: ((CymaticsState) -> CymaticsState) -> Unit,
    onCamera: (CymaticsCameraPreset) -> Unit,
    onResetSand: () -> Unit,
    onResetScene: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    CymSection("Placa y excitación")
    Text(
        "Explora patrones modales calculados para esta placa. Toca la escena para mover el excitador.",
        style = MaterialTheme.typography.bodyMedium,
        color = SonicPalette.Muted
    )
    CymChoiceRow(PlateGeometry.entries.toList(), state.geometry, { it.label }) {
        update { current -> current.copy(geometry = it) }
    }
    CymChoiceRow(PlateBoundary.entries.toList(), state.boundary, { it.label }) {
        update { current -> current.copy(boundary = it) }
    }
    CymSlider(
        label = "Frecuencia de excitación",
        value = frequencyToSlider(state.driveFrequencyHz),
        valueRange = 0f..1f,
        display = cymHz(state.driveFrequencyHz)
    ) { slider -> update { it.copy(driveFrequencyHz = sliderToFrequency(slider)) } }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(listOf(60, 120, 200, 432, 440, 528, 1_000, 5_000)) { preset ->
            FilterChip(
                selected = kotlin.math.abs(state.driveFrequencyHz - preset) < 0.5,
                onClick = { update { it.copy(driveFrequencyHz = preset.toDouble()) } },
                label = { Text(cymHz(preset.toDouble())) }
            )
        }
    }
    CymToggle("Selección automática del modo dominante", state.automaticMode) {
        update { current -> current.copy(automaticMode = it) }
    }
    if (!state.automaticMode) {
        CymSlider(
            "Modo propio",
            state.modeIndex.toFloat(),
            0f..(CymaticsModeBank.MODE_COUNT - 1).toFloat(),
            "Modo ${state.modeIndex + 1}",
            steps = CymaticsModeBank.MODE_COUNT - 2
        ) { value -> update { it.copy(modeIndex = value.toInt()) } }
    }
    CymMetricRow(metrics, renderInfo, stats)
    Button(
        onClick = {
            metrics?.let { value ->
                update { it.copy(driveFrequencyHz = value.naturalFrequencyHz, automaticMode = false, modeIndex = value.modeIndex) }
                if (state.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        },
        enabled = metrics != null,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Cyan, contentColor = SonicPalette.Void),
        shape = RoundedCornerShape(13.dp)
    ) {
        Text("◎  Ajustar a resonancia del modo", fontWeight = FontWeight.Bold)
    }

    CymSection("Aspecto de la placa")
    CymChoiceRow(CymaticsVisualStyle.entries.toList(), state.visualStyle, { it.label }) {
        update { current -> current.copy(visualStyle = it) }
    }
    CymChoiceRow(CymaticsPalette.entries.toList(), state.palette, { it.label }) {
        update { current -> current.copy(palette = it) }
    }
    CymChoiceRow(CymaticsVariable.entries.toList(), state.variable, { it.label }) {
        update { current -> current.copy(variable = it) }
    }
    CymToggle("Líneas nodales", state.showNodes) { update { current -> current.copy(showNodes = it) } }
    CymToggle("Antinodos", state.showAntinodes) { update { current -> current.copy(showAntinodes = it) } }
    CymToggle("Contornos modales", state.showPhaseLines) { update { current -> current.copy(showPhaseLines = it) } }
    if (state.showPhaseLines) {
        CymSlider("Densidad de contornos", state.contourDensity, 4f..32f, state.contourDensity.toInt().toString()) {
            update { current -> current.copy(contourDensity = it) }
        }
    }
    CymToggle("Retícula de referencia", state.showGrid) { update { current -> current.copy(showGrid = it) } }
    CymToggle("Sonda modal · pulsación larga", state.probeEnabled) { update { current -> current.copy(probeEnabled = it) } }
    CymToggle("Arena cualitativa", state.sandEnabled) { update { current -> current.copy(sandEnabled = it) } }
    CymSlider("Relieve visual", state.visualScale, 0.02f..1.2f, cymNumber(state.visualScale.toDouble(), 2)) {
        update { current -> current.copy(visualScale = it) }
    }
    CymSlider(
        "Ritmo visual",
        state.visualTimeScale,
        0.005f..0.25f,
        "${cymNumber(CymaticsMotion.visualCyclesPerSecond(state.copy(paused = false, reduceMotion = false)).toDouble(), 2)} ciclos/s"
    ) {
        update { current -> current.copy(visualTimeScale = it) }
    }
    CymSlider("Color de luz", state.lightHueDegrees, 0f..360f, "${state.lightHueDegrees.toInt()}°") {
        update { current -> current.copy(lightHueDegrees = it) }
    }
    if (state.sandEnabled) {
        CymSlider("Tamaño de arena", state.sandPointSizePx, 1f..7f, "${cymNumber(state.sandPointSizePx.toDouble(), 1)} px") {
            update { current -> current.copy(sandPointSizePx = it) }
        }
        CymSlider("Relajación nodal", state.sandRelaxation, 0.08f..1.5f, cymNumber(state.sandRelaxation.toDouble(), 2)) {
            update { current -> current.copy(sandRelaxation = it) }
        }
        CymSlider("Flujo orgánico de arena", state.sandFlowIntensity, 0f..1f, "${(state.sandFlowIntensity * 100).toInt()} %") {
            update { current -> current.copy(sandFlowIntensity = it) }
        }
        OutlinedButton(onClick = onResetSand, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("↻  Redistribuir arena")
        }
    }

    CymSection("Movimiento cinematográfico")
    Text(
        "Los efectos nacen del mismo campo modal; luz, cámara y ambiente son capas visuales y no modifican la solución física.",
        style = MaterialTheme.typography.bodyMedium,
        color = SonicPalette.Muted
    )
    CymChoiceRow(CymaticsMotionPreset.entries.toList(), state.motionPreset, { it.label }) { preset ->
        update { current -> CymaticsMotion.applyPreset(current, preset) }
    }
    CymNotice("PERFIL ${state.motionPreset.label.uppercase()}", state.motionPreset.description, SonicPalette.Violet)
    CymSlider("Intensidad escénica", state.motionIntensity, 0f..1f, "${(state.motionIntensity * 100).toInt()} %") {
        update { current -> current.copy(motionIntensity = it) }
    }
    CymSlider(
        "Morph entre modos",
        state.modeTransitionSeconds,
        0.12f..2.4f,
        "${cymNumber(state.modeTransitionSeconds.toDouble(), 2)} s"
    ) {
        update { current -> current.copy(modeTransitionSeconds = it) }
    }
    CymToggle("Coreografía de cámara con pausa táctil", state.cameraDriftEnabled) {
        update { current -> current.copy(cameraDriftEnabled = it) }
    }
    if (state.cameraDriftEnabled) {
        CymChoiceRow(CymaticsCameraMotion.entries.toList(), state.cameraMotion, { it.label }) {
            update { current -> current.copy(cameraMotion = it) }
        }
    }
    CymToggle("Doble luz orbital", state.orbitingLightEnabled) {
        update { current -> current.copy(orbitingLightEnabled = it) }
    }
    CymToggle("Aura reactiva de resonancia", state.resonanceAuraEnabled) {
        update { current -> current.copy(resonanceAuraEnabled = it) }
    }
    CymToggle("Partículas ambientales", state.atmosphericDustEnabled) {
        update { current -> current.copy(atmosphericDustEnabled = it) }
    }
    CymToggle("Escenario profundo bajo la placa", state.stageEnabled) {
        update { current -> current.copy(stageEnabled = it) }
    }
    CymToggle("Pulsos de energía desde el excitador", state.energyPulsesEnabled) {
        update { current -> current.copy(energyPulsesEnabled = it) }
    }
    CymToggle("Corrientes luminosas sobre los nodos", state.nodalStreamsEnabled) {
        update { current -> current.copy(nodalStreamsEnabled = it) }
    }
    CymToggle("Postprocesado cinematográfico", state.postProcessingEnabled) {
        update { current -> current.copy(postProcessingEnabled = it) }
    }
    if (state.postProcessingEnabled) {
        CymSlider(
            "Bloom de filamentos",
            state.bloomIntensity,
            0f..1.5f,
            "${(state.bloomIntensity * 100f / 1.5f).toInt()} %"
        ) {
            update { current -> current.copy(bloomIntensity = it) }
        }
    }
    Button(
        onClick = {
            update { current ->
                CymaticsMotion.applyPreset(
                    current.copy(
                        visualStyle = CymaticsVisualStyle.SPECTACULAR,
                        palette = CymaticsPalette.AURORA,
                        showNodes = true,
                        showAntinodes = true,
                        sandEnabled = true,
                        visualScale = 0.64f,
                        sandPointSizePx = 2.8f,
                        reduceMotion = false
                    ),
                    CymaticsMotionPreset.CINEMATIC
                )
            }
            onCamera(CymaticsCameraPreset.GRAZING)
            onResetSand()
            if (state.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SonicPalette.Magenta,
            contentColor = SonicPalette.Ice
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        Text("✦  Activar escena show", fontWeight = FontWeight.Bold)
    }
    if (state.reduceMotion) {
        CymNotice(
            "MOVIMIENTO REDUCIDO ACTIVO",
            "La oscilación y los efectos decorativos quedan detenidos; los cambios de modo se aplican sin morph prolongado.",
            SonicPalette.Green
        )
    }

    CymSection("Vista y rendimiento")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(CymaticsCameraPreset.entries) { preset ->
            FilterChip(selected = false, onClick = { onCamera(preset) }, label = { Text(preset.label) })
        }
    }
    CymChoiceRow(ParticleQuality.entries.toList(), state.quality, { "${it.label} · ${it.particleCount / 1000}k" }) {
        update { current -> current.copy(quality = it) }
    }
    CymToggle("Calidad adaptativa ~60 FPS", state.autoQuality) {
        update { current -> current.copy(autoQuality = it) }
    }
    CymToggle("Reducir movimiento decorativo", state.reduceMotion) {
        update { current -> current.copy(reduceMotion = it) }
    }
    CymToggle("Respuesta háptica", state.hapticsEnabled) {
        update { current -> current.copy(hapticsEnabled = it) }
    }
    OutlinedButton(onClick = onResetScene, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text("Restablecer modo Cimática")
    }
    CymNotice(
        "LECTURA HONESTA",
        "La amplitud es normalizada (a.u.). La arena usa relajación nodal cualitativa y no simula granos reales ni predice una figura universal para cada frecuencia.",
        SonicPalette.Amber
    )
}

@Composable
private fun CymaticsLab(
    state: CymaticsState,
    metrics: CymaticsMetrics?,
    bank: CymaticsModeBank?,
    renderInfo: CymaticsRenderInfo,
    stats: PerformanceStats,
    update: ((CymaticsState) -> CymaticsState) -> Unit,
    onExport: (CymaticsModeBank, CymaticsMetrics) -> Unit
) {
    CymSection("Material ideal configurable")
    Text(
        "Modelo modal de Kirchhoff–Love. Cambia una variable por vez y compara las frecuencias propias.",
        style = MaterialTheme.typography.bodyMedium,
        color = SonicPalette.Muted
    )
    CymSlider("Módulo de Young", logSlider(state.youngModulusPa, 1.0e8, 3.0e11), 0f..1f, "${cymNumber(state.youngModulusPa / 1.0e9, 2)} GPa") {
        update { current -> current.copy(youngModulusPa = expSlider(it, 1.0e8, 3.0e11)) }
    }
    CymSlider("Densidad", state.densityKgM3.toFloat(), 100f..12_000f, "${state.densityKgM3.toInt()} kg/m³") {
        update { current -> current.copy(densityKgM3 = it.toDouble()) }
    }
    CymSlider("Coeficiente de Poisson", state.poissonRatio.toFloat(), 0f..0.49f, cymNumber(state.poissonRatio, 3)) {
        update { current -> current.copy(poissonRatio = it.toDouble()) }
    }
    CymSlider("Espesor", (state.thicknessM * 1_000.0).toFloat(), 0.2f..10f, "${cymNumber(state.thicknessM * 1_000.0, 2)} mm") {
        update { current -> current.copy(thicknessM = it / 1_000.0) }
    }
    CymSlider("Tamaño característico", (state.characteristicSizeM * 100.0).toFloat(), 8f..120f, "${cymNumber(state.characteristicSizeM * 100.0, 1)} cm") {
        update { current -> current.copy(characteristicSizeM = it / 100.0) }
    }
    CymSlider("Amortiguamiento ζ", (state.dampingRatio * 100.0).toFloat(), 0.2f..15f, "${cymNumber(state.dampingRatio * 100.0, 2)} %") {
        update { current -> current.copy(dampingRatio = it / 100.0) }
    }
    CymSlider("Fuerza normalizada", state.normalizedDrive, 0.05f..1f, "${(state.normalizedDrive * 100).toInt()} %") {
        update { current -> current.copy(normalizedDrive = it) }
    }

    CymSection("Posición del excitador")
    CymSlider("Eje X", state.driveX, -0.95f..0.95f, cymNumber(state.driveX.toDouble(), 2)) {
        update { current -> current.copy(driveX = it) }
    }
    CymSlider("Eje Y", state.driveY, -0.95f..0.95f, cymNumber(state.driveY.toDouble(), 2)) {
        update { current -> current.copy(driveY = it) }
    }
    CymMetricRow(metrics, renderInfo, stats)
    metrics?.let { value ->
        CymTable(
            listOf(
                "Rigidez flexional D" to "${cymNumber(value.flexuralRigidityNm, 5)} N·m",
                "Diferencia |f−fₙ|" to "${cymNumber(kotlin.math.abs(state.driveFrequencyHz - value.naturalFrequencyHz), 3)} Hz",
                "Desajuste |f−fₙ|/fₙ" to cymNumber(value.relativeDetuning, 4),
                "Amortiguamiento ζ" to cymNumber(state.dampingRatio, 4),
                "Respuesta normalizada" to "${cymNumber(value.response, 4)} a.u.",
                "Acoplamiento" to "${cymNumber(value.coupling, 4)} a.u.",
                "Lectura de sonda φ" to if (state.probeEnabled) "${cymNumber(renderInfo.probeValue, 4)} a.u." else "desactivada",
                "Amplitud mostrada" to "${cymNumber(value.normalizedAmplitude, 4)} a.u."
            )
        )
    }
    if (bank != null) {
        CymSection("Espectro modal")
        bank.eigenvalues.take(8).forEachIndexed { index, eigenvalue ->
            val frequency = CymaticsPhysics.naturalFrequencyHz(state, eigenvalue.toDouble())
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("M${index + 1}", color = if (metrics?.modeIndex == index) SonicPalette.Cyan else SonicPalette.Muted, fontSize = 9.sp, modifier = Modifier.width(30.dp))
                LinearProgressIndicator(
                    progress = { (frequency / metrics.orLastFrequency(bank, state)).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.weight(1f).height(5.dp),
                    color = if (metrics?.modeIndex == index) SonicPalette.Magenta else SonicPalette.Violet,
                    trackColor = SonicPalette.Grid
                )
                Text(cymHz(frequency), modifier = Modifier.width(72.dp), color = SonicPalette.Ice, fontSize = 9.sp)
            }
        }
    }
    Button(
        onClick = { if (bank != null && metrics != null) onExport(bank, metrics) },
        enabled = bank != null && metrics != null,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Green, contentColor = SonicPalette.Void)
    ) {
        Text("↗  Exportar experimento JSON", fontWeight = FontWeight.Bold)
    }
    CymNotice(
        "ALCANCE DEL MODELO",
        "Los modos son una solución numérica de placa delgada idealizada. El borde sujeto usa una penalización de pendiente en malla finita. No representa una medición ni sustituye un ensayo modal.",
        SonicPalette.Amber
    )
}

private fun CymaticsMetrics?.orLastFrequency(bank: CymaticsModeBank, state: CymaticsState): Double =
    this?.lastFrequencyHz ?: CymaticsPhysics.naturalFrequencyHz(state, bank.eigenvalues.last().toDouble())

@Composable
private fun CymaticsLearn(
    state: CymaticsState,
    metrics: CymaticsMetrics?,
    bank: CymaticsModeBank?,
    renderInfo: CymaticsRenderInfo,
    experience: AcademicExperienceState,
    onRoleChange: (AcademicRole) -> Unit,
    onPrepare: (String) -> Unit,
    onSubmit: (String, String) -> Unit,
    onPublish: (String, CymaticsAcademicLocks, String, String) -> Unit,
    onCloseActivity: () -> Unit,
    onResetProgress: () -> Unit,
    onExportAcademic: () -> Unit
) {
    CymSection("Experiencia académica · Cimática")
    CymNotice(
        "MODELO Y SUPUESTOS",
        "D∇⁴w + cᵈ∂w/∂t + ρh∂²w/∂t² = q. Placa delgada, homogénea, isotrópica y lineal; amplitud normalizada, no medida.",
        SonicPalette.Cyan
    )
    if (metrics != null && bank != null) {
        val responseCurve = remember(metrics.naturalFrequencyHz, state.dampingRatio) {
            List(121) { index ->
                val ratio = 0.65 + 0.70 * index / 120.0
                (2.0 * state.dampingRatio * CymaticsPhysics.responseMagnitude(
                    metrics.naturalFrequencyHz * ratio,
                    metrics.naturalFrequencyHz,
                    state.dampingRatio
                )).coerceIn(0.0, 1.0)
            }
        }
        ScientificGraph(
            title = "Respuesta alrededor de la resonancia · modo ${metrics.modeIndex + 1}",
            values = responseCurve,
            unit = "a.u.",
            cursorFraction = ((state.driveFrequencyHz / metrics.naturalFrequencyHz - 0.65) / 0.70).toFloat(),
            lineColor = SonicPalette.Magenta
        )
        CymTable(
            bank.eigenvalues.take(6).mapIndexed { index, eigenvalue ->
                "Modo ${index + 1}" to cymHz(CymaticsPhysics.naturalFrequencyHz(state, eigenvalue.toDouble()))
            } + ("Sonda φ" to if (state.probeEnabled) "${cymNumber(renderInfo.probeValue, 4)} a.u." else "mantén pulsada la placa")
        )
    }
    CymChoiceRow(AcademicRole.entries.toList(), experience.role, { it.label }, onRoleChange)
    if (experience.role == AcademicRole.STUDENT) {
        CymaticsStudent(experience, state, onPrepare, onSubmit, onExportAcademic)
    } else {
        CymaticsTeacher(experience, state, onPrepare, onPublish, onCloseActivity, onResetProgress, onExportAcademic)
    }
}

@Composable
private fun CymaticsStudent(
    experience: AcademicExperienceState,
    state: CymaticsState,
    onPrepare: (String) -> Unit,
    onSubmit: (String, String) -> Unit,
    onExport: () -> Unit
) {
    val assignment = experience.cymaticsTeacherActivity.takeIf { it.active }
    var selectedId by rememberSaveable { mutableStateOf(assignment?.practiceId ?: CymaticsAcademicPractices.all.first().id) }
    LaunchedEffect(assignment?.practiceId) { assignment?.let { selectedId = it.practiceId } }
    val practice = CymaticsAcademicPractices.byId(assignment?.practiceId ?: selectedId)
    var answer by rememberSaveable(practice.id, experience.answers[practice.id]) {
        mutableStateOf(experience.answers[practice.id].orEmpty())
    }
    var prediction by rememberSaveable(practice.id) { mutableStateOf("") }
    var showHint by rememberSaveable(practice.id) { mutableStateOf(false) }

    CymProgress(experience)
    if (assignment == null) {
        CymPracticePicker(practice.id) { selectedId = it }
    } else {
        CymNotice(
            "ACTIVIDAD ASIGNADA",
            "${assignment.practice.number} · ${assignment.practice.title}. ${assignment.locks.count} grupos bloqueados por el docente.",
            SonicPalette.Amber
        )
    }
    CymPracticeOverview(practice)
    OutlinedTextField(
        value = prediction,
        onValueChange = { prediction = it.take(500) },
        label = { Text("Predicción antes de observar") },
        supportingText = { Text("Opcional · registra qué esperas que ocurra") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = { onPrepare(practice.id) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Cyan, contentColor = SonicPalette.Void)
    ) { Text("▶  ${practice.actionLabel}", fontWeight = FontWeight.Bold) }
    Text(
        "Caso: ${state.geometry.label} · ${state.boundary.label} · modo ${state.modeIndex + 1} · ${cymHz(state.driveFrequencyHz)}",
        color = SonicPalette.Cyan,
        fontSize = 9.sp
    )
    Surface(
        color = SonicPalette.Violet.copy(alpha = 0.08f),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, SonicPalette.Violet.copy(alpha = 0.22f))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PREGUNTA DE CIERRE", color = SonicPalette.Violet, style = MaterialTheme.typography.labelSmall)
            Text(practice.question, color = SonicPalette.Ice)
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it.take(1_500) },
                label = { Text("Tu respuesta") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Button(
                    onClick = { onSubmit(practice.id, answer) },
                    enabled = answer.isNotBlank(),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Green, contentColor = SonicPalette.Void)
                ) { Text("Entregar", fontSize = 10.sp) }
                OutlinedButton(onClick = { showHint = !showHint }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Text(if (showHint) "Ocultar pista" else "Ver pista", fontSize = 10.sp)
                }
            }
            if (showHint) Text(practice.hint, color = SonicPalette.Amber, fontSize = 10.sp)
        }
    }
    if (practice.id in experience.completedPracticeIds) {
        CymNotice(
            "CONTRASTE",
            "Respuesta esperada: ${assignment?.expectedAnswer ?: practice.expectedAnswer}\n\nCriterio: ${assignment?.rubric ?: practice.defaultRubric}",
            SonicPalette.Green
        )
    }
    OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text("↗  Exportar progreso JSON")
    }
}

@Composable
private fun CymaticsTeacher(
    experience: AcademicExperienceState,
    state: CymaticsState,
    onPrepare: (String) -> Unit,
    onPublish: (String, CymaticsAcademicLocks, String, String) -> Unit,
    onCloseActivity: () -> Unit,
    onResetProgress: () -> Unit,
    onExport: () -> Unit
) {
    val active = experience.cymaticsTeacherActivity
    var selectedId by rememberSaveable { mutableStateOf(active.practiceId) }
    val practice = CymaticsAcademicPractices.byId(selectedId)
    var geometryLock by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.locks.geometryAndBoundary else true)
    }
    var materialLock by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.locks.materialAndDimensions else true)
    }
    var frequencyLock by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.locks.frequencyAndMode else true)
    }
    var exciterLock by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.locks.exciter else false)
    }
    var visualLock by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.locks.visualAids else false)
    }
    var expected by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.expectedAnswer else practice.expectedAnswer)
    }
    var rubric by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.rubric else practice.defaultRubric)
    }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    if (active.active) {
        CymNotice("ACTIVIDAD PUBLICADA", "${active.practice.title} · ${active.locks.count} bloqueos activos · almacenamiento local.", SonicPalette.Green)
    }
    CymPracticePicker(practice.id) { selectedId = it }
    CymPracticeOverview(practice)
    Text("Los grupos marcados quedan fijados a la línea base al publicar.", color = SonicPalette.Muted, fontSize = 10.sp)
    CymLock("Geometría y borde", geometryLock) { geometryLock = it }
    CymLock("Material y dimensiones", materialLock) { materialLock = it }
    CymLock("Frecuencia y modo", frequencyLock) { frequencyLock = it }
    CymLock("Posición del excitador", exciterLock) { exciterLock = it }
    CymLock("Ayudas visuales", visualLock) { visualLock = it }
    OutlinedTextField(expected, { expected = it.take(2_000) }, label = { Text("Respuesta esperada") }, minLines = 3, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(rubric, { rubric = it.take(2_000) }, label = { Text("Rúbrica") }, minLines = 3, modifier = Modifier.fillMaxWidth())
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        OutlinedButton(onClick = { onPrepare(practice.id) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
            Text("Previsualizar", fontSize = 10.sp)
        }
        Button(
            onClick = {
                onPublish(
                    practice.id,
                    CymaticsAcademicLocks(geometryLock, materialLock, frequencyLock, exciterLock, visualLock),
                    expected,
                    rubric
                )
            },
            enabled = expected.isNotBlank() && rubric.isNotBlank(),
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Violet)
        ) { Text(if (active.active) "Actualizar" else "Publicar", fontSize = 10.sp) }
    }
    Text(
        "Línea base visible: ${state.geometry.label} · modo ${state.modeIndex + 1} · ${cymHz(state.driveFrequencyHz)}",
        color = SonicPalette.Cyan,
        fontSize = 9.sp
    )
    if (active.active) {
        OutlinedButton(onClick = onCloseActivity, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Cerrar actividad asignada")
        }
    }
    CymSection("Resultados locales")
    CymaticsAcademicPractices.all.forEach { item ->
        val answer = experience.answers[item.id]
        Surface(color = Color.White.copy(alpha = 0.025f), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(10.dp)) {
                Text("${item.number} · ${item.shortTitle}", fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                Text(answer ?: "Sin entrega", color = if (answer == null) SonicPalette.Muted else SonicPalette.Green, fontSize = 9.sp, maxLines = 3)
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Exportar JSON", fontSize = 10.sp) }
        if (confirmReset) {
            Button(
                onClick = { onResetProgress(); confirmReset = false },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Magenta)
            ) { Text("Confirmar borrado", fontSize = 10.sp) }
        } else {
            OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                Text("Borrar progreso", fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun CymProgress(experience: AcademicExperienceState) {
    Surface(
        color = SonicPalette.Cyan.copy(alpha = 0.065f),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, SonicPalette.Cyan.copy(alpha = 0.18f))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${experience.cymaticsCompletedCount} de ${CymaticsAcademicPractices.all.size} prácticas", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text("${(experience.cymaticsProgress * 100).toInt()} %", color = SonicPalette.Green, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { experience.cymaticsProgress },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = SonicPalette.Green,
                trackColor = SonicPalette.Grid
            )
        }
    }
}

@Composable
private fun CymPracticePicker(selectedId: String, onSelected: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(CymaticsAcademicPractices.all, key = { it.id }) { practice ->
            FilterChip(
                selected = selectedId == practice.id,
                onClick = { onSelected(practice.id) },
                label = { Text("${practice.number} · ${practice.shortTitle}") }
            )
        }
    }
}

@Composable
private fun CymPracticeOverview(practice: AcademicPractice) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1A29)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SonicPalette.Cyan.copy(alpha = 0.16f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${practice.number} · ${practice.title}", color = SonicPalette.Cyan, style = MaterialTheme.typography.titleMedium)
            Text(practice.objective, color = SonicPalette.Ice, style = MaterialTheme.typography.bodyMedium)
            practice.steps.forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("${index + 1}", color = SonicPalette.Green, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(step, color = SonicPalette.Muted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun CymMetricRow(metrics: CymaticsMetrics?, renderInfo: CymaticsRenderInfo, stats: PerformanceStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CymMetric("MODO", if (metrics == null) "…" else "${metrics.modeIndex + 1}", SonicPalette.Cyan, Modifier.weight(1f))
        CymMetric("f PROPIA", metrics?.let { cymHz(it.naturalFrequencyHz) } ?: "…", SonicPalette.Violet, Modifier.weight(1f))
        CymMetric("RESP.", metrics?.let { cymNumber(it.response, 2) } ?: "…", SonicPalette.Magenta, Modifier.weight(1f))
        CymMetric("FPS", cymNumber(stats.fps.toDouble(), 0), SonicPalette.Green, Modifier.weight(1f))
    }
    renderInfo.warning?.let { CymNotice("DIAGNÓSTICO", it, SonicPalette.Amber) }
}

@Composable
private fun CymMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = accent.copy(alpha = 0.07f), shape = RoundedCornerShape(11.dp), border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))) {
        Column(Modifier.padding(horizontal = 7.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = SonicPalette.Muted, fontSize = 7.sp, maxLines = 1)
            Text(value, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun <T> CymChoiceRow(items: List<T>, selected: T, label: (T) -> String, onSelected: (T) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(items) { item ->
            FilterChip(selected = item == selected, onClick = { onSelected(item) }, label = { Text(label(item)) })
        }
    }
}

@Composable
private fun CymSlider(label: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, display: String, steps: Int = 0, onValueChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), color = SonicPalette.Muted, fontSize = 10.sp)
            Text(display, color = SonicPalette.Cyan, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
        }
        Slider(value = value.coerceIn(valueRange), onValueChange = onValueChange, valueRange = valueRange, steps = steps)
    }
}

@Composable
private fun CymToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(color = Color.White.copy(alpha = 0.025f), shape = RoundedCornerShape(13.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), color = if (checked) SonicPalette.Ice else SonicPalette.Muted, fontSize = 10.sp)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun CymLock(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.025f),
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(1.dp, if (checked) SonicPalette.Amber.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f))
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), color = SonicPalette.Ice, fontSize = 10.sp)
            Text(if (checked) "BLOQUEADO" else "LIBRE", color = if (checked) SonicPalette.Amber else SonicPalette.Green, fontSize = 8.sp)
            Spacer(Modifier.width(7.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun CymSection(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(4.dp).height(20.dp).background(Brush.verticalGradient(listOf(SonicPalette.Cyan, SonicPalette.Magenta)), RoundedCornerShape(50)))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = SonicPalette.Ice)
    }
}

@Composable
private fun CymNotice(title: String, body: String, accent: Color) {
    Surface(color = accent.copy(alpha = 0.07f), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, accent.copy(alpha = 0.23f))) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = accent)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = SonicPalette.Ice)
        }
    }
}

@Composable
private fun CymTable(rows: List<Pair<String, String>>) {
    Surface(color = Color.White.copy(alpha = 0.025f), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            rows.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth()) {
                    Text(label, modifier = Modifier.weight(1f), color = SonicPalette.Muted, fontSize = 9.sp)
                    Text(value, color = SonicPalette.Ice, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun frequencyToSlider(frequency: Double): Float =
    ((ln(frequency.coerceIn(20.0, 20_000.0)) - ln(20.0)) / (ln(20_000.0) - ln(20.0))).toFloat()

private fun sliderToFrequency(value: Float): Double =
    20.0 * 1_000.0.pow(value.coerceIn(0f, 1f).toDouble())

private fun logSlider(value: Double, minimum: Double, maximum: Double): Float =
    ((ln(value.coerceIn(minimum, maximum)) - ln(minimum)) / (ln(maximum) - ln(minimum))).toFloat()

private fun expSlider(value: Float, minimum: Double, maximum: Double): Double =
    minimum * (maximum / minimum).pow(value.coerceIn(0f, 1f).toDouble())

private fun cymHz(value: Double): String = when {
    value >= 1_000.0 -> "${cymNumber(value / 1_000.0, if (value >= 10_000.0) 1 else 2)} kHz"
    else -> "${cymNumber(value, if (value < 100.0) 1 else 0)} Hz"
}

private fun cymNumber(value: Double, decimals: Int): String =
    String.format(Locale.US, "%.${decimals}f", value)
