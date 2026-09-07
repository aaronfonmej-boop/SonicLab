package com.soniclab3d.academic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.soniclab3d.physics.SecondarySourceSettings
import com.soniclab3d.physics.Waveform
import com.soniclab3d.cymatics.CymaticsCameraMotion
import com.soniclab3d.cymatics.CymaticsState
import com.soniclab3d.cymatics.CymaticsMotionPreset
import com.soniclab3d.cymatics.CymaticsPalette
import com.soniclab3d.cymatics.CymaticsVariable
import com.soniclab3d.cymatics.CymaticsVisualStyle
import com.soniclab3d.cymatics.PlateBoundary
import com.soniclab3d.cymatics.PlateGeometry
import com.soniclab3d.particles.ParticleQuality
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.academicExperienceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "academic_experience"
)

class AcademicExperienceRepository(context: Context) {
    private val dataStore = context.applicationContext.academicExperienceDataStore

    private val roleKey = stringPreferencesKey("role_v1")
    private val completedKey = stringSetPreferencesKey("completed_practices_v1")
    private val answersKey = stringPreferencesKey("student_answers_v1")
    private val activityActiveKey = booleanPreferencesKey("activity_active_v1")
    private val activityPracticeKey = stringPreferencesKey("activity_practice_v1")
    private val lockFrequencyKey = booleanPreferencesKey("lock_frequency_v1")
    private val lockLevelKey = booleanPreferencesKey("lock_level_v1")
    private val lockEnvironmentKey = booleanPreferencesKey("lock_environment_v1")
    private val lockSourcesKey = booleanPreferencesKey("lock_sources_v1")
    private val expectedAnswerKey = stringPreferencesKey("expected_answer_v1")
    private val rubricKey = stringPreferencesKey("rubric_v1")
    private val baselineKey = stringPreferencesKey("scene_baseline_v1")
    private val cymaticsActivityActiveKey = booleanPreferencesKey("cymatics_activity_active_v2")
    private val cymaticsActivityPracticeKey = stringPreferencesKey("cymatics_activity_practice_v2")
    private val cymaticsLockGeometryKey = booleanPreferencesKey("cymatics_lock_geometry_v2")
    private val cymaticsLockMaterialKey = booleanPreferencesKey("cymatics_lock_material_v2")
    private val cymaticsLockFrequencyKey = booleanPreferencesKey("cymatics_lock_frequency_v2")
    private val cymaticsLockExciterKey = booleanPreferencesKey("cymatics_lock_exciter_v2")
    private val cymaticsLockVisualKey = booleanPreferencesKey("cymatics_lock_visual_v2")
    private val cymaticsExpectedAnswerKey = stringPreferencesKey("cymatics_expected_answer_v2")
    private val cymaticsRubricKey = stringPreferencesKey("cymatics_rubric_v2")
    private val cymaticsBaselineKey = stringPreferencesKey("cymatics_scene_baseline_v2")
    private val selectedLearningPathKey = stringPreferencesKey("selected_learning_path_v3")
    private val completedGuidesKey = stringSetPreferencesKey("completed_guides_v3")
    private val favoriteGuidesKey = stringSetPreferencesKey("favorite_guides_v3")
    private val practicePhasesKey = stringPreferencesKey("practice_phases_v3")
    private val practiceDraftsKey = stringPreferencesKey("practice_drafts_v3")
    private val personalDoubtsKey = stringPreferencesKey("personal_doubts_v3")
    private val notebookEntriesKey = stringPreferencesKey("notebook_entries_v3")
    private val learningAssignmentsKey = stringPreferencesKey("learning_assignments_v3")
    private val importedSubmissionsKey = stringPreferencesKey("imported_submissions_v3")
    private val teacherFeedbackKey = stringPreferencesKey("teacher_feedback_v3")

    val state: Flow<AcademicExperienceState> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(::decode)
        .distinctUntilChanged()

    suspend fun setRole(role: AcademicRole) {
        dataStore.edit { it[roleKey] = role.name }
    }

