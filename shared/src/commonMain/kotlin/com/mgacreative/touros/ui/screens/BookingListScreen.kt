package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSColumn
import com.mgacreative.touros.ui.components.TourOSDataTable
import com.mgacreative.touros.ui.components.TourOSEmptyState
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.BookingListUiState
import com.mgacreative.touros.ui.viewmodel.BookingListViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Adaptif Rezervasyon Listesi Ekranı.
 * - Üstte Durum Filtre Çipleri (TourOSStatusBadge renk uyumlu).
 * - Expanded: TourOSDataTable Tablo Görünümü.
 * - Compact: Kart Listesi (Sağ üstte Durum Rozeti ile).
 */
@Composable
fun BookingListScreen(
    onNavigateToCreateBooking: () -> Unit = {},
    onNavigateToBookingDetail: (String) -> Unit = {},
    viewModel: BookingListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rezervasyon Yönetimi"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tüm turların rezervasyon taleplerini ve durumlarını takip edin"),
                actions = {
                    TourOSButton(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("+ Yeni Rezervasyon"),
                        onClick = onNavigateToCreateBooking,
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            )
        },
        containerColor = TourOSColors.Surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val successState = uiState as? BookingListUiState.Success

            // Ultra Kompakt Arama ve Durum Filtre Çubuğu
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = TourOSColors.Background,
                border = BorderStroke(1.dp, TourOSColors.Border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TourOSTextField(
                        value = successState?.searchQuery ?: "",
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = "🔍 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("PNR, rezervasyon kodu, müşteri adı veya telefon ile ara..."),
                        modifier = Modifier.weight(1f)
                    )

                    // Yatay Kompakt Filtre Butonları
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val selectedStatus = successState?.selectedStatusFilter
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { viewModel.onStatusFilterSelected(null) },
                            label = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tümü"), style = TourOSTypography.Caption) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                selectedLabelColor = TourOSColors.Primary
                            )
                        )

                        BookingStatus.entries.forEach { status ->
                            val isSelected = selectedStatus == status
                            val (badgeBg, badgeText) = getStatusColors(status)

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onStatusFilterSelected(status) },
                                label = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate(status.displayName), style = TourOSTypography.Caption) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = badgeBg,
                                    selectedLabelColor = badgeText
                                )
                            )
                        }
                    }
                }
            }

            // Adaptif Tablo / Kart Listesi Görünümü
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val isCompact = maxWidth < 768.dp

                when (val state = uiState) {
                    is BookingListUiState.Loading -> {
                        TourOSLoadingIndicator(message = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rezervasyonlar yükleniyor..."))
                    }
                    is BookingListUiState.Error -> {
                        TourOSEmptyState(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hata Oluştu"),
                            description = state.message,
                            actionButtonText = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Yeniden Dene"),
                            onActionClick = { viewModel.onSearchQueryChanged("") }
                        )
                    }
                    is BookingListUiState.Success -> {
                        if (state.bookings.isEmpty()) {
                            TourOSEmptyState(
                                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rezervasyon Bulunamadı"),
                                description = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Aradığınız kriterlere uygun herhangi bir rezervasyon kaydı yok."),
                                actionButtonText = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("+ Yeni Rezervasyon Ekle"),
                                onActionClick = onNavigateToCreateBooking
                            )
                        } else {
                            val bookingColumns = listOf(
                                TourOSColumn<Booking>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("PNR / REZERVASYON & MÜŞTERİ"), weight = 2.2f) { booking ->
                                    val hasPnr = !booking.operatorPnrCode.isNullOrBlank()
                                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = if (hasPnr) "✈️ ${booking.operatorPnrCode}" else booking.bookingCode, 
                                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                                            )
                                            if (hasPnr) {
                                                Surface(
                                                    shape = RoundedCornerShape(3.dp),
                                                    color = Color(0xFFECFDF5),
                                                    border = BorderStroke(0.5.dp, Color(0xFF10B981))
                                                ) {
                                                    Text(
                                                        text = "✓ PNR",
                                                        style = TourOSTypography.Caption.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = booking.customerName, 
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                            )
                                            if (!booking.customerPhone.isNullOrBlank()) {
                                                Text(
                                                    text = "· 📞 ${booking.customerPhone}", 
                                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                                )
                                            }
                                        }
                                    }
                                },
                                TourOSColumn<Booking>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("ACENTE / OPERATÖR"), weight = 1.6f) { booking ->
                                    Text(
                                        text = "🏢 ${booking.operatorName}", 
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Medium)
                                    )
                                },
                                TourOSColumn<Booking>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("ÜRÜN & TARİH"), weight = 2.4f) { booking ->
                                    val isHotel = booking.bookingType == "HOTEL" || !booking.hotelId.isNullOrBlank()
                                    val icon = if (isHotel) "🏨" else "🚌"
                                    val dateInfo = if (isHotel && !booking.checkInDate.isNullOrBlank()) {
                                        "${booking.checkInDate} (${booking.nights}G)"
                                    } else {
                                        "${booking.departureDate}"
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                        Text(
                                            text = "$icon ${booking.productName}", 
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.SemiBold),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "📅 $dateInfo", 
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                                        )
                                    }
                                },
                                TourOSColumn<Booking>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("TUTAR / PAX"), weight = 1.4f) { booking ->
                                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                        Text(
                                            text = "${booking.totalPrice} ${booking.currency}", 
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "👥 ${booking.paxCount} Kişi", 
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
                                    }
                                },
                                TourOSColumn<Booking>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("DURUM"), weight = 1.1f) { booking ->
                                    val (badgeBg, badgeText) = getStatusColors(booking.status)
                                    TourOSStatusBadge(
                                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(booking.status.displayName),
                                        backgroundColor = badgeBg,
                                        textColor = badgeText
                                    )
                                },
                                TourOSColumn<Booking>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İŞLEM"), weight = 0.9f) { booking ->
                                    TourOSButton(
                                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Detay ›"),
                                        onClick = { onNavigateToBookingDetail(booking.id.ifBlank { booking.bookingCode }) },
                                        variant = TourOSButtonVariant.TERTIARY
                                    )
                                }
                            )

                            TourOSDataTable(
                                items = state.bookings,
                                columns = bookingColumns,
                                isCompact = isCompact,
                                dense = true,
                                modifier = Modifier.fillMaxSize(),
                                onItemClick = { onNavigateToBookingDetail(it.id.ifBlank { it.bookingCode }) },
                                compactCardContent = { booking ->
                                    val targetId = booking.id.ifBlank { booking.bookingCode }
                                    val isHotel = booking.bookingType == "HOTEL" || !booking.hotelId.isNullOrBlank()
                                    val icon = if (isHotel) "🏨" else "📍"
                                    val hasPnr = !booking.operatorPnrCode.isNullOrBlank()
                                    val dateText = if (isHotel && !booking.checkInDate.isNullOrBlank()) {
                                        "📅 Giriş: ${booking.checkInDate} (${booking.nights} Gece)"
                                    } else {
                                        "📅 Kalkış: ${booking.departureDate}"
                                    }

                                    // COMPACT MOBİL KART (Durum Rozeti Sağ Üstte)
                                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(
                                                        text = if (hasPnr) "✈️ ${booking.operatorPnrCode}" else booking.bookingCode, 
                                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                                                    )
                                                    if (hasPnr) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color(0xFFECFDF5),
                                                            border = BorderStroke(1.dp, Color(0xFF10B981))
                                                        ) {
                                                            Text(
                                                                text = "✓ Kilitli",
                                                                style = TourOSTypography.Caption.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (hasPnr && booking.bookingCode != booking.operatorPnrCode) {
                                                    Text(text = "Ref: #${booking.bookingCode}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                                }
                                                Text(text = booking.customerName, style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary))
                                            }

                                            // DURUM ROZETİ SAĞ ÜSTTE
                                            val (badgeBg, badgeText) = getStatusColors(booking.status)
                                            TourOSStatusBadge(
                                                text = booking.status.displayName,
                                                backgroundColor = badgeBg,
                                                textColor = badgeText
                                            )
                                        }

                                        Text(
                                            text = "🏢 ${booking.operatorName}  •  $icon ${booking.productName}",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Text(
                                            text = "$dateText  •  📞 ${booking.customerPhone ?: '-'}  •  👥 ${booking.paxCount} Kişi",
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${booking.totalPrice} ${booking.currency}",
                                                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                                            )
                                            TourOSButton(
                                                text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Detay") + " ›",
                                                onClick = { onNavigateToBookingDetail(targetId) },
                                                variant = TourOSButtonVariant.SECONDARY
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getStatusColors(status: BookingStatus): Pair<Color, Color> {
    return when (status) {
        BookingStatus.BEKLIYOR -> TourOSColors.WarningContainer to TourOSColors.Warning
        BookingStatus.OPSIYON -> TourOSColors.PrimaryContainer to TourOSColors.Primary
        BookingStatus.ONAYLANDI -> TourOSColors.SuccessContainer to TourOSColors.Success
        BookingStatus.IPTAL -> TourOSColors.ErrorContainer to TourOSColors.Error
        BookingStatus.TAMAMLANDI -> TourOSColors.Surface to TourOSColors.TextDisabled
    }
}
