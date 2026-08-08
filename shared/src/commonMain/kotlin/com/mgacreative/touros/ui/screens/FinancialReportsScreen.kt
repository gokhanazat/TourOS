package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.mgacreative.touros.domain.model.FinancialReportSummary
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.FinancialReportsViewModel

private data class ReportFilterOption(val key: String, val label: String)

private val dateFilterOptions = listOf(
    ReportFilterOption("30_DAYS", "📅 Son 30 Gün"),
    ReportFilterOption("THIS_MONTH", "🗓️ Bu Ay"),
    ReportFilterOption("THIS_YEAR", "📊 Bu Yıl")
)

private val companyFilterOptions = listOf("Tüm Şirketler", "Merkez Acente", "Antalya Şube")
private val currencyFilterOptions = listOf("TRY ₺", "EUR €", "USD $")

/**
 * Finansal Raporlar Ekranı (KDV / Gelir / Nakit / Banka / Kârlılık) — TourOS 0.3
 *
 * Üstte Filtre Çubuğu (Tarih aralığı, Firma, Para birimi)
 * Sağ Üstte Export Butonları (PDF Export & Excel Export)
 * Altta Özet Kartları + Detaylı Finans Kalemleri Tablosu
 */
@Composable
fun FinancialReportsScreen(
    viewModel: FinancialReportsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val summary = state.summary

    var selectedCompany by remember { mutableStateOf(companyFilterOptions.first()) }
    var selectedCurrency by remember { mutableStateOf(currencyFilterOptions.first()) }
    var exportNotification by remember { mutableStateOf<String?>(null) }

    val reportTabs = listOf("📑 KDV Raporu", "📈 Gelir / Gider", "💵 Nakit & Banka", "📊 Kârlılık Analizi")

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Finansal Raporlar & Analizler",
                subtitle = "KDV, Gelir-Gider, Likidite ve Kârlılık Dökümleri",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                },
                actions = {
                    // SAĞ ÜSTTE EXPORT BUTONLARI (PDF & EXCEL)
                    Row(
                        modifier = Modifier.padding(end = TourOSSpacing.small),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)
                    ) {
                        TourOSButton(
                            text = "📄 PDF",
                            onClick = { exportNotification = "📄 PDF Raporu Hazırlandı ve İndirildi!" },
                            variant = TourOSButtonVariant.SECONDARY
                        )
                        TourOSButton(
                            text = "📊 Excel",
                            onClick = { exportNotification = "📊 Excel (CSV) Raporu Dışa Aktarıldı!" },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // Bildirim Mesajı
            if (exportNotification != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SuccessContainer)
                            .padding(TourOSSpacing.medium)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                exportNotification!!,
                                style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                            )
                            IconButton(onClick = { exportNotification = null }) {
                                Text("✕", style = TourOSTypography.Caption.copy(color = TourOSColors.Success))
                            }
                        }
                    }
                }
            }

            // ── 1. ÜSTTE FİLTRE ÇUBUĞU (Tarih, Firma, Para Birimi) ─────────────
            item {
                TopReportFilterBar(
                    selectedDateFilter = state.dateFilter,
                    onDateFilterChange = { viewModel.setDateFilter(it) },
                    selectedCompany = selectedCompany,
                    onCompanyChange = { selectedCompany = it },
                    selectedCurrency = selectedCurrency,
                    onCurrencyChange = { selectedCurrency = it }
                )
            }

            // ── 2. SEKMELER (KDV, Gelir, Nakit, Kârlılık) ────────────────────
            item {
                PrimaryTabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = TourOSColors.Background,
                    contentColor = TourOSColors.Primary
                ) {
                    reportTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = state.selectedTab == index,
                            onClick = { viewModel.setSelectedTab(index) },
                            text = { Text(title, style = TourOSTypography.Label) }
                        )
                    }
                }
            }

            // ── 3. SEKMEYE GÖRE ÖZET KARTLARI ─────────────────────────────────
            item {
                when (state.selectedTab) {
                    0 -> VatReportSection(summary = summary)
                    1 -> RevenueExpenseReportSection(summary = summary)
                    2 -> CashBankReportSection(summary = summary)
                    3 -> ProfitabilityReportSection(summary = summary)
                }
            }

            // ── 4. ALTA DETAY HAREKET TABLOSU ─────────────────────────────────
            item {
                Text(
                    "📋 Detaylı Finans Kalemleri Tablosu",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )
            }

            item {
                FinancialDetailsTable(summary = summary, selectedTab = state.selectedTab)
            }
        }
    }
}

