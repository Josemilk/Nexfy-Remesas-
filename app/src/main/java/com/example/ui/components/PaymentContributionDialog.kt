package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Pantalla de Pago y Aportes de NexFy.
 * Implementa fielmente la interfaz de aportes con copiado rápido al portapapeles
 * y redirección nativa al navegador para el enlace de PayPal.
 */
@Composable
fun PaymentContributionDialog(
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val context = LocalContext.current

    // Paleta de colores cálida y elegante correspondiente al diseño de Aportes
    val screenBgColor = Color(0xFFFBF8F0) // Fondo crema cálido
    val cardBgColor = Color(0xFFFFFDF8) // Fondo tarjetas crema suave
    val cardBorderColor = Color(0xFFEFE8DA) // Borde suave
    val titleColor = Color(0xFF18181B) // Negro tipográfico display
    val subtitleColor = Color(0xFF3F3F46) // Gris oscuro elegante
    val textPrimary = Color(0xFF18181B)
    val textSecondary = Color(0xFF27272A)
    val linkColor = Color(0xFF1E293B)
    val copyIconTint = Color(0xFF27272A)

    fun copyToClipboard(label: String, value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, value)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }

    fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir el navegador", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBgColor)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Botón superior de cerrar
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .testTag("btn_close_aportes")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = titleColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Ilustración de Cabecera: Mano sosteniendo corazón
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color(0x15000000),
                            spotColor = Color(0x15000000)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFFF6EFE3))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = Color(0xFF1E1E1E), modifier = Modifier.size(54.dp))
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Título Display
                Text(
                    text = "APORTES",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtítulo
                Text(
                    text = "Apoya con tu aporte — gracias por tu colaboración",
                    fontSize = 13.5.sp,
                    color = subtitleColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Lista de Métodos / Direcciones
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Transfermóvil Bandec CUP
                    ContributionItemCard(
                        title = "Transfermóvil Bandec CUP",
                        value = "9224-0699-9736-6790",
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor,
                        textPrimary = textPrimary,
                        copyIconTint = copyIconTint,
                        testTagPrefix = "bandec",
                        onCopy = { copyToClipboard("Transfermóvil Bandec CUP", "9224-0699-9736-6790") },
                        leadingIcon = {
                            BankVectorIcon(modifier = Modifier.size(28.dp), color = textPrimary)
                        }
                    )

                    // 2. Monedero MiTransfer o ENZONA
                    ContributionItemCard(
                        title = "Monedero MiTransfer o ENZONA",
                        value = "51076491",
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor,
                        textPrimary = textPrimary,
                        copyIconTint = copyIconTint,
                        testTagPrefix = "enzona",
                        onCopy = { copyToClipboard("Monedero MiTransfer o ENZONA", "51076491") },
                        leadingIcon = {
                            WalletVectorIcon(modifier = Modifier.size(28.dp), color = textPrimary)
                        }
                    )

                    // 3. Dirección (BTC) Bitcoin
                    ContributionItemCard(
                        title = "Dirección (BTC) Bitcoin",
                        value = "bc1qcsa8z36r0fe9f4at7xkc04x7pqzjx2fp09ke95",
                        isMonospace = true,
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor,
                        textPrimary = textPrimary,
                        copyIconTint = copyIconTint,
                        testTagPrefix = "btc",
                        onCopy = { copyToClipboard("Dirección BTC", "bc1qcsa8z36r0fe9f4at7xkc04x7pqzjx2fp09ke95") },
                        leadingIcon = {
                            Icon(painter = painterResource(id = R.drawable.ic_bitcoin), contentDescription = "Bitcoin", modifier = Modifier.size(28.dp), tint = Color.Unspecified)
                        }
                    )

                    // 4. Dirección (ETH) Ethereum
                    ContributionItemCard(
                        title = "Dirección (ETH) Ethereum",
                        value = "0xAD97b69C7Db9d582ce5c7147317E52C4dF9CBc86",
                        isMonospace = true,
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor,
                        textPrimary = textPrimary,
                        copyIconTint = copyIconTint,
                        testTagPrefix = "eth",
                        onCopy = { copyToClipboard("Dirección ETH", "0xAD97b69C7Db9d582ce5c7147317E52C4dF9CBc86") },
                        leadingIcon = {
                            Icon(painter = painterResource(id = R.drawable.ic_ethereum), contentDescription = "Ethereum", modifier = Modifier.size(28.dp), tint = Color.Unspecified)
                        }
                    )

                    // 5. Dirección (USDT) ERC-20
                    ContributionItemCard(
                        title = "Dirección (USDT) ERC-20",
                        value = "0xAD97b69C7Db9d582ce5c7147317E52C4dF9CBc86",
                        isMonospace = true,
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor,
                        textPrimary = textPrimary,
                        copyIconTint = copyIconTint,
                        testTagPrefix = "usdt_erc20",
                        onCopy = { copyToClipboard("Dirección USDT ERC-20", "0xAD97b69C7Db9d582ce5c7147317E52C4dF9CBc86") },
                        leadingIcon = {
                            Icon(painter = painterResource(id = R.drawable.ic_tether), contentDescription = "USDT", modifier = Modifier.size(28.dp), tint = Color.Unspecified)
                        }
                    )

                    // 6. Dirección (USDT) TRC-20
                    ContributionItemCard(
                        title = "Dirección (USDT) TRC-20",
                        value = "TXex7WJv2Vyw8aNZevAP3dKzGVZEwgsjCQ",
                        isMonospace = true,
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor,
                        textPrimary = textPrimary,
                        copyIconTint = copyIconTint,
                        testTagPrefix = "usdt_trc20",
                        onCopy = { copyToClipboard("Dirección USDT TRC-20", "TXex7WJv2Vyw8aNZevAP3dKzGVZEwgsjCQ") },
                        leadingIcon = {
                            Icon(painter = painterResource(id = R.drawable.ic_tether), contentDescription = "USDT", modifier = Modifier.size(28.dp), tint = Color.Unspecified)
                        }
                    )

                    // 7. Dirección de QR de Paypal (Clickable URL)
                    val paypalUrl = "https://www.paypal.com/qrcodes/managed/9fd7bc32-09ac-4f24-8bf1-77bab64b7b0a?utm_source=consweb_more"
                    ContributionItemCard(
                        title = "Dirección de QR de Paypal",
                        value = paypalUrl,
                        isLink = true,
                        onLinkClick = { openBrowser(paypalUrl) },
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor,
                        textPrimary = textPrimary,
                        copyIconTint = copyIconTint,
                        testTagPrefix = "paypal",
                        onCopy = { copyToClipboard("Enlace PayPal", paypalUrl) },
                        leadingIcon = {
                            Icon(painter = painterResource(id = R.drawable.ic_paypal), contentDescription = "PayPal", modifier = Modifier.size(28.dp), tint = Color.Unspecified)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Mensaje informativo inferior
                Text(
                    text = "Toca el ícono para copiar cada dirección · Asegúrate de verificar la red antes de enviar",
                    fontSize = 11.5.sp,
                    color = Color(0xFF52525B),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Tarjeta individual para cada método de aporte con icono a la izquierda,
 * textos descriptivos y botón de copiar a la derecha.
 */
@Composable
private fun ContributionItemCard(
    title: String,
    value: String,
    cardBgColor: Color,
    cardBorderColor: Color,
    textPrimary: Color,
    copyIconTint: Color,
    testTagPrefix: String,
    onCopy: () -> Unit,
    leadingIcon: @Composable () -> Unit,
    isMonospace: Boolean = false,
    isLink: Boolean = false,
    onLinkClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.5.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x0C000000),
                spotColor = Color(0x0C000000)
            )
            .testTag("card_contribution_$testTagPrefix"),
        shape = RoundedCornerShape(18.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono Izquierdo
            Box(
                modifier = Modifier
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                leadingIcon()
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Textos (Título y Valor)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (isLink && onLinkClick != null) {
                    Text(
                        text = value,
                        fontSize = 11.5.sp,
                        color = Color(0xFF1D4ED8),
                        textDecoration = TextDecoration.Underline,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .clickable(onClick = onLinkClick)
                            .testTag("link_${testTagPrefix}")
                    )
                } else {
                    Text(
                        text = value,
                        fontSize = if (isMonospace) 12.sp else 14.sp,
                        fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                        fontWeight = if (isMonospace) FontWeight.Normal else FontWeight.Medium,
                        color = textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Botón de Copiar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onCopy)
                    .testTag("btn_copy_$testTagPrefix"),
                contentAlignment = Alignment.Center
            ) {
                CopyOutlineVectorIcon(
                    modifier = Modifier.size(22.dp),
                    color = copyIconTint
                )
            }
        }
    }
}

// -------------------------------------------------------------
// ICONOS VECTORIALES NATIVOS DISEÑADOS A MEDIDA PARA ESTA VISTA
// -------------------------------------------------------------

/**
 * Ilustración minimalista, nítida y perfectamente delineada de una mano abierta sosteniendo un corazón.
 */
@Composable
private fun HandHeartIcon(modifier: Modifier = Modifier, strokeColor: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = 2.2.dp.toPx()

        // 1. Corazón en la parte superior (Perfectamente simétrico y nítido)
        val heartPath = Path().apply {
            val cx = w * 0.50f
            val topDipY = h * 0.16f
            val lobeTopY = h * 0.08f
            val lobeLeftX = w * 0.32f
            val lobeRightX = w * 0.68f
            val tipY = h * 0.46f

            moveTo(cx, topDipY)
            // Lóbulo izquierdo
            cubicTo(cx - w * 0.08f, lobeTopY, lobeLeftX, lobeTopY, lobeLeftX, topDipY + (tipY - topDipY) * 0.35f)
            cubicTo(lobeLeftX, tipY * 0.85f, cx - w * 0.05f, tipY * 0.95f, cx, tipY)
            // Lóbulo derecho
            cubicTo(cx + w * 0.05f, tipY * 0.95f, lobeRightX, tipY * 0.85f, lobeRightX, topDipY + (tipY - topDipY) * 0.35f)
            cubicTo(lobeRightX, lobeTopY, cx + w * 0.08f, lobeTopY, cx, topDipY)
            close()
        }

        // Relleno sutil del corazón para mayor nitidez
        drawPath(
            path = heartPath,
            color = strokeColor.copy(alpha = 0.08f)
        )
        drawPath(
            path = heartPath,
            color = strokeColor,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 2. Mano abierta estilizada sosteniendo suavemente la base del corazón
        val handOutlinePath = Path().apply {
            // Muñeca izquierda
            moveTo(w * 0.15f, h * 0.72f)
            // Palma curvada horizontalmente
            cubicTo(w * 0.28f, h * 0.62f, w * 0.42f, h * 0.60f, w * 0.58f, h * 0.58f)
            // Dedos extendidos hacia la derecha y arriba
            cubicTo(w * 0.72f, h * 0.56f, w * 0.85f, h * 0.62f, w * 0.85f, h * 0.70f)
            // Yema del dedo y retorno
            cubicTo(w * 0.85f, h * 0.78f, w * 0.72f, h * 0.82f, w * 0.55f, h * 0.80f)
            // Retorno por la base de la mano hacia la muñeca inferior
            cubicTo(w * 0.38f, h * 0.78f, w * 0.25f, h * 0.84f, w * 0.15f, h * 0.82f)
        }

        drawPath(
            path = handOutlinePath,
            color = strokeColor,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Detalle de pliegue de pulgar
        val thumbLine = Path().apply {
            moveTo(w * 0.38f, h * 0.62f)
            cubicTo(w * 0.44f, h * 0.54f, w * 0.52f, h * 0.54f, w * 0.58f, h * 0.58f)
        }
        drawPath(
            path = thumbLine,
            color = strokeColor,
            style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Icono de Banco / Entidad financiera (Columnas con tejado triangular).
 */
@Composable
private fun BankVectorIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = 1.8.dp.toPx()

        // Techo triangular (Pedimento)
        val roofPath = Path().apply {
            moveTo(w * 0.1f, h * 0.36f)
            lineTo(w * 0.5f, h * 0.12f)
            lineTo(w * 0.9f, h * 0.36f)
            close()
        }
        drawPath(roofPath, color = color, style = Stroke(width = strokeW, join = StrokeJoin.Round))

        // Viga bajo el techo
        drawLine(color, Offset(w * 0.12f, h * 0.42f), Offset(w * 0.88f, h * 0.42f), strokeWidth = strokeW)

        // 4 Columnas
        val colY1 = h * 0.45f
        val colY2 = h * 0.76f
        val colXs = listOf(w * 0.22f, w * 0.40f, w * 0.60f, w * 0.78f)
        colXs.forEach { cx ->
            drawLine(color, Offset(cx, colY1), Offset(cx, colY2), strokeWidth = strokeW + 0.5f, cap = StrokeCap.Square)
        }

        // Base escalonada
        drawLine(color, Offset(w * 0.10f, h * 0.80f), Offset(w * 0.90f, h * 0.80f), strokeWidth = strokeW)
        drawLine(color, Offset(w * 0.05f, h * 0.88f), Offset(w * 0.95f, h * 0.88f), strokeWidth = strokeW)
    }
}

/**
 * Icono de Billetera / Monedero.
 */
@Composable
private fun WalletVectorIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = 1.8.dp.toPx()

        // Cuerpo de la cartera
        val walletPath = Path().apply {
            moveTo(w * 0.12f, h * 0.28f)
            lineTo(w * 0.88f, h * 0.28f)
            cubicTo(w * 0.94f, h * 0.28f, w * 0.94f, h * 0.34f, w * 0.94f, h * 0.38f)
            lineTo(w * 0.94f, h * 0.76f)
            cubicTo(w * 0.94f, h * 0.82f, w * 0.88f, h * 0.82f, w * 0.84f, h * 0.82f)
            lineTo(w * 0.20f, h * 0.82f)
            cubicTo(w * 0.12f, h * 0.82f, w * 0.10f, h * 0.76f, w * 0.10f, h * 0.70f)
            lineTo(w * 0.10f, h * 0.36f)
            cubicTo(w * 0.10f, h * 0.28f, w * 0.16f, h * 0.28f, w * 0.20f, h * 0.28f)
        }
        drawPath(walletPath, color = color, style = Stroke(width = strokeW, join = StrokeJoin.Round))

        // Broche / solapa lateral
        val flapPath = Path().apply {
            moveTo(w * 0.68f, h * 0.44f)
            lineTo(w * 0.92f, h * 0.44f)
            cubicTo(w * 0.98f, h * 0.44f, w * 0.98f, h * 0.64f, w * 0.92f, h * 0.64f)
            lineTo(w * 0.68f, h * 0.64f)
            close()
        }
        drawPath(flapPath, color = color, style = Stroke(width = strokeW, join = StrokeJoin.Round))

        // Punto del broche
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(w * 0.86f, h * 0.54f))
    }
}

/**
 * Icono de Bitcoin (₿ estilizado).
 */
@Composable
private fun BitcoinSymbolIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "₿",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Icono de Ethereum (Diamante octaédrico oficial).
 */
@Composable
private fun EthereumSymbolIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = 1.6.dp.toPx()

        val cx = w * 0.5f
        val topY = h * 0.10f
        val midY = h * 0.52f
        val botY = h * 0.90f
        val leftX = w * 0.20f
        val rightX = w * 0.80f
        val midCrackY = h * 0.60f

        // Pirámide superior
        val topDiamond = Path().apply {
            moveTo(cx, topY)
            lineTo(rightX, midY)
            lineTo(cx, midCrackY)
            lineTo(leftX, midY)
            close()
        }
        drawPath(topDiamond, color = color, style = Stroke(width = strokeW, join = StrokeJoin.Round))

        // Línea divisoria central superior
        drawLine(color, Offset(cx, topY), Offset(cx, midCrackY), strokeWidth = strokeW)

        // Pirámide inferior
        val botDiamond = Path().apply {
            moveTo(leftX, midY + h * 0.08f)
            lineTo(cx, botY)
            lineTo(rightX, midY + h * 0.08f)
            lineTo(cx, midCrackY + h * 0.04f)
            close()
        }
        drawPath(botDiamond, color = color, style = Stroke(width = strokeW, join = StrokeJoin.Round))

        // Línea divisoria central inferior
        drawLine(color, Offset(cx, midCrackY + h * 0.04f), Offset(cx, botY), strokeWidth = strokeW)
    }
}

