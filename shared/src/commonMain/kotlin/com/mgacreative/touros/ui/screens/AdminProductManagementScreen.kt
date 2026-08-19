package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.DataFeedSource
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AdminDataManagementViewModel
import com.mgacreative.touros.ui.viewmodel.AgencyProductPublishingViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * SaaS Admin - Merkezi Ürün & Data Yönetimi Ekranı.
 *
 * Sekme 1: Tur Operatörleri Ürünleri (Merkez Ürün Havuzu)
 * Sekme 2: Data Yönetimi (API Beslemeleri, Ekleme/Çıkarma, Anahtarlar ve Senkronizasyon Motoru)
 */
@Composable
fun AdminProductManagementScreen(
    publishingViewModel: AgencyProductPublishingViewModel = koinViewModel(),
    dataManagementViewModel: AdminDataManagementViewModel = koinViewModel()
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Ürün Havuzu, 1: Data Yönetimi

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Admin Ürün & Data Yönetimi",
                subtitle = "SaaS Admin: Tur operatörleri ürün havuzu ve merkezi API veri besleme kontrolü"
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
            // SEKME DEĞİŞTİRİCİ
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "📦 1. Tur Operatörleri Ürün Havuzu",
                    "🗄️ 2. Data Yönetimi (API Beslemeleri & Entegrasyon)"
                ).forEachIndexed { index, title ->
                    val isSelected = activeTab == index
                    OutlinedButton(
                        onClick = { activeTab = index },
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

            // İÇERİK: SEKME 1 veya SEKME 2
            if (activeTab == 0) {
                // ── SEKME 1: TUR OPERATÖRLERİ ÜRÜN HAVUZU ─────────────────────────
                Box(modifier = Modifier.fillMaxSize()) {
                    AgencyProductPublishingScreen(viewModel = publishingViewModel)
                }
            } else {
                // ── SEKME 2: DATA YÖNETİMİ ────────────────────────────────────────
                AdminDataManagementSection(
                    viewModel = dataManagementViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ─── SEKME 2: DATA YÖNETİMİ BİLEŞENİ ──────────────────────────────────────────

@Composable
private fun AdminDataManagementSection(
    viewModel: AdminDataManagementViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var sourcePendingDelete by remember { mutableStateOf<DataFeedSource?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        // Bildirim & Hata Mesajları
        uiState.notificationMessage?.let { msg ->
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

        uiState.errorMessage?.let { err ->
            Surface(
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                color = TourOSColors.SecondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(TourOSSpacing.medium).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(err, style = TourOSTypography.Label.copy(color = TourOSColors.Secondary))
                    Text(
                        "✕",
                        modifier = Modifier.clickable { viewModel.clearNotification() }.padding(horizontal = 4.dp),
                        style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
                    )
                }
            }
        }

        // Üst Başlık & Ekle Butonu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "🗄️ Merkezi API Data Besleme & Entegrasyon Motoru",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                )
                Text(
                    "Tur Operatörleri ve tedarikçi API'lerinden otomatik veri çekme, yeni operatör ekleme/çıkarma ve sorgu yönetimi.",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            TourOSButton(
                text = "➕ Yeni API Operatörü Ekle",
                onClick = { viewModel.openNewSource() },
                variant = TourOSButtonVariant.PRIMARY
            )
        }

        // DATA BESLEME LİSTESİ
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            items(uiState.feedSources, key = { it.id }) { source ->
                DataFeedSourceCardItem(
                    source = source,
                    onEdit = { viewModel.openEditSource(source) },
                    onDelete = { sourcePendingDelete = source },
                    onToggleLive = { isLive -> viewModel.toggleLiveStatus(source.id, isLive) },
                    onTestConnection = { viewModel.testConnection(source) },
                    onManualSync = { viewModel.manualSyncNow(source.id) }
                )
            }
        }

        // DÜZENLEME / EKLEME MODAL DİYALOĞU
        uiState.selectedSourceForEdit?.let { source ->
            DataFeedEditModalDialog(
                source = source,
                onDismiss = { viewModel.closeEditSource() },
                onSave = { updated -> viewModel.saveFeedSource(updated) },
                onDelete = {
                    viewModel.closeEditSource()
                    sourcePendingDelete = source
                }
            )
        }

        // SİLME ONAY DİYALOĞU
        sourcePendingDelete?.let { source ->
            AlertDialog(
                onDismissRequest = { sourcePendingDelete = null },
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🗑️", fontSize = 24.sp)
                        Text("Operatör API Kaynağını Sil", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Secondary))
                    }
                },
                text = {
                    Text(
                        "'${source.sourceName}' API besleme kaynağını sistemden tamamen kaldırmak istediğinizden emin misiniz? Bu işlem geri alınamaz.",
                        style = TourOSTypography.BodyMedium
                    )
                },
                confirmButton = {
                    TourOSButton(
                        text = "Evet, Kaynağı Sil",
                        onClick = {
                            viewModel.deleteFeedSource(source.id)
                            sourcePendingDelete = null
                        },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                },
                dismissButton = {
                    TourOSButton(text = "Vazgeç", onClick = { sourcePendingDelete = null }, variant = TourOSButtonVariant.TERTIARY)
                },
                containerColor = TourOSColors.Surface,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusLarge)
            )
        }
    }
}

