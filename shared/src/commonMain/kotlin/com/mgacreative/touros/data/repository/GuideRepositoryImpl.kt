package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.GuideEntity
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.repository.GuideRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

import com.mgacreative.touros.data.util.isValidUuid

class GuideRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : GuideRepository {

    override suspend fun getGuides(tenantId: String): Result<List<Guide>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("guides")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                    }
                }
                .decodeList<GuideEntity>()

            entities.map { entity ->
                Guide(
                    id = entity.id,
                    fullName = entity.fullName,
                    phone = entity.phone,
                    email = entity.email,
                    licenseNumber = entity.licenseNumber,
                    languages = entity.languages,
                    specialization = entity.specialization,
                    tcNo = entity.tcNo,
                    birthDate = entity.birthDate,
                    rating = entity.rating,
                    totalToursCompleted = entity.totalToursCompleted,
                    notes = entity.notes,
                    isActive = entity.isActive,
                    tenantId = entity.tenantId
                )
            }
        }
    }

    override suspend fun getGuideById(id: String): Result<Guide> {
        return runCatching {
            val entity = supabaseClient.postgrest.from("guides")
                .select { filter { eq("id", id) } }
                .decodeSingle<GuideEntity>()

            Guide(
                id = entity.id,
                fullName = entity.fullName,
                phone = entity.phone,
                email = entity.email,
                licenseNumber = entity.licenseNumber,
                languages = entity.languages,
                specialization = entity.specialization,
                tcNo = entity.tcNo,
                birthDate = entity.birthDate,
                rating = entity.rating,
                totalToursCompleted = entity.totalToursCompleted,
                notes = entity.notes,
                isActive = entity.isActive,
                tenantId = entity.tenantId
            )
        }
    }

    override suspend fun createGuide(guide: Guide): Result<Guide> {
        return runCatching {
            val entity = GuideEntity(
                fullName = guide.fullName,
                phone = guide.phone,
                email = guide.email,
                licenseNumber = guide.licenseNumber,
                languages = guide.languages,
                specialization = guide.specialization,
                tcNo = guide.tcNo,
                birthDate = guide.birthDate,
                rating = guide.rating,
                totalToursCompleted = guide.totalToursCompleted,
                notes = guide.notes,
                isActive = guide.isActive,
                tenantId = guide.tenantId
            )
            val created = supabaseClient.postgrest.from("guides")
                .insert(entity) { select() }
                .decodeSingle<GuideEntity>()

            guide.copy(id = created.id)
        }
    }

    override suspend fun updateGuide(guide: Guide): Result<Guide> {
        return runCatching {
            val entity = GuideEntity(
                id = guide.id,
                fullName = guide.fullName,
                phone = guide.phone,
                email = guide.email,
                licenseNumber = guide.licenseNumber,
                languages = guide.languages,
                specialization = guide.specialization,
                tcNo = guide.tcNo,
                birthDate = guide.birthDate,
                rating = guide.rating,
                totalToursCompleted = guide.totalToursCompleted,
                notes = guide.notes,
                isActive = guide.isActive,
                tenantId = guide.tenantId
            )
            supabaseClient.postgrest.from("guides")
                .update(entity) { filter { eq("id", guide.id) } }
            guide
        }
    }

    override suspend fun deleteGuide(id: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("guides")
                .delete { filter { eq("id", id) } }
            true
        }
    }

    override suspend fun submitGuideReview(review: com.mgacreative.touros.domain.model.GuideReview): Result<com.mgacreative.touros.domain.model.GuideReview> {
        return runCatching {
            val entity = com.mgacreative.touros.data.database.entity.GuideReviewEntity(
                guideId = review.guideId,
                departureId = review.departureId,
                bookingId = review.bookingId,
                customerName = review.customerName,
                rating = review.rating,
                comment = review.comment,
                tenantId = review.tenantId
            )
            val created = supabaseClient.postgrest.from("guide_reviews")
                .insert(entity) { select() }
                .decodeSingle<com.mgacreative.touros.data.database.entity.GuideReviewEntity>()

            review.copy(id = created.id, createdAt = created.createdAt)
        }
    }

    override suspend fun getGuideReviews(guideId: String): Result<List<com.mgacreative.touros.domain.model.GuideReview>> {
        return runCatching {
            val list = supabaseClient.postgrest.from("guide_reviews")
                .select { filter { eq("guide_id", guideId) } }
                .decodeList<com.mgacreative.touros.data.database.entity.GuideReviewEntity>()

            list.map {
                com.mgacreative.touros.domain.model.GuideReview(
                    id = it.id,
                    guideId = it.guideId,
                    departureId = it.departureId,
                    bookingId = it.bookingId,
                    customerName = it.customerName,
                    rating = it.rating,
                    comment = it.comment,
                    tenantId = it.tenantId,
                    createdAt = it.createdAt
                )
            }
        }
    }
}
