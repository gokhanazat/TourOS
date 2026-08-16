package com.mgacreative.touros.domain.model

data class GuideStatusInfo(
    val id: String = "",
    val fullName: String = "",
    val phone: String = "-",
    val languages: List<String> = emptyList(),
    val status: String = "Müsait",
    val assignedTourTitle: String? = null
)
