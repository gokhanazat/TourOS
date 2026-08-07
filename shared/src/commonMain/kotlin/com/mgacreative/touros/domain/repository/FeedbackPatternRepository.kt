package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.feedback.FeedbackAnalysisSummary
import com.mgacreative.touros.domain.model.feedback.FeedbackPattern

interface FeedbackPatternRepository {
    suspend fun runPatternAnalysis(tenantId: String): Result<FeedbackAnalysisSummary>
    suspend fun getRecurringPatterns(tenantId: String): Result<List<FeedbackPattern>>
}
