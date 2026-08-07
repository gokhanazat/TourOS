package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.DocumentItem
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetDocumentsUseCase
import com.mgacreative.touros.domain.usecase.UploadDocumentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentManagementUiState(
    val selectedCategory: String = "all", // all, passport, visa, contract, voucher, pdf, photo
    val documents: List<DocumentItem> = emptyList(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class DocumentManagementViewModel(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val uploadDocumentUseCase: UploadDocumentUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentManagementUiState())
    val uiState: StateFlow<DocumentManagementUiState> = _uiState.asStateFlow()

    init {
        loadDocuments()
    }

    fun loadDocuments(category: String = _uiState.value.selectedCategory) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedCategory = category)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getDocumentsUseCase(tenantId, category)
            res.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    documents = list,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message
                )
            }
        }
    }

    fun uploadSampleDocument(title: String, type: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val sampleBytes = "Sample document content bytes for Supabase storage".encodeToByteArray()
            val extension = if (type == "photo" || type == "visa") "jpg" else "pdf"
            val mime = if (extension == "jpg") "image/jpeg" else "application/pdf"
            val fileName = "${type}_sample.$extension"

            val res = uploadDocumentUseCase(
                tenantId = tenantId,
                documentType = type,
                title = title,
                fileBytes = sampleBytes,
                fileName = fileName,
                mimeType = mime,
                customerId = "c1",
                bookingId = "b1"
            )

            res.onSuccess { item ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    documents = listOf(item) + _uiState.value.documents,
                    notificationMessage = "✅ '${item.title}' Belgesi Supabase Storage 'documents/$type/' Klasörüne Yüklendi."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Belge yükleme başarısız."
                )
            }
        }
    }
}
