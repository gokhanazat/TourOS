package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * 📊 RAPORLAR & ANALİTİK MERKEZİ — TourOS (Ultra-Kompakt ERP Sürümü)
 *
 * - 2 Satırlık İnce Filtre ve Toolbar Alanı (Rapor Türü Çipleri + Dışa Aktarma + Tarih/Operatör/Durum/Arama).
 * - Tek Satırlık İnce KPI Özet Şeridi.
 * - TourOSDataTable(dense = true) ile satır araları daraltılmış 18-20 kayıt kapasiteli veri tablosu.
 */
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Operasyonel & Finansal Raporlar"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ciro, kârlılık, doluluk ve tur operatörü performans dökümleri"),
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isCompact = maxWidth < 768.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.small),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                // ── BİLDİRİM BANNERİ ──────────────────────────────────────────────
                if (state.notificationMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TourOSColors.PrimaryContainer)
                            .padding(horizontal = TourOSSpacing.medium, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.notificationMessage!!,
                                style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { viewModel.clearNotification() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("✕", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                            }
                        }
                    }
                }

                // ── 1. KOMPAKT 2 SATIRLI FİLTRE VE AKSİYON TOOLBAR'I ─────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TourOSColors.Background),
                    border = androidx.compose.foundation.BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border),
                    shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = TourOSSpacing.medium, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // SATIR 1: Rapor Türü Çipleri + Dışa Aktarma Butonları
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rapor Türü Çipleri
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ReportType.entries.forEach { type ->
                                    val isSelected = state.reportType == type
                                    val chipBg = if (isSelected) TourOSColors.Primary else TourOSColors.Surface
                                    val chipText = if (isSelected) Color.White else TourOSColors.TextPrimary
                                    val chipBorder = if (isSelected) TourOSColors.Primary else TourOSColors.Border

                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(1.dp, chipBorder, RoundedCornerShape(14.dp))
                                            .clickable { viewModel.setReportType(type) },
                                        color = chipBg
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(type.icon, style = TourOSTypography.Caption)
                                            Text(
                                                text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(type.label),
                                                style = TourOSTypography.Caption,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = chipText
                                            )
                                        }
                                    }
                                }
                            }

                            // Dışa Aktarma Butonları
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { viewModel.exportToCsv() },
                                    color = TourOSColors.Surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Border)
                                ) {
                                    Text(
                                        text = "📄 CSV",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = TourOSTypography.Caption,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.TextPrimary
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { viewModel.generatePdfReport() },
                                    color = TourOSColors.Surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Border)
                                ) {
                                    Text(
                                        text = "📑 PDF",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = TourOSTypography.Caption,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.TextPrimary
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { viewModel.triggerPrint() },
                                    color = TourOSColors.PrimaryContainer
                                ) {
                                    Text(
                                        text = "🖨️ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Yazdır")}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = TourOSTypography.Caption,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.Primary
                                    )
                                }
                            }
                        }

                        // SATIR 2: Tarih Presetleri, Tur Operatörü, Durum ve Arama
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tarih Presetleri
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DatePreset.entries.forEach { preset ->
                                    val isSelected = state.datePreset == preset
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { viewModel.setDatePreset(preset) },
                                        color = if (isSelected) TourOSColors.PrimaryContainer else Color.Transparent
                                    ) {
                                        Text(
                                            text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(preset.label),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            style = TourOSTypography.Caption,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                                        )
                                    }
                                }
                            }

                            // Operatör Seçimi
                            TourOSDropdown(
                                items = state.availableOperators,
                                selectedItem = state.selectedOperator,
                                onItemSelected = { viewModel.setOperatorFilter(it) },
                                itemLabel = { "💼 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(it)}" },
                                modifier = Modifier.width(180.dp)
                            )

                            // Durum Seçimi
                            TourOSDropdown(
                                items = state.availableStatuses,
                                selectedItem = state.selectedStatus,
                                onItemSelected = { viewModel.setStatusFilter(it) },
                                itemLabel = { "📌 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(it)}" },
                                modifier = Modifier.width(130.dp)
                            )

                            // Anlık Arama
                            TourOSTextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("🔍 Müşteri, PNR, ürün ara..."),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── 2. TEK SATIRLIK İNCE ÖZET ŞERİDİ (KPI STRIP) ─────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.4f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Primary.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = TourOSSpacing.medium, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rezervasyon")}: ${state.totalBookingCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Adet")}",
                                style = TourOSTypography.Caption,
                                fontWeight = FontWeight.Bold,
                                color = TourOSColors.TextPrimary
                            )
                            Text(
                                text = "📊 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hacim")}: ${state.totalQuantityCount} Pax/${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gece")}",
                                style = TourOSTypography.Caption,
                                color = TourOSColors.TextSecondary
                            )
                            Text(
                                text = "📈 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ort. Tutar")}: ₺ ${formatCurrency(state.averageRevenue)}",
                                style = TourOSTypography.Caption,
                                color = TourOSColors.TextSecondary
                            )
                        }

                        Text(
                            text = "💰 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Ciro")}: ₺ ${formatCurrency(state.totalRevenue)}",
                            style = TourOSTypography.BodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TourOSColors.Primary
                        )
                    }
                }

                // ── 3. ULTRA-KOMPAKT RAPOR ERP TABLOSU ──────────────────────────
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                } else {
                    val reportColumns = listOf(
                        TourOSColumn<Booking>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kod / TO PNR"),
                            weight = 1.6f,
                            cellContent = { booking ->
                                val hasPnr = !booking.operatorPnrCode.isNullOrBlank()
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = booking.bookingCode,
                                        style = TourOSTypography.BodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.Primary
                                    )
                                    if (hasPnr) {
                                        Text(
                                            text = "✈️ ${booking.operatorPnrCode}",
                                            style = TourOSTypography.Caption,
                                            fontWeight = FontWeight.Bold,
                                            color = TourOSColors.Success
                                        )
                                    }
                                }
                            }
                        ),
                        TourOSColumn<Booking>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tür / Ürün / Operatör"),
                            weight = 2.4f,
                            cellContent = { booking ->
                                val isHotel = booking.bookingType == "HOTEL"
                                val icon = if (isHotel) "🏨" else "🚌"
                                val isOwnProduct = booking.operatorName.isNullOrBlank() || booking.operatorName.contains("MGA", ignoreCase = true)
                                val operatorStr = if (isOwnProduct) com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kendi Ürünümüz") else booking.operatorName

                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = "$icon ${booking.productName.takeIf { it.isNotBlank() } ?: (if (isHotel) com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Otel") else com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tur"))}",
                                        style = TourOSTypography.BodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "💼 $operatorStr",
                                        style = TourOSTypography.Caption,
                                        color = TourOSColors.TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        ),
                        TourOSColumn<Booking>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri"),
                            weight = 2.0f,
                            cellContent = { booking ->
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = booking.customerName.takeIf { it.isNotBlank() } ?: "Müşteri",
                                        style = TourOSTypography.BodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.TextPrimary,
                                        maxLines = 1
                                    )
                                    if (!booking.customerPhone.isNullOrBlank()) {
                                        Text(
                                            text = "📞 ${booking.customerPhone}",
                                            style = TourOSTypography.Caption,
                                            color = TourOSColors.TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        ),
                        TourOSColumn<Booking>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tarih / Süre"),
                            weight = 1.4f,
                            cellContent = { booking ->
                                val isHotel = booking.bookingType == "HOTEL"
                                val dateStr = booking.checkInDate ?: booking.departureDate ?: "-"
                                val durationStr = if (isHotel) "${booking.nights} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gece")}" else "${booking.paxCount} Pax"

                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = dateStr,
                                        style = TourOSTypography.BodyMedium,
                                        color = TourOSColors.TextPrimary
                                    )
                                    Text(
                                        text = durationStr,
                                        style = TourOSTypography.Caption,
                                        color = TourOSColors.TextSecondary
                                    )
                                }
                            }
                        ),
                        TourOSColumn<Booking>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tutar"),
                            weight = 1.3f,
                            cellContent = { booking ->
                                Text(
                                    text = "₺ ${formatCurrency(booking.totalPrice)}",
                                    style = TourOSTypography.BodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TourOSColors.Primary
                                )
                            }
                        ),
                        TourOSColumn<Booking>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Durum"),
                            weight = 1.2f,
                            cellContent = { booking ->
                                TourOSStatusBadge(
                                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(booking.status.displayName),
                                    backgroundColor = if (booking.status.name == "CONFIRMED" || booking.status.name == "ONAYLANDI") TourOSColors.SuccessContainer else TourOSColors.PrimaryContainer,
                                    textColor = if (booking.status.name == "CONFIRMED" || booking.status.name == "ONAYLANDI") TourOSColors.Success else TourOSColors.Primary
                                )
                            }
                        )
                    )

                    TourOSDataTable(
                        items = state.filteredBookings,
                        columns = reportColumns,
                        isCompact = isCompact,
                        dense = true,
                        modifier = Modifier.fillMaxSize(),
                        compactCardContent = { booking ->
                            val isHotel = booking.bookingType == "HOTEL"
                            val icon = if (isHotel) "🏨" else "🚌"
                            val hasPnr = !booking.operatorPnrCode.isNullOrBlank()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(TourOSSpacing.small),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = if (hasPnr) "✈️ ${booking.operatorPnrCode}" else booking.bookingCode,
                                            style = TourOSTypography.BodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TourOSColors.Primary
                                        )
                                        TourOSStatusBadge(text = booking.status.displayName)
                                    }
                                    Text(
                                        text = "$icon ${booking.productName} · ${booking.customerName}",
                                        style = TourOSTypography.Caption,
                                        color = TourOSColors.TextPrimary
                                    )
                                }
                                Text(
                                    text = "₺ ${formatCurrency(booking.totalPrice)}",
                                    style = TourOSTypography.BodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TourOSColors.Primary
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun formatCurrency(value: Double): String {
    return com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(value, decimals = false)
}
