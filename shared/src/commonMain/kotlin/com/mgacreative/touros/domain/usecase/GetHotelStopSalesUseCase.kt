package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.HotelStopSale
import com.mgacreative.touros.domain.repository.HotelRepository

/**
 * 2.3.5 Stop Sale & Release Kayıtlarını Getirme Use Case.
 */
class GetHotelStopSalesUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(hotelId: String): Result<List<HotelStopSale>> {
        return hotelRepository.getStopSalesForHotel(hotelId)
    }
}
