package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.faq.FaqCategory

class FaqChatbotEngine {

    fun matchAnswer(query: String): Pair<String, FaqCategory> {
        val lowerQuery = query.lowercase()
        return when {
            lowerQuery.contains("iptal") || lowerQuery.contains("iade") || lowerQuery.contains("paramı alabilir miyim") -> {
                "Turlarımızda kalkıştan 48 saat öncesine kadar yapılan iptallerde kesintisiz %100 iade garantisi bulunmaktadır. 24-48 saat arası iptallerde %50 iade kesintisi uygulanır." to FaqCategory.CANCELLATION_POLICY
            }
            lowerQuery.contains("vize") || lowerQuery.contains("pasaport") || lowerQuery.contains("evrak") -> {
                "Yurt dışı turlarımız için en az 6 ay geçerliliği olan pasaport ve tur kategorisine göre Schengen / e-Vize gereklidir. Vize evrak listesini e-posta olarak da talep edebilirsiniz." to FaqCategory.VISA_REQUIREMENTS
            }
            lowerQuery.contains("rezervasyon") || lowerQuery.contains("durum") || lowerQuery.contains("voucher") -> {
                "Rezervasyon durumunuzu B2C veya Web Portal üzerinden 'Rezervasyonlarım' alanındaki Voucher Kodunuz (ör: TR-8814) ile anında sorgulayabilirsiniz." to FaqCategory.BOOKING_STATUS
            }
            else -> {
                "TourOS Destek Asistanına hoş geldiniz. Rezervasyon durumunuz, iptal/iade şartları veya vize evrakları hakkında soru sorabilirsiniz." to FaqCategory.GENERAL
            }
        }
    }
}
