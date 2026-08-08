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
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2CTourDetailCheckoutViewModel

private data class GalleryPhotoItem(val id: String, val title: String, val icon: String)

private val sampleGalleryPhotos = listOf(
    GalleryPhotoItem("g1", "Göreme Vadisi", "🎈"),
    GalleryPhotoItem("g2", "Gün Doğumu", "🌄"),
    GalleryPhotoItem("g3", "Uçhisar Kalesi", "🏰"),
    GalleryPhotoItem("g4", "Mahsen Tadım", "🍷")
)

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

    var selectedDate by remember { mutableStateOf("15 Ağustos 2026") }
    var selectedPhotoIndex by remember { mutableStateOf(0) }
    var isCheckoutFormOpen by remember { mutableStateOf(false) }

    var passengerName by remember { mutableStateOf("Elif Yılmaz") }
    var passengerPhone by remember { mutableStateOf("+90 532 111 2233") }
    var passengerEmail by remember { mutableStateOf("elif.yilmaz@email.com") }

    var cardHolder by remember { mutableStateOf("ELIF YILMAZ") }
    var cardNumber by remember { mutableStateOf("4543 2100 8899 4242") }
    var cardExpiry by remember { mutableStateOf("12/28") }
    var cvv by remember { mutableStateOf("321") }

    val detail = state.tourDetail

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Tur Detayı & Rezervasyon",
                subtitle = detail.title.ifBlank { "Kapadokya VIP Balon Turu" },
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

            // ── 1. BÜYÜK KAPAK GÖRSELİ + GALERİ ÜSTTE ──────────────────────────
            item {
                HeroImageAndGalleryBanner(
                    title = detail.title.ifBlank { "Kapadokya VIP Balon & Vadi Turu" },
                    category = detail.category.ifBlank { "Balon & Vadi" },
                    rating = detail.rating.takeIf { it > 0 } ?: 4.9,
                    selectedPhotoIndex = selectedPhotoIndex,
                    onPhotoSelect = { idx -> selectedPhotoIndex = idx }
                )
            }

            // ── 2. TUR GENEL BİLGİLERİ VE PROGRAM ÖZETİ ─────────────────────────
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
                                detail.description.ifBlank { "Kapadokya'nın eşsiz vadileri üzerinde gün doğumu sıcak hava balonu deneyimi ve bölgenin tarihi manastır gezisi." },
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                            )

                            HorizontalDivider(color = TourOSColors.Divider)

                            Text(
                                "🗓️ Günlük Tur Programı Özeti",
                                style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                            )
                            Text(
                                detail.itinerarySummary.ifBlank { "05:00 Otelden transfer -> 05:45 Balon kalkış -> 07:30 Şampanya kutlaması -> 09:30 Göreme Açık Hava Müzesi rehberli gezi -> 13:00 Öğle yemeği." },
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
                                val included = if (detail.includedServices.isNotEmpty()) detail.includedServices else listOf("VIP Otel Transferi", "Sıcak Hava Balonu", "Öğle Yemeği", "Profesyonel Rehber")
                                included.forEach { s ->
                                    Text("• $s", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
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
                                val excluded = if (detail.excludedServices.isNotEmpty()) detail.excludedServices else listOf("Kişisel Harcamalar", "Müze Giriş Biletleri", "İçecekler")
                                excluded.forEach { s ->
                                    Text("• $s", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
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
                                        "💳 3D Secure Güvenli Ödeme",
                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                                    )

                                    IconButton(onClick = { isCheckoutFormOpen = false }) {
                                        Text("✕", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextSecondary))
                                    }
                                }

                                TourOSTextField(
                                    value = passengerName,
                                    onValueChange = { passengerName = it },
                                    label = "Yolcu Adı Soyadı",
                                    placeholder = "Elif Yılmaz",
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
                                        placeholder = "+90 532 111 2233",
                                        modifier = Modifier.weight(1f)
                                    )
                                    TourOSTextField(
                                        value = passengerEmail,
                                        onValueChange = { passengerEmail = it },
                                        label = "E-Posta",
                                        placeholder = "elif@email.com",
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                TourOSTextField(
                                    value = cardHolder,
                                    onValueChange = { cardHolder = it },
                                    label = "Kart Üzerindeki İsim",
                                    placeholder = "ELIF YILMAZ",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                TourOSTextField(
                                    value = cardNumber,
                                    onValueChange = { cardNumber = it },
                                    label = "Kart Numarası",
                                    placeholder = "4543 2100 8899 4242",
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
                                        placeholder = "12/28",
                                        modifier = Modifier.weight(1f)
                                    )
                                    TourOSTextField(
                                        value = cvv,
                                        onValueChange = { cvv = it },
                                        label = "CVV",
                                        placeholder = "321",
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                TourOSButton(
                                    text = "💳 ₺ ${formatDetailMoney(state.totalPrice)} Öde & Rezervasyonu Tamamla",
                                    onClick = {
                                        viewModel.processCheckout(
                                            passengerName, passengerPhone, passengerEmail,
                                            cardHolder, cardNumber, cardExpiry, cvv
                                        )
                                    },
                                    enabled = !state.isLoading && passengerName.isNotBlank() && cardHolder.isNotBlank(),
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

// ─── BÜYÜK KAPAK GÖRSELİ + GALERİ BANNERI ─────────────────────────────────────

@Composable
private fun HeroImageAndGalleryBanner(
    title: String,
    category: String,
    rating: Double,
    selectedPhotoIndex: Int,
    onPhotoSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
    ) {
        // BÜYÜK KAPAK GÖRSELİ (HERO BANNER 220.DP)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(TourOSColors.PrimaryContainer)
                .padding(TourOSSpacing.large)
        ) {
            TourOSStatusBadge(
                text = category,
                backgroundColor = TourOSColors.Primary,
                textColor = TourOSColors.OnPrimary,
                modifier = Modifier.align(Alignment.TopStart)
            )

            TourOSStatusBadge(
                text = "⭐ $rating Müşteri Puanı",
                backgroundColor = TourOSColors.SecondaryContainer,
                textColor = TourOSColors.Secondary,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    sampleGalleryPhotos.getOrNull(selectedPhotoIndex)?.icon ?: "🎈",
                    style = TourOSTypography.DisplaySmall
                )
                Text(
                    title,
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                )
            }
        }

        // FOTOĞRAF GALERİSİ THUMBNAIL SLIDER
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TourOSSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            items(sampleGalleryPhotos.size) { idx ->
                val photo = sampleGalleryPhotos[idx]
                val isSelected = idx == selectedPhotoIndex

                Box(
                    modifier = Modifier
                        .size(68.dp, 52.dp)
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Surface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
                            shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                        )
                        .clickable { onPhotoSelect(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${photo.icon} ${photo.title}",
                        style = TourOSTypography.Caption.copy(
                            color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
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
