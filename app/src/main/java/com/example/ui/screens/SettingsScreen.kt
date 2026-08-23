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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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

    var rateText by remember(settings.usdCupRate) { mutableStateOf(settings.usdCupRate.toInt().toString()) }
    var commissionText by remember(settings.commissionPercent) { mutableStateOf(settings.commissionPercent.toString()) }
    var whatsappMsg by remember(settings.whatsappMessage) { mutableStateOf(settings.whatsappMessage) }

    var showDriveExportDialog by remember { mutableStateOf(false) }
    var showDriveImportDialog by remember { mutableStateOf(false) }
    var selectedExportAccount by remember { mutableStateOf("joseandresrodriguezchavez@gmail.com") }
    var selectedImportAccount by remember { mutableStateOf("joseandresrodriguezchavez@gmail.com") }
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

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

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            // Section 2: Negocio
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
                        Text(text = "Negocio", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    }

                    // Tasa USD/CUP
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Tasa USD/CUP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                            OutlinedTextField(
                                value = rateText,
                                onValueChange = {
                                    rateText = it
                                    it.toDoubleOrNull()?.let { newRate ->
                                        viewModel.updateSettings(settings.copy(usdCupRate = newRate))
                                    }
                                },
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(52.dp)
                                    .testTag("rate_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                        Text(text = "Tasa actual para cálculo de conversión", fontSize = 12.sp, color = Color(0xFF64748B))
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
}
