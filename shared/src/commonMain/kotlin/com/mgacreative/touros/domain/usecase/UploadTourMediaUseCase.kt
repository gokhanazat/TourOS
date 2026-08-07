package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.MediaItem
import com.mgacreative.touros.domain.model.MediaType
import com.mgacreative.touros.domain.repository.TourMediaRepository

class UploadTourMediaUseCase(
    private val tourMediaRepository: TourMediaRepository
) {
    suspend operator fun invoke(
        tourId: String,
        fileName: String,
        bytes: ByteArray,
        mediaType: MediaType
    ): Result<MediaItem> {
        if (tourId.isBlank()) {
            return Result.failure(IllegalArgumentException("Tur ID boş olamaz"))
        }
        if (fileName.isBlank()) {
            return Result.failure(IllegalArgumentException("Dosya adı boş olamaz"))
        }
        if (bytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Yüklenecek dosya içeriği boş"))
        }
        return tourMediaRepository.uploadTourMedia(tourId, fileName, bytes, mediaType)
    }
}
