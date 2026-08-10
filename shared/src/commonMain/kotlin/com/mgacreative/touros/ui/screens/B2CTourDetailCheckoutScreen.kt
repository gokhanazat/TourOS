package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.DepartureOption
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2CTourDetailCheckoutViewModel


/**
 * B2C Tur Detay & Rezervasyon Ekranı — TourOS 0.3
 *
 * Üstte Büyük Kapak Görseli + Galeri Thumbnail Çubuğu.
 * Altında Tarih/Kişi Seçici Sabit (Sticky) 'Rezervasyon Yap' Çubuğu.
 */
@Composable
fun B2CTourDetailCheckoutScreen(
    viewModel: B2CTourDetailCheckoutViewModel,
    tourId: String = "t101",
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(tourId) {
        viewModel.loadTourDetail(tourId)
    }

    var selectedDate by remember { mutableStateOf("") }
    var isCheckoutFormOpen by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("KREDİ_KARTI") } // KREDİ_KARTI, BANKA_HAVALESİ, NAKİT

    var passengerName by remember { mutableStateOf("") }
    var passengerPhone by remember { mutableStateOf("") }
    var passengerEmail by remember { mutableStateOf("") }

    var cardHolder by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val detail = state.tourDetail

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Tur Detayı & Rezervasyon",
                subtitle = detail.title,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        },
        // ── SABİT (STICKY) REZERVASYON YAP ÇUBUĞU ─────────────────────────────
        bottomBar = {
            StickyReservationBottomBar(
                selectedDate = selectedDate,
                onDateSelect = { selectedDate = it },
                paxCount = state.paxCount,
                onPaxChange = { newPax -> viewModel.updatePaxCount(newPax) },
                totalPrice = state.totalPrice,
                onBookClick = { isCheckoutFormOpen = true }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // Bildirim Mesajı
            if (state.notificationMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = TourOSSpacing.large)
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SuccessContainer)
                            .padding(TourOSSpacing.medium)
                    ) {
                        Text(
                            state.notificationMessage!!,
                            style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                        )
                    }
                }
            }

            // ── 1. BÜYÜK KAPAK GÖRSELİ ÜSTTE ──────────────────────────
            item {
                HeroImageBanner(
                    title = detail.title,
                    category = detail.category,
                    rating = detail.rating,
                    coverImageUrl = detail.coverImageUrl
                )
            }

            // ── 2. KALKIŞ TARİHLERİ SEÇİMİ ───────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TourOSSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    Text(
                        "🗓️ Kalkış Tarihleri Seçimi",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )

                    val departures = detail.availableDepartures

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(departures) { dep ->
                            val isSelected = (state.selectedDepartureId == dep.id) || (selectedDate == dep.departureDate)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Surface
                                ),
                                shape = RoundedCornerShape(TourOSSpacing.cornerRadius),
                                modifier = Modifier
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
                                        shape = RoundedCornerShape(TourOSSpacing.cornerRadius)
                                    )
                                    .clickable {
                                        viewModel.selectDeparture(dep.id, dep.departureDate)
                                        selectedDate = dep.departureDate
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        dep.departureDate,
                                        style = TourOSTypography.Label.copy(
                                            color = if (isSelected) TourOSColors.Primary else TourOSColors.TextPrimary
                                        )
                                    )
                                    val itemPrice = dep.price ?: detail.price
                                    if (itemPrice > 0) {
                                        Text(
                                            "₺ ${formatDetailMoney(itemPrice)}",
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. TUR GENEL BİLGİLERİ VE PROGRAM ÖZETİ ─────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TourOSSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    TourOSCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = TourOSColors.Surface,
                        contentPadding = TourOSSpacing.large
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            Text(
                                "🏔️ Tur Hakkında & Öne Çıkanlar",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                            )
                            Text(
                                detail.description.ifBlank { "Açıklama belirtilmemiş." },
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                            )

                            HorizontalDivider(color = TourOSColors.Divider)

                            Text(
                                "🗓️ Günlük Tur Programı Özeti",
                                style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                            )
                            Text(
                                detail.itinerarySummary.ifBlank { "Tur programı bilgisi bulunmuyor." },
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    }

                    // Fiyata Dahil Olan / Olmayan Hizmetler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // Dahil Olanlar
                        TourOSCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = TourOSColors.SuccessContainer.copy(alpha = 0.4f),
                            contentPadding = TourOSSpacing.medium
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("✅ Dahil Hizmetler", style = TourOSTypography.Label.copy(color = TourOSColors.Success))
                                if (detail.includedServices.isNotEmpty()) {
                                    detail.includedServices.forEach { s ->
                                        Text("• $s", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
                                    }
                                } else {
                                    Text("Belirtilmemiş", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                }
                            }
                        }

                        // Dahil Olmayanlar
                        TourOSCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = TourOSColors.SecondaryContainer.copy(alpha = 0.4f),
                            contentPadding = TourOSSpacing.medium
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("❌ Dahil Olmayanlar", style = TourOSTypography.Label.copy(color = TourOSColors.Secondary))
                                if (detail.excludedServices.isNotEmpty()) {
                                    detail.excludedServices.forEach { s ->
                                        Text("• $s", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
                                    }
                                } else {
                                    Text("Belirtilmemiş", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. ÖDEME VE REZERVASYON MODAL / FORM ALANI (AÇILDIĞINDA) ───────
            if (isCheckoutFormOpen) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        TourOSCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.3f),
                            contentPadding = TourOSSpacing.large
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "📝 Rezervasyon & Ödeme İşlemi",
                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                                    )

                                    IconButton(onClick = { isCheckoutFormOpen = false }) {
                                        Text("✕", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextSecondary))
                                    }
                                }

                                if (state.checkoutResult != null) {
                                    val res = state.checkoutResult!!
                                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                                .background(TourOSColors.SuccessContainer)
                                                .padding(TourOSSpacing.medium)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    "🎉 Rezervasyon Başarıyla Oluşturuldu!",
                                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Success)
                                                )
                                                Text(
                                                    "Kod: ${res.bookingCode} | Tutar: ₺ ${formatDetailMoney(res.totalAmount)}",
                                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (res.whatsappCustomerDirectUrl.isNotBlank()) {
                                                        runCatching { uriHandler.openUri(res.whatsappCustomerDirectUrl) }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = androidx.compose.ui.graphics.Color(0xFF25D366)
                                                ),
                                                shape = RoundedCornerShape(TourOSSpacing.cornerRadius),
                                                modifier = Modifier.weight(1f).height(48.dp)
                                            ) {
                                                Text(
                                                    "💬 Müşteriye",
                                                    style = TourOSTypography.TitleMedium.copy(
                                                        color = androidx.compose.ui.graphics.Color.White
                                                    )
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    if (res.whatsappDirectUrl.isNotBlank()) {
                                                        runCatching { uriHandler.openUri(res.whatsappDirectUrl) }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = androidx.compose.ui.graphics.Color(0xFF128C7E)
                                                ),
                                                shape = RoundedCornerShape(TourOSSpacing.cornerRadius),
                                                modifier = Modifier.weight(1f).height(48.dp)
                                            ) {
                                                Text(
                                                    "💬 Acentaya",
                                                    style = TourOSTypography.TitleMedium.copy(
                                                        color = androidx.compose.ui.graphics.Color.White
                                                    )
                                                )
                                            }

                                            TourOSButton(
                                                text = "Formu Kapat",
                                                onClick = { isCheckoutFormOpen = false },
                                                variant = TourOSButtonVariant.SECONDARY,
                                                modifier = Modifier.weight(1f).height(48.dp)
                                            )
                                        }
                                    }
                                } else {
                                    // 1. ÖDEME YÖNTEMİ SEÇİM CHİPLERİ
                                    Text(
                                        "💳 Ödeme Yöntemi Seçiniz",
                                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                    ) {
                                        PaymentOptionChip(
                                            title = "💳 Kredi Kartı",
                                            isSelected = paymentMethod == "KREDİ_KARTI",
                                            onClick = { paymentMethod = "KREDİ_KARTI" },
                                            modifier = Modifier.weight(1f)
                                        )
                                        PaymentOptionChip(
                                            title = "🏦 Havale / EFT",
                                            isSelected = paymentMethod == "BANKA_HAVALESİ",
                                            onClick = { paymentMethod = "BANKA_HAVALESİ" },
                                            modifier = Modifier.weight(1f)
                                        )
                                        PaymentOptionChip(
                                            title = "💵 Kapıda / Nakit",
                                            isSelected = paymentMethod == "NAKİT",
                                            onClick = { paymentMethod = "NAKİT" },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    HorizontalDivider(color = TourOSColors.Divider)

                                    // 2. YOLCU VE İLETİŞİM BİLGİLERİ (Giriş Alanları)
                                    Text(
                                        "👤 Yolcu & İletişim Bilgileri",
                                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                                    )

                                    TourOSTextField(
                                        value = passengerName,
                                        onValueChange = { passengerName = it },
                                        label = "Yolcu Adı Soyadı",
                                        placeholder = "Adınız ve Soyadınız",
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                    ) {
                                        TourOSTextField(
                                            value = passengerPhone,
                                            onValueChange = { passengerPhone = it },
                                            label = "Telefon",
                                            placeholder = "+90 5xx xxx xx xx",
                                            modifier = Modifier.weight(1f)
                                        )
                                        TourOSTextField(
                                            value = passengerEmail,
                                            onValueChange = { passengerEmail = it },
                                            label = "E-Posta",
                                            placeholder = "ornek@eposta.com",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    HorizontalDivider(color = TourOSColors.Divider)

                                    // 3. SEÇİLEN ÖDEME YÖNTEMİNE GÖRE İÇERİK
                                    when (paymentMethod) {
                                        "KREDİ_KARTI" -> {
                                            TourOSTextField(
                                                value = cardHolder,
                                                onValueChange = { cardHolder = it },
                                                label = "Kart Üzerindeki İsim",
                                                placeholder = "Kart sahibinin adı",
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            TourOSTextField(
                                                value = cardNumber,
                                                onValueChange = { cardNumber = it },
                                                label = "Kart Numarası",
                                                placeholder = "0000 0000 0000 0000",
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                            ) {
                                                TourOSTextField(
                                                    value = cardExpiry,
                                                    onValueChange = { cardExpiry = it },
                                                    label = "SKT (AA/YY)",
                                                    placeholder = "AA/YY",
                                                    modifier = Modifier.weight(1f)
                                                )
                                                TourOSTextField(
                                                    value = cvv,
                                                    onValueChange = { cvv = it },
                                                    label = "CVV",
                                                    placeholder = "123",
                                                    visualTransformation = PasswordVisualTransformation(),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            TourOSButton(
                                                text = "💳 ₺ ${formatDetailMoney(state.totalPrice)} Kredi Kartı İle Öde",
                                                onClick = {
                                                    viewModel.processCheckout(
                                                        passengerName = passengerName,
                                                        phone = passengerPhone,
                                                        email = passengerEmail,
                                                        paymentMethod = "KREDİ_KARTI",
                                                        cardHolder = cardHolder,
                                                        cardNumber = cardNumber,
                                                        expiry = cardExpiry,
                                                        cvv = cvv
                                                    )
                                                },
                                                enabled = !state.isLoading && passengerName.isNotBlank() && cardHolder.isNotBlank(),
                                                variant = TourOSButtonVariant.PRIMARY,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        "BANKA_HAVALESİ" -> {
                                            TourOSCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                backgroundColor = TourOSColors.Surface,
                                                contentPadding = TourOSSpacing.medium
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        "🏦 Acente Banka & IBAN Bilgileri",
                                                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                                                    )
                                                    Text(
                                                        "Acente: ${detail.agencyName}",
                                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                                                    )
                                                    if (!detail.bankName.isNullOrBlank()) {
                                                        Text(
                                                            "Banka: ${detail.bankName}",
                                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                                        )
                                                    }
                                                    if (!detail.iban.isNullOrBlank()) {
                                                        Text(
                                                            "IBAN: ${detail.iban}",
                                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                                                        )
                                                    }
                                                    if (!detail.accountHolder.isNullOrBlank()) {
                                                        Text(
                                                            "Hesap Sahibi: ${detail.accountHolder}",
                                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                                        )
                                                    }
                                                    Text(
                                                        "Ödeme açıklamasına rezervasyon kodunuzu eklemeyi unutmayınız.",
                                                        style = TourOSTypography.Caption.copy(color = TourOSColors.Secondary)
                                                    )
                                                }
                                            }

                                            TourOSButton(
                                                text = "🏦 ₺ ${formatDetailMoney(state.totalPrice)} Havale / EFT İle Tamamla",
                                                onClick = {
                                                    viewModel.processCheckout(
                                                        passengerName = passengerName,
                                                        phone = passengerPhone,
                                                        email = passengerEmail,
                                                        paymentMethod = "BANKA_HAVALESİ"
                                                    )
                                                },
                                                enabled = !state.isLoading && passengerName.isNotBlank(),
                                                variant = TourOSButtonVariant.PRIMARY,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        "NAKİT" -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                                    .background(TourOSColors.SecondaryContainer.copy(alpha = 0.5f))
                                                    .padding(TourOSSpacing.medium)
                                            ) {
                                                Text(
                                                    "💵 Tur günü rehberimize veya acente yetkilimize nakit ödeme yapabilirsiniz.",
                                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Secondary)
                                                )
                                            }

                                            TourOSButton(
                                                text = "💵 ₺ ${formatDetailMoney(state.totalPrice)} Nakit Ödeme İle Tamamla",
                                                onClick = {
                                                    viewModel.processCheckout(
                                                        passengerName = passengerName,
                                                        phone = passengerPhone,
                                                        email = passengerEmail,
                                                        paymentMethod = "NAKİT"
                                                    )
                                                },
                                                enabled = !state.isLoading && passengerName.isNotBlank(),
                                                variant = TourOSButtonVariant.PRIMARY,
                                                modifier = Modifier.fillMaxWidth()
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

// ─── BÜYÜK KAPAK GÖRSELİ BANNERI ─────────────────────────────────────

@Composable
private fun HeroImageBanner(
    title: String,
    category: String,
    rating: Double,
    coverImageUrl: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = TourOSSpacing.cornerRadiusSmall, bottomEnd = TourOSSpacing.cornerRadiusSmall))
            .background(TourOSColors.PrimaryContainer)
    ) {
        val imgUrl = coverImageUrl?.takeIf { it.isNotBlank() }

        if (imgUrl != null) {
            coil3.compose.AsyncImage(
                model = imgUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }

        // YAZILARIN OKUNMASI İÇİN GRADIENT OVERLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        Box(modifier = Modifier.fillMaxSize().padding(TourOSSpacing.large)) {
            if (category.isNotBlank()) {
                TourOSStatusBadge(
                    text = category,
                    backgroundColor = TourOSColors.Primary,
                    textColor = TourOSColors.OnPrimary,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }

            if (rating > 0) {
                TourOSStatusBadge(
                    text = "⭐ $rating Müşteri Puanı",
                    backgroundColor = TourOSColors.SecondaryContainer,
                    textColor = TourOSColors.Secondary,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    title,
                    style = TourOSTypography.TitleLarge.copy(color = Color.White)
                )
            }
        }
    }
}

// ─── ALTINDA TARİH / KİŞİ SEÇİCİ SABİT (STICKY) 'REZERVASYON YAP' ÇUBUĞU ───────

@Composable
private fun StickyReservationBottomBar(
    selectedDate: String,
    onDateSelect: (String) -> Unit,
    paxCount: Int,
    onPaxChange: (Int) -> Unit,
    totalPrice: Double,
    onBookClick: () -> Unit
) {
    Surface(
        color = TourOSColors.Surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tarih Seçimi
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Tarih:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(selectedDate, style = TourOSTypography.Label.copy(color = TourOSColors.Primary))
                }

                // Pax Counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = { onPaxChange(paxCount - 1) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                    ) {
                        Text("-", style = TourOSTypography.TitleMedium)
                    }
                    Text(
                        "$paxCount Pax",
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                    )
                    OutlinedButton(
                        onClick = { onPaxChange(paxCount + 1) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                    ) {
                        Text("+", style = TourOSTypography.TitleMedium)
                    }
                }

                // Toplam Fiyat
                Column(horizontalAlignment = Alignment.End) {
                    Text("Toplam Tutar", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "₺ ${formatDetailMoney(totalPrice)}",
                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                    )
                }
            }

            TourOSButton(
                text = "🛒 Hemen Rezervasyon Yap (₺ ${formatDetailMoney(totalPrice)})",
                onClick = onBookClick,
                variant = TourOSButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatDetailMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

@Composable
private fun PaymentOptionChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Surface
        ),
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
            )
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                title,
                style = TourOSTypography.Caption.copy(
                    color = if (isSelected) TourOSColors.Primary else TourOSColors.TextPrimary
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
