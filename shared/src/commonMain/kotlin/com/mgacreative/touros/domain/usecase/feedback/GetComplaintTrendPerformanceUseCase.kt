package com.mgacreative.touros.domain.usecase.feedback

import com.mgacreative.touros.domain.model.feedback.VendorPerformanceImpact
import com.mgacreative.touros.domain.repository.ComplaintTrendPerformanceRepository

class GetComplaintTrendPerformanceUseCase(
    private val repository: ComplaintTrendPerformanceRepository
) {
    suspend operator fun invoke(tenantId: String): Result<List<VendorPerformanceImpact>> {
        return repository.getVendorPerformanceImpacts(tenantId)
    }
}
