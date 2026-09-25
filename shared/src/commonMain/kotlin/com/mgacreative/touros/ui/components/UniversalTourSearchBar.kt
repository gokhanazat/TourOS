package com.mgacreative.touros.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

enum class SearchBarVariant {
    B2B_PORTAL,
    PUBLIC_WEB_HERO
}

@Composable
fun UniversalTourSearchBar(
    variant: SearchBarVariant = SearchBarVariant.B2B_PORTAL,
    activeTab: String = "TOURS", // "TOURS", "FLIGHTS", "HOTELS"
    onTabChange: (String) -> Unit = {},
    departureCity: String = "",
    onDepartureCityChange: (String) -> Unit = {},
    selectedRegion: String = "",
    onRegionChange: (String) -> Unit = {},
    startDateText: String = "20.08.2026",
    endDateText: String = "28.08.2026",
    onDateRangeChange: (startDate: String, endDate: String) -> Unit = { _, _ -> },
    nightsText: String = "7 - 10 Gece",
    onNightsTextChange: (String) -> Unit = {},
    adults: Int = 2,
    onAdultsChange: (Int) -> Unit = {},
    childrenAges: List<Int> = emptyList(),
    onChildrenAgesChange: (List<Int>) -> Unit = {},
    isRoundTrip: Boolean = true,
    onRoundTripChange: (Boolean) -> Unit = {},
    availableDepartureCities: List<String> = emptyList(),
    availableDestinations: List<String> = emptyList(),
    onSearchClick: () -> Unit = {},
    onResetFiltersClick: (() -> Unit)? = null,
    extraBottomContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    var showDepartureDropdown by remember { mutableStateOf(false) }
    var showRussianDepartureModal by remember { mutableStateOf(false) }
    var showDestinationModal by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showNightsDropdown by remember { mutableStateOf(false) }
    var showTouristDialog by remember { mutableStateOf(false) }

    // ── MODAL DİALOGLARI ────────────────────────────────────────────────────────
    if (showRussianDepartureModal) {
        RussianDepartureCityPickerDialog(
            currentSelection = departureCity,
            allowedCityNames = if (availableDepartureCities.isNotEmpty()) availableDepartureCities.toSet() else null,
            onCitySelected = { city ->
                onDepartureCityChange("${city.nameRu} (${city.airportCode})")
                showRussianDepartureModal = false
            },
            onDismiss = { showRussianDepartureModal = false }
        )
    }

    if (showDestinationModal) {
        val isFlightTab = activeTab.uppercase() == "FLIGHTS" || activeTab.uppercase() == "FLIGHT"
        HierarchicalDestinationPickerDialog(
            currentSelection = selectedRegion,
            onlyAirports = isFlightTab,
            customTitle = if (isFlightTab) "✈️ UÇUŞ VARIŞ HAVALİMANI / АЭРОПОРТ НАЗНАЧЕНИЯ" else null,
            onDestinationSelected = { destItem ->
                val formatted = if (isFlightTab && destItem.airportCode != null) {
                    "${destItem.name.substringBefore(" Havalimanı").substringBefore(" Uluslararası")} (${destItem.airportCode})"
                } else if (destItem.nameRu.isNotBlank()) {
                    if (AppLanguageManager.currentLanguage.value.code == "ru") destItem.nameRu
                    else "${destItem.name} (${destItem.nameRu})"
                } else {
                    destItem.name
                }
                onRegionChange(formatted)
                showDestinationModal = false
            },
            onDismiss = { showDestinationModal = false }
        )
    }

    if (showDateRangePicker) {
        DualMonthRangeDatePickerDialog(
            initialStartDateText = startDateText,
            initialEndDateText = endDateText,
            onRangeSelected = { start, end, nights, _ ->
                onDateRangeChange(start, end)
                if (nights in 1..30) {
                    onNightsTextChange("$nights Gece")
                }
                showDateRangePicker = false
            },
            onDismiss = { showDateRangePicker = false }
        )
    }

    if (showTouristDialog) {
        UniversalTouristPickerDialog(
            adults = adults,
            childrenAges = childrenAges,
            onAdultsChange = onAdultsChange,
            onChildrenAgesChange = onChildrenAgesChange,
            onDismiss = { showTouristDialog = false }
        )
    }

    val isHero = variant == SearchBarVariant.PUBLIC_WEB_HERO
    val isFlightsTab = activeTab.uppercase() == "FLIGHTS" || activeTab.uppercase() == "FLIGHT"

    val containerModifier = if (isHero) {
        modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(16.dp)
    } else {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TourOSColors.Surface)
            .border(1.dp, TourOSColors.Border, RoundedCornerShape(12.dp))
            .padding(TourOSSpacing.large)
    }

    Column(
        modifier = containerModifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. ÜST KATEGORİ SEKMELERİ (TURLAR | UÇAK BİLETİ | OTELLER) ─────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple("TOURS", AppLanguageManager.translate("Turlar & Paketler"), Icons.Default.Luggage),
                Triple("FLIGHTS", AppLanguageManager.translate("Uçak Bileti"), Icons.Default.Flight),
                Triple("HOTELS", AppLanguageManager.translate("Sadece Otel"), Icons.Default.Hotel)
            ).forEach { (tabKey, tabLabel, tabIcon) ->
                val isSelected = activeTab.uppercase() == tabKey || (tabKey == "FLIGHTS" && activeTab.uppercase() == "FLIGHT")
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isSelected -> if (isHero) Color(0xFF1E293B) else TourOSColors.Primary
                        else -> if (isHero) Color(0xFFF1F5F9) else TourOSColors.PrimaryContainer.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.clickable { onTabChange(tabKey) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = tabIcon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else if (isHero) Color(0xFF475569) else TourOSColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tabLabel,
                            style = TourOSTypography.Caption.copy(
                                color = if (isSelected) Color.White else if (isHero) Color(0xFF475569) else TourOSColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompact = maxWidth < 840.dp

            if (isFlightsTab) {
                // ── 2. UÇUŞ SEKME ARAMA FORMU (GİDİŞ-DÖNÜŞ VE TEK YÖN DESTEKLİ) ───────
                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FlightSearchFields(
                            departureCity = departureCity,
                            onDepartureCityClick = { showRussianDepartureModal = true },
                            selectedRegion = selectedRegion,
                            onRegionClick = { showDestinationModal = true },
                            startDateText = startDateText,
                            onStartDateClick = { showDateRangePicker = true },
                            endDateText = endDateText,
                            onEndDateClick = { showDateRangePicker = true },
                            isRoundTrip = isRoundTrip,
                            onRoundTripChange = onRoundTripChange,
                            adults = adults,
                            childrenAges = childrenAges,
                            onTouristClick = { showTouristDialog = true },
                            onSearchClick = onSearchClick,
                            onResetFiltersClick = onResetFiltersClick,
                            isCompact = true
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlightSearchFields(
                            departureCity = departureCity,
                            onDepartureCityClick = { showRussianDepartureModal = true },
                            selectedRegion = selectedRegion,
                            onRegionClick = { showDestinationModal = true },
                            startDateText = startDateText,
                            onStartDateClick = { showDateRangePicker = true },
                            endDateText = endDateText,
                            onEndDateClick = { showDateRangePicker = true },
                            isRoundTrip = isRoundTrip,
                            onRoundTripChange = onRoundTripChange,
                            adults = adults,
                            childrenAges = childrenAges,
                            onTouristClick = { showTouristDialog = true },
                            onSearchClick = onSearchClick,
                            onResetFiltersClick = onResetFiltersClick,
                            isCompact = false
                        )
                    }
                }
            } else {
                // ── 3. TUR VE OTEL ARAMA FORMU (KALKIŞ, DESTİNASYON, TARİH ARALIĞI, GECE, TURİST) ─
                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TourSearchFields(
                            activeTab = activeTab,
                            departureCity = departureCity,
                            onDepartureCityClick = { showRussianDepartureModal = true },
                            selectedRegion = selectedRegion,
                            onRegionClick = { showDestinationModal = true },
                            startDateText = startDateText,
                            endDateText = endDateText,
                            onDateRangeClick = { showDateRangePicker = true },
                            nightsText = nightsText,
                            onNightsClick = { showNightsDropdown = true },
                            showNightsDropdown = showNightsDropdown,
                            onNightsDropdownDismiss = { showNightsDropdown = false },
                            onNightsSelect = { onNightsTextChange(it); showNightsDropdown = false },
                            adults = adults,
                            childrenAges = childrenAges,
                            onTouristClick = { showTouristDialog = true },
                            onSearchClick = onSearchClick,
                            onResetFiltersClick = onResetFiltersClick,
                            isCompact = true,
                            isHero = isHero
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TourSearchFields(
                            activeTab = activeTab,
                            departureCity = departureCity,
                            onDepartureCityClick = { showRussianDepartureModal = true },
                            selectedRegion = selectedRegion,
                            onRegionClick = { showDestinationModal = true },
                            startDateText = startDateText,
                            endDateText = endDateText,
                            onDateRangeClick = { showDateRangePicker = true },
                            nightsText = nightsText,
                            onNightsClick = { showNightsDropdown = true },
                            showNightsDropdown = showNightsDropdown,
                            onNightsDropdownDismiss = { showNightsDropdown = false },
                            onNightsSelect = { onNightsTextChange(it); showNightsDropdown = false },
                            adults = adults,
                            childrenAges = childrenAges,
                            onTouristClick = { showTouristDialog = true },
                            onSearchClick = onSearchClick,
                            onResetFiltersClick = onResetFiltersClick,
                            isCompact = false,
                            isHero = isHero
                        )
                    }
                }
            }
        }

        if (extraBottomContent != null) {
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            extraBottomContent()
        }
    }
}

