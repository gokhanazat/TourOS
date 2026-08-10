package com.mgacreative.touros.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.mgacreative.touros.data.database.entity.AgencyStorefrontTourItem
import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSDropdown
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AgencyStorefrontUiState
import com.mgacreative.touros.ui.viewmodel.AgencyStorefrontViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class StorefrontHotelCardItem(
    val id: String,
    val name: String,
    val location: String,
    val rating: String,
    val boardType: String,
    val pricePerNight: String,
    val imageUrl: String = ""
)

/**
 * Acente Storefront (Sletat.ru Konseptli Kesintisiz Carousel Tasarımı).
 * 1. Üst Header Banner (Görsel Değişebilir Banner)
 * 2. Header ALTINA Alınmış Bağımsız Arama Bloğu + Tur Operatörü Filtresi
 * 3. Blok 1: 🔥 Turlar (Carousel + Tümünü Göster İşlevi)
 * 4. Blok 2: 🏨 Oteller (Carousel + Tümünü Göster İşlevi)
 * 5. Blok 3: ⚡ Tur Operatörü Ürünleri (Carousel + Kart Bloğu + Tümünü Göster İşlevi)
 * 6. Blok 4: 📞 Rezervasyon & İletişim Formu
 */
@Composable
fun AgencyStorefrontScreen(
    onNavigateToTourDetail: (String) -> Unit = {},
    onNavigateToHotelDetail: (String) -> Unit = {},
    viewModel: AgencyStorefrontViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()

    var departureCityInput by remember { mutableStateOf("İstanbul") }
    var destinationInput by remember { mutableStateOf("") }
    var maxBudgetInput by remember { mutableStateOf("") }
    var selectedOperatorFilter by remember { mutableStateOf("Tüm Operatörler") }

    // Carousel List State'leri
    val tourCarouselState = rememberLazyListState()
    val hotelCarouselState = rememberLazyListState()
    val operatorProductCarouselState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Tümünü Göster Tam Liste Görünüm State'leri
    var showAllToursView by remember { mutableStateOf(false) }
    var showAllHotelsView by remember { mutableStateOf(false) }
    var showAllOperatorProductsView by remember { mutableStateOf(false) }
    var selectedHotelDetail by remember { mutableStateOf<Hotel?>(null) }

    // Rezervasyon & İletişim Form State'leri
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactNotes by remember { mutableStateOf("") }
    var reservationSuccessMsg by remember { mutableStateOf<String?>(null) }

    val operatorOptions = listOf(
        "Tüm Operatörler",
        "Coral Travel",
        "Anex Tour",
        "Pegas Touristik",
        "Fun & Sun",
        "Sunmar",
        "Ankara Turizm A.Ş."
    )

    val sampleHotels = listOf(
        StorefrontHotelCardItem("h-1", "Crystal De Luxe Resort & Spa", "Kemer, Antalya", "9.2", "Ultra Her Şey Dahil", "2.450 ₺ / Gece"),
        StorefrontHotelCardItem("h-2", "Sunrise Holidays Resort (16+)", "Hurghada, Mısır", "9.0", "Her Şey Dahil", "1.890 ₺ / Gece"),
        StorefrontHotelCardItem("h-3", "Kaya Palazzo Golf Resort", "Belek, Antalya", "9.5", "Ultra Her Şey Dahil", "4.200 ₺ / Gece"),
        StorefrontHotelCardItem("h-4", "Voyage Belek Golf & Spa", "Belek, Antalya", "9.4", "Her Şey Dahil", "3.850 ₺ / Gece"),
        StorefrontHotelCardItem("h-5", "Rixos Premium Dubai", "JBR, Dubai", "9.6", "Oda Kahvaltı", "6.500 ₺ / Gece")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TourOSColors.Background)
    ) {
        when (val state = uiState) {
            is AgencyStorefrontUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is AgencyStorefrontUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = TourOSColors.Error)
                }
            }
            is AgencyStorefrontUiState.Success -> {
                val filteredOperatorTours = state.tours.filter {
                    selectedOperatorFilter == "Tüm Operatörler" || it.operatorName.contains(selectedOperatorFilter, ignoreCase = true)
                }

                // ── TÜMÜNÜ GÖSTER TAM İZGARA GÖRÜNÜMLERİ ──────────────────────────────────
                if (showAllToursView) {
                    Column(modifier = Modifier.fillMaxSize().padding(TourOSSpacing.large)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔥 Tüm Manuel Turlar (Acente Paketleri)", style = TourOSTypography.TitleLarge, fontWeight = FontWeight.Bold)
                            TourOSButton(text = "← GERİ DÖN", onClick = { showAllToursView = false })
                        }
                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                            items(filteredOperatorTours.ifEmpty { state.tours }) { t ->
                                SletatTourCard(item = t, onClickDetail = { onNavigateToTourDetail(t.tourId) })
                            }
                        }
                    }
                    return@Column
                }

                if (showAllHotelsView) {
                    Column(modifier = Modifier.fillMaxSize().padding(TourOSSpacing.large)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🏨 Tüm Anlaşmalı Oteller", style = TourOSTypography.TitleLarge, fontWeight = FontWeight.Bold)
                            TourOSButton(text = "← GERİ DÖN", onClick = { showAllHotelsView = false })
                        }
                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                            items(state.hotels) { h ->
                                RegisteredHotelCard(hotel = h, onClickInspect = { onNavigateToHotelDetail(h.id) })
                            }
                        }
                    }
                    return@Column
                }

                if (showAllOperatorProductsView) {
                    Column(modifier = Modifier.fillMaxSize().padding(TourOSSpacing.large)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⚡ Tüm Tur Operatörü Ürünleri ($selectedOperatorFilter)", style = TourOSTypography.TitleLarge, fontWeight = FontWeight.Bold)
                            TourOSButton(text = "← GERİ DÖN", onClick = { showAllOperatorProductsView = false })
                        }
                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                            items(filteredOperatorTours) { t ->
                                SletatTourCard(item = t, onClickDetail = { onNavigateToTourDetail(t.tourId) })
                            }
                        }
                    }
                    return@Column
                }

                // ── NORMAL KESİNTİSİZ LANDING PAGE SAYFA AKIŞI ────────────────────────────
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // ── 1. CLEAN HEADER BANNER ALANI (GÖRSEL BANNER, ÖZEL LOGO SOL ÜSTTE) ─
                    item {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(TourOSColors.Primary),
        contentAlignment = Alignment.TopStart
    ) {
        val headerImg = state.branding.headerImageUrl
        if (!headerImg.isNullOrBlank()) {
            val imgModel = remember(headerImg) {
                val trimmed = headerImg.trim()
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://")) {
                    trimmed
                } else {
                    "file://$trimmed"
                }
            }
            AsyncImage(
                model = imgModel,
                contentDescription = "Header Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Sol Üst Özel Logo Rozeti ve İletişim Rozeti
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    val customLogo = state.branding.customLogoUrl
                    if (!customLogo.isNullOrBlank()) {
                        val logoModel = remember(customLogo) {
                            val trimmed = customLogo.trim()
                            if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://")) {
                                trimmed
                            } else {
                                "file://$trimmed"
                            }
                        }
                        AsyncImage(
                            model = logoModel,
                            contentDescription = "Acente Logosu",
                            modifier = Modifier.height(36.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = state.branding.heroTitle.ifBlank { "🏢 ACENTE LOGO" },
                            style = TourOSTypography.TitleMedium,
                            color = TourOSColors.Primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "📞 ${state.branding.contactPhone?.takeIf { it.isNotBlank() } ?: "0850 300 00 00"}",
                    style = TourOSTypography.BodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

                    // ── 2. HEADER ALTINA ALINMIŞ ARAMA BLOĞU + OPERATÖR FİLTRESİ ───────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = TourOSSpacing.large)
                                .padding(top = 16.dp, bottom = TourOSSpacing.medium)
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
                                    Text(
                                        text = "🔍 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tur ve Otel Arama Motoru"),
                                        style = TourOSTypography.TitleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            TourOSTextField(
                                                value = departureCityInput,
                                                onValueChange = { departureCityInput = it },
                                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kalkış Şehri (Nereden)")
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1.2f)) {
                                            TourOSTextField(
                                                value = destinationInput,
                                                onValueChange = { destinationInput = it },
                                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Nereye (Ülke / Bölge / Otel)")
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1.3f)) {
                                            TourOSDropdown(
                                                items = operatorOptions,
                                                selectedItem = selectedOperatorFilter,
                                                onItemSelected = { selectedOperatorFilter = it },
                                                itemLabel = { com.mgacreative.touros.ui.localization.AppLanguageManager.translate(it) },
                                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tur Operatörü Filtresi")
                                            )
                                        }
                                        Box(modifier = Modifier.weight(0.9f)) {
                                            TourOSTextField(
                                                value = maxBudgetInput,
                                                onValueChange = { maxBudgetInput = it },
                                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Maks. Bütçe (₺)")
                                            )
                                        }
                                        TourOSButton(
                                            text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("TURLARI ARA 🔍"),
                                            onClick = {
                                                val b = maxBudgetInput.toDoubleOrNull() ?: 100000.0
                                                viewModel.loadStorefront(countryFilter = destinationInput, maxBudget = b)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── 3. OPERATÖR SEÇİM ÇİPLERİ STRİP ──────────────────────────────────
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.xSmall),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hızlı Operatör Filtresi:"), style = TourOSTypography.Label, fontWeight = FontWeight.Bold, color = TourOSColors.TextSecondary)
                            operatorOptions.drop(1).forEach { opName ->
                                val isSelected = selectedOperatorFilter == opName
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Surface)
                                        .border(1.dp, if (isSelected) TourOSColors.Primary else TourOSColors.Border, RoundedCornerShape(6.dp))
                                        .clickable { selectedOperatorFilter = if (isSelected) "Tüm Operatörler" else opName }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = opName, style = TourOSTypography.Label, fontSize = 11.sp, color = if (isSelected) TourOSColors.Primary else TourOSColors.TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // ── 4. BLOK 1: TURLAR (MANUEL ACENTE TURLARI CAROUSEL) ─────────────────
                    item {
                        Column(modifier = Modifier.padding(top = TourOSSpacing.large, bottom = TourOSSpacing.small)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = TourOSSpacing.large),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "🔥 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Turlar"), style = TourOSTypography.TitleLarge, fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                                    Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Acente tarafından özel oluşturulan manuel tur paketleri."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val target = (tourCarouselState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                                tourCarouselState.animateScrollToItem(target)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp).clip(CircleShape).border(1.dp, TourOSColors.Border, CircleShape)
                                    ) { Text("←") }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val target = tourCarouselState.firstVisibleItemIndex + 1
                                                tourCarouselState.animateScrollToItem(target)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(TourOSColors.Primary)
                                    ) { Text("→", color = TourOSColors.OnPrimary) }

                                    Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                    TourOSButton(
                                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tümünü Göster ➔"),
                                        onClick = { showAllToursView = true }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                            LazyRow(
                                state = tourCarouselState,
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                modifier = Modifier.padding(horizontal = TourOSSpacing.large)
                            ) {
                                items(filteredOperatorTours.ifEmpty { state.tours }) { tourItem ->
                                    SletatCarouselTourCard(item = tourItem, onClickDetail = { onNavigateToTourDetail(tourItem.tourId) })
                                }
                            }
                        }
                    }

                    // ── 5. BLOK 2: OTELLER (ACENTE ANLAŞMALI OTELLER CAROUSEL) ─────────────
                    item {
                        Column(modifier = Modifier.padding(top = TourOSSpacing.large, bottom = TourOSSpacing.small)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = TourOSSpacing.large),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "🏨 Anlaşmalı Oteller", style = TourOSTypography.TitleLarge, fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                                    Text(text = "Antalya, Bodrum, Mısır ve Dubai popüler 5 yıldızlı tesisleri.", style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val target = (hotelCarouselState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                                hotelCarouselState.animateScrollToItem(target)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp).clip(CircleShape).border(1.dp, TourOSColors.Border, CircleShape)
                                    ) { Text("←") }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val target = hotelCarouselState.firstVisibleItemIndex + 1
                                                hotelCarouselState.animateScrollToItem(target)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(TourOSColors.Primary)
                                    ) { Text("→", color = TourOSColors.OnPrimary) }

                                    Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                    TourOSButton(
                                        text = "Tüm Otelleri Göster ➔",
                                        onClick = { showAllHotelsView = true }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                            LazyRow(
                                state = hotelCarouselState,
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                modifier = Modifier.padding(horizontal = TourOSSpacing.large)
                            ) {
                                items(state.hotels) { hotel ->
                                    RegisteredHotelCard(hotel = hotel, onClickInspect = { onNavigateToHotelDetail(hotel.id) })
                                }
                            }
                        }
                    }

                    // ── 6. BLOK 3: TUR OPERATÖRÜ ÜRÜNLERİ (KART BLOĞU + CAROUSEL) ───────────
                    item {
                        Column(modifier = Modifier.padding(top = TourOSSpacing.large, bottom = TourOSSpacing.small)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = TourOSSpacing.large),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "⚡ Tur Operatörleri Ürün Kataloğu", style = TourOSTypography.TitleLarge, fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                                    Text(text = "Coral, Anex, Pegas canlı operatör tur kartları teklifleri.", style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val target = (operatorProductCarouselState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                                operatorProductCarouselState.animateScrollToItem(target)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp).clip(CircleShape).border(1.dp, TourOSColors.Border, CircleShape)
                                    ) { Text("←") }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val target = operatorProductCarouselState.firstVisibleItemIndex + 1
                                                operatorProductCarouselState.animateScrollToItem(target)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(TourOSColors.Primary)
                                    ) { Text("→", color = TourOSColors.OnPrimary) }

                                    Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                    TourOSButton(
                                        text = "Tüm Ürünleri Göster ➔",
                                        onClick = { showAllOperatorProductsView = true }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                            LazyRow(
                                state = operatorProductCarouselState,
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                modifier = Modifier.padding(horizontal = TourOSSpacing.large)
                            ) {
                                items(filteredOperatorTours) { tourItem ->
                                    SletatCarouselTourCard(item = tourItem, onClickDetail = { onNavigateToTourDetail(tourItem.tourId) })
                                }
                            }
                        }
                    }

                    // ── 7. BLOK 4: REZERVASYON VE İLETİŞİM (FORM & İLETİŞİM BİLGİLERİ) ───────
                    item {
                        Column(modifier = Modifier.padding(TourOSSpacing.large)) {
                            Text(text = "📞 Hızlı Rezervasyon Talep & Danışman İletişimi", style = TourOSTypography.TitleLarge, fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                            Text(text = "Hızlı rezervasyon talebinizi iletin, müşteri temsilcimiz size anında ulaşsın.", style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                            Spacer(modifier = Modifier.height(TourOSSpacing.large))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Column(modifier = Modifier.padding(TourOSSpacing.large)) {
                                        Text(text = "Rezervasyon Talep Formu", style = TourOSTypography.TitleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        TourOSTextField(value = contactName, onValueChange = { contactName = it }, label = "Adınız Soyadınız", modifier = Modifier.fillMaxWidth())
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                                        TourOSTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = "Telefon Numaranız", modifier = Modifier.fillMaxWidth())
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                                        TourOSTextField(value = contactNotes, onValueChange = { contactNotes = it }, label = "Tur / Otel Tercihi ve Notlar", modifier = Modifier.fillMaxWidth())
                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        if (reservationSuccessMsg != null) {
                                            Text(text = reservationSuccessMsg!!, style = TourOSTypography.BodyMedium, color = TourOSColors.Success, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                        }

                                        TourOSButton(
                                             text = "REZERVASYON TALEBİ GÖNDER ➔",
                                             onClick = {
                                                 if (contactName.isNotBlank() && contactPhone.isNotBlank()) {
                                                     val targetMail = state.branding.contactEmail?.takeIf { it.isNotBlank() } ?: "destek@touros.com"
                                                     reservationSuccessMsg = "✓ Talebiniz alındı! Rezervasyon detayları $targetMail adresine ve temsilcinize iletildi."
                                                 } else {
                                                     reservationSuccessMsg = "Lütfen Ad Soyad ve Telefon alanlarını doldurun."
                                                 }
                                             }
                                         )
                                    }
                                }

                                Card(
                                     shape = RoundedCornerShape(12.dp),
                                     colors = CardDefaults.cardColors(containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.3f)),
                                     border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Primary.copy(alpha = 0.3f)),
                                     modifier = Modifier.weight(1f)
                                 ) {
                                     Column(modifier = Modifier.padding(TourOSSpacing.large)) {
                                         Text(text = "🏢 Acente İletişim Bilgileri", style = TourOSTypography.TitleMedium, color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                                         Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                                         Text(text = "📍 Adres: ${state.branding.contactAddress?.takeIf { it.isNotBlank() } ?: "Şişli / İstanbul"}", style = TourOSTypography.BodyMedium)
                                         Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                         Text(text = "📞 Destek Hattı: ${state.branding.contactPhone?.takeIf { it.isNotBlank() } ?: "0850 300 00 00"}", style = TourOSTypography.BodyMedium, fontWeight = FontWeight.Bold)
                                         Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                         Text(text = "✉️ E-posta: ${state.branding.contactEmail?.takeIf { it.isNotBlank() } ?: "destek@touros.com"}", style = TourOSTypography.BodyMedium)

                                         val whatsappNum = state.branding.whatsappNumber?.takeIf { it.isNotBlank() }
                                         if (whatsappNum != null) {
                                             Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                                             Box(
                                                 modifier = Modifier
                                                     .fillMaxWidth()
                                                     .clip(RoundedCornerShape(8.dp))
                                                     .background(Color(0xFF25D366))
                                                     .clickable { /* WhatsApp Action */ }
                                                     .padding(vertical = 10.dp, horizontal = 14.dp),
                                                 contentAlignment = Alignment.Center
                                             ) {
                                                 Text(
                                                     text = "💬 WhatsApp ile İletişime Geç ($whatsappNum)",
                                                     style = TourOSTypography.Label,
                                                     color = Color.White,
                                                     fontWeight = FontWeight.Bold
                                                 )
                                             }
                                         }
                                     }
                                 }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(TourOSSpacing.xxLarge)) }
                }
            }
        }
    }

    selectedHotelDetail?.let { hotel ->
        HotelDetailDialog(
            hotel = hotel,
            onDismiss = { selectedHotelDetail = null }
        )
    }
}

