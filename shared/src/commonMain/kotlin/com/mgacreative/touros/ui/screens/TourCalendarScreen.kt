package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSColumn
import com.mgacreative.touros.ui.components.TourOSDataTable
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

enum class CalendarViewMode(val title: String) {
    WEEKLY("Haftalık"),
    MONTHLY("Aylık"),
    LIST("Liste")
}

data class CalendarDepartureItem(
    val id: String,
    val dayNumber: Int,
    val monthName: String = "Ağustos",
    val tourTitle: String,
    val code: String,
    val bookedCount: Int,
    val capacity: Int
) {
    val occupancyPercentage: Int get() = if (capacity > 0) (bookedCount * 100 / capacity) else 0

    // Doluluk Noktası Rengi (Hücre arka planı renklendirilmez, sadece nokta gösterilir)
    val indicatorColor: Color
        get() = when {
            occupancyPercentage >= 80 -> TourOSColors.Success
            occupancyPercentage >= 50 -> TourOSColors.Warning
            else -> TourOSColors.Error
        }
}

/**
 * TourOS 0.3 Tasarım Sistemine uygun Tur Takvimi Ekranı.
 * - Üstte Görünüm Anahtarı (Haftalık / Aylık / Liste) Segmented Button.
 * - Takvim hücrelerinde doluluk durumu SADECE Success/Warning/Error renkli küçük nokta ile gösterilir (hücre arka planı renklendirilmez).
 */
