import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# Replace inner Rows in status cards
# In status card 1: Internet
search1 = """                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),"""

replace1 = """                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),"""
content = content.replace(search1, replace1)

# In status card 2: Map
search2 = """                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isMapDownloaded) Color(0xFFEFF6FF) else Color(0xFFFEF3C7)),"""

replace2 = """                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isMapDownloaded) Color(0xFFEFF6FF) else Color(0xFFFEF3C7)),"""
content = content.replace(search2, replace2)


search_col1 = """                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (isOnline) "Conexión: En línea" else "Conexión: Modo Offline (100% Funcional)","""

replace_col1 = """                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOnline) "Conexión: En línea" else "Conexión: Modo Offline (100% Funcional)","""

content = content.replace(search_col1, replace_col1)

search_col2 = """                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (isMapDownloaded) "Mapa Cuba Offline: Sincronizado" else "Mapa Cuba Offline: No descargado","""

replace_col2 = """                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isMapDownloaded) "Mapa Cuba Offline: Sincronizado" else "Mapa Cuba Offline: No descargado","""
                                
content = content.replace(search_col2, replace_col2)

# Also fix the deliveries list
search_col3 = """                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = delivery.clientName,"""
replace_col3 = """                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = delivery.clientName,"""
content = content.replace(search_col3, replace_col3)

search_row4 = """                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {"""

replace_row4 = """                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {"""
content = content.replace(search_row4, replace_row4)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
print("Patched DashboardScreen.kt")
