package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.recommendation.TourRecommendation
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

/**
 * Akıllı Öneriler Paneli — TourOS 0.3
 *
 * Yatay kaydırmalı öneri kartları şeridi ('Senin için önerilenler' başlığıyla).
 * Dashboard veya B2C ana ekranına gömülü bir bölüm olarak tasarlanmıştır.
 */
@Composable
fun B2CRecommendedToursSection(
    recommendations: List<TourRecommendation>,
    onSelectTour: (String) -> Unit = {},
    onSeeAllClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TourOSSpacing.small),
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        // ── 'SENİN İÇİN ÖNERİLENLER' BAŞLIĞI VE TÜMÜNÜ GÖR AKSİYONU ─────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💡", style = TourOSTypography.TitleLarge)
                Column {
                    Text(
                        "Senin İçin Önerilenler",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                    Text(
                        "Yapay zeka kişiselleştirilmiş seyahat rotaları",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }
            }

            TourOSButton(
                text = "Tümünü Gör →",
                onClick = onSeeAllClick,
                variant = TourOSButtonVariant.TERTIARY
            )
        }

        // ── YATAY KAYDIRMALI ÖNERİ KARTLARI ŞERİDİ (LAZYROW) ───────────────────
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(recommendations) { item ->
                SmartRecommendationCard(
                    recommendation = item,
                    onSelect = { onSelectTour(item.tourId) }
                )
            }
        }
    }
}

// ─── AKILLI ÖNERİ KARTI BİLEŞENİ ───────────────────────────────────────────────

@Composable
fun SmartRecommendationCard(
    recommendation: TourRecommendation,
    onSelect: () -> Unit
) {
    val matchScore = recommendation.matchScore.toInt().coerceIn(75, 99)

    TourOSCard(
        modifier = Modifier
            .width(280.dp)
            .wrapContentHeight(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // EŞLEŞME SKORU ROZETİ VE KATEGORİ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TourOSStatusBadge(
                    text = "%$matchScore Eşleşme",
                    backgroundColor = TourOSColors.SuccessContainer,
                    textColor = TourOSColors.Success
                )

                TourOSStatusBadge(
                    text = recommendation.category.ifBlank { "VIP Tur" },
                    backgroundColor = TourOSColors.PrimaryContainer,
                    textColor = TourOSColors.Primary
                )
            }

            // TUR BAŞLIĞI
            Text(
                text = recommendation.tourName,
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ÖNERİ NEDENİ
            Text(
                text = recommendation.recommendationReason.ifBlank { "Geçmiş seyahat aramalarınız ve beğendikleriniz temel alınarak önerildi." },
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = TourOSColors.Divider)

            // FİYAT VE İNCELE BUTONU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Başlangıç Fiyatı",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                    Text(
                        "₺ ${formatRecMoney(recommendation.price)}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }

                TourOSButton(
                    text = "İncele →",
                    onClick = onSelect,
                    variant = TourOSButtonVariant.PRIMARY
                )
            }
        }
    }
}

private fun formatRecMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}
