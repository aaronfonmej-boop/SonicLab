package com.soniclab3d.academic

import com.soniclab3d.physics.AcousticState
import com.soniclab3d.physics.SecondarySourceSettings
import com.soniclab3d.physics.Waveform
import com.soniclab3d.cymatics.CymaticsPalette
import com.soniclab3d.cymatics.CymaticsState
import com.soniclab3d.cymatics.CymaticsVariable
import com.soniclab3d.cymatics.CymaticsVisualStyle
import com.soniclab3d.cymatics.PlateBoundary
import com.soniclab3d.cymatics.PlateGeometry

enum class AcademicRole(val label: String) {
    STUDENT("Estudiante"),
    TEACHER("Docente")
}

data class AcademicPractice(
    val id: String,
    val number: Int,
    val title: String,
    val shortTitle: String,
    val objective: String,
    val steps: List<String>,
    val question: String,
    val expectedAnswer: String,
    val hint: String,
    val actionLabel: String,
    val defaultRubric: String
)

object AcademicPractices {
    val all: List<AcademicPractice> = listOf(
        AcademicPractice(
            id = "wave_particle",
            number = 1,
            title = "La onda avanza; el aire oscila",
            shortTitle = "Onda y partícula",
            objective = "Distinguir la propagación de la perturbación del movimiento local del aire.",
            steps = listOf(
                "Prepara la escena a 120 Hz y 20 °C.",
                "Usa tiempo ×100 y toca una partícula de la escena.",
                "Compara su posición de equilibrio con la posición instantánea."
            ),
            question = "¿Una partícula seleccionada debería alejarse continuamente de la fuente? Explica por qué.",
            expectedAnswer = "No. La partícula oscila alrededor de su equilibrio; lo que avanza es la fase de la perturbación.",
            hint = "Sigue una sola partícula, no un color o un frente de onda.",
            actionLabel = "Preparar 120 Hz",
            defaultRubric = "2 pt: distingue propagación y oscilación; 1 pt: menciona la posición de equilibrio; 1 pt: usa evidencia de la escena."
        ),
        AcademicPractice(
            id = "frequency_wavelength",
            number = 2,
            title = "Frecuencia y longitud de onda",
            shortTitle = "f y λ",
            objective = "Comprobar que la longitud de onda disminuye cuando aumenta la frecuencia a velocidad constante.",
            steps = listOf(
                "Observa λ a 120 Hz con la temperatura en 20 °C.",
                "Prepara el caso de 1.000 Hz sin cambiar la temperatura.",
                "Contrasta ambos valores en el panel Ciencia."
            ),
            question = "¿Qué ocurre con λ si multiplicamos la frecuencia y mantenemos c? Justifica con la relación física.",
            expectedAnswer = "Disminuye en la misma proporción porque λ = c/f.",
            hint = "Si c no cambia, compara el numerador y el denominador de λ = c/f.",
            actionLabel = "Comparar a 1 kHz",
            defaultRubric = "2 pt: indica que λ disminuye; 1 pt: cita λ=c/f; 1 pt: relaciona correctamente la proporción."
        ),
        AcademicPractice(
            id = "interference",
            number = 3,
            title = "Interferencia entre dos fuentes",
            shortTitle = "Interferencia",
            objective = "Identificar zonas donde dos presiones se refuerzan o se cancelan.",
            steps = listOf(
                "Prepara dos fuentes de 120 Hz con igual nivel.",
                "Activa el corte de presión para localizar nodos.",
                "Compara la fuente B en fase 0 y en fase π."
            ),
            question = "¿Qué debe ocurrir en un punto donde llegan presiones instantáneas iguales y opuestas?",
            expectedAnswer = "La suma instantánea se aproxima a cero: aparece interferencia destructiva.",
            hint = "Aplica superposición lineal: suma las dos presiones con su signo.",
            actionLabel = "Preparar dos fuentes",
            defaultRubric = "2 pt: identifica cancelación; 1 pt: usa superposición; 1 pt: reconoce que depende de posición y fase."
        ),
        AcademicPractice(
            id = "harmonics",
            number = 4,
            title = "Una voz no es una sola frecuencia",
            shortTitle = "Voz y armónicos",
            objective = "Relacionar una frecuencia fundamental con la composición armónica y el timbre.",
            steps = listOf(
                "Prepara 120 Hz como frecuencia fundamental.",
                "Modifica las amplitudes de 2f, 3f y 4f.",
                "Compara el espectro antes y después del cambio."
            ),
            question = "¿Dos señales con la misma fundamental necesariamente tienen el mismo timbre? Fundamenta.",
            expectedAnswer = "No. Pueden compartir la fundamental y tener amplitudes armónicas o envolventes diferentes.",
            hint = "Observa qué datos cambian en el espectro aunque la fundamental permanezca en 120 Hz.",
            actionLabel = "Preparar voz 120 Hz",
            defaultRubric = "2 pt: responde que no; 1 pt: relaciona timbre y armónicos; 1 pt: cita evidencia del espectro."
        )
    )

