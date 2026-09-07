package com.soniclab3d.academic

internal object LearningFaqCatalog {
    private data class Seed(
        val question: String,
        val answer: String,
        val explanation: String,
        val tags: String,
        val guideId: String? = null,
        val practiceId: String? = null,
        val presetId: String? = null,
        val limitation: String? = null
    )

    val all: List<FaqEntry> = buildList {
        addAll(group(LearningPath.FUNDAMENTALS, "fund", fundamentals))
        addAll(group(LearningPath.SIGNALS, "sig", signals))
        addAll(group(LearningPath.VOICE_HEARING, "voice", voice))
        addAll(group(LearningPath.ROOMS_NOISE, "room", rooms))
        addAll(group(LearningPath.VIBRATIONS, "plate", vibrations))
        addAll(group(LearningPath.METHODS_DATA, "method", methods))
        addAll(group(LearningPath.METHODS_DATA, "app", appHelp))
    }

    private fun group(path: LearningPath, prefix: String, seeds: List<Seed>): List<FaqEntry> =
        seeds.mapIndexed { index, seed ->
            FaqEntry(
                id = "faq_${prefix}_${(index + 1).toString().padStart(2, '0')}",
                question = seed.question,
                shortAnswer = seed.answer,
                explanation = seed.explanation,
                path = path,
                tags = seed.tags.split(' ').filter { it.isNotBlank() }.toSet(),
                relatedGuideIds = listOfNotNull(seed.guideId),
                relatedPracticeIds = listOfNotNull(seed.practiceId),
                presetId = seed.presetId,
                limitation = seed.limitation
            )
        }

    private val fundamentals = listOf(
        Seed(
            "¿Las partículas del aire viajan desde el parlante hasta el oyente?",
            "No; en el modelo acústico oscilan alrededor de su posición de equilibrio.",
            "Lo que se propaga es la perturbación. Puede existir transporte medio en otros fenómenos, pero no es lo que representa esta onda lineal.",
            "particula aire propagacion equilibrio onda", "g01_oscillation", "wave_particle", "air_oscillation_120"
        ),
        Seed(
            "¿Frecuencia y tono percibido son exactamente lo mismo?",
            "No. La frecuencia es física; el tono es una percepción relacionada.",
            "En señales simples se correlacionan estrechamente, pero timbre, nivel y contexto también influyen en la percepción.",
            "frecuencia tono percepcion hz", "g02_frequency_wavelength"
        ),
        Seed(
            "¿Aumentar amplitud aumenta también la frecuencia?",
            "No en el modelo lineal de SonicLab.",
            "Amplitud y frecuencia son parámetros independientes: una cambia el tamaño de la oscilación y la otra la rapidez de los ciclos.",
            "amplitud frecuencia nivel independiente", "g03_pressure_level", "level_units", "air_level_relative"
        ),
        Seed(
            "¿Por qué cambia la longitud de onda con la temperatura?",
            "Porque cambia la velocidad del sonido mientras λ=c/f.",
            "Si la frecuencia permanece fija y c aumenta, la separación espacial entre ciclos también aumenta.",
            "temperatura velocidad longitud onda lambda", "g02_frequency_wavelength", "temperature_wavelength", "air_temperature"
        ),
        Seed(
            "¿La fase se mide en segundos?",
            "La fase suele expresarse como ángulo o fracción de ciclo.",
            "Un retardo temporal puede convertirse en fase para una frecuencia dada: φ=2πfΔt.",
            "fase radian grados retraso tiempo", "g04_superposition", "phase_map", "air_interference_phase"
        ),
        Seed(
            "¿Dos fuentes en oposición de fase se cancelan en toda la sala?",
            "No; la cancelación depende también de la diferencia de camino.",
            "La fase con que cada contribución llega a un punto cambia con posición, por lo que aparecen regiones de refuerzo y cancelación.",
            "interferencia cancelacion fuentes camino fase", "g04_superposition", "interference", "air_interference_phase"
        ),
        Seed(
            "¿Nodo de presión y nodo de desplazamiento significan lo mismo?",
            "No necesariamente; pueden aparecer en posiciones distintas.",
            "Presión y velocidad/desplazamiento son variables relacionadas, pero sus condiciones de máximo y mínimo dependen del sistema.",
            "nodo presion desplazamiento antinodo", "g05_standing_waves", "room_standing_modes", "air_room_mode"
        ),
        Seed(
            "¿La resonancia produce amplitud infinita?",
            "No en un sistema real ni en el modelo amortiguado.",
            "Las pérdidas y el acoplamiento limitan la respuesta. La solución ideal sin amortiguamiento no representa un experimento real sostenido.",
            "resonancia amplitud amortiguamiento infinito", "g06_resonance", "cymatics_resonance", "plate_resonance"
        ),
        Seed(
            "¿El nivel sonoro cambia la velocidad del sonido?",
            "No dentro del régimen lineal usado por la app.",
            "La velocidad se calcula principalmente con las propiedades del medio; los efectos no lineales de niveles extremos están fuera del alcance.",
            "nivel spl velocidad sonido lineal", "g02_frequency_wavelength"
        ),
        Seed(
            "¿La exageración visual cambia el resultado físico?",
            "No; solo amplifica lo dibujado.",
            "SonicLab separa la escala física o normalizada del factor gráfico para que movimientos diminutos sean visibles.",
            "escala visual exageracion fisica simulacion", "g03_pressure_level", "level_units",
            limitation = "Una imagen amplificada no debe interpretarse como desplazamiento real a escala."
        )
    )

