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
import com.mgacreative.touros.utils.rememberFilePickerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.repository.AuthRepository
import org.koin.compose.koinInject

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
    val currentUser by viewModel.currentUserState.collectAsState()

    val isSystemAdmin = currentUser?.role == UserRole.SYSTEM_ADMIN || 
            currentUser?.email == "mgazat@gmail.com" || 
            currentUser?.email == "gkhnazat@gmail.com" || 
            currentUser?.email?.lowercase()?.contains("mgazat") == true || 
            currentUser?.email?.lowercase()?.contains("gkhn") == true || 
            currentUser?.email?.lowercase()?.contains("admin") == true

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
                onNavigateBack = onNavigateBack
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

                    // SADECE SİSTEM YÖNETİCİSİ (gkhnazat@gmail.com) İÇİN GÖRÜNÜR
                    if (isSystemAdmin) {
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
            }

            // ── KATALOG DURUMU VE OPERATÖR İSTATİSTİK ROZETLERİ ─────────────────────
            val successState = uiState as? AgencyProductPublishingUiState.Success
            val allImportedProducts = successState?.importedProducts ?: emptyList()

            val totalCount = allImportedProducts.size
            val packageTourCount = remember(allImportedProducts) { allImportedProducts.count { it.safeProductType == "PACKAGE_TOUR" } }
            val hotelCount = remember(allImportedProducts) { allImportedProducts.count { it.safeProductType == "HOTEL" } }
            val flightCount = remember(allImportedProducts) { allImportedProducts.count { it.safeProductType == "FLIGHT" } }

            val availableOperators = remember(allImportedProducts) {
                allImportedProducts.map { it.safeOperatorName }.filter { it.isNotBlank() }.distinct()
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
                                val countForOp = allImportedProducts.count { it.safeOperatorName.equals(opName, ignoreCase = true) }
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
                            val matchesCategory = selectedCategoryFilter == null || item.safeProductType == selectedCategoryFilter
                            val matchesOperator = selectedOperatorFilter == null || item.safeOperatorName.equals(selectedOperatorFilter, ignoreCase = true)
                            val matchesSearch = q.isBlank() ||
                                    item.safeHotelName.lowercase().contains(q) ||
                                    item.safeTourName.lowercase().contains(q) ||
                                    item.safeOperatorName.lowercase().contains(q) ||
                                    item.safeRegion.lowercase().contains(q) ||
                                    item.safeCountry.lowercase().contains(q) ||
                                    item.safeDepartureCity.lowercase().contains(q) ||
                                    item.safeRoomType.lowercase().contains(q) ||
                                    item.safeMealType.lowercase().contains(q) ||
                                    item.safeAirlineName.lowercase().contains(q) ||
                                    item.safeFlightNumber.lowercase().contains(q)

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
                        val displayProducts = remember(filteredProducts) { filteredProducts.take(150) }
                        val displayTours = remember(filteredTours) { filteredTours.take(150) }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // YÜKLENEN GERÇEK VERİLER (UNIFIED PRODUCTS)
                            items(displayProducts, key = { "prod-${it.id}" }) { product ->
                                UnifiedProductCardItem(
                                    product = product,
                                    onTogglePublish = { isPub ->
                                        viewModel.togglePublishStatus(product.id, isPub)
                                    }
                                )
                            }

                            // YAYIN KARTLARI (FALLBACK TOURS)
                            if (filteredProducts.isEmpty()) {
                                items(displayTours, key = { "tour-${it.id}" }) { tourItem ->
                                    PublishingTourCard(
                                        item = tourItem,
                                        onTogglePublish = { isPublished ->
                                            viewModel.togglePublishStatus(tourItem.tourId, isPublished)
                                        }
                                    )
                                }
                            }

                            if (filteredProducts.size > 150) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = TourOSSpacing.small),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "ℹ️ ${AppLanguageManager.translate("Toplam")} ${filteredProducts.size} ${AppLanguageManager.translate("üründen ilk 150 tanesi gösteriliyor. Özel ürün için yukarıdaki canlı aramayı kullanın.")}",
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
                                    }
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
            onImport = { content, onSuccess, onError ->
                viewModel.importRawJsonPayload(
                    rawContent = content,
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
    val marginCalculatedPrice = remember(product.safePrice) { product.safePrice * 1.125 } // %12.5 Acente Kar Marjı

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
                            if (product.safeHotelCategory > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "⭐".repeat(product.safeHotelCategory.coerceAtMost(5)),
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
                            text = "✈️ ${AppLanguageManager.translate("Kalkış")}: ${product.departureCity.ifBlank { "İstanbul/Moskova" }}  ·  📅 ${product.departureDate ?: ""}  ·  🌙 ${product.safeNights} ${AppLanguageManager.translate("Gece")}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (product.roomType.isNotBlank() || product.mealType.isNotBlank()) {
                        Text(
                            text = "🍽️ ${product.mealType.ifBlank { "Standart" }}  ·  🛏️ ${product.roomType.ifBlank { "Standart Oda" }}  ·  👥 ${product.safeAdults} ${AppLanguageManager.translate("Yetişkin")}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }

                    if (product.airlineName.isNotBlank()) {
                        Text(
                            text = "🛫 ${product.airlineName} (${product.flightNumber})  ·  🧳 ${product.safeBaggageKg} kg",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }

                // FİYATLANDIRMA DİNAMİĞİ (OPERATÖR FİYATI VS ACENTE SATIŞ FİYATI)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${AppLanguageManager.translate("Operatör Net")}: ${product.safePrice.toInt()} ${product.currency}",
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
    onImport: (content: String, onSuccess: (count: Int) -> Unit, onError: (String) -> Unit) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var loadedFileContent by remember { mutableStateOf<String?>(null) }
    var selectedFileLabel by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filePickerLauncher = rememberFilePickerLauncher(mimeType = "*/*") { fileName, bytes ->
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                successMessage = null

                if (bytes.isEmpty()) {
                    throw IllegalArgumentException("Seçilen dosya boş (0 bytes).")
                }

                val decoded = withContext(Dispatchers.Default) {
                    bytes.decodeToString()
                        .replace("\uFEFF", "")
                        .replace("\u200B", "")
                        .trim()
                }

                selectedFileLabel = "📁 $fileName (${bytes.size / 1024} KB)"
                loadedFileContent = decoded
                // TextField'a devasa 10MB metin yükleyip Compose UI layout motorunu kilitletmeyi engelle:
                if (decoded.length < 5000) {
                    rawText = decoded
                } else {
                    rawText = "" // Büyüklük 5KB üzerindeyse TextField'ı boş tut, arkada loadedFileContent'i işle
                }

                onImport(
                    decoded,
                    { count ->
                        isLoading = false
                        successMessage = "$count ${AppLanguageManager.translate("adet güncel ürün başarıyla yüklendi!")}"
                    },
                    { err ->
                        isLoading = false
                        errorMessage = err
                    }
                )
            } catch (e: Throwable) {
                isLoading = false
                errorMessage = "Dosya işlenirken hata oluştu: ${e.message ?: "Geçersiz dosya biçimi"}"
            } finally {
                isLoading = false
            }
        }
    }

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
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Text(
                    text = AppLanguageManager.translate("Operatörden alınan JSON veya TXT dosyasını doğrudan seçebilir ya da ham metni aşağıya yapıştırabilirsiniz."),
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSButton(
                            text = "📁 ${AppLanguageManager.translate("Bilgisayardan Dosya Seç (.txt / .json)")}",
                            onClick = { filePickerLauncher() },
                            enabled = !isLoading,
                            isLoading = isLoading,
                            variant = TourOSButtonVariant.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                selectedFileLabel?.let { label ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TourOSColors.SuccessContainer.copy(alpha = 0.5f))
                            .padding(TourOSSpacing.small)
                    ) {
                        Text(
                            text = label,
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = TourOSColors.Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                        Text(
                            text = AppLanguageManager.translate("Yüksek hacimli veri ayrıştırılıyor ve veritabanına işleniyor..."),
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = rawText,
                    onValueChange = {
                        rawText = it
                        loadedFileContent = null
                        errorMessage = null
                    },
                    label = { Text(AppLanguageManager.translate("Ham JSON / TXT Verisi")) },
                    placeholder = { Text("{\n   \"id\": \"13237740228098\",\n   \"name\": \"Moscow Antalya PROMO\",\n   ...\n}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    enabled = !isLoading,
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
                text = "⚡ ${AppLanguageManager.translate("Metni İçeri Aktar & İşle")}",
                onClick = {
                    val targetPayload = loadedFileContent ?: rawText
                    if (targetPayload.isBlank()) {
                        errorMessage = AppLanguageManager.translate("Lütfen bir dosya seçin ya da geçerli metin yapıştırın.")
                        return@TourOSButton
                    }
                    isLoading = true
                    errorMessage = null
                    successMessage = null
                    onImport(
                        targetPayload,
                        { count ->
                            isLoading = false
                            successMessage = "$count ${AppLanguageManager.translate("adet güncel ürün başarıyla yüklendi!")}"
                        },
                        { err ->
                            isLoading = false
                            errorMessage = err
                        }
                    )
                },
                enabled = !isLoading,
                isLoading = isLoading,
                variant = TourOSButtonVariant.PRIMARY
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(AppLanguageManager.translate("İptal / Kapat"))
            }
        }
    )
}