    fun byId(id: String): AcademicPractice = all.firstOrNull { it.id == id } ?: all.first()

    fun prepare(state: AcousticState, practiceId: String): AcousticState = when (practiceId) {
        "frequency_wavelength" -> state.copy(
            frequencyHz = 1_000.0,
            temperatureC = 20.0,
            harmonicMode = false,
            secondarySource = state.secondarySource.copy(enabled = false),
            waveform = Waveform.SINE
        )

        "interference" -> state.copy(
            frequencyHz = 120.0,
            levelDbSpl = 70f,
            harmonicMode = false,
            waveform = Waveform.SINE,
            showPressureSlice = true,
            secondarySource = state.secondarySource.copy(
                enabled = true,
                frequencyHz = 120.0,
                levelDbSpl = 70f,
                phaseRadians = 0f,
                xM = 2f,
                yM = 0f,
                zM = 0f
            )
        )

        "harmonics" -> state.copy(
            frequencyHz = 120.0,
            harmonicMode = true,
            harmonicAmplitudes = listOf(1f, 0.45f, 0.25f, 0.15f) + List(12) { 0f },
            secondarySource = state.secondarySource.copy(enabled = false),
            waveform = Waveform.SINE
        )

        else -> state.copy(
            frequencyHz = 120.0,
            temperatureC = 20.0,
            visualTimeScale = 0.01f,
            paused = false,
            harmonicMode = false,
            secondarySource = state.secondarySource.copy(enabled = false),
            waveform = Waveform.SINE
        )
    }
}

