package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaType(val displayName: String) {
    @SerialName("image") IMAGE("Fotoğraf"),
    @SerialName("video") VIDEO("Video"),
    @SerialName("document") DOCUMENT_PDF("Broşür (PDF)");

    companion object {
        fun fromKey(key: String): MediaType =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) || it.key.equals(key, ignoreCase = true) } ?: IMAGE
    }

    val key: String get() = name.lowercase()
}

/**
 * Tur Medya Elemanı Domain Modeli.
 */
@Serializable
data class MediaItem(
    val id: String = "",
    val tourId: String,
    val mediaType: MediaType = MediaType.IMAGE,
    val url: String,
    val title: String? = null,
    val fileSizeBytes: Long? = null,
    val tenantId: String = ""
)
