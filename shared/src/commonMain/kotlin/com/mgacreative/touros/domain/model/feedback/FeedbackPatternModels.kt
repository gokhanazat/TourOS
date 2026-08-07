package com.mgacreative.touros.domain.model.feedback

import kotlinx.serialization.Serializable

enum class IssueCategory {
    GUIDE_DELAY,
    VEHICLE_COMFORT,
    HOTEL_QUALITY,
    PRICING_REFUND,
    WEATHER_CANCEL,
    OTHER
}

enum class PatternSeverity {
    HIGH,
    MEDIUM,
    LOW
}

@Serializable
data class FeedbackPattern(
    val id: String,
    val patternName: String,
    val issueCategory: IssueCategory = IssueCategory.VEHICLE_COMFORT,
    val occurrenceCount: Int = 1,
    val severity: PatternSeverity = PatternSeverity.HIGH,
    val sentimentScore: Double = -0.75,
    val suggestedAction: String
)

@Serializable
data class FeedbackAnalysisSummary(
    val totalFeedbacksAnalyzed: Int = 0,
    val highSeverityPatterns: Int = 0,
    val mediumSeverityPatterns: Int = 0,
    val topIssueCategory: String = "VEHICLE_COMFORT"
)