    suspend fun submit(practiceId: String, answer: String) {
        val cleanAnswer = answer.trim()
        require(practiceId in LearningCatalog.practiceIds) { "Práctica desconocida" }
        require(cleanAnswer.isNotEmpty()) { "La respuesta está vacía" }
        dataStore.edit { preferences ->
            preferences[completedKey] = preferences[completedKey].orEmpty() + practiceId
            val answers = AcademicTextMapCodec.decode(preferences[answersKey].orEmpty()).toMutableMap()
            answers[practiceId] = cleanAnswer
            preferences[answersKey] = AcademicTextMapCodec.encode(answers)
            val drafts = LearningPersistenceCodecs.decodePracticeDrafts(
                preferences[practiceDraftsKey].orEmpty()
            ).toMutableMap()
            val previous = drafts[practiceId]
            drafts[practiceId] = (previous ?: PracticeDraft(
                practiceId = practiceId,
                updatedAtEpochMs = System.currentTimeMillis()
            )).copy(
                updatedAtEpochMs = System.currentTimeMillis(),
                analysis = cleanAnswer,
                status = PracticeWorkStatus.SUBMITTED
            )
            preferences[practiceDraftsKey] = LearningPersistenceCodecs.encodePracticeDrafts(drafts)
        }
    }

    suspend fun resetStudentProgress() {
        dataStore.edit { preferences ->
            preferences.remove(completedKey)
            preferences.remove(answersKey)
            preferences.remove(completedGuidesKey)
            preferences.remove(practicePhasesKey)
            preferences.remove(practiceDraftsKey)
        }
    }

    suspend fun selectLearningPath(path: LearningPath) {
        dataStore.edit { preferences -> preferences[selectedLearningPathKey] = path.id }
    }

    suspend fun setGuideCompleted(guideId: String, completed: Boolean) {
        require(LearningCatalog.guide(guideId) != null) { "Guía desconocida" }
        dataStore.edit { preferences ->
            val current = preferences[completedGuidesKey].orEmpty()
            preferences[completedGuidesKey] = if (completed) current + guideId else current - guideId
        }
    }

    suspend fun toggleGuideFavorite(guideId: String) {
        require(LearningCatalog.guide(guideId) != null) { "Guía desconocida" }
        dataStore.edit { preferences ->
            val current = preferences[favoriteGuidesKey].orEmpty()
            preferences[favoriteGuidesKey] = if (guideId in current) current - guideId else current + guideId
        }
    }

    suspend fun setPracticePhase(practiceId: String, phase: PracticePhase) {
        require(practiceId in LearningCatalog.practiceIds) { "Práctica desconocida" }
        dataStore.edit { preferences ->
            val phases = AcademicTextMapCodec.decode(preferences[practicePhasesKey].orEmpty()).toMutableMap()
            phases[practiceId] = phase.ordinal.toString()
            preferences[practicePhasesKey] = AcademicTextMapCodec.encode(phases)
        }
    }

    suspend fun savePracticeDraft(draft: PracticeDraft) {
        require(draft.practiceId in LearningCatalog.practiceIds) { "Práctica desconocida" }
        dataStore.edit { preferences ->
            val drafts = LearningPersistenceCodecs.decodePracticeDrafts(
                preferences[practiceDraftsKey].orEmpty()
            ).toMutableMap()
            drafts[draft.practiceId] = draft.copy(
                updatedAtEpochMs = System.currentTimeMillis(),
                attempt = draft.attempt.coerceIn(1, 999)
            )
            preferences[practiceDraftsKey] = LearningPersistenceCodecs.encodePracticeDrafts(drafts)
        }
    }

    suspend fun resetLearningPractice(practiceId: String) {
        require(practiceId in LearningCatalog.practiceIds) { "Práctica desconocida" }
        dataStore.edit { preferences ->
            preferences[completedKey] = preferences[completedKey].orEmpty() - practiceId
            val answers = AcademicTextMapCodec.decode(preferences[answersKey].orEmpty()).toMutableMap()
            answers.remove(practiceId)
            preferences[answersKey] = AcademicTextMapCodec.encode(answers)
            val phases = AcademicTextMapCodec.decode(preferences[practicePhasesKey].orEmpty()).toMutableMap()
            phases.remove(practiceId)
            preferences[practicePhasesKey] = AcademicTextMapCodec.encode(phases)
            val drafts = LearningPersistenceCodecs.decodePracticeDrafts(
                preferences[practiceDraftsKey].orEmpty()
            ).toMutableMap()
            val nextAttempt = (drafts[practiceId]?.attempt ?: 0) + 1
            drafts[practiceId] = PracticeDraft(
                practiceId = practiceId,
                updatedAtEpochMs = System.currentTimeMillis(),
                attempt = nextAttempt.coerceIn(1, 999)
            )
            preferences[practiceDraftsKey] = LearningPersistenceCodecs.encodePracticeDrafts(drafts)
        }
    }

