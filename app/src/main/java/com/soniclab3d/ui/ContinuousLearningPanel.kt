package com.soniclab3d.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soniclab3d.academic.AcademicExperienceState
import com.soniclab3d.academic.AcademicRole
import com.soniclab3d.academic.DoubtStatus
import com.soniclab3d.academic.FaqEntry
import com.soniclab3d.academic.LearningAssignment
import com.soniclab3d.academic.LearningCatalog
import com.soniclab3d.academic.LearningDomain
import com.soniclab3d.academic.LearningGuide
import com.soniclab3d.academic.LearningLevel
import com.soniclab3d.academic.LearningPath
import com.soniclab3d.academic.LearningPractice
import com.soniclab3d.academic.NotebookEntry
import com.soniclab3d.academic.PersonalDoubt
import com.soniclab3d.academic.PracticeDraft
import com.soniclab3d.academic.PracticePhase
import com.soniclab3d.academic.TeacherFeedback
import com.soniclab3d.ui.theme.SonicPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class LearningSection(val label: String) {
    PATHS("Rutas"),
    GUIDES("Guías"),
    PRACTICES("Prácticas"),
    DOUBTS("Dudas"),
    NOTEBOOK("Mi cuaderno"),
    CONTENT("Contenido"),
    ASSIGNMENTS("Asignaciones"),
    SUBMISSIONS("Entregas"),
    TEACHER_DOUBTS("Dudas"),
    PROGRESS("Progreso")
}

@Composable
internal fun ContinuousLearningPanel(
    experience: AcademicExperienceState,
    onRoleChange: (AcademicRole) -> Unit,
    onSelectPath: (LearningPath) -> Unit,
    onGuideCompleted: (String, Boolean) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenPreset: (String) -> Unit,
    onPracticePhase: (String, PracticePhase) -> Unit,
    onSavePracticeDraft: (PracticeDraft) -> Unit,
    onResetPractice: (String) -> Unit,
    onSubmitPractice: (String, String) -> Unit,
    onSaveDoubt: (PersonalDoubt) -> Unit,
    onAnswerDoubt: (String, String) -> Unit,
    onArchiveDoubt: (String) -> Unit,
    onSaveNotebook: (NotebookEntry) -> Unit,
    onPublishAssignment: (LearningAssignment) -> Unit,
    onCloseAssignment: (String) -> Unit,
    onImportExchange: () -> Unit,
    onExportAssignment: (String) -> Unit,
    onExportSubmission: () -> Unit,
    onExportFeedback: (TeacherFeedback) -> Unit,
    hasSceneSnapshot: Boolean,
    onRestoreScene: () -> Unit,
    onExport: () -> Unit
) {
    val studentSections = listOf(
        LearningSection.PATHS,
        LearningSection.GUIDES,
        LearningSection.PRACTICES,
        LearningSection.DOUBTS,
        LearningSection.NOTEBOOK
    )
    val teacherSections = listOf(
        LearningSection.CONTENT,
        LearningSection.ASSIGNMENTS,
        LearningSection.SUBMISSIONS,
        LearningSection.TEACHER_DOUBTS,
        LearningSection.PROGRESS
    )
    val sections = if (experience.role == AcademicRole.STUDENT) studentSections else teacherSections
    var sectionName by rememberSaveable(experience.role) { mutableStateOf(sections.first().name) }
    val section = sections.firstOrNull { it.name == sectionName } ?: sections.first()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LearningHero(experience)
        if (hasSceneSnapshot) {
            OutlinedButton(onClick = onRestoreScene, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("Volver a mi escena anterior")
            }
        }
        RoleSelector(experience.role, onRoleChange)
        OutlinedButton(onClick = onImportExchange, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(if (experience.role == AcademicRole.STUDENT) "Importar paquete o feedback" else "Importar entrega")
        }
        ScrollableChips(
            values = sections,
            selected = section,
            label = { it.label },
            onSelected = { sectionName = it.name }
        )
        when (section) {
            LearningSection.PATHS -> StudentPaths(experience, onSelectPath)
            LearningSection.GUIDES -> StudentGuides(
                experience, onGuideCompleted, onToggleFavorite, onOpenPreset, onSaveDoubt
            )
            LearningSection.PRACTICES -> StudentPractices(
                experience, onOpenPreset, onPracticePhase, onSavePracticeDraft,
                onResetPractice, onSubmitPractice, onSaveDoubt, onExportSubmission
            )
            LearningSection.DOUBTS -> StudentDoubts(experience, onOpenPreset, onSaveDoubt, onArchiveDoubt)
            LearningSection.NOTEBOOK -> StudentNotebook(experience, onSaveNotebook, onExport)
            LearningSection.CONTENT -> TeacherContent(experience, onSelectPath)
            LearningSection.ASSIGNMENTS -> TeacherAssignments(
                experience, onOpenPreset, onPublishAssignment, onCloseAssignment, onExportAssignment
            )
            LearningSection.SUBMISSIONS -> TeacherSubmissions(experience, onExport, onExportFeedback)
            LearningSection.TEACHER_DOUBTS -> TeacherDoubts(experience, onAnswerDoubt, onArchiveDoubt)
            LearningSection.PROGRESS -> TeacherProgress(experience, onExport)
        }
    }
}

