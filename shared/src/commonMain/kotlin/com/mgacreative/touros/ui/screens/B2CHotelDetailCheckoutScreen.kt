package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2CHotelDetailCheckoutViewModel

/**
 * B2C Otel Detay & Rezervasyon Ekranı — TourOS
 *
 * Üstte Büyük Otel Kapak Görseli + Adres & Tanıtım Bloğu.
 * Altında Tarih / Gece / Oda Tipi Seçici & Tur Rezervasyonundaki Birebir Ödeme Sistemi.
 * Sabit (Sticky) "Hemen Rezervasyonu Yap" Çubuğu.
 */
@Composable
fun B2CHotelDetailCheckoutScreen(
    viewModel: B2CHotelDetailCheckoutViewModel,
    hotelId: String,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(hotelId) {
        viewModel.loadHotelDetail(hotelId)
    }

    val hotel = state.hotel

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Otel Detayı & Rezervasyon",
                subtitle = hotel?.name ?: "Otel Detayı",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        },
        bottomBar = {
            if (hotel != null) {
                StickyHotelReservationBottomBar(
                    nights = state.nights,
                    roomType = state.selectedRoomType,
                    pricePerNight = state.pricePerNight,
                    totalPrice = state.totalPrice,
                    onSubmitBooking = { viewModel.submitHotelBooking() }
                )
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TourOSColors.Primary)
            }
        } else if (hotel == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = state.errorMessage ?: "Otel bulunamadı", color = TourOSColors.Error)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = TourOSSpacing.xxLarge),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // ── BİLDİRİM / MESAJ BANNERİ ─────────────────────────────
                if (state.notificationMessage != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = TourOSSpacing.large)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TourOSColors.SuccessContainer)
                                .padding(TourOSSpacing.medium)
                        ) {
                            Text(
                                state.notificationMessage!!,
                                style = TourOSTypography.Label.copy(color = TourOSColors.Success),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (state.errorMessage != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = TourOSSpacing.large)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TourOSColors.ErrorContainer)
                                .padding(TourOSSpacing.medium)
                        ) {
                            Text(
                                state.errorMessage!!,
                                style = TourOSTypography.Label.copy(color = TourOSColors.Error),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ── 1. BÜYÜK KAPAK RESMİ & OTEL BAŞLIĞI ──────────────────
                item {
                    HotelHeroBanner(hotel = hotel)
                }

                // ── 2. OTEL TANITIM AÇIKLAMASI VE ADRES ──────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        TourOSCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TourOSColors.Background,
                            borderColor = TourOSColors.Border,
                            contentPadding = TourOSSpacing.large
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                Text(
                                    text = "🏨 Otel Tanıtım Açıklaması",
                                    style = TourOSTypography.TitleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TourOSColors.Primary
                                )
                                Text(
                                    text = hotel.description?.takeIf { it.isNotBlank() } ?: "Acentemiz özel anlaşması ile yüksek konfor ve eşsiz tatil deneyimi sunan 5 yıldızlı lüks tesis.",
                                    style = TourOSTypography.BodyMedium,
                                    color = TourOSColors.TextSecondary
                                )
                            }
                        }

                        TourOSCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TourOSColors.Background,
                            borderColor = TourOSColors.Border,
                            contentPadding = TourOSSpacing.large
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                Text(
                                    text = "📍 Otel Adresi ve Konum",
                                    style = TourOSTypography.TitleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TourOSColors.Primary
                                )
                                Text(
                                    text = "${hotel.address?.takeIf { it.isNotBlank() } ?: "Merkez Mahallesi, Sahil Caddesi No:45"}, ${hotel.city ?: "Antalya"} / ${hotel.country}",
                                    style = TourOSTypography.BodyMedium,
                                    color = TourOSColors.TextPrimary
                                )
                            }
                        }
                    }
                }

                // ── 3. REZERVASYON DETAYLARI VE FORM ───────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        Text(
                            text = "📝 Hemen Rezervasyon Yap",
                            style = TourOSTypography.TitleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TourOSColors.TextPrimary
                        )

                        TourOSCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TourOSColors.Surface,
                            borderColor = TourOSColors.Primary.copy(alpha = 0.3f),
                            contentPadding = TourOSSpacing.large
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                                Text(
                                    text = "1. Müşteri İletişim Bilgileri",
                                    style = TourOSTypography.TitleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TourOSColors.Primary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                ) {
                                    TourOSTextField(
                                        value = state.customerName,
                                        onValueChange = { name -> viewModel.updateCustomerInfo(name, state.customerPhone, state.customerEmail) },
                                        label = "Müşteri Adı Soyadı *",
                                        placeholder = "Ahmet Yılmaz",
                                        modifier = Modifier.weight(1f)
                                    )

                                    TourOSTextField(
                                        value = state.customerPhone,
                                        onValueChange = { phone -> viewModel.updateCustomerInfo(state.customerName, phone, state.customerEmail) },
                                        label = "Müşteri Telefonu *",
                                        placeholder = "0532 000 00 00",
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                TourOSTextField(
                                    value = state.customerEmail,
                                    onValueChange = { email -> viewModel.updateCustomerInfo(state.customerName, state.customerPhone, email) },
                                    label = "E-posta Adresi",
                                    placeholder = "ahmet@example.com",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                HorizontalDivider(color = TourOSColors.Border)

                                Text(
                                    text = "2. Konaklama Tarihi & Oda Tipi Seçimi",
                                    style = TourOSTypography.TitleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TourOSColors.Primary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                ) {
                                    TourOSTextField(
                                        value = state.checkInDate,
                                        onValueChange = { checkIn -> viewModel.updateDates(checkIn, state.checkOutDate) },
                                        label = "Giriş Tarihi (Check-in)",
                                        placeholder = "2026-07-01",
                                        modifier = Modifier.weight(1f)
                                    )

                                    TourOSTextField(
                                        value = state.checkOutDate,
                                        onValueChange = { checkOut -> viewModel.updateDates(state.checkInDate, checkOut) },
                                        label = "Çıkış Tarihi (Check-out)",
                                        placeholder = "2026-07-05",
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                TourOSDropdown(
                                    items = state.availableRoomTypes,
                                    selectedItem = state.selectedRoomType,
                                    onItemSelected = { viewModel.updateRoomType(it) },
                                    itemLabel = { it },
                                    label = "Oda Tipi Seçimi *"
                                )

                                // Gece & Dynamic Fiyatlama Bilgi Kutusu
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(TourOSColors.PrimaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(TourOSSpacing.medium)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Konaklama: ${state.nights} Gece • ${state.selectedRoomType}",
                                                style = TourOSTypography.BodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TourOSColors.Primary
                                            )
                                            Text(
                                                text = "Oda Gecelik Fiyatı: ${state.pricePerNight} ₺",
                                                style = TourOSTypography.Caption,
                                                color = TourOSColors.TextSecondary
                                            )
                                        }

                                        Text(
                                            text = "Toplam: ${state.totalPrice} ₺",
                                            style = TourOSTypography.TitleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = TourOSColors.Primary
                                        )
                                    }
                                }

                                HorizontalDivider(color = TourOSColors.Border)

                                // ── TUR REZERVASYONUNDAKİ ÖDEME SİSTEMİ ──
                                Text(
                                    text = "3. Ödeme Yöntemi & Ödeme İşlemi",
                                    style = TourOSTypography.TitleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TourOSColors.Primary
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    val paymentOptions = listOf(
                                        "KREDİ_KARTI" to "💳 Kredi Kartı / Banka Kartı",
                                        "HAVALE_EFT" to "🏦 Banka Havalesi / EFT",
                                        "ACENTEDE_ODEME" to "💵 Kapıda / Acentede Ödeme"
                                    )

                                    paymentOptions.forEach { (code, label) ->
                                        val isSelected = state.paymentMethod == code
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Surface)
                                                .border(1.dp, if (isSelected) TourOSColors.Primary else TourOSColors.Border, RoundedCornerShape(8.dp))
                                                .clickable { viewModel.updatePaymentMethod(code) }
                                                .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { viewModel.updatePaymentMethod(code) }
                                            )
                                            Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                            Text(
                                                text = label,
                                                style = TourOSTypography.BodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) TourOSColors.Primary else TourOSColors.TextPrimary
                                            )
                                        }
                                    }
                                }

                                // ── SEÇİLİ ÖDEME YÖNTEMİ FORMU ──
                                when (state.paymentMethod) {
                                    "KREDİ_KARTI" -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(top = TourOSSpacing.small),
                                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                        ) {
                                            TourOSTextField(
                                                value = state.cardHolder,
                                                onValueChange = { holder -> viewModel.updateCardInfo(holder, state.cardNumber, state.cardExpiry, state.cvv) },
                                                label = "Kart Üzerindeki İsim",
                                                placeholder = "Ahmet Yılmaz",
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            TourOSTextField(
                                                value = state.cardNumber,
                                                onValueChange = { num -> viewModel.updateCardInfo(state.cardHolder, num, state.cardExpiry, state.cvv) },
                                                label = "Kart Numarası",
                                                placeholder = "4543 **** **** 1234",
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                            ) {
                                                TourOSTextField(
                                                    value = state.cardExpiry,
                                                    onValueChange = { exp -> viewModel.updateCardInfo(state.cardHolder, state.cardNumber, exp, state.cvv) },
                                                    label = "Son Kullanma (AA/YY)",
                                                    placeholder = "12/28",
                                                    modifier = Modifier.weight(1f)
                                                )

                                                TourOSTextField(
                                                    value = state.cvv,
                                                    onValueChange = { c -> viewModel.updateCardInfo(state.cardHolder, state.cardNumber, state.cardExpiry, c) },
                                                    label = "CVV Güvenlik Kodu",
                                                    placeholder = "123",
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }

                                    "HAVALE_EFT" -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(TourOSColors.PrimaryContainer.copy(alpha = 0.5f))
                                                .padding(TourOSSpacing.medium)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    "🏦 Acente Banka & IBAN Bilgileri",
                                                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    "Banka: Garanti BBVA - TR45 0006 2000 1234 5678 9000 01",
                                                    style = TourOSTypography.BodyMedium,
                                                    color = TourOSColors.TextPrimary
                                                )
                                                Text(
                                                    "Açıklamaya adınızı ve oluşturulacak rezervasyon kodunuzu ekleyiniz.",
                                                    style = TourOSTypography.Caption,
                                                    color = TourOSColors.TextSecondary
                                                )
                                            }
                                        }
                                    }

                                    "ACENTEDE_ODEME" -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(TourOSColors.SecondaryContainer.copy(alpha = 0.5f))
                                                .padding(TourOSSpacing.medium)
                                        ) {
                                            Text(
                                                "💵 Giriş günü resepsiyonumuzda veya acente yetkilimize ödeme yapabilirsiniz.",
                                                style = TourOSTypography.BodyMedium,
                                                color = TourOSColors.Secondary
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

@Composable
private fun HotelHeroBanner(hotel: Hotel) {
    val starsText = "⭐".repeat((hotel.starRating ?: 4).coerceIn(1, 5))
    val cover = hotel.coverImageUrl?.takeIf { it.isNotBlank() }
        ?: "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&auto=format&fit=crop"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
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
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(TourOSSpacing.large)
        ) {
            Box(
                modifier = Modifier
                    .background(TourOSColors.Primary, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = starsText,
                    style = TourOSTypography.Label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(TourOSSpacing.small))

            Text(
                text = hotel.name,
                style = TourOSTypography.TitleLarge.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "📍 ${hotel.city ?: "Türkiye"}, ${hotel.country}",
                style = TourOSTypography.BodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun StickyHotelReservationBottomBar(
    nights: Int,
    roomType: String,
    pricePerNight: Double,
    totalPrice: Double,
    onSubmitBooking: () -> Unit
) {
    Surface(
        color = TourOSColors.Surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$nights Gece • $roomType",
                    style = TourOSTypography.Caption,
                    color = TourOSColors.TextSecondary
                )
                Text(
                    text = "Toplam: ${totalPrice} ₺",
                    style = TourOSTypography.TitleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.Primary
                )
            }

            TourOSButton(
                text = "Hemen Rezervasyonu Yap (₺ ${totalPrice})",
                onClick = onSubmitBooking
            )
        }
    }
}
