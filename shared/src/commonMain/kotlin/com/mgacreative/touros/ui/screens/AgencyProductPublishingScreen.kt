package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.data.database.entity.AgencyPublishedTourEntity
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AgencyProductPublishingUiState
import com.mgacreative.touros.ui.viewmodel.AgencyProductPublishingViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Katalog & Ürün Yayınlama Yönetimi — Operatör Filtresi Destekli Canlı Sorgu Motoru
 */
@Composable
fun AgencyProductPublishingScreen(
    viewModel: AgencyProductPublishingViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToSearchWizard: () -> Unit = {}
) {
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showImportModal by remember { mutableStateOf(false) }
    var showClearConfirmModal by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedOperatorFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = AppLanguageManager.translate("Katalog & Ürün Yayınlama Yönetimi"),
                subtitle = AppLanguageManager.translate("Bağlı operatör turlarını ve yüklenen verileri kendi sitenizde yayınlayın veya yönetin"),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // ── ÜST AKSİYON BAR VE TOPLU İÇERİ AKTARMA BUTONLARI ───────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AppLanguageManager.translate("Operatör Ürün Kataloğu & Sorgu Motoru"),
                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppLanguageManager.translate("Operatörlerden çekilen ve yüklenen otel, paket tur ve uçuş ürün havuzu"),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    TourOSButton(
                        text = "🔍 ${AppLanguageManager.translate("Gelişmiş Arama Motoruna Geç")}",
                        onClick = onNavigateToSearchWizard,
                        variant = TourOSButtonVariant.SECONDARY
                    )
                    TourOSButton(
                        text = "🗑️ ${AppLanguageManager.translate("Kataloğu Sıfırla")}",
                        onClick = { showClearConfirmModal = true },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                    TourOSButton(
                        text = "📥 ${AppLanguageManager.translate("Toplu Veri Yükle (JSON/TXT Import)")}",
                        onClick = { showImportModal = true },
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            }

            // ── KATALOG DURUMU VE OPERATÖR İSTATİSTİK ROZETLERİ ─────────────────────
            val successState = uiState as? AgencyProductPublishingUiState.Success
            val allImportedProducts = successState?.importedProducts ?: emptyList()

            val totalCount = allImportedProducts.size
            val packageTourCount = remember(allImportedProducts) { allImportedProducts.count { it.productType == "PACKAGE_TOUR" } }
            val hotelCount = remember(allImportedProducts) { allImportedProducts.count { it.productType == "HOTEL" } }
            val flightCount = remember(allImportedProducts) { allImportedProducts.count { it.productType == "FLIGHT" } }

            val availableOperators = remember(allImportedProducts) {
                allImportedProducts.map { it.operatorName }.filter { it.isNotBlank() }.distinct()
            }

            if (totalCount > 0) {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.3f),
                    contentPadding = TourOSSpacing.small
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊 ${AppLanguageManager.translate("Toplam Yüklü")}: $totalCount ${AppLanguageManager.translate("Ürün")}",
                                style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🏝️ $packageTourCount ${AppLanguageManager.translate("Paket Tur")}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
                            )
                            Text(
                                text = "🏨 $hotelCount ${AppLanguageManager.translate("Otel")}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
                            )
                            Text(
                                text = "✈️ $flightCount ${AppLanguageManager.translate("Uçuş")}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
                            )
                        }

                        if (availableOperators.isNotEmpty()) {
                            Text(
                                text = "🏢 ${AppLanguageManager.translate("Mevcut Operatörler")}: ${availableOperators.joinToString(", ")}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ── ARAMA, KATEGORİ VE OPERATÖR FİLTRELERİ ─────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = AppLanguageManager.translate("Ürün Sorgusu"),
                            placeholder = AppLanguageManager.translate("Otel adı, bölge, şehir, pansiyon veya operatör ile canlı ara..."),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // KATEGORİ FİLTRELERİ
                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text(AppLanguageManager.translate("Tüm Tipler"), style = TourOSTypography.Caption) }
                        )
                        FilterChip(
                            selected = selectedCategoryFilter == "PACKAGE_TOUR",
                            onClick = { selectedCategoryFilter = "PACKAGE_TOUR" },
                            label = { Text("🏝️ ${AppLanguageManager.translate("Paket Turlar")}", style = TourOSTypography.Caption) }
                        )
                        FilterChip(
                            selected = selectedCategoryFilter == "HOTEL",
                            onClick = { selectedCategoryFilter = "HOTEL" },
                            label = { Text("🏨 ${AppLanguageManager.translate("Oteller")}", style = TourOSTypography.Caption) }
                        )
                        FilterChip(
                            selected = selectedCategoryFilter == "FLIGHT",
                            onClick = { selectedCategoryFilter = "FLIGHT" },
                            label = { Text("✈️ ${AppLanguageManager.translate("Uçuşlar")}", style = TourOSTypography.Caption) }
                        )
                    }
                }

                // 🏢 OPERATÖR FİLTRE ÇUBUĞU (DINAMIK EKLENEN OPERATÖRLER)
                if (availableOperators.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                    ) {
                        Text(
                            text = "🏢 ${AppLanguageManager.translate("Operatör")}:",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                            fontWeight = FontWeight.Bold
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            item {
                                FilterChip(
                                    selected = selectedOperatorFilter == null,
                                    onClick = { selectedOperatorFilter = null },
                                    label = { Text("🌐 ${AppLanguageManager.translate("Tüm Operatörler")}", style = TourOSTypography.Caption) }
                                )
                            }
                            items(availableOperators) { opName ->
                                val countForOp = allImportedProducts.count { it.operatorName.equals(opName, ignoreCase = true) }
                                FilterChip(
                                    selected = selectedOperatorFilter.equals(opName, ignoreCase = true),
                                    onClick = {
                                        selectedOperatorFilter = if (selectedOperatorFilter.equals(opName, ignoreCase = true)) null else opName
                                    },
                                    label = { Text("$opName ($countForOp)", style = TourOSTypography.Caption) }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // ── KATALOG VEYA YÜKLENEN ÜRÜNLER LİSTESİ ─────────────────────────────
            when (val state = uiState) {
                is AgencyProductPublishingUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                }

                is AgencyProductPublishingUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = TourOSColors.Error, style = TourOSTypography.BodyMedium)
                    }
                }

                is AgencyProductPublishingUiState.Success -> {
                    val filteredProducts = remember(state.importedProducts, searchQuery, selectedCategoryFilter, selectedOperatorFilter) {
                        val q = searchQuery.trim().lowercase()
                        state.importedProducts.filter { item ->
                            val matchesCategory = selectedCategoryFilter == null || item.productType == selectedCategoryFilter
                            val matchesOperator = selectedOperatorFilter == null || item.operatorName.equals(selectedOperatorFilter, ignoreCase = true)
                            val matchesSearch = q.isBlank() ||
                                    item.hotelName.lowercase().contains(q) ||
                                    item.tourName.lowercase().contains(q) ||
                                    item.operatorName.lowercase().contains(q) ||
                                    item.region.lowercase().contains(q) ||
                                    item.country.lowercase().contains(q) ||
                                    item.departureCity.lowercase().contains(q) ||
                                    item.roomType.lowercase().contains(q) ||
                                    item.mealType.lowercase().contains(q) ||
                                    item.airlineName.lowercase().contains(q) ||
                                    item.flightNumber.lowercase().contains(q)

                            matchesCategory && matchesOperator && matchesSearch
                        }
                    }

                    val filteredTours = remember(state.tours, searchQuery, selectedOperatorFilter) {
                        val q = searchQuery.trim().lowercase()
                        state.tours.filter { tour ->
                            val matchesOperator = selectedOperatorFilter == null || tour.operatorName.equals(selectedOperatorFilter, ignoreCase = true)
                            val matchesSearch = q.isBlank() ||
                                    tour.tourTitle.lowercase().contains(q) ||
                                    tour.operatorName.lowercase().contains(q) ||
                                    tour.tourCode.lowercase().contains(q)

                            matchesOperator && matchesSearch
                        }
                    }

                    if (filteredProducts.isEmpty() && filteredTours.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = AppLanguageManager.translate("Seçili kriterlere uygun ürün bulunamadı. Yukarıdaki 'Toplu Veri Yükle' butonundan dosya yükleyebilirsiniz."),
                                    style = TourOSTypography.BodyMedium,
                                    color = TourOSColors.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                TourOSButton(
                                    text = "📥 ${AppLanguageManager.translate("Toplu Veri Yükle")}",
                                    onClick = { showImportModal = true },
                                    variant = TourOSButtonVariant.SECONDARY
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // YÜKLENEN GERÇEK VERİLER (UNIFIED PRODUCTS)
                            items(filteredProducts, key = { "prod-${it.id}" }) { product ->
                                UnifiedProductCardItem(
                                    product = product,
                                    onTogglePublish = { isPub ->
                                        viewModel.togglePublishStatus(product.id, isPub)
                                    }
                                )
                            }

                            // YAYIN KARTLARI (FALLBACK TOURS)
                            if (filteredProducts.isEmpty()) {
                                items(filteredTours, key = { "tour-${it.id}" }) { tourItem ->
                                    PublishingTourCard(
                                        item = tourItem,
                                        onTogglePublish = { isPublished ->
                                            viewModel.togglePublishStatus(tourItem.tourId, isPublished)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── TOPLU VERİ YÜKLEME (IMPORT) MODALI ────────────────────────────────────
    if (showImportModal) {
        ImportPayloadModal(
            onDismiss = { showImportModal = false },
            onImport = { content, replaceExisting, onSuccess, onError ->
                viewModel.importRawJsonPayload(
                    rawContent = content,
                    replaceExisting = replaceExisting,
                    onSuccess = { count ->
                        onSuccess(count)
                        showImportModal = false
                    },
                    onError = onError
                )
            }
        )
    }

    // ── KATALOĞU SIFIRLAMA ONAY MODALI ───────────────────────────────────────
    if (showClearConfirmModal) {
        AlertDialog(
            onDismissRequest = { showClearConfirmModal = false },
            title = {
                Text(
                    text = AppLanguageManager.translate("Kataloğu Sıfırla"),
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Error),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = AppLanguageManager.translate("Tüm operatör ürünleri ve yüklenmiş kataloğunuz tamamen silinecektir. Emin misiniz?"),
                    style = TourOSTypography.BodyMedium
                )
            },
            confirmButton = {
                TourOSButton(
                    text = AppLanguageManager.translate("Evet, Sıfırla"),
                    onClick = {
                        viewModel.clearCatalog {
                            showClearConfirmModal = false
                        }
                    },
                    variant = TourOSButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmModal = false }) {
                    Text(AppLanguageManager.translate("İptal"))
                }
            }
        )
    }
}

// ─── CANLI ÜRÜN KART BİLEŞENİ (UNIFIED PRODUCT CARD) ───────────────────────────

@Composable
private fun UnifiedProductCardItem(
    product: UnifiedProductEntity,
    onTogglePublish: (Boolean) -> Unit
) {
    var isPublished by remember(product) { mutableStateOf(true) }
    val marginCalculatedPrice = remember(product.price) { product.price * 1.125 } // %12.5 Acente Kar Marjı

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // BAŞLIK & DURUM ROZETİ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (product.productType) {
                                    "PACKAGE_TOUR" -> TourOSColors.PrimaryContainer
                                    "HOTEL" -> TourOSColors.SecondaryContainer
                                    else -> TourOSColors.PrimaryContainer.copy(alpha = 0.5f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (product.productType) {
                                "PACKAGE_TOUR" -> "🏝️"
                                "HOTEL" -> "🏨"
                                else -> "✈️"
                            },
                            style = TourOSTypography.TitleMedium
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val displayName = remember(product) {
                                product.hotelName.ifBlank { product.tourName }.ifBlank { "Paket Tur & Otel" }
                            }
                            Text(
                                text = displayName,
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                                fontWeight = FontWeight.Bold
                            )
                            if (product.hotelCategory > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "⭐".repeat(product.hotelCategory.coerceAtMost(5)),
                                    style = TourOSTypography.Caption
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            Text(
                                text = "ID: ${product.id}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                                fontSize = 11.sp
                            )
                            Text(text = "•", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            Text(
                                text = "${product.country} · ${product.region}${if (product.subRegion.isNotBlank()) " (${product.subRegion})" else ""}  |  ${AppLanguageManager.translate("Operatör")}: ${product.operatorName}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    }
                }

                Switch(
                    checked = isPublished,
                    onCheckedChange = {
                        isPublished = it
                        onTogglePublish(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TourOSColors.Surface,
                        checkedTrackColor = TourOSColors.Success
                    )
                )
            }

            HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.5f))

            // ÜRÜN PARAMETRELERİ (TÜR, ODA, PANSİYON, GECE, KALKIŞ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (product.departureCity.isNotBlank() || !product.departureDate.isNullOrBlank()) {
                        Text(
                            text = "✈️ ${AppLanguageManager.translate("Kalkış")}: ${product.departureCity.ifBlank { "İstanbul/Moskova" }}  ·  📅 ${product.departureDate ?: ""}  ·  🌙 ${product.nights} ${AppLanguageManager.translate("Gece")}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (product.roomType.isNotBlank() || product.mealType.isNotBlank()) {
                        Text(
                            text = "🍽️ ${product.mealType.ifBlank { "Standart" }}  ·  🛏️ ${product.roomType.ifBlank { "Standart Oda" }}  ·  👥 ${product.adults} ${AppLanguageManager.translate("Yetişkin")}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }

                    if (product.airlineName.isNotBlank()) {
                        Text(
                            text = "🛫 ${product.airlineName} (${product.flightNumber})  ·  🧳 ${product.baggageKg} kg",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }

                // FİYATLANDIRMA DİNAMİĞİ (OPERATÖR FİYATI VS ACENTE SATIŞ FİYATI)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${AppLanguageManager.translate("Operatör Net")}: ${product.price.toInt()} ${product.currency}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                    Text(
                        text = "${marginCalculatedPrice.toInt()} ${product.currency}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%12.5 ${AppLanguageManager.translate("Kar Marjlı")}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─── YAYINLAMA KART BİLEŞENİ ──────────────────────────────────────────────────

@Composable
private fun PublishingTourCard(
    item: AgencyPublishedTourEntity,
    onTogglePublish: (Boolean) -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    Text(
                        text = item.tourTitle,
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    TourOSStatusBadge(
                        text = if (item.isPublished) AppLanguageManager.translate("Yayında") else AppLanguageManager.translate("Taslak"),
                        backgroundColor = if (item.isPublished) TourOSColors.SuccessContainer else TourOSColors.WarningContainer,
                        textColor = if (item.isPublished) TourOSColors.Success else TourOSColors.Warning
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${AppLanguageManager.translate("Kod")}: ${item.tourCode}  ·  ${AppLanguageManager.translate("Operatör")}: ${item.operatorName}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(
                        text = "${AppLanguageManager.translate("Operatör Fiyatı")}: ${item.basePrice} RUB",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                    Text(
                        text = "${AppLanguageManager.translate("Acente Satış Fiyatı")}: ${item.calculatedPrice} RUB",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Switch(
                checked = item.isPublished,
                onCheckedChange = onTogglePublish,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TourOSColors.Surface,
                    checkedTrackColor = TourOSColors.Success
                )
            )
        }
    }
}

// ─── VERİ İÇERİ AKTARMA MODALI (IMPORT MODAL) ─────────────────────────────────

@Composable
private fun ImportPayloadModal(
    onDismiss: () -> Unit,
    onImport: (content: String, replaceExisting: Boolean, onSuccess: (count: Int) -> Unit, onError: (String) -> Unit) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var replaceExisting by remember { mutableStateOf(false) } // Varsayılan: Mevcut Kataloğa Ekle & Birleştir (Eski Silinmez!)
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppLanguageManager.translate("Operatör Verisi İçeri Aktarma (JSON / TXT Import)"),
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Text(
                    text = AppLanguageManager.translate("Operatörden alınan JSON veya TXT formatındaki ham veriyi buraya yapıştırın. Çoklu dosya yüklediğinizde veriler üst üste birleştirilir."),
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )

                // YÜKLEME MODU SEÇENEĞİ (SİL VS BİRLEŞTİR)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    Checkbox(
                        checked = replaceExisting,
                        onCheckedChange = { replaceExisting = it }
                    )
                    Column {
                        Text(
                            text = AppLanguageManager.translate("Eski Kataloğu Sıfırla & Sadece Bu Dosyayı Yükle"),
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppLanguageManager.translate("İşaretlenmezse yüklenen tüm dosyalar birbirinin üstüne eklenerek birleştirilir."),
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }

                OutlinedTextField(
                    value = rawText,
                    onValueChange = {
                        rawText = it
                        errorMessage = null
                    },
                    label = { Text(AppLanguageManager.translate("Ham JSON / TXT Verisi")) },
                    placeholder = { Text("{\n   \"id\": \"13237740228098\",\n   \"name\": \"Moscow Antalya PROMO\",\n   ...\n}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    textStyle = TourOSTypography.Caption
                )

                errorMessage?.let { err ->
                    Text(text = "❌ $err", color = TourOSColors.Error, style = TourOSTypography.Caption)
                }

                successMessage?.let { msg ->
                    Text(text = "✅ $msg", color = TourOSColors.Success, style = TourOSTypography.Caption, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TourOSButton(
                text = "⚡ ${AppLanguageManager.translate("Veriyi İçeri Aktar & İşle")}",
                onClick = {
                    if (rawText.isBlank()) {
                        errorMessage = AppLanguageManager.translate("Lütfen geçerli bir JSON/TXT metni yapıştırın.")
                        return@TourOSButton
                    }
                    onImport(
                        rawText,
                        replaceExisting,
                        { count ->
                            successMessage = "$count ${AppLanguageManager.translate("adet güncel ürün başarıyla yüklendi!")}"
                        },
                        { err ->
                            errorMessage = err
                        }
                    )
                },
                variant = TourOSButtonVariant.PRIMARY
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppLanguageManager.translate("İptal"))
            }
        }
    )
}