// ─── TUR VE OTEL ARAMA ALANLARI COMPOSABLE ─────────────────────────────────────

@Composable
private fun RowScope.TourSearchFields(
    activeTab: String = "TOURS",
    departureCity: String,
    onDepartureCityClick: () -> Unit,
    selectedRegion: String,
    onRegionClick: () -> Unit,
    startDateText: String,
    endDateText: String,
    onDateRangeClick: () -> Unit,
    nightsText: String,
    onNightsClick: () -> Unit,
    showNightsDropdown: Boolean,
    onNightsDropdownDismiss: () -> Unit,
    onNightsSelect: (String) -> Unit,
    adults: Int,
    childrenAges: List<Int>,
    onTouristClick: () -> Unit,
    onSearchClick: () -> Unit,
    onResetFiltersClick: (() -> Unit)? = null,
    isCompact: Boolean,
    isHero: Boolean
) {
    val touristSummary = if (childrenAges.isEmpty()) {
        "$adults ${AppLanguageManager.translate("Yetişkin")}"
    } else {
        "$adults ${AppLanguageManager.translate("Yet")}, ${childrenAges.size} ${AppLanguageManager.translate("Çoc")} (${childrenAges.joinToString(",") { "${it}y" }})"
    }

    val searchButtonText = when (activeTab.uppercase()) {
        "HOTELS", "HOTEL", "LOCAL_HOTELS" -> "OTELLERİ BUL"
        "FLIGHTS", "FLIGHT" -> "UÇUŞLARI BUL"
        else -> "TURLARI BUL"
    }

    val isFlightTab = activeTab.uppercase() == "FLIGHTS" || activeTab.uppercase() == "FLIGHT"

    // 1. NEREDEN
    Box(modifier = Modifier.weight(1.3f)) {
        TourOSTextField(
            value = departureCity.ifBlank { if (isFlightTab) AppLanguageManager.translate("Tüm Kalkış Havalimanları") else AppLanguageManager.translate("Tüm Kalkış Şehirleri") },
            onValueChange = {},
            readOnly = true,
            label = if (isFlightTab) AppLanguageManager.translate("Nereden (Kalkış)") else AppLanguageManager.translate("Nereden (Kalkış Şehri)"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onDepartureCityClick() })
    }

    // 2. NEREYE
    Box(modifier = Modifier.weight(1.4f)) {
        val displayRegion = if (selectedRegion.isBlank()) {
            if (isFlightTab) AppLanguageManager.translate("Tüm Varış Havalimanları") else AppLanguageManager.translate("Tüm Destinasyonlar / Ülkeler")
        } else {
            AppLanguageManager.translate(selectedRegion)
        }
        TourOSTextField(
            value = displayRegion,
            onValueChange = {},
            readOnly = true,
            label = if (isFlightTab) AppLanguageManager.translate("Nereye (Varış Havalimanı)") else AppLanguageManager.translate("Nereye (Destinasyon / Otel)"),
            placeholder = AppLanguageManager.translate("Tüm Bölgeler"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onRegionClick() })
    }

    // 3. GİDİŞ TARİH ARALIĞI
    Box(modifier = Modifier.weight(1.3f)) {
        val displayDates = if (startDateText.isNotBlank() && endDateText.isNotBlank()) "$startDateText — $endDateText"
        else if (startDateText.isNotBlank()) startDateText
        else AppLanguageManager.translate("gg.aa.yyyy")
        TourOSTextField(
            value = displayDates,
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Tarih Aralığı"),
            placeholder = AppLanguageManager.translate("gg.aa.yyyy"),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = TourOSColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onDateRangeClick() })
    }

    // 4. GECE SAYISI
    Box(modifier = Modifier.weight(0.9f)) {
        TourOSTextField(
            value = AppLanguageManager.formatNights(nightsText.removeSuffix(" ▼")),
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Gece Sayısı"),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TourOSColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onNightsClick() })
        DropdownMenu(
            expanded = showNightsDropdown,
            onDismissRequest = onNightsDropdownDismiss,
            modifier = Modifier.width(180.dp).background(TourOSColors.Surface)
        ) {
            listOf("1 - 4 Gece", "5 - 7 Gece", "7 - 10 Gece", "10 - 14 Gece", "14 - 21 Gece", "Tüm Geceler (1 - 30)").forEach { nOpt ->
                DropdownMenuItem(
                    text = { Text(AppLanguageManager.translate(nOpt), style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                    onClick = { onNightsSelect(nOpt) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }

    // 5. TURİST SAYISI
    Box(modifier = Modifier.weight(1.1f)) {
        TourOSTextField(
            value = touristSummary,
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Turist"),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TourOSColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onTouristClick() })
    }

    // 6. ARAMA VE SIFIRLAMA BUTONLARI
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Button(
            onClick = onSearchClick,
            modifier = Modifier.height(if (onResetFiltersClick != null) 42.dp else 50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isHero) Color(0xFF0F5A56) else TourOSColors.Primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = AppLanguageManager.translate(searchButtonText),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        if (onResetFiltersClick != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onResetFiltersClick() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "↺",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHero) Color(0xFF0F5A56) else TourOSColors.Primary
                )
                Text(
                    text = "Сбросить фильтры",
                    style = TourOSTypography.Caption.copy(
                        color = if (isHero) Color(0xFF0F5A56) else TourOSColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// ─── DİKEY MOBİL TUR ALANLARI ──────────────────────────────────────────────────

@Composable
private fun ColumnScope.TourSearchFields(
    activeTab: String = "TOURS",
    departureCity: String,
    onDepartureCityClick: () -> Unit,
    selectedRegion: String,
    onRegionClick: () -> Unit,
    startDateText: String,
    endDateText: String,
    onDateRangeClick: () -> Unit,
    nightsText: String,
    onNightsClick: () -> Unit,
    showNightsDropdown: Boolean,
    onNightsDropdownDismiss: () -> Unit,
    onNightsSelect: (String) -> Unit,
    adults: Int,
    childrenAges: List<Int>,
    onTouristClick: () -> Unit,
    onSearchClick: () -> Unit,
    onResetFiltersClick: (() -> Unit)? = null,
    isCompact: Boolean,
    isHero: Boolean
) {
    val touristSummary = if (childrenAges.isEmpty()) {
        "$adults ${AppLanguageManager.translate("Yetişkin")}"
    } else {
        "$adults ${AppLanguageManager.translate("Yet")}, ${childrenAges.size} ${AppLanguageManager.translate("Çoc")} (${childrenAges.joinToString(",") { "${it}y" }})"
    }

    val searchButtonText = when (activeTab.uppercase()) {
        "HOTELS", "HOTEL", "LOCAL_HOTELS" -> "OTELLERİ BUL"
        "FLIGHTS", "FLIGHT" -> "UÇUŞLARI BUL"
        else -> "TURLARI BUL"
    }

    val isFlightTab = activeTab.uppercase() == "FLIGHTS" || activeTab.uppercase() == "FLIGHT"

    Box(modifier = Modifier.fillMaxWidth()) {
        TourOSTextField(
            value = departureCity.ifBlank { if (isFlightTab) AppLanguageManager.translate("Tüm Kalkış Havalimanları") else AppLanguageManager.translate("Tüm Kalkış Şehirleri") },
            onValueChange = {},
            readOnly = true,
            label = if (isFlightTab) AppLanguageManager.translate("Nereden (Kalkış)") else AppLanguageManager.translate("Nereden (Kalkış Şehri)"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onDepartureCityClick() })
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        val displayRegion = if (selectedRegion.isBlank()) {
            if (isFlightTab) AppLanguageManager.translate("Tüm Varış Havalimanları") else AppLanguageManager.translate("Tüm Destinasyonlar / Ülkeler")
        } else {
            AppLanguageManager.translate(selectedRegion)
        }
        TourOSTextField(
            value = displayRegion,
            onValueChange = {},
            readOnly = true,
            label = if (isFlightTab) AppLanguageManager.translate("Nereye (Varış Havalimanı)") else AppLanguageManager.translate("Nereye (Destinasyon / Otel)"),
            placeholder = AppLanguageManager.translate("Tüm Bölgeler"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onRegionClick() })
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        val displayDates = if (startDateText.isNotBlank() && endDateText.isNotBlank()) "$startDateText — $endDateText"
        else if (startDateText.isNotBlank()) startDateText
        else AppLanguageManager.translate("gg.aa.yyyy")
        TourOSTextField(
            value = displayDates,
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Tarih Aralığı"),
            placeholder = AppLanguageManager.translate("gg.aa.yyyy"),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = TourOSColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onDateRangeClick() })
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            TourOSTextField(
                value = AppLanguageManager.translate(nightsText.removeSuffix(" ▼")),
                onValueChange = {},
                readOnly = true,
                label = AppLanguageManager.translate("Gece Sayısı"),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = TourOSColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onNightsClick() })
            DropdownMenu(
                expanded = showNightsDropdown,
                onDismissRequest = onNightsDropdownDismiss,
                modifier = Modifier.width(180.dp).background(TourOSColors.Surface)
            ) {
                listOf("1 - 4 Gece", "5 - 7 Gece", "7 - 10 Gece", "10 - 14 Gece", "14 - 21 Gece", "Tüm Geceler (1 - 30)").forEach { nOpt ->
                    DropdownMenuItem(
                        text = { Text(AppLanguageManager.translate(nOpt), style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                        onClick = { onNightsSelect(nOpt) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            TourOSTextField(
                value = touristSummary,
                onValueChange = {},
                readOnly = true,
                label = AppLanguageManager.translate("Turist"),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TourOSColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onTouristClick() })
        }
    }

    Button(
        onClick = onSearchClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isHero) Color(0xFF0F5A56) else TourOSColors.Primary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = AppLanguageManager.translate(searchButtonText),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }

    if (onResetFiltersClick != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable { onResetFiltersClick() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "↺ Сбросить фильтры",
                style = TourOSTypography.Caption.copy(
                    color = if (isHero) Color(0xFF0F5A56) else TourOSColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

// ─── UÇUŞ ARAMA ALANLARI COMPOSABLE ───────────────────────────────────────────

@Composable
private fun RowScope.FlightSearchFields(
    departureCity: String,
    onDepartureCityClick: () -> Unit,
    selectedRegion: String,
    onRegionClick: () -> Unit,
    startDateText: String,
    onStartDateClick: () -> Unit,
    endDateText: String,
    onEndDateClick: () -> Unit,
    isRoundTrip: Boolean,
    onRoundTripChange: (Boolean) -> Unit,
    adults: Int,
    childrenAges: List<Int>,
    onTouristClick: () -> Unit,
    onSearchClick: () -> Unit,
    onResetFiltersClick: (() -> Unit)? = null,
    isCompact: Boolean
) {
    val touristSummary = if (childrenAges.isEmpty()) "$adults ${AppLanguageManager.translate("Yolcu")}" else "$adults ${AppLanguageManager.translate("Yet")}, ${childrenAges.size} ${AppLanguageManager.translate("Çoc")}"

    Box(modifier = Modifier.weight(1.2f)) {
        TourOSTextField(
            value = departureCity.ifBlank { AppLanguageManager.translate("Tüm Kalkış Noktaları") },
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Nereden"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onDepartureCityClick() })
    }

    Box(modifier = Modifier.weight(1.3f)) {
        TourOSTextField(
            value = AppLanguageManager.translate(selectedRegion.ifBlank { AppLanguageManager.translate("Tüm Varış Noktaları") }),
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Nereye"),
            placeholder = AppLanguageManager.translate("Tüm Bölgeler"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onRegionClick() })
    }

    Box(modifier = Modifier.weight(1.1f)) {
        TourOSTextField(
            value = startDateText.ifBlank { AppLanguageManager.translate("gg.aa.yyyy") },
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Gidiş"),
            placeholder = AppLanguageManager.translate("gg.aa.yyyy"),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = TourOSColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onStartDateClick() })
    }

    Box(modifier = Modifier.weight(1.1f)) {
        TourOSTextField(
            value = if (isRoundTrip) endDateText.ifBlank { AppLanguageManager.translate("gg.aa.yyyy") } else AppLanguageManager.translate("Tek Yön"),
            onValueChange = {},
            readOnly = true,
            enabled = isRoundTrip,
            label = AppLanguageManager.translate("Dönüş"),
            placeholder = AppLanguageManager.translate("gg.aa.yyyy"),
            trailingIcon = if (isRoundTrip) {
                {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = TourOSColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        if (isRoundTrip) {
            Box(modifier = Modifier.matchParentSize().clickable { onEndDateClick() })
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.padding(horizontal = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onRoundTripChange(false) }) {
            RadioButton(
                selected = !isRoundTrip,
                onClick = { onRoundTripChange(false) },
                colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(AppLanguageManager.translate("Tek Yön"), style = TourOSTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onRoundTripChange(true) }) {
            RadioButton(
                selected = isRoundTrip,
                onClick = { onRoundTripChange(true) },
                colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(AppLanguageManager.translate("Gidiş & Dönüş"), style = TourOSTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
        }
    }

    Box(modifier = Modifier.weight(1.0f)) {
        TourOSTextField(
            value = touristSummary,
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Yolcu"),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TourOSColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onTouristClick() })
    }

    // ARAMA VE SIFIRLAMA BUTONLARI (UÇUŞ)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Button(
            onClick = onSearchClick,
            modifier = Modifier.height(if (onResetFiltersClick != null) 42.dp else 50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TourOSColors.Primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Flight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = AppLanguageManager.translate("UÇUŞLARI BUL"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        if (onResetFiltersClick != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onResetFiltersClick() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "↺",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.Primary
                )
                Text(
                    text = "Сбросить фильтры",
                    style = TourOSTypography.Caption.copy(
                        color = TourOSColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// ─── DİKEY MOBİL UÇUŞ ALANLARI ────────────────────────────────────────────────

@Composable
private fun ColumnScope.FlightSearchFields(
    departureCity: String,
    onDepartureCityClick: () -> Unit,
    selectedRegion: String,
    onRegionClick: () -> Unit,
    startDateText: String,
    onStartDateClick: () -> Unit,
    endDateText: String,
    onEndDateClick: () -> Unit,
    isRoundTrip: Boolean,
    onRoundTripChange: (Boolean) -> Unit,
    adults: Int,
    childrenAges: List<Int>,
    onTouristClick: () -> Unit,
    onSearchClick: () -> Unit,
    onResetFiltersClick: (() -> Unit)? = null,
    isCompact: Boolean
) {
    val touristSummary = if (childrenAges.isEmpty()) "$adults ${AppLanguageManager.translate("Yolcu")}" else "$adults ${AppLanguageManager.translate("Yet")}, ${childrenAges.size} ${AppLanguageManager.translate("Çoc")}"

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            TourOSTextField(
                value = departureCity.ifBlank { AppLanguageManager.translate("Tüm Kalkış Noktaları") },
                onValueChange = {},
                readOnly = true,
                label = AppLanguageManager.translate("Nereden"),
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onDepartureCityClick() })
        }

        Box(modifier = Modifier.weight(1f)) {
            TourOSTextField(
                value = AppLanguageManager.translate(selectedRegion.ifBlank { AppLanguageManager.translate("Tüm Varış Noktaları") }),
                onValueChange = {},
                readOnly = true,
                label = AppLanguageManager.translate("Nereye"),
                placeholder = AppLanguageManager.translate("Tüm Bölgeler"),
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onRegionClick() })
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            TourOSTextField(
                value = startDateText.ifBlank { AppLanguageManager.translate("gg.aa.yyyy") },
                onValueChange = {},
                readOnly = true,
                label = AppLanguageManager.translate("Gidiş"),
                placeholder = AppLanguageManager.translate("gg.aa.yyyy"),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = TourOSColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onStartDateClick() })
        }

        Box(modifier = Modifier.weight(1f)) {
            TourOSTextField(
                value = if (isRoundTrip) endDateText.ifBlank { AppLanguageManager.translate("gg.aa.yyyy") } else AppLanguageManager.translate("Tek Yön"),
                onValueChange = {},
                readOnly = true,
                enabled = isRoundTrip,
                label = AppLanguageManager.translate("Dönüş"),
                placeholder = AppLanguageManager.translate("gg.aa.yyyy"),
                trailingIcon = if (isRoundTrip) {
                    {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = TourOSColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            if (isRoundTrip) {
                Box(modifier = Modifier.matchParentSize().clickable { onEndDateClick() })
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onRoundTripChange(false) }) {
                RadioButton(
                    selected = !isRoundTrip,
                    onClick = { onRoundTripChange(false) },
                    colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppLanguageManager.translate("Tek Yön"), style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onRoundTripChange(true) }) {
                RadioButton(
                    selected = isRoundTrip,
                    onClick = { onRoundTripChange(true) },
                    colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppLanguageManager.translate("Gidiş & Dönüş"), style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold))
            }
        }

        Box(modifier = Modifier.widthIn(min = 140.dp)) {
            TourOSTextField(
                value = touristSummary,
                onValueChange = {},
                readOnly = true,
                label = AppLanguageManager.translate("Yolcu"),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TourOSColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onTouristClick() })
        }
    }

    Button(
        onClick = onSearchClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TourOSColors.Primary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Flight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = AppLanguageManager.translate("UÇUŞLARI BUL"),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }

    if (onResetFiltersClick != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable { onResetFiltersClick() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "↺ Сбросить фильтры",
                style = TourOSTypography.Caption.copy(
                    color = TourOSColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

// ─── EVRENSEL YOLCU & ÇOCUK YAŞI SEÇİCİ MODALI ────────────────────────────────

@Composable
fun UniversalTouristPickerDialog(
    adults: Int,
    childrenAges: List<Int>,
    onAdultsChange: (Int) -> Unit,
    onChildrenAgesChange: (List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempAdults by remember { mutableStateOf(adults) }
    var tempChildAges by remember { mutableStateOf(childrenAges) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TourOSColors.Surface,
            border = BorderStroke(1.dp, TourOSColors.Border),
            modifier = Modifier.width(380.dp).padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 ${AppLanguageManager.translate("Yolcu & Turist Sayısı")}",
                        style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Text("✕", fontSize = 14.sp, color = TourOSColors.TextSecondary)
                    }
                }

                // 1. Yetişkin Sayacı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(AppLanguageManager.translate("Yetişkinler"), style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(AppLanguageManager.translate("18 yaş ve üzeri"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilledIconButton(
                            onClick = { if (tempAdults > 1) tempAdults-- },
                            enabled = tempAdults > 1,
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = TourOSColors.PrimaryContainer)
                        ) {
                            Text("–", fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                        }
                        Text("$tempAdults", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.widthIn(min = 20.dp))
                        FilledIconButton(
                            onClick = { if (tempAdults < 8) tempAdults++ },
                            enabled = tempAdults < 8,
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = TourOSColors.PrimaryContainer)
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                        }
                    }
                }

                HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.5f))

                // 2. Çocuk Sayacı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(AppLanguageManager.translate("Çocuklar & Bebekler"), style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(AppLanguageManager.translate("0 - 17 yaş arası"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilledIconButton(
                            onClick = {
                                if (tempChildAges.isNotEmpty()) {
                                    tempChildAges = tempChildAges.dropLast(1)
                                }
                            },
                            enabled = tempChildAges.isNotEmpty(),
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = TourOSColors.PrimaryContainer)
                        ) {
                            Text("–", fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                        }
                        Text("${tempChildAges.size}", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.widthIn(min = 20.dp))
                        FilledIconButton(
                            onClick = {
                                if (tempChildAges.size < 5) {
                                    tempChildAges = tempChildAges + listOf(5)
                                }
                            },
                            enabled = tempChildAges.size < 5,
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = TourOSColors.PrimaryContainer)
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                        }
                    }
                }

                // 3. Çocuk Yaşları Seçimi
                if (tempChildAges.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TourOSColors.PrimaryContainer.copy(alpha = 0.25f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "👶 ${AppLanguageManager.translate("Çocuk Yaşları & İndirim Oranları:")}",
                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                        )

                        val currentLangCode = AppLanguageManager.currentLanguage.value.code
                        val formatAge: (Int) -> String = { a ->
                            when (currentLangCode) {
                                "ru" -> when {
                                    a == 1 -> "1 год"
                                    a in 2..4 -> "$a года"
                                    else -> "$a лет"
                                }
                                "en" -> if (a == 1) "1 Year" else "$a Years"
                                "de" -> if (a == 1) "1 Jahr" else "$a Jahre"
                                else -> "$a Yaş"
                            }
                        }

                        tempChildAges.forEachIndexed { index, age ->
                            var showAgeMenu by remember { mutableStateOf(false) }
                            val categoryText = when (currentLangCode) {
                                "ru" -> when {
                                    age <= 2 -> "(0-2 года: Скидка 90%)"
                                    age <= 6 -> "(3-6 лет: Скидка 50%)"
                                    age <= 12 -> "(7-12 лет: Скидка 30%)"
                                    else -> "(13-17 лет: Стандарт)"
                                }
                                "en" -> when {
                                    age <= 2 -> "(0-2 Years: 90% Discount)"
                                    age <= 6 -> "(3-6 Years: 50% Discount)"
                                    age <= 12 -> "(7-12 Years: 30% Discount)"
                                    else -> "(13-17 Years: Standard)"
                                }
                                "de" -> when {
                                    age <= 2 -> "(0-2 Jahre: 90% Rabatt)"
                                    age <= 6 -> "(3-6 Jahre: 50% Rabatt)"
                                    age <= 12 -> "(7-12 Jahre: 30% Rabatt)"
                                    else -> "(13-17 Jahre: Standard)"
                                }
                                else -> when {
                                    age <= 2 -> "(0-2 Yaş Bebek: %90 İndirim)"
                                    age <= 6 -> "(3-6 Yaş: %50 İndirim)"
                                    age <= 12 -> "(7-12 Yaş: %30 İndirim)"
                                    else -> "(13-17 Yaş: Standart)"
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${AppLanguageManager.translate("Çocuk")}: ${formatAge(age)} $categoryText",
                                    style = TourOSTypography.Caption.copy(fontSize = 11.sp)
                                )

                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = TourOSColors.Surface,
                                        border = BorderStroke(1.dp, TourOSColors.Border),
                                        modifier = Modifier.clickable { showAgeMenu = true }
                                    ) {
                                        Text(
                                            text = "${formatAge(age)} ▼",
                                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showAgeMenu,
                                        onDismissRequest = { showAgeMenu = false },
                                        modifier = Modifier.heightIn(max = 200.dp)
                                    ) {
                                        (0..17).forEach { possibleAge ->
                                            DropdownMenuItem(
                                                text = { Text(formatAge(possibleAge), style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                onClick = {
                                                    tempChildAges = tempChildAges.mapIndexed { i, a -> if (i == index) possibleAge else a }
                                                    showAgeMenu = false
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                TourOSButton(
                    text = AppLanguageManager.translate("Uygula & Kaydet"),
                    onClick = {
                        onAdultsChange(tempAdults)
                        onChildrenAgesChange(tempChildAges)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * B2B & Public Web Ortak Detaylı Filtre Paneli
 * - Sahil Şeridi, Beslenme Konsepti, Yıldız, Puan
 * - Paket Tur Otelleri Seçimi & Donanım/Özellikler
 * - Anında Onay, Direkt Uçuş, Transfer Dahil
 * - Tur Operatörü Çoklu Seçim Dropdown
 */
@Composable
private fun DetailedFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) TourOSColors.Primary else TourOSColors.Surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) TourOSColors.Primary else TourOSColors.Border
        ),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    style = TourOSTypography.Caption.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
            Text(
                text = label,
                style = TourOSTypography.Caption.copy(
                    color = if (selected) Color.White else TourOSColors.TextPrimary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun UniversalTourDetailedFilters(
    activeSearchTab: String = "TOURS",
    isExpanded: Boolean = true,
    onExpandedToggle: () -> Unit = {},
    onResetAllFilters: () -> Unit = {},
    selectedBeachLine: Int = 0,
    onBeachLineChange: (Int) -> Unit = {},
    dbMealTypes: List<String> = listOf("UAI", "AI", "FB", "HB", "BB"),
    selectedMealTypes: Set<String> = emptySet(),
    onMealTypesChange: (Set<String>) -> Unit = {},
    selectedStars: Set<Int> = emptySet(),
    onStarsChange: (Set<Int>) -> Unit = {},
    minRating: Double = 0.0,
    onMinRatingChange: (Double) -> Unit = {},
    dbProductHotels: List<String> = emptyList(),
    selectedHotels: Set<String> = emptySet(),
    onHotelsChange: (Set<String>) -> Unit = {},
    selectedAmenities: Set<String> = emptySet(),
    onAmenitiesChange: (Set<String>) -> Unit = {},
    isInstantOnly: Boolean = false,
    onInstantOnlyChange: (Boolean) -> Unit = {},
    isDirectFlightOnly: Boolean = false,
    onDirectFlightOnlyChange: (Boolean) -> Unit = {},
    isTransferIncludedOnly: Boolean = false,
    onTransferIncludedOnlyChange: (Boolean) -> Unit = {},
    dbOperators: List<String> = emptyList(),
    selectedOperators: Set<String> = emptySet(),
    onOperatorsChange: (Set<String>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (activeSearchTab != "TOURS" && activeSearchTab != "HOTELS") return

    var showHotelDropdown by remember { mutableStateOf(false) }
    var showOperatorDropdown by remember { mutableStateOf(false) }
    var operatorSearchText by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TourOSColors.Surface,
        border = BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border),
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier.padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // FİLTRE BAŞLIK VE AÇILIR/KAPANIR TETİKLEYİCİ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    modifier = Modifier.clickable { onExpandedToggle() }
                ) {
                    Text(
                        text = AppLanguageManager.translate("Detaylı Filtreler (Sahil, Beslenme, Yıldız, Puan, Otel & Donanım)"),
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isExpanded) "▲ (${AppLanguageManager.translate("Gizle")})" else "▼ (${AppLanguageManager.translate("Göster")})",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onResetAllFilters) {
                        Text("↺ ${AppLanguageManager.translate("Filtreleri Sıfırla")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                    }
                }
            }

            // İÇERİK (GENİŞLETİLDİĞİNDE GÖRÜNÜR)
            if (isExpanded) {
                HorizontalDivider(color = TourOSColors.Border)

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isFilterNarrow = maxWidth < 1120.dp

                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                        if (isFilterNarrow) {
                            // ── DAR EKRAN / TABLET / MOBİL (2'Lİ SATIRLAR) ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                // 1. Sahil Şeridi (Denize Mesafe)
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(AppLanguageManager.translate("Sahil Şeridi (Denize Mesafe):"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(0 to "Hepsi", 1 to "< 100m", 2 to "< 500m", 3 to "< 2km").forEach { (code, label) ->
                                            val isSelected = (selectedBeachLine == code)
                                            DetailedFilterChip(
                                                selected = isSelected,
                                                onClick = { onBeachLineChange(code) },
                                                label = AppLanguageManager.translate(label)
                                            )
                                        }
                                    }
                                }

                                // 2. Beslenme / Konsept
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(AppLanguageManager.translate("Beslenme / Konsept:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        dbMealTypes.forEach { meal ->
                                            val isSelected = selectedMealTypes.contains(meal)
                                            DetailedFilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    onMealTypesChange(if (isSelected) selectedMealTypes - meal else selectedMealTypes + meal)
                                                },
                                                label = meal
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                // 3. Otel Kategorisi (Yıldız)
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(AppLanguageManager.translate("Otel Kategorisi:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(5, 4, 3, 2).forEach { star ->
                                            val isSelected = selectedStars.contains(star)
                                            DetailedFilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    onStarsChange(if (isSelected) selectedStars - star else selectedStars + star)
                                                },
                                                label = "$star★"
                                            )
                                        }
                                    }
                                }

                                // 4. Otel Puanı (Misafir Değerlendirmesi)
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(AppLanguageManager.translate("Otel Puanı:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(0.0 to "Hepsi", 7.0 to "7.0+", 8.0 to "8.0+", 9.0 to "9.0+").forEach { (rVal, label) ->
                                            val isSelected = (minRating == rVal)
                                            DetailedFilterChip(
                                                selected = isSelected,
                                                onClick = { onMinRatingChange(rVal) },
                                                label = AppLanguageManager.translate(label)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // ── GENİŞ EKRAN (4 SÜTUN YAN YANA) ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                verticalAlignment = Alignment.Top
                            ) {
                                // 1. Sahil Şeridi (Denize Mesafe)
                                Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${AppLanguageManager.translate("Sahil Şeridi (Denize Mesafe)")}:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(0 to "Hepsi", 1 to "< 100m", 2 to "< 500m", 3 to "< 2km").forEach { (code, label) ->
                                            val isSelected = (selectedBeachLine == code)
                                            DetailedFilterChip(
                                                selected = isSelected,
                                                onClick = { onBeachLineChange(code) },
                                                label = AppLanguageManager.translate(label)
                                            )
                                        }
                                    }
                                }

                                // 2. Beslenme / Konsept (Çoklu Seçim)
                                Column(
                                    modifier = Modifier.weight(1.1f),
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("${AppLanguageManager.translate("Beslenme / Konsept")}:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        dbMealTypes.forEach { meal ->
                                            val isSelected = selectedMealTypes.contains(meal)
                                            DetailedFilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    onMealTypesChange(if (isSelected) selectedMealTypes - meal else selectedMealTypes + meal)
                                                },
                                                label = meal
                                            )
                                        }
                                    }
                                }

                                // 3. Otel Kategorisi (Yıldız)
                                Column(modifier = Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${AppLanguageManager.translate("Otel Kategorisi")}:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(5, 4, 3, 2).forEach { star ->
                                            val isSelected = selectedStars.contains(star)
                                            DetailedFilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    onStarsChange(if (isSelected) selectedStars - star else selectedStars + star)
                                                },
                                                label = "$star★"
                                            )
                                        }
                                    }
                                }

                                // 4. Otel Puanı (Misafir Değerlendirmesi)
                                Column(modifier = Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${AppLanguageManager.translate("Otel Puanı")}:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(0.0 to "Hepsi", 7.0 to "7.0+", 8.0 to "8.0+", 9.0 to "9.0+").forEach { (rVal, label) ->
                                            val isSelected = (minRating == rVal)
                                            DetailedFilterChip(
                                                selected = isSelected,
                                                onClick = { onMinRatingChange(rVal) },
                                                label = AppLanguageManager.translate(label)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = TourOSColors.Border)

                // SATIR 2: PAKET TUR OTELLERİ SEÇİMİ & DONANIM/HİZMETLER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Otel Seçimi Dropdown
                    Box(modifier = Modifier.weight(1.1f)) {
                        TourOSTextField(
                            value = if (selectedHotels.isEmpty()) "${AppLanguageManager.translate("Tüm Paket Tur Otelleri")} (${dbProductHotels.size}) ▼" else "${selectedHotels.size} ${AppLanguageManager.translate("Otel Seçili")} ▼",
                            onValueChange = { },
                            readOnly = true,
                            label = AppLanguageManager.translate("Paket Tur Otelleri Seçin (Tümü)"),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showHotelDropdown = !showHotelDropdown }
                        )

                        DropdownMenu(
                            expanded = showHotelDropdown,
                            onDismissRequest = { showHotelDropdown = false },
                            modifier = Modifier.width(420.dp).background(TourOSColors.Surface)
                        ) {
                            Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                DropdownMenuItem(
                                    text = { Text("✓ ${AppLanguageManager.translate("Tüm Paket Tur Otelleri")}", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary, fontSize = 12.sp)) },
                                    onClick = {
                                        onHotelsChange(emptySet())
                                        showHotelDropdown = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                )
                                HorizontalDivider(color = TourOSColors.Border)

                                Column(
                                    modifier = Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    dbProductHotels.forEach { hotelName ->
                                        val isChecked = selectedHotels.contains(hotelName)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable {
                                                    onHotelsChange(if (isChecked) selectedHotels - hotelName else selectedHotels + hotelName)
                                                }
                                                .padding(vertical = 3.dp, horizontal = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isChecked) TourOSColors.Primary else Color.Transparent)
                                                    .border(1.dp, if (isChecked) TourOSColors.Primary else TourOSColors.Border, RoundedCornerShape(4.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isChecked) {
                                                    Text("✓", style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(hotelName, style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Donanım & Hizmetler Filter Chips
                    Column(modifier = Modifier.weight(1.4f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${AppLanguageManager.translate("Donanım & Özellikler")}:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            listOf("Aquapark", "Wi-Fi", "SPA", "Kum Plaj", "Çocuk Kulübü", "Havuz").forEach { am ->
                                val isSelected = am in selectedAmenities
                                DetailedFilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        onAmenitiesChange(if (isSelected) selectedAmenities - am else selectedAmenities + am)
                                    },
                                    label = AppLanguageManager.translate(am)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = TourOSColors.Border)

                // SATIR 3: HIZLI ONAY VE ULAŞIM SEÇENEKLERİ (CHECKBOX GRUBU) & TUR OPERATÖRÜ SEÇİMİ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onInstantOnlyChange(!isInstantOnly) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = isInstantOnly,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = TourOSColors.Primary,
                                    uncheckedColor = TourOSColors.Border
                                )
                            )
                            Text(
                                text = AppLanguageManager.translate("Anında Onaylı Turlar"),
                                style = TourOSTypography.BodyMedium.copy(
                                    color = if (isInstantOnly) TourOSColors.Primary else TourOSColors.TextPrimary,
                                    fontWeight = if (isInstantOnly) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }

                        if (activeSearchTab == "TOURS") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onDirectFlightOnlyChange(!isDirectFlightOnly) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = isDirectFlightOnly,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = TourOSColors.Primary,
                                        uncheckedColor = TourOSColors.Border
                                    )
                                )
                                Text(
                                    text = AppLanguageManager.translate("Aktarmasız / Direkt Uçuş"),
                                    style = TourOSTypography.BodyMedium.copy(
                                        color = if (isDirectFlightOnly) TourOSColors.Primary else TourOSColors.TextPrimary,
                                        fontWeight = if (isDirectFlightOnly) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onTransferIncludedOnlyChange(!isTransferIncludedOnly) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = isTransferIncludedOnly,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = TourOSColors.Primary,
                                    uncheckedColor = TourOSColors.Border
                                )
                            )
                            Text(
                                text = AppLanguageManager.translate("Transfer Dahil"),
                                style = TourOSTypography.BodyMedium.copy(
                                    color = if (isTransferIncludedOnly) TourOSColors.Primary else TourOSColors.TextPrimary,
                                    fontWeight = if (isTransferIncludedOnly) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Tur Operatörü Seçim Kutusu
                    Box(modifier = Modifier.width(300.dp)) {
                        TourOSTextField(
                            value = if (selectedOperators.isEmpty()) "${AppLanguageManager.translate("Tüm Tur Operatörleri")} (${dbOperators.size}) ▼" else "${selectedOperators.size} ${AppLanguageManager.translate("Operatör Seçili")} ▼",
                            onValueChange = { },
                            readOnly = true,
                            label = AppLanguageManager.translate("Tur Operatörü Seçin (Tümü)"),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showOperatorDropdown = !showOperatorDropdown }
                        )

                        DropdownMenu(
                            expanded = showOperatorDropdown,
                            onDismissRequest = { showOperatorDropdown = false },
                            containerColor = Color.White,
                            modifier = Modifier
                                .width(320.dp)
                                .heightIn(max = 380.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, TourOSColors.Border, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                TourOSTextField(
                                    value = operatorSearchText,
                                    onValueChange = { operatorSearchText = it },
                                    placeholder = AppLanguageManager.translate("Operatör ara..."),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { onOperatorsChange(emptySet()) }) {
                                        Text(AppLanguageManager.translate("Temizle"), fontSize = 11.sp, color = TourOSColors.TextSecondary)
                                    }
                                    TextButton(onClick = { onOperatorsChange(dbOperators.toSet()) }) {
                                        Text(AppLanguageManager.translate("Tümünü Seç"), fontSize = 11.sp, color = TourOSColors.Primary)
                                    }
                                }
                            }
                            HorizontalDivider(color = TourOSColors.Border)
                            val filteredOps = dbOperators.filter { it.contains(operatorSearchText, ignoreCase = true) }
                            filteredOps.forEach { opName ->
                                val isChecked = opName in selectedOperators
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = null,
                                                colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = opName, 
                                                style = TourOSTypography.BodyMedium.copy(
                                                    fontSize = 12.sp, 
                                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        }
                                    },
                                    onClick = {
                                        onOperatorsChange(if (isChecked) selectedOperators - opName else selectedOperators + opName)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

