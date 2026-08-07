package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.feedback.ClassifiedComplaint
import com.mgacreative.touros.domain.model.feedback.ComplaintCategory
import com.mgacreative.touros.domain.model.feedback.ComplaintSeverity

class ComplaintClassifierEngine {

    fun classifyText(complaintText: String): ClassifiedComplaint {
        val lowerText = complaintText.lowercase()
        val (category, severity, tags) = when {
            lowerText.contains("otel") || lowerText.contains("oda") || lowerText.contains("kahvaltı") -> {
                Triple(ComplaintCategory.HOTEL, ComplaintSeverity.HIGH, listOf("Otel Konaklama", "Hizmet Standardı"))
            }
            lowerText.contains("rehber") || lowerText.contains("anlatım") || lowerText.contains("gecikme") -> {
                Triple(ComplaintCategory.GUIDE, ComplaintSeverity.HIGH, listOf("Tur Rehberi", "Zamanlama"))
            }
            lowerText.contains("transfer") || lowerText.contains("araç") || lowerText.contains("klima") || lowerText.contains("minibüs") -> {
                Triple(ComplaintCategory.TRANSFER, ComplaintSeverity.CRITICAL, listOf("Araç Konforu", "Filo Operasyon"))
            }
            lowerText.contains("fiyat") || lowerText.contains("ücret") || lowerText.contains("iade") -> {
                Triple(ComplaintCategory.PRICING, ComplaintSeverity.HIGH, listOf("Finans & İade", "Fiyatlandırma"))
            }
            lowerText.contains("telefon") || lowerText.contains("ulaşamadım") || lowerText.contains("iletişim") -> {
                Triple(ComplaintCategory.COMMUNICATION, ComplaintSeverity.MEDIUM, listOf("İletişim Kanalı", "Çağrı Merkezi"))
            }
            else -> {
                Triple(ComplaintCategory.OTHER, ComplaintSeverity.LOW, listOf("Genel Geri Bildirim"))
            }
        }

        return ClassifiedComplaint(
            complaintId = "cmp-${complaintText.hashCode().toString().take(6)}",
            complaintText = complaintText,
            category = category,
            severity = severity,
            autoTags = tags
        )
    }
}
