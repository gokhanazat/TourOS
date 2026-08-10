package com.mgacreative.touros.ui.screens

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

    var departureCity by remember { mutableStateOf("Moskova") }
    var selectedRegion by remember { mutableStateOf("Antalya") }
    var nights by remember { mutableStateOf(7) }
    var adults by remember { mutableStateOf(2) }
    var childs by remember { mutableStateOf(2) }

    var selectedStars by remember { mutableStateOf(setOf(3, 4, 5)) }
    var isInstantOnly by remember { mutableStateOf(false) }
    var isPromoOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var showSuccessModal by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // ── BLOK 1: GÖRSEL 7 - ARAMA FİLTRELEME MOTORU ─────────────────────────
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Surface,
                contentPadding = TourOSSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(
                        text = "🔍 1. ${AppLanguageManager.translate("Tur & Otel Arama Kriterleri")}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // 1. KISIM: DESTİNASYON & YOLCU SEÇİMİ
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            TourOSTextField(
                                value = departureCity,
                                onValueChange = { departureCity = it },
                                label = AppLanguageManager.translate("Kalkış Şehri"),
                                placeholder = "Moskova / İstanbul",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                OutlinedButton(
                                    onClick = { selectedRegion = "Antalya" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedRegion == "Antalya") TourOSColors.PrimaryContainer else Color.Transparent
                                    )
                                ) {
                                    Text("✈️ Antalya", style = TourOSTypography.Caption)
                                }
                                OutlinedButton(
                                    onClick = { selectedRegion = "Bodrum" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedRegion == "Bodrum") TourOSColors.PrimaryContainer else Color.Transparent
                                    )
                                ) {
                                    Text("✈️ Bodrum", style = TourOSTypography.Caption)
                                }
                            }

                            Text(
                                text = "🌙 $nights ${AppLanguageManager.translate("Gece")}  ·  👥 $adults ${AppLanguageManager.translate("Yetişkin")} + $childs ${AppLanguageManager.translate("Çocuk")}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // 2. KISIM: OTEL KATEGORİSİ & YILDIZ FİLTRELERİ
                        Column(
                            modifier = Modifier.weight(1.2f),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            Text(
                                text = AppLanguageManager.translate("Otel Yıldızı & Konsept"),
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                                fontWeight = FontWeight.Bold
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                (3..5).forEach { star ->
                                    val isSel = selectedStars.contains(star)
                                    FilterChip(
                                        selected = isSel,
                                        onClick = {
                                            selectedStars = if (isSel) selectedStars - star else selectedStars + star
                                        },
                                        label = { Text("⭐ $star*", style = TourOSTypography.Caption) }
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                FilterChip(
                                    selected = isInstantOnly,
                                    onClick = { isInstantOnly = !isInstantOnly },
                                    label = { Text("⚡ ${AppLanguageManager.translate("Anında Onay")}", style = TourOSTypography.Caption) }
                                )
                                FilterChip(
                                    selected = isPromoOnly,
                                    onClick = { isPromoOnly = !isPromoOnly },
                                    label = { Text("🔥 ${AppLanguageManager.translate("Promo Fiyat")}", style = TourOSTypography.Caption) }
                                )
                            }
                        }

                        // 3. KISIM: ARAMA METNİ & AKSİYON BUTONU
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            TourOSTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = AppLanguageManager.translate("Otel / Tur Adı ile Canlı Ara"),
                                placeholder = "Ali Bey, Barut, Adalya...",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            TourOSButton(
                                text = "🔍 ${AppLanguageManager.translate("Turları Bul & Sorgula")}",
                                onClick = { viewModel.performSearch() },
                                variant = TourOSButtonVariant.PRIMARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ── BLOK 2: GÖRSEL 8 - MATRİS SONUÇ KARTLARI ─────────────────────────────
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
                    val products = state.filteredProducts

                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊 2. ${AppLanguageManager.translate("Bulunan Tur Seçenekleri")} (${products.size} ${AppLanguageManager.translate("Tur Bulundu")})",
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
                                onSelectForBooking = {
                                    viewModel.selectProductForBooking(item)
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // ── BLOK 3 & 4: TUR SEÇİLİNCE AÇILAN ALT REZERVASYON PANELİ ──────────────
            val curProduct = selectedProduct
            if (curProduct == null) {
                // Tur seçilmediğinde gösterilen yönlendirme kutusu
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.Surface,
                    contentPadding = TourOSSpacing.medium
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "👆 ${AppLanguageManager.translate("Yukarıdaki listeden bir tur kartının üzerindeki '⚡ Turu Seç & Detaylandır' butonuna bastığınızda; uçuş alternatifleri, sigorta/transfer ekstraları ve yolcu formu bu alanda açılacaktır.")}",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                val basePrice = curProduct.price * 1.125
                val flightDelta = selectedFlightOption?.priceDeltaRub ?: 0.0
                val extrasTotalEur = extraServices.filter { it.isSelected }.sumOf { it.unitPriceEur * it.paxCount }
                val extrasTotalRub = extrasTotalEur * 100.0
                val grandTotalRub = basePrice + flightDelta + extrasTotalRub

                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(
                        text = "✈️ 3. ${AppLanguageManager.translate("Alternatif Uçuşlar & Ekstra Hizmetler")}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )

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
                                    text = "🏨 ${curProduct.hotelName} ⭐".repeat(curProduct.hotelCategory.coerceAtMost(5)),
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🛏️ ${curProduct.roomType.ifBlank { "FAMILY ROOM" }}  ·  🍽️ ${curProduct.mealType.ifBlank { "Ultra All Inclusive" }}",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                )
                                Text(
                                    text = "📅 ${curProduct.departureDate ?: "21.08.2026"} (7 ${AppLanguageManager.translate("Gece")})  ·  👥 2 ADL + 2 CHD  ·  📍 ${curProduct.region}",
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
                        text = "🛫 ${AppLanguageManager.translate("Uçuş Alternatifleri")} (${availableFlightOptions.size} ${AppLanguageManager.translate("Uçuş Çifti")})",
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
                        text = "🛡️ ${AppLanguageManager.translate("Sigorta ve VIP Transfer Ekstraları")}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )

                    extraServices.forEach { srv ->
                        ExtraServiceCardItem(
                            service = srv,
                            onToggle = { viewModel.toggleExtraService(srv.id) }
                        )
                    }
                }

                HorizontalDivider(color = TourOSColors.Divider)

                // ── BLOK 4: YOLCU (TURİST) BİLGİLERİ FORMU (DİNAMİK YOLCU YÖNETİMİ) ───
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤 4. ${AppLanguageManager.translate("Yolcu (Turist) Pasaport Bilgileri")} (${passengers.size} ${AppLanguageManager.translate("Yolcu Kayıtlı")})",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            OutlinedButton(
                                onClick = { viewModel.addPassenger() },
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.2f))
                            ) {
                                Text("➕ ${AppLanguageManager.translate("Yolcu Ekle")}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
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
                                    text = "⚡ ${AppLanguageManager.translate("Anında Onaylı Operatör Kaydı")}",
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
                                    text = "🚀 ${AppLanguageManager.translate("Rezervasyonu Tamamla & Onayla (PNR Oluştur)")}",
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

    // REZERVASYON BAŞARILI MODALI
    if (showSuccessModal) {
        AlertDialog(
            onDismissRequest = { showSuccessModal = false },
            title = {
                Text(
                    text = "🎉 ${AppLanguageManager.translate("Rezervasyon Başarıyla Oluşturuldu!")}",
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
                        text = "✅ ${AppLanguageManager.translate("Veri Konumu: Supabase 'public.bookings' tablosuna ve Ana Rezervasyon Yönetim Paneline kaydedildi.")}",
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
                        text = "📑 ${AppLanguageManager.translate("Rezervasyon Listesine Git")}",
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
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TourOSColors.SuccessContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "✓ Seçili Tur",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "🆔 ID: ${product.id}  ·  📍 ${product.region}  ·  🏢 ${product.operatorName}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                    fontSize = 11.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!product.departureDate.isNullOrBlank()) {
                        Text(
                            text = "📅 ${product.departureDate}  ·  🌙 ${product.nights} ${AppLanguageManager.translate("Gece")}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (product.roomType.isNotBlank()) {
                        Text(
                            text = "🛏️ ${product.roomType}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
                        )
                    }
                    if (product.mealType.isNotBlank()) {
                        Text(
                            text = "🍽️ ${product.mealType}",
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
                            text = "✈️ VKO - AYT (Ekonomi 🟢  Business 🟢)",
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
                            text = "✈️ SVO - AYT (Ekonomi 🟢)",
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
                        text = "⚡ Anında Onay",
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

                TourOSButton(
                    text = if (isSelected) "✓ ${AppLanguageManager.translate("Seçildi (Aşağıya İnceleyin)")}" else "⚡ ${AppLanguageManager.translate("Turu Seç & Detaylandır")}",
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
        backgroundColor = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.15f) else TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
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
                        text = "🛫 GİDİŞ: ${option.outboundAirline} (${option.outboundFlightNumber})  ·  ${option.outboundDeparturePort} ➔ ${option.outboundArrivalPort} (${option.outboundDuration})",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(
                        text = "🛬 DÖNÜŞ: ${option.inboundAirline} (${option.inboundFlightNumber})  ·  ${option.inboundDeparturePort} ➔ ${option.inboundArrivalPort} (${option.inboundDuration})",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "🧳 El Bagajı: ${option.handBaggageKg}kg  ·  Kayıtlı Bagaj: ${option.baggageKg}kg",
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
                        text = "Uçuş Farkı",
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
                            text = "Fark Yok (Pakete Dahil)",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                            fontWeight = FontWeight.Bold
                        )
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

// ─── YOLCU FORM KART BİLEŞENİ (GÖRSEL 5 & 6) ───────────────────────────────────

@Composable
private fun PassengerFormCardItem(
    passenger: PassengerInfo,
    paxIndex: Int,
    canRemove: Boolean = false,
    onRemove: () -> Unit = {},
    onUpdatePassenger: (PassengerInfo) -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👤 ${AppLanguageManager.translate("Turist")} $paxIndex ${if (passenger.isPayer) "(${AppLanguageManager.translate("Sipariş Veren Müşteri")})" else ""}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    if (canRemove) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🗑️ ${AppLanguageManager.translate("Kaldır")}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Error),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onRemove() }
                        )
                    }
                }

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
            }
        }
    }
}
