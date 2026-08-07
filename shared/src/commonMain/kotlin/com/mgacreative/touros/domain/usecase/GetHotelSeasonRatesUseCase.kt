package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.HotelSeasonRate
import com.mgacreative.touros.domain.repository.HotelRepository

/**
 * 2.3.4 Sezon Fiyat Matrisi Getirme Use Case.
 */
class GetHotelSeasonRatesUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(hotelId: String): Result<List<HotelSeasonRate>> {
        return hotelRepository.getSeasonRatesForHotel(hotelId)
    }
}
