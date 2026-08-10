package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.domain.model.HotelContract
import com.mgacreative.touros.domain.model.HotelSeasonRate
import com.mgacreative.touros.domain.model.HotelStopSale
import com.mgacreative.touros.domain.model.RoomType

/**
 * Otel, Oda Tipi, Kontrat, Sezon Fiyat Matrisi ve Stop Sale / Release Repository Arayüzü.
 */
interface HotelRepository {
    suspend fun getHotels(tenantId: String, city: String? = null): Result<List<Hotel>>
    suspend fun getHotelById(id: String): Result<Hotel>
    suspend fun createHotel(hotel: Hotel): Result<Hotel>
    suspend fun updateHotel(hotel: Hotel): Result<Hotel>
    suspend fun deleteHotel(id: String): Result<Boolean>
    suspend fun getRoomTypesForHotel(hotelId: String): Result<List<RoomType>>
    suspend fun createRoomType(roomType: RoomType): Result<RoomType>
    suspend fun updateRoomType(roomType: RoomType): Result<RoomType>
    suspend fun getContractsForHotel(hotelId: String): Result<List<HotelContract>>
    suspend fun createContract(contract: HotelContract): Result<HotelContract>
    suspend fun updateContract(contract: HotelContract): Result<HotelContract>
    suspend fun deleteContract(id: String): Result<Boolean>
    suspend fun getSeasonRatesForHotel(hotelId: String): Result<List<HotelSeasonRate>>
    suspend fun createSeasonRate(rate: HotelSeasonRate): Result<HotelSeasonRate>
    suspend fun updateSeasonRate(rate: HotelSeasonRate): Result<HotelSeasonRate>
    suspend fun deleteSeasonRate(id: String): Result<Boolean>
    suspend fun deleteSeasonRatesForHotel(hotelId: String): Result<Boolean>
    suspend fun getStopSalesForHotel(hotelId: String): Result<List<HotelStopSale>>
    suspend fun createStopSale(stopSale: HotelStopSale): Result<HotelStopSale>
    suspend fun toggleStopSaleStatus(id: String, isActive: Boolean): Result<Boolean>
    suspend fun deleteStopSale(id: String): Result<Boolean>
    suspend fun checkStopSaleActive(hotelId: String, roomTypeId: String?, checkDate: String): Result<Boolean>
}
