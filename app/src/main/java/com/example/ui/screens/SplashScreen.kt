package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3000L) // Displays welcome splash for 3 seconds
        onTimeout()
    }

    // Soft pulsing glow transition for the NEOAPP hexagon logo
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Soft sky gradient background matching the provided image exactly
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFD6E8FB), // Soft sky blue top
            Color(0xFFE2EFFC), // Mid soft sky blue
            Color(0xFFEEF5FD)  // Light bottom gradient
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onTimeout() // Tap anywhere to skip
            }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // --- CENTER LOGO: "NexFy" ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Rounded square badge with blue-to-purple gradient
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0xFF6366F1).copy(alpha = 0.35f),
                        ambientColor = Color(0xFF2563EB).copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2580FF), // Bright blue
                                Color(0xFF6366F1), // Indigo
                                Color(0xFF8B5CF6)  // Purple
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(220f, 220f)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // White text "NexFy"
            Text(
                text = "NexFy",
                color = Color.White,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp
            )
        }

        // --- BOTTOM CENTER: "from NEOAPP" ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Small "from" label
            Text(
                text = "from",
                color = Color(0xFF334155),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // NEOAPP row: Glowing Hexagon Icon + "NEOAPP" Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hexagon with glowing purple aura
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Soft glowing aura behind the hexagon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = Color(0xFFA855F7).copy(alpha = glowAlpha),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .blur(14.dp)
                    )

                    // Hexagon drawing
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .drawWithCache {
                                val path = Path()
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val radius = size.width / 2f - 3f

                                for (i in 0 until 6) {
                                    val angle = Math.toRadians((60 * i - 30).toDouble())
                                    val x = cx + radius * Math.cos(angle).toFloat()
                                    val y = cy + radius * Math.sin(angle).toFloat()
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                path.close()

                                onDrawBehind {
                                    drawPath(
                                        path = path,
                                        color = Color(0xFFC084FC),
                                        style = Stroke(width = 3.2.dp.toPx())
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "N",
                            color = Color(0xFFC084FC),
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // "NEOAPP" styled text
                Text(
                    text = "NEOAPP",
                    color = Color(0xFFC084FC),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
