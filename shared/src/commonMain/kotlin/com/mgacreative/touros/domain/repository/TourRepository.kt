package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.model.TourDetail

/**
 * Tur Yönetimi Repository Arayüzü.
 */
interface TourRepository {
    suspend fun getTours(tenantId: String): Result<List<Tour>>
    suspend fun getTourById(id: String): Result<Tour>
    suspend fun getTourDetail(tourId: String): Result<TourDetail>
    suspend fun createTour(tour: Tour): Result<Tour>
    suspend fun updateTour(tour: Tour): Result<Tour>
    suspend fun toggleTourStatus(tourId: String, isActive: Boolean): Result<Unit>
    suspend fun deleteTour(id: String): Result<Unit>
    suspend fun uploadTourCoverImage(tourId: String, fileBytes: ByteArray, fileName: String): Result<String>
}
