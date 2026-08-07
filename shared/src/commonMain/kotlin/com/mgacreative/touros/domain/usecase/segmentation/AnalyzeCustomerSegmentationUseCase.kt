package com.mgacreative.touros.domain.usecase.segmentation

import com.mgacreative.touros.domain.model.segmentation.SegmentationAnalysisResult
import com.mgacreative.touros.domain.repository.CustomerSegmentationRepository

class AnalyzeCustomerSegmentationUseCase(
    private val repository: CustomerSegmentationRepository
) {
    suspend operator fun invoke(tenantId: String): Result<SegmentationAnalysisResult> {
        return repository.runSegmentationAnalysis(tenantId)
    }
}