@Composable
private fun LearningHero(experience: AcademicExperienceState) {
    val guideCount = experience.learning.completedGuideIds.size
    val practiceCount = experience.totalLearningPracticeCount
    Surface(
        color = SonicPalette.Violet.copy(alpha = 0.075f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SonicPalette.Violet.copy(alpha = 0.24f))
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .background(
                            Brush.linearGradient(listOf(SonicPalette.Violet, SonicPalette.Cyan)),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                ) { Text("∞", color = SonicPalette.Void, fontWeight = FontWeight.Black) }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text("ACADEMIA CONTINUA", style = MaterialTheme.typography.labelSmall, color = SonicPalette.Violet)
                    Text(experience.learning.selectedPath.label, style = MaterialTheme.typography.titleMedium)
                }
                Text("v1.1", color = SonicPalette.Cyan, fontWeight = FontWeight.Bold)
            }
            Text(
                "$guideCount/${LearningCatalog.guides.size} guías · $practiceCount/${LearningCatalog.practices.size} prácticas · ${LearningCatalog.faq.size} respuestas",
                color = SonicPalette.Muted,
                fontSize = 9.sp
            )
            val total = LearningCatalog.guides.size + LearningCatalog.practices.size
            val done = guideCount + practiceCount
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else done.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = SonicPalette.Green,
                trackColor = SonicPalette.Grid
            )
            Text(
                "Contenido local · sin cuentas · las mediciones, simulaciones y visualizaciones están identificadas",
                color = SonicPalette.Muted,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun RoleSelector(role: AcademicRole, onRoleChange: (AcademicRole) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        AcademicRole.entries.forEach { item ->
            FilterChip(
                selected = role == item,
                onClick = { onRoleChange(item) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
private fun StudentPaths(experience: AcademicExperienceState, onSelectPath: (LearningPath) -> Unit) {
    LearningSectionTitle("Elige una ruta")
    Text(
        "Puedes cambiar de ruta cuando quieras. La relación con una carrera es orientativa y no constituye homologación oficial.",
        color = SonicPalette.Muted,
        fontSize = 9.sp
    )
    LearningPath.entries.forEach { path ->
        val guides = LearningCatalog.guides.filter { it.path == path }
        val done = guides.count { it.id in experience.learning.completedGuideIds }
        val selected = experience.learning.selectedPath == path
        Card(
            onClick = { onSelectPath(path) },
            colors = CardDefaults.cardColors(
                containerColor = if (selected) SonicPalette.Violet.copy(alpha = 0.12f) else Color(0xFF0C1A29)
            ),
            border = BorderStroke(1.dp, if (selected) SonicPalette.Violet.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.07f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(path.shortLabel, fontWeight = FontWeight.Bold, color = if (selected) SonicPalette.Violet else SonicPalette.Ice, modifier = Modifier.weight(1f))
                    Text("$done/${guides.size}", color = SonicPalette.Green, fontSize = 10.sp)
                }
                Text(path.description, color = SonicPalette.Ice, fontSize = 10.sp)
                Text(path.careerHint, color = SonicPalette.Muted, fontSize = 8.sp)
                LinearProgressIndicator(
                    progress = { if (guides.isEmpty()) 0f else done.toFloat() / guides.size },
                    modifier = Modifier.fillMaxWidth().height(5.dp),
                    color = SonicPalette.Green,
                    trackColor = SonicPalette.Grid
                )
            }
        }
    }
}

@Composable
private fun StudentGuides(
    experience: AcademicExperienceState,
    onGuideCompleted: (String, Boolean) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenPreset: (String) -> Unit,
    onSaveDoubt: (PersonalDoubt) -> Unit
) {
    val path = experience.learning.selectedPath
    val guides = LearningCatalog.guides.filter { it.path == path }
    var selectedId by rememberSaveable(path.id) { mutableStateOf(guides.firstOrNull()?.id.orEmpty()) }
    val selected = guides.firstOrNull { it.id == selectedId } ?: guides.firstOrNull()
    LearningSectionTitle("Guías · ${path.shortLabel}")
    ScrollableChips(guides, selected, { guide ->
        val done = guide.id in experience.learning.completedGuideIds
        (if (done) "✓ " else "") + guide.title
    }) { selectedId = it.id }
    selected?.let { guide ->
        GuideDetail(
            guide = guide,
            completed = guide.id in experience.learning.completedGuideIds,
            favorite = guide.id in experience.learning.favoriteGuideIds,
            onCompleted = { onGuideCompleted(guide.id, it) },
            onFavorite = { onToggleFavorite(guide.id) },
            onOpenPreset = { onOpenPreset(guide.id) },
            onSaveDoubt = onSaveDoubt
        )
    }
}

@Composable
private fun GuideDetail(
    guide: LearningGuide,
    completed: Boolean,
    favorite: Boolean,
    onCompleted: (Boolean) -> Unit,
    onFavorite: () -> Unit,
    onOpenPreset: () -> Unit,
    onSaveDoubt: (PersonalDoubt) -> Unit
) {
    var revealCheck by rememberSaveable(guide.id) { mutableStateOf(false) }
    var doubtText by rememberSaveable(guide.id) { mutableStateOf("") }
    ContentCard(accent = SonicPalette.Cyan) {
        MetaRow(guide.level.label, guide.domain.label, "${guide.estimatedMinutes} min", "Sem. ${guide.suggestedSemesters}")
        Text(guide.title, style = MaterialTheme.typography.titleLarge, color = SonicPalette.Cyan)
        Text(guide.objective, color = SonicPalette.Ice, fontSize = 11.sp)
        Notice(guide.scientificKind.label, guide.limitation, SonicPalette.Amber)
        Text("IDEA CENTRAL", color = SonicPalette.Violet, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(guide.summary, color = SonicPalette.Ice, fontSize = 10.sp)
        guide.equation?.let {
            Surface(color = Color.Black.copy(alpha = 0.22f), shape = RoundedCornerShape(10.dp)) {
                Text(it, modifier = Modifier.fillMaxWidth().padding(10.dp), color = SonicPalette.Green, fontWeight = FontWeight.Bold)
            }
        }
        Text("Conceptos: ${guide.keyConcepts.joinToString(" · ")}", color = SonicPalette.Muted, fontSize = 9.sp)
        guide.explorationSteps.forEachIndexed { index, step -> StepRow(index + 1, step) }
        if (guide.presetId != null) {
            Button(onClick = onOpenPreset, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("Abrir demostración reproducible")
            }
        }
        Text(guide.checkQuestion, color = SonicPalette.Ice, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
        OutlinedButton(onClick = { revealCheck = !revealCheck }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(if (revealCheck) "Ocultar comprobación" else "Comprobar mi idea")
        }
        if (revealCheck) {
            Notice("RESPUESTA RAZONADA", guide.checkAnswer, SonicPalette.Green)
            Text("Error frecuente: ${guide.commonError}", color = SonicPalette.Amber, fontSize = 9.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Button(
                onClick = { onCompleted(!completed) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (completed) SonicPalette.Green else SonicPalette.Violet)
            ) { Text(if (completed) "✓ Completada" else "Marcar completa", fontSize = 10.sp) }
            OutlinedButton(onClick = onFavorite, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                Text(if (favorite) "★ Favorita" else "☆ Guardar", fontSize = 10.sp)
            }
        }
        OutlinedTextField(
            value = doubtText,
            onValueChange = { doubtText = it.take(1_500) },
            label = { Text("Tengo una duda sobre esta guía") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Button(
            onClick = {
                val now = System.currentTimeMillis()
                onSaveDoubt(
                    PersonalDoubt(
                        id = "d_$now",
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now,
                        contextId = guide.id,
                        contextTitle = guide.title,
                        domain = guide.domain,
                        question = doubtText.trim()
                    )
                )
                doubtText = ""
            },
            enabled = doubtText.isNotBlank(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Cyan)
        ) { Text("Guardar en Mis dudas", color = SonicPalette.Void) }
        Text("Referencias: ${guide.references.joinToString(" · ")}", color = SonicPalette.Muted, fontSize = 8.sp)
    }
}

@Composable
private fun StudentPractices(
    experience: AcademicExperienceState,
    onOpenPreset: (String) -> Unit,
    onPracticePhase: (String, PracticePhase) -> Unit,
    onSavePracticeDraft: (PracticeDraft) -> Unit,
    onResetPractice: (String) -> Unit,
    onSubmitPractice: (String, String) -> Unit,
    onSaveDoubt: (PersonalDoubt) -> Unit,
    onExportSubmission: () -> Unit
) {
    val selectedPath = experience.learning.selectedPath
    val assignmentPractice = experience.learning.activeAssignment?.let { LearningCatalog.practice(it.practiceId) }
    val routePractices = LearningCatalog.practices.filter { it.path == selectedPath }
    val practices = listOfNotNull(assignmentPractice) + routePractices.filterNot { it.id == assignmentPractice?.id }
    var selectedId by rememberSaveable(selectedPath.id, assignmentPractice?.id) {
        mutableStateOf(assignmentPractice?.id ?: practices.firstOrNull()?.id.orEmpty())
    }
    val selected = practices.firstOrNull { it.id == selectedId } ?: practices.firstOrNull()
    LearningSectionTitle("Prácticas guiadas")
    experience.learning.activeAssignment?.let { assignment ->
        Notice("ASIGNACIÓN ACTIVA · ${assignment.courseName}", assignment.instructions, SonicPalette.Amber)
        OutlinedButton(onClick = onExportSubmission, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Exportar entrega o borrador")
        }
    }
    ScrollableChips(practices, selected, { practice ->
        val done = practice.id in experience.completedPracticeIds
        (if (done) "✓ " else "") + "${practice.number} · ${practice.shortTitle}"
    }) { selectedId = it.id }
    selected?.let { practice ->
        PracticeDetail(
            experience, practice, onOpenPreset, onPracticePhase, onSavePracticeDraft,
            onResetPractice, onSubmitPractice, onSaveDoubt
        )
    }
}

@Composable
private fun PracticeDetail(
    experience: AcademicExperienceState,
    practice: LearningPractice,
    onOpenPreset: (String) -> Unit,
    onPracticePhase: (String, PracticePhase) -> Unit,
    onSavePracticeDraft: (PracticeDraft) -> Unit,
    onResetPractice: (String) -> Unit,
    onSubmitPractice: (String, String) -> Unit,
    onSaveDoubt: (PersonalDoubt) -> Unit
) {
    val phase = experience.learning.phaseFor(practice.id)
    val persistedDraft = experience.learning.practiceDrafts[practice.id]
    val attempt = persistedDraft?.attempt ?: 1
    var prediction by rememberSaveable(practice.id, attempt) {
        mutableStateOf(persistedDraft?.prediction.orEmpty())
    }
    var evidence by rememberSaveable(practice.id, attempt) {
        mutableStateOf(persistedDraft?.evidence.orEmpty())
    }
    var answer by rememberSaveable(practice.id, attempt) {
        mutableStateOf(persistedDraft?.analysis ?: experience.answers[practice.id].orEmpty())
    }
    var reflection by rememberSaveable(practice.id, attempt) {
        mutableStateOf(persistedDraft?.reflection.orEmpty())
    }
    var showHint by rememberSaveable(practice.id) { mutableStateOf(false) }
    var doubtText by rememberSaveable(practice.id) { mutableStateOf("") }
    var confirmReset by rememberSaveable(practice.id) { mutableStateOf(false) }
    val phaseProgress = (phase.ordinal + 1).toFloat() / PracticePhase.entries.size
    // Save from the input event so changing tabs cannot cancel a delayed save.
    // Empty values are intentional edits and must replace the previous draft too.
    fun saveDraft() {
        onSavePracticeDraft(
            PracticeDraft(
                practiceId = practice.id,
                updatedAtEpochMs = System.currentTimeMillis(),
                prediction = prediction,
                evidence = evidence,
                analysis = answer,
                reflection = reflection,
                attempt = attempt,
                status = persistedDraft?.status ?: com.soniclab3d.academic.PracticeWorkStatus.DRAFT
            )
        )
    }
    ContentCard(accent = SonicPalette.Violet) {
        MetaRow(practice.level.label, practice.domain.label, "${practice.estimatedMinutes} min", practice.scientificKind.label)
        Text("${practice.number} · ${practice.title}", style = MaterialTheme.typography.titleLarge, color = SonicPalette.Violet)
        experience.learning.feedback.lastOrNull { it.practiceId == practice.id }?.let { feedback ->
            Notice(
                "RETROALIMENTACIÓN IMPORTADA${feedback.scoreText.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()}",
                feedback.comment,
                SonicPalette.Green
            )
        }
        Text(practice.objective, color = SonicPalette.Ice, fontSize = 10.sp)
        LinearProgressIndicator(
            progress = { phaseProgress },
            modifier = Modifier.fillMaxWidth().height(7.dp),
            color = SonicPalette.Green,
            trackColor = SonicPalette.Grid
        )
        Text("ETAPA ${phase.ordinal + 1}/8 · ${phase.label.uppercase()}", color = SonicPalette.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        when (phase) {
            PracticePhase.ORIENT -> {
                Notice("PROPÓSITO", practice.objective, SonicPalette.Cyan)
                Notice("ALCANCE", practice.limitation, SonicPalette.Amber)
            }
            PracticePhase.PREDICT -> OutlinedTextField(
                value = prediction,
                onValueChange = { prediction = it.take(2_000); saveDraft() },
                label = { Text("¿Qué esperas observar y por qué?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            PracticePhase.PREPARE -> {
                practice.steps.forEachIndexed { index, step -> StepRow(index + 1, step) }
                Button(onClick = { onOpenPreset(practice.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Preparar escena")
                }
            }
            PracticePhase.EXPERIMENT -> {
                practice.steps.forEachIndexed { index, step -> StepRow(index + 1, step) }
                Text("Cambia solo las variables indicadas y conserva cámara, calidad y unidades visibles.", color = SonicPalette.Muted, fontSize = 9.sp)
            }
            PracticePhase.RECORD -> OutlinedTextField(
                value = evidence,
                onValueChange = { evidence = it.take(4_000); saveDraft() },
                label = { Text(practice.evidencePrompt) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
            PracticePhase.ANALYZE -> {
                Text(practice.question, color = SonicPalette.Ice, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it.take(4_000); saveDraft() },
                    label = { Text("Análisis") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
                TextButton(onClick = { showHint = !showHint }) { Text(if (showHint) "Ocultar pista" else "Necesito una pista") }
                if (showHint) Notice("PISTA", practice.hint, SonicPalette.Amber)
            }
            PracticePhase.CONCLUDE -> {
                Notice("RÚBRICA", practice.rubric, SonicPalette.Violet)
                Text("Tu respuesta se guarda localmente. La evidencia escrita en esta sesión debe copiarse al cuaderno si deseas conservarla aparte.", color = SonicPalette.Muted, fontSize = 9.sp)
                Button(
                    onClick = { onSubmitPractice(practice.id, answer) },
                    enabled = answer.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Green)
                ) { Text("Entregar respuesta", color = SonicPalette.Void) }
            }
            PracticePhase.REFLECT -> {
                Notice(
                    if (practice.id in experience.completedPracticeIds) "PRÁCTICA ENTREGADA" else "FALTA ENTREGA",
                    "Compara tu predicción con el resultado, identifica una limitación y decide qué investigarías después.",
                    if (practice.id in experience.completedPracticeIds) SonicPalette.Green else SonicPalette.Amber
                )
                OutlinedTextField(
                    value = reflection,
                    onValueChange = { reflection = it.take(4_000); saveDraft() },
                    label = { Text("¿Qué aprendiste y qué investigarías después?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedButton(
                onClick = { onPracticePhase(practice.id, PracticePhase.entries[(phase.ordinal - 1).coerceAtLeast(0)]) },
                enabled = phase.ordinal > 0,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) { Text("Anterior") }
            Button(
                onClick = { onPracticePhase(practice.id, PracticePhase.entries[(phase.ordinal + 1).coerceAtMost(PracticePhase.entries.lastIndex)]) },
                enabled = phase.ordinal < PracticePhase.entries.lastIndex && phaseGateOpen(phase, prediction, evidence, answer),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) { Text("Continuar") }
        }
        OutlinedTextField(
            value = doubtText,
            onValueChange = { doubtText = it.take(1_500) },
            label = { Text("Tengo una duda en ${phase.label}") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        OutlinedButton(
            onClick = {
                val now = System.currentTimeMillis()
                onSaveDoubt(
                    PersonalDoubt(
                        id = "d_$now",
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now,
                        contextId = practice.id,
                        contextTitle = "${practice.title} · ${phase.label}",
                        domain = practice.domain,
                        question = doubtText.trim(),
                        expectation = prediction,
                        observation = evidence
                    )
                )
                doubtText = ""
            },
            enabled = doubtText.isNotBlank(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) { Text("Guardar duda contextual") }
        TextButton(onClick = { confirmReset = true }) { Text("Reiniciar solo esta práctica") }
        if (confirmReset) {
            Notice("CONFIRMACIÓN", "Se borrarán el borrador, la respuesta y el estado de esta práctica. Las demás actividades no cambian.", SonicPalette.Amber)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                OutlinedButton(onClick = { confirmReset = false }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        onResetPractice(practice.id)
                        prediction = ""
                        evidence = ""
                        answer = ""
                        reflection = ""
                        confirmReset = false
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SonicPalette.Amber)
                ) { Text("Reiniciar", color = SonicPalette.Void) }
            }
        }
    }
}

private fun phaseGateOpen(phase: PracticePhase, prediction: String, evidence: String, answer: String): Boolean = when (phase) {
    PracticePhase.PREDICT -> prediction.isNotBlank()
    PracticePhase.RECORD -> evidence.isNotBlank()
    PracticePhase.ANALYZE -> answer.isNotBlank()
    else -> true
}

@Composable
private fun StudentDoubts(
    experience: AcademicExperienceState,
    onOpenPreset: (String) -> Unit,
    onSaveDoubt: (PersonalDoubt) -> Unit,
    onArchiveDoubt: (String) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedFaqId by rememberSaveable { mutableStateOf("") }
    var ownQuestion by rememberSaveable { mutableStateOf("") }
    var expectation by rememberSaveable { mutableStateOf("") }
    var observation by rememberSaveable { mutableStateOf("") }
    val results = remember(search, experience.learning.selectedPath) {
        LearningCatalog.searchFaq(search, if (search.isBlank()) experience.learning.selectedPath else null).take(10)
    }
    LearningSectionTitle("Dudas frecuentes")
    OutlinedTextField(
        value = search,
        onValueChange = { search = it.take(120) },
        label = { Text("Buscar concepto, error o función") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Text("Respuestas revisadas y locales. Si no existe una coincidencia, la app no inventará una respuesta.", color = SonicPalette.Muted, fontSize = 9.sp)
    results.forEach { entry ->
        FaqCard(entry, expanded = selectedFaqId == entry.id, onToggle = {
            selectedFaqId = if (selectedFaqId == entry.id) "" else entry.id
        }, onOpenPreset = onOpenPreset)
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
    LearningSectionTitle("Mis dudas")
    OutlinedTextField(ownQuestion, { ownQuestion = it.take(2_000) }, label = { Text("¿Qué no comprendes todavía?") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
    OutlinedTextField(expectation, { expectation = it.take(2_000) }, label = { Text("¿Qué esperabas observar? (opcional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
    OutlinedTextField(observation, { observation = it.take(2_000) }, label = { Text("¿Qué observaste? (opcional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
    Button(
        onClick = {
            val now = System.currentTimeMillis()
            onSaveDoubt(
                PersonalDoubt(
                    id = "d_$now",
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now,
                    contextId = "free_${experience.learning.selectedPath.id}",
                    contextTitle = experience.learning.selectedPath.label,
                    domain = LearningDomain.MIXED,
                    question = ownQuestion.trim(),
                    expectation = expectation.trim(),
                    observation = observation.trim()
                )
            )
            ownQuestion = ""
            expectation = ""
            observation = ""
        },
        enabled = ownQuestion.isNotBlank(),
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
    ) { Text("Guardar para el docente") }
    experience.learning.personalDoubts.filter { it.status != DoubtStatus.ARCHIVED }.reversed().take(12).forEach { doubt ->
        PersonalDoubtCard(doubt, onArchiveDoubt)
    }
}

@Composable
private fun FaqCard(entry: FaqEntry, expanded: Boolean, onToggle: () -> Unit, onOpenPreset: (String) -> Unit) {
    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1A29)),
        border = BorderStroke(1.dp, if (expanded) SonicPalette.Cyan.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.07f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(entry.question, color = SonicPalette.Ice, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
            Text(entry.shortAnswer, color = SonicPalette.Cyan, fontSize = 9.sp)
            if (expanded) {
                Text(entry.explanation, color = SonicPalette.Ice, fontSize = 10.sp)
                entry.limitation?.let { Notice("LÍMITE", it, SonicPalette.Amber) }
                if (entry.presetId != null) {
                    OutlinedButton(onClick = { onOpenPreset(entry.relatedGuideIds.firstOrNull() ?: entry.relatedPracticeIds.firstOrNull() ?: entry.presetId) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text("Pruébalo en SonicLab")
                    }
                }
                Text("Revisado ${entry.reviewedAt} · ${entry.tags.joinToString(" · ")}", color = SonicPalette.Muted, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun PersonalDoubtCard(doubt: PersonalDoubt, onArchive: (String) -> Unit) {
    Surface(color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(13.dp)) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(doubt.contextTitle, color = SonicPalette.Violet, fontSize = 8.sp, modifier = Modifier.weight(1f))
                Text(doubt.status.label.uppercase(), color = if (doubt.status == DoubtStatus.ANSWERED) SonicPalette.Green else SonicPalette.Amber, fontSize = 8.sp)
            }
            Text(doubt.question, color = SonicPalette.Ice, fontSize = 10.sp)
            if (doubt.teacherAnswer.isNotBlank()) Notice("RESPUESTA DOCENTE", doubt.teacherAnswer, SonicPalette.Green)
            Text(shortDate(doubt.updatedAtEpochMs), color = SonicPalette.Muted, fontSize = 8.sp)
            TextButton(onClick = { onArchive(doubt.id) }) { Text("Archivar") }
        }
    }
}

@Composable
private fun StudentNotebook(
    experience: AcademicExperienceState,
    onSaveNotebook: (NotebookEntry) -> Unit,
    onExport: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var period by rememberSaveable { mutableStateOf("") }
    var hypothesis by rememberSaveable { mutableStateOf("") }
    var observation by rememberSaveable { mutableStateOf("") }
    var conclusion by rememberSaveable { mutableStateOf("") }
    LearningSectionTitle("Cuaderno de laboratorio")
    Text("Conserva hipótesis, observaciones y conclusiones por asignatura o semestre.", color = SonicPalette.Muted, fontSize = 9.sp)
    OutlinedTextField(title, { title = it.take(160) }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    OutlinedTextField(period, { period = it.take(80) }, label = { Text("Periodo o asignatura") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    OutlinedTextField(hypothesis, { hypothesis = it.take(4_000) }, label = { Text("Hipótesis") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
    OutlinedTextField(observation, { observation = it.take(6_000) }, label = { Text("Observaciones y parámetros") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
    OutlinedTextField(conclusion, { conclusion = it.take(4_000) }, label = { Text("Conclusión y límites") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
    Button(
        onClick = {
            val now = System.currentTimeMillis()
            onSaveNotebook(
                NotebookEntry(
                    id = "n_$now",
                    createdAtEpochMs = now,
                    pathId = experience.learning.selectedPath.id,
                    period = period.trim(),
                    contextId = experience.learning.activeAssignment?.practiceId.orEmpty(),
                    title = title.trim(),
                    hypothesis = hypothesis.trim(),
                    observation = observation.trim(),
                    conclusion = conclusion.trim()
                )
            )
            title = ""
            hypothesis = ""
            observation = ""
            conclusion = ""
        },
        enabled = title.isNotBlank() && (hypothesis.isNotBlank() || observation.isNotBlank() || conclusion.isNotBlank()),
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
    ) { Text("Guardar entrada") }
    OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Exportar portafolio académico") }
    experience.learning.notebookEntries.reversed().take(20).forEach { entry ->
        Surface(color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(13.dp)) {
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text(entry.title, color = SonicPalette.Cyan, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(shortDate(entry.createdAtEpochMs), color = SonicPalette.Muted, fontSize = 8.sp)
                }
                if (entry.period.isNotBlank()) Text(entry.period, color = SonicPalette.Violet, fontSize = 8.sp)
                if (entry.hypothesis.isNotBlank()) Text("Hipótesis: ${entry.hypothesis}", color = SonicPalette.Ice, fontSize = 9.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                if (entry.observation.isNotBlank()) Text("Observación: ${entry.observation}", color = SonicPalette.Ice, fontSize = 9.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                if (entry.conclusion.isNotBlank()) Text("Conclusión: ${entry.conclusion}", color = SonicPalette.Green, fontSize = 9.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun TeacherContent(experience: AcademicExperienceState, onSelectPath: (LearningPath) -> Unit) {
    LearningSectionTitle("Catálogo académico")
    Notice(
        "ALINEACIÓN ORIENTATIVA",
        "Paquete UNAP consultado el 15-08-2026. Los planes pueden cambiar y SonicLab no afirma respaldo institucional.",
        SonicPalette.Amber
    )
    ScrollableChips(LearningPath.entries.toList(), experience.learning.selectedPath, { it.shortLabel }) { onSelectPath(it) }
    val path = experience.learning.selectedPath
    val guides = LearningCatalog.guides.filter { it.path == path }
    val practices = LearningCatalog.practices.filter { it.path == path }
    ContentCard(accent = SonicPalette.Violet) {
        Text(path.label, style = MaterialTheme.typography.titleLarge, color = SonicPalette.Violet)
        Text(path.careerHint, color = SonicPalette.Muted, fontSize = 9.sp)
        MetaRow("${guides.size} guías", "${practices.size} prácticas", "3 niveles", "Offline")
        LearningLevel.entries.forEach { level ->
            val levelGuides = guides.filter { it.level == level }
            Text("${level.label.uppercase()} · ${levelGuides.size}", color = SonicPalette.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            levelGuides.forEach { guide ->
                Text("• ${guide.title} · ${guide.estimatedMinutes} min", color = SonicPalette.Ice, fontSize = 9.sp)
            }
        }
    }
    Notice("VALIDACIÓN DE CONTENIDO", if (LearningCatalog.validationErrors().isEmpty()) "Catálogo íntegro: IDs, recuentos y referencias internas coherentes." else LearningCatalog.validationErrors().joinToString(), SonicPalette.Green)
}

@Composable
private fun TeacherAssignments(
    experience: AcademicExperienceState,
    onOpenPreset: (String) -> Unit,
    onPublish: (LearningAssignment) -> Unit,
    onClose: (String) -> Unit,
    onExportAssignment: (String) -> Unit
) {
    var selectedId by rememberSaveable { mutableStateOf(experience.learning.activeAssignment?.practiceId ?: LearningCatalog.practices.first().id) }
    val practice = LearningCatalog.practice(selectedId) ?: LearningCatalog.practices.first()
    var course by rememberSaveable { mutableStateOf(experience.learning.activeAssignment?.courseName ?: "Laboratorio de acústica") }
    var period by rememberSaveable { mutableStateOf(experience.learning.activeAssignment?.period ?: "2026") }
    var instructions by rememberSaveable(practice.id) { mutableStateOf("Completa la práctica, adjunta la evidencia solicitada y registra tus dudas.") }
    var rubric by rememberSaveable(practice.id) { mutableStateOf(practice.rubric) }
    LearningSectionTitle("Crear asignación local")
    ScrollableChips(LearningCatalog.practices, practice, { "${it.number} · ${it.shortTitle}" }) { selectedId = it.id }
    ContentCard(accent = SonicPalette.Violet) {
        Text(practice.title, style = MaterialTheme.typography.titleMedium, color = SonicPalette.Violet)
        Text(practice.objective, color = SonicPalette.Ice, fontSize = 10.sp)
        MetaRow(practice.path.shortLabel, practice.level.label, practice.domain.label, "${practice.estimatedMinutes} min")
        Notice("CLAVE DOCENTE · NO SE EXPORTA EN EL PACK", practice.expectedAnswer, SonicPalette.Amber)
        OutlinedButton(onClick = { onOpenPreset(practice.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Previsualizar escena") }
        OutlinedTextField(course, { course = it.take(120) }, label = { Text("Curso") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(period, { period = it.take(80) }, label = { Text("Periodo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(instructions, { instructions = it.take(3_000) }, label = { Text("Instrucciones") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        OutlinedTextField(rubric, { rubric = it.take(4_000) }, label = { Text("Rúbrica") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
        Button(
            onClick = {
                val now = System.currentTimeMillis()
                onPublish(
                    LearningAssignment(
                        id = "a_$now",
                        createdAtEpochMs = now,
                        courseName = course.trim(),
                        period = period.trim(),
                        practiceId = practice.id,
                        instructions = instructions.trim(),
                        rubric = rubric.trim()
                    )
                )
            },
            enabled = course.isNotBlank() && rubric.isNotBlank(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) { Text("Publicar en este dispositivo") }
    }
    experience.learning.activeAssignment?.let { active ->
        Notice("ASIGNACIÓN ACTIVA · ${active.courseName}", LearningCatalog.contentTitle(active.practiceId), SonicPalette.Green)
        Button(onClick = { onExportAssignment(active.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Exportar paquete para estudiante")
        }
        OutlinedButton(onClick = { onClose(active.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Cerrar asignación") }
    }
    Text("Esta versión trabaja con datos locales. No representa un aula sincronizada en tiempo real.", color = SonicPalette.Muted, fontSize = 9.sp)
}

@Composable
private fun TeacherSubmissions(
    experience: AcademicExperienceState,
    onExport: () -> Unit,
    onExportFeedback: (TeacherFeedback) -> Unit
) {
    LearningSectionTitle("Entregas importadas")
    val imported = experience.learning.importedSubmissions
    var selectedId by rememberSaveable(imported.size) { mutableStateOf(imported.lastOrNull()?.id.orEmpty()) }
    val selected = imported.firstOrNull { it.id == selectedId } ?: imported.lastOrNull()
    var score by rememberSaveable(selectedId) { mutableStateOf("") }
    var comment by rememberSaveable(selectedId) { mutableStateOf("") }
    Notice(
        "TABLERO LOCAL",
        "${imported.size} archivos importados. No representa actividad remota ni en tiempo real.",
        SonicPalette.Cyan
    )
    if (imported.isEmpty()) {
        Text("Usa Importar entrega para cargar un archivo .soniclabsubmission.json.", color = SonicPalette.Muted, fontSize = 10.sp)
    }
    ScrollableChips(imported, selected, { submission ->
        LearningCatalog.practice(submission.practiceId)?.shortTitle ?: submission.practiceId
    }) { selectedId = it.id }
    selected?.let { submission ->
        val practice = LearningCatalog.practice(submission.practiceId)
        ContentCard(accent = SonicPalette.Cyan) {
            MetaRow(submission.sourceLabel, shortDate(submission.importedAtEpochMs), submission.assignmentId)
            Text(practice?.title ?: submission.practiceId, color = SonicPalette.Cyan, fontWeight = FontWeight.Bold)
            if (submission.prediction.isNotBlank()) Text("Predicción: ${submission.prediction}", color = SonicPalette.Ice, fontSize = 9.sp)
            if (submission.evidence.isNotBlank()) Text("Evidencia: ${submission.evidence}", color = SonicPalette.Ice, fontSize = 9.sp)
            Text("Análisis: ${submission.answer}", color = SonicPalette.Ice, fontSize = 10.sp)
            if (submission.reflection.isNotBlank()) Text("Reflexión: ${submission.reflection}", color = SonicPalette.Green, fontSize = 9.sp)
            submission.doubtNotes.forEach { Text("Duda: $it", color = SonicPalette.Amber, fontSize = 9.sp) }
            practice?.let { Notice("RÚBRICA INCORPORADA", it.rubric, SonicPalette.Violet) }
            OutlinedTextField(score, { score = it.take(120) }, label = { Text("Puntaje o estado") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(comment, { comment = it.take(4_000) }, label = { Text("Retroalimentación global") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    onExportFeedback(
                        TeacherFeedback(
                            id = "feedback_$now",
                            createdAtEpochMs = now,
                            submissionId = submission.id,
                            practiceId = submission.practiceId,
                            scoreText = score.trim(),
                            comment = comment.trim()
                        )
                    )
                },
                enabled = comment.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text("Guardar y exportar feedback") }
        }
    }

    LearningSectionTitle("Trabajo de este dispositivo")
    val answered = LearningCatalog.practices.filter { experience.answers[it.id]?.isNotBlank() == true }
    Notice("RESUMEN", "${answered.size} prácticas con respuesta local.", SonicPalette.Violet)
    answered.reversed().forEach { practice ->
        Surface(color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(13.dp)) {
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("${practice.number} · ${practice.title}", color = SonicPalette.Cyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Text(experience.answers[practice.id].orEmpty(), color = SonicPalette.Ice, fontSize = 9.sp)
            }
        }
    }
    OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Exportar progreso local") }
}

@Composable
private fun TeacherDoubts(
    experience: AcademicExperienceState,
    onAnswer: (String, String) -> Unit,
    onArchive: (String) -> Unit
) {
    val pending = experience.learning.personalDoubts.filter { it.status != DoubtStatus.ARCHIVED }
    var selectedId by rememberSaveable(pending.size) { mutableStateOf(pending.firstOrNull { it.status != DoubtStatus.ANSWERED }?.id ?: pending.firstOrNull()?.id.orEmpty()) }
    val selected = pending.firstOrNull { it.id == selectedId }
    var answer by rememberSaveable(selectedId) { mutableStateOf(selected?.teacherAnswer.orEmpty()) }
    LearningSectionTitle("Bandeja de dudas")
    Notice("LOCAL", "${pending.count { it.status != DoubtStatus.ANSWERED }} pendientes · ${pending.count { it.status == DoubtStatus.ANSWERED }} respondidas", SonicPalette.Cyan)
    if (pending.isEmpty()) Text("No hay dudas guardadas en este dispositivo.", color = SonicPalette.Muted)
    ScrollableChips(pending, selected, { "${it.status.label} · ${it.contextTitle}" }) { selectedId = it.id }
    selected?.let { doubt ->
        ContentCard(accent = SonicPalette.Violet) {
            MetaRow(doubt.status.label, doubt.domain.label, shortDate(doubt.createdAtEpochMs), doubt.contextTitle)
            Text(doubt.question, color = SonicPalette.Ice, fontWeight = FontWeight.SemiBold)
            if (doubt.expectation.isNotBlank()) Text("Esperaba: ${doubt.expectation}", color = SonicPalette.Muted, fontSize = 9.sp)
            if (doubt.observation.isNotBlank()) Text("Observó: ${doubt.observation}", color = SonicPalette.Muted, fontSize = 9.sp)
            OutlinedTextField(answer, { answer = it.take(4_000) }, label = { Text("Respuesta docente") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
            Button(
                onClick = { onAnswer(doubt.id, answer) },
                enabled = answer.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text("Guardar respuesta") }
            OutlinedButton(onClick = { onArchive(doubt.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Archivar") }
        }
    }
}

@Composable
private fun TeacherProgress(experience: AcademicExperienceState, onExport: () -> Unit) {
    LearningSectionTitle("Progreso del dispositivo")
    Notice(
        "ALCANCE",
        "Este resumen incluye solamente el trabajo local o importado. No afirma sincronización con estudiantes remotos.",
        SonicPalette.Amber
    )
    LearningPath.entries.forEach { path ->
        val guides = LearningCatalog.guides.filter { it.path == path }
        val practices = LearningCatalog.practices.filter { it.path == path }
        val doneGuides = guides.count { it.id in experience.learning.completedGuideIds }
        val donePractices = practices.count { it.id in experience.completedPracticeIds }
        Surface(color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(13.dp)) {
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(path.shortLabel, color = SonicPalette.Cyan, fontWeight = FontWeight.Bold)
                Text("Guías $doneGuides/${guides.size} · Prácticas $donePractices/${practices.size}", color = SonicPalette.Muted, fontSize = 9.sp)
                val total = guides.size + practices.size
                LinearProgressIndicator(
                    progress = { if (total == 0) 0f else (doneGuides + donePractices).toFloat() / total },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = SonicPalette.Green,
                    trackColor = SonicPalette.Grid
                )
            }
        }
    }
    Notice(
        "PORTAFOLIO",
        "${experience.learning.notebookEntries.size} entradas · ${experience.learning.personalDoubts.size} dudas · ${experience.learning.assignments.size} asignaciones · ${experience.learning.importedSubmissions.size} entregas importadas · ${experience.learning.feedback.size} feedback",
        SonicPalette.Violet
    )
    Button(onClick = onExport, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Exportar informe académico") }
}

@Composable
private fun LearningSectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(
            Modifier.width(4.dp).height(21.dp).background(
                Brush.verticalGradient(listOf(SonicPalette.Cyan, SonicPalette.Violet)),
                RoundedCornerShape(50)
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = SonicPalette.Ice)
    }
}

@Composable
private fun ContentCard(accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1A29)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
    }
}

@Composable
private fun Notice(title: String, body: String, accent: Color) {
    Surface(
        color = accent.copy(alpha = 0.07f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
    ) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = accent, fontWeight = FontWeight.Bold, fontSize = 8.sp)
            Text(body, color = SonicPalette.Ice, fontSize = 9.sp)
        }
    }
}

@Composable
private fun MetaRow(vararg values: String) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        values.forEach { value ->
            Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(50)) {
                Text(value, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = SonicPalette.Muted, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun StepRow(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(color = SonicPalette.Green.copy(alpha = 0.13f), shape = RoundedCornerShape(8.dp)) {
            Text(number.toString(), modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), color = SonicPalette.Green, fontWeight = FontWeight.Bold, fontSize = 9.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text(text, color = SonicPalette.Ice, fontSize = 10.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun <T> ScrollableChips(
    values: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelected: (T) -> Unit
) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        values.forEach { value ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelected(value) },
                label = { Text(label(value), maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}

private fun shortDate(epochMs: Long): String = runCatching {
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(epochMs))
}.getOrDefault("Fecha local")
