package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.OTAHubViewModel

/**
 * OTA Dashboard & Kanal Yöneticisi Ekranı (Dinamik & Hardcoded Olmayan)
 */
@Composable
fun OTADashboardScreen(
    viewModel: OTAHubViewModel,
    tenantId: String = "tenant-001",
    onNavigateToLogs: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "OTA & Kanal Yöneticisi (Channel Manager)",
                subtitle = "Viator, GetYourGuide, Booking.com, Airbnb entegrasyonu ve ürün dağıtımı",
                actions = {
                    TourOSButton(
                        text = "📋 Senkronizasyon Logları",
                        onClick = { onNavigateToLogs("ALL") },
                        variant = TourOSButtonVariant.SECONDARY,
                        modifier = Modifier.padding(end = TourOSSpacing.small)
                    )
                    TourOSButton(
                        text = "🔄 Canlı Yenile",
                        onClick = { viewModel.loadInitialData() },
                        variant = TourOSButtonVariant.PRIMARY
                    )
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
            // Bildirim Çubuğu
            uiState.syncStatusMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                    color = TourOSColors.SuccessContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(TourOSSpacing.medium).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(msg, style = TourOSTypography.Label.copy(color = TourOSColors.Success))
                        Text(
                            "✕",
                            modifier = Modifier.clickable { viewModel.clearNotification() }.padding(horizontal = 4.dp),
                            style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                        )
                    }
                }
            }

            // ÖZET METRİK BARI (Dinamik)
            val totalChannels = uiState.accounts.size
            val activeChannels = uiState.accounts.count { it.isConnected }
            val totalPublishedMappings = uiState.mappings.count { it.isEnabled }
            val errorChannels = uiState.accounts.count { it.hasError }

            OTADynamicMetricsOverviewBar(
                totalChannels = totalChannels,
                activeChannels = activeChannels,
                totalPublishedProducts = totalPublishedMappings,
                errorChannels = errorChannels
            )

            // SEKME DEĞİŞTİRİCİ: 1. Kanallar & API | 2. Kanal Ürün Dağıtımı
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("🔗 OTA Kanalları & API Kimlikleri", "📦 Kanal Ürün Dağıtımı (Hangi Ürün Nerede Satılacak?)").forEachIndexed { index, title ->
                    val isSelected = uiState.activeTab == index
                    OutlinedButton(
                        onClick = { viewModel.setTab(index) },
                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                        colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(
                            title,
                            style = TourOSTypography.Label.copy(
                                color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            // İÇERİK: TAB 0 (Kanallar) veya TAB 1 (Ürün Dağıtımı)
            if (uiState.activeTab == 0) {
                // ── TAB 0: OTA KANALLARI GRID'İ ──────────────────────────────
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(uiState.accounts) { account ->
                        val productCount = uiState.mappings.count { it.providerId == account.providerId && it.isEnabled }
                        OTADynamicChannelCard(
                            account = account,
                            activeProductCount = productCount,
                            onManage = { viewModel.openProviderConfig(account) },
                            onSync = { viewModel.syncNow(account.providerId, true, tenantId) }
                        )
                    }
                }
            } else {
                // ── TAB 1: KANAL ÜRÜN DAĞITIMI (HANGİ ÜRÜN NEREDE SATILACAK?) ──
                OTAProductDistributionSection(
                    tours = uiState.tours,
                    hotels = uiState.hotels,
                    accounts = uiState.accounts,
                    mappings = uiState.mappings,
                    onToggle = { productId, title, type, providerId, isEnabled ->
                        viewModel.toggleProductChannel(productId, title, type, providerId, isEnabled)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // API & KEY YAPILANDIRMA MODAL DİYALOĞU
        uiState.selectedProviderForConfig?.let { account ->
            OTAApiConfigModalDialog(
                account = account,
                onDismiss = { viewModel.closeProviderConfig() },
                onSave = { updated -> viewModel.saveAccountConfig(updated) }
            )
        }
    }
}

// ─── DİNAMİK METRİK ÇUBUĞU ───────────────────────────────────────────────────

@Composable
private fun OTADynamicMetricsOverviewBar(
    totalChannels: Int,
    activeChannels: Int,
    totalPublishedProducts: Int,
    errorChannels: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = TourOSColors.PrimaryContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Kanal Durumu", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text("$activeChannels / $totalChannels AKTİF", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
            }
        }

        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = TourOSColors.SuccessContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("OTA'da Yayında Olan Ürün", style = TourOSTypography.Caption.copy(color = TourOSColors.Success))
                Text("$totalPublishedProducts EŞLEŞME", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Success))
            }
        }

        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = if (errorChannels > 0) TourOSColors.SecondaryContainer else TourOSColors.Surface,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Hatalı Kanal", style = TourOSTypography.Caption.copy(color = if (errorChannels > 0) TourOSColors.Secondary else TourOSColors.TextSecondary))
                Text(if (errorChannels > 0) "$errorChannels HATA" else "SORUNSUZ", style = TourOSTypography.TitleLarge.copy(color = if (errorChannels > 0) TourOSColors.Secondary else TourOSColors.TextPrimary))
            }
        }
    }
}

