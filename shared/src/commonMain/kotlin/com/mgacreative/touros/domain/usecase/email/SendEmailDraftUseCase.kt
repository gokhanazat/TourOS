package com.mgacreative.touros.domain.usecase.email

import com.mgacreative.touros.domain.model.email.SendEmailDraftResponse
import com.mgacreative.touros.domain.repository.EmailDraftRepository

class SendEmailDraftUseCase(
    private val repository: EmailDraftRepository
) {
    suspend operator fun invoke(draftId: String, updatedSubject: String, updatedBody: String, tenantId: String): Result<SendEmailDraftResponse> {
        return repository.sendEmailDraft(draftId, updatedSubject, updatedBody, tenantId)
    }
}
