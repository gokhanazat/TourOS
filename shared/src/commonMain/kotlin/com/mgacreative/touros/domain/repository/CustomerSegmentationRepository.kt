package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.segmentation.CustomerSegment
import com.mgacreative.touros.domain.model.segmentation.SegmentationAnalysisResult

interface CustomerSegmentationRepository {
    suspend fun runSegmentationAnalysis(tenantId: String): Result<SegmentationAnalysisResult>
    suspend fun getCustomerSegments(tenantId: String): Result<List<CustomerSegment>>
    suspend fun getCustomerSegmentById(customerId: String, tenantId: String): Result<CustomerSegment?>
}