    private val signals = listOf(
        Seed(
            "¿Qué muestra una FFT?",
            "Muestra cómo se distribuye una señal entre componentes de frecuencia.",
            "La gráfica depende del bloque temporal, frecuencia de muestreo, ventana y escala utilizada.",
            "fft espectro frecuencia señal", "g09_fft", "mic_noise_dbfs", "signal_spectrum"
        ),
        Seed(
            "¿Por qué los valores dBFS suelen ser negativos?",
            "Porque 0 dBFS es el máximo digital de referencia.",
            "Las señales de menor amplitud quedan por debajo de ese límite y se expresan con valores negativos.",
            "dbfs negativo digital escala", "g11_signal_chain", "mic_noise_dbfs", "mic_signal_chain"
        ),
        Seed(
            "¿Qué significa alcanzar 0 dBFS?",
            "Que la señal llegó al límite digital representable.",
            "Superarlo no entrega más información: suele producir recorte y distorsión en la cadena digital.",
            "cero dbfs clipping saturacion", "g11_signal_chain", presetId = "mic_signal_chain"
        ),
        Seed(
            "¿Por qué aparece energía alrededor de un pico espectral?",
            "Puede deberse a fuga espectral, ventana, modulación o inestabilidad.",
            "Una observación aislada no permite elegir una única causa; revisa duración, ventana y comportamiento temporal.",
            "fuga espectral leakage ventana pico", "g09_fft", presetId = "signal_spectrum"
        ),
        Seed(
            "¿Cómo mejoro la resolución en frecuencia?",
            "Observando un bloque temporal más largo, si fₛ se mantiene.",
            "Como Δf=fₛ/N, más muestras mejoran el espaciado de bins, aunque reducen rapidez de actualización.",
            "resolucion frecuencia bin muestras", "g09_fft"
        ),
        Seed(
            "¿Qué es aliasing?",
            "Es la representación errónea de una componente por muestreo insuficiente.",
            "Las frecuencias por encima de Nyquist pueden plegarse dentro del rango observable.",
            "aliasing muestreo nyquist plegamiento", "g10_sampling", presetId = "data_sampling",
            limitation = "La guía usa una señal sintética; no modifica la tasa física del micrófono."
        ),
        Seed(
            "¿Todo pico que no es la fundamental es un armónico?",
            "No. Un armónico debe estar relacionado aproximadamente por un múltiplo entero.",
            "Otros picos pueden ser ruido, modulación, resonancias, interferencia o fuentes externas.",
            "armonico fundamental ruido pico", "g08_harmonics", "harmonics", "signal_harmonics"
        ),
        Seed(
            "¿Cómo reconozco una señal recortada?",
            "La forma temporal se aplana en los extremos y aparecen componentes adicionales.",
            "Reduce ganancia o nivel de entrada y repite bajo las mismas condiciones.",
            "clipping recorte saturacion ganancia", "g11_signal_chain", presetId = "mic_signal_chain"
        ),
        Seed(
            "¿El teléfono puede aplicar ganancia automática?",
            "Sí; algunas rutas Android procesan la entrada automáticamente.",
            "Eso puede cambiar nivel y espectro entre dispositivos o aplicaciones, por lo que debe constar como limitación.",
            "agc ganancia automatica android microfono", "g11_signal_chain",
            limitation = "La app no controla toda la cadena de audio de cada fabricante."
        ),
        Seed(
            "¿Cuándo conviene CSV y cuándo JSON?",
            "CSV es cómodo para tablas; JSON conserva mejor estructura y metadatos.",
            "Para reproducibilidad, exporta el formato que mantenga parámetros, unidades y versión además de las muestras.",
            "csv json exportacion metadatos", "g12_signal_export", "reproducible_sweep", "data_export"
        )
    )