object CymaticsAcademicPractices {
    val all: List<AcademicPractice> = listOf(
        AcademicPractice(
            id = "cymatics_nodes",
            number = 1,
            title = "Nodos y antinodos",
            shortTitle = "Nodos",
            objective = "Distinguir regiones de desplazamiento casi nulo y máximo en una placa vibrante.",
            steps = listOf(
                "Prepara una placa cuadrada y observa el campo sin arena.",
                "Activa las líneas nodales y compara con los antinodos.",
                "Reinicia la arena y observa dónde se acumula."
            ),
            question = "¿Por qué la arena revela líneas estables aunque otras regiones de la placa vibren?",
            expectedAnswer = "Las líneas nodales tienen desplazamiento modal cercano a cero; la relajación cualitativa desplaza la arena desde regiones de mayor vibración hacia ellas.",
            hint = "Compara el brillo de nodos con el mapa de desplazamiento, no solo con la arena.",
            actionLabel = "Preparar nodos",
            defaultRubric = "2 pt: identifica nodos; 1 pt: distingue antinodos; 1 pt: aclara que la arena es cualitativa."
        ),
        AcademicPractice(
            id = "cymatics_resonance",
            number = 2,
            title = "Resonancia de la placa",
            shortTitle = "Resonancia",
            objective = "Relacionar frecuencia de excitación, frecuencia propia y respuesta modal.",
            steps = listOf(
                "Selecciona manualmente un modo.",
                "Observa la respuesta lejos de su frecuencia propia.",
                "Pulsa Ajustar a resonancia y compara la amplitud normalizada."
            ),
            question = "¿Por qué la respuesta aumenta cerca de la frecuencia propia y qué papel cumple el amortiguamiento?",
            expectedAnswer = "La excitación transfiere energía de forma más eficiente cerca de la frecuencia propia; el amortiguamiento limita y ensancha el máximo de respuesta.",
            hint = "Observa simultáneamente f de excitación, f del modo y ζ.",
            actionLabel = "Preparar resonancia",
            defaultRubric = "2 pt: relaciona ambas frecuencias; 1 pt: explica el máximo; 1 pt: menciona amortiguamiento."
        ),
        AcademicPractice(
            id = "cymatics_geometry",
            number = 3,
            title = "Geometría y condición de borde",
            shortTitle = "Geometría",
            objective = "Comprobar que la forma y la sujeción cambian modos y frecuencias propias.",
            steps = listOf(
                "Compara una placa cuadrada y una hexagonal con parámetros iguales.",
                "Mantén el modo seleccionado y registra su frecuencia propia.",
                "Cambia entre borde apoyado y sujeto."
            ),
            question = "¿Puede una frecuencia aislada determinar una figura universal de cimática?",
            expectedAnswer = "No. La figura también depende de geometría, dimensiones, material, espesor, borde, excitación y modo.",
            hint = "Cambia solo una variable por vez y compara el resultado.",
            actionLabel = "Preparar comparación",
            defaultRubric = "2 pt: rechaza universalidad; 1 pt: menciona geometría/borde; 1 pt: añade otra variable física."
        ),
        AcademicPractice(
            id = "cymatics_coupling",
            number = 4,
            title = "Punto de excitación y acoplamiento",
            shortTitle = "Excitador",
            objective = "Observar que la posición del excitador determina cuánto se acopla a un modo.",
            steps = listOf(
                "Selecciona un modo manual y activa sus nodos.",
                "Mueve el excitador hacia un antinodo y registra el acoplamiento.",
                "Muévelo cerca de una línea nodal y vuelve a comparar."
            ),
            question = "¿Por qué excitar exactamente sobre un nodo puede producir una respuesta débil para ese modo?",
            expectedAnswer = "La forma modal vale casi cero en el nodo, por lo que la fuerza aplicada allí tiene poco acoplamiento con ese modo.",
            hint = "El indicador de acoplamiento usa el valor de la forma modal en la posición del excitador.",
            actionLabel = "Preparar acoplamiento",
            defaultRubric = "2 pt: relaciona nodo y forma modal; 1 pt: explica acoplamiento; 1 pt: usa evidencia del indicador."
        )
    )

    fun byId(id: String): AcademicPractice = all.firstOrNull { it.id == id } ?: all.first()

    fun prepare(state: CymaticsState, practiceId: String): CymaticsState = when (practiceId) {
        "cymatics_resonance" -> state.copy(
            geometry = PlateGeometry.CIRCLE,
            boundary = PlateBoundary.CLAMPED,
            automaticMode = false,
            modeIndex = 2,
            dampingRatio = 0.018,
            sandEnabled = false,
            showNodes = true,
            visualStyle = CymaticsVisualStyle.SCIENTIFIC
        )
        "cymatics_geometry" -> state.copy(
            geometry = PlateGeometry.HEXAGON,
            boundary = PlateBoundary.SIMPLY_SUPPORTED,
            automaticMode = false,
            modeIndex = 5,
            sandEnabled = true,
            showNodes = true
        )
        "cymatics_coupling" -> state.copy(
            geometry = PlateGeometry.SQUARE,
            boundary = PlateBoundary.SIMPLY_SUPPORTED,
            automaticMode = false,
            modeIndex = 6,
            driveX = 0.32f,
            driveY = 0.26f,
            sandEnabled = false,
            showNodes = true,
            visualStyle = CymaticsVisualStyle.SCIENTIFIC
        )
        else -> state.copy(
            geometry = PlateGeometry.SQUARE,
            boundary = PlateBoundary.SIMPLY_SUPPORTED,
            automaticMode = false,
            modeIndex = 4,
            sandEnabled = true,
            showNodes = true,
            showAntinodes = true,
            palette = CymaticsPalette.CYAN_MAGENTA,
            variable = CymaticsVariable.DISPLACEMENT
        )
    }
}

