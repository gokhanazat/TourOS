package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mgacreative.touros.domain.model.Departure
import com.mgacreative.touros.domain.model.Itinerary
import com.mgacreative.touros.domain.model.TourDetail
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSEmptyState
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.TourDetailUiState
import com.mgacreative.touros.ui.viewmodel.TourDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Tur Detay Ekranı.
 * - Üstte geniş Kapak Görseli / Galeri alanı.
 * - Sekmeli Yapı: Genel Bilgiler, Kalkışlar, Koşullar.
 * - Expanded: Sekme içeriklerinde yan yana geniş yerleşim.
 */
@Composable
fun TourDetailScreen(
    tourId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToMediaGallery: (String) -> Unit = {},
    onNavigateToDepartureForm: (String, String?) -> Unit = { _, _ -> },
    viewModel: TourDetailViewModel = koinViewModel()
) {
    LaunchedEffect(tourId) {
        viewModel.loadTourDetail(tourId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Tur Detayları",
                subtitle = "Tur programı, kalkış tarihleri ve fiyat şartları",
                navigationIcon = {
                    TourOSButton(
                        text = "← Geri",
                        onClick = onNavigateBack,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                },
                actions = {
                    TourOSButton(
                        text = "🖼️ Galeri",
                        onClick = { onNavigateToMediaGallery(tourId) },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                    Spacer(modifier = Modifier.width(TourOSSpacing.small))
                    TourOSButton(
                        text = "✏️ Düzenle",
                        onClick = { onNavigateToEdit(tourId) },
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            )
        },
        containerColor = TourOSColors.Surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TourDetailUiState.Loading -> {
                    TourOSLoadingIndicator(message = "Tur detayları yükleniyor...")
                }
                is TourDetailUiState.Error -> {
                    TourOSEmptyState(
                        title = "Detay Yüklenemedi",
                        description = state.message,
                        actionButtonText = "← Geri Dön",
                        onActionClick = onNavigateBack
                    )
                }
                is TourDetailUiState.Success -> {
                    TourDetailContent(
                        tourDetail = state.tourDetail,
                        onNavigateToDepartureForm = onNavigateToDepartureForm,
                        onDeleteDepartureClick = { depId -> viewModel.deleteDeparture(depId, state.tourDetail.tour.id) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Yeniden kullanılabilir Tur Detayı İçerik Bileşeni.
 */
@Composable
fun TourDetailContent(
    tourDetail: TourDetail,
    onNavigateToDepartureForm: (String, String?) -> Unit = { _, _ -> },
    onDeleteDepartureClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tour = tourDetail.tour
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Genel Bilgiler",
        "Kalkışlar (${tourDetail.departures.size})",
        "Koşullar & Şartlar"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TourOSColors.Surface)
    ) {
        // Üst Geniş Kapak Görseli / Banner Kartı
        TourOSCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TourOSSpacing.large),
            backgroundColor = TourOSColors.Background,
            borderColor = TourOSColors.Border,
            contentPadding = TourOSSpacing.large
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                // Cover Image Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!tour.coverImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = tour.coverImageUrl,
                            contentDescription = tour.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🏔️", style = TourOSTypography.DisplaySmall)
                            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                            Text(
                                text = "${tour.city}, ${tour.country}",
                                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        TourOSStatusBadge(
                            text = tour.category.displayName,
                            backgroundColor = TourOSColors.PrimaryContainer,
                            textColor = TourOSColors.Primary
                        )

                        TourOSStatusBadge(
                            text = if (tour.isActive) "AKTİF TUR" else "PASİF TUR",
                            backgroundColor = if (tour.isActive) TourOSColors.SuccessContainer else TourOSColors.Surface,
                            textColor = if (tour.isActive) TourOSColors.Success else TourOSColors.TextDisabled
                        )
                    }

                    Text(
                        text = "Kod: ${tour.code}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary)
                    )
                }

                Text(
                    text = tour.title,
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xLarge)
                ) {
                    Text(text = "📍 ${tour.city}, ${tour.country}", style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextSecondary))
                    Text(text = "⏱️ ${tour.durationDays} Gün", style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextSecondary))
                    Text(text = "👥 Kapasite: ${tour.capacity} Kişi", style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextSecondary))
                }
            }
        }

        // Sekmeli Navigasyon Barı
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = TourOSColors.Background,
            contentColor = TourOSColors.Primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = TourOSTypography.TitleMedium.copy(
                                color = if (selectedTabIndex == index) TourOSColors.Primary else TourOSColors.TextSecondary
                            )
                        )
                    }
                )
            }
        }

        // Sekme İçeriği (Adaptif Expanded/Compact Düzen)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(TourOSSpacing.large)
        ) {
            val isExpanded = maxWidth >= 840.dp

            when (selectedTabIndex) {
                0 -> OverviewSection(tour = tour, itineraries = tourDetail.itineraries, isExpanded = isExpanded)
                1 -> DeparturesSection(
                    tourId = tour.id,
                    departures = tourDetail.departures,
                    onAddDepartureClick = onNavigateToDepartureForm,
                    onDeleteDepartureClick = onDeleteDepartureClick
                )
                2 -> TermsAndConditionsSection(tour = tour)
            }
        }
    }
}

