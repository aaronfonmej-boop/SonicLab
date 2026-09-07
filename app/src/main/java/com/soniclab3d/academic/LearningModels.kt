package com.soniclab3d.academic

/** Curriculum routes are deliberately independent from a single institution. */
enum class LearningPath(
    val id: String,
    val label: String,
    val shortLabel: String,
    val description: String,
    val careerHint: String
) {
    FUNDAMENTALS(
        "fundamentals",
        "Fundamentos de ondas y sonido",
        "Fundamentos",
        "Oscilación, propagación, interferencia, ondas estacionarias y resonancia.",
        "Pedagogía en Matemática y Física, ingenierías, Fonoaudiología y carreras técnicas."
    ),
    SIGNALS(
        "signals",
        "Señales, electrónica y computación",
        "Señales",
        "Tiempo, frecuencia, armónicos, espectro, muestreo, sensores y exportación.",
        "Ingeniería Civil Electrónica, Computación e Informática y Electricidad/Electrónica Industrial."
    ),
    VOICE_HEARING(
        "voice_hearing",
        "Voz, audición y comunicación",
        "Voz y audición",
        "Acústica de la voz, micrófonos, niveles y lectura no diagnóstica de espectros.",
        "Fonoaudiología; apoyo acotado para Kinesiología y educación."
    ),
    ROOMS_NOISE(
        "rooms_noise",
        "Acústica arquitectónica, ambiental y ocupacional",
        "Recintos y ruido",
        "Modos de recinto, absorción, reverberación, confort y límites de medición.",
        "Arquitectura, Ingeniería Civil Ambiental, Prevención de Riesgos e ingenierías industriales."
    ),
    VIBRATIONS(
        "vibrations",
        "Vibraciones, resonancia y cimática",
        "Vibraciones",
        "Modos de placa, bordes, acoplamiento, amortiguamiento y arena nodal cualitativa.",
        "Pedagogía en Física, Electrónica, estructuras, arquitectura y artes tecnológicas."
    ),
    METHODS_DATA(
        "methods_data",
        "Método experimental y datos",
        "Método y datos",
        "Hipótesis, control de variables, incertidumbre, gráficos, evidencia e informes.",
        "Ruta transversal para laboratorios, proyectos y trabajos de titulación."
    );

    companion object {
        fun fromId(id: String?): LearningPath = entries.firstOrNull { it.id == id } ?: FUNDAMENTALS
    }
}

enum class LearningLevel(val label: String, val description: String) {
    FOUNDATIONS("Fundamentos", "Primer contacto y relaciones físicas esenciales."),
    APPLICATION("Aplicación", "Uso de conceptos para interpretar y comparar casos."),
    PROJECT("Proyecto", "Diseño, evidencia, evaluación de límites y comunicación de resultados.")
}

enum class LearningDomain(val label: String) {
    AIR("Aire"),
    PLATE("Placa"),
    DATA("Datos"),
    MIXED("Aire + Placa")
}

enum class ScientificKind(val label: String) {
    SIMULATION("Simulación"),
    MEASUREMENT("Medición"),
    VISUALIZATION("Visualización"),
    EDUCATIONAL("Modelo educativo"),
    MIXED("Mixto")
}

enum class EditorialStatus { DRAFT, REVIEWED, PUBLISHED }

enum class AlignmentRelevance(val label: String) {
    DIRECT("Directa"),
    COMPLEMENTARY("Complementaria"),
    EXPLORATORY("Exploratoria")
}

data class CareerAlignment(
    val careerId: String,
    val careerName: String,
    val relevance: AlignmentRelevance,
    val relatedSubjects: List<String>,
    val rationale: String
)

data class LearningGuide(
    val id: String,
    val title: String,
    val path: LearningPath,
    val level: LearningLevel,
    val domain: LearningDomain,
    val suggestedSemesters: String,
    val estimatedMinutes: Int,
    val objective: String,
    val summary: String,
    val keyConcepts: List<String>,
    val equation: String?,
    val explorationSteps: List<String>,
    val checkQuestion: String,
    val checkAnswer: String,
    val commonError: String,
    val presetId: String?,
    val prerequisiteIds: List<String> = emptyList(),
    val scientificKind: ScientificKind = ScientificKind.SIMULATION,
    val limitation: String,
    val references: List<String>,
    val schemaVersion: Int = 3,
    val contentVersion: String = "1.1.0",
    val language: String = "es",
    val minimumAppVersion: String = "1.1.0-academia-continua",
    val editorialStatus: EditorialStatus = EditorialStatus.PUBLISHED,
    val reviewedAt: String = "2026-08-15"
)