/**
 * Escudo USDT con etiquetas ERC-20 / TRC-20.
 */
@Composable
private fun UsdtShieldIcon(
    label: String,
    subLabel: String?,
    modifier: Modifier = Modifier,
    color: Color = Color.Black
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeW = 1.8.dp.toPx()

            // Silueta de escudo
            val shieldPath = Path().apply {
                moveTo(w * 0.14f, h * 0.18f)
                lineTo(w * 0.86f, h * 0.18f)
                cubicTo(w * 0.86f, h * 0.52f, w * 0.70f, h * 0.80f, w * 0.50f, h * 0.90f)
                cubicTo(w * 0.30f, h * 0.80f, w * 0.14f, h * 0.52f, w * 0.14f, h * 0.18f)
                close()
            }
            drawPath(shieldPath, color = color, style = Stroke(width = strokeW, join = StrokeJoin.Round))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = if (subLabel != null) 0.dp else 2.dp)
        ) {
            Text(
                text = label,
                fontSize = if (subLabel != null) 7.5.sp else 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                textAlign = TextAlign.Center,
                lineHeight = 9.sp
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    textAlign = TextAlign.Center,
                    lineHeight = 7.5.sp
                )
            }
        }
    }
}

/**
 * Icono de PayPal (Letra P en cursiva gruesa).
 */
