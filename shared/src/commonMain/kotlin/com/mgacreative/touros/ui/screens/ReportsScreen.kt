package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.DatePreset
import com.mgacreative.touros.ui.viewmodel.ReportType
import com.mgacreative.touros.ui.viewmodel.ReportsViewModel

/**
 * 📊 RAPORLAR & ANALİTİK MERKEZİ — TourOS
 *
 * Gelişmiş Filtre Header'ı (Tarih, Rapor Tipi, Tur Operatörü, Durum, Arama).
 * Dışa Aktarma Aksiyonları (CSV, PDF, Yazdır - Print).
 * Web/Desktop/Android/iOS platformlarında 100% aynı premium tasarım dili.
 */
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Operasyonel & Finansal Raporlar"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ciro, kârlılık, doluluk ve operasyonel performans grafikleri"),
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // ── BİLDİRİM BANNERİ ──────────────────────────────────────────────
            if (state.notificationMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TourOSColors.PrimaryContainer)
                        .padding(TourOSSpacing.medium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.notificationMessage!!,
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Primary),
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.clearNotification() }) {
                            Text("✕", style = TourOSTypography.Label.copy(color = TourOSColors.Primary))
                        }
                    }
                }
            }

            // ── GELİŞMİŞ FİLTRE HEADER'I VE DIŞA AKTARMA AKSİYONLARI ─────────
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Primary.copy(alpha = 0.3f),
                contentPadding = TourOSSpacing.large
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    // Header Başlık & Aktarım Butonları
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("⚡ Gelişmiş Rapor Filtreleme Header'ı"),
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İstediğiniz tarih, ürün, tur operatörü veya duruma göre anlık rapor oluşturun"),
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }

                        // Dışa Aktar Buton Grubu
                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            TourOSButton(
                                text = "📄 CSV",
                                onClick = { viewModel.exportToCsv() },
                                variant = TourOSButtonVariant.SECONDARY
                            )
                            TourOSButton(
                                text = "📑 PDF",
                                onClick = { viewModel.generatePdfReport() },
                                variant = TourOSButtonVariant.SECONDARY
                            )
                            TourOSButton(
                                text = "🖨️ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Yazdır")}",
                                onClick = { viewModel.triggerPrint() },
                                variant = TourOSButtonVariant.PRIMARY
                            )
                        }
                    }

                    HorizontalDivider(color = TourOSColors.Border)

                    // 1. RAPOR TİPİ SEÇİMİ (Kategoriler)
                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rapor Türü Seçimi:"), style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            ReportType.entries.forEach { type ->
                                val isSelected = state.reportType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setReportType(type) },
                                    label = { Text("${type.icon} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(type.label)}", style = TourOSTypography.BodyMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TourOSColors.PrimaryContainer,
                                        selectedLabelColor = TourOSColors.Primary
                                    )
                                )
                            }
                        }
                    }

                    // 2. TARİH HIZLI SEÇENEKLERİ & BAŞLANGIÇ/BİTİŞ
                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tarih Aralığı Filtresi:"), style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DatePreset.entries.forEach { preset ->
                                val isSelected = state.datePreset == preset
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setDatePreset(preset) },
                                    label = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate(preset.label), style = TourOSTypography.Label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TourOSColors.PrimaryContainer,
                                        selectedLabelColor = TourOSColors.Primary
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            TourOSTextField(
                                value = state.startDate,
                                onValueChange = { viewModel.setCustomDates(it, state.endDate) },
                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Başlangıç Tarihi"),
                                placeholder = "YYYY-MM-DD",
                                modifier = Modifier.weight(1f)
                            )
                            TourOSTextField(
                                value = state.endDate,
                                onValueChange = { viewModel.setCustomDates(state.startDate, it) },
                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Bitiş Tarihi"),
                                placeholder = "YYYY-MM-DD",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 3. TUR OPERATÖRÜ, REZERVASYON DURUMU & ANLIK ARAMA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        TourOSDropdown(
                            items = state.availableOperators,
                            selectedItem = state.selectedOperator,
                            onItemSelected = { viewModel.setOperatorFilter(it) },
                            itemLabel = { "💼 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(it)}" },
                            label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tur Operatörü"),
                            modifier = Modifier.weight(1f)
                        )

                        TourOSDropdown(
                            items = state.availableStatuses,
                            selectedItem = state.selectedStatus,
                            onItemSelected = { viewModel.setStatusFilter(it) },
                            itemLabel = { "📌 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(it)}" },
                            label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rezervasyon Durumu"),
                            modifier = Modifier.weight(1f)
                        )

                        TourOSTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Anlık Arama"),
                            placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri, kod, ürün..."),
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }

            // ── ÖZET METRİK KARTLARI (SUMMARY KPI) ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                MetricSummaryCard(
                    title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Rezervasyon"),
                    value = "${state.totalBookingCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Adet")}",
                    icon = "📋",
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hacim / Gece / Pax"),
                    value = "${state.totalQuantityCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Birim")}",
                    icon = "📊",
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Ciro"),
                    value = "${formatCurrency(state.totalRevenue)} ₺",
                    icon = "💰",
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ort. Rezervasyon Tutarı"),
                    value = "${formatCurrency(state.averageRevenue)} ₺",
                    icon = "📈",
                    modifier = Modifier.weight(1f)
                )
            }

            // ── CANLI RAPOR VERİ TABLOSU ────────────────────────────────────
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(
                        text = "📋 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Filtrelenmiş Rapor Veri Listesi")} (${state.filteredBookings.size} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kayıt")})",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )

                    if (state.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TourOSColors.Primary)
                        }
                    } else if (state.filteredBookings.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Seçilen filtrelere uygun rapor kaydı bulunamadı."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                        }
                    } else {
                        // Rapor Başlık Kolonları
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TourOSColors.PrimaryContainer.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kod"), modifier = Modifier.weight(1f), style = TourOSTypography.Label, fontWeight = FontWeight.Bold)
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("TO PNR"), modifier = Modifier.weight(1f), style = TourOSTypography.Label, fontWeight = FontWeight.Bold)
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tür / Operatör"), modifier = Modifier.weight(1.4f), style = TourOSTypography.Label, fontWeight = FontWeight.Bold)
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri"), modifier = Modifier.weight(1.4f), style = TourOSTypography.Label, fontWeight = FontWeight.Bold)
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tarih / Gece"), modifier = Modifier.weight(1.1f), style = TourOSTypography.Label, fontWeight = FontWeight.Bold)
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tutar"), modifier = Modifier.weight(1f), style = TourOSTypography.Label, fontWeight = FontWeight.Bold)
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Durum"), modifier = Modifier.weight(1f), style = TourOSTypography.Label, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = TourOSColors.Border)

                        state.filteredBookings.forEach { booking ->
                            ReportDataRowItem(booking = booking)
                            HorizontalDivider(color = TourOSColors.Border.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    icon: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    TourOSCard(
        modifier = modifier,
        backgroundColor = if (highlight) TourOSColors.PrimaryContainer else TourOSColors.Surface,
        borderColor = if (highlight) TourOSColors.Primary else TourOSColors.Border,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
                Text(icon, style = TourOSTypography.TitleMedium)
            }
            Text(
                text = value,
                style = TourOSTypography.TitleMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlight) TourOSColors.Primary else TourOSColors.TextPrimary
            )
        }
    }
}