    suspend fun savePersonalDoubt(doubt: PersonalDoubt) {
        require(doubt.question.isNotBlank()) { "La duda está vacía" }
        dataStore.edit { preferences ->
            val values = LearningPersistenceCodecs.decodeDoubts(preferences[personalDoubtsKey].orEmpty()).toMutableList()
            val index = values.indexOfFirst { it.id == doubt.id }
            if (index >= 0) values[index] = doubt else values += doubt
            preferences[personalDoubtsKey] = LearningPersistenceCodecs.encodeDoubts(values)
        }
    }

    suspend fun answerPersonalDoubt(doubtId: String, answer: String) {
        val clean = answer.trim()
        require(clean.isNotEmpty()) { "La respuesta está vacía" }
        dataStore.edit { preferences ->
            val now = System.currentTimeMillis()
            val values = LearningPersistenceCodecs.decodeDoubts(preferences[personalDoubtsKey].orEmpty())
                .map { doubt ->
                    if (doubt.id == doubtId) doubt.copy(
                        updatedAtEpochMs = now,
                        status = DoubtStatus.ANSWERED,
                        teacherAnswer = clean
                    ) else doubt
                }
            require(values.any { it.id == doubtId }) { "Duda desconocida" }
            preferences[personalDoubtsKey] = LearningPersistenceCodecs.encodeDoubts(values)
        }
    }

    suspend fun archivePersonalDoubt(doubtId: String) {
        dataStore.edit { preferences ->
            val values = LearningPersistenceCodecs.decodeDoubts(preferences[personalDoubtsKey].orEmpty())
                .map { doubt ->
                    if (doubt.id == doubtId) doubt.copy(
                        updatedAtEpochMs = System.currentTimeMillis(),
                        status = DoubtStatus.ARCHIVED
                    ) else doubt
                }
            preferences[personalDoubtsKey] = LearningPersistenceCodecs.encodeDoubts(values)
        }
    }

    suspend fun saveNotebookEntry(entry: NotebookEntry) {
        require(entry.title.isNotBlank()) { "El título está vacío" }
        require(entry.observation.isNotBlank() || entry.hypothesis.isNotBlank() || entry.conclusion.isNotBlank()) {
            "La entrada no contiene evidencia"
        }
        dataStore.edit { preferences ->
            val values = LearningPersistenceCodecs.decodeNotebook(preferences[notebookEntriesKey].orEmpty()).toMutableList()
            val index = values.indexOfFirst { it.id == entry.id }
            if (index >= 0) values[index] = entry else values += entry
            preferences[notebookEntriesKey] = LearningPersistenceCodecs.encodeNotebook(values)
        }
    }

    suspend fun publishLearningAssignment(assignment: LearningAssignment) {
        require(assignment.practiceId in LearningCatalog.practiceIds) { "Práctica desconocida" }
        require(assignment.courseName.isNotBlank()) { "El curso está vacío" }
        require(assignment.rubric.isNotBlank()) { "La rúbrica está vacía" }
        dataStore.edit { preferences ->
            val previous = LearningPersistenceCodecs.decodeAssignments(preferences[learningAssignmentsKey].orEmpty())
                .filterNot { it.id == assignment.id }
                .map { it.copy(active = false) }
            preferences[learningAssignmentsKey] = LearningPersistenceCodecs.encodeAssignments(previous + assignment.copy(active = true))
        }
    }

    suspend fun closeLearningAssignment(assignmentId: String) {
        dataStore.edit { preferences ->
            val values = LearningPersistenceCodecs.decodeAssignments(preferences[learningAssignmentsKey].orEmpty())
                .map { if (it.id == assignmentId) it.copy(active = false) else it }
            preferences[learningAssignmentsKey] = LearningPersistenceCodecs.encodeAssignments(values)
        }
    }

