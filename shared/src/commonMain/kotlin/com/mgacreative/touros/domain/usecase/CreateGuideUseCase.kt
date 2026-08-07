package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.repository.GuideRepository

/**
 * 2.5.1 Rehber Ekleme / Güncelleme Use Case.
 */
class CreateGuideUseCase(
    private val guideRepository: GuideRepository
) {
    suspend operator fun invoke(guide: Guide): Result<Guide> {
        if (guide.fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Rehber adı soyadı boş olamaz."))
        }

        return if (guide.id.isBlank()) {
            guideRepository.createGuide(guide)
        } else {
            guideRepository.updateGuide(guide)
        }
    }
}
