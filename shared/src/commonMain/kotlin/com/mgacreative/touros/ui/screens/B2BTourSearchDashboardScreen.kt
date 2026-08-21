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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
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

    val dailyQuota by viewModel.dailyQuota.collectAsState()
    val todayQueries by viewModel.todayQueries.collectAsState()
    val monthlyQuota by viewModel.monthlyQuota.collectAsState()
    val currentQueries by viewModel.currentQueries.collectAsState()
    val isQuotaExceeded by viewModel.isQuotaExceeded.collectAsState()
    val quotaErrorMessage by viewModel.quotaErrorMessage.collectAsState()

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

    // Paket Turlar & Oteller Sekmesi Özel Ekstra Detaylı Filtreler
    var selectedBeachLine by remember { mutableStateOf(0) } // 0: Hepsi, 1: <100m, 2: <500m, 3: <2km
    var minRating by remember { mutableStateOf(0.0) } // 0.0: Hepsi, 7+, 8+, 9+
    var selectedAmenities by remember { mutableStateOf(setOf<String>()) }
    var isDirectFlightOnly by remember { mutableStateOf(false) }
    var isTransferIncludedOnly by remember { mutableStateOf(false) }
    var activeStep by remember { mutableStateOf(1) }
    var showSuccessModal by remember { mutableStateOf(false) }
    var selectedProductForOperatorModal by remember { mutableStateOf<UnifiedProductEntity?>(null) }

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

    // ── 🏬 OPERATÖR KARŞILAŞTIRMA & TEKLİF LİSTESİ MODALI (B2B METASEARCH) ────────
    if (selectedProductForOperatorModal != null) {
        val prod = selectedProductForOperatorModal!!
        val isFlightProd = prod.safeProductType.uppercase().contains("FLIGHT") || prod.flightNumber.isNotBlank() || prod.tourName.contains("Uçuş", ignoreCase = true)
        val basePrice = prod.price

        val effectiveAgencyPrices = listOf(
            AgencyPriceOption("AGN-CORAL", "${prod.safeOperatorName.ifBlank { "Coral Travel" }} B2B Main", prod.safeOperatorName.ifBlank { "Coral Travel" }, prod.roomType.ifBlank { "Standart Oda" }, prod.mealType.ifBlank { "Her Şey Dahil" }, basePrice, isBestDeal = true),
            AgencyPriceOption("AGN-ANEX", "Anex Tour B2B Partner", "Anex Tour", if (isFlightProd) "Ekonomi Uçuş" else "Deluxe Room", if (isFlightProd) "Standart Bagaj" else "Her Şey Dahil", basePrice * 1.08),
            AgencyPriceOption("AGN-PEGAS", "Pegas Touristik Agency", "Pegas Touristik", if (isFlightProd) "Flexi Uçuş" else "Standard Room", if (isFlightProd) "20kg Bagaj" else "Oda Kahvaltı", basePrice * 1.15),
            AgencyPriceOption("AGN-TRAVELATA", "Travelata B2B Online", "Travelata", if (isFlightProd) "Promo Uçuş" else "Promo Room", if (isFlightProd) "El Bagajı" else "Bez pitaniya", basePrice * 1.04),
            AgencyPriceOption("AGN-SUNEX", "SunExpress Charter B2B", "SunExpress", if (isFlightProd) "Charter Sefer" else "Standart Oda", if (isFlightProd) "Sıcak İkram" else "Ultra Her Şey Dahil", basePrice * 1.10)
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
                                text = "📍 ${prod.region}, ${prod.country} • ID: ${prod.id}",
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
                            text = "🏬 " + AppLanguageManager.translate("Hangi Acente / Operatör Kaç Satıyor?"),
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
                                                text = "🏢 ${option.agencyName}",
                                                style = TourOSTypography.BodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            )
                                            if (option.isBestDeal) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF22C55E))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(AppLanguageManager.translate("En İyi Fiyat ⭐"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp))
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
                                            text = "${option.price.toInt()} ${if (prod.currency == "RUB") "RUB" else "₺"}",
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
                                                    text = AppLanguageManager.translate("Rezerve Et ➔"),
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
                                        "📅 Bugün: $todayQueries/$dailyQuota",
                                        style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                    Text("|", style = TourOSTypography.Caption.copy(color = Color.White.copy(alpha = 0.6f)))
                                    Text(
                                        "🗓️ Bu Ay: $currentQueries/${if (monthlyQuota > 0) monthlyQuota else "∞"}",
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
                                    Text("⛔", fontSize = 22.sp)
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

                        // 3. AKILLI ARAMA BARI (Nereden | Nereye | Başlangıç Tarihi | Bitiş Tarihi | Gece | Turist)
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

                        if (activeSearchTab == "TOURS" || activeSearchTab == "HOTELS") {
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
                                                text = "🔍 Detaylı Filtreler (Operatör, Beslenme, Otel, Sahil, Puan & Donanım)",
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
                                                selectedHotels = emptySet()
                                                selectedBeachLine = 0
                                                minRating = 0.0
                                                selectedAmenities = emptySet()
                                                isDirectFlightOnly = false
                                                isTransferIncludedOnly = false
                                            }) {
                                                Text("↺ Filtreleri Sıfırla", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                            }
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
                                            // 1. TUR OPERATÖRÜ AÇILIR KUTUSU
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
                                        }

                                        HorizontalDivider(color = TourOSColors.Border)

                                        // SATIR 2: SAHİL ŞERİDİ & OTEL DEĞERLENDİRME PUANI
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 1. Sahil Şeridi
                                            Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("🏖️ Sahil Şeridi (Denize Mesafe):", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                                    listOf(0 to "Hepsi", 1 to "1. Hat (<100m)", 2 to "2. Hat (<500m)", 3 to "3. Hat (<2km)").forEach { (code, label) ->
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

                                            // 2. Otel Puanı
                                            Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("🏆 Otel Puanı (Misafir Değerlendirmesi):", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
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

                                        HorizontalDivider(color = TourOSColors.Border)

                                        // SATIR 3: PAKET TUR OTELLERİ & DONANIM/HİZMETLER
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Otel Seçimi Dropdown
                                            Box(modifier = Modifier.weight(1.3f)) {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    TourOSTextField(
                                                        value = if (selectedHotels.isEmpty()) "Tüm Paket Tur Otelleri (${dbProductHotels.size})" else selectedHotels.joinToString(", "),
                                                        onValueChange = { },
                                                        readOnly = true,
                                                        label = "🏨 Paket Tur Otelleri Seçin (Tümü)",
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

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

                                            // Donanım & Hizmetler Filter Chips
                                            Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("✨ Donanım & Özellikler:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                                    listOf("Aquapark", "Wi-Fi", "SPA", "Kum Plaj", "Çocuk Kulübü", "Havuz").forEach { am ->
                                                        val isSelected = am in selectedAmenities
                                                        FilterChip(
                                                            selected = isSelected,
                                                            onClick = {
                                                                selectedAmenities = if (isSelected) selectedAmenities - am else selectedAmenities + am
                                                            },
                                                            label = { Text(am, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

                                        // SATIR 4: HIZLI ONAY VE ULAŞIM SEÇENEKLERİ
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
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
                                                    text = "⚡ Anında Onaylı Turlar",
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
                                                        text = "✈️ Aktarmasız / Direkt Uçuş",
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
                                                    text = "🚐 Transfer Dahil",
                                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Medium)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // KOTA ENGELLEME VE BİLGİLENDİRME UYARISI
                    val isQuotaExceeded by viewModel.isQuotaExceeded.collectAsState()
                    val quotaErrorMessage by viewModel.quotaErrorMessage.collectAsState()

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
                                Text("⛔ AYLIK ARAMA & SORGU KOTANIZ DOLMUŞTUR", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Secondary, fontWeight = FontWeight.Bold))
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

                                val nightsMatch = isFlightTab || item.nights == nights || item.nights <= 0

                                val isTourOrHotel = (activeSearchTab == "TOURS" || activeSearchTab == "HOTELS")
                                val beachMatch = !isTourOrHotel || selectedBeachLine == 0 || item.beachLine == 0 || item.beachLine == selectedBeachLine
                                val ratingMatch = !isTourOrHotel || minRating <= 0.0 || item.hotelRating <= 0.0 || item.hotelRating >= minRating
                                val amenityMatch = !isTourOrHotel || selectedAmenities.isEmpty() || selectedAmenities.all { am -> item.amenities.any { a -> a.contains(am, ignoreCase = true) } }
                                val directFlightMatch = (activeSearchTab != "TOURS") || !isDirectFlightOnly || item.isDirectFlight
                                val transferMatch = !isTourOrHotel || !isTransferIncludedOnly || item.hasTransfer

                                tabMatch && operatorMatch && mealMatch && starMatch && hotelMatch && queryMatch && flightDepMatch && flightDestMatch && nightsMatch && beachMatch && ratingMatch && amenityMatch && directFlightMatch && transferMatch
                            }.ifEmpty {
                                // Tam gece/filtre eşleşmesi yoksa -> Mevcut olan tur/otel paketlerini getir (hiçbiri gelmiyor sorunu engellendi)
                                rawProducts.filter { item ->
                                    val pType = item.safeProductType.uppercase()
                                    val opName = item.safeOperatorName.uppercase()
                                    val isLocalHotel = item.id.startsWith("local-hotel-") || pType == "LOCAL_HOTEL" || opName == "YEREL OTELLER"
                                    val isLocalTour = item.id.startsWith("local-tour-") || pType == "LOCAL_TOUR" || opName == "YEREL TURLAR"
                                    when (activeSearchTab) {
                                        "TOURS" -> !isLocalHotel && !isLocalTour
                                        "HOTELS" -> !isLocalHotel && !isLocalTour
                                        "FLIGHTS" -> pType.contains("FLIGHT") || item.flightNumber.isNotBlank()
                                        else -> true
                                    }
                                }
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

                                // 📱/💻 KOMPAKT SATIR LİSTE DÜZENİ (B2B Compact Horizontal List Row)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                ) {
                                    products.forEach { item ->
                                        val isSelected = selectedProduct?.id == item.id
                                        TourResultMatrixCard(
                                            product = item,
                                            isSelected = isSelected,
                                            isFlightTab = (activeSearchTab == "FLIGHTS"),
                                            onSelectForBooking = {
                                                selectedProductForOperatorModal = item
                                            }
                                        )
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
    val isFlightCard = isFlightTab || product.safeProductType.uppercase().contains("FLIGHT") || product.flightNumber.isNotBlank() || product.tourName.contains("Uçuş", ignoreCase = true)

    val effectiveImage = remember(product) {
        when {
            product.pictureUrl.isNotBlank() -> product.pictureUrl
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
                        text = if (isFlightCard) "✈️ Uçuş" else "⭐ ${product.hotelCategory.coerceAtMost(5)}★",
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
                            text = product.safeOperatorName,
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
                                text = "🏆 ${product.hotelRating}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            )
                        }
                    }
                }

                // Destinasyon / Konum
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍 ", fontSize = 11.sp)
                    Text(
                        text = "${product.region}, ${product.country}".trim().removePrefix(", ").removeSuffix(", "),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Medium, fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Tarih & Gece, Konsept, Sahil & Donanım Rozetleri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!product.departureDate.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "📅 ${product.departureDate} (${product.nights} Gece)",
                                style = TourOSTypography.Caption.copy(color = Color(0xFF334155), fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                            )
                        }
                    }

                    if (product.mealType.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = product.mealType,
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
                                text = "🏖️ ${product.beachLine}. Hat",
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
                                text = "🚐 Transfer",
                                style = TourOSTypography.Caption.copy(color = Color(0xFF9333EA), fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                            )
                        }
                    }
                }

                // Oda Tipi / Uçuş Bilgisi
                if (product.roomType.isNotBlank() || product.flightNumber.isNotBlank()) {
                    Text(
                        text = if (isFlightCard) "✈️ PNR / Sefer: ${product.flightNumber.ifBlank { "VKO - AYT Charter" }}" else "🏨 ${product.roomType}",
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
                        text = "${product.price.toInt()} ${product.currency}",
                        style = TourOSTypography.Caption.copy(
                            color = TourOSColors.TextSecondary,
                            textDecoration = TextDecoration.LineThrough,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "⚡ Anında Onay",
                        style = TourOSTypography.Caption.copy(color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                }

                Text(
                    text = "${marginCalculatedPrice.toInt()} ${product.currency}",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                )

                val buttonText = when {
                    isSelected -> AppLanguageManager.translate("Seçildi ✓")
                    isFlightCard -> AppLanguageManager.translate("Uçuş Seç ➔")
                    else -> AppLanguageManager.translate("Turu Seç & Detaylandır ➔")
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
internal fun SimpleDatePickerDialog(
    title: String,
    initialDateText: String,
    onDateSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val monthNames = listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
    
    val parts = initialDateText.split(".")
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

    val maxDays = getDaysInMonth(currentMonth, currentYear)
    if (selectedDay > maxDays) {
        selectedDay = maxDays
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TourOSButton(
                text = "Tarihi Seç",
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
                        text = "📅 ${monthNames[currentMonth - 1]} $currentYear",
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(maxDays) { index ->
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
