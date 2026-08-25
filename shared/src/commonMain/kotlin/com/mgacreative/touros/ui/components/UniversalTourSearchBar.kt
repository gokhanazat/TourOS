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
    extraBottomContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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
        HierarchicalDestinationPickerDialog(
            currentSelection = selectedRegion,
            onDestinationSelected = { destItem ->
                onRegionChange(destItem.name)
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
                "TOURS" to "🏝️ ${AppLanguageManager.translate("Turlar & Paketler")}",
                "FLIGHTS" to "✈️ ${AppLanguageManager.translate("Uçak Bileti")}",
                "HOTELS" to "🏨 ${AppLanguageManager.translate("Sadece Otel")}"
            ).forEach { (tabKey, tabLabel) ->
                val isSelected = activeTab.uppercase() == tabKey || (tabKey == "FLIGHTS" && activeTab.uppercase() == "FLIGHT")
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isSelected -> if (isHero) Color(0xFF1E293B) else TourOSColors.Primary
                        else -> if (isHero) Color(0xFFF1F5F9) else TourOSColors.PrimaryContainer.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.clickable { onTabChange(tabKey) }
                ) {
                    Text(
                        text = tabLabel,
                        style = TourOSTypography.Caption.copy(
                            color = if (isSelected) Color.White else if (isHero) Color(0xFF475569) else TourOSColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
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
                            isCompact = false
                        )
                    }
                }
            } else {
                // ── 3. TUR VE OTEL ARAMA FORMU (KALKIŞ, DESTİNASYON, TARİH ARALIĞI, GECE, TURİST) ─
                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TourSearchFields(
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
    isCompact: Boolean,
    isHero: Boolean
) {
    val touristSummary = if (childrenAges.isEmpty()) {
        "$adults ${AppLanguageManager.translate("Yetişkin")}"
    } else {
        "$adults ${AppLanguageManager.translate("Yet")}, ${childrenAges.size} ${AppLanguageManager.translate("Çoc")} (${childrenAges.joinToString(",") { "${it}y" }})"
    }

    // 1. NEREDEN
    Box(modifier = Modifier.weight(1.3f)) {
        TourOSTextField(
            value = departureCity.ifBlank { AppLanguageManager.translate("Tüm Kalkış Şehirleri") },
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Nereden (Kalkış Şehri)"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onDepartureCityClick() })
    }

    // 2. NEREYE
    Box(modifier = Modifier.weight(1.4f)) {
        TourOSTextField(
            value = selectedRegion.ifBlank { AppLanguageManager.translate("Tüm Destinasyonlar / Ülkeler") },
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Nereye (Destinasyon / Otel)"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onRegionClick() })
    }

    // 3. GİDİŞ TARİH ARALIĞI
    Box(modifier = Modifier.weight(1.3f)) {
        TourOSTextField(
            value = "$startDateText — $endDateText 📅",
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Tarih Aralığı"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onDateRangeClick() })
    }

    // 4. GECE SAYISI
    Box(modifier = Modifier.weight(0.9f)) {
        TourOSTextField(
            value = "$nightsText ▼",
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Gece Sayısı"),
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
            value = "$touristSummary ▼",
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Turist"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onTouristClick() })
    }

    // 6. ARAMA BUTONU
    TourOSButton(
        text = "🔍 ${AppLanguageManager.translate("TURLARI BUL")}",
        onClick = onSearchClick,
        modifier = Modifier.height(50.dp)
    )
}

// ─── DİKEY MOBİL TUR ALANLARI ──────────────────────────────────────────────────

@Composable
private fun ColumnScope.TourSearchFields(
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
    isCompact: Boolean,
    isHero: Boolean
) {
    val touristSummary = if (childrenAges.isEmpty()) {
        "$adults ${AppLanguageManager.translate("Yetişkin")}"
    } else {
        "$adults ${AppLanguageManager.translate("Yet")}, ${childrenAges.size} ${AppLanguageManager.translate("Çoc")} (${childrenAges.joinToString(",") { "${it}y" }})"
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        TourOSTextField(
            value = departureCity.ifBlank { AppLanguageManager.translate("Tüm Kalkış Şehirleri") },
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Nereden (Kalkış Şehri)"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onDepartureCityClick() })
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        TourOSTextField(
            value = selectedRegion.ifBlank { AppLanguageManager.translate("Tüm Destinasyonlar / Ülkeler") },
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Nereye (Destinasyon / Otel)"),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onRegionClick() })
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        TourOSTextField(
            value = "$startDateText — $endDateText 📅",
            onValueChange = {},
            readOnly = true,
            label = AppLanguageManager.translate("Tarih Aralığı"),
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
                value = "$nightsText ▼",
                onValueChange = {},
                readOnly = true,
                label = AppLanguageManager.translate("Gece Sayısı"),
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
                value = "$touristSummary ▼",
                onValueChange = {},
                readOnly = true,
                label = AppLanguageManager.translate("Turist"),
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onTouristClick() })
        }
    }

    TourOSButton(
        text = "🔍 ${AppLanguageManager.translate("TURLARI BUL")}",
        onClick = onSearchClick,
        modifier = Modifier.fillMaxWidth().height(48.dp)
    )
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
    isCompact: Boolean
) {
    val touristSummary = if (childrenAges.isEmpty()) "$adults ${AppLanguageManager.translate("Yolcu")}" else "$adults Yetişkin, ${childrenAges.size} Çoc"

    Box(modifier = Modifier.weight(1.2f)) {
        TourOSTextField(
            value = departureCity.ifBlank { "Tüm Kalkış Noktaları" },
            onValueChange = {},
            readOnly = true,
            label = "Nereden",
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onDepartureCityClick() })
    }

    Box(modifier = Modifier.weight(1.3f)) {
        TourOSTextField(
            value = selectedRegion.ifBlank { "Tüm Varış Noktaları" },
            onValueChange = {},
            readOnly = true,
            label = "Nereye",
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onRegionClick() })
    }

    Box(modifier = Modifier.weight(1.1f)) {
        TourOSTextField(
            value = "$startDateText 📅",
            onValueChange = {},
            readOnly = true,
            label = "Gidiş",
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onStartDateClick() })
    }

    Box(modifier = Modifier.weight(1.1f)) {
        TourOSTextField(
            value = if (isRoundTrip) "$endDateText 📅" else "Tek Yön",
            onValueChange = {},
            readOnly = true,
            enabled = isRoundTrip,
            label = "Dönüş",
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
            Text("Tek Yön", style = TourOSTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onRoundTripChange(true) }) {
            RadioButton(
                selected = isRoundTrip,
                onClick = { onRoundTripChange(true) },
                colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text("Gidiş-Dönüş", style = TourOSTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
        }
    }

    Box(modifier = Modifier.weight(1.0f)) {
        TourOSTextField(
            value = "$touristSummary ▼",
            onValueChange = {},
            readOnly = true,
            label = "Yolcu",
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { onTouristClick() })
    }

    TourOSButton(
        text = "✈️ UÇUŞ BUL",
        onClick = onSearchClick,
        modifier = Modifier.height(50.dp)
    )
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
    isCompact: Boolean
) {
    val touristSummary = if (childrenAges.isEmpty()) "$adults ${AppLanguageManager.translate("Yolcu")}" else "$adults Yetişkin, ${childrenAges.size} Çoc"

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            TourOSTextField(
                value = departureCity.ifBlank { "Tüm Kalkış Noktaları" },
                onValueChange = {},
                readOnly = true,
                label = "Nereden",
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onDepartureCityClick() })
        }

        Box(modifier = Modifier.weight(1f)) {
            TourOSTextField(
                value = selectedRegion.ifBlank { "Tüm Varış Noktaları" },
                onValueChange = {},
                readOnly = true,
                label = "Nereye",
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onRegionClick() })
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            TourOSTextField(
                value = "$startDateText 📅",
                onValueChange = {},
                readOnly = true,
                label = "Gidiş",
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onStartDateClick() })
        }

        Box(modifier = Modifier.weight(1f)) {
            TourOSTextField(
                value = if (isRoundTrip) "$endDateText 📅" else "Tek Yön",
                onValueChange = {},
                readOnly = true,
                enabled = isRoundTrip,
                label = "Dönüş",
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
                Text("Tek Yön", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onRoundTripChange(true) }) {
                RadioButton(
                    selected = isRoundTrip,
                    onClick = { onRoundTripChange(true) },
                    colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gidiş-Dönüş", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold))
            }
        }

        Box(modifier = Modifier.widthIn(min = 140.dp)) {
            TourOSTextField(
                value = "$touristSummary ▼",
                onValueChange = {},
                readOnly = true,
                label = "Yolcu",
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { onTouristClick() })
        }
    }

    TourOSButton(
        text = "✈️ UÇUŞ BUL",
        onClick = onSearchClick,
        modifier = Modifier.fillMaxWidth().height(48.dp)
    )
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

                        tempChildAges.forEachIndexed { index, age ->
                            var showAgeMenu by remember { mutableStateOf(false) }
                            val categoryText = when {
                                age <= 2 -> "(0-2 Yaş Bebek: %90 İndirim)"
                                age <= 6 -> "(3-6 Yaş: %50 İndirim)"
                                age <= 12 -> "(7-12 Yaş: %30 İndirim)"
                                else -> "(13-17 Yaş: Standart)"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${AppLanguageManager.translate("Çocuk")}: $age ${AppLanguageManager.translate("Yaşında")} $categoryText",
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
                                            text = "$age Yaş ▼",
                                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showAgeMenu,
                                        onDismissRequest = { showAgeMenu = false },
                                        modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())
                                    ) {
                                        (0..17).forEach { possibleAge ->
                                            DropdownMenuItem(
                                                text = { Text("$possibleAge ${AppLanguageManager.translate("Yaşında")}", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
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
