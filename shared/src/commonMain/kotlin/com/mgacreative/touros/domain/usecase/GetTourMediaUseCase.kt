package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.MediaItem
import com.mgacreative.touros.domain.repository.TourMediaRepository

class GetTourMediaUseCase(
    private val tourMediaRepository: TourMediaRepository
) {
    suspend fun getMedia(tourId: String): Result<List<MediaItem>> {
        if (tourId.isBlank()) {
            return Result.failure(IllegalArgumentException("Tur ID boş olamaz"))
        }
        return tourMediaRepository.getTourMedia(tourId)
    }

    suspend fun deleteMedia(mediaId: String, storagePath: String): Result<Unit> {
        if (mediaId.isBlank()) {
            return Result.failure(IllegalArgumentException("Medya ID boş olamaz"))
        }
        return tourMediaRepository.deleteTourMedia(mediaId, storagePath)
    }
}
