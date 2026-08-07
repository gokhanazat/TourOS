package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.feedback.VendorPerformanceImpact

interface ComplaintTrendPerformanceRepository {
    suspend fun getVendorPerformanceImpacts(tenantId: String): Result<List<VendorPerformanceImpact>>
}