    private val voice = listOf(
        Seed(
            "¿SonicLab puede diagnosticar un problema de voz?",
            "No. Solo ofrece visualización y análisis educativo.",
            "Un diagnóstico exige evaluación profesional, protocolo clínico e instrumentos adecuados.",
            "voz diagnostico clinico fonoaudiologia", "g17_voice_spectrum", "educational_voice",
            limitation = "No usar para diagnóstico ni decisiones de tratamiento."
        ),
        Seed(
            "¿La frecuencia fundamental siempre es el pico más alto?",
            "No necesariamente.",
            "El filtrado del tracto, el micrófono y el ambiente pueden hacer que otro armónico tenga mayor amplitud.",
            "f0 fundamental pico dominante armonicos", "g14_voice_harmonics", "educational_voice", "voice_harmonics"
        ),
        Seed(
            "¿La app mide formantes clínicamente?",
            "No. Puede ayudar a comprender una envolvente, pero no realiza estimación clínica validada.",
            "Los formantes requieren métodos, configuración y revisión que están fuera de esta versión.",
            "formantes envolvente voz clinica", "g13_voice_source_filter",
            limitation = "No reportar valores de formantes como resultado clínico."
        ),
        Seed(
            "¿Un susurro tiene una fundamental clara?",
            "A menudo no presenta la periodicidad armónica típica de la fonación sonora.",
            "Su espectro puede ser más parecido a ruido filtrado, por lo que buscar F0 puede ser inapropiado.",
            "susurro f0 periodicidad ruido voz", "g14_voice_harmonics"
        ),
        Seed(
            "¿Por qué cambia el espectro al alejar el teléfono?",
            "Cambian nivel, relación señal/ruido y contribución del recinto.",
            "También pueden intervenir directividad y procesamiento automático de la entrada.",
            "distancia microfono espectro sala", "g16_microphone_distance", presetId = "mic_distance"
        ),
        Seed(
            "¿Cómo registro el ruido de fondo?",
            "Mantén la misma ruta y posición, sin la fuente de interés, durante un intervalo comparable.",
            "Documenta ambiente, duración y hora; no lo confundas con el ruido electrónico del micrófono.",
            "ruido fondo registro microfono", "g16_microphone_distance", "mic_noise_dbfs"
        ),
        Seed(
            "¿Puedo usar la calibración de otro teléfono?",
            "No. Una calibración es aplicable solo a la cadena y condiciones documentadas.",
            "Modelo, micrófono, ruta, accesorios y procesamiento pueden cambiar la relación entre dBFS y presión.",
            "calibracion telefono ruta perfil", "g15_dbfs_spl", "calibration_limits", "level_calibration"
        ),
        Seed(
            "¿dBFS y dB SPL se pueden comparar directamente?",
            "No sin una calibración trazable y aplicable.",
            "dBFS usa el límite digital; dB SPL usa una referencia de presión de 20 µPa.",
            "dbfs spl referencia presion", "g15_dbfs_spl", "calibration_limits"
        ),
        Seed(
            "¿Es seguro reproducir tonos fuertes para probar la app?",
            "No es necesario ni recomendable.",
            "Usa volumen bajo, limita duración y detén el audio ante molestia. No realices pruebas personales de umbral.",
            "seguridad tono volumen exposicion", "g18_hearing_safety",
            limitation = "La app no evalúa exposición segura individual."
        ),
        Seed(
            "¿Por qué cambia mi voz entre repeticiones?",
            "La producción vocal, distancia, nivel y ambiente nunca son perfectamente idénticos.",
            "Realiza varias repeticiones cómodas y describe variabilidad antes de interpretar una tendencia.",
            "voz repeticion variabilidad protocolo", "g17_voice_spectrum", "educational_voice"
        )
    )

