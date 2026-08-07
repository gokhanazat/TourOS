package com.mgacreative.touros.domain.model

data class GuideStatusInfo(
    val id: String = "",
    val fullName: String = "",
    val phone: String? = null,
    val languages: List<String> = emptyList(),
    val status: String = "Müsait", // "Müsait", "Görevde", "İzinli"
    val assignedTourTitle: String? = null
)
