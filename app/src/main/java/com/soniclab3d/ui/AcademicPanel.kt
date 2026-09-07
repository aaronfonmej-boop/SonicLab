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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soniclab3d.academic.AcademicExperienceState
import com.soniclab3d.academic.AcademicLocks
import com.soniclab3d.academic.AcademicPractice
import com.soniclab3d.academic.AcademicPractices
import com.soniclab3d.academic.AcademicRole
import com.soniclab3d.physics.AcousticState
import com.soniclab3d.ui.theme.SonicPalette

@Composable
internal fun AcademicPanel(
    sceneState: AcousticState,
    experience: AcademicExperienceState,
    onRoleChange: (AcademicRole) -> Unit,
    onPrepare: (String) -> Unit,
    onSubmit: (String, String) -> Unit,
    onResetProgress: () -> Unit,
    onPublish: (String, AcademicLocks, String, String) -> Unit,
    onCloseActivity: () -> Unit,
    onExport: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        AcademicSectionTitle("Experiencia académica")
        Text(
            "La escena, la física y los datos son los mismos en ambos perfiles. Solo cambia el flujo de trabajo.",
            style = MaterialTheme.typography.bodyMedium,
            color = SonicPalette.Muted
        )
        AcademicRoleSelector(experience.role, onRoleChange)
        if (experience.role == AcademicRole.STUDENT) {
            StudentWorkspace(
                sceneState = sceneState,
                experience = experience,
                onPrepare = onPrepare,
                onSubmit = onSubmit,
                onExport = onExport
            )
        } else {
            TeacherWorkspace(
                sceneState = sceneState,
                experience = experience,
                onPrepare = onPrepare,
                onPublish = onPublish,
                onCloseActivity = onCloseActivity,
                onResetProgress = onResetProgress,
                onExport = onExport
            )
        }
    }
}

@Composable
private fun AcademicRoleSelector(selected: AcademicRole, onSelected: (AcademicRole) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.025f), RoundedCornerShape(17.dp))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(17.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        AcademicRole.entries.forEach { role ->
            val active = role == selected
            Surface(
                onClick = { onSelected(role) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                color = if (active) roleColor(role).copy(alpha = 0.15f) else Color.Transparent,
                contentColor = if (active) roleColor(role) else SonicPalette.Muted,
                shape = RoundedCornerShape(13.dp),
                border = BorderStroke(
                    1.dp,
                    if (active) roleColor(role).copy(alpha = 0.45f) else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (role == AcademicRole.STUDENT) "◎" else "✦", fontSize = 15.sp)
                    Spacer(Modifier.width(7.dp))
                    Text(role.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun StudentWorkspace(
    sceneState: AcousticState,
    experience: AcademicExperienceState,
    onPrepare: (String) -> Unit,
    onSubmit: (String, String) -> Unit,
    onExport: () -> Unit
) {
    val assigned = experience.teacherActivity.takeIf { it.active }
    var selectedId by rememberSaveable { mutableStateOf(assigned?.practiceId ?: AcademicPractices.all.first().id) }
    LaunchedEffect(assigned?.practiceId) {
        if (assigned != null) selectedId = assigned.practiceId
    }
    val practice = AcademicPractices.byId(if (assigned != null) assigned.practiceId else selectedId)
    val previousAnswer = experience.answers[practice.id].orEmpty()
    var prediction by rememberSaveable(practice.id) { mutableStateOf("") }
    var answer by rememberSaveable(practice.id, previousAnswer) { mutableStateOf(previousAnswer) }
    var showHint by rememberSaveable(practice.id) { mutableStateOf(false) }
    val completed = practice.id in experience.completedPracticeIds

    ProgressCard(experience)
    if (assigned != null) {
        AcademicNotice(
            title = "ACTIVIDAD ASIGNADA",
            body = "${assigned.practice.number} · ${assigned.practice.title}. ${assigned.locks.count} grupos de parámetros están bloqueados por el docente.",
            accent = SonicPalette.Amber
        )
    } else {
        PracticePicker(selectedId = practice.id, onSelected = { selectedId = it }, enabled = true)
    }
    PracticeOverview(practice)
    OutlinedTextField(
        value = prediction,
        onValueChange = { prediction = it.take(500) },
        label = { Text("Predicción antes de observar") },
        supportingText = { Text("Opcional · formula primero qué esperas que ocurra") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = { onPrepare(practice.id) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SonicPalette.Cyan,
            contentColor = SonicPalette.Void
        ),
        shape = RoundedCornerShape(13.dp)
    ) {
        Text("▶  ${practice.actionLabel}", fontWeight = FontWeight.Bold)
    }
    SceneCaseSummary(sceneState)
    Surface(
        color = SonicPalette.Violet.copy(alpha = 0.08f),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, SonicPalette.Violet.copy(alpha = 0.20f))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PREGUNTA DE CIERRE", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Violet)
            Text(practice.question, style = MaterialTheme.typography.bodyMedium, color = SonicPalette.Ice)
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
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SonicPalette.Green,
                        contentColor = SonicPalette.Void
                    )
                ) {
                    Text(if (completed) "Actualizar entrega" else "Entregar respuesta", fontSize = 10.sp)
                }
                OutlinedButton(
                    onClick = { showHint = !showHint },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 10.dp)
                ) {
                    Text(if (showHint) "Ocultar pista" else "Ver pista", fontSize = 10.sp)
                }
            }
            if (showHint) Text(practice.hint, color = SonicPalette.Amber, fontSize = 10.sp)
        }
    }
    if (completed) {
        AcademicNotice(
            title = "ENTREGA REGISTRADA · CONTRASTE",
            body = "Respuesta esperada: ${assigned?.expectedAnswer ?: practice.expectedAnswer}\n\nCriterio: ${assigned?.rubric ?: practice.defaultRubric}",
            accent = SonicPalette.Green
        )
    }
    OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text("↗  Exportar progreso JSON")
    }
}

