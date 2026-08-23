import re
with open("app/src/main/java/com/example/ui/MainScreen.kt", "r") as f:
    content = f.read()

# Fix HistoryScreen call
content = content.replace("""                        1 -> HistoryScreen(
                            viewModel = viewModel,
                            onBack = { selectedTab = 0 },
                            onSelectDelivery = { id -> viewModel.navigateTo(Screen.DeliveryDetail(id)) },
                            onNavigateToMap = { viewModel.navigateTo(Screen.OfflineMap(null, false)) }
                        )""", """                        1 -> HistoryScreen(
                            viewModel = viewModel,
                            onBack = { selectedTab = 0 },
                            onSelectDelivery = { id -> viewModel.navigateTo(Screen.DeliveryDetail(id)) }
                        )""")

with open("app/src/main/java/com/example/ui/MainScreen.kt", "w") as f:
    f.write(content)
