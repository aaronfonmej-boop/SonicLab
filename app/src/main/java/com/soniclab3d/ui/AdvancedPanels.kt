package com.soniclab3d.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soniclab3d.audio.MicAnalysis
import com.soniclab3d.measurement.SplCalibrationCaptureState
import com.soniclab3d.measurement.SplCalibrationProfile
import com.soniclab3d.physics.AcousticPhysics
import com.soniclab3d.physics.AcousticState
import com.soniclab3d.physics.RectangularRoom
import com.soniclab3d.physics.RoomAcoustics
import com.soniclab3d.physics.ScientificPalette
import com.soniclab3d.physics.SliceAxis
import com.soniclab3d.ui.theme.SonicPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.floor

@Composable
fun FieldPanel(
    state: AcousticState,
    simulationTimeS: Double,
    update: ((AcousticState) -> AcousticState) -> Unit,
    onSeekTime: (Double) -> Unit
) {
    val period = AcousticPhysics.periodSeconds(state.frequencyHz)
    val currentCycle = floor(simulationTimeS / period)
    val phaseFraction = ((simulationTimeS / period) % 1.0).toFloat().let { if (it < 0f) it + 1f else it }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AdvancedSectionTitle("Campo y frentes")
        AdvancedToggle(
            "Corte 3D de presión",
            "Malla GPU semitransparente dentro de la nube",
            state.showPressureSlice
        ) { enabled -> update { it.copy(showPressureSlice = enabled) } }
        if (state.showPressureSlice) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(SliceAxis.entries) { axis ->
                    FilterChip(
                        selected = state.sliceAxis == axis,
                        onClick = { update { it.copy(sliceAxis = axis) } },
                        label = { Text("Plano ${axis.name}") }
                    )
                }
            }
            AdvancedSlider(
                "Posición del corte",
                state.sliceOffsetM,
                -state.maxDistanceM..state.maxDistanceM,
                "${formatAdvancedPanel(state.sliceOffsetM.toDouble(), 2)} m"
            ) { value -> update { it.copy(sliceOffsetM = value) } }
        }
        AdvancedToggle(
            "Resaltar frentes de onda",
            "Aumenta brillo y tamaño cerca de compresiones y rarefacciones máximas",
            state.showWavefronts
        ) { enabled -> update { it.copy(showWavefronts = enabled) } }

        AdvancedSectionTitle("Paleta accesible")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(ScientificPalette.entries) { palette ->
                FilterChip(
                    selected = state.scientificPalette == palette,
                    onClick = { update { it.copy(scientificPalette = palette) } },
                    label = { Text(paletteLabel(palette)) }
                )
            }
        }
        PressureSliceMap(state, simulationTimeS)

        AdvancedSectionTitle("Línea temporal precisa")
        AdvancedCard {
            Text("FASE DENTRO DEL PERÍODO", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Violet)
            Slider(
                value = phaseFraction,
                onValueChange = { fraction -> onSeekTime((currentCycle + fraction) * period) },
                colors = advancedSliderColors()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TimelineButton("−T/4", Modifier.weight(1f)) { onSeekTime((simulationTimeS - period * 0.25).coerceAtLeast(0.0)) }
                TimelineButton("+1°", Modifier.weight(1f)) { onSeekTime(simulationTimeS + period / 360.0) }
                TimelineButton("+T/4", Modifier.weight(1f)) { onSeekTime(simulationTimeS + period * 0.25) }
            }
            Text(
                "t = ${formatAdvancedPanel(simulationTimeS, 6)} s · fase ${(phaseFraction * 360f).toInt()}°",
                style = MaterialTheme.typography.labelSmall,
                color = SonicPalette.Muted
            )
        }

        AdvancedSectionTitle("Sondas espaciales")
        state.probes.forEachIndexed { index, probe ->
            val sample = AcousticPhysics.sampleAt(state, probe.xM, probe.yM, probe.zM, simulationTimeS)
            AdvancedCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = probeColor(index).copy(alpha = 0.14f), shape = RoundedCornerShape(9.dp)) {
                        Text(probe.label, color = probeColor(index), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("x = ${formatAdvancedPanel(probe.xM, 2)} m", modifier = Modifier.weight(1f), fontSize = 11.sp)
                    Text("${formatAdvancedPanel(sample.phaseRadians * 180.0 / PI, 1)}°", color = SonicPalette.Cyan, fontSize = 11.sp)
                }
                Slider(
                    value = probe.xM.toFloat().coerceIn(-state.maxDistanceM, state.maxDistanceM),
                    onValueChange = { value ->
                        update { current ->
                            val probes = current.probes.toMutableList()
                            probes[index] = probe.copy(xM = value.toDouble())
                            current.copy(probes = probes)
                        }
                    },
                    valueRange = -state.maxDistanceM..state.maxDistanceM,
                    colors = advancedSliderColors(probeColor(index))
                )
                Row(Modifier.fillMaxWidth()) {
                    Text("p ${scientificAdvancedPanel(sample.pressurePa)} Pa", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = SonicPalette.Muted)
                    Text("ξ ${scientificAdvancedPanel(sample.displacementMagnitudeM)} m", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Muted)
                }
            }
        }
        if (state.probes.size >= 2) {
            val first = state.probes[0]
            val second = state.probes[1]
            val distance = kotlin.math.abs(second.xM - first.xM)
            Text(
                "A→B: Δx = ${formatAdvancedPanel(distance, 3)} m · tiempo ideal de propagación = ${formatAdvancedPanel(distance / state.soundSpeedMps * 1_000.0, 4)} ms",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 10.sp,
                color = SonicPalette.Cyan
            )
        }
    }
}