    private val rooms = listOf(
        Seed(
            "¿Dos salas con igual volumen tienen los mismos modos?",
            "No. Importan Lx, Ly y Lz por separado.",
            "Los índices modales se dividen por cada dimensión; cambiar proporciones desplaza familias distintas.",
            "sala volumen dimensiones modos", "g20_room_modes", "room_dimensions", "room_dimensions"
        ),
        Seed(
            "¿Por qué los modos se notan más a baja frecuencia?",
            "Porque hay menos modos por intervalo y su separación puede ser perceptible.",
            "A frecuencias mayores aumenta la densidad modal y otras propiedades del recinto ganan importancia.",
            "baja frecuencia densidad modal recinto", "g21_low_frequency", "room_standing_modes"
        ),
        Seed(
            "¿Por qué una esquina puede mostrar más nivel?",
            "Algunos modos tienen máximos de presión cerca de fronteras y esquinas.",
            "La respuesta exacta depende del modo, fuente, posición y pérdidas.",
            "esquina pared presion maximo modo", "g21_low_frequency", presetId = "room_resonance"
        ),
        Seed(
            "¿Qué representa RT60?",
            "El tiempo asociado a una caída de 60 dB del campo reverberante bajo un método definido.",
            "En práctica puede estimarse desde un tramo menor; SonicLab solo ofrece un calculador educativo.",
            "rt60 reverberacion decaimiento", "g22_reverberation", "reverberation_absorption", "room_reverb",
            "No es una medición de RT60."
        ),
        Seed(
            "¿Un coeficiente de absorción sirve para toda frecuencia?",
            "No. Depende de banda, montaje, incidencia y material.",
            "Registra la fuente del dato y evita usar un único valor como propiedad universal.",
            "absorcion coeficiente frecuencia material", "g19_reflection_absorption"
        ),
        Seed(
            "¿Puedo hacer una evaluación legal de ruido con el teléfono?",
            "No con SonicLab.",
            "La evaluación normativa requiere instrumentación, calibración, ponderaciones y procedimientos exigidos por la regulación aplicable.",
            "ruido legal sonometro dosimetro normativa", "g23_environmental_noise",
            limitation = "Uso educativo; no emitir informes regulatorios."
        ),
        Seed(
            "¿Leq es el promedio aritmético de los dB?",
            "No. Se promedia energía en escala lineal y luego se vuelve a decibelios.",
            "Por eso los eventos altos pueden influir más que en un promedio directo de números dB.",
            "leq promedio energia decibel", "g23_environmental_noise", presetId = "noise_metrics"
        ),
        Seed(
            "¿Una sala muy reflectante siempre suena más fuerte?",
            "Puede aumentar energía reverberante, pero el resultado depende de fuente, posición y frecuencia.",
            "Reflexión, claridad, nivel y reverberación son conceptos relacionados pero no idénticos.",
            "reflexion nivel reverberacion claridad", "g19_reflection_absorption"
        ),
        Seed(
            "¿Una medición en un punto representa todo el recinto?",
            "No, especialmente cerca de modos de baja frecuencia.",
            "Usa varias posiciones y documenta coordenadas para describir variación espacial.",
            "punto medicion posicion recinto", "g24_room_comparison", "room_standing_modes"
        ),
        Seed(
            "¿Qué dimensión debo cambiar para mover un modo axial?",
            "La dimensión asociada a su índice no nulo.",
            "Por ejemplo, un modo (1,0,0) depende directamente de Lx en el recinto rectangular ideal.",
            "dimension axial indice modo", "g20_room_modes", "room_dimensions", "room_dimensions"
        )
    )