    suspend fun importSubmission(submission: ImportedSubmission) {
        require(submission.practiceId in LearningCatalog.practiceIds) { "Práctica desconocida" }
        dataStore.edit { preferences ->
            val values = LearningPersistenceCodecs.decodeSubmissions(
                preferences[importedSubmissionsKey].orEmpty()
            ).toMutableList()
            val index = values.indexOfFirst { it.id == submission.id }
            if (index >= 0) values[index] = submission else values += submission
            preferences[importedSubmissionsKey] = LearningPersistenceCodecs.encodeSubmissions(values)
        }
    }

    suspend fun saveFeedback(feedback: TeacherFeedback) {
        require(feedback.practiceId in LearningCatalog.practiceIds) { "Práctica desconocida" }
        require(feedback.comment.isNotBlank()) { "El comentario está vacío" }
        dataStore.edit { preferences ->
            val values = LearningPersistenceCodecs.decodeFeedback(
                preferences[teacherFeedbackKey].orEmpty()
            ).toMutableList()
            val index = values.indexOfFirst { it.id == feedback.id }
            if (index >= 0) values[index] = feedback else values += feedback
            preferences[teacherFeedbackKey] = LearningPersistenceCodecs.encodeFeedback(values)
        }
    }

    suspend fun publish(activity: TeacherActivity) {
        require(AcademicPractices.all.any { it.id == activity.practiceId }) { "Práctica desconocida" }
        require(activity.expectedAnswer.isNotBlank()) { "La respuesta esperada está vacía" }
        require(activity.rubric.isNotBlank()) { "La rúbrica está vacía" }
        dataStore.edit { preferences ->
            preferences[activityActiveKey] = true
            preferences[activityPracticeKey] = activity.practiceId
            preferences[lockFrequencyKey] = activity.locks.frequency
            preferences[lockLevelKey] = activity.locks.level
            preferences[lockEnvironmentKey] = activity.locks.environment
            preferences[lockSourcesKey] = activity.locks.sources
            preferences[expectedAnswerKey] = activity.expectedAnswer.trim()
            preferences[rubricKey] = activity.rubric.trim()
            preferences[baselineKey] = AcademicBaselineCodec.encode(activity.baseline)
        }
    }

    suspend fun closeActivity() {
        dataStore.edit { preferences -> preferences[activityActiveKey] = false }
    }

    suspend fun publishCymatics(activity: CymaticsTeacherActivity) {
        require(CymaticsAcademicPractices.all.any { it.id == activity.practiceId }) { "Práctica desconocida" }
        require(activity.expectedAnswer.isNotBlank()) { "La respuesta esperada está vacía" }
        require(activity.rubric.isNotBlank()) { "La rúbrica está vacía" }
        dataStore.edit { preferences ->
            preferences[cymaticsActivityActiveKey] = true
            preferences[cymaticsActivityPracticeKey] = activity.practiceId
            preferences[cymaticsLockGeometryKey] = activity.locks.geometryAndBoundary
            preferences[cymaticsLockMaterialKey] = activity.locks.materialAndDimensions
            preferences[cymaticsLockFrequencyKey] = activity.locks.frequencyAndMode
            preferences[cymaticsLockExciterKey] = activity.locks.exciter
            preferences[cymaticsLockVisualKey] = activity.locks.visualAids
            preferences[cymaticsExpectedAnswerKey] = activity.expectedAnswer.trim()
            preferences[cymaticsRubricKey] = activity.rubric.trim()
            preferences[cymaticsBaselineKey] = CymaticsBaselineCodec.encode(activity.baseline)
        }
    }

    suspend fun closeCymaticsActivity() {
        dataStore.edit { preferences -> preferences[cymaticsActivityActiveKey] = false }
    }

