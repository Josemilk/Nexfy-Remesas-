import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

# Add imports
if "import android.speech.tts.TextToSpeech" not in content:
    content = content.replace("import android.widget.Toast", "import android.widget.Toast\nimport android.speech.tts.TextToSpeech\nimport java.util.Locale")

# Find the start of OfflineMapScreen
search_start = """    val context = LocalContext.current"""

replace_start = """    val context = LocalContext.current
    
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale("es", "ES")
            }
        }
        tts.value = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }"""
content = content.replace(search_start, replace_start)

# Find LaunchedEffect(isNavigating)
search_loop = """    // Active Navigation loop simulation for live turn-by-turn feedback
    LaunchedEffect(isNavigating) {
        if (isNavigating) {
            val steps = listOf(
                "En 150m gire a la derecha en Calle 23",
                "Siga recto 300m por Avenida de los Presidentes",
                "⚠️ Zona de peligro/desvío: Recalculando ruta óptima...",
                "Nueva ruta sugerida. Gire a la izquierda en 50m",
                "En 80m gire a la izquierda hacia destino",
                "¡Ha llegado a la ubicación del cliente!"
            )
            var idx = 0
            while (isNavigating) {
                delay(3500)
                idx = (idx + 1) % steps.size
                navStep = steps[idx]
                speedKmH = if (idx == 2) 15f else 25f + (idx * 3)
            }
        } else {
            speedKmH = 0f
        }
    }"""

replace_loop = """    // Active Navigation loop simulation for live turn-by-turn feedback
    LaunchedEffect(isNavigating) {
        if (isNavigating) {
            val steps = listOf(
                "En 150 metros gire a la derecha en Calle 23",
                "Siga recto 300 metros por Avenida de los Presidentes",
                "Atención, zona de desvío. Recalculando ruta óptima...",
                "Nueva ruta sugerida. Gire a la izquierda en 50 metros",
                "En 80 metros gire a la izquierda hacia el destino",
                "¡Ha llegado a la ubicación del cliente!"
            )
            tts.value?.speak("Iniciando ruta hacia el destino", TextToSpeech.QUEUE_FLUSH, null, null)
            var idx = 0
            while (isNavigating) {
                delay(4000)
                idx = (idx + 1) % steps.size
                navStep = steps[idx]
                speedKmH = if (idx == 2) 15f else 25f + (idx * 3)
                tts.value?.speak(navStep, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } else {
            speedKmH = 0f
        }
    }"""
content = content.replace(search_loop, replace_loop)


with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)
print("Patched TTS")
