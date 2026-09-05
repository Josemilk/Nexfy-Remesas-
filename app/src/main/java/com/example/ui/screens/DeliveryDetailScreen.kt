package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Delivery
import com.example.data.model.DeliveryStatus
import com.example.ui.NexFyViewModel
import com.example.ui.Screen
import com.example.ui.components.CustomerNotificationDialog
import com.example.ui.components.DigitalReceiptDialog
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VerifiedUser

@Composable
fun DeliveryDetailScreen(
    deliveryId: Long,
    viewModel: NexFyViewModel,
    onBack: () -> Unit,
    onOpenMap: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val deliveries by viewModel.deliveries.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val delivery = deliveries.find { it.id == deliveryId } ?: deliveries.firstOrNull()

    if (delivery == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Entrega no encontrada")
        }
        return
    }

    var currentNote by remember(delivery.id) { mutableStateOf(delivery.note) }
    val isPending = delivery.status == DeliveryStatus.PENDING
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Photo picker launcher (Gallery)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.attachPhotoToDelivery(delivery, uri.toString())
            Toast.makeText(context, "Foto de confirmación adjuntada correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera photo launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val file = File(context.cacheDir, "proof_${delivery.id}_${System.currentTimeMillis()}.jpg")
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                val uri = Uri.fromFile(file).toString()
                viewModel.attachPhotoToDelivery(delivery, uri)
                Toast.makeText(context, "Foto de confirmación capturada ✓", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al guardar foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al abrir cámara: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_LONG).show()
        }
    }

    val launchCamera = {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al abrir cámara", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val openWhatsApp = {
        val digitsOnly = delivery.phone.replace(Regex("[^0-9]"), "")
        if (digitsOnly.isEmpty()) {
            Toast.makeText(context, "Número de teléfono inválido para WhatsApp", Toast.LENGTH_SHORT).show()
        } else {
            val formattedPhone = if (digitsOnly.length == 8 && digitsOnly.startsWith("5")) "53$digitsOnly" else digitsOnly
            val encodedMsg = Uri.encode(settings.whatsappMessage)
            val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMsg"
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=$formattedPhone&text=$encodedMsg"))
                    context.startActivity(waIntent)
                } catch (e2: Exception) {
                    Toast.makeText(context, "No se pudo abrir WhatsApp. Verifica que la aplicación esté instalada.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("delivery_detail_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Detalle de entrega",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E7FF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "3/4",
                        color = Color(0xFF4338CA),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Datos del Cliente Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        Text(
                            text = "Datos del cliente",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.5.dp,
                                    color = if (isPending) Color(0xFFF97316) else Color(0xFF10B981),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .background(
                                    if (isPending) Color(0xFFFFF7ED) else Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPending) "• Pendiente" else "• Entregada",
                                color = if (isPending) Color(0xFFEA580C) else Color(0xFF059669),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Nombre
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Nombre", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(text = delivery.clientName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Teléfono
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Teléfono", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(text = delivery.phone, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Monto USD / CUP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Monto USD", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    text = if (settings.hideAmounts) "$ **** USD" else "$${String.format("%.2f", delivery.amountUsd)} USD",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Monto CUP", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    text = if (settings.hideAmounts) "$ **** CUP" else "$${String.format("%,.2f", delivery.amountCup)} CUP",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Dirección
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Dirección", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                text = delivery.address,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Quick Actions (Llamar, Notificar, WhatsApp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Llamar
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${delivery.phone}"))
                            context.startActivity(intent)
                        }
                        .testTag("action_call"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC7D2FE))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF1E1B4B), modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Llamar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    }
                }

                // Notificar por SMS / WhatsApp
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showNotificationDialog = true }
                        .testTag("action_notify_customer"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC7D2FE))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null, tint = Color(0xFF1E1B4B), modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Notificar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    }
                }

                // WhatsApp
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { openWhatsApp() }
                        .testTag("action_whatsapp"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC7D2FE))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "WhatsApp", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    }
                }
            }

            // Comprobante Oficial, Firma Digital y Código QR Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showReceiptDialog = true }
                    .testTag("btn_open_digital_receipt"),
                color = Color(0xFFEFF6FF),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF3B82F6))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Comprobante Oficial Certificado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                            }
                            Text("Firma digital en pantalla + QR de verificación", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                    Icon(Icons.Default.QrCode2, contentDescription = "Ver QR", tint = Color(0xFF2563EB), modifier = Modifier.size(26.dp))
                }
            }

            // Main Action Buttons
            Button(
                onClick = {
                    val nextStatus = if (isPending) DeliveryStatus.DELIVERED else DeliveryStatus.PENDING
                    viewModel.updateDeliveryStatus(delivery, nextStatus)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("mark_delivered_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPending) Color(0xFF3730A3) else Color(0xFF059669),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isPending) Icons.Default.Check else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isPending) "Marcar entregada" else "Entregada ✓ (Tocar para reabrir)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Attached Photo Proof Section
            if (!delivery.photoUri.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Foto de Confirmación ✓",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.attachPhotoToDelivery(delivery, "") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar foto", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            }
                        }

                        // Photo Display Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (delivery.photoUri!!.startsWith("content://") || delivery.photoUri!!.startsWith("file://") || delivery.photoUri!!.startsWith("http")) {
                                AsyncImage(
                                    model = delivery.photoUri,
                                    contentDescription = "Comprobante de entrega",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Default stylized proof preview badge
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Comprobante Guardado (${delivery.clientName})",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Firma digital y foto registrada en la app",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { launchCamera() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tomar con Cámara", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Galería", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }
                    }
                }
            } else {
                // Subir foto buttons when no photo is attached
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { launchCamera() },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp)
                            .testTag("upload_photo_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tomar Foto",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                photoPickerLauncher.launch("image/*")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF3730A3), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Galería",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3730A3)
                        )
                    }
                }
            }

            // Abrir en mapa button
            OutlinedButton(
                onClick = { onOpenMap(delivery.latitude ?: 23.1136, delivery.longitude ?: -82.3668) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("open_map_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF3730A3), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Abrir en mapa",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3730A3)
                )
            }

            // Notas Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF3730A3), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Notas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    OutlinedTextField(
                        value = currentNote,
                        onValueChange = {
                            currentNote = it
                            viewModel.updateDeliveryNote(delivery, it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = Color(0xFF3730A3),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            // Warning footer text
            Text(
                text = "Asegúrate de obtener evidencia de la entrega",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Digital Receipt Dialog (Signature + QR Code)
        if (showReceiptDialog) {
            DigitalReceiptDialog(
                delivery = delivery,
                onDismiss = { showReceiptDialog = false },
                onConfirmSignature = { signCode ->
                    // Optionally update note or confirmation
                }
            )
        }

        // Customer Notification Sheet (WhatsApp + SMS)
        if (showNotificationDialog) {
            CustomerNotificationDialog(
                delivery = delivery,
                onDismiss = { showNotificationDialog = false }
            )
        }
    }
}
