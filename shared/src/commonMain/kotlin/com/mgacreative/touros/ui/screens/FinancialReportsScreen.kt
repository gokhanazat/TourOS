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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.FinancialReportSummary
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.FinancialReportsViewModel

private data class ReportFilterOption(val key: String, val label: String)

/**
 * Finansal Raporlar Ekranı — TourOS Canlı Dönüşümlü & Hızlı Giriş Destekli Sürüm
 */
@Composable
fun FinancialReportsScreen(
    viewModel: FinancialReportsViewModel,
    onNavigateToInvoice: () -> Unit = {},
    onNavigateToCurrentAccount: () -> Unit = {},
    onNavigateToSupplierExpense: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()
    val summary = state.summary
    val symbol = state.currencySymbol
    val rate = state.currencyRate

    var exportNotification by remember { mutableStateOf<String?>(null) }

    val dateFilterOptions = remember(currentLanguage) {
        listOf(
            ReportFilterOption("30_DAYS", "📅 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Son 30 Gün")}"),
            ReportFilterOption("THIS_MONTH", "🗓️ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Bu Ay")}"),
            ReportFilterOption("THIS_YEAR", "📊 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Bu Yıl")}")
        )
    }

    val reportTabs = remember(currentLanguage) {
        listOf(
            com.mgacreative.touros.ui.localization.AppLanguageManager.translate("📑 KDV Raporu"),
            com.mgacreative.touros.ui.localization.AppLanguageManager.translate("📈 Gelir / Gider"),
            com.mgacreative.touros.ui.localization.AppLanguageManager.translate("💵 Nakit & Banka"),
            com.mgacreative.touros.ui.localization.AppLanguageManager.translate("📊 Kârlılık Analizi")
        )
    }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Finansal Raporlar & Analizler"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("KDV, Gelir-Gider, Likidite ve Kârlılık Dökümleri"),
                onNavigateBack = onNavigateBack,
                actions = {
                    Row(
                        modifier = Modifier.padding(end = TourOSSpacing.small),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)
                    ) {
                        TourOSButton(
                            text = "📄 PDF",
                            onClick = { exportNotification = "📄 PDF Finans Raporu (${state.selectedCurrency}) İndirildi!" },
                            variant = TourOSButtonVariant.SECONDARY
                        )
                        TourOSButton(
                            text = "📊 Excel",
                            onClick = { exportNotification = "📊 Excel (CSV) Finans Raporu Dışa Aktarıldı!" },
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
                                style = TourOSTypography.Label.copy(color = TourOSColors.Success),
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { exportNotification = null }) {
                                Text("✕", style = TourOSTypography.Caption.copy(color = TourOSColors.Success))
                            }
                        }
                    }
                }
            }

            item {
                TopReportFilterBar(
                    dateFilterOptions = dateFilterOptions,
                    selectedDateFilter = state.dateFilter,
                    onDateFilterChange = { viewModel.setDateFilter(it) },
                    companyOptions = state.companyOptions,
                    selectedCompany = state.selectedCompany,
                    onCompanyChange = { viewModel.setSelectedCompany(it) },
                    currencyOptions = state.currencyOptions,
                    selectedCurrency = state.selectedCurrency,
                    onCurrencyChange = { viewModel.setSelectedCurrency(it) }
                )
            }

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
                            text = { Text(title, style = TourOSTypography.Label, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            item {
                when (state.selectedTab) {
                    0 -> VatReportSection(summary = summary, symbol = symbol, rate = rate)
                    1 -> RevenueExpenseReportSection(summary = summary, symbol = symbol, rate = rate)
                    2 -> CashBankReportSection(summary = summary, symbol = symbol, rate = rate)
                    3 -> ProfitabilityReportSection(summary = summary)
                }
            }

            // HIZLI FİNANSAL GİRİŞ BUTONLARI
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📋 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Detaylı Finans Kalemleri Tablosu")} (${state.selectedCurrency})",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                        TourOSButton(
                            text = "🧾 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Fatura Kes")}",
                            onClick = onNavigateToInvoice,
                            variant = TourOSButtonVariant.SECONDARY
                        )
                        TourOSButton(
                            text = "➕ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gider Gir")}",
                            onClick = onNavigateToSupplierExpense,
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }
                }
            }

            item {
                FinancialDetailsTable(summary = summary, selectedTab = state.selectedTab, symbol = symbol, rate = rate)
            }
        }
    }
}

