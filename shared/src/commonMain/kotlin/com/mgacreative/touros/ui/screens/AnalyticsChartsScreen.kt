package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.CountrySalesData
import com.mgacreative.touros.domain.model.DailySalesData
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
 * Analytics & Tahmin Grafikleri Paneli — TourOS 0.3
 *
 * Dashboard grafiklerine eklenen 'tahmini' seri kesikli çizgi ve daha açık ton ile gösterilir.
 * Gerçek veriyle karışmaması için net bir gösterge (legend) barı içerir.
 */
@Composable
fun AnalyticsChartsScreen(
    viewModel: AnalyticsChartsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Analitik & Tahmin Grafikleri",
                subtitle = "Gerçekleşen satış trendleri ve AI gelecek tahmin analizi",
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
            // ── 1. ZAMAN ARALIĞI FİLTRE ÇUBUĞU ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📊 Operasyonel & Tahmin Analitik Paneli",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    filterOptions.forEach { opt ->
                        FilterChip(
                            selected = state.selectedDays == opt.days,
                            onClick = { viewModel.loadData(opt.days) },
                            label = { Text(opt.label, style = TourOSTypography.Caption) },
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
                            // Grafik 1: AI Tahminli Ciro Trend Grafiği (Strict Rule: Kesikli Çizgi + Legend)
                            item {
                                ForecastRevenueTrendChartCard(dailySales = state.dailySales)
                            }

                            // Grafik 2: Bar Chart (Günlük Satışlar)
                            item {
                                DailySalesBarChartCard(dailySales = state.dailySales)
                            }

                            // Grafik 3: Ülke Pazar Dağılımı
                            item {
                                CountryMarketShareChartCard(countrySales = state.countrySales)
                            }

                            // Grafik 4: Kanal Bazlı Karşılaştırmalı Doluluk
                            item {
                                ChannelOccupancyComparisonCard()
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
                                ChannelOccupancyComparisonCard()
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── TAHMİN GRAFİĞİ: GERÇEKLEŞEN (DÜZ ÇİZGİ) VE AI TAHMİNİ (KESİKLİ ÇİZGİ) ─────

@Composable
private fun ForecastRevenueTrendChartCard(dailySales: List<DailySalesData>) {
    // Örnek: İlk 4 eleman Gerçekleşen Veri, son 3 eleman AI Tahmin Verisi
    val realAmounts = listOf(14500.0, 18200.0, 16800.0, 22400.0)
    val forecastAmounts = listOf(22400.0, 25800.0, 28400.0, 31200.0)

    val allAmounts = realAmounts + forecastAmounts.drop(1)
    val maxVal = (allAmounts.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val minVal = (allAmounts.minOrNull() ?: 0.0)

    val realColor = TourOSColors.Primary
    val forecastColor = TourOSColors.Primary.copy(alpha = 0.45f) // Daha açık ton (Strict Rule)
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
                        "🔮 AI Ciro & Gelecek Tahmin Grafiği",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                    Text(
                        "Gerçekleşen trend ve makine öğrenimi tahmini",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                TourOSStatusBadge(
                    text = "↗ %24 Gelecek Tahmini",
                    backgroundColor = TourOSColors.SuccessContainer,
                    textColor = TourOSColors.Success
                )
            }

            // ── GERÇEK VERİYLE KARIŞMAMASI İÇİN NET GÖSTERGE (LEGEND) (Strict Rule) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.PrimaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Legend 1: Gerçekleşen (Düz Çizgi)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(3.dp)
                            .background(realColor)
                    )
                    Text(
                        "━ Gerçekleşen Ciro",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                    )
                }

                // Legend 2: Tahmini Seri (Kesikli Çizgi & Açık Ton)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(3.dp)
                            .background(forecastColor)
                    )
                    Text(
                        "╌╌╌ AI Gelecek Tahmini",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // CANVAS İLE GERÇEKLEŞEN (DÜZ) VE TAHMİNİ (KESİKLİ ÇİZGİ) GRAFİK ÇİZİMİ
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                val width = size.width
                val height = size.height
                val totalPoints = allAmounts.size
                val stepX = width / (totalPoints - 1)

                // 1. GERÇEKLEŞEN VERİ NOKTALARI (DÜZ ÇİZGİ - SOLID PATH)
                val realPoints = realAmounts.mapIndexed { index, value ->
                    val normY = ((value - minVal) / (maxVal - minVal)).toFloat().coerceIn(0.1f, 0.9f)
                    Offset(index * stepX, height - (normY * height))
                }

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

                // 2. TAHMİNİ VERİ NOKTALARI (KESİKLİ ÇİZGİ - DASHED PATH & AÇIK TON) (Strict Rule)
                val forecastStartIndex = realAmounts.size - 1
                val forecastPoints = allAmounts.subList(forecastStartIndex, allAmounts.size).mapIndexed { index, value ->
                    val globalIdx = forecastStartIndex + index
                    val normY = ((value - minVal) / (maxVal - minVal)).toFloat().coerceIn(0.1f, 0.9f)
                    Offset(globalIdx * stepX, height - (normY * height))
                }

                val forecastPath = Path().apply {
                    moveTo(forecastPoints.first().x, forecastPoints.first().y)
                    for (i in 1 until forecastPoints.size) {
                        lineTo(forecastPoints[i].x, forecastPoints[i].y)
                    }
                }

                // KESİKLİ ÇİZGİ EFEKTİ (DASHED STROKE)
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
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                    Text(
                        "Dönem İçi Toplam Satış",
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
                        "₺ ${(totalPeriodSales.toInt())}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

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
                            text = "${(dayData.totalAmount / 1000).toInt()}k",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
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
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            }
        }
    }
}

