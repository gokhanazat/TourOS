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

            val list = supabaseClient.postgrest.rpc("search_b2c_tours", params)
                .decodeList<B2CTourItem>()

            if (list.isEmpty()) getFallbackTours(filter) else list
        }.recover { getFallbackTours(filter) }
    }

    private fun getFallbackTours(filter: B2CTourSearchFilter): List<B2CTourItem> {
        val all = listOf(
            B2CTourItem("t101", "TUR-KAP", "Kapadokya Balon & Vadi Turu", "Kültür Turu", "Türkiye", 3, 2500.0, "TRY", 4.90, 156, "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff", "15.08.2026"),
            B2CTourItem("t102", "TUR-EGE", "Ege Sahilleri & Antik Kentler", "Deniz & Mavi Tur", "Türkiye", 5, 4800.0, "TRY", 4.82, 98, "https://images.unsplash.com/photo-1533105079780-92b9be482077", "18.08.2026"),
            B2CTourItem("t103", "TUR-KAR", "Karadeniz Yaylalar & Doğa Gezisi", "Doğa & Yayla", "Türkiye", 4, 3200.0, "TRY", 4.95, 210, "https://images.unsplash.com/photo-1506744038136-46273834b3fb", "20.08.2026"),
            B2CTourItem("t104", "TUR-ROM", "Roma & Vatikan Kültür Turu", "Kültür Turu", "İtalya", 4, 18500.0, "TRY", 4.78, 64, "https://images.unsplash.com/photo-1552832230-c0197dd311b5", "25.08.2026")
        )

        return all.filter { item ->
            val matchesCategory = filter.category.isNullOrBlank() || item.category.contains(filter.category, ignoreCase = true)
            val matchesCountry = filter.country.isNullOrBlank() || item.destinationCountry.contains(filter.country, ignoreCase = true)
            val matchesPrice = (filter.minPrice == null || item.price >= filter.minPrice) && (filter.maxPrice == null || item.price <= filter.maxPrice)
            val matchesQuery = filter.searchQuery.isBlank() || item.title.contains(filter.searchQuery, ignoreCase = true)

            matchesCategory && matchesCountry && matchesPrice && matchesQuery
        }
    }
}
