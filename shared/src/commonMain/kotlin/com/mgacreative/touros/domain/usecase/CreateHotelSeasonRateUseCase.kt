package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.HotelSeasonRate
import com.mgacreative.touros.domain.repository.HotelRepository

/**
 * 2.3.4 Sezon Fiyat Kaydı Oluşturma / Güncelleme Use Case.
 */
class CreateHotelSeasonRateUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(rate: HotelSeasonRate): Result<HotelSeasonRate> {
        if (rate.seasonName.isBlank()) {
            return Result.failure(IllegalArgumentException("Sezon adı boş olamaz."))
        }
        if (rate.startDate.isBlank() || rate.endDate.isBlank()) {
            return Result.failure(IllegalArgumentException("Tarih aralığı (başlangıç ve bitiş) belirtilmelidir."))
        }

        return if (rate.id.isBlank()) {
            hotelRepository.createSeasonRate(rate)
        } else {
            hotelRepository.updateSeasonRate(rate)
        }
    }
}
