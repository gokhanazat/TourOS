package com.mgacreative.touros.ai.model

import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import kotlinx.serialization.Serializable

/**
 * Kullanıcı serbest metninden YandexGPT'nin çıkaracağı B2B arama parametreleri.
 */
@Serializable
data class AITourSearchParams(
    val departureCity: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val nights: Int? = null,
    val adults: Int = 2,
    val children: Int = 0,
    val hotelStars: Int? = null,
    val boardType: String? = null,
    val maxBudgetRub: Double? = null,
    val isSeafront: Boolean = false,
    val isDirectFlight: Boolean = true,
    val category: String = "ALL", // "ALL", "PACKAGE_TOUR", "FLIGHT", "HOTEL"
    val tripType: String = "ONE_WAY", // "ONE_WAY", "ROUND_TRIP"
    val missingInfoQuestionRu: String? = null
)

/**
 * Chat geçmişindeki mesaj modeli
 */
@Serializable
data class AIAssistantMessage(
    val id: String,
    val sender: String, // "USER" veya "AGENT"
    val textRu: String,
    val debugTranslationTr: String = "",
    val extractedParams: AITourSearchParams? = null,
    val foundProductsCount: Int = 0,
    val timestamp: Long = 0L
)

/**
 * Aynı oteli satan farklı tur operatörlerinin gruplanmış teklif modeli.
 * (Aynı otel tek satırda gelir, tıklandığında operatörler açılır).
 */
data class AIGroupedHotelOffer(
    val hotelName: String,
    val stars: Int,
    val region: String,
    val subRegion: String,
    val country: String,
    val pictureUrl: String?,
    val minPrice: Double,
    val currency: String,
    val departureCity: String,
    val nights: Int,
    val mealType: String,
    val operatorOffers: List<UnifiedProductEntity>
)
