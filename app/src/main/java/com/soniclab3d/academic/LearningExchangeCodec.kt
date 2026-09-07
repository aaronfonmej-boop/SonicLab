package com.soniclab3d.academic

import java.time.OffsetDateTime
import org.json.JSONArray
import org.json.JSONObject

/** Versioned, data-only interchange. No script, expression or filesystem field is accepted. */
object LearningExchangeCodec {
    const val MAX_DOCUMENT_BYTES = 1_000_000
    private const val SCHEMA_VERSION = 1
    private const val MAX_TEXT = 8_000
    private val safeId = Regex("[A-Za-z0-9._-]{1,120}")

    fun documentType(json: String): String = root(json).getString("documentType")

    fun studentPack(assignment: LearningAssignment): String {
        val practice = requireNotNull(LearningCatalog.practice(assignment.practiceId))
        val assignmentJson = JSONObject()
            .put("id", assignment.id)
            .put("courseName", assignment.courseName)
            .put("period", assignment.period)
            .put("practiceId", assignment.practiceId)
            .put("instructions", assignment.instructions)
            .put("title", practice.title)
            .put("objective", practice.objective)
            .put("domain", practice.domain.name.lowercase())
            .put("estimatedMinutes", practice.estimatedMinutes)
        return document("soniclabpack")
            .put("assignment", assignmentJson)
            .put("containsExpectedAnswer", false)
            .put("containsPrivateRubric", false)
            .toString(2)
    }

    fun parseStudentPack(json: String): LearningAssignment {
        val root = requireDocument(json, "soniclabpack")
        require(root.optBoolean("containsExpectedAnswer", true).not()) { "El paquete declara una solución" }
        require(root.optBoolean("containsPrivateRubric", true).not()) { "El paquete declara una rúbrica privada" }
        val item = root.getJSONObject("assignment")
        require(!item.has("expectedAnswer") && !item.has("rubric") && !item.has("script")) {
            "El paquete de estudiante contiene campos privados o ejecutables"
        }
        val practiceId = item.requiredId("practiceId")
        val practice = requireNotNull(LearningCatalog.practice(practiceId)) { "Práctica no compatible" }
        return LearningAssignment(
            id = item.requiredId("id"),
            createdAtEpochMs = System.currentTimeMillis(),
            courseName = item.safeString("courseName", 120),
            period = item.safeString("period", 80),
            practiceId = practiceId,
            instructions = item.safeString("instructions", 3_000),
            rubric = practice.rubric,
            active = true
        )
    }

    fun submission(experience: AcademicExperienceState, assignment: LearningAssignment): String {
        val practiceId = assignment.practiceId
        val draft = experience.learning.practiceDrafts[practiceId]
        val doubts = experience.learning.personalDoubts.filter {
            it.contextId == practiceId && it.status != DoubtStatus.ARCHIVED
        }
        val doubtJson = JSONArray()
        doubts.forEach { doubt ->
            doubtJson.put(
                JSONObject()
                    .put("id", doubt.id)
                    .put("question", doubt.question)
                    .put("expectation", doubt.expectation)
                    .put("observation", doubt.observation)
            )
        }
        val work = JSONObject()
            .put("id", "submission_${System.currentTimeMillis()}")
            .put("assignmentId", assignment.id)
            .put("practiceId", practiceId)
            .put("answer", experience.answers[practiceId].orEmpty())
            .put("prediction", draft?.prediction.orEmpty())
            .put("evidence", draft?.evidence.orEmpty())
            .put("reflection", draft?.reflection.orEmpty())
            .put("doubts", doubtJson)
        return document("soniclabsubmission")
            .put("submission", work)
            .put("privacy", "selected-academic-work-only")
            .put("containsExpectedAnswer", false)
            .toString(2)
    }

    fun parseSubmission(json: String): ImportedSubmission {
        val root = requireDocument(json, "soniclabsubmission")
        require(root.optBoolean("containsExpectedAnswer", true).not()) { "La entrega declara una solución" }
        val item = root.getJSONObject("submission")
        require(!item.has("expectedAnswer") && !item.has("script")) { "Entrega con campo prohibido" }
        val practiceId = item.requiredId("practiceId")
        require(LearningCatalog.practice(practiceId) != null) { "Práctica no compatible" }
        val doubts = item.optJSONArray("doubts") ?: JSONArray()
        val notes = buildList {
            repeat(doubts.length().coerceAtMost(50)) { index ->
                val doubt = doubts.getJSONObject(index)
                add(doubt.safeString("question", 2_000))
            }
        }
        return ImportedSubmission(
            id = item.requiredId("id"),
            importedAtEpochMs = System.currentTimeMillis(),
            assignmentId = item.requiredId("assignmentId"),
            practiceId = practiceId,
            answer = item.safeString("answer", 4_000),
            prediction = item.safeString("prediction", 4_000),
            evidence = item.safeString("evidence", 8_000),
            reflection = item.safeString("reflection", 4_000),
            doubtNotes = notes
        )
    }

    fun feedback(value: TeacherFeedback): String = document("soniclabfeedback")
        .put(
            "feedback",
            JSONObject()
                .put("id", value.id)
                .put("submissionId", value.submissionId)
                .put("practiceId", value.practiceId)
                .put("scoreText", value.scoreText)
                .put("comment", value.comment)
        )
        .put("privacy", "feedback-for-one-submission")
        .toString(2)

    fun parseFeedback(json: String): TeacherFeedback {
        val root = requireDocument(json, "soniclabfeedback")
        val item = root.getJSONObject("feedback")
        require(!item.has("script") && !item.has("expectedAnswer")) { "Feedback con campo prohibido" }
        val practiceId = item.requiredId("practiceId")
        require(LearningCatalog.practice(practiceId) != null) { "Práctica no compatible" }
        return TeacherFeedback(
            id = item.requiredId("id"),
            createdAtEpochMs = System.currentTimeMillis(),
            submissionId = item.requiredId("submissionId"),
            practiceId = practiceId,
            scoreText = item.safeString("scoreText", 120),
            comment = item.safeString("comment", 4_000)
        )
    }

    private fun document(type: String): JSONObject = JSONObject()
        .put("documentType", type)
        .put("schemaVersion", SCHEMA_VERSION)
        .put("appId", "com.soniclab3d")
        .put("appVersion", "1.1.0-academia-continua")
        .put("createdAt", OffsetDateTime.now().toString())
        .put("scientificWarning", "Contenido educativo; simulación, visualización y medición se interpretan según sus etiquetas.")

    private fun requireDocument(json: String, expectedType: String): JSONObject = root(json).also { value ->
        require(value.getString("documentType") == expectedType) { "Tipo de documento incorrecto" }
        require(value.getInt("schemaVersion") == SCHEMA_VERSION) { "Esquema no compatible" }
        require(value.optString("appId") == "com.soniclab3d") { "Aplicación de origen no compatible" }
    }

    private fun root(json: String): JSONObject {
        require(json.toByteArray(Charsets.UTF_8).size <= MAX_DOCUMENT_BYTES) { "Archivo demasiado grande" }
        require(json.trimStart().startsWith('{')) { "JSON inválido" }
        return JSONObject(json)
    }

    private fun JSONObject.requiredId(name: String): String = getString(name).also {
        require(safeId.matches(it)) { "ID inválido: $name" }
    }

    private fun JSONObject.safeString(name: String, limit: Int): String = optString(name)
        .take(limit.coerceAtMost(MAX_TEXT))
        .trim()
}
