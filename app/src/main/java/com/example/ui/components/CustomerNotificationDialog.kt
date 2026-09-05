package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.data.model.Delivery

enum class NotificationTemplateType {
    ASSIGNED,
    EN_ROUTE,
    DELIVERED,
    LOCATION_CONFIRM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerNotificationDialog(
    delivery: Delivery,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTemplate by remember { mutableStateOf(NotificationTemplateType.EN_ROUTE) }
    var isCustomizing by remember { mutableStateOf(false) }

    val workerName = if (delivery.assignedWorkerName.isNotBlank()) delivery.assignedWorkerName else "nuestro repartidor"
    val code = "NXF-${(delivery.id * 73 + 1042).toString().takeLast(5)}"

    val defaultEnRouteText = "🛵 Hola ${delivery.clientName}, tu remesa de $${String.format("%.2f", delivery.amountUsd)} USD ($${String.format("%,.0f", delivery.amountCup)} CUP) está en camino con el repartidor $workerName a tu dirección: ${delivery.address}. Por favor ten tu carnet de identidad a mano. ¡Gracias por elegir NexFy!"
    val defaultAssignedText = "📦 Hola ${delivery.clientName}, tu orden de remesa por $${String.format("%.2f", delivery.amountUsd)} USD ha sido procesada con éxito y asignada para entrega a $workerName. Te estaremos informando tan pronto esté en reparto."
    val defaultDeliveredText = "✅ [NEXFY REMESAS] Hola ${delivery.clientName}, confirmamos la entrega exitosa de tu remesa por $${String.format("%.2f", delivery.amountUsd)} USD ($${String.format("%,.0f", delivery.amountCup)} CUP). Folio de Comprobante: $code. ¡Gracias por tu confianza!"
    val defaultLocationText = "📍 Hola ${delivery.clientName}, estamos organizando la ruta de entrega de tu remesa en ${delivery.address}. ¿Podrías confirmarnos algún punto de referencia o enviar tu ubicación por aquí? Gracias."

    var messageText by remember(selectedTemplate) {
        mutableStateOf(
            when (selectedTemplate) {
                NotificationTemplateType.EN_ROUTE -> defaultEnRouteText
                NotificationTemplateType.ASSIGNED -> defaultAssignedText
                NotificationTemplateType.DELIVERED -> defaultDeliveredText
                NotificationTemplateType.LOCATION_CONFIRM -> defaultLocationText
            }
        )
    }

    val openWhatsApp = {
        val digitsOnly = delivery.phone.replace(Regex("[^0-9]"), "")
        if (digitsOnly.isEmpty()) {
            Toast.makeText(context, "Teléfono no válido para WhatsApp", Toast.LENGTH_SHORT).show()
        } else {
            val formattedPhone = if (digitsOnly.length == 8 && digitsOnly.startsWith("5")) "53$digitsOnly" else digitsOnly
            val encodedMsg = Uri.encode(messageText)
            val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMsg"
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=$formattedPhone&text=$encodedMsg"))
                    context.startActivity(waIntent)
                } catch (e2: Exception) {
                    Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val openSMS = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${delivery.phone}")).apply {
            putExtra("sms_body", messageText)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la app de SMS", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Notificar al Cliente",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = "Aviso directo por WhatsApp o SMS",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Client banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = delivery.clientName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                            Text(text = "Tel: ${delivery.phone}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Text(
                            text = "$${String.format("%.2f", delivery.amountUsd)} USD",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }

                // Template selector chips
                Text(
                    text = "Selecciona la plantilla de aviso:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val templates = listOf(
                        Pair(NotificationTemplateType.EN_ROUTE, "🛵 En Camino a la Dirección"),
                        Pair(NotificationTemplateType.DELIVERED, "✅ Comprobante de Entrega"),
                        Pair(NotificationTemplateType.ASSIGNED, "📦 Notificar Asignación"),
                        Pair(NotificationTemplateType.LOCATION_CONFIRM, "📍 Confirmar Punto / Ubicación")
                    )

                    templates.forEach { (type, label) ->
                        val isSel = selectedTemplate == type
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedTemplate = type
                                    isCustomizing = false
                                },
                            color = if (isSel) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isSel) Color(0xFF2563EB) else Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (isSel) Color(0xFF2563EB) else Color.Transparent)
                                        .border(1.5.dp, if (isSel) Color(0xFF2563EB) else Color(0xFF94A3B8), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color(0xFF1E3A8A) else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }

                // Message Text / Editor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mensaje a enviar:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B)
                    )
                    Text(
                        text = if (isCustomizing) "Editando..." else "Tocar para editar",
                        fontSize = 11.sp,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable { isCustomizing = !isCustomizing }
                    )
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1B4B),
                        unfocusedTextColor = Color(0xFF1E1B4B),
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    maxLines = 5
                )

                // Fast Action Buttons: WhatsApp and SMS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            openWhatsApp()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("btn_send_whatsapp_notif"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            openSMS()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_send_sms_notif"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SMS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Mensaje NexFy", messageText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Texto copiado al portapapeles ✓", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color(0xFF1E1B4B), modifier = Modifier.size(16.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