@Composable
private fun ReportDataRowItem(booking: Booking) {
    val isHotel = booking.bookingType == "HOTEL"
    val icon = if (isHotel) "🏨" else "🚌"
    val typeName = if (isHotel) com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Otel") else com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tur")
    val isOwnProduct = booking.operatorName.isNullOrBlank() || booking.operatorName?.contains("MGA", ignoreCase = true) == true
    val rawOp = booking.operatorName
    val operatorStr = if (isOwnProduct) com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kendi Ürünümüz") else (if (rawOp != null) com.mgacreative.touros.ui.localization.AppLanguageManager.translate(rawOp) else "-")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TourOSSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kod
        Text(
            text = booking.bookingCode,
            modifier = Modifier.weight(1f),
            style = TourOSTypography.Label,
            fontWeight = FontWeight.Bold,
            color = TourOSColors.Primary
        )

        // TO PNR Kodu
        Box(modifier = Modifier.weight(1f)) {
            val pnrText = booking.operatorPnrCode?.takeIf { it.isNotBlank() } ?: "-"
            Text(
                text = pnrText,
                style = TourOSTypography.Label.copy(
                    color = if (pnrText != "-") TourOSColors.Success else TourOSColors.TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Tür / Operatör
        Column(modifier = Modifier.weight(1.4f)) {
            Text("$icon $typeName", style = TourOSTypography.Label, fontWeight = FontWeight.Bold)
            Text("💼 $operatorStr", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
        }

        // Müşteri
        Column(modifier = Modifier.weight(1.4f)) {
            Text(booking.customerName.takeIf { it.isNotBlank() } ?: com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri"), style = TourOSTypography.Label, fontWeight = FontWeight.Bold)
            Text(booking.customerPhone?.takeIf { it.isNotBlank() } ?: "-", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
        }

        // Tarih / Gece
        Column(modifier = Modifier.weight(1.1f)) {
            Text(booking.checkInDate ?: booking.departureDate ?: "-", style = TourOSTypography.Label)
            Text(if (isHotel) "${booking.nights} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gece")}" else "${booking.paxCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kişi")}", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
        }

        // Tutar
        Text(
            text = "${formatCurrency(booking.totalPrice)} ₺",
            modifier = Modifier.weight(1f),
            style = TourOSTypography.Label,
            fontWeight = FontWeight.Bold,
            color = TourOSColors.Primary
        )

        // Durum
        Box(modifier = Modifier.weight(1f)) {
            TourOSStatusBadge(text = booking.status.displayName)
        }
    }
}

private fun formatCurrency(value: Double): String {
    val longVal = value.toLong()
    return if (value % 1.0 == 0.0) {
        longVal.toString()
    } else {
        val whole = (value.toInt()).toString()
        val decimal = ((value - value.toInt()) * 100).toInt()
        "$whole.${if (decimal < 10) "0$decimal" else decimal}"
    }
}
