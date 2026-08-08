package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.GuideAssignedTour
import com.mgacreative.touros.domain.model.GuidePassengerInfo
import com.mgacreative.touros.domain.model.PickupPoint
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.GuideMobileUiState
import com.mgacreative.touros.ui.viewmodel.GuideMobileViewModel

/**
 * Rehber Mobil Görev Ekranı — TourOS 0.3
 *
 * Mobilde optimize edilmiş, büyük dokunma alanlı (touch target ≥ 56dp) kart listesi.
 * Tur adı, tarih, yolcu sayısı doğrudan görünür.
 * Karta dokununca AnimatedVisibility ile detaylı yolcu yoklaması ve araç bilgileri açılır.
 */
@Composable
fun GuideMobileScreen(
    viewModel: GuideMobileViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Rehber Mobil Görevlerim",
                subtitle = "Saha tur operasyonu ve yoklama takibi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is GuideMobileUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is GuideMobileUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is GuideMobileUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(TourOSSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    // ── Rehber Profil & Tur Özet Kartı ─────────────────────────
                    item {
                        GuideProfileHeaderCard(
                            guideName = state.guideName,
                            licenseNumber = state.licenseNumber,
                            totalToursCount = state.assignedTours.size
                        )
                    }

                    // ── Sekme Seçici ──────────────────────────────────────────
                    item {
                        PrimaryTabRow(
                            selectedTabIndex = state.activeTab,
                            containerColor = TourOSColors.Background,
                            contentColor = TourOSColors.Primary
                        ) {
                            Tab(
                                selected = state.activeTab == 0,
                                onClick = { viewModel.selectTab(0) },
                                text = { Text("🗺️ Atanmış Turlarım", style = TourOSTypography.Label) }
                            )
                            Tab(
                                selected = state.activeTab == 1,
                                onClick = { viewModel.selectTab(1) },
                                text = { Text("👥 Yolcu Yoklaması", style = TourOSTypography.Label) }
                            )
                            Tab(
                                selected = state.activeTab == 2,
                                onClick = { viewModel.selectTab(2) },
                                text = { Text("📍 Pickup Durakları", style = TourOSTypography.Label) }
                            )
                        }
                    }

                    // ── Sekme İçerikleri ─────────────────────────────────────
                    when (state.activeTab) {
                        0 -> {
                            if (state.assignedTours.isEmpty()) {
                                item {
                                    EmptyStateBox("Atanmış aktif tur göreviniz bulunmamaktadır.")
                                }
                            } else {
                                items(state.assignedTours) { tour ->
                                    val isSelected = state.selectedTour?.departureId == tour.departureId
                                    LargeTouchableTourCard(
                                        tour = tour,
                                        isExpanded = isSelected,
                                        onCardClick = { viewModel.selectTour(tour) },
                                        onToggleCheckIn = { viewModel.toggleCheckIn(it) }
                                    )
                                }
                            }
                        }
                        1 -> {
                            item {
                                PassengerCheckInSection(
                                    selectedTour = state.selectedTour,
                                    searchQuery = state.passengerSearchQuery,
                                    onSearchChange = { viewModel.updatePassengerSearch(it) },
                                    onToggleCheckIn = { viewModel.toggleCheckIn(it) }
                                )
                            }
                        }
                        2 -> {
                            item {
                                PickupStopsSection(selectedTour = state.selectedTour)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Rehber Profil Header Kartı ──────────────────────────────────────────────

@Composable
private fun GuideProfileHeaderCard(
    guideName: String,
    licenseNumber: String,
    totalToursCount: Int
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.large
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "🚩 $guideName",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                )
                Text(
                    "Kokart No: $licenseNumber (TUREB Ülke Rehberi)",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }
            TourOSStatusBadge(
                text = "🚩 $totalToursCount Tur Görevi",
                backgroundColor = TourOSColors.SecondaryContainer,
                textColor = TourOSColors.Secondary
            )
        }
    }
}

// ─── Büyük Dokunma Alanlı Dokunulabilir Tur Kartı ──────────────────────────────

@Composable
private fun LargeTouchableTourCard(
    tour: GuideAssignedTour,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    onToggleCheckIn: (String) -> Unit
) {
    val isCurrentActive = tour.status == "active"
    val checkedPax = tour.passengers.count { it.isCheckIn }

    val borderColor = if (isExpanded) TourOSColors.Primary else TourOSColors.Border
    val cardBg = if (isExpanded) TourOSColors.PrimaryContainer.copy(alpha = 0.3f) else TourOSColors.Background

    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .border(
                width = if (isExpanded) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
            )
            .clickable { onCardClick() },
        backgroundColor = cardBg,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            // Header Satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TourOSStatusBadge(
                    text = if (isCurrentActive) "🟢 Aktif Tur Görevi" else "🗓️ Gelecek Tur",
                    backgroundColor = if (isCurrentActive) TourOSColors.SuccessContainer else TourOSColors.Surface,
                    textColor = if (isCurrentActive) TourOSColors.Success else TourOSColors.TextSecondary
                )

                Text(
                    "📅 ${tour.departureDate} → ${tour.returnDate}",
                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                )
            }

            // 1. TUR ADI (Büyük Tipografi)
            Text(
                tour.tourTitle,
                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
            )

            // 2. YOLCU SAYISI & ÖZET (Büyük Rozet Alanı)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.Surface)
                    .padding(TourOSSpacing.medium),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👥 Yolcu Sayısı", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "${tour.totalPaxCount} Pax",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚌 Araç Plaka", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        tour.assignedVehiclePlate,
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👨‍✈️ Kaptan", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        tour.assignedDriverName,
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                }
            }

            // Tıklama İpucu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Yoklama: $checkedPax / ${tour.totalPaxCount} Otobüste",
                    style = TourOSTypography.Caption.copy(
                        color = if (checkedPax == tour.totalPaxCount && tour.totalPaxCount > 0) TourOSColors.Success else TourOSColors.TextSecondary
                    )
                )
                Text(
                    text = if (isExpanded) "▲ Detayı Kapat" else "▼ Dokun & Detayları Aç",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                )
            }

            // 3. KARTA DOKUNUNCA AÇILAN GENİŞ DETAY ALANI
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    HorizontalDivider(color = TourOSColors.Divider)

                    Text(
                        "👥 Yolcu Yoklama Listesi",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )

                    tour.passengers.forEach { passenger ->
                        PassengerRowItem(
                            passenger = passenger,
                            onToggleCheckIn = { onToggleCheckIn(passenger.passengerId) }
                        )
                    }

                    if (tour.pickups.isNotEmpty()) {
                        HorizontalDivider(color = TourOSColors.Divider)
                        Text(
                            "📍 Pickup Noktaları (${tour.pickups.size})",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                        )
                        tour.pickups.forEach { pickup ->
                            MiniPickupStopItem(pickup = pickup)
                        }
                    }
                }
            }
        }
    }
}