@Composable
private fun OverviewSection(
    tour: com.mgacreative.touros.domain.model.Tour,
    itineraries: List<Itinerary>,
    isExpanded: Boolean
) {
    if (isExpanded) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // Solda: Tur Programı (Itinerary)
            Column(modifier = Modifier.weight(1.4f)) {
                Text(text = "Gün Gün Tur Programı", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                ItineraryList(itineraries = itineraries)
            }

            // Sağda: Tur Açıklaması & Özet Bilgiler
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
            ) {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.Background,
                    borderColor = TourOSColors.Border,
                    contentPadding = TourOSSpacing.large
                ) {
                    Text(text = "Tur Hakkında", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                    Spacer(modifier = Modifier.height(TourOSSpacing.small))
                    Text(
                        text = tour.description ?: "Tur açıklaması bulunmuyor.",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                    )
                }

                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.Background,
                    borderColor = TourOSColors.Border,
                    contentPadding = TourOSSpacing.large
                ) {
                    Text(text = "Kontenjan & Katılım Şartları", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                    Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoColumn(label = "Min Katılımcı", value = "${tour.minParticipants} Kişi")
                        InfoColumn(label = "Max Katılımcı", value = "${tour.maxParticipants} Kişi")
                        InfoColumn(label = "Toplam Kontenjan", value = "${tour.capacity} Kişi")
                    }
                }
            }
        }
    } else {
        // Compact Tek Sütun
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.Background,
                    borderColor = TourOSColors.Border,
                    contentPadding = TourOSSpacing.large
                ) {
                    Text(text = "Tur Açıklaması", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                    Spacer(modifier = Modifier.height(TourOSSpacing.small))
                    Text(
                        text = tour.description ?: "Tur açıklaması bulunmuyor.",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                    )
                }
            }

            item {
                Text(text = "Gün Gün Tur Programı", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
            }

            items(itineraries) { item ->
                ItineraryItemCard(item = item)
            }
        }
    }
}

@Composable
private fun ItineraryList(itineraries: List<Itinerary>) {
    if (itineraries.isEmpty()) {
        TourOSEmptyState(title = "Henüz Tur Programı Eklenmemiş", description = "Bu tur için gün bazlı gezi rotası bulunmuyor.")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
            modifier = Modifier.fillMaxSize()
        ) {
            items(itineraries) { item ->
                ItineraryItemCard(item = item)
            }
        }
    }
}

@Composable
private fun ItineraryItemCard(item: Itinerary) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.large
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.dayNumber}.Gün",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )
            }

            Spacer(modifier = Modifier.width(TourOSSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                if (!item.location.isNullOrBlank()) {
                    Text(text = "📍 ${item.location}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                }
                if (!item.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                    Text(text = item.description, style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                }
            }
        }
    }
}

@Composable
private fun DeparturesSection(
    tourId: String,
    departures: List<Departure>,
    onAddDepartureClick: (String, String?) -> Unit,
    onDeleteDepartureClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = TourOSSpacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kalkış Seferleri (${departures.size})",
                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
            )
            TourOSButton(
                text = "➕ Yeni Kalkış Tarihi Ekle",
                onClick = { onAddDepartureClick(tourId, null) },
                variant = TourOSButtonVariant.PRIMARY
            )
        }

        if (departures.isEmpty()) {
            TourOSEmptyState(
                title = "Tanımlı Kalkış Yok",
                description = "Bu tur için henüz aktif bir kalkış veya hareket tarihi eklenmemiş.",
                actionButtonText = "➕ Kalkış Tarihi Ekle",
                onActionClick = { onAddDepartureClick(tourId, null) }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                modifier = Modifier.fillMaxSize()
            ) {
                items(departures) { dep ->
                    TourOSCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = TourOSColors.Background,
                        borderColor = TourOSColors.Border,
                        contentPadding = TourOSSpacing.large
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📅 Kalkış: ${dep.departureDate} ${dep.returnDate?.let { "- Dönüş: $it" } ?: ""}",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                )
                                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                Text(
                                    text = "👥 Doluluk: ${dep.bookedCount} / ${dep.capacity ?: "Sınırsız"} Kişi",
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                            ) {
                                TourOSStatusBadge(
                                    text = dep.status.uppercase(),
                                    backgroundColor = TourOSColors.PrimaryContainer,
                                    textColor = TourOSColors.Primary
                                )
                                TourOSButton(
                                    text = "✏️ Düzenle",
                                    onClick = { onAddDepartureClick(tourId, dep.id) },
                                    variant = TourOSButtonVariant.SECONDARY
                                )
                                TourOSButton(
                                    text = "🗑️ Sil",
                                    onClick = { onDeleteDepartureClick(dep.id) },
                                    variant = TourOSButtonVariant.TERTIARY
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
private fun TermsAndConditionsSection(tour: com.mgacreative.touros.domain.model.Tour) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Text(text = "İptal & İade Şartları", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.small))
                Text(
                    text = tour.cancellationPolicy ?: "Standard iptal şartları geçerlidir. Harekat gününden 7 gün öncesine kadar kesintisiz iade yapılmaktadır.",
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                )
            }
        }

        item {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Text(text = "Sigorta & Teminat Bilgileri", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.small))
                Text(
                    text = tour.insuranceDetails ?: "Zorunlu seyahat sağlık sigortası tur fiyatına dahildir.",
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                )
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(text = label, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
        Text(text = value, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
    }
}
