package com.mgacreative.touros.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path

/**
 * Vektörel Chat İkonlu Yüzen Asistan Butonu (Floating Chat FAB).
 * Web, Desktop, Android ve iOS platformlarında sağ alt köşede ergonomik ve şık olarak konumlanır.
 */
@Composable
fun TourOSHelpAssistantFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean = true,
    badgeText: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = modifier
            .scale(pulseScale)
            .shadow(12.dp, shape = CircleShape, spotColor = Color(0xFF006B5E).copy(alpha = 0.45f))
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF006B5E),
                            Color(0xFF00897B),
                            Color(0xFF1F4E5F)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Vektörel Chat Bubble Çizimi
            VectorChatIcon(modifier = Modifier.size(26.dp))

            // Küçük bilgi rozeti
            if (!badgeText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                        .background(Color(0xFFF5BE48), shape = CircleShape)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color(0xFF271900),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

/**
 * Temiz ve keskin Vektörel Chat Balonu İkonu.
 */
@Composable
private fun VectorChatIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Ana Konuşma Balonu
        val bubblePath = Path().apply {
            moveTo(w * 0.15f, h * 0.1f)
            lineTo(w * 0.85f, h * 0.1f)
            // Sağ üst kavis
            cubicTo(w * 0.95f, h * 0.1f, w * 0.95f, h * 0.2f, w * 0.95f, h * 0.25f)
            lineTo(w * 0.95f, h * 0.65f)
            // Sağ alt kavis
            cubicTo(w * 0.95f, h * 0.75f, w * 0.85f, h * 0.75f, w * 0.8f, h * 0.75f)
            // Kuyruk başlangıcı
            lineTo(w * 0.45f, h * 0.75f)
            lineTo(w * 0.2f, h * 0.95f)
            lineTo(w * 0.25f, h * 0.75f)
            lineTo(w * 0.15f, h * 0.75f)
            // Sol alt kavis
            cubicTo(w * 0.05f, h * 0.75f, w * 0.05f, h * 0.65f, w * 0.05f, h * 0.65f)
            lineTo(w * 0.05f, h * 0.25f)
            // Sol üst kavis
            cubicTo(w * 0.05f, h * 0.1f, w * 0.15f, h * 0.1f, w * 0.15f, h * 0.1f)
            close()
        }

        drawPath(
            path = bubblePath,
            color = Color.White
        )

        // Balon içindeki 3 nokta (Chat dots)
        val dotRadius = w * 0.055f
        val dotY = h * 0.42f
        val dotColor = Color(0xFF006B5E)

        drawCircle(color = dotColor, radius = dotRadius, center = Offset(w * 0.32f, dotY))
        drawCircle(color = dotColor, radius = dotRadius, center = Offset(w * 0.50f, dotY))
        drawCircle(color = dotColor, radius = dotRadius, center = Offset(w * 0.68f, dotY))
    }
}
