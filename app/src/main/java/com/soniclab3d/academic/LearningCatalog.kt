package com.soniclab3d.academic

import java.text.Normalizer
import java.util.Locale

object LearningCatalog {
    private val physicsRefs = listOf(
        "OpenStax University Physics · Oscillations and Waves",
        "ISO 80000 · Magnitudes y unidades",
        "SonicLab 3D · Modelo científico y limitaciones"
    )
    private val signalRefs = listOf(
        "Oppenheim & Willsky · Signals and Systems",
        "SonicLab 3D · Analizador y exportación",
        "IEC 61672 · Alcance de sonómetros, solo como referencia de límites"
    )
    private val voiceRefs = listOf(
        "Titze · Principles of Voice Production",
        "SonicLab 3D · Analizador relativo y calibración",
        "Malla publicada Fonoaudiología UNAP · consulta 2026-08-15"
    )
    private val roomRefs = listOf(
        "Kuttruff · Room Acoustics",
        "SonicLab 3D · Modelo modal rectangular",
        "Malla publicada Arquitectura UNAP · consulta 2026-08-15"
    )
    private val plateRefs = listOf(
        "Leissa · Vibration of Plates",
        "SonicLab 3D · Modelo Kirchhoff–Love modal",
        "SonicLab 3D · Validación Cymatics"
    )
    private val methodRefs = listOf(
        "JCGM 100 · Expresión de incertidumbre de medida",
        "SonicLab 3D · Formato de exportación reproducible",
        "Método científico experimental universitario"
    )

