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
            val rawType = product.safeProductType.uppercase()
            val isFlight = rawType.contains("FLIGHT") || rawType.contains("CHARTER") || product.flightNumber.isNotBlank() || product.safeTourName.startsWith("Uçuş", ignoreCase = true) || product.safeHotelName.startsWith("✈️", ignoreCase = true)
            val isHotelOnly = !isFlight && (rawType.contains("HOTEL") || rawType.contains("LOCAL_HOTEL") || product.safeOperatorName.contains("Yerel Otel", ignoreCase = true))
            val isPackageTour = !isFlight && !isHotelOnly

            // 0. Kategori Eşleşmesi (FLIGHT / HOTEL / PACKAGE_TOUR)
            val matchesCategory = when (params.category.uppercase()) {
                "FLIGHT" -> isFlight
                "HOTEL" -> isHotelOnly || (!isFlight && product.safeHotelName.isNotBlank())
                "PACKAGE_TOUR" -> isPackageTour
                else -> true
            }
            if (!matchesCategory) return@filter false

            // 1. Kalkış Şehri Kontrolü (Otel aramasında kalkış aranmaz!)
            val matchesDeparture = if (params.category.uppercase() == "HOTEL" || params.departureCity.isBlank()) {
                true
            } else {
                B2BTourSearchViewModel.isDepartureMatchingText(product.departureCity, params.departureCity)
            }

            // 2. Destinasyon / Varış Noktası Dinamik Kontrolü (Otel, Bölge, Alt Bölge, Ülke)
            val matchesDestination = if (params.destination.isBlank()) {
                true
            } else {
                val destLower = params.destination.lowercase().trim()
                val hotelText = "${product.hotelName} ${product.region} ${product.subRegion} ${product.country}".lowercase()
                
                // Bölgesel genişletmeler (Alt beldeler dinamik olarak kapsanır)
                when {
                    destLower.contains("belek") || destLower.contains("белек") -> {
                        hotelText.contains("belek") || hotelText.contains("белек") || hotelText.contains("boğazkent") || hotelText.contains("kadriye")
                    }
                    destLower.contains("kemer") || destLower.contains("кемер") -> {
                        hotelText.contains("kemer") || hotelText.contains("кемер") || hotelText.contains("beldibi") || hotelText.contains("göynük") || hotelText.contains("çamyuva") || hotelText.contains("tekirova") || hotelText.contains("kiriş")
                    }
                    destLower.contains("side") || destLower.contains("сиде") -> {
                        hotelText.contains("side") || hotelText.contains("сиде") || hotelText.contains("kumköy") || hotelText.contains("evrenseki") || hotelText.contains("çolaklı") || hotelText.contains("manavgat") || hotelText.contains("titreyengöl")
                    }
                    destLower.contains("alanya") || destLower.contains("алань") -> {
                        hotelText.contains("alanya") || hotelText.contains("алань") || hotelText.contains("konaklı") || hotelText.contains("mahmutlar") || hotelText.contains("okurcalar") || hotelText.contains("avsallar") || hotelText.contains("türkler")
                    }
                    destLower.contains("bodrum") || destLower.contains("бодрум") -> {
                        hotelText.contains("bodrum") || hotelText.contains("бодрум") || hotelText.contains("torba") || hotelText.contains("gümbet") || hotelText.contains("yalıkavak") || hotelText.contains("turgutreis")
                    }
                    destLower.contains("antalya") || destLower.contains("анталья") -> {
                        hotelText.contains("antalya") || hotelText.contains("анталья") || hotelText.contains("lara") || hotelText.contains("kundu") || hotelText.contains("konyaaltı")
                    }
                    else -> {
                        hotelText.contains(destLower) || B2BTourSearchViewModel.isDestinationMatching(product, params.destination)
                    }
                }
            }

            // 3. Yıldız Kontrolü (Uçuşlarda aranmaz)
            val matchesStars = if (isFlight || params.hotelStars == null || params.hotelStars <= 0) {
                true
            } else {
                product.hotelCategory >= params.hotelStars
            }

            // 4. Gece Sayısı Kontrolü (Uçuşlarda aranmaz)
            val matchesNights = if (isFlight || params.nights == null || params.nights <= 0) {
                true
            } else {
                product.nights == params.nights
            }

            // 5. Bütçe Kontrolü (RUB veya diğer para birimleri)
            val matchesBudget = if (params.maxBudgetRub != null && params.maxBudgetRub > 0) {
                val productPriceRub = when (product.currency.uppercase().trim()) {
                    "RUB" -> product.price
                    "EUR" -> product.price * 95.0
                    "USD" -> product.price * 88.0
                    else -> product.price
                }
                productPriceRub <= params.maxBudgetRub
            } else {
                true
            }

            // 6. Konsept Kontrolü (Uçuşlarda aranmaz)
            val matchesBoard = if (isFlight || params.boardType.isNullOrBlank()) {
                true
            } else {
                val meal = (product.mealType ?: "").lowercase()
                val target = params.boardType.lowercase()
                meal.contains(target) || (target.contains("uai") && (meal.contains("ultra") || meal.contains("uai"))) || (target.contains("ai") && meal.contains("all"))
            }

            matchesDeparture && matchesDestination && matchesStars && matchesNights && matchesBudget && matchesBoard
        }.sortedBy { it.price }
    }

    /**
     * Aynı oteli veya uçuş seferini tek çatı altında toplar.
     */
    fun groupProductsByHotel(products: List<UnifiedProductEntity>): List<com.mgacreative.touros.ai.model.AIGroupedHotelOffer> {
        val groupedMap = products.groupBy { prod ->
            val rawType = prod.safeProductType.uppercase()
            val isFlight = rawType.contains("FLIGHT") || rawType.contains("CHARTER") || prod.flightNumber.isNotBlank() || prod.safeTourName.startsWith("Uçuş", ignoreCase = true)
            if (isFlight) {
                val fCode = prod.flightNumber.ifBlank { prod.airlineName.ifBlank { "FLIGHT" } }
                "FLIGHT_${fCode}_${prod.safeDepartureCity}_${prod.safeRegion}"
            } else {
                prod.safeHotelName.trim().lowercase().ifBlank { prod.safeTourName.trim().lowercase() }
            }
        }

        return groupedMap.map { (_, hotelProducts) ->
            val first = hotelProducts.first()
            val sortedByPrice = hotelProducts.sortedBy { it.price }
            val cheapest = sortedByPrice.first()
            val isFlight = first.safeProductType.uppercase().contains("FLIGHT") || first.flightNumber.isNotBlank()

            val displayName = if (isFlight) {
                val airline = first.airlineName.ifBlank { "Charter" }
                val fNum = first.flightNumber.ifBlank { "" }
                "✈️ $airline $fNum (${first.safeDepartureCity} ➔ ${first.safeRegion})"
            } else {
                first.safeHotelName.ifBlank { first.safeTourName }
            }

            com.mgacreative.touros.ai.model.AIGroupedHotelOffer(
                hotelName = displayName,
                stars = if (isFlight) 0 else if (first.hotelCategory > 0) first.hotelCategory else 5,
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