// ─── GRAFİK 3: ÜLKE PAZAR DAĞILIMI ───────────────────────────────────────────

@Composable
private fun CountryMarketShareChartCard(countrySales: List<CountrySalesData>) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "🌐 Ülke Pazar Payı Dağılımı",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )
            Text(
                "Yabancı turist kaynak pazarları",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                countrySales.forEachIndexed { index, country ->
                    val flag = when (country.countryCode.uppercase()) {
                        "DE" -> "🇩🇪"
                        "GB" -> "🇬🇧"
                        "RU" -> "🇷🇺"
                        "US" -> "🇺🇸"
                        "AE" -> "🇦🇪"
                        else -> "🇹🇷"
                    }
                    val barColor = if (index % 2 == 0) TourOSColors.Primary else TourOSColors.Secondary

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$flag ${country.countryName} (${country.bookingCount} Rezervasyon)",
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                            )
                            Text(
                                "% ${country.percentage}  ·  ₺ ${(country.totalAmount.toInt())}",
                                style = TourOSTypography.Label.copy(color = barColor)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (country.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
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

// ─── GRAFİK 4: KANAL BAZLI KARŞILAŞTIRMALI DOLULUK ───────────────────────────

@Composable
private fun ChannelOccupancyComparisonCard() {
    val channels = listOf(
        ChannelData("B2C Web Direct", 68.0, TourOSColors.Primary),
        ChannelData("Acente (B2B)", 82.0, TourOSColors.Secondary),
        ChannelData("OTA Entegrasyon", 45.0, TourOSColors.Primary),
        ChannelData("Mobil Uygulama", 54.0, TourOSColors.Secondary)
    )

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "🏢 Satış Kanalı Bazlı Doluluk Performansı",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )
            Text(
                "Kanal karşılaştırmalı doluluk oranları",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                channels.forEach { ch ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ch.name, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                            Text("% ${ch.occupancy.toInt()}", style = TourOSTypography.Label.copy(color = ch.color))
                        }

                        LinearProgressIndicator(
                            progress = { (ch.occupancy / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = ch.color,
                            trackColor = TourOSColors.PrimaryContainer.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

private data class ChannelData(val name: String, val occupancy: Double, val color: Color)
