package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.B2BAgencyPrivateReport
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2BAgencyPrivateReportsViewModel

private data class DateRangeFilterOption(val key: String, val label: String)

private val b2bDateRangeFilters = listOf(
    DateRangeFilterOption("30_DAYS", "📅 Son 30 Gün"),
    DateRangeFilterOption("THIS_MONTH", "🗓️ Bu Ay"),
    DateRangeFilterOption("THIS_YEAR", "📊 Bu Yıl")
)

private val b2bStatusFilters = listOf("Tüm Durumlar", "Onaylı ✅", "İptal 🚫")

private data class B2BReportRowItem(
    val date: String,
    val code: String,
    val guest: String,
    val tour: String,
    val gross: Double,
    val commission: Double,
    val net: Double,
    val status: String
)

private fun formatB2BMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

/**
 * B2B Acente Özel Raporlar Ekranı — TourOS 0.3
 *
 * İç raporlama ekranıyla birebir tutarlı Filtre + Tablo Düzeni.
 * Supabase RLS ile sadece oturum açan acenteye ait satış ve komisyon verileriyle sınırlıdır.
 */
@Composable
fun B2BAgencyPrivateReportsScreen(
    viewModel: B2BAgencyPrivateReportsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val report = state.report

    var selectedDateRange by remember { mutableStateOf("THIS_MONTH") }
    var selectedStatus by remember { mutableStateOf(b2bStatusFilters.first()) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "B2B Acente Özel Raporu",
                subtitle = "Sadece acentenize ait özel satış ve cari dökümler",
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
                // ── 1. RLS GÜVENLİK & İZOLASYON BANNERI ─────────────────────────
                item {
                    RlsSecurityBannerCard()
                }

                // ── 2. ÜSTTE FİLTRE ÇUBUĞU (Tarih Aralığı & Durum) ───────────────
                item {
                    B2BReportFilterBar(
                        selectedDateRange = selectedDateRange,
                        onDateRangeChange = { selectedDateRange = it },
                        selectedStatus = selectedStatus,
                        onStatusChange = { selectedStatus = it }
                    )
                }

                // ── 3. ÖZET KPI PERFORMANS KARTLARI ─────────────────────────────
                item {
                    B2BPerformanceKpiSection(report = report)
                }

                // ── 4. ACENTEYE ÖZEL REZERVASYON & FİNANS TABLOSU ──────────────
                item {
                    Text(
                        "📋 Acente Satış & Komisyon Detay Tablosu",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                }

                if (state.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TourOSColors.Primary)
                        }
                    }
                } else if (isExpanded) {
                    // Expanded: Tablo Düzeni
                    item {
                        B2BSalesDetailsTable()
                    }
                } else {
                    // Compact: Kart Akışı
                    item {
                        B2BSalesDetailsCardList()
                    }
                }
            }
        }
    }
}

// ─── RLS Güvenlik & İzolasyon Banner'ı ───────────────────────────────────────

@Composable
private fun RlsSecurityBannerCard() {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔒", style = TourOSTypography.TitleLarge)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Supabase RLS Veri İzolasyonu Aktif",
                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                )
                Text(
                    "Acente JWT yetkiniz doğrultusunda yalnızca acentenize ait satış ve cari dökümler gösterilmektedir.",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }
            TourOSStatusBadge(
                text = "GÜVENLİ",
                backgroundColor = TourOSColors.SuccessContainer,
                textColor = TourOSColors.Success
            )
        }
    }
}

// ─── Üst Filtre Çubuğu (İç Raporlama İle Birebir Tutarlı) ───────────────────