@Composable
private fun TeacherWorkspace(
    sceneState: AcousticState,
    experience: AcademicExperienceState,
    onPrepare: (String) -> Unit,
    onPublish: (String, AcademicLocks, String, String) -> Unit,
    onCloseActivity: () -> Unit,
    onResetProgress: () -> Unit,
    onExport: () -> Unit
) {
    val active = experience.teacherActivity
    var selectedId by rememberSaveable { mutableStateOf(active.practiceId) }
    val practice = AcademicPractices.byId(selectedId)
    var lockFrequency by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.locks.frequency else true)
    }
    var lockLevel by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.locks.level else false)
    }
    var lockEnvironment by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.locks.environment else true)
    }
    var lockSources by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.locks.sources else practice.id in setOf("interference", "harmonics"))
    }
    var expectedAnswer by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.expectedAnswer else practice.expectedAnswer)
    }
    var rubric by rememberSaveable(selectedId) {
        mutableStateOf(if (active.practiceId == selectedId) active.rubric else practice.defaultRubric)
    }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    if (active.active) {
        AcademicNotice(
            title = "ACTIVIDAD PUBLICADA LOCALMENTE",
            body = "${active.practice.number} · ${active.practice.title} · ${active.locks.count} bloqueos activos. Funciona sin cuentas ni conexión.",
            accent = SonicPalette.Green
        )
    }
    PracticePicker(selectedId = selectedId, onSelected = { selectedId = it }, enabled = true)
    PracticeOverview(practice)

    AcademicSectionTitle("Parámetros que podrá cambiar el estudiante")
    Text(
        "Al publicar, la práctica prepara la escena y fija una línea base. Los grupos marcados quedan bloqueados; cámara, pausa y calidad GPU permanecen libres.",
        style = MaterialTheme.typography.bodyMedium,
        color = SonicPalette.Muted
    )
    AcademicLockRow("Frecuencia principal", lockFrequency) { lockFrequency = it }
    AcademicLockRow("Nivel simulado a 1 m", lockLevel) { lockLevel = it }
    AcademicLockRow("Temperatura, humedad y presión", lockEnvironment) { lockEnvironment = it }
    AcademicLockRow("Fuentes, fase y armónicos", lockSources) { lockSources = it }

    OutlinedTextField(
        value = expectedAnswer,
        onValueChange = { expectedAnswer = it.take(2_000) },
        label = { Text("Respuesta esperada") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = rubric,
        onValueChange = { rubric = it.take(2_000) },
        label = { Text("Rúbrica o criterio de evaluación") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        OutlinedButton(
            onClick = { onPrepare(practice.id) },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Text("Previsualizar", fontSize = 10.sp)
        }
        Button(
            onClick = {
                onPublish(
                    practice.id,
                    AcademicLocks(lockFrequency, lockLevel, lockEnvironment, lockSources),
                    expectedAnswer,
                    rubric
                )
            },
            enabled = expectedAnswer.isNotBlank() && rubric.isNotBlank(),
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SonicPalette.Violet,
                contentColor = SonicPalette.Ice
            )
        ) {
            Text(if (active.active) "Actualizar actividad" else "Publicar actividad", fontSize = 10.sp)
        }
    }
    SceneCaseSummary(sceneState)
    if (active.active) {
        OutlinedButton(onClick = onCloseActivity, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Cerrar actividad asignada")
        }
    }

    AcademicSectionTitle("Resultados locales")
    Text(
        "${experience.completedCount}/${AcademicPractices.all.size} prácticas entregadas en este dispositivo",
        color = SonicPalette.Cyan,
        fontWeight = FontWeight.SemiBold
    )
    AcademicPractices.all.forEach { item ->
        val response = experience.answers[item.id]
        Surface(
            color = Color.White.copy(alpha = 0.025f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
        ) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("${item.number} · ${item.shortTitle}", fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                Text(
                    response ?: "Sin entrega",
                    color = if (response == null) SonicPalette.Muted else SonicPalette.Green,
                    fontSize = 9.sp,
                    maxLines = 3
                )
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
            Text("Exportar JSON", fontSize = 10.sp)
        }
        if (confirmReset) {
            Button(
                onClick = {
                    onResetProgress()
                    confirmReset = false
                },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Magenta)
            ) {
                Text("Confirmar borrado", fontSize = 10.sp)
            }
        } else {
            OutlinedButton(
                onClick = { confirmReset = true },
                enabled = experience.completedCount > 0,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) {
                Text("Borrar progreso", fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun ProgressCard(experience: AcademicExperienceState) {
    Surface(
        color = SonicPalette.Cyan.copy(alpha = 0.065f),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, SonicPalette.Cyan.copy(alpha = 0.18f))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("PROGRESO LOCAL", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Cyan)
                    Text(
                        "${experience.completedCount} de ${AcademicPractices.all.size} prácticas",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text("${(experience.progress * 100).toInt()} %", color = SonicPalette.Green, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { experience.progress },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = SonicPalette.Green,
                trackColor = SonicPalette.Grid
            )
        }
    }
}

@Composable
private fun PracticePicker(selectedId: String, onSelected: (String) -> Unit, enabled: Boolean) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(AcademicPractices.all, key = { it.id }) { practice ->
            FilterChip(
                selected = selectedId == practice.id,
                onClick = { onSelected(practice.id) },
                enabled = enabled,
                label = { Text("${practice.number} · ${practice.shortTitle}") }
            )
        }
    }
}

@Composable
private fun PracticeOverview(practice: AcademicPractice) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1A29)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SonicPalette.Cyan.copy(alpha = 0.15f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    practice.number.toString(),
                    color = SonicPalette.Void,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(listOf(SonicPalette.Cyan, SonicPalette.Violet)),
                            RoundedCornerShape(9.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text(practice.title, style = MaterialTheme.typography.titleMedium, color = SonicPalette.Cyan)
            }
            Text("OBJETIVO", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Violet)
            Text(practice.objective, style = MaterialTheme.typography.bodyMedium, color = SonicPalette.Ice)
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
private fun AcademicLockRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.025f),
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(1.dp, if (checked) SonicPalette.Amber.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 11.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, modifier = Modifier.weight(1f), fontSize = 10.sp, color = if (checked) SonicPalette.Ice else SonicPalette.Muted)
            Text(if (checked) "BLOQUEADO" else "LIBRE", color = if (checked) SonicPalette.Amber else SonicPalette.Green, fontSize = 8.sp)
            Spacer(Modifier.width(7.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SceneCaseSummary(state: AcousticState) {
    Text(
        "Escena: ${academicHz(state.frequencyHz)} · ${state.levelDbSpl.toInt()} dB SPL simulado a 1 m · ${if (state.secondarySource.enabled) "dos fuentes" else "una fuente"}",
        fontSize = 9.sp,
        color = SonicPalette.Cyan
    )
}

@Composable
private fun AcademicSectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(
            Modifier
                .width(4.dp)
                .height(20.dp)
                .background(
                    Brush.verticalGradient(listOf(SonicPalette.Cyan, SonicPalette.Violet)),
                    RoundedCornerShape(50)
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = SonicPalette.Ice)
    }
}

@Composable
private fun AcademicNotice(title: String, body: String, accent: Color) {
    Surface(
        color = accent.copy(alpha = 0.07f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.23f))
    ) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = accent)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = SonicPalette.Ice)
        }
    }
}

private fun roleColor(role: AcademicRole): Color =
    if (role == AcademicRole.STUDENT) SonicPalette.Cyan else SonicPalette.Violet

private fun academicHz(value: Double): String = if (value >= 1_000.0) {
    if (value % 1_000.0 == 0.0) "${(value / 1_000.0).toInt()} kHz" else "${"%.2f".format(value / 1_000.0)} kHz"
} else {
    "${value.toInt()} Hz"
}
