package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppSettings
import com.example.data.model.Delivery
import com.example.data.model.DeliveryStatus
import com.example.data.model.Worker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyCashSettlementDialog(
    deliveries: List<Delivery>,
    workers: List<Worker>,
    settings: AppSettings? = null,
    onUpdateSettings: ((AppSettings) -> Unit)? = null,
    onUpdateDelivery: ((Delivery) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayDisplayStr = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    var selectedFilter by remember { mutableStateOf("TODAY") } // TODAY, ALL

    // Editable commission per delivery rate (USD)
    var commissionRateUsd by remember(settings?.commissionPercent) {
        mutableDoubleStateOf(settings?.commissionPercent?.takeIf { it > 0.0 } ?: 1.50)
    }
    var showCommissionEditDialog by remember { mutableStateOf(false) }

    // Delivery being edited in dialog
    var deliveryToEdit by remember { mutableStateOf<Delivery?>(null) }

    // Filter deliveries based on real date / status
    val filteredDeliveries = remember(selectedFilter, deliveries) {
        if (selectedFilter == "TODAY") {
            deliveries.filter {
                it.date.contains(todayDisplayStr) ||
                it.date.startsWith(todayStr) ||
                it.status == DeliveryStatus.DELIVERED
            }
        } else {
            deliveries
        }
    }

    val deliveredList = filteredDeliveries.filter { it.status == DeliveryStatus.DELIVERED }
    val pendingList = filteredDeliveries.filter { it.status == DeliveryStatus.PENDING }

    // Real financial totals calculated dynamically from real deliveries
    val totalReceivedUsd = filteredDeliveries.sumOf { it.amountUsd }
    val totalReceivedCup = filteredDeliveries.sumOf { it.amountCup }
    val totalDeliveredUsd = deliveredList.sumOf { it.amountUsd }
    val totalDeliveredCup = deliveredList.sumOf { it.amountCup }
    val totalPendingUsd = pendingList.sumOf { it.amountUsd }
    val totalPendingCup = pendingList.sumOf { it.amountCup }

    // Worker settlement state
    val settledWorkers = remember { mutableStateMapOf<String, Boolean>() }

    // Group delivered items by assigned worker
    val workerDeliveries = deliveredList.groupBy {
        if (it.assignedWorkerName.isNotBlank()) it.assignedWorkerName else "Sin Asignar / En Oficina"
    }

    val totalCommissionsUsd = deliveredList.size * commissionRateUsd
    val netCashInBoxUsd = totalReceivedUsd - totalDeliveredUsd - totalCommissionsUsd

    // Formatted WhatsApp / Export report
    val settlementReportText = buildString {
        appendLine("═════════════════════════════════════")
        appendLine(" 📊 NEXFY REMESAS - CUADRE DE CAJA DIARIO")
        appendLine("═════════════════════════════════════")
        appendLine("Fecha de Cierre: $todayDisplayStr")
        appendLine("Total Remesas Registradas: ${filteredDeliveries.size}")
        appendLine("Entregadas: ${deliveredList.size} | Pendientes: ${pendingList.size}")
        appendLine("─────────────────────────────────────")
        appendLine("💰 RECAUDO & ASIGNACIÓN (REAL):")
        appendLine("• Dinero Total Recibido: $${String.format("%.2f", totalReceivedUsd)} USD | $${String.format("%,.0f", totalReceivedCup)} CUP")
        appendLine("• Dinero Entregado a Clientes: $${String.format("%.2f", totalDeliveredUsd)} USD | $${String.format("%,.0f", totalDeliveredCup)} CUP")
        appendLine("• Dinero Pendiente de Entrega: $${String.format("%.2f", totalPendingUsd)} USD | $${String.format("%,.0f", totalPendingCup)} CUP")
        appendLine("• Total Comisiones Reparto: $${String.format("%.2f", totalCommissionsUsd)} USD (Tarifa: $$commissionRateUsd USD/entrega)")
        appendLine("─────────────────────────────────────")
        appendLine("🛵 DESGLOSE POR REPARTIDOR:")
        if (workerDeliveries.isEmpty()) {
            appendLine("• No hay entregas completadas en este período.")
        } else {
            workerDeliveries.forEach { (worker, list) ->
                val workerUsd = list.sumOf { it.amountUsd }
                val workerCup = list.sumOf { it.amountCup }
                val comm = list.size * commissionRateUsd
                val isPaid = settledWorkers[worker] == true
                appendLine("👤 $worker:")
                appendLine("   - Entregas realizadas: ${list.size}")
                appendLine("   - Monto entregado: $${String.format("%.2f", workerUsd)} USD / $${String.format("%,.0f", workerCup)} CUP")
                appendLine("   - Comisión a pagar: $${String.format("%.2f", comm)} USD [${if (isPaid) "LIQUIDADO ✓" else "PENDIENTE"}]")
            }
        }
        appendLine("═════════════════════════════════════")
        appendLine("Reporte generado con datos 100% reales desde NexFy.")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFFF8FAFC),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0284C7).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cuadre de Caja Diario",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Datos reales calculados de remesas del día",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                    }
                }

                // Filter Buttons (Hoy vs Todo el Historial)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        Pair("TODAY", "📅 Hoy ($todayDisplayStr)"),
                        Pair("ALL", "📊 Todo el Historial")
                    )

                    filters.forEach { (key, label) ->
                        val isSel = selectedFilter == key
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedFilter = key },
                            color = if (isSel) Color(0xFF0284C7) else Color.White,
                            border = BorderStroke(1.dp, if (isSel) Color(0xFF0284C7) else Color(0xFFCBD5E1))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else Color(0xFF334155),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Banner: Resumen de Dinero Recibido vs Entregado (100% Real)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dinero Total Ingresado (Asignado)",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${filteredDeliveries.size} remesas totales",
                                fontSize = 11.sp,
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$${String.format("%.2f", totalReceivedUsd)} USD",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "$${String.format("%,.0f", totalReceivedCup)} CUP",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                // KPI Cards Grid (USD Entregado & CUP Entregado)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // USD Entregado
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total USD Entregado", fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format("%.2f", totalDeliveredUsd)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D)
                            )
                            Text("${deliveredList.size} entregas liquidadas", fontSize = 10.sp, color = Color(0xFF16A34A))
                        }
                    }

                    // CUP Entregado
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total CUP Entregado", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format("%,.0f", totalDeliveredCup)}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0284C7)
                            )
                            Text("Efectivo distribuido", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                // Pendientes Card (Si hay remesas pendientes de entregar)
                if (pendingList.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Remesas Pendientes de Entrega", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                Text("${pendingList.size} clientes aún en reparto", fontSize = 11.sp, color = Color(0xFFB45309))
                            }
                            Text(
                                text = "$${String.format("%.2f", totalPendingUsd)} USD",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }

                // Commission summary banner (Editable with Pencil Icon)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showCommissionEditDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Comisiones de Reparto", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Edit, contentDescription = "Editar comisión", tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp))
                                }
                                Text("Tarifa: $$commissionRateUsd USD / entrega (Toca para editar)", fontSize = 10.sp, color = Color(0xFF3B82F6))
                            }
                        }
                        Text(
                            text = "$${String.format("%.2f", totalCommissionsUsd)} USD",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }

                // Section: Desglose y Edición de Remesas del Día
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalle de Remesas (${filteredDeliveries.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Toca para editar monto",
                        fontSize = 11.sp,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Medium
                    )
                }

                if (filteredDeliveries.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("No hay remesas registradas para este filtro.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        filteredDeliveries.forEach { item ->
                            val isDelivered = item.status == DeliveryStatus.DELIVERED
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { deliveryToEdit = item },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, if (isDelivered) Color(0xFFBBF7D0) else Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.clientName.ifEmpty { "Cliente" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${item.assignedWorkerName.ifEmpty { "Sin asignar" }} • ${if (isDelivered) "Entregado" else "Pendiente"}",
                                            fontSize = 11.sp,
                                            color = if (isDelivered) Color(0xFF16A34A) else Color(0xFFD97706),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "$${String.format("%.2f", item.amountUsd)} USD",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "$${String.format("%,.0f", item.amountCup)} CUP",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar remesa",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Desglose y Liquidación por Repartidor
                Text(
                    text = "Liquidación por Repartidor / Mensajero",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                if (workerDeliveries.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("No hay entregas liquidadas por repartidores.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        workerDeliveries.forEach { (workerName, list) ->
                            val workerUsd = list.sumOf { it.amountUsd }
                            val workerCup = list.sumOf { it.amountCup }
                            val commission = list.size * commissionRateUsd
                            val isSettled = settledWorkers[workerName] == true

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, if (isSettled) Color(0xFF10B981) else Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(workerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                Text("${list.size} entregas realizadas", fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                        }

                                        // Status badge
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSettled) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                                            border = BorderStroke(1.dp, if (isSettled) Color(0xFF10B981) else Color(0xFFF59E0B))
                                        ) {
                                            Text(
                                                text = if (isSettled) "LIQUIDADO ✓" else "PENDIENTE",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSettled) Color(0xFF059669) else Color(0xFFD97706)
                                            )
                                        }
                                    }

                                    // Amount summary row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Entregado: $${String.format("%.2f", workerUsd)} USD | $${String.format("%,.0f", workerCup)} CUP",
                                            fontSize = 11.sp,
                                            color = Color(0xFF334155),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Comisión: $${String.format("%.2f", commission)} USD",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB)
                                        )
                                    }

                                    // Action to settle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                settledWorkers[workerName] = !isSettled
                                                Toast.makeText(
                                                    context,
                                                    if (!isSettled) "$workerName marcado como Liquidado ✓" else "$workerName marcado como Pendiente",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSettled) Color(0xFFF1F5F9) else Color(0xFF10B981)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(
                                                text = if (isSettled) "Desmarcar Liquidación" else "Marcar como Liquidado",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSettled) Color(0xFF64748B) else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons: Compartir / Copiar Cierre de Caja
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, settlementReportText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Enviar Cierre de Caja NexFy")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_share_daily_settlement"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar / WhatsApp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Cuadre NexFy", settlementReportText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Reporte de cuadre copiado al portapapeles ✓", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color(0xFF0F172A), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // Dialog for Editing Commission Rate
    if (showCommissionEditDialog) {
        var tempCommStr by remember { mutableStateOf(commissionRateUsd.toString()) }
        AlertDialog(
            onDismissRequest = { showCommissionEditDialog = false },
            title = { Text("Editar Tarifa de Comisión", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Define la comisión en USD que se paga al repartidor por cada entrega completada:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = tempCommStr,
                        onValueChange = { tempCommStr = it },
                        label = { Text("Comisión por Entrega (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold, color = Color(0xFF0284C7)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempCommStr.toDoubleOrNull()
                        if (parsed != null && parsed >= 0.0) {
                            commissionRateUsd = parsed
                            if (settings != null && onUpdateSettings != null) {
                                onUpdateSettings(settings.copy(commissionPercent = parsed))
                            }
                            Toast.makeText(context, "Tarifa de comisión actualizada a $$parsed USD", Toast.LENGTH_SHORT).show()
                            showCommissionEditDialog = false
                        } else {
                            Toast.makeText(context, "Ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommissionEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog for Editing an Individual Delivery Amount & Note
    deliveryToEdit?.let { targetDelivery ->
        var editUsdStr by remember(targetDelivery) { mutableStateOf(targetDelivery.amountUsd.toString()) }
        var editCupStr by remember(targetDelivery) { mutableStateOf(targetDelivery.amountCup.toString()) }
        var editWorkerName by remember(targetDelivery) { mutableStateOf(targetDelivery.assignedWorkerName) }
        var editStatus by remember(targetDelivery) { mutableStateOf(targetDelivery.status) }

        AlertDialog(
            onDismissRequest = { deliveryToEdit = null },
            title = {
                Text(
                    text = "Editar Remesa: ${targetDelivery.clientName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Modifica los montos de dinero para recalcular automáticamente el cuadre de caja:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedTextField(
                        value = editUsdStr,
                        onValueChange = {
                            editUsdStr = it
                            val usd = it.toDoubleOrNull()
                            if (usd != null && settings != null) {
                                editCupStr = String.format(Locale.US, "%.2f", usd * settings.usdCupRate)
                            }
                        },
                        label = { Text("Monto en USD") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCupStr,
                        onValueChange = { editCupStr = it },
                        label = { Text("Monto en CUP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Delivery status toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estado de la entrega:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (editStatus == DeliveryStatus.DELIVERED) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, if (editStatus == DeliveryStatus.DELIVERED) Color(0xFF86EFAC) else Color(0xFFFDE68A)),
                            modifier = Modifier.clickable {
                                editStatus = if (editStatus == DeliveryStatus.DELIVERED) DeliveryStatus.PENDING else DeliveryStatus.DELIVERED
                            }
                        ) {
                            Text(
                                text = if (editStatus == DeliveryStatus.DELIVERED) "ENTREGADO ✓" else "PENDIENTE ⏳",
                                color = if (editStatus == DeliveryStatus.DELIVERED) Color(0xFF15803D) else Color(0xFFB45309),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newUsd = editUsdStr.toDoubleOrNull() ?: targetDelivery.amountUsd
                        val newCup = editCupStr.toDoubleOrNull() ?: targetDelivery.amountCup

                        val updated = targetDelivery.copy(
                            amountUsd = newUsd,
                            amountCup = newCup,
                            status = editStatus
                        )

                        if (onUpdateDelivery != null) {
                            onUpdateDelivery(updated)
                        }

                        Toast.makeText(context, "Remesa actualizada y cuadre de caja recalculado ✓", Toast.LENGTH_SHORT).show()
                        deliveryToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { deliveryToEdit = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
