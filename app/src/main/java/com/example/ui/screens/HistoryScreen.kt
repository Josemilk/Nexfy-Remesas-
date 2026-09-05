package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Delivery
import com.example.data.model.DeliveryStatus
import com.example.ui.NexFyViewModel

@OptIn(ExperimentalFoundationApi::class)
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
    val scope = rememberCoroutineScope()

    var showGroupActionsMenu by remember { mutableStateOf(false) }
    var hoveredOrLongPressedDeliveryId by remember { mutableStateOf<Long?>(null) }

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

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val deliveriesToExport = if (selectedIds.isNotEmpty()) {
                            filteredDeliveries.filter { selectedIds.contains(it.id) }
                        } else {
                            filteredDeliveries
                        }
                        Toast.makeText(context, "Generando PDF...", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            val shareIntent = com.example.utils.PdfExporter.exportClientDeliveriesToPdf(
                                context,
                                null,
                                deliveriesToExport,
                                "Historial de Entregas"
                            )
                            if (shareIntent != null) {
                                context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF de Historial"))
                            } else {
                                Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("export_pdf_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "PDF", color = Color(0xFF4338CA), fontWeight = FontWeight.Bold)
                }

                // Close X Button on top right margin
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar e ir al inicio",
                        tint = Color(0xFF1E1B4B),
                        modifier = Modifier.size(20.dp)
                    )
                }
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
                                val deliveriesToExport = filteredDeliveries.filter { selectedIds.contains(it.id) }
                                Toast.makeText(context, "Generando PDF del lote...", Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    val shareIntent = com.example.utils.PdfExporter.exportClientDeliveriesToPdf(
                                        context,
                                        null,
                                        deliveriesToExport,
                                        "Lote de Entregas Seleccionadas"
                                    )
                                    if (shareIntent != null) {
                                        context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF de Lote"))
                                    } else {
                                        Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                                    }
                                }
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

        // Filtros & Acciones de Búsqueda Organizados (Diseño Estético Ecosistema)
        val activeFiltersCount = (if (dateFilter.isNotEmpty()) 1 else 0) +
                (if (nameSearch.isNotEmpty()) 1 else 0) +
                (if (phoneSearch.isNotEmpty()) 1 else 0)
        val filteredTotalUsd = remember(filteredDeliveries) { filteredDeliveries.sumOf { it.amountUsd } }
        val filteredTotalCup = remember(filteredDeliveries) { filteredDeliveries.sumOf { it.amountCup } }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header del Filtro
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF4338CA),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Filtros de Búsqueda",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                            Text(
                                text = "Búsqueda por fecha, nombre o teléfono",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (activeFiltersCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC))
                            ) {
                                Text(
                                    text = "$activeFiltersCount activo${if (activeFiltersCount > 1) "s" else ""}",
                                    color = Color(0xFF15803D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Menú de opciones de lote / grupo
                        Box {
                            IconButton(
                                onClick = { showGroupActionsMenu = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_history_group_options")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opciones de lote y grupo",
                                    tint = Color(0xFF64748B)
                                )
                            }

                            DropdownMenu(
                                expanded = showGroupActionsMenu,
                                onDismissRequest = { showGroupActionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.SelectAll, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (selectedIds.size == filteredDeliveries.size && filteredDeliveries.isNotEmpty()) "Desmarcar todas" else "Seleccionar todas (${filteredDeliveries.size})",
                                                color = Color(0xFF2563EB),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    },
                                    onClick = {
                                        showGroupActionsMenu = false
                                        if (selectedIds.size == filteredDeliveries.size && filteredDeliveries.isNotEmpty()) {
                                            selectedIds.clear()
                                        } else {
                                            selectedIds.clear()
                                            selectedIds.addAll(filteredDeliveries.map { it.id })
                                        }
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (selectedIds.isNotEmpty()) "Eliminar seleccionadas (${selectedIds.size})" else "Eliminar seleccionadas",
                                                color = if (selectedIds.isNotEmpty()) Color(0xFFDC2626) else Color(0xFF94A3B8),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    onClick = {
                                        showGroupActionsMenu = false
                                        if (selectedIds.isNotEmpty()) {
                                            showDeleteConfirmDialog = selectedIds.toList()
                                        } else {
                                            Toast.makeText(context, "Primero selecciona entregas marcando sus casillas", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = selectedIds.isNotEmpty()
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val completedCount = deliveries.count { it.status == DeliveryStatus.DELIVERED }
                                            Text("Eliminar entregadas ($completedCount)", color = Color(0xFF1E1B4B))
                                        }
                                    },
                                    onClick = {
                                        showGroupActionsMenu = false
                                        val completedIds = deliveries.filter { it.status == DeliveryStatus.DELIVERED }.map { it.id }
                                        if (completedIds.isNotEmpty()) {
                                            showDeleteConfirmDialog = completedIds
                                        } else {
                                            Toast.makeText(context, "No hay entregas con estado 'Entregada'", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )

                                if (dateFilter.isNotEmpty() || nameSearch.isNotEmpty() || phoneSearch.isNotEmpty()) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Eliminar grupo filtrado (${filteredDeliveries.size})", color = Color(0xFF2563EB))
                                            }
                                        },
                                        onClick = {
                                            showGroupActionsMenu = false
                                            if (filteredDeliveries.isNotEmpty()) {
                                                showDeleteConfirmDialog = filteredDeliveries.map { it.id }
                                            } else {
                                                Toast.makeText(context, "No hay entregas que coincidan con los filtros actuales", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Vaciar todo el historial (${deliveries.size})", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        showGroupActionsMenu = false
                                        if (deliveries.isNotEmpty()) {
                                            showDeleteConfirmDialog = deliveries.map { it.id }
                                        } else {
                                            Toast.makeText(context, "El historial ya está vacío", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 1. Campo Fecha (Estilo Outlined con selector y accesos rápidos)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = dateFilter,
                        onValueChange = {
                            dateFilter = it
                            viewModel.setHistoryFilters(it, nameSearch, phoneSearch)
                        },
                        label = { Text("Fecha de Entrega", fontSize = 12.sp, color = Color(0xFF64748B)) },
                        placeholder = { Text("Ej: 25/08/2026", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
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
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Elegir fecha",
                                    tint = Color(0xFF4338CA),
                                    modifier = Modifier.size(20.dp)
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
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar fecha",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_date_search"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E1B4B),
                            unfocusedTextColor = Color(0xFF1E1B4B),
                            focusedBorderColor = Color(0xFF4338CA),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )

                    // Chips de fecha rápida
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val calendar = Calendar.getInstance()
                        val todayStr = String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        val yesterdayStr = String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (dateFilter == todayStr) Color(0xFF4338CA).copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (dateFilter == todayStr) Color(0xFF4338CA) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    dateFilter = todayStr
                                    viewModel.setHistoryFilters(todayStr, nameSearch, phoneSearch)
                                }
                        ) {
                            Text(
                                text = "📅 Hoy",
                                color = if (dateFilter == todayStr) Color(0xFF4338CA) else Color(0xFF475569),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (dateFilter == yesterdayStr) Color(0xFF4338CA).copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (dateFilter == yesterdayStr) Color(0xFF4338CA) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    dateFilter = yesterdayStr
                                    viewModel.setHistoryFilters(yesterdayStr, nameSearch, phoneSearch)
                                }
                        ) {
                            Text(
                                text = "📅 Ayer",
                                color = if (dateFilter == yesterdayStr) Color(0xFF4338CA) else Color(0xFF475569),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (dateFilter.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEE2E2),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        dateFilter = ""
                                        viewModel.setHistoryFilters("", nameSearch, phoneSearch)
                                    }
                            ) {
                                Text(
                                    text = "✕ Todas las fechas",
                                    color = Color(0xFFDC2626),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Campo Nombre de Cliente
                OutlinedTextField(
                    value = nameSearch,
                    onValueChange = {
                        nameSearch = it
                        viewModel.setHistoryFilters(dateFilter, it, phoneSearch)
                    },
                    label = { Text("Nombre del Cliente", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    placeholder = { Text("Buscar cliente por nombre...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (nameSearch.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    nameSearch = ""
                                    viewModel.setHistoryFilters(dateFilter, "", phoneSearch)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar nombre",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_name_search"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1B4B),
                        unfocusedTextColor = Color(0xFF1E1B4B),
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )
                )

                // 3. Campo Teléfono
                OutlinedTextField(
                    value = phoneSearch,
                    onValueChange = {
                        phoneSearch = it
                        viewModel.setHistoryFilters(dateFilter, nameSearch, it)
                    },
                    label = { Text("Número de Teléfono", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    placeholder = { Text("Ej: 5351234567", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (phoneSearch.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    phoneSearch = ""
                                    viewModel.setHistoryFilters(dateFilter, nameSearch, "")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar teléfono",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_phone_search"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1B4B),
                        unfocusedTextColor = Color(0xFF1E1B4B),
                        focusedBorderColor = Color(0xFF059669),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )
                )

                // Barra de Resumen y Limpieza Rápida
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${filteredDeliveries.size} coincidencia(s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Total: $${String.format(java.util.Locale.US, "%,.2f", filteredTotalUsd)} USD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF059669)
                                )
                            }
                            if (settings.usdCupRate > 0) {
                                Text(
                                    text = "Equiv: ${String.format(java.util.Locale.US, "%,.0f", filteredTotalCup)} CUP",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        if (activeFiltersCount > 0) {
                            TextButton(
                                onClick = {
                                    dateFilter = ""
                                    nameSearch = ""
                                    phoneSearch = ""
                                    viewModel.setHistoryFilters("", "", "")
                                },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Limpiar filtros",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredDeliveries, key = { it.id }) { delivery ->
                val isDelivered = delivery.status == DeliveryStatus.DELIVERED
                val isSelected = selectedIds.contains(delivery.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                onSelectDelivery(delivery.id)
                            },
                            onLongClick = {
                                hoveredOrLongPressedDeliveryId = delivery.id
                                showDeleteConfirmDialog = listOf(delivery.id)
                            }
                        )
                        .testTag("history_delivery_${delivery.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
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
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIds.add(delivery.id)
                                    else selectedIds.remove(delivery.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB)),
                                modifier = Modifier.size(22.dp).testTag("checkbox_${delivery.id}")
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDelivered) Color(0xFFD1FAE5) else Color(0xFFFFEDD5))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isDelivered) "Entregada" else "Pendiente",
                                    color = if (isDelivered) Color(0xFF047857) else Color(0xFFC2410C),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = delivery.clientName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B),
                                    maxLines = 1
                                )
                                Text(
                                    text = "${delivery.date}${if (delivery.assignedWorkerName.isNotBlank()) " • " + delivery.assignedWorkerName else ""}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (settings.hideAmounts) "$ ****" else "$${String.format("%,.2f", delivery.amountUsd)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )

                            IconButton(
                                onClick = { showDeleteConfirmDialog = listOf(delivery.id) },
                                modifier = Modifier.size(32.dp).testTag("delete_individual_${delivery.id}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar entrega individual",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Confirmation Dialog for Delete (Individual or Group)
    showDeleteConfirmDialog?.let { idsToDelete ->
        val isGroup = idsToDelete.size > 1
        val singleDelivery = if (!isGroup) deliveries.find { it.id == idsToDelete.firstOrNull() } else null

        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            icon = {
                Icon(
                    imageVector = if (isGroup) Icons.Default.DeleteSweep else Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isGroup) "¿Eliminar grupo de ${idsToDelete.size} entregas?" else "¿Eliminar entrega de ${singleDelivery?.clientName ?: "esta entrega"}?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isGroup) {
                            "Estás a punto de eliminar un grupo de ${idsToDelete.size} entregas seleccionadas del historial."
                        } else {
                            "Monto: $${String.format("%.2f", singleDelivery?.amountUsd ?: 0.0)} USD • Fecha: ${singleDelivery?.date ?: ""}"
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "El registro se trasladará a la papelera donde podrás restaurarlo si lo deseas.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDeliveries(idsToDelete)
                        selectedIds.removeAll(idsToDelete)
                        showDeleteConfirmDialog = null
                        Toast.makeText(
                            context,
                            if (isGroup) "${idsToDelete.size} entregas eliminadas del historial" else "Entrega eliminada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isGroup) "Eliminar ${idsToDelete.size}" else "Eliminar")
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