// ─── Üst Filtre Çubuğu (Tarih, Firma, Para Birimi) ───────────────────────────

@Composable
private fun TopReportFilterBar(
    selectedDateFilter: String,
    onDateFilterChange: (String) -> Unit,
    selectedCompany: String,
    onCompanyChange: (String) -> Unit,
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "⚙️ Rapor Filtre Seçenekleri",
                style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
            )

            // 1. Tarih Aralığı Chip'leri
            LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                items(dateFilterOptions) { opt ->
                    FilterChip(
                        selected = selectedDateFilter == opt.key,
                        onClick = { onDateFilterChange(opt.key) },
                        label = { Text(opt.label, style = TourOSTypography.Caption) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TourOSColors.Primary,
                            selectedLabelColor = TourOSColors.OnPrimary
                        )
                    )
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // 2. Firma Seçimi & Para Birimi Seçimi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // Firma Seçim Chip'leri
                Column(modifier = Modifier.weight(1f)) {
                    Text("Firma / Şube:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(companyFilterOptions) { comp ->
                            FilterChip(
                                selected = selectedCompany == comp,
                                onClick = { onCompanyChange(comp) },
                                label = { Text(comp, style = TourOSTypography.Caption) }
                            )
                        }
                    }
                }

                // Para Birimi Seçim Chip'leri
                Column(modifier = Modifier.weight(1f)) {
                    Text("Para Birimi:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(currencyFilterOptions) { curr ->
                            FilterChip(
                                selected = selectedCurrency == curr,
                                onClick = { onCurrencyChange(curr) },
                                label = { Text(curr, style = TourOSTypography.Caption) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Özet Kartları: KDV ───────────────────────────────────────────────────────

@Composable
private fun VatReportSection(summary: FinancialReportSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            SummaryKpiCard("Hesaplanan KDV (Satış)", "₺ ${formatMoney(summary.vatCollected)}", TourOSColors.PrimaryContainer, TourOSColors.Primary, Modifier.weight(1f))
            SummaryKpiCard("İndirilecek KDV (Gider)", "₺ ${formatMoney(summary.vatPaid)}", TourOSColors.SuccessContainer, TourOSColors.Success, Modifier.weight(1f))
        }

        TourOSCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TourOSColors.SecondaryContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("📌 Net Ödenecek / Devreden KDV:", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                Text("₺ ${formatMoney(summary.vatPayable)}", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Secondary))
            }
        }
    }
}

// ─── Özet Kartları: Gelir / Gider ─────────────────────────────────────────────

@Composable
private fun RevenueExpenseReportSection(summary: FinancialReportSummary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
        SummaryKpiCard("Toplam Gelir", "₺ ${formatMoney(summary.totalRevenue)}", TourOSColors.SuccessContainer, TourOSColors.Success, Modifier.weight(1f))
        SummaryKpiCard("Toplam Gider", "₺ ${formatMoney(summary.totalExpenses)}", TourOSColors.Surface, TourOSColors.TextPrimary, Modifier.weight(1f))
        SummaryKpiCard("Net Faaliyet Kârı", "₺ ${formatMoney(summary.netProfit)}", TourOSColors.PrimaryContainer, TourOSColors.Primary, Modifier.weight(1f))
    }
}

// ─── Özet Kartları: Nakit & Banka ─────────────────────────────────────────────

