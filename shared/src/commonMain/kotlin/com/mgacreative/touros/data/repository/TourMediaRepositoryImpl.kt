package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.DocumentEntity
import com.mgacreative.touros.data.database.entity.ImageEntity
import com.mgacreative.touros.domain.model.MediaItem
import com.mgacreative.touros.domain.model.MediaType
import com.mgacreative.touros.domain.repository.TourMediaRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.days

class TourMediaRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : TourMediaRepository {

    private val bucketName = "tour-media"

    override suspend fun uploadTourMedia(
        tourId: String,
        fileName: String,
        bytes: ByteArray,
        mediaType: MediaType
    ): Result<MediaItem> {
        return runCatching {
            val path = "tours/$tourId/${mediaType.key}_${fileName}"
            supabaseClient.storage.from(bucketName).upload(path, bytes) {
                upsert = true
            }

            val publicUrl = supabaseClient.storage.from(bucketName).publicUrl(path)

            if (mediaType == MediaType.DOCUMENT_PDF) {
                val docEntity = DocumentEntity(
                    ownerType = "tour",
                    ownerId = tourId,
                    title = fileName,
                    fileUrl = publicUrl,
                    fileType = "pdf",
                    fileSizeBytes = bytes.size.toLong()
                )
                val created = supabaseClient.postgrest.from("documents").insert(docEntity) {
                    select()
                }.decodeSingle<DocumentEntity>()

                MediaItem(
                    id = created.id,
                    tourId = tourId,
                    mediaType = mediaType,
                    url = publicUrl,
                    title = fileName,
                    fileSizeBytes = bytes.size.toLong()
                )
            } else {
                val imgEntity = ImageEntity(
                    ownerType = "tour",
                    ownerId = tourId,
                    url = publicUrl,
                    altText = fileName,
                    fileSizeBytes = bytes.size.toLong()
                )
                val created = supabaseClient.postgrest.from("images").insert(imgEntity) {
                    select()
                }.decodeSingle<ImageEntity>()

                MediaItem(
                    id = created.id,
                    tourId = tourId,
                    mediaType = mediaType,
                    url = publicUrl,
                    title = fileName,
                    fileSizeBytes = bytes.size.toLong()
                )
            }
        }
    }

    override suspend fun getTourMedia(tourId: String): Result<List<MediaItem>> {
        return runCatching {
            val list = mutableListOf<MediaItem>()

            // 1. Image & Video kayıtlarını çek
            val images = supabaseClient.postgrest.from("images")
                .select {
                    filter {
                        eq("owner_type", "tour")
                        eq("owner_id", tourId)
                    }
                }
                .decodeList<ImageEntity>()

            images.forEach { img ->
                val type = if (img.url.endsWith(".mp4", ignoreCase = true) || img.url.endsWith(".mov", ignoreCase = true)) {
                    MediaType.VIDEO
                } else {
                    MediaType.IMAGE
                }
                list.add(
                    MediaItem(
                        id = img.id,
                        tourId = tourId,
                        mediaType = type,
                        url = img.url,
                        title = img.altText ?: "Fotoğraf",
                        fileSizeBytes = img.fileSizeBytes
                    )
                )
            }

            // 2. Document (PDF Broşür) kayıtlarını çek
            val docs = supabaseClient.postgrest.from("documents")
                .select {
                    filter {
                        eq("owner_type", "tour")
                        eq("owner_id", tourId)
                    }
                }
                .decodeList<DocumentEntity>()

            docs.forEach { doc ->
                list.add(
                    MediaItem(
                        id = doc.id,
                        tourId = tourId,
                        mediaType = MediaType.DOCUMENT_PDF,
                        url = doc.fileUrl,
                        title = doc.title,
                        fileSizeBytes = doc.fileSizeBytes
                    )
                )
            }

            list
        }
    }

    override suspend fun deleteTourMedia(mediaId: String, storagePath: String): Result<Unit> {
        return runCatching {
            if (storagePath.isNotBlank()) {
                supabaseClient.storage.from(bucketName).delete(listOf(storagePath))
            }
            supabaseClient.postgrest.from("images").delete {
                filter {
                    eq("id", mediaId)
                }
            }
            supabaseClient.postgrest.from("documents").delete {
                filter {
                    eq("id", mediaId)
                }
            }
        }
    }
}
