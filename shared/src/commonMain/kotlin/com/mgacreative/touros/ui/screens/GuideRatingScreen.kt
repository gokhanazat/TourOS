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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.GuideReview
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.GuideRatingUiState
import com.mgacreative.touros.ui.viewmodel.GuideRatingViewModel

/**
 * B2C Tur & Rehber Değerlendirme Ekranı — TourOS 0.3
 *
 * Ortada büyük yıldız seçici (1-5).
 * Altında serbest metin yorum alanı.
 * Alt kısımda Primary 'Gönder' butonu.
 */
@Composable
fun GuideRatingScreen(
    viewModel: GuideRatingViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Tur & Rehber Değerlendirme",
                subtitle = "Seyahat deneyiminizi puanlayın ve görüşlerinizi paylaşın",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val isExpanded = maxWidth >= 768.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                when (val state = uiState) {
                    is GuideRatingUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TourOSColors.Primary)
                        }
                    }
                    is GuideRatingUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Secondary))
                        }
                    }
                    is GuideRatingUiState.Success -> {
                        // 1. Rehber & Tur Canlı Puan Kartı
                        GuideScoreCard(guide = state.targetGuide)

                        if (state.successNotification != null) {
                            TourOSCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = TourOSColors.SuccessContainer,
                                contentPadding = TourOSSpacing.medium
                            ) {
                                Text(
                                    text = state.successNotification,
                                    style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                                )
                            }
                        }

                        // 2. ORTADA BÜYÜK YILDIZ SEÇİCİ & YORUM FORMU
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
                        Text(
                            "💬 Değerlendirme Geçmişi (${state.reviews.size})",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
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
}

// ─── REHBER CANLI PUAN KARTI ──────────────────────────────────────────────────

@Composable
private fun GuideScoreCard(guide: Guide) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.large
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "🚩 ${guide.fullName}",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )
                Text(
                    "Kokart No: ${guide.licenseNumber ?: "Lisanslı Rehber"}  ·  ${guide.specialization ?: "Kültür & Doğa"}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            TourOSStatusBadge(
                text = "⭐ ${guide.rating} / 5.0",
                backgroundColor = TourOSColors.SecondaryContainer,
                textColor = TourOSColors.Secondary
            )
        }
    }
}

// ─── ORTADA BÜYÜK YILDIZ SEÇİCİ VE YORUM FORMU ───────────────────────────────

@Composable
private fun ReviewFormCard(
    selectedStar: Int,
    customerName: String,
    comment: String,
    onStarSelect: (Int) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            Text(
                "📝 Değerlendirmenizi Paylaşın",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                textAlign = TextAlign.Center
            )

            // ORTADA BÜYÜK YILDIZ SEÇİCİ (1-5 YILDIZ) (Strict Rule)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { star ->
                    val isSelected = star <= selectedStar
                    Text(
                        text = if (isSelected) "⭐" else "☆",
                        style = TourOSTypography.DisplaySmall.copy(
                            color = if (isSelected) TourOSColors.Secondary else TourOSColors.TextSecondary
                        ),
                        modifier = Modifier
                            .clickable { onStarSelect(star) }
                            .padding(horizontal = TourOSSpacing.xSmall)
                    )
                }
            }

            Text(
                "Puanınız: $selectedStar / 5 Yıldız",
                style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
            )

            // Serbest Metin Yorum Alanı
            TourOSTextField(
                value = customerName,
                onValueChange = onCustomerNameChange,
                label = "Müşteri / Yolcu Adı Soyadı",
                placeholder = "Elif Yılmaz",
                modifier = Modifier.fillMaxWidth()
            )

            TourOSTextField(
                value = comment,
                onValueChange = onCommentChange,
                label = "Serbest Metin Yorumunuz",
                placeholder = "Rehberlik kalitesi, zamanlama ve tur hakkındaki düşüncelerinizi buraya yazabilirsiniz...",
                modifier = Modifier.fillMaxWidth()
            )

            // ALT KISIMDA PRIMARY 'GÖNDER' BUTONU (Strict Rule)
            TourOSButton(
                text = "🌟 Değerlendirmeyi Gönder",
                onClick = onSubmit,
                enabled = customerName.isNotBlank() && comment.isNotBlank(),
                variant = TourOSButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── GEÇMİŞ DEĞERLENDİRME İTEM KARTI ──────────────────────────────────────────

@Composable
private fun ReviewItemCard(review: GuideReview) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    review.customerName,
                    style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                )

                TourOSStatusBadge(
                    text = "⭐ ${review.rating} / 5",
                    backgroundColor = TourOSColors.SecondaryContainer,
                    textColor = TourOSColors.Secondary
                )
            }

            if (!review.comment.isNullOrBlank()) {
                Text(
                    "💬 ${review.comment}",
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                )
            }
        }
    }
}
