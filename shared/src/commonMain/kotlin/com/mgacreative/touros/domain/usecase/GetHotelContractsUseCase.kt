package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.HotelContract
import com.mgacreative.touros.domain.repository.HotelRepository

/**
 * 2.3.3 Otel Kontratlarını Getirme Use Case.
 */
class GetHotelContractsUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(hotelId: String): Result<List<HotelContract>> {
        return hotelRepository.getContractsForHotel(hotelId)
    }
}
