package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.GuideReview
import com.mgacreative.touros.ui.viewmodel.GuideRatingUiState
import com.mgacreative.touros.ui.viewmodel.GuideRatingViewModel

/**
 * 2.5.4 Otomatik Rehber Puan Güncelleme ve Tur Sonu Değerlendirme Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideRatingScreen(
    viewModel: GuideRatingViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⭐ Rehber Otomatik Puan Güncelleme", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = uiState) {
                is GuideRatingUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is GuideRatingUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is GuideRatingUiState.Success -> {
                    // 1. Rehber Canlı Puan Kartı
                    GuideScoreCard(guide = state.targetGuide)

                    if (state.successNotification != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.successNotification,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // 2. Müşteri Değerlendirme Formu
                    ReviewFormCard(
                        selectedStar = state.selectedStar,
                        customerName = state.customerNameInput,
                        comment = state.commentInput,
                        onStarSelect = { viewModel.setSelectedStar(it) },
                        onCustomerNameChange = { viewModel.updateCustomerName(it) },
                        onCommentChange = { viewModel.updateComment(it) },
                        onSubmit = { viewModel.submitReview() }
                    )

                    // 3. Geçmiş Müşteri Yorumları Listesi
                    Text("💬 Müşteri Değerlendirmeleri ve Puan Geçmişi:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(state.reviews) { review ->
                            ReviewItemCard(review = review)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuideScoreCard(guide: Guide) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🚩 ${guide.fullName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Kokart: ${guide.licenseNumber ?: "Lisanslı Rehber"} | Uzmanlık: ${guide.specialization ?: "Kültür"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF08A)
                ) {
                    Text(
                        text = "⭐ ${guide.rating} / 5.0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF854D0E),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Text("Otomatik Güncel Puan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ReviewFormCard(
    selectedStar: Int,
    customerName: String,
    comment: String,
    onStarSelect: (Int) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("📝 Tur Sonu Müşteri Değerlendirme Formu", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            // Yıldız Seçimi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { star ->
                    Text(
                        text = if (star <= selectedStar) "⭐" else "☆",
                        fontSize = 28.sp,
                        modifier = Modifier.clickable { onStarSelect(star) }.padding(horizontal = 4.dp)
                    )
                }
            }

            OutlinedTextField(
                value = customerName,
                onValueChange = onCustomerNameChange,
                label = { Text("Müşteri / Yolcu Adı Soyadı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                label = { Text("Rehberlik Yorumu & Değerlendirme Notu") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                enabled = customerName.isNotBlank()
            ) {
                Text("⭐ Değerlendirmeyi Gönder ve Rehber Puanını Güncelle")
            }
        }
    }
}

@Composable
fun ReviewItemCard(review: GuideReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(review.customerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEF08A)
                ) {
                    Text(
                        text = "⭐ ${review.rating} / 5",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF854D0E),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (!review.comment.isNullOrBlank()) {
                Text("💬 ${review.comment}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
