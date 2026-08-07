package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.HotelContract
import com.mgacreative.touros.domain.repository.HotelRepository

/**
 * 2.3.3 Otel Kontratı Oluşturma / Güncelleme Use Case.
 */
class CreateHotelContractUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(contract: HotelContract): Result<HotelContract> {
        if (contract.seasonName.isBlank()) {
            return Result.failure(IllegalArgumentException("Sezon adı boş olamaz."))
        }
        if (contract.startDate.isBlank() || contract.endDate.isBlank()) {
            return Result.failure(IllegalArgumentException("Başlangıç ve bitiş tarihleri belirtilmelidir."))
        }
        if (contract.pricePerNight < 0) {
            return Result.failure(IllegalArgumentException("Gecelik fiyat 0'dan küçük olamaz."))
        }

        return if (contract.id.isBlank()) {
            hotelRepository.createContract(contract)
        } else {
            hotelRepository.updateContract(contract)
        }
    }
}