    val guides: List<LearningGuide> = listOf(
        g(
            "g01_oscillation", "Oscilación, equilibrio, amplitud, periodo y fase",
            LearningPath.FUNDAMENTALS, LearningLevel.FOUNDATIONS, LearningDomain.AIR, "1–2", 18,
            "Distinguir la posición de equilibrio de la posición instantánea de una partícula.",
            "Una partícula del medio oscila localmente mientras la perturbación transporta fase y energía.",
            listOf("equilibrio", "amplitud", "periodo", "fase"), "T = 1/f",
            listOf("Observa una partícula a 120 Hz.", "Reduce el reloj visual.", "Compara equilibrio y desplazamiento."),
            "¿La partícula viaja junto al frente de onda?", "No; oscila alrededor de su equilibrio.",
            "Confundir movimiento de fase con transporte permanente de materia.", "air_oscillation_120",
            ScientificKind.SIMULATION, "El desplazamiento dibujado está amplificado para poder verse.", physicsRefs
        ),
        g(
            "g02_frequency_wavelength", "Frecuencia, longitud de onda y velocidad",
            LearningPath.FUNDAMENTALS, LearningLevel.FOUNDATIONS, LearningDomain.AIR, "1–2", 20,
            "Relacionar frecuencia y longitud de onda cuando la velocidad del medio se mantiene.",
            "Al aumentar la frecuencia, caben más ciclos por metro y la longitud de onda disminuye.",
            listOf("frecuencia", "longitud de onda", "velocidad"), "λ = c/f",
            listOf("Registra λ a 120 Hz.", "Cambia a 1 kHz sin variar el ambiente.", "Compara el cociente c/f."),
            "¿Qué variable debe mantenerse para afirmar proporcionalidad inversa?", "La velocidad de propagación c.",
            "Comparar dos casos que también cambiaron la temperatura.", "air_frequency_120",
            ScientificKind.SIMULATION, "El modelo usa propiedades ideales del aire declaradas en la app.", physicsRefs
        ),
        g(
            "g03_pressure_level", "Presión acústica, intensidad y niveles",
            LearningPath.FUNDAMENTALS, LearningLevel.APPLICATION, LearningDomain.AIR, "2–4", 24,
            "Diferenciar presión instantánea, amplitud eficaz, intensidad y nivel logarítmico.",
            "Las magnitudes lineales y sus niveles en decibelios no son intercambiables.",
            listOf("presión", "intensidad", "RMS", "decibel"), "Lₚ = 20 log₁₀(p/p₀)",
            listOf("Compara 60 y 70 dB en la simulación.", "Observa la amplitud relativa.", "Identifica la referencia utilizada."),
            "¿Sumar 10 dB duplica la presión?", "No; multiplica aproximadamente por 3,16 la presión eficaz.",
            "Tratar el decibel como una unidad lineal sin referencia.", "air_level_relative",
            ScientificKind.EDUCATIONAL, "El nivel simulado no es una lectura del micrófono ni un SPL certificado.", physicsRefs
        ),
        g(
            "g04_superposition", "Superposición, fase e interferencia",
            LearningPath.FUNDAMENTALS, LearningLevel.APPLICATION, LearningDomain.AIR, "2–4", 24,
            "Predecir refuerzo y cancelación a partir de amplitud, fase y posición.",
            "En un modelo lineal, las presiones con signo se suman punto por punto.",
            listOf("superposición", "fase", "constructiva", "destructiva"), "p = p₁ + p₂",
            listOf("Activa dos fuentes iguales.", "Compara fase 0 y π.", "Mueve la sonda por nodos y antinodos."),
            "¿La interferencia destructiva ocurre igual en todo el espacio?", "No; depende de fase y diferencia de camino.",
            "Suponer que dos fuentes en oposición se cancelan en todos los puntos.", "air_interference_phase",
            ScientificKind.SIMULATION, "Se usa superposición lineal ideal y fuentes simplificadas.", physicsRefs
        ),
        g(
            "g05_standing_waves", "Ondas estacionarias, nodos y antinodos",
            LearningPath.FUNDAMENTALS, LearningLevel.APPLICATION, LearningDomain.AIR, "2–5", 25,
            "Reconocer patrones estacionarios como resultado de componentes contrapropagantes.",
            "Los nodos conservan amplitud pequeña y los antinodos concentran la respuesta del modo.",
            listOf("onda estacionaria", "nodo", "antinodo", "modo"), "fₙ = nc/(2L)",
            listOf("Selecciona un recinto rectangular.", "Excita cerca de un modo.", "Compara corte de presión y sonda."),
            "¿Un nodo significa que nunca existe presión acústica?", "No necesariamente; depende de la variable modal observada.",
            "Usar nodo de desplazamiento y nodo de presión como sinónimos.", "air_room_mode",
            ScientificKind.SIMULATION, "El recinto es rectangular e idealizado; no incluye todos los materiales reales.", physicsRefs
        ),
        g(
            "g06_resonance", "Resonancia, amortiguamiento y factor de calidad",
            LearningPath.FUNDAMENTALS, LearningLevel.PROJECT, LearningDomain.MIXED, "3–7", 28,
            "Interpretar el máximo de respuesta y el efecto del amortiguamiento.",
            "La resonancia depende de frecuencias propias, acoplamiento y pérdidas; no es amplificación ilimitada.",
            listOf("resonancia", "amortiguamiento", "ancho de banda", "Q"), "Q ≈ f₀/Δf",
            listOf("Barre alrededor de una frecuencia propia.", "Aumenta el amortiguamiento.", "Compara altura y ancho del máximo."),
            "¿Más amortiguamiento vuelve el máximo más agudo?", "No; normalmente reduce y ensancha la respuesta.",
            "Confundir frecuencia de excitación con frecuencia propia.", "plate_resonance",
            ScientificKind.SIMULATION, "La respuesta es modal normalizada y no predice amplitud absoluta sin fuerza trazable.", physicsRefs
        ),

        g(
            "g07_time_frequency", "Dominio temporal y dominio frecuencial",
            LearningPath.SIGNALS, LearningLevel.FOUNDATIONS, LearningDomain.DATA, "2–4", 20,
            "Leer una misma señal como evolución temporal y como distribución espectral.",
            "El tiempo muestra cuándo cambia una señal; el espectro muestra qué componentes la forman.",
            listOf("señal", "tiempo", "frecuencia", "espectro"), "x(t) ↔ X(f)",
            listOf("Genera una senoide.", "Activa armónicos.", "Relaciona forma temporal con picos espectrales."),
            "¿El espectro conserva por sí solo toda la fase temporal?", "No en una gráfica de magnitud únicamente.",
            "Leer un espectro de magnitud como si mostrara directamente el instante de un evento.", "signal_time_frequency",
            ScientificKind.EDUCATIONAL, "La vista espectral usa resolución finita.", signalRefs
        ),
        g(
            "g08_harmonics", "Fundamental, armónicos y timbre",
            LearningPath.SIGNALS, LearningLevel.FOUNDATIONS, LearningDomain.AIR, "2–5", 22,
            "Relacionar múltiplos de la fundamental con la forma de onda y el timbre.",
            "Señales con igual fundamental pueden diferir por amplitudes armónicas y envolvente.",
            listOf("fundamental", "armónico", "timbre", "forma de onda"), "fₙ = n f₀",
            listOf("Fija f₀ en 120 Hz.", "Modifica 2f, 3f y 4f.", "Compara espectro y percepción con volumen bajo."),
            "¿Misma fundamental implica mismo timbre?", "No; la composición armónica puede ser distinta.",
            "Llamar armónico a cualquier pico no relacionado con un múltiplo.", "signal_harmonics",
            ScientificKind.SIMULATION, "El sintetizador es educativo y no reproduce una fuente real completa.", signalRefs
        ),
        g(
            "g09_fft", "Lectura de espectros y resolución FFT",
            LearningPath.SIGNALS, LearningLevel.APPLICATION, LearningDomain.DATA, "3–6", 26,
            "Interpretar picos, piso de ruido y resolución sin atribuir precisión inexistente.",
            "La duración, frecuencia de muestreo y ventana condicionan lo que una FFT puede separar.",
            listOf("FFT", "bin", "ventana", "resolución"), "Δf = fₛ/N",
            listOf("Observa una señal estable.", "Compara dos frecuencias cercanas.", "Relaciona Δf con el tamaño de bloque."),
            "¿Un pico ancho siempre significa una fuente inestable?", "No; también puede deberse a resolución y ventana.",
            "Reportar más decimales que los permitidos por la resolución.", "signal_spectrum",
            ScientificKind.MIXED, "El micrófono y la FFT del teléfono no son un analizador certificado.", signalRefs
        ),
        g(
            "g10_sampling", "Muestreo y aliasing",
            LearningPath.SIGNALS, LearningLevel.APPLICATION, LearningDomain.DATA, "3–6", 24,
            "Predecir cuándo una frecuencia muestreada se representa como otra más baja.",
            "Una señal por encima de Nyquist puede plegarse en el espectro discreto.",
            listOf("muestreo", "Nyquist", "aliasing", "reconstrucción"), "fₙᵧq = fₛ/2",
            listOf("Usa la señal sintética.", "Baja fₛ manteniendo f.", "Identifica la frecuencia aparente."),
            "¿Aumentar N sin cambiar fₛ eleva Nyquist?", "No; mejora resolución, pero Nyquist depende de fₛ.",
            "Confundir resolución espectral con frecuencia máxima representable.", "data_sampling",
            ScientificKind.EDUCATIONAL, "La demostración es sintética y no cambia el muestreo físico del micrófono.", signalRefs
        ),
        g(
            "g11_signal_chain", "Cadena de señal, ganancia, saturación y ruido",
            LearningPath.SIGNALS, LearningLevel.APPLICATION, LearningDomain.DATA, "3–7", 25,
            "Identificar cómo sensor, ganancia y conversión afectan una lectura.",
            "Una cadena puede introducir piso de ruido, recorte y respuesta en frecuencia.",
            listOf("sensor", "ganancia", "clipping", "ruido"), null,
            listOf("Observa el piso de ruido.", "Aumenta la fuente sin cambiar distancia.", "Busca aplanamiento o saturación."),
            "¿Más ganancia siempre mejora la relación señal/ruido?", "No; puede amplificar ruido o provocar recorte.",
            "Interpretar una señal recortada como mayor fidelidad.", "mic_signal_chain",
            ScientificKind.MEASUREMENT, "La respuesta del hardware Android puede incluir procesamiento automático desconocido.", signalRefs
        ),
        g(
            "g12_signal_export", "Exportación reproducible de señales",
            LearningPath.SIGNALS, LearningLevel.PROJECT, LearningDomain.DATA, "4–10", 26,
            "Crear un registro que otra persona pueda interpretar sin ver la pantalla original.",
            "Los valores requieren metadatos, unidades, configuración y advertencias para ser reproducibles.",
            listOf("CSV", "JSON", "metadatos", "reproducibilidad"), null,
            listOf("Prepara una escena.", "Exporta CSV y JSON.", "Comprueba versión, unidades y parámetros."),
            "¿Una tabla sin unidades es autocontenida?", "No; falta información para interpretar sus columnas.",
            "Guardar solo una captura y perder los parámetros originales.", "data_export",
            ScientificKind.MIXED, "La exportación documenta el modelo, no convierte la simulación en medición.", signalRefs
        ),

        g(
            "g13_voice_source_filter", "Producción de voz y modelo fuente-filtro",
            LearningPath.VOICE_HEARING, LearningLevel.FOUNDATIONS, LearningDomain.DATA, "2–5", 24,
            "Explicar de forma introductoria la fuente glótica y el filtrado del tracto vocal.",
            "La fuente aporta periodicidad y el tracto modifica la envolvente espectral.",
            listOf("fuente glótica", "tracto vocal", "resonancia", "envolvente"), "V(f) = G(f)H(f)",
            listOf("Genera una fundamental educativa.", "Modifica armónicos.", "Distingue fuente y filtro conceptual."),
            "¿Un pico espectral aislado diagnostica una alteración vocal?", "No; esta visualización no es diagnóstica.",
            "Usar el modelo introductorio como descripción clínica completa.", "voice_source_filter",
            ScientificKind.EDUCATIONAL, "No estima formantes clínicos ni diagnostica voz.", voiceRefs
        ),
        g(
            "g14_voice_harmonics", "Fundamental y armónicos de una señal de voz",
            LearningPath.VOICE_HEARING, LearningLevel.APPLICATION, LearningDomain.DATA, "2–6", 23,
            "Distinguir frecuencia fundamental, armónicos y envolvente en un ejemplo de voz.",
            "Los armónicos aparecen cerca de múltiplos; su distribución cambia con fuente y filtrado.",
            listOf("F0", "armónicos", "timbre", "envolvente"), "fₙ ≈ nF₀",
            listOf("Habla a nivel cómodo.", "Observa el espectro relativo.", "Compara una vocal sostenida sin forzar."),
            "¿El armónico de mayor amplitud debe ser siempre F0?", "No necesariamente.",
            "Confundir el pico dominante con un diagnóstico de tono o patología.", "voice_harmonics",
            ScientificKind.MEASUREMENT, "El análisis depende del micrófono, ambiente y procesamiento del teléfono.", voiceRefs
        ),
        g(
            "g15_dbfs_spl", "dBFS, nivel relativo y dB SPL",
            LearningPath.VOICE_HEARING, LearningLevel.APPLICATION, LearningDomain.DATA, "2–6", 26,
            "Distinguir una escala digital relativa de una presión acústica calibrada.",
            "0 dBFS representa el límite digital; dB SPL requiere referencia física y calibración aplicable.",
            listOf("dBFS", "dB SPL", "calibración", "referencia"), "Lₚ = 20 log₁₀(p/20 µPa)",
            listOf("Observa dBFS sin calibración.", "Revisa la ruta de entrada.", "Lee qué cambia al aplicar un perfil válido."),
            "¿Un valor dBFS puede copiarse como dB SPL?", "No; son escalas distintas.",
            "Sumar un desplazamiento arbitrario y llamarlo calibración universal.", "level_calibration",
            ScientificKind.MEASUREMENT, "SonicLab no es un sonómetro ni audiómetro certificado.", voiceRefs
        ),
        g(
            "g16_microphone_distance", "Micrófono, distancia, orientación y ruido",
            LearningPath.VOICE_HEARING, LearningLevel.APPLICATION, LearningDomain.DATA, "2–7", 22,
            "Controlar condiciones básicas al comparar dos capturas.",
            "Distancia, orientación, sala y control automático pueden alterar nivel y espectro.",
            listOf("distancia", "orientación", "ruido de fondo", "repetibilidad"), null,
            listOf("Fija una distancia.", "Registra el ruido de fondo.", "Repite sin cambiar la ruta de entrada."),
            "¿Dos teléfonos deben entregar el mismo espectro absoluto?", "No; sus cadenas de entrada pueden ser distintas.",
            "Comparar capturas con posiciones y rutas diferentes.", "mic_distance",
            ScientificKind.MEASUREMENT, "No hay corrección universal de respuesta del micrófono.", voiceRefs
        ),
        g(
            "g17_voice_spectrum", "Lectura no diagnóstica de un espectro de voz",
            LearningPath.VOICE_HEARING, LearningLevel.PROJECT, LearningDomain.DATA, "4–9", 28,
            "Describir observaciones acústicas sin convertirlas en conclusiones clínicas.",
            "Una captura permite describir picos y variación, pero requiere protocolo y herramientas clínicas para evaluar salud.",
            listOf("descripción", "variabilidad", "protocolo", "límite clínico"), null,
            listOf("Registra tres repeticiones cómodas.", "Compara tendencias, no diagnósticos.", "Documenta ambiente y distancia."),
            "¿La app puede confirmar una patología vocal?", "No.",
            "Etiquetar una persona a partir de una sola gráfica.", "voice_spectrum",
            ScientificKind.MEASUREMENT, "Uso exclusivamente educativo; ante una inquietud corresponde evaluación profesional.", voiceRefs
        ),
        g(
            "g18_hearing_safety", "Seguridad auditiva y límites de SonicLab",
            LearningPath.VOICE_HEARING, LearningLevel.FOUNDATIONS, LearningDomain.DATA, "1–10", 18,
            "Usar tonos y audio de manera conservadora y comprender qué no evalúa la app.",
            "La exposición depende de nivel, duración y condiciones; la app no mide dosis certificada.",
            listOf("nivel", "duración", "exposición", "seguridad"), null,
            listOf("Mantén audio apagado al iniciar.", "Usa volumen bajo.", "No realices pruebas de umbral personal."),
            "¿Una frecuencia visible sin calibración indica que es segura?", "No; falta conocer el nivel real y la exposición.",
            "Confundir frecuencia con intensidad o dosis.", null,
            ScientificKind.EDUCATIONAL, "No sustituye orientación clínica ni normativa ocupacional.", voiceRefs
        ),

        g(
            "g19_reflection_absorption", "Reflexión, absorción y transmisión",
            LearningPath.ROOMS_NOISE, LearningLevel.FOUNDATIONS, LearningDomain.AIR, "2–5", 22,
            "Diferenciar destinos de la energía cuando una onda encuentra una superficie.",
            "Una parte puede reflejarse, otra disiparse y otra transmitirse; los coeficientes dependen de frecuencia y montaje.",
            listOf("reflexión", "absorción", "transmisión", "material"), "α = Eabs/Einc",
            listOf("Compara una frontera ideal.", "Cambia la frecuencia.", "Anota qué simplificaciones usa el modelo."),
            "¿Absorber significa bloquear toda transmisión?", "No; son fenómenos diferentes.",
            "Usar un único coeficiente para toda frecuencia y construcción.", "room_reflection",
            ScientificKind.EDUCATIONAL, "El modelo de recinto actual no resuelve cada material y trayectoria real.", roomRefs
        ),
        g(
            "g20_room_modes", "Modos de recinto y efecto de dimensiones",
            LearningPath.ROOMS_NOISE, LearningLevel.APPLICATION, LearningDomain.AIR, "3–7", 26,
            "Relacionar dimensiones rectangulares con frecuencias modales.",
            "Cambiar una dimensión desplaza las familias axial, tangencial y oblicua.",
            listOf("modo axial", "dimensión", "frecuencia propia", "recinto"), "f = c/2·√[(n/Lx)²+(m/Ly)²+(p/Lz)²]",
            listOf("Registra los primeros modos.", "Duplica una dimensión.", "Compara qué índices cambian más."),
            "¿Dos salas con igual volumen tienen idénticos modos?", "No; importan sus dimensiones individuales.",
            "Reducir el problema al volumen total.", "room_dimensions",
            ScientificKind.SIMULATION, "Solo recintos rectangulares ideales; no incluye geometría compleja.", roomRefs
        ),
        g(
            "g21_low_frequency", "Resonancias de baja frecuencia en recintos",
            LearningPath.ROOMS_NOISE, LearningLevel.APPLICATION, LearningDomain.AIR, "3–8", 25,
            "Explicar por qué la respuesta varía con posición cerca de modos discretos.",
            "A baja frecuencia, pocos modos pueden producir máximos y mínimos espaciales marcados.",
            listOf("baja frecuencia", "posición", "máximo", "mínimo"), null,
            listOf("Excita un modo bajo.", "Mueve la sonda.", "Compara centro, pared y esquina."),
            "¿Una lectura en un punto representa toda la sala?", "No; el campo puede variar espacialmente.",
            "Generalizar un único punto de medición.", "room_resonance",
            ScientificKind.SIMULATION, "Las pérdidas y fuentes se simplifican.", roomRefs
        ),
        g(
            "g22_reverberation", "Reverberación con un modelo educativo",
            LearningPath.ROOMS_NOISE, LearningLevel.APPLICATION, LearningDomain.DATA, "4–8", 27,
            "Calcular una estimación y reconocer cuándo deja de ser válida.",
            "Sabine relaciona volumen y absorción equivalente bajo supuestos de campo difuso.",
            listOf("RT60", "Sabine", "volumen", "absorción equivalente"), "RT60 ≈ 0,161 V/A",
            listOf("Define V y superficies.", "Asigna absorciones por banda.", "Compara el resultado al cambiar A."),
            "¿Sabine es exacto para cualquier sala pequeña?", "No; sus supuestos pueden fallar.",
            "Usar absorción promedio sin indicar frecuencia.", "room_reverb",
            ScientificKind.EDUCATIONAL, "Estimación idealizada, no medición de RT60.", roomRefs
        ),
        g(
            "g23_environmental_noise", "Ruido ambiental y ocupacional: métricas y límites",
            LearningPath.ROOMS_NOISE, LearningLevel.PROJECT, LearningDomain.DATA, "4–10", 29,
            "Distinguir nivel instantáneo, equivalente, máximo y dosis sin fingir certificación.",
            "Cada métrica resume de forma distinta una señal variable y depende del protocolo.",
            listOf("Leq", "Lmax", "ponderación", "dosis"), "Lₑq = 10 log₁₀[(1/T)∫10^(L(t)/10)dt]",
            listOf("Observa una serie simulada.", "Compara promedio lineal y Leq.", "Documenta intervalo y ponderación."),
            "¿El promedio aritmético de dB es siempre Leq?", "No.",
            "Usar el teléfono como dosímetro legal.", "noise_metrics",
            ScientificKind.EDUCATIONAL, "No reemplaza equipos, calibración ni procedimientos normativos.", roomRefs
        ),
        g(
            "g24_room_comparison", "Diseño de una comparación entre recintos",
            LearningPath.ROOMS_NOISE, LearningLevel.PROJECT, LearningDomain.MIXED, "5–10", 30,
            "Diseñar un protocolo que cambie una variable y conserve trazabilidad.",
            "Comparar exige mismas posiciones, fuente, ruta, duración y metadatos relevantes.",
            listOf("protocolo", "control de variables", "posición", "trazabilidad"), null,
            listOf("Formula una hipótesis.", "Define posiciones repetibles.", "Exporta resultados y limitaciones."),
            "¿Dos capturas de distinto teléfono son una comparación controlada?", "No sin caracterización y control adicionales.",
            "Cambiar simultáneamente fuente, distancia y sala.", "room_compare",
            ScientificKind.MIXED, "Las observaciones del teléfono son educativas si no hay instrumentación calibrada.", roomRefs
        ),

        g(
            "g25_air_plate", "Onda en aire y vibración transversal de placa",
            LearningPath.VIBRATIONS, LearningLevel.FOUNDATIONS, LearningDomain.MIXED, "2–5", 20,
            "Distinguir propagación longitudinal en aire de deformación transversal de una placa.",
            "Ambos sistemas presentan modos y resonancia, pero sus variables y ecuaciones no son iguales.",
            listOf("aire", "placa", "longitudinal", "transversal"), null,
            listOf("Observa Aire a 120 Hz.", "Cambia a Placa.", "Compara dirección de desplazamiento y unidades."),
            "¿Una figura de placa es el patrón de presión del aire?", "No.",
            "Mezclar partículas de aire con arena sobre una placa.", "mixed_air_plate",
            ScientificKind.SIMULATION, "Son dos dominios conectados pedagógicamente, no un único solver acoplado.", plateRefs
        ),
        g(
            "g26_plate_modes", "Formas modales, frecuencias propias y bordes",
            LearningPath.VIBRATIONS, LearningLevel.APPLICATION, LearningDomain.PLATE, "3–7", 26,
            "Relacionar geometría y condición de borde con una forma propia.",
            "Una forma modal es una solución espacial del problema de autovalores bajo supuestos declarados.",
            listOf("autovalor", "forma modal", "borde", "geometría"), "D∇⁴φ = ρhω²φ",
            listOf("Selecciona cuadrado apoyado.", "Fija un índice modal.", "Cambia geometría o borde."),
            "¿El modo 5 de dos geometrías debe verse igual?", "No.",
            "Tratar el índice como una figura universal.", "plate_modes",
            ScientificKind.SIMULATION, "Banco modal numérico idealizado con resolución finita.", plateRefs
        ),
        g(
            "g27_plate_coupling", "Punto de excitación y acoplamiento modal",
            LearningPath.VIBRATIONS, LearningLevel.APPLICATION, LearningDomain.PLATE, "3–8", 24,
            "Predecir respuesta débil al excitar cerca de un nodo del modo seleccionado.",
            "El acoplamiento depende del valor de la forma modal en el punto donde actúa la fuerza.",
            listOf("excitador", "acoplamiento", "nodo", "antinodo"), "Aᵢ ∝ φᵢ(xd,yd)",
            listOf("Fija un modo.", "Mueve el excitador a un antinodo.", "Compáralo con una línea nodal."),
            "¿Una fuerza grande siempre excita por igual todos los modos?", "No; importa el acoplamiento espacial.",
            "Ignorar la posición del excitador.", "plate_coupling",
            ScientificKind.SIMULATION, "La fuerza absoluta no está calibrada; se muestra respuesta normalizada.", plateRefs
        ),
        g(
            "g28_plate_scaling", "Material, espesor y dimensión de placa",
            LearningPath.VIBRATIONS, LearningLevel.APPLICATION, LearningDomain.PLATE, "4–8", 27,
            "Interpretar cómo rigidez, masa y escala geométrica mueven las frecuencias propias.",
            "La rigidez flexional crece fuertemente con el espesor y la dimensión cambia la escala espacial.",
            listOf("rigidez", "densidad", "espesor", "dimensión"), "D = Eh³/[12(1−ν²)]",
            listOf("Fija modo y borde.", "Duplica espesor.", "Cambia dimensión conservando lo demás."),
            "¿El espesor modifica solo la apariencia visual?", "No; modifica la rigidez y frecuencias calculadas.",
            "Cambiar varias propiedades a la vez.", "plate_scaling",
            ScientificKind.SIMULATION, "Presets ideales; verifica propiedades reales antes de comparar un material.", plateRefs
        ),
        g(
            "g29_plate_response", "Curva de respuesta y amortiguamiento de placa",
            LearningPath.VIBRATIONS, LearningLevel.PROJECT, LearningDomain.PLATE, "4–9", 28,
            "Separar frecuencia de excitación, frecuencia propia y respuesta normalizada.",
            "La respuesta crece cerca del modo cuando existe acoplamiento y queda limitada por amortiguamiento.",
            listOf("respuesta modal", "resonancia", "amortiguamiento", "barrido"), null,
            listOf("Selecciona un modo.", "Barre alrededor de fmodo.", "Repite con dos valores de ζ."),
            "¿Ajustar a resonancia garantiza un patrón fuerte?", "No si el acoplamiento es casi nulo.",
            "Omitir el amortiguamiento o el punto excitador.", "plate_resonance",
            ScientificKind.SIMULATION, "Amplitud modal normalizada en a.u.", plateRefs
        ),
        g(
            "g30_sand_nodes", "Arena nodal: representación y límites",
            LearningPath.VIBRATIONS, LearningLevel.PROJECT, LearningDomain.PLATE, "3–10", 23,
            "Explicar por qué la arena visual converge hacia nodos sin presentarla como dinámica granular exacta.",
            "Los destinos se derivan del campo nodal y la relajación GPU es cualitativa.",
            listOf("arena", "nodo", "relajación", "visualización"), null,
            listOf("Oculta la arena.", "Comprueba el campo modal.", "Activa arena y compara sus destinos."),
            "¿La velocidad de cada grano predice un experimento real?", "No.",
            "Usar la arena como variable cuantitativa.", "plate_sand",
            ScientificKind.VISUALIZATION, "No es un solver granular predictivo ni una medición.", plateRefs
        ),

        g(
            "g31_control_variables", "Hipótesis y control de variables",
            LearningPath.METHODS_DATA, LearningLevel.FOUNDATIONS, LearningDomain.MIXED, "1–5", 22,
            "Formular una predicción comprobable y cambiar una variable por vez.",
            "Una comparación interpretable conserva las condiciones que no forman parte de la pregunta.",
            listOf("hipótesis", "variable independiente", "control", "evidencia"), null,
            listOf("Escribe una predicción.", "Elige una variable.", "Registra qué mantendrás constante."),
            "¿Cambiar frecuencia y temperatura permite aislar su efecto?", "No.",
            "Modificar varios parámetros y atribuir todo el cambio a uno.", "method_control",
            ScientificKind.MIXED, "La calidad de la conclusión depende del diseño y de los límites del modelo.", methodRefs
        ),
        g(
            "g32_uncertainty", "Incertidumbre, repetibilidad y cifras significativas",
            LearningPath.METHODS_DATA, LearningLevel.APPLICATION, LearningDomain.DATA, "3–8", 27,
            "Reportar variación y resolución sin una precisión aparente excesiva.",
            "Repetir y documentar condiciones permite separar dispersión de resolución y sesgo conocido.",
            listOf("incertidumbre", "repetibilidad", "resolución", "sesgo"), null,
            listOf("Repite tres observaciones.", "Calcula rango o dispersión.", "Redondea según resolución."),
            "¿Más decimales significan automáticamente mayor exactitud?", "No.",
            "Confundir precisión numérica con validez física.", "method_uncertainty",
            ScientificKind.MIXED, "La app no calcula una incertidumbre metrológica completa.", methodRefs
        ),
        g(
            "g33_parameter_sweep", "Barrido de parámetros y lectura de gráficos",
            LearningPath.METHODS_DATA, LearningLevel.APPLICATION, LearningDomain.MIXED, "3–9", 30,
            "Construir una serie ordenada y reconocer tendencias, máximos y regiones de cambio.",
            "Un barrido conserva el resto de las condiciones y registra cada punto con metadatos.",
            listOf("barrido", "serie", "gráfico", "tendencia"), null,
            listOf("Define inicio, fin y paso.", "Registra respuesta.", "Grafica y señala límites del rango."),
            "¿Un máximo visual prueba causalidad por sí solo?", "No.",
            "Elegir solo puntos que confirman la expectativa.", "method_sweep",
            ScientificKind.SIMULATION, "La tendencia corresponde al modelo y rango seleccionados.", methodRefs
        ),
        g(
            "g34_reproducible_report", "Informe reproducible y portafolio",
            LearningPath.METHODS_DATA, LearningLevel.PROJECT, LearningDomain.MIXED, "4–10", 32,
            "Comunicar pregunta, método, resultados, límites y archivos suficientes para repetir el caso.",
            "Un informe sólido separa observación, interpretación y alcance.",
            listOf("informe", "metadatos", "evidencia", "limitaciones"), null,
            listOf("Exporta el caso.", "Añade hipótesis y conclusión.", "Comprueba que otra persona pueda reconstruirlo."),
            "¿Una captura espectacular reemplaza los parámetros?", "No.",
            "Omitir versión del modelo, unidades o condiciones.", "method_report",
            ScientificKind.MIXED, "La reproducibilidad numérica no equivale a validación experimental.", methodRefs
        )
    )

