package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.MediaItem
import com.mgacreative.touros.domain.model.MediaType
import com.mgacreative.touros.domain.usecase.GetTourMediaUseCase
import com.mgacreative.touros.domain.usecase.UploadTourMediaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TourMediaGalleryUiState {
    data object Loading : TourMediaGalleryUiState
    data class Success(val mediaItems: List<MediaItem>) : TourMediaGalleryUiState
    data object Uploading : TourMediaGalleryUiState
    data class Error(val message: String) : TourMediaGalleryUiState
}

class TourMediaGalleryViewModel(
    private val getTourMediaUseCase: GetTourMediaUseCase,
    private val uploadTourMediaUseCase: UploadTourMediaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TourMediaGalleryUiState>(TourMediaGalleryUiState.Loading)
    val uiState: StateFlow<TourMediaGalleryUiState> = _uiState.asStateFlow()

    private var currentTourId: String = ""

    fun loadMedia(tourId: String) {
        currentTourId = tourId
        viewModelScope.launch {
            _uiState.value = TourMediaGalleryUiState.Loading
            getTourMediaUseCase.getMedia(tourId)
                .onSuccess { list ->
                    _uiState.value = TourMediaGalleryUiState.Success(list)
                }
                .onFailure { exception ->
                    _uiState.value = TourMediaGalleryUiState.Error(
                        exception.message ?: "Medya galerisi yüklenemedi"
                    )
                }
        }
    }

    fun uploadSampleMedia(fileName: String, mediaType: MediaType, bytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = TourMediaGalleryUiState.Uploading
            uploadTourMediaUseCase(currentTourId, fileName, bytes, mediaType)
                .onSuccess {
                    loadMedia(currentTourId)
                }
                .onFailure { exception ->
                    _uiState.value = TourMediaGalleryUiState.Error(
                        exception.message ?: "Medya yüklenirken hata oluştu"
                    )
                }
        }
    }

    fun deleteMedia(mediaId: String, storagePath: String) {
        viewModelScope.launch {
            getTourMediaUseCase.deleteMedia(mediaId, storagePath)
                .onSuccess {
                    loadMedia(currentTourId)
                }
        }
    }
}
