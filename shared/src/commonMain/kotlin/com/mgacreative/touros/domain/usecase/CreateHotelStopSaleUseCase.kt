package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.HotelStopSale
import com.mgacreative.touros.domain.repository.HotelRepository

/**
 * 2.3.5 Satış Durdurma (Stop Sale) veya Release İşlemi Uygulama Use Case.
 * İlgili tarih aralığında stop sale uygulandığında departure / booking süreçlerini otomatik kontrol eder.
 */
class CreateHotelStopSaleUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(stopSale: HotelStopSale): Result<HotelStopSale> {
        if (stopSale.startDate.isBlank() || stopSale.endDate.isBlank()) {
            return Result.failure(IllegalArgumentException("Satış durdurma için tarih aralığı seçilmelidir."))
        }
        if (stopSale.hotelId.isBlank()) {
            return Result.failure(IllegalArgumentException("Otel seçimi zorunludur."))
        }

        return hotelRepository.createStopSale(stopSale)
    }
}
