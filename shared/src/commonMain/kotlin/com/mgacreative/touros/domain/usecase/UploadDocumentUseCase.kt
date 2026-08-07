package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.DocumentItem
import com.mgacreative.touros.domain.repository.DocumentStorageRepository

/**
 * 3.4.1 Supabase Storage'a Belge Yükleme Use Case.
 */
class UploadDocumentUseCase(
    private val repository: DocumentStorageRepository
) {
    suspend operator fun invoke(
        tenantId: String,
        documentType: String,
        title: String,
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        customerId: String? = null,
        bookingId: String? = null
    ): Result<DocumentItem> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Belge başlığı boş olamaz."))
        }
        return repository.uploadDocument(
            tenantId = tenantId,
            documentType = documentType,
            title = title,
            fileBytes = fileBytes,
            fileName = fileName,
            mimeType = mimeType,
            customerId = customerId,
            bookingId = bookingId
        )
    }
}
