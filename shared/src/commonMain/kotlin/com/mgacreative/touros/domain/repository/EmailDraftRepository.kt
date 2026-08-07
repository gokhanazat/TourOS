package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.email.EmailDraft
import com.mgacreative.touros.domain.model.email.EmailType
import com.mgacreative.touros.domain.model.email.SendEmailDraftResponse

interface EmailDraftRepository {
    suspend fun generateEmailDraft(bookingId: String, emailType: EmailType, tenantId: String): Result<EmailDraft>
    suspend fun sendEmailDraft(draftId: String, updatedSubject: String, updatedBody: String, tenantId: String): Result<SendEmailDraftResponse>
}
