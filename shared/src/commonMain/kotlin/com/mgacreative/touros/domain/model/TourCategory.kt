package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 12 Tur Tipi / Kategorisi Enum Modeli.
 */
@Serializable
enum class TourCategory(val displayName: String) {
    @SerialName("DAILY_TOUR") DAILY_TOUR("Günübirlik Tur"),
    @SerialName("CULTURAL") CULTURAL("Kültür Turu"),
    @SerialName("ADVENTURE") ADVENTURE("Macera & Doğa"),
    @SerialName("CRUISE") CRUISE("Gemi & Cruise"),
    @SerialName("VIP") VIP("VIP Özel Tur"),
    @SerialName("TRANSFER") TRANSFER("Transfer Hizmeti"),
    @SerialName("HOTEL_PACKAGE") HOTEL_PACKAGE("Otel Paket Turu"),
    @SerialName("UMRAH") UMRAH("Umre Turu"),
    @SerialName("HAJJ") HAJJ("Hac Turu"),
    @SerialName("SKI") SKI("Kayak Turu"),
    @SerialName("INTERNATIONAL") INTERNATIONAL("Yurtdışı Turu"),
    @SerialName("DOMESTIC") DOMESTIC("Yurtiçi Turu");

    companion object {
        fun fromKey(key: String): TourCategory =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) || it.displayName.equals(key, ignoreCase = true) } ?: CULTURAL
    }
}