    private val vibrations = listOf(
        Seed(
            "¿Una frecuencia produce una figura universal de cimática?",
            "No. También importan geometría, bordes, material, dimensiones y excitación.",
            "La misma frecuencia puede quedar cerca de modos diferentes en dos placas.",
            "cimatica frecuencia figura universal geometria", "g26_plate_modes", "cymatics_geometry", "plate_modes"
        ),
        Seed(
            "¿Qué diferencia hay entre nodo y antinodo?",
            "Un nodo tiene respuesta modal cercana a cero; un antinodo, respuesta grande.",
            "La definición debe indicar qué variable y modo se están observando.",
            "nodo antinodo placa modo", "g26_plate_modes", "cymatics_nodes", "plate_modes"
        ),
        Seed(
            "¿La arena de SonicLab reproduce cada grano real?",
            "No. Es una visualización cualitativa de acumulación nodal.",
            "Los destinos derivan del campo modal, pero no existe un solver granular predictivo.",
            "arena grano cualitativa nodo", "g30_sand_nodes", "cymatics_nodes", "plate_sand",
            "No usar velocidad o trayectoria de granos como dato experimental."
        ),
        Seed(
            "¿Cambiar material modifica el patrón?",
            "Modifica las frecuencias propias; la forma normalizada depende principalmente de geometría y borde en el modelo ideal.",
            "Al excitar a una frecuencia fija puede cambiar qué modo domina y, por ello, el patrón observado.",
            "material frecuencia propia patron", "g28_plate_scaling", "plate_scaling"
        ),
        Seed(
            "¿Por qué el espesor afecta tanto la frecuencia?",
            "Porque la rigidez flexional incluye h³, mientras la masa superficial incluye h.",
            "El resultado modal combina ambos efectos; no es solo un cambio visual.",
            "espesor rigidez h3 frecuencia", "g28_plate_scaling", "plate_scaling", "plate_scaling"
        ),
        Seed(
            "¿Borde sujeto y apoyado son solo etiquetas visuales?",
            "No. Cargan bancos y condiciones diferentes.",
            "Las restricciones de desplazamiento y pendiente cambian autovalores y formas modales.",
            "borde sujeto apoyado condicion", "g26_plate_modes", "cymatics_geometry", "plate_modes"
        ),
        Seed(
            "¿Por qué excitar sobre un nodo da poca respuesta?",
            "Porque la forma modal vale casi cero allí y el acoplamiento es débil.",
            "Mover el excitador a un antinodo del mismo modo suele aumentar la respuesta normalizada.",
            "excitador nodo acoplamiento", "g27_plate_coupling", "cymatics_coupling", "plate_coupling"
        ),
        Seed(
            "¿f de excitación y f del modo son lo mismo?",
            "No. Una la impone la fuente y la otra pertenece al sistema.",
            "La diferencia entre ambas, junto con amortiguamiento y acoplamiento, determina la respuesta.",
            "frecuencia excitacion propia modo", "g29_plate_response", "cymatics_resonance", "plate_resonance"
        ),
        Seed(
            "¿Qué significa a.u. en la amplitud de placa?",
            "Significa unidades arbitrarias o normalizadas.",
            "Se usa porque no hay fuerza y calibración suficientes para reportar desplazamiento absoluto trazable.",
            "au unidades arbitrarias amplitud", "g29_plate_response",
            limitation = "No convertir a milímetros sin un modelo y calibración adicionales."
        ),
        Seed(
            "¿El color de la placa corresponde físicamente a la frecuencia?",
            "No. Es una codificación visual seleccionable.",
            "La paleta ayuda a leer fase, signo o energía, pero no afirma que el sonido tenga ese color.",
            "color frecuencia paleta visual", "g30_sand_nodes",
            limitation = "Color, bloom y escenario son presentación, no medición."
        )
    )

