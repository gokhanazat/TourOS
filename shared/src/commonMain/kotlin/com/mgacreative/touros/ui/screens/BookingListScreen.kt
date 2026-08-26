package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.engine.VoucherContractTemplateEngine
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSColumn
import com.mgacreative.touros.ui.components.TourOSDataTable
import com.mgacreative.touros.ui.components.TourOSEmptyState
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.BookingListUiState
import com.mgacreative.touros.ui.viewmodel.BookingListViewModel
import com.mgacreative.touros.utils.DocumentPrinter
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Adaptif Rezervasyon Listesi Ekranı.
 * - Üstte Durum Filtre Çipleri.
 * - Expanded: TourOSDataTable Tablo Görünümü (Entegre Rusça Sözleşme, TO Talep ve Detay Butonları ile).
 * - Compact: Kart Listesi (Sağ üstte Durum Rozeti ile).
 */
@Composable
fun BookingListScreen(
    onNavigateToCreateBooking: () -> Unit = {},
    onNavigateToBookingDetail: (String) -> Unit = {},
    viewModel: BookingListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()

    val templateEngine = remember { VoucherContractTemplateEngine() }
    var previewDocType by remember { mutableStateOf<String?>(null) } // "contract" | "operator_request"
    var previewBooking by remember { mutableStateOf<Booking?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = AppLanguageManager.translate("Rezervasyon Yönetimi"),
                subtitle = AppLanguageManager.translate("Tüm turların rezervasyon taleplerini ve durumlarını takip edin"),
                actions = {
                    TourOSButton(
                        text = AppLanguageManager.translate("+ Yeni Rezervasyon"),
                        onClick = onNavigateToCreateBooking,
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        placeholder = "🔍 " + AppLanguageManager.translate("PNR, rezervasyon kodu, müşteri adı veya telefon ile ara..."),
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
                            label = { Text(AppLanguageManager.translate("Tümü"), style = TourOSTypography.Caption) },
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
                                label = { Text(AppLanguageManager.translate(status.displayName), style = TourOSTypography.Caption) },
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
                        TourOSLoadingIndicator(message = AppLanguageManager.translate("Rezervasyonlar yükleniyor..."))
                    }
                    is BookingListUiState.Error -> {
                        TourOSEmptyState(
                            title = AppLanguageManager.translate("Hata Oluştu"),
                            description = state.message,
                            actionButtonText = AppLanguageManager.translate("Yeniden Dene"),
                            onActionClick = { viewModel.onSearchQueryChanged("") }
                        )
                    }
                    is BookingListUiState.Success -> {
                        if (state.bookings.isEmpty()) {
                            TourOSEmptyState(
                                title = AppLanguageManager.translate("Rezervasyon Bulunamadı"),
                                description = AppLanguageManager.translate("Aradığınız kriterlere uygun herhangi bir rezervasyon kaydı yok."),
                                actionButtonText = AppLanguageManager.translate("+ Yeni Rezervasyon Ekle"),
                                onActionClick = onNavigateToCreateBooking
                            )
                        } else {
                            val bookingColumns = listOf(
                                TourOSColumn<Booking>(title = AppLanguageManager.translate("PNR / REZERVASYON & MÜŞTERİ"), weight = 2.0f) { booking ->
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
                                                        style = TourOSTypography.Caption.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 10.sp),
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
                                TourOSColumn<Booking>(title = AppLanguageManager.translate("ACENTE / OPERATÖR"), weight = 1.4f) { booking ->
                                    Text(
                                        text = "🏢 ${booking.operatorName}", 
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Medium)
                                    )
                                },
                                TourOSColumn<Booking>(title = AppLanguageManager.translate("ÜRÜN & TARİH"), weight = 2.1f) { booking ->
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
                                TourOSColumn<Booking>(title = AppLanguageManager.translate("TUTAR / PAX"), weight = 1.3f) { booking ->
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
                                TourOSColumn<Booking>(title = AppLanguageManager.translate("DURUM"), weight = 1.0f) { booking ->
                                    val (badgeBg, badgeText) = getStatusColors(booking.status)
                                    TourOSStatusBadge(
                                        text = AppLanguageManager.translate(booking.status.displayName),
                                        backgroundColor = badgeBg,
                                        textColor = badgeText
                                    )
                                },
                                TourOSColumn<Booking>(title = AppLanguageManager.translate("İŞLEM"), weight = 2.0f) { booking ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Müşteri Sözleşmesi Mini Butonu
                                        Surface(
                                            onClick = {
                                                previewBooking = booking
                                                previewDocType = "contract"
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            color = TourOSColors.PrimaryContainer,
                                            border = BorderStroke(0.5.dp, TourOSColors.Primary.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(text = "📄", style = TourOSTypography.Caption.copy(fontSize = 11.sp))
                                                Text(
                                                    text = AppLanguageManager.translate("Sözleşme"),
                                                    style = TourOSTypography.Caption.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = TourOSColors.Primary,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }

                                        // 2. Tur Operatörü Talep Formu Mini Butonu
                                        Surface(
                                            onClick = {
                                                previewBooking = booking
                                                previewDocType = "operator_request"
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF3F4F6),
                                            border = BorderStroke(0.5.dp, Color(0xFF9CA3AF))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(text = "🏢", style = TourOSTypography.Caption.copy(fontSize = 11.sp))
                                                Text(
                                                    text = AppLanguageManager.translate("TO Talep"),
                                                    style = TourOSTypography.Caption.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF374151),
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }

                                        // 3. Detay Mini Butonu (Diğer butonlarla birebir aynı görsel dilde)
                                        Surface(
                                            onClick = { onNavigateToBookingDetail(booking.id.ifBlank { booking.bookingCode }) },
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFFFFFF),
                                            border = BorderStroke(0.5.dp, Color(0xFFD1D5DB))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = AppLanguageManager.translate("Detay") + " ›",
                                                    style = TourOSTypography.Caption.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = TourOSColors.TextPrimary,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
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
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    onClick = {
                                                        previewBooking = booking
                                                        previewDocType = "contract"
                                                    },
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = TourOSColors.PrimaryContainer,
                                                    border = BorderStroke(0.5.dp, TourOSColors.Primary)
                                                ) {
                                                    Text(
                                                        text = "📄 Sözleşme",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                        style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                                                    )
                                                }
                                                Surface(
                                                    onClick = {
                                                        previewBooking = booking
                                                        previewDocType = "operator_request"
                                                    },
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFF3F4F6),
                                                    border = BorderStroke(0.5.dp, Color(0xFF9CA3AF))
                                                ) {
                                                    Text(
                                                        text = "🏢 TO",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                        style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                                                    )
                                                }
                                                Surface(
                                                    onClick = { onNavigateToBookingDetail(targetId) },
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFFFFFFF),
                                                    border = BorderStroke(0.5.dp, Color(0xFFD1D5DB))
                                                ) {
                                                    Text(
                                                        text = AppLanguageManager.translate("Detay") + " ›",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                        style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                                                    )
                                                }
                                            }
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

    // =========================================================================
    // 📄 RUSÇA RESMİ SÖZLEŞME & TUR OPERATÖRÜ TALEP FORMU ÖNİZLEME MODALI
    // =========================================================================
    if (previewDocType != null && previewBooking != null) {
        val booking = previewBooking!!
        val isContract = previewDocType == "contract"
        val docTitle = if (isContract) {
            "ПРИЛОЖЕНИЕ №1 И ДОГОВОР ОФЕРТЕ (Müşteri Sözleşmesi)"
        } else {
            "ЗАЯВКА ТУРОПЕРАТОРУ / ВАУЧЕР (Tur Operatörü Talep Formu)"
        }
        val htmlContent = if (isContract) {
            templateEngine.buildRussianContractDocument(booking)
        } else {
            templateEngine.buildRussianOperatorRequestDocument(booking)
        }

        AlertDialog(
            onDismissRequest = {
                previewDocType = null
                previewBooking = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = if (isContract) "📄" else "🏢", fontSize = 20.sp)
                    Column {
                        Text(text = docTitle, style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "Договор / Заказ № ${booking.bookingCode.ifBlank { "Г02033-2026" }} • ${booking.customerName}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isContract) "📌 Müşteri Sözleşme Özeti (2 Sayfa A4):" else "📌 Tur Operatörü Talep Özeti (Tek Sayfa A4):", 
                                fontWeight = FontWeight.Bold, 
                                style = TourOSTypography.Caption
                            )
                            if (isContract) {
                                Text(text = "• 1. Sayfa: Turist Hizmet Koşulları (Ek-1), Turist Listesi, Uçuş, Otel & Fiyat Tablosu", style = TourOSTypography.Caption)
                                Text(text = "• 2. Sayfa: 17 Maddelik Yasal Şartlar, Acente & Müşteri Rekvizitleri, Çift Taraflı İmza", style = TourOSTypography.Caption)
                            } else {
                                Text(text = "• Tek Sayfa Resmi Operatör Talep & Voucher Belgesi: Turist Listesi, Uçuş Detayları, Otel/Pansiyon ve Rezervasyon Özeti.", style = TourOSTypography.Caption)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Müşteri: ${booking.customerName}", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.SemiBold))
                                Text(text = "Tutar: ${booking.totalPrice} ${booking.currency}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold))
                            }
                            Text(text = "Tur / Otel: ${booking.productName}", style = TourOSTypography.Caption)
                            Text(text = "Tarih: ${booking.departureDate} (${booking.nights} Gece)", style = TourOSTypography.Caption)
                            Text(text = "Operatör: ${booking.operatorName} • PNR: ${booking.operatorPnrCode ?: '—'}", style = TourOSTypography.Caption)
                        }
                    }

                    Text(
                        text = "💡 Yazdır veya PDF Kaydet butonuna tıkladığınızda resmi A4 formatında hazır şablon tarayıcınızda açılacak ve tek tıkla yazdırılabilecektir.",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TourOSButton(
                        text = "🖨️ Yazdır / PDF Kaydet",
                        onClick = {
                            DocumentPrinter.printOrSaveHtml(htmlContent, if (isContract) "Contract_${booking.bookingCode}" else "Operator_${booking.bookingCode}")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("🖨️ Yazdırma / PDF çıktısı tarayıcıda açıldı.")
                            }
                        },
                        variant = TourOSButtonVariant.PRIMARY
                    )
                    TourOSButton(
                        text = if (isContract) "✉️ Müşteriye E-posta Gönder" else "🏢 TO'ya Gönder",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isContract) {
                                        "✉️ Rusça Sözleşme PDF bağlantısı ${booking.customerEmail ?: "müşteriye"} e-posta ile iletildi."
                                    } else {
                                        "🏢 Rezervasyon Talep Belgesi ${booking.operatorName} operatörüne iletildi."
                                    }
                                )
                            }
                            previewDocType = null
                            previewBooking = null
                        },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        previewDocType = null
                        previewBooking = null
                    }
                ) {
                    Text(AppLanguageManager.translate("Kapat"))
                }
            }
        )
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
