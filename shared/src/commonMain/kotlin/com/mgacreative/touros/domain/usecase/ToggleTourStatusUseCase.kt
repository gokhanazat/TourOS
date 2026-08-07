package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.repository.TourRepository

class ToggleTourStatusUseCase(
    private val tourRepository: TourRepository
) {
    suspend operator fun invoke(tourId: String, isActive: Boolean): Result<Unit> {
        if (tourId.isBlank()) {
            return Result.failure(IllegalArgumentException("Tur ID boş olamaz"))
        }
        return tourRepository.toggleTourStatus(tourId, isActive)
    }
}
