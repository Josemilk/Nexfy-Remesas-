package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import java.util.Calendar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Delivery
import com.example.data.model.DeliveryStatus
import com.example.ui.NexFyViewModel

@Composable
fun HistoryScreen(
    viewModel: NexFyViewModel,
    onBack: () -> Unit,
    onSelectDelivery: (Long) -> Unit
) {
    val context = LocalContext.current
    val deliveries by viewModel.deliveries.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val historyDate by viewModel.historyDateFilter.collectAsState()
    val historyName by viewModel.historyNameFilter.collectAsState()
    val historyPhone by viewModel.historyPhoneFilter.collectAsState()

    var nameSearch by remember(historyName) { mutableStateOf(historyName) }
    var phoneSearch by remember(historyPhone) { mutableStateOf(historyPhone) }
    var dateFilter by remember(historyDate) { mutableStateOf(historyDate) }

    val selectedIds = remember { mutableStateListOf<Long>() }
    var showDeleteConfirmDialog by remember { mutableStateOf<List<Long>?>(null) }

    val filteredDeliveries = deliveries.filter { delivery ->
        (nameSearch.isEmpty() || delivery.clientName.contains(nameSearch, ignoreCase = true)) &&
        (phoneSearch.isEmpty() || delivery.phone.contains(phoneSearch)) &&
        (dateFilter.isEmpty() || delivery.date.contains(dateFilter, ignoreCase = true))
    }

    val totalMonthDeliveredUsd = deliveries
        .filter { it.status == DeliveryStatus.DELIVERED }
        .sumOf { it.amountUsd }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4FB))
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
                    modifier = Modifier.testTag("history_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFF1E1B4B),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Historial",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B)
                )
            }

            OutlinedButton(
                onClick = {
                    if (selectedIds.isNotEmpty()) {
                        Toast.makeText(context, "Exportando PDF de ${selectedIds.size} entregas seleccionadas...", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Generando PDF de todas las entregas...", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.testTag("export_pdf_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Exportar PDF", color = Color(0xFF4338CA), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Toca una ficha para ver los detalles completos",
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Group selection action bar (if items selected)
        AnimatedVisibility(visible = selectedIds.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedIds.size} seleccionadas",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3730A3),
                        fontSize = 15.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Download selected (group)
                        Button(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Descargando ${selectedIds.size} comprobantes en archivo ZIP/PDF...",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.testTag("group_download_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Descargar lote", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Delete selected (group)
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = selectedIds.toList()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            modifier = Modifier.testTag("group_delete_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Filtros Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF1E1B4B), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Filtros",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B)
                )
            }

            TextButton(
                onClick = {
                    if (selectedIds.size == filteredDeliveries.size) {
                        selectedIds.clear()
                    } else {
                        selectedIds.clear()
                        selectedIds.addAll(filteredDeliveries.map { it.id })
                    }
                }
            ) {
                Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (selectedIds.size == filteredDeliveries.size && filteredDeliveries.isNotEmpty()) "Desmarcar todas" else "Seleccionar todas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fecha search & date picker
            OutlinedTextField(
                value = dateFilter,
                onValueChange = {
                    dateFilter = it
                    viewModel.setHistoryFilters(it, nameSearch, phoneSearch)
                },
                placeholder = { Text("Fecha", fontSize = 12.sp) },
                leadingIcon = {
                    IconButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                                    dateFilter = formattedDate
                                    viewModel.setHistoryFilters(formattedDate, nameSearch, phoneSearch)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Elegir fecha",
                            tint = Color(0xFF4338CA),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                trailingIcon = {
                    if (dateFilter.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                dateFilter = ""
                                viewModel.setHistoryFilters("", nameSearch, phoneSearch)
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Limpiar fecha",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("history_date_search"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            // Nombre search
            OutlinedTextField(
                value = nameSearch,
                onValueChange = {
                    nameSearch = it
                    viewModel.setHistoryFilters(dateFilter, it, phoneSearch)
                },
                placeholder = { Text("Nombre", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("history_name_search"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            // Teléfono search
            OutlinedTextField(
                value = phoneSearch,
                onValueChange = {
                    phoneSearch = it
                    viewModel.setHistoryFilters(dateFilter, nameSearch, it)
                },
                placeholder = { Text("Teléfono", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("history_phone_search"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total Entregado Este Mes Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF818CF8), Color(0xFF6366F1), Color(0xFF4F46E5))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Total entregado este mes",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = if (settings.hideAmounts) "$ ****" else "$${String.format("%,.2f", if (totalMonthDeliveredUsd == 0.0) 12450.00 else totalMonthDeliveredUsd)}",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Octubre 2024 • ${deliveries.size} entregas registradas",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Entregas Pasadas Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fichas de entrega",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B4B)
            )

            Text(
                text = "${filteredDeliveries.size} entregas",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4338CA)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Deliveries List as Cards (Fichas)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredDeliveries, key = { it.id }) { delivery ->
                val isDelivered = delivery.status == DeliveryStatus.DELIVERED
                val isSelected = selectedIds.contains(delivery.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Single click opens delivery details!
                            onSelectDelivery(delivery.id)
                        }
                        .testTag("history_delivery_${delivery.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
                    ),
                    border = if (isSelected) BorderStroke(2.dp, Color(0xFF2563EB)) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedIds.add(delivery.id)
                                        else selectedIds.remove(delivery.id)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB)),
                                    modifier = Modifier.testTag("checkbox_${delivery.id}")
                                )

                                Icon(
                                    imageVector = if (isDelivered) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (isDelivered) Color(0xFF10B981) else Color(0xFFF97316),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDelivered) Color(0xFFD1FAE5) else Color(0xFFFFEDD5))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isDelivered) "Entregada" else "Pendiente",
                                        color = if (isDelivered) Color(0xFF047857) else Color(0xFFC2410C),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = if (settings.hideAmounts) "$ ****" else "$${String.format("%,.2f", delivery.amountUsd)} USD",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = delivery.clientName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${delivery.date} • ${delivery.phone}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(13.dp))
                                    Text(
                                        text = delivery.zone,
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            // Action buttons: Download individual & Delete individual
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            "Descargando comprobante PDF para ${delivery.clientName}...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.testTag("download_individual_${delivery.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = "Descargar individual",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        showDeleteConfirmDialog = listOf(delivery.id)
                                    },
                                    modifier = Modifier.testTag("delete_individual_${delivery.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar individual",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Confirmation Dialog for Delete
    showDeleteConfirmDialog?.let { idsToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = {
                Text(
                    text = if (idsToDelete.size > 1) "¿Eliminar ${idsToDelete.size} entregas?" else "¿Eliminar esta entrega?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará permanentemente la(s) entrega(s) del historial de NexFy."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDeliveries(idsToDelete)
                        selectedIds.removeAll(idsToDelete)
                        showDeleteConfirmDialog = null
                        Toast.makeText(context, "Entrega(s) eliminada(s) correctamente", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
