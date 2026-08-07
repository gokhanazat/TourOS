package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.components.SharedMapView
import com.mgacreative.touros.ui.viewmodel.SharedMapViewModel

/**
 * 4.4.3 Otel Konumları, Tur Rotaları ve Canlı Araç Konumunu Gösteren Ortak Harita Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMapScreen(
    viewModel: SharedMapViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗺️ Ortak Harita (Expect/Actual)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Layer Selection Chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "ALL" to "🌐 Tümü",
                    "HOTELS" to "🏨 Oteller",
                    "ROUTES" to "🚩 Rotalar",
                    "LIVE_VEHICLE" to "🚐 Canlı Araç"
                ).forEach { (code, label) ->
                    FilterChip(
                        selected = state.selectedLayer == code,
                        onClick = { viewModel.selectLayer(code) },
                        label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }

            // Expect/Actual SharedMapView Composable Bridge Render Box
            SharedMapView(
                modifier = Modifier.fillMaxWidth(),
                points = state.mapPoints,
                selectedLayer = state.selectedLayer
            )

            Text("📍 Haritada Gösterilen Konum Detayları (${state.mapPoints.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.mapPoints) { pt ->
                    val badgeColor = when (pt.category) {
                        "HOTEL" -> Color(0xFF2563EB)
                        "ROUTE_STOP" -> Color(0xFFD97706)
                        "VEHICLE" -> Color(0xFF15803D)
                        else -> Color.Gray
                    }
                    val badgeEmoji = when (pt.category) {
                        "HOTEL" -> "🏨 OTEL"
                        "ROUTE_STOP" -> "🚩 TUR DURAĞI"
                        "VEHICLE" -> "🚐 CANLI ARAÇ"
                        else -> "📍 NOKTA"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(shape = RoundedCornerShape(4.dp), color = badgeColor.copy(alpha = 0.15f)) {
                                    Text(badgeEmoji, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Text(pt.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(pt.snippet, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "${pt.latitude.toString().take(6)}, ${pt.longitude.toString().take(6)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
