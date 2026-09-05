package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun ExchangeRateCalculatorCard(
    currentRate: Double,
    onRateChanged: (Double) -> Unit,
    isDark: Boolean = false,
    modifier: Modifier = Modifier,
    title: String = "Calculadora de Tasa de Cambio en Vivo",
    subtitle: String = "Referencia y conversor en tiempo real (USD ⇄ CUP)"
) {
    val context = LocalContext.current

    var rateInputStr by remember(currentRate) {
        mutableStateOf(if (currentRate > 0) String.format(Locale.US, "%.2f", currentRate).replace(".00", "") else "350")
    }

    var customUsdInput by remember { mutableStateOf("100") }
    var isReverseConversion by remember { mutableStateOf(false) } // false: USD -> CUP, true: CUP -> USD

    val activeRate = rateInputStr.toDoubleOrNull() ?: currentRate.takeIf { it > 0 } ?: 350.0

    val bgColor = if (isDark) Color(0xFF182238) else Color.White
    val innerSurface = if (isDark) Color(0xFF101626) else Color(0xFFF8FAFC)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDark) Color(0xFF2E384D) else Color(0xFFE2E8F0)
    val accentColor = Color(0xFF059669) // Emerald Green
    val badgeBg = if (isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = "EN VIVO",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Input: Tasa Actual USD/CUP
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = innerSurface,
                border = BorderStroke(1.dp, borderColor)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tasa de Cambio Oficial (1 USD)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Introduce el valor de 1 USD en CUP",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }

                        // Input Box
                        OutlinedTextField(
                            value = rateInputStr,
                            onValueChange = {
                                rateInputStr = it
                                it.toDoubleOrNull()?.let { newRate ->
                                    if (newRate > 0) {
                                        onRateChanged(newRate)
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = {
                                Text(
                                    text = "CUP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = accentColor,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = borderColor,
                                focusedContainerColor = if (isDark) Color(0xFF161E31) else Color.White,
                                unfocusedContainerColor = if (isDark) Color(0xFF161E31) else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(135.dp)
                        )
                    }

                    // Quick Step Buttons (-10, -5, +5, +10)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(-10.0, -5.0, 5.0, 10.0).forEach { delta ->
                            val isPos = delta > 0
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val cur = rateInputStr.toDoubleOrNull() ?: 350.0
                                        val nextVal = maxOf(1.0, cur + delta)
                                        val formatted = String.format(Locale.US, "%.2f", nextVal).replace(".00", "")
                                        rateInputStr = formatted
                                        onRateChanged(nextVal)
                                    },
                                color = if (isPos) accentColor.copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, if (isPos) accentColor.copy(alpha = 0.3f) else Color(0xFFEF4444).copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isPos) "+${delta.toInt()}" else "${delta.toInt()}",
                                        color = if (isPos) accentColor else Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Live Converter Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF14243B) else Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (!isReverseConversion) "Conversor Rápido: USD ➔ CUP" else "Conversor Rápido: CUP ➔ USD",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        IconButton(
                            onClick = { isReverseConversion = !isReverseConversion },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Invertir dirección",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Amount input
                        OutlinedTextField(
                            value = customUsdInput,
                            onValueChange = { customUsdInput = it },
                            label = {
                                Text(
                                    if (!isReverseConversion) "Monto USD" else "Monto CUP",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = borderColor,
                                focusedContainerColor = if (isDark) Color(0xFF101626) else Color.White,
                                unfocusedContainerColor = if (isDark) Color(0xFF101626) else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        // Result Box
                        val inputNum = customUsdInput.toDoubleOrNull() ?: 0.0
                        val convertedVal = if (!isReverseConversion) {
                            inputNum * activeRate
                        } else {
                            if (activeRate > 0) inputNum / activeRate else 0.0
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val clipText = if (!isReverseConversion) {
                                        "${String.format(Locale.US, "%,.2f", convertedVal)} CUP"
                                    } else {
                                        "$${String.format(Locale.US, "%,.2f", convertedVal)} USD"
                                    }
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Conversión", clipText))
                                    Toast.makeText(context, "Copiado: $clipText", Toast.LENGTH_SHORT).show()
                                },
                            color = accentColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (!isReverseConversion) "Resultado en CUP:" else "Resultado en USD:",
                                    fontSize = 10.sp,
                                    color = textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (!isReverseConversion) {
                                        "${String.format(Locale.US, "%,.2f", convertedVal)} CUP"
                                    } else {
                                        "$${String.format(Locale.US, "%,.2f", convertedVal)} USD"
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Reference Exchange Rate Table (Tabla de Equivalencias en Vivo)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tabla de Equivalencias en Vivo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "1 USD = ${String.format(Locale.US, "%,.2f", activeRate)} CUP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = innerSurface,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) Color(0xFF161E31) else Color(0xFFF1F5F9))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monto USD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Tasa",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.8f)
                            )
                            Text(
                                text = "Equivalente CUP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        HorizontalDivider(thickness = 1.dp, color = borderColor)

                        val referenceAmounts = listOf(1.0, 5.0, 10.0, 20.0, 50.0, 100.0, 150.0, 200.0, 300.0, 500.0, 1000.0)

                        referenceAmounts.forEachIndexed { index, usdAmount ->
                            val cupEquivalent = usdAmount * activeRate
                            val rowBg = if (index % 2 == 0) {
                                Color.Transparent
                            } else {
                                if (isDark) Color(0xFF141C2E) else Color(0xFFF8FAFC)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBg)
                                    .clickable {
                                        customUsdInput = usdAmount.toInt().toString()
                                        val clipText = "$${usdAmount.toInt()} USD = ${String.format(Locale.US, "%,.2f", cupEquivalent)} CUP (Tasa: $activeRate)"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Equivalencia", clipText))
                                        Toast.makeText(context, "Copiado: $clipText", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$${usdAmount.toInt()} USD",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary
                                    )
                                }

                                Text(
                                    text = "${activeRate.toInt()}",
                                    fontSize = 11.sp,
                                    color = textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.8f)
                                )

                                Text(
                                    text = "${String.format(Locale.US, "%,.2f", cupEquivalent)} CUP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.2f)
                                )
                            }

                            if (index < referenceAmounts.size - 1) {
                                HorizontalDivider(thickness = 0.5.dp, color = borderColor.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Text(
                    text = "💡 Toca cualquier fila para cargar el monto al conversor o copiarlo.",
                    fontSize = 11.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            // Save / Apply Button
            Button(
                onClick = {
                    val parsed = rateInputStr.toDoubleOrNull()
                    if (parsed != null && parsed > 0) {
                        onRateChanged(parsed)
                        Toast.makeText(context, "Tasa de cambio actualizada: 1 USD = $parsed CUP ✓", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Introduce una tasa de cambio válida", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aplicar Tasa a Todo el Sistema (1 USD = $rateInputStr CUP)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