@Composable
private fun SletatCarouselTourCard(
    item: AgencyStorefrontTourItem,
    onClickDetail: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.width(280.dp).clickable { onClickDetail() }
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp).background(TourOSColors.PrimaryContainer),
                contentAlignment = Alignment.TopStart
            ) {
                if (!item.coverImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.coverImageUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (item.operatorName.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(TourOSColors.Primary.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.operatorName,
                            style = TourOSTypography.Label,
                            color = TourOSColors.OnPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
                Text(text = item.title, style = TourOSTypography.TitleMedium, fontWeight = FontWeight.Bold, maxLines = 2, color = TourOSColors.TextPrimary)
                Text(text = "📍 ${item.city}, ${item.country} • ${item.nights} Gece", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "${item.finalPrice} ₺", style = TourOSTypography.TitleMedium, fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                    }
                    TourOSButton(text = "İncele ➔", onClick = onClickDetail)
                }
            }
        }
    }
}

@Composable
private fun RegisteredHotelCard(
    hotel: Hotel,
    onClickInspect: () -> Unit
) {
    val starsText = "⭐".repeat((hotel.starRating ?: 4).coerceIn(1, 5))
    val cover = hotel.coverImageUrl?.takeIf { it.isNotBlank() }
        ?: "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&auto=format&fit=crop"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.width(280.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(TourOSColors.SecondaryContainer)
            ) {
                AsyncImage(
                    model = cover,
                    contentDescription = hotel.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(TourOSColors.Primary.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = starsText,
                        style = TourOSTypography.Label,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
                Text(
                    text = hotel.name,
                    style = TourOSTypography.TitleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = TourOSColors.TextPrimary
                )
                Text(
                    text = "📍 ${hotel.city ?: "Türkiye"}, ${hotel.country}",
                    style = TourOSTypography.Caption,
                    color = TourOSColors.TextSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                Text(
                    text = hotel.description?.takeIf { it.isNotBlank() } ?: "Lüks konaklama ve özel acente hizmetleri sunan tesis.",
                    style = TourOSTypography.BodyMedium,
                    fontSize = 12.sp,
                    color = TourOSColors.TextSecondary,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TourOSButton(text = "İncele ➔", onClick = onClickInspect)
                }
            }
        }
    }
}

@Composable
private fun HotelDetailDialog(
    hotel: Hotel,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = hotel.name,
                    style = TourOSTypography.TitleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.TextPrimary
                )
                Text(
                    text = "⭐".repeat((hotel.starRating ?: 4).coerceIn(1, 5)) + " • 📍 ${hotel.city ?: "Türkiye"}, ${hotel.country}",
                    style = TourOSTypography.BodyMedium,
                    color = TourOSColors.Primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                val cover = hotel.coverImageUrl?.takeIf { it.isNotBlank() }
                    ?: "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&auto=format&fit=crop"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = cover,
                        contentDescription = hotel.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "🏨 Otel Tanıtımı & Açıklama",
                    style = TourOSTypography.TitleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.Primary
                )
                Text(
                    text = hotel.description?.takeIf { it.isNotBlank() } ?: "Acentemiz tarafından özel olarak anlaşılan ve misafirlerimize sunulan tesis.",
                    style = TourOSTypography.BodyMedium,
                    color = TourOSColors.TextSecondary
                )
            }
        },
        confirmButton = {
            TourOSButton(
                text = "Formu Kapat",
                onClick = onDismiss,
                variant = TourOSButtonVariant.SECONDARY
            )
        }
    )
}

@Composable
private fun SletatTourCard(
    item: AgencyStorefrontTourItem,
    onClickDetail: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.title, style = TourOSTypography.TitleMedium, fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                Text(text = "⚡ ${item.operatorName}", style = TourOSTypography.Label, color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(TourOSSpacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "✈️ ${item.city}, ${item.country} • ${item.nights} Gece", style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                Text(text = "${item.finalPrice} ₺", style = TourOSTypography.TitleLarge, fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
            }
        }
    }
}
