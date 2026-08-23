import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

bad = """                    val urlStr = "https://router.project-osrm.org/route/v1/$mode/$userLng,$userLat;$destLng,$destLat?overview=full&geometries=geojson\""""
good = """                    val currentLat = viewModel.userLatitude.value
                    val currentLng = viewModel.userLongitude.value
                    val urlStr = "https://router.project-osrm.org/route/v1/$mode/$currentLng,$currentLat;$destLng,$destLat?overview=full&geometries=geojson\""""

content = content.replace(bad, good)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

