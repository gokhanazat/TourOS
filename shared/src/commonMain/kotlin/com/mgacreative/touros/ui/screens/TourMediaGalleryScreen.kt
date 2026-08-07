package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import com.mgacreative.touros.domain.model.MediaItem
import com.mgacreative.touros.domain.model.MediaType
import com.mgacreative.touros.ui.viewmodel.TourMediaGalleryUiState
import com.mgacreative.touros.ui.viewmodel.TourMediaGalleryViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Tur Medya Önizleme Galerisi ve Yükleme Ekranı (Material 3).
 */
@Composable
fun TourMediaGalleryScreen(
    tourId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: TourMediaGalleryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Tümü, 1: Fotoğraflar, 2: Videolar, 3: Broşürler

    LaunchedEffect(tourId) {
        viewModel.loadMedia(tourId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tur Medya Galerisi",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tura ait fotoğraf, video ve PDF broşürleri yönetin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(onClick = onNavigateBack) {
                Text("Geri Dön")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upload Action Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Medya Yükle:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        val randomId = kotlin.random.Random.nextInt(1000, 9999)
                        viewModel.uploadSampleMedia(
                            fileName = "tur_gorseli_$randomId.jpg",
                            mediaType = MediaType.IMAGE,
                            bytes = "SAMPLE_IMAGE_DATA".encodeToByteArray()
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ Fotoğraf Ekle")
                }

                OutlinedButton(
                    onClick = {
                        val randomId = kotlin.random.Random.nextInt(1000, 9999)
                        viewModel.uploadSampleMedia(
                            fileName = "tur_brosuru_$randomId.pdf",
                            mediaType = MediaType.DOCUMENT_PDF,
                            bytes = "SAMPLE_PDF_DATA".encodeToByteArray()
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ PDF Broşür Ekle")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Tümü", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Fotoğraflar", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Videolar", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("Broşürler (PDF)", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is TourMediaGalleryUiState.Loading, is TourMediaGalleryUiState.Uploading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (state is TourMediaGalleryUiState.Uploading) "Medya yükleniyor..." else "Medya galerisi yükleniyor...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            is TourMediaGalleryUiState.Error -> {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
                }
            }
            is TourMediaGalleryUiState.Success -> {
                val filteredItems = when (selectedTab) {
                    1 -> state.mediaItems.filter { it.mediaType == MediaType.IMAGE }
                    2 -> state.mediaItems.filter { it.mediaType == MediaType.VIDEO }
                    3 -> state.mediaItems.filter { it.mediaType == MediaType.DOCUMENT_PDF }
                    else -> state.mediaItems
                }

                if (filteredItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Bu kategoride yüklü medya bulunmuyor.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 180.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            MediaItemCard(
                                item = item,
                                onDelete = { viewModel.deleteMedia(item.id, "") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaItemCard(
    item: MediaItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        when (item.mediaType) {
                            MediaType.IMAGE -> MaterialTheme.colorScheme.primaryContainer
                            MediaType.VIDEO -> MaterialTheme.colorScheme.secondaryContainer
                            MediaType.DOCUMENT_PDF -> MaterialTheme.colorScheme.tertiaryContainer
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (item.mediaType) {
                        MediaType.IMAGE -> "🖼️"
                        MediaType.VIDEO -> "🎥"
                        MediaType.DOCUMENT_PDF -> "📄 PDF"
                    },
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title ?: "Medya Dosyası",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = item.mediaType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Sil", fontSize = 12.sp)
            }
        }
    }
}
