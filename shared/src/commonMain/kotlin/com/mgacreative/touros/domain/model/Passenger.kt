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
    val firstName: String? = null,
    val lastName: String? = null,
    val tcNo: String? = null,
    val passportSeries: String? = null,
    val passportNo: String? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val citizenship: String? = null,
    val birthCountry: String? = null,
    val documentType: String? = null,
    val documentIssueDate: String? = null,
    val documentIssuedBy: String? = null,
    val documentExpiryDate: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val isLead: Boolean = false,
    val notes: String? = null
)
