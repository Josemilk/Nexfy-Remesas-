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
import com.example.ui.screens.AdminUnlockScreen
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.ClientDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeliveryDetailScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.NewDeliveryScreen
import com.example.ui.screens.OfflineMapScreen
import com.example.ui.screens.PinScreen
import com.example.ui.screens.RecycleBinScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import androidx.compose.material.icons.filled.DeleteSweep

@Composable
fun MainScreen(viewModel: NexFyViewModel) {
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

    when (val screen = currentScreen) {
        is Screen.Splash -> {
            SplashScreen(onTimeout = { viewModel.onSplashFinished() })
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
                onOpenMap = { id -> viewModel.navigateTo(Screen.OfflineMap(id)) }
            )
        }
        is Screen.OfflineMap -> {
            OfflineMapScreen(targetLocation = screen.targetLocation, 
                deliveryId = screen.deliveryId,
                isManualPin = screen.isManualPin,
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(Screen.MainTabs(0)) },
                onViewDetails = { id -> viewModel.navigateTo(Screen.DeliveryDetail(id)) }
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
                            onNavigateToMap = { viewModel.navigateTo(Screen.OfflineMap(null, false)) }
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
                    }
                }
            }
        }
    }
}
