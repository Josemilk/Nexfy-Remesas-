import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onSelectDelivery: (Long) -> Unit\n) {", "onSelectDelivery: (Long) -> Unit,\n    onNavigateToMap: () -> Unit\n) {")
content = content.replace("onClick = { /* navigate to offline map */ }", "onClick = onNavigateToMap")

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/MainScreen.kt", "r") as f:
    main_content = f.read()

main_content = main_content.replace("onSelectDelivery = { id -> viewModel.navigateTo(Screen.DeliveryDetail(id)) }\n                        )", "onSelectDelivery = { id -> viewModel.navigateTo(Screen.DeliveryDetail(id)) },\n                            onNavigateToMap = { viewModel.navigateTo(Screen.OfflineMap(null, false)) }\n                        )")

with open("app/src/main/java/com/example/ui/MainScreen.kt", "w") as f:
    f.write(main_content)
