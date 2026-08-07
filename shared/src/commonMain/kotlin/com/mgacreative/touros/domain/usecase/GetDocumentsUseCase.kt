package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.DocumentItem
import com.mgacreative.touros.domain.repository.DocumentStorageRepository

/**
 * 3.4.1 Yüklü Belgeleri Kategorik Getirme Use Case.
 */
class GetDocumentsUseCase(
    private val repository: DocumentStorageRepository
) {
    suspend operator fun invoke(tenantId: String, category: String? = null): Result<List<DocumentItem>> {
        return repository.getDocuments(tenantId, category)
    }
}
