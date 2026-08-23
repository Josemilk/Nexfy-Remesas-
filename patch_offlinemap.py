import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

# Replace pointerInput stuff on OsmMapView
mapview_start = "            OsmMapView("
canvas_end = "            }\n"

# Using a regex to find OsmMapView up to the end of Canvas
pattern = re.compile(r'            OsmMapView\(.*?Canvas\(modifier = Modifier\.fillMaxSize\(\)\) \{.*?center = clientCenter\s*\)\s*\}', re.DOTALL)

replacement = """
            // Real map renderer with its own controls
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                mapType = settings.mapLayer,
                userLat = userLat,
                userLng = userLng,
                clientLat = targetLocation?.first ?: 23.1367,
                clientLng = targetLocation?.second ?: -82.3584,
                isNavigating = isNavigating,
                headingUp = headingUp,
                routePoints = routePoints
            )
"""

content = pattern.sub(replacement, content)

# I also need to add 'var headingUp by remember { mutableStateOf(false) }'
# and 'var routePoints by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }'
# and 'var transportMode by remember { mutableStateOf("car") }'

state_vars = """    var distanceKm by remember { mutableFloatStateOf(0f) }
    var etaMinutes by remember { mutableStateOf(0) }
    
    var showMapSettingsSheet by remember { mutableStateOf(false) }"""

new_state_vars = """    var distanceKm by remember { mutableFloatStateOf(0f) }
    var etaMinutes by remember { mutableStateOf(0) }
    
    var showMapSettingsSheet by remember { mutableStateOf(false) }
    var headingUp by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }
    var transportMode by remember { mutableStateOf("car") }"""

content = content.replace(state_vars, new_state_vars)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

