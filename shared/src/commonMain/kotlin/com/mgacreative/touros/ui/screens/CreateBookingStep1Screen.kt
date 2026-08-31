package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var showDualCalendarModal by remember { mutableStateOf(false) }
    var showHierarchicalDestModal by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf(com.mgacreative.touros.utils.DateUtils.getTodayDot()) }
    var customEndDate by remember { mutableStateOf(com.mgacreative.touros.utils.DateUtils.getFutureDot(7)) }
    var customFlexDays by remember { mutableStateOf(3) }

    if (showHierarchicalDestModal) {
        com.mgacreative.touros.ui.components.HierarchicalDestinationPickerDialog(
            currentSelection = uiState.searchQuery,
            onDestinationSelected = { destItem ->
                val keyword = when (destItem.level) {
                    com.mgacreative.touros.ui.components.DestinationLevel.COUNTRY -> ""
                    com.mgacreative.touros.ui.components.DestinationLevel.CITY -> destItem.name.substringBefore(" ")
                    com.mgacreative.touros.ui.components.DestinationLevel.RESORT -> destItem.name.substringBefore(" /").substringBefore(" (")
                    else -> destItem.name
                }
                viewModel.onSearchQueryChanged(keyword)
            },
            onDismiss = { showHierarchicalDestModal = false }
        )
    }

    if (showDualCalendarModal) {
        com.mgacreative.touros.ui.components.DualMonthRangeDatePickerDialog(
            initialStartDateText = customStartDate,
            initialEndDateText = customEndDate,
            initialFlexibilityDays = customFlexDays,
            onRangeSelected = { start, end, nights, flex ->
                customStartDate = start
                customEndDate = end
                customFlexDays = flex
                viewModel.setNightCount(nights)
            },
            onDismiss = { showDualCalendarModal = false }
        )
    }

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
                                onOpenDestinationPicker = { showHierarchicalDestModal = true },
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
                                startDate = customStartDate,
                                endDate = customEndDate,
                                flexDays = customFlexDays,
                                onOpenDualCalendar = { showDualCalendarModal = true },
                                onFlexDaysChanged = { customFlexDays = it },
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
                            title = "4. Oda Tipi, Oda Sayısı & Kişi Detayları",
                            summaryText = uiState.selectedRoomType?.let {
                                "${it.name} • ${uiState.roomCount} Oda • ${uiState.adultCount} Yetişkin" + (if (uiState.childCount > 0) " · ${uiState.childCount} Çocuk" else "")
                            } ?: "Henüz oda seçilmedi",
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
                                onRoomCountChanged = { viewModel.setRoomCount(it) },
                                onAdultCountChanged = { viewModel.updatePaxCounts(it, uiState.childCount, uiState.infantCount) },
                                onChildCountChanged = { viewModel.updatePaxCounts(uiState.adultCount, it, uiState.infantCount) },
                                onChildrenAgesChanged = { viewModel.setChildrenAges(it) }
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
    onOpenDestinationPicker: () -> Unit,
    onTourSelected: (Tour) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                TourOSTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = "🔍 Tur adı, şehir veya otel arayın...",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            TourOSButton(
                text = "📍 Destinasyon Ağacı",
                onClick = onOpenDestinationPicker,
                variant = TourOSButtonVariant.SECONDARY
            )
        }

        // Ülke Hızlı Seçim Sekmeleri (Türkiye, Mısır, Tayland, Vietnam, BAE, Rusya)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                "Tümü" to "",
                "🇹🇷 Türkiye" to "Türkiye",
                "🇪🇬 Mısır" to "Mısır",
                "🇹🇭 Tayland" to "Tayland",
                "🇻🇳 Vietnam" to "Vietnam",
                "🇦🇪 BAE (Dubai)" to "Dubai",
                "🇷🇺 Rusya" to "Rusya"
            ).forEach { (label, keyword) ->
                val isSelected = (keyword.isEmpty() && uiState.searchQuery.isEmpty()) || (keyword.isNotEmpty() && uiState.searchQuery.contains(keyword, ignoreCase = true))
                TourOSStatusBadge(
                    text = label,
                    backgroundColor = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Surface,
                    textColor = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary,
                    modifier = Modifier.clickable { onSearchQueryChanged(keyword) }
                )
            }
        }

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
    startDate: String,
    endDate: String,
    flexDays: Int,
    onOpenDualCalendar: () -> Unit,
    onFlexDaysChanged: (Int) -> Unit,
    onDepartureSelected: (Departure) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
        // Çift Ay Takvim ve Esneklik Çubuğu
        TourOSCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TourOSColors.Surface,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🗓️ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gidiş - Dönüş Tarihleri")}",
                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextSecondary)
                        )
                        Text(
                            text = "$startDate ➔ $endDate (${uiState.nightCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gece")})",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                        )
                    }

                    TourOSButton(
                        text = "📅 Çift Ay Takvim Aç ›",
                        onClick = onOpenDualCalendar,
                        variant = TourOSButtonVariant.SECONDARY
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Esneklik")}: ",
                        style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextSecondary)
                    )

                    listOf(0 to "Tam Tarihler", 1 to "±1 Gün", 2 to "±2 Gün", 3 to "±3 Gün Esnek").forEach { (fDays, label) ->
                        val isSelected = (flexDays == fDays)
                        TourOSButton(
                            text = (if (isSelected) "✓ " else "") + com.mgacreative.touros.ui.localization.AppLanguageManager.translate(label),
                            onClick = { onFlexDaysChanged(fDays) },
                            variant = if (isSelected) TourOSButtonVariant.PRIMARY else TourOSButtonVariant.TERTIARY
                        )
                    }
                }

                if (flexDays > 0) {
                    Text(
                        text = "💡 ±$flexDays gün esneklik devrede: Seçilen tarihin yakınındaki en uygun kalkışlar listeleniyor.",
                        style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

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
    onRoomCountChanged: (Int) -> Unit,
    onAdultCountChanged: (Int) -> Unit,
    onChildCountChanged: (Int) -> Unit,
    onChildrenAgesChanged: (List<Int>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
        // Konaklama ve Kişi Parametreleri Kartı
        TourOSCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TourOSColors.Surface,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Oda Sayısı
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Oda: ", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary))
                        TourOSButton(
                            text = "-",
                            onClick = { if (uiState.roomCount > 1) onRoomCountChanged(uiState.roomCount - 1) },
                            variant = TourOSButtonVariant.TERTIARY
                        )
                        Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
                        Text(text = "${uiState.roomCount}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                        Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
                        TourOSButton(
                            text = "+",
                            onClick = { if (uiState.roomCount < 5) onRoomCountChanged(uiState.roomCount + 1) },
                            variant = TourOSButtonVariant.TERTIARY
                        )
                    }

                    // Yetişkin Sayısı
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Yetişkin: ", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary))
                        TourOSButton(
                            text = "-",
                            onClick = { if (uiState.adultCount > 1) onAdultCountChanged(uiState.adultCount - 1) },
                            variant = TourOSButtonVariant.TERTIARY
                        )
                        Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
                        Text(text = "${uiState.adultCount}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                        Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
                        TourOSButton(
                            text = "+",
                            onClick = { if (uiState.adultCount < 10) onAdultCountChanged(uiState.adultCount + 1) },
                            variant = TourOSButtonVariant.TERTIARY
                        )
                    }

                    // Çocuk Sayısı
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Çocuk: ", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary))
                        TourOSButton(
                            text = "-",
                            onClick = { if (uiState.childCount > 0) onChildCountChanged(uiState.childCount - 1) },
                            variant = TourOSButtonVariant.TERTIARY
                        )
                        Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
                        Text(text = "${uiState.childCount}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                        Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
                        TourOSButton(
                            text = "+",
                            onClick = { if (uiState.childCount < 6) onChildCountChanged(uiState.childCount + 1) },
                            variant = TourOSButtonVariant.TERTIARY
                        )
                    }

                    // Gece Sayısı
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Gece: ", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary))
                        TourOSButton(
                            text = "-",
                            onClick = { if (uiState.nightCount > 1) onNightCountChanged(uiState.nightCount - 1) },
                            variant = TourOSButtonVariant.TERTIARY
                        )
                        Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
                        Text(text = "${uiState.nightCount}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                        Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
                        TourOSButton(
                            text = "+",
                            onClick = { onNightCountChanged(uiState.nightCount + 1) },
                            variant = TourOSButtonVariant.TERTIARY
                        )
                    }
                }

                // Çocuk Yaşları Dinamik Seçimi (Çocuk > 0 ise)
                if (uiState.childCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👶 Çocuk Yaşları: ", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextSecondary))
                        uiState.childrenAges.forEachIndexed { index, age ->
                            ChildAgeSelectorItem(
                                index = index,
                                age = age,
                                onAgeSelected = { selectedAge ->
                                    val updated = uiState.childrenAges.toMutableList()
                                    if (index < updated.size) {
                                        updated[index] = selectedAge
                                        onChildrenAgesChanged(updated)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Text(text = "Müsait Oda Tipleri:", style = TourOSTypography.TitleSmall.copy(color = TourOSColors.TextPrimary))

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

@Composable
private fun ChildAgeSelectorItem(
    index: Int,
    age: Int,
    onAgeSelected: (Int) -> Unit
) {
    var showAgeMenu by remember { mutableStateOf(false) }
    Box {
        TourOSButton(
            text = "${index + 1}. Çocuk: $age Yaş ▼",
            onClick = { showAgeMenu = true },
            variant = TourOSButtonVariant.SECONDARY
        )
        DropdownMenu(
            expanded = showAgeMenu,
            onDismissRequest = { showAgeMenu = false }
        ) {
            (0..17).forEach { a ->
                DropdownMenuItem(
                    text = { Text("$a Yaş" + (if (a < 2) " (Bebek)" else if (a in 2..6) " (Çocuk)" else "")) },
                    onClick = {
                        onAgeSelected(a)
                        showAgeMenu = false
                    }
                )
            }
        }
    }
}

