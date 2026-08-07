package com.mgacreative.touros.domain.usecase.email

import com.mgacreative.touros.domain.model.email.EmailDraft
import com.mgacreative.touros.domain.model.email.EmailType
import com.mgacreative.touros.domain.repository.EmailDraftRepository

class GenerateEmailDraftUseCase(
    private val repository: EmailDraftRepository
) {
    suspend operator fun invoke(bookingId: String, emailType: EmailType, tenantId: String): Result<EmailDraft> {
        return repository.generateEmailDraft(bookingId, emailType, tenantId)
    }
}
