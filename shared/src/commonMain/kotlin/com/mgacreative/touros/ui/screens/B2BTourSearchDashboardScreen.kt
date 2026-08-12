package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.*
import org.koin.compose.viewmodel.koinViewModel

/**
 * TEK SAYFA MASTER PANELİ: Arama Filtreleri + Sonuç Matrisi + Alternatif Uçuşlar + Ekstra Hizmetler + Yolcu Formu
 */
@Composable
fun B2BTourSearchDashboardScreen(
    viewModel: B2BTourSearchViewModel = koinViewModel(),
    isEmbedded: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onSelectTourForBooking: (productId: String) -> Unit = {},
    onNavigateToBookings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val availableFlightOptions by viewModel.availableFlightOptions.collectAsState()
    val selectedFlightOption by viewModel.selectedFlightOption.collectAsState()
    val extraServices by viewModel.extraServices.collectAsState()
    val passengers by viewModel.passengers.collectAsState()
    val createdPnrCode by viewModel.createdPnrCode.collectAsState()

    var activeSearchTab by remember { mutableStateOf("TOURS") } // "TOURS", "HOTELS", "FLIGHTS", "LOCAL_TOURS", "LOCAL_HOTELS"
    var departureCity by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("") }
    var nights by remember { mutableStateOf(7) }
    var adults by remember { mutableStateOf(2) }
    var childs by remember { mutableStateOf(0) }

    // Ayrıştırılmış ve Pop-up Açılır Takvim Destekli Tarih State'leri
    var startDateText by remember { mutableStateOf("18.08.2026") }
    var endDateText by remember { mutableStateOf("25.08.2026") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var selectedMealTypes by remember { mutableStateOf(setOf("UAI", "AI", "FB", "HB", "BB")) }
    var selectedOperators by remember { mutableStateOf(setOf("Coral Travel", "Pegas Touristik", "Anex Tour", "Fun & Sun", "MGA Creative")) }

    var selectedStars by remember { mutableStateOf(setOf(3, 4, 5)) }
    var isInstantOnly by remember { mutableStateOf(false) }
    var isPromoOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedHotels by remember { mutableStateOf(emptySet<String>()) }
    var isRoundTrip by remember { mutableStateOf(true) }
    var activeStep by remember { mutableStateOf(1) }
    var showSuccessModal by remember { mutableStateOf(false) }

    // Veritabanından gelen benzersiz Kalkış Şehirleri ve Destinasyonlar
    val allDbProducts = (uiState as? B2BTourSearchUiState.Success)?.allProducts ?: emptyList()
    val dbDepartureCities = remember(allDbProducts) {
        allDbProducts.map { it.departureCity }.filter { it.isNotBlank() && it != "Yerel Otel" }.distinct().sorted().ifEmpty { listOf("Moskova", "Saint Petersburg", "Kazan", "Yekaterinburg", "İstanbul").sorted() }
    }
    val dbDestinations = remember(allDbProducts) {
        allDbProducts.map { "${it.country} - ${it.region}" }.filter { it.isNotBlank() && !it.startsWith(" -") }.distinct().sorted().ifEmpty { listOf("Türkiye - Antalya", "Mısır - Sharm El Sheikh", "BAE - Dubai", "Rusya - Soçi").sorted() }
    }

    // Pop-Up Takvim Modalları
    if (showStartDatePicker) {
        SimpleDatePickerDialog(
            title = "Gidiş Tarihi (Başlangıç) Seçin",
            initialDateText = startDateText,
            onDateSelected = { startDateText = it },
            onDismissRequest = { showStartDatePicker = false }
        )
    }
    if (showEndDatePicker) {
        SimpleDatePickerDialog(
            title = "Gidiş Tarihi (Bitiş) Seçin",
            initialDateText = endDateText,
            onDateSelected = { endDateText = it },
            onDismissRequest = { showEndDatePicker = false }
        )
    }

    val content: @Composable (PaddingValues) -> Unit = { padding ->
        val columnModifier = if (isEmbedded) {
            Modifier.fillMaxWidth().padding(TourOSSpacing.large)
        } else {
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(TourOSSpacing.large)
        }
        Column(
            modifier = columnModifier,
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // ── ADIM YÖNLENDİRME ÇUBUĞU (STEPPER NAVIGATION BAR) ─────────────────
            WizardStepHeaderBar(
                currentStep = activeStep,
                onStepClick = { step ->
                    if (step == 1 || selectedProduct != null) {
                        activeStep = step
                    }
                }
            )

            when (activeStep) {
                1 -> {
                    // ── TOUROS 0.3 DİLİNE UYGUN AKILLI B2B ARAMA PANERİ ───────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                            .border(TourOSSpacing.borderWidth, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadius))
                            .background(TourOSColors.Surface)
                    ) {
                        // 1. ÜST SEKMELER (TourOS Kurumsal Teması)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TourOSColors.Primary),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            listOf(
                                "Paket Turlar" to "TOURS",
                                "Oteller" to "HOTELS",
                                "Uçuşlar" to "FLIGHTS",
                                "Yerel Turlar" to "LOCAL_TOURS",
                                "Yerel Oteller" to "LOCAL_HOTELS"
                            ).forEach { (label, key) ->
                                val isSelected = (activeSearchTab == key)
                                Surface(
                                    modifier = Modifier
                                        .clickable { 
                                            activeSearchTab = key 
                                            if (key == "FLIGHTS") {
                                                departureCity = ""
                                                selectedRegion = ""
                                            }
                                        },
                                    color = if (isSelected) TourOSColors.PrimaryContainer else Color.Transparent
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.medium),
                                        style = TourOSTypography.TitleMedium.copy(
                                            color = if (isSelected) TourOSColors.Primary else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                        }

                        // 2. AKILLI ARAMA BARI (Nereden | Nereye | Başlangıç Tarihi | Bitiş Tarihi | Gece | Turist)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = TourOSColors.Background
                        ) {
                            Column(
                                modifier = Modifier.padding(TourOSSpacing.large),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                var showDepartureDropdown by remember { mutableStateOf(false) }
                                var showRegionDropdown by remember { mutableStateOf(false) }
                                var showNightsDropdown by remember { mutableStateOf(false) }
                                var showTouristsDropdown by remember { mutableStateOf(false) }

                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val windowWidthClass = com.mgacreative.touros.ui.theme.getWindowWidthClass(maxWidth)
                                    val isCompact = windowWidthClass == com.mgacreative.touros.ui.theme.WindowWidthClass.COMPACT

                                    if (activeSearchTab == "FLIGHTS") {
                                        // ── UÇUŞLAR SEKME ÖZEL ARAMA BARI (BİREBİR GÖRSELE UYGUN) ───────────
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 1. NEREDEN DROPDOWN
                                            Box(modifier = Modifier.weight(1.2f)) {
                                                TourOSTextField(
                                                    value = departureCity.ifBlank { "Tüm Kalkış Şehirleri" },
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = "Nereden (Kalkış Şehri)",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .clickable { showDepartureDropdown = !showDepartureDropdown }
                                                )
                                                DropdownMenu(
                                                    expanded = showDepartureDropdown && dbDepartureCities.isNotEmpty(),
                                                    onDismissRequest = { showDepartureDropdown = false },
                                                    modifier = Modifier.width(260.dp).background(TourOSColors.Surface)
                                                ) {
                                                    Column(modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                                                        DropdownMenuItem(
                                                            text = { Text("✓ Tüm Kalkış Şehirleri (Tümü)", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary, fontSize = 12.sp)) },
                                                            onClick = {
                                                                departureCity = ""
                                                                showDepartureDropdown = false
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        )
                                                        HorizontalDivider(color = TourOSColors.Border)
                                                        dbDepartureCities.forEach { city ->
                                                            DropdownMenuItem(
                                                                text = { Text("✈️ $city", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                                onClick = {
                                                                    departureCity = city
                                                                    showDepartureDropdown = false
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                                modifier = Modifier.height(28.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // 2. NEREYE DROPDOWN
                                            Box(modifier = Modifier.weight(1.4f)) {
                                                TourOSTextField(
                                                    value = selectedRegion.ifBlank { "Tüm Destinasyonlar" },
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = "Nereye (Destinasyon / Ülke)",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .clickable { showRegionDropdown = !showRegionDropdown }
                                                )
                                                DropdownMenu(
                                                    expanded = showRegionDropdown && dbDestinations.isNotEmpty(),
                                                    onDismissRequest = { showRegionDropdown = false },
                                                    modifier = Modifier.width(300.dp).background(TourOSColors.Surface)
                                                ) {
                                                    Column(modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                                                        DropdownMenuItem(
                                                            text = { Text("✓ Tüm Destinasyonlar / Ülkeler (Tümü)", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary, fontSize = 12.sp)) },
                                                            onClick = {
                                                                selectedRegion = ""
                                                                showRegionDropdown = false
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        )
                                                        HorizontalDivider(color = TourOSColors.Border)
                                                        dbDestinations.forEach { dest ->
                                                            DropdownMenuItem(
                                                                text = { Text("📍 $dest", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                                onClick = {
                                                                    selectedRegion = dest.substringAfter("- ").ifBlank { dest }
                                                                    showRegionDropdown = false
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                                modifier = Modifier.height(28.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // 3. GİDİŞ TARİHİ
                                            Box(modifier = Modifier.weight(1.1f)) {
                                                TourOSTextField(
                                                    value = "$startDateText 📅",
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = "Gidiş",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
                                            }

                                            // 4. DÖNÜŞ TARİHİ (TEK YÖN İSE KİLİTLİ)
                                            Box(modifier = Modifier.weight(1.1f)) {
                                                TourOSTextField(
                                                    value = if (isRoundTrip) "$endDateText 📅" else "Tek Yön (Yok)",
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    enabled = isRoundTrip,
                                                    label = "Dönüş",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                if (isRoundTrip) {
                                                    Box(modifier = Modifier.matchParentSize().clickable { showEndDatePicker = true })
                                                }
                                            }

                                            // 5. TEK YÖN / GİDİŞ-DÖNÜŞ SEÇİM KUTUCUKLARI
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(1.dp),
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.clickable { isRoundTrip = false }
                                                ) {
                                                    RadioButton(
                                                        selected = !isRoundTrip,
                                                        onClick = { isRoundTrip = false },
                                                        colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary),
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Tek Yön", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.clickable { isRoundTrip = true }
                                                ) {
                                                    RadioButton(
                                                        selected = isRoundTrip,
                                                        onClick = { isRoundTrip = true },
                                                        colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary),
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Gidiş-Dönüş", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                                }
                                            }

                                            // 6. YOLCU SAYISI DROPDOWN
                                            Box(modifier = Modifier.weight(1.1f)) {
                                                TourOSTextField(
                                                    value = ("$adults Yetişkin" + (if (childs > 0) " + $childs Çoc" else "")) + " ▼",
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = "Yolcu Sayısı",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(modifier = Modifier.matchParentSize().clickable { showTouristsDropdown = !showTouristsDropdown })
                                                DropdownMenu(
                                                    expanded = showTouristsDropdown,
                                                    onDismissRequest = { showTouristsDropdown = false },
                                                    modifier = Modifier.width(220.dp).background(TourOSColors.Surface)
                                                ) {
                                                    listOf(
                                                        "1 Yetişkin" to (1 to 0),
                                                        "2 Yetişkin" to (2 to 0),
                                                        "2 Yetişkin + 1 Çocuk" to (2 to 1),
                                                        "2 Yetişkin + 2 Çocuk" to (2 to 2),
                                                        "3 Yetişkin" to (3 to 0),
                                                        "4 Yetişkin" to (4 to 0)
                                                    ).forEach { (label, counts) ->
                                                        DropdownMenuItem(
                                                            text = { Text(label, style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                            onClick = {
                                                                adults = counts.first
                                                                childs = counts.second
                                                                showTouristsDropdown = false
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // 7. UÇUŞ ARA BUTONU
                                            TourOSButton(
                                                text = "UÇUŞ ARA",
                                                onClick = { viewModel.performSearch() }
                                            )
                                        }
                                    } else if (isCompact) {
                                        // ── Mobil Dikey Düzen ────────────────────────────────
                                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                            // 1. NEREDEN DROPDOWN
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                TourOSTextField(
                                                    value = departureCity,
                                                    onValueChange = {
                                                        departureCity = it
                                                        showDepartureDropdown = true
                                                    },
                                                    label = "Nereden (Kalkış Şehri)",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                val matchingCities = dbDepartureCities.filter { isFuzzyMatch(departureCity, it) }
                                                DropdownMenu(
                                                    expanded = showDepartureDropdown && matchingCities.isNotEmpty(),
                                                    onDismissRequest = { showDepartureDropdown = false },
                                                    modifier = Modifier.fillMaxWidth(0.9f).background(TourOSColors.Surface)
                                                ) {
                                                    Column(modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                                                        matchingCities.forEach { city ->
                                                            DropdownMenuItem(
                                                                text = { Text("✈️ $city", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                                onClick = {
                                                                    departureCity = city
                                                                    showDepartureDropdown = false
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // 2. NEREYE DROPDOWN
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                TourOSTextField(
                                                    value = selectedRegion,
                                                    onValueChange = {
                                                        selectedRegion = it
                                                        showRegionDropdown = true
                                                    },
                                                    label = "Nereye (Destinasyon / Ülke)",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                val matchingDest = dbDestinations.filter { isFuzzyMatch(selectedRegion, it) }
                                                DropdownMenu(
                                                    expanded = showRegionDropdown && matchingDest.isNotEmpty(),
                                                    onDismissRequest = { showRegionDropdown = false },
                                                    modifier = Modifier.fillMaxWidth(0.9f).background(TourOSColors.Surface)
                                                ) {
                                                    Column(modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                                                        matchingDest.forEach { dest ->
                                                            DropdownMenuItem(
                                                                text = { Text("📍 $dest", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                                onClick = {
                                                                    selectedRegion = dest.substringAfter("- ").ifBlank { dest }
                                                                    showRegionDropdown = false
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // 3 & 4. TAKVİM POP-UP SEÇİCİLERİ
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    TourOSTextField(
                                                        value = "$startDateText 📅",
                                                        onValueChange = { },
                                                        readOnly = true,
                                                        label = "Gidiş Başlangıç",
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    TourOSTextField(
                                                        value = "$endDateText 📅",
                                                        onValueChange = { },
                                                        readOnly = true,
                                                        label = "Gidiş Bitiş",
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Box(modifier = Modifier.matchParentSize().clickable { showEndDatePicker = true })
                                                }
                                            }

                                            // 5 & 6. GECE VE TURİST DROPDOWN'LARI
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    TourOSTextField(
                                                        value = "$nights Gece ▼",
                                                        onValueChange = { },
                                                        readOnly = true,
                                                        label = "Gece Sayısı",
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Box(modifier = Modifier.matchParentSize().clickable { showNightsDropdown = !showNightsDropdown })
                                                    DropdownMenu(
                                                        expanded = showNightsDropdown,
                                                        onDismissRequest = { showNightsDropdown = false },
                                                        modifier = Modifier.width(140.dp).background(TourOSColors.Surface)
                                                    ) {
                                                        (1..14).forEach { n ->
                                                            DropdownMenuItem(
                                                                text = { Text("$n Gece", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                                onClick = {
                                                                    nights = n
                                                                    showNightsDropdown = false
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Box(modifier = Modifier.weight(1f)) {
                                                    TourOSTextField(
                                                        value = ("$adults Yetişkin" + (if (childs > 0) " + $childs Çoc" else "")) + " ▼",
                                                        onValueChange = { },
                                                        readOnly = true,
                                                        label = "Turist Sayısı",
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Box(modifier = Modifier.matchParentSize().clickable { showTouristsDropdown = !showTouristsDropdown })
                                                    DropdownMenu(
                                                        expanded = showTouristsDropdown,
                                                        onDismissRequest = { showTouristsDropdown = false },
                                                        modifier = Modifier.width(200.dp).background(TourOSColors.Surface)
                                                     ) {
                                                        listOf(
                                                            "1 Yetişkin" to (1 to 0),
                                                            "2 Yetişkin" to (2 to 0),
                                                            "2 Yetişkin + 1 Çocuk" to (2 to 1),
                                                            "2 Yetişkin + 2 Çocuk" to (2 to 2),
                                                            "3 Yetişkin" to (3 to 0),
                                                            "4 Yetişkin" to (4 to 0)
                                                        ).forEach { (label, counts) ->
                                                            DropdownMenuItem(
                                                                text = { Text(label, style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                                onClick = {
                                                                    adults = counts.first
                                                                    childs = counts.second
                                                                    showTouristsDropdown = false
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else if (activeSearchTab == "FLIGHTS") {
                                        // ── UÇUŞLAR SEKME ÖZEL ARAMA BARI (GÖRSELE UYGUN) ──────────────────────────
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 1. NEREDEN DROPDOWN
                                            Box(modifier = Modifier.weight(1.2f)) {
                                                TourOSTextField(
                                                    value = departureCity,
                                                    onValueChange = {
                                                        departureCity = it
                                                        showDepartureDropdown = true
                                                    },
                                                    label = "Nereden (Kalkış Şehri)",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                val matchingCities = dbDepartureCities.filter { isFuzzyMatch(departureCity, it) }
                                                DropdownMenu(
                                                    expanded = showDepartureDropdown && matchingCities.isNotEmpty(),
                                                    onDismissRequest = { showDepartureDropdown = false },
                                                    modifier = Modifier.width(240.dp).background(TourOSColors.Surface)
                                                ) {
                                                    Column(modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                                                        matchingCities.forEach { city ->
                                                            DropdownMenuItem(
                                                                text = { Text("✈️ $city", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                                onClick = {
                                                                    departureCity = city
                                                                    showDepartureDropdown = false
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // 2. NEREYE DROPDOWN
                                            Box(modifier = Modifier.weight(1.4f)) {
                                                TourOSTextField(
                                                    value = selectedRegion,
                                                    onValueChange = {
                                                        selectedRegion = it
                                                        showRegionDropdown = true
                                                    },
                                                    label = "Nereye (Destinasyon / Ülke)",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                val matchingDest = dbDestinations.filter { isFuzzyMatch(selectedRegion, it) }
                                                DropdownMenu(
                                                    expanded = showRegionDropdown && matchingDest.isNotEmpty(),
                                                    onDismissRequest = { showRegionDropdown = false },
                                                    modifier = Modifier.width(300.dp).background(TourOSColors.Surface)
                                                ) {
                                                    Column(modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                                                        matchingDest.forEach { dest ->
                                                            DropdownMenuItem(
                                                                text = { Text("📍 $dest", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                                onClick = {
                                                                    selectedRegion = dest.substringAfter("- ").ifBlank { dest }
                                                                    showRegionDropdown = false
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // 3. GİDİŞ TARİHİ
                                            Box(modifier = Modifier.weight(1.1f)) {
                                                TourOSTextField(
                                                    value = "$startDateText 📅",
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = "Gidiş",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
                                            }

                                            // 4. DÖNÜŞ TARİHİ (TEK YÖN İSE KİLİTLİ)
                                            Box(modifier = Modifier.weight(1.1f)) {
                                                TourOSTextField(
                                                    value = if (isRoundTrip) "$endDateText 📅" else "Tek Yön (Yok)",
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    enabled = isRoundTrip,
                                                    label = "Dönüş",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                if (isRoundTrip) {
                                                    Box(modifier = Modifier.matchParentSize().clickable { showEndDatePicker = true })
                                                }
                                            }

                                            // 5. TEK YÖN / GİDİŞ-DÖNÜŞ SEÇİM KUTUCUKLARI
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(1.dp),
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.clickable { isRoundTrip = false }
                                                ) {
                                                    RadioButton(
                                                        selected = !isRoundTrip,
                                                        onClick = { isRoundTrip = false },
                                                        colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary),
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Tek Yön", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.clickable { isRoundTrip = true }
                                                ) {
                                                    RadioButton(
                                                        selected = isRoundTrip,
                                                        onClick = { isRoundTrip = true },
                                                        colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary),
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Gidiş-Dönüş", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                                }
                                            }

                                            // 6. YOLCU SAYISI DROPDOWN
                                            Box(modifier = Modifier.weight(1.1f)) {
                                                TourOSTextField(
                                                    value = ("$adults Yetişkin" + (if (childs > 0) " + $childs Çoc" else "")) + " ▼",
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = "Yolcu Sayısı",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(modifier = Modifier.matchParentSize().clickable { showTouristsDropdown = !showTouristsDropdown })
                                                DropdownMenu(
                                                    expanded = showTouristsDropdown,
                                                    onDismissRequest = { showTouristsDropdown = false },
                                                    modifier = Modifier.width(220.dp).background(TourOSColors.Surface)
                                                ) {
                                                    listOf(
                                                        "1 Yetişkin" to (1 to 0),
                                                        "2 Yetişkin" to (2 to 0),
                                                        "2 Yetişkin + 1 Çocuk" to (2 to 1),
                                                        "2 Yetişkin + 2 Çocuk" to (2 to 2),
                                                        "3 Yetişkin" to (3 to 0),
                                                        "4 Yetişkin" to (4 to 0)
                                                    ).forEach { (label, counts) ->
                                                        DropdownMenuItem(
                                                            text = { Text(label, style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                            onClick = {
                                                                adults = counts.first
                                                                childs = counts.second
                                                                showTouristsDropdown = false
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // ── Masaüstü & Tablet Yan Yana Esnek Düzen ───────────
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 1. NEREDEN DROPDOWN
                                            Box(modifier = Modifier.weight(1.2f)) {
                                                TourOSTextField(
                                                    value = departureCity,
                                                    onValueChange = {
                                                        departureCity = it
                                                        showDepartureDropdown = true
                                                    },
                                                    label = "Nereden (Kalkış Şehri)",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                val matchingCities = dbDepartureCities.filter { isFuzzyMatch(departureCity, it) }
                                                DropdownMenu(
                                                    expanded = showDepartureDropdown && matchingCities.isNotEmpty(),
                                                    onDismissRequest = { showDepartureDropdown = false },
                                                    modifier = Modifier.width(240.dp).background(TourOSColors.Surface)
                                                ) {
                                                    Column(modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                                                        matchingCities.forEach { city ->
                                                            DropdownMenuItem(
                                                                text = { Text("✈️ $city", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                                onClick = {
                                                                    departureCity = city
                                                                    showDepartureDropdown = false
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // 2. NEREYE DROPDOWN
                                            Box(modifier = Modifier.weight(1.5f)) {
                                                TourOSTextField(
                                                    value = selectedRegion,
                                                    onValueChange = {
                                                        selectedRegion = it
                                                        showRegionDropdown = true
                                                    },
                                                    label = "Nereye (Destinasyon / Ülke)",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                val matchingDest = dbDestinations.filter { isFuzzyMatch(selectedRegion, it) }
                                                DropdownMenu(
                                                    expanded = showRegionDropdown && matchingDest.isNotEmpty(),
                                                    onDismissRequest = { showRegionDropdown = false },
                                                    modifier = Modifier.width(300.dp).background(TourOSColors.Surface)
                                                ) {
                                                    Column(modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                                                        matchingDest.forEach { dest ->
                                                            DropdownMenuItem(
                                                                text = { Text("📍 $dest", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                                onClick = {
                                                                    selectedRegion = dest.substringAfter("- ").ifBlank { dest }
                                                                    showRegionDropdown = false
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // 3. GİDİŞ BAŞLANGIÇ TARİHİ (POP-UP TAKVİMLİ)
                                            Box(modifier = Modifier.weight(1.2f)) {
                                                TourOSTextField(
                                                    value = "$startDateText 📅",
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = "Gidiş Başlangıç",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
                                            }

                                            // 4. GİDİŞ BİTİŞ TARİHİ (POP-UP TAKVİMLİ)
                                            Box(modifier = Modifier.weight(1.2f)) {
                                                TourOSTextField(
                                                    value = "$endDateText 📅",
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = "Gidiş Bitiş",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(modifier = Modifier.matchParentSize().clickable { showEndDatePicker = true })
                                            }

                                            // 5. GECE SAYISI DROPDOWN
                                            Box(modifier = Modifier.weight(0.9f)) {
                                                TourOSTextField(
                                                    value = "$nights Gece ▼",
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = "Gece Sayısı",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(modifier = Modifier.matchParentSize().clickable { showNightsDropdown = !showNightsDropdown })
                                                DropdownMenu(
                                                    expanded = showNightsDropdown,
                                                    onDismissRequest = { showNightsDropdown = false },
                                                    modifier = Modifier.width(140.dp).background(TourOSColors.Surface)
                                                ) {
                                                    (1..14).forEach { n ->
                                                        DropdownMenuItem(
                                                            text = { Text("$n Gece", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                            onClick = {
                                                                nights = n
                                                                showNightsDropdown = false
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // 6. TURİST SAYISI DROPDOWN
                                            Box(modifier = Modifier.weight(1.1f)) {
                                                TourOSTextField(
                                                    value = ("$adults Yetişkin" + (if (childs > 0) " + $childs Çoc" else "")) + " ▼",
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = "Turist Sayısı",
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(modifier = Modifier.matchParentSize().clickable { showTouristsDropdown = !showTouristsDropdown })
                                                DropdownMenu(
                                                    expanded = showTouristsDropdown,
                                                    onDismissRequest = { showTouristsDropdown = false },
                                                    modifier = Modifier.width(220.dp).background(TourOSColors.Surface)
                                                ) {
                                                    listOf(
                                                        "1 Yetişkin" to (1 to 0),
                                                        "2 Yetişkin" to (2 to 0),
                                                        "2 Yetişkin + 1 Çocuk" to (2 to 1),
                                                        "2 Yetişkin + 2 Çocuk" to (2 to 2),
                                                        "3 Yetişkin" to (3 to 0),
                                                        "4 Yetişkin" to (4 to 0)
                                                    ).forEach { (label, counts) ->
                                                        DropdownMenuItem(
                                                            text = { Text(label, style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                            onClick = {
                                                                adults = counts.first
                                                                childs = counts.second
                                                                showTouristsDropdown = false
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Destinasyon Hızlı Çip Filtreleri (UÇUŞLAR Sekmesinde Gizlendi)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (activeSearchTab != "FLIGHTS") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                            Text("Destinasyonlar:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                            dbDestinations.take(3).forEach { dest ->
                                                val shortName = dest.substringAfter("- ").ifBlank { dest }
                                                TourOSStatusBadge(
                                                    text = shortName,
                                                    backgroundColor = if (selectedRegion.contains(shortName, ignoreCase = true)) TourOSColors.PrimaryContainer else TourOSColors.Surface,
                                                    textColor = if (selectedRegion.contains(shortName, ignoreCase = true)) TourOSColors.Primary else TourOSColors.TextSecondary,
                                                    modifier = Modifier.clickable { selectedRegion = shortName }
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = {
                                            selectedStars = setOf(3, 4, 5)
                                            selectedMealTypes = emptySet()
                                            selectedOperators = emptySet()
                                            searchQuery = ""
                                            selectedHotels = emptySet()
                                        }) {
                                            Text("↺ Sıfırla", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                        }

                                        if (activeSearchTab != "FLIGHTS") {
                                            TourOSButton(
                                                text = "TURLARI BUL",
                                                onClick = { viewModel.performSearch() }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. TOUROS 0.3 TASARIM DİLİNE UYGUN AÇILIR-KAPANIR DETAYLI FİLTRE PANERİ
                        var isDetailFilterExpanded by remember { mutableStateOf(true) }

                        // Dış Paket Tur Operatörleri (Yerel Turlar ve Yerel Oteller Filtrelendi)
                        val dbOperators = remember(allDbProducts) {
                            allDbProducts.map { it.safeOperatorName }
                                .filter { it.isNotBlank() && !it.contains("YEREL", ignoreCase = true) && !it.contains("ACENTE", ignoreCase = true) }
                                .distinct()
                                .ifEmpty { listOf("Coral Travel", "Pegas Touristik", "Anex Tour", "FUN&SUN (TUI)", "Biblio-Globus", "Turkish Airlines") }
                        }
                        // Sadece Ürünlerdeki (Paket Turlardaki) Oteller (Uçuş kayıtları filtrelendi)
                        val dbProductHotels = remember(allDbProducts) {
                            allDbProducts.filter { !it.id.startsWith("local-hotel-") && !it.id.startsWith("local-tour-") && it.safeProductType != "LOCAL_HOTEL" && it.safeProductType != "LOCAL_TOUR" }
                                .map { it.safeHotelName }
                                .filter { h -> 
                                    h.isNotBlank() && 
                                    !h.startsWith("Uçuş:", ignoreCase = true) && 
                                    !h.startsWith("Fly:", ignoreCase = true) && 
                                    !h.contains("➔")
                                }
                                .distinct()
                                .ifEmpty { listOf("Grand Resort Hotel & Spa", "Akra Hotel Antalya", "Rixos Premium Belek", "Titanic Mardan Palace", "Nirvana Cosmopolitan") }
                        }
                        val dbMealTypes = remember(allDbProducts) {
                            listOf("UAI", "AI", "FB", "HB", "BB")
                        }

                        var showOperatorDropdown by remember { mutableStateOf(false) }
                        var operatorSearchText by remember { mutableStateOf("") }
                        var showHotelDropdown by remember { mutableStateOf(false) }

                        if (activeSearchTab != "FLIGHTS") {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = TourOSColors.Surface,
                                border = BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border),
                                shape = RoundedCornerShape(bottomStart = TourOSSpacing.cornerRadius, bottomEnd = TourOSSpacing.cornerRadius)
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
                                            modifier = Modifier.clickable { isDetailFilterExpanded = !isDetailFilterExpanded }
                                        ) {
                                            Text(
                                                text = "🔍 Detaylı Filtreler (Operatör, Beslenme, Otel & Yıldız)",
                                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = if (isDetailFilterExpanded) "▲ (Gizle)" else "▼ (Göster)",
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(onClick = {
                                                selectedStars = setOf(3, 4, 5)
                                                selectedMealTypes = emptySet()
                                                selectedOperators = emptySet()
                                                searchQuery = ""
                                                operatorSearchText = ""
                                            }) {
                                                Text("↺ Filtreleri Sıfırla", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                            }

                                            TourOSButton(
                                                text = "TURLARI BUL",
                                                onClick = { viewModel.performSearch() }
                                            )
                                        }
                                    }

                                    // İÇERİK (GENİŞLETİLDİĞİNDE GÖRÜNÜR)
                                    if (isDetailFilterExpanded) {
                                        HorizontalDivider(color = TourOSColors.Border)

                                        // SATIR 1: TUR OPERATÖRLERİ AÇILIR ARAMA KUTUSU & BESLENME & YILDIZ
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            // 1. TUR OPERATÖRÜ AÇILIR KUTUSU (SEARCHABLE DROPDOWN - DAR SATIRLI)
                                            Box(modifier = Modifier.weight(1.2f)) {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    TourOSTextField(
                                                        value = if (selectedOperators.isEmpty()) "Tüm Tur Operatörleri (${dbOperators.size})" else selectedOperators.joinToString(", "),
                                                        onValueChange = { },
                                                        readOnly = true,
                                                        label = "🏢 Tur Operatörü Seçin",
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                // Tıklama yakalayıcı katman
                                                Box(
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .clickable { showOperatorDropdown = !showOperatorDropdown }
                                                )

                                                DropdownMenu(
                                                    expanded = showOperatorDropdown,
                                                    onDismissRequest = { showOperatorDropdown = false },
                                                    modifier = Modifier.width(360.dp).background(TourOSColors.Surface)
                                                ) {
                                                    Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        OutlinedTextField(
                                                            value = operatorSearchText,
                                                            onValueChange = { operatorSearchText = it },
                                                            placeholder = { Text("Operatör adı ara...", fontSize = 12.sp) },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(6.dp),
                                                            singleLine = true
                                                        )

                                                        DropdownMenuItem(
                                                            text = { Text("✓ Tüm Tur Operatörleri", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary, fontSize = 12.sp)) },
                                                            onClick = {
                                                                selectedOperators = emptySet()
                                                                showOperatorDropdown = false
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                        HorizontalDivider(color = TourOSColors.Border)

                                                        val filteredOps = dbOperators.filter { it.contains(operatorSearchText, ignoreCase = true) }
                                                        Column(
                                                            modifier = Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState()),
                                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                                        ) {
                                                            filteredOps.forEach { op ->
                                                                val isChecked = selectedOperators.contains(op)
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .clickable {
                                                                            selectedOperators = if (isChecked) selectedOperators - op else selectedOperators + op
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
                                                                    Text(op, style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // 2. BESLENME / KONSEPT (ÇOKLU SEÇİM)
                                            Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("🍴 Beslenme / Konsept:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                                    dbMealTypes.forEach { meal ->
                                                        val isSelected = selectedMealTypes.contains(meal)
                                                        FilterChip(
                                                            selected = isSelected,
                                                            onClick = {
                                                                selectedMealTypes = if (isSelected) selectedMealTypes - meal else selectedMealTypes + meal
                                                            },
                                                            label = { Text(meal, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                                                selectedLabelColor = TourOSColors.Primary
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                            // 3. OTEL KATEGORİSİ (YILDIZ)
                                            Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("⭐ Otel Kategorisi:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                                    listOf(5, 4, 3).forEach { star ->
                                                        val isSelected = selectedStars.contains(star)
                                                        FilterChip(
                                                            selected = isSelected,
                                                            onClick = {
                                                                selectedStars = if (isSelected) selectedStars - star else selectedStars + star
                                                            },
                                                            label = { Text("$star★", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                                                selectedLabelColor = TourOSColors.Primary
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = TourOSColors.Border)

                                        // SATIR 2: PAKET TUR OTELLERİ (ÇOKLU SEÇİLEBİLİR AÇILIR KUTU - SADECE PAKET TUR OTELLERİ)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.weight(2f)) {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    TourOSTextField(
                                                        value = if (selectedHotels.isEmpty()) "Tüm Paket Tur Otelleri (${dbProductHotels.size})" else selectedHotels.joinToString(", "),
                                                        onValueChange = { },
                                                        readOnly = true,
                                                        label = "🏨 Paket Tur Otelleri Seçin (Tümü)",
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                // Tıklama yakalayıcı katman
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
                                                            text = { Text("✓ Tüm Paket Tur Otelleri", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary, fontSize = 12.sp)) },
                                                            onClick = {
                                                                selectedHotels = emptySet()
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
                                                                            selectedHotels = if (isChecked) selectedHotels - hotelName else selectedHotels + hotelName
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

                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                            ) {
                                                var isInstantConf by remember { mutableStateOf(false) }
                                                Checkbox(
                                                    checked = isInstantConf,
                                                    onCheckedChange = { viewModel.isInstantConfirmationOnly.value = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary)
                                                )
                                                Text(
                                                    text = "⚡ Anında Onaylı Turlar",
                                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Medium)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ARAMA SONUÇLARI MATRİSİ
                    when (val state = uiState) {
                        is B2BTourSearchUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = TourOSColors.Primary)
                            }
                        }

                        is B2BTourSearchUiState.Error -> {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(text = state.message, color = TourOSColors.Error, style = TourOSTypography.BodyMedium)
                            }
                        }

                        is B2BTourSearchUiState.Success -> {
                            val rawProducts = state.filteredProducts
                            val products = rawProducts.filter { item ->
                                val pType = item.safeProductType.uppercase()
                                val opName = item.safeOperatorName.uppercase()
                                val isLocalHotel = item.id.startsWith("local-hotel-") || pType == "LOCAL_HOTEL" || opName == "YEREL OTELLER"
                                val isLocalTour = item.id.startsWith("local-tour-") || pType == "LOCAL_TOUR" || opName == "YEREL TURLAR"

                                val tabMatch = when (activeSearchTab) {
                                    "TOURS" -> !isLocalHotel && !isLocalTour && (pType.contains("TOUR") || pType.contains("PACKAGE") || (pType.isBlank() && !pType.contains("FLIGHT")))
                                    "HOTELS" -> !isLocalHotel && !isLocalTour && (pType.contains("HOTEL") || (item.hotelName.isNotBlank() && item.flightNumber.isBlank()))
                                    "FLIGHTS" -> pType.contains("FLIGHT") || pType.contains("CHARTER") || item.flightNumber.isNotBlank() || item.tourName.contains("Uçuş", ignoreCase = true)
                                    "LOCAL_TOURS" -> isLocalTour
                                    "LOCAL_HOTELS" -> isLocalHotel
                                    else -> true
                                }

                                val isFlightTab = (activeSearchTab == "FLIGHTS")
                                val operatorMatch = isFlightTab || selectedOperators.isEmpty() || selectedOperators.any { op -> item.safeOperatorName.contains(op, ignoreCase = true) }
                                val mealMatch = isFlightTab || selectedMealTypes.isEmpty() || selectedMealTypes.any { m -> item.safeMealType.contains(m, ignoreCase = true) }
                                val starMatch = isFlightTab || selectedStars.isEmpty() || selectedStars.contains(item.hotelCategory)
                                val hotelMatch = isFlightTab || selectedHotels.isEmpty() || selectedHotels.any { hName -> item.safeHotelName.contains(hName, ignoreCase = true) }
                                val queryMatch = searchQuery.isBlank() || item.safeHotelName.contains(searchQuery, ignoreCase = true) || item.tourName.contains(searchQuery, ignoreCase = true) || item.region.contains(searchQuery, ignoreCase = true)

                                val flightDepMatch = !isFlightTab || departureCity.isBlank() || item.departureCity.contains(departureCity, ignoreCase = true)
                                val flightDestMatch = !isFlightTab || selectedRegion.isBlank() || item.region.contains(selectedRegion, ignoreCase = true) || item.country.contains(selectedRegion, ignoreCase = true)

                                tabMatch && operatorMatch && mealMatch && starMatch && hotelMatch && queryMatch && flightDepMatch && flightDestMatch
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val foundTitle = if (activeSearchTab == "FLIGHTS") "Bulunan Uçuş Seferleri" else "Bulunan Tur Seçenekleri"
                                    val foundCountLabel = if (activeSearchTab == "FLIGHTS") "Uçuş Seferi Bulundu" else "Tur Bulundu"
                                    Text(
                                        text = "2. ${AppLanguageManager.translate(foundTitle)} (${products.size} ${AppLanguageManager.translate(foundCountLabel)})",
                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = AppLanguageManager.translate("Sıralama: Fiyata Göre (En Düşük)"),
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                    )
                                }

                                products.forEach { item ->
                                    val isSelected = selectedProduct?.id == item.id
                                    TourResultMatrixCard(
                                        product = item,
                                        isSelected = isSelected,
                                        isFlightTab = (activeSearchTab == "FLIGHTS"),
                                        onSelectForBooking = {
                                            viewModel.selectProductForBooking(item)
                                            // Tur seçildiğinde anında Adım 2'ye (Uçuş & Ekstra Hizmetler) geçiş yap:
                                            activeStep = 2
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // ── ADIM 2: UÇUŞ SEÇİMİ VE EKSTRA HİZMETLER ────────────────────────
                    val curProduct = selectedProduct
                    if (curProduct == null) {
                        TourOSCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TourOSColors.Surface,
                            contentPadding = TourOSSpacing.medium
                        ) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = AppLanguageManager.translate("Lütfen öncelikle Adım 1'den bir tur seçiniz."),
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Warning),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                    TourOSButton(
                                        text = "← ${AppLanguageManager.translate("Adım 1: Tur Aramaya Dön")}",
                                        onClick = { activeStep = 1 },
                                        variant = TourOSButtonVariant.SECONDARY
                                    )
                                }
                            }
                        }
                    } else {
                        val basePrice = curProduct.price * 1.125

                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2. ${AppLanguageManager.translate("Alternatif Uçuşlar & Ekstra Hizmetler")}",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                    fontWeight = FontWeight.Bold
                                )

                                TourOSButton(
                                    text = "← ${AppLanguageManager.translate("Arama Sonuçlarına Dön")}",
                                    onClick = { activeStep = 1 },
                                    variant = TourOSButtonVariant.SECONDARY
                                )
                            }

                            // SEÇİLİ TUR ÖZET KARTI
                            TourOSCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.2f),
                                contentPadding = TourOSSpacing.medium
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "${curProduct.hotelName} (${curProduct.hotelCategory.coerceAtMost(5)} Yıldız)",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${curProduct.roomType.ifBlank { "FAMILY ROOM" }}  ·  ${curProduct.mealType.ifBlank { "Ultra All Inclusive" }}",
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
                                        Text(
                                            text = "${curProduct.departureDate ?: "21.08.2026"} (7 ${AppLanguageManager.translate("Gece")})  ·  2 ADL + 2 CHD  ·  ${curProduct.region}",
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = AppLanguageManager.translate("Konaklama Net"),
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
                                        Text(
                                            text = "${basePrice.toInt()} ${curProduct.currency}",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // UÇUŞ SEÇENEKLERİ (GÖRSEL 9 & 10)
                            Text(
                                text = "${AppLanguageManager.translate("Uçuş Alternatifleri")} (${availableFlightOptions.size} ${AppLanguageManager.translate("Uçuş Çifti")})",
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                fontWeight = FontWeight.Bold
                            )

                            availableFlightOptions.forEach { option ->
                                FlightOptionCardItem(
                                    option = option,
                                    isSelected = selectedFlightOption?.id == option.id,
                                    onSelect = { viewModel.selectedFlightOption.value = option }
                                )
                            }

                            // EKSTRA HİZMETLER (GÖRSEL 3 & 4)
                            Text(
                                text = AppLanguageManager.translate("Sigorta ve VIP Transfer Ekstraları"),
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                fontWeight = FontWeight.Bold
                            )

                            extraServices.forEach { srv ->
                                ExtraServiceCardItem(
                                    service = srv,
                                    onToggle = { viewModel.toggleExtraService(srv.id) }
                                )
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.small))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TourOSButton(
                                    text = AppLanguageManager.translate("İleri: Turist Bilgileri & Onaya Geç"),
                                    onClick = { activeStep = 3 },
                                    variant = TourOSButtonVariant.PRIMARY
                                )
                            }
                        }
                    }
                }

                3 -> {
                    // ── ADIM 3: YOLCU BILGILERI FORMU VE REZERVASYON ONAYI ───────────────
                    val curProduct = selectedProduct
                    if (curProduct == null) {
                        TourOSCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TourOSColors.Surface,
                            contentPadding = TourOSSpacing.medium
                        ) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = AppLanguageManager.translate("Lütfen öncelikle Adım 1'den bir tur seçiniz."),
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Warning),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                    TourOSButton(
                                        text = "← ${AppLanguageManager.translate("Adım 1: Tur Aramaya Dön")}",
                                        onClick = { activeStep = 1 },
                                        variant = TourOSButtonVariant.SECONDARY
                                    )
                                }
                            }
                        }
                    } else {
                        val basePrice = curProduct.price * 1.125
                        val flightDelta = selectedFlightOption?.priceDeltaRub ?: 0.0
                        val extrasTotalEur = extraServices.filter { it.isSelected }.sumOf { it.unitPriceEur * it.paxCount }
                        val extrasTotalRub = extrasTotalEur * 100.0
                        val grandTotalRub = basePrice + flightDelta + extrasTotalRub

                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "3. ${AppLanguageManager.translate("Yolcu (Turist) Pasaport Bilgileri")} (${passengers.size} ${AppLanguageManager.translate("Yolcu Kayıtlı")})",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                    fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    TourOSButton(
                                        text = "← ${AppLanguageManager.translate("Geri: Uçuş/Hizmet Seçimi")}",
                                        onClick = { activeStep = 2 },
                                        variant = TourOSButtonVariant.SECONDARY
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.addPassenger() },
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.2f))
                                    ) {
                                        Text("+ ${AppLanguageManager.translate("Yolcu Ekle")}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            passengers.forEachIndexed { idx, pax ->
                                PassengerFormCardItem(
                                    passenger = pax,
                                    paxIndex = idx + 1,
                                    canRemove = passengers.size > 1,
                                    onRemove = { viewModel.removePassenger(pax.index) },
                                    onUpdatePassenger = { updated ->
                                        viewModel.passengers.value = viewModel.passengers.value.mapIndexed { i, old ->
                                            if (i == idx) updated else old
                                        }
                                    }
                                )
                            }

                            // TOPLAM SATIŞ VE REZERVASYON TAMAMLAMA ÇUBUĞU
                            TourOSCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.3f),
                                contentPadding = TourOSSpacing.medium
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = AppLanguageManager.translate("TOPLAM REZERVASYON TUTARI"),
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${grandTotalRub.toInt()} ${curProduct.currency}",
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = AppLanguageManager.translate("Anında Onaylı Operatör Kaydı"),
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                        TourOSButton(
                                            text = AppLanguageManager.translate("Taslak Kaydet"),
                                            onClick = { showSuccessModal = true },
                                            variant = TourOSButtonVariant.SECONDARY
                                        )
                                        TourOSButton(
                                            text = AppLanguageManager.translate("Rezervasyonu Tamamla & Onayla (PNR Oluştur)"),
                                            onClick = {
                                                viewModel.confirmBookingAndSaveToSupabase { pnrCode ->
                                                    showSuccessModal = true
                                                }
                                            },
                                            variant = TourOSButtonVariant.PRIMARY
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isEmbedded) {
        content(PaddingValues(0.dp))
    } else {
        Scaffold(
            containerColor = TourOSColors.Surface,
            topBar = {
                TourOSTopBar(
                    title = AppLanguageManager.translate("Gelişmiş Tur & Otel Arama ve Rezervasyon Paneli"),
                    subtitle = AppLanguageManager.translate("Sletat / Coral B2B Standartlarında Canlı Arama, Uçuş, Ekstra Hizmetler ve Yolcu Kaydı"),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                        }
                    }
                )
            }
        ) { padding ->
            content(padding)
        }
    }

    // REZERVASYON BAŞARILI MODALI
    if (showSuccessModal) {
        AlertDialog(
            onDismissRequest = { showSuccessModal = false },
            title = {
                Text(
                    text = AppLanguageManager.translate("Rezervasyon Başarıyla Oluşturuldu!"),
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Success),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    Text(
                        text = "${AppLanguageManager.translate("PNR / Rezervasyon Kodu")}: ${createdPnrCode.ifBlank { "B2B-PNR-758924" }}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppLanguageManager.translate("Veri Konumu: Supabase 'public.bookings' tablosuna ve Ana Rezervasyon Yönetim Paneline kaydedildi."),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppLanguageManager.translate("Turist bilgileri, uçuş detayları ve bilet konfirmasyonu kayıt altına alındı. Ana Rezervasyon Listesinden detayları inceleyebilirsiniz."),
                        style = TourOSTypography.BodyMedium
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    TourOSButton(
                        text = AppLanguageManager.translate("Rezervasyon Listesine Git"),
                        onClick = {
                            showSuccessModal = false
                            onNavigateToBookings()
                        },
                        variant = TourOSButtonVariant.PRIMARY
                    )
                    TourOSButton(
                        text = AppLanguageManager.translate("Tamam & Kapat"),
                        onClick = {
                            showSuccessModal = false
                        },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                }
            }
        )
    }
}

// ─── MATRİS SONUÇ KART BİLEŞENİ (GÖRSEL 8) ──────────────────────────────────────

@Composable
private fun TourResultMatrixCard(
    product: UnifiedProductEntity,
    isSelected: Boolean,
    isFlightTab: Boolean = false,
    onSelectForBooking: () -> Unit
) {
    val marginCalculatedPrice = remember(product.price) { product.price * 1.125 }

    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectForBooking() },
        backgroundColor = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.15f) else TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1.5f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.hotelName,
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⭐".repeat(product.hotelCategory.coerceAtMost(5)),
                        style = TourOSTypography.Caption
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TourOSColors.SecondaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Starway Award",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Secondary),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!product.departureDate.isNullOrBlank()) {
                        Text(
                            text = "${product.departureDate}  ·  ${product.nights} ${AppLanguageManager.translate("Gece")}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (product.roomType.isNotBlank()) {
                        Text(
                            text = product.roomType,
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
                        )
                    }
                    if (product.mealType.isNotBlank()) {
                        Text(
                            text = product.mealType,
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, TourOSColors.Divider, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VKO - AYT (Ekonomi  Business)",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .border(1.dp, TourOSColors.Divider, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SVO - AYT (Ekonomi)",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${product.price.toInt()} ${product.currency}",
                        style = TourOSTypography.Caption.copy(
                            color = TourOSColors.TextSecondary,
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Anında Onay",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Warning),
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${marginCalculatedPrice.toInt()} ${product.currency}",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                val buttonText = when {
                    isSelected -> AppLanguageManager.translate("Seçildi")
                    isFlightTab -> AppLanguageManager.translate("Uçuş Seç")
                    else -> AppLanguageManager.translate("Turu Seç & Detaylandır")
                }

                TourOSButton(
                    text = buttonText,
                    onClick = onSelectForBooking,
                    variant = if (isSelected) TourOSButtonVariant.SECONDARY else TourOSButtonVariant.PRIMARY
                )
            }
        }
    }
}

// ─── UÇUŞ SEÇENEĞİ KART BİLEŞENİ (GÖRSEL 9 & 10) ───────────────────────────────

@Composable
private fun FlightOptionCardItem(
    option: FlightOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        backgroundColor = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.2f) else TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                        Text(
                            text = "GİDİŞ: ${option.outboundAirline} (${option.outboundFlightNumber})  ·  ${option.outboundDeparturePort} ➔ ${option.outboundArrivalPort} (${option.outboundDuration})",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                        Text(
                            text = "DÖNÜŞ: ${option.inboundAirline} (${option.inboundFlightNumber})  ·  ${option.inboundDeparturePort} ➔ ${option.inboundArrivalPort} (${option.inboundDuration})",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "El Bagajı: ${option.handBaggageKg}kg  ·  Kayıtlı Bagaj: ${option.baggageKg}kg",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (option.priceDeltaRub > 0) {
                        Text(
                            text = "+${option.priceDeltaRub.toInt()} RUB",
                            style = TourOSTypography.Label.copy(color = TourOSColors.Warning),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppLanguageManager.translate("Uçuş Farkı"),
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TourOSColors.SuccessContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = AppLanguageManager.translate("Fark Yok (Pakete Dahil)"),
                                style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ── GÖRSEL 10: UÇUŞ DETAYLARI VE ZORUNLU EK ÜCRETLER (MANDATORY SURCHARGES BREAKDOWN) ──
            if (isSelected) {
                Spacer(modifier = Modifier.height(TourOSSpacing.small))
                HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(TourOSSpacing.small))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TourOSColors.PrimaryContainer.copy(alpha = 0.5f))
                        .padding(TourOSSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "⚡ ${AppLanguageManager.translate("Zorunlu Uçuş Farkları ve Ek Ücret Dökümü (Mandatory Surcharges)")}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "• ${AppLanguageManager.translate("Dönem Uçuş Farkı (TURKISH AIRLINES)")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        Text(text = "+34.333 RUB", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "• ${AppLanguageManager.translate("Sabah Gidiş Uçuş Ek Ücreti (02:05 VKO)")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        Text(text = "+14.137 RUB", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "• ${AppLanguageManager.translate("Akşam Dönüş Uçuş Ek Ücreti (18:40 AYT)")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        Text(text = "+18.176 RUB", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "• ${AppLanguageManager.translate("Grup Havalimanı Transferi")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        Text(text = AppLanguageManager.translate("Dahil (Включен)"), style = TourOSTypography.Caption.copy(color = TourOSColors.Success), fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.3f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = AppLanguageManager.translate("Toplam Zorunlu Ek Ücretler:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.Bold)
                        Text(text = "66.646 RUB", style = TourOSTypography.Label.copy(color = TourOSColors.Warning), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── EKSTRA HİZMET KART BİLEŞENİ (GÖRSEL 3 & 4) ────────────────────────────────

@Composable
private fun ExtraServiceCardItem(
    service: ExtraService,
    onToggle: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Switch(
                    checked = service.isSelected,
                    onCheckedChange = { if (!service.isMandatory) onToggle() },
                    enabled = !service.isMandatory,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TourOSColors.Surface,
                        checkedTrackColor = TourOSColors.Success
                    )
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = service.name,
                            style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.Bold
                        )
                        if (service.isMandatory) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TourOSColors.PrimaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Zorunlu",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Kişi Başı: ${service.unitPriceEur} EUR  ·  Toplam (${service.paxCount} Yolcu): ${(service.unitPriceEur * service.paxCount)} EUR",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }
            }

            Text(
                text = "${(service.unitPriceEur * service.paxCount * 100).toInt()} RUB",
                style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── GÖRSEL 5 & 6 ESİNTİLİ YOLCU FORM KART BİLEŞENİ ───────────────────────────

@Composable
private fun PassengerFormCardItem(
    passenger: PassengerInfo,
    paxIndex: Int,
    canRemove: Boolean,
    onRemove: () -> Unit,
    onUpdatePassenger: (PassengerInfo) -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // BAŞLIK VE CİNSİYET SEÇİMİ (GÖRSEL 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👤 ${AppLanguageManager.translate("Turist")} $paxIndex ${if (passenger.isPayer) "(${AppLanguageManager.translate("Sipariş Veren Müşteri")})" else ""}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    if (canRemove) {
                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                            Text("🗑️", fontSize = 12.sp)
                        }
                    }
                }

                // CİNSİYET TOGGLE BUTONLARI (GÖRSEL 5 & 6)
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    FilterChip(
                        selected = passenger.gender == "MALE",
                        onClick = { onUpdatePassenger(passenger.copy(gender = "MALE")) },
                        label = { Text(AppLanguageManager.translate("Bay (Мужской)"), style = TourOSTypography.Caption) }
                    )
                    FilterChip(
                        selected = passenger.gender == "FEMALE",
                        onClick = { onUpdatePassenger(passenger.copy(gender = "FEMALE")) },
                        label = { Text(AppLanguageManager.translate("Bayan (Женский)"), style = TourOSTypography.Caption) }
                    )
                }
            }

            HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.5f))

            // FORM SATIRI 1: AD, SOYAD, DOĞUM TARİHİ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.firstName,
                        onValueChange = { onUpdatePassenger(passenger.copy(firstName = it)) },
                        label = AppLanguageManager.translate("Adı (Имя)"),
                        placeholder = "AHMET",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.lastName,
                        onValueChange = { onUpdatePassenger(passenger.copy(lastName = it)) },
                        label = AppLanguageManager.translate("Soyadı (Фамилия)"),
                        placeholder = "YILMAZ",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.birthDate,
                        onValueChange = { onUpdatePassenger(passenger.copy(birthDate = it)) },
                        label = AppLanguageManager.translate("Doğum Tarihi (GG.AA.YYYY)"),
                        placeholder = "12.05.1985",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // FORM SATIRI 2: UYRUK, PASAPORT NO, GEÇERLİLİK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.citizenship,
                        onValueChange = { onUpdatePassenger(passenger.copy(citizenship = it)) },
                        label = AppLanguageManager.translate("Uyruk (Гражданство)"),
                        placeholder = "Türkiye",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.passportNumber,
                        onValueChange = { onUpdatePassenger(passenger.copy(passportNumber = it)) },
                        label = AppLanguageManager.translate("Pasaport No (Номер)"),
                        placeholder = "84920492",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.documentExpiryDate,
                        onValueChange = { onUpdatePassenger(passenger.copy(documentExpiryDate = it)) },
                        label = AppLanguageManager.translate("Son Geçerlilik (Срок действия)"),
                        placeholder = "12.05.2030",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // FORM SATIRI 3: İLETİŞİM BİLGİLERİ (Sadece 1. Turist için)
            if (passenger.isPayer) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSTextField(
                            value = passenger.phone,
                            onValueChange = { onUpdatePassenger(passenger.copy(phone = it)) },
                            label = AppLanguageManager.translate("Telefon No"),
                            placeholder = "+90 532 100 2030",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(modifier = Modifier.weight(1.5f)) {
                        TourOSTextField(
                            value = passenger.email,
                            onValueChange = { onUpdatePassenger(passenger.copy(email = it)) },
                            label = AppLanguageManager.translate("E-posta Adresi"),
                            placeholder = "ahmet@gmail.com",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // ── GÖRSEL 6: ÇOCUK/BEBEK YOLCU ÖZEL ALANLARI ("Ответственный за ребенка") ──
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TourOSColors.PrimaryContainer.copy(alpha = 0.5f))
                        .padding(TourOSSpacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👨‍👦 ${AppLanguageManager.translate("Çocuktan Sorumlu Yetişkin (Ответственный за ребенка)")}:",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Turist 1 (Yetişkin / Lead)",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = passenger.isInfantSeatRequested,
                            onCheckedChange = { isChecked ->
                                onUpdatePassenger(passenger.copy(isInfantSeatRequested = isChecked))
                            },
                            colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary)
                        )
                        Text(
                            text = AppLanguageManager.translate("İnfant İçin Uchakta Ayrı Koltuk"),
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleDatePickerDialog(
    title: String,
    initialDateText: String,
    onDateSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedDay by remember { mutableStateOf(initialDateText.take(2).toIntOrNull() ?: 18) }
    var selectedMonth by remember { mutableStateOf("Ağustos") }
    var selectedYear by remember { mutableStateOf("2026") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TourOSButton(
                text = "Tarihi Seç",
                onClick = {
                    val formatted = "${selectedDay.toString().padStart(2, '0')}.08.2026"
                    onDateSelected(formatted)
                    onDismissRequest()
                }
            )
        },
        dismissButton = {
            TourOSButton(
                text = "İptal",
                variant = TourOSButtonVariant.TERTIARY,
                onClick = onDismissRequest
            )
        },
        title = {
            Text(
                text = title,
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                Text(
                    text = "📅 $selectedMonth $selectedYear",
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz").forEach { dayName ->
                        Text(
                            text = dayName,
                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextSecondary),
                            modifier = Modifier.width(32.dp)
                        )
                    }
                }
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(31) { index ->
                        val day = index + 1
                        val isSelected = (day == selectedDay)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) TourOSColors.Primary else TourOSColors.Background)
                                .border(1.dp, if (isSelected) TourOSColors.Primary else TourOSColors.Border, RoundedCornerShape(6.dp))
                                .clickable { selectedDay = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$day",
                                style = TourOSTypography.Caption.copy(
                                    color = if (isSelected) Color.White else TourOSColors.TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = TourOSColors.Surface
    )
}

private fun isFuzzyMatch(query: String, target: String): Boolean {
    if (query.isBlank()) return true
    val q = query.trim().lowercase()
    val t = target.lowercase()
    if (t.contains(q)) return true

    var matchCount = 0
    var qIdx = 0
    for (i in 0 until t.length) {
        if (qIdx < q.length && t[i] == q[qIdx]) {
            matchCount++
            qIdx++
        }
    }
    return matchCount >= (q.length - 1) && q.length >= 3
}
