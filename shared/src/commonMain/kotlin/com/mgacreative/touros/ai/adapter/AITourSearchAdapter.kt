package com.mgacreative.touros.ai.adapter

import com.mgacreative.touros.ai.model.AITourSearchParams
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel

/**
 * AI Parametrelerini mevcut B2B Arama mantığıyla eşleyen adaptör (Mevcut koda dokunmaz).
 */
object AITourSearchAdapter {

    fun filterProducts(
        allProducts: List<UnifiedProductEntity>,
        params: AITourSearchParams
    ): List<UnifiedProductEntity> {
        return allProducts.filter { product ->
            // 1. Kalkış Şehri Kontrolü
            val matchesDeparture = if (params.departureCity.isBlank()) {
                true
            } else {
                B2BTourSearchViewModel.isDepartureMatchingText(product.departureCity, params.departureCity)
            }

            // 2. Destinasyon / Varış Noktası Kontrolü (Belek, Kemer, Antalya vb.)
            val matchesDestination = if (params.destination.isBlank()) {
                true
            } else {
                val destLower = params.destination.lowercase().trim()
                val hotelText = "${product.hotelName} ${product.region} ${product.subRegion} ${product.country}".lowercase()
                
                if (destLower.contains("belek") || destLower.contains("белек")) {
                    hotelText.contains("belek") || hotelText.contains("белек") || hotelText.contains("boğazkent") || hotelText.contains("kadriye")
                } else if (destLower.contains("kemer") || destLower.contains("кемер")) {
                    hotelText.contains("kemer") || hotelText.contains("кемер") || hotelText.contains("beldibi") || hotelText.contains("göynük")
                } else {
                    B2BTourSearchViewModel.isDestinationMatching(product, params.destination)
                }
            }

            // 3. Yıldız Kontrolü (Kesin 5 Yıldız veya üzeri)
            val matchesStars = if (params.hotelStars != null && params.hotelStars > 0) {
                product.hotelCategory >= params.hotelStars
            } else {
                true
            }

            // 4. Bütçe Kontrolü (RUB veya EUR dönüşümlü)
            val matchesBudget = if (params.maxBudgetRub != null && params.maxBudgetRub > 0) {
                // Yaklaşık 1 EUR ≈ 100 RUB üzerinden emniyetli karşılaştırma
                val productPriceRub = if (product.currency.equals("RUB", ignoreCase = true)) {
                    product.price
                } else {
                    product.price * 100.0
                }
                productPriceRub <= params.maxBudgetRub
            } else {
                true
            }

            // 5. Konsept Kontrolü (Varsa)
            val matchesBoard = if (!params.boardType.isNullOrBlank()) {
                val meal = (product.mealType ?: "").lowercase()
                val target = params.boardType.lowercase()
                meal.contains(target) || (target.contains("uai") && (meal.contains("ultra") || meal.contains("uai")))
            } else {
                true
            }

            matchesDeparture && matchesDestination && matchesStars && matchesBudget && matchesBoard
        }.sortedBy { it.price }
    }

    /**
     * Aynı oteli satan farklı operatör tekliflerini tek bir otel altında toplar.
     */
    fun groupProductsByHotel(products: List<UnifiedProductEntity>): List<com.mgacreative.touros.ai.model.AIGroupedHotelOffer> {
        val groupedMap = products.groupBy { it.safeHotelName.trim().lowercase() }

        return groupedMap.map { (_, hotelProducts) ->
            val first = hotelProducts.first()
            val sortedByPrice = hotelProducts.sortedBy { it.price }
            val cheapest = sortedByPrice.first()

            com.mgacreative.touros.ai.model.AIGroupedHotelOffer(
                hotelName = first.safeHotelName,
                stars = if (first.hotelCategory > 0) first.hotelCategory else 5,
                region = first.region,
                subRegion = first.subRegion,
                country = first.country,
                pictureUrl = first.pictureUrl,
                minPrice = cheapest.price,
                currency = cheapest.currency,
                departureCity = cheapest.departureCity,
                nights = cheapest.nights,
                mealType = cheapest.safeMealType,
                operatorOffers = sortedByPrice
            )
        }.sortedBy { it.minPrice }
    }
}