    private fun decode(preferences: Preferences): AcademicExperienceState {
        val validIds = LearningCatalog.practiceIds
        val role = AcademicRole.entries.firstOrNull { it.name == preferences[roleKey] }
            ?: AcademicRole.STUDENT
        val completed = preferences[completedKey].orEmpty().filterTo(mutableSetOf()) { it in validIds }
        val answers = AcademicTextMapCodec.decode(preferences[answersKey].orEmpty())
            .filterKeys { it in validIds }
        val practice = AcademicPractices.byId(preferences[activityPracticeKey].orEmpty())
        val fallbackBaseline = AcademicSceneBaseline.from(
            AcademicPractices.prepare(com.soniclab3d.physics.AcousticState(), practice.id)
        )
        val activity = TeacherActivity(
            active = preferences[activityActiveKey] ?: false,
            practiceId = practice.id,
            locks = AcademicLocks(
                frequency = preferences[lockFrequencyKey] ?: true,
                level = preferences[lockLevelKey] ?: false,
                environment = preferences[lockEnvironmentKey] ?: true,
                sources = preferences[lockSourcesKey] ?: false
            ),
            expectedAnswer = preferences[expectedAnswerKey]?.takeIf { it.isNotBlank() }
                ?: practice.expectedAnswer,
            rubric = preferences[rubricKey]?.takeIf { it.isNotBlank() }
                ?: practice.defaultRubric,
            baseline = AcademicBaselineCodec.decode(preferences[baselineKey].orEmpty())
                ?: fallbackBaseline
        )
        val cymaticsPractice = CymaticsAcademicPractices.byId(
            preferences[cymaticsActivityPracticeKey].orEmpty()
        )
        val fallbackCymaticsBaseline = CymaticsSceneBaseline(
            CymaticsAcademicPractices.prepare(CymaticsState(), cymaticsPractice.id)
        )
        val cymaticsActivity = CymaticsTeacherActivity(
            active = preferences[cymaticsActivityActiveKey] ?: false,
            practiceId = cymaticsPractice.id,
            locks = CymaticsAcademicLocks(
                geometryAndBoundary = preferences[cymaticsLockGeometryKey] ?: true,
                materialAndDimensions = preferences[cymaticsLockMaterialKey] ?: true,
                frequencyAndMode = preferences[cymaticsLockFrequencyKey] ?: true,
                exciter = preferences[cymaticsLockExciterKey] ?: false,
                visualAids = preferences[cymaticsLockVisualKey] ?: false
            ),
            expectedAnswer = preferences[cymaticsExpectedAnswerKey]?.takeIf { it.isNotBlank() }
                ?: cymaticsPractice.expectedAnswer,
            rubric = preferences[cymaticsRubricKey]?.takeIf { it.isNotBlank() }
                ?: cymaticsPractice.defaultRubric,
            baseline = CymaticsBaselineCodec.decode(preferences[cymaticsBaselineKey].orEmpty())
                ?: fallbackCymaticsBaseline
        )
        val validGuideIds = LearningCatalog.guides.mapTo(mutableSetOf()) { it.id }
        val phases = AcademicTextMapCodec.decode(preferences[practicePhasesKey].orEmpty())
            .mapNotNull { (id, value) ->
                val index = value.toIntOrNull()?.coerceIn(0, PracticePhase.entries.lastIndex)
                if (id in validIds && index != null) id to index else null
            }
            .toMap()
        val learning = LearningExperienceState(
            selectedPathId = LearningPath.fromId(preferences[selectedLearningPathKey]).id,
            completedGuideIds = preferences[completedGuidesKey].orEmpty().filterTo(mutableSetOf()) { it in validGuideIds },
            favoriteGuideIds = preferences[favoriteGuidesKey].orEmpty().filterTo(mutableSetOf()) { it in validGuideIds },
            practicePhaseIndexes = phases,
            practiceDrafts = LearningPersistenceCodecs.decodePracticeDrafts(preferences[practiceDraftsKey].orEmpty()),
            personalDoubts = LearningPersistenceCodecs.decodeDoubts(preferences[personalDoubtsKey].orEmpty()),
            notebookEntries = LearningPersistenceCodecs.decodeNotebook(preferences[notebookEntriesKey].orEmpty()),
            assignments = LearningPersistenceCodecs.decodeAssignments(preferences[learningAssignmentsKey].orEmpty()),
            importedSubmissions = LearningPersistenceCodecs.decodeSubmissions(preferences[importedSubmissionsKey].orEmpty()),
            feedback = LearningPersistenceCodecs.decodeFeedback(preferences[teacherFeedbackKey].orEmpty())
        )
        return AcademicExperienceState(
            role = role,
            completedPracticeIds = completed,
            answers = answers,
            teacherActivity = activity,
            cymaticsTeacherActivity = cymaticsActivity,
            learning = learning
        )
    }
}

