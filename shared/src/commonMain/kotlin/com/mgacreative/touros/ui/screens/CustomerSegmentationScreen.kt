package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.CustomerCrmDetail
import com.mgacreative.touros.ui.viewmodel.CustomerSegmentationViewModel

/**
 * TourOS B2B Müşteri CRM & Segmentasyon Ekranı
 *
 * - Ultra-kompakt ERP Tablosu (TourOSDataTable dense=true): Ekranda 18-20 müşteri tek bakışta görünür.
 * - Tek satırlık dinamik Segment Filtre Çipleri.
 * - Anlık isim, telefon ve e-posta araması.
 * - Tek tıkla WhatsApp ve telefonla arama aksiyonları.
 * - Müşteri Geçmişi ve Rezervasyon Dökümü Modalı.
 */
@Composable
fun CustomerSegmentationScreen(
    viewModel: CustomerSegmentationViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    var selectedCustomerForDetail by remember { mutableStateOf<CustomerCrmDetail?>(null) }

    val selectedSegment = state.segments.find { it.id == state.selectedSegmentId } ?: state.segments.firstOrNull()

    val filteredCustomers = remember(selectedSegment, state.searchQuery) {
        val list = selectedSegment?.customers ?: emptyList()
        val q = state.searchQuery.trim().lowercase()
        if (q.isBlank()) list
        else list.filter {
            it.name.lowercase().contains(q) ||
            it.email.lowercase().contains(q) ||
            it.phone.lowercase().contains(q)
        }
    }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri CRM & Sadakat Yönetimi"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kayıtlı müşteriler, harcama analizleri (LTV) ve rezervasyon geçmişi"),
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
                    .padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // ── 1. ÜST KONTROL BARI: ARAMA & SEGMENT ÇİPLERİ ─────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TourOSColors.Background),
                    border = androidx.compose.foundation.BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border),
                    shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = TourOSSpacing.medium, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Arama Kutusu
                        TourOSTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("🔍 İsim, telefon veya e-posta ara..."),
                            modifier = Modifier.width(if (isCompact) 180.dp else 260.dp)
                        )

                        // Yatay Kaydırılabilir Segment Filtre Çipleri
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            state.segments.forEach { segment ->
                                val isSelected = segment.id == state.selectedSegmentId
                                val chipBg = if (isSelected) TourOSColors.Primary else TourOSColors.Surface
                                val chipText = if (isSelected) Color.White else TourOSColors.TextPrimary
                                val chipBorder = if (isSelected) TourOSColors.Primary else TourOSColors.Border

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, chipBorder, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.selectSegment(segment.id) },
                                    color = chipBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(segment.icon, style = TourOSTypography.Caption)
                                        Text(
                                            text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(segment.name),
                                            style = TourOSTypography.Caption,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = chipText
                                        )
                                        Text(
                                            text = "(${segment.customerCount})",
                                            style = TourOSTypography.Caption,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else TourOSColors.TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Toplam Sayıcı Rozeti
                        TourOSStatusBadge(
                            text = "${filteredCustomers.size} / ${state.totalCustomerCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri")}",
                            backgroundColor = TourOSColors.PrimaryContainer,
                            textColor = TourOSColors.Primary
                        )
                    }
                }

                // ── 2. ULTRA-KOMPAKT MÜŞTERİ ERP TABLOSU ──────────────────────────────
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                } else {
                    val customerColumns = listOf(
                        TourOSColumn<CustomerCrmDetail>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri"),
                            weight = 2.2f,
                            cellContent = { customer ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (customer.totalBookings >= 3 || customer.ltvAmount >= 20000.0) TourOSColors.PrimaryContainer
                                                else TourOSColors.Surface
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = customer.name.take(1).uppercase(),
                                            style = TourOSTypography.Caption,
                                            fontWeight = FontWeight.Bold,
                                            color = TourOSColors.Primary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = customer.name,
                                            style = TourOSTypography.BodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TourOSColors.TextPrimary,
                                            maxLines = 1
                                        )
                                        if (customer.totalBookings >= 3 || customer.ltvAmount >= 20000.0) {
                                            Text("👑", style = TourOSTypography.Caption)
                                        } else if (customer.totalBookings == 1) {
                                            Text("🌟", style = TourOSTypography.Caption)
                                        }
                                    }
                                }
                            }
                        ),
                        TourOSColumn<CustomerCrmDetail>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İletişim"),
                            weight = 2.0f,
                            cellContent = { customer ->
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    if (customer.phone.isNotBlank() && customer.phone != "-") {
                                        Text(
                                            text = "📞 ${customer.phone}",
                                            style = TourOSTypography.Caption,
                                            color = TourOSColors.TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                    if (customer.email.isNotBlank() && customer.email != "-") {
                                        Text(
                                            text = "✉ ${customer.email}",
                                            style = TourOSTypography.Caption,
                                            color = TourOSColors.TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        ),
                        TourOSColumn<CustomerCrmDetail>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rezervasyon"),
                            weight = 1.3f,
                            cellContent = { customer ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${customer.totalBookings}",
                                        style = TourOSTypography.BodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.Primary
                                    )
                                    Text(
                                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(customer.bookingTypeStr),
                                        style = TourOSTypography.Caption,
                                        color = TourOSColors.TextSecondary
                                    )
                                }
                            }
                        ),
                        TourOSColumn<CustomerCrmDetail>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Ciro (LTV)"),
                            weight = 1.5f,
                            cellContent = { customer ->
                                Text(
                                    text = "₺ ${formatLtvCurrency(customer.ltvAmount)}",
                                    style = TourOSTypography.BodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (customer.ltvAmount >= 20000.0) TourOSColors.Primary else TourOSColors.TextPrimary
                                )
                            }
                        ),
                        TourOSColumn<CustomerCrmDetail>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Son İşlem"),
                            weight = 1.2f,
                            cellContent = { customer ->
                                Text(
                                    text = customer.lastActivityDate,
                                    style = TourOSTypography.Caption,
                                    color = TourOSColors.TextSecondary
                                )
                            }
                        ),
                        TourOSColumn<CustomerCrmDetail>(
                            title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hızlı İletişim & Detay"),
                            weight = 2.0f,
                            cellContent = { customer ->
                                val digitsPhone = customer.phone.filter { it.isDigit() }
                                val waPhone = when {
                                    digitsPhone.startsWith("90") -> digitsPhone
                                    digitsPhone.startsWith("0") -> "90" + digitsPhone.removePrefix("0")
                                    digitsPhone.length == 10 -> "90$digitsPhone"
                                    else -> digitsPhone
                                }
                                val telPhone = if (customer.phone.startsWith("+")) customer.phone.replace(" ", "") else "+$waPhone"

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // WhatsApp Butonu
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                if (waPhone.isNotBlank()) {
                                                    runCatching { uriHandler.openUri("https://wa.me/$waPhone") }
                                                }
                                            },
                                        color = Color(0xFF25D366).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "💬 WA",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = TourOSTypography.Caption,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF128C7E)
                                        )
                                    }

                                    // Arama Butonu
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                if (digitsPhone.isNotBlank()) {
                                                    runCatching { uriHandler.openUri("tel:$telPhone") }
                                                }
                                            },
                                        color = TourOSColors.PrimaryContainer
                                    ) {
                                        Text(
                                            text = "📞 Ara",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = TourOSTypography.Caption,
                                            fontWeight = FontWeight.Bold,
                                            color = TourOSColors.Primary
                                        )
                                    }

                                    // Detay Aç Butonu
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { selectedCustomerForDetail = customer },
                                        color = TourOSColors.Surface
                                    ) {
                                        Text(
                                            text = "Detay ›",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = TourOSTypography.Caption,
                                            fontWeight = FontWeight.Bold,
                                            color = TourOSColors.TextPrimary
                                        )
                                    }
                                }
                            }
                        )
                    )

                    TourOSDataTable(
                        items = filteredCustomers,
                        columns = customerColumns,
                        isCompact = isCompact,
                        dense = true,
                        modifier = Modifier.fillMaxSize(),
                        onItemClick = { selectedCustomerForDetail = it },
                        compactCardContent = { customer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(TourOSSpacing.small),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = customer.name,
                                        style = TourOSTypography.BodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TourOSColors.TextPrimary
                                    )
                                    Text(
                                        text = "📞 ${customer.phone}  ·  ${customer.totalBookings} Rezervasyon",
                                        style = TourOSTypography.Caption,
                                        color = TourOSColors.TextSecondary
                                    )
                                }
                                Text(
                                    text = "₺ ${formatLtvCurrency(customer.ltvAmount)}",
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

    // ── 3. MÜŞTERİ GEÇMİŞİ & REZERVASYON DETAY MODALI ─────────────────────────
    selectedCustomerForDetail?.let { customer ->
        CustomerDetailHistoryDialog(
            customer = customer,
            onDismiss = { selectedCustomerForDetail = null },
            uriHandler = uriHandler
        )
    }
}

