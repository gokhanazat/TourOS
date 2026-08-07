package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.repository.TourRepository

class CreateOrUpdateTourUseCase(
    private val tourRepository: TourRepository
) {
    suspend operator fun invoke(tour: Tour): Result<Tour> {
        if (tour.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Tur Adı boş olamaz"))
        }
        if (tour.code.isBlank()) {
            return Result.failure(IllegalArgumentException("Tur Kodu boş olamaz"))
        }
        if (tour.country.isBlank() || tour.city.isBlank()) {
            return Result.failure(IllegalArgumentException("Ülke ve Şehir alanları doldurulmalıdır"))
        }
        if (tour.durationDays <= 0) {
            return Result.failure(IllegalArgumentException("Tur süresi en az 1 gün olmalıdır"))
        }
        if (tour.minParticipants > tour.maxParticipants) {
            return Result.failure(IllegalArgumentException("Minimum katılımcı sayısı maksimum sayıdan büyük olamaz"))
        }

        return if (tour.id.isBlank()) {
            tourRepository.createTour(tour)
        } else {
            tourRepository.updateTour(tour)
        }
    }
}