internal object AcademicTextMapCodec {
    fun encode(values: Map<String, String>): String = values.entries
        .sortedBy { it.key }
        .joinToString("\n") { (key, value) ->
            val encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.toByteArray(StandardCharsets.UTF_8))
            "$key:$encoded"
        }

    fun decode(serialized: String): Map<String, String> = serialized.lineSequence()
        .mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator <= 0) return@mapNotNull null
            val key = line.substring(0, separator)
            val value = runCatching {
                String(
                    Base64.getUrlDecoder().decode(line.substring(separator + 1)),
                    StandardCharsets.UTF_8
                )
            }.getOrNull() ?: return@mapNotNull null
            key to value
        }
        .toMap()
}

internal object AcademicBaselineCodec {
    fun encode(baseline: AcademicSceneBaseline): String = listOf(
        "1",
        baseline.frequencyHz,
        baseline.levelDbSpl,
        baseline.phaseRadians,
        baseline.temperatureC,
        baseline.humidityPercent,
        baseline.atmosphericPressureKPa,
        baseline.waveform.name,
        baseline.harmonicMode,
        baseline.harmonicAmplitudes.joinToString(","),
        baseline.secondarySource.enabled,
        baseline.secondarySource.frequencyHz,
        baseline.secondarySource.levelDbSpl,
        baseline.secondarySource.phaseRadians,
        baseline.secondarySource.xM,
        baseline.secondarySource.yM,
        baseline.secondarySource.zM
    ).joinToString("\t")

    fun decode(serialized: String): AcademicSceneBaseline? = runCatching {
        val fields = serialized.split('\t')
        require(fields.size == 17 && fields[0] == "1")
        val harmonics = fields[9].split(',').map { it.toFloat() }
        require(harmonics.isNotEmpty() && harmonics.all { it.isFinite() })
        AcademicSceneBaseline(
            frequencyHz = fields[1].toDouble().also { require(it.isFinite()) },
            levelDbSpl = fields[2].toFloat().also { require(it.isFinite()) },
            phaseRadians = fields[3].toFloat().also { require(it.isFinite()) },
            temperatureC = fields[4].toDouble().also { require(it.isFinite()) },
            humidityPercent = fields[5].toFloat().also { require(it.isFinite()) },
            atmosphericPressureKPa = fields[6].toFloat().also { require(it.isFinite()) },
            waveform = Waveform.valueOf(fields[7]),
            harmonicMode = fields[8].toBooleanStrict(),
            harmonicAmplitudes = harmonics,
            secondarySource = SecondarySourceSettings(
                enabled = fields[10].toBooleanStrict(),
                frequencyHz = fields[11].toDouble().also { require(it.isFinite()) },
                levelDbSpl = fields[12].toFloat().also { require(it.isFinite()) },
                phaseRadians = fields[13].toFloat().also { require(it.isFinite()) },
                xM = fields[14].toFloat().also { require(it.isFinite()) },
                yM = fields[15].toFloat().also { require(it.isFinite()) },
                zM = fields[16].toFloat().also { require(it.isFinite()) }
            )
        )
    }.getOrNull()
}

internal object CymaticsBaselineCodec {
    fun encode(baseline: CymaticsSceneBaseline): String {
        val state = baseline.state
        return listOf(
            "5",
            state.geometry.name,
            state.boundary.name,
            state.driveFrequencyHz,
            state.modeIndex,
            state.automaticMode,
            state.youngModulusPa,
            state.densityKgM3,
            state.poissonRatio,
            state.thicknessM,
            state.characteristicSizeM,
            state.dampingRatio,
            state.driveX,
            state.driveY,
            state.normalizedDrive,
            state.visualScale,
            state.visualTimeScale,
            state.paused,
            state.visualStyle.name,
            state.palette.name,
            state.variable.name,
            state.showNodes,
            state.showAntinodes,
            state.showGrid,
            state.showExciter,
            state.sandEnabled,
            state.sandPointSizePx,
            state.sandRelaxation,
            state.lightHueDegrees,
            state.reduceMotion,
            state.quality.name,
            state.autoQuality,
            state.showPhaseLines,
            state.contourDensity,
            state.probeEnabled,
            state.probeX,
            state.probeY,
            state.hapticsEnabled,
            state.motionPreset.name,
            state.motionIntensity,
            state.modeTransitionSeconds,
            state.cameraDriftEnabled,
            state.orbitingLightEnabled,
            state.resonanceAuraEnabled,
            state.atmosphericDustEnabled,
            state.sandFlowIntensity,
            state.cameraMotion.name,
            state.stageEnabled,
            state.nodalStreamsEnabled,
            state.energyPulsesEnabled,
            state.postProcessingEnabled,
            state.bloomIntensity
        ).joinToString("\t")
    }

