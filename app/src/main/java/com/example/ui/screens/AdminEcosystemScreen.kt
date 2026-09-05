package com.example.ui.screens

import java.util.Locale
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import com.example.ui.components.DailyCashSettlementDialog
import com.example.ui.components.ExchangeRateCalculatorCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.components.PaymentContributionDialog
import com.example.data.model.AppSettings
import com.example.data.model.DeliveryStatus
import com.example.data.model.Worker
import com.example.ui.NexFyViewModel

@Composable
fun AdminEcosystemScreen(
    viewModel: NexFyViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDeliveries: () -> Unit
) {
    val context = LocalContext.current
    var selectedNavIndex by remember { mutableIntStateOf(0) } // 0: Home, 1: Clientes, 2: Trabajadores, 3: Historial, 4: Soporte

    val deliveries by viewModel.deliveries.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val workers by viewModel.workers.collectAsState()

    var showAdminConfigDialog by remember { mutableStateOf(false) }
    var showWorkerLinkingModal by remember { mutableStateOf(false) }
    var selectedWorkerForLinking by remember { mutableStateOf<Worker?>(null) }
    var showPhotoPickerDialog by remember { mutableStateOf(false) }
    var showContributionPaymentDialog by remember { mutableStateOf(false) }
    var showCashSettlementDialog by remember { mutableStateOf(false) }

    var activeHomeEditTitle by remember { mutableStateOf<String?>(null) }
    var activeHomeEditInitialValue by remember { mutableStateOf("") }
    var activeHomeEditOnSave by remember { mutableStateOf<((String) -> Unit)?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateAdminProfile(
                name = settings.adminName,
                username = settings.adminUsername,
                phone = settings.adminPhone,
                email = settings.adminEmail,
                photoUri = it.toString()
            )
            Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    val isDark = settings.darkMode
    val backgroundColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val mintGreen = if (isDark) Color(0xFF10B981) else Color(0xFF0D9488)
    val tealIconBg = if (isDark) Color(0xFF134E4A) else Color(0xFFCCFBF1)
    val amberWarning = Color(0xFFF59E0B)
    val mutedText = textSecondary
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val bottomNavBg = if (isDark) Color(0xFF1E293B) else Color.White

    // Dynamic stats from Room DB (100% Real figures)
    val totalRemesas = deliveries.sumOf { it.amountUsd }
    val displayRemesasStr = "$${String.format(Locale.US, "%,.2f", totalRemesas)} USD"
    val displayUsersStr = "${clients.size}"
    val pendingDeliveriesCount = deliveries.count { it.status == DeliveryStatus.PENDING }
    val displayPendingStr = "$pendingDeliveriesCount"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen Content depending on selected bottom navigation tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 800.dp)
                ) {
                    when (selectedNavIndex) {
                    0 -> AdminHomeContent(
                        settings = settings,
                        deliveries = deliveries,
                        workers = workers,
                        displayRemesasStr = displayRemesasStr,
                        displayUsersStr = displayUsersStr,
                        displayPendingStr = displayPendingStr,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        mintGreen = mintGreen,
                        tealIconBg = tealIconBg,
                        amberWarning = amberWarning,
                        mutedText = mutedText,
                        isDark = isDark,
                        onOpenPhotoPicker = { showPhotoPickerDialog = true },
                        onOpenSettingsMenu = { showAdminConfigDialog = true },
                        onNavigateToDeliveries = { selectedNavIndex = 3 },
                        onNavigateToClients = { selectedNavIndex = 1 },
                        onSaveFirestore = {
                            viewModel.saveToFirestore { ok, msg ->
                                Toast.makeText(context, if (ok) "🔥 ¡Datos guardados en Firebase / Firestore con éxito!" else "Error Firebase: $msg", Toast.LENGTH_LONG).show()
                            }
                        },
                        onAssignDelivery = { deliveryId, workerId, workerName ->
                            viewModel.assignDeliveryToWorker(deliveryId, workerId, workerName)
                        },
                        onUpdateSettings = { newSettings -> viewModel.updateSettings(newSettings) },
                        onEditElement = { title, initialValue, onSave ->
                            activeHomeEditTitle = title
                            activeHomeEditInitialValue = initialValue
                            activeHomeEditOnSave = onSave
                        },
                        onOpenCashSettlement = { showCashSettlementDialog = true }
                    )
                    1 -> {
                        ClientsScreen(
                            viewModel = viewModel,
                            onBack = { selectedNavIndex = 0 },
                            onNewDeliveryForClient = { _, _ -> },
                            onClientSelected = { id -> viewModel.navigateTo(com.example.ui.Screen.ClientDetail(id)) }
                        )
                    }
                    2 -> AdminTrabajadoresContent(
                        workers = workers,
                        deliveries = deliveries,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        mintGreen = mintGreen,
                        mutedText = mutedText,
                        isDark = isDark,
                        onBack = { selectedNavIndex = 0 },
                        onLinkWorker = { worker -> selectedWorkerForLinking = worker },
                        onAddWorker = { name, role, phone, email, address -> viewModel.addWorker(name, role, phone, email, address) },
                        onUpdateWorker = { worker -> viewModel.updateWorker(worker) },
                        onDeleteWorker = { workerId -> viewModel.deleteWorker(workerId) }
                    )
                    3 -> {
                        HistoryScreen(
                            viewModel = viewModel,
                            onBack = { selectedNavIndex = 0 },
                            onSelectDelivery = { id -> viewModel.navigateTo(com.example.ui.Screen.DeliveryDetail(id)) }
                        )
                    }
                    4 -> AdminSoporteContent(
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        mintGreen = mintGreen,
                        mutedText = mutedText,
                        isDark = isDark,
                        onBack = { selectedNavIndex = 0 },
                        onOpenContributionScreen = { showContributionPaymentDialog = true }
                    )
                }
                } // Close inner Box
            }

            // Custom Rounded Floating Bottom Navigation Bar
            AdminBottomNavigationBar(
                selectedIndex = selectedNavIndex,
                onSelectIndex = { index ->
                    selectedNavIndex = index
                },
                mintGreen = mintGreen,
                navBg = bottomNavBg,
                isDark = isDark
            )
        }

        // --- DIALOGS AND MODALS ---

        // 1. Photo Picker Selection Dialog
        if (showPhotoPickerDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoPickerDialog = false },
                containerColor = Color(0xFF0E1226),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = mintGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Foto de Perfil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Selecciona una opción para actualizar la foto del Administrador:",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )

                        Button(
                            onClick = {
                                showPhotoPickerDialog = false
                                photoPickerLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = mintGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Elegir de la Galería", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        if (settings.adminPhotoUri.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.updateAdminProfile(
                                        name = settings.adminName,
                                        username = settings.adminUsername,
                                        phone = settings.adminPhone,
                                        email = settings.adminEmail,
                                        photoUri = ""
                                    )
                                    showPhotoPickerDialog = false
                                    Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF4444))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Eliminar Foto Actual", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPhotoPickerDialog = false }) {
                        Text(text = "Cancelar", color = Color(0xFF94A3B8))
                    }
                }
            )
        }

        // 2. Admin Settings Wheel Config Dialog
        if (showAdminConfigDialog) {
            AdminSettingsWheelDialog(
                settings = settings,
                workers = workers,
                viewModel = viewModel,
                isDark = isDark,
                onDismiss = { showAdminConfigDialog = false },
                onSave = { name, username, phone, email, updatedSettings ->
                    viewModel.updateAdminProfile(name, username, phone, email)
                    viewModel.updateSettings(updatedSettings)
                    showAdminConfigDialog = false
                    Toast.makeText(context, "Configuración y cambios del ecosistema guardados", Toast.LENGTH_SHORT).show()
                },
                onOpenWorkerLinking = {
                    showAdminConfigDialog = false
                    showWorkerLinkingModal = true
                },
                onOpenCashSettlement = {
                    showAdminConfigDialog = false
                    showCashSettlementDialog = true
                }
            )
        }

        // 3. Worker Linking Modal (List of workers with red/green status)
        if (showWorkerLinkingModal) {
            WorkerLinkingListDialog(
                workers = workers,
                onDismiss = { showWorkerLinkingModal = false },
                onSelectLinkWorker = { worker ->
                    showWorkerLinkingModal = false
                    selectedWorkerForLinking = worker
                },
                onUnlinkWorker = { workerId ->
                    viewModel.unlinkWorker(workerId)
                    Toast.makeText(context, "Trabajador desvinculado", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 4. Device ID Input Modal for linking a specific worker
        selectedWorkerForLinking?.let { worker ->
            WorkerDeviceIdInputDialog(
                worker = worker,
                onDismiss = { selectedWorkerForLinking = null },
                onConfirmLink = { deviceIdCode ->
                    viewModel.linkWorkerWithDeviceId(worker.id, deviceIdCode)
                    selectedWorkerForLinking = null
                    showWorkerLinkingModal = true
                    Toast.makeText(context, "¡${worker.name} ha sido vinculado con éxito!", Toast.LENGTH_LONG).show()
                }
            )
        }

        // 5. Payment & Contribution Screen Modal
        if (showContributionPaymentDialog) {
            PaymentContributionScreenDialog(
                onDismiss = { showContributionPaymentDialog = false }
            )
        }

        // 6. Home Universal Element Editor Dialog
        activeHomeEditTitle?.let { title ->
            HomeElementEditorDialog(
                title = title,
                initialValue = activeHomeEditInitialValue,
                onDismiss = { activeHomeEditTitle = null },
                onSave = { newValue ->
                    activeHomeEditOnSave?.invoke(newValue)
                    activeHomeEditTitle = null
                    Toast.makeText(context, "Referencia de Home actualizada y sincronizada", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 7. Daily Cash Settlement & Courier Liquidation Dialog
        if (showCashSettlementDialog) {
            DailyCashSettlementDialog(
                deliveries = deliveries,
                workers = workers,
                settings = settings,
                onUpdateSettings = { viewModel.updateSettings(it) },
                onUpdateDelivery = { viewModel.updateDelivery(it) },
                onDismiss = { showCashSettlementDialog = false }
            )
        }
    }
}

@Composable
private fun AdminHomeContent(
    settings: AppSettings,
    deliveries: List<com.example.data.model.Delivery>,
    workers: List<Worker>,
    displayRemesasStr: String,
    displayUsersStr: String,
    displayPendingStr: String,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    mintGreen: Color,
    tealIconBg: Color,
    amberWarning: Color,
    mutedText: Color,
    isDark: Boolean,
    onOpenPhotoPicker: () -> Unit,
    onOpenSettingsMenu: () -> Unit,
    onNavigateToDeliveries: () -> Unit,
    onNavigateToClients: () -> Unit,
    onSaveFirestore: () -> Unit,
    onAssignDelivery: (deliveryId: Long, workerId: Long, workerName: String) -> Unit,
    onUpdateSettings: (AppSettings) -> Unit,
    onEditElement: (title: String, initialValue: String, onSave: (String) -> Unit) -> Unit,
    onOpenCashSettlement: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Top Bar: Profile Avatar & Details (Left) & Settings Gear (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E2838) else Color(0xFFE2E8F0))
                        .border(BorderStroke(2.dp, mintGreen), CircleShape)
                        .clickable { onOpenPhotoPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    if (settings.adminPhotoUri.isNotEmpty()) {
                        AsyncImage(
                            model = settings.adminPhotoUri,
                            contentDescription = "Foto Administrador",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil Administrador",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = settings.adminName.ifEmpty { "Administrador Principal" },
                        color = textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (settings.adminUsername.startsWith("@")) settings.adminUsername else "@${settings.adminUsername.ifEmpty { "admin_nexfy" }}"} • Panel de Control",
                        color = if (isDark) mintGreen else Color(0xFF0D9488),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Settings Gear Icon
            IconButton(
                onClick = onOpenSettingsMenu,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    .testTag("btn_admin_settings_wheel")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración Administrador",
                    tint = textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Screen Main Title & Subtitle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
        ) {
            Text(
                text = settings.homeTitle.ifEmpty { "Ecosistema de Administración" },
                color = textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = settings.homeSubtitle.ifEmpty { "Monitorea y gestiona las operaciones de remesas en tiempo real" },
                color = mutedText,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        // Cuadre de Caja Diario & Liquidaciones Banner
        val deliveredCount = deliveries.count { it.status == DeliveryStatus.DELIVERED }
        val deliveredUsd = deliveries.filter { it.status == DeliveryStatus.DELIVERED }.sumOf { it.amountUsd }
        
        if (settings.showAdminSettlementBanner) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        BorderStroke(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE)),
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onOpenCashSettlement() },
                color = if (isDark) Color(0xFF0F1E36) else Color(0xFFEFF6FF)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cuadre de Caja Diario",
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$deliveredCount entregas • $${String.format("%.2f", deliveredUsd)} USD",
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0284C7)
                        ) {
                            Text(
                                text = "Abrir",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onUpdateSettings(settings.copy(showAdminSettlementBanner = false)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Ocultar", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Main Metric Card: "Remesas Hoy"
        if (settings.showRemesasCard) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(20.dp))
                    .clickable { onNavigateToDeliveries() },
                color = cardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(height = 42.dp, width = 56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(tealIconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Remesas Icon",
                                tint = mintGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = { onUpdateSettings(settings.copy(showRemesasCard = false)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Ocultar", tint = mutedText, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = settings.remesasCardTitle.ifEmpty { "Remesas Hoy" },
                        color = textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = displayRemesasStr,
                        color = textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val deliveredUsdForTrend = deliveries.filter { it.status == DeliveryStatus.DELIVERED }.sumOf { it.amountUsd }
                        val realTrendSummary = "${deliveries.count { it.status == DeliveryStatus.DELIVERED }} entregadas hoy ($${String.format(java.util.Locale.US, "%.2f", deliveredUsdForTrend)} USD)"
                        val displayTrend = if (settings.remesasTrendText.isNotBlank() && settings.remesasTrendText != "+12% vs día anterior") {
                            settings.remesasTrendText
                        } else {
                            realTrendSummary
                        }

                        Text(
                            text = displayTrend,
                            color = mintGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Tendencia",
                            tint = mintGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid Row: 2 Cards (Usuarios Activos & Pagos Pendientes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 2: Usuarios Activos -> Navigates to Clients
            if (settings.showUsersCard) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToClients() },
                    color = cardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tealIconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Usuarios Activos Icon",
                                    tint = mintGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(
                                onClick = { onUpdateSettings(settings.copy(showUsersCard = false)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Ocultar", tint = mutedText, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = settings.usersCardTitle.ifEmpty { "Clientes" },
                            color = textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = displayUsersStr,
                            color = textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = settings.usersBadgeText.ifEmpty { "Registrados" },
                            color = mintGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Card 3: Pagos Pendientes -> Navigates to History / Deliveries
            if (settings.showPendingCard) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToDeliveries() },
                    color = cardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Pagos Pendientes Icon",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(
                                onClick = { onUpdateSettings(settings.copy(showPendingCard = false)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Ocultar", tint = mutedText, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = settings.pendingCardTitle.ifEmpty { "Pendientes" },
                            color = textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = displayPendingStr,
                            color = textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = settings.pendingWarningText.ifEmpty { "Por entregar" },
                            color = amberWarning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Clientes por atender hoy (Por asignar) section
        val unassignedDeliveries = deliveries.filter { !it.isAssigned && it.status == DeliveryStatus.PENDING }
        var selectedDeliveryForAssign by remember { mutableStateOf<com.example.data.model.Delivery?>(null) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = settings.unassignedSectionTitle.ifEmpty { "Clientes por atender hoy" },
                    color = textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = settings.unassignedSectionSubtitle.ifEmpty { "Depósitos registrados por asignar" },
                    color = mutedText,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) Color(0xFF371B1E) else Color(0xFFFEE2E2))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${unassignedDeliveries.size} por asignar",
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (unassignedDeliveries.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(18.dp)),
                color = cardBg
            ) {
                Box(
                    modifier = Modifier.padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay depósitos pendientes por asignar en el día de hoy.",
                        color = mutedText,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                unassignedDeliveries.forEach { delivery ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                BorderStroke(1.dp, if (isDark) Color(0xFF371B1E) else Color(0xFFFECACA)),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedDeliveryForAssign = delivery },
                        color = cardBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isDark) Color(0xFF450A0A) else Color(0xFFFEE2E2))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "POR ASIGNAR",
                                        color = Color(0xFFDC2626),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = delivery.clientName,
                                        color = textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = delivery.date,
                                        color = mutedText,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${String.format("%.2f", delivery.amountUsd)} USD",
                                    color = mintGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { selectedDeliveryForAssign = delivery },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Asignar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedDeliveryForAssign?.let { delivery ->
            AssignWorkerDialog(
                delivery = delivery,
                workers = workers,
                isDark = isDark,
                onDismiss = { selectedDeliveryForAssign = null },
                onConfirmAssign = { worker ->
                    onAssignDelivery(delivery.id, worker.id, worker.name)
                    selectedDeliveryForAssign = null
                }
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ecosistema de Administración • NexFy Remesas",
                color = mutedText.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Dialog triggered by the Settings Wheel (⚙️)
@Composable
private fun AdminSettingsWheelDialog(
    settings: AppSettings,
    workers: List<Worker>,
    viewModel: NexFyViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, username: String, phone: String, email: String, updatedSettings: AppSettings) -> Unit,
    onOpenWorkerLinking: () -> Unit,
    onOpenCashSettlement: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nameInput by remember { mutableStateOf(settings.adminName) }
    var usernameInput by remember { mutableStateOf(settings.adminUsername) }
    var phoneInput by remember { mutableStateOf(settings.adminPhone) }
    var emailInput by remember { mutableStateOf(settings.adminEmail) }
    var pinInput by remember { mutableStateOf(settings.pinCode) }
    var currentRateState by remember(settings.usdCupRate) { mutableStateOf(settings.usdCupRate) }

    var darkModeState by remember { mutableStateOf(settings.darkMode) }
    var autoBackupState by remember { mutableStateOf(settings.autoBackup) }
    var backupFreqState by remember { mutableStateOf(settings.backupFrequency) }
    var onlineSyncState by remember { mutableStateOf(settings.googleDriveSyncEnabled) }
    var workspaceIdInput by remember { mutableStateOf(settings.firestoreWorkspaceId.ifEmpty { "ADMIN_NEXFY_01" }) }
    var firestoreSyncEnabledState by remember { mutableStateOf(settings.firestoreSyncEnabled) }

    val isFirestoreSyncing by viewModel.isFirestoreSyncing.collectAsState()
    val firestoreSyncStatus by viewModel.firestoreSyncStatus.collectAsState()

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    val linkedCount = workers.count { it.isLinked }

    val dialogBg = if (isDark) Color(0xFF0E1226) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDark) Color(0xFF2E384D) else Color(0xFFE2E8F0)
    val surfaceColor = if (isDark) Color(0xFF182238) else Color(0xFFF8FAFC)
    val innerSurfaceColor = if (isDark) Color(0xFF101626) else Color(0xFFF1F5F9)
    val mintGreen = Color(0xFF16D490)
    val skyBlue = Color(0xFF38BDF8)
    val amberOrange = Color(0xFFFF9100)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp)
                .padding(vertical = 20.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = dialogBg,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header (Icon, Title, Close Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(mintGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = mintGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ajustes del Ecosistema",
                                color = textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configuración global de administración",
                                color = textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Configura tu perfil, tema visual, copias en la nube y sincronizaciones en tiempo real.",
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    // --- SECCIÓN 1: PERFIL DEL ADMINISTRADOR ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = mintGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Perfil de Administrador",
                                    color = mintGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Nombre Completo", color = textSecondary, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = mintGreen, modifier = Modifier.size(20.dp)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedBorderColor = mintGreen,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = if (isDark) Color(0xFF101626) else Color.White,
                                    unfocusedContainerColor = if (isDark) Color(0xFF101626) else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = usernameInput,
                                onValueChange = { usernameInput = it },
                                label = { Text("Nombre de usuario", color = textSecondary, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = mintGreen, modifier = Modifier.size(20.dp)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedBorderColor = mintGreen,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = if (isDark) Color(0xFF101626) else Color.White,
                                    unfocusedContainerColor = if (isDark) Color(0xFF101626) else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("Teléfono de contacto", color = textSecondary, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = mintGreen, modifier = Modifier.size(20.dp)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedBorderColor = mintGreen,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = if (isDark) Color(0xFF101626) else Color.White,
                                    unfocusedContainerColor = if (isDark) Color(0xFF101626) else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Correo de Cuenta (Google Drive)", color = textSecondary, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = mintGreen, modifier = Modifier.size(20.dp)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedBorderColor = mintGreen,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = if (isDark) Color(0xFF101626) else Color.White,
                                    unfocusedContainerColor = if (isDark) Color(0xFF101626) else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { if (it.length <= 6) pinInput = it },
                                label = { Text("PIN de Acceso Administrador", color = textSecondary, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = mintGreen, modifier = Modifier.size(20.dp)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedBorderColor = mintGreen,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = if (isDark) Color(0xFF101626) else Color.White,
                                    unfocusedContainerColor = if (isDark) Color(0xFF101626) else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // --- SECCIÓN 1.5: CALCULADORA DE TASA DE CAMBIO EN VIVO (USD <-> CUP) ---
                    ExchangeRateCalculatorCard(
                        currentRate = currentRateState,
                        onRateChanged = { newRate ->
                            currentRateState = newRate
                            viewModel.updateSettings(settings.copy(usdCupRate = newRate))
                        },
                        isDark = isDark,
                        title = "Tasa de Cambio en Vivo (USD ⇄ CUP)",
                        subtitle = "Calcula automáticamente el valor en CUP para el ecosistema"
                    )

                    // --- SECCIÓN 1.6: ESTADO DE LICENCIA SAAS & PRUEBA GRATIS (ADMINISTRACIÓN) ---
                    val licenseState by viewModel.licenseState.collectAsState()
                    val adminDaysRemaining = licenseState.daysRemaining
                    val adminExpFormatted = licenseState.expirationFormatted
                    val isTrialMode = licenseState.isTrial
                    val adminDeviceId by viewModel.deviceId.collectAsState()

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isTrialMode) "Prueba Gratis (30 Días)" else "Licencia Anual SaaS (365 Días)",
                                            color = textPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Vence el: $adminExpFormatted ($adminDaysRemaining días restantes)",
                                            color = textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (adminDaysRemaining > 7) Color(0xFF10B981) else Color(0xFFF59E0B)
                                ) {
                                    Text(
                                        text = if (isTrialMode) "$adminDaysRemaining días prueba" else if (adminDaysRemaining > 0) "$adminDaysRemaining días" else "Por renovar",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xFF101626) else Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "ID Dispositivo: $adminDeviceId", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Copiar ID",
                                        color = Color(0xFF6366F1),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            val clip = android.content.ClipData.newPlainText("ID de Dispositivo", adminDeviceId)
                                            (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                                            Toast.makeText(context, "ID copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    Text(
                                        text = "WhatsApp",
                                        color = Color(0xFF16A34A),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            try {
                                                val msg = "Hola soporte de NexFy, solicito la clave de activación anual para mi ID de dispositivo: $adminDeviceId"
                                                val intent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://api.whatsapp.com/send?phone=51076491&text=${Uri.encode(msg)}")
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "ID: $adminDeviceId listo para enviar", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // --- SECCIÓN 2: SELECCIÓN DE TEMA (CLARO / OSCURO) ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(skyBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = skyBlue, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Tema de la Aplicación",
                                        color = textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (darkModeState) "Modo Oscuro Activo" else "Modo Claro Activo",
                                        color = textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Switch(
                                checked = darkModeState,
                                onCheckedChange = { darkModeState = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = mintGreen,
                                    checkedTrackColor = mintGreen.copy(alpha = 0.3f),
                                    uncheckedThumbColor = Color(0xFF94A3B8),
                                    uncheckedTrackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }

                    // --- SECCIÓN 3: GOOGLE DRIVE BACKUP Y SINCRONIZACIÓN ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(mintGreen.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = mintGreen, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Copia en Google Drive",
                                            color = textPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Guardar respaldo de datos en tu cuenta",
                                            color = textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = autoBackupState,
                                    onCheckedChange = { autoBackupState = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = mintGreen,
                                        checkedTrackColor = mintGreen.copy(alpha = 0.3f),
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                                    )
                                )
                            }

                            if (autoBackupState) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Frecuencia de copia de seguridad:",
                                        color = textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("Diaria", "Semanal", "Mensual", "Al cerrar").forEach { freq ->
                                            val isSel = backupFreqState == freq
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { backupFreqState = freq },
                                                color = if (isSel) mintGreen else innerSurfaceColor,
                                                border = BorderStroke(1.dp, if (isSel) mintGreen else borderColor)
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = freq,
                                                        color = if (isSel) Color.Black else textPrimary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Toggle Sincronización Online
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(skyBlue.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null, tint = skyBlue, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Sincronización Online",
                                            color = textPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Sincronizar todo al conectarse a Internet",
                                            color = textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = onlineSyncState,
                                    onCheckedChange = { onlineSyncState = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = skyBlue,
                                        checkedTrackColor = skyBlue.copy(alpha = 0.3f),
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                                    )
                                )
                            }

                            // Botones de acción manual: Crear Copia y Sincronizar
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.createBackupJsonString(emailInput)
                                            Toast.makeText(context, "Copia guardada con éxito en Google Drive ($emailInput)", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = mintGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Crear Copia", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Sincronizando todos los datos con la nube...", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, skyBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = skyBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sincronizar", color = skyBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }

                            // Botón de Importar copia de seguridad en caso de pérdida o reinstalación del dispositivo
                            OutlinedButton(
                                onClick = { showRestoreConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Color(0xFFA855F7)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Importar Última Copia de Seguridad", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }

                    // --- SECCIÓN 4: BASE DE DATOS FIREBASE & FIRESTORE ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor,
                        border = BorderStroke(1.dp, amberOrange.copy(alpha = if (isDark) 0.5f else 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(amberOrange.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = "Sincronización de Base de Datos",
                                            tint = amberOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Base de Datos en la Nube",
                                            color = textPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Flujo y sincronización en tiempo real",
                                            color = if (isDark) Color(0xFFFFB74D) else Color(0xFFD97706),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = firestoreSyncEnabledState,
                                    onCheckedChange = { firestoreSyncEnabledState = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = amberOrange,
                                        checkedTrackColor = amberOrange.copy(alpha = 0.3f),
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                                    )
                                )
                            }

                            // Espacio / Workspace ID para Aislamiento estricto
                            OutlinedTextField(
                                value = workspaceIdInput,
                                onValueChange = { workspaceIdInput = it.uppercase().trim() },
                                label = { Text("Código de Espacio Privado (Workspace ID)", color = if (isDark) Color(0xFFFFB74D) else Color(0xFFD97706), fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = amberOrange, modifier = Modifier.size(20.dp)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedBorderColor = amberOrange,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = if (isDark) Color(0xFF101626) else Color.White,
                                    unfocusedContainerColor = if (isDark) Color(0xFF101626) else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Información de Aislamiento Estricto
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = innerSurfaceColor,
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = mintGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Espacio 100% aislado: Tus entregas, comprobantes y trabajadores se respaldan de forma segura y privada.",
                                        color = textSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            // Estado de conexión / Sincronización
                            Text(
                                text = "Última sincronización: ${settings.lastFirestoreSyncDate.ifBlank { "Pendiente" }}",
                                color = textSecondary,
                                fontSize = 11.sp
                            )

                            // Botón para Forzar Guardado y Sincronización con la Base de Datos
                            Button(
                                onClick = {
                                    val updated = settings.copy(
                                        adminName = nameInput,
                                        adminUsername = usernameInput,
                                        adminPhone = phoneInput,
                                        adminEmail = emailInput,
                                        pinCode = pinInput,
                                        darkMode = darkModeState,
                                        autoBackup = autoBackupState,
                                        backupFrequency = backupFreqState,
                                        googleDriveSyncEnabled = onlineSyncState,
                                        firestoreWorkspaceId = workspaceIdInput.ifBlank { "ADMIN_NEXFY_01" },
                                        firestoreSyncEnabled = firestoreSyncEnabledState
                                    )
                                    viewModel.updateSettings(updated)
                                    viewModel.saveToFirestore(workspaceIdInput) { ok, msg ->
                                        Toast.makeText(context, if (ok) "✅ ¡Datos guardados y sincronizados con la base de datos!" else "Error al sincronizar: $msg", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = amberOrange),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isFirestoreSyncing
                            ) {
                                if (isFirestoreSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sincronizando base de datos...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Forzar Guardado y Sincronización de Base de Datos", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // --- SECCIÓN 5: CUADRE DE CAJA Y LIQUIDACIONES ---
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenCashSettlement() },
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(skyBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = skyBlue, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cuadre de Caja Diario & Liquidaciones",
                                    color = textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Balances por mensajero, comisiones y exportación",
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = textSecondary)
                        }
                    }

                    // --- SECCIÓN 6: VINCULAR TRABAJADORES ---
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenWorkerLinking() },
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(mintGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = mintGreen, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vincular Trabajadores",
                                    color = textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$linkedCount de ${workers.size} vinculados por ID",
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = textSecondary)
                        }
                    }

                    // --- SECCIÓN 7: CAMBIAR ECOSISTEMA ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor,
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Cambiar Ecosistema",
                                        color = textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Alterna entre Administrador y Usuario",
                                        color = textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "Actualmente estás usando el ecosistema de Administrador. Puedes cambiar tu rol para acceder a las pantallas del repartidor.",
                                color = textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Button(
                                onClick = {
                                    viewModel.selectEcosystemRole("")
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Text("Volver a Selección de Ecosistema", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Buttons (Centrados y ordenados)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            val updated = settings.copy(
                                adminName = nameInput,
                                adminUsername = usernameInput,
                                adminPhone = phoneInput,
                                adminEmail = emailInput,
                                pinCode = pinInput,
                                usdCupRate = currentRateState,
                                darkMode = darkModeState,
                                autoBackup = autoBackupState,
                                backupFrequency = backupFreqState,
                                googleDriveSyncEnabled = onlineSyncState,
                                firestoreWorkspaceId = workspaceIdInput.ifBlank { "ADMIN_NEXFY_01" },
                                firestoreSyncEnabled = firestoreSyncEnabledState
                            )
                            onSave(nameInput, usernameInput, phoneInput, emailInput, updated)
                            if (firestoreSyncEnabledState) {
                                viewModel.saveToFirestore(workspaceIdInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = mintGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Guardar Cambios", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Cancelar", color = textSecondary, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Modal de confirmación para Importar copia de seguridad tras re-instalación o pérdida de dispositivo
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            containerColor = dialogBg,
            icon = {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    text = "Recuperar Datos desde Google Drive",
                    color = textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Se importará la última copia de seguridad guardada en tu cuenta de Google Drive ($emailInput).\n\nAl restaurar, recuperarás automáticamente tus entregas, lista de clientes y configuraciones guardadas tras iniciar sesión.",
                    color = textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val jsonBackup = viewModel.createBackupJsonString(emailInput)
                            val ok = viewModel.restoreFromBackupJson(jsonBackup)
                            showRestoreConfirmDialog = false
                            if (ok) {
                                Toast.makeText(context, "¡Copia de seguridad importada con éxito! Todos tus datos han sido restaurados.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "No se pudo leer la copia de seguridad", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                ) {
                    Text(text = "Importar y Restaurar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text(text = "Cancelar", color = textSecondary)
                }
            }
        )
    }
}

// Dialog listing workers with Red/Green indicators and linking option
@Composable
private fun WorkerLinkingListDialog(
    workers: List<Worker>,
    onDismiss: () -> Unit,
    onSelectLinkWorker: (Worker) -> Unit,
    onUnlinkWorker: (Long) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .padding(vertical = 20.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF0E1226),
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, Color(0xFF2E384D))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF16D490).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = Color(0xFF16D490), modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Vincular Trabajadores",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Asignación y estado de conexión",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Lista de trabajadores registrados en la plataforma. Utiliza el ID de dispositivo enviado por el trabajador para vincularlo.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    if (workers.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF161E33),
                            border = BorderStroke(1.dp, Color(0xFF2E384D))
                        ) {
                            Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No hay trabajadores registrados en la base de datos.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        workers.forEach { worker ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp)),
                                color = Color(0xFF161E33),
                                border = BorderStroke(1.dp, if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444).copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                if (worker.isLinked) Color(0xFF10B981).copy(alpha = 0.2f)
                                                else Color(0xFFEF4444).copy(alpha = 0.2f)
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = worker.name,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = worker.role,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 12.sp
                                            )
                                            if (worker.deviceId.isNotEmpty()) {
                                                Text(
                                                    text = "ID: ${worker.deviceId}",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        // RED or GREEN indicator badge
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (worker.isLinked) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444))
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (worker.isLinked) "Vinculado" else "No vinculado",
                                                    color = if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action button
                                    if (!worker.isLinked) {
                                        Button(
                                            onClick = { onSelectLinkWorker(worker) },
                                            modifier = Modifier.fillMaxWidth().height(38.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16D490)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Link, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "Vincular por Código ID", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { onUnlinkWorker(worker.id) },
                                            modifier = Modifier.fillMaxWidth().height(34.dp),
                                            border = BorderStroke(1.dp, Color(0xFF64748B)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(text = "Desvincular", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Cerrar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// Dialog for entering the Device ID code to link a specific worker
@Composable
private fun WorkerDeviceIdInputDialog(
    worker: Worker,
    onDismiss: () -> Unit,
    onConfirmLink: (deviceIdCode: String) -> Unit
) {
    var deviceIdInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0E1226),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PhonelinkSetup, contentDescription = null, tint = Color(0xFF16D490))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Vincular ID de Dispositivo",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Trabajador: ${worker.name}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Introduce a continuación el código ID único del dispositivo enviado por el trabajador (vía WhatsApp o SMS). Es el mismo ID de su pantalla de Ajustes / Desbloqueo:",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = deviceIdInput,
                    onValueChange = {
                        deviceIdInput = it
                        isError = false
                    },
                    label = { Text("Código ID del Dispositivo", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("Ej: a1b2c3d4e5f6", color = Color(0xFF475569)) },
                    singleLine = true,
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text(
                        text = "Por favor ingresa un código ID válido",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (deviceIdInput.trim().isEmpty()) {
                        isError = true
                    } else {
                        onConfirmLink(deviceIdInput.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16D490))
            ) {
                Text(text = "Guardar y Vincular", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
private fun AdminTrabajadoresContent(
    workers: List<Worker>,
    deliveries: List<com.example.data.model.Delivery>,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    mintGreen: Color,
    mutedText: Color,
    isDark: Boolean,
    onBack: () -> Unit,
    onLinkWorker: (Worker) -> Unit,
    onAddWorker: (name: String, role: String, phone: String, email: String, address: String) -> Unit,
    onUpdateWorker: (Worker) -> Unit,
    onDeleteWorker: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedWorkerForDetail by remember { mutableStateOf<Worker?>(null) }
    var selectedWorkerForEdit by remember { mutableStateOf<Worker?>(null) }
    var selectedWorkerForDelete by remember { mutableStateOf<Worker?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trabajadores",
                        color = textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gestión de personal, fichas e historial de entregas",
                        color = textSecondary,
                        fontSize = 14.sp
                    )
                }
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(cardBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (workers.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(16.dp)),
                    color = cardBg
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay trabajadores registrados. Pulsa el botón '+' para agregar el primero.",
                            color = mutedText,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                workers.forEach { worker ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444).copy(alpha = 0.5f)
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedWorkerForDetail = worker },
                        color = cardBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (worker.isLinked) Color(0xFF10B981).copy(alpha = 0.2f)
                                        else Color(0xFFEF4444).copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = worker.name.take(1).uppercase(),
                                    color = if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = worker.name,
                                    color = textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (worker.phone.isNotEmpty()) "${worker.role} • 📞 ${worker.phone}" else worker.role,
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (worker.isLinked) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (worker.isLinked) "Vinculado" else "No vinculado",
                                            color = if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Floating Action Button (+) for registering new worker
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp),
            containerColor = mintGreen,
            contentColor = Color.Black
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Registrar Trabajador", modifier = Modifier.size(28.dp))
        }
    }

    // Modal / Dialog: Add New Worker
    if (showAddDialog) {
        AddWorkerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, role, phone, email, address ->
                onAddWorker(name, role, phone, email, address)
                showAddDialog = false
            }
        )
    }

    // Modal / Dialog: Worker Detail (Ficha del Trabajador)
    selectedWorkerForDetail?.let { worker ->
        WorkerDetailDialog(
            worker = worker,
            deliveries = deliveries,
            onDismiss = { selectedWorkerForDetail = null },
            onEdit = {
                selectedWorkerForEdit = worker
                selectedWorkerForDetail = null
            },
            onDelete = {
                selectedWorkerForDelete = worker
                selectedWorkerForDetail = null
            },
            onLinkDevice = {
                onLinkWorker(worker)
                selectedWorkerForDetail = null
            }
        )
    }

    // Modal / Dialog: Edit Worker
    selectedWorkerForEdit?.let { worker ->
        EditWorkerDialog(
            worker = worker,
            onDismiss = { selectedWorkerForEdit = null },
            onSave = { updatedWorker ->
                onUpdateWorker(updatedWorker)
                selectedWorkerForEdit = null
            }
        )
    }

    // Modal / Dialog: Delete / Unlink Confirmation
    selectedWorkerForDelete?.let { worker ->
        AlertDialog(
            onDismissRequest = { selectedWorkerForDelete = null },
            containerColor = Color(0xFF0E1226),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Eliminar y Desvincular",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar a '${worker.name}'? Se desvinculará su dispositivo y se eliminará todo su registro del sistema.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteWorker(worker.id)
                        selectedWorkerForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Eliminar y Desvincular", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedWorkerForDelete = null }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, role: String, phone: String, email: String, address: String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var roleInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0E1226),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF16D490))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Registrar Trabajador",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ingresa los datos para crear la ficha del trabajador:",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Nombre Completo *", color = Color(0xFF94A3B8)) },
                    isError = isError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = roleInput,
                    onValueChange = { roleInput = it },
                    label = { Text("Cargo / Rol", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("Ej: Entregador Zona Centro", color = Color(0xFF475569)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Número de Teléfono", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF16D490)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Correo Electrónico", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF16D490)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Dirección", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF16D490)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.trim().isEmpty()) {
                        isError = true
                    } else {
                        onSave(nameInput.trim(), roleInput.trim(), phoneInput.trim(), emailInput.trim(), addressInput.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16D490))
            ) {
                Text("Guardar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
private fun EditWorkerDialog(
    worker: Worker,
    onDismiss: () -> Unit,
    onSave: (Worker) -> Unit
) {
    var nameInput by remember { mutableStateOf(worker.name) }
    var roleInput by remember { mutableStateOf(worker.role) }
    var phoneInput by remember { mutableStateOf(worker.phone) }
    var emailInput by remember { mutableStateOf(worker.email) }
    var addressInput by remember { mutableStateOf(worker.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0E1226),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Editar Ficha de Trabajador",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nombre Completo", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = roleInput,
                    onValueChange = { roleInput = it },
                    label = { Text("Cargo / Rol", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Teléfono", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Correo Electrónico", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Dirección", color = Color(0xFF94A3B8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF2E384D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        worker.copy(
                            name = nameInput.trim(),
                            role = roleInput.trim(),
                            phone = phoneInput.trim(),
                            email = emailInput.trim(),
                            address = addressInput.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16D490))
            ) {
                Text("Guardar Cambios", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
private fun WorkerDetailDialog(
    worker: Worker,
    deliveries: List<com.example.data.model.Delivery>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLinkDevice: () -> Unit
) {
    val context = LocalContext.current
    val assignedDeliveries = remember(deliveries, worker) {
        deliveries.filter {
            it.assignedWorkerId == worker.id || (it.assignedWorkerName.isNotBlank() && it.assignedWorkerName.equals(worker.name, ignoreCase = true))
        }
    }
    val totalUsdDelivered = remember(assignedDeliveries) {
        assignedDeliveries.filter { it.status == com.example.data.model.DeliveryStatus.DELIVERED }.sumOf { it.amountUsd }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0E1226),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (worker.isLinked) Color(0xFF10B981) else Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = worker.name.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = worker.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = worker.role,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Ficha",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // --- DATOS DEL TRABAJADOR ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF141C30),
                    border = BorderStroke(1.dp, Color(0xFF23304D))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📋 DATOS DEL TRABAJADOR",
                            color = Color(0xFF16D490),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (worker.phone.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = worker.phone, color = Color.White, fontSize = 13.sp)
                            }
                        }

                        if (worker.email.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = worker.email, color = Color.White, fontSize = 13.sp)
                            }
                        }

                        if (worker.address.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = worker.address, color = Color.White, fontSize = 13.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (worker.isLinked) "Vinculado ✓ (ID: ${worker.deviceId})" else "No vinculado",
                                color = if (worker.isLinked) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (!worker.isLinked) {
                                TextButton(
                                    onClick = onLinkDevice,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Vincular ahora", color = Color(0xFF16D490), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // --- ICONOS DE CONTACTO DIRECTO (Llamada, SMS, WhatsApp) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 1. Teléfono / Llamar
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (worker.phone.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "No hay número de teléfono registrado", Toast.LENGTH_SHORT).show()
                                }
                            },
                        color = Color(0xFF1E293B)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Llamar", tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Llamar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // 2. SMS
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (worker.phone.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${worker.phone}"))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "No hay número de teléfono registrado", Toast.LENGTH_SHORT).show()
                                }
                            },
                        color = Color(0xFF1E293B)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = "SMS", tint = Color(0xFFF59E0B), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("SMS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // 3. WhatsApp
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (worker.phone.isNotEmpty()) {
                                    val cleanPhone = worker.phone.replace(" ", "").replace("+", "")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone"))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "No hay número de teléfono registrado", Toast.LENGTH_SHORT).show()
                                }
                            },
                        color = Color(0xFF14532D)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF22C55E), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- HISTORIAL DE ENTREGAS ASIGNADAS ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📜 HISTORIAL DE ENTREGAS",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Total entregado: $${String.format("%.2f", totalUsdDelivered)} USD",
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (assignedDeliveries.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = Color(0xFF161E33)
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No se le han asignado entregas a este trabajador aún.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        assignedDeliveries.forEach { delivery ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                color = Color(0xFF161E33),
                                border = BorderStroke(1.dp, Color(0xFF23304D))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = delivery.clientName,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        val (statusText, statusBg, statusTextColor) = when (delivery.status) {
                                            com.example.data.model.DeliveryStatus.DELIVERED -> Triple("ENTREGADA ✓", Color(0xFF064E3B), Color(0xFF34D399))
                                            com.example.data.model.DeliveryStatus.PENDING -> Triple("ASIGNADA", Color(0xFF451A03), Color(0xFFFBBF24))
                                            else -> Triple("PENDIENTE", Color(0xFF450A0A), Color(0xFFFCA5A5))
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = statusBg
                                        ) {
                                            Text(
                                                text = statusText,
                                                color = statusTextColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Monto: $${String.format("%.2f", delivery.amountUsd)} USD",
                                            color = Color(0xFF10B981),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = delivery.date,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (delivery.address.isNotEmpty()) {
                                        Text(
                                            text = "🏠 ${delivery.address}",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // --- BOTÓN ELIMINAR Y DESVINCULAR ---
                Button(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar y Desvincular Trabajador", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
            ) {
                Text("Cerrar", color = Color.White)
            }
        }
    )
}

@Composable
private fun AdminSoporteContent(
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    mintGreen: Color,
    mutedText: Color,
    isDark: Boolean,
    onBack: () -> Unit,
    onOpenContributionScreen: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Centro de Soporte y Contribuciones",
                    color = textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Atención técnica prioritaria y plataforma de contribución para NeoApp y NexFy Remesas",
                    color = textSecondary,
                    fontSize = 14.sp
                )
            }
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(cardBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))

        // Card 1: Correo Electrónico neoappsoluciones@gmail.com
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)),
            color = cardBg
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Correo Electrónico de Soporte",
                            color = textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "neoappsoluciones@gmail.com",
                            color = Color(0xFF38BDF8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Envía tus inquietudes, consultas, sugerencias y opiniones directamente a nuestro correo oficial de soporte.",
                    color = textSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:neoappsoluciones@gmail.com")
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Sugerencias y Opiniones - NexFy Remesas")
                            }
                            try {
                                context.startActivity(android.content.Intent.createChooser(intent, "Enviar correo a neoappsoluciones@gmail.com..."))
                            } catch (e: Exception) {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Soporte Email", "neoappsoluciones@gmail.com")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Correo neoappsoluciones@gmail.com copiado al portapapeles", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Escribir Correo", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Soporte Email", "neoappsoluciones@gmail.com")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "neoappsoluciones@gmail.com copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Copiar", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 2: Hacer una Contribución
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)),
            color = cardBg
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(mintGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Contribución",
                            tint = mintGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Hacer una Contribución",
                            color = textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Apoya el desarrollo de NeoApp",
                            color = mintGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Abre la pantalla de pago para realizar aportes o contribuciones destinadas al mantenimiento y mejora continua de las herramientas del sistema.",
                    color = textSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onOpenContributionScreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Contribución",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Realizar una Contribución",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeElementEditorDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var textState by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0E1226),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF16D490)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Modifica el valor o referencia seleccionada en Home (sincronizado con el ecosistema):",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF16D490),
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(textState)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16D490)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Cambios", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
private fun PaymentContributionScreenDialog(
    onDismiss: () -> Unit
) {
    PaymentContributionDialog(
        onDismiss = onDismiss,
        isDarkTheme = true
    )
}

@Composable
private fun AdminBottomNavigationBar(
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    mintGreen: Color,
    navBg: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .widthIn(max = 800.dp)
            .height(82.dp),
        color = navBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = listOf(
                Pair("Home", Icons.Default.Home),
                Pair("Clientes", Icons.Default.People),
                Pair("Trabajadores", Icons.Default.AssignmentInd),
                Pair("Historial", Icons.Default.History),
                Pair("Soporte", Icons.Default.Headset)
            )

            navItems.forEachIndexed { index, (label, icon) ->
                val isSelected = selectedIndex == index
                val itemColor = if (isSelected) mintGreen else if (isDark) Color(0xFF8E9BB4) else Color(0xFF64748B)

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectIndex(index) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("admin_tab_$index"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = itemColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = itemColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AssignWorkerDialog(
    delivery: com.example.data.model.Delivery,
    workers: List<Worker>,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirmAssign: (Worker) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    val dialogBg = if (isDark) Color(0xFF0E1226) else Color.White
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = Color(0xFF16D490))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Asignar Trabajador",
                            color = textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = textPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cliente: ${delivery.clientName} ($${String.format("%.2f", delivery.amountUsd)} USD)",
                    color = textSecondary,
                    fontSize = 13.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡 Toca o mantén pulsado un trabajador para seleccionar y habilitar la opción de asignar:",
                    color = Color(0xFF7E8C9E),
                    fontSize = 12.sp
                )

                if (workers.isEmpty()) {
                    Text(
                        text = "No hay trabajadores registrados en la lista.",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp
                    )
                } else {
                    workers.forEach { worker ->
                        val isSelected = selectedWorker?.id == worker.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF16D490) else Color(0xFF192038)
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .combinedClickable(
                                    onClick = { selectedWorker = worker },
                                    onLongClick = { selectedWorker = worker }
                                ),
                            color = if (isSelected) Color(0xFF103632) else Color(0xFF162032)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF16D490) else Color(0xFF1E293B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = worker.name.take(1).uppercase(),
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = worker.name,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (worker.isLinked) "Vinculado ✓ (ID: ${worker.deviceId})" else "No vinculado",
                                            color = if (worker.isLinked) Color(0xFF16D490) else Color(0xFFEF4444),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Seleccionado",
                                        tint = Color(0xFF16D490),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedWorker?.let { worker ->
                        onConfirmAssign(worker)
                        Toast.makeText(context, "Entrega asignada a ${worker.name} correctamente ✓", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = selectedWorker != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16D490),
                    disabledContainerColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Asignar",
                    color = if (selectedWorker != null) Color.Black else Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF94A3B8))
            }
        }
    )
}