@Composable
fun RoomPanel(
    state: AcousticState,
    update: ((AcousticState) -> AcousticState) -> Unit
) {
    val room = state.room
    val frequency = RoomAcoustics.modeFrequencyHz(room, state.soundSpeedMps)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AdvancedSectionTitle("Recinto rectangular")
        Text(
            "Modelo modal analítico ideal. No es todavía un solver de reflexiones arbitrarias ni incluye absorción de materiales.",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 10.sp,
            color = SonicPalette.Muted
        )
        AdvancedSlider("Largo X", room.lengthM.toFloat(), 2f..12f, "${formatAdvancedPanel(room.lengthM, 2)} m") {
            update { stateNow -> stateNow.copy(room = stateNow.room.copy(lengthM = it.toDouble())) }
        }
        AdvancedSlider("Ancho Y", room.widthM.toFloat(), 2f..10f, "${formatAdvancedPanel(room.widthM, 2)} m") {
            update { stateNow -> stateNow.copy(room = stateNow.room.copy(widthM = it.toDouble())) }
        }
        AdvancedSlider("Alto Z", room.heightM.toFloat(), 2f..6f, "${formatAdvancedPanel(room.heightM, 2)} m") {
            update { stateNow -> stateNow.copy(room = stateNow.room.copy(heightM = it.toDouble())) }
        }

        AdvancedSectionTitle("Índices modales")
        AdvancedModeSlider("nₓ", room.modeX) { value -> updateRoomMode(update, value, room.modeY, room.modeZ) }
        AdvancedModeSlider("nᵧ", room.modeY) { value -> updateRoomMode(update, room.modeX, value, room.modeZ) }
        AdvancedModeSlider("n_z", room.modeZ) { value -> updateRoomMode(update, room.modeX, room.modeY, value) }
        RoomModeMap(room, state.soundSpeedMps)
        Button(
            onClick = { update { it.copy(frequencyHz = frequency.coerceIn(20.0, 20_000.0)) } },
            colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Cyan, contentColor = SonicPalette.Void),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aplicar ${formatAdvancedPanel(frequency, 2)} Hz a la fuente")
        }
    }
}

@Composable
private fun AdvancedModeSlider(
    label: String,
    value: Int,
    onValue: (Int) -> Unit
) {
    AdvancedSlider(label, value.toFloat(), 0f..5f, value.toString(), steps = 4) { onValue(it.toInt()) }
}

private fun updateRoomMode(
    update: ((AcousticState) -> AcousticState) -> Unit,
    x: Int,
    y: Int,
    z: Int
) {
    val safeX = if (x + y + z == 0) 1 else x
    update { it.copy(room = it.room.copy(modeX = safeX, modeY = y, modeZ = z)) }
}

