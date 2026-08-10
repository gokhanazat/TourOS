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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AutoRevenueEngineViewModel
import com.mgacreative.touros.ui.viewmodel.AutoRevenueLogItem
import com.mgacreative.touros.ui.viewmodel.AutoRevenueUiState

/**
 * 3.1.2 Otomatik Gelir Kaydı Motoru Ekranı — TourOS Canlı Veri Sürümü
 */
@Composable
fun AutoRevenueEngineScreen(
    viewModel: AutoRevenueEngineViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Otomatik Gelir Kaydı Motoru",
                subtitle = "Onaylanan rezervasyonların matrah ve KDV otomatik fatura akışı",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            when (val state = uiState) {
                is AutoRevenueUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                }

                is AutoRevenueUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                    }
                }

                is AutoRevenueUiState.Success -> {
                    // 1. Accounting Engine Durum Kartı
                    TourOSCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = TourOSColors.PrimaryContainer,
                        contentPadding = TourOSSpacing.large
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "⚡ Accounting Engine (Aktif Motor)",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                        .background(TourOSColors.SuccessContainer)
                                        .padding(horizontal = TourOSSpacing.small, vertical = 2.dp)
                                ) {
                                    Text(
                                        "🟢 Otomatik Tetikleyici Aktif",
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                "Onaylanan her rezervasyon için veritabanında KDV matrah hesaplaması yapılıp anında otomatik satış faturası (Invoice) oluşturulur.",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )

                            HorizontalDivider(color = TourOSColors.Divider)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Toplam Muhasebeleşen Ciro", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                    Text(
                                        "₺ ${formatMoney(state.totalRevenue)}",
                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Tahakkuk Eden KDV (%20)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                    Text(
                                        "₺ ${formatMoney(state.totalTaxCollected)}",
                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // 2. Gelir Kayıt Günlüğü (Log List)
                    Text(
                        "🧾 Otomatik Gelir Kaydı Günlüğü & Faturalar (${state.logs.size}):",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )

                    if (state.logs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Henüz veritabanında işlenmiş otomatik gelir kaydı bulunmamaktadır.",
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(state.logs) { log ->
                                AutoRevenueLogCard(log = log)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoRevenueLogCard(log: AutoRevenueLogItem) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📌 Rezervasyon: ${log.bookingCode}",
                    style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.SecondaryContainer)
                        .padding(horizontal = TourOSSpacing.small, vertical = 2.dp)
                ) {
                    Text(
                        "🧾 ${log.invoiceNo}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Secondary),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                "👤 Müşteri: ${log.customerName}  ·  📅 Tarih: ${log.autoProcessedAt}",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Matrah (KDV Hariç)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("₺ ${formatMoney(log.subtotal)}", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("KDV (%20)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("₺ ${formatMoney(log.taxAmount)}", style = TourOSTypography.Label.copy(color = TourOSColors.Secondary), fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Fatura Toplamı", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("₺ ${formatMoney(log.totalAmount)}", style = TourOSTypography.Label.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}
