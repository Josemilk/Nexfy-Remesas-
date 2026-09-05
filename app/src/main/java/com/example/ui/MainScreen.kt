package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.AdminEcosystemScreen
import com.example.ui.screens.AdminUnlockScreen
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.ClientDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeliveryDetailScreen
import com.example.ui.screens.EcosystemSelectionScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.NewDeliveryScreen
import com.example.ui.screens.PinScreen
import com.example.ui.screens.RecycleBinScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.UserSupportScreen
import com.example.ui.components.FreeTrialModalDialog
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.SupportAgent

@Composable
fun MainScreen(viewModel: NexFyViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val settings by viewModel.settings.collectAsState()

    if (currentScreen is Screen.Splash) {
        SplashScreen(
            onTimeout = {
                viewModel.onSplashFinished()
            }
        )
        return
    }

    // SaaS License & Free Trial Check (30-day Free Trial -> Block after 30 days if not activated with license)
    val isAccessAllowed = viewModel.isAppAccessAllowed()
    if (!isAccessAllowed) {
        AdminUnlockScreen(
            viewModel = viewModel,
            isLicenseMode = true,
            onBackToPin = {},
            onUnlocked = {
                if (viewModel.authManager.isUserLoggedIn()) {
                    if (settings.pinRequired && !isAuthenticated) {
                        viewModel.navigateTo(Screen.PinLock)
                    } else {
                        viewModel.navigateTo(Screen.MainTabs(0))
                    }
                } else {
                    viewModel.navigateTo(Screen.EcosystemSelection)
                }
            }
        )
        return
    }

    if (currentScreen is Screen.EcosystemSelection) {
        EcosystemSelectionScreen(
            viewModel = viewModel,
            onSelectRole = { role ->
                viewModel.selectEcosystemRole(role)
            }
        )
        return
    }

    if (!isAuthenticated && settings.pinRequired) {
        when (currentScreen) {
            is Screen.AdminUnlock -> {
                AdminUnlockScreen(
                    viewModel = viewModel,
                    onBackToPin = { viewModel.navigateTo(Screen.PinLock) },
                    onUnlocked = { viewModel.startPinChange() }
                )
            }
            else -> {
                PinScreen(viewModel = viewModel, isCreationMode = false)
            }
        }
        return
    }

    // Show Free Trial Dialog notification if user is in main screens and hasn't acknowledged it yet
    val licenseState by viewModel.licenseState.collectAsState()
    if ((!settings.pinRequired || isAuthenticated) && !settings.freeTrialAcknowledged && licenseState.isTrial) {
        val daysRemaining = licenseState.daysRemaining
        FreeTrialModalDialog(
            daysRemaining = daysRemaining,
            onDismiss = {
                viewModel.acknowledgeFreeTrial()
            }
        )
    }

    when (val screen = currentScreen) {
        is Screen.Splash -> {
            SplashScreen(onTimeout = { viewModel.onSplashFinished() })
        }
        is Screen.EcosystemSelection -> {
            EcosystemSelectionScreen(
                viewModel = viewModel,
                onSelectRole = { role -> viewModel.selectEcosystemRole(role) }
            )
        }
        is Screen.PinLock -> {
            PinScreen(viewModel = viewModel, isCreationMode = false)
        }
        is Screen.PinSetup -> {
            PinScreen(viewModel = viewModel, isCreationMode = true)
        }
        is Screen.AdminUnlock -> {
            AdminUnlockScreen(
                viewModel = viewModel,
                onBackToPin = { viewModel.navigateTo(Screen.MainTabs(0)) },
                onUnlocked = { viewModel.startPinChange() }
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(Screen.MainTabs(0)) }
            )
        }
        is Screen.NewDelivery -> {
            NewDeliveryScreen(
                initialClientName = screen.initialClientName,
                initialPhone = screen.initialPhone,
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(Screen.MainTabs(0)) }
            )
        }
        is Screen.DeliveryDetail -> {
            DeliveryDetailScreen(
                deliveryId = screen.deliveryId,
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(Screen.MainTabs(1)) },
                onOpenMap = { lat, lng -> 
                    // Se lanza OsmAnd localmente (Submódulo) o la app de OsmAnd
                    launchOsmAndMap(context, lat, lng)
                }
            )
        }
        is Screen.ClientDetail -> {
            ClientDetailScreen(
                clientId = screen.clientId,
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(Screen.MainTabs(2)) }
            )
        }
        is Screen.MainTabs -> {
            var selectedTab by remember(screen.tabIndex) { mutableIntStateOf(screen.tabIndex) }

            if (settings.ecosystemRole == "ADMIN") {
                AdminEcosystemScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { viewModel.navigateTo(Screen.Settings) },
                    onNavigateToClients = { selectedTab = 2 },
                    onNavigateToHistory = { selectedTab = 1 },
                    onNavigateToDeliveries = { selectedTab = 1 }
                )
            } else {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1E1B4B)
                        ) {
                            // Tab 0: Inicio
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                                label = { Text("Inicio") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2563EB),
                                    selectedTextColor = Color(0xFF2563EB),
                                    indicatorColor = Color(0xFFEFF6FF)
                                ),
                                modifier = Modifier.testTag("tab_home")
                            )

                            // Tab 1: Entregas
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.History, contentDescription = "Entregas") },
                                label = { Text("Entregas") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2563EB),
                                    selectedTextColor = Color(0xFF2563EB),
                                    indicatorColor = Color(0xFFEFF6FF)
                                ),
                                modifier = Modifier.testTag("tab_deliveries")
                            )

                            // Tab 2: Clientes
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.People, contentDescription = "Clientes") },
                                label = { Text("Clientes") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2563EB),
                                    selectedTextColor = Color(0xFF2563EB),
                                    indicatorColor = Color(0xFFEFF6FF)
                                ),
                                modifier = Modifier.testTag("tab_clients")
                            )

                            // Tab 3: Papelera de Reciclaje
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = { Icon(Icons.Default.DeleteSweep, contentDescription = "Papelera") },
                                label = { Text("Papelera") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFFEF4444),
                                    selectedTextColor = Color(0xFFEF4444),
                                    indicatorColor = Color(0xFFFEF2F2)
                                ),
                                modifier = Modifier.testTag("tab_recycle_bin")
                            )

                            // Tab 4: Soporte y Aportes
                            NavigationBarItem(
                                selected = selectedTab == 4,
                                onClick = { selectedTab = 4 },
                                icon = { Icon(Icons.Default.SupportAgent, contentDescription = "Soporte") },
                                label = { Text("Soporte") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2563EB),
                                    selectedTextColor = Color(0xFF2563EB),
                                    indicatorColor = Color(0xFFEFF6FF)
                                ),
                                modifier = Modifier.testTag("tab_support")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToNewDelivery = { viewModel.navigateTo(Screen.NewDelivery()) },
                                onNavigateToClients = { selectedTab = 2 },
                                onNavigateToSettings = { viewModel.navigateTo(Screen.Settings) },
                                onNavigateToDeliveries = { selectedTab = 1 },
                                onSelectDelivery = { id -> viewModel.navigateTo(Screen.DeliveryDetail(id)) },
                                onNavigateToMap = { launchOsmAndMap(context, null, null) }
                            )
                            1 -> HistoryScreen(
                                viewModel = viewModel,
                                onBack = { selectedTab = 0 },
                                onSelectDelivery = { id -> viewModel.navigateTo(Screen.DeliveryDetail(id)) }
                            )
                            2 -> ClientsScreen(
                                viewModel = viewModel,
                                onBack = { selectedTab = 0 },
                                onNewDeliveryForClient = { name, phone -> viewModel.navigateTo(Screen.NewDelivery(name, phone)) },
                                onClientSelected = { id -> viewModel.navigateTo(Screen.ClientDetail(id)) }
                            )
                            3 -> RecycleBinScreen(
                                viewModel = viewModel,
                                onBack = { selectedTab = 0 }
                            )
                            4 -> UserSupportScreen(
                                viewModel = viewModel,
                                onBack = { selectedTab = 0 }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun launchOsmAndMap(context: android.content.Context, lat: Double?, lng: Double?) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        if (lat != null && lng != null) {
            intent.data = android.net.Uri.parse("geo:$lat,$lng?q=$lat,$lng")
        } else {
            // Solo abrimos la app en la ubicación actual
            intent.data = android.net.Uri.parse("geo:0,0")
        }
        // Como OsmAnd es submódulo y compartirá ecosistema o estará instalada:
        intent.setPackage("net.osmand.plus")
        
        // Verificamos si OsmAnd existe de manera standalone
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Si no está instalada, usamos intent genérico para que lo atrape el submódulo u otro mapa
            val genericIntent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            if (lat != null && lng != null) {
                genericIntent.data = android.net.Uri.parse("geo:$lat,$lng?q=$lat,$lng")
            } else {
                genericIntent.data = android.net.Uri.parse("geo:0,0")
            }
            context.startActivity(genericIntent)
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "No se encontró el mapa de OsmAnd", android.widget.Toast.LENGTH_SHORT).show()
    }
}