    fun decode(serialized: String): CymaticsSceneBaseline? = runCatching {
        val fields = serialized.split('\t')
        require(
            (fields.size == 32 && fields[0] == "2") ||
                (fields.size == 38 && fields[0] == "3") ||
                (fields.size == 46 && fields[0] == "4") ||
                (fields.size == 52 && fields[0] == "5")
        )
        CymaticsSceneBaseline(
            CymaticsState(
                geometry = PlateGeometry.valueOf(fields[1]),
                boundary = PlateBoundary.valueOf(fields[2]),
                driveFrequencyHz = fields[3].toDouble(),
                modeIndex = fields[4].toInt(),
                automaticMode = fields[5].toBooleanStrict(),
                youngModulusPa = fields[6].toDouble(),
                densityKgM3 = fields[7].toDouble(),
                poissonRatio = fields[8].toDouble(),
                thicknessM = fields[9].toDouble(),
                characteristicSizeM = fields[10].toDouble(),
                dampingRatio = fields[11].toDouble(),
                driveX = fields[12].toFloat(),
                driveY = fields[13].toFloat(),
                normalizedDrive = fields[14].toFloat(),
                visualScale = fields[15].toFloat(),
                visualTimeScale = fields[16].toFloat(),
                paused = fields[17].toBooleanStrict(),
                visualStyle = CymaticsVisualStyle.valueOf(fields[18]),
                palette = CymaticsPalette.valueOf(fields[19]),
                variable = CymaticsVariable.valueOf(fields[20]),
                showNodes = fields[21].toBooleanStrict(),
                showAntinodes = fields[22].toBooleanStrict(),
                showGrid = fields[23].toBooleanStrict(),
                showExciter = fields[24].toBooleanStrict(),
                sandEnabled = fields[25].toBooleanStrict(),
                sandPointSizePx = fields[26].toFloat(),
                sandRelaxation = fields[27].toFloat(),
                lightHueDegrees = fields[28].toFloat(),
                reduceMotion = fields[29].toBooleanStrict(),
                quality = ParticleQuality.valueOf(fields[30]),
                autoQuality = fields[31].toBooleanStrict(),
                showPhaseLines = fields.getOrNull(32)?.toBooleanStrict() ?: false,
                contourDensity = fields.getOrNull(33)?.toFloat() ?: 12f,
                probeEnabled = fields.getOrNull(34)?.toBooleanStrict() ?: false,
                probeX = fields.getOrNull(35)?.toFloat() ?: -0.28f,
                probeY = fields.getOrNull(36)?.toFloat() ?: 0.22f,
                hapticsEnabled = fields.getOrNull(37)?.toBooleanStrict() ?: true,
                motionPreset = fields.getOrNull(38)?.let(CymaticsMotionPreset::valueOf)
                    ?: CymaticsMotionPreset.CINEMATIC,
                motionIntensity = fields.getOrNull(39)?.toFloat() ?: 0.88f,
                modeTransitionSeconds = fields.getOrNull(40)?.toFloat() ?: 0.95f,
                cameraDriftEnabled = fields.getOrNull(41)?.toBooleanStrict() ?: true,
                orbitingLightEnabled = fields.getOrNull(42)?.toBooleanStrict() ?: true,
                resonanceAuraEnabled = fields.getOrNull(43)?.toBooleanStrict() ?: true,
                atmosphericDustEnabled = fields.getOrNull(44)?.toBooleanStrict() ?: true,
                sandFlowIntensity = fields.getOrNull(45)?.toFloat() ?: 0.84f,
                cameraMotion = fields.getOrNull(46)?.let(CymaticsCameraMotion::valueOf)
                    ?: CymaticsCameraMotion.SHOW,
                stageEnabled = fields.getOrNull(47)?.toBooleanStrict() ?: true,
                nodalStreamsEnabled = fields.getOrNull(48)?.toBooleanStrict() ?: true,
                energyPulsesEnabled = fields.getOrNull(49)?.toBooleanStrict() ?: true,
                postProcessingEnabled = fields.getOrNull(50)?.toBooleanStrict() ?: true,
                bloomIntensity = fields.getOrNull(51)?.toFloat() ?: 0.92f
            ).sanitized()
        )
    }.getOrNull()
}
