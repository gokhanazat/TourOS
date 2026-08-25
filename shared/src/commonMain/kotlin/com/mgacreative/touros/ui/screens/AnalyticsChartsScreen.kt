package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
 * Analitik & Satış Trendleri Paneli — TourOS Kurumsal İstatistik ve Performans Paneli
 *
 * Gerçekleşen rezervasyon ciro trendleri, operasyonel kategori dağılımı ve kanal analitiği.
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
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Analitik & Satış Trendleri"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gerçekleşen satış trendleri, operasyonel kategori ve kanal performans dökümü"),
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.small),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            // ── 1. ZAMAN ARALIĞI FİLTRE ÇUBUĞU ────────────────────────────
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📊 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Operasyonel Trend & İstatistik Paneli")}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        filterOptions.forEach { opt ->
                            val isSelected = state.selectedDays == opt.days
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, if (isSelected) TourOSColors.Primary else TourOSColors.Border, RoundedCornerShape(14.dp))
                                    .clickable { viewModel.loadData(opt.days) },
                                color = if (isSelected) TourOSColors.Primary else TourOSColors.Surface
                            ) {
                                Text(
                                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(opt.label),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = TourOSTypography.Caption,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TourOSColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. TEK SATIRLIK KPI FİNANSAL PERFORMANS ŞERİDİ ──────────────
            val topCategory = state.countrySales.maxByOrNull { it.totalAmount }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.45f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Primary.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TourOSSpacing.medium, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💰 Toplam Ciro: ₺ ${formatAmount(state.totalRevenue)}",
                            style = TourOSTypography.BodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TourOSColors.Primary
                        )
                        Text(
                            text = "📋 Toplam Rezervasyon: ${state.totalBookingsCount} Adet (${state.totalPaxOrNights} Pax/Gece)",
                            style = TourOSTypography.Caption,
                            color = TourOSColors.TextPrimary
                        )
                        Text(
                            text = "📈 Ort. Sepet: ₺ ${formatAmount(state.averageBookingValue)}",
                            style = TourOSTypography.Caption,
                            color = TourOSColors.TextSecondary
                        )
                    }

                    if (topCategory != null && topCategory.totalAmount > 0) {
                        Text(
                            text = "🏆 Lider Operasyon: ${topCategory.countryName} (%${topCategory.percentage.toInt()})",
                            style = TourOSTypography.Caption,
                            fontWeight = FontWeight.Bold,
                            color = TourOSColors.Primary
                        )
                    }
                }
            }

            // ── 3. GRAFİK GRID DÜZENİ ───────────────────────────────────────
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val isExpanded = maxWidth >= 768.dp

                    if (isExpanded) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                RevenueTrendChartCard(dailySales = state.dailySales)
                            }
                            item {
                                DailySalesBarChartCard(dailySales = state.dailySales)
                            }
                            item {
                                OperationalCategoryDistributionCard(categorySales = state.countrySales)
                            }
                            item {
                                ChannelOccupancyComparisonCard(channelSales = state.channelSales)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                RevenueTrendChartCard(dailySales = state.dailySales)
                            }
                            item {
                                DailySalesBarChartCard(dailySales = state.dailySales)
                            }
                            item {
                                OperationalCategoryDistributionCard(categorySales = state.countrySales)
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

// ─── GRAFİK 1: CİRO VE DÖNEMSEL TREND ÇİZGİSİ ──────────────────────────────────

@Composable
private fun RevenueTrendChartCard(dailySales: List<DailySalesData>) {
    val realAmounts = if (dailySales.isNotEmpty()) {
        dailySales.map { it.totalAmount }
    } else {
        emptyList()
    }

    val avgAmount = if (realAmounts.isNotEmpty()) realAmounts.average() else 0.0
    val maxVal = (realAmounts.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val minVal = (realAmounts.minOrNull() ?: 0.0)

    val realColor = TourOSColors.Primary
    val trendColor = Color(0xFFE5A93C)

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.medium) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "📈 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Dönemsel Satış & Trend Çizgisi")}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gerçekleşen ciro ve dönemsel ortalama hareket trendi"),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                TourOSStatusBadge(
                    text = if (realAmounts.isNotEmpty()) "● Canlı Trend" else "Veri Bekleniyor",
                    backgroundColor = TourOSColors.SuccessContainer,
                    textColor = TourOSColors.Success
                )
            }

            // GÖSTERGE (LEGEND)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.PrimaryContainer.copy(alpha = 0.35f))
                    .padding(horizontal = TourOSSpacing.medium, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(18.dp).height(3.dp).background(realColor))
                    Text("━ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gerçekleşen Ciro")}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(18.dp).height(3.dp).background(trendColor))
                    Text("╌╌ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Dönem Ortalaması")}: ₺ ${formatAmount(avgAmount)}", style = TourOSTypography.Caption.copy(color = trendColor), fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = TourOSColors.Border)

            if (realAmounts.isEmpty()) {
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
                    val totalPoints = realAmounts.size
                    val stepX = if (totalPoints > 1) width / (totalPoints - 1) else width

                    val realPoints = realAmounts.mapIndexed { index, value ->
                        val normY = if (maxVal > minVal) ((value - minVal) / (maxVal - minVal)).toFloat().coerceIn(0.15f, 0.85f) else 0.5f
                        Offset(index * stepX, height - (normY * height))
                    }

                    if (realPoints.isNotEmpty()) {
                        // Alan Gölgelendirmesi (Area Gradient Fill)
                        val areaPath = Path().apply {
                            moveTo(realPoints.first().x, height)
                            realPoints.forEach { lineTo(it.x, it.y) }
                            lineTo(realPoints.last().x, height)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(realColor.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )

                        // Gerçekleşen Çizgi
                        val realPath = Path().apply {
                            moveTo(realPoints.first().x, realPoints.first().y)
                            for (i in 1 until realPoints.size) {
                                lineTo(realPoints[i].x, realPoints[i].y)
                            }
                        }
                        drawPath(
                            path = realPath,
                            color = realColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Ortalama Trend Çizgisi
                        val normAvgY = if (maxVal > minVal) ((avgAmount - minVal) / (maxVal - minVal)).toFloat().coerceIn(0.15f, 0.85f) else 0.5f
                        val avgLineY = height - (normAvgY * height)
                        drawLine(
                            color = trendColor,
                            start = Offset(0f, avgLineY),
                            end = Offset(width, avgLineY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        )

                        realPoints.forEach { point ->
                            drawCircle(color = realColor, radius = 5.dp.toPx(), center = point)
                            drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = point)
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

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.medium) {
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
                        "Dönem İçi Toplam Gerçekleşen Ciro Dağılımı",
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
                        "₺ ${formatAmount(totalPeriodSales)}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = TourOSColors.Border)

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
                        val barHeight = (105 * ratio).dp
                        val barColor = if (index % 2 == 0) TourOSColors.Primary else TourOSColors.Secondary

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "₺${formatAmount(dayData.totalAmount)}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                                maxLines = 1
                            )

                            Spacer(Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(22.dp)
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

// ─── GRAFİK 3: 5 ANA OPERASYONEL KATEGORİ DAĞILIMI ───────────────────────────

@Composable
private fun OperationalCategoryDistributionCard(categorySales: List<CountrySalesData>) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.medium) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "🌍 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kategori & Operasyon Dağılımı")}",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                fontWeight = FontWeight.Bold
            )
            Text(
                com.mgacreative.touros.ui.localization.AppLanguageManager.translate("TO Paketleri, Otel, Yerel Tur, Uçuş ve Transfer kategorilerine göre satış oranı"),
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Border)

            if (categorySales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kategori bazlı satış verisi bulunmamaktadır."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categorySales.forEach { item ->
                        val icon = when (item.countryCode) {
                            "HTL" -> "🏨"
                            "TO_PKG" -> "💼"
                            "LOCAL_TOUR" -> "🚌"
                            "FLIGHT" -> "✈️"
                            "TRANSFER" -> "🚐"
                            else -> "📍"
                        }
                        val barColor = when (item.countryCode) {
                            "HTL" -> TourOSColors.Primary
                            "TO_PKG" -> Color(0xFFE5A93C)
                            "LOCAL_TOUR" -> Color(0xFF2E7D32)
                            "FLIGHT" -> Color(0xFF0288D1)
                            "TRANSFER" -> Color(0xFF7B1FA2)
                            else -> TourOSColors.Secondary
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(icon, style = TourOSTypography.Caption)
                                    Text(
                                        text = "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(item.countryName)} (${item.bookingCount} Rezervasyon)",
                                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "% ${item.percentage.toInt()}  ·  ₺ ${formatAmount(item.totalAmount)}",
                                    style = TourOSTypography.Label.copy(color = if (item.totalAmount > 0) barColor else TourOSColors.TextSecondary),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { (item.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = barColor,
                                trackColor = TourOSColors.PrimaryContainer.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── GRAFİK 4: SATIŞ KANALI DAĞILIMI ─────────────────────────────────────────

@Composable
private fun ChannelOccupancyComparisonCard(channelSales: List<ChannelSalesData>) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.medium) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "🏢 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Satış Kanalı Bazlı Canlı Dağılım")}",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                fontWeight = FontWeight.Bold
            )
            Text(
                com.mgacreative.touros.ui.localization.AppLanguageManager.translate("B2C Web, B2B Acente Portalı ve Ofis/Çağrı Merkezi canlı satış payları"),
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Border)

            if (channelSales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Henüz kanal bazlı rezervasyon verisi bulunmamaktadır."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    channelSales.forEach { ch ->
                        val icon = when {
                            ch.channelName.contains("B2C", ignoreCase = true) -> "🌐"
                            ch.channelName.contains("B2B", ignoreCase = true) -> "🏢"
                            else -> "📞"
                        }
                        val barColor = when {
                            ch.channelName.contains("B2C", ignoreCase = true) -> TourOSColors.Primary
                            ch.channelName.contains("B2B", ignoreCase = true) -> Color(0xFF1E88E5)
                            else -> Color(0xFF6D4C41)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(icon, style = TourOSTypography.Caption)
                                    Text(
                                        text = "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(ch.channelName)} (${ch.bookingCount} Rezervasyon)",
                                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "% ${ch.percentage.toInt()}  ·  ₺ ${formatAmount(ch.totalSales)}",
                                    style = TourOSTypography.Label.copy(color = if (ch.totalSales > 0) barColor else TourOSColors.TextSecondary),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { (ch.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = barColor,
                                trackColor = TourOSColors.PrimaryContainer.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatAmount(value: Double): String {
    return com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(value, decimals = false)
}