data class LearningPractice(
    val id: String,
    val number: Int,
    val title: String,
    val shortTitle: String,
    val path: LearningPath,
    val level: LearningLevel,
    val domain: LearningDomain,
    val estimatedMinutes: Int,
    val objective: String,
    val steps: List<String>,
    val question: String,
    val expectedAnswer: String,
    val hint: String,
    val rubric: String,
    val presetId: String,
    val evidencePrompt: String,
    val scientificKind: ScientificKind,
    val limitation: String,
    val inheritedFromV1: Boolean = false,
    val schemaVersion: Int = 3,
    val contentVersion: String = "1.1.0",
    val language: String = "es",
    val minimumAppVersion: String = "1.1.0-academia-continua",
    val editorialStatus: EditorialStatus = EditorialStatus.PUBLISHED,
    val reviewedAt: String = "2026-08-15"
)

enum class PracticePhase(val label: String) {
    ORIENT("Orientar"),
    PREDICT("Predecir"),
    PREPARE("Preparar"),
    EXPERIMENT("Experimentar"),
    RECORD("Registrar"),
    ANALYZE("Analizar"),
    CONCLUDE("Concluir"),
    REFLECT("Reflexionar")
}

data class FaqEntry(
    val id: String,
    val question: String,
    val shortAnswer: String,
    val explanation: String,
    val path: LearningPath,
    val tags: Set<String>,
    val relatedGuideIds: List<String> = emptyList(),
    val relatedPracticeIds: List<String> = emptyList(),
    val presetId: String? = null,
    val limitation: String? = null,
    val reviewedAt: String = "2026-08-15",
    val schemaVersion: Int = 3,
    val contentVersion: String = "1.1.0",
    val language: String = "es",
    val minimumAppVersion: String = "1.1.0-academia-continua",
    val editorialStatus: EditorialStatus = EditorialStatus.PUBLISHED
)

enum class PracticeWorkStatus(val label: String) {
    DRAFT("Borrador"),
    SUBMITTED("Entregada"),
    REVIEWED("Revisada"),
    COMPLETED("Completada")
}

data class PracticeDraft(
    val practiceId: String,
    val updatedAtEpochMs: Long,
    val prediction: String = "",
    val evidence: String = "",
    val analysis: String = "",
    val reflection: String = "",
    val attempt: Int = 1,
    val status: PracticeWorkStatus = PracticeWorkStatus.DRAFT
)

enum class DoubtStatus(val label: String) {
    DRAFT("Borrador"),
    READY("Pendiente"),
    ANSWERED("Respondida"),
    ARCHIVED("Archivada")
}

data class PersonalDoubt(
    val id: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val contextId: String,
    val contextTitle: String,
    val domain: LearningDomain,
    val question: String,
    val expectation: String = "",
    val observation: String = "",
    val status: DoubtStatus = DoubtStatus.READY,
    val teacherAnswer: String = ""
)

data class NotebookEntry(
    val id: String,
    val createdAtEpochMs: Long,
    val pathId: String,
    val period: String,
    val contextId: String,
    val title: String,
    val hypothesis: String,
    val observation: String,
    val conclusion: String
)

data class LearningAssignment(
    val id: String,
    val createdAtEpochMs: Long,
    val courseName: String,
    val period: String,
    val practiceId: String,
    val instructions: String,
    val rubric: String,
    val active: Boolean = true
)

data class ImportedSubmission(
    val id: String,
    val importedAtEpochMs: Long,
    val assignmentId: String,
    val practiceId: String,
    val answer: String,
    val prediction: String,
    val evidence: String,
    val reflection: String,
    val doubtNotes: List<String>,
    val sourceLabel: String = "Archivo importado"
)

data class TeacherFeedback(
    val id: String,
    val createdAtEpochMs: Long,
    val submissionId: String,
    val practiceId: String,
    val scoreText: String,
    val comment: String
)

data class LearningExperienceState(
    val selectedPathId: String = LearningPath.FUNDAMENTALS.id,
    val completedGuideIds: Set<String> = emptySet(),
    val favoriteGuideIds: Set<String> = emptySet(),
    val practicePhaseIndexes: Map<String, Int> = emptyMap(),
    val practiceDrafts: Map<String, PracticeDraft> = emptyMap(),
    val personalDoubts: List<PersonalDoubt> = emptyList(),
    val notebookEntries: List<NotebookEntry> = emptyList(),
    val assignments: List<LearningAssignment> = emptyList(),
    val importedSubmissions: List<ImportedSubmission> = emptyList(),
    val feedback: List<TeacherFeedback> = emptyList()
) {
    val selectedPath: LearningPath get() = LearningPath.fromId(selectedPathId)
    val activeAssignment: LearningAssignment? get() = assignments.lastOrNull { it.active }

    fun phaseFor(practiceId: String): PracticePhase {
        val index = practicePhaseIndexes[practiceId] ?: 0
        return PracticePhase.entries[index.coerceIn(0, PracticePhase.entries.lastIndex)]
    }
}