@Composable
fun AnalyzerPanel(
    analysis: MicAnalysis,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    profiles: List<SplCalibrationProfile>,
    captureState: SplCalibrationCaptureState,
    onStartCalibration: (Double, String) -> Unit,
    onCancelCalibration: () -> Unit,
    onInvalidateCalibration: (String) -> Unit,
    onDeleteCalibration: (String) -> Unit
) {
    var referenceText by rememberSaveable { mutableStateOf("94.0") }
    var methodText by rememberSaveable { mutableStateOf("Comparación con sonómetro o calibrador externo") }
    val referenceValue = referenceText.replace(',', '.').toDoubleOrNull()
    val routeProfiles = analysis.route?.let { route ->
        profiles.filter { it.routeKey == route.calibrationKey }
            .sortedByDescending { it.createdAtEpochMs }
    }.orEmpty()
    val activeProfile = routeProfiles.firstOrNull { it.valid }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AdvancedSectionTitle("Analizador y calibración SPL")
        AdvancedToggle(
            "Escuchar entrada del teléfono",
            "Procesamiento local; no guarda ni transmite grabaciones",
            enabled,
            onEnabledChange
        )
        AdvancedCard {
            Text(
                if (analysis.calibratedSplDb != null) "NIVEL CALIBRADO PARA ESTA RUTA" else "NIVEL DIGITAL SIN CALIBRAR",
                style = MaterialTheme.typography.labelSmall,
                color = if (analysis.calibratedSplDb != null) SonicPalette.Green else SonicPalette.Amber
            )
            Text(
                analysis.calibratedSplDb?.let { "${formatAdvancedPanel(it, 1)} dB SPL" }
                    ?: "${formatAdvancedPanel(analysis.relativeDbFs, 1)} dBFS",
                style = MaterialTheme.typography.displaySmall,
                color = if (analysis.calibratedSplDb != null) SonicPalette.Green else SonicPalette.Cyan
            )
            Text(
                "Frecuencia dominante ${if (analysis.fundamentalHz > 0.0) "${formatAdvancedPanel(analysis.fundamentalHz, 1)} Hz" else "— Hz"}",
                style = MaterialTheme.typography.labelSmall,
                color = SonicPalette.Violet
            )
            if (analysis.calibratedSplDb != null) {
                Text(
                    "Señal digital: ${formatAdvancedPanel(analysis.relativeDbFs, 1)} dBFS · perfil válido ${activeProfile?.id?.take(8).orEmpty()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SonicPalette.Muted
                )
            }
            Canvas(Modifier.fillMaxWidth().height(120.dp)) {
                val values = analysis.spectrum
                val gap = 2f
                val barWidth = (size.width - gap * (values.size - 1)) / values.size.coerceAtLeast(1)
                values.forEachIndexed { index, value ->
                    val height = value.coerceIn(0f, 1f) * size.height
                    val x = index * (barWidth + gap)
                    drawLine(
                        if (index < values.size / 3) SonicPalette.Cyan else SonicPalette.Violet,
                        Offset(x + barWidth / 2f, size.height),
                        Offset(x + barWidth / 2f, size.height - height),
                        barWidth.coerceAtLeast(2f),
                        StrokeCap.Round
                    )
                }
            }
            analysis.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp) }
        }

        AdvancedSectionTitle("Ruta de entrada activa")
        AdvancedCard {
            val route = analysis.route
            if (route == null) {
                Text(
                    if (enabled) "Esperando identificación de la ruta de Android…" else "Activa el micrófono para identificar la entrada",
                    color = SonicPalette.Muted,
                    fontSize = 10.sp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(route.displayName(), fontWeight = FontWeight.Bold, color = SonicPalette.Ice)
                        Text(route.deviceTypeLabel(), fontSize = 10.sp, color = SonicPalette.Cyan)
                    }
                    Surface(
                        color = if (activeProfile != null) SonicPalette.Green.copy(alpha = 0.12f) else SonicPalette.Amber.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            if (activeProfile != null) "CALIBRADA" else "SIN PERFIL",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (activeProfile != null) SonicPalette.Green else SonicPalette.Amber
                        )
                    }
                }
                Text(route.configurationLabel(), fontSize = 9.sp, color = SonicPalette.Muted)
                Text(
                    "ID Android ${route.deviceId}${route.address.takeIf { it.isNotBlank() }?.let { " · ruta $it" }.orEmpty()}",
                    fontSize = 9.sp,
                    color = SonicPalette.Muted
                )
            }
        }

        if (analysis.routeChanged) {
            Surface(
                color = SonicPalette.Amber.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SonicPalette.Amber.copy(alpha = 0.34f))
            ) {
                Text(
                    "La ruta de audio cambió durante esta sesión. SonicLab anuló el SPL anterior y solo lo mostrará si existe un perfil válido para la nueva entrada y configuración.",
                    modifier = Modifier.padding(11.dp),
                    fontSize = 10.sp,
                    color = SonicPalette.Amber
                )
            }
        }

        AdvancedSectionTitle("Crear perfil SPL")
        AdvancedCard {
            Text(
                "Coloca el micrófono de referencia y el teléfono en el mismo punto. Aplica una fuente estable y escribe el nivel indicado por el sonómetro o calibrador externo.",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 10.sp,
                color = SonicPalette.Muted
            )
            OutlinedTextField(
                value = referenceText,
                onValueChange = { referenceText = it },
                label = { Text("Nivel de referencia (dB SPL)") },
                supportingText = { Text("Rango aceptado: 20–140 dB SPL") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                enabled = !captureState.active,
                isError = referenceValue == null || referenceValue !in 20.0..140.0,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = methodText,
                onValueChange = { methodText = it },
                label = { Text("Método o equipo de referencia") },
                supportingText = { Text("Este texto queda guardado como trazabilidad") },
                enabled = !captureState.active,
                modifier = Modifier.fillMaxWidth()
            )
            if (captureState.active) {
                LinearProgressIndicator(
                    progress = { captureState.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = SonicPalette.Green,
                    trackColor = SonicPalette.Grid
                )
                Text(
                    "${captureState.message} · ${captureState.sampleCount} muestras",
                    fontSize = 10.sp,
                    color = SonicPalette.Green
                )
                TextButton(onClick = onCancelCalibration, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar captura")
                }
            } else {
                Button(
                    onClick = { onStartCalibration(referenceValue!!, methodText) },
                    enabled = enabled && analysis.running && analysis.route != null &&
                        referenceValue != null && referenceValue in 20.0..140.0 && methodText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SonicPalette.Green,
                        contentColor = SonicPalette.Void
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar captura estable de 5 segundos")
                }
                if (captureState.message != "Sin captura activa") {
                    Text(captureState.message, fontSize = 10.sp, color = SonicPalette.Muted)
                }
            }
            captureState.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
            }
        }

        AdvancedSectionTitle("Perfil de esta ruta")
        if (routeProfiles.isEmpty()) {
            AdvancedCard {
                Text(
                    "No hay un perfil guardado para la entrada y configuración actuales. La lectura permanece en dBFS.",
                    fontSize = 10.sp,
                    color = SonicPalette.Amber
                )
                if (profiles.isNotEmpty()) {
                    Text(
                        "Hay ${profiles.size} perfil${if (profiles.size == 1) "" else "es"} de otra ruta; no se aplica${if (profiles.size == 1) "" else "n"} automáticamente.",
                        fontSize = 9.sp,
                        color = SonicPalette.Muted
                    )
                }
            }
        } else {
            routeProfiles.forEach { profile ->
                AdvancedCard {
                    Text(
                        if (profile.valid) "PERFIL VÁLIDO" else "PERFIL INVALIDADO",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (profile.valid) SonicPalette.Green else SonicPalette.Amber
                    )
                    Text(
                        "Referencia ${formatAdvancedPanel(profile.referenceSplDb, 1)} dB SPL · captura ${formatAdvancedPanel(profile.capturedDbFs, 1)} dBFS",
                        fontSize = 10.sp
                    )
                    Text(
                        "Offset ${formatAdvancedPanel(profile.offsetDb, 2)} dB · ${formatCalibrationDate(profile.createdAtEpochMs)}",
                        fontSize = 10.sp,
                        color = SonicPalette.Cyan
                    )
                    Text(profile.method, fontSize = 9.sp, color = SonicPalette.Muted)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        if (profile.valid) {
                            Button(
                                onClick = { onInvalidateCalibration(profile.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SonicPalette.Amber.copy(alpha = 0.22f),
                                    contentColor = SonicPalette.Amber
                                )
                            ) { Text("Invalidar", fontSize = 10.sp) }
                        }
                        Button(
                            onClick = { onDeleteCalibration(profile.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.20f),
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("Borrar", fontSize = 10.sp) }
                    }
                }
            }
        }

        Surface(
            color = SonicPalette.Amber.copy(alpha = 0.08f),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SonicPalette.Amber.copy(alpha = 0.24f))
        ) {
            Text(
                "Una calibración por offset aporta trazabilidad para esta ruta, pero no certifica el teléfono ni demuestra conformidad IEC 61672 o equivalencia con un sonómetro de clase 1 o 2.",
                modifier = Modifier.padding(11.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 10.sp,
                color = SonicPalette.Amber
            )
        }
    }
}

