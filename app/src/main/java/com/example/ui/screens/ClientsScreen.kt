package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Client
import com.example.ui.NexFyViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClientsScreen(
    viewModel: NexFyViewModel,
    onBack: () -> Unit,
    onNewDeliveryForClient: (String, String) -> Unit,
    onClientSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val clients by viewModel.clients.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var search by remember { mutableStateOf("") }

    val selectedIds = remember { mutableStateListOf<Long>() }
    var showAddClientDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<List<Long>?>(null) }

    val filteredClients = clients.filter {
        it.name.contains(search, ignoreCase = true) ||
        it.phone.contains(search) ||
        it.zone.contains(search, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
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
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Directorio de clientes",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Action: Add new client button
                Button(
                    onClick = { showAddClientDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_client_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Nuevo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selection / Batch deletion action bar
        if (selectedIds.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedIds.size == filteredClients.size && filteredClients.isNotEmpty(),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    selectedIds.clear()
                                    selectedIds.addAll(filteredClients.map { it.id })
                                } else {
                                    selectedIds.clear()
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFDC2626))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${selectedIds.size} seleccionados",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { selectedIds.clear() }) {
                            Text("Cancelar", color = Color(0xFF64748B), fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showDeleteConfirmDialog = selectedIds.toList() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar en grupo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Search bar
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Buscar cliente por nombre, teléfono o zona...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("clients_search_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Clients List
        if (filteredClients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No se encontraron clientes", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showAddClientDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Crear nuevo cliente")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredClients) { client ->
                    val isSelected = selectedIds.contains(client.id)
                    var showCardMenu by remember { mutableStateOf(false) }

                    val initials = client.name
                        .split(" ")
                        .mapNotNull { it.firstOrNull() }
                        .take(2)
                        .joinToString("")
                        .uppercase()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (selectedIds.isNotEmpty()) {
                                        if (isSelected) selectedIds.remove(client.id) else selectedIds.add(client.id)
                                    } else {
                                        onClientSelected(client.id)
                                    }
                                },
                                onLongClick = {
                                    if (isSelected) selectedIds.remove(client.id) else selectedIds.add(client.id)
                                }
                            )
                            .testTag("client_item_${client.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF2563EB)) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (selectedIds.isNotEmpty()) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedIds.add(client.id) else selectedIds.remove(client.id)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4F46E5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = client.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${client.phone} • ${client.zone}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onNewDeliveryForClient(client.name, client.phone) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Nueva entrega",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Box {
                                    IconButton(
                                        onClick = { showCardMenu = true },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Opciones del cliente",
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showCardMenu,
                                        onDismissRequest = { showCardMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Ver ficha completa") },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF4338CA)) },
                                            onClick = {
                                                showCardMenu = false
                                                onClientSelected(client.id)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Nueva entrega para este cliente") },
                                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2563EB)) },
                                            onClick = {
                                                showCardMenu = false
                                                onNewDeliveryForClient(client.name, client.phone)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Copiar datos del cliente") },
                                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                            onClick = {
                                                showCardMenu = false
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Cliente", "Nombre: ${client.name}\nTeléfono: ${client.phone}\nDirección: ${client.address}\nZona: ${client.zone}")
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Datos del cliente copiados ✓", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Enviar WhatsApp") },
                                            leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color(0xFF059669)) },
                                            onClick = {
                                                showCardMenu = false
                                                val cleanPhone = client.phone.replace(" ", "").replace("+", "")
                                                val msg = settings.whatsappMessage
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(msg)}"))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Llamar por teléfono") },
                                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2563EB)) },
                                            onClick = {
                                                showCardMenu = false
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                                                context.startActivity(intent)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar cliente", color = Color(0xFFDC2626)) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
                                            onClick = {
                                                showCardMenu = false
                                                showDeleteConfirmDialog = listOf(client.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Modal Dialog: Add New Client
    if (showAddClientDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("+53 5") }
        var newAddress by remember { mutableStateOf("") }
        var newIdentity by remember { mutableStateOf("") }
        var newZone by remember { mutableStateOf("Zona Centro") }

        AlertDialog(
            onDismissRequest = { showAddClientDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agregar nuevo cliente",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B)
                    )
                    IconButton(onClick = { showAddClientDialog = false }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF64748B))
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre completo *") },
                        placeholder = { Text("Ej. Carlos Ramos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Teléfono *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Dirección") },
                        placeholder = { Text("Calle, número y reparto") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = newIdentity,
                        onValueChange = { newIdentity = it },
                        label = { Text("Carnet de Identidad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = newZone,
                        onValueChange = { newZone = it },
                        label = { Text("Zona") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isBlank()) {
                            Toast.makeText(context, "El nombre del cliente es obligatorio", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addClient(
                                name = newName,
                                phone = newPhone,
                                address = newAddress,
                                identityNumber = newIdentity,
                                zone = newZone
                            )
                            showAddClientDialog = false
                            Toast.makeText(context, "Cliente agregada/o correctamente ✓", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar cliente")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddClientDialog = false }) {
                    Text("Cancelar", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Modal Dialog: Delete Confirmation (Individual or Batch)
    if (showDeleteConfirmDialog != null) {
        val count = showDeleteConfirmDialog!!.size
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = {
                Text(
                    text = if (count == 1) "Eliminar ficha de cliente" else "Eliminar $count clientes",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            },
            text = {
                Text(
                    text = if (count == 1)
                        "¿Estás seguro de que deseas eliminar la ficha de este cliente del directorio?"
                    else
                        "¿Estás seguro de que deseas eliminar los $count clientes seleccionados?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idsToDelete = showDeleteConfirmDialog!!
                        viewModel.deleteClients(idsToDelete)
                        selectedIds.removeAll(idsToDelete)
                        showDeleteConfirmDialog = null
                        Toast.makeText(
                            context,
                            if (count == 1) "Cliente eliminado ✓" else "$count clientes eliminados ✓",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancelar", color = Color(0xFF64748B))
                }
            }
        )
    }
}
