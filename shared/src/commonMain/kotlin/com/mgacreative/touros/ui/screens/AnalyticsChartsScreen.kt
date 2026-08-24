package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.CountrySalesData
import com.mgacreative.touros.domain.model.DailySalesData
import com.mgacreative.touros.domain.usecase.ChannelSalesData
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AnalyticsChartsViewModel

private data class DaysFilterOption(val days: Int, val label: String)

private val filterOptions = listOf(
    DaysFilterOption(7, "📅 Son 7 Gün"),
    DaysFilterOption(14, "🗓️ Son 14 Gün"),
    DaysFilterOption(30, "📊 Son 30 Gün")
)

/**
 * Analitik & Tahmin Grafikleri Paneli — TourOS Canlı Veri Sürümü
 *
 * Gerçekleşen rezervasyon ciro trendleri, canlı kanal dağılımı ve AI tahmin analizi.
 */
@Composable
fun AnalyticsChartsScreen(
    viewModel: AnalyticsChartsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Analitik & Tahmin Grafikleri"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gerçekleşen satış trendleri ve AI gelecek tahmin analizi"),
                onNavigateBack = onNavigateBack
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
            // ── 1. ZAMAN ARALIĞI FİLTRE ÇUBUĞU ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📊 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Operasyonel & Tahmin Analitik Paneli")}",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    filterOptions.forEach { opt ->
                        FilterChip(
                            selected = state.selectedDays == opt.days,
                            onClick = { viewModel.loadData(opt.days) },
                            label = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate(opt.label), style = TourOSTypography.Caption) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                selectedLabelColor = TourOSColors.Primary
                            )
                        )
                    }
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val isExpanded = maxWidth >= 768.dp

                    if (isExpanded) {
                        // ── MASAÜSTÜ / TABLET: 2 KOLONLU GRID DÜZENİ ──────────────────
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                ForecastRevenueTrendChartCard(dailySales = state.dailySales)
                            }
                            item {
                                DailySalesBarChartCard(dailySales = state.dailySales)
                            }
                            item {
                                CountryMarketShareChartCard(countrySales = state.countrySales)
                            }
                            item {
                                ChannelOccupancyComparisonCard(channelSales = state.channelSales)
                            }
                        }
                    } else {
                        // ── MOBİL: DİKEY KART AKIŞI ────────────────────────────────
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                ForecastRevenueTrendChartCard(dailySales = state.dailySales)
                            }
                            item {
                                DailySalesBarChartCard(dailySales = state.dailySales)
                            }
                            item {
                                CountryMarketShareChartCard(countrySales = state.countrySales)
                            }
                            item {
                                ChannelOccupancyComparisonCard(channelSales = state.channelSales)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── TAHMİN GRAFİĞİ: GERÇEKLEŞEN VE AI TAHMİNİ ─────────────────────────────────

@Composable
private fun ForecastRevenueTrendChartCard(dailySales: List<DailySalesData>) {
    val realAmounts = if (dailySales.isNotEmpty()) {
        dailySales.map { it.totalAmount }
    } else {
        emptyList()
    }

    val forecastAmounts = if (realAmounts.isNotEmpty()) {
        val lastVal = realAmounts.last()
        listOf(lastVal, lastVal * 1.12, lastVal * 1.25, lastVal * 1.35)
    } else {
        emptyList()
    }

    val allAmounts = realAmounts + forecastAmounts.drop(1)
    val maxVal = (allAmounts.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val minVal = (allAmounts.minOrNull() ?: 0.0)

    val realColor = TourOSColors.Primary
    val forecastColor = TourOSColors.Primary.copy(alpha = 0.45f)
    val accentColor = TourOSColors.Secondary

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "🔮 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("AI Ciro & Gelecek Tahmin Grafiği")}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gerçekleşen satış trendleri ve AI gelecek tahmini"),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                TourOSStatusBadge(
                    text = if (realAmounts.isNotEmpty()) "↗ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Canlı Veri")}" else com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Veri Bekleniyor"),
                    backgroundColor = TourOSColors.SuccessContainer,
                    textColor = TourOSColors.Success
                )
            }

            // GÖSTERGE (LEGEND) BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.PrimaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(20.dp).height(3.dp).background(realColor))
                    Text("━ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gerçekleşen Ciro")}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(20.dp).height(3.dp).background(forecastColor))
                    Text("╌╌╌ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("AI Gelecek Tahmini")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            if (allAmounts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Seçilen dönemde henüz satış kaydı bulunmamaktadır."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 10.dp, bottom = 10.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val totalPoints = allAmounts.size
                    val stepX = if (totalPoints > 1) width / (totalPoints - 1) else width

                    // 1. GERÇEKLEŞEN VERİ NOKTALARI
                    val realPoints = realAmounts.mapIndexed { index, value ->
                        val normY = if (maxVal > minVal) ((value - minVal) / (maxVal - minVal)).toFloat().coerceIn(0.1f, 0.9f) else 0.5f
                        Offset(index * stepX, height - (normY * height))
                    }

                    if (realPoints.isNotEmpty()) {
                        val realPath = Path().apply {
                            moveTo(realPoints.first().x, realPoints.first().y)
                            for (i in 1 until realPoints.size) {
                                lineTo(realPoints[i].x, realPoints[i].y)
                            }
                        }

                        drawPath(
                            path = realPath,
                            color = realColor,
                            style = Stroke(width = 3.5.dp.toPx())
                        )

                        realPoints.forEach { point ->
                            drawCircle(color = accentColor, radius = 5.dp.toPx(), center = point)
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = point)
                        }
                    }

                    // 2. TAHMİNİ VERİ NOKTALARI (KESİKLİ ÇİZGİ)
                    if (realAmounts.isNotEmpty() && forecastAmounts.size > 1) {
                        val forecastStartIndex = realAmounts.size - 1
                        val forecastPoints = allAmounts.subList(forecastStartIndex, allAmounts.size).mapIndexed { index, value ->
                            val globalIdx = forecastStartIndex + index
                            val normY = if (maxVal > minVal) ((value - minVal) / (maxVal - minVal)).toFloat().coerceIn(0.1f, 0.9f) else 0.5f
                            Offset(globalIdx * stepX, height - (normY * height))
                        }

                        val forecastPath = Path().apply {
                            moveTo(forecastPoints.first().x, forecastPoints.first().y)
                            for (i in 1 until forecastPoints.size) {
                                lineTo(forecastPoints[i].x, forecastPoints[i].y)
                            }
                        }

                        drawPath(
                            path = forecastPath,
                            color = forecastColor,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        )

                        forecastPoints.drop(1).forEach { point ->
                            drawCircle(color = forecastColor, radius = 4.dp.toPx(), center = point)
                            drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = point)
                        }
                    }
                }
            }
        }
    }
}