    private val inheritedPractices: List<LearningPractice> =
        AcademicPractices.all.mapIndexed { index, item ->
            item.toLearning(
                number = index + 1,
                path = when (item.id) {
                    "harmonics" -> LearningPath.VOICE_HEARING
                    else -> LearningPath.FUNDAMENTALS
                },
                domain = LearningDomain.AIR
            )
        } + CymaticsAcademicPractices.all.mapIndexed { index, item ->
            item.toLearning(
                number = index + 5,
                path = LearningPath.VIBRATIONS,
                domain = LearningDomain.PLATE
            )
        }

    private val newPractices: List<LearningPractice> = listOf(
        p("temperature_wavelength", 9, "Temperatura, velocidad y longitud de onda", "Temperatura", LearningPath.FUNDAMENTALS, LearningLevel.APPLICATION, LearningDomain.AIR, 28,
            "Comprobar cómo cambia c y λ al variar temperatura manteniendo f.", listOf("Predice el signo del cambio.", "Compara 0, 20 y 35 °C.", "Registra c y λ."),
            "¿Por qué λ cambia si f permanece fija?", "Porque cambia la velocidad del sonido y λ=c/f.", "Mantén la frecuencia constante.",
            "4 pt: predicción, relación, datos y limitación.", "air_temperature", "Tabla con temperatura, c y λ.", ScientificKind.SIMULATION,
            "Modelo atmosférico ideal; no es medición meteorológica."),
        p("phase_map", 10, "Fase relativa y mapa espacial", "Mapa de fase", LearningPath.FUNDAMENTALS, LearningLevel.APPLICATION, LearningDomain.AIR, 30,
            "Relacionar fase de fuentes con nodos y máximos espaciales.", listOf("Prepara dos fuentes iguales.", "Compara 0, π/2 y π.", "Marca dos posiciones con sonda."),
            "¿Por qué un punto puede cambiar de refuerzo a cancelación?", "Porque cambia la suma con signo por fase y camino.", "Usa superposición local.",
            "4 pt: fase, camino, evidencia y conclusión.", "air_interference_phase", "Dos lecturas de sonda y una explicación.", ScientificKind.SIMULATION,
            "Fuentes puntuales y campo lineal idealizado."),
        p("beats", 11, "Batidos entre frecuencias cercanas", "Batidos", LearningPath.SIGNALS, LearningLevel.APPLICATION, LearningDomain.AIR, 26,
            "Relacionar la envolvente temporal con la diferencia de frecuencias.", listOf("Usa 120 y 124 Hz.", "Observa la envolvente.", "Repite con 128 Hz."),
            "¿Cómo cambia la tasa de batido al aumentar Δf?", "Aumenta aproximadamente con |f1-f2|.", "Cuenta máximos de la envolvente.",
            "4 pt: cálculo, comparación, evidencia y límite.", "air_beats", "Frecuencias, Δf y periodo observado.", ScientificKind.SIMULATION,
            "Síntesis educativa; usa volumen bajo."),
        p("room_standing_modes", 12, "Ondas estacionarias en recinto", "Modos de sala", LearningPath.ROOMS_NOISE, LearningLevel.APPLICATION, LearningDomain.AIR, 32,
            "Identificar variación espacial cerca de un modo rectangular.", listOf("Selecciona dimensiones.", "Excita el primer modo axial.", "Mueve la sonda entre pared y centro."),
            "¿Por qué una lectura no representa toda la sala?", "Porque la amplitud modal cambia con posición.", "Busca máximos y mínimos.",
            "4 pt: modo, posición, lecturas y límite.", "air_room_mode", "Tres posiciones y valores relativos.", ScientificKind.SIMULATION,
            "Recinto rectangular idealizado."),
        p("level_units", 13, "Amplitud, nivel y unidades", "Nivel y unidades", LearningPath.FUNDAMENTALS, LearningLevel.APPLICATION, LearningDomain.AIR, 25,
            "Evitar mezclar magnitud lineal, nivel y escala visual.", listOf("Compara dos niveles simulados.", "Anota la referencia.", "Separa valor físico y amplificación gráfica."),
            "¿Duplicar la escala visual duplica el SPL?", "No; solo modifica la representación.", "Busca la etiqueta de cada control.",
            "4 pt: magnitud, nivel, escala y advertencia.", "air_level_relative", "Tabla de controles físicos y visuales.", ScientificKind.EDUCATIONAL,
            "No es una medición SPL."),
        p("mic_noise_dbfs", 14, "Espectro de micrófono, ruido y dBFS", "Micrófono", LearningPath.SIGNALS, LearningLevel.APPLICATION, LearningDomain.DATA, 30,
            "Caracterizar una captura relativa y su piso de ruido.", listOf("Registra silencio ambiental.", "Genera una fuente suave.", "Compara pico y piso en dBFS."),
            "¿El pico en dBFS es un SPL?", "No sin calibración válida para esa ruta.", "Verifica la unidad mostrada.",
            "4 pt: piso, pico, unidad y condiciones.", "mic_signal_chain", "Ruta, distancia, pico y piso.", ScientificKind.MEASUREMENT,
            "Micrófono no certificado y posible procesamiento Android."),
        p("calibration_limits", 15, "Calibración y límites del teléfono", "Calibración", LearningPath.VOICE_HEARING, LearningLevel.PROJECT, LearningDomain.DATA, 32,
            "Explicar qué hace válido o inválido un perfil de calibración.", listOf("Identifica la ruta.", "Revisa método y referencia.", "Simula un cambio de dispositivo o entrada."),
            "¿Una calibración sirve para todo teléfono y micrófono?", "No; está ligada a dispositivo, ruta y condiciones.", "Busca metadatos del perfil.",
            "4 pt: ruta, referencia, trazabilidad y límite.", "level_calibration", "Lista de condiciones de aplicabilidad.", ScientificKind.MEASUREMENT,
            "No convierte la app en sonómetro certificado."),
        p("educational_voice", 16, "Fundamental y armónicos de voz educativa", "Voz", LearningPath.VOICE_HEARING, LearningLevel.APPLICATION, LearningDomain.DATA, 30,
            "Describir una señal de voz sin emitir conclusiones clínicas.", listOf("Registra una vocal cómoda.", "Identifica F0 aproximada.", "Describe armónicos y variación."),
            "¿Qué puedes concluir de salud vocal?", "Nada clínico; solo observaciones acústicas del registro.", "Separa descripción y diagnóstico.",
            "4 pt: observación, repetición, condiciones y límite clínico.", "voice_spectrum", "Tres capturas y descripción no diagnóstica.", ScientificKind.MEASUREMENT,
            "No diagnostica ni evalúa salud vocal."),
        p("room_dimensions", 17, "Dimensiones y frecuencias modales", "Dimensiones", LearningPath.ROOMS_NOISE, LearningLevel.APPLICATION, LearningDomain.AIR, 30,
            "Cuantificar cómo una dimensión desplaza una familia modal.", listOf("Registra modos del caso A.", "Cambia solo Lx.", "Compara índices con n>0."),
            "¿Por qué no basta conocer el volumen?", "Porque cada frecuencia depende de Lx, Ly y Lz por separado.", "Mira los índices modales.",
            "4 pt: control, cálculo, comparación y limitación.", "room_dimensions", "Tabla antes/después de cinco modos.", ScientificKind.SIMULATION,
            "Geometría rectangular ideal."),
        p("reverberation_absorption", 18, "Absorción y reverberación idealizada", "Reverberación", LearningPath.ROOMS_NOISE, LearningLevel.PROJECT, LearningDomain.DATA, 34,
            "Usar Sabine con supuestos y detectar resultados no aplicables.", listOf("Define volumen y superficies.", "Calcula absorción equivalente.", "Compara dos tratamientos."),
            "¿Más absorción equivalente aumenta RT60?", "No; en Sabine lo reduce.", "Observa RT60≈0,161V/A.",
            "4 pt: datos, cálculo, supuestos y límite.", "room_reverb", "Tabla de superficies, α y RT60.", ScientificKind.EDUCATIONAL,
            "No es medición ni simulación geométrica de reverberación."),
        p("plate_scaling", 19, "Escalamiento de una placa", "Escalamiento", LearningPath.VIBRATIONS, LearningLevel.PROJECT, LearningDomain.PLATE, 34,
            "Separar efectos de espesor, tamaño, densidad y elasticidad.", listOf("Fija modo y borde.", "Cambia una propiedad por vez.", "Compara fmodo y respuesta."),
            "¿Qué propiedad afecta fuertemente la rigidez flexional?", "El espesor, porque D depende de h³.", "Usa la ecuación de D.",
            "4 pt: control, tendencia, ecuación y límite.", "plate_scaling", "Cuatro casos y frecuencias propias.", ScientificKind.SIMULATION,
            "Propiedades ideales; no reemplaza caracterización de material."),
        p("reproducible_sweep", 20, "Barrido e informe reproducible", "Barrido", LearningPath.METHODS_DATA, LearningLevel.PROJECT, LearningDomain.MIXED, 40,
            "Producir una comparación autocontenida con evidencia y limitaciones.", listOf("Formula hipótesis.", "Define rango y paso.", "Exporta datos, gráfico y conclusión."),
            "¿Qué necesita otra persona para repetir el caso?", "Modelo, versión, parámetros, unidades, método y datos.", "Revisa los metadatos exportados.",
            "6 pt: diseño, datos, gráfico, análisis, límites y reproducibilidad.", "method_sweep", "CSV/JSON y entrada completa de cuaderno.", ScientificKind.MIXED,
            "Reproducibilidad numérica no equivale a validación experimental.")
    )