// ─── 1. Kompakt Tek Satır Finans Kontrol & Filtre Araç Çubuğu ────────────────

@Composable
private fun TopReportFilterBar(
    dateFilterOptions: List<ReportFilterOption>,
    selectedDateFilter: String,
    onDateFilterChange: (String) -> Unit,
    companyOptions: List<String>,
    selectedCompany: String,
    onCompanyChange: (String) -> Unit,
    currencyOptions: List<String>,
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Divider),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Tarih Önayarları
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📅",
                    style = TourOSTypography.Caption.copy(fontSize = 12.sp)
                )
                dateFilterOptions.forEach { opt ->
                    val isSelected = selectedDateFilter == opt.key
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) TourOSColors.Primary else Color(0xFFF1F5F9),
                        modifier = Modifier.clickable { onDateFilterChange(opt.key) }
                    ) {
                        Text(
                            text = opt.label.replace("📅 ", "").replace("🗓️ ", "").replace("📊 ", ""),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = TourOSTypography.Caption.copy(
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // 2. Şirket / Şube Seçimi
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🏢 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Şube:"),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                )
                companyOptions.forEach { comp ->
                    val isSelected = selectedCompany == comp
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { onCompanyChange(comp) }
                    ) {
                        Text(
                            text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(comp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = TourOSTypography.Caption.copy(
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // 3. Para Birimi
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "💱",
                    style = TourOSTypography.Caption.copy(fontSize = 12.sp)
                )
                currencyOptions.forEach { curr ->
                    val isSelected = selectedCurrency == curr
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFFF1F5F9),
                        modifier = Modifier.clickable { onCurrencyChange(curr) }
                    ) {
                        Text(
                            text = curr,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            style = TourOSTypography.Caption.copy(
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

// ─── 2. KDV Raporu 3'lü Kompakt KPI Kart Şeridi ──────────────────────────────

@Composable
private fun VatReportSection(summary: FinancialReportSummary, symbol: String, rate: Double) {
    val isNetPayable = summary.vatPayable >= 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        // 1. Hesaplanan KDV (Satış)
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF0FDF4),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    "📈 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hesaplanan KDV (Satış)"),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF166534), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    formatConvertedMoney(summary.vatCollected, symbol, rate),
                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            }
        }

        // 2. İndirilecek KDV (Gider)
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFF1F2),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    "📉 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İndirilecek KDV (Gider)"),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF9F1239), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    formatConvertedMoney(summary.vatPaid, symbol, rate),
                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFFBE123C), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            }
        }

        // 3. Net Ödenecek / Devreden KDV
        Surface(
            modifier = Modifier.weight(1.2f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0A2540), // Koyu Lacivert Kurumsal Vurgu
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "⚖️ " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("NET ÖDENECEK KDV"),
                        style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        formatConvertedMoney(summary.vatPayable, symbol, rate),
                        style = TourOSTypography.TitleMedium.copy(
                            color = if (isNetPayable) Color(0xFF34D399) else Color(0xFFF87171),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E3A5F)
                ) {
                    Text(
                        if (isNetPayable) "Ödenecek" else "Devreden",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = TourOSTypography.Caption.copy(
                            color = if (isNetPayable) Color(0xFF34D399) else Color(0xFFF87171),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

// ─── 3. Gelir / Gider 3'lü Kompakt KPI Kart Şeridi ───────────────────────────

@Composable
private fun RevenueExpenseReportSection(summary: FinancialReportSummary, symbol: String, rate: Double) {
    val isProfit = summary.netProfit >= 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        // Toplam Gelir
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF0FDF4),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    "📈 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Gelir"),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF166534), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    formatConvertedMoney(summary.totalRevenue, symbol, rate),
                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            }
        }

        // Toplam Gider
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFF1F2),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    "📉 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Gider"),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF9F1239), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    formatConvertedMoney(summary.totalExpenses, symbol, rate),
                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFFBE123C), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            }
        }

        // Net Faaliyet Kârı
        Surface(
            modifier = Modifier.weight(1.2f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0A2540),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "💰 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("NET FAALİYET KÂRI"),
                        style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        formatConvertedMoney(summary.netProfit, symbol, rate),
                        style = TourOSTypography.TitleMedium.copy(
                            color = if (isProfit) Color(0xFF34D399) else Color(0xFFF87171),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E3A5F)
                ) {
                    Text(
                        if (isProfit) "Net Kâr" else "Net Zarar",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = TourOSTypography.Caption.copy(
                            color = if (isProfit) Color(0xFF34D399) else Color(0xFFF87171),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

// ─── 4. Nakit & Banka 4'lü Kompakt KPI Kart Şeridi ───────────────────────────

@Composable
private fun CashBankReportSection(summary: FinancialReportSummary, symbol: String, rate: Double) {
    val totalLiquid = summary.cashBalance + summary.bankBalance + summary.posBalance
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        // Nakit Kasa
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF8FAFC),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("💵 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Nakit Kasası"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold))
                Text(formatConvertedMoney(summary.cashBalance, symbol, rate), style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp))
            }
        }

        // Banka
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF8FAFC),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("🏦 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Banka Hesapları"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold))
                Text(formatConvertedMoney(summary.bankBalance, symbol, rate), style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp))
            }
        }

        // Sanal POS
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF8FAFC),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("💳 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Sanal POS"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold))
                Text(formatConvertedMoney(summary.posBalance, symbol, rate), style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp))
            }
        }

        // Toplam Likit
        Surface(
            modifier = Modifier.weight(1.2f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0A2540),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("💰 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("TOPLAM LİKİT"), style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold))
                Text(formatConvertedMoney(totalLiquid, symbol, rate), style = TourOSTypography.TitleMedium.copy(color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp))
            }
        }
    }
}

