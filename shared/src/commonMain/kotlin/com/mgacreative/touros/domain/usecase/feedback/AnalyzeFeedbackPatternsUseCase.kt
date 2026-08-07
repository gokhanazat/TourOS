package com.mgacreative.touros.domain.usecase.feedback

import com.mgacreative.touros.domain.model.feedback.FeedbackAnalysisSummary
import com.mgacreative.touros.domain.model.feedback.FeedbackPattern
import com.mgacreative.touros.domain.repository.FeedbackPatternRepository

class AnalyzeFeedbackPatternsUseCase(
    private val repository: FeedbackPatternRepository
) {
    suspend fun getSummary(tenantId: String): Result<FeedbackAnalysisSummary> {
        return repository.runPatternAnalysis(tenantId)
    }

    suspend fun getPatterns(tenantId: String): Result<List<FeedbackPattern>> {
        return repository.getRecurringPatterns(tenantId)
    }
}