@Composable
private fun B2BReportFilterBar(
    selectedDateRange: String,
    onDateRangeChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.SecondaryContainer.copy(alpha = 0.4f),
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "⚙️ Acente Rapor Filtre Seçenekleri",
                style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // Tarih Aralığı
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Tarih Aralığı:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(b2bDateRangeFilters) { opt ->
                            FilterChip(
                                selected = selectedDateRange == opt.key,
                                onClick = { onDateRangeChange(opt.key) },
                                label = { Text(opt.label, style = TourOSTypography.Caption) }
                            )
                        }
                    }
                }

                // Rezervasyon Durumu
                Column(modifier = Modifier.weight(1f)) {
                    Text("Durum:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(b2bStatusFilters) { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { onStatusChange(st) },
                                label = { Text(st, style = TourOSTypography.Caption) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Özet KPI Performans Kartları ─────────────────────────────────────────────

@Composable
private fun B2BPerformanceKpiSection(report: B2BAgencyPrivateReport) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // Toplam Satış Cirosu
            TourOSCard(
                modifier = Modifier.weight(1f),
                backgroundColor = TourOSColors.PrimaryContainer,
                contentPadding = TourOSSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Top. Satış Cirosu", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary.copy(alpha = 0.8f)))
                    Text("₺ ${formatB2BMoney(report.totalGrossSales)}", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                    Text("${report.totalSalesCount} Toplam Rezervasyon", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                }
            }

            // Net Komisyon Kazancı
            TourOSCard(
                modifier = Modifier.weight(1f),
                backgroundColor = TourOSColors.SuccessContainer,
                contentPadding = TourOSSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Net Komisyon Kazancı", style = TourOSTypography.Caption.copy(color = TourOSColors.Success.copy(alpha = 0.8f)))
                    Text("₺ ${formatB2BMoney(report.netEarnedCommission)}", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Success))
                    Text("%10 Sabit İskonto Hak Ediş", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // Tamamlanan vs İptal
            TourOSCard(
                modifier = Modifier.weight(1f),
                backgroundColor = TourOSColors.Surface,
                contentPadding = TourOSSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Tamamlanan Rezervasyon", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("${report.activeConfirmedCount} Adet", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Success))
                }
            }

            // İptal Oranı
            TourOSCard(
                modifier = Modifier.weight(1f),
                backgroundColor = TourOSColors.SecondaryContainer,
                contentPadding = TourOSSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("İptal Oranı", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("% ${report.cancellationRate}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary))
                }
            }
        }
    }
}

// ─── Expanded: Acenteye Özel Satış Tablosu ───────────────────────────────────

@Composable
private fun B2BSalesDetailsTable() {
    val rowItems = listOf(
        B2BReportRowItem("07.08.2026", "BK-9812", "Johann Schmidt", "Kapadokya VIP Balon Turu", 15000.0, 1500.0, 13500.0, "ONAYLI"),
        B2BReportRowItem("06.08.2026", "BK-9804", "Sarah Jenkins", "Ege Sahilleri Turu", 22000.0, 2200.0, 19800.0, "ONAYLI"),
        B2BReportRowItem("04.08.2026", "BK-9788", "Hans Müller", "İstanbul Kültür Turu", 10000.0, 1000.0, 9000.0, "İPTAL")
    )

    val headers = listOf("Tarih", "Kodu", "Misafir Adı", "Tur Adı", "Brüt Ciro (₺)", "Komisyon (₺)", "Net Ödenen (₺)", "Durum")
    val weights = listOf(1.0f, 1.0f, 1.4f, 1.8f, 1.1f, 1.1f, 1.2f, 0.9f)

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TourOSColors.Primary)
                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small)
            ) {
                headers.forEachIndexed { i, h ->
                    Text(
                        h,
                        style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary),
                        modifier = Modifier.weight(weights[i])
                    )
                }
            }

            rowItems.forEachIndexed { idx, row ->
                val bg = if (idx % 2 == 0) TourOSColors.Background else TourOSColors.Surface
                val isConfirmed = row.status == "ONAYLI"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(row.date, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), modifier = Modifier.weight(weights[0]))
                    Text(row.code, style = TourOSTypography.Label.copy(color = TourOSColors.Primary), modifier = Modifier.weight(weights[1]))
                    Text(row.guest, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[2]))
                    Text(row.tour, style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[3]))
                    Text("₺ ${formatB2BMoney(row.gross)}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[4]))
                    Text("₺ ${formatB2BMoney(row.commission)}", style = TourOSTypography.Label.copy(color = TourOSColors.Success), modifier = Modifier.weight(weights[5]))
                    Text("₺ ${formatB2BMoney(row.net)}", style = TourOSTypography.Label.copy(color = TourOSColors.Primary), modifier = Modifier.weight(weights[6]))
                    Box(modifier = Modifier.weight(weights[7])) {
                        TourOSStatusBadge(
                            text = if (isConfirmed) "Onaylı" else "İptal",
                            backgroundColor = if (isConfirmed) TourOSColors.SuccessContainer else TourOSColors.SecondaryContainer,
                            textColor = if (isConfirmed) TourOSColors.Success else TourOSColors.Secondary
                        )
                    }
                }

                if (idx < rowItems.size - 1) {
                    HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ─── Compact: Acenteye Özel Satış Kart Akışı ──────────────────────────────────

@Composable
private fun B2BSalesDetailsCardList() {
    val rowItems = listOf(
        B2BReportRowItem("07.08.2026", "BK-9812", "Johann Schmidt", "Kapadokya VIP Balon Turu", 15000.0, 1500.0, 13500.0, "ONAYLI"),
        B2BReportRowItem("06.08.2026", "BK-9804", "Sarah Jenkins", "Ege Sahilleri Turu", 22000.0, 2200.0, 19800.0, "ONAYLI")
    )

    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
        rowItems.forEach { row ->
            val isConfirmed = row.status == "ONAYLI"

            TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.medium) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${row.code} · ${row.guest}", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                        TourOSStatusBadge(
                            text = if (isConfirmed) "✅ ONAYLI" else "🚫 İPTAL",
                            backgroundColor = if (isConfirmed) TourOSColors.SuccessContainer else TourOSColors.SecondaryContainer,
                            textColor = if (isConfirmed) TourOSColors.Success else TourOSColors.Secondary
                        )
                    }

                    Text("Tur: ${row.tour}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))

                    HorizontalDivider(color = TourOSColors.Divider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Brüt: ₺ ${formatB2BMoney(row.gross)}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        Text("Komisyon: ₺ ${formatB2BMoney(row.commission)}", style = TourOSTypography.Label.copy(color = TourOSColors.Success))
                        Text("Net: ₺ ${formatB2BMoney(row.net)}", style = TourOSTypography.Label.copy(color = TourOSColors.Primary))
                    }
                }
            }
        }
    }
}
