package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.DocumentItem

/**
 * 3.4.1 Supabase Storage & Belge Yönetim Repository Arayüzü.
 */
interface DocumentStorageRepository {
    suspend fun getDocuments(tenantId: String, category: String? = null): Result<List<DocumentItem>>
    suspend fun uploadDocument(
        tenantId: String,
        documentType: String,
        title: String,
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        customerId: String? = null,
        bookingId: String? = null
    ): Result<DocumentItem>
}