    val practices: List<LearningPractice> = inheritedPractices + newPractices
    val practiceIds: Set<String> = practices.mapTo(linkedSetOf()) { it.id }
    val faq: List<FaqEntry> get() = LearningFaqCatalog.all

    fun guide(id: String): LearningGuide? = guides.firstOrNull { it.id == id }
    fun practice(id: String): LearningPractice? = practices.firstOrNull { it.id == id }
    fun contentTitle(id: String): String = guide(id)?.title ?: practice(id)?.title ?: "Exploración libre"

    fun searchFaq(query: String, path: LearningPath? = null): List<FaqEntry> {
        val needle = normalize(query)
        return faq.asSequence()
            .filter { path == null || it.path == path }
            .map { entry ->
                val title = normalize(entry.question)
                val body = normalize(entry.shortAnswer + " " + entry.explanation)
                val tags = normalize(entry.tags.joinToString(" "))
                val score = when {
                    needle.isBlank() -> 1
                    title.contains(needle) -> 6
                    tags.contains(needle) -> 4
                    body.contains(needle) -> 2
                    needle.split(' ').all { it.length < 3 || title.contains(it) || tags.contains(it) || body.contains(it) } -> 1
                    else -> 0
                }
                entry to score
            }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<FaqEntry, Int>> { it.second }.thenBy { it.first.question })
            .map { it.first }
            .toList()
    }

    fun validationErrors(): List<String> {
        val errors = mutableListOf<String>()
        val guideIds = guides.map { it.id }
        val practiceIdsList = practices.map { it.id }
        val faqIds = faq.map { it.id }
        if (guideIds.size != guideIds.toSet().size) errors += "IDs de guía repetidos"
        if (practiceIdsList.size != practiceIdsList.toSet().size) errors += "IDs de práctica repetidos"
        if (faqIds.size != faqIds.toSet().size) errors += "IDs de duda repetidos"
        if (guides.size < 30) errors += "Se requieren al menos 30 guías"
        if (practices.size < 20) errors += "Se requieren al menos 20 prácticas"
        if (faq.size < 60) errors += "Se requieren al menos 60 dudas frecuentes"
        guides.forEach { guide ->
            guide.prerequisiteIds.filterNot { it in guideIds }.forEach {
                errors += "Prerrequisito desconocido $it en ${guide.id}"
            }
            if (guide.objective.isBlank() || guide.explorationSteps.isEmpty() || guide.references.isEmpty()) {
                errors += "Guía incompleta ${guide.id}"
            }
            if (guide.schemaVersion <= 0 || guide.contentVersion.isBlank() || guide.language.isBlank()) {
                errors += "Metadatos editoriales incompletos ${guide.id}"
            }
        }
        practices.forEach { practice ->
            if (practice.steps.isEmpty() || practice.question.isBlank() || practice.rubric.isBlank()) {
                errors += "Práctica incompleta ${practice.id}"
            }
        }
        faq.forEach { entry ->
            entry.relatedGuideIds.filterNot { it in guideIds }.forEach {
                errors += "Guía relacionada desconocida $it en ${entry.id}"
            }
            entry.relatedPracticeIds.filterNot { it in practiceIdsList }.forEach {
                errors += "Práctica relacionada desconocida $it en ${entry.id}"
            }
            if (entry.question.isBlank() || entry.shortAnswer.isBlank() || entry.explanation.isBlank() || entry.tags.isEmpty()) {
                errors += "Duda incompleta ${entry.id}"
            }
        }
        val prerequisites = guides.associate { it.id to it.prerequisiteIds }
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        fun visit(id: String): Boolean {
            if (id in visiting) return true
            if (!visited.add(id)) return false
            visiting += id
            val cycle = prerequisites[id].orEmpty().any(::visit)
            visiting -= id
            return cycle
        }
        if (guideIds.any(::visit)) errors += "Ciclo de prerrequisitos"
        return errors
    }

    private fun normalize(value: String): String = Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .replace("[^a-z0-9 ]".toRegex(), " ")
        .replace("\\s+".toRegex(), " ")
        .trim()

    private fun AcademicPractice.toLearning(
        number: Int,
        path: LearningPath,
        domain: LearningDomain
    ): LearningPractice = LearningPractice(
        id = id,
        number = number,
        title = title,
        shortTitle = shortTitle,
        path = path,
        level = LearningLevel.FOUNDATIONS,
        domain = domain,
        estimatedMinutes = 24,
        objective = objective,
        steps = steps,
        question = question,
        expectedAnswer = expectedAnswer,
        hint = hint,
        rubric = defaultRubric,
        presetId = id,
        evidencePrompt = "Registra al menos una observación de la escena y los parámetros utilizados.",
        scientificKind = ScientificKind.SIMULATION,
        limitation = if (domain == LearningDomain.PLATE) {
            "Modelo modal idealizado; amplitud a.u. y arena cualitativa."
        } else {
            "Simulación educativa; no es una medición acústica certificada."
        },
        inheritedFromV1 = true
    )

    private fun g(
        id: String,
        title: String,
        path: LearningPath,
        level: LearningLevel,
        domain: LearningDomain,
        semesters: String,
        minutes: Int,
        objective: String,
        summary: String,
        concepts: List<String>,
        equation: String?,
        steps: List<String>,
        question: String,
        answer: String,
        commonError: String,
        presetId: String?,
        kind: ScientificKind,
        limitation: String,
        references: List<String>
    ) = LearningGuide(
        id, title, path, level, domain, semesters, minutes, objective, summary, concepts,
        equation, steps, question, answer, commonError, presetId, emptyList(), kind,
        limitation, references
    )

    private fun p(
        id: String,
        number: Int,
        title: String,
        shortTitle: String,
        path: LearningPath,
        level: LearningLevel,
        domain: LearningDomain,
        minutes: Int,
        objective: String,
        steps: List<String>,
        question: String,
        expected: String,
        hint: String,
        rubric: String,
        presetId: String,
        evidence: String,
        kind: ScientificKind,
        limitation: String
    ) = LearningPractice(
        id, number, title, shortTitle, path, level, domain, minutes, objective, steps,
        question, expected, hint, rubric, presetId, evidence, kind, limitation
    )
}