    private val methods = listOf(
        Seed(
            "¿Por qué conviene cambiar una variable por vez?",
            "Porque permite atribuir con más claridad el cambio observado.",
            "Si varias condiciones cambian simultáneamente, sus efectos quedan confundidos.",
            "variable control experimento hipotesis", "g31_control_variables", "reproducible_sweep", "method_control"
        ),
        Seed(
            "¿Cuál es la diferencia entre simulación y medición?",
            "La simulación calcula un modelo; la medición observa una señal con un instrumento.",
            "Ambas tienen supuestos y límites diferentes y deben etiquetarse por separado.",
            "simulacion medicion visualizacion modelo", "g34_reproducible_report"
        ),
        Seed(
            "¿Más cifras decimales significan un resultado mejor?",
            "No. Deben ser coherentes con resolución e incertidumbre.",
            "Mostrar decimales sin respaldo crea precisión aparente y dificulta interpretar el dato.",
            "decimales cifras significativas resolucion", "g32_uncertainty", presetId = "method_uncertainty"
        ),
        Seed(
            "¿Repetibilidad es lo mismo que exactitud?",
            "No. Resultados muy repetibles pueden compartir un sesgo.",
            "La exactitud requiere relación con una referencia adecuada; la repetibilidad describe dispersión bajo condiciones similares.",
            "repetibilidad exactitud sesgo incertidumbre", "g32_uncertainty"
        ),
        Seed(
            "¿Una captura de pantalla basta como evidencia?",
            "Rara vez. Debe acompañarse de parámetros, unidades, versión y contexto.",
            "Una imagen puede apoyar la observación, pero no reconstruir por sí sola el caso.",
            "captura evidencia metadatos", "g34_reproducible_report", "reproducible_sweep"
        ),
        Seed(
            "¿Qué metadatos mínimos debo guardar?",
            "Versión, modelo, fecha, parámetros, unidades, dominio y advertencias.",
            "En mediciones añade ruta, dispositivo, condiciones y calibración aplicable.",
            "metadatos version parametros unidades", "g12_signal_export", "reproducible_sweep", "data_export"
        ),
        Seed(
            "¿Por qué importa la versión del modelo?",
            "Porque una actualización puede cambiar ecuaciones, activos o algoritmos.",
            "Registrar la versión permite saber si dos resultados son realmente comparables.",
            "version modelo comparacion reproducibilidad", "g34_reproducible_report"
        ),
        Seed(
            "¿Para qué escribir una predicción antes de explorar?",
            "Obliga a expresar una relación que luego puede contrastarse.",
            "Así se distingue una explicación previa de una narración creada después de ver el resultado.",
            "prediccion hipotesis contraste", "g31_control_variables"
        ),
        Seed(
            "¿Qué hago si el resultado contradice mi expectativa?",
            "Revisa controles, supuestos y evidencia; no borres el resultado.",
            "Una contradicción puede revelar un error, una variable no controlada o una idea que debe revisarse.",
            "resultado contradiccion error revision", "g31_control_variables"
        ),
        Seed(
            "¿Una correlación en un barrido demuestra causalidad?",
            "No por sí sola.",
            "Necesitas un mecanismo, control de variables, repetición y evaluación de explicaciones alternativas.",
            "correlacion causalidad barrido", "g33_parameter_sweep", "reproducible_sweep", "method_sweep"
        )
    )

    private val appHelp = listOf(
        Seed(
            "¿Por qué SonicLab solicita permiso de micrófono?",
            "Solo lo solicita cuando activas el analizador o una captura que lo necesita.",
            "Puedes denegarlo o revocarlo desde Android; Aire y Placa simulados siguen funcionando sin ese permiso.",
            "app permiso microfono privacidad analizador"
        ),
        Seed(
            "¿Qué hago si la escena aparece negra o no se actualiza?",
            "Pausa y continúa, centra la cámara y cambia una vez entre Aire y Placa.",
            "Si persiste, reduce calidad, desactiva postprocesado en Placa y reinicia la app; documenta dispositivo y pasos para reproducirlo.",
            "app pantalla negra escena recuperar opengl rendimiento",
            limitation = "No se oculta un fallo del renderer como si fuera un resultado científico."
        ),
        Seed(
            "¿Cómo reduzco el consumo o una caída de FPS?",
            "Usa calidad adaptativa, menos partículas y el perfil Preciso o Reducir movimiento.",
            "La app informa métricas disponibles y estado térmico oficial, pero no inventa carga de GPU ni temperatura interna.",
            "app fps rendimiento calidad termico particulas"
        ),
        Seed(
            "¿Cómo comparto una actividad con otro dispositivo?",
            "El docente exporta un pack; el estudiante importa, trabaja y exporta una entrega.",
            "La devolución usa un archivo de feedback. No existe sincronización automática ni servidor en esta versión.",
            "app paquete importacion entrega feedback docente estudiante"
        ),
        Seed(
            "¿Por qué Dudas no respondió mi pregunta automáticamente?",
            "Porque solo muestra respuestas locales revisadas y no inventa una explicación.",
            "Prueba términos más breves o guarda la pregunta en Mis dudas para conservar su contexto y recibir feedback local.",
            "app dudas busqueda sin resultado curado"
        ),
        Seed(
            "¿Cómo regreso a la escena que tenía antes de una guía?",
            "Pulsa Volver a mi escena anterior mientras la instantánea esté disponible.",
            "La restauración actual conserva el estado durante la sesión; los textos y el paso de práctica sí persisten en DataStore.",
            "app volver escena preset restaurar estado",
            limitation = "Una muerte completa del proceso puede descartar la instantánea visual de la escena."
        )
    )
}
