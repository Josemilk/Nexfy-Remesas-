package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NexFyViewModel

@Composable
fun AdminUnlockScreen(
    viewModel: NexFyViewModel,
    isLicenseMode: Boolean = false,
    onBackToPin: () -> Unit = {},
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val deviceId by viewModel.deviceId.collectAsState()
    val masterPinInput by viewModel.masterPinInput.collectAsState()
    val failedAttempts by viewModel.failedUnlockAttempts.collectAsState()

    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF312E81), // Dark indigo
            Color(0xFF3730A3),
            Color(0xFF4338CA),
            Color(0xFF4F46E5), // Primary indigo
            Color(0xFF6366F1), // Soft indigo
            Color(0xFF818CF8)  // Light purple-indigo
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBg)
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Spacing
                Spacer(modifier = Modifier.height(24.dp))

                Spacer(modifier = Modifier.height(10.dp))

                // Shield Icon with Gear/Lock inside
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLicenseMode) Icons.Default.Security else Icons.Default.AdminPanelSettings,
                        contentDescription = if (isLicenseMode) "Licencia Anual" else "Desbloqueo Admin",
                        tint = Color(0xFFC7D2FE),
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = if (isLicenseMode) "Activación de\nLicencia Anual" else "Desbloqueo de\nAdministrador",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 35.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Device ID Pill Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ID del dispositivo: ",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = deviceId,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        // Copiar button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(deviceId))
                                    Toast.makeText(context, "ID de dispositivo copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("copy_device_id_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copiar",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isLicenseMode)
                        "Envía tu ID de activación a soporte para activar tu plan anual (365 días)"
                    else
                        "Contacta al admin y pide el PIN Maestro para este ID",
                    color = Color(0xFFE0E7FF),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                if (isLicenseMode) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Direct WhatsApp Action Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF25D366).copy(alpha = 0.25f))
                            .clickable {
                                try {
                                    val msg = "Hola soporte de NexFy, solicito la clave de activación anual para mi ID de dispositivo: $deviceId"
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://api.whatsapp.com/send?phone=51076491&text=${android.net.Uri.encode(msg)}")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "ID: $deviceId listo para enviar por mensaje", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("whatsapp_license_request_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💬 Enviar ID por WhatsApp",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Subtitle: Introduce PIN Maestro / Código de Activación
                Text(
                    text = if (isLicenseMode) "Introduce Código de Activación" else "Introduce PIN Maestro",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 8 Circles for Master PIN / Activation Code Input
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 8) {
                        val isFilled = i < masterPinInput.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) Color(0xFFC7D2FE) else Color.Transparent
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Numeric Keypad Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("DEL", "0", "BIO")
                )

                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (key in row) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .clickable {
                                        when (key) {
                                            "DEL" -> viewModel.deleteMasterPinDigit()
                                            "BIO" -> {
                                                Toast.makeText(context, "Verificación biométrica solicitada", Toast.LENGTH_SHORT).show()
                                            }
                                            else -> viewModel.appendMasterPinDigit(key)
                                        }
                                    }
                                    .testTag("master_pin_key_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "DEL" -> {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Borrar",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    "BIO" -> {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Biometría",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = key,
                                            color = Color.White,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Primary Action Button
                Button(
                    onClick = {
                        if (isLicenseMode) {
                            viewModel.activateAnnualLicense(
                                onSuccess = { daysGranted, expiresDateStr ->
                                    Toast.makeText(
                                        context,
                                        "🎉 ¡Licencia Anual activada ($daysGranted días)! Válida hasta $expiresDateStr",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onUnlocked()
                                },
                                onError = { errorMsg ->
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            viewModel.unlockWithMasterPin(
                                onSuccess = {
                                    Toast.makeText(context, "¡Acceso concedido! Crea tu nuevo PIN", Toast.LENGTH_LONG).show()
                                    onUnlocked()
                                },
                                onError = { errorMsg ->
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("master_pin_unlock_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = if (isLicenseMode) "Activar Licencia (365 Días)" else "Desbloquear y crear PIN nuevo",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Warning / Info Banner Pill
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLicenseMode) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFD97706).copy(alpha = 0.35f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLicenseMode) Icons.Default.Security else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isLicenseMode) Color(0xFF93C5FD) else Color(0xFFFDE047),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLicenseMode) {
                                if (failedAttempts > 0)
                                    "Clave vinculada a este hardware ($failedAttempts/3 intentos)"
                                else
                                    "Licencia vinculada a este hardware • Vigencia: 365 días"
                            } else {
                                if (failedAttempts > 0)
                                    "Si fallas 3 veces se borrarán los datos ($failedAttempts/3 fallidos)"
                                else
                                    "Si fallas 3 veces se borrarán todos los datos"
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer
                Text(
                    text = if (isLicenseMode) "NexFy Remesas • Licencia SaaS Anual • Soporte" else "NexFy Remesas • Versión 2.4.1 • Ayuda",
                    color = Color(0xFFC7D2FE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