// ─── Yolcu Yoklama Satırı (Büyük Dokunma Alanlı Checkbox) ────────────────────

@Composable
private fun PassengerRowItem(
    passenger: GuidePassengerInfo,
    onToggleCheckIn: () -> Unit
) {
    val bg = if (passenger.isCheckIn) TourOSColors.SuccessContainer.copy(alpha = 0.4f) else TourOSColors.Surface
    val borderCol = if (passenger.isCheckIn) TourOSColors.Success else TourOSColors.Border

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .clickable { onToggleCheckIn() }
            .padding(TourOSSpacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    passenger.fullName,
                    style = TourOSTypography.TitleMedium.copy(
                        color = if (passenger.isCheckIn) TourOSColors.Success else TourOSColors.TextPrimary
                    )
                )
                Text(
                    "🆔 Pasaport/TC: ${passenger.tcPassport ?: "—"}  ·  🏨 ${passenger.pickupHotel}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
                Text(
                    "💺 Koltuk: ${passenger.seatNumber ?: "Atanmadı"}  ·  📞 ${passenger.phone ?: "—"}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
                if (!passenger.specialNotes.isNullOrBlank()) {
                    Text(
                        "📝 Not: ${passenger.specialNotes}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                    )
                }
            }

            // Büyük Dokunma Alanlı Checkbox + Rozet
            Column(horizontalAlignment = Alignment.End) {
                Checkbox(
                    checked = passenger.isCheckIn,
                    onCheckedChange = { onToggleCheckIn() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = TourOSColors.Success,
                        uncheckedColor = TourOSColors.TextSecondary
                    )
                )
                Text(
                    text = if (passenger.isCheckIn) "✅ Otobüste" else "⏳ Bekliyor",
                    style = TourOSTypography.Caption.copy(
                        color = if (passenger.isCheckIn) TourOSColors.Success else TourOSColors.TextSecondary
                    )
                )
            }
        }
    }
}

// ─── Mini Pickup Durak Öğesi ──────────────────────────────────────────────────

@Composable
private fun MiniPickupStopItem(pickup: PickupPoint) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .background(TourOSColors.PrimaryContainer.copy(alpha = 0.3f))
            .padding(TourOSSpacing.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "⏰ ${pickup.scheduledTime}  ·  🏨 ${pickup.hotelName}",
                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                )
                Text(
                    "👤 ${pickup.passengerName}  ·  📍 ${pickup.locationName}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }
            Text(
                "👥 ${pickup.paxCount} Pax",
                style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
            )
        }
    }
}

// ─── Yolcu Yoklama Sekme İçeriği ──────────────────────────────────────────────

@Composable
private fun PassengerCheckInSection(
    selectedTour: GuideAssignedTour?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleCheckIn: (String) -> Unit
) {
    if (selectedTour == null) {
        EmptyStateBox("Lütfen yoklama listesini görmek için Turlarım sekmesinden bir tur seçin.")
        return
    }

    val passengers = selectedTour.passengers
    val filtered = if (searchQuery.isBlank()) passengers else passengers.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) || it.tcPassport?.contains(searchQuery, ignoreCase = true) == true
    }
    val checkedCount = passengers.count { it.isCheckIn }

    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TourOSTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                label = "Yolcu Ara",
                placeholder = "Ad soyad veya pasaport...",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(TourOSSpacing.small))
            TourOSStatusBadge(
                text = "Yoklama: $checkedCount / ${passengers.size}",
                backgroundColor = TourOSColors.SuccessContainer,
                textColor = TourOSColors.Success
            )
        }

        filtered.forEach { passenger ->
            PassengerRowItem(
                passenger = passenger,
                onToggleCheckIn = { onToggleCheckIn(passenger.passengerId) }
            )
        }
    }
}

// ─── Pickup Durakları Sekme İçeriği ──────────────────────────────────────────

@Composable
private fun PickupStopsSection(selectedTour: GuideAssignedTour?) {
    if (selectedTour == null || selectedTour.pickups.isEmpty()) {
        EmptyStateBox("Bu tur için tanımlı otel pickup durağı bulunamadı.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
        selectedTour.pickups.forEach { pickup ->
            MiniPickupStopItem(pickup = pickup)
        }
    }
}

@Composable
private fun EmptyStateBox(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
            textAlign = TextAlign.Center
        )
    }
}
