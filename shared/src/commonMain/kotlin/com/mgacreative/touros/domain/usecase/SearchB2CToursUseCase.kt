package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2CTourItem
import com.mgacreative.touros.domain.model.B2CTourSearchFilter
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.2.1 B2C Müşteri Mobil Uygulaması Tur Arama ve Filtreleme Use Case.
 */
class SearchB2CToursUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(filter: B2CTourSearchFilter, tenantId: String): Result<List<B2CTourItem>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                if (!filter.category.isNullOrBlank()) put("p_category", filter.category)
                if (!filter.country.isNullOrBlank()) put("p_country", filter.country)
                if (filter.minPrice != null) put("p_min_price", filter.minPrice)
                if (filter.maxPrice != null) put("p_max_price", filter.maxPrice)
                if (!filter.startDate.isNullOrBlank()) put("p_start_date", filter.startDate)
                if (!filter.endDate.isNullOrBlank()) put("p_end_date", filter.endDate)
                if (filter.searchQuery.isNotBlank()) put("p_search_query", filter.searchQuery)
            }

            val rpcResult = runCatching {
                supabaseClient.postgrest.rpc("search_b2c_tours", params)
                    .decodeList<B2CTourItem>()
            }.getOrDefault(emptyList())

            if (rpcResult.isNotEmpty()) {
                rpcResult
            } else {
                // Fallback: Query real 'tours' table directly from Supabase DB
                supabaseClient.postgrest.from("tours")
                    .select {
                        filter {
                            eq("is_active", true)
                        }
                    }
                    .decodeList<com.mgacreative.touros.data.database.entity.TourEntity>()
                    .map { entity ->
                        B2CTourItem(
                            tourId = entity.id ?: "",
                            tourCode = entity.code,
                            title = entity.title,
                            category = entity.category,
                            destinationCountry = entity.country,
                            durationDays = entity.durationDays,
                            price = entity.basePrice,
                            currency = "TRY",
                            rating = 5.0,
                            reviewCount = 0,
                            coverImageUrl = entity.coverImageUrl ?: "",
                            nextDepartureDate = ""
                        )
                    }
                    .filter { item ->
                        val matchesCategory = filter.category.isNullOrBlank() || item.category.contains(filter.category, ignoreCase = true)
                        val matchesCountry = filter.country.isNullOrBlank() || item.destinationCountry.contains(filter.country, ignoreCase = true)
                        val matchesPrice = (filter.minPrice == null || item.price >= filter.minPrice) && (filter.maxPrice == null || item.price <= filter.maxPrice)
                        val matchesQuery = filter.searchQuery.isBlank() || item.title.contains(filter.searchQuery, ignoreCase = true)

                        matchesCategory && matchesCountry && matchesPrice && matchesQuery
                    }
            }
        }
    }
}
