package com.mgacreative.touros.domain.model.email

import kotlinx.serialization.Serializable

enum class EmailType {
    BOOKING_CONFIRMATION,
    PRE_TRIP_REMINDER,
    POST_TRIP_THANK_YOU
}

enum class EmailDraftStatus {
    DRAFT,
    EDITED,
    SENT,
    FAILED
}

@Serializable
data class EmailDraft(
    val draftId: String,
    val bookingId: String,
    val emailType: EmailType = EmailType.BOOKING_CONFIRMATION,
    val recipientEmail: String = "musteri@example.com",
    val subject: String,
    val bodyHtml: String,
    val status: EmailDraftStatus = EmailDraftStatus.DRAFT
)

@Serializable
data class SendEmailDraftResponse(
    val draftId: String,
    val status: EmailDraftStatus = EmailDraftStatus.SENT,
    val sentAt: String
)
