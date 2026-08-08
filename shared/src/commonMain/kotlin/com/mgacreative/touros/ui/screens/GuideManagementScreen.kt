package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.GuideFormState
import com.mgacreative.touros.ui.viewmodel.GuideManagementViewModel
import com.mgacreative.touros.ui.viewmodel.GuideUiState

private data class LanguageFilterItem(val key: String?, val label: String)

private val defaultLanguages = listOf(
    LanguageFilterItem(null, "Tüm Diller"),
    LanguageFilterItem("İngilizce", "İngilizce"),
    LanguageFilterItem("Almanca", "Almanca"),
    LanguageFilterItem("Fransızca", "Fransızca"),
    LanguageFilterItem("İspanyolca", "İspanyolca"),
    LanguageFilterItem("İtalyanca", "İtalyanca"),
    LanguageFilterItem("Rusça", "Rusça")
)

/**
 * Rehber Yönetimi & Portföyü — TourOS 0.3
 *
 * Expanded: Tablo Düzeni (Kokart, Rehber, Diller Chip Grubu, Puan Yıldız İkonu, Tur Sayısı, İşlem)
 * Compact: Kart Düzeni (Diller chip grubu, ⭐ Puan rozeti)
 */
@Composable
fun GuideManagementScreen(
    viewModel: GuideManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Rehber Portföyü",
                subtitle = "Kokartlı rehber kadrosu, diller ve puanlama yönetimi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is GuideUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is GuideUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is GuideUiState.Success -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val isExpanded = maxWidth >= 768.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // ── Özet KPI ──────────────────────────────────────────
                        item {
                            GuideKpiRow(guides = state.guides)
                        }

                        // ── Arama & Filtreler ──────────────────────────────────
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TourOSTextField(
                                        value = state.searchQuery,
                                        onValueChange = { viewModel.loadGuides(it, state.selectedLanguageFilter) },
                                        label = "Arama",
                                        placeholder = "Rehber adı veya kokart no ara...",
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (!formState.isFormOpen) {
                                        Spacer(Modifier.width(TourOSSpacing.small))
                                        TourOSButton(
                                            text = "+ Rehber Ekle",
                                            onClick = { viewModel.openNewForm() },
                                            variant = TourOSButtonVariant.PRIMARY
                                        )
                                    }
                                }

                                // Dil Filtre Çipleri
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    items(defaultLanguages) { item ->
                                        FilterChip(
                                            selected = state.selectedLanguageFilter == item.key,
                                            onClick = { viewModel.loadGuides(state.searchQuery, item.key) },
                                            label = {
                                                Text(item.label, style = TourOSTypography.Caption)
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                                selectedLabelColor = TourOSColors.Primary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // ── Form ────────────────────────────────────────────────
                        item {
                            AnimatedVisibility(
                                visible = formState.isFormOpen,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                GuideForm(
                                    formState = formState,
                                    onFullNameChange = { viewModel.updateFullName(it) },
                                    onPhoneChange = { viewModel.updatePhone(it) },
                                    onEmailChange = { viewModel.updateEmail(it) },
                                    onLicenseNumberChange = { viewModel.updateLicenseNumber(it) },
                                    onLanguagesCsvChange = { viewModel.updateLanguagesCsv(it) },
                                    onSpecializationChange = { viewModel.updateSpecialization(it) },
                                    onRatingChange = { viewModel.updateRating(it) },
                                    onTotalToursChange = { viewModel.updateTotalTours(it) },
                                    onNotesChange = { viewModel.updateNotes(it) },
                                    onIsActiveChange = { viewModel.updateIsActive(it) },
                                    onSave = { viewModel.saveGuide() },
                                    onCancel = { viewModel.closeForm() }
                                )
                            }
                        }

                        // ── Liste / Tablo ───────────────────────────────────────
                        if (state.guides.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Kriterlere uygun rehber kaydı bulunamadı.\n+ Rehber Ekle butonu ile kadronuzu oluşturun.",
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else if (isExpanded) {
                            // Expanded: Tablo düzeni
                            item {
                                GuideTable(
                                    guides = state.guides,
                                    onEditClick = { viewModel.openEditForm(it) },
                                    onDeleteClick = { viewModel.deleteGuide(it.id) }
                                )
                            }
                        } else {
                            // Compact: Kart düzeni
                            items(state.guides) { guide ->
                                GuideCard(
                                    guide = guide,
                                    onEditClick = { viewModel.openEditForm(guide) },
                                    onDeleteClick = { viewModel.deleteGuide(guide.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── KPI Satırı ──────────────────────────────────────────────────────────────

@Composable
private fun GuideKpiRow(guides: List<Guide>) {
    val active = guides.count { it.isActive }
    val avgRating = if (guides.isNotEmpty()) guides.map { it.rating }.average() else 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        GuideKpi("Toplam Rehber", guides.size.toString(), TourOSColors.PrimaryContainer, TourOSColors.Primary, Modifier.weight(1f))
        GuideKpi("Aktif Kadro", active.toString(), TourOSColors.SuccessContainer, TourOSColors.Success, Modifier.weight(1f))
        GuideKpi("Ortalama Puan", "⭐ ${((avgRating * 10).toInt() / 10.0)}", TourOSColors.SecondaryContainer, TourOSColors.Secondary, Modifier.weight(1f))
    }
}

@Composable
private fun GuideKpi(label: String, value: String, bg: Color, text: Color, modifier: Modifier) {
    TourOSCard(modifier = modifier, backgroundColor = bg, contentPadding = TourOSSpacing.small) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = TourOSTypography.TitleLarge.copy(color = text))
            Text(label, style = TourOSTypography.Caption.copy(color = text.copy(alpha = 0.8f)), textAlign = TextAlign.Center)
        }
    }
}

// ─── Expanded: Tablo Düzeni ───────────────────────────────────────────────────

@Composable
private fun GuideTable(
    guides: List<Guide>,
    onEditClick: (Guide) -> Unit,
    onDeleteClick: (Guide) -> Unit
) {
    val headers = listOf("Kokart No", "Rehber Adı", "Uzmanlık", "Bildiği Diller", "Puan", "Tur Sayısı", "Durum", "İşlem")
    val weights = listOf(1.0f, 1.4f, 1.1f, 1.6f, 0.8f, 0.8f, 0.8f, 1.0f)

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TourOSColors.Primary)
                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small)
            ) {
                headers.forEachIndexed { i, h ->
                    Text(
                        h,
                        style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary),
                        modifier = Modifier.weight(weights[i])
                    )
                }
            }

            // Satırlar
            guides.forEachIndexed { idx, g ->
                val bg = if (idx % 2 == 0) TourOSColors.Background else TourOSColors.Surface

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kokart No
                    Box(
                        modifier = Modifier
                            .weight(weights[0])
                            .clip(RoundedCornerShape(4.dp))
                            .background(TourOSColors.PrimaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            g.licenseNumber ?: "Lisanssız",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                        )
                    }

                    // Rehber Adı
                    Column(modifier = Modifier.weight(weights[1])) {
                        Text(g.fullName, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                        Text(g.phone ?: "—", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    }

                    // Uzmanlık
                    Text(
                        g.specialization ?: "Genel Kültür",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                        modifier = Modifier.weight(weights[2])
                    )

                    // Bildiği Diller Chip Grubu
                    Row(
                        modifier = Modifier.weight(weights[3]),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!g.languages.isNullOrEmpty()) {
                            g.languages.take(3).forEach { lang ->
                                LanguageChip(lang)
                            }
                            if (g.languages.size > 3) {
                                Text("+${g.languages.size - 3}", style = TourOSTypography.Caption)
                            }
                        } else {
                            Text("—", style = TourOSTypography.Caption)
                        }
                    }

                    // Puan (Yıldız İkonlu)
                    Row(
                        modifier = Modifier.weight(weights[4]),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("⭐", style = TourOSTypography.Caption)
                        Text(
                            g.rating.toString(),
                            style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
                        )
                    }

                    // Tur Sayısı
                    Text(
                        "${g.totalToursCompleted} Tur",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                        modifier = Modifier.weight(weights[5])
                    )

                    // Durum Badgesi
                    Box(modifier = Modifier.weight(weights[6])) {
                        TourOSStatusBadge(
                            text = if (g.isActive) "Aktif" else "Pasif",
                            backgroundColor = if (g.isActive) TourOSColors.SuccessContainer else TourOSColors.Surface,
                            textColor = if (g.isActive) TourOSColors.Success else TourOSColors.TextSecondary
                        )
                    }

                    // İşlem Butonları
                    Row(
                        modifier = Modifier.weight(weights[7]),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TourOSButton("Düzenle", { onEditClick(g) }, variant = TourOSButtonVariant.TERTIARY)
                        TourOSButton("Sil", { onDeleteClick(g) }, variant = TourOSButtonVariant.DESTRUCTIVE)
                    }
                }

                if (idx < guides.size - 1) {
                    HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ─── Compact: Kart Düzeni ─────────────────────────────────────────────────────

@Composable
private fun GuideCard(
    guide: Guide,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    // Kokart Badgesi
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TourOSColors.PrimaryContainer)
                            .padding(horizontal = TourOSSpacing.small, vertical = 4.dp)
                    ) {
                        Text(
                            "Kokart: ${guide.licenseNumber ?: "Lisanssız"}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                        )
                    }

                    // ⭐ Puan Rozeti
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TourOSColors.SecondaryContainer)
                            .padding(horizontal = TourOSSpacing.small, vertical = 4.dp)
                    ) {
                        Text(
                            "⭐ ${guide.rating}",
                            style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
                        )
                    }
                }

                TourOSStatusBadge(
                    text = if (guide.isActive) "Aktif" else "Pasif",
                    backgroundColor = if (guide.isActive) TourOSColors.SuccessContainer else TourOSColors.Surface,
                    textColor = if (guide.isActive) TourOSColors.Success else TourOSColors.TextSecondary
                )
            }

            // İsim & Detaylar
            Text(
                guide.fullName,
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )

            Text(
                "🎯 Uzmanlık: ${guide.specialization ?: "Genel Kültür"}  ·  🚩 ${guide.totalToursCompleted} Tur  ·  📞 ${guide.phone ?: "—"}",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            // Diller Chip Grubu
            if (!guide.languages.isNullOrEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    Text("🗣️ Diller:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(guide.languages) { lang ->
                            LanguageChip(lang)
                        }
                    }
                }
            }

            if (!guide.notes.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.Surface)
                        .padding(TourOSSpacing.small)
                ) {
                    Text("📝 ${guide.notes}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Butonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TourOSButton("Sil", onDeleteClick, variant = TourOSButtonVariant.DESTRUCTIVE)
                Spacer(Modifier.width(TourOSSpacing.small))
                TourOSButton("Düzenle", onEditClick, variant = TourOSButtonVariant.TERTIARY)
            }
        }
    }
}

