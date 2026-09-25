package com.mgacreative.touros.domain.model

/**
 * TourOS - İzin Verilen Kanonik Tur Operatörleri Konfigürasyonu
 * KESİN KURAL: Sistemde ve aramalarda YALNIZCA bu 9 Tur Operatörü bulunabilir.
 */
object TourOperatorConfig {

    val ALLOWED_OPERATOR_NAMES: List<String> = listOf(
        "Bibloglobus",
        "Anex",
        "Coral Travel",
        "Sunmar",
        "Fun&Sun (Ru)",
        "Kazunion",
        "Loti",
        "Pegas Touristik",
        "Интурист"
    )

    /**
     * Ham operatör adını veya alias'ını 9 Kanonik Tur Operatöründen birine çözümler.
     * Bu 9 operatör dışındaysa null döner ve aramadan/listelerden tamamen elenir.
     */
    fun resolveCanonicalOperatorName(rawName: String?): String? {
        if (rawName.isNullOrBlank()) return null
        val trimmed = rawName.trim()

        // 1. Doğrudan tam eşleşme (büyük/küçük harf duyarsız)
        ALLOWED_OPERATOR_NAMES.firstOrNull { it.equals(trimmed, ignoreCase = true) }?.let { return it }

        val lower = trimmed.lowercase()

        // 2. Alias / Eş anlamlı kelime eşleşmeleri
        return when {
            lower.contains("biblo") || lower.contains("biblio") || lower.contains("библио") -> "Bibloglobus"
            lower.contains("anex") || lower.contains("анекс") -> "Anex"
            lower.contains("coral") || lower.contains("корал") -> "Coral Travel"
            lower.contains("sunmar") || lower.contains("санмар") -> "Sunmar"
            (lower.contains("fun") && (lower.contains("sun") || lower.contains("&"))) || 
                lower.contains("фан сан") || lower.contains("funsun") || lower.contains("tui") -> "Fun&Sun (Ru)"
            lower.contains("kazunion") || lower.contains("казунион") -> "Kazunion"
            lower.contains("loti") || lower.contains("лоти") -> "Loti"
            lower.contains("pegas") || lower.contains("пегас") -> "Pegas Touristik"
            lower.contains("интурист") || lower.contains("intourist") -> "Интурист"
            else -> null
        }
    }

    /**
     * Bir operatörün izin verilen 9 kanonik operatörden biri olup olmadığını test eder.
     */
    fun isAllowedOperator(rawName: String?): Boolean {
        return resolveCanonicalOperatorName(rawName) != null
    }
}
