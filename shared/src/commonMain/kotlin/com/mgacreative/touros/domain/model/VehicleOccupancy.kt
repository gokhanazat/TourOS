package com.mgacreative.touros.domain.model

data class VehicleOccupancy(
    val id: String = "",
    val plateNumber: String = "",
    val modelName: String = "",
    val driverName: String = "",
    val occupiedSeats: Int = 0,
    val totalCapacity: Int = 46,
    val assignedTourTitle: String? = null
)
