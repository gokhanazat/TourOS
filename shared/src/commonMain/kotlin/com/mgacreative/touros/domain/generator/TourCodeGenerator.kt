package com.mgacreative.touros.domain.generator

/**
 * 4.6.2 Otomatik Tur Kodu Üretici (TourCodeGenerator).
 * Format: PREFIX-00001 (ör. ANK-00001, IST-00042)
 */
object TourCodeGenerator {

    fun generateCode(operatorCode: String?, sequenceNumber: Int): String {
        val prefix = operatorCode?.trim()?.uppercase()?.takeIf { it.isNotBlank() } ?: "TUR"
        val seq = if (sequenceNumber <= 0) 1 else sequenceNumber
        val formattedSeq = seq.toString().padStart(5, '0')
        return "$prefix-$formattedSeq"
    }
}
