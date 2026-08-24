package com.mgacreative.touros

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun getCurrentEpochMillis(): Long

fun getTodayTriple(): Triple<Int, Int, Int> {
    val ms = getCurrentEpochMillis()
    var days = (ms / (1000L * 60 * 60 * 24)).toInt()
    var year = 1970
    while (true) {
        val leap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        val daysInYear = if (leap) 366 else 365
        if (days >= daysInYear) {
            days -= daysInYear
            year++
        } else {
            break
        }
    }
    val leap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    val daysInMonths = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var month = 1
    for (dim in daysInMonths) {
        if (days >= dim) {
            days -= dim
            month++
        } else {
            break
        }
    }
    val day = days + 1
    return Triple(day, month, year)
}

fun addDaysToTriple(triple: Triple<Int, Int, Int>, daysToAdd: Int): Triple<Int, Int, Int> {
    var (d, m, y) = triple
    fun getDim(mon: Int, yr: Int): Int {
        val leap = (yr % 4 == 0 && yr % 100 != 0) || (yr % 400 == 0)
        return when (mon) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (leap) 29 else 28
            else -> 31
        }
    }
    d += daysToAdd
    while (d > getDim(m, y)) {
        d -= getDim(m, y)
        m++
        if (m > 12) {
            m = 1
            y++
        }
    }
    return Triple(d, m, y)
}