@Composable
private fun LanguageChip(language: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TourOSColors.PrimaryContainer.copy(alpha = 0.5f))
            .border(0.5.dp, TourOSColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            language,
            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
        )
    }
}

// ─── Form ─────────────────────────────────────────────────────────────────────

@Composable
private fun GuideForm(
    formState: GuideFormState,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onLicenseNumberChange: (String) -> Unit,
    onLanguagesCsvChange: (String) -> Unit,
    onSpecializationChange: (String) -> Unit,
    onRatingChange: (String) -> Unit,
    onTotalToursChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.SecondaryContainer,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (formState.isEditing) "✏️ Rehber Düzenle" else "➕ Yeni Rehber Kaydı",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )
                IconButton(onClick = onCancel) {
                    Text("✕", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextSecondary))
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Ad Soyad
            TourOSTextField(
                value = formState.fullName,
                onValueChange = onFullNameChange,
                label = "Rehber Adı Soyadı",
                placeholder = "Örn: Ahmet Yılmaz",
                modifier = Modifier.fillMaxWidth()
            )

            // Telefon + E-Posta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSTextField(
                    value = formState.phone,
                    onValueChange = onPhoneChange,
                    label = "Telefon",
                    placeholder = "+90 555...",
                    modifier = Modifier.weight(1f)
                )
                TourOSTextField(
                    value = formState.email,
                    onValueChange = onEmailChange,
                    label = "E-Posta",
                    placeholder = "rehber@touros.com",
                    modifier = Modifier.weight(1f)
                )
            }

            // Kokart No + Uzmanlık
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSTextField(
                    value = formState.licenseNumber,
                    onValueChange = onLicenseNumberChange,
                    label = "Kokart / Lisans No",
                    placeholder = "K-12345",
                    modifier = Modifier.weight(1f)
                )
                TourOSTextField(
                    value = formState.specialization,
                    onValueChange = onSpecializationChange,
                    label = "Uzmanlık Alanı",
                    placeholder = "Kapadokya / Ege",
                    modifier = Modifier.weight(1f)
                )
            }

            // Bildiği Diller
            TourOSTextField(
                value = formState.languagesCsv,
                onValueChange = onLanguagesCsvChange,
                label = "Bildiği Diller (Virgülle)",
                placeholder = "Türkçe, İngilizce, Almanca",
                modifier = Modifier.fillMaxWidth()
            )

            // Puan + Tur Sayısı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSTextField(
                    value = formState.rating,
                    onValueChange = onRatingChange,
                    label = "Puan (1-5)",
                    placeholder = "4.9",
                    modifier = Modifier.weight(1f)
                )
                TourOSTextField(
                    value = formState.totalToursCompleted,
                    onValueChange = onTotalToursChange,
                    label = "Tur Sayısı",
                    placeholder = "45",
                    modifier = Modifier.weight(1f)
                )
            }

            // Notlar
            TourOSTextField(
                value = formState.notes,
                onValueChange = onNotesChange,
                label = "Rehber Notları",
                placeholder = "Kokart geçerlilik tarihi, bölge detayları...",
                modifier = Modifier.fillMaxWidth()
            )

            // Aktif Switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = formState.isActive,
                    onCheckedChange = onIsActiveChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TourOSColors.Primary,
                        checkedTrackColor = TourOSColors.PrimaryContainer
                    )
                )
                Spacer(Modifier.width(TourOSSpacing.small))
                Text("Rehberi Aktif Kadroda Göster", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
            }

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSButton("İptal", onCancel, variant = TourOSButtonVariant.SECONDARY, modifier = Modifier.weight(1f))
                TourOSButton(
                    "💾 Rehber Kaydını Kaydet",
                    onSave,
                    variant = TourOSButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f),
                    enabled = formState.fullName.isNotBlank()
                )
            }
        }
    }
}
