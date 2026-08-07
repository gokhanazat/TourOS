package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Booking Durum Enum Modeli.
 * Veritabanında Türkçe string değerler ile eşleşir ('Bekliyor', 'Opsiyon', 'Onaylandı', 'İptal', 'Tamamlandı').
 */
@Serializable
enum class BookingStatus(val dbValue: String, val displayName: String) {
    @SerialName("Bekliyor") BEKLIYOR("Bekliyor", "Bekliyor"),
    @SerialName("Opsiyon") OPSIYON("Opsiyon", "Opsiyonlu"),
    @SerialName("Onaylandı") ONAYLANDI("Onaylandı", "Onaylandı"),
    @SerialName("İptal") IPTAL("İptal", "İptal Edildi"),
    @SerialName("Tamamlandı") TAMAMLANDI("Tamamlandı", "Tamamlandı");

    companion object {
        fun fromDbValue(value: String): BookingStatus =
            entries.firstOrNull { 
                it.dbValue.equals(value, ignoreCase = true) || 
                it.name.equals(value, ignoreCase = true) 
            } ?: BEKLIYOR
    }
}
