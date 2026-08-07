package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * Yolcu (Passenger) Domain Modeli.
 */
@Serializable
data class Passenger(
    val id: String = "",
    val bookingId: String = "",
    val fullName: String = "",
    val tcNo: String? = null,
    val passportNo: String? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val isLead: Boolean = false,
    val notes: String? = null
)
