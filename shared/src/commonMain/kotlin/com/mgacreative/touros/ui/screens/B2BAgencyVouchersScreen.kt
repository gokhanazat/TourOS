package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.B2BAgencyVoucherItem
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2BAgencyVouchersViewModel

/**
 * B2B Voucher Yazdırma & İndirme Ekranı — TourOS 0.3
 *
 * Ortada Kağıt-Benzeri Kart İçinde Canlı Voucher Önizlemesi.
 * Altta '📄 İndir' ve '🖨️ Yazdır' Primary/Secondary Butonları.
 */
@Composable
fun B2BAgencyVouchersScreen(
    viewModel: B2BAgencyVouchersViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var selectedVoucherId by remember { mutableStateOf<String?>(null) }

    val activeVoucher = remember(state.vouchers, selectedVoucherId) {
        state.vouchers.find { it.voucherId == selectedVoucherId } ?: state.vouchers.firstOrNull()
    }


    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "B2B Voucher Önizleme & Yazdırma",
                subtitle = "Acente seyahat belgesi belgelendirme ve döküm",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val isExpanded = maxWidth >= 768.dp

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // Bildirim Mesajı
                if (state.notificationMessage != null) {
                    item {
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
                }

                // ── 1. VOUCHER SEÇİM CHIP ÇUBUĞU ──────────────────────────────
                item {
                    Text(
                        "🎟️ Yazdırılacak Voucher Belgesi Seçin",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.vouchers) { v ->
                            FilterChip(
                                selected = activeVoucher?.voucherId == v.voucherId,
                                onClick = { selectedVoucherId = v.voucherId },
                                label = { Text("${v.bookingCode} - ${v.guestName}", style = TourOSTypography.Caption) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TourOSColors.Primary,
                                    selectedLabelColor = TourOSColors.OnPrimary
                                )
                            )
                        }

                    }
                }

                // ── 2. ORTADA KAĞIT-BENZERİ KART İÇİNDE VOUCHER ÖNİZLEMESİ ───────
                if (activeVoucher != null) {
                    item {
                        PaperVoucherPreviewCard(voucher = activeVoucher)
                    }

                    // ── 3. ALTTA 'İNDİR' VE 'YAZDIR' PRIMARY / SECONDARY BUTONLARI ──
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            // PRIMARY BUTON: PDF İNDİR
                            TourOSButton(
                                text = "📄 PDF İndir",
                                onClick = {
                                    viewModel.setSearchQuery("")
                                },
                                variant = TourOSButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1f)
                            )

                            // SECONDARY BUTON: YAZDIR
                            TourOSButton(
                                text = "🖨️ Yazdır",
                                onClick = {
                                    viewModel.printVoucher(activeVoucher)
                                },
                                variant = TourOSButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else if (state.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TourOSColors.Primary)
                        }
                    }
                }
            }
        }
    }
}

// ─── KAĞIT-BENZERİ KART İÇİNDE VOUCHER ÖNİZLEMESİ (A4 PAPER MOCKUP) ─────────

@Composable
private fun PaperVoucherPreviewCard(voucher: B2BAgencyVoucherItem) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.3f),
        contentPadding = TourOSSpacing.large
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            Text(
                "🖨️ A4 Kağıt Belge Önizlemesi",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            // DÜZ BEYAZ KAĞIT ARKA PLANLI VOUCHER BELGESİ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(Color.White)
                    .border(1.dp, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .padding(TourOSSpacing.large)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    // Header: Acente Logo & Belge Tipi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                "TourOS B2B Partner Network",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                            )
                            Text(
                                "Düzenleyen Acente: Global Travel Agency A.Ş.",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "SEYAHAT VOUCHER'I",
                                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                            )
                            Text(
                                "Ref: ${voucher.bookingCode}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
                            )
                        }
                    }

                    HorizontalDivider(color = TourOSColors.Primary, thickness = 2.dp)

                    // Misafir & Konaklama Bilgileri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                .background(TourOSColors.PrimaryContainer.copy(alpha = 0.3f))
                                .padding(TourOSSpacing.medium)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("👤 MİSAFİR ADI SOYADI:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                Text(voucher.guestName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                Text("Yolcu Sayısı: ${voucher.paxCount} Kişi (Pax)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                .background(TourOSColors.SecondaryContainer.copy(alpha = 0.3f))
                                .padding(TourOSSpacing.medium)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("🏨 OTEL & KONAKLAMA:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                Text(voucher.hotelName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary))
                                Text("Tarih: ${voucher.departureDate}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            }
                        }
                    }

                    // Tur Programı Detayı
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .padding(TourOSSpacing.medium)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🏔️ Tur Programı:", style = TourOSTypography.Label.copy(color = TourOSColors.Primary))
                            Text(voucher.tourTitle, style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
                            Text("Havalimanı Transferi: Dahil (Karşılama İsimliği: ${voucher.guestName})", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                    }

                    // QR Kod & Barkod Mockup'ı
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Barkod / QR Onay Kodu:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            Text(
                                "||||| |||| |||||| |||||||",
                                style = TourOSTypography.TitleLarge.copy(fontFamily = FontFamily.Monospace, color = TourOSColors.TextPrimary)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(TourOSColors.PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("QR", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                        }
                    }
                }
            }
        }
    }
}