// ─── DİNAMİK KANAL KARTI ─────────────────────────────────────────────────────

@Composable
private fun OTADynamicChannelCard(
    account: OTAAccount,
    activeProductCount: Int,
    onManage: () -> Unit,
    onSync: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
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
                    Text(account.logoIcon, style = TourOSTypography.TitleLarge)
                    Column {
                        Text(account.accountName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                        Text(
                            if (account.supplierId.isNotBlank()) "ID: ${account.supplierId}" else "API Key Tanımlanmadı",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }

                TourOSStatusBadge(
                    text = if (account.hasError) "❌ HATA" else if (account.isConnected) "✅ BAĞLI" else "○ PASİF",
                    backgroundColor = if (account.hasError) TourOSColors.SecondaryContainer else if (account.isConnected) TourOSColors.SuccessContainer else TourOSColors.Surface,
                    textColor = if (account.hasError) TourOSColors.Secondary else if (account.isConnected) TourOSColors.Success else TourOSColors.TextSecondary
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Son Senkronizasyon:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text(account.lastSyncedAt, style = TourOSTypography.Label.copy(color = TourOSColors.Primary))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Yayındaki Ürün Sayısı:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text("$activeProductCount Ürün Satışta", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Fiyat Marjı (Surge):", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text("+%${account.rateMarginPercent}", style = TourOSTypography.Label.copy(color = TourOSColors.Success))
            }

            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                TourOSButton(
                    text = "⚙️ API Ayarla",
                    onClick = onManage,
                    variant = TourOSButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f)
                )
                TourOSButton(
                    text = "⚡ Senkronize Et",
                    onClick = onSync,
                    variant = TourOSButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─── TAB 1: KANAL ÜRÜN DAĞITIMI BİLEŞENİ ──────────────────────────────────────

@Composable
private fun OTAProductDistributionSection(
    tours: List<com.mgacreative.touros.domain.model.Tour>,
    hotels: List<com.mgacreative.touros.domain.model.Hotel>,
    accounts: List<OTAAccount>,
    mappings: List<com.mgacreative.touros.domain.model.ota.OTAChannelProductMapping>,
    onToggle: (String, String, String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    TourOSCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            Text(
                "📦 Yerel Tur ve Otel Portföyünüzü Hangi Sitede Satacağınızı Seçin",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )
            Text(
                "Aşağıdaki ürünlerinizin yanındaki kanal butonlarına tıklayarak Viator, GetYourGuide veya Booking.com'da anında satışa açabilir veya kaldırabilirsiniz.",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                // TURLAR
                if (tours.isNotEmpty()) {
                    item {
                        Text("🗺️ Yerel Turlar (${tours.size})", fontWeight = FontWeight.Bold, color = TourOSColors.Primary, fontSize = 14.sp)
                    }
                    items(tours) { tour ->
                        ProductChannelRowItem(
                            productId = tour.id,
                            productTitle = tour.title,
                            productType = "tour",
                            priceText = "${tour.basePrice} EUR",
                            accounts = accounts,
                            mappings = mappings,
                            onToggle = onToggle
                        )
                    }
                }

                // OTELLER
                if (hotels.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(TourOSSpacing.small))
                        Text("🏨 Yerel Oteller (${hotels.size})", fontWeight = FontWeight.Bold, color = TourOSColors.Primary, fontSize = 14.sp)
                    }
                    items(hotels) { hotel ->
                        ProductChannelRowItem(
                            productId = hotel.id,
                            productTitle = hotel.name,
                            productType = "hotel",
                            priceText = "${hotel.city}, ${hotel.country}",
                            accounts = accounts,
                            mappings = mappings,
                            onToggle = onToggle
                        )
                    }
                }

                if (tours.isEmpty() && hotels.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(TourOSSpacing.large), contentAlignment = Alignment.Center) {
                            Text("Sistemde henüz yerel tur veya otel kaydı bulunmuyor.", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductChannelRowItem(
    productId: String,
    productTitle: String,
    productType: String,
    priceText: String,
    accounts: List<OTAAccount>,
    mappings: List<com.mgacreative.touros.domain.model.ota.OTAChannelProductMapping>,
    onToggle: (String, String, String, String, Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
        color = TourOSColors.SurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(TourOSSpacing.medium).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = TourOSSpacing.small)) {
                Text(
                    productTitle,
                    style = TourOSTypography.Label.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(priceText, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
            }

            // KANAL SEÇİM BUTONLARI (CHIPS)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                accounts.forEach { account ->
                    val isMapped = mappings.any { it.productId == productId && it.providerId == account.providerId && it.isEnabled }
                    FilterChip(
                        selected = isMapped,
                        onClick = { onToggle(productId, productTitle, productType, account.providerId, !isMapped) },
                        label = {
                            Text(
                                "${account.logoIcon} ${account.providerId.uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = if (isMapped) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TourOSColors.SuccessContainer,
                            selectedLabelColor = TourOSColors.Success
                        )
                    )
                }
            }
        }
    }
}

// ─── API VE KEY YAPILANDIRMA MODAL DİYALOĞU ──────────────────────────────────

@Composable
private fun OTAApiConfigModalDialog(
    account: OTAAccount,
    onDismiss: () -> Unit,
    onSave: (OTAAccount) -> Unit
) {
    var supplierId by remember { mutableStateOf(account.supplierId) }
    var apiKey by remember { mutableStateOf(account.apiKey) }
    var apiSecret by remember { mutableStateOf(account.apiSecret) }
    var webhookUrl by remember { mutableStateOf(account.webhookUrl) }
    var rateMarginPercent by remember { mutableStateOf(account.rateMarginPercent.toString()) }
    var syncIntervalMinutes by remember { mutableStateOf(account.syncIntervalMinutes) }
    var isConnected by remember { mutableStateOf(account.isConnected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small), verticalAlignment = Alignment.CenterVertically) {
                Text(account.logoIcon, style = TourOSTypography.TitleLarge)
                Text("${account.accountName} API Yapılandırması", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                TourOSTextField(
                    value = supplierId,
                    onValueChange = { supplierId = it },
                    label = "Merchant / Supplier ID",
                    placeholder = "Örn: VIA-SUP-9012",
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = "API Key / Public Key",
                    placeholder = "pk_live_...",
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = apiSecret,
                    onValueChange = { apiSecret = it },
                    label = "API Secret / Private Token",
                    placeholder = "sk_live_...",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = webhookUrl,
                    onValueChange = { webhookUrl = it },
                    label = "Webhook Callback URL",
                    placeholder = "https://...",
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = rateMarginPercent,
                    onValueChange = { rateMarginPercent = it },
                    label = "Kanal Fiyat Artış Marjı (%)",
                    placeholder = "12.0",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kanal Bağlantı Durumu:", style = TourOSTypography.Label)
                    Switch(checked = isConnected, onCheckedChange = { isConnected = it })
                }
            }
        },
        confirmButton = {
            TourOSButton(
                text = "💾 Ayarları Kaydet",
                onClick = {
                    val margin = rateMarginPercent.toDoubleOrNull() ?: 0.0
                    val updated = account.copy(
                        supplierId = supplierId,
                        apiKey = apiKey,
                        apiSecret = apiSecret,
                        webhookUrl = webhookUrl,
                        rateMarginPercent = margin,
                        syncIntervalMinutes = syncIntervalMinutes,
                        isConnected = isConnected,
                        lastSyncedAt = if (isConnected) "Şimdi (Güncellendi)" else "Bağlantı Kesildi"
                    )
                    onSave(updated)
                },
                variant = TourOSButtonVariant.PRIMARY
            )
        },
        dismissButton = {
            TourOSButton(text = "İptal", onClick = onDismiss, variant = TourOSButtonVariant.TERTIARY)
        },
        containerColor = TourOSColors.Surface,
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusLarge)
    )
}
