package com.soniclab3d.academic

import java.nio.charset.StandardCharsets
import java.util.Base64

internal object LearningPersistenceCodecs {
    private const val MAX_RECORDS = 300
    private const val MAX_TEXT = 8_000

    fun encodePracticeDrafts(values: Map<String, PracticeDraft>): String = values.values
        .filter { it.practiceId in LearningCatalog.practiceIds }
        .sortedBy { it.practiceId }
        .take(MAX_RECORDS)
        .joinToString("\n") { value ->
            listOf(
                "p1", e(value.practiceId), value.updatedAtEpochMs,
                e(value.prediction), e(value.evidence), e(value.analysis), e(value.reflection),
                value.attempt.coerceIn(1, 999), value.status.name
            ).joinToString("\t")
        }

    fun decodePracticeDrafts(serialized: String): Map<String, PracticeDraft> = serialized.lineSequence()
        .take(MAX_RECORDS)
        .mapNotNull { line ->
            runCatching {
                val f = line.split('\t')
                require(f.size == 9 && f[0] == "p1")
                PracticeDraft(
                    practiceId = d(f[1]),
                    updatedAtEpochMs = f[2].toLong(),
                    prediction = d(f[3]).safeText(),
                    evidence = d(f[4]).safeText(),
                    analysis = d(f[5]).safeText(),
                    reflection = d(f[6]).safeText(),
                    attempt = f[7].toInt().coerceIn(1, 999),
                    status = PracticeWorkStatus.valueOf(f[8])
                )
            }.getOrNull()
        }
        .filter { it.practiceId in LearningCatalog.practiceIds }
        .associateBy { it.practiceId }

    fun encodeDoubts(values: List<PersonalDoubt>): String = values
        .sortedBy { it.createdAtEpochMs }
        .takeLast(MAX_RECORDS)
        .joinToString("\n") { value ->
            listOf(
                "d1", e(value.id), value.createdAtEpochMs, value.updatedAtEpochMs,
                e(value.contextId), e(value.contextTitle), value.domain.name,
                e(value.question), e(value.expectation), e(value.observation), value.status.name,
                e(value.teacherAnswer)
            ).joinToString("\t")
        }

    fun decodeDoubts(serialized: String): List<PersonalDoubt> = serialized.lineSequence()
        .take(MAX_RECORDS)
        .mapNotNull { line ->
            runCatching {
                val f = line.split('\t')
                require(f.size == 12 && f[0] == "d1")
                PersonalDoubt(
                    id = d(f[1]),
                    createdAtEpochMs = f[2].toLong(),
                    updatedAtEpochMs = f[3].toLong(),
                    contextId = d(f[4]),
                    contextTitle = d(f[5]),
                    domain = LearningDomain.valueOf(f[6]),
                    question = d(f[7]).safeText(),
                    expectation = d(f[8]).safeText(),
                    observation = d(f[9]).safeText(),
                    status = DoubtStatus.valueOf(f[10]),
                    teacherAnswer = d(f[11]).safeText()
                )
            }.getOrNull()
        }
        .filter { it.id.isNotBlank() && it.question.isNotBlank() }
        .distinctBy { it.id }
        .toList()

    fun encodeNotebook(values: List<NotebookEntry>): String = values
        .sortedBy { it.createdAtEpochMs }
        .takeLast(MAX_RECORDS)
        .joinToString("\n") { value ->
            listOf(
                "n1", e(value.id), value.createdAtEpochMs, e(value.pathId), e(value.period),
                e(value.contextId), e(value.title), e(value.hypothesis), e(value.observation),
                e(value.conclusion)
            ).joinToString("\t")
        }

    fun decodeNotebook(serialized: String): List<NotebookEntry> = serialized.lineSequence()
        .take(MAX_RECORDS)
        .mapNotNull { line ->
            runCatching {
                val f = line.split('\t')
                require(f.size == 10 && f[0] == "n1")
                NotebookEntry(
                    id = d(f[1]),
                    createdAtEpochMs = f[2].toLong(),
                    pathId = d(f[3]),
                    period = d(f[4]).safeText(),
                    contextId = d(f[5]),
                    title = d(f[6]).safeText(),
                    hypothesis = d(f[7]).safeText(),
                    observation = d(f[8]).safeText(),
                    conclusion = d(f[9]).safeText()
                )
            }.getOrNull()
        }
        .filter { it.id.isNotBlank() && it.title.isNotBlank() }
        .distinctBy { it.id }
        .toList()

