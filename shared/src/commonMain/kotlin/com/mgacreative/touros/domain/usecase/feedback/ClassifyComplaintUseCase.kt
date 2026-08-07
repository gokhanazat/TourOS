package com.mgacreative.touros.domain.usecase.feedback

import com.mgacreative.touros.domain.model.feedback.ClassifiedComplaint
import com.mgacreative.touros.domain.repository.ComplaintClassificationRepository

class ClassifyComplaintUseCase(
    private val repository: ComplaintClassificationRepository
) {
    suspend operator fun invoke(complaintText: String, tenantId: String): Result<ClassifiedComplaint> {
        return repository.classifyComplaint(complaintText, tenantId)
    }
}
