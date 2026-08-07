package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.GuideReview
import com.mgacreative.touros.domain.repository.GuideRepository

/**
 * 2.5.4 Müşteri Tur Sonu Değerlendirmesi ile Rehber Puanı Güncelleme Use Case.
 */
class SubmitGuideReviewUseCase(
    private val guideRepository: GuideRepository
) {
    suspend operator fun invoke(review: GuideReview): Result<GuideReview> {
        if (review.customerName.isBlank()) {
            return Result.failure(IllegalArgumentException("Müşteri adı boş olamaz."))
        }
        if (review.rating !in 1..5) {
            return Result.failure(IllegalArgumentException("Puan 1 ile 5 arasında olmalıdır."))
        }
        return guideRepository.submitGuideReview(review)
    }
}
