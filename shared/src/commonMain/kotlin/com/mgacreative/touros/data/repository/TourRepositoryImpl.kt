package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.TourEntity
import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.model.TourCategory
import com.mgacreative.touros.domain.repository.TourRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

import com.mgacreative.touros.data.util.generateUuid
import com.mgacreative.touros.data.util.isValidUuid

class TourRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : TourRepository {

    override suspend fun getTours(tenantId: String): Result<List<Tour>> {
        return runCatching {
            if (tenantId != "ALL" && !tenantId.isValidUuid()) {
                return@runCatching emptyList()
            }
            val entities = supabaseClient.postgrest.from("tours")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                    }
                }
                .decodeList<TourEntity>()

            entities.map { entity ->
                Tour(
                    id = entity.id ?: "",
                    code = entity.code,
                    title = entity.title,
                    category = TourCategory.fromKey(entity.category),
                    country = entity.country,
                    city = entity.city,
                    durationDays = entity.durationDays,
                    basePrice = entity.basePrice,
                    childPrice06 = entity.childPrice06,
                    childPrice712 = entity.childPrice712,
                    adultCostPrice = entity.adultCostPrice,
                    childCostPrice06 = entity.childCostPrice06,
                    childCostPrice712 = entity.childCostPrice712,
                    capacity = entity.capacity,
                    minParticipants = entity.minParticipants,
                    maxParticipants = entity.maxParticipants,
                    description = entity.description,
                    cancellationPolicy = entity.cancellationPolicy,
                    insuranceDetails = entity.insuranceDetails,
                    coverImageUrl = entity.coverImageUrl,
                    tenantId = entity.tenantId,
                    isActive = entity.isActive
                )
            }
        }
    }

    override suspend fun getTourById(id: String): Result<Tour> {
        return runCatching {
            val entity = supabaseClient.postgrest.from("tours")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<TourEntity>()

            Tour(
                id = entity.id ?: "",
                code = entity.code,
                title = entity.title,
                category = TourCategory.fromKey(entity.category),
                country = entity.country,
                city = entity.city,
                durationDays = entity.durationDays,
                basePrice = entity.basePrice,
                childPrice06 = entity.childPrice06,
                childPrice712 = entity.childPrice712,
                adultCostPrice = entity.adultCostPrice,
                childCostPrice06 = entity.childCostPrice06,
                childCostPrice712 = entity.childCostPrice712,
                capacity = entity.capacity,
                minParticipants = entity.minParticipants,
                maxParticipants = entity.maxParticipants,
                description = entity.description,
                cancellationPolicy = entity.cancellationPolicy,
                insuranceDetails = entity.insuranceDetails,
                includedServices = entity.includedServices,
                excludedServices = entity.excludedServices,
                coverImageUrl = entity.coverImageUrl,
                tenantId = entity.tenantId,
                isActive = entity.isActive
            )
        }
    }

    override suspend fun getTourDetail(tourId: String): Result<com.mgacreative.touros.domain.model.TourDetail> {
        return runCatching {
            val tour = getTourById(tourId).getOrThrow()

            val departureEntities = runCatching {
                supabaseClient.postgrest.from("departures")
                    .select { filter { eq("tour_id", tourId) } }
                    .decodeList<com.mgacreative.touros.data.database.entity.DepartureEntity>()
            }.getOrDefault(emptyList())

            val itineraryEntities = runCatching {
                supabaseClient.postgrest.from("itineraries")
                    .select { filter { eq("tour_id", tourId) } }
                    .decodeList<com.mgacreative.touros.data.database.entity.ItineraryEntity>()
            }.getOrDefault(emptyList())

            val departures = departureEntities.map {
                com.mgacreative.touros.domain.model.Departure(
                    id = it.id,
                    tourId = it.tourId,
                    departureDate = it.departureDate,
                    returnDate = it.returnDate,
                    priceOverride = it.priceOverride,
                    childPriceOverride = it.childPriceOverride,
                    infantPriceOverride = it.infantPriceOverride,
                    currency = it.currency,
                    capacity = it.capacity,
                    bookedCount = it.bookedCount,
                    optionDeadlineDays = it.optionDeadlineDays,
                    isGuaranteed = it.isGuaranteed,
                    status = it.status,
                    notes = it.notes
                )
            }

            val itineraries = itineraryEntities
                .sortedBy { it.sortOrder.takeIf { s -> s > 0 } ?: it.dayNumber }
                .mapIndexed { index, it ->
                    com.mgacreative.touros.domain.model.Itinerary(
                        id = it.id,
                        tourId = it.tourId,
                        dayNumber = index + 1,
                        title = it.title,
                        description = it.description,
                        location = it.location,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        sortOrder = index + 1
                    )
                }

            com.mgacreative.touros.domain.model.TourDetail(
                tour = tour,
                departures = departures,
                itineraries = itineraries
            )
        }
    }

    override suspend fun createTour(tour: Tour): Result<Tour> {
        return runCatching {
            val targetId = if (tour.id.isValidUuid()) tour.id else generateUuid()
            val entity = TourEntity(
                id = targetId,
                code = tour.code,
                title = tour.title,
                category = tour.category.name,
                country = tour.country,
                city = tour.city,
                durationDays = tour.durationDays,
                basePrice = tour.basePrice,
                childPrice06 = tour.childPrice06,
                childPrice712 = tour.childPrice712,
                adultCostPrice = tour.adultCostPrice,
                childCostPrice06 = tour.childCostPrice06,
                childCostPrice712 = tour.childCostPrice712,
                capacity = tour.capacity,
                minParticipants = tour.minParticipants,
                maxParticipants = tour.maxParticipants,
                description = tour.description,
                cancellationPolicy = tour.cancellationPolicy,
                insuranceDetails = tour.insuranceDetails,
                includedServices = tour.includedServices,
                excludedServices = tour.excludedServices,
                coverImageUrl = tour.coverImageUrl,
                isActive = tour.isActive,
                tenantId = if (tour.tenantId.isValidUuid()) tour.tenantId else "00000000-0000-0000-0000-000000000001"
            )
            supabaseClient.postgrest.from("tours").insert(entity)
            tour.copy(id = targetId)
        }
    }

    override suspend fun updateTour(tour: Tour): Result<Tour> {
        return runCatching {
            val targetId = if (tour.id.isValidUuid()) tour.id else generateUuid()
            val entity = TourEntity(
                id = targetId,
                code = tour.code,
                title = tour.title,
                category = tour.category.name,
                country = tour.country,
                city = tour.city,
                durationDays = tour.durationDays,
                basePrice = tour.basePrice,
                childPrice06 = tour.childPrice06,
                childPrice712 = tour.childPrice712,
                adultCostPrice = tour.adultCostPrice,
                childCostPrice06 = tour.childCostPrice06,
                childCostPrice712 = tour.childCostPrice712,
                capacity = tour.capacity,
                minParticipants = tour.minParticipants,
                maxParticipants = tour.maxParticipants,
                description = tour.description,
                cancellationPolicy = tour.cancellationPolicy,
                insuranceDetails = tour.insuranceDetails,
                includedServices = tour.includedServices,
                excludedServices = tour.excludedServices,
                coverImageUrl = tour.coverImageUrl,
                isActive = tour.isActive,
                tenantId = if (tour.tenantId.isValidUuid()) tour.tenantId else "00000000-0000-0000-0000-000000000001"
            )
            supabaseClient.postgrest.from("tours").upsert(entity)
            tour.copy(id = targetId)
        }
    }

    override suspend fun toggleTourStatus(tourId: String, isActive: Boolean): Result<Unit> {
        return runCatching {
            supabaseClient.postgrest.from("tours").update(
                mapOf("is_active" to isActive)
            ) {
                filter {
                    eq("id", tourId)
                }
            }
        }
    }

    override suspend fun deleteTour(id: String): Result<Unit> {
        return runCatching {
            supabaseClient.postgrest.from("tours").delete {
                filter {
                    eq("id", id)
                }
            }
        }
    }

    override suspend fun uploadTourCoverImage(
        tourId: String,
        fileBytes: ByteArray,
        fileName: String
    ): Result<String> {
        return runCatching {
            val cleanFileName = fileName
                .substringAfterLast("/")
                .substringAfterLast("\\")
                .lowercase()
                .replace(" ", "_")
                .replace("ğ", "g")
                .replace("ü", "u")
                .replace("ş", "s")
                .replace("ı", "i")
                .replace("ö", "o")
                .replace("ç", "c")
                .replace("i̇", "i")
                .filter { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }

            val randomSuffix = (100000..999999).random()
            val safeTourId = tourId.ifBlank { "new_$randomSuffix" }
            val targetPath = "cover_${safeTourId}_${randomSuffix}_${cleanFileName.ifBlank { "cover.jpg" }}"
            
            val storageResult = runCatching {
                val bucket = supabaseClient.storage.from("tour-covers")
                bucket.upload(targetPath, fileBytes) {
                    upsert = true
                }
                bucket.publicUrl(targetPath)
            }

            if (storageResult.isSuccess && storageResult.getOrNull() != null) {
                storageResult.getOrThrow()
            } else {
                // Fallback: If Supabase Storage upload fails (bucket missing/unauthorized), save as Base64 Data URL
                @OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
                val base64Str = kotlin.io.encoding.Base64.Default.encode(fileBytes)
                val ext = cleanFileName.substringAfterLast('.', "jpg").lowercase()
                val mime = if (ext == "png") "image/png" else if (ext == "webp") "image/webp" else "image/jpeg"
                "data:$mime;base64,$base64Str"
            }
        }
    }
}
