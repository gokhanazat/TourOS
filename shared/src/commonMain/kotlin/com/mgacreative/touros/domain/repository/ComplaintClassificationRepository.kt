package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.feedback.ClassifiedComplaint

interface ComplaintClassificationRepository {
    suspend fun classifyComplaint(complaintText: String, tenantId: String): Result<ClassifiedComplaint>
}
