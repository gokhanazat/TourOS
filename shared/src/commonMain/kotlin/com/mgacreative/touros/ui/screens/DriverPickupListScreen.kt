package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.PickupPoint
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.DriverPickupListViewModel
import com.mgacreative.touros.ui.viewmodel.DriverPickupUiState

/**
 * Şoför Mobil Pickup Listesi & Harita Entegrasyon Ekranı — TourOS 0.3
 *
 * Mobil  : Üstte Harita sabit, Altta Sıralı Durak Listesi kaydırılabilir.
 * Masaüstü: Harita ve Sıralı Durak Listesi yan yana (Row).
 * Alınan yolcular Success renkli check (✅) ile işaretlenir.
 */
@Composable
fun DriverPickupListScreen(
    viewModel: DriverPickupListViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Pickup & Navigasyon",
                subtitle = "Sıralı yolcu toplama ve rota takibi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DriverPickupUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is DriverPickupUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is DriverPickupUiState.Success -> {
                val selected = state.selectedPickup ?: state.pickups.firstOrNull()
                val pickedUpCount = state.pickups.count { it.status == "picked_up" }

                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val isExpanded = maxWidth >= 768.dp

                    if (isExpanded) {
                        // ── MASAÜSTÜ: HARİTA VE LİSTE YAN YANA ─────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(TourOSSpacing.large),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            // SOL: Harita Alanı (Genişlik olarak büyük pay)
                            Column(
                                modifier = Modifier.weight(1.3f).fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                DriverSummaryCard(
                                    driverName = state.driverName,
                                    vehicleInfo = state.vehicleInfo,
                                    pickedUpCount = pickedUpCount,
                                    totalCount = state.pickups.size
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                        .border(1.dp, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                ) {
                                    if (selected != null) {
                                        GoogleMapView(
                                            modifier = Modifier.fillMaxSize(),
                                            latitude = selected.latitude,
                                            longitude = selected.longitude,
                                            title = "${selected.hotelName} - ${selected.passengerName}"
                                        )
                                    } else {
                                        MapPlaceholder()
                                    }
                                }
                            }

                            VerticalDivider(color = TourOSColors.Divider, thickness = 1.dp)

                            // SAĞ: Sıralı Durak Listesi
                            Column(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                            ) {
                                Text(
                                    "📍 Sıralı Pickup Durakları (${state.pickups.size})",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                )

                                PickupStopsList(
                                    pickups = state.pickups,
                                    selectedId = selected?.id,
                                    onSelectMap = { viewModel.selectPickupForMap(it) },
                                    onPickedUpClick = { viewModel.updateStatus(it.id, "picked_up") },
                                    onNoShowClick = { viewModel.updateStatus(it.id, "no_show") },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    } else {
                        // ── MOBİL: HARİTA ÜSTTE SABİT, LİSTE ALTA KAYDIRILABİLİR ─────────────
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Üst Sabit Alan: Özet + Harita
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TourOSColors.Surface)
                                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                            ) {
                                DriverSummaryCard(
                                    driverName = state.driverName,
                                    vehicleInfo = state.vehicleInfo,
                                    pickedUpCount = pickedUpCount,
                                    totalCount = state.pickups.size
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                        .border(1.dp, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                ) {
                                    if (selected != null) {
                                        GoogleMapView(
                                            modifier = Modifier.fillMaxSize(),
                                            latitude = selected.latitude,
                                            longitude = selected.longitude,
                                            title = "${selected.hotelName} - ${selected.passengerName}"
                                        )
                                    } else {
                                        MapPlaceholder()
                                    }
                                }
                            }

                            HorizontalDivider(color = TourOSColors.Divider, thickness = 1.dp)

                            // Alt Alan: Sıralı Durak Listesi (Kaydırılabilir)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                            ) {
                                Text(
                                    "📍 Durak Listesi (Alınanlar ✅ İşaretli)",
                                    style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
                                )

                                PickupStopsList(
                                    pickups = state.pickups,
                                    selectedId = selected?.id,
                                    onSelectMap = { viewModel.selectPickupForMap(it) },
                                    onPickedUpClick = { viewModel.updateStatus(it.id, "picked_up") },
                                    onNoShowClick = { viewModel.updateStatus(it.id, "no_show") },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Özet Şoför & İlerleme Kartı ─────────────────────────────────────────────

@Composable
private fun DriverSummaryCard(
    driverName: String,
    vehicleInfo: String,
    pickedUpCount: Int,
    totalCount: Int
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "👨‍✈️ Şoför: $driverName",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )
                Text(
                    "🚌 Araç: $vehicleInfo",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }
            TourOSStatusBadge(
                text = "✅ $pickedUpCount / $totalCount Alındı",
                backgroundColor = TourOSColors.SuccessContainer,
                textColor = TourOSColors.Success
            )
        }
    }
}

// ─── Durak Listesi ────────────────────────────────────────────────────────────

@Composable
private fun PickupStopsList(
    pickups: List<PickupPoint>,
    selectedId: String?,
    onSelectMap: (PickupPoint) -> Unit,
    onPickedUpClick: (PickupPoint) -> Unit,
    onNoShowClick: (PickupPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
        contentPadding = PaddingValues(bottom = TourOSSpacing.large)
    ) {
        itemsIndexed(pickups) { index, pickup ->
            val isSelected = selectedId == pickup.id
            PickupStopCard(
                orderNumber = index + 1,
                pickup = pickup,
                isSelected = isSelected,
                onSelectMap = { onSelectMap(pickup) },
                onPickedUpClick = { onPickedUpClick(pickup) },
                onNoShowClick = { onNoShowClick(pickup) }
            )
        }
    }
}

// ─── Durak Kartı (Alınanlar Success Renkli Check ile İşaretlenir) ────────────

@Composable
private fun PickupStopCard(
    orderNumber: Int,
    pickup: PickupPoint,
    isSelected: Boolean,
    onSelectMap: () -> Unit,
    onPickedUpClick: () -> Unit,
    onNoShowClick: () -> Unit
) {
    val isPickedUp = pickup.status == "picked_up"
    val isNoShow = pickup.status == "no_show"

    val cardBg = when {
        isPickedUp -> TourOSColors.SuccessContainer.copy(alpha = 0.35f)
        isNoShow -> TourOSColors.ErrorContainer.copy(alpha = 0.35f)
        isSelected -> TourOSColors.PrimaryContainer.copy(alpha = 0.4f)
        else -> TourOSColors.Background
    }

    val borderColor = when {
        isPickedUp -> TourOSColors.Success
        isNoShow -> TourOSColors.Error
        isSelected -> TourOSColors.Primary
        else -> TourOSColors.Border
    }

    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectMap() }
            .border(
                width = if (isSelected || isPickedUp) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
            ),
        backgroundColor = cardBg,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // Header Satırı: Sıra no, Saat, Durum Badgesi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    // Sıra Numarası Rozeti
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isPickedUp) TourOSColors.Success else TourOSColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPickedUp) "✓" else orderNumber.toString(),
                            style = TourOSTypography.Caption.copy(color = Color.White)
                        )
                    }

                    // Saat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.PrimaryContainer)
                            .padding(horizontal = TourOSSpacing.small, vertical = 2.dp)
                    ) {
                        Text(
                            "⏰ ${pickup.scheduledTime}",
                            style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                        )
                    }
                }

                // Durum Rozeti (Success Renkli Check İşareti)
                when {
                    isPickedUp -> {
                        TourOSStatusBadge(
                            text = "✅ Yolcu Alındı",
                            backgroundColor = TourOSColors.SuccessContainer,
                            textColor = TourOSColors.Success
                        )
                    }
                    isNoShow -> {
                        TourOSStatusBadge(
                            text = "⚠️ No-Show",
                            backgroundColor = TourOSColors.ErrorContainer,
                            textColor = TourOSColors.Error
                        )
                    }
                    else -> {
                        TourOSStatusBadge(
                            text = "⏳ Alış Bekliyor",
                            backgroundColor = TourOSColors.Surface,
                            textColor = TourOSColors.TextSecondary
                        )
                    }
                }
            }

            // Otel & Yolcu Bilgileri
            Column {
                Text(
                    text = "🏨 ${pickup.hotelName} ${if (!pickup.roomNumber.isNullOrBlank()) "(Oda: ${pickup.roomNumber})" else ""}",
                    style = TourOSTypography.TitleMedium.copy(
                        color = if (isPickedUp) TourOSColors.Success else TourOSColors.TextPrimary
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "👤 ${pickup.passengerName}  ·  👥 ${pickup.paxCount} Pax  ·  📞 ${pickup.passengerPhone ?: "—"}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
                Text(
                    text = "📍 ${pickup.locationName}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            if (!pickup.notes.isNullOrBlank()) {
                Text(
                    text = "📝 Not: ${pickup.notes}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Aksiyon Butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSelectMap) {
                    Text("🗺️ Harita", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    TourOSButton(
                        text = "Gelmedi",
                        onClick = onNoShowClick,
                        variant = TourOSButtonVariant.DESTRUCTIVE
                    )
                    TourOSButton(
                        text = if (isPickedUp) "✅ Alındı" else "Alındı İşaretle",
                        onClick = onPickedUpClick,
                        variant = TourOSButtonVariant.SUCCESS
                    )
                }
            }
        }
    }
}

@Composable
private fun MapPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().background(TourOSColors.Surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🗺️", style = TourOSTypography.DisplaySmall)
            Text(
                "Haritada göstermek için bir durak seçin.",
                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
            )
        }
    }
}
