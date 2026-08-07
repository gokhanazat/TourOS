package com.mgacreative.touros.domain.model

data class UpcomingTour(
    val id: String = "",
    val tourTitle: String = "",
    val departureDate: String = "",
    val bookedCount: Int = 0,
    val capacity: Int = 30,
    val status: String = "planned"
)
