package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 4.2.1 B2C Müşteri Mobil Uygulaması Tur Filtreleme Modeli.
 */
@Serializable
data class B2CTourSearchFilter(
    val category: String? = null,
    val country: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val searchQuery: String = ""
)
