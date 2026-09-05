package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.Client
import com.example.ui.NexFyViewModel
import com.example.ui.Screen
import com.example.ui.components.DailyCashSettlementDialog
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Payments
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff

sealed class DashboardDeleteTarget {
    data class DeliveryItem(val id: Long, val name: String, val amount: Double) : DashboardDeleteTarget()
    data class ClientItem(val id: Long, val name: String) : DashboardDeleteTarget()
    data class CardWidget(val key: String, val title: String) : DashboardDeleteTarget()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: NexFyViewModel,
    onNavigateToNewDelivery: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeliveries: () -> Unit,
    onSelectDelivery: (Long) -> Unit,
    onNavigateToMap: () -> Unit
) {
    val pendingCount by viewModel.pendingCount.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val deliveries by viewModel.deliveries.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val workers by viewModel.workers.collectAsState()
    var showDailySettlementDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var activeDeleteTarget by remember { mutableStateOf<DashboardDeleteTarget?>(null) }
    var longPressedCardId by remember { mutableStateOf<String?>(null) }

    val hasHiddenCards = !settings.showConnectionStatusCard || !settings.showPendingHeroCard || !settings.showDailySettlementCard

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top App Bar
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
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2563EB), Color(0xFF6D28D9))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "NexFy Logo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "NexFy Remesas",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (hasHiddenCards) {
                    IconButton(
                        onClick = {
                            viewModel.updateSettings(
                                settings.copy(
                                    showConnectionStatusCard = true,
                                    showPendingHeroCard = true,
                                    showDailySettlementCard = true
                                )
                            )
                            Toast.makeText(context, "Tarjetas restauradas", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Restaurar tarjetas",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.testTag("dashboard_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dual Status Card: Connection & Offline Map Sync Status (Long-press to delete/hide)
        if (settings.showConnectionStatusCard) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (isOnline) {
                                viewModel.performAutoSync()
                            }
                        },
                        onLongClick = {
                            longPressedCardId = "card_connection"
                            activeDeleteTarget = DashboardDeleteTarget.CardWidget(
                                "showConnectionStatusCard",
                                "Estado de Conexión y Mapa"
                            )
                        }
                    )
                    .testTag("dashboard_connection_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Indicator 1: Internet & Data Connection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = if (isOnline) Color(0xFF059669) else Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = if (isOnline) "Conexión: En línea" else "Conexión: Modo Offline (100% Funcional)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnline) Color(0xFF065F46) else Color(0xFF92400E)
                                )
                                Text(
                                    text = when {
                                        !isOnline -> "Sincronización automática al reconectar"
                                        isSyncing -> "Sincronizando cambios con el servidor..."
                                        pendingSyncCount > 0 -> "$pendingSyncCount cambios pendientes por enviar"
                                        else -> "Datos sincronizados en tiempo real"
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFF4B5563)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (!isOnline) Color(0xFFFEF3C7) else if (pendingSyncCount > 0) Color(0xFFDBEAFE) else Color(0xFFDCFCE7)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (!isOnline) "Offline" else if (isSyncing) "Syncing..." else if (pendingSyncCount > 0) "Pendiente" else "Online",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isOnline) Color(0xFFD97706) else if (pendingSyncCount > 0) Color(0xFF1D4ED8) else Color(0xFF059669)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    activeDeleteTarget = DashboardDeleteTarget.CardWidget(
                                        "showConnectionStatusCard",
                                        "Estado de Conexión y Mapa"
                                    )
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(
            text = "Dashboard de inicio",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1B4B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Card (Entregas pendientes) (Long-press to delete/hide)
        val pendingDeliveries = deliveries.filter { it.status == com.example.data.model.DeliveryStatus.PENDING }
        
        if (settings.showPendingHeroCard) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            longPressedCardId = "card_hero_pending"
                            activeDeleteTarget = DashboardDeleteTarget.CardWidget(
                                "showPendingHeroCard",
                                "Tarjeta Entregas Pendientes"
                            )
                        }
                    )
                    .testTag("dashboard_pending_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2563EB), Color(0xFF6D28D9))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Entregas pendientes",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (pendingDeliveries.isEmpty()) {
                                        Text(
                                            text = "Al día",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (pendingDeliveries.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.25f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$pendingCount",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        activeDeleteTarget = DashboardDeleteTarget.CardWidget(
                                            "showPendingHeroCard",
                                            "Tarjeta Entregas Pendientes"
                                        )
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color(0xFFFCA5A5), modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        if (pendingDeliveries.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tienes entregas pendientes en este momento. ¡Buen trabajo!",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(20.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pendingDeliveries.forEach { delivery ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = { onSelectDelivery(delivery.id) },
                                                onLongClick = {
                                                    longPressedCardId = "delivery_${delivery.id}"
                                                    activeDeleteTarget = DashboardDeleteTarget.DeliveryItem(
                                                        delivery.id,
                                                        delivery.clientName,
                                                        delivery.amountUsd
                                                    )
                                                }
                                            ),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                val initials = delivery.clientName
                                                    .split(" ")
                                                    .mapNotNull { it.firstOrNull() }
                                                    .take(2)
                                                    .joinToString("")
                                                    .uppercase()
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = initials.ifEmpty { "C" },
                                                        color = Color(0xFF1E1B4B),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                    Text(
                                                        text = delivery.clientName,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.LocationOn,
                                                            contentDescription = null,
                                                            tint = Color.White.copy(alpha = 0.7f),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = delivery.zone,
                                                            fontSize = 12.sp,
                                                            color = Color.White.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (settings.hideAmounts) "$ ****" else "$${String.format("%.0f", delivery.amountUsd)}",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )

                                                IconButton(
                                                    onClick = {
                                                        activeDeleteTarget = DashboardDeleteTarget.DeliveryItem(
                                                            delivery.id,
                                                            delivery.clientName,
                                                            delivery.amountUsd
                                                        )
                                                    },
                                                    modifier = Modifier.size(28.dp).padding(start = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar entrega",
                                                        tint = Color(0xFFFCA5A5),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
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

        // Cuadre de Caja Diario Card (Long-press to delete/hide)
        val deliveredCount = deliveries.count { it.status == com.example.data.model.DeliveryStatus.DELIVERED }
        val deliveredUsd = deliveries.filter { it.status == com.example.data.model.DeliveryStatus.DELIVERED }.sumOf { it.amountUsd }
        
        if (settings.showDailySettlementCard) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .combinedClickable(
                        onClick = { showDailySettlementDialog = true },
                        onLongClick = {
                            longPressedCardId = "card_daily_settlement"
                            activeDeleteTarget = DashboardDeleteTarget.CardWidget(
                                "showDailySettlementCard",
                                "Cuadre de Caja Diario"
                            )
                        }
                    )
                    .testTag("dashboard_cash_settlement_card"),
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF16A34A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cuadre de Caja Diario",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14532D)
                            )
                            Text(
                                text = "$deliveredCount entregadas • $${String.format("%.2f", deliveredUsd)} USD",
                                fontSize = 12.sp,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF16A34A)
                        ) {
                            Text(
                                text = "Liquidar",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                activeDeleteTarget = DashboardDeleteTarget.CardWidget(
                                    "showDailySettlementCard",
                                    "Cuadre de Caja Diario"
                                )
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Clientes Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clientes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B4B)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onNavigateToClients() }
                    .padding(4.dp)
            ) {
                Text(
                    text = "Ver todos (${clients.size})",
                    color = Color(0xFF3B82F6),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Recent Clients List (Long-press to delete client)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (clients.isEmpty()) {
                Text(
                    text = "No tienes clientes registrados.",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
                )
            } else {
                clients.take(4).forEach { client ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onNavigateToClients() },
                                onLongClick = {
                                    longPressedCardId = "client_${client.id}"
                                    activeDeleteTarget = DashboardDeleteTarget.ClientItem(client.id, client.name)
                                }
                            ),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                val initials = client.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials.ifEmpty { "C" },
                                        color = Color(0xFF1E1B4B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = client.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E1B4B)
                                    )
                                    Text(
                                        text = client.phone,
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    activeDeleteTarget = DashboardDeleteTarget.ClientItem(client.id, client.name)
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar cliente",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        // Big Action Button "+ Nueva entrega"
        Button(
            onClick = onNavigateToNewDelivery,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("dashboard_new_delivery_button"),
            shape = RoundedCornerShape(29.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nueva entrega",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

        FloatingActionButton(
            onClick = onNavigateToMap,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            containerColor = Color(0xFF2563EB),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Map, contentDescription = "Mapa offline")
        }

        // Daily Cash Settlement & Courier Liquidation Dialog
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

        // Delete Confirmation Dialog for Dashboard elements
        activeDeleteTarget?.let { target ->
            AlertDialog(
                onDismissRequest = { activeDeleteTarget = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = when (target) {
                            is DashboardDeleteTarget.DeliveryItem -> "¿Eliminar entrega de ${target.name}?"
                            is DashboardDeleteTarget.ClientItem -> "¿Eliminar cliente ${target.name}?"
                            is DashboardDeleteTarget.CardWidget -> "¿Eliminar tarjeta del panel?"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = when (target) {
                            is DashboardDeleteTarget.DeliveryItem -> "Monto: $${String.format("%.2f", target.amount)} USD. El registro se moverá a la papelera."
                            is DashboardDeleteTarget.ClientItem -> "Esta acción eliminará el cliente de la lista de contactos."
                            is DashboardDeleteTarget.CardWidget -> "Ocultará la tarjeta '${target.title}' del dashboard. Puedes restaurarla cuando lo desees tocando el botón de restaurar en la parte superior."
                        },
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when (target) {
                                is DashboardDeleteTarget.DeliveryItem -> {
                                    viewModel.deleteDelivery(target.id)
                                    Toast.makeText(context, "Entrega eliminada", Toast.LENGTH_SHORT).show()
                                }
                                is DashboardDeleteTarget.ClientItem -> {
                                    viewModel.deleteClient(target.id)
                                    Toast.makeText(context, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                                }
                                is DashboardDeleteTarget.CardWidget -> {
                                    when (target.key) {
                                        "showConnectionStatusCard" -> viewModel.updateSettings(settings.copy(showConnectionStatusCard = false))
                                        "showPendingHeroCard" -> viewModel.updateSettings(settings.copy(showPendingHeroCard = false))
                                        "showDailySettlementCard" -> viewModel.updateSettings(settings.copy(showDailySettlementCard = false))
                                    }
                                    Toast.makeText(context, "Tarjeta eliminada del panel", Toast.LENGTH_SHORT).show()
                                }
                            }
                            activeDeleteTarget = null
                            longPressedCardId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            activeDeleteTarget = null
                            longPressedCardId = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
