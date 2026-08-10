package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.data.database.entity.CompanyEntity
import com.mgacreative.touros.data.database.entity.DepartureEntity
import com.mgacreative.touros.data.database.entity.ItineraryEntity
import com.mgacreative.touros.data.database.entity.TourEntity
import com.mgacreative.touros.domain.model.B2CTourDetail
import com.mgacreative.touros.domain.model.DepartureOption
import com.mgacreative.touros.domain.model.TourCategory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 4.2.2 B2C Mobil / Web Tur Detayını Getirme Use Case.
 */
class GetB2CTourDetailUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tourId: String, tenantId: String): Result<B2CTourDetail> {
        return runCatching {
            val targetTourId = tourId.trim()
            if (targetTourId.isBlank()) return@runCatching B2CTourDetail()

            // 1. Doğrudan tours Tablosundan Çek
            val tourEntity = runCatching {
                supabaseClient.postgrest.from("tours")
                    .select { filter { eq("id", targetTourId) } }
                    .decodeSingleOrNull<TourEntity>()
            }.getOrNull()

            if (tourEntity != null) {
                // 2. Gün Gün Tur Programını (Itineraries) Çek
                val itineraries = runCatching {
                    supabaseClient.postgrest.from("itineraries")
                        .select { filter { eq("tour_id", targetTourId) } }
                        .decodeList<ItineraryEntity>()
                        .sortedBy { it.sortOrder.takeIf { s -> s > 0 } ?: it.dayNumber }
                }.getOrDefault(emptyList())

                val itinSummary = itineraries.joinToString(" | ") { itin ->
                    val dayNum = itin.dayNumber.takeIf { it > 0 } ?: (itineraries.indexOf(itin) + 1)
                    "$dayNum. Gün: ${itin.title}"
                }

                // 3. Kalkış Tarihlerini (Departures) Çek
                val departuresList = runCatching {
                    supabaseClient.postgrest.from("departures")
                        .select { filter { eq("tour_id", targetTourId) } }
                        .decodeList<DepartureEntity>()
                        .filter { it.status.lowercase() != "cancelled" }
                        .map { dep ->
                            DepartureOption(
                                id = dep.id,
                                departureDate = dep.departureDate,
                                returnDate = dep.returnDate,
                                price = dep.priceOverride ?: tourEntity.basePrice,
                                status = dep.status
                            )
                        }
                }.getOrDefault(emptyList())

                // 4. Acente ve Banka Hesap Bilgilerini (Companies) Çek
                val companyEntity = runCatching {
                    if (tourEntity.tenantId.isNotBlank()) {
                        supabaseClient.postgrest.from("companies")
                            .select { filter { eq("tenant_id", tourEntity.tenantId) } }
                            .decodeSingleOrNull<CompanyEntity>()
                    } else null
                }.getOrNull()

                val incList = tourEntity.includedServices
                    ?.split("\n", ",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()

                val excList = tourEntity.excludedServices
                    ?.split("\n", ",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()

                val categoryDisplayName = TourCategory.entries.find { 
                    it.name.equals(tourEntity.category, ignoreCase = true) || it.displayName.equals(tourEntity.category, ignoreCase = true) 
                }?.displayName ?: tourEntity.category

                return@runCatching B2CTourDetail(
                    tourId = tourEntity.id ?: targetTourId,
                    title = tourEntity.title,
                    description = tourEntity.description ?: "",
                    category = categoryDisplayName,
                    destinationCountry = tourEntity.country,
                    durationDays = tourEntity.durationDays,
                    price = tourEntity.basePrice,
                    rating = 0.0,
                    coverImageUrl = tourEntity.coverImageUrl,
                    includedServices = incList,
                    excludedServices = excList,
                    itinerarySummary = itinSummary,
                    agencyName = companyEntity?.name ?: companyEntity?.legalTitle ?: "TourOS Acentesi",
                    bankName = companyEntity?.bankName,
                    iban = companyEntity?.iban,
                    accountHolder = companyEntity?.accountHolder ?: companyEntity?.name,
                    paypalEmail = companyEntity?.paypalEmail,
                    paypalMeUrl = companyEntity?.paypalMeUrl,
                    availableDepartures = departuresList
                )
            }

            B2CTourDetail(tourId = targetTourId)
        }.recover { B2CTourDetail(tourId = tourId) }
    }
}
