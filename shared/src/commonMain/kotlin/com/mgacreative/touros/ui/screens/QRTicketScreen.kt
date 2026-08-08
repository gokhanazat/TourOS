package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.QRTicketViewModel

/**
 * B2C QR Bilet Ekranı — TourOS 0.3
 *
 * Ortalanmış büyük QR kod kartı.
 * Altında tur, tarih ve yolcu bilgisi detayları.
 * Ekran parlaklığını artıracak sade ve temiz beyaz zemin (Color.White / Surface).
 */
@Composable
fun QRTicketScreen(
    viewModel: QRTicketViewModel,
    bookingId: String = "b2c-101",
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val ticket = state.qrTicket

    LaunchedEffect(bookingId) {
        viewModel.loadTicket(bookingId)
    }

    // SADE BEYAZ ZEMİN (Ekran Parlaklığını Artırmak İçin Strict Rule)
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TourOSTopBar(
                title = "Dijital QR Bilet",
                subtitle = "Turnike ve otobüs geçiş kontrol bileti",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(TourOSSpacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // Bildirim Mesajı
                if (state.notificationMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
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

                // ── 1. ORTALANMIŞ BÜYÜK QR KOD KARTI ───────────────────────────
                CenteredQrCodeCard(
                    bookingCode = ticket.bookingCode.ifBlank { "B2C-TKT-9812" },
                    ticketHash = ticket.ticketHash.ifBlank { "QR-TKT-9A8B7C6D5E4F" },
                    isCheckedIn = ticket.checkinStatus == "CHECKED_IN"
                )

                // ── 2. ALTINDA TUR / TARİH / YOLCU BİLGİSİ ─────────────────────
                TicketDetailsCard(
                    passengerName = ticket.passengerName.ifBlank { "Elif Yılmaz" },
                    tourTitle = ticket.tourTitle.ifBlank { "Kapadokya VIP Balon & Vadi Turu" },
                    paxCount = if (ticket.paxCount > 0) ticket.paxCount else 2,
                    departureDate = "15 Ağustos 2026, Sa: 05:00",
                    pickupPoint = "Nevşehir Kapadokya VIP Transfer Alanı"
                )

                // ── 3. WALLET VE İNDİRME AKSİYON BUTONLARI ──────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TourOSButton(
                        text = " Apple / Google Wallet'a Ekle",
                        onClick = { },
                        variant = TourOSButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TourOSButton(
                        text = "📄 PDF Bilet İndir",
                        onClick = { },
                        variant = TourOSButtonVariant.SECONDARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ─── ORTALANMIŞ BÜYÜK QR KOD KARTI ───────────────────────────────────────────

@Composable
private fun CenteredQrCodeCard(
    bookingCode: String,
    ticketHash: String,
    isCheckedIn: Boolean
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        contentPadding = TourOSSpacing.large
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Kod: $bookingCode",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )

                TourOSStatusBadge(
                    text = if (isCheckedIn) "✅ GİRİŞ YAPILDI" else "🟢 GEÇİŞE HAZIR",
                    backgroundColor = if (isCheckedIn) TourOSColors.SuccessContainer else TourOSColors.PrimaryContainer,
                    textColor = if (isCheckedIn) TourOSColors.Success else TourOSColors.Primary
                )
            }

            // BÜYÜK YÜKSEK KONTRASTLI SAF BEYAZ ZEMİNLİ QR KOD KUTUSU (200.DP)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                    .background(Color.White)
                    .border(2.5.dp, TourOSColors.Primary, RoundedCornerShape(TourOSSpacing.cornerRadius))
                    .padding(TourOSSpacing.medium),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("████  ██  ████", style = TourOSTypography.TitleMedium.copy(fontFamily = FontFamily.Monospace, color = Color.Black))
                    Text("██  ██████  ██", style = TourOSTypography.TitleMedium.copy(fontFamily = FontFamily.Monospace, color = Color.Black))
                    Text("████  ██  ████", style = TourOSTypography.TitleMedium.copy(fontFamily = FontFamily.Monospace, color = Color.Black))
                    Text("██  ██████  ██", style = TourOSTypography.TitleMedium.copy(fontFamily = FontFamily.Monospace, color = Color.Black))

                    Text(
                        ticketHash,
                        style = TourOSTypography.Caption.copy(fontFamily = FontFamily.Monospace, color = TourOSColors.Primary),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                "📱 Lütfen turnike veya araç girişinde bu ekranı görevliye gösteriniz.",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── ALTINDA TUR / TARİH / YOLCU BİLGİSİ KARTI ────────────────────────────────

@Composable
private fun TicketDetailsCard(
    passengerName: String,
    tourTitle: String,
    paxCount: Int,
    departureDate: String,
    pickupPoint: String
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.35f),
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "📋 Bilet & Yolcu Detayları",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Yolcu Adı Soyadı:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(passengerName, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Kişi Sayısı:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("$paxCount Kişi (Pax)", style = TourOSTypography.Label.copy(color = TourOSColors.Primary))
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Tur Adı:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text(tourTitle, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Kalkış Tarihi & Saat:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(departureDate, style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Kalkış Noktası:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(pickupPoint, style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
                }
            }
        }
    }
}
