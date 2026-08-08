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

private val sampleRecommendations = listOf(
    TourRecommendation(
        recommendationId = "rec-101",
        tourId = "t101",
        tourName = "Kapadokya VIP Balon & Vadi Turu",
        category = "Balon & Doğa",
        price = 12500.0,
        matchScore = 96.0,
        recommendationReason = "Geçmiş balon turu aramalarınız ve beğendikleriniz temel alınarak özel önerildi."
    ),
    TourRecommendation(
        recommendationId = "rec-102",
        tourId = "t102",
        tourName = "Ege & Efes Antik Kenti Günübirlik Turu",
        category = "Kültür & Tarih",
        price = 8400.0,
        matchScore = 91.0,
        recommendationReason = "Tarih turlarını tercih eden benzer 1,200 gezgin bu turu çok beğendi."
    ),
    TourRecommendation(
        recommendationId = "rec-103",
        tourId = "t103",
        tourName = "Antalya Yat & Koyu Macera Gezisi",
        category = "Deniz & Yaz",
        price = 9800.0,
        matchScore = 88.0,
        recommendationReason = "Yaz sezonu kişiselleştirilmiş popüler deniz rotaları eşleşmesi."
    ),
    TourRecommendation(
        recommendationId = "rec-104",
        tourId = "t104",
        tourName = "Trabzon Uzungöl & Yayla Safari Turu",
        category = "Yaylalar & Doğa",
        price = 11200.0,
        matchScore = 85.0,
        recommendationReason = "Doğa yürüyüşü ve fotoğrafçılık ilgi alanlarınıza özel öneri."
    )
)


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
                title = "Akıllı Öneriler Paneli",
                subtitle = "Yapay zeka kişiselleştirilmiş tur ve rota önerileri",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
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
