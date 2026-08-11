package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.BookingStatusLog
import com.mgacreative.touros.ui.viewmodel.BookingDetailUiState
import com.mgacreative.touros.ui.viewmodel.BookingDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * 1.4.5 Rezervasyon Detay Ekranı.
 * Yolcu bilgileri, hizmet/ödeme dökümü ve durum geçmişi (audit log) içerir.
 */
@Composable
fun BookingDetailScreen(
    bookingId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: BookingDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bookingId) {
        viewModel.loadBooking(bookingId)
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNavigateBack) {
                        Text("‹ Geri", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Rezervasyon Detayı",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is BookingDetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is BookingDetailUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is BookingDetailUiState.Success -> {
                    if (state.isDeleted) {
                        LaunchedEffect(Unit) {
                            onNavigateBack()
                        }
                    } else {
                        BookingDetailContent(
                            state = state,
                            onTabSelected = { viewModel.selectTab(it) },
                            onStatusChange = { viewModel.updateStatus(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingDetailContent(
    state: BookingDetailUiState.Success,
    onTabSelected: (Int) -> Unit,
    onStatusChange: (BookingStatus) -> Unit
) {
    val booking = state.booking

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = booking.bookingCode,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    DetailStatusBadge(status = booking.status)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Müşteri: ${booking.customerName}", fontWeight = FontWeight.Bold)
                if (!booking.customerPhone.isNullOrBlank()) Text("Telefon: ${booking.customerPhone}")
                if (!booking.customerEmail.isNullOrBlank()) Text("E-posta: ${booking.customerEmail}")

                Spacer(modifier = Modifier.height(12.dp))

                // Allowed Status Transition Actions
                val allowedNext = booking.allowedNextStatuses
                if (allowedNext.isNotEmpty()) {
                    Text("Durum Değiştir:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        allowedNext.forEach { target ->
                            val buttonBg = when (target) {
                                BookingStatus.OPSIYON -> Color(0xFF1E88E5) // Canlı Mavi
                                BookingStatus.ONAYLANDI -> Color(0xFF2E7D32) // Canlı Yeşil
                                BookingStatus.IPTAL -> Color(0xFFC62828) // Canlı Kırmızı
                                BookingStatus.TAMAMLANDI -> Color(0xFF00838F) // Turkuaz
                                else -> Color(0xFF0D9488)
                            }

                            Button(
                                onClick = { onStatusChange(target) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonBg,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = target.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        PrimaryTabRow(selectedTabIndex = state.selectedTab) {
            Tab(
                selected = state.selectedTab == 0,
                onClick = { onTabSelected(0) },
                text = { Text("Yolcular & Hizmetler") }
            )
            Tab(
                selected = state.selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text("Ödeme Özeti") }
            )
            Tab(
                selected = state.selectedTab == 2,
                onClick = { onTabSelected(2) },
                text = { Text("Durum Geçmişi (${state.statusLogs.size})") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state.selectedTab) {
            0 -> PassengersAndServicesTab(booking = booking)
            1 -> PaymentSummaryTab(booking = booking)
            2 -> StatusLogsTab(logs = state.statusLogs)
        }
    }
}

@Composable
private fun PassengersAndServicesTab(booking: Booking) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // KART 0: ACENTE VE OPERATÖR KİMLİK KART
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏢 Acente: Coral Travel B2B (MGA Partner)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "⚙️ Operatör: ${booking.operatorName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!booking.notes.isNullOrBlank()) {
                        Text(text = "📌 Soru & Notlar: ${booking.notes}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // KART 1: YOLCU (TURİST) PASAPORT VE SORUMLULUK DÖKÜMÜ
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("👥 Yolcu Listesi & Pasaport Detayları (${booking.passengers.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (booking.passengers.isEmpty()) {
                        Text("Yolcu detayı: Ana Yolcu (${booking.customerName})", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        booking.passengers.forEachIndexed { idx, pass ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "👤 Turist ${idx + 1}: ${pass.fullName} ${if (pass.isLead) "⭐ (Sipariş Veren / Lead)" else ""}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (!pass.gender.isNullOrBlank()) {
                                        Text(text = pass.gender ?: "", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (!pass.passportNo.isNullOrBlank()) Text("Pasaport No: ${pass.passportNo}", style = MaterialTheme.typography.bodySmall)
                                    if (!pass.birthDate.isNullOrBlank()) Text("Doğum Tarihi: ${pass.birthDate}", style = MaterialTheme.typography.bodySmall)
                                }

                                if (!pass.notes.isNullOrBlank()) {
                                    Text(text = "ℹ️ ${pass.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // KART 2: HİZMET, UÇUŞ VE ZORUNLU SURCHARGES DÖKÜMÜ
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🧳 Seçilen Uçuş, Konaklama & Ekstra Hizmet Dökümü", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (booking.items.isEmpty()) {
                        Text("Temel Tur Paketi x ${booking.paxCount} Pax", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        booking.items.forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.description, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    Text("${item.totalPrice.toInt()} ${booking.currency}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                if (!item.notes.isNullOrBlank()) {
                                    Text(item.notes ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun PaymentSummaryTab(booking: Booking) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💳 Ödeme Detayı & Esnek Alanlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    val unitPrice = booking.totalPrice / maxOf(1, booking.paxCount)
                    val netCostCalculated = booking.totalPrice * 0.88 // %12 kâr marjı hesabı
                    val profitCalculated = booking.totalPrice - netCostCalculated

                    DetailPriceRow("Kişi Sayısı (Pax):", "${booking.paxCount} Kişi")
                    DetailPriceRow("Kişi Başı Pax Fiyatı:", "$unitPrice ${booking.currency}")
                    DetailPriceRow("Para Birimi:", booking.currency)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailPriceRow("TOPLAM SATIŞ (GROSS):", "${booking.totalPrice} ${booking.currency}", isBold = true)

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── GÖRSEL 1: ERP BACK-OFFICE FİNANSAL DÖKÜM TABLOSU (SCREENSHOT 2535 STİLİ) ──
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "📊 ERP Back-Office Finansal Kâr / Maliyet Matrisi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Operatör Net Alış Tutarı (Net Cost):", style = MaterialTheme.typography.bodySmall)
                                Text("${netCostCalculated.toInt()} ${booking.currency}", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Acente Satış Tutarı (Gross Sales):", style = MaterialTheme.typography.bodySmall)
                                Text("${booking.totalPrice.toInt()} ${booking.currency}", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Net Kâr Marjı (Profit Margin):", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                                Text("+${profitCalculated.toInt()} ${booking.currency}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Operatör Komisyon Oranı:", style = MaterialTheme.typography.bodySmall)
                                Text("%12.0", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Incoming Voucher Kodu:", style = MaterialTheme.typography.bodySmall)
                                Text("VCH-${booking.bookingCode}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusLogsTab(logs: List<BookingStatusLog>) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Durum geçmişi kaydı bulunamadı.", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(logs, key = { it.id }) { log ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${log.fromStatus ?: '—'} ➔ ${log.toStatus}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(log.createdAt.take(19).replace("T", " "), style = MaterialTheme.typography.labelSmall)
                        }
                        if (!log.notes.isNullOrBlank()) {
                            Text(log.notes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailPriceRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DetailStatusBadge(status: BookingStatus) {
    val (bgColor, textColor) = when (status) {
        BookingStatus.BEKLIYOR -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        BookingStatus.OPSIYON -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        BookingStatus.ONAYLANDI -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        BookingStatus.IPTAL -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        BookingStatus.TAMAMLANDI -> Color.LightGray to Color.DarkGray
    }

    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = status.displayName.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
