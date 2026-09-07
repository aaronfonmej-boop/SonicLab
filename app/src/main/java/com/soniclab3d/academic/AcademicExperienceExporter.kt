package com.soniclab3d.academic

import com.soniclab3d.cymatics.CymaticsRenderInfo
import com.soniclab3d.cymatics.CymaticsState
import com.soniclab3d.physics.AcousticState
import java.util.Locale

object AcademicExperienceExporter {
    fun json(
        experience: AcademicExperienceState,
        scene: AcousticState,
        simulationTimeS: Double,
        cymaticsScene: CymaticsState? = null,
        cymaticsInfo: CymaticsRenderInfo? = null
    ): String = buildString {
        val activity = experience.teacherActivity
        val cymaticsActivity = experience.cymaticsTeacherActivity
        appendLine("{")
        appendLine("  \"format\": \"SonicLab academic experience 1.1\",")
        appendLine("  \"schemaVersion\": 3,")
        appendLine("  \"role\": \"${experience.role.name.lowercase()}\",")
        appendLine("  \"localOnly\": true,")
        appendLine("  \"calibratedMeasurement\": false,")
        appendLine("  \"model\": \"analytical-simulation\",")
        appendLine("  \"airProgress\": {")
        appendLine("    \"completed\": ${experience.completedCount},")
        appendLine("    \"total\": ${AcademicPractices.all.size},")
        appendLine("    \"responses\": [")
        val responses = AcademicPractices.all.mapNotNull { practice ->
            experience.answers[practice.id]?.let { practice to it }
        }
        responses.forEachIndexed { index, (practice, answer) ->
            append("      {\"practiceId\": \"${practice.id}\", \"title\": \"${escape(practice.title)}\", \"answer\": \"${escape(answer)}\"}")
            appendLine(if (index == responses.lastIndex) "" else ",")
        }
        appendLine("    ]")
        appendLine("  },")
        appendLine("  \"cymaticsProgress\": {")
        appendLine("    \"completed\": ${experience.cymaticsCompletedCount},")
        appendLine("    \"total\": ${CymaticsAcademicPractices.all.size},")
        appendLine("    \"responses\": [")
        val cymaticsResponses = CymaticsAcademicPractices.all.mapNotNull { practice ->
            experience.answers[practice.id]?.let { practice to it }
        }
        cymaticsResponses.forEachIndexed { index, (practice, answer) ->
            append("      {\"practiceId\": \"${practice.id}\", \"title\": \"${escape(practice.title)}\", \"answer\": \"${escape(answer)}\"}")
            appendLine(if (index == cymaticsResponses.lastIndex) "" else ",")
        }
        appendLine("    ]")
        appendLine("  },")
        appendLine("  \"continuousLearning\": {")
        appendLine("    \"selectedPathId\": \"${escape(experience.learning.selectedPathId)}\",")
        val completedGuideJson = experience.learning.completedGuideIds.sorted()
            .joinToString(prefix = "[", postfix = "]") { id -> "\"" + escape(id) + "\"" }
        val favoriteGuideJson = experience.learning.favoriteGuideIds.sorted()
            .joinToString(prefix = "[", postfix = "]") { id -> "\"" + escape(id) + "\"" }
        appendLine("    \"completedGuideIds\": $completedGuideJson,")
        appendLine("    \"favoriteGuideIds\": $favoriteGuideJson,")
        appendLine("    \"completedPractices\": ${experience.totalLearningPracticeCount},")
        appendLine("    \"totalPractices\": ${LearningCatalog.practices.size},")
        appendLine("    \"practicePhases\": [")
        val phases = experience.learning.practicePhaseIndexes.toList().sortedBy { it.first }
        phases.forEachIndexed { index, (id, phaseIndex) ->
            append("      {\"practiceId\": \"${escape(id)}\", \"phase\": \"${PracticePhase.entries[phaseIndex.coerceIn(0, PracticePhase.entries.lastIndex)].name.lowercase()}\"}")
            appendLine(if (index == phases.lastIndex) "" else ",")
        }
        appendLine("    ],")
        appendLine("    \"practiceDrafts\": [")
        val drafts = experience.learning.practiceDrafts.values.sortedBy { it.practiceId }
        drafts.forEachIndexed { index, draft ->
            append("      {\"practiceId\": \"${escape(draft.practiceId)}\", \"attempt\": ${draft.attempt}, \"status\": \"${draft.status.name.lowercase()}\", \"prediction\": \"${escape(draft.prediction)}\", \"evidence\": \"${escape(draft.evidence)}\", \"analysis\": \"${escape(draft.analysis)}\", \"reflection\": \"${escape(draft.reflection)}\"}")
            appendLine(if (index == drafts.lastIndex) "" else ",")
        }
        appendLine("    ],")
        appendLine("    \"responses\": [")
        val allResponses = LearningCatalog.practices.mapNotNull { practice ->
            experience.answers[practice.id]?.let { practice to it }
        }
        allResponses.forEachIndexed { index, (practice, answer) ->
            append("      {\"practiceId\": \"${practice.id}\", \"title\": \"${escape(practice.title)}\", \"answer\": \"${escape(answer)}\"}")
            appendLine(if (index == allResponses.lastIndex) "" else ",")
        }
        appendLine("    ],")
        appendLine("    \"personalDoubts\": [")
        experience.learning.personalDoubts.forEachIndexed { index, doubt ->
            append("      {\"id\": \"${escape(doubt.id)}\", \"contextId\": \"${escape(doubt.contextId)}\", \"question\": \"${escape(doubt.question)}\", \"status\": \"${doubt.status.name.lowercase()}\", \"teacherAnswer\": \"${escape(doubt.teacherAnswer)}\"}")
            appendLine(if (index == experience.learning.personalDoubts.lastIndex) "" else ",")
        }
        appendLine("    ],")
        appendLine("    \"notebookEntries\": [")
        experience.learning.notebookEntries.forEachIndexed { index, entry ->
            append("      {\"id\": \"${escape(entry.id)}\", \"createdAtEpochMs\": ${entry.createdAtEpochMs}, \"period\": \"${escape(entry.period)}\", \"title\": \"${escape(entry.title)}\", \"hypothesis\": \"${escape(entry.hypothesis)}\", \"observation\": \"${escape(entry.observation)}\", \"conclusion\": \"${escape(entry.conclusion)}\"}")
            appendLine(if (index == experience.learning.notebookEntries.lastIndex) "" else ",")
        }
        appendLine("    ],")
        appendLine("    \"assignments\": [")
        experience.learning.assignments.forEachIndexed { index, assignment ->
            append("      {\"id\": \"${escape(assignment.id)}\", \"courseName\": \"${escape(assignment.courseName)}\", \"period\": \"${escape(assignment.period)}\", \"practiceId\": \"${escape(assignment.practiceId)}\", \"instructions\": \"${escape(assignment.instructions)}\", \"active\": ${assignment.active}")
            if (experience.role == AcademicRole.TEACHER) append(", \"rubric\": \"${escape(assignment.rubric)}\"")
            append("}")
            appendLine(if (index == experience.learning.assignments.lastIndex) "" else ",")
        }
        appendLine("    ],")
        appendLine("    \"importedSubmissions\": [")
        val importedSubmissions = if (experience.role == AcademicRole.TEACHER) {
            experience.learning.importedSubmissions
        } else {
            emptyList()
        }
        importedSubmissions.forEachIndexed { index, submission ->
            append("      {\"id\": \"${escape(submission.id)}\", \"practiceId\": \"${escape(submission.practiceId)}\", \"answer\": \"${escape(submission.answer)}\", \"evidence\": \"${escape(submission.evidence)}\"}")
            appendLine(if (index == importedSubmissions.lastIndex) "" else ",")
        }
        appendLine("    ],")
        appendLine("    \"feedback\": [")
        experience.learning.feedback.forEachIndexed { index, feedback ->
            append("      {\"id\": \"${escape(feedback.id)}\", \"submissionId\": \"${escape(feedback.submissionId)}\", \"practiceId\": \"${escape(feedback.practiceId)}\", \"scoreText\": \"${escape(feedback.scoreText)}\", \"comment\": \"${escape(feedback.comment)}\"}")
            appendLine(if (index == experience.learning.feedback.lastIndex) "" else ",")
        }
        appendLine("    ]")
        appendLine("  },")
        appendLine("  \"airTeacherActivity\": {")
        appendLine("    \"active\": ${activity.active},")
        appendLine("    \"practiceId\": \"${activity.practiceId}\",")
        if (experience.role == AcademicRole.TEACHER) {
            appendLine("    \"expectedAnswer\": \"${escape(activity.expectedAnswer)}\",")
            appendLine("    \"rubric\": \"${escape(activity.rubric)}\",")
        } else {
            appendLine("    \"privateEvaluationIncluded\": false,")
        }
        appendLine("    \"locks\": {\"frequency\": ${activity.locks.frequency}, \"level\": ${activity.locks.level}, \"environment\": ${activity.locks.environment}, \"sources\": ${activity.locks.sources}}")
        appendLine("  },")
        appendLine("  \"cymaticsTeacherActivity\": {")
        appendLine("    \"active\": ${cymaticsActivity.active},")
        appendLine("    \"practiceId\": \"${cymaticsActivity.practiceId}\",")
        if (experience.role == AcademicRole.TEACHER) {
            appendLine("    \"expectedAnswer\": \"${escape(cymaticsActivity.expectedAnswer)}\",")
            appendLine("    \"rubric\": \"${escape(cymaticsActivity.rubric)}\",")
        } else {
            appendLine("    \"privateEvaluationIncluded\": false,")
        }
        appendLine("    \"locks\": {\"geometryAndBoundary\": ${cymaticsActivity.locks.geometryAndBoundary}, \"materialAndDimensions\": ${cymaticsActivity.locks.materialAndDimensions}, \"frequencyAndMode\": ${cymaticsActivity.locks.frequencyAndMode}, \"exciter\": ${cymaticsActivity.locks.exciter}, \"visualAids\": ${cymaticsActivity.locks.visualAids}}")
        appendLine("  },")
        appendLine("  \"simulatedAirScene\": {")
        appendLine("    \"frequencyHz\": ${format(scene.frequencyHz)},")
        appendLine("    \"simulatedLevelDbSplAtOneMeter\": ${format(scene.levelDbSpl.toDouble())},")
        appendLine("    \"temperatureC\": ${format(scene.temperatureC)},")
        appendLine("    \"simulationTimeS\": ${format(simulationTimeS)},")
        appendLine("    \"secondarySourceEnabled\": ${scene.secondarySource.enabled},")
        appendLine("    \"harmonicMode\": ${scene.harmonicMode}")
        appendLine("  },")
        if (cymaticsScene == null) {
            appendLine("  \"simulatedPlateScene\": null")
        } else {
            appendLine("  \"simulatedPlateScene\": {")
            appendLine("    \"classification\": \"numerical-simulation\",")
            appendLine("    \"geometry\": \"${cymaticsScene.geometry.name.lowercase()}\",")
            appendLine("    \"boundary\": \"${cymaticsScene.boundary.name.lowercase()}\",")
            appendLine("    \"driveFrequencyHz\": ${format(cymaticsScene.driveFrequencyHz)},")
            appendLine("    \"activeModeIndex\": ${(cymaticsInfo?.activeModeIndex ?: cymaticsScene.modeIndex) + 1},")
            appendLine("    \"naturalFrequencyHz\": ${format(cymaticsInfo?.naturalFrequencyHz ?: 0.0)},")
            appendLine("    \"normalizedResponse\": ${format(cymaticsInfo?.response ?: 0.0)},")
            appendLine("    \"granularDynamics\": \"qualitative-nodal-relaxation\",")
            appendLine("    \"amplitudeUnit\": \"a.u.\"")
            appendLine("  }")
        }
        appendLine("}")
    }

    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (character.code < 0x20) append(' ') else append(character)
            }
        }
    }

    private fun format(value: Double): String = String.format(Locale.US, "%.10g", value)
}
