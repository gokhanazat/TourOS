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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.mgacreative.touros.data.database.entity.AgencyStorefrontTourItem
import com.mgacreative.touros.ui.components.TourOSButton
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
 * 1. Üst Arama Çubuğu + Tur Operatörü Filtresi
 * 2. Blok 1: 🔥 Turlar (Manuel Acente Turları Carousel)
 * 3. Blok 2: 🏨 Oteller (Acente Anlaşmalı Oteller Carousel)
 * 4. Blok 3: ⚡ Tur Operatörü Ürünleri (Entegre Ürün Kataloğu)
 * 5. Blok 4: 📞 Rezervasyon & İletişim (Talep Formu)
 */
@Composable
fun AgencyStorefrontScreen(
    onNavigateToTourDetail: (String) -> Unit = {},
    viewModel: AgencyStorefrontViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var departureCityInput by remember { mutableStateOf("İstanbul") }
    var destinationInput by remember { mutableStateOf("") }
    var maxBudgetInput by remember { mutableStateOf("") }
    var selectedOperatorFilter by remember { mutableStateOf("Tüm Operatörler") }

    // Carousel List State'leri
    val tourCarouselState = rememberLazyListState()
    val hotelCarouselState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ── 1. SLETAT TOP BRAND HEADER BAR ──────────────────────────────────────
                    item {
                        Card(
                            shape = RoundedCornerShape(0.dp),
                            colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                                            .background(TourOSColors.Primary, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "SLETAT",
                                            style = TourOSTypography.TitleMedium,
                                            color = TourOSColors.OnPrimary,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                    Text(
                                        text = "| ${state.branding.heroTitle}",
                                        style = TourOSTypography.BodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.TextPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "📞 0850 300 00 00",
                                        style = TourOSTypography.BodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.Primary
                                    )
                                    Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, TourOSColors.Border, RoundedCornerShape(20.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "🔒 Güvenli Ödeme", style = TourOSTypography.Label, color = TourOSColors.Success)
                                    }
                                }
                            }
                        }
                    }

                    // ── 2. HERO SEARCH MODULE WITH OPERATOR FILTER ──────────────────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TourOSColors.Primary)
                                .padding(vertical = TourOSSpacing.xLarge, horizontal = TourOSSpacing.large)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "80+ Tur Operatöründen En Uygun Fiyatları Karşılaştırın",
                                    style = TourOSTypography.TitleLarge,
                                    color = TourOSColors.OnPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                Text(
                                    text = "Tüm tatiliniz tek bir yerde. Anında rezervasyon ve 7/24 canlı destek.",
                                    style = TourOSTypography.BodyMedium,
                                    color = TourOSColors.OnPrimary.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                // Arama Kartı (Kalkış, Varış, Bütçe ve Tur Operatörü Filtresi)
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = departureCityInput,
                                                    onValueChange = { departureCityInput = it },
                                                    label = "Kalkış Şehri (Nereden)"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1.2f)) {
                                                TourOSTextField(
                                                    value = destinationInput,
                                                    onValueChange = { destinationInput = it },
                                                    label = "Nereye (Ülke / Bölge / Otel)"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1.3f)) {
                                                TourOSDropdown(
                                                    items = operatorOptions,
                                                    selectedItem = selectedOperatorFilter,
                                                    onItemSelected = { selectedOperatorFilter = it },
                                                    itemLabel = { it },
                                                    label = "Tur Operatörü Filtresi"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(0.9f)) {
                                                TourOSTextField(
                                                    value = maxBudgetInput,
                                                    onValueChange = { maxBudgetInput = it },
                                                    label = "Maks. Bütçe (₺)"
                                                )
                                            }
                                            TourOSButton(
                                                text = "TURLARI ARA 🔍",
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
                    }

                    // ── 3. OPERATOR TRUST LOGOS STRIP ─────────────────────────────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TourOSColors.Surface)
                                .padding(vertical = TourOSSpacing.medium, horizontal = TourOSSpacing.large)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Anlaşmalı Güvenilir Tur Operatörleri:",
                                    style = TourOSTypography.Label,
                                    color = TourOSColors.TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                                    operatorOptions.drop(1).forEach { opName ->
                                        val isSelected = selectedOperatorFilter == opName
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Surface)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    selectedOperatorFilter = if (isSelected) "Tüm Operatörler" else opName
                                                }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = opName,
                                                style = TourOSTypography.Label,
                                                fontSize = 11.sp,
                                                color = if (isSelected) TourOSColors.Primary else TourOSColors.TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── 4. BLOK 1: TURLAR (MANUEL ACENTE TURLARI CAROUSEL) ─────────────────
                    item {
                        Column(modifier = Modifier.padding(top = TourOSSpacing.large, bottom = TourOSSpacing.small)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = TourOSSpacing.large),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "🔥 Turlar",
                                            style = TourOSTypography.TitleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = TourOSColors.TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                        Box(
                                            modifier = Modifier
                                                .background(TourOSColors.Error.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "Acente Özel Paketleri", style = TourOSTypography.Label, color = TourOSColors.Error, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "Acente tarafından özel olarak hazırlanan ve en çok tercih edilen tur paketleri.",
                                        style = TourOSTypography.BodyMedium,
                                        color = TourOSColors.TextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    // Sol Ok
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val target = (tourCarouselState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                                tourCarouselState.animateScrollToItem(target)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, TourOSColors.Border, CircleShape)
                                    ) {
                                        Text("←", style = TourOSTypography.TitleMedium)
                                    }
                                    // Sağ Ok
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val target = tourCarouselState.firstVisibleItemIndex + 1
                                                tourCarouselState.animateScrollToItem(target)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(TourOSColors.Primary)
                                    ) {
                                        Text("→", style = TourOSTypography.TitleMedium, color = TourOSColors.OnPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                            // Yatay Kaydırılabilir Tur Kartları (Carousel)
                            val manualTours = state.tours.filter {
                                selectedOperatorFilter == "Tüm Operatörler" || it.operatorName.contains(selectedOperatorFilter, ignoreCase = true)
                            }
                            LazyRow(
                                state = tourCarouselState,
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                modifier = Modifier.padding(horizontal = TourOSSpacing.large)
                            ) {
                                items(manualTours.ifEmpty { state.tours }) { tourItem ->
                                    SletatCarouselTourCard(
                                        item = tourItem,
                                        onClickDetail = { onNavigateToTourDetail(tourItem.tourId) }
                                    )
                                }
                            }
                        }
                    }

                    // ── 5. BLOK 2: OTELLER (ACENTE ANLAŞMALI OTELLER CAROUSEL) ─────────────
                    item {
                        Column(modifier = Modifier.padding(top = TourOSSpacing.large, bottom = TourOSSpacing.small)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = TourOSSpacing.large),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "🏨 Anlaşmalı Oteller",
                                            style = TourOSTypography.TitleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = TourOSColors.TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                        Box(
                                            modifier = Modifier
                                                .background(TourOSColors.Secondary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "Lüks Konaklama Tesisleri", style = TourOSTypography.Label, color = TourOSColors.Secondary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "Antalya, Bodrum, Mısır ve Dubai bölgesindeki popüler 5 yıldızlı tesisler.",
                                        style = TourOSTypography.BodyMedium,
                                        color = TourOSColors.TextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val target = (hotelCarouselState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                                hotelCarouselState.animateScrollToItem(target)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, TourOSColors.Border, CircleShape)
                                    ) {
                                        Text("←", style = TourOSTypography.TitleMedium)
                                    }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val target = hotelCarouselState.firstVisibleItemIndex + 1
                                                hotelCarouselState.animateScrollToItem(target)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(TourOSColors.Primary)
                                    ) {
                                        Text("→", style = TourOSTypography.TitleMedium, color = TourOSColors.OnPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                            // Yatay Kaydırılabilir Otel Kartları Carousel
                            val sampleHotels = listOf(
                                StorefrontHotelCardItem("h-1", "Crystal De Luxe Resort & Spa", "Kemer, Antalya", "9.2", "Ultra Her Şey Dahil", "2.450 ₺ / Gece"),
                                StorefrontHotelCardItem("h-2", "Sunrise Holidays Resort (16+)", "Hurghada, Mısır", "9.0", "Her Şey Dahil", "1.890 ₺ / Gece"),
                                StorefrontHotelCardItem("h-3", "Kaya Palazzo Golf Resort", "Belek, Antalya", "9.5", "Ultra Her Şey Dahil", "4.200 ₺ / Gece"),
                                StorefrontHotelCardItem("h-4", "Voyage Belek Golf & Spa", "Belek, Antalya", "9.4", "Her Şey Dahil", "3.850 ₺ / Gece"),
                                StorefrontHotelCardItem("h-5", "Rixos Premium Dubai", "JBR, Dubai", "9.6", "Oda Kahvaltı", "6.500 ₺ / Gece")
                            )

                            LazyRow(
                                state = hotelCarouselState,
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                modifier = Modifier.padding(horizontal = TourOSSpacing.large)
                            ) {
                                items(sampleHotels) { hotel ->
                                    SletatCarouselHotelCard(hotel = hotel)
                                }
                            }
                        }
                    }

                    // ── 6. BLOK 3: TUR OPERATÖRÜ ÜRÜNLERİ (ENTEGRE KATALOG) ─────────────────
                    item {
                        Column(modifier = Modifier.padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.large)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "⚡ Tur Operatörleri Ürün Kataloğu",
                                        style = TourOSTypography.TitleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.TextPrimary
                                    )
                                    Text(
                                        text = "Coral, Anex, Pegas ve entegre operatörlerin yayınlanmış canlı tur satışları.",
                                        style = TourOSTypography.BodyMedium,
                                        color = TourOSColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    val filteredOperatorTours = state.tours.filter {
                        selectedOperatorFilter == "Tüm Operatörler" || it.operatorName.contains(selectedOperatorFilter, ignoreCase = true)
                    }

                    items(filteredOperatorTours) { tourItem ->
                        Box(modifier = Modifier.padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.xSmall)) {
                            SletatTourCard(
                                item = tourItem,
                                onClickDetail = { onNavigateToTourDetail(tourItem.tourId) }
                            )
                        }
                    }

                    // ── 7. BLOK 4: REZERVASYON VE İLETİŞİM (FORM & İLETİŞİM BİLGİLERİ) ───────
                    item {
                        Column(modifier = Modifier.padding(TourOSSpacing.large)) {
                            Text(
                                text = "📞 Hızlı Rezervasyon Talep & Danışman İletişimi",
                                style = TourOSTypography.TitleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TourOSColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                            Text(
                                text = "Hızlı rezervasyon talebinizi iletin, acente müşteri temsilcimiz size anında ulaşsın.",
                                style = TourOSTypography.BodyMedium,
                                color = TourOSColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(TourOSSpacing.large))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                            ) {
                                // Sol Taraf: Rezervasyon Formu
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Column(modifier = Modifier.padding(TourOSSpacing.large)) {
                                        Text(text = "Rezervasyon Talep Formu", style = TourOSTypography.TitleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        TourOSTextField(
                                            value = contactName,
                                            onValueChange = { contactName = it },
                                            label = "Adınız Soyadınız",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                                        TourOSTextField(
                                            value = contactPhone,
                                            onValueChange = { contactPhone = it },
                                            label = "Telefon Numaranız",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                                        TourOSTextField(
                                            value = contactNotes,
                                            onValueChange = { contactNotes = it },
                                            label = "Tur / Otel Tercihi ve Kalkış Tarihi Notu",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        if (reservationSuccessMsg != null) {
                                            Text(
                                                text = reservationSuccessMsg!!,
                                                style = TourOSTypography.BodyMedium,
                                                color = TourOSColors.Success,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                        }

                                        TourOSButton(
                                            text = "REZERVASYON TALEBİ GÖNDER ➔",
                                            onClick = {
                                                if (contactName.isNotBlank() && contactPhone.isNotBlank()) {
                                                    reservationSuccessMsg = "✓ Talebiniz başarıyla alındı! Danışmanımız sizinle iletişime geçecektir."
                                                } else {
                                                    reservationSuccessMsg = "Lütfen Ad Soyad ve Telefon alanlarını doldurun."
                                                }
                                            }
                                        )
                                    }
                                }

                                // Sağ Taraf: Acente Danışman Bilgi Kartı
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.3f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Primary.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(TourOSSpacing.large)) {
                                        Text(text = "🏢 Acente İletişim Bilgileri", style = TourOSTypography.TitleMedium, color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        Text(text = "📍 Adres: Merkez Mah. Atatürk Cad. No: 42, Şişli / İstanbul", style = TourOSTypography.BodyMedium)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                        Text(text = "📞 Destek Hattı: 0850 300 00 00", style = TourOSTypography.BodyMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                        Text(text = "✉️ E-posta: destek@touros.com", style = TourOSTypography.BodyMedium)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                        Text(text = "🕒 Çalışma Saatleri: Pazartesi - Cumartesi 09:00 - 20:00", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
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
}

/**
 * Sletat.ru 2. Resimdeki Gibi Yatay Carousel Tur Kartı
 */
@Composable
private fun SletatCarouselTourCard(
    item: AgencyStorefrontTourItem,
    onClickDetail: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .width(280.dp)
            .clickable { onClickDetail() }
    ) {
        Column {
            // Görsel Görünüm Alanı (Gradient / Badge)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(TourOSColors.PrimaryContainer),
                contentAlignment = Alignment.TopStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .background(TourOSColors.Error, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "%15 İNDİRİM", style = TourOSTypography.Label, color = TourOSColors.OnError, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .background(TourOSColors.Success, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "⭐ 9.2", style = TourOSTypography.Label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
                Text(
                    text = item.title,
                    style = TourOSTypography.TitleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    color = TourOSColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 ${item.city}, ${item.country} • ${item.nights} Gece",
                    style = TourOSTypography.Caption,
                    color = TourOSColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${(item.finalPrice * 1.15).toInt()} ₺",
                            style = TourOSTypography.Caption,
                            color = TourOSColors.TextSecondary,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = "${item.finalPrice} ₺",
                            style = TourOSTypography.TitleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TourOSColors.Primary
                        )
                    }
                    TourOSButton(text = "İncele ➔", onClick = onClickDetail)
                }
            }
        }
    }
}

/**
 * Sletat.ru Tipi Yatay Carousel Otel Kartı
 */
@Composable
private fun SletatCarouselHotelCard(hotel: StorefrontHotelCardItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.width(260.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(TourOSColors.SecondaryContainer),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(TourOSColors.Primary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "⭐ ${hotel.rating}", style = TourOSTypography.Label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "📍 ${hotel.location} • ${hotel.boardType}",
                    style = TourOSTypography.Caption,
                    color = TourOSColors.TextSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.small))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = hotel.pricePerNight,
                        style = TourOSTypography.TitleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.Primary
                    )
                    TourOSButton(text = "Detay", onClick = {})
                }
            }
        }
    }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = TourOSTypography.TitleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    Box(
                        modifier = Modifier
                            .background(TourOSColors.Error.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "🔥 Hot Deal %15", style = TourOSTypography.Label, color = TourOSColors.Error, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .background(TourOSColors.Secondary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "⚡ ${item.comparedOperatorCount} Operatör Karşılaştırıldı", style = TourOSTypography.Label, color = TourOSColors.Secondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(TourOSSpacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "✈️ Kalkış: İstanbul • Konum: ${item.city}, ${item.country} • ${item.nights} Gece",
                        style = TourOSTypography.BodyMedium,
                        color = TourOSColors.TextSecondary
                    )
                    Text(
                        text = "Sağlayıcı Operatör: ${item.operatorName} • Her Şey Dahil",
                        style = TourOSTypography.Label,
                        color = TourOSColors.TextSecondary.copy(alpha = 0.7f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${(item.finalPrice * 1.15).toInt()} ₺",
                        style = TourOSTypography.Label,
                        color = TourOSColors.TextSecondary,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Text(
                        text = "${item.finalPrice} ₺",
                        style = TourOSTypography.TitleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.Primary
                    )
                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                    TourOSButton(
                        text = "TURU İNCELE ➔",
                        onClick = onClickDetail
                    )
                }
            }
        }
    }
}
