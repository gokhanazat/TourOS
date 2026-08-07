package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.repository.GuideRepository

/**
 * 2.5.1 Rehber Listesi Getirme Use Case.
 */
class GetGuidesUseCase(
    private val guideRepository: GuideRepository
) {
    suspend operator fun invoke(tenantId: String): Result<List<Guide>> {
        return guideRepository.getGuides(tenantId)
    }
}
