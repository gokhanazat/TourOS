package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * Tur Domain Modeli.
 */
@Serializable
data class Tour(
    val id: String = "",
    val code: String,
    val title: String,
    val category: TourCategory = TourCategory.CULTURAL,
    val country: String,
    val city: String,
    val durationDays: Int = 1,
    val basePrice: Double = 0.0,
    val childPrice06: Double = 0.0,
    val childPrice712: Double = 0.0,
    val capacity: Int = 20,
    val minParticipants: Int = 1,
    val maxParticipants: Int = 30,
    val description: String? = null,
    val cancellationPolicy: String? = null,
    val insuranceDetails: String? = null,
    val tenantId: String = "",
    val isActive: Boolean = true
)
