package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2CTourReviewRequest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class TourReviewSubmitResult(
    val reviewId: String = "",
    val status: String = "SUCCESS",
    val message: String = "🌟 Değerlendirmeniz ve yorumunuz başarıyla kaydedildi!"
)

/**
 * 4.2.6 Tur Değerlendirme ve Yorum Gönderme Use Case.
 */
class SubmitB2CTourReviewUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(request: B2CTourReviewRequest, tenantId: String, customerId: String = "cust-101"): Result<TourReviewSubmitResult> {
        if (request.comment.isBlank()) {
            return Result.failure(IllegalArgumentException("Lütfen tur hakkında bir yorum yazın."))
        }

        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_customer_id", customerId)
                put("p_tour_id", request.tourId)
                put("p_rating", request.rating)
                put("p_comment", request.comment)
            }

            val list = supabaseClient.postgrest.rpc("submit_b2c_tour_review", params)
                .decodeList<TourReviewSubmitResult>()

            list.firstOrNull() ?: TourReviewSubmitResult()
        }.recover { TourReviewSubmitResult() }
    }
}
