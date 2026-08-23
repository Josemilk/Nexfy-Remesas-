package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NexFyViewModel

@Composable
fun NewDeliveryScreen(
    viewModel: NexFyViewModel,
    onBack: () -> Unit,
    initialClientName: String = "",
    initialPhone: String = ""
) {
    val settings by viewModel.settings.collectAsState()
    val clients by viewModel.clients.collectAsState()

    var clientName by remember { mutableStateOf(initialClientName) }
    var phone by remember { mutableStateOf(if (initialPhone.isNotEmpty()) initialPhone else "+53 5 ") }
    var amountUsdText by remember { mutableStateOf("250.00") }
    var address by remember { mutableStateOf("") }
    var identityNumber by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedClientBanner by remember { mutableStateOf<String?>(null) }

    // If initial params were passed, try to prefill address & identity from matching client
    androidx.compose.runtime.LaunchedEffect(initialClientName, initialPhone, clients) {
        if (initialClientName.isNotEmpty() || initialPhone.isNotEmpty()) {
            val match = clients.find {
                (initialClientName.isNotEmpty() && it.name.trim().equals(initialClientName.trim(), ignoreCase = true)) ||
                (initialPhone.isNotEmpty() && it.phone.replace(" ", "") == initialPhone.replace(" ", ""))
            }
            if (match != null) {
                clientName = match.name
                phone = match.phone
                address = match.address
                identityNumber = match.identityNumber
                selectedClientBanner = "Ficha de cliente '${match.name}' cargada ✓"
            }
        }
    }

    val usdAmount = amountUsdText.toDoubleOrNull() ?: 0.0
    val calculatedCup = usdAmount * settings.usdCupRate

    val scrollState = rememberScrollState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = Color(0xFF2563EB),
        unfocusedBorderColor = Color(0xFF94A3B8),
        focusedLabelColor = Color(0xFF2563EB),
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )

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
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("new_delivery_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Nueva entrega",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Box {
                var showNewDeliveryMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showNewDeliveryMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
                DropdownMenu(
                    expanded = showNewDeliveryMenu,
                    onDismissRequest = { showNewDeliveryMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Limpiar campos") },
                        onClick = {
                            showNewDeliveryMenu = false
                            clientName = ""
                            phone = ""
                            amountUsdText = ""
                            address = ""
                            identityNumber = ""
                            note = ""
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cargar ejemplo") },
                        onClick = {
                            showNewDeliveryMenu = false
                            clientName = "María González"
                            phone = "+53 52123456"
                            amountUsdText = "150"
                            address = "Calle 23 #456 e/ G y H, Vedado"
                            identityNumber = "90051212345"
                            note = "Entregar en la tarde"
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Step Indicator Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Formulario de registro",
                color = Color(0xFF4338CA),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF94A3B8))
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF94A3B8), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF94A3B8), CircleShape)
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
            // Datos de la entrega Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Datos de la entrega",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (selectedClientBanner != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedClientBanner!!,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { selectedClientBanner = null },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = Color(0xFF065F46)
                                    )
                                }
                            }
                        }
                    }

                    // Nombre completo
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = {
                            clientName = it
                            selectedClientBanner = null
                        },
                        label = { Text("Nombre completo") },
                        placeholder = { Text("Ej. Juan Pérez García") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2563EB))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_delivery_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )

                    // Client Suggestions Box
                    val matchingClients = remember(clientName, phone, clients) {
                        if (clientName.length >= 2 || (phone.length >= 6 && phone != "+53 5 ")) {
                            clients.filter { c ->
                                (clientName.length >= 2 && c.name.contains(clientName, ignoreCase = true)) ||
                                (phone.length >= 6 && c.phone.contains(phone.replace(" ", "")))
                            }
                        } else emptyList()
                    }

                    if (matchingClients.isNotEmpty() && selectedClientBanner == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "💡 Fichas de clientes encontradas (${matchingClients.size}) - Toca para autocompletar:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                matchingClients.take(3).forEach { client ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .clickable {
                                                clientName = client.name
                                                phone = client.phone
                                                address = client.address
                                                identityNumber = client.identityNumber
                                                selectedClientBanner = "Ficha de '${client.name}' seleccionada ✓ Edita la cantidad a entregar."
                                            }
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = client.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = "${client.phone} • ${client.zone}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Text(
                                            text = "Usar ficha",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }

                    // Teléfono
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2563EB))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_delivery_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )

                    // Cantidad USD
                    Column {
                        OutlinedTextField(
                            value = amountUsdText,
                            onValueChange = { amountUsdText = it },
                            label = { Text("Cantidad USD") },
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF2563EB))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_delivery_usd_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                        if (calculatedCup > 0) {
                            Text(
                                text = "Equivalente: $${String.format("%.2f", calculatedCup)} CUP (Tasa: ${settings.usdCupRate})",
                                fontSize = 12.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }

                    // Dirección
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección") },
                        placeholder = { Text("Calle, número, municipio, provincia") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2563EB))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_delivery_address_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )

                    // Número de identidad
                    OutlinedTextField(
                        value = identityNumber,
                        onValueChange = { identityNumber = it },
                        label = { Text("Número de identidad") },
                        placeholder = { Text("Opcional - CI o pasaporte") },
                        leadingIcon = {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF2563EB))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_delivery_identity_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )

                    // Nota
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Nota") },
                        placeholder = { Text("Instrucciones, referencia, entre la 3ra y 4ta...") },
                        leadingIcon = {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF2563EB))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_delivery_note_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = textFieldColors
                    )
                }
            }

            // Ubicación Card
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ubicación",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Map Preview Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Vector map grid simulation background
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (i in 0..4) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFCBD5E1))
                                )
                            }
                        }

                        // Map Pin
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pin de ubicación",
                            tint = Color(0xFF6D28D9),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val finalName = clientName.ifEmpty { "Juan Pérez" }
                            val finalAddress = address.ifEmpty { "Marcador Manual" }
                            viewModel.saveNewDelivery(
                                clientName = finalName,
                                phone = phone,
                                amountUsd = usdAmount,
                                address = finalAddress,
                                identityNumber = identityNumber,
                                note = note,
                                isManualPin = true
                            )
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Poner pin manual",
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Save Button
            Button(
                onClick = {
                    val finalName = clientName.ifEmpty { "Juan Pérez" }
                    val finalAddress = address.ifEmpty { "Calle 23 #451, Vedado, La Habana" }
                    viewModel.saveNewDelivery(
                        clientName = finalName,
                        phone = phone,
                        amountUsd = usdAmount,
                        address = finalAddress,
                        identityNumber = identityNumber,
                        note = note
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_delivery_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Guardar y ver ruta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