data class AcademicLocks(
    val frequency: Boolean = true,
    val level: Boolean = false,
    val environment: Boolean = true,
    val sources: Boolean = false
) {
    val count: Int get() = listOf(frequency, level, environment, sources).count { it }
}

data class AcademicSceneBaseline(
    val frequencyHz: Double = 120.0,
    val levelDbSpl: Float = 70f,
    val phaseRadians: Float = 0f,
    val temperatureC: Double = 20.0,
    val humidityPercent: Float = 50f,
    val atmosphericPressureKPa: Float = 101.325f,
    val waveform: Waveform = Waveform.SINE,
    val harmonicMode: Boolean = false,
    val harmonicAmplitudes: List<Float> = listOf(1f, 0.45f, 0.25f, 0.15f) + List(12) { 0f },
    val secondarySource: SecondarySourceSettings = SecondarySourceSettings()
) {
    fun applyTo(state: AcousticState): AcousticState = state.copy(
        frequencyHz = frequencyHz,
        levelDbSpl = levelDbSpl,
        phaseRadians = phaseRadians,
        temperatureC = temperatureC,
        humidityPercent = humidityPercent,
        atmosphericPressureKPa = atmosphericPressureKPa,
        waveform = waveform,
        harmonicMode = harmonicMode,
        harmonicAmplitudes = harmonicAmplitudes,
        secondarySource = secondarySource
    )

    companion object {
        fun from(state: AcousticState): AcademicSceneBaseline = AcademicSceneBaseline(
            frequencyHz = state.frequencyHz,
            levelDbSpl = state.levelDbSpl,
            phaseRadians = state.phaseRadians,
            temperatureC = state.temperatureC,
            humidityPercent = state.humidityPercent,
            atmosphericPressureKPa = state.atmosphericPressureKPa,
            waveform = state.waveform,
            harmonicMode = state.harmonicMode,
            harmonicAmplitudes = state.harmonicAmplitudes,
            secondarySource = state.secondarySource
        )
    }
}

data class TeacherActivity(
    val active: Boolean = false,
    val practiceId: String = AcademicPractices.all.first().id,
    val locks: AcademicLocks = AcademicLocks(),
    val expectedAnswer: String = AcademicPractices.all.first().expectedAnswer,
    val rubric: String = AcademicPractices.all.first().defaultRubric,
    val baseline: AcademicSceneBaseline = AcademicSceneBaseline()
) {
    val practice: AcademicPractice get() = AcademicPractices.byId(practiceId)

    fun enforce(candidate: AcousticState): AcousticState {
        if (!active) return candidate
        var next = candidate
        if (locks.frequency) next = next.copy(frequencyHz = baseline.frequencyHz)
        if (locks.level) next = next.copy(levelDbSpl = baseline.levelDbSpl)
        if (locks.environment) {
            next = next.copy(
                temperatureC = baseline.temperatureC,
                humidityPercent = baseline.humidityPercent,
                atmosphericPressureKPa = baseline.atmosphericPressureKPa
            )
        }
        if (locks.sources) {
            next = next.copy(
                phaseRadians = baseline.phaseRadians,
                waveform = baseline.waveform,
                harmonicMode = baseline.harmonicMode,
                harmonicAmplitudes = baseline.harmonicAmplitudes,
                secondarySource = baseline.secondarySource
            )
        }
        return next
    }
}

data class AcademicExperienceState(
    val role: AcademicRole = AcademicRole.STUDENT,
    val completedPracticeIds: Set<String> = emptySet(),
    val answers: Map<String, String> = emptyMap(),
    val teacherActivity: TeacherActivity = TeacherActivity(),
    val cymaticsTeacherActivity: CymaticsTeacherActivity = CymaticsTeacherActivity(),
    val learning: LearningExperienceState = LearningExperienceState()
) {
    val completedCount: Int get() = completedPracticeIds.count { id -> AcademicPractices.all.any { it.id == id } }
    val progress: Float get() = completedCount.toFloat() / AcademicPractices.all.size
    val cymaticsCompletedCount: Int
        get() = completedPracticeIds.count { id -> CymaticsAcademicPractices.all.any { it.id == id } }
    val cymaticsProgress: Float
        get() = cymaticsCompletedCount.toFloat() / CymaticsAcademicPractices.all.size
    val totalLearningPracticeCount: Int
        get() = completedPracticeIds.count { it in LearningCatalog.practiceIds }
    val totalLearningProgress: Float
        get() = if (LearningCatalog.practices.isEmpty()) 0f
        else totalLearningPracticeCount.toFloat() / LearningCatalog.practices.size
}