@Composable
private fun ProfitabilityReportSection(summary: FinancialReportSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Divider),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(TourOSSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("🎯 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ortalama Kâr Marjı:")}", style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A)), fontWeight = FontWeight.Bold)
                Text("% ${summary.profitMarginPercentage}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontSize = 18.sp), fontWeight = FontWeight.Bold)
            }

            LinearProgressIndicator(
                progress = { (summary.profitMarginPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = TourOSColors.Primary,
                trackColor = Color(0xFFF1F5F9)
            )
        }
    }
}

@Composable
private fun SummaryKpiCard(label: String, value: String, bg: Color, text: Color, modifier: Modifier) {
    TourOSCard(modifier = modifier, backgroundColor = bg, contentPadding = TourOSSpacing.medium) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = TourOSTypography.TitleMedium.copy(color = text), fontWeight = FontWeight.Bold)
            Text(label, style = TourOSTypography.Caption.copy(color = text.copy(alpha = 0.8f)), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FinancialDetailsTable(summary: FinancialReportSummary, selectedTab: Int, symbol: String, rate: Double) {
    val items = summary.items

    val headers = listOf(
        com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tarih"),
        com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Açıklama / Kalem"),
        com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kategori"),
        "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Matrah")} ($symbol)",
        "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("KDV")} ($symbol)",
        "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Genel Toplam")} ($symbol)"
    )
    val weights = listOf(1.0f, 1.8f, 1.2f, 1.1f, 1.0f, 1.2f)

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
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(weights[i])
                    )
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(TourOSSpacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Henüz kaydedilmiş bir finansal işlem bulunmamaktadır."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                }
            } else {
                items.forEachIndexed { idx, item ->
                    val bg = if (idx % 2 == 0) TourOSColors.Background else TourOSColors.Surface
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bg)
                            .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.date, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), modifier = Modifier.weight(weights[0]))
                        Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate(item.description), style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.Bold, modifier = Modifier.weight(weights[1]))
                        Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate(item.category), style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), modifier = Modifier.weight(weights[2]))
                        Text(formatConvertedMoney(item.subtotal, symbol, rate), style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[3]))
                        Text(formatConvertedMoney(item.vat, symbol, rate), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), modifier = Modifier.weight(weights[4]))
                        Text(formatConvertedMoney(item.total, symbol, rate), style = TourOSTypography.Label.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold, modifier = Modifier.weight(weights[5]))
                    }
                    if (idx < items.size - 1) {
                        HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

private fun formatConvertedMoney(amount: Double, symbol: String, rate: Double): String {
    val converted = amount * rate
    val formattedVal = if (converted % 1.0 == 0.0) {
        converted.toLong().toString()
    } else {
        val whole = converted.toInt().toString()
        val decimal = ((converted - converted.toInt()) * 100).toInt()
        "$whole.${if (decimal < 10) "0$decimal" else decimal}"
    }
    return "$symbol $formattedVal"
}
