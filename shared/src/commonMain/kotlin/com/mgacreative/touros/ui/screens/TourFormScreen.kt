package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.decodeToImageBitmap
import com.mgacreative.touros.domain.model.TourCategory
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSSnackbarHost
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.TourFormUiState
import com.mgacreative.touros.ui.viewmodel.TourFormViewModel
import org.koin.compose.viewmodel.koinViewModel

import coil3.compose.AsyncImage

import com.mgacreative.touros.utils.MAX_IMAGE_SIZE_BYTES
import com.mgacreative.touros.utils.rememberFilePickerLauncher

/**
 * TourOS 0.3 Tasarım Sistemine uygun Tur Oluşturma / Düzenleme Ekranı.
 * - Expanded (Desktop/Web): İki sütunlu düzen (Solda form, sağda Medya/Galeri yükleme).
 * - Compact (Mobil): Tek sütun, Medya alanı en üstte.
 * - Kategori Seçimi: Tek Primary tonlu chip grubu (#1F4E5F).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TourFormScreen(
    tourId: String? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: TourFormViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val loadedTour by viewModel.loadedTour.collectAsState()
    val departuresDrafts by viewModel.departuresDrafts.collectAsState()
    val itinerariesDrafts by viewModel.itinerariesDrafts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TourCategory.CULTURAL) }
    var country by remember { mutableStateOf("Türkiye") }
    var city by remember { mutableStateOf("İstanbul") }
    var durationDaysText by remember { mutableStateOf("1") }
    var basePriceText by remember { mutableStateOf("0") }
    var childPrice06Text by remember { mutableStateOf("0") }
    var childPrice712Text by remember { mutableStateOf("0") }
    var adultCostPriceText by remember { mutableStateOf("0") }
    var childCostPrice06Text by remember { mutableStateOf("0") }
    var childCostPrice712Text by remember { mutableStateOf("0") }
    var capacityText by remember { mutableStateOf("20") }
    var minParticipantsText by remember { mutableStateOf("1") }
    var maxParticipantsText by remember { mutableStateOf("30") }
    var description by remember { mutableStateOf("") }
    var cancellationPolicy by remember { mutableStateOf("") }
    var insuranceDetails by remember { mutableStateOf("") }
    var includedServices by remember { mutableStateOf("") }
    var excludedServices by remember { mutableStateOf("") }
    var initialDepartureDate by remember { mutableStateOf("") }
    var initialReturnDate by remember { mutableStateOf("") }
    var errorMessageOverride by remember { mutableStateOf<String?>(null) }

    var coverFileName by remember { mutableStateOf<String?>(null) }
    var coverBytes by remember { mutableStateOf<ByteArray?>(null) }
    var galleryItems by remember { mutableStateOf<List<Pair<String, ByteArray>>>(emptyList()) }

    LaunchedEffect(tourId) {
        viewModel.loadTourForEdit(tourId)
    }

    LaunchedEffect(uiState) {
        if (uiState is TourFormUiState.Success) {
            kotlinx.coroutines.delay(800)
            onNavigateBack()
        }
    }

    LaunchedEffect(loadedTour) {
        loadedTour?.let { tour ->
            title = tour.title
            code = tour.code
            selectedCategory = tour.category
            country = tour.country
            city = tour.city
            durationDaysText = tour.durationDays.toString()
            basePriceText = if (tour.basePrice > 0) tour.basePrice.toString() else ""
            childPrice06Text = if (tour.childPrice06 > 0) tour.childPrice06.toString() else ""
            childPrice712Text = if (tour.childPrice712 > 0) tour.childPrice712.toString() else ""
            adultCostPriceText = if (tour.adultCostPrice > 0) tour.adultCostPrice.toString() else ""
            childCostPrice06Text = if (tour.childCostPrice06 > 0) tour.childCostPrice06.toString() else ""
            childCostPrice712Text = if (tour.childCostPrice712 > 0) tour.childCostPrice712.toString() else ""
            capacityText = tour.capacity.toString()
            minParticipantsText = tour.minParticipants.toString()
            maxParticipantsText = tour.maxParticipants.toString()
            description = tour.description ?: ""
            cancellationPolicy = tour.cancellationPolicy ?: ""
            insuranceDetails = tour.insuranceDetails ?: ""
            includedServices = tour.includedServices ?: ""
            excludedServices = tour.excludedServices ?: ""
        }
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = if (tourId == null) "Yeni Tur Oluştur" else "Turu Düzenle",
                subtitle = "Tur detaylarını, lokasyon bilgilerini ve kapak medyasını tanımlayın",
                navigationIcon = {
                    TourOSButton(
                        text = "← İptal",
                        onClick = onNavigateBack,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                }
            )
        },
        snackbarHost = { TourOSSnackbarHost(hostState = snackbarHostState) },
        containerColor = TourOSColors.Surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(TourOSColors.Surface)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TourOSSpacing.large)
            ) {
                val isCompact = maxWidth < 768.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                ) {
                    // Custom Size Error or State Banners
                    val activeError = errorMessageOverride ?: (uiState as? TourFormUiState.Error)?.message
                    if (activeError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                .background(TourOSColors.ErrorContainer)
                                .padding(TourOSSpacing.medium)
                        ) {
                            Text(
                                text = activeError,
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error)
                            )
                        }
                    } else if (uiState is TourFormUiState.Success) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                .background(TourOSColors.SuccessContainer)
                                .padding(TourOSSpacing.medium)
                        ) {
                            Text(
                                text = (uiState as TourFormUiState.Success).message,
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Success)
                            )
                        }
                    }

                    if (isCompact) {
                        // COMPACT LAYOUT (Mobil: Medya alanı en üstte, sonra form)
                        MediaUploadSection(
                            coverFileName = coverFileName,
                            coverBytes = coverBytes,
                            existingCoverUrl = loadedTour?.coverImageUrl,
                            galleryItems = galleryItems,
                            onCoverSelected = { fileName, bytes ->
                                if (bytes.size > MAX_IMAGE_SIZE_BYTES) {
                                    val sizeMb = bytes.size / (1024 * 1024.0)
                                    errorMessageOverride = "⚠️ Görsel boyutu 1 MB sınırını aşıyor (${(sizeMb * 100).toInt() / 100.0} MB). Veritabanını korumak için lütfen 1 MB'tan küçük bir görsel seçin."
                                } else {
                                    errorMessageOverride = null
                                    coverFileName = fileName
                                    coverBytes = bytes
                                }
                            },
                            onGalleryImageSelected = { fileName, bytes ->
                                if (bytes.size > MAX_IMAGE_SIZE_BYTES) {
                                    val sizeMb = bytes.size / (1024 * 1024.0)
                                    errorMessageOverride = "⚠️ Görsel boyutu 1 MB sınırını aşıyor (${(sizeMb * 100).toInt() / 100.0} MB). Veritabanını korumak için lütfen 1 MB'tan küçük bir görsel seçin."
                                } else {
                                    errorMessageOverride = null
                                    galleryItems = (galleryItems + (fileName to bytes)).take(10)
                                }
                            },
                            onRemoveGalleryImage = { index ->
                                galleryItems = galleryItems.filterIndexed { i, _ -> i != index }
                            }
                        )
                        TourFormFieldsSection(
                            title = title,
                            onTitleChange = { title = it },
                            code = code,
                            onCodeChange = { code = it },
                            selectedCategory = selectedCategory,
                            onCategorySelect = { selectedCategory = it },
                            country = country,
                            onCountryChange = { country = it },
                            city = city,
                            onCityChange = { city = it },
                            durationDaysText = durationDaysText,
                            onDurationChange = { durationDaysText = it },
                            basePriceText = basePriceText,
                            onBasePriceChange = { basePriceText = it },
                            childPrice06Text = childPrice06Text,
                            onChildPrice06Change = { childPrice06Text = it },
                            childPrice712Text = childPrice712Text,
                            onChildPrice712Change = { childPrice712Text = it },
                            adultCostPriceText = adultCostPriceText,
                            onAdultCostPriceChange = { adultCostPriceText = it },
                            childCostPrice06Text = childCostPrice06Text,
                            onChildCostPrice06Change = { childCostPrice06Text = it },
                            childCostPrice712Text = childCostPrice712Text,
                            onChildCostPrice712Change = { childCostPrice712Text = it },
                            capacityText = capacityText,
                            onCapacityChange = { capacityText = it },
                            minParticipantsText = minParticipantsText,
                            onMinChange = { minParticipantsText = it },
                            maxParticipantsText = maxParticipantsText,
                            onMaxChange = { maxParticipantsText = it },
                            description = description,
                            onDescriptionChange = { description = it },
                            cancellationPolicy = cancellationPolicy,
                            onCancellationChange = { cancellationPolicy = it },
                            insuranceDetails = insuranceDetails,
                            onInsuranceChange = { insuranceDetails = it },
                            includedServices = includedServices,
                            onIncludedServicesChange = { includedServices = it },
                            excludedServices = excludedServices,
                            onExcludedServicesChange = { excludedServices = it }
                        )

                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                        ItinerarySection(
                            itineraries = itinerariesDrafts,
                            onAddItinerary = { viewModel.addItineraryDraft() },
                            onUpdateItinerary = { index, draft -> viewModel.updateItineraryDraft(index, draft) },
                            onRemoveItinerary = { index -> viewModel.removeItineraryDraft(index) }
                        )
                    } else {
                        // EXPANDED LAYOUT (Masaüstü/Web: Solda Form, Sağda Medya Yükleme)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                TourFormFieldsSection(
                                    title = title,
                                    onTitleChange = { title = it },
                                    code = code,
                                    onCodeChange = { code = it },
                                    selectedCategory = selectedCategory,
                                    onCategorySelect = { selectedCategory = it },
                                    country = country,
                                    onCountryChange = { country = it },
                                    city = city,
                                    onCityChange = { city = it },
                                    durationDaysText = durationDaysText,
                                    onDurationChange = { durationDaysText = it },
                                    basePriceText = basePriceText,
                                    onBasePriceChange = { basePriceText = it },
                                    childPrice06Text = childPrice06Text,
                                    onChildPrice06Change = { childPrice06Text = it },
                                    childPrice712Text = childPrice712Text,
                                    onChildPrice712Change = { childPrice712Text = it },
                                    adultCostPriceText = adultCostPriceText,
                                    onAdultCostPriceChange = { adultCostPriceText = it },
                                    childCostPrice06Text = childCostPrice06Text,
                                    onChildCostPrice06Change = { childCostPrice06Text = it },
                                    childCostPrice712Text = childCostPrice712Text,
                                    onChildCostPrice712Change = { childCostPrice712Text = it },
                                    capacityText = capacityText,
                                    onCapacityChange = { capacityText = it },
                                    minParticipantsText = minParticipantsText,
                                    onMinChange = { minParticipantsText = it },
                                    maxParticipantsText = maxParticipantsText,
                                    onMaxChange = { maxParticipantsText = it },
                                    description = description,
                                    onDescriptionChange = { description = it },
                                    cancellationPolicy = cancellationPolicy,
                                    onCancellationChange = { cancellationPolicy = it },
                                    insuranceDetails = insuranceDetails,
                                    onInsuranceChange = { insuranceDetails = it },
                                    includedServices = includedServices,
                                    onIncludedServicesChange = { includedServices = it },
                                    excludedServices = excludedServices,
                                    onExcludedServicesChange = { excludedServices = it }
                                )

                                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                ItinerarySection(
                                    itineraries = itinerariesDrafts,
                                    onAddItinerary = { viewModel.addItineraryDraft() },
                                    onUpdateItinerary = { index, draft -> viewModel.updateItineraryDraft(index, draft) },
                                    onRemoveItinerary = { index -> viewModel.removeItineraryDraft(index) }
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                MediaUploadSection(
                                    coverFileName = coverFileName,
                                    coverBytes = coverBytes,
                                    existingCoverUrl = loadedTour?.coverImageUrl,
                                    galleryItems = galleryItems,
                                    onCoverSelected = { fileName, bytes ->
                                        if (bytes.size > MAX_IMAGE_SIZE_BYTES) {
                                            val sizeMb = bytes.size / (1024 * 1024.0)
                                            errorMessageOverride = "⚠️ Görsel boyutu 1 MB sınırını aşıyor (${(sizeMb * 100).toInt() / 100.0} MB). Veritabanını korumak için lütfen 1 MB'tan küçük bir görsel seçin."
                                        } else {
                                            errorMessageOverride = null
                                            coverFileName = fileName
                                            coverBytes = bytes
                                        }
                                    },
                                    onGalleryImageSelected = { fileName, bytes ->
                                        if (bytes.size > MAX_IMAGE_SIZE_BYTES) {
                                            val sizeMb = bytes.size / (1024 * 1024.0)
                                            errorMessageOverride = "⚠️ Görsel boyutu 1 MB sınırını aşıyor (${(sizeMb * 100).toInt() / 100.0} MB). Veritabanını korumak için lütfen 1 MB'tan küçük bir görsel seçin."
                                        } else {
                                            errorMessageOverride = null
                                            galleryItems = (galleryItems + (fileName to bytes)).take(10)
                                        }
                                    },
                                    onRemoveGalleryImage = { index ->
                                        galleryItems = galleryItems.filterIndexed { i, _ -> i != index }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                    // Form Aksiyon Butonu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TourOSButton(
                            text = if (tourId == null) "Turu Kaydet ✓" else "Turu Güncelle ✓",
                            onClick = {
                                viewModel.saveTour(
                                    id = tourId ?: "",
                                    code = code,
                                    title = title,
                                    category = selectedCategory,
                                    country = country,
                                    city = city,
                                    durationDays = durationDaysText.toIntOrNull() ?: 1,
                                    basePrice = basePriceText.toDoubleOrNull() ?: 0.0,
                                    childPrice06 = childPrice06Text.toDoubleOrNull() ?: 0.0,
                                    childPrice712 = childPrice712Text.toDoubleOrNull() ?: 0.0,
                                    adultCostPrice = adultCostPriceText.toDoubleOrNull() ?: 0.0,
                                    childCostPrice06 = childCostPrice06Text.toDoubleOrNull() ?: 0.0,
                                    childCostPrice712 = childCostPrice712Text.toDoubleOrNull() ?: 0.0,
                                    capacity = capacityText.toIntOrNull() ?: 20,
                                    minParticipants = minParticipantsText.toIntOrNull() ?: 1,
                                    maxParticipants = maxParticipantsText.toIntOrNull() ?: 30,
                                    description = description,
                                    cancellationPolicy = cancellationPolicy,
                                    insuranceDetails = insuranceDetails,
                                    includedServices = includedServices,
                                    excludedServices = excludedServices,
                                    coverBytes = coverBytes,
                                    coverFileName = coverFileName,
                                    existingCoverImageUrl = loadedTour?.coverImageUrl
                                )
                            },
                            variant = TourOSButtonVariant.PRIMARY,
                            enabled = title.isNotBlank() && code.isNotBlank(),
                            isLoading = uiState is TourFormUiState.Loading
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TourFormFieldsSection(
    title: String, onTitleChange: (String) -> Unit,
    code: String, onCodeChange: (String) -> Unit,
    selectedCategory: TourCategory, onCategorySelect: (TourCategory) -> Unit,
    country: String, onCountryChange: (String) -> Unit,
    city: String, onCityChange: (String) -> Unit,
    durationDaysText: String, onDurationChange: (String) -> Unit,
    basePriceText: String, onBasePriceChange: (String) -> Unit,
    childPrice06Text: String, onChildPrice06Change: (String) -> Unit,
    childPrice712Text: String, onChildPrice712Change: (String) -> Unit,
    adultCostPriceText: String, onAdultCostPriceChange: (String) -> Unit,
    childCostPrice06Text: String, onChildCostPrice06Change: (String) -> Unit,
    childCostPrice712Text: String, onChildCostPrice712Change: (String) -> Unit,
    capacityText: String, onCapacityChange: (String) -> Unit,
    minParticipantsText: String, onMinChange: (String) -> Unit,
    maxParticipantsText: String, onMaxChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    cancellationPolicy: String, onCancellationChange: (String) -> Unit,
    insuranceDetails: String, onInsuranceChange: (String) -> Unit,
    includedServices: String, onIncludedServicesChange: (String) -> Unit,
    excludedServices: String, onExcludedServicesChange: (String) -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.xLarge
    ) {
        // 1. Temel Bilgiler
        Text(text = "1. Temel Bilgiler", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        Row(modifier = Modifier.fillMaxWidth()) {
            TourOSTextField(
                value = title,
                onValueChange = onTitleChange,
                label = "Tur Adı *",
                placeholder = "Örn: Kapadokya Balon & Kültür Turu",
                modifier = Modifier.weight(2f)
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSTextField(
                value = code,
                onValueChange = onCodeChange,
                label = "Tur Kodu *",
                placeholder = "KPD-2026",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        // Tur Kategorisi Seçimi (Tek Primary Tonlu Chip Grubu)
        Text(text = "Tur Kategorisi *", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
        Spacer(modifier = Modifier.height(TourOSSpacing.small))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            TourCategory.entries.forEach { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(cat) },
                    label = { Text(cat.displayName, style = TourOSTypography.BodyMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TourOSColors.PrimaryContainer,
                        selectedLabelColor = TourOSColors.Primary,
                        containerColor = TourOSColors.Surface,
                        labelColor = TourOSColors.TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))
        HorizontalDivider(color = TourOSColors.Divider)
        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        // 2. Lokasyon & Süre
        Text(text = "2. Lokasyon & Süre", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        Row(modifier = Modifier.fillMaxWidth()) {
            TourOSTextField(
                value = country,
                onValueChange = onCountryChange,
                label = "Ülke *",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSTextField(
                value = city,
                onValueChange = onCityChange,
                label = "Şehir *",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSTextField(
                value = durationDaysText,
                onValueChange = onDurationChange,
                label = "Süre (Gün)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))
        HorizontalDivider(color = TourOSColors.Divider)
        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        // 3. Kapasite Bilgileri
        Text(text = "3. Kapasite & Kontenjan", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        Row(modifier = Modifier.fillMaxWidth()) {
            TourOSTextField(
                value = capacityText,
                onValueChange = onCapacityChange,
                label = "Toplam Kapasite",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSTextField(
                value = minParticipantsText,
                onValueChange = onMinChange,
                label = "Min Kişi",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSTextField(
                value = maxParticipantsText,
                onValueChange = onMaxChange,
                label = "Max Kişi",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))
        HorizontalDivider(color = TourOSColors.Divider)
        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        // 4. Fiyatlandırma, Pax Maliyetleri & Karlılık
        Text(text = "4. Fiyatlandırma, Pax Maliyetleri & Karlılık Hesaplama", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
        Spacer(modifier = Modifier.height(TourOSSpacing.small))
        Text(text = "🏷️ Satış Fiyatları", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(TourOSSpacing.small))

        Row(modifier = Modifier.fillMaxWidth()) {
            TourOSTextField(
                value = basePriceText,
                onValueChange = onBasePriceChange,
                label = "Yetişkin Satış (₺) *",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSTextField(
                value = childPrice06Text,
                onValueChange = onChildPrice06Change,
                label = "Çocuk (0-6 Yaş) Satış (₺)",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSTextField(
                value = childPrice712Text,
                onValueChange = onChildPrice712Change,
                label = "Çocuk (7-12 Yaş) Satış (₺)",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
        Text(text = "💰 Pax Maliyetleri (Kişi Başı Maliyet)", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(TourOSSpacing.small))

        Row(modifier = Modifier.fillMaxWidth()) {
            TourOSTextField(
                value = adultCostPriceText,
                onValueChange = onAdultCostPriceChange,
                label = "Yetişkin Maliyeti (₺)",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSTextField(
                value = childCostPrice06Text,
                onValueChange = onChildCostPrice06Change,
                label = "Çocuk (0-6) Maliyeti (₺)",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSTextField(
                value = childCostPrice712Text,
                onValueChange = onChildCostPrice712Change,
                label = "Çocuk (7-12) Maliyeti (₺)",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        // Anlık Karlılık Hesaplama Göstergesi (Özet Kart)
        val adultSale = basePriceText.toDoubleOrNull() ?: 0.0
        val adultCost = adultCostPriceText.toDoubleOrNull() ?: 0.0
        val adultProfit = adultSale - adultCost
        val adultMargin = if (adultSale > 0) (adultProfit / adultSale) * 100 else 0.0

        val child06Sale = childPrice06Text.toDoubleOrNull() ?: 0.0
        val child06Cost = childCostPrice06Text.toDoubleOrNull() ?: 0.0
        val child06Profit = child06Sale - child06Cost
        val child06Margin = if (child06Sale > 0) (child06Profit / child06Sale) * 100 else 0.0

        val child712Sale = childPrice712Text.toDoubleOrNull() ?: 0.0
        val child712Cost = childCostPrice712Text.toDoubleOrNull() ?: 0.0
        val child712Profit = child712Sale - child712Cost
        val child712Margin = if (child712Sale > 0) (child712Profit / child712Sale) * 100 else 0.0

        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
        TourOSCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.4f),
            borderColor = TourOSColors.Primary.copy(alpha = 0.3f),
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                Text(text = "📈 Tahmini Pax Başı Karlılık Analizi", style = TourOSTypography.Label.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Yetişkin Karı: ${adultProfit.toLong()} ₺ (%${adultMargin.toInt()})",
                        style = TourOSTypography.Caption,
                        fontWeight = FontWeight.Bold,
                        color = if (adultProfit >= 0) TourOSColors.Primary else TourOSColors.Error
                    )
                    Text(
                        text = "0-6 Çocuk Karı: ${child06Profit.toLong()} ₺ (%${child06Margin.toInt()})",
                        style = TourOSTypography.Caption,
                        fontWeight = FontWeight.Bold,
                        color = if (child06Profit >= 0) TourOSColors.Primary else TourOSColors.Error
                    )
                    Text(
                        text = "7-12 Çocuk Karı: ${child712Profit.toLong()} ₺ (%${child712Margin.toInt()})",
                        style = TourOSTypography.Caption,
                        fontWeight = FontWeight.Bold,
                        color = if (child712Profit >= 0) TourOSColors.Primary else TourOSColors.Error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))
        HorizontalDivider(color = TourOSColors.Divider)
        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        // 6. Detaylar & Koşullar
        Text(text = "6. Tur Açıklaması & Şartlar", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        TourOSTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Tur Programı Açıklaması",
            placeholder = "Tur rotası ve dahil olan hizmetler...",
            singleLine = false,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        TourOSTextField(
            value = includedServices,
            onValueChange = onIncludedServicesChange,
            label = "✅ Fiyata Dahil Olan Hizmetler",
            placeholder = "Her satıra 1 hizmet yazın.\nÖrn:\nLüks Otobüs İle Ulaşım\n4 Yıldızlı Otel Konaklama\nProfesyonel Rehberlik Hizmeti\nAçık Büfe Kahvaltı",
            singleLine = false,
            minLines = 3,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        TourOSTextField(
            value = excludedServices,
            onValueChange = onExcludedServicesChange,
            label = "❌ Fiyata Dahil Olmayan Hizmetler",
            placeholder = "Her satıra 1 hizmet yazın.\nÖrn:\nKişisel Harcamalar\nMüze Ören Yeri Giriş Ücretleri\nÖğle Yemekleri",
            singleLine = false,
            minLines = 3,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        TourOSTextField(
            value = cancellationPolicy,
            onValueChange = onCancellationChange,
            label = "İptal ve İade Koşulları",
            placeholder = "7 gün kalaya kadar ücretsiz iptal...",
            singleLine = false,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MediaUploadSection(
    coverFileName: String?,
    coverBytes: ByteArray?,
    existingCoverUrl: String? = null,
    galleryItems: List<Pair<String, ByteArray>>,
    onCoverSelected: (String, ByteArray) -> Unit,
    onGalleryImageSelected: (String, ByteArray) -> Unit,
    onRemoveGalleryImage: (Int) -> Unit
) {
    val launchCoverPicker = rememberFilePickerLauncher(mimeType = "image/*", onFileSelected = onCoverSelected)
    val launchGalleryPicker = rememberFilePickerLauncher(mimeType = "image/*", onFileSelected = onGalleryImageSelected)

    val coverBitmap = remember(coverBytes) {
        coverBytes?.let {
            runCatching { it.decodeToImageBitmap() }.getOrNull()
        }
    }

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.xLarge
    ) {
        Text(text = "Tur Kapak Görseli & Galeri", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
        Text(text = "Voucher ve web katalogunda gösterilecek tur fotoğrafları.", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))

        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        // Kapak Resmi Yükleme Dropzone / Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                .background(if (coverFileName != null || !existingCoverUrl.isNullOrBlank()) TourOSColors.SuccessContainer else TourOSColors.PrimaryContainer)
                .border(
                    width = TourOSSpacing.borderWidth,
                    color = if (coverFileName != null || !existingCoverUrl.isNullOrBlank()) TourOSColors.Success else TourOSColors.Primary,
                    shape = RoundedCornerShape(TourOSSpacing.cornerRadius)
                )
                .clickable { launchCoverPicker() },
            contentAlignment = Alignment.Center
        ) {
            if (coverBitmap != null) {
                Image(
                    bitmap = coverBitmap,
                    contentDescription = "Kapak Görseli",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅ $coverFileName",
                            style = TourOSTypography.Caption.copy(color = Color.White),
                            maxLines = 1
                        )
                        Text(
                            text = "Değiştir ✏️",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.PrimaryContainer)
                        )
                    }
                }
            } else if (!existingCoverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = existingCoverUrl,
                    contentDescription = "Kapak Görseli",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅ Mevcut Kapak Görseli",
                            style = TourOSTypography.Caption.copy(color = Color.White),
                            maxLines = 1
                        )
                        Text(
                            text = "Değiştir ✏️",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.PrimaryContainer)
                        )
                    }
                }
            } else if (coverFileName != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "✅ Kapak Seçildi: $coverFileName", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Success))
                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                    Text(text = "Değiştirmek için tekrar tıklayın (Maks 1MB)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "📸 Kapak Resmi Yükleyin", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                    Text(text = "Yüksek çözünürlüklü JPG/PNG (Maks 1MB)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                }
            }
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        Text(text = "Ek Fotoğraf Galerisi (Maks. 10 Görsel)", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
        Spacer(modifier = Modifier.height(TourOSSpacing.small))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            galleryItems.forEachIndexed { index, (name, bytes) ->
                val galleryBitmap = remember(bytes) {
                    runCatching { bytes.decodeToImageBitmap() }.getOrNull()
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.PrimaryContainer)
                        .border(TourOSSpacing.borderWidth, TourOSColors.Primary, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)),
                    contentAlignment = Alignment.Center
                ) {
                    if (galleryBitmap != null) {
                        Image(
                            bitmap = galleryBitmap,
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = "🖼️", style = TourOSTypography.TitleLarge)
                    }

                    // Görsel Silme (X) Butonu
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                            .background(TourOSColors.Error, shape = RoundedCornerShape(bottomStart = 8.dp))
                            .clickable { onRemoveGalleryImage(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", style = TourOSTypography.Caption.copy(color = Color.White))
                    }
                }
            }

            if (galleryItems.size < 10) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.Surface)
                        .border(TourOSSpacing.borderWidth, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .clickable { launchGalleryPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextSecondary))
                }
            }
        }
    }
}

@Composable
private fun ItinerarySection(
    itineraries: List<com.mgacreative.touros.ui.viewmodel.ItineraryDraft>,
    onAddItinerary: () -> Unit,
    onUpdateItinerary: (Int, com.mgacreative.touros.ui.viewmodel.ItineraryDraft) -> Unit,
    onRemoveItinerary: (Int) -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.xLarge
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "📍 Gün Gün Tur Programı (Itinerary & Rota)", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Text(text = "Turun gün bazlı rotası, gezilecek yerler ve program detayları.", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
            }
            TourOSButton(
                text = "➕ Yeni Gün Ekle",
                onClick = onAddItinerary,
                variant = TourOSButtonVariant.SECONDARY
            )
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        if (itineraries.isEmpty()) {
            Text(
                text = "Henüz gün programı eklenmedi. 'Yeni Gün Ekle' butonuna basarak tur rotasını oluşturabilirsiniz.",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )
        } else {
            itineraries.forEachIndexed { index, itin ->
                TourOSCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = TourOSSpacing.small),
                    backgroundColor = TourOSColors.Surface,
                    borderColor = TourOSColors.Border,
                    contentPadding = TourOSSpacing.medium
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${itin.dayNumber}. GÜN PROGRAMI",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                            )
                            TourOSButton(
                                text = "Sil 🗑️",
                                onClick = { onRemoveItinerary(index) },
                                variant = TourOSButtonVariant.TERTIARY
                            )
                        }

                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            TourOSTextField(
                                value = itin.title,
                                onValueChange = { onUpdateItinerary(index, itin.copy(title = it)) },
                                label = "Gün Başlığı *",
                                placeholder = "Örn: Kapadokya Karşılama ve Göreme Gezisi",
                                modifier = Modifier.weight(1.5f)
                            )
                            TourOSTextField(
                                value = itin.location,
                                onValueChange = { onUpdateItinerary(index, itin.copy(location = it)) },
                                label = "Rota / Lokasyon",
                                placeholder = "Örn: Nevşehir / Göreme",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                        TourOSTextField(
                            value = itin.description,
                            onValueChange = { onUpdateItinerary(index, itin.copy(description = it)) },
                            label = "Günün Detaylı Açıklaması",
                            placeholder = "Sabah transfer, Göreme Açık Hava Müzesi ve Ürgüp Paşabağ vadisi yürüyüşü...",
                            singleLine = false,
                            minLines = 4,
                            maxLines = 10,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

