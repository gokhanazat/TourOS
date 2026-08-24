package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSTypography

/**
 * Booking.com standartlarında Çift Ay (Dual-Month) Yan Yana Tarih Aralığı ve ±3 Gün Esneklik Seçici Dialog'u.
 */
@Composable
fun DualMonthRangeDatePickerDialog(
    initialStartDateText: String = "20.08.2026",
    initialEndDateText: String = "28.08.2026",
    initialFlexibilityDays: Int = 3,
    onRangeSelected: (startDate: String, endDate: String, nights: Int, flexDays: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val monthNames = listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
    val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    fun parseDate(str: String, defDay: Int, defMonth: Int, defYear: Int): Triple<Int, Int, Int> {
        val parts = if (str.contains(".")) str.split(".")
        else if (str.contains("-")) {
            val p = str.split("-")
            if (p.size == 3 && p[0].length == 4) listOf(p[2], p[1], p[0]) else p
        } else emptyList()
        val d = parts.getOrNull(0)?.toIntOrNull() ?: defDay
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: defMonth).coerceIn(1, 12)
        val y = parts.getOrNull(2)?.toIntOrNull() ?: defYear
        return Triple(d, m, y)
    }

    val (startD, startM, startY) = parseDate(initialStartDateText, 20, 8, 2026)
    val (endD, endM, endY) = parseDate(initialEndDateText, 28, 8, 2026)

    // Sol ay başlangıç state'i
    var leftMonth by remember { mutableStateOf(startM) }
    var leftYear by remember { mutableStateOf(startY) }

    // Sağ ay (Sol ayın 1 sonrası)
    val rightMonth = if (leftMonth == 12) 1 else leftMonth + 1
    val rightYear = if (leftMonth == 12) leftYear + 1 else leftYear

    // Seçili aralık: YYYYMMDD tamsayı formatında karşılaştırma
    var selStart by remember { mutableStateOf(startY * 10000 + startM * 100 + startD) }
    var selEnd by remember { mutableStateOf(endY * 10000 + endM * 100 + endD) }
    var selectedFlexibility by remember { mutableStateOf(initialFlexibilityDays) }

    fun getDaysInMonth(m: Int, y: Int): Int {
        return when (m) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)) 29 else 28
            else -> 31
        }
    }

    fun getFirstDayOfWeek(m: Int, y: Int): Int {
        val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
        val yr = if (m < 3) y - 1 else y
        val dayOfWeekSunday0 = (yr + yr / 4 - yr / 100 + yr / 400 + t[m - 1] + 1) % 7
        return if (dayOfWeekSunday0 == 0) 6 else dayOfWeekSunday0 - 1
    }

    // Gece hesaplama
    val nightsCount = remember(selStart, selEnd) {
        if (selStart > 0 && selEnd > selStart) {
            val sD = selStart % 100
            val sM = (selStart / 100) % 100
            val sY = selStart / 10000
            val eD = selEnd % 100
            val eM = (selEnd / 100) % 100
            val eY = selEnd / 10000
            if (sY == eY && sM == eM) (eD - sD).coerceAtLeast(1)
            else (getDaysInMonth(sM, sY) - sD + eD).coerceAtLeast(1)
        } else 7
    }

    fun formatDisplayDate(dateNum: Int): String {
        if (dateNum <= 0) return "--"
        val d = (dateNum % 100).toString().padStart(2, '0')
        val m = ((dateNum / 100) % 100).coerceIn(1, 12)
        val y = dateNum / 10000
        return "$d ${monthNames[m - 1]} $y"
    }

    fun formatIsoDate(dateNum: Int): String {
        val d = (dateNum % 100).toString().padStart(2, '0')
        val m = ((dateNum / 100) % 100).toString().padStart(2, '0')
        val y = dateNum / 10000
        return "$d.$m.$y"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(720.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Tarih Aralığı & Gece Özeti)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = AppLanguageManager.translate("Gidiş - Dönüş Tarihleri").uppercase(),
                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                        Text(
                            text = "${formatDisplayDate(selStart)}  ➔  ${formatDisplayDate(selEnd)}  (${nightsCount} ${AppLanguageManager.translate("Gece")})",
                            style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        )
                    }

                    // Aylar Arası İleri / Geri Navigasyon
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable {
                                    if (leftMonth > 1) leftMonth--
                                    else { leftMonth = 12; leftYear-- }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("◀", fontSize = 12.sp, color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable {
                                    if (leftMonth < 12) leftMonth++
                                    else { leftMonth = 1; leftYear++ }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶", fontSize = 12.sp, color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Yan Yana İki Ay Takvim Izgarası
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. SOL AY
                    SingleMonthCalendarView(
                        month = leftMonth,
                        year = leftYear,
                        monthName = monthNames[leftMonth - 1],
                        dayNames = dayNames,
                        selStart = selStart,
                        selEnd = selEnd,
                        firstDayOffset = getFirstDayOfWeek(leftMonth, leftYear),
                        maxDays = getDaysInMonth(leftMonth, leftYear),
                        modifier = Modifier.weight(1f),
                        onDayClicked = { clickedDateNum ->
                            if (selStart == 0 || selEnd > 0) {
                                selStart = clickedDateNum
                                selEnd = 0
                            } else {
                                if (clickedDateNum >= selStart) {
                                    selEnd = clickedDateNum
                                } else {
                                    selStart = clickedDateNum
                                    selEnd = 0
                                }
                            }
                        }
                    )

                    // 2. SAĞ AY
                    SingleMonthCalendarView(
                        month = rightMonth,
                        year = rightYear,
                        monthName = monthNames[rightMonth - 1],
                        dayNames = dayNames,
                        selStart = selStart,
                        selEnd = selEnd,
                        firstDayOffset = getFirstDayOfWeek(rightMonth, rightYear),
                        maxDays = getDaysInMonth(rightMonth, rightYear),
                        modifier = Modifier.weight(1f),
                        onDayClicked = { clickedDateNum ->
                            if (selStart == 0 || selEnd > 0) {
                                selStart = clickedDateNum
                                selEnd = 0
                            } else {
                                if (clickedDateNum >= selStart) {
                                    selEnd = clickedDateNum
                                } else {
                                    selStart = clickedDateNum
                                    selEnd = 0
                                }
                            }
                        }
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Esneklik Seçenekleri Barı (±1, ±2, ±3 Gün Esnek)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ ${AppLanguageManager.translate("Esneklik")}:",
                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = Color(0xFF475569), fontSize = 12.sp)
                        )

                        listOf(
                            0 to "Tam Tarihler",
                            1 to "±1 Gün",
                            2 to "±2 Gün",
                            3 to "±3 Gün Esnek"
                        ).forEach { (flexDays, label) ->
                            val isSelected = (selectedFlexibility == flexDays)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFF0F5A56) else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { selectedFlexibility = flexDays }
                            ) {
                                Text(
                                    text = AppLanguageManager.translate(label),
                                    style = TourOSTypography.Caption.copy(
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text(AppLanguageManager.translate("İptal"), color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                val effectiveEnd = if (selEnd > 0) selEnd else selStart
                                onRangeSelected(
                                    formatIsoDate(selStart),
                                    formatIsoDate(effectiveEnd),
                                    nightsCount,
                                    selectedFlexibility
                                )
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A56))
                        ) {
                            Text(AppLanguageManager.translate("Uygula"), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleMonthCalendarView(
    month: Int,
    year: Int,
    monthName: String,
    dayNames: List<String>,
    selStart: Int,
    selEnd: Int,
    firstDayOffset: Int,
    maxDays: Int,
    modifier: Modifier = Modifier,
    onDayClicked: (Int) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Ay Başlığı
        Text(
            text = "$monthName $year",
            style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Gün İsimleri
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            dayNames.forEach { dayName ->
                Text(
                    text = dayName,
                    style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Günler Grid'i
        val totalCells = firstDayOffset + maxDays
        val cellsList = (0 until totalCells).toList()

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            cellsList.chunked(7).forEach { weekRow ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    weekRow.forEach { cellIndex ->
                        if (cellIndex < firstDayOffset) {
                            Spacer(modifier = Modifier.size(32.dp))
                        } else {
                            val dayNum = cellIndex - firstDayOffset + 1
                            val dateNum = year * 10000 + month * 100 + dayNum

                            val isStart = (dateNum == selStart)
                            val isEnd = (dateNum == selEnd)
                            val isInRange = (selEnd > selStart && dateNum > selStart && dateNum < selEnd)

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isStart || isEnd -> Color(0xFF0F5A56)
                                            isInRange -> Color(0xFFE6F4F1)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDayClicked(dateNum) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayNum",
                                    style = TourOSTypography.Caption.copy(
                                        color = when {
                                            isStart || isEnd -> Color.White
                                            isInRange -> Color(0xFF0F5A56)
                                            else -> Color(0xFF1E293B)
                                        },
                                        fontWeight = if (isStart || isEnd) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                    repeat(7 - weekRow.size) {
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
