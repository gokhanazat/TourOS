package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.domain.repository.HotelRepository

class GetHotelsUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(tenantId: String, city: String? = null): Result<List<Hotel>> {
        return hotelRepository.getHotels(tenantId, city)
    }
}
