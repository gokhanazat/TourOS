package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.recommendation.TourRecommendation
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

private val sampleRecommendations = emptyList<TourRecommendation>()



/**
 * Akıllı Öneriler Ekranı — TourOS 0.3
 *
 * Dashboard veya B2C ana ekranına gömülebilen veya bağımsız açılabilen Akıllı Öneriler Paneli gösterimi.
 */
@Composable
fun SmartRecommendationsScreen(
    onNavigateToTourDetail: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val recommendationsList by remember { mutableStateOf(sampleRecommendations) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Akıllı Öneri & Çapraz Satış Motoru"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("AI destekli tur, transfer ve otel çapraz satış önerileri"),
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .padding(TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
            ) {
                // DASHBOARD VEYA B2C ANA EKRANINA GÖMÜLÜ AKILLI ÖNERİLER PANELERİ ŞERİDİ (Strict Rule)
                B2CRecommendedToursSection(
                    recommendations = recommendationsList,
                    onSelectTour = onNavigateToTourDetail,
                    onSeeAllClick = { }
                )

                HorizontalDivider(color = TourOSColors.Divider)

                // DİĞER KİŞİSELLEŞTİRİLMİŞ ÖNERİ DETAY KARTLARI (BİLGİLENDİRME)
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.35f),
                    contentPadding = TourOSSpacing.large
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        Text(
                            "🤖 Yapay Zeka Öneri Motoru Nasıl Çalışır?",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                        )
                        Text(
                            "Sistem; geçmiş seyahat geçmişiniz, arama filtreleriniz, konum ve mevsim tercihleriniz temel alınarak %90+ üzeri uyumluluktaki turları akıllı şeritte otomatik olarak sıralar.",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                        )
                    }
                }
            }
        }
    }
}
