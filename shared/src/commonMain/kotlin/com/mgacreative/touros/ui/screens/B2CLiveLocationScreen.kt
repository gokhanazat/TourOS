package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2CLiveLocationViewModel

/**
 * B2C Canlı Konum Ekranı — TourOS 0.3
 *
 * Tam ekran harita görünümü.
 * Üstte yarı saydam bilgi kartı (Tahmini Varış Süresi - ETA, Araç Plakası, Rehber/Sürücü Adı) sabit konumda.
 */
@Composable
fun B2CLiveLocationScreen(
    viewModel: B2CLiveLocationViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val loc = state.liveLocation

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Canlı Konum Takibi",
                subtitle = "Tur otobüsü ve rehberinizin anlık harita konumu",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        // ── TAM EKRAN HARİTA DÜZENİ ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. TAM EKRAN HARİTA ZEMİN SİMÜLASYONU
            FullScreenMapViewContainer(
                lat = loc.latitude,
                lng = loc.longitude,
                speedKmh = loc.speedKmh
            )

            // 2. ÜSTTE YARI SAYDAM BİLĞİ KARTI (SABİT OVERLAY)
            TopOverlayInfoCard(
                etaMinutes = 12,
                distanceKm = 3.4,
                vehiclePlate = loc.vehiclePlate.ifBlank { "34 TUR 06" },
                guideName = loc.guideName.ifBlank { "Mehmet Can" },
                driverName = loc.driverName.ifBlank { "Ali Yılmaz" },
                lastUpdated = loc.updatedAt,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(TourOSSpacing.large)
            )

            // 3. ALTA SABİT HIZLI İLETİŞİM AKSİYON BAR'I
            BottomContactActionBar(
                onCallDriver = { },
                onWhatsappGuide = { },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(TourOSSpacing.large)
            )
        }
    }
}

// ─── ÜSTTE YARI SAYDAM BİLGİ KARTI (TAHMİNİ VARIŞ, ARAÇ / REHBER ADI) ───────

@Composable
private fun TopOverlayInfoCard(
    etaMinutes: Int,
    distanceKm: Double,
    vehiclePlate: String,
    guideName: String,
    driverName: String,
    lastUpdated: String,
    modifier: Modifier = Modifier
) {
    // YARI SAYDAM KURUMSAL BİLGİ KARTI (Strict Rule: alpha = 0.94f)
    TourOSCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface.copy(alpha = 0.94f),
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏱️", style = TourOSTypography.TitleLarge)
                    Column {
                        Text(
                            "Tahmini Varış: $etaMinutes Dakika",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                        )
                        Text(
                            "$distanceKm km uzaklıkta  ·  Son sinyal: $lastUpdated",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }

                TourOSStatusBadge(
                    text = "🔴 CANLI TAKİP",
                    backgroundColor = TourOSColors.SuccessContainer,
                    textColor = TourOSColors.Success
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Araç & Rehber Detayı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Araç & Plaka:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "🚍 $vehiclePlate (VIP Sprinter)",
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                    )
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Rehber & Sürücü:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "👤 $guideName  ·  👨‍✈️ $driverName",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                    )
                }
            }
        }
    }
}

// ─── TAM EKRAN HARİTA GÖRÜNÜM SİMÜLASYONU ────────────────────────────────────

@Composable
private fun FullScreenMapViewContainer(
    lat: Double,
    lng: Double,
    speedKmh: Double
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center
    ) {
        // Harita Izgara Ve Rota Akış Simülasyon Çizgileri
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(80.dp))
                    .background(TourOSColors.PrimaryContainer.copy(alpha = 0.5f))
                    .border(2.dp, TourOSColors.Primary, RoundedCornerShape(80.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚌 📍", style = TourOSTypography.DisplaySmall)
                    Text(
                        "CANLI ARAÇ PİNİ",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                    )
                    Text(
                        "$speedKmh km/h",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                    )
                }
            }

            Text(
                "🗺️ Harita Koordinatları: $lat° N, $lng° E",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )
        }
    }
}

// ─── ALTA SABİT HIZLI İLETİŞİM AKSİYON BAR'I ─────────────────────────────────

@Composable
private fun BottomContactActionBar(
    onCallDriver: () -> Unit,
    onWhatsappGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        TourOSButton(
            text = "📞 Sürücüyü Ara",
            onClick = onCallDriver,
            variant = TourOSButtonVariant.SECONDARY,
            modifier = Modifier.weight(1f)
        )

        TourOSButton(
            text = "💬 Rehbere Mesaj At",
            onClick = onWhatsappGuide,
            variant = TourOSButtonVariant.PRIMARY,
            modifier = Modifier.weight(1f)
        )
    }
}
