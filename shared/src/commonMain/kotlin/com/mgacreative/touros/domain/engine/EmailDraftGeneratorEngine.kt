package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.email.EmailDraft
import com.mgacreative.touros.domain.model.email.EmailDraftStatus
import com.mgacreative.touros.domain.model.email.EmailType

class EmailDraftGeneratorEngine {

    fun generatePersonalizedDraft(
        bookingId: String,
        customerName: String,
        customerEmail: String,
        tourName: String,
        departureTime: String,
        voucherCode: String,
        emailType: EmailType
    ): EmailDraft {
        val (subject, body) = when (emailType) {
            EmailType.BOOKING_CONFIRMATION -> {
                "Sayın $customerName, Rezervasyon Onayınız ($tourName - Voucher #$voucherCode)" to
                        "<p>Merhaba <b>$customerName</b>,</p><p><b>$tourName</b> turu için rezervasyonunuz onaylanmıştır. Voucher kodunuz: <b>$voucherCode</b>.</p><p>Bizi tercih ettiğiniz için teşekkür ederiz.</p>"
            }
            EmailType.PRE_TRIP_REMINDER -> {
                "Tur Hatırlatması: Yarınki $tourName Turunuz! (Saat: $departureTime)" to
                        "<p>Merhaba <b>$customerName</b>,</p><p>Yarın saat <b>$departureTime</b> için planlanan <b>$tourName</b> turunuza hazır olmanızı rica ederiz. Lütfen voucher ve kimliğinizi yanınızda bulundurun.</p>"
            }
            EmailType.POST_TRIP_THANK_YOU -> {
                "$tourName Turumuza Katıldığınız İçin Teşekkür Ederiz!" to
                        "<p>Merhaba <b>$customerName</b>,</p><p><b>$tourName</b> turumuza katıldığınız için içtenlikle teşekkür ederiz. Turunuzu 1 dakikada değerlendirmek ister misiniz?</p>"
            }
        }

        return EmailDraft(
            draftId = "draft-${bookingId.take(4)}-${emailType.name.lowercase()}",
            bookingId = bookingId,
            emailType = emailType,
            recipientEmail = customerEmail,
            subject = subject,
            bodyHtml = body,
            status = EmailDraftStatus.DRAFT
        )
    }
}