@Composable
fun ToolsPanel(
    presentationRunning: Boolean,
    onPresentation: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AdvancedSectionTitle("Presentación automática")
        AdvancedCard {
            Text("RECORRIDO PARA CLASES Y FERIAS", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Cyan)
            Text(
                "Configura una secuencia de onda longitudinal, microscopio, corte de presión, interferencia y armónicos.",
                style = MaterialTheme.typography.bodyMedium,
                color = SonicPalette.Muted
            )
            Button(
                onClick = onPresentation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (presentationRunning) SonicPalette.Magenta else SonicPalette.Cyan,
                    contentColor = SonicPalette.Void
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (presentationRunning) "Detener presentación" else "Iniciar presentación")
            }
        }

        AdvancedSectionTitle("Exportación reproducible")
        AdvancedCard {
            Text(
                "Los archivos incluyen unidades, parámetros, modelo utilizado y tiempo de simulación.",
                style = MaterialTheme.typography.bodyMedium,
                color = SonicPalette.Muted
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExportCsv, modifier = Modifier.weight(1f)) { Text("CSV") }
                Button(onClick = onExportJson, modifier = Modifier.weight(1f)) { Text("JSON") }
            }
        }

        AdvancedSectionTitle("Estado de capacidades")
        CapabilityRow("Corte GPU", "IMPLEMENTADO", SonicPalette.Green)
        CapabilityRow("Frentes de fase", "IMPLEMENTADO", SonicPalette.Green)
        CapabilityRow("Recinto modal", "ANALÍTICO", SonicPalette.Cyan)
        CapabilityRow("Micrófono", "DBFS / SPL CALIBRABLE", SonicPalette.Cyan)
        CapabilityRow("Revisión externa", "CHECKLIST · SIN APROBAR", SonicPalette.Amber)
        CapabilityRow("Solver FDTD/Vulkan", "SIGUIENTE MOTOR", SonicPalette.Violet)
        CapabilityRow("Realidad aumentada", "REQUIERE ARCORE", SonicPalette.Violet)
    }
}