// ─── DATA BESLEME KARTI BİLEŞENİ ──────────────────────────────────────────────

@Composable
private fun DataFeedSourceCardItem(
    source: DataFeedSource,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleLive: (Boolean) -> Unit,
    onTestConnection: () -> Unit,
    onManualSync: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // Başlık & Canlı/Beklemede Durum Rozeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(source.logoIcon, style = TourOSTypography.TitleLarge)
                    Column {
                        Text(
                            source.sourceName,
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (source.endpointUrl.isNotBlank()) source.endpointUrl else "Endpoint URL tanımlanmadı",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }

                // CANLI / BEKLEMEDE ROZETİ
                TourOSStatusBadge(
                    text = if (source.isLive) "🟢 CANLI DEVREDE" else "🟡 HAZIR / BEKLEMEDE",
                    backgroundColor = if (source.isLive) TourOSColors.SuccessContainer else TourOSColors.PrimaryContainer.copy(alpha = 0.5f),
                    textColor = if (source.isLive) TourOSColors.Success else TourOSColors.Primary
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Detay Satırları: Veri Tipleri, Sıklık, Son Senkronizasyon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Envanter Ayrıştırma:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "⚡ Tam Envanter (Tur, Otel, Uçuş)",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Sorgu / Sync Sıklığı:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        when (source.syncInterval) {
                            "10_MIN" -> "⚡ Her 10 Dakika"
                            "30_MIN" -> "⏱️ Her 30 Dakika"
                            "1_HOUR" -> "🕒 Saatte Bir (1 Sa)"
                            "6_HOUR" -> "🕕 6 Saatte Bir"
                            "24_HOUR" -> "🌙 Günde Bir (Gece)"
                            else -> "🖐️ Sadece Manuel"
                        },
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                    )
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Son Veri Çekimi:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(source.lastSyncedAt, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                }
            }

            // Durum Mesajı
            source.statusMessage?.let { status ->
                Surface(
                    shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                    color = TourOSColors.SurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        status,
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                        modifier = Modifier.padding(horizontal = TourOSSpacing.medium, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

            // AKSİYON BUTONLARI & CANLIYA ALMA SWITCH'İ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Canlı / Devre Dışı Switch
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = source.isLive,
                        onCheckedChange = onToggleLive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TourOSColors.Success,
                            checkedTrackColor = TourOSColors.SuccessContainer
                        )
                    )
                    Text(
                        if (source.isLive) "Otomatik Çekim Açık" else "Devre Dışı (Hazırda Bekle)",
                        style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // İşlem Butonları
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    OutlinedButton(
                        onClick = onTestConnection,
                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                    ) {
                        Text("🔌 Bağlantıyı Test Et", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onManualSync,
                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                    ) {
                        Text("⚡ Şimdi Datayı Çek", fontSize = 12.sp)
                    }

                    TourOSButton(
                        text = "⚙️ API Yapılandır",
                        onClick = onEdit,
                        variant = TourOSButtonVariant.SECONDARY
                    )

                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TourOSColors.Secondary)
                    ) {
                        Text("🗑️ Sil", fontSize = 12.sp, color = TourOSColors.Secondary)
                    }
                }
            }
        }
    }
}

// ─── API DATA BESLEME DÜZENLEME / EKLEME MODAL DİYALOĞU ───────────────────────