@Composable
private fun PaypalSymbolIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "P",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Serif,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Icono de copiar contorneado elegante con bordes redondeados.
 */
@Composable
private fun CopyOutlineVectorIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = 1.8.dp.toPx()
        val corner = 3.dp.toPx()

        // Rectángulo trasero superior izquierdo
        val backPath = Path().apply {
            moveTo(w * 0.38f, h * 0.14f)
            lineTo(w * 0.72f, h * 0.14f)
            cubicTo(w * 0.82f, h * 0.14f, w * 0.82f, h * 0.20f, w * 0.82f, h * 0.24f)
            moveTo(w * 0.24f, h * 0.68f)
            lineTo(w * 0.24f, h * 0.24f)
            cubicTo(w * 0.24f, h * 0.14f, w * 0.32f, h * 0.14f, w * 0.38f, h * 0.14f)
        }
        drawPath(backPath, color = color, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Rectángulo delantero principal
        val frontPath = Path().apply {
            moveTo(w * 0.42f, h * 0.32f)
            lineTo(w * 0.84f, h * 0.32f)
            cubicTo(w * 0.90f, h * 0.32f, w * 0.90f, h * 0.38f, w * 0.90f, h * 0.42f)
            lineTo(w * 0.90f, h * 0.84f)
            cubicTo(w * 0.90f, h * 0.90f, w * 0.84f, h * 0.90f, w * 0.80f, h * 0.90f)
            lineTo(w * 0.42f, h * 0.90f)
            cubicTo(w * 0.36f, h * 0.90f, w * 0.36f, h * 0.84f, w * 0.36f, h * 0.80f)
            lineTo(w * 0.36f, h * 0.42f)
            cubicTo(w * 0.36f, h * 0.32f, w * 0.42f, h * 0.32f, w * 0.42f, h * 0.32f)
            close()
        }
        drawPath(frontPath, color = color, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