@Composable
private fun CapabilityRow(label: String, status: String, color: Color) {
    Surface(color = Color.White.copy(alpha = 0.025f), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), fontSize = 11.sp)
            Text(status, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable
private fun AdvancedToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = if (checked) SonicPalette.Cyan.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.025f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) SonicPalette.Cyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f))
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 11.sp)
                Text(description, fontSize = 9.sp, color = SonicPalette.Muted)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun AdvancedSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    shown: String,
    steps: Int = 0,
    onValue: (Float) -> Unit
) {
    AdvancedCard {
        Row {
            Text(title, modifier = Modifier.weight(1f), fontSize = 11.sp)
            Text(shown, color = SonicPalette.Cyan, fontSize = 11.sp)
        }
        Slider(value = value.coerceIn(range.start, range.endInclusive), onValueChange = onValue, valueRange = range, steps = steps, colors = advancedSliderColors())
    }
}

@Composable
private fun AdvancedCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        color = Color(0xFF091725),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { content() }
    }
}

@Composable
private fun AdvancedSectionTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(3.dp).height(14.dp).background(SonicPalette.Cyan, RoundedCornerShape(50)))
        Spacer(Modifier.width(7.dp))
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = SonicPalette.Cyan)
    }
}

@Composable
private fun TimelineButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.PanelBright, contentColor = SonicPalette.Cyan),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 7.dp)
    ) { Text(label, fontSize = 10.sp) }
}

@Composable
private fun advancedSliderColors(accent: Color = SonicPalette.Cyan) = SliderDefaults.colors(
    thumbColor = accent,
    activeTrackColor = accent,
    inactiveTrackColor = SonicPalette.Grid
)

private fun probeColor(index: Int): Color = when (index % 3) {
    0 -> SonicPalette.Cyan
    1 -> SonicPalette.Violet
    else -> SonicPalette.Green
}

private fun paletteLabel(palette: ScientificPalette): String = when (palette) {
    ScientificPalette.RED_BLUE -> "Rojo/Azul"
    ScientificPalette.COLORBLIND -> "Naranja/Cian"
    ScientificPalette.MONOCHROME -> "Monocromo"
}

private fun formatAdvancedPanel(value: Double, decimals: Int): String = String.format(Locale.US, "%.${decimals}f", value)
private fun scientificAdvancedPanel(value: Double): String = String.format(Locale.US, "%.3e", value)
private fun formatCalibrationDate(epochMs: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(epochMs))
