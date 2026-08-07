package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.TourDetail
import com.mgacreative.touros.domain.repository.TourRepository

class GetTourDetailUseCase(
    private val tourRepository: TourRepository
) {
    suspend operator fun invoke(tourId: String): Result<TourDetail> {
        if (tourId.isBlank()) {
            return Result.failure(IllegalArgumentException("Tur ID boş olamaz"))
        }
        return tourRepository.getTourDetail(tourId)
    }
}