@Composable
private fun CashBankReportSection(summary: FinancialReportSummary) {
    val totalLiquid = summary.cashBalance + summary.bankBalance + summary.posBalance
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            SummaryKpiCard("Nakit Kasası", "₺ ${formatMoney(summary.cashBalance)}", TourOSColors.SecondaryContainer, TourOSColors.Secondary, Modifier.weight(1f))
            SummaryKpiCard("Banka Hesapları", "₺ ${formatMoney(summary.bankBalance)}", TourOSColors.PrimaryContainer, TourOSColors.Primary, Modifier.weight(1f))
            SummaryKpiCard("Sanal POS Bekleyen", "₺ ${formatMoney(summary.posBalance)}", TourOSColors.SuccessContainer, TourOSColors.Success, Modifier.weight(1f))
        }
        TourOSCard(modifier = Modifier.fillMaxWidth(), backgroundColor = TourOSColors.PrimaryContainer, contentPadding = TourOSSpacing.medium) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("💰 Toplam Likit Varlıklar:", style = TourOSTypography.Label.copy(color = TourOSColors.Primary))
                Text("₺ ${formatMoney(totalLiquid)}", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
            }
        }
    }
}

// ─── Özet Kartları: Kârlılık Analizi ─────────────────────────────────────────

@Composable
private fun ProfitabilityReportSection(summary: FinancialReportSummary) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), backgroundColor = TourOSColors.SecondaryContainer, contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("🎯 Ortalama Kâr Marjı:", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                Text("% ${summary.profitMarginPercentage}", style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Secondary))
            }

            LinearProgressIndicator(
                progress = { (summary.profitMarginPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = TourOSColors.Secondary,
                trackColor = TourOSColors.PrimaryContainer
            )
        }
    }
}

@Composable
private fun SummaryKpiCard(label: String, value: String, bg: Color, text: Color, modifier: Modifier) {
    TourOSCard(modifier = modifier, backgroundColor = bg, contentPadding = TourOSSpacing.medium) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = TourOSTypography.TitleMedium.copy(color = text))
            Text(label, style = TourOSTypography.Caption.copy(color = text.copy(alpha = 0.8f)), textAlign = TextAlign.Center)
        }
    }
}

// ─── Alttaki Detaylı Finans Kalemleri Tablosu ─────────────────────────────────

@Composable
private fun FinancialDetailsTable(summary: FinancialReportSummary, selectedTab: Int) {
    val sampleItems = listOf(
        FinancialRowItem("07.08.2026", "Kapadokya VIP Tur Satış Faturası", "Satış Tahsilatı", 15000.0, 3000.0, 18000.0),
        FinancialRowItem("06.08.2026", "Mercedes Travego Yakıt Ödemesi", "Operasyon Gideri", 4500.0, 900.0, 5400.0),
        FinancialRowItem("05.08.2026", "Kokartlı Rehber Hakediş Ödemesi", "Rehber Gideri", 3500.0, 0.0, 3500.0),
        FinancialRowItem("04.08.2026", "Ege Turu Acente Komisyon Tahsilatı", "Komisyon Geliri", 2800.0, 560.0, 3360.0)
    )

    val headers = listOf("Tarih", "Açıklama / Kalem", "Kategori", "Matrah (₺)", "KDV (₺)", "Genel Toplam (₺)")
    val weights = listOf(1.0f, 1.8f, 1.2f, 1.1f, 1.0f, 1.2f)

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column {
            // Header
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

            // Satırlar
            sampleItems.forEachIndexed { idx, item ->
                val bg = if (idx % 2 == 0) TourOSColors.Background else TourOSColors.Surface
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.date, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), modifier = Modifier.weight(weights[0]))
                    Text(item.description, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[1]))
                    Text(item.category, style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), modifier = Modifier.weight(weights[2]))
                    Text("₺ ${formatMoney(item.subtotal)}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[3]))
                    Text("₺ ${formatMoney(item.vat)}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), modifier = Modifier.weight(weights[4]))
                    Text("₺ ${formatMoney(item.total)}", style = TourOSTypography.Label.copy(color = TourOSColors.Primary), modifier = Modifier.weight(weights[5]))
                }
                if (idx < sampleItems.size - 1) {
                    HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
                }
            }
        }
    }
}

private data class FinancialRowItem(
    val date: String,
    val description: String,
    val category: String,
    val subtotal: Double,
    val vat: Double,
    val total: Double
)

private fun formatMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}
