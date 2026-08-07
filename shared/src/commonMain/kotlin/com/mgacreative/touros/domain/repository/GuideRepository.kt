package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.GuideReview

/**
 * 2.5.1 ve 2.5.4 Rehber Repository Arayüzü.
 */
interface GuideRepository {
    suspend fun getGuides(tenantId: String): Result<List<Guide>>
    suspend fun getGuideById(id: String): Result<Guide>
    suspend fun createGuide(guide: Guide): Result<Guide>
    suspend fun updateGuide(guide: Guide): Result<Guide>
    suspend fun deleteGuide(id: String): Result<Boolean>
    suspend fun submitGuideReview(review: GuideReview): Result<GuideReview>
    suspend fun getGuideReviews(guideId: String): Result<List<GuideReview>>
}
