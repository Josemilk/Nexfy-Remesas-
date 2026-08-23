package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrashItem
import com.example.data.model.TrashType
import com.example.ui.NexFyViewModel

@Composable
fun RecycleBinScreen(
    viewModel: NexFyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val trashItems by viewModel.trashItems.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterType by remember { mutableStateOf<TrashType?>(null) }

    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedItemIds = remember { mutableStateListOf<Long>() }

    var showClearAllDialog by remember { mutableStateOf(false) }
    var itemToRestore by remember { mutableStateOf<TrashItem?>(null) }
    var itemToDeletePermanently by remember { mutableStateOf<TrashItem?>(null) }

    val filteredItems = trashItems.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true) ||
                item.clientName.contains(searchQuery, ignoreCase = true)

        val matchesFilter = selectedFilterType == null || item.itemType == selectedFilterType

        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Papelera",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Papelera de reciclaje",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${trashItems.size} elementos archivados",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            if (trashItems.isNotEmpty()) {
                IconButton(
                    onClick = { showClearAllDialog = true },
                    modifier = Modifier.testTag("empty_trash_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Vaciar papelera",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Retention Notice Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Los elementos permanecen hasta 30 días en la papelera antes de eliminarse automáticamente. Puedes reintegrarlos al historial o borrarlos de forma permanente.",
                    fontSize = 12.sp,
                    color = Color(0xFF1E40AF),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar en la papelera...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips and Selection mode action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilterType == null,
                    onClick = { selectedFilterType = null },
                    label = { Text("Todos (${trashItems.size})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedFilterType == TrashType.CLIENT,
                    onClick = { selectedFilterType = TrashType.CLIENT },
                    label = { Text("Clientes", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedFilterType == TrashType.DELIVERY,
                    onClick = { selectedFilterType = TrashType.DELIVERY },
                    label = { Text("Entregas", fontSize = 12.sp) }
                )
            }

            if (trashItems.isNotEmpty()) {
                TextButton(onClick = {
                    isSelectionMode = !isSelectionMode
                    if (!isSelectionMode) selectedItemIds.clear()
                }) {
                    Text(
                        text = if (isSelectionMode) "Cancelar" else "Seleccionar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Selected items batch action bar
        if (isSelectionMode && selectedItemIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedItemIds.size} seleccionados",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.restoreTrashItems(selectedItemIds)
                                Toast.makeText(context, "${selectedItemIds.size} elementos reintegrados al historial ✓", Toast.LENGTH_SHORT).show()
                                selectedItemIds.clear()
                                isSelectionMode = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reintegrar", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.permanentlyDeleteTrashItems(selectedItemIds)
                                Toast.makeText(context, "${selectedItemIds.size} elementos eliminados permanentemente", Toast.LENGTH_SHORT).show()
                                selectedItemIds.clear()
                                isSelectionMode = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Trash Items List
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No hay coincidencias en la papelera" else "La papelera de reciclaje está vacía",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Las fichas o entregas que elimines aparecerán aquí por 30 días",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val isSelected = selectedItemIds.contains(item.id)
                    val daysLeft = item.daysRemaining()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelectionMode) {
                                    if (isSelected) selectedItemIds.remove(item.id)
                                    else selectedItemIds.add(item.id)
                                }
                            }
                            .testTag("trash_item_${item.id}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelectionMode) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedItemIds.add(item.id)
                                                else selectedItemIds.remove(item.id)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (item.itemType == TrashType.CLIENT) Color(0xFFEEF2FF) else Color(0xFFECFDF5)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (item.itemType == TrashType.CLIENT) Icons.Default.Person else Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = if (item.itemType == TrashType.CLIENT) Color(0xFF4338CA) else Color(0xFF059669),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = item.subtitle,
                                            fontSize = 13.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                // Days remaining badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (daysLeft <= 5) Color(0xFFFEE2E2) else Color(0xFFF1F5F9)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$daysLeft días restantes",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (daysLeft <= 5) Color(0xFFDC2626) else Color(0xFF475569)
                                    )
                                }
                            }

                            if (!isSelectionMode) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { itemToRestore = item },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Restore, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reintegrar al cliente", color = Color(0xFF059669), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    TextButton(
                                        onClick = { itemToDeletePermanently = item },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Eliminar permanente", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialogs
    if (itemToRestore != null) {
        val item = itemToRestore!!
        AlertDialog(
            onDismissRequest = { itemToRestore = null },
            title = { Text("Reintegrar a Historial") },
            text = {
                Text("¿Deseas reintegrar '${item.title}' (${item.itemType.displayName}) nuevamente a los registros activos?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreTrashItem(item)
                        Toast.makeText(context, "'${item.title}' reintegrado con éxito ✓", Toast.LENGTH_SHORT).show()
                        itemToRestore = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("Reintegrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRestore = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (itemToDeletePermanently != null) {
        val item = itemToDeletePermanently!!
        AlertDialog(
            onDismissRequest = { itemToDeletePermanently = null },
            title = { Text("Eliminar Permanentemente") },
            text = {
                Text("¿Estás seguro de que deseas eliminar permanentemente '${item.title}' de la papelera? Esta acción NO se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.permanentlyDeleteTrashItem(item.id)
                        Toast.makeText(context, "Elemento eliminado de forma permanente", Toast.LENGTH_SHORT).show()
                        itemToDeletePermanently = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Eliminar definitivamente")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDeletePermanently = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Vaciar Papelera de Reciclaje") },
            text = {
                Text("¿Deseas eliminar PERMANENTEMENTE todos los ${trashItems.size} elementos de la papelera? Esta operación no se podrá revertir.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllTrash()
                        Toast.makeText(context, "Papelera vaciada por completo", Toast.LENGTH_SHORT).show()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Vaciar todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