data class CymaticsAcademicLocks(
    val geometryAndBoundary: Boolean = true,
    val materialAndDimensions: Boolean = true,
    val frequencyAndMode: Boolean = true,
    val exciter: Boolean = false,
    val visualAids: Boolean = false
) {
    val count: Int
        get() = listOf(geometryAndBoundary, materialAndDimensions, frequencyAndMode, exciter, visualAids).count { it }
}

data class CymaticsSceneBaseline(
    val state: CymaticsState = CymaticsState()
) {
    fun enforce(candidate: CymaticsState, locks: CymaticsAcademicLocks): CymaticsState {
        var next = candidate
        if (locks.geometryAndBoundary) {
            next = next.copy(geometry = state.geometry, boundary = state.boundary)
        }
        if (locks.materialAndDimensions) {
            next = next.copy(
                youngModulusPa = state.youngModulusPa,
                densityKgM3 = state.densityKgM3,
                poissonRatio = state.poissonRatio,
                thicknessM = state.thicknessM,
                characteristicSizeM = state.characteristicSizeM,
                dampingRatio = state.dampingRatio
            )
        }
        if (locks.frequencyAndMode) {
            next = next.copy(
                driveFrequencyHz = state.driveFrequencyHz,
                automaticMode = state.automaticMode,
                modeIndex = state.modeIndex,
                normalizedDrive = state.normalizedDrive
            )
        }
        if (locks.exciter) next = next.copy(driveX = state.driveX, driveY = state.driveY)
        if (locks.visualAids) {
            next = next.copy(
                showNodes = state.showNodes,
                showAntinodes = state.showAntinodes,
                showPhaseLines = state.showPhaseLines,
                contourDensity = state.contourDensity,
                showGrid = state.showGrid,
                showExciter = state.showExciter,
                sandEnabled = state.sandEnabled,
                motionPreset = state.motionPreset,
                motionIntensity = state.motionIntensity,
                modeTransitionSeconds = state.modeTransitionSeconds,
                cameraDriftEnabled = state.cameraDriftEnabled,
                cameraMotion = state.cameraMotion,
                orbitingLightEnabled = state.orbitingLightEnabled,
                resonanceAuraEnabled = state.resonanceAuraEnabled,
                atmosphericDustEnabled = state.atmosphericDustEnabled,
                sandFlowIntensity = state.sandFlowIntensity,
                stageEnabled = state.stageEnabled,
                nodalStreamsEnabled = state.nodalStreamsEnabled,
                energyPulsesEnabled = state.energyPulsesEnabled,
                postProcessingEnabled = state.postProcessingEnabled,
                bloomIntensity = state.bloomIntensity,
                variable = state.variable,
                probeEnabled = state.probeEnabled,
                probeX = state.probeX,
                probeY = state.probeY
            )
        }
        return next
    }
}

data class CymaticsTeacherActivity(
    val active: Boolean = false,
    val practiceId: String = CymaticsAcademicPractices.all.first().id,
    val locks: CymaticsAcademicLocks = CymaticsAcademicLocks(),
    val expectedAnswer: String = CymaticsAcademicPractices.all.first().expectedAnswer,
    val rubric: String = CymaticsAcademicPractices.all.first().defaultRubric,
    val baseline: CymaticsSceneBaseline = CymaticsSceneBaseline()
) {
    val practice: AcademicPractice get() = CymaticsAcademicPractices.byId(practiceId)
    fun enforce(candidate: CymaticsState): CymaticsState =
        if (active) baseline.enforce(candidate, locks) else candidate
}