@Composable
private fun CustomerDetailHistoryDialog(
    customer: CustomerCrmDetail,
    onDismiss: () -> Unit,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    val digitsPhone = customer.phone.filter { it.isDigit() }
    val waPhone = when {
        digitsPhone.startsWith("90") -> digitsPhone
        digitsPhone.startsWith("0") -> "90" + digitsPhone.removePrefix("0")
        digitsPhone.length == 10 -> "90$digitsPhone"
        else -> digitsPhone
    }
    val telPhone = if (customer.phone.startsWith("+")) customer.phone.replace(" ", "") else "+$waPhone"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = TourOSColors.Background),
            shape = RoundedCornerShape(TourOSSpacing.cornerRadius),
            border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TourOSColors.PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = customer.name.take(1).uppercase(),
                                style = TourOSTypography.TitleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TourOSColors.Primary
                            )
                        }

                        Column {
                            Text(
                                text = customer.name,
                                style = TourOSTypography.TitleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TourOSColors.TextPrimary
                            )
                            Text(
                                text = "${customer.phone}  ·  ${customer.email}",
                                style = TourOSTypography.Caption,
                                color = TourOSColors.TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Kapat", tint = TourOSColors.TextSecondary)
                    }
                }

                HorizontalDivider(color = TourOSColors.Divider)

                // Finansal & Sadakat Özet Kartları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    SummaryMetricCard(
                        title = "Toplam Harcama (LTV)",
                        value = "₺ ${formatLtvCurrency(customer.ltvAmount)}",
                        modifier = Modifier.weight(1f),
                        color = TourOSColors.Primary
                    )
                    SummaryMetricCard(
                        title = "Rezervasyon Sayısı",
                        value = "${customer.totalBookings} Adet",
                        modifier = Modifier.weight(1f),
                        color = TourOSColors.TextPrimary
                    )
                    SummaryMetricCard(
                        title = "Son Seyahat Tarihi",
                        value = customer.lastActivityDate,
                        modifier = Modifier.weight(1f),
                        color = TourOSColors.TextSecondary
                    )
                }

                // Rezervasyon Geçmişi Başlığı
                Text(
                    text = "📋 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rezervasyon & Seyahat Geçmişi")} (${customer.bookings.size})",
                    style = TourOSTypography.BodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.TextPrimary
                )

                // Rezervasyon Listesi
                if (customer.bookings.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Detaylı rezervasyon kaydı listelenemedi."),
                            style = TourOSTypography.BodyMedium,
                            color = TourOSColors.TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                    ) {
                        items(customer.bookings) { booking ->
                            BookingHistoryCard(booking = booking)
                        }
                    }
                }

                HorizontalDivider(color = TourOSColors.Divider)

                // Alt Aksiyon Butonları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        TourOSButton(
                            text = "💬 WhatsApp ile Teklif / Mesaj Gönder",
                            onClick = {
                                if (waPhone.isNotBlank()) {
                                    runCatching { uriHandler.openUri("https://wa.me/$waPhone") }
                                }
                            },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                        TourOSButton(
                            text = "📞 Telefonla Ara",
                            onClick = {
                                if (digitsPhone.isNotBlank()) {
                                    runCatching { uriHandler.openUri("tel:$telPhone") }
                                }
                            },
                            variant = TourOSButtonVariant.SECONDARY
                        )
                    }

                    TourOSButton(
                        text = "Kapat",
                        onClick = onDismiss,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = TourOSColors.TextPrimary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Border),
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier.padding(TourOSSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate(title), style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
            Text(value, style = TourOSTypography.TitleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun BookingHistoryCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Border),
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TourOSSpacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (booking.bookingType == "HOTEL") "🏨 Otel" else "✈️ Tur",
                        style = TourOSTypography.Caption,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.Primary
                    )
                    Text(
                        text = if (!booking.operatorPnrCode.isNullOrBlank()) "PNR: ${booking.operatorPnrCode}" else "Kod: ${booking.bookingCode}",
                        style = TourOSTypography.BodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.TextPrimary
                    )
                    TourOSStatusBadge(
                        text = booking.status.name,
                        backgroundColor = if (booking.status.name == "CONFIRMED" || booking.status.name == "ONAYLANDI") TourOSColors.SuccessContainer else TourOSColors.PrimaryContainer,
                        textColor = if (booking.status.name == "CONFIRMED" || booking.status.name == "ONAYLANDI") TourOSColors.Success else TourOSColors.Primary
                    )
                }

                val dateStr = booking.departureDate ?: booking.checkInDate ?: "-"
                Text(
                    text = "Tarih: $dateStr  ·  Kişi: ${booking.paxCount} Pax",
                    style = TourOSTypography.Caption,
                    color = TourOSColors.TextSecondary
                )
            }

            Text(
                text = "₺ ${formatLtvCurrency(booking.totalPrice)}",
                style = TourOSTypography.BodyMedium,
                fontWeight = FontWeight.Bold,
                color = TourOSColors.Primary
            )
        }
    }
}

private fun formatLtvCurrency(value: Double): String {
    val longVal = value.toLong()
    return if (value % 1.0 == 0.0) {
        longVal.toString()
    } else {
        val whole = (value.toInt()).toString()
        val decimal = ((value - value.toInt()) * 100).toInt()
        "$whole.${if (decimal < 10) "0$decimal" else decimal}"
    }
}
