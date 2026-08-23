package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.Client
import com.example.data.model.Delivery
import com.example.ui.NexFyViewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun ClientDetailScreen(
    clientId: Long,
    viewModel: NexFyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clients by viewModel.clients.collectAsState()
    val client = clients.find { it.id == clientId }
    val settings by viewModel.settings.collectAsState()
    val deliveries by viewModel.deliveries.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    var selectedDeliveryForModal by remember { mutableStateOf<Delivery?>(null) }

    var editName by remember(client) { mutableStateOf(client?.name ?: "") }
    var editPhone by remember(client) { mutableStateOf(client?.phone ?: "") }
    var editZone by remember(client) { mutableStateOf(client?.zone ?: "") }
    var editAddress by remember(client) { mutableStateOf(client?.address ?: "") }
    var editIdentity by remember(client) { mutableStateOf(client?.identityNumber ?: "") }

    if (client == null) {
        onBack()
        return
    }

    // Helper functions for image compression and sharing
    val compressImageFile = { originalFile: File ->
        try {
            val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
            if (bitmap != null) {
                val compressedFile = File(context.cacheDir, "comp_${System.currentTimeMillis()}.jpg")
                FileOutputStream(compressedFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
                }
                compressedFile
            } else {
                originalFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
            originalFile
        }
    }

    val shareIndividualDeliveryDetail = { delivery: Delivery ->
        val text = StringBuilder()
            .append("📄 **DETALLE DE ENTREGA DE MUESTRA**\n")
            .append("━━━━━━━━━━━━━━━━━━━━━\n")
            .append("👤 Cliente: ${client.name}\n")
            .append("📞 Teléfono: ${client.phone}\n")
            .append("📍 Dirección: ${client.address.ifEmpty { "N/A" }}\n")
            .append("💵 Importe: $${String.format("%.2f", delivery.amountUsd)} USD (${String.format("%,.2f", delivery.amountCup)} CUP)\n")
            .append("📅 Fecha/Hora: ${delivery.date}\n")
            .append("📌 Estado: ${if (delivery.status == com.example.data.model.DeliveryStatus.DELIVERED) "Entregada" else "Pendiente"}\n")
            .append("📝 Nota: ${delivery.note.ifEmpty { "Sin observaciones" }}\n")
            .toString()

        val photoUriStr = delivery.photoUri
        if (!photoUriStr.isNullOrEmpty() && (photoUriStr.startsWith("file://") || photoUriStr.startsWith("content://"))) {
            try {
                val file = if (photoUriStr.startsWith("file://")) {
                    File(Uri.parse(photoUriStr).path ?: "")
                } else {
                    File(photoUriStr)
                }

                val imageToShare = if (file.exists()) compressImageFile(file) else null

                if (imageToShare != null && imageToShare.exists()) {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        imageToShare
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_TEXT, text)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Exportar detalle de entrega"))
                } else {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Exportar detalle de entrega"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Exportar detalle de entrega"))
            }
        } else {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Exportar detalle de entrega"))
        }
    }

    // Camera launcher for delivery proof photo capture
    var activeDeliveryForCamera by remember { mutableStateOf<Delivery?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        val delivery = activeDeliveryForCamera
        if (bitmap != null && delivery != null) {
            val file = File(context.cacheDir, "proof_${delivery.id}_${System.currentTimeMillis()}.jpg")
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                val uri = Uri.fromFile(file).toString()
                viewModel.attachPhotoToDelivery(delivery, uri)
                if (selectedDeliveryForModal?.id == delivery.id) {
                    selectedDeliveryForModal = delivery.copy(photoUri = uri)
                }
                Toast.makeText(context, "Foto de comprobante guardada y optimizada ✓", Toast.LENGTH_SHORT).show()
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

    val launchCameraForDelivery = { delivery: Delivery ->
        activeDeliveryForCamera = delivery
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
        val digitsOnly = client.phone.replace(Regex("[^0-9]"), "")
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
                    Toast.makeText(context, "No se pudo abrir WhatsApp.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val initials = client.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
    val clientDeliveries = deliveries.filter { 
        it.clientName.trim().equals(client.name.trim(), ignoreCase = true) || 
        it.phone.replace(Regex("[^0-9]"), "") == client.phone.replace(Regex("[^0-9]"), "")
    }

    val exportFullClientFile = {
        val sb = StringBuilder()
        sb.append("📋 **FICHA COMPLETA DEL CLIENTE**\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("👤 Nombre: ${client.name}\n")
        sb.append("📞 Teléfono: ${client.phone}\n")
        if (client.identityNumber.isNotEmpty()) sb.append("🪪 Carnet: ${client.identityNumber}\n")
        sb.append("📍 Zona: ${client.zone}\n")
        if (client.address.isNotEmpty()) sb.append("🏠 Dirección: ${client.address}\n")
        sb.append("📊 Total de entregas recibidas: ${clientDeliveries.size}\n")
        sb.append("\n📜 **HISTORIAL DETALLADO DE ENTREGAS:**\n")

        val imageUris = mutableListOf<Uri>()

        clientDeliveries.forEachIndexed { idx, delivery ->
            sb.append("\n#${idx + 1} - Fecha: ${delivery.date}\n")
            sb.append("   • Importe: $${String.format("%.2f", delivery.amountUsd)} USD (${String.format("%,.2f", delivery.amountCup)} CUP)\n")
            sb.append("   • Estado: ${if (delivery.status == com.example.data.model.DeliveryStatus.DELIVERED) "Entregada" else "Pendiente"}\n")
            if (delivery.note.isNotEmpty()) sb.append("   • Nota: ${delivery.note}\n")

            val photoUriStr = delivery.photoUri
            if (!photoUriStr.isNullOrEmpty() && (photoUriStr.startsWith("file://") || photoUriStr.startsWith("content://"))) {
                try {
                    val file = if (photoUriStr.startsWith("file://")) File(Uri.parse(photoUriStr).path ?: "") else File(photoUriStr)
                    if (file.exists()) {
                        val compFile = compressImageFile(file)
                        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", compFile)
                        imageUris.add(contentUri)
                        sb.append("   • Photo comprobante adjuntada ✓\n")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (imageUris.isNotEmpty()) {
            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/jpeg"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
                putExtra(Intent.EXTRA_TEXT, sb.toString())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Exportar Ficha Completa del Cliente"))
        } else {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, sb.toString())
            }
            context.startActivity(Intent.createChooser(shareIntent, "Exportar Ficha Completa del Cliente"))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Top App Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ficha de cliente",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isEditing) {
                        IconButton(onClick = {
                            viewModel.updateClient(client.copy(
                                name = editName,
                                phone = editPhone,
                                zone = editZone,
                                address = editAddress,
                                identityNumber = editIdentity
                            ))
                            isEditing = false
                            Toast.makeText(context, "Cliente actualizado ✓", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Guardar",
                                tint = Color(0xFF059669)
                            )
                        }
                    }

                    // Three Dots Options Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Exportar ficha completa (con fotos)") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF2563EB)) },
                                onClick = {
                                    showMenu = false
                                    exportFullClientFile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Copiar datos del cliente") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Datos Cliente", "Cliente: ${client.name}\nTeléfono: ${client.phone}\nCarnet: ${client.identityNumber}\nDirección: ${client.address}\nZona: ${client.zone}")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Datos copiados al portapapeles", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Enviar mensaje WhatsApp") },
                                leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color(0xFF059669)) },
                                onClick = {
                                    showMenu = false
                                    openWhatsApp()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Llamar al cliente") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2563EB)) },
                                onClick = {
                                    showMenu = false
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se pudo realizar la llamada", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Editar cliente") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    isEditing = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar cliente", color = Color(0xFFEF4444)) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Header profile card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4F46E5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editIdentity,
                            onValueChange = { editIdentity = it },
                            label = { Text("Carnet de identidad") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    } else {
                        Text(
                            text = client.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = client.phone,
                            fontSize = 16.sp,
                            color = Color(0xFF64748B)
                        )
                        if (client.identityNumber.isNotEmpty()) {
                            Text(
                                text = "🪪 CI: ${client.identityNumber}",
                                fontSize = 14.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2FF))
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Llamar", tint = Color(0xFF4338CA), modifier = Modifier.size(26.dp))
                            }

                            IconButton(
                                onClick = { openWhatsApp() },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD1FAE5))
                            ) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = "WhatsApp", tint = Color(0xFF059669), modifier = Modifier.size(26.dp))
                            }

                            Button(
                                onClick = { exportFullClientFile() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exportar Ficha", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Additional information card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información Adicional", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editZone,
                            onValueChange = { editZone = it },
                            label = { Text("Zona") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = { Text("Dirección") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Zona: ${client.zone}", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        }

                        if (client.address.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Transparent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Dirección: ${client.address}", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Deliveries list title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de entregas (${clientDeliveries.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "💡 Toca para desplegar",
                    fontSize = 12.sp,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (clientDeliveries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No hay entregas registradas para este cliente aún.", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(clientDeliveries) { delivery ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDeliveryForModal = delivery },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$${String.format("%.2f", delivery.amountUsd)} USD (${String.format("%,.2f", delivery.amountCup)} CUP)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Fecha: ${delivery.date}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { launchCameraForDelivery(delivery) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = "Cámara", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (!delivery.photoUri.isNullOrEmpty()) "Foto ✓" else "Foto",
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = { shareIndividualDeliveryDetail(delivery) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Exportar", tint = Color(0xFF475569), modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        if (!delivery.photoUri.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (delivery.photoUri.startsWith("content://") || delivery.photoUri.startsWith("file://")) {
                                    AsyncImage(
                                        model = delivery.photoUri,
                                        contentDescription = "Comprobante",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF10B981))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Comprobante de entrega adjuntado ✓", color = Color.White, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Sheet / Dialog for displaying individual delivery details
    if (selectedDeliveryForModal != null) {
        val delivery = selectedDeliveryForModal!!
        AlertDialog(
            onDismissRequest = { selectedDeliveryForModal = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Detalle de Entrega", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = { selectedDeliveryForModal = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Cliente: ${client.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E3A8A))
                            Text("Teléfono: ${client.phone}", fontSize = 13.sp, color = Color(0xFF3B82F6))
                            if (client.address.isNotEmpty()) {
                                Text("Dirección: ${client.address}", fontSize = 13.sp, color = Color(0xFF475569))
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Importe USD:", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text("$${String.format("%.2f", delivery.amountUsd)} USD", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Importe CUP:", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text("${String.format("%,.2f", delivery.amountCup)} CUP", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF059669))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fecha:", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text(delivery.date, fontSize = 13.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estado:", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text(if (delivery.status == com.example.data.model.DeliveryStatus.DELIVERED) "Entregada" else "Pendiente", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 13.sp)
                    }

                    if (delivery.note.isNotEmpty()) {
                        Text("Observaciones/Notas:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(delivery.note, fontSize = 13.sp, color = Color(0xFF334155))
                    }

                    if (!delivery.photoUri.isNullOrEmpty()) {
                        Text("Comprobante de entrega:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = delivery.photoUri,
                                contentDescription = "Comprobante",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Button(
                            onClick = { launchCameraForDelivery(delivery) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tomar foto de comprobante")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareIndividualDeliveryDetail(delivery)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exportar Detalle")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDeliveryForModal = null }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Cliente") },
            text = { Text("¿Estás seguro de que deseas eliminar este cliente? Se mantendrán las entregas asociadas a él, pero no aparecerá más en el directorio.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteClient(client.id)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
