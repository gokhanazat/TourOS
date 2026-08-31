package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

import com.mgacreative.touros.utils.DateUtils

enum class CalendarViewMode(val title: String) {
    MONTH("Aylık"),
    WEEK("Haftalık"),
    LIST("Liste")
}

data class CalendarEventItem(
    val id: String,
    val tourTitle: String,
    val departureDate: String, // YYYY-MM-DD
    val price: Double,
    val bookedCount: Int,
    val capacity: Int,
    val status: String,
    val isGuaranteed: Boolean = false
) {
    val occupancyRatio: Float
        get() = (bookedCount.toFloat() / capacity.coerceAtLeast(1).toFloat())

    val occupancyColor: Color
        get() = when {
            occupancyRatio >= 1.0f -> Color(0xFFD32F2F) // 🔴 Kırmızı (Tamamen Doldu)
            occupancyRatio >= 0.70f -> Color(0xFFED6C02) // 🟡 Sarı/Turuncu (Kritik / Yüksek Doluluk)
            else -> Color(0xFF2E7D32)                   // 🟢 Yeşil (Müsait Kontenjan)
        }

    val occupancyStatusText: String
        get() = when {
            occupancyRatio >= 1.0f -> "Doldu"
            occupancyRatio >= 0.70f -> "Yoğun"
            else -> "Müsait"
        }
}

/**
 * 2.2.2 & 2.2.4 Doluluk Renk Kodlamalı Ortak CalendarView Composable Bileşeni.
 * Yeşil (<%70), Sarı (%70-%99), Kırmızı (%100) renk kodlaması ile doluluk takibi.
 */
@Composable
fun CalendarView(
    events: List<CalendarEventItem>,
    modifier: Modifier = Modifier,
    onDepartureSelected: (String) -> Unit = {}
) {
    var selectedMode by remember { mutableStateOf(CalendarViewMode.MONTH) }
    var selectedMonthName by remember { mutableStateOf(DateUtils.getCurrentMonthAndYear()) }
    var selectedDate by remember { mutableStateOf(DateUtils.getTodayIso()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Header & Görünüm Değiştirme Tabları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text(
                        text = selectedMonthName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {}) {
                        Text(">", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                SingleChoiceSegmentedButtonRow {
                    CalendarViewMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = CalendarViewMode.entries.size),
                            onClick = { selectedMode = mode },
                            selected = selectedMode == mode
                        ) {
                            Text(mode.title, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🟢🟡🔴 Doluluk Lejantı (Legend)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFF2E7D32), label = "Müsait (<%70)")
                Spacer(modifier = Modifier.width(12.dp))
                LegendItem(color = Color(0xFFED6C02), label = "Yoğun (%70-%99)")
                Spacer(modifier = Modifier.width(12.dp))
                LegendItem(color = Color(0xFFD32F2F), label = "Doldu (%100)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedMode) {
                CalendarViewMode.MONTH -> MonthCalendarView(events = events, selectedDate = selectedDate, onDateSelected = { selectedDate = it }, onDepartureClick = onDepartureSelected)
                CalendarViewMode.WEEK -> WeekCalendarView(events = events, selectedDate = selectedDate, onDepartureClick = onDepartureSelected)
                CalendarViewMode.LIST -> ListCalendarView(events = events, onDepartureClick = onDepartureSelected)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MonthCalendarView(
    events: List<CalendarEventItem>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onDepartureClick: (String) -> Unit
) {
    val dayNames = DateUtils.dayNamesTr
    val today = DateUtils.getToday()
    val currentMonth = today.second
    val currentYear = today.third
    val totalDays = DateUtils.getDaysInMonth(currentMonth, currentYear)
    val monthStr = currentMonth.toString().padStart(2, '0')
    val daysInMonth = (1..totalDays).map { day ->
        val dayStr = if (day < 10) "0$day" else "$day"
        "$currentYear-$monthStr-$dayStr"
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            dayNames.forEach { dayName ->
                Text(
                    text = dayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().height(320.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(daysInMonth) { dateStr ->
                val dayNumber = dateStr.takeLast(2)
                val dayEvents = events.filter { it.departureDate == dateStr }
                val isSelected = dateStr == selectedDate
                val firstEvent = dayEvents.firstOrNull()

                Box(
                    modifier = Modifier
                        .height(55.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onDateSelected(dateStr) }
                        .padding(4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = dayNumber,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (firstEvent != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(firstEvent.occupancyColor)
                                    .clickable { onDepartureClick(firstEvent.id) }
                                    .padding(vertical = 1.dp, horizontal = 2.dp)
                            ) {
                                Text(
                                    text = "${firstEvent.bookedCount}/${firstEvent.capacity}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekCalendarView(
    events: List<CalendarEventItem>,
    selectedDate: String,
    onDepartureClick: (String) -> Unit
) {
    val dayNames = DateUtils.dayNamesTr
    val today = DateUtils.getToday()
    val weekDays = (0..6).map { offset ->
        val dateTriple = com.mgacreative.touros.addDaysToTriple(today, offset)
        val ds = dateTriple.first.toString().padStart(2, '0')
        val ms = dateTriple.second.toString().padStart(2, '0')
        val ys = dateTriple.third.toString()
        val iso = "$ys-$ms-$ds"
        val label = "${dayNames[offset % 7]} ${dateTriple.first}"
        iso to label
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            weekDays.forEach { (dateStr, label) ->
                val dayEvents = events.filter { it.departureDate == dateStr }
                val firstEvent = dayEvents.firstOrNull()

                Card(
                    modifier = Modifier.weight(1f).height(60.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dateStr == selectedDate) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        if (firstEvent != null) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(firstEvent.occupancyColor))
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        Text("Haftalık Tur Seferleri:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        LazyColumn(modifier = Modifier.height(200.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(events) { event ->
                CalendarEventRowCard(event = event, onDepartureClick = onDepartureClick)
            }
        }
    }
}

@Composable
private fun ListCalendarView(
    events: List<CalendarEventItem>,
    onDepartureClick: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.height(320.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(events) { event ->
            CalendarEventRowCard(event = event, onDepartureClick = onDepartureClick)
        }
    }
}

@Composable
private fun CalendarEventRowCard(
    event: CalendarEventItem,
    onDepartureClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDepartureClick(event.id) },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.tourTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    // Renk Kodlu Doluluk Rozeti (🟢 Yeşil, 🟡 Sarı, 🔴 Kırmızı)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = event.occupancyColor
                    ) {
                        Text(
                            text = "${event.occupancyStatusText} (${event.bookedCount}/${event.capacity})",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    if (event.isGuaranteed) {
                        Spacer(modifier = Modifier.width(4.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("✓ Garanti", fontSize = 9.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("📅 Tarih: ${event.departureDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = "${event.price.toInt()} TRY",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
        }
    }
}
