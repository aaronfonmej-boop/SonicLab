package com.soniclab3d.academic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningPersistenceCodecsTest {
    @Test
    fun practiceDraftRoundTripsEveryAutosavedPhaseField() {
        val input = mapOf(
            "phase_map" to PracticeDraft(
                practiceId = "phase_map",
                updatedAtEpochMs = 9L,
                prediction = "Habrá cancelación.",
                evidence = "Sonda A: 0,03 a.u.",
                analysis = "La fase y el camino cambian la suma.",
                reflection = "Repetiría con π/2.",
                attempt = 2,
                status = PracticeWorkStatus.SUBMITTED
            )
        )

        assertEquals(
            input,
            LearningPersistenceCodecs.decodePracticeDrafts(
                LearningPersistenceCodecs.encodePracticeDrafts(input)
            )
        )
    }

    @Test
    fun personalDoubtsRoundTripUnicodeAndMultilineText() {
        val input = listOf(
            PersonalDoubt(
                id = "d_1",
                createdAtEpochMs = 10L,
                updatedAtEpochMs = 20L,
                contextId = "g02_frequency_wavelength",
                contextTitle = "Frecuencia y λ",
                domain = LearningDomain.AIR,
                question = "¿Por qué cambia λ?\nNo comprendo c/f.",
                expectation = "Esperaba que no cambiara.",
                observation = "La separación aumentó.",
                status = DoubtStatus.ANSWERED,
                teacherAnswer = "Revisa qué variable mantuviste fija."
            )
        )

        assertEquals(input, LearningPersistenceCodecs.decodeDoubts(LearningPersistenceCodecs.encodeDoubts(input)))
    }

    @Test
    fun notebookAndAssignmentsRoundTrip() {
        val notebook = listOf(
            NotebookEntry(
                id = "n_1",
                createdAtEpochMs = 30L,
                pathId = LearningPath.METHODS_DATA.id,
                period = "Física II · 2026",
                contextId = "reproducible_sweep",
                title = "Barrido",
                hypothesis = "Aparecerá un máximo.",
                observation = "Máximo cerca de f₀.",
                conclusion = "Compatible con resonancia amortiguada."
            )
        )
        val assignments = listOf(
            LearningAssignment(
                id = "a_1",
                createdAtEpochMs = 40L,
                courseName = "Laboratorio",
                period = "2026-2",
                practiceId = "reproducible_sweep",
                instructions = "Registra datos y límites.",
                rubric = "6 puntos",
                active = true
            )
        )

        assertEquals(notebook, LearningPersistenceCodecs.decodeNotebook(LearningPersistenceCodecs.encodeNotebook(notebook)))
        assertEquals(assignments, LearningPersistenceCodecs.decodeAssignments(LearningPersistenceCodecs.encodeAssignments(assignments)))
        assertTrue(LearningPersistenceCodecs.decodeAssignments("corrupto").isEmpty())
    }

    @Test
    fun importedSubmissionAndFeedbackRoundTripWithoutSolutions() {
        val submission = ImportedSubmission(
            id = "submission_1",
            importedAtEpochMs = 50L,
            assignmentId = "a_1",
            practiceId = "temperature_wavelength",
            answer = "λ aumenta si c aumenta.",
            prediction = "Aumentará.",
            evidence = "0 °C y 35 °C registrados.",
            reflection = "Controlaría humedad.",
            doubtNotes = listOf("¿Cuánto influye la humedad?"),
            sourceLabel = "Archivo importado"
        )
        val feedback = TeacherFeedback(
            id = "feedback_1",
            createdAtEpochMs = 60L,
            submissionId = submission.id,
            practiceId = submission.practiceId,
            scoreText = "5/6",
            comment = "Buena evidencia; explicita el supuesto del modelo."
        )

        assertEquals(listOf(submission), LearningPersistenceCodecs.decodeSubmissions(LearningPersistenceCodecs.encodeSubmissions(listOf(submission))))
        assertEquals(listOf(feedback), LearningPersistenceCodecs.decodeFeedback(LearningPersistenceCodecs.encodeFeedback(listOf(feedback))))
    }
}
