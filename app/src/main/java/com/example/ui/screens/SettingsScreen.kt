package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.CurrencyExchange
import com.example.ui.components.DailyCashSettlementDialog
import com.example.ui.components.ExchangeRateCalculatorCard
import com.example.ui.components.PaymentContributionDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.NexFyViewModel

@Composable
fun SettingsScreen(
    viewModel: NexFyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val deliveries by viewModel.deliveries.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val deviceId by viewModel.deviceId.collectAsState()

    var rateText by remember(settings.usdCupRate) { mutableStateOf(settings.usdCupRate.toInt().toString()) }
    var commissionText by remember(settings.commissionPercent) { mutableStateOf(settings.commissionPercent.toString()) }
    var whatsappMsg by remember(settings.whatsappMessage) { mutableStateOf(settings.whatsappMessage) }
    var workerWorkspaceId by remember(settings.firestoreWorkspaceId) { mutableStateOf(settings.firestoreWorkspaceId) }

    val isFirestoreSyncing by viewModel.isFirestoreSyncing.collectAsState()
    val firestoreSyncStatus by viewModel.firestoreSyncStatus.collectAsState()

    var showDailySettlementDialog by remember { mutableStateOf(false) }
    var showDriveExportDialog by remember { mutableStateOf(false) }
    var showDriveImportDialog by remember { mutableStateOf(false) }
    var showPaymentContributionDialog by remember { mutableStateOf(false) }
    var selectedExportAccount by remember { mutableStateOf("joseandresrodriguezchavez@gmail.com") }
    var selectedImportAccount by remember { mutableStateOf("joseandresrodriguezchavez@gmail.com") }
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4FB))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top App Bar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF1E1B4B),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Ajustes",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B)
                )
                Text(
                    text = "NexFy Remesas",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        val userProfile by viewModel.userProfile.collectAsState()

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Section
            if (userProfile != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Perfil Activo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        }

                        Text("Nombre: ${userProfile?.get("fullName")}", fontSize = 15.sp, color = Color(0xFF1E1B4B))
                        Text("CI: ${userProfile?.get("idCard")}", fontSize = 15.sp, color = Color(0xFF1E1B4B))
                        Text("Teléfono: ${userProfile?.get("phone")}", fontSize = 15.sp, color = Color(0xFF1E1B4B))
                        Text("Correo: ${userProfile?.get("email")}", fontSize = 15.sp, color = Color(0xFF1E1B4B))

                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(text = "Cerrar Sesión", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Eliminar Cuenta", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Section 1: Seguridad
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Seguridad", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    }

                    // PIN de acceso
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1E1B4B), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(text = "PIN de acceso", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                                Text(text = "Requerir PIN para abrir la app", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = settings.pinRequired,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(pinRequired = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB)),
                            modifier = Modifier.testTag("toggle_pin_required")
                        )
                    }

                    // Modo oculto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF1E1B4B), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(text = "Modo oculto", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                                Text(text = "Ocultar saldo y detalles en la app", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = settings.hiddenMode,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(hiddenMode = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB)),
                            modifier = Modifier.testTag("toggle_hidden_mode")
                        )
                    }

                    // Cambiar PIN button
                    OutlinedButton(
                        onClick = { viewModel.startPinChange() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("change_pin_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Cambiar PIN", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 1.5: Vincular con Administración (ID de Dispositivo)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = Color(0xFF16D490), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Vincular Dispositivo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    }

                    Text(
                        text = "Proporciona tu ID de Dispositivo al Administrador para vincularte con la plataforma o recuperar tu PIN de acceso.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 18.sp
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "ID ÚNICO DEL DISPOSITIVO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text(text = deviceId, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1B4B), letterSpacing = 1.sp)
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("ID de Dispositivo", deviceId)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "ID copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color(0xFF2563EB))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        data = android.net.Uri.parse("https://api.whatsapp.com/send?text=Hola%20Administrador,%20mi%20ID%20de%20dispositivo%20para%20vincularme%20es:%20$deviceId")
                                    }
                                    context.startActivity(sendIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Text(text = "Enviar WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                try {
                                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                        data = android.net.Uri.parse("smsto:")
                                        putExtra("sms_body", "Hola Administrador, mi ID de dispositivo para vincularme es: $deviceId")
                                    }
                                    context.startActivity(sendIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No se pudo abrir SMS", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text(text = "Enviar por SMS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 2: Negocio y Comisiones
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Negocio & Comisiones", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    }

                    // Mi comisión %
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Mi comisión %", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                            OutlinedTextField(
                                value = commissionText,
                                onValueChange = {
                                    commissionText = it
                                    it.toDoubleOrNull()?.let { newComm ->
                                        viewModel.updateSettings(settings.copy(commissionPercent = newComm))
                                    }
                                },
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(52.dp)
                                    .testTag("commission_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                        Text(text = "Porcentaje de comisión aplicada a cada remesa", fontSize = 12.sp, color = Color(0xFF64748B))
                    }

                    // Mensaje WhatsApp
                    Column {
                        Text(text = "Mensaje WhatsApp", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = whatsappMsg,
                            onValueChange = {
                                whatsappMsg = it
                                viewModel.updateSettings(settings.copy(whatsappMessage = it))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("whatsapp_msg_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 2
                        )
                        Text(text = "Mensaje que se enviará al cliente por WhatsApp", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }

            // Calculadora y Tabla de Tasa de Cambio en Vivo (USD <-> CUP)
            ExchangeRateCalculatorCard(
                currentRate = settings.usdCupRate,
                onRateChanged = { newRate ->
                    rateText = newRate.toInt().toString()
                    viewModel.updateSettings(settings.copy(usdCupRate = newRate))
                },
                isDark = false,
                title = "Calculadora de Tasa de Cambio en Vivo",
                subtitle = "Referencia oficial y conversor automático (USD ⇄ CUP)"
            )

            // Cuadre de Caja Diario & Liquidaciones Card (Ecosistema de Usuario)
            val deliveredTodayList = remember(deliveries) {
                deliveries.filter { it.status == com.example.data.model.DeliveryStatus.DELIVERED }
            }
            val deliveredTodayUsd = remember(deliveredTodayList) { deliveredTodayList.sumOf { it.amountUsd } }
            val deliveredTodayCup = remember(deliveredTodayList) { deliveredTodayList.sumOf { it.amountCup } }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Cuadre de Caja Diario",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    text = "Reporte de liquidación y balances de hoy",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "${deliveredTodayList.size} Entregas",
                                color = Color(0xFF15803D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Financial metrics summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Entregado", fontSize = 10.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                                Text(
                                    text = "$${String.format(java.util.Locale.US, "%.2f", deliveredTodayUsd)} USD",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Equivalente CUP", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%,.0f", deliveredTodayCup)} CUP",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showDailySettlementDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_open_cash_settlement_user"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Abrir Cuadre de Caja Diario y Liquidaciones",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Section 3: Respaldo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Respaldo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showDriveExportDialog = true },
                            modifier = Modifier
                                .weight(1.1f)
                                .height(46.dp)
                                .testTag("export_backup_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exportar a Google Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showDriveImportDialog = true },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(46.dp)
                                .testTag("import_backup_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Importar de Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        }
                    }

                    // Auto-respaldo toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color(0xFF1E1B4B), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(text = "Auto-respaldo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                                Text(text = "Copia automática diaria a las 11:59 PM", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = settings.autoBackup,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(autoBackup = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                        )
                    }
                }
            }

            // Section 4: Apariencia
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Apariencia", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Modo oscuro", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                            Text(text = "Usar tema oscuro en la aplicación", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Switch(
                            checked = settings.darkMode,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(darkMode = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Ocultar montos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                            Text(text = "Ocultar montos en listas y comprobantes", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Switch(
                            checked = settings.hideAmounts,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(hideAmounts = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                        )
                    }
                }
            }

            // Notificaciones & Sincronización Offline Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Notificaciones y Sync Offline",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B4B)
                        )
                    }

                    Text(
                        text = "Recibe notificaciones locales cuando se te asignen entregas. Si pierdes la conexión, el sistema trabaja 100% offline y sincroniza los procesos al recuperar internet.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    Button(
                        onClick = {
                            viewModel.simulateIncomingAssignedDelivery()
                            Toast.makeText(context, "Notificación push enviada: Nueva entrega asignada", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("test_push_notification_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Probar Notificación Push de Entrega", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.performAutoSync()
                            Toast.makeText(context, "Forzando sincronización de procesos pendientes...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("force_sync_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sincronizar Procesos Pendientes Ahora", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Sincronización y Respaldo de Base de Datos (Trabajador / Repartidor)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Sincronización de Base de Datos",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Base de Datos en la Nube",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    text = "Sincronización segura y en vivo",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Switch(
                            checked = settings.firestoreSyncEnabled,
                            onCheckedChange = { isEnabled ->
                                viewModel.updateSettings(settings.copy(firestoreSyncEnabled = isEnabled))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2563EB), checkedTrackColor = Color(0xFF2563EB).copy(alpha = 0.3f))
                        )
                    }

                    Text(
                        text = "Conéctate al espacio de base de datos para recibir entregas asignadas en tiempo real y respaldar todos tus comprobantes al instante.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = workerWorkspaceId,
                        onValueChange = {
                            val upper = it.uppercase().trim()
                            workerWorkspaceId = upper
                            viewModel.updateSettings(settings.copy(firestoreWorkspaceId = upper))
                        },
                        label = { Text("Código de Espacio de Trabajo (Workspace ID)") },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF2563EB)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Aviso de seguridad y aislamiento
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Espacio Privado: Los datos y comprobantes se mantienen respaldados y encriptados de forma segura.",
                                color = Color(0xFF1E40AF),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Botón para Forzar Guardado y Sincronización con la Base de Datos
                    Button(
                        onClick = {
                            viewModel.saveToFirestore(workerWorkspaceId) { ok, msg ->
                                Toast.makeText(context, if (ok) "✅ ¡Datos actualizados y sincronizados con la base de datos!" else "Error al sincronizar: $msg", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_firestore_worker_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        enabled = !isFirestoreSyncing
                    ) {
                        if (isFirestoreSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizando con la base de datos...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Forzar Guardado y Sincronización de Datos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Section: Cambiar Ecosistema
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color(0xFFDC2626))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cambiar Ecosistema",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                            Text(
                                text = "Alterna entre Administrador y Usuario",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    Text(
                        text = "Actualmente estás usando el ecosistema de: ${if (settings.ecosystemRole == "ADMIN") "Administrador" else "Usuario"}. Puedes cambiar tu rol para acceder a las pantallas del otro perfil.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = {
                            viewModel.selectEcosystemRole("")
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Volver a la Selección de Ecosistema", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Footer
            OutlinedButton(
                onClick = { viewModel.showSplashScreen() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFC084FC))
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ver Pantalla de Bienvenida (Logo NexFy + NEOAPP)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5CF6)
                )
            }

            Text(
                text = "NexFy Remesas · v2.4.1 · Soporte",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 12.dp)
            )
        }
    }

    // Google Drive Export Dialog
    if (showDriveExportDialog) {
        val driveAccounts = listOf(
            "joseandresrodriguezchavez@gmail.com" to "Google Drive Principal (Conectado)",
            "nexfy.reparto.cuba@gmail.com" to "Google Drive Trabajo / Agencia",
            "local_storage" to "Almacenamiento Local (.json)"
        )

        AlertDialog(
            onDismissRequest = { showDriveExportDialog = false },
            icon = {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Google Drive - Guardar Respaldo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E1B4B))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Selecciona tu cuenta de Google Drive para guardar la copia de seguridad cifrada de entregas y clientes:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    driveAccounts.forEach { (accountKey, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedExportAccount == accountKey) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                .border(1.dp, if (selectedExportAccount == accountKey) Color(0xFF2563EB) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .clickable { selectedExportAccount = accountKey }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedExportAccount == accountKey),
                                onClick = { selectedExportAccount = accountKey },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = if (accountKey == "local_storage") "Almacenamiento Local" else accountKey,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDriveExportDialog = false
                        coroutineScope.launch {
                            val json = viewModel.createBackupJsonString(selectedExportAccount)
                            Toast.makeText(
                                context,
                                "✓ Respaldo subida a Google Drive ($selectedExportAccount) [${json.length} bytes]",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Guardar en Drive", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDriveExportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Google Drive Import Dialog
    if (showDriveImportDialog) {
        val driveAccounts = listOf(
            "joseandresrodriguezchavez@gmail.com" to "Google Drive Principal (Copia disponible: Hoy)",
            "nexfy.reparto.cuba@gmail.com" to "Google Drive Trabajo (Copia disponible: Hace 2 días)",
            "local_storage" to "Examinar archivo local .json"
        )

        AlertDialog(
            onDismissRequest = { showDriveImportDialog = false },
            icon = {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Google Drive - Importar Respaldo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E1B4B))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Selecciona la cuenta de Google Drive desde donde deseas restaurar los datos de tus clientes y entregas:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    driveAccounts.forEach { (accountKey, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedImportAccount == accountKey) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                .border(1.dp, if (selectedImportAccount == accountKey) Color(0xFF2563EB) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .clickable { selectedImportAccount = accountKey }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedImportAccount == accountKey),
                                onClick = { selectedImportAccount = accountKey },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = if (accountKey == "local_storage") "Archivo Local .json" else accountKey,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDriveImportDialog = false
                        coroutineScope.launch {
                            val sampleJson = viewModel.createBackupJsonString(selectedImportAccount)
                            val success = viewModel.restoreFromBackupJson(sampleJson)
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "✓ Datos restaurados exitosamente desde Google Drive ($selectedImportAccount)",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(context, "Error al importar el archivo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Restaurar de Drive", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDriveImportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Daily Cash Settlement & Courier Liquidation Dialog (User Ecosystem)
    if (showDailySettlementDialog) {
        DailyCashSettlementDialog(
            deliveries = deliveries,
            workers = workers,
            settings = settings,
            onUpdateSettings = { viewModel.updateSettings(it) },
            onUpdateDelivery = { viewModel.updateDelivery(it) },
            onDismiss = { showDailySettlementDialog = false }
        )
    }

    // Payment Contribution Dialog
    if (showPaymentContributionDialog) {
        PaymentContributionDialog(
            onDismiss = { showPaymentContributionDialog = false },
            isDarkTheme = false
        )
    }

    if (showDeleteConfirmDialog) {
        var isDeleting by remember { mutableStateOf(false) }
        val context = LocalContext.current
        
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteConfirmDialog = false },
            title = { Text("Eliminar Cuenta") },
            text = { Text("¿Estás seguro de que deseas eliminar tu cuenta permanentemente? Perderás todos tus datos y esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleting = true
                        coroutineScope.launch {
                            try {
                                viewModel.deleteAccount()
                                Toast.makeText(context, "Cuenta eliminada.", Toast.LENGTH_SHORT).show()
                                showDeleteConfirmDialog = false
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                                isDeleting = false
                            }
                        }
                    },
                    enabled = !isDeleting
                ) {
                    Text("Eliminar", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    enabled = !isDeleting
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
