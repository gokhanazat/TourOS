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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "⚙️ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rapor Filtre Seçenekleri")}",
                style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                fontWeight = FontWeight.Bold
            )

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Firma / Şube:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(companyOptions) { comp ->
                            FilterChip(
                                selected = selectedCompany == comp,
                                onClick = { onCompanyChange(comp) },
                                label = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate(comp), style = TourOSTypography.Caption) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TourOSColors.PrimaryContainer,
                                    selectedLabelColor = TourOSColors.Primary
                                )
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Para Birimi:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(currencyOptions) { curr ->
                            FilterChip(
                                selected = selectedCurrency == curr,
                                onClick = { onCurrencyChange(curr) },
                                label = { Text(curr, style = TourOSTypography.Caption) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TourOSColors.SecondaryContainer,
                                    selectedLabelColor = TourOSColors.Secondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VatReportSection(summary: FinancialReportSummary, symbol: String, rate: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            SummaryKpiCard(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hesaplanan KDV (Satış)"), formatConvertedMoney(summary.vatCollected, symbol, rate), TourOSColors.PrimaryContainer, TourOSColors.Primary, Modifier.weight(1f))
            SummaryKpiCard(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İndirilecek KDV (Gider)"), formatConvertedMoney(summary.vatPaid, symbol, rate), TourOSColors.SuccessContainer, TourOSColors.Success, Modifier.weight(1f))
        }

        TourOSCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TourOSColors.SecondaryContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("📌 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Net Ödenecek / Devreden KDV:")}", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.Bold)
                Text(formatConvertedMoney(summary.vatPayable, symbol, rate), style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Secondary), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RevenueExpenseReportSection(summary: FinancialReportSummary, symbol: String, rate: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
        SummaryKpiCard(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Gelir"), formatConvertedMoney(summary.totalRevenue, symbol, rate), TourOSColors.SuccessContainer, TourOSColors.Success, Modifier.weight(1f))
        SummaryKpiCard(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Gider"), formatConvertedMoney(summary.totalExpenses, symbol, rate), TourOSColors.Surface, TourOSColors.TextPrimary, Modifier.weight(1f))
        SummaryKpiCard(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Net Faaliyet Kârı"), formatConvertedMoney(summary.netProfit, symbol, rate), TourOSColors.PrimaryContainer, TourOSColors.Primary, Modifier.weight(1f))
    }
}

@Composable
private fun CashBankReportSection(summary: FinancialReportSummary, symbol: String, rate: Double) {
    val totalLiquid = summary.cashBalance + summary.bankBalance + summary.posBalance
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            SummaryKpiCard(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Nakit Kasası"), formatConvertedMoney(summary.cashBalance, symbol, rate), TourOSColors.SecondaryContainer, TourOSColors.Secondary, Modifier.weight(1f))
            SummaryKpiCard(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Banka Hesapları"), formatConvertedMoney(summary.bankBalance, symbol, rate), TourOSColors.PrimaryContainer, TourOSColors.Primary, Modifier.weight(1f))
            SummaryKpiCard(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Sanal POS Bekleyen"), formatConvertedMoney(summary.posBalance, symbol, rate), TourOSColors.SuccessContainer, TourOSColors.Success, Modifier.weight(1f))
        }
        TourOSCard(modifier = Modifier.fillMaxWidth(), backgroundColor = TourOSColors.PrimaryContainer, contentPadding = TourOSSpacing.medium) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("💰 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Likit Varlıklar:")}", style = TourOSTypography.Label.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
                Text(formatConvertedMoney(totalLiquid, symbol, rate), style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfitabilityReportSection(summary: FinancialReportSummary) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), backgroundColor = TourOSColors.SecondaryContainer, contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("🎯 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ortalama Kâr Marjı:")}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.Bold)
                Text("% ${summary.profitMarginPercentage}", style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Secondary), fontWeight = FontWeight.Bold)
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
