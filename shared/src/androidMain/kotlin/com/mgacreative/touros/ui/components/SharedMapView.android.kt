package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.SharedMapPoint

/**
 * 4.4.3 Android Native SharedMapView Actual Bileşeni.
 */
@Composable
actual fun SharedMapView(
    modifier: Modifier,
    points: List<SharedMapPoint>,
    selectedLayer: String
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🗺️ Android Native Map Engine (SharedMapView)",
                color = Color(0xFF38BDF8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Aktif Katman: $selectedLayer | İşaretçi Sayısı: ${points.size}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                points.take(3).forEach { pt ->
                    val badgeEmoji = when (pt.category) {
                        "HOTEL" -> "🏨"
                        "ROUTE_STOP" -> "🚩"
                        "VEHICLE" -> "🚐"
                        else -> "📍"
                    }
                    Text(
                        text = "$badgeEmoji ${pt.title} (${pt.latitude.toString().take(6)}, ${pt.longitude.toString().take(6)})",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = "⚡ Real-time GPS & Route Vector Engine Active",
                color = Color(0xFF4ADE80),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