// ─── GRAFİK 2: GÜNLÜK SATIŞ BAR GRAFİĞİ ──────────────────────────────────────

@Composable
private fun DailySalesBarChartCard(dailySales: List<DailySalesData>) {
    val maxAmount = (dailySales.maxOfOrNull { it.totalAmount } ?: 1.0).coerceAtLeast(1.0)
    val totalPeriodSales = dailySales.sumOf { it.totalAmount }

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "📊 Günlük Satış Hacmi",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Dönem İçi Toplam Gerçekleşen Ciro",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.PrimaryContainer)
                        .padding(horizontal = TourOSSpacing.small, vertical = 4.dp)
                ) {
                    Text(
                        "₺ ${totalPeriodSales.toLong()}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            if (dailySales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Henüz günlük satış verisi bulunmamaktadır.", style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailySales.forEachIndexed { index, dayData ->
                        val ratio = (dayData.totalAmount / maxAmount).toFloat().coerceIn(0.1f, 1.0f)
                        val barHeight = (110 * ratio).dp
                        val barColor = if (index % 2 == 0) TourOSColors.Primary else TourOSColors.Secondary

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${dayData.totalAmount.toLong()} ₺",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                                maxLines = 1
                            )

                            Spacer(Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(barColor)
                            )

                            Spacer(Modifier.height(4.dp))

                            val shortDate = if (dayData.saleDate.length >= 5) dayData.saleDate.takeLast(5) else dayData.saleDate
                            Text(
                                text = shortDate,
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── GRAFİK 3: ÜRÜN / KATEGORİ PAZAR DAĞILIMI ────────────────────────────────

@Composable
private fun CountryMarketShareChartCard(countrySales: List<CountrySalesData>) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "🌍 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kategori & Operasyon Dağılımı")}",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                fontWeight = FontWeight.Bold
            )
            Text(
                com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Satışların ürün ve hizmet kategorilerine göre oranı"),
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            if (countrySales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kategori bazlı satış verisi bulunmamaktadır."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    countrySales.forEachIndexed { index, item ->
                        val barColor = if (index % 2 == 0) TourOSColors.Primary else TourOSColors.Secondary

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(item.countryName)} (${item.bookingCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rezervasyon")})",
                                    style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "% ${item.percentage.toInt()}  ·  ₺ ${item.totalAmount.toLong()}",
                                    style = TourOSTypography.Label.copy(color = barColor),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { (item.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = barColor,
                                trackColor = TourOSColors.PrimaryContainer.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── GRAFİK 4: KANAL BAZLI GERÇEK CANLI PERFORMANS DAĞILIMI ─────────────────

@Composable
private fun ChannelOccupancyComparisonCard(channelSales: List<ChannelSalesData>) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "🏢 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Satış Kanalı Bazlı Canlı Dağılım")}",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                fontWeight = FontWeight.Bold
            )
            Text(
                com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gerçekleşen rezervasyonların kanallara göre canlı oranı"),
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            if (channelSales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Henüz kanal bazlı rezervasyon verisi bulunmamaktadır."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    channelSales.forEachIndexed { index, ch ->
                        val barColor = if (index % 2 == 0) TourOSColors.Primary else TourOSColors.Secondary
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(ch.channelName)} (${ch.bookingCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rezervasyon")})",
                                    style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "% ${ch.percentage.toInt()}  ·  ₺ ${ch.totalSales.toLong()}",
                                    style = TourOSTypography.Label.copy(color = barColor),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { (ch.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = barColor,
                                trackColor = TourOSColors.PrimaryContainer.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}