    fun encodeAssignments(values: List<LearningAssignment>): String = values
        .sortedBy { it.createdAtEpochMs }
        .takeLast(100)
        .joinToString("\n") { value ->
            listOf(
                "a1", e(value.id), value.createdAtEpochMs, e(value.courseName), e(value.period),
                e(value.practiceId), e(value.instructions), e(value.rubric), value.active
            ).joinToString("\t")
        }

    fun decodeAssignments(serialized: String): List<LearningAssignment> = serialized.lineSequence()
        .take(100)
        .mapNotNull { line ->
            runCatching {
                val f = line.split('\t')
                require(f.size == 9 && f[0] == "a1")
                LearningAssignment(
                    id = d(f[1]),
                    createdAtEpochMs = f[2].toLong(),
                    courseName = d(f[3]).safeText(),
                    period = d(f[4]).safeText(),
                    practiceId = d(f[5]),
                    instructions = d(f[6]).safeText(),
                    rubric = d(f[7]).safeText(),
                    active = f[8].toBooleanStrict()
                )
            }.getOrNull()
        }
        .filter { it.id.isNotBlank() && it.practiceId in LearningCatalog.practiceIds }
        .distinctBy { it.id }
        .toList()

    fun encodeSubmissions(values: List<ImportedSubmission>): String = values
        .sortedBy { it.importedAtEpochMs }
        .takeLast(200)
        .joinToString("\n") { value ->
            listOf(
                "s1", e(value.id), value.importedAtEpochMs, e(value.assignmentId),
                e(value.practiceId), e(value.answer), e(value.prediction), e(value.evidence),
                e(value.reflection), e(value.doubtNotes.take(50).joinToString("\u001e")),
                e(value.sourceLabel)
            ).joinToString("\t")
        }

    fun decodeSubmissions(serialized: String): List<ImportedSubmission> = serialized.lineSequence()
        .take(200)
        .mapNotNull { line ->
            runCatching {
                val f = line.split('\t')
                require(f.size == 11 && f[0] == "s1")
                ImportedSubmission(
                    id = d(f[1]),
                    importedAtEpochMs = f[2].toLong(),
                    assignmentId = d(f[3]),
                    practiceId = d(f[4]),
                    answer = d(f[5]).safeText(),
                    prediction = d(f[6]).safeText(),
                    evidence = d(f[7]).safeText(),
                    reflection = d(f[8]).safeText(),
                    doubtNotes = d(f[9]).split('\u001e').filter { it.isNotBlank() }.take(50),
                    sourceLabel = d(f[10]).safeText()
                )
            }.getOrNull()
        }
        .filter { it.id.isNotBlank() && it.practiceId in LearningCatalog.practiceIds }
        .distinctBy { it.id }
        .toList()

    fun encodeFeedback(values: List<TeacherFeedback>): String = values
        .sortedBy { it.createdAtEpochMs }
        .takeLast(200)
        .joinToString("\n") { value ->
            listOf(
                "f1", e(value.id), value.createdAtEpochMs, e(value.submissionId),
                e(value.practiceId), e(value.scoreText), e(value.comment)
            ).joinToString("\t")
        }

    fun decodeFeedback(serialized: String): List<TeacherFeedback> = serialized.lineSequence()
        .take(200)
        .mapNotNull { line ->
            runCatching {
                val f = line.split('\t')
                require(f.size == 7 && f[0] == "f1")
                TeacherFeedback(
                    id = d(f[1]),
                    createdAtEpochMs = f[2].toLong(),
                    submissionId = d(f[3]),
                    practiceId = d(f[4]),
                    scoreText = d(f[5]).safeText(),
                    comment = d(f[6]).safeText()
                )
            }.getOrNull()
        }
        .filter { it.id.isNotBlank() && it.practiceId in LearningCatalog.practiceIds }
        .distinctBy { it.id }
        .toList()

    private fun e(value: Any): String = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(value.toString().take(MAX_TEXT).toByteArray(StandardCharsets.UTF_8))

    private fun d(value: String): String = String(
        Base64.getUrlDecoder().decode(value),
        StandardCharsets.UTF_8
    )

    private fun String.safeText(): String = take(MAX_TEXT).trim()
}
