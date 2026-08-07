package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
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
import com.mgacreative.touros.domain.model.GuideRecommendation
import com.mgacreative.touros.ui.viewmodel.DepartureInfo
import com.mgacreative.touros.ui.viewmodel.GuideAssignmentUiState
import com.mgacreative.touros.ui.viewmodel.GuideAssignmentViewModel

/**
 * 2.5.2 Akıllı Rehber Atama ve Öneri Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideAssignmentScreen(
    viewModel: GuideAssignmentViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 Tur Kalkışına Rehber Atama", fontWeight = FontWeight.Bold) },
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
                is GuideAssignmentUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is GuideAssignmentUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is GuideAssignmentUiState.Success -> {
                    // 1. Tur Kalkış Özet Başlık Kartı
                    DepartureSummaryHeaderCard(departure = state.departure)

                    if (state.assignedSuccessMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.assignedSuccessMessage,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // 2. Filtreler: Dil Filtresi & Sadece Müsait Olanlar Toggle
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🗣️ Tur Dili Filtresi & Müsaitlik:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = state.selectedLanguage == null,
                                onClick = { viewModel.setLanguageFilter(null) },
                                label = { Text("Tüm Diller", fontSize = 11.sp) }
                            )
                            listOf("İngilizce", "Almanca", "Fransızca", "İspanyolca").forEach { lang ->
                                FilterChip(
                                    selected = state.selectedLanguage == lang,
                                    onClick = { viewModel.setLanguageFilter(lang) },
                                    label = { Text(lang, fontSize = 11.sp) }
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Checkbox(
                                checked = state.onlyAvailableFilter,
                                onCheckedChange = { viewModel.toggleOnlyAvailable(it) }
                            )
                            Text("🟢 Sadece Müsait ve Görevde Olmayan Rehberleri Göster", fontSize = 12.sp)
                        }
                    }

                    // 3. Rehber Öneri Listesi
                    Text("🌟 Tur için En Uygun Akıllı Öneriler:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    if (state.recommendations.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text("Seçilen kriterlere uygun rehber bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(state.recommendations) { rec ->
                                GuideRecommendationCard(
                                    recommendation = rec,
                                    isCurrentlyAssigned = state.departure.currentGuideName == rec.guide.fullName,
                                    onAssignClick = { viewModel.assignGuide(rec.guide) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DepartureSummaryHeaderCard(departure: DepartureInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(departure.tourTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Gerekli Dil: ${departure.requiredLanguage}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text("📅 Kalkış Tarihi: ${departure.departureDate} | 👥 Yolcu Sayısı: ${departure.bookedPax}/${departure.capacity} Pax", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (departure.currentGuideName != null) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (departure.currentGuideName != null) "🚩 Atanmış Rehber: ${departure.currentGuideName}" else "⚠️ Bu kalkışa henüz rehber atanmadı!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (departure.currentGuideName != null) Color(0xFF15803D) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun GuideRecommendationCard(
    recommendation: GuideRecommendation,
    isCurrentlyAssigned: Boolean,
    onAssignClick: () -> Unit
) {
    val guide = recommendation.guide

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyAssigned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "🎯 %${recommendation.matchScore} Uyum",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF08A)
                    ) {
                        Text(
                            text = "⭐ ${guide.rating}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF854D0E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (recommendation.isAvailable) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = if (recommendation.isAvailable) "🟢 Müsait" else "🔴 Görevde",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (recommendation.isAvailable) Color(0xFF15803D) else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column {
                Text(guide.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Kokart No: ${guide.licenseNumber ?: "Lisanssız"} | 📞 ${guide.phone ?: "-"} | 🚩 ${guide.totalToursCompleted} Tur Geçmişi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 Öneri Gerekçesi: ${recommendation.recommendationReason}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }

            if (!guide.languages.isNullOrEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🗣️ Bildiği Diller:", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                    guide.languages.forEach { lang ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (recommendation.languageMatch && lang.contains("İngilizce")) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(lang, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }

            Button(
                onClick = onAssignClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCurrentlyAssigned,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isCurrentlyAssigned) "✅ Tura Atanmış Rehber" else "🚩 Bu Rehberi Tura Ata")
            }
        }
    }
}
