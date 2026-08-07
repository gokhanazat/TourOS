package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.mgacreative.touros.domain.model.PickupPoint
import com.mgacreative.touros.ui.components.GoogleMapView
import com.mgacreative.touros.ui.viewmodel.DriverPickupListViewModel
import com.mgacreative.touros.ui.viewmodel.DriverPickupUiState

/**
 * 2.4.3 Şoför Mobil Pickup Listesi ve Google Maps Entegrasyon Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverPickupListScreen(
    viewModel: DriverPickupListViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚗 Mobil Şoför Pickup & Navigasyon", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = uiState) {
                is DriverPickupUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DriverPickupUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is DriverPickupUiState.Success -> {
                    val selected = state.selectedPickup

                    // 1. Şoför & Araç Özet Kartı
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("👨‍✈️ Şoför: ${state.driverName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("🚌 Araç: ${state.vehicleInfo}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "${state.pickups.count { it.status == "picked_up" }}/${state.pickups.size} Alındı",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 2. Google Maps Entegrasyonu (Expect/Actual Köprü Bileşeni)
                    if (selected != null) {
                        GoogleMapView(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            latitude = selected.latitude,
                            longitude = selected.longitude,
                            title = "${selected.hotelName} - ${selected.passengerName}"
                        )
                    }

                    // 3. Pickup Durak Listesi
                    Text("📍 Alış Durakları & Yolcu Listesi:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(state.pickups) { pickup ->
                            PickupStopCard(
                                pickup = pickup,
                                isSelected = selected?.id == pickup.id,
                                onSelectMap = { viewModel.selectPickupForMap(pickup) },
                                onPickedUpClick = { viewModel.updateStatus(pickup.id, "picked_up") },
                                onNoShowClick = { viewModel.updateStatus(pickup.id, "no_show") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PickupStopCard(
    pickup: PickupPoint,
    isSelected: Boolean,
    onSelectMap: () -> Unit,
    onPickedUpClick: () -> Unit,
    onNoShowClick: () -> Unit
) {
    val (statusLabel, statusBg, statusFg) = when (pickup.status) {
        "picked_up" -> Triple("✅ Yolcu Alındı", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        "no_show" -> Triple("⚠️ No-Show (Gelmedi)", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        else -> Triple("⏳ Alış Bekliyor", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectMap() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "⏰ ${pickup.scheduledTime}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusFg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (isSelected) {
                    Text("📍 Haritada Odaklı", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Column {
                Text(
                    text = "🏨 ${pickup.hotelName} ${if (!pickup.roomNumber.isNullOrBlank()) "(Oda: ${pickup.roomNumber})" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "👤 ${pickup.passengerName} | 👥 ${pickup.paxCount} Pax | 📞 ${pickup.passengerPhone ?: "-"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "📍 ${pickup.locationName}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!pickup.notes.isNullOrBlank()) {
                Text("📝 Not: ${pickup.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSelectMap) {
                    Text("🗺️ Haritada Göster", fontSize = 11.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onNoShowClick,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Gelmedi", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onPickedUpClick,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("✅ Alındı", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
