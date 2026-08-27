package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.ChannelSalesItem
import com.mgacreative.touros.domain.model.CountrySalesItem
import com.mgacreative.touros.domain.model.DashboardAnalyticsCharts
import com.mgacreative.touros.domain.model.MonthlyTrendItem
import com.mgacreative.touros.domain.model.TourRevenueItem
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

/**
 * TourOS 0.3 Tasarım Sistemine uygun Operasyonel Analytics Grafik Paneli.
 * Renk Kuralı: Sadece TourOSColors.Primary ve Secondary (Accent) tonlarının opaklıkları kullanılır.
 */
@Composable
fun DashboardChartsSection(
    charts: DashboardAnalyticsCharts,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = TourOSColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Operasyonel Analytics & Trend Analizi"),
                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
            )
        }

        // Row 1: Aylık Satış Trendi & Ülkelere Göre Satış
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = TourOSColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Aylık Satış Trendi (Ciro)"),
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                MonthlySalesTrendChart(items = charts.monthlyTrends)
            }

            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = TourOSColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ülkelere Göre Satış Dağılımı"),
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                SalesByCountryChart(items = charts.countrySales)
            }
        }

        // Row 2: Tur Bazlı Gelir & Kanal Bazlı Satış
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = null,
                        tint = TourOSColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tur Bazlı Gelir Sıralaması"),
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                TourRevenueChart(items = charts.tourRevenues)
            }

            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = TourOSColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kanal Bazlı Satış Dağılımı"),
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                SalesByChannelChart(items = charts.channelSales)
            }
        }
    }
}

@Composable
fun MonthlySalesTrendChart(items: List<MonthlyTrendItem>) {
    val maxVal = items.maxOfOrNull { it.sales } ?: 1.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        items.forEach { item ->
            val fillRatio = (item.sales / maxVal).toFloat().coerceIn(0.1f, 1.0f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "${(item.sales / 1000).toInt()}k",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .fillMaxHeight(fillRatio)
                        .clip(RoundedCornerShape(topStart = TourOSSpacing.cornerRadiusSmall, topEnd = TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.Primary)
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                Text(text = item.monthName, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
            }
        }
    }
}

@Composable
fun SalesByCountryChart(items: List<CountrySalesItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small), modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = item.countryName, style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
                    Text(text = "%${item.percentage.toInt()} (${item.revenue.toInt()} TRY)", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                }
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                LinearProgressIndicator(
                    progress = { item.percentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)),
                    color = TourOSColors.Primary,
                    trackColor = TourOSColors.PrimaryContainer
                )
            }
        }
    }
}

@Composable
fun TourRevenueChart(items: List<TourRevenueItem>) {
    val maxRev = items.maxOfOrNull { it.revenue } ?: 1.0

    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small), modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            val ratio = (item.revenue / maxRev).toFloat().coerceIn(0.05f, 1f)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = item.tourTitle, style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary), maxLines = 1)
                    Text(text = "${item.revenue.toInt()} TRY", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary))
                }
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)),
                    color = TourOSColors.Secondary,
                    trackColor = TourOSColors.Secondary.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun SalesByChannelChart(items: List<ChannelSalesItem>) {
    val colors = listOf(
        TourOSColors.Primary,
        TourOSColors.Primary.copy(alpha = 0.6f),
        TourOSColors.Secondary,
        TourOSColors.Secondary.copy(alpha = 0.5f)
    )

    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
        ) {
            items.forEachIndexed { index, item ->
                val weight = (item.percentage / 100f).coerceAtLeast(0.01f)
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxHeight()
                        .background(colors.getOrElse(index) { TourOSColors.Primary })
                )
            }
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

        items.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.getOrElse(index) { TourOSColors.Primary })
                    )
                    Spacer(modifier = Modifier.width(TourOSSpacing.small))
                    Text(text = item.channelName, style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
                }
                Text(text = "%${item.percentage.toInt()} (${item.amount.toInt()} TRY)", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
            }
        }
    }
}
