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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TourCategory.CULTURAL) }
    var country by remember { mutableStateOf("Türkiye") }
    var city by remember { mutableStateOf("İstanbul") }
    var durationDaysText by remember { mutableStateOf("1") }
    var capacityText by remember { mutableStateOf("20") }
    var minParticipantsText by remember { mutableStateOf("1") }
    var maxParticipantsText by remember { mutableStateOf("30") }
    var description by remember { mutableStateOf("") }
    var cancellationPolicy by remember { mutableStateOf("") }
    var insuranceDetails by remember { mutableStateOf("") }

    LaunchedEffect(tourId) {
        viewModel.loadTourForEdit(tourId)
    }

    LaunchedEffect(loadedTour) {
        loadedTour?.let { tour ->
            title = tour.title
            code = tour.code
            selectedCategory = tour.category
            country = tour.country
            city = tour.city
            durationDaysText = tour.durationDays.toString()
            capacityText = tour.capacity.toString()
            minParticipantsText = tour.minParticipants.toString()
            maxParticipantsText = tour.maxParticipants.toString()
            description = tour.description ?: ""
            cancellationPolicy = tour.cancellationPolicy ?: ""
            insuranceDetails = tour.insuranceDetails ?: ""
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
                    // State Banners (Error / Success)
                    when (val state = uiState) {
                        is TourFormUiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                    .background(TourOSColors.ErrorContainer)
                                    .padding(TourOSSpacing.medium)
                            ) {
                                Text(
                                    text = state.message,
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error)
                                )
                            }
                        }
                        is TourFormUiState.Success -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                    .background(TourOSColors.SuccessContainer)
                                    .padding(TourOSSpacing.medium)
                            ) {
                                Text(
                                    text = state.message,
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Success)
                                )
                            }
                        }
                        else -> {}
                    }

                    if (isCompact) {
                        // COMPACT LAYOUT (Mobil: Medya alanı en üstte, sonra form)
                        MediaUploadSection()
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
                            onInsuranceChange = { insuranceDetails = it }
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
                                    onInsuranceChange = { insuranceDetails = it }
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                MediaUploadSection()
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
                                    capacity = capacityText.toIntOrNull() ?: 20,
                                    minParticipants = minParticipantsText.toIntOrNull() ?: 1,
                                    maxParticipants = maxParticipantsText.toIntOrNull() ?: 30,
                                    description = description,
                                    cancellationPolicy = cancellationPolicy,
                                    insuranceDetails = insuranceDetails
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
    capacityText: String, onCapacityChange: (String) -> Unit,
    minParticipantsText: String, onMinChange: (String) -> Unit,
    maxParticipantsText: String, onMaxChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    cancellationPolicy: String, onCancellationChange: (String) -> Unit,
    insuranceDetails: String, onInsuranceChange: (String) -> Unit
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

        // 4. Detaylar & Koşullar
        Text(text = "4. Tur Açıklaması & Şartlar", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
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
            value = cancellationPolicy,
            onValueChange = onCancellationChange,
            label = "İptal ve İade Koşulları",
            placeholder = "7 gün kalaya kadar ücretsi iptal...",
            singleLine = false,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MediaUploadSection() {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.xLarge
    ) {
        Text(text = "Tur Kapak Görseli & Galeri", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
        Text(text = "Voucher ve web katalogunda gösterilecek tur fotoğrafları.", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))

        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        // Kapak Resmi Yükleme Dropzone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                .background(TourOSColors.PrimaryContainer)
                .border(
                    width = TourOSSpacing.borderWidth,
                    color = TourOSColors.Primary,
                    shape = RoundedCornerShape(TourOSSpacing.cornerRadius)
                )
                .clickable { /* Upload Cover */ },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "📸 Kapak Resmi Yükleyin", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                Text(text = "Yüksek çözünürlüklü JPG/PNG (Maks 5MB)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
            }
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        Text(text = "Ek Fotoğraf Galerisi (Maks. 10 Görsel)", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
        Spacer(modifier = Modifier.height(TourOSSpacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.Surface)
                        .border(TourOSSpacing.borderWidth, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextDisabled))
                }
            }
        }
    }
}
