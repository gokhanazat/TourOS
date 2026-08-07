package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.MediaItem
import com.mgacreative.touros.domain.model.MediaType

/**
 * Tur Medyası Yönetim Repository Arayüzü.
 */
interface TourMediaRepository {
    suspend fun uploadTourMedia(
        tourId: String,
        fileName: String,
        bytes: ByteArray,
        mediaType: MediaType
    ): Result<MediaItem>

    suspend fun getTourMedia(tourId: String): Result<List<MediaItem>>
    suspend fun deleteTourMedia(mediaId: String, storagePath: String): Result<Unit>
}
