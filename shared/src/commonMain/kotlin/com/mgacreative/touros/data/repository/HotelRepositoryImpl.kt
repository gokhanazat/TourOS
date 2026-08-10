package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.HotelContractEntity
import com.mgacreative.touros.data.database.entity.HotelEntity
import com.mgacreative.touros.data.database.entity.HotelSeasonRateEntity
import com.mgacreative.touros.data.database.entity.HotelStopSaleEntity
import com.mgacreative.touros.data.database.entity.RoomTypeEntity
import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.domain.model.HotelContract
import com.mgacreative.touros.domain.model.HotelSeasonRate
import com.mgacreative.touros.domain.model.HotelStopSale
import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.domain.repository.HotelRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

import com.mgacreative.touros.data.util.isValidUuid

class HotelRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : HotelRepository {




    override suspend fun getHotels(tenantId: String, city: String?): Result<List<Hotel>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("hotels")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                        if (!city.isNullOrBlank()) {
                            eq("city", city)
                        }
                    }
                }
                .decodeList<HotelEntity>()

            entities.map { entity ->
                Hotel(
                    id = entity.id,
                    name = entity.name,
                    slug = entity.slug,
                    starRating = entity.starRating ?: 4,
                    address = entity.address,
                    city = entity.city,
                    country = entity.country,
                    phone = entity.phone,
                    email = entity.email,
                    website = entity.website,
                    description = entity.description,
                    coverImageUrl = entity.coverImageUrl,
                    isActive = entity.isActive,
                    tenantId = entity.tenantId
                )
            }
        }
    }

    override suspend fun getHotelById(id: String): Result<Hotel> {
        return runCatching {
            val entity = supabaseClient.postgrest.from("hotels")
                .select { filter { eq("id", id) } }
                .decodeSingle<HotelEntity>()

            Hotel(
                id = entity.id,
                name = entity.name,
                slug = entity.slug,
                starRating = entity.starRating ?: 4,
                address = entity.address,
                city = entity.city,
                country = entity.country,
                phone = entity.phone,
                email = entity.email,
                website = entity.website,
                description = entity.description,
                coverImageUrl = entity.coverImageUrl,
                isActive = entity.isActive,
                tenantId = entity.tenantId
            )
        }
    }

    override suspend fun createHotel(hotel: Hotel): Result<Hotel> {
        return runCatching {
            val validTenantId = if (hotel.tenantId.isValidUuid()) hotel.tenantId else "00000000-0000-0000-0000-000000000001"
            val entityJson = buildJsonObject {
                put("name", hotel.name)
                put("slug", hotel.name.lowercase().replace(" ", "-"))
                put("city", hotel.city)
                put("country", hotel.country.ifBlank { "Türkiye" })
                if (!hotel.address.isNullOrBlank()) put("address", hotel.address)
                put("star_rating", hotel.starRating ?: 4)
                if (!hotel.phone.isNullOrBlank()) put("phone", hotel.phone)
                if (!hotel.email.isNullOrBlank()) put("email", hotel.email)
                if (!hotel.website.isNullOrBlank()) put("website", hotel.website)
                if (!hotel.description.isNullOrBlank()) put("description", hotel.description)
                if (!hotel.coverImageUrl.isNullOrBlank()) put("cover_image_url", hotel.coverImageUrl)
                put("is_active", hotel.isActive)
                put("tenant_id", validTenantId)
            }
            val created = supabaseClient.postgrest.from("hotels")
                .insert(entityJson) { select() }
                .decodeSingle<HotelEntity>()

            hotel.copy(id = created.id, tenantId = created.tenantId)
        }
    }

    override suspend fun updateHotel(hotel: Hotel): Result<Hotel> {
        return runCatching {
            val validTenantId = if (hotel.tenantId.isValidUuid()) hotel.tenantId else "00000000-0000-0000-0000-000000000001"
            val entityJson = buildJsonObject {
                put("name", hotel.name)
                put("slug", hotel.name.lowercase().replace(" ", "-"))
                put("city", hotel.city)
                put("country", hotel.country.ifBlank { "Türkiye" })
                if (!hotel.address.isNullOrBlank()) put("address", hotel.address)
                put("star_rating", hotel.starRating ?: 4)
                if (!hotel.phone.isNullOrBlank()) put("phone", hotel.phone)
                if (!hotel.email.isNullOrBlank()) put("email", hotel.email)
                if (!hotel.website.isNullOrBlank()) put("website", hotel.website)
                if (!hotel.description.isNullOrBlank()) put("description", hotel.description)
                if (!hotel.coverImageUrl.isNullOrBlank()) put("cover_image_url", hotel.coverImageUrl)
                put("is_active", hotel.isActive)
                put("tenant_id", validTenantId)
            }
            supabaseClient.postgrest.from("hotels")
                .update(entityJson) { filter { eq("id", hotel.id) } }
            hotel
        }
    }

    override suspend fun deleteHotel(id: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("hotels")
                .delete { filter { eq("id", id) } }
            true
        }
    }

    override suspend fun getRoomTypesForHotel(hotelId: String): Result<List<RoomType>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("room_types")
                .select { filter { eq("hotel_id", hotelId) } }
                .decodeList<RoomTypeEntity>()

            entities.map { entity ->
                RoomType(
                    id = entity.id,
                    hotelId = entity.hotelId,
                    name = entity.name,
                    description = entity.description,
                    basePricePerNight = entity.basePricePerNight,
                    currency = entity.currency,
                    maxOccupancy = entity.maxOccupancy,
                    totalRooms = entity.totalRooms,
                    allotment = entity.allotment,
                    bookedRooms = entity.bookedRooms,
                    isActive = entity.isActive,
                    tenantId = entity.tenantId
                )
            }
        }
    }

    override suspend fun createRoomType(roomType: RoomType): Result<RoomType> {
        return runCatching {
            val entity = RoomTypeEntity(
                hotelId = roomType.hotelId,
                name = roomType.name,
                description = roomType.description,
                basePricePerNight = roomType.basePricePerNight,
                currency = roomType.currency,
                maxOccupancy = roomType.maxOccupancy,
                totalRooms = roomType.totalRooms,
                allotment = roomType.allotment,
                bookedRooms = roomType.bookedRooms,
                isActive = roomType.isActive,
                tenantId = roomType.tenantId
            )
            val created = supabaseClient.postgrest.from("room_types")
                .insert(entity) { select() }
                .decodeSingle<RoomTypeEntity>()

            roomType.copy(id = created.id)
        }
    }

    override suspend fun updateRoomType(roomType: RoomType): Result<RoomType> {
        return runCatching {
            val entity = RoomTypeEntity(
                id = roomType.id,
                hotelId = roomType.hotelId,
                name = roomType.name,
                description = roomType.description,
                basePricePerNight = roomType.basePricePerNight,
                currency = roomType.currency,
                maxOccupancy = roomType.maxOccupancy,
                totalRooms = roomType.totalRooms,
                allotment = roomType.allotment,
                bookedRooms = roomType.bookedRooms,
                isActive = roomType.isActive,
                tenantId = roomType.tenantId
            )
            supabaseClient.postgrest.from("room_types")
                .update(entity) { filter { eq("id", roomType.id) } }
            roomType
        }
    }

    override suspend fun getContractsForHotel(hotelId: String): Result<List<HotelContract>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("hotel_contracts")
                .select { filter { eq("hotel_id", hotelId) } }
                .decodeList<HotelContractEntity>()

            entities.map { entity ->
                HotelContract(
                    id = entity.id,
                    hotelId = entity.hotelId,
                    roomTypeId = entity.roomTypeId,
                    seasonName = entity.seasonName,
                    startDate = entity.startDate,
                    endDate = entity.endDate,
                    pricePerNight = entity.pricePerNight,
                    currency = entity.currency,
                    allotment = entity.allotment,
                    releaseDays = entity.releaseDays,
                    mealPlan = entity.mealPlan,
                    notes = entity.notes,
                    isActive = entity.isActive,
                    tenantId = entity.tenantId,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun createContract(contract: HotelContract): Result<HotelContract> {
        return runCatching {
            val entity = HotelContractEntity(
                hotelId = contract.hotelId,
                roomTypeId = contract.roomTypeId,
                seasonName = contract.seasonName,
                startDate = contract.startDate,
                endDate = contract.endDate,
                pricePerNight = contract.pricePerNight,
                currency = contract.currency,
                allotment = contract.allotment,
                releaseDays = contract.releaseDays,
                mealPlan = contract.mealPlan,
                notes = contract.notes,
                isActive = contract.isActive,
                tenantId = contract.tenantId
            )
            val created = supabaseClient.postgrest.from("hotel_contracts")
                .insert(entity) { select() }
                .decodeSingle<HotelContractEntity>()

            contract.copy(id = created.id)
        }
    }

    override suspend fun updateContract(contract: HotelContract): Result<HotelContract> {
        return runCatching {
            val entity = HotelContractEntity(
                id = contract.id,
                hotelId = contract.hotelId,
                roomTypeId = contract.roomTypeId,
                seasonName = contract.seasonName,
                startDate = contract.startDate,
                endDate = contract.endDate,
                pricePerNight = contract.pricePerNight,
                currency = contract.currency,
                allotment = contract.allotment,
                releaseDays = contract.releaseDays,
                mealPlan = contract.mealPlan,
                notes = contract.notes,
                isActive = contract.isActive,
                tenantId = contract.tenantId
            )
            supabaseClient.postgrest.from("hotel_contracts")
                .update(entity) { filter { eq("id", contract.id) } }
            contract
        }
    }

    override suspend fun deleteContract(id: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("hotel_contracts")
                .delete { filter { eq("id", id) } }
            true
        }
    }

    override suspend fun getSeasonRatesForHotel(hotelId: String): Result<List<HotelSeasonRate>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("hotel_season_rates")
                .select { filter { eq("hotel_id", hotelId) } }
                .decodeList<HotelSeasonRateEntity>()

            entities.map { entity ->
                HotelSeasonRate(
                    id = entity.id,
                    hotelId = entity.hotelId,
                    roomTypeId = entity.roomTypeId,
                    roomTypeName = entity.roomTypeName,
                    seasonName = entity.seasonName,
                    startDate = entity.startDate,
                    endDate = entity.endDate,
                    singlePrice = entity.singlePrice,
                    doublePrice = entity.doublePrice,
                    triplePrice = entity.triplePrice,
                    extraBedPrice = entity.extraBedPrice,
                    childPrice = entity.childPrice,
                    costPrice = entity.costPrice,
                    salePrice = entity.salePrice,
                    allotment = entity.allotment,
                    currency = entity.currency,
                    mealPlan = entity.mealPlan,
                    minStayDays = entity.minStayDays,
                    isActive = entity.isActive,
                    tenantId = entity.tenantId,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun createSeasonRate(rate: HotelSeasonRate): Result<HotelSeasonRate> {
        return runCatching {
            val entity = HotelSeasonRateEntity(
                hotelId = rate.hotelId,
                roomTypeId = rate.roomTypeId,
                roomTypeName = rate.roomTypeName,
                seasonName = rate.seasonName,
                startDate = rate.startDate,
                endDate = rate.endDate,
                singlePrice = rate.singlePrice,
                doublePrice = rate.doublePrice,
                triplePrice = rate.triplePrice,
                extraBedPrice = rate.extraBedPrice,
                childPrice = rate.childPrice,
                costPrice = rate.costPrice,
                salePrice = rate.salePrice,
                allotment = rate.allotment,
                currency = rate.currency,
                mealPlan = rate.mealPlan,
                minStayDays = rate.minStayDays,
                isActive = rate.isActive,
                tenantId = rate.tenantId
            )
            val created = supabaseClient.postgrest.from("hotel_season_rates")
                .insert(entity) { select() }
                .decodeSingle<HotelSeasonRateEntity>()

            rate.copy(id = created.id)
        }
    }

    override suspend fun updateSeasonRate(rate: HotelSeasonRate): Result<HotelSeasonRate> {
        return runCatching {
            val entity = HotelSeasonRateEntity(
                id = rate.id,
                hotelId = rate.hotelId,
                roomTypeId = rate.roomTypeId,
                roomTypeName = rate.roomTypeName,
                seasonName = rate.seasonName,
                startDate = rate.startDate,
                endDate = rate.endDate,
                singlePrice = rate.singlePrice,
                doublePrice = rate.doublePrice,
                triplePrice = rate.triplePrice,
                extraBedPrice = rate.extraBedPrice,
                childPrice = rate.childPrice,
                costPrice = rate.costPrice,
                salePrice = rate.salePrice,
                allotment = rate.allotment,
                currency = rate.currency,
                mealPlan = rate.mealPlan,
                minStayDays = rate.minStayDays,
                isActive = rate.isActive,
                tenantId = rate.tenantId
            )
            supabaseClient.postgrest.from("hotel_season_rates")
                .update(entity) { filter { eq("id", rate.id) } }
            rate
        }
    }

    override suspend fun deleteSeasonRate(id: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("hotel_season_rates")
                .delete { filter { eq("id", id) } }
            true
        }
    }

    override suspend fun deleteSeasonRatesForHotel(hotelId: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("hotel_season_rates")
                .delete { filter { eq("hotel_id", hotelId) } }
            true
        }
    }

    override suspend fun getStopSalesForHotel(hotelId: String): Result<List<HotelStopSale>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("hotel_stop_sales")
                .select { filter { eq("hotel_id", hotelId) } }
                .decodeList<HotelStopSaleEntity>()

            entities.map { entity ->
                HotelStopSale(
                    id = entity.id,
                    hotelId = entity.hotelId,
                    roomTypeId = entity.roomTypeId,
                    actionType = entity.actionType,
                    startDate = entity.startDate,
                    endDate = entity.endDate,
                    reason = entity.reason,
                    isActive = entity.isActive,
                    tenantId = entity.tenantId,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun createStopSale(stopSale: HotelStopSale): Result<HotelStopSale> {
        return runCatching {
            val entity = HotelStopSaleEntity(
                hotelId = stopSale.hotelId,
                roomTypeId = stopSale.roomTypeId,
                actionType = stopSale.actionType,
                startDate = stopSale.startDate,
                endDate = stopSale.endDate,
                reason = stopSale.reason,
                isActive = stopSale.isActive,
                tenantId = stopSale.tenantId
            )
            val created = supabaseClient.postgrest.from("hotel_stop_sales")
                .insert(entity) { select() }
                .decodeSingle<HotelStopSaleEntity>()

            stopSale.copy(id = created.id)
        }
    }

    override suspend fun toggleStopSaleStatus(id: String, isActive: Boolean): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("hotel_stop_sales")
                .update(buildJsonObject { put("is_active", isActive) }) { filter { eq("id", id) } }
            true
        }
    }

    override suspend fun deleteStopSale(id: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("hotel_stop_sales")
                .delete { filter { eq("id", id) } }
            true
        }
    }

    override suspend fun checkStopSaleActive(
        hotelId: String,
        roomTypeId: String?,
        checkDate: String
    ): Result<Boolean> {
        return runCatching {
            val list = supabaseClient.postgrest.from("hotel_stop_sales")
                .select {
                    filter {
                        eq("hotel_id", hotelId)
                        eq("action_type", "STOP_SALE")
                        eq("is_active", true)
                        gte("end_date", checkDate)
                        lte("start_date", checkDate)
                    }
                }
                .decodeList<HotelStopSaleEntity>()

            if (roomTypeId.isNullOrBlank()) {
                list.isNotEmpty()
            } else {
                list.any { it.roomTypeId == null || it.roomTypeId == roomTypeId }
            }
        }
    }
}



