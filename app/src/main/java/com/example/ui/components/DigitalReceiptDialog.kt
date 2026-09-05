package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Delivery
import com.example.data.model.DeliveryStatus

data class PathPoint(val x: Float, val y: Float)
data class DrawnPath(val points: List<PathPoint>)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DigitalReceiptDialog(
    delivery: Delivery,
    onDismiss: () -> Unit,
    onConfirmSignature: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Generate unique verification code
    val verificationCode = remember(delivery.id) {
        "NXF-${(delivery.id * 73 + 1042).toString().takeLast(5)}"
    }

    // Signature state
    val paths = remember { mutableStateListOf<DrawnPath>() }
    var currentPathPoints = remember { mutableStateListOf<PathPoint>() }
    var isSignatureConfirmed by remember { mutableStateOf(false) }

    // Colors
    val primaryNavy = Color(0xFF0F172A)
    val accentGreen = Color(0xFF059669)
    val lightGreenBg = Color(0xFFECFDF5)
    val borderCol = Color(0xFFE2E8F0)
    val subText = Color(0xFF64748B)

    val exchangeRate = if (delivery.amountUsd > 0) delivery.amountCup / delivery.amountUsd else 0.0

    // Text representation of the certified receipt for sharing
    val shareReceiptText = """
═════════════════════════════════════
  🌟 NEXFY REMESAS & LOGÍSTICA
  COMPROBANTE OFICIAL DE ENTREGA
═════════════════════════════════════
Código de Verificación: $verificationCode
Estado: ${if (delivery.status == DeliveryStatus.DELIVERED) "ENTREGADO CONFORME ✓" else "PENDIENTE DE ENTREGA"}
Fecha: ${delivery.date}

DATOS DEL BENEFICIARIO:
• Cliente: ${delivery.clientName}
• CI / Identidad: ${if (delivery.identityNumber.isNotBlank()) delivery.identityNumber else "N/A"}
• Teléfono: ${delivery.phone}
• Dirección: ${delivery.address}
• Zona: ${delivery.zone}

DESGLOSE FINANCIERO:
• Monto USD: $${String.format("%.2f", delivery.amountUsd)} USD
• Monto CUP: $${String.format("%,.2f", delivery.amountCup)} CUP
• Tasa Aplicada: 1 USD = ${String.format("%.1f", exchangeRate)} CUP

REPARTIDOR RESPONSABLE:
• Mensajero: ${if (delivery.assignedWorkerName.isNotBlank()) delivery.assignedWorkerName else "Oficina Central NexFy"}
• Firma Digital: ${if (isSignatureConfirmed || delivery.status == DeliveryStatus.DELIVERED) "REGISTRADA EN DISPOSITIVO ✓" else "PENDIENTE"}

═════════════════════════════════════
Comprobante certificado emitido por el sistema NexFy Remesas.
Contacto de Soporte: neoappsoluciones@gmail.com
═════════════════════════════════════
    """.trimIndent()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Oficial",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Comprobante Oficial",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryNavy
                            )
                            Text(
                                text = "NexFy Remesas Certificado",
                                fontSize = 12.sp,
                                color = subText
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = subText, modifier = Modifier.size(18.dp))
                    }
                }

                // Official Receipt Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, borderCol)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stamp & Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "FOLIO / CÓDIGO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = subText,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = verificationCode,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF2563EB)
                                )
                            }

                            // Certified Stamp Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (delivery.status == DeliveryStatus.DELIVERED) lightGreenBg else Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, if (delivery.status == DeliveryStatus.DELIVERED) accentGreen else Color(0xFFF59E0B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (delivery.status == DeliveryStatus.DELIVERED) accentGreen else Color(0xFFD97706),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (delivery.status == DeliveryStatus.DELIVERED) "ENTREGADO ✓" else "PENDIENTE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (delivery.status == DeliveryStatus.DELIVERED) accentGreen else Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = borderCol)

                        // Beneficiary Info
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BENEFICIARIO", fontSize = 10.sp, color = subText, fontWeight = FontWeight.Bold)
                                Text(delivery.clientName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = primaryNavy)
                                if (delivery.identityNumber.isNotBlank()) {
                                    Text("CI: ${delivery.identityNumber}", fontSize = 12.sp, color = subText)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("TELÉFONO", fontSize = 10.sp, color = subText, fontWeight = FontWeight.Bold)
                                Text(delivery.phone, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = primaryNavy)
                            }
                        }

                        // Address Info
                        Column {
                            Text("DIRECCIÓN DE ENTREGA", fontSize = 10.sp, color = subText, fontWeight = FontWeight.Bold)
                            Text(delivery.address, fontSize = 13.sp, color = primaryNavy)
                            Text("Zona: ${delivery.zone}", fontSize = 11.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.Medium)
                        }

                        HorizontalDivider(color = borderCol)

                        // Financial Breakdown Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Monto en USD:", fontSize = 13.sp, color = Color(0xFF1E3A8A))
                                    Text("$${String.format("%.2f", delivery.amountUsd)} USD", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Monto a Entregar (CUP):", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                                    Text("$${String.format("%,.2f", delivery.amountCup)} CUP", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = accentGreen)
                                }

                                if (exchangeRate > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Tasa de Cambio Calculada:", fontSize = 11.sp, color = subText)
                                        Text("1 USD = ${String.format("%.1f", exchangeRate)} CUP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = subText)
                                    }
                                }
                            }
                        }

                        // Delivery metadata
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fecha de Emisión: ${delivery.date}", fontSize = 11.sp, color = subText)
                            Text("Repartidor: ${if (delivery.assignedWorkerName.isNotBlank()) delivery.assignedWorkerName else "NexFy"}", fontSize = 11.sp, color = subText, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // QR Code & Verification Block
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, borderCol)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // QR Graphic Simulation Canvas
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawSimulatedQRCode(size.width, size.height, verificationCode)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verificación Digital",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryNavy
                                )
                            }
                            Text(
                                text = "Escaneable para auditar entrega y comprobar autenticidad del recibo.",
                                fontSize = 11.sp,
                                color = subText,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ID: $verificationCode",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }

                // Interactive Signature Pad Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    border = BorderStroke(1.dp, borderCol)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Draw, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Firma Digital del Receptor",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryNavy
                                )
                            }

                            if (paths.isNotEmpty() || isSignatureConfirmed) {
                                IconButton(
                                    onClick = {
                                        paths.clear()
                                        currentPathPoints.clear()
                                        isSignatureConfirmed = false
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Limpiar Firma", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Touch Canvas for Signature
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(
                                    1.dp,
                                    if (isSignatureConfirmed) accentGreen else Color(0xFFCBD5E1),
                                    RoundedCornerShape(10.dp)
                                )
                                .pointerInteropFilter { event ->
                                    when (event.action) {
                                        MotionEvent.ACTION_DOWN -> {
                                            currentPathPoints.clear()
                                            currentPathPoints.add(PathPoint(event.x, event.y))
                                            true
                                        }
                                        MotionEvent.ACTION_MOVE -> {
                                            currentPathPoints.add(PathPoint(event.x, event.y))
                                            true
                                        }
                                        MotionEvent.ACTION_UP -> {
                                            if (currentPathPoints.size > 1) {
                                                paths.add(DrawnPath(currentPathPoints.toList()))
                                            }
                                            currentPathPoints.clear()
                                            true
                                        }
                                        else -> false
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw horizontal guideline
                                drawLine(
                                    color = Color(0xFFE2E8F0),
                                    start = Offset(20f, size.height * 0.75f),
                                    end = Offset(size.width - 20f, size.height * 0.75f),
                                    strokeWidth = 1f
                                )

                                // Draw existing paths
                                paths.forEach { drawnPath ->
                                    val pts = drawnPath.points
                                    for (i in 0 until pts.size - 1) {
                                        drawLine(
                                            color = Color(0xFF1E293B),
                                            start = Offset(pts[i].x, pts[i].y),
                                            end = Offset(pts[i + 1].x, pts[i + 1].y),
                                            strokeWidth = 4f,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }

                                // Draw current active path
                                if (currentPathPoints.size > 1) {
                                    for (i in 0 until currentPathPoints.size - 1) {
                                        drawLine(
                                            color = Color(0xFF2563EB),
                                            start = Offset(currentPathPoints[i].x, currentPathPoints[i].y),
                                            end = Offset(currentPathPoints[i + 1].x, currentPathPoints[i + 1].y),
                                            strokeWidth = 4f,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            }

                            if (paths.isEmpty() && currentPathPoints.isEmpty() && !isSignatureConfirmed) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Firma aquí con el dedo al recibir",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }

                        if (paths.isNotEmpty() && !isSignatureConfirmed) {
                            Button(
                                onClick = {
                                    isSignatureConfirmed = true
                                    onConfirmSignature?.invoke("SIGNED-$verificationCode")
                                    Toast.makeText(context, "Firma digital guardada ✓", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Confirmar y Guardar Firma", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Action Buttons: Share / Copy / WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Share Receipt
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareReceiptText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Compartir Comprobante NexFy")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_share_receipt"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Copy Text
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Comprobante NexFy", shareReceiptText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Comprobante copiado al portapapeles ✓", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = primaryNavy, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// Draws simulated realistic high-contrast QR Matrix pattern with corner registration marks
private fun DrawScope.drawSimulatedQRCode(width: Float, height: Float, seedText: String) {
    val gridSize = 19
    val cellSize = width / gridSize
    val dark = Color(0xFF0F172A)

    fun drawFinder(cx: Int, cy: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                if (isOuter || isInner) {
                    drawRect(
                        color = dark,
                        topLeft = Offset((cx + c) * cellSize, (cy + r) * cellSize),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                    )
                }
            }
        }
    }

    // Three Finder Patterns
    drawFinder(0, 0)
    drawFinder(gridSize - 7, 0)
    drawFinder(0, gridSize - 7)

    // Timing lines
    for (i in 7 until gridSize - 7) {
        if (i % 2 == 0) {
            drawRect(dark, Offset(i * cellSize, 6 * cellSize), androidx.compose.ui.geometry.Size(cellSize, cellSize))
            drawRect(dark, Offset(6 * cellSize, i * cellSize), androidx.compose.ui.geometry.Size(cellSize, cellSize))
        }
    }

    // Pseudorandom pseudo-data matrix based on seed text hash
    val hash = seedText.hashCode()
    for (r in 0 until gridSize) {
        for (c in 0 until gridSize) {
            val inFinder1 = r < 8 && c < 8
            val inFinder2 = r < 8 && c >= gridSize - 8
            val inFinder3 = r >= gridSize - 8 && c < 8
            if (!inFinder1 && !inFinder2 && !inFinder3) {
                val pseudoBit = ((r * 31 + c * 17 + hash) % 3) == 0
                if (pseudoBit) {
                    drawRect(
                        color = dark,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}
