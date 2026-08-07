package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.model.TourCategory
import com.mgacreative.touros.domain.repository.TourRepository

class GetToursUseCase(
    private val tourRepository: TourRepository
) {
    suspend fun getTours(
        tenantId: String,
        categoryFilter: TourCategory? = null,
        statusFilter: Boolean? = null,
        searchQuery: String = ""
    ): Result<List<Tour>> {
        return tourRepository.getTours(tenantId).map { tours ->
            tours.filter { tour ->
                val matchesCategory = categoryFilter == null || tour.category == categoryFilter
                val matchesStatus = statusFilter == null || tour.isActive == statusFilter
                val matchesSearch = searchQuery.isBlank() ||
                        tour.title.contains(searchQuery, ignoreCase = true) ||
                        tour.code.contains(searchQuery, ignoreCase = true) ||
                        tour.city.contains(searchQuery, ignoreCase = true)

                matchesCategory && matchesStatus && matchesSearch
            }
        }
    }

    suspend fun getTourById(id: String): Result<Tour> {
        if (id.isBlank()) {
            return Result.failure(IllegalArgumentException("Tur ID boş olamaz"))
        }
        return tourRepository.getTourById(id)
    }
}
