package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.mgacreative.touros.domain.model.DocumentItem
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.DocumentManagementViewModel

private data class DocCategoryFilter(val key: String, val label: String, val icon: String)

private val categories = listOf(
    DocCategoryFilter("all", "Tüm Belgeler", "📂"),
    DocCategoryFilter("contract", "Sözleşmeler", "📝"),
    DocCategoryFilter("invoice", "Faturalar", "🧾"),
    DocCategoryFilter("passport", "Pasaport & Kimlik", "🛂"),
    DocCategoryFilter("insurance", "Sigorta Poliçeleri", "🛡️"),
    DocCategoryFilter("voucher", "Voucher", "🎟️")
)

/**
 * Belge Yönetimi & Storage Ekranı — TourOS 0.3
 *
 * Üstte Sabit Sürükle-Bırak Yükleme Alanı (Drag & Drop Zone).
 * Belge tipine göre sekmeli/filtreli dosya listesi.
 * Her belge sol tarafında küçük önizleme ikonu/kutusu ile gösterilir.
 */
@Composable
fun DocumentManagementScreen(
    viewModel: DocumentManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var isDraggingFile by remember { mutableStateOf(false) }
    var selectedUploadCategory by remember { mutableStateOf("contract") }
    var uploadFileNameInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Belge Yönetimi & Arşiv",
                subtitle = "Pasaport, sözleşme, fatura ve poliçe saklama",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // ── 1. ÜSTTE SABİT SÜRÜKLE-BIRAK YÜKLEME ALANI (DRAG & DROP ZONE) ──
            item {
                DragDropUploadZoneCard(
                    isDragging = isDraggingFile,
                    fileNameInput = uploadFileNameInput,
                    onFileNameChange = { uploadFileNameInput = it },
                    selectedCategory = selectedUploadCategory,
                    onCategoryChange = { selectedUploadCategory = it },
                    onUploadClick = {
                        if (uploadFileNameInput.isNotBlank()) {
                            viewModel.uploadSampleDocument(uploadFileNameInput, selectedUploadCategory)
                            uploadFileNameInput = ""
                        }
                    }
                )
            }

            // Bildirim Mesajı
            if (state.notificationMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SuccessContainer)
                            .padding(TourOSSpacing.medium)
                    ) {
                        Text(
                            state.notificationMessage!!,
                            style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                        )
                    }
                }
            }

            // ── 2. BELGE TİPİNE GÖRE SEKMELİ / FİLTRELİ ÇUBUK ─────────────────
            item {
                Text(
                    "📂 Belge Arşivi & Kategoriler",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = state.selectedCategory == cat.key,
                            onClick = { viewModel.loadDocuments(cat.key) },
                            label = { Text("${cat.icon} ${cat.label}", style = TourOSTypography.Caption) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TourOSColors.Primary,
                                selectedLabelColor = TourOSColors.OnPrimary
                            )
                        )
                    }
                }
            }

            // ── 3. KÜÇÜK ÖNİZLEME İKONLU DOSYA LİSTESİ ────────────────────────
            item {
                Text(
                    "📋 Yüklü Belgeler (${state.documents.size})",
                    style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
                )
            }

            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                }
            } else if (state.documents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Bu kategoride kayıtlı belge bulunamadı.",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            } else {
                items(state.documents) { doc ->
                    DocumentCard(document = doc)
                }
            }
        }
    }
}

// ─── Üstte Sabit Sürükle-Bırak Yükleme Kartı (Drag & Drop Zone) ─────────────

@Composable
private fun DragDropUploadZoneCard(
    isDragging: Boolean,
    fileNameInput: String,
    onFileNameChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onUploadClick: () -> Unit
) {
    val borderColor = if (isDragging) TourOSColors.Secondary else TourOSColors.Primary
    val bgColor = if (isDragging) TourOSColors.SecondaryContainer else TourOSColors.PrimaryContainer

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = bgColor,
        contentPadding = TourOSSpacing.large
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // Sürükle-Bırak Görsel Alanı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.Surface)
                    .border(
                        width = 1.5.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                    )
                    .padding(TourOSSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)
                ) {
                    Text(
                        "📥",
                        style = TourOSTypography.DisplaySmall
                    )
                    Text(
                        "Dosyaları Buraya Sürükleyip Bırakın",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "veya cihazınızdan dosya seçin (PDF, JPG, PNG, DOCX — Max 25 MB)",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Dosya Adı Girişi & Kategori Seçimi Formu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TourOSTextField(
                    value = fileNameInput,
                    onValueChange = onFileNameChange,
                    label = "Belge Adı / Başlığı",
                    placeholder = "Örn: Kapadokya Tur Sözleşmesi",
                    modifier = Modifier.weight(1.5f)
                )

                TourOSButton(
                    text = "📤 Yükle",
                    onClick = onUploadClick,
                    enabled = fileNameInput.isNotBlank(),
                    variant = TourOSButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─── Küçük Önizleme İkonlu Belge Kartı ───────────────────────────────────────

@Composable
private fun DocumentCard(document: DocumentItem) {
    val (typeIcon, typeLabel, previewBg, previewFg) = when (document.documentType) {
        "passport" -> Quadruple("🛂", "PASAPORT", TourOSColors.PrimaryContainer, TourOSColors.Primary)
        "visa" -> Quadruple("✈️", "VİZE", TourOSColors.SecondaryContainer, TourOSColors.Secondary)
        "contract" -> Quadruple("📝", "SÖZLEŞME", TourOSColors.PrimaryContainer, TourOSColors.Primary)
        "voucher" -> Quadruple("🎟️", "VOUCHER", TourOSColors.SuccessContainer, TourOSColors.Success)
        "photo" -> Quadruple("🖼️", "GÖRSEL", TourOSColors.SecondaryContainer, TourOSColors.Secondary)
        else -> Quadruple("📄", "PDF BELGE", TourOSColors.PrimaryContainer, TourOSColors.Primary)
    }

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.medium) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // KÜÇÜK ÖNİZLEME KUTUSU / İKONU (THUMBNAIL PREVIEW BOX)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(previewBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    typeIcon,
                    style = TourOSTypography.TitleLarge
                )
            }

            // Belge Bilgileri
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        document.title,
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                        maxLines = 1
                    )
                    TourOSStatusBadge(
                        text = typeLabel,
                        backgroundColor = previewBg,
                        textColor = previewFg
                    )
                }

                Text(
                    "📁 Yol: ${document.filePath}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sizeKb = document.fileSize / 1024
                    Text(
                        "Boyut: ${sizeKb} KB  ·  Tarih: ${document.createdAt}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                        TourOSButton(
                            text = "👁️ Görüntüle",
                            onClick = {},
                            variant = TourOSButtonVariant.TERTIARY
                        )
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
