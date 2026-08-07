package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.2.2 B2C Tur Detay Modeli.
 */
@Serializable
data class B2CTourDetail(
    @SerialName("tour_id") val tourId: String = "t101",
    val title: String = "Kapadokya Balon & Vadi Turu",
    val description: String = "Eşsiz peri bacaları, vadi yürüyüşleri ve sabah balon turu ile unutulmaz bir seyahat deneyimi.",
    val category: String = "Kültür Turu",
    @SerialName("destination_country") val destinationCountry: String = "Türkiye",
    @SerialName("duration_days") val durationDays: Int = 3,
    val price: Double = 2500.0,
    val currency: String = "TRY",
    val rating: Double = 4.85,
    @SerialName("included_services") val includedServices: List<String> = listOf("Lüks Otobüs İle Ulaşım", "4 Yıldızlı Otel Konaklama", "Profesyonel Rehberlik Hizmeti", "Açık Büfe Kahvaltı"),
    @SerialName("excluded_services") val excludedServices: List<String> = listOf("Kişisel Harcamalar", "Müze Ören Yeri Giriş Ücretleri", "Öğle Yemekleri"),
    @SerialName("itinerary_summary") val itinerarySummary: String = "1. Gün: Panoramik Şehir Turu & Otel Girişi | 2. Gün: Vadi Gezisi & Balon İzleme | 3. Gün: Antik Ören Yeri & Dönüş"
)