@Composable
fun TourCalendarScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToTourDetail: (String) -> Unit = {}
) {
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTHLY) }
    var selectedMonth by remember { mutableStateOf("Ağustos 2026") }

    val sampleDepartures = remember {
        listOf(
            CalendarDepartureItem("1", 2, "Ağustos", "Kapadokya Balon Turu", "KPD-101", 28, 30),
            CalendarDepartureItem("2", 5, "Ağustos", "Ege Koyları Mavi Yolculuk", "EGE-202", 15, 20),
            CalendarDepartureItem("3", 9, "Ağustos", "Karadeniz Yayla Turu", "KDN-303", 10, 25),
            CalendarDepartureItem("4", 12, "Ağustos", "İstanbul Boğaz & Kültür", "IST-404", 24, 25),
            CalendarDepartureItem("5", 16, "Ağustos", "Antalya VIP Safari", "ANT-505", 5, 15),
            CalendarDepartureItem("6", 20, "Ağustos", "GAP Kültür Turu", "GAP-606", 18, 20),
            CalendarDepartureItem("7", 25, "Ağustos", "Bursa Uludağ Günübirlik", "BRS-707", 22, 22),
            CalendarDepartureItem("8", 28, "Ağustos", "Pamukkale & Efes Turu", "PMK-808", 8, 30)
        )
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Tur Takvimi & Kalkış Planı",
                subtitle = "Tüm tur hareket tarihlerini ve doluluk oranlarını takip edin",
                actions = {
                    TourOSButton(
                        text = "+ Yeni Kalkış Ekle",
                        onClick = { },
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            )
        },
        containerColor = TourOSColors.Surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // Görünüm Modu Segmented Bar & Ay Seçici
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.medium
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ay Gezinme Butonları
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TourOSButton(text = "‹", onClick = { }, variant = TourOSButtonVariant.TERTIARY)
                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                        Text(
                            text = selectedMonth,
                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                        )
                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                        TourOSButton(text = "›", onClick = { }, variant = TourOSButtonVariant.TERTIARY)
                    }

                    // Görünüm Anahtarı (Segmented Button Group)
                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                        CalendarViewMode.entries.forEach { mode ->
                            val isSelected = viewMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewMode = mode },
                                label = { Text(mode.title, style = TourOSTypography.BodyMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TourOSColors.PrimaryContainer,
                                    selectedLabelColor = TourOSColors.Primary,
                                    containerColor = TourOSColors.Surface,
                                    labelColor = TourOSColors.TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Doluluk Lejantı (Legend)
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Doluluk Durumu:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                LegendItem(color = TourOSColors.Success, label = "Yüksek Doluluk (%80+)")
                LegendItem(color = TourOSColors.Warning, label = "Orta Doluluk (%50-%80)")
                LegendItem(color = TourOSColors.Error, label = "Kritik / Düşük Doluluk (<%50)")
            }

            // Takvim İçeriği Moduna Göre Render Et
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (viewMode) {
                    CalendarViewMode.MONTHLY -> MonthlyCalendarGrid(departures = sampleDepartures, onDepartureClick = onNavigateToTourDetail)
                    CalendarViewMode.WEEKLY -> WeeklyCalendarGrid(departures = sampleDepartures, onDepartureClick = onNavigateToTourDetail)
                    CalendarViewMode.LIST -> DeparturesListView(departures = sampleDepartures, onDepartureClick = onNavigateToTourDetail)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
        Text(text = label, style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
    }
}

@Composable
private fun MonthlyCalendarGrid(
    departures: List<CalendarDepartureItem>,
    onDepartureClick: (String) -> Unit
) {
    val weekDays = listOf("PZT", "SAL", "ÇAR", "PER", "CUM", "CTS", "PAZ")

    Column(modifier = Modifier.fillMaxSize()) {
        // Gün Başlıkları
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = TourOSSpacing.small),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }
            }
        }

        // 31 Günlük Izgara (Grid)
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
            modifier = Modifier.fillMaxSize()
        ) {
            items((1..31).toList()) { dayNum ->
                val dayDepartures = departures.filter { it.dayNumber == dayNum }

                // Hücre arka planı daima nötr Surface/Background. Arka plan ASLA renklendirilmez.
                TourOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    backgroundColor = TourOSColors.Background,
                    borderColor = TourOSColors.Border,
                    contentPadding = TourOSSpacing.xSmall
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dayNum.toString(),
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )

                        if (dayDepartures.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xxSmall)) {
                                dayDepartures.forEach { dep ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                            .background(TourOSColors.PrimaryContainer)
                                            .clickable { onDepartureClick(dep.id) }
                                            .padding(horizontal = TourOSSpacing.xxSmall, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = dep.code,
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Doluluk Durum Noktası (Dot Indicator) - Hücre zeminine değil sadece noktaya verilir
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(dep.indicatorColor)
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(1.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyCalendarGrid(
    departures: List<CalendarDepartureItem>,
    onDepartureClick: (String) -> Unit
) {
    val weekDays = listOf("10 Ağu Pazartesi", "11 Ağu Salı", "12 Ağu Çarşamba", "13 Ağu Perşembe", "14 Ağu Cuma", "15 Ağu Cumartesi", "16 Ağu Pazar")

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
    ) {
        weekDays.forEachIndexed { index, dayTitle ->
            val dayNum = index + 10
            val dayDepartures = departures.filter { it.dayNumber == dayNum }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = dayTitle,
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                    modifier = Modifier.padding(bottom = TourOSSpacing.small)
                )

                TourOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    backgroundColor = TourOSColors.Background,
                    borderColor = TourOSColors.Border,
                    contentPadding = TourOSSpacing.small
                ) {
                    if (dayDepartures.isEmpty()) {
                        Text(text = "Kalkış yok", style = TourOSTypography.Caption.copy(color = TourOSColors.TextDisabled))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            items(dayDepartures) { dep ->
                                TourOSCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onDepartureClick(dep.id) },
                                    backgroundColor = TourOSColors.Surface,
                                    borderColor = TourOSColors.Border,
                                    contentPadding = TourOSSpacing.small
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xxSmall)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = dep.code, style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                                            // Doluluk Noktası
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(dep.indicatorColor)
                                            )
                                        }

                                        Text(text = dep.tourTitle, style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
                                        Text(text = "Doluluk: ${dep.bookedCount}/${dep.capacity}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeparturesListView(
    departures: List<CalendarDepartureItem>,
    onDepartureClick: (String) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 768.dp

        val columns = listOf(
            TourOSColumn<CalendarDepartureItem>(title = "TARIH", weight = 1f) { dep ->
                Text(text = "${dep.dayNumber} ${dep.monthName}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
            },
            TourOSColumn<CalendarDepartureItem>(title = "TUR ADI & KODU", weight = 2.5f) { dep ->
                Column {
                    Text(text = dep.tourTitle, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                    Text(text = "Kod: ${dep.code}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                }
            },
            TourOSColumn<CalendarDepartureItem>(title = "KONTENJAN", weight = 1.2f) { dep ->
                Text(text = "${dep.bookedCount} / ${dep.capacity} Pax", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
            },
            TourOSColumn<CalendarDepartureItem>(title = "DOLULUK DURUMU", weight = 1.5f) { dep ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dep.indicatorColor)
                    )
                    Spacer(modifier = Modifier.width(TourOSSpacing.small))
                    Text(
                        text = "%${dep.occupancyPercentage} Dolu",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                }
            }
        )

        TourOSDataTable(
            items = departures,
            columns = columns,
            isCompact = isCompact,
            modifier = Modifier.fillMaxSize(),
            onItemClick = { onDepartureClick(it.id) },
            compactCardContent = { dep ->
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${dep.dayNumber} ${dep.monthName} • ${dep.code}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(dep.indicatorColor)
                            )
                            Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))
                            Text(text = "%${dep.occupancyPercentage}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
                        }
                    }
                    Text(text = dep.tourTitle, style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary))
                    Text(text = "Rezakvasyon: ${dep.bookedCount} / ${dep.capacity} Koltuk", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                }
            }
        )
    }
}
