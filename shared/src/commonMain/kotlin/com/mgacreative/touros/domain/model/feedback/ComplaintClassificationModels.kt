package com.mgacreative.touros.domain.model.feedback

import kotlinx.serialization.Serializable

enum class ComplaintCategory {
    HOTEL,
    GUIDE,
    TRANSFER,
    PRICING,
    COMMUNICATION,
    OTHER
}

enum class ComplaintSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

@Serializable
data class ClassifiedComplaint(
    val complaintId: String,
    val complaintText: String,
    val category: ComplaintCategory = ComplaintCategory.OTHER,
    val severity: ComplaintSeverity = ComplaintSeverity.MEDIUM,
    val autoTags: List<String> = emptyList()
)
