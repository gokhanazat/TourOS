package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
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
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val availableFlightOptions by viewModel.availableFlightOptions.collectAsState()
    val selectedFlightOption by viewModel.selectedFlightOption.collectAsState()
    val extraServices by viewModel.extraServices.collectAsState()
    val passengers by viewModel.passengers.collectAsState()
    val createdPnrCode by viewModel.createdPnrCode.collectAsState()

    val dailyQuota by viewModel.dailyQuota.collectAsState()
    val todayQueries by viewModel.todayQueries.collectAsState()
    val monthlyQuota by viewModel.monthlyQuota.collectAsState()
    val currentQueries by viewModel.currentQueries.collectAsState()
    val isQuotaExceeded by viewModel.isQuotaExceeded.collectAsState()
    val quotaErrorMessage by viewModel.quotaErrorMessage.collectAsState()

    var activeSearchTab by remember { mutableStateOf("TOURS") } // "TOURS", "HOTELS", "FLIGHTS", "LOCAL_TOURS", "LOCAL_HOTELS"
    var departureCity by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("") }
    var nightsText by remember { mutableStateOf("7 - 10 Gece") }
    val adults by viewModel.adults.collectAsState()
    val childs by viewModel.childs.collectAsState()
    val childrenAges by viewModel.childrenAges.collectAsState()
    var showTouristDialog by remember { mutableStateOf(false) }

    // Ayrıştırılmış ve Pop-up Açılır Takvim Destekli Canlı Dinamik Tarih State'leri
    val todayDateTriple = remember { com.mgacreative.touros.getTodayTriple() }
    val defaultEndDateTriple = remember { com.mgacreative.touros.addDaysToTriple(todayDateTriple, 7) }
    var startDateText by remember {
        val sD = todayDateTriple.first.toString().padStart(2, '0')
        val sM = todayDateTriple.second.toString().padStart(2, '0')
        mutableStateOf("$sD.$sM.${todayDateTriple.third}")
    }
    var endDateText by remember {
        val eD = defaultEndDateTriple.first.toString().padStart(2, '0')
        val eM = defaultEndDateTriple.second.toString().padStart(2, '0')
        mutableStateOf("$eD.$eM.${defaultEndDateTriple.third}")
    }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var selectedMealTypes by remember { mutableStateOf(emptySet<String>()) }
    var selectedOperators by remember { mutableStateOf(emptySet<String>()) }
    var operatorSearchText by remember { mutableStateOf("") }

    var selectedStars by remember { mutableStateOf(emptySet<Int>()) }
    var isInstantOnly by remember { mutableStateOf(false) }
    var isPromoOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedHotels by remember { mutableStateOf(emptySet<String>()) }
    var isRoundTrip by remember { mutableStateOf(true) }

    // Paket Turlar & Oteller Sekmesi Özel Ekstra Detaylı Filtreler
    var selectedBeachLine by remember { mutableStateOf(0) } // 0: Hepsi, 1: <100m, 2: <500m, 3: <2km
    var minRating by remember { mutableStateOf(0.0) } // 0.0: Hepsi, 7+, 8+, 9+
    var selectedAmenities by remember { mutableStateOf(setOf<String>()) }
    var isDirectFlightOnly by remember { mutableStateOf(false) }
    var isTransferIncludedOnly by remember { mutableStateOf(false) }
    var activeStep by remember { mutableStateOf(1) }
    var showSuccessModal by remember { mutableStateOf(false) }
    var showB2BDestinationPicker by remember { mutableStateOf(false) }
    var b2bSelectedCountryTab by remember { mutableStateOf("ALL") }
    var b2bSelectedSubRegion by remember { mutableStateOf<String?>(null) }
    var selectedProductForOperatorModal by remember { mutableStateOf<UnifiedProductEntity?>(null) }
    var isDetailFilterExpanded by remember { mutableStateOf(true) }
    var b2bCurrentPage by remember { mutableStateOf(1) }

    if (showB2BDestinationPicker) {
        val isFlightTab = activeSearchTab == "FLIGHTS"
        com.mgacreative.touros.ui.components.HierarchicalDestinationPickerDialog(
            currentSelection = selectedRegion,
            onlyAirports = isFlightTab,
            customTitle = if (isFlightTab) "✈️ UÇUŞ VARIŞ HAVALİMANI / АЭРОПОРТ НАЗНАЧЕНИЯ" else null,
            onDestinationSelected = { destItem ->
                selectedRegion = if (isFlightTab && destItem.airportCode != null) {
                    "${destItem.name.substringBefore(" Havalimanı").substringBefore(" Uluslararası")} (${destItem.airportCode})"
                } else {
                    destItem.name
                }
            },
            onDismiss = { showB2BDestinationPicker = false }
        )
    }

    if (showTouristDialog) {
        B2BTouristAndChildAgePickerDialog(
            adults = adults,
            childrenAges = childrenAges,
            onAdultsChange = { newAdults ->
                viewModel.adults.value = newAdults
            },
            onChildrenAgesChange = { newAges ->
                viewModel.childrenAges.value = newAges
                viewModel.childs.value = newAges.size
            },
            onDismiss = { showTouristDialog = false }
        )
    }

    // Tam Kapsamlı Sıfırlama Fonksiyonu
    fun resetAllFilters() {
        departureCity = ""
        selectedRegion = ""
        b2bSelectedCountryTab = "ALL"
        b2bSelectedSubRegion = null
        selectedStars = emptySet()
        selectedMealTypes = emptySet()
        selectedOperators = emptySet()
        operatorSearchText = ""
        searchQuery = ""
        selectedHotels = emptySet()
        selectedBeachLine = 0
        minRating = 0.0
        selectedAmenities = emptySet()
        isDirectFlightOnly = false
        isTransferIncludedOnly = false
        isInstantOnly = false
        viewModel.isInstantConfirmationOnly.value = false
        nightsText = "Tüm Geceler (1 - 30)"
        viewModel.adults.value = 2
        viewModel.childs.value = 0
        viewModel.childrenAges.value = emptyList()
        viewModel.performSearch()
    }

    // Veritabanından gelen benzersiz Kalkış Şehirleri ve Destinasyonlar
    val defaultCities = remember {
        listOf(
            "Moskova", "Saint Petersburg", "Kazan", "Yekaterinburg", "Novosibirsk",
            "Samara", "Ufa", "Nizhny Novgorod", "Chelyabinsk", "Krasnoyarsk",
            "İstanbul", "Antalya", "Ankara", "İzmir", "Adana"
        )
    }
    val allDbProducts = (uiState as? B2BTourSearchUiState.Success)?.allProducts ?: emptyList()
    val dbDepartureCities = remember(allDbProducts) {
        val extracted = allDbProducts.map { it.departureCity }.filter { it.isNotBlank() && it != "Yerel Otel" }
        (extracted + defaultCities).distinct().sorted()
    }
    val dbDestinations = remember(allDbProducts) {
        allDbProducts.map { "${it.country} - ${it.region}" }.filter { it.isNotBlank() && !it.startsWith(" -") }.distinct().sorted().ifEmpty { listOf("Türkiye - Antalya", "Mısır - Sharm El Sheikh", "BAE - Dubai", "Rusya - Soçi").sorted() }
    }

    val b2bCountryTabsList = remember {
        listOf(
            Triple("ALL", "Tüm Ülkeler", "ALL"),
            Triple("TR", "Türkiye", "TR"),
            Triple("EG", "Mısır", "EG"),
            Triple("TH", "Tayland", "TH"),
            Triple("VN", "Vietnam", "VN"),
            Triple("AE", "BAE (Dubai)", "AE"),
            Triple("RU", "Rusya", "RU")
        )
    }

    val b2bSubRegionsMap = remember {
        mapOf(
            "TR" to listOf("Tümü", "Antalya", "Belek", "Kemer", "Lara", "Alanya", "Side", "Bodrum", "Marmaris", "Fethiye", "Çeşme"),
            "EG" to listOf("Tümü", "Şarm El-Şeyh", "Hurgada", "El Gouna", "Makadi Bay"),
            "TH" to listOf("Tümü", "Phuket", "Pattaya", "Bangkok", "Koh Samui", "Krabi"),
            "VN" to listOf("Tümü", "Da Nang", "Phu Quoc", "Nha Trang", "Hoi An"),
            "AE" to listOf("Tümü", "Dubai Marina", "Palm Jumeirah", "Downtown", "Abu Dhabi"),
            "RU" to listOf("Tümü", "Moskova", "St. Petersburg", "Sochi", "Kazan")
        )
    }

    fun isB2BMatchingCountry(item: UnifiedProductEntity, countryCode: String): Boolean {
        return B2BTourSearchViewModel.isCountryMatching(item, countryCode)
    }

    fun isB2BMatchingSubRegion(item: UnifiedProductEntity, subRegion: String?): Boolean {
        return B2BTourSearchViewModel.isSubRegionMatching(item, subRegion)
    }

    fun isDepartureMatching(item: UnifiedProductEntity, departure: String): Boolean {
        return B2BTourSearchViewModel.isDepartureMatching(item, departure)
    }

    fun isDestinationMatching(item: UnifiedProductEntity, selectedDest: String): Boolean {
        return B2BTourSearchViewModel.isDestinationMatching(item, selectedDest)
    }

    // Pop-Up Takvim Modalları (B2B Çift Tarih Aralığı Seçici & Tekli Seçici)
    if (showDateRangePicker) {
        B2BDateRangePickerDialog(
            initialStartDateText = startDateText,
            initialEndDateText = endDateText,
            onDateRangeSelected = { start, end ->
                startDateText = start
                endDateText = end
                viewModel.selectedStartDate.value = start
                viewModel.selectedEndDate.value = end
                viewModel.performSearch(forceRefresh = true)
            },
            onDismissRequest = { showDateRangePicker = false }
        )
    }
    if (showStartDatePicker) {
        SimpleDatePickerDialog(
            title = "Gidiş Tarihi (Başlangıç) Seçin",
            initialDateText = startDateText,
            onDateSelected = { 
                startDateText = it
                viewModel.selectedStartDate.value = it
                viewModel.performSearch(forceRefresh = true)
            },
            onDismissRequest = { showStartDatePicker = false }
        )
    }
    if (showEndDatePicker) {
        SimpleDatePickerDialog(
            title = "Gidiş Tarihi (Bitiş) Seçin",
            initialDateText = endDateText,
            onDateSelected = { 
                endDateText = it
                viewModel.selectedEndDate.value = it
                viewModel.performSearch(forceRefresh = true)
            },
            onDismissRequest = { showEndDatePicker = false }
        )
    }

    // ── 🏬 OPERATÖR KARŞILAŞTIRMA & TEKLİF LİSTESİ MODALI (B2B METASEARCH) ────────
    if (selectedProductForOperatorModal != null) {
        val prod = selectedProductForOperatorModal!!
        val isFlightProd = prod.safeProductType.uppercase().contains("FLIGHT") || prod.flightNumber.isNotBlank() || prod.tourName.contains("Uçuş", ignoreCase = true)
        val basePrice = prod.price

        val mainOp = prod.safeOperatorName.ifBlank { "Coral Travel" }
        val effectiveAgencyPrices = listOf(
            AgencyPriceOption("AGN-1", mainOp, mainOp, prod.roomType.ifBlank { "Standart Oda" }, prod.mealType.ifBlank { "Her Şey Dahil" }, basePrice, isBestDeal = true),
            AgencyPriceOption("AGN-2", "Anex Tour", "Anex Tour", if (isFlightProd) "Ekonomi Uçuş" else "Deluxe Room", if (isFlightProd) "Standart Bagaj" else "Her Şey Dahil", basePrice * 1.08),
            AgencyPriceOption("AGN-3", "Pegas Touristik", "Pegas Touristik", if (isFlightProd) "Flexi Uçuş" else "Standard Room", if (isFlightProd) "20kg Bagaj" else "Oda Kahvaltı", basePrice * 1.15),
            AgencyPriceOption("AGN-4", "Biblioglobus", "Biblioglobus", if (isFlightProd) "Promo Uçuş" else "Promo Room", if (isFlightProd) "El Bagajı" else "Bez pitaniya", basePrice * 1.04),
            AgencyPriceOption("AGN-5", "Fun&Sun (RU)", "Fun&Sun (RU)", if (isFlightProd) "Charter Sefer" else "Standart Oda", if (isFlightProd) "Sıcak İkram" else "Ultra Her Şey Dahil", basePrice * 1.10)
        )

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedProductForOperatorModal = null }
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 740.dp)
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── SABİT ÜST BAŞLIK ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (prod.hotelName.isNotBlank()) prod.hotelName else prod.tourName,
                                style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            )
                            Text(
                                text = "${prod.region}, ${prod.country} • ID: ${prod.id}",
                                style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable { selectedProductForOperatorModal = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", fontSize = 14.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppLanguageManager.translate("Hangi Acente / Operatör Kaç Satıyor?"),
                            style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                        Text(
                            text = AppLanguageManager.translate("Canlı Fiyat Karşılaştırma"),
                            style = TourOSTypography.Caption.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        )
                    }

                    // ── AŞAĞI KAYDIRILABİLİR İNCE KOMPAKT OPERATÖR LİSTESİ (LAZYCOLUMN) ──
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(effectiveAgencyPrices) { option ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = if (option.isBestDeal) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    1.dp,
                                    if (option.isBestDeal) Color(0xFF22C55E) else Color(0xFFE2E8F0)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Sol Bilgi
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "${option.agencyName}",
                                                style = TourOSTypography.BodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            )
                                            if (option.isBestDeal) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF22C55E))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(AppLanguageManager.translate("En İyi Fiyat"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${AppLanguageManager.translate("Operatör")}: ${option.operatorName} • ${option.roomType} (${option.boardType})",
                                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Sağ Taraf: Fiyat + Kompakt Rezerve Et Butonu
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(option.price, decimals = false)} ${if (prod.currency == "RUB") "RUB" else "₺"}",
                                            style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                        )

                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    viewModel.selectProductForBooking(
                                                        prod.copy(
                                                            operatorName = option.operatorName,
                                                            price = option.price
                                                        )
                                                    )
                                                    activeStep = 2
                                                    selectedProductForOperatorModal = null
                                                },
                                            color = Color(0xFF1E4D58)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = AppLanguageManager.translate("Rezerve Et"),
                                                    style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
        }
    }

    val rootScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val content: @Composable (PaddingValues) -> Unit = { padding ->
        val columnModifier = if (isEmbedded) {
            Modifier.fillMaxWidth().padding(TourOSSpacing.large)
        } else {
            Modifier.fillMaxSize().padding(padding).verticalScroll(rootScrollState).padding(TourOSSpacing.large)
        }
        Column(
            modifier = columnModifier,
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // ── GERİ DÖN TUŞU (ADIMLI AKIŞTA ÖNCEKİ ADIMA DÖNÜŞ) ────────────────
            if (activeStep > 1) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { activeStep = activeStep - 1 }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = AppLanguageManager.translate("Önceki Adıma Dön"),
                        style = TourOSTypography.BodyMedium.copy(
                            color = TourOSColors.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }

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
                        // 1. ÜST SEKMELER (TourOS Kurumsal Teması) + CANLI KOTA ROZETİ
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TourOSColors.Primary),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.Start) {
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
                                                    viewModel.departureCity.value = ""
                                                    viewModel.selectedRegion.value = ""
                                                }
                                                viewModel.selectedCategory.value = key
                                                viewModel.performSearch()
                                            },
                                        color = if (isSelected) TourOSColors.PrimaryContainer else Color.Transparent
                                    ) {
                                        Text(
                                            text = AppLanguageManager.translate(label),
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

                            // CANLI GÜNLÜK & AYLIK KOTA ROZETİ
                            Surface(
                                color = if (isQuotaExceeded) TourOSColors.Secondary else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                                modifier = Modifier.padding(end = TourOSSpacing.medium)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${AppLanguageManager.translate("Bugün")}: $todayQueries/$dailyQuota",
                                        style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                    Text("|", style = TourOSTypography.Caption.copy(color = Color.White.copy(alpha = 0.6f)))
                                    Text(
                                        "${AppLanguageManager.translate("Bu Ay")}: $currentQueries/${if (monthlyQuota > 0) monthlyQuota else "∞"}",
                                        style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                }
                            }
                        }

                        // 2. KOTA AŞIMI KİLİT UYARISI BANNER'I
                        if (isQuotaExceeded && !quotaErrorMessage.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = TourOSColors.SecondaryContainer,
                                border = BorderStroke(1.dp, TourOSColors.Secondary.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(TourOSSpacing.medium),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = TourOSColors.Secondary, modifier = Modifier.size(22.dp))
                                    Column {
                                        Text(
                                            text = "Arama ve Canlı Fiyat Sorgulama Kısıtlaması Devrede",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary, fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = quotaErrorMessage ?: "",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Secondary)
                                        )
                                    }
                                }
                            }
                        }

                        val defaultTourVisorOperators = listOf(
                            "Ambotis", "Amigo-S", "Anex", "Biblioglobus", "China Travel", 
                            "Crystal Bay Tours", "Evroport", "Fun&Sun (RU)", "ICS Travel Group", 
                            "ITM group", "Kazunion", "Lets Fly", "One Click Travel", "OneTouch & Travel", 
                            "Paks", "Panteon", "Pegas Touristik", "Resort Holiday", "Russian Express", 
                            "Space Travel", "Алеан", "Арт Тревел", "Арт-Тур", "Интурист", 
                            "Меркурий", "Планета Travel", "Премьера", "Турплатформа"
                        )
                        // Dış Paket Tur Operatörleri (Yerel Turlar ve Yerel Oteller Filtrelendi)
                        val dbOperators = remember(allDbProducts) {
                            val fromProducts = allDbProducts.map { it.safeOperatorName }
                                .filter { op ->
                                    op.isNotBlank() && 
                                    !op.contains("•") && 
                                    !op.contains("/") && 
                                    !op.contains("Direct Contract", ignoreCase = true) && 
                                    !op.contains("YEREL", ignoreCase = true) && 
                                    !op.contains("ACENTE", ignoreCase = true)
                                }
                                .distinct()
                            (fromProducts + defaultTourVisorOperators).distinct().sorted()
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

                        // 3. AKILLI ARAMA BARI (Nereden | Nereye | Başlangıç Tarihi | Bitiş Tarihi | Gece | Turist)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = TourOSColors.Background
                        ) {
                            Column(
                                modifier = Modifier.padding(TourOSSpacing.large),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                UniversalTourSearchBar(
                                    variant = SearchBarVariant.B2B_PORTAL,
                                    activeTab = activeSearchTab,
                                    onTabChange = { 
                                        activeSearchTab = it
                                        viewModel.selectedCategory.value = it
                                        viewModel.performSearch()
                                    },
                                    departureCity = departureCity,
                                    onDepartureCityChange = { 
                                        departureCity = it 
                                        viewModel.departureCity.value = it
                                    },
                                    selectedRegion = selectedRegion,
                                    onRegionChange = { 
                                        selectedRegion = it 
                                        viewModel.selectedRegion.value = it
                                    },
                                    startDateText = startDateText,
                                    endDateText = endDateText,
                                    onDateRangeChange = { start, end ->
                                        startDateText = start
                                        endDateText = end
                                        viewModel.selectedStartDate.value = start
                                        viewModel.selectedEndDate.value = end
                                        viewModel.performSearch(forceRefresh = true)
                                    },
                                    nightsText = nightsText,
                                    onNightsTextChange = { nightsText = it },
                                    adults = adults,
                                    onAdultsChange = { viewModel.adults.value = it },
                                    childrenAges = childrenAges,
                                    onChildrenAgesChange = {
                                        viewModel.childrenAges.value = it
                                        viewModel.childs.value = it.size
                                    },
                                    isRoundTrip = isRoundTrip,
                                    onRoundTripChange = { isRoundTrip = it },
                                    availableDepartureCities = dbDepartureCities,
                                    availableDestinations = dbDestinations,
                                    onSearchClick = { 
                                        viewModel.selectedCategory.value = activeSearchTab
                                        viewModel.departureCity.value = departureCity
                                        viewModel.selectedRegion.value = selectedRegion
                                        viewModel.selectedStartDate.value = startDateText
                                        viewModel.selectedEndDate.value = endDateText
                                        viewModel.performSearch(forceRefresh = true) 
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // ── BLOK 2: TOUROS 0.3 TASARIM DİLİNE UYGUN AÇILIR-KAPANIR DETAYLI FİLTRE PANELİ ──
                        if (activeSearchTab == "TOURS" || activeSearchTab == "HOTELS") {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
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
                                            modifier = Modifier.clickable { isDetailFilterExpanded = !isDetailFilterExpanded }
                                        ) {
                                            Text(
                                                text = AppLanguageManager.translate("Detaylı Filtreler (Sahil, Beslenme, Yıldız, Puan, Otel & Donanım)"),
                                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = if (isDetailFilterExpanded) "▲ (${AppLanguageManager.translate("Gizle")})" else "▼ (${AppLanguageManager.translate("Göster")})",
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(onClick = { resetAllFilters() }) {
                                                Text("↺ ${AppLanguageManager.translate("Filtreleri Sıfırla")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                            }
                                        }
                                    }

                                    // İÇERİK (GENİŞLETİLDİĞİNDE GÖRÜNÜR)
                                    if (isDetailFilterExpanded) {
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
                                                                    FilterChip(
                                                                        selected = isSelected,
                                                                        onClick = { selectedBeachLine = code },
                                                                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                                        colors = FilterChipDefaults.filterChipColors(
                                                                            selectedContainerColor = TourOSColors.PrimaryContainer,
                                                                            selectedLabelColor = TourOSColors.Primary
                                                                        )
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

                                                        // 4. Otel Puanı (Misafir Değerlendirmesi)
                                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Text(AppLanguageManager.translate("Otel Puanı:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                listOf(0.0 to "Hepsi", 7.0 to "7.0+", 8.0 to "8.0+", 9.0 to "9.0+").forEach { (rVal, label) ->
                                                                    val isSelected = (minRating == rVal)
                                                                    FilterChip(
                                                                        selected = isSelected,
                                                                        onClick = { minRating = rVal },
                                                                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                                        colors = FilterChipDefaults.filterChipColors(
                                                                            selectedContainerColor = TourOSColors.PrimaryContainer,
                                                                            selectedLabelColor = TourOSColors.Primary
                                                                        )
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
                                                                    FilterChip(
                                                                        selected = isSelected,
                                                                        onClick = { selectedBeachLine = code },
                                                                        label = { Text(AppLanguageManager.translate(label), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                                        colors = FilterChipDefaults.filterChipColors(
                                                                            selectedContainerColor = TourOSColors.PrimaryContainer,
                                                                            selectedLabelColor = TourOSColors.Primary
                                                                        )
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

                                                        // 3. Otel Kategorisi (Yıldız)
                                                        Column(modifier = Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Text("${AppLanguageManager.translate("Otel Kategorisi")}:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                listOf(5, 4, 3, 2).forEach { star ->
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

                                                        // 4. Otel Puanı (Misafir Değerlendirmesi)
                                                        Column(modifier = Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Text("${AppLanguageManager.translate("Otel Puanı")}:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                listOf(0.0 to "Hepsi", 7.0 to "7.0+", 8.0 to "8.0+", 9.0 to "9.0+").forEach { (rVal, label) ->
                                                                    val isSelected = (minRating == rVal)
                                                                    FilterChip(
                                                                        selected = isSelected,
                                                                        onClick = { minRating = rVal },
                                                                        label = { Text(AppLanguageManager.translate(label), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                                        colors = FilterChipDefaults.filterChipColors(
                                                                            selectedContainerColor = TourOSColors.PrimaryContainer,
                                                                            selectedLabelColor = TourOSColors.Primary
                                                                        )
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

                                            // Donanım & Hizmetler Filter Chips
                                            Column(modifier = Modifier.weight(1.4f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("${AppLanguageManager.translate("Donanım & Özellikler")}:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                                    listOf("Aquapark", "Wi-Fi", "SPA", "Kum Plaj", "Çocuk Kulübü", "Havuz").forEach { am ->
                                                        val isSelected = am in selectedAmenities
                                                        FilterChip(
                                                            selected = isSelected,
                                                            onClick = {
                                                                selectedAmenities = if (isSelected) selectedAmenities - am else selectedAmenities + am
                                                            },
                                                            label = { Text(AppLanguageManager.translate(am), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
                                                    modifier = Modifier.clickable {
                                                        isInstantOnly = !isInstantOnly
                                                        viewModel.isInstantConfirmationOnly.value = isInstantOnly
                                                    }
                                                ) {
                                                    Checkbox(
                                                        checked = isInstantOnly,
                                                        onCheckedChange = {
                                                            isInstantOnly = it
                                                            viewModel.isInstantConfirmationOnly.value = it
                                                        },
                                                        colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary)
                                                    )
                                                    Text(
                                                        text = AppLanguageManager.translate("Anında Onaylı Turlar"),
                                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Medium)
                                                    )
                                                }

                                                if (activeSearchTab == "TOURS") {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                                        modifier = Modifier.clickable { isDirectFlightOnly = !isDirectFlightOnly }
                                                    ) {
                                                        Checkbox(
                                                            checked = isDirectFlightOnly,
                                                            onCheckedChange = { isDirectFlightOnly = it },
                                                            colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary)
                                                        )
                                                        Text(
                                                            text = AppLanguageManager.translate("Aktarmasız / Direkt Uçuş"),
                                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Medium)
                                                        )
                                                    }
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                                    modifier = Modifier.clickable { isTransferIncludedOnly = !isTransferIncludedOnly }
                                                ) {
                                                    Checkbox(
                                                        checked = isTransferIncludedOnly,
                                                        onCheckedChange = { isTransferIncludedOnly = it },
                                                        colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary)
                                                    )
                                                    Text(
                                                        text = AppLanguageManager.translate("Transfer Dahil"),
                                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Medium)
                                                    )
                                                }
                                            }

                                            // ── Kırmızı Ok ile Belirtilen Alan: Tur Operatörü Seçim Kutusu ──
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
                                                            TextButton(onClick = { selectedOperators = emptySet() }) {
                                                                Text(AppLanguageManager.translate("Temizle"), fontSize = 11.sp, color = TourOSColors.TextSecondary)
                                                            }
                                                            TextButton(onClick = { selectedOperators = dbOperators.toSet() }) {
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
                                                                        onCheckedChange = {
                                                                            selectedOperators = if (isChecked) selectedOperators - opName else selectedOperators + opName
                                                                        },
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
                                                                selectedOperators = if (isChecked) selectedOperators - opName else selectedOperators + opName
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

                        // ── BLOK 3: 🌍 ÜLKE & ALT BÖLGE (BELDELER) SEÇİM ÇUBUĞU ──────────────────────────
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = TourOSColors.Background,
                            border = BorderStroke(1.dp, TourOSColors.Border)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val tabFilteredDbProducts = remember(allDbProducts, activeSearchTab) {
                                    allDbProducts.filter { item ->
                                        val pType = item.safeProductType.uppercase()
                                        val isPureFlight = pType == "FLIGHT" || item.airlineName.isNotBlank() || item.flightNumber.startsWith("TK-") || item.flightNumber.startsWith("N4-") || item.flightNumber.startsWith("SU-") || item.flightNumber.startsWith("PC-") || item.tourName.startsWith("Uçuş:", ignoreCase = true) || item.hotelName.startsWith("Uçuş:", ignoreCase = true) || item.hotelName.startsWith("✈️", ignoreCase = true)
                                        val isPureHotel = (pType == "HOTEL" || pType == "LOCAL_HOTEL" || item.operatorName.contains("Yerel Otel", ignoreCase = true)) && !isPureFlight && item.flightNumber.isBlank()
                                        val isPackageTour = (pType == "PACKAGE_TOUR" || pType == "LOCAL_TOUR" || pType == "TOUR" || item.hasTransfer) && !isPureFlight && !isPureHotel

                                        when (activeSearchTab.uppercase()) {
                                            "TOURS", "PACKAGE_TOUR" -> isPackageTour
                                            "HOTELS", "HOTEL" -> isPureHotel
                                            "FLIGHTS", "FLIGHT" -> isPureFlight
                                            "LOCAL_TOURS" -> pType == "LOCAL_TOUR" || item.id.startsWith("local-tour-")
                                            "LOCAL_HOTELS" -> pType == "LOCAL_HOTEL" || item.id.startsWith("local-hotel-")
                                            else -> true
                                        }
                                    }
                                }

                                if (activeSearchTab != "LOCAL_TOURS" && activeSearchTab != "LOCAL_HOTELS") {
                                    // 1. Ülke Hap Sekmeleri (Pills)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        b2bCountryTabsList.forEach { (code, name, flag) ->
                                            val isSelected = (b2bSelectedCountryTab == code)
                                            val cCount = if (code == "ALL") tabFilteredDbProducts.size else tabFilteredDbProducts.count { isB2BMatchingCountry(it, code) }

                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        b2bSelectedCountryTab = code
                                                        b2bSelectedSubRegion = null
                                                        selectedRegion = ""
                                                        viewModel.destinationCountry.value = if (code == "ALL") "" else code
                                                        viewModel.selectedRegion.value = ""
                                                        viewModel.performSearch()
                                                    },
                                                color = if (isSelected) TourOSColors.Primary else TourOSColors.Surface,
                                                border = BorderStroke(1.dp, if (isSelected) TourOSColors.Primary else TourOSColors.Border),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(flag, fontSize = 13.sp)
                                                    Text(
                                                        text = AppLanguageManager.translate(name),
                                                        style = TourOSTypography.BodyMedium.copy(
                                                            color = if (isSelected) Color.White else TourOSColors.TextPrimary,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            fontSize = 12.sp
                                                        )
                                                    )
                                                    if (cCount > 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else TourOSColors.PrimaryContainer)
                                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = "$cCount",
                                                                style = TourOSTypography.Caption.copy(
                                                                    color = if (isSelected) Color.White else TourOSColors.Primary,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 9.sp
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 2. Alt Beldeler (Seçili Ülkeye Göre)
                                    val curSubRegs = b2bSubRegionsMap[b2bSelectedCountryTab]
                                    if (!curSubRegs.isNullOrEmpty()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = AppLanguageManager.translate("Beldeler:"),
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            )
                                            curSubRegs.forEach { sReg ->
                                                val isSubActive = if (sReg == "Tümü") b2bSelectedSubRegion == null else b2bSelectedSubRegion == sReg
                                                TourOSStatusBadge(
                                                    text = AppLanguageManager.translate(sReg),
                                                    backgroundColor = if (isSubActive) TourOSColors.PrimaryContainer else TourOSColors.Surface,
                                                    textColor = if (isSubActive) TourOSColors.Primary else TourOSColors.TextSecondary,
                                                    modifier = Modifier.clickable {
                                                        val chosen = if (sReg == "Tümü") null else sReg
                                                        b2bSelectedSubRegion = chosen
                                                        selectedRegion = chosen ?: ""
                                                        viewModel.selectedRegion.value = chosen ?: ""
                                                        viewModel.performSearch()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // KOTA ENGELLEME VE BİLGİLENDİRME UYARISI

                    if (isQuotaExceeded) {
                        Surface(
                            shape = RoundedCornerShape(TourOSSpacing.cornerRadius),
                            color = TourOSColors.SecondaryContainer,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, TourOSColors.Secondary),
                            modifier = Modifier.fillMaxWidth().padding(vertical = TourOSSpacing.medium)
                        ) {
                            Column(
                                modifier = Modifier.padding(TourOSSpacing.large),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(AppLanguageManager.translate("AYLIK ARAMA & SORGU KOTANIZ DOLMUŞTUR"), style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Secondary, fontWeight = FontWeight.Bold))
                                Text(
                                    quotaErrorMessage ?: "Bu ay için acentenize tanımlanan arama kotası limitine ulaşılmıştır. Yeni arama yapabilmek ve rezervasyon oluşturmaya devam edebilmek için lütfen SaaS Sistem Yöneticiniz ile iletişime geçerek ek kota talep ediniz.",
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                )
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
                            if (!isQuotaExceeded) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(text = state.message, color = TourOSColors.Error, style = TourOSTypography.BodyMedium)
                                }
                            }
                        }

                        is B2BTourSearchUiState.Success -> {
                            val rawProducts = state.filteredProducts
                            val products = remember(
                                rawProducts,
                                activeSearchTab,
                                selectedOperators,
                                selectedMealTypes,
                                selectedStars,
                                selectedHotels,
                                searchQuery,
                                departureCity,
                                selectedRegion,
                                nightsText,
                                selectedBeachLine,
                                minRating,
                                selectedAmenities,
                                isDirectFlightOnly,
                                isTransferIncludedOnly,
                                isInstantOnly,
                                b2bSelectedCountryTab,
                                b2bSelectedSubRegion
                            ) {
                                rawProducts.filter { item ->
                                    val pType = item.safeProductType.uppercase()
                                    val isPureFlight = pType == "FLIGHT" || item.airlineName.isNotBlank() || item.flightNumber.startsWith("TK-") || item.flightNumber.startsWith("N4-") || item.flightNumber.startsWith("SU-") || item.flightNumber.startsWith("PC-") || item.tourName.startsWith("Uçuş:", ignoreCase = true) || item.hotelName.startsWith("Uçuş:", ignoreCase = true) || item.hotelName.startsWith("✈️", ignoreCase = true)
                                    val isPureHotel = (pType == "HOTEL" || pType == "LOCAL_HOTEL" || item.operatorName.contains("Yerel Otel", ignoreCase = true)) && !isPureFlight && item.flightNumber.isBlank()
                                    val isPackageTour = (pType == "PACKAGE_TOUR" || pType == "LOCAL_TOUR" || pType == "TOUR" || item.hasTransfer) && !isPureFlight && !isPureHotel

                                    val tabMatch = when (activeSearchTab.uppercase()) {
                                        "TOURS", "PACKAGE_TOUR" -> isPackageTour
                                        "HOTELS", "HOTEL" -> isPureHotel
                                        "FLIGHTS", "FLIGHT" -> isPureFlight
                                        "LOCAL_TOURS" -> pType == "LOCAL_TOUR" || item.id.startsWith("local-tour-")
                                        "LOCAL_HOTELS" -> pType == "LOCAL_HOTEL" || item.id.startsWith("local-hotel-")
                                        else -> true
                                    }

                                    val countryMatch = (b2bSelectedCountryTab == "ALL") || isB2BMatchingCountry(item, b2bSelectedCountryTab)
                                    val subRegionMatch = (b2bSelectedSubRegion.isNullOrBlank() || b2bSelectedSubRegion == "Tümü") || isB2BMatchingSubRegion(item, b2bSelectedSubRegion)

                                    val isFlightTab = (activeSearchTab == "FLIGHTS")
                                    val operatorMatch = isFlightTab || selectedOperators.isEmpty() || selectedOperators.any { op -> item.safeOperatorName.contains(op, ignoreCase = true) }
                                    
                                    val mealMatch = isFlightTab || selectedMealTypes.isEmpty() || selectedMealTypes.any { m ->
                                        val lower = item.safeMealType.lowercase()
                                        when (m.uppercase()) {
                                            "UAI" -> lower.contains("uai") || lower.contains("ultra") || lower.contains("ультра")
                                            "AI" -> lower.contains("ai") || lower.contains("all inclusive") || lower.contains("her şey") || lower.contains("все включено")
                                            "FB" -> lower.contains("fb") || lower.contains("full board") || lower.contains("tam pansiyon") || lower.contains("полный пансион")
                                            "HB" -> lower.contains("hb") || lower.contains("half board") || lower.contains("yarım pansiyon") || lower.contains("полупансион")
                                            "BB" -> lower.contains("bb") || lower.contains("bed & breakfast") || lower.contains("oda kahvaltı") || lower.contains("завтрак") || lower.contains("breakfast")
                                            "RO" -> lower.contains("ro") || lower.contains("room only") || lower.contains("sadece oda") || lower.contains("bez pitaniya") || lower.contains("без питания")
                                            else -> lower.contains(m.lowercase())
                                        }
                                    }

                                    val starMatch = isFlightTab || selectedStars.isEmpty() || selectedStars.contains(item.hotelCategory)
                                    val hotelMatch = isFlightTab || selectedHotels.isEmpty() || selectedHotels.any { hName -> item.safeHotelName.contains(hName, ignoreCase = true) }
                                    val queryMatch = searchQuery.isBlank() || item.safeHotelName.contains(searchQuery, ignoreCase = true) || item.tourName.contains(searchQuery, ignoreCase = true) || item.region.contains(searchQuery, ignoreCase = true)

                                    val flightDepMatch = isDepartureMatching(item, departureCity)
                                    val flightDestMatch = isDestinationMatching(item, selectedRegion)

                                    val (b2bMinNights, b2bMaxNights) = when {
                                        nightsText.contains("1 - 4") -> 1 to 4
                                        nightsText.contains("5 - 7") -> 5 to 7
                                        nightsText.contains("7 - 10") -> 7 to 10
                                        nightsText.contains("10 - 14") -> 10 to 14
                                        nightsText.contains("14 - 21") -> 14 to 21
                                        nightsText.contains("Tüm") -> 1 to 30
                                        else -> {
                                            val num = nightsText.filter { it.isDigit() }.toIntOrNull() ?: 7
                                            num to num
                                        }
                                    }
                                    val nightsMatch = isFlightTab || nightsText.contains("Tüm") || (item.nights in b2bMinNights..b2bMaxNights) || item.nights <= 0

                                    val isTourOrHotel = (activeSearchTab == "TOURS" || activeSearchTab == "HOTELS")
                                    val beachMatch = !isTourOrHotel || selectedBeachLine == 0 || item.beachLine == 0 || item.beachLine == selectedBeachLine
                                    val ratingMatch = !isTourOrHotel || minRating <= 0.0 || item.hotelRating <= 0.0 || item.hotelRating >= minRating
                                    val amenityMatch = !isTourOrHotel || selectedAmenities.isEmpty() || selectedAmenities.all { am -> item.amenities.any { a -> a.contains(am, ignoreCase = true) } }
                                    val directFlightMatch = (activeSearchTab != "TOURS") || !isDirectFlightOnly || item.isDirectFlight
                                    val transferMatch = !isTourOrHotel || !isTransferIncludedOnly || item.hasTransfer
                                    val instantMatch = !isInstantOnly || item.isInstantConfirmation

                                    tabMatch && countryMatch && subRegionMatch && operatorMatch && mealMatch && starMatch && hotelMatch && queryMatch && flightDepMatch && flightDestMatch && nightsMatch && beachMatch && ratingMatch && amenityMatch && directFlightMatch && transferMatch && instantMatch
                                }
                            }

                            // Ülke ve Alt Bölgeye Göre Kesin Filtrelenmiş Sonuçlar
                            val b2bCountryFilteredProducts = remember(products, b2bSelectedCountryTab, b2bSelectedSubRegion) {
                                if (b2bSelectedCountryTab == "ALL" && (b2bSelectedSubRegion.isNullOrBlank() || b2bSelectedSubRegion == "Tümü")) {
                                    products
                                } else {
                                    products.filter { item ->
                                        isB2BMatchingCountry(item, b2bSelectedCountryTab) &&
                                        isB2BMatchingSubRegion(item, b2bSelectedSubRegion)
                                    }
                                }
                            }

                            // ── B2B SAYFALAMA (PAGINATION) MANTIĞI ──
                            val b2bPageSize = 15
                            val totalB2BPages = remember(b2bCountryFilteredProducts.size) {
                                maxOf(1, (b2bCountryFilteredProducts.size + b2bPageSize - 1) / b2bPageSize)
                            }
                            val safeB2BCurrentPage = remember(b2bCurrentPage, totalB2BPages) {
                                b2bCurrentPage.coerceIn(1, totalB2BPages)
                            }
                            val b2bPagedProducts = remember(b2bCountryFilteredProducts, safeB2BCurrentPage) {
                                val fromIdx = (safeB2BCurrentPage - 1) * b2bPageSize
                                val toIdx = minOf(fromIdx + b2bPageSize, b2bCountryFilteredProducts.size)
                                if (fromIdx in b2bCountryFilteredProducts.indices) {
                                    b2bCountryFilteredProducts.subList(fromIdx, toIdx)
                                } else {
                                    emptyList()
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val foundTitle = when (activeSearchTab.uppercase()) {
                                        "FLIGHTS", "FLIGHT" -> "Bulunan Uçuş Seferleri"
                                        "LOCAL_TOURS" -> "Bulunan Yerel Tur Seçenekleri"
                                        "LOCAL_HOTELS" -> "Bulunan Yerel Otel Seçenekleri"
                                        "HOTELS", "HOTEL" -> "Bulunan Otel Seçenekleri"
                                        else -> "Bulunan Tur Seçenekleri"
                                    }
                                    val foundCountLabel = when (activeSearchTab.uppercase()) {
                                        "FLIGHTS", "FLIGHT" -> "Uçuş Seferi Bulundu"
                                        "LOCAL_TOURS" -> "Yerel Tur Bulundu"
                                        "LOCAL_HOTELS" -> "Yerel Otel Bulundu"
                                        "HOTELS", "HOTEL" -> "Otel Bulundu"
                                        else -> "Tur Bulundu"
                                    }
                                    Text(
                                        text = "${AppLanguageManager.translate(foundTitle)} (${b2bCountryFilteredProducts.size} ${AppLanguageManager.translate(foundCountLabel)} - ${AppLanguageManager.translate("Sayfa")} $safeB2BCurrentPage / $totalB2BPages)",
                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = AppLanguageManager.translate("Sıralama: Fiyata Göre (En Düşük)"),
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                    )
                                }

                                // 📱/💻 KOMPAKT SATIR LİSTE DÜZENİ VEYA TEMİZ BOŞ DURUM MESAJI
                                if (b2bCountryFilteredProducts.isEmpty()) {
                                    val emptyTitle = when (activeSearchTab.uppercase()) {
                                        "LOCAL_TOURS" -> "Seçilen Kriterler İçin Aktif Yerel Tur Bulunamadı"
                                        "LOCAL_HOTELS" -> "Seçilen Kriterler İçin Aktif Yerel Otel Bulunamadı"
                                        else -> "Seçilen Ülke / Belde İçin Aktif Tur Bulunamadı"
                                    }
                                    val emptyDesc = when (activeSearchTab.uppercase()) {
                                        "LOCAL_TOURS" -> "Arama kriterlerinize uygun yerel tur bulunamadı. Lütfen tarih veya kalkış noktasını değiştirerek tekrar deneyin."
                                        "LOCAL_HOTELS" -> "Arama kriterlerinize uygun yerel otel bulunamadı. Lütfen diğer ülkeleri inceleyin veya tüm ülkelere dönün."
                                        else -> "Arama kriterlerinize uygun tur veya otel bulunamadı. Lütfen diğer ülkeleri inceleyin veya tüm ülkelere dönün."
                                    }
                                    val emptyBtnText = when (activeSearchTab.uppercase()) {
                                        "LOCAL_TOURS" -> "Tüm Turlara Dön"
                                        "LOCAL_HOTELS" -> "Tüm Otellere Dön"
                                        else -> "Tüm Ülkelere Dön"
                                    }
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = TourOSSpacing.medium),
                                        shape = RoundedCornerShape(12.dp),
                                        color = TourOSColors.Surface,
                                        border = BorderStroke(1.dp, TourOSColors.Border)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Luggage, contentDescription = null, tint = TourOSColors.Primary, modifier = Modifier.size(32.dp))
                                            Text(
                                                text = AppLanguageManager.translate(emptyTitle),
                                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = AppLanguageManager.translate(emptyDesc),
                                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            TourOSButton(
                                                text = AppLanguageManager.translate(emptyBtnText),
                                                onClick = {
                                                    b2bSelectedCountryTab = "ALL"
                                                    b2bSelectedSubRegion = null
                                                    b2bCurrentPage = 1
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    val b2bResultsScrollState = rememberScrollState()
                                    LaunchedEffect(safeB2BCurrentPage) {
                                        b2bResultsScrollState.scrollTo(0)
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 580.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .verticalScroll(b2bResultsScrollState)
                                                    .padding(end = 10.dp),
                                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                            ) {
                                                b2bPagedProducts.forEach { item ->
                                                    val isSelected = selectedProduct?.id == item.id
                                                    TourResultMatrixCard(
                                                        product = item,
                                                        isSelected = isSelected,
                                                        adults = adults,
                                                        childrenAges = childrenAges,
                                                        isFlightTab = (activeSearchTab == "FLIGHTS"),
                                                        onSelectForBooking = {
                                                            selectedProductForOperatorModal = item
                                                        }
                                                    )
                                                }
                                            }

                                            TourOSVerticalScrollbar(
                                                scrollState = b2bResultsScrollState,
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .fillMaxHeight()
                                                    .padding(end = 2.dp)
                                            )
                                        }

                                        // B2B Sayfalama Kontrol Çubuğu
                                        if (totalB2BPages > 1) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                color = TourOSColors.Surface,
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, TourOSColors.Border)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .clickable(enabled = safeB2BCurrentPage > 1) {
                                                                if (safeB2BCurrentPage > 1) {
                                                                    b2bCurrentPage = safeB2BCurrentPage - 1
                                                                    coroutineScope.launch {
                                                                        rootScrollState.animateScrollTo(0)
                                                                    }
                                                                }
                                                            },
                                                        color = if (safeB2BCurrentPage > 1) TourOSColors.PrimaryContainer else TourOSColors.Background,
                                                        border = BorderStroke(1.dp, if (safeB2BCurrentPage > 1) TourOSColors.Primary.copy(alpha = 0.4f) else TourOSColors.Border),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "◀ ${AppLanguageManager.translate("Önceki")}",
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                            style = TourOSTypography.Caption.copy(
                                                                color = if (safeB2BCurrentPage > 1) TourOSColors.Primary else TourOSColors.TextSecondary,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp
                                                            )
                                                        )
                                                    }

                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        val startP = maxOf(1, safeB2BCurrentPage - 2)
                                                        val endP = minOf(totalB2BPages, startP + 4)
                                                        (startP..endP).forEach { pNum ->
                                                            val isCurr = (pNum == safeB2BCurrentPage)
                                                            Surface(
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .clickable {
                                                                        b2bCurrentPage = pNum
                                                                        coroutineScope.launch {
                                                                            rootScrollState.animateScrollTo(0)
                                                                        }
                                                                    },
                                                                color = if (isCurr) TourOSColors.Primary else TourOSColors.Background,
                                                                border = BorderStroke(1.dp, if (isCurr) TourOSColors.Primary else TourOSColors.Border),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Text(
                                                                        text = "$pNum",
                                                                        style = TourOSTypography.Caption.copy(
                                                                            color = if (isCurr) Color.White else TourOSColors.TextPrimary,
                                                                            fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal,
                                                                            fontSize = 11.sp
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    Surface(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .clickable(enabled = safeB2BCurrentPage < totalB2BPages) {
                                                                if (safeB2BCurrentPage < totalB2BPages) {
                                                                    b2bCurrentPage = safeB2BCurrentPage + 1
                                                                    coroutineScope.launch {
                                                                        rootScrollState.animateScrollTo(0)
                                                                    }
                                                                }
                                                            },
                                                        color = if (safeB2BCurrentPage < totalB2BPages) TourOSColors.PrimaryContainer else TourOSColors.Background,
                                                        border = BorderStroke(1.dp, if (safeB2BCurrentPage < totalB2BPages) TourOSColors.Primary.copy(alpha = 0.4f) else TourOSColors.Border),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "${AppLanguageManager.translate("Sonraki")} ▶",
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                            style = TourOSTypography.Caption.copy(
                                                                color = if (safeB2BCurrentPage < totalB2BPages) TourOSColors.Primary else TourOSColors.TextSecondary,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp
                                                            )
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
                                        text = AppLanguageManager.translate("Adım 1: Tur Aramaya Dön"),
                                        onClick = { activeStep = 1 },
                                        variant = TourOSButtonVariant.SECONDARY
                                    )
                                }
                            }
                        }
                    } else {
                        val isFlight = curProduct.productType.equals("FLIGHT", ignoreCase = true) || curProduct.flightNumber.isNotBlank() || curProduct.tourName.contains("Uçuş", ignoreCase = true)
                        val dynamicMultiplier = B2BTourSearchViewModel.calculateMultiplier(adults, childrenAges, isFlight)
                        val basePrice = curProduct.price * dynamicMultiplier

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
                                    text = AppLanguageManager.translate("Arama Sonuçlarına Dön"),
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
                                            text = "${AppLanguageManager.translate("Operatör")}: ${curProduct.safeOperatorName}  ·  ${curProduct.roomType.ifBlank { "FAMILY ROOM" }}  ·  ${curProduct.mealType.ifBlank { "Ultra All Inclusive" }}",
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.SemiBold)
                                        )
                                        val paxDesc = "$adults ADL" + (if (childrenAges.isNotEmpty()) " + ${childrenAges.size} CHD (${childrenAges.joinToString(",") { "${it}y" }})" else "")
                                        Text(
                                            text = "${curProduct.departureDate ?: startDateText} (${curProduct.nights} ${AppLanguageManager.translate("Gece")})  ·  $paxDesc  ·  ${curProduct.region}",
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
                                    currency = curProduct.currency,
                                    onToggle = { viewModel.toggleExtraService(srv.id) }
                                )
                            }

                            // CANLI FİYAT VE EKSTRALAR HESAPLAMASI
                            val flightDelta = selectedFlightOption?.priceDeltaRub ?: 0.0
                            val conversionRate = when (curProduct.currency.uppercase()) {
                                "RUB" -> 100.0
                                "TRY", "TL" -> 38.0
                                "USD" -> 1.08
                                else -> 1.0 // EUR
                            }
                            val selectedExtras = extraServices.filter { it.isSelected }
                            val extrasTotalInCurrency = selectedExtras.sumOf { (it.unitPriceEur * conversionRate) * it.paxCount }
                            val step2GrandTotal = basePrice + flightDelta + extrasTotalInCurrency

                            Spacer(modifier = Modifier.height(TourOSSpacing.small))

                            // ── CANLI GENEL TOPLAM VE ÖZET KARTI ──
                            TourOSCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = TourOSColors.Surface,
                                borderColor = TourOSColors.Primary,
                                contentPadding = TourOSSpacing.medium
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = AppLanguageManager.translate("Canlı Fiyat & Hizmet Özeti"),
                                            style = TourOSTypography.TitleSmall.copy(color = TourOSColors.Primary),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${AppLanguageManager.translate("Konaklama")}: ${basePrice.toInt()} ${curProduct.currency}",
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                            )
                                            if (flightDelta != 0.0) {
                                                Text(
                                                    text = "• ${AppLanguageManager.translate("Uçuş Farkı")}: +${flightDelta.toInt()} ${curProduct.currency}",
                                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                                )
                                            }
                                            if (extrasTotalInCurrency > 0) {
                                                Text(
                                                    text = "• ${AppLanguageManager.translate("Ekstralar")} (${selectedExtras.size}): +${extrasTotalInCurrency.toInt()} ${curProduct.currency}",
                                                    style = TourOSTypography.Caption.copy(color = TourOSColors.Success, fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = AppLanguageManager.translate("Güncel Toplam"),
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.SemiBold)
                                            )
                                            Text(
                                                text = "${step2GrandTotal.toInt()} ${curProduct.currency}",
                                                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        TourOSButton(
                                            text = AppLanguageManager.translate("İleri: Turist Bilgileri & Onaya Geç"),
                                            onClick = { activeStep = 3 },
                                            variant = TourOSButtonVariant.PRIMARY
                                        )
                                    }
                                }
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
                                        text = AppLanguageManager.translate("Adım 1: Tur Aramaya Dön"),
                                        onClick = { activeStep = 1 },
                                        variant = TourOSButtonVariant.SECONDARY
                                    )
                                }
                            }
                        }
                    } else {
                        val isFlight = curProduct.productType.equals("FLIGHT", ignoreCase = true) || curProduct.flightNumber.isNotBlank() || curProduct.tourName.contains("Uçuş", ignoreCase = true)
                        val dynamicMultiplier = B2BTourSearchViewModel.calculateMultiplier(adults, childrenAges, isFlight)
                        val basePrice = curProduct.price * dynamicMultiplier
                        val flightDelta = selectedFlightOption?.priceDeltaRub ?: 0.0

                        val conversionRate = when (curProduct.currency.uppercase()) {
                            "RUB" -> 100.0
                            "TRY", "TL" -> 38.0
                            "USD" -> 1.08
                            else -> 1.0 // EUR
                        }
                        val extrasTotalInCurrency = extraServices.filter { it.isSelected }.sumOf { (it.unitPriceEur * conversionRate) * it.paxCount }
                        val grandTotal = basePrice + flightDelta + extrasTotalInCurrency

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
                                        text = AppLanguageManager.translate("Geri: Uçuş/Hizmet Seçimi"),
                                        onClick = { activeStep = 2 },
                                        variant = TourOSButtonVariant.SECONDARY
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.addAdultPassenger() },
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.2f))
                                    ) {
                                        Text("+ ${AppLanguageManager.translate("Yetişkin")}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.addChildPassenger(5) },
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.2f))
                                    ) {
                                        Text("+ ${AppLanguageManager.translate("Çocuk")}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
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
                                        val current = viewModel.passengers.value.mapIndexed { i, old ->
                                            if (i == idx) updated else old
                                        }
                                        viewModel.passengers.value = current
                                        val childAges = current.filter { it.passengerType != "ADULT" }.mapNotNull { it.childAge }
                                        viewModel.childrenAges.value = childAges
                                        viewModel.childs.value = childAges.size
                                        viewModel.adults.value = current.count { it.passengerType == "ADULT" }.coerceAtLeast(1)
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
                                            text = "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(grandTotal, decimals = false)} ${curProduct.currency}",
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
                                            onClick = {
                                                viewModel.confirmBookingAndSaveToSupabase { pnrCode ->
                                                    showSuccessModal = true
                                                }
                                            },
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
                    subtitle = AppLanguageManager.translate("B2B Standartlarında Canlı Arama, Uçuş, Ekstra Hizmetler ve Yolcu Kaydı"),
                    onNavigateBack = onNavigateBack
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
    adults: Int = 2,
    childrenAges: List<Int> = emptyList(),
    isFlightTab: Boolean = false,
    onSelectForBooking: () -> Unit
) {
    val isFlightCard = isFlightTab || product.safeProductType.uppercase().contains("FLIGHT") || product.flightNumber.isNotBlank() || product.tourName.contains("Uçuş", ignoreCase = true)
    val dynamicMultiplier = remember(adults, childrenAges, isFlightCard) {
        B2BTourSearchViewModel.calculateMultiplier(adults, childrenAges, isFlightCard)
    }
    val marginCalculatedPrice = remember(product.price, dynamicMultiplier) { product.price * dynamicMultiplier }

    val effectiveImage = remember(product) {
        when {
            product.safePictureUrl.isNotBlank() -> product.safePictureUrl
            isFlightCard -> "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800"
            product.hotelName.contains("Rixos", ignoreCase = true) -> "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800"
            product.hotelName.contains("Lujo", ignoreCase = true) -> "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800"
            product.hotelName.contains("Maxx", ignoreCase = true) -> "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800"
            else -> "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelectForBooking() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.15f) else TourOSColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TourOSSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. SOL: KOMPAKT GÖRSEL (Image Thumb with Badges)
            Box(
                modifier = Modifier
                    .size(width = 130.dp, height = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
            ) {
                AsyncImage(
                    model = effectiveImage,
                    contentDescription = product.hotelName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Sol Üst Rozet (Yıldız / Uçuş)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isFlightCard) AppLanguageManager.translate("Uçuş") else "${product.hotelCategory.coerceAtMost(5)}★",
                        style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    )
                }

                // Sağ Alt Rozet (Operatör)
                if (product.safeOperatorName.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TourOSColors.Primary.copy(alpha = 0.9f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = AppLanguageManager.translate(product.safeOperatorName),
                            style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            maxLines = 1
                        )
                    }
                }
            }

            // 2. ORTA: TÜM AÇIKLAMALAR & ÖZELLİKLER
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Başlık & Puan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (product.hotelName.isNotBlank()) product.hotelName else product.tourName,
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (product.hotelRating > 0.0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TourOSColors.PrimaryContainer)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${product.hotelRating}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            )
                        }
                    }
                }

                // 1. Ülke & Bölge Hiyerarşi Rozeti (Veritabanından dinamik ve kesin)
                val fullDestinationText = remember(product.country, product.region, product.subRegion) {
                    val ctry = product.country.trim()
                    val reg = product.region.trim()
                    val sub = product.subRegion.trim()
                    when {
                        ctry.isNotBlank() && reg.isNotBlank() && sub.isNotBlank() && !sub.equals(reg, ignoreCase = true) -> "$ctry · $reg · $sub"
                        ctry.isNotBlank() && reg.isNotBlank() -> "$ctry · $reg"
                        ctry.isNotBlank() -> ctry
                        reg.isNotBlank() -> reg
                        else -> "Türkiye · Antalya"
                    }
                }

                val destAirportBadge = remember(product.country, product.region, product.subRegion) {
                    val combined = "${product.country} ${product.region} ${product.subRegion}".lowercase()
                    when {
                        combined.contains("antalya") || combined.contains("kemer") || combined.contains("belek") || combined.contains("lara") || combined.contains("side") || combined.contains("alanya") || combined.contains("manavgat") -> "AYT (Antalya)"
                        combined.contains("bodrum") -> "BJV (Bodrum)"
                        combined.contains("dalaman") || combined.contains("marmaris") || combined.contains("fethiye") -> "DLM (Dalaman)"
                        combined.contains("istanbul") -> "IST (İstanbul)"
                        combined.contains("dubai") -> "DXB (Dubai)"
                        combined.contains("şarm") || combined.contains("sharm") -> "SSH (Şarm El-Şeyh)"
                        combined.contains("hurgada") || combined.contains("hurghada") -> "HRG (Hurgada)"
                        combined.contains("phuket") || combined.contains("пхукет") || combined.contains("патонг") -> "HKT (Phuket)"
                        combined.contains("bangkok") || combined.contains("бангкок") || combined.contains("паттайя") || combined.contains("pattaya") -> "BKK (Bangkok)"
                        combined.contains("vietnam") || combined.contains("nha trang") || combined.contains("нячанг") -> "CXR (Cam Ranh)"
                        combined.contains("moskova") || combined.contains("москва") -> "VKO/SVO (Moskova)"
                        else -> null
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Acenta / Tur Operatörü Rozeti
                    val opName = product.safeOperatorName.ifBlank { product.operatorName.ifBlank { "Coral Travel" } }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TourOSColors.Primary.copy(alpha = 0.12f))
                            .border(1.dp, TourOSColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = AppLanguageManager.translate(opName),
                            style = TourOSTypography.Caption.copy(
                                color = TourOSColors.Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            maxLines = 1
                        )
                    }

                    // 2. Ülke & Bölge Hiyerarşi Rozeti
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE6F4F1))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = fullDestinationText,
                            style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            maxLines = 1
                        )
                    }

                    if (destAirportBadge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = destAirportBadge,
                                style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontWeight = FontWeight.SemiBold, fontSize = 9.sp),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Tarih & Gece, Konsept, Sahil & Donanım Rozetleri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tourDateRange = remember(product.departureDate, product.nights) {
                        val dep = product.departureDate?.takeIf { it.isNotBlank() } ?: "16.09.2026"
                        val n = if (product.nights > 0) product.nights else 7
                        val ret = try {
                            if (dep.contains("-")) {
                                val parts = dep.split("-")
                                if (parts.size == 3) {
                                    val y = parts[0].toIntOrNull() ?: 2026
                                    val m = parts[1].toIntOrNull() ?: 9
                                    val d = parts[2].toIntOrNull() ?: 16
                                    val (rd, rm, ry) = com.mgacreative.touros.addDaysToTriple(Triple(d, m, y), n)
                                    "${rd.toString().padStart(2, '0')}.${rm.toString().padStart(2, '0')}.$ry"
                                } else null
                            } else if (dep.contains(".")) {
                                val parts = dep.split(".")
                                if (parts.size == 3) {
                                    val d = parts[0].toIntOrNull() ?: 16
                                    val m = parts[1].toIntOrNull() ?: 9
                                    val y = parts[2].toIntOrNull() ?: 2026
                                    val (rd, rm, ry) = com.mgacreative.touros.addDaysToTriple(Triple(d, m, y), n)
                                    "${rd.toString().padStart(2, '0')}.${rm.toString().padStart(2, '0')}.$ry"
                                } else null
                            } else null
                        } catch (_: Exception) { null }
                        if (!ret.isNullOrBlank() && ret != dep) "$dep — $ret" else "$dep — 23.09.2026"
                    }
                    val nightsCount = if (product.nights > 0) product.nights else 7

                    // 1. Tarih Aralığı Rozeti
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEFF6FF))
                            .border(0.5.dp, Color(0xFFBFDBFE), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "📅 $tourDateRange",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF1E40AF), fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                            maxLines = 1
                        )
                    }

                    // 2. Gece Rozeti
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF0FDF4))
                            .border(0.5.dp, Color(0xFFBBF7D0), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🌙 $nightsCount ${AppLanguageManager.translate("Gece")}",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                            maxLines = 1
                        )
                    }

                    if (product.mealType.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = AppLanguageManager.translate(product.mealType),
                                style = TourOSTypography.Caption.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            )
                        }
                    }

                    if (product.beachLine in 1..3) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEFF6FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (product.beachLine == 1) "< 100m" else if (product.beachLine == 2) "< 500m" else "< 2km",
                                style = TourOSTypography.Caption.copy(color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                            )
                        }
                    }

                    if (product.hasTransfer) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFDF4FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = AppLanguageManager.translate("Transfer"),
                                style = TourOSTypography.Caption.copy(color = Color(0xFF9333EA), fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                            )
                        }
                    }
                }

                // Oda Tipi / Uçuş Bilgisi
                if (product.roomType.isNotBlank() || product.flightNumber.isNotBlank()) {
                    Text(
                        text = if (isFlightCard) "PNR / Sefer: ${product.flightNumber.ifBlank { "VKO - AYT Charter" }}" else AppLanguageManager.translate(product.roomType),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3. SAĞ: FİYAT VE SEÇİM BUTONU
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(product.price, decimals = false)} ${product.currency}",
                        style = TourOSTypography.Caption.copy(
                            color = TourOSColors.TextSecondary,
                            textDecoration = TextDecoration.LineThrough,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppLanguageManager.translate("Anında Onay"),
                        style = TourOSTypography.Caption.copy(color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                }

                Text(
                    text = "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(marginCalculatedPrice, decimals = false)} ${product.currency}",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                )

                val buttonText = when {
                    isSelected -> AppLanguageManager.translate("Seçildi")
                    else -> AppLanguageManager.translate("Rezerve Et")
                }

                Button(
                    onClick = onSelectForBooking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF64748B) else TourOSColors.Primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = buttonText,
                        style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }
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
                            text = "+${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(option.priceDeltaRub, decimals = false)} RUB",
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
                        text = AppLanguageManager.translate("Zorunlu Uçuş Farkları ve Ek Ücret Dökümü (Mandatory Surcharges)"),
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
    currency: String = "EUR",
    onToggle: () -> Unit
) {
    val conversionRate = when (currency.uppercase()) {
        "RUB" -> 100.0
        "TRY", "TL" -> 38.0
        "USD" -> 1.08
        else -> 1.0
    }
    val unitPriceInCurrency = service.unitPriceEur * conversionRate
    val totalPriceInCurrency = unitPriceInCurrency * service.paxCount

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
                        text = "Kişi Başı: ${unitPriceInCurrency.toInt()} $currency  ·  Toplam (${service.paxCount} Yolcu): ${totalPriceInCurrency.toInt()} $currency",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }
            }

            Text(
                text = "${totalPriceInCurrency.toInt()} $currency",
                style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── GÖRSEL 5 & 6 ESİNTİLİ YOLCU FORM KART BİLEŞENİ ───────────────────────────

@Composable
private fun B2BCompactField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = TourOSTypography.Caption.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TourOSColors.TextSecondary
            ),
            maxLines = 1
        )
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TourOSTypography.BodyMedium.copy(
                fontSize = 13.sp,
                color = TourOSColors.TextPrimary,
                fontWeight = FontWeight.Medium
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TourOSColors.Background)
                        .border(1.dp, TourOSColors.Border, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = TourOSTypography.Caption.copy(fontSize = 12.sp, color = TourOSColors.TextDisabled)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

// ─── GÖRSEL 5 & 6 ESİNTİLİ ULTRA-KOMPAKT B2B YOLCU FORM KART BİLEŞENİ ─────────

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
        contentPadding = 8.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            var showTypeAgeMenu by remember { mutableStateOf(false) }

            // 1. ÜST BAŞLIK: TİP / YAŞ SEÇİCİ, TURİST ADI VE CİNSİYET (KOMPAKT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val typeBadgeText = when (passenger.passengerType) {
                        "INFANT" -> "${AppLanguageManager.translate("Bebek")} (${passenger.childAge ?: 1} ${AppLanguageManager.translate("Yaş")})"
                        "CHILD" -> "${AppLanguageManager.translate("Çocuk")} (${passenger.childAge ?: 5} ${AppLanguageManager.translate("Yaş")})"
                        else -> AppLanguageManager.translate("Yetişkin")
                    }
                    val badgeColor = when (passenger.passengerType) {
                        "INFANT" -> Color(0xFFEA580C)
                        "CHILD" -> Color(0xFF0284C7)
                        else -> TourOSColors.Primary
                    }

                    // Tıklanabilir Yaş & Tip Açılır Menüsü
                    Box {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { showTypeAgeMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "$typeBadgeText ▼",
                                    style = TourOSTypography.Caption.copy(color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showTypeAgeMenu,
                            onDismissRequest = { showTypeAgeMenu = false },
                            modifier = Modifier.width(260.dp).background(TourOSColors.Surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("${AppLanguageManager.translate("Yetişkin")} (18+ ${AppLanguageManager.translate("Yaş")})", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)) },
                                onClick = {
                                    onUpdatePassenger(passenger.copy(passengerType = "ADULT", childAge = null, documentType = "Pasaport"))
                                    showTypeAgeMenu = false
                                }
                            )
                            HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.5f))
                            (0..17).forEach { age ->
                                val label = when {
                                    age == 0 -> "0 ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Bebek")} < 1 - %90)"
                                    age <= 2 -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Bebek")} - %90)"
                                    age <= 6 -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Küçük Çocuk")} - %50)"
                                    age <= 12 -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Büyük Çocuk")} - %30)"
                                    else -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Genç")} - Standart)"
                                }
                                DropdownMenuItem(
                                    text = { Text(label, style = TourOSTypography.Caption.copy(fontSize = 12.sp)) },
                                    onClick = {
                                        val pType = if (age <= 2) "INFANT" else "CHILD"
                                        val docType = if (age <= 2) "Doğum Belgesi / Pasaport" else "Pasaport"
                                        onUpdatePassenger(passenger.copy(passengerType = pType, childAge = age, documentType = docType))
                                        showTypeAgeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "${AppLanguageManager.translate("Turist")} $paxIndex ${if (passenger.isPayer) "(${AppLanguageManager.translate("Sipariş Veren Lead")})" else ""}",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    )
                    if (canRemove) {
                        IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TourOSColors.Secondary, modifier = Modifier.size(12.dp))
                        }
                    }
                }

                // Cinsiyet Seçimi (Kompakt Butonlar)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val isMale = passenger.gender == "MALE"
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isMale) TourOSColors.Primary else TourOSColors.Background,
                        border = BorderStroke(1.dp, if (isMale) TourOSColors.Primary else TourOSColors.Border),
                        modifier = Modifier.clickable { onUpdatePassenger(passenger.copy(gender = "MALE")) }
                    ) {
                        Text(
                            text = AppLanguageManager.translate("Bay (Муж)"),
                            style = TourOSTypography.Caption.copy(
                                color = if (isMale) Color.White else TourOSColors.TextPrimary,
                                fontWeight = if (isMale) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    val isFemale = passenger.gender == "FEMALE"
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isFemale) TourOSColors.Primary else TourOSColors.Background,
                        border = BorderStroke(1.dp, if (isFemale) TourOSColors.Primary else TourOSColors.Border),
                        modifier = Modifier.clickable { onUpdatePassenger(passenger.copy(gender = "FEMALE")) }
                    ) {
                        Text(
                            text = AppLanguageManager.translate("Bayan (Жен)"),
                            style = TourOSTypography.Caption.copy(
                                color = if (isFemale) Color.White else TourOSColors.TextPrimary,
                                fontWeight = if (isFemale) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // 2. ANA FORM: TEK SATIRDA 6 SÜTUN GRID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                B2BCompactField(
                    value = passenger.firstName,
                    onValueChange = { onUpdatePassenger(passenger.copy(firstName = it.uppercase())) },
                    label = AppLanguageManager.translate("Adı (İмя)"),
                    placeholder = "",
                    modifier = Modifier.weight(1.1f)
                )
                B2BCompactField(
                    value = passenger.lastName,
                    onValueChange = { onUpdatePassenger(passenger.copy(lastName = it.uppercase())) },
                    label = AppLanguageManager.translate("Soyadı (Фамилия)"),
                    placeholder = "",
                    modifier = Modifier.weight(1.1f)
                )

                // EĞER ÇOCUK/BEBEK İSE SÜTUN İÇİNDE DİREKT YAŞ SEÇİM KUTUSU
                if (passenger.passengerType != "ADULT") {
                    var showGridAgeMenu by remember { mutableStateOf(false) }
                    val currentAge = passenger.childAge ?: 5
                    Column(modifier = Modifier.weight(1.0f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = AppLanguageManager.translate("Çocuk Yaşı"),
                            style = TourOSTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7)),
                            maxLines = 1
                        )
                        Box {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().height(34.dp).clickable { showGridAgeMenu = true }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$currentAge Yaş",
                                        style = TourOSTypography.BodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                    )
                                    Text("▼", fontSize = 10.sp, color = Color(0xFF0284C7))
                                }
                            }

                            DropdownMenu(
                                expanded = showGridAgeMenu,
                                onDismissRequest = { showGridAgeMenu = false },
                                modifier = Modifier.width(260.dp).background(TourOSColors.Surface)
                            ) {
                                (0..17).forEach { age ->
                                    val label = when {
                                        age == 0 -> "0 ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Bebek")} < 1 - %90)"
                                        age <= 2 -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Bebek")} - %90)"
                                        age <= 6 -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Küçük Çocuk")} - %50)"
                                        age <= 12 -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Büyük Çocuk")} - %30)"
                                        else -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Genç")} - Standart)"
                                    }
                                    DropdownMenuItem(
                                        text = { Text(label, style = TourOSTypography.Caption.copy(fontSize = 12.sp, fontWeight = if (age == currentAge) FontWeight.Bold else FontWeight.Normal)) },
                                        onClick = {
                                            val pType = if (age <= 2) "INFANT" else "CHILD"
                                            val docType = if (age <= 2) "Doğum Belgesi / Pasaport" else "Pasaport"
                                            onUpdatePassenger(passenger.copy(passengerType = pType, childAge = age, documentType = docType))
                                            showGridAgeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                B2BCompactField(
                    value = passenger.birthDate,
                    onValueChange = { onUpdatePassenger(passenger.copy(birthDate = com.mgacreative.touros.utils.DateUtils.formatDateInput(it))) },
                    label = AppLanguageManager.translate("Doğum Tarihi"),
                    placeholder = "GG.AA.YYYY",
                    modifier = Modifier.weight(1.0f)
                )
                B2BCompactField(
                    value = passenger.citizenship,
                    onValueChange = { onUpdatePassenger(passenger.copy(citizenship = it.uppercase())) },
                    label = AppLanguageManager.translate("Uyruk"),
                    placeholder = "",
                    modifier = Modifier.weight(0.9f)
                )
                B2BCompactField(
                    value = passenger.passportNumber,
                    onValueChange = { onUpdatePassenger(passenger.copy(passportNumber = it.uppercase())) },
                    label = AppLanguageManager.translate("Pasaport No"),
                    placeholder = "",
                    modifier = Modifier.weight(1.0f)
                )
                if (passenger.passengerType == "ADULT") {
                    B2BCompactField(
                        value = passenger.documentExpiryDate,
                        onValueChange = { onUpdatePassenger(passenger.copy(documentExpiryDate = com.mgacreative.touros.utils.DateUtils.formatDateInput(it))) },
                        label = AppLanguageManager.translate("Son Geçerlilik"),
                        placeholder = "GG.AA.YYYY",
                        modifier = Modifier.weight(1.0f)
                    )
                }
            }

            // 3. ALT ŞERİT (Turist 1 için İletişim, Çocuk için Sorumlu Yetişkin)
            if (passenger.isPayer) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(TourOSColors.PrimaryContainer.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TourOSColors.TextSecondary, modifier = Modifier.size(12.dp))
                        Text(
                            text = AppLanguageManager.translate("Telefon:"),
                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary, fontSize = 11.sp)
                        )
                        androidx.compose.foundation.text.BasicTextField(
                            value = passenger.phone,
                            onValueChange = { onUpdatePassenger(passenger.copy(phone = it)) },
                            singleLine = true,
                            textStyle = TourOSTypography.Caption.copy(fontSize = 12.sp, color = TourOSColors.Primary, fontWeight = FontWeight.SemiBold),
                            decorationBox = { inner ->
                                Box(modifier = Modifier.width(130.dp).background(TourOSColors.Surface, RoundedCornerShape(4.dp)).border(1.dp, TourOSColors.Border, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    if (passenger.phone.isEmpty()) Text("+90 532...", style = TourOSTypography.Caption.copy(color = TourOSColors.TextDisabled, fontSize = 11.sp))
                                    inner()
                                }
                            }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TourOSColors.TextSecondary, modifier = Modifier.size(12.dp))
                        Text(
                            text = AppLanguageManager.translate("E-posta:"),
                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary, fontSize = 11.sp)
                        )
                        androidx.compose.foundation.text.BasicTextField(
                            value = passenger.email,
                            onValueChange = { onUpdatePassenger(passenger.copy(email = it)) },
                            singleLine = true,
                            textStyle = TourOSTypography.Caption.copy(fontSize = 12.sp, color = TourOSColors.Primary, fontWeight = FontWeight.SemiBold),
                            decorationBox = { inner ->
                                Box(modifier = Modifier.width(180.dp).background(TourOSColors.Surface, RoundedCornerShape(4.dp)).border(1.dp, TourOSColors.Border, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    if (passenger.email.isEmpty()) Text("ornek@mail.com", style = TourOSTypography.Caption.copy(color = TourOSColors.TextDisabled, fontSize = 11.sp))
                                    inner()
                                }
                            }
                        )
                    }
                }
            } else if (passenger.passengerType != "ADULT") {
                var showSubAgeMenu by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(TourOSColors.PrimaryContainer.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cAge = passenger.childAge ?: 5
                    val discountLabel = when {
                        cAge <= 2 -> "%90 İndirim (Bebek)"
                        cAge <= 6 -> "%50 İndirim"
                        cAge <= 12 -> "%30 İndirim"
                        else -> "Standart"
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${AppLanguageManager.translate("Sorumlu Yetişkin:")} Turist 1 (Lead)",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        )

                        Box {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { showSubAgeMenu = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${AppLanguageManager.translate("Yaş")}: $cAge ($discountLabel)",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showSubAgeMenu,
                                onDismissRequest = { showSubAgeMenu = false },
                                modifier = Modifier.width(260.dp).background(TourOSColors.Surface)
                            ) {
                                (0..17).forEach { age ->
                                    val label = when {
                                        age == 0 -> "0 ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Bebek")} < 1 - %90)"
                                        age <= 2 -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Bebek")} - %90)"
                                        age <= 6 -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Küçük Çocuk")} - %50)"
                                        age <= 12 -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Büyük Çocuk")} - %30)"
                                        else -> "$age ${AppLanguageManager.translate("Yaş")} (${AppLanguageManager.translate("Genç")} - Standart)"
                                    }
                                    DropdownMenuItem(
                                        text = { Text(label, style = TourOSTypography.Caption.copy(fontSize = 12.sp, fontWeight = if (age == cAge) FontWeight.Bold else FontWeight.Normal)) },
                                        onClick = {
                                            val pType = if (age <= 2) "INFANT" else "CHILD"
                                            val docType = if (age <= 2) "Doğum Belgesi / Pasaport" else "Pasaport"
                                            onUpdatePassenger(passenger.copy(passengerType = pType, childAge = age, documentType = docType))
                                            showSubAgeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = passenger.isInfantSeatRequested,
                            onCheckedChange = { isChecked ->
                                onUpdatePassenger(passenger.copy(isInfantSeatRequested = isChecked))
                            },
                            colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppLanguageManager.translate("İnfant Ayrı Koltuk"),
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 11.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun B2BDateRangePickerDialog(
    initialStartDateText: String,
    initialEndDateText: String,
    title: String = "Gidiş Tarih Aralığı Seçin",
    onDateRangeSelected: (startDate: String, endDate: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val monthNames = listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
    
    fun parseDate(dStr: String, defaultDay: Int, defaultMonth: Int, defaultYear: Int): Triple<Int, Int, Int> {
        val parts = if (dStr.contains(".")) {
            dStr.split(".")
        } else if (dStr.contains("-")) {
            val p = dStr.split("-")
            if (p.size == 3 && p[0].length == 4) listOf(p[2], p[1], p[0]) else p
        } else listOf()
        val d = parts.getOrNull(0)?.toIntOrNull() ?: defaultDay
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: defaultMonth).coerceIn(1, 12)
        val y = parts.getOrNull(2)?.toIntOrNull() ?: defaultYear
        return Triple(d, m, y)
    }

    val today = remember { com.mgacreative.touros.getTodayTriple() }
    val defaultEnd = remember { com.mgacreative.touros.addDaysToTriple(today, 7) }
    val (initStartDay, initStartMonth, initStartYear) = parseDate(initialStartDateText, today.first, today.second, today.third)
    val (initEndDay, initEndMonth, initEndYear) = parseDate(initialEndDateText, defaultEnd.first, defaultEnd.second, defaultEnd.third)

    var startDay by remember { mutableStateOf(initStartDay) }
    var startMonth by remember { mutableStateOf(initStartMonth) }
    var startYear by remember { mutableStateOf(initStartYear) }

    var endDay by remember { mutableStateOf<Int?>(initEndDay) }
    var endMonth by remember { mutableStateOf<Int?>(initEndMonth) }
    var endYear by remember { mutableStateOf<Int?>(initEndYear) }

    var viewMonth by remember { mutableStateOf(initStartMonth) }
    var viewYear by remember { mutableStateOf(initStartYear) }

    fun getDaysInMonth(m: Int, y: Int): Int {
        return when (m) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)) 29 else 28
            else -> 31
        }
    }

    fun getFirstDayOfWeek(m: Int, y: Int): Int {
        val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
        val yr = if (m < 3) y - 1 else y
        val dayOfWeekSunday0 = (yr + yr / 4 - yr / 100 + yr / 400 + t[m - 1] + 1) % 7
        return if (dayOfWeekSunday0 == 0) 6 else dayOfWeekSunday0 - 1
    }

    fun toAbsoluteDays(d: Int, m: Int, y: Int): Int {
        return y * 365 + m * 31 + d
    }

    val maxDays = getDaysInMonth(viewMonth, viewYear)
    val firstDayOffset = getFirstDayOfWeek(viewMonth, viewYear)

    val startAbs = toAbsoluteDays(startDay, startMonth, startYear)
    val endAbs = if (endDay != null && endMonth != null && endYear != null) toAbsoluteDays(endDay!!, endMonth!!, endYear!!) else null

    val in7 = remember(today) { com.mgacreative.touros.addDaysToTriple(today, 7) }
    val in14 = remember(today) { com.mgacreative.touros.addDaysToTriple(today, 14) }
    val nextMonth = remember(today) { if (today.second == 12) 1 else today.second + 1 }
    val nextMonthYear = remember(today) { if (today.second == 12) today.third + 1 else today.third }
    val monthAfter = remember(nextMonth) { if (nextMonth == 12) 1 else nextMonth + 1 }
    val monthAfterYear = remember(nextMonth, nextMonthYear) { if (nextMonth == 12) nextMonthYear + 1 else nextMonthYear }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TourOSButton(
                text = "Tarih Aralığını Uygula",
                onClick = {
                    val sDayStr = startDay.toString().padStart(2, '0')
                    val sMonthStr = startMonth.toString().padStart(2, '0')
                    val eD = endDay ?: (startDay + 7).coerceAtMost(maxDays)
                    val eM = endMonth ?: viewMonth
                    val eY = endYear ?: viewYear
                    val eDayStr = eD.toString().padStart(2, '0')
                    val eMonthStr = eM.toString().padStart(2, '0')
                    onDateRangeSelected("$sDayStr.$sMonthStr.$startYear", "$eDayStr.$eMonthStr.$eY")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppLanguageManager.translate(title),
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.widthIn(max = 380.dp)) {
                // Hızlı Aralık Seçim Çipleri (Canlı Bugünden İtibaren)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Önümüzdeki 7 Gün" to (today to in7),
                        "Önümüzdeki 14 Gün" to (today to in14),
                        "${monthNames[today.second - 1]} ${today.third}" to (Triple(1, today.second, today.third) to Triple(getDaysInMonth(today.second, today.third), today.second, today.third)),
                        "${monthNames[nextMonth - 1]} $nextMonthYear" to (Triple(1, nextMonth, nextMonthYear) to Triple(getDaysInMonth(nextMonth, nextMonthYear), nextMonth, nextMonthYear)),
                        "${monthNames[monthAfter - 1]} $monthAfterYear" to (Triple(1, monthAfter, monthAfterYear) to Triple(getDaysInMonth(monthAfter, monthAfterYear), monthAfter, monthAfterYear))
                    ).forEach { (label, range) ->
                        val (startT, endT) = range
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TourOSColors.Background,
                            border = BorderStroke(1.dp, TourOSColors.Border),
                            modifier = Modifier.clickable {
                                startDay = startT.first
                                startMonth = startT.second
                                startYear = startT.third
                                endDay = endT.first
                                endMonth = endT.second
                                endYear = endT.third
                                viewMonth = startT.second
                                viewYear = startT.third
                            }
                        ) {
                            Text(
                                text = label,
                                style = TourOSTypography.Caption.copy(fontSize = 11.sp, color = TourOSColors.Primary, fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Ay / Yıl Seçim Başlığı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TourOSColors.Background)
                            .clickable {
                                if (viewMonth > 1) {
                                    viewMonth--
                                } else {
                                    viewMonth = 12
                                    viewYear--
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("◄", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                    }

                    Text(
                        text = "${monthNames[viewMonth - 1]} $viewYear",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Bold)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TourOSColors.Background)
                            .clickable {
                                if (viewMonth < 12) {
                                    viewMonth++
                                } else {
                                    viewMonth = 1
                                    viewYear++
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("►", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                    }
                }

                // Gün Başlıkları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz").forEach { dayName ->
                        Text(
                            text = dayName,
                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextSecondary),
                            modifier = Modifier.width(32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Günler Grid
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(firstDayOffset + maxDays) { index ->
                        if (index < firstDayOffset) {
                            Spacer(modifier = Modifier.size(32.dp))
                        } else {
                            val day = index - firstDayOffset + 1
                            val thisAbs = toAbsoluteDays(day, viewMonth, viewYear)
                            val isStart = (day == startDay && viewMonth == startMonth && viewYear == startYear)
                            val isEnd = (endDay != null && day == endDay && viewMonth == endMonth && viewYear == endYear)
                            val isInRange = endAbs != null && thisAbs > startAbs && thisAbs < endAbs

                            val bgColor = when {
                                isStart || isEnd -> TourOSColors.Primary
                                isInRange -> TourOSColors.PrimaryContainer
                                else -> TourOSColors.Background
                            }

                            val textColor = when {
                                isStart || isEnd -> Color.White
                                isInRange -> TourOSColors.Primary
                                else -> TourOSColors.TextPrimary
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bgColor)
                                    .border(1.dp, if (isStart || isEnd) TourOSColors.Primary else TourOSColors.Border.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (endDay != null || thisAbs < startAbs) {
                                            startDay = day
                                            startMonth = viewMonth
                                            startYear = viewYear
                                            endDay = null
                                            endMonth = null
                                            endYear = null
                                        } else {
                                            endDay = day
                                            endMonth = viewMonth
                                            endYear = viewYear
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$day",
                                    style = TourOSTypography.Caption.copy(
                                        color = textColor,
                                        fontWeight = if (isStart || isEnd || isInRange) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }

                // Seçim Özet Kutusu
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = TourOSColors.PrimaryContainer.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sText = "${startDay.toString().padStart(2, '0')}.${startMonth.toString().padStart(2, '0')}.$startYear"
                        val eText = if (endDay != null) "${endDay.toString().padStart(2, '0')}.${endMonth?.toString()?.padStart(2, '0')}.$endYear" else "(Bitiş Seçin)"
                        Text(
                            text = "Seçilen: $sText — $eText",
                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary, fontSize = 11.sp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
internal fun SimpleDatePickerDialog(
    title: String,
    initialDateText: String,
    onDateSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val monthNames = com.mgacreative.touros.utils.DateUtils.monthNamesTr.map { AppLanguageManager.translate(it) }
    
    val parts = if (initialDateText.contains(".")) {
        initialDateText.split(".")
    } else if (initialDateText.contains("-")) {
        val p = initialDateText.split("-")
        if (p.size == 3 && p[0].length == 4) listOf(p[2], p[1], p[0]) else p
    } else listOf()

    val initDay = parts.getOrNull(0)?.toIntOrNull() ?: 18
    val initMonth = (parts.getOrNull(1)?.toIntOrNull() ?: 8).coerceIn(1, 12)
    val initYear = parts.getOrNull(2)?.toIntOrNull() ?: 2026

    var selectedDay by remember { mutableStateOf(initDay) }
    var currentMonth by remember { mutableStateOf(initMonth) }
    var currentYear by remember { mutableStateOf(initYear) }

    fun getDaysInMonth(m: Int, y: Int): Int {
        return when (m) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)) 29 else 28
            else -> 31
        }
    }

    fun getFirstDayOfWeek(m: Int, y: Int): Int {
        val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
        val yr = if (m < 3) y - 1 else y
        val dayOfWeekSunday0 = (yr + yr / 4 - yr / 100 + yr / 400 + t[m - 1] + 1) % 7
        return if (dayOfWeekSunday0 == 0) 6 else dayOfWeekSunday0 - 1
    }

    val maxDays = getDaysInMonth(currentMonth, currentYear)
    if (selectedDay > maxDays) {
        selectedDay = maxDays
    }
    val firstDayOffset = getFirstDayOfWeek(currentMonth, currentYear)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TourOSButton(
                text = AppLanguageManager.translate("Tarihi Seç"),
                onClick = {
                    val dayStr = selectedDay.toString().padStart(2, '0')
                    val monthStr = currentMonth.toString().padStart(2, '0')
                    onDateSelected("$dayStr.$monthStr.$currentYear")
                    onDismissRequest()
                }
            )
        },
        dismissButton = {
            TourOSButton(
                text = AppLanguageManager.translate("İptal"),
                variant = TourOSButtonVariant.TERTIARY,
                onClick = onDismissRequest
            )
        },
        title = {
            Text(
                text = AppLanguageManager.translate(title),
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                // Ay & Yıl Gezinti Başlığı (◄ / ► Okları İle Ay Değiştirme)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TourOSColors.Background)
                            .clickable {
                                if (currentMonth > 1) {
                                    currentMonth--
                                } else {
                                    currentMonth = 12
                                    currentYear--
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("◄", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                    }

                    Text(
                        text = "${monthNames[currentMonth - 1]} $currentYear",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Bold)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TourOSColors.Background)
                            .clickable {
                                if (currentMonth < 12) {
                                    currentMonth++
                                } else {
                                    currentMonth = 1
                                    currentYear++
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("►", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                    }
                }

                // Haftanın Günleri Başlığı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    com.mgacreative.touros.utils.DateUtils.dayNamesTr.forEach { dayName ->
                        Text(
                            text = AppLanguageManager.translate(dayName),
                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextSecondary),
                            modifier = Modifier.width(32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Günler Grid
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(firstDayOffset + maxDays) { index ->
                        if (index < firstDayOffset) {
                            Spacer(modifier = Modifier.size(32.dp))
                        } else {
                            val day = index - firstDayOffset + 1
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

@Composable
fun B2BTouristAndChildAgePickerDialog(
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
                        text = AppLanguageManager.translate("Yolcu & Turist Sayısı"),
                        style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Text("✕", fontSize = 14.sp, color = TourOSColors.TextSecondary)
                    }
                }

                // 1. Yetişkin Sayacı (Adults Counter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Yetişkinler", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text("18 yaş ve üzeri", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
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

                // 2. Çocuk Sayacı (Children Counter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Çocuklar & Bebekler", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text("0 - 17 yaş arası", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
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
                                    tempChildAges = tempChildAges + listOf(5) // Varsayılan 5 yaş
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

                // 3. Çocuk Yaşları Seçimi (Dinamik)
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
                            text = AppLanguageManager.translate("Çocuk Yaşları & İndirim Oranları:"),
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
                                    text = "${index + 1}. Çocuk $categoryText",
                                    style = TourOSTypography.Caption.copy(fontSize = 11.sp, color = TourOSColors.TextPrimary)
                                )
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, TourOSColors.Primary),
                                        color = TourOSColors.Surface,
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
                                        onDismissRequest = { showAgeMenu = false }
                                    ) {
                                        (0..17).forEach { a ->
                                            val label = when (a) {
                                                0 -> "0 Yaş (Bebek < 1 yaş)"
                                                1, 2 -> "$a Yaş (Bebek)"
                                                in 3..6 -> "$a Yaş (Küçük Çocuk)"
                                                in 7..12 -> "$a Yaş (Büyük Çocuk)"
                                                else -> "$a Yaş (Genç)"
                                            }
                                            DropdownMenuItem(
                                                text = { Text(label, style = TourOSTypography.Caption.copy(fontSize = 12.sp)) },
                                                onClick = {
                                                    val updated = tempChildAges.toMutableList()
                                                    if (index < updated.size) {
                                                        updated[index] = a
                                                        tempChildAges = updated
                                                    }
                                                    showAgeMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Uygula Butonu
                TourOSButton(
                    text = "Seçimi Uygula",
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


