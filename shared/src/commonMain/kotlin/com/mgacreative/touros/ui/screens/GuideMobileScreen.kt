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
import com.mgacreative.touros.domain.model.GuideAssignedTour
import com.mgacreative.touros.domain.model.GuidePassengerInfo
import com.mgacreative.touros.domain.model.PickupPoint
import com.mgacreative.touros.ui.viewmodel.GuideMobileUiState
import com.mgacreative.touros.ui.viewmodel.GuideMobileViewModel

/**
 * 2.5.3 Rehber Mobil Tur, Yolcu Listesi ve Pickup Arayüzü Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideMobileScreen(
    viewModel: GuideMobileViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚩 Rehber Mobil Operasyon", fontWeight = FontWeight.Bold) },
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
            when (val state = uiState) {
                is GuideMobileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is GuideMobileUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is GuideMobileUiState.Success -> {
                    // 1. Rehber Profil Kartı
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
                                Text("🚩 ${state.guideName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Kokart No: ${state.licenseNumber} (TUREB Ülke Rehberi)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "${state.assignedTours.size} Atanmış Tur",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 2. Sekmeler (0: Turlarım, 1: Yolcu Listesi, 2: Pickup Noktaları)
                    PrimaryTabRow(selectedTabIndex = state.activeTab) {
                        Tab(
                            selected = state.activeTab == 0,
                            onClick = { viewModel.selectTab(0) },
                            text = { Text("🗺️ Turlarım", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = state.activeTab == 1,
                            onClick = { viewModel.selectTab(1) },
                            text = { Text("👥 Yolcu & Yoklama", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = state.activeTab == 2,
                            onClick = { viewModel.selectTab(2) },
                            text = { Text("📍 Pickup Noktaları", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    // Sekme İçerikleri
                    when (state.activeTab) {
                        0 -> AssignedToursTabContent(
                            tours = state.assignedTours,
                            selectedTour = state.selectedTour,
                            onSelectTour = { viewModel.selectTour(it) }
                        )
                        1 -> PassengerListTabContent(
                            selectedTour = state.selectedTour,
                            searchQuery = state.passengerSearchQuery,
                            onSearchChange = { viewModel.updatePassengerSearch(it) },
                            onToggleCheckIn = { viewModel.toggleCheckIn(it) }
                        )
                        2 -> PickupStopsTabContent(
                            selectedTour = state.selectedTour
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssignedToursTabContent(
    tours: List<GuideAssignedTour>,
    selectedTour: GuideAssignedTour?,
    onSelectTour: (GuideAssignedTour) -> Unit
) {
    if (tours.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Atanmış turunuz bulunmamaktadır.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(tours) { tour ->
                val isSelected = selectedTour?.departureId == tour.departureId
                val isCurrentActive = tour.status == "active"

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelectTour(tour) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCurrentActive) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = if (isCurrentActive) "🟢 Aktif Tur Görevi" else "🗓️ Gelecek Tur",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentActive) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text("📅 ${tour.departureDate} - ${tour.returnDate}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Text(tour.tourTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("🚌 Araç / Plaka", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(tour.assignedVehiclePlate, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("👨‍✈️ Kaptan Şoför", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(tour.assignedDriverName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("👥 Yolcu", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${tour.totalPaxCount} Pax", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PassengerListTabContent(
    selectedTour: GuideAssignedTour?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleCheckIn: (String) -> Unit
) {
    if (selectedTour == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Lütfen seyahat listesini görmek için bir tur seçin.")
        }
        return
    }

    val passengers = selectedTour.passengers
    val filteredPassengers = if (searchQuery.isBlank()) passengers else passengers.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) || it.tcPassport?.contains(searchQuery, ignoreCase = true) == true
    }

    val checkedCount = passengers.count { it.isCheckIn }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Yolcu Adı veya Pasaport Ara...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = "Yoklama: $checkedCount/${passengers.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(filteredPassengers) { passenger ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (passenger.isCheckIn) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(passenger.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("🆔 Pasaport: ${passenger.tcPassport ?: "-"} | 🏨 ${passenger.pickupHotel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("💺 ${passenger.seatNumber ?: "Koltuk Atanmadı"} | 📞 ${passenger.phone ?: "-"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!passenger.specialNotes.isNullOrBlank()) {
                                Text("📝 Not: ${passenger.specialNotes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Checkbox(
                                checked = passenger.isCheckIn,
                                onCheckedChange = { onToggleCheckIn(passenger.passengerId) }
                            )
                            Text(
                                text = if (passenger.isCheckIn) "✅ Otobüste" else "⏳ Bekliyor",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (passenger.isCheckIn) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PickupStopsTabContent(selectedTour: GuideAssignedTour?) {
    if (selectedTour == null || selectedTour.pickups.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bu tur için tanımlı otel pickup durağı bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        items(selectedTour.pickups) { pickup ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary) {
                            Text("⏰ Saat: ${pickup.scheduledTime}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Text("👥 ${pickup.paxCount} Yolcu Alınacak", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Text("🏨 ${pickup.hotelName} ${if (!pickup.roomNumber.isNullOrBlank()) "(Oda: ${pickup.roomNumber})" else ""}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("👤 Yolcu: ${pickup.passengerName} | 📍 ${pickup.locationName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
