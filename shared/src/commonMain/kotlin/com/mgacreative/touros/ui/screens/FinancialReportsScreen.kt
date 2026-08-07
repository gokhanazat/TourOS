package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.FinancialReportSummary
import com.mgacreative.touros.ui.viewmodel.FinancialReportsViewModel

/**
 * 3.3.1 Finansal Raporlar Ekranı (KDV, Gelir, Nakit, Banka, Kârlılık).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportsScreen(
    viewModel: FinancialReportsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val summary = state.summary

    val tabs = listOf("📑 KDV", "📈 Gelir/Gider", "💵 Nakit&Banka", "📊 Kârlılık")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Finansal Raporlar & Analizler", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Tarih Filtre Seçenekleri
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.dateFilter == "30_DAYS",
                    onClick = { viewModel.setDateFilter("30_DAYS") },
                    label = { Text("📅 Son 30 Gün", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = state.dateFilter == "THIS_MONTH",
                    onClick = { viewModel.setDateFilter("THIS_MONTH") },
                    label = { Text("🗓️ Bu Ay", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = state.dateFilter == "THIS_YEAR",
                    onClick = { viewModel.setDateFilter("THIS_YEAR") },
                    label = { Text("📊 Bu Yıl", fontSize = 11.sp) }
                )
            }

            // 2. Sekmeler (TabRow replacement using ScrollableTabRow / PrimaryTabRow)
            ScrollableTabRow(selectedTabIndex = state.selectedTab, edgePadding = 0.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            // 3. Sekme İçeriği
            when (state.selectedTab) {
                0 -> VatReportView(summary = summary)
                1 -> RevenueExpenseReportView(summary = summary)
                2 -> CashBankReportView(summary = summary)
                3 -> ProfitabilityReportView(summary = summary)
            }
        }
    }
}

@Composable
fun VatReportView(summary: FinancialReportSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("📑 KDV Raporu ve Beyanname Özeti:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        ReportKpiCard(
            title = "Hesaplanan KDV (Satış Faturaları Tahsilatı)",
            amount = "${summary.vatCollected} TRY",
            badgeText = "%20 KDV",
            badgeColor = MaterialTheme.colorScheme.primary
        )

        ReportKpiCard(
            title = "İndirilecek KDV (Gider Faturaları Ödemesi)",
            amount = "${summary.vatPaid} TRY",
            badgeText = "İndirim",
            badgeColor = Color(0xFF15803D)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("📌 Net Ödenecek / Devreden KDV:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${summary.vatPayable} TRY", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun RevenueExpenseReportView(summary: FinancialReportSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("📈 Gelir ve Gider Dengesi Raporu:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        ReportKpiCard(
            title = "Toplam Brüt Gelir (Rezervasyon Tahsilatları)",
            amount = "${summary.totalRevenue} TRY",
            badgeText = "Gelir",
            badgeColor = Color(0xFF15803D)
        )

        ReportKpiCard(
            title = "Toplam Operasyonel Giderler (Tedarikçi/Otel/Rehber)",
            amount = "${summary.totalExpenses} TRY",
            badgeText = "Gider",
            badgeColor = MaterialTheme.colorScheme.error
        )

        ReportKpiCard(
            title = "Net Faaliyet Kârı",
            amount = "${summary.netProfit} TRY",
            badgeText = "Net Dönem Kârı",
            badgeColor = Color(0xFF15803D)
        )
    }
}

@Composable
fun CashBankReportView(summary: FinancialReportSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("💵 Nakit, Banka ve Sanal POS Varlık Raporu:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        ReportKpiCard(
            title = "Firma Kasaları Nakit Bakiyesi",
            amount = "${summary.cashBalance} TRY",
            badgeText = "Nakit Kasa",
            badgeColor = Color(0xFFB45309)
        )

        ReportKpiCard(
            title = "Banka Hesapları Bakiyesi (Garanti/İş Bankası)",
            amount = "${summary.bankBalance} TRY",
            badgeText = "Banka",
            badgeColor = MaterialTheme.colorScheme.primary
        )

        ReportKpiCard(
            title = "Sanal POS Bekleyen Bakiyesi (İyzico/Stripe)",
            amount = "${summary.posBalance} TRY",
            badgeText = "Sanal POS",
            badgeColor = Color(0xFF15803D)
        )

        val totalLiquid = summary.cashBalance + summary.bankBalance + summary.posBalance
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("💰 Toplam Likit Varlıklar:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("$totalLiquid TRY", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ProfitabilityReportView(summary: FinancialReportSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("📊 Şirket Kârlılık ve Marj Raporu:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯 Ortalama Net Kâr Marjı:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("%${summary.profitMarginPercentage}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                }

                LinearProgressIndicator(
                    progress = (summary.profitMarginPercentage / 100.0).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color(0xFF15803D)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Toplam Ciro", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${summary.totalRevenue} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Net Kâr Tutar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${summary.netProfit} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    }
                }
            }
        }
    }
}

@Composable
fun ReportKpiCard(title: String, amount: String, badgeText: String, badgeColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(amount, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Surface(shape = RoundedCornerShape(6.dp), color = badgeColor.copy(alpha = 0.15f)) {
                Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}
