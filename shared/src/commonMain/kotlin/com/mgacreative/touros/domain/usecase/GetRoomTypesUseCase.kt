package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.domain.repository.HotelRepository

class GetRoomTypesUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(hotelId: String): Result<List<RoomType>> {
        if (hotelId.isBlank()) {
            return Result.failure(IllegalArgumentException("Otel ID boş olamaz"))
        }
        return hotelRepository.getRoomTypesForHotel(hotelId)
    }
}
