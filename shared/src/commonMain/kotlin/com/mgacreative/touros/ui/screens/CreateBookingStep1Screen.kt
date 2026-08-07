package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.Departure
import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.BookingWizardStep
import com.mgacreative.touros.ui.viewmodel.CreateBookingWizardUiState
import com.mgacreative.touros.ui.viewmodel.CreateBookingWizardViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Rezervasyon Sihirbazı - Adım 1.
 * - Üstte Adım Göstergesi (Adım 1/2).
 * - Sıralı Accordion (Açılır-Kapanır) TourOSCard bölümleri:
 *   1. Tur Seçimi
 *   2. Tarih & Kalkış Seçimi
 *   3. Otel Seçimi
 *   4. Oda Tipi & PAX Seçimi
 * Altta hesaplanan toplam fiyat & 2. Adıma Geçiş Butonu.
 */
@Composable
fun CreateBookingStep1Screen(
    onNavigateBack: () -> Unit = {},
    onCompleteStep1: () -> Unit = {},
    viewModel: CreateBookingWizardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Yeni Rezervasyon Oluştur",
                subtitle = "Adım 1 / 2: Tur, Tarih, Otel ve Oda Seçimi",
                navigationIcon = {
                    TourOSButton(
                        text = "← İptal",
                        onClick = onNavigateBack,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                }
            )
        },
        bottomBar = {
            Step1BottomBar(
                uiState = uiState,
                onPreviousClick = {
                    if (uiState.currentStep == BookingWizardStep.SELECT_TOUR) {
                        onNavigateBack()
                    } else {
                        viewModel.goToPreviousStep()
                    }
                },
                onNextClick = {
                    if (uiState.currentStep == BookingWizardStep.SELECT_ROOM_TYPE) {
                        onCompleteStep1()
                    } else {
                        viewModel.goToNextStep()
                    }
                }
            )
        },
        containerColor = TourOSColors.Surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(TourOSSpacing.large)
        ) {
            if (uiState.isLoading) {
                TourOSLoadingIndicator(message = "Turlar ve fiyatlar yükleniyor...")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    // Adım İlerleme Göstergesi Barı
                    item {
                        StepIndicatorHeader(currentStep = uiState.currentStep)
                    }

                    // Section 1: Tur Seçimi (Accordion)
                    item {
                        AccordionCardSection(
                            stepNumber = 1,
                            title = "1. Tur Seçimi",
                            summaryText = uiState.selectedTour?.title ?: "Henüz tur seçilmedi",
                            isExpanded = uiState.currentStep == BookingWizardStep.SELECT_TOUR,
                            isCompleted = uiState.selectedTour != null,
                            onHeaderClick = { viewModel.goToStep(BookingWizardStep.SELECT_TOUR) }
                        ) {
                            SelectTourContent(
                                uiState = uiState,
                                onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                onTourSelected = { viewModel.selectTour(it) }
                            )
                        }
                    }

                    // Section 2: Tarih & Kalkış Seçimi (Accordion)
                    item {
                        AccordionCardSection(
                            stepNumber = 2,
                            title = "2. Tarih & Kalkış Seçimi",
                            summaryText = uiState.selectedDeparture?.let { "Kalkış: ${it.departureDate}" } ?: "Henüz tarih seçilmedi",
                            isExpanded = uiState.currentStep == BookingWizardStep.SELECT_DEPARTURE,
                            isCompleted = uiState.selectedDeparture != null,
                            onHeaderClick = {
                                if (uiState.selectedTour != null) {
                                    viewModel.goToStep(BookingWizardStep.SELECT_DEPARTURE)
                                }
                            }
                        ) {
                            SelectDepartureContent(
                                uiState = uiState,
                                onDepartureSelected = { viewModel.selectDeparture(it) }
                            )
                        }
                    }

                    // Section 3: Otel Seçimi (Accordion)
                    item {
                        AccordionCardSection(
                            stepNumber = 3,
                            title = "3. Otel Seçimi",
                            summaryText = uiState.selectedHotel?.name ?: "Henüz otel seçilmedi",
                            isExpanded = uiState.currentStep == BookingWizardStep.SELECT_HOTEL,
                            isCompleted = uiState.selectedHotel != null,
                            onHeaderClick = {
                                if (uiState.selectedDeparture != null) {
                                    viewModel.goToStep(BookingWizardStep.SELECT_HOTEL)
                                }
                            }
                        ) {
                            SelectHotelContent(
                                uiState = uiState,
                                onHotelSelected = { viewModel.selectHotel(it) }
                            )
                        }
                    }

                    // Section 4: Oda Tipi & PAX Seçimi (Accordion)
                    item {
                        AccordionCardSection(
                            stepNumber = 4,
                            title = "4. Oda Tipi & Kişi Sayısı (PAX)",
                            summaryText = uiState.selectedRoomType?.let { "${it.name} • ${uiState.paxCount} Kişi" } ?: "Henüz oda seçilmedi",
                            isExpanded = uiState.currentStep == BookingWizardStep.SELECT_ROOM_TYPE,
                            isCompleted = uiState.selectedRoomType != null,
                            onHeaderClick = {
                                if (uiState.selectedHotel != null) {
                                    viewModel.goToStep(BookingWizardStep.SELECT_ROOM_TYPE)
                                }
                            }
                        ) {
                            SelectRoomTypeContent(
                                uiState = uiState,
                                onRoomTypeSelected = { viewModel.selectRoomType(it) },
                                onNightCountChanged = { viewModel.setNightCount(it) },
                                onPaxCountChanged = { viewModel.setPaxCount(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccordionCardSection(
    stepNumber: Int,
    title: String,
    summaryText: String,
    isExpanded: Boolean,
    isCompleted: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = if (isExpanded) TourOSColors.Primary else TourOSColors.Border,
        contentPadding = TourOSSpacing.medium
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(TourOSSpacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isExpanded -> TourOSColors.Primary
                                    isCompleted -> TourOSColors.Success
                                    else -> TourOSColors.Surface
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCompleted && !isExpanded) "✓" else stepNumber.toString(),
                            style = TourOSTypography.Caption.copy(
                                color = if (isExpanded || isCompleted) TourOSColors.Background else TourOSColors.TextDisabled
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(TourOSSpacing.medium))

                    Column {
                        Text(
                            text = title,
                            style = TourOSTypography.TitleMedium.copy(
                                color = if (isExpanded) TourOSColors.Primary else TourOSColors.TextPrimary
                            )
                        )
                        if (!isExpanded) {
                            Text(
                                text = summaryText,
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    }
                }

                Text(
                    text = if (isExpanded) "▲" else "▼",
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = TourOSSpacing.medium)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun StepIndicatorHeader(currentStep: BookingWizardStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TourOSSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookingWizardStep.entries.forEach { step ->
            val isSelected = step == currentStep
            val isPassed = step.stepNumber < currentStep.stepNumber

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> TourOSColors.Primary
                                isPassed -> TourOSColors.Success
                                else -> TourOSColors.Surface
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPassed) "✓" else step.stepNumber.toString(),
                        style = TourOSTypography.Caption.copy(
                            color = if (isSelected || isPassed) TourOSColors.Background else TourOSColors.TextDisabled
                        )
                    )
                }

                Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
                Text(
                    text = step.title,
                    style = TourOSTypography.Caption.copy(
                        color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
private fun SelectTourContent(
    uiState: CreateBookingWizardUiState,
    onSearchQueryChanged: (String) -> Unit,
    onTourSelected: (Tour) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
        TourOSTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = "🔍 Tur adı veya şehir arayın...",
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier.height(240.dp),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            items(uiState.tours, key = { it.id }) { tour ->
                val isSelected = uiState.selectedTour?.id == tour.id
                TourOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTourSelected(tour) },
                    backgroundColor = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Background,
                    borderColor = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
                    contentPadding = TourOSSpacing.medium
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            TourOSStatusBadge(
                                text = tour.category.displayName,
                                backgroundColor = TourOSColors.Surface,
                                textColor = TourOSColors.Primary
                            )
                            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                            Text(text = tour.title, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                            Text(text = "📍 ${tour.city}, ${tour.country} • ⏱️ ${tour.durationDays} Gün", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }

                        Text(
                            text = "${tour.basePrice} TRY",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectDepartureContent(
    uiState: CreateBookingWizardUiState,
    onDepartureSelected: (Departure) -> Unit
) {
    if (uiState.departures.isEmpty()) {
        Text(text = "Seçilen tur için aktif kalkış tarihi bulunmuyor.", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
    } else {
        LazyColumn(
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            items(uiState.departures, key = { it.id }) { dep ->
                val isSelected = uiState.selectedDeparture?.id == dep.id
                TourOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDepartureSelected(dep) },
                    backgroundColor = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Background,
                    borderColor = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
                    contentPadding = TourOSSpacing.medium
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "📅 Kalkış: ${dep.departureDate}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                            Text(text = "Dönüş: ${dep.returnDate ?: '-'}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                        Text(
                            text = "${dep.priceOverride ?: uiState.selectedTour?.basePrice ?: 0.0} TRY",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectHotelContent(
    uiState: CreateBookingWizardUiState,
    onHotelSelected: (Hotel) -> Unit
) {
    if (uiState.hotels.isEmpty()) {
        Text(text = "Bölgede tanımlı otel bulunamadı.", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
    } else {
        LazyColumn(
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            items(uiState.hotels, key = { it.id }) { hotel ->
                val isSelected = uiState.selectedHotel?.id == hotel.id
                TourOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHotelSelected(hotel) },
                    backgroundColor = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Background,
                    borderColor = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
                    contentPadding = TourOSSpacing.medium
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = hotel.name, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                            Text(text = "⭐ ${hotel.starRating ?: 4} Yıldız • 📍 ${hotel.city ?: ""}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                        Text(
                            text = if (isSelected) "Seçildi ✓" else "Seç ›",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectRoomTypeContent(
    uiState: CreateBookingWizardUiState,
    onRoomTypeSelected: (RoomType) -> Unit,
    onNightCountChanged: (Int) -> Unit,
    onPaxCountChanged: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Kişi Sayısı (PAX): ", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                TourOSButton(
                    text = "-",
                    onClick = { onPaxCountChanged(uiState.paxCount - 1) },
                    variant = TourOSButtonVariant.TERTIARY
                )
                Spacer(modifier = Modifier.width(TourOSSpacing.small))
                Text(text = "${uiState.paxCount}", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.width(TourOSSpacing.small))
                TourOSButton(
                    text = "+",
                    onClick = { onPaxCountChanged(uiState.paxCount + 1) },
                    variant = TourOSButtonVariant.TERTIARY
                )
            }
        }

        LazyColumn(
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            items(uiState.roomTypes, key = { it.id }) { room ->
                val isSelected = uiState.selectedRoomType?.id == room.id
                TourOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRoomTypeSelected(room) },
                    backgroundColor = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Background,
                    borderColor = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
                    contentPadding = TourOSSpacing.medium
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = room.name, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                            Text(text = "Maks ${room.maxOccupancy} Kişi", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                        Text(
                            text = "${room.basePricePerNight} TRY / Gece",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1BottomBar(
    uiState: CreateBookingWizardUiState,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TourOSButton(
                text = "‹ İptal / Geri",
                onClick = onPreviousClick,
                variant = TourOSButtonVariant.TERTIARY
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Hesaplanan Toplam Tutar", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text(
                    text = "${uiState.totalCalculatedPrice} TRY",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                )
            }

            TourOSButton(
                text = if (uiState.currentStep == BookingWizardStep.SELECT_ROOM_TYPE) "Devam Et (Yolcu Bilgileri) ›" else "İlerle ›",
                onClick = onNextClick,
                variant = TourOSButtonVariant.PRIMARY,
                enabled = uiState.canProceedNext
            )
        }
    }
}
