package com.mgacreative.touros.utils

import com.mgacreative.touros.addDaysToTriple
import com.mgacreative.touros.getTodayTriple

/**
 * TourOS Merkezi Tarih ve Zaman Yardımcı Sınıfı.
 * Tüm platformlarda (Android, iOS, JVM/Desktop, Web/WASM) dinamik olarak günün tarihini ve tarih hesaplamalarını yönetir.
 */
object DateUtils {
    val monthNamesTr = listOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    )

    val dayNamesTr = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    /**
     * Günün tarihini (Gün, Ay, Yıl) Triple olarak döner.
     */
    fun getToday(): Triple<Int, Int, Int> = getTodayTriple()

    fun getCurrentDay(): Int = getToday().first
    fun getCurrentMonth(): Int = getToday().second
    fun getCurrentYear(): Int = getToday().third

    fun getCurrentMonthName(): String {
        val m = getCurrentMonth().coerceIn(1, 12)
        return monthNamesTr[m - 1]
    }

    fun getCurrentMonthAndYear(): String {
        return "${getCurrentMonthName()} ${getCurrentYear()}"
    }

    /**
     * Günün tarihini formatlı string olarak döner.
     * @param delimiter Ayraç (ör: "." veya "-")
     * @param reverseOrder true ise YYYY-MM-DD, false ise DD.MM.YYYY
     */
    fun getTodayFormatted(delimiter: String = ".", reverseOrder: Boolean = false): String {
        val (d, m, y) = getToday()
        val ds = d.toString().padStart(2, '0')
        val ms = m.toString().padStart(2, '0')
        val ys = y.toString()
        return if (reverseOrder) "$ys$delimiter$ms$delimiter$ds" else "$ds$delimiter$ms$delimiter$ys"
    }

    /**
     * Günün tarihine belirtilen gün sayısını ekleyerek formatlı string olarak döner.
     */
    fun getFutureDateFormatted(daysToAdd: Int, delimiter: String = ".", reverseOrder: Boolean = false): String {
        val (d, m, y) = addDaysToTriple(getToday(), daysToAdd)
        val ds = d.toString().padStart(2, '0')
        val ms = m.toString().padStart(2, '0')
        val ys = y.toString()
        return if (reverseOrder) "$ys$delimiter$ms$delimiter$ds" else "$ds$delimiter$ms$delimiter$ys"
    }

    /**
     * YYYY-MM-DD formatında günün tarihini döner.
     */
    fun getTodayIso(): String = getTodayFormatted(delimiter = "-", reverseOrder = true)

    /**
     * YYYY-MM-DD formatında gelecekteki tarihi döner.
     */
    fun getFutureIso(daysToAdd: Int): String = getFutureDateFormatted(daysToAdd, delimiter = "-", reverseOrder = true)

    /**
     * DD.MM.YYYY formatında günün tarihini döner.
     */
    fun getTodayDot(): String = getTodayFormatted(delimiter = ".", reverseOrder = false)

    /**
     * DD.MM.YYYY formatında gelecekteki tarihi döner.
     */
    fun getFutureDot(daysToAdd: Int): String = getFutureDateFormatted(daysToAdd, delimiter = ".", reverseOrder = false)

    /**
     * Doğum tarihi veya tarih girişlerinde (GG.AA.YYYY) akıllı otomatik nokta (.) yerleşimini sağlar.
     * Kullanıcı 13/4/1960, 13.4.1960, 1/1/1985 veya 13041960 yazsa bile standart GG.AA.YYYY formatına çevirir.
     */
    fun formatDateInput(input: String): String {
        if (input.isBlank()) return ""

        val hasExplicitDelimiter = input.any { it == '/' || it == '-' || it == '.' || it == ' ' }

        if (hasExplicitDelimiter) {
            val parts = input.split(Regex("[./\\- ]")).filter { it.isNotEmpty() }
            if (parts.isEmpty()) return ""

            val endsWithDelim = input.endsWith(".") || input.endsWith("/") || input.endsWith("-") || input.endsWith(" ")

            val dRaw = parts.getOrNull(0)?.filter { it.isDigit() }?.take(2) ?: ""
            val mRaw = parts.getOrNull(1)?.filter { it.isDigit() }?.take(2) ?: ""
            val yRaw = parts.getOrNull(2)?.filter { it.isDigit() }?.take(4) ?: ""

            return when {
                parts.size >= 3 -> {
                    val d = dRaw.padStart(2, '0')
                    val m = mRaw.padStart(2, '0')
                    "$d.$m.$yRaw"
                }
                parts.size == 2 -> {
                    val d = dRaw.padStart(2, '0')
                    if (endsWithDelim) {
                        val m = mRaw.padStart(2, '0')
                        "$d.$m."
                    } else {
                        "$d.$mRaw"
                    }
                }
                parts.size == 1 -> {
                    if (endsWithDelim) {
                        val d = dRaw.padStart(2, '0')
                        "$d."
                    } else {
                        dRaw
                    }
                }
                else -> input
            }
        }

        // Delimitersiz ham rakam girişi (örn: 13041960)
        val clean = input.filter { it.isDigit() }.take(8)
        return when {
            clean.length < 2 -> clean
            clean.length == 2 -> if (input.length > 2) "$clean." else clean
            clean.length < 4 -> "${clean.substring(0, 2)}.${clean.substring(2)}"
            clean.length == 4 -> if (input.length > 5) "${clean.substring(0, 2)}.${clean.substring(2, 4)}." else "${clean.substring(0, 2)}.${clean.substring(2, 4)}"
            else -> "${clean.substring(0, 2)}.${clean.substring(2, 4)}.${clean.substring(4)}"
        }
    }

    /**
     * Bir ayın toplam gün sayısını döner.
     */
    fun getDaysInMonth(m: Int, y: Int): Int {
        val leap = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
        return when (m.coerceIn(1, 12)) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (leap) 29 else 28
            else -> 31
        }
    }
}