@Composable
private fun DataFeedEditModalDialog(
    source: DataFeedSource,
    onDismiss: () -> Unit,
    onSave: (DataFeedSource) -> Unit,
    onDelete: () -> Unit
) {
    var sourceName by remember { mutableStateOf(source.sourceName) }
    var logoIcon by remember { mutableStateOf(source.logoIcon) }
    var endpointUrl by remember { mutableStateOf(source.endpointUrl) }
    var apiKey by remember { mutableStateOf(source.apiKey) }
    var apiSecret by remember { mutableStateOf(source.apiSecret) }
    var agencyCode by remember { mutableStateOf(source.agencyCode) }
    var syncInterval by remember { mutableStateOf(source.syncInterval) }
    var isLive by remember { mutableStateOf(source.isLive) }

    val isNew = source.endpointUrl.isBlank() && source.apiKey.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small), verticalAlignment = Alignment.CenterVertically) {
                Text(logoIcon, style = TourOSTypography.TitleLarge)
                Text(if (isNew) "➕ Yeni API Operatörü Ekle" else "${source.sourceName} Yapılandırması", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                // HIZLI OPERATÖR ŞABLONU SEÇİCİ
                Text("Hızlı Operatör Şablonu Seç:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        Triple("🇷🇺 TourVisor (Rusya)", "https://api.tourvisor.ru/search/api/v1", "🇷🇺"),
                        Triple("✈️ Paximum / SanTSG", "https://api.paximum.com/v2/service", "✈️"),
                        Triple("🌴 Coral / Odeon", "https://b2bapi.coraltravel.com/api/v1", "🌴"),
                        Triple("🏖️ Pegas Touristik", "https://api.pegast.com/v1/search", "🏖️"),
                        Triple("🏨 Sejour Incoming", "https://xml.sejour.com.tr/service.asmx", "🏨"),
                        Triple("🌍 Amadeus GDS", "https://api.amadeus.com/v1", "🌍"),
                        Triple("🔗 Özel Operatör", "https://api.operatör.com/feed.json", "🔗")
                    ).forEach { (name, defaultUrl, icon) ->
                        OutlinedButton(
                            onClick = {
                                sourceName = name
                                endpointUrl = defaultUrl
                                logoIcon = icon
                                if (name.contains("TourVisor")) {
                                    syncInterval = "30_MIN"
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                        ) {
                            Text(name.substringBefore(" /").substringBefore(" "), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                TourOSTextField(
                    value = sourceName,
                    onValueChange = { sourceName = it },
                    label = "Data Kaynağı / Operatör Adı",
                    placeholder = "Örn: Pegas Touristik Global API",
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = endpointUrl,
                    onValueChange = { endpointUrl = it },
                    label = "API Base Endpoint URL",
                    placeholder = "https://api.operatör.com/v1/...",
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = "API Key / Public Token",
                    placeholder = "pk_live_...",
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = apiSecret,
                    onValueChange = { apiSecret = it },
                    label = "API Secret / Private Key",
                    placeholder = "sk_live_...",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = agencyCode,
                    onValueChange = { agencyCode = it },
                    label = "Acente / Merchant Kodu (Opsiyonel)",
                    placeholder = "Örn: SAN-TR-001",
                    modifier = Modifier.fillMaxWidth()
                )



                // SENKRONİZASYON SIKLIĞI SEÇİMİ
                Text("Otomatik Veri Çekme Sıklığı:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "10_MIN" to "10 Dk",
                        "30_MIN" to "30 Dk",
                        "1_HOUR" to "1 Saat",
                        "6_HOUR" to "6 Saat",
                        "24_HOUR" to "Günde 1",
                        "MANUAL" to "Manuel"
                    ).forEach { (iKey, iLabel) ->
                        val isSelected = syncInterval == iKey
                        OutlinedButton(
                            onClick = { syncInterval = iKey },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                            colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                iLabel,
                                style = TourOSTypography.Caption.copy(
                                    color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // CANLI DEVREYE ALMA SWITCH'İ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Canlı Devreye Alma Durumu:", style = TourOSTypography.Label)
                    Switch(
                        checked = isLive,
                        onCheckedChange = { isLive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TourOSColors.Success,
                            checkedTrackColor = TourOSColors.SuccessContainer
                        )
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isNew) {
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TourOSColors.Secondary)
                    ) {
                        Text("🗑️ Sil", color = TourOSColors.Secondary)
                    }
                }
                TourOSButton(
                    text = "💾 Operatörü Kaydet",
                    onClick = {
                        val updated = source.copy(
                            sourceName = sourceName,
                            logoIcon = logoIcon,
                            endpointUrl = endpointUrl,
                            apiKey = apiKey,
                            apiSecret = apiSecret,
                            agencyCode = agencyCode,
                            dataTypes = listOf("TOURS", "HOTELS", "FLIGHTS"),
                            syncInterval = syncInterval,
                            isLive = isLive,
                            statusMessage = if (isLive) "🟢 CANLI DEVREDE" else "🟡 BEKLEMEDE (Hazır)"
                        )
                        onSave(updated)
                    },
                    variant = TourOSButtonVariant.PRIMARY
                )
            }
        },
        dismissButton = {
            TourOSButton(text = "İptal", onClick = onDismiss, variant = TourOSButtonVariant.TERTIARY)
        },
        containerColor = TourOSColors.Surface,
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusLarge)
    )
}
