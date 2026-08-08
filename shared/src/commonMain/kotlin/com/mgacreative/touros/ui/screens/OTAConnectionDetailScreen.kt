package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.OTAHubViewModel

/**
 * OTA Bağlantı Detayı Ekranı — TourOS 0.3
 *
 * Üstte durum özeti (Provider adı, bağlantı rozeti, son sync zamanı).
 * Sağlayıcıya özel API ayar formu.
 * Connect / Disconnect butonları net şekilde ayrı renklerde (Primary vs Error Outline).
 */
@Composable
fun OTAConnectionDetailScreen(
    viewModel: OTAHubViewModel,
    providerId: String = "viator",
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var isConnected by remember { mutableStateOf(true) }
    var supplierId by remember { mutableStateOf("VIA-MCH-9812") }
    var apiKey by remember { mutableStateOf("pk_live_891273918273645") }
    var apiSecret by remember { mutableStateOf("sk_live_secret_9988112233") }
    var webhookUrl by remember { mutableStateOf("https://api.touros.io/v1/ota/viator/webhook") }
    var syncIntervalMinutes by remember { mutableStateOf("15") }
    var rateMarginPercent by remember { mutableStateOf("12.0") }

    val providerName = when (providerId.lowercase()) {
        "getyourguide" -> "GetYourGuide"
        "booking" -> "Booking.com Experiences"
        "expedia" -> "Expedia Local Expert"
        else -> "Viator / TripAdvisor"
    }

    val providerIcon = when (providerId.lowercase()) {
        "getyourguide" -> "🎯"
        "booking" -> "🏨"
        "expedia" -> "✈️"
        else -> "🌐"
    }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "OTA Bağlantı Detayı",
                subtitle = "$providerName API ve kanal senkronizasyon ayarları",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .padding(TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // Bildirim Mesajı
                if (uiState.syncStatusMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SuccessContainer)
                            .padding(TourOSSpacing.medium)
                    ) {
                        Text(
                            uiState.syncStatusMessage!!,
                            style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                        )
                    }
                }

                // ── 1. ÜSTTE DURUM ÖZETİ KARTI ─────────────────────────────────
                TopStatusSummaryHeader(
                    providerName = providerName,
                    providerIcon = providerIcon,
                    isConnected = isConnected,
                    lastSyncedAt = "10 Dk Önce (18:20)",
                    mappedProductCount = 14
                )

                // ── 2. SAĞLAYICIYA ÖZEL AYAR FORMU KARTI ────────────────────────
                ProviderConfigFormCard(
                    supplierId = supplierId,
                    onSupplierIdChange = { supplierId = it },
                    apiKey = apiKey,
                    onApiKeyChange = { apiKey = it },
                    apiSecret = apiSecret,
                    onApiSecretChange = { apiSecret = it },
                    webhookUrl = webhookUrl,
                    onWebhookUrlChange = { webhookUrl = it },
                    syncIntervalMinutes = syncIntervalMinutes,
                    onSyncIntervalChange = { syncIntervalMinutes = it },
                    rateMarginPercent = rateMarginPercent,
                    onRateMarginChange = { rateMarginPercent = it }
                )

                // ── 3. CONNECT / DISCONNECT BUTONLARI (NET AYRI RENKLERDE) ───────
                ConnectDisconnectButtonGroup(
                    isConnected = isConnected,
                    isLoading = uiState.isLoading,
                    onConnectClick = {
                        isConnected = true
                        viewModel.connectChannel(
                            OTAAccount(accountId = providerId, providerId = providerId, accountName = providerName, apiKey = apiKey),
                            "tenant-001"
                        )
                    },
                    onDisconnectClick = {
                        isConnected = false
                        viewModel.syncNow(providerId, false, "tenant-001")
                    }
                )
            }
        }
    }
}

// ─── ÜSTTE DURUM ÖZETİ HEADER KARTI ───────────────────────────────────────────

@Composable
private fun TopStatusSummaryHeader(
    providerName: String,
    providerIcon: String,
    isConnected: Boolean,
    lastSyncedAt: String,
    mappedProductCount: Int
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isConnected) TourOSColors.PrimaryContainer.copy(alpha = 0.45f) else TourOSColors.SecondaryContainer.copy(alpha = 0.35f),
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
                    Text(providerIcon, style = TourOSTypography.DisplaySmall)
                    Column {
                        Text(
                            providerName,
                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                        )
                        Text(
                            "API V2 Integration Engine",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }

                // BAĞLANTI DURUMU ROZETİ (Success/Error Strict Rule)
                TourOSStatusBadge(
                    text = if (isConnected) "✅ BAĞLANTI AKTİF" else "❌ BAĞLANTI KESİLDİ",
                    backgroundColor = if (isConnected) TourOSColors.SuccessContainer else TourOSColors.SecondaryContainer,
                    textColor = if (isConnected) TourOSColors.Success else TourOSColors.Secondary
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Son Başarılı Senkronizasyon:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(lastSyncedAt, style = TourOSTypography.Label.copy(color = TourOSColors.Primary))
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Eşleşen Tur / Hizmet:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("$mappedProductCount Aktif Ürün", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                }
            }
        }
    }
}

// ─── SAĞLAYICIYA ÖZEL AYAR FORMU KARTI ────────────────────────────────────────

@Composable
private fun ProviderConfigFormCard(
    supplierId: String,
    onSupplierIdChange: (String) -> Unit,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    apiSecret: String,
    onApiSecretChange: (String) -> Unit,
    webhookUrl: String,
    onWebhookUrlChange: (String) -> Unit,
    syncIntervalMinutes: String,
    onSyncIntervalChange: (String) -> Unit,
    rateMarginPercent: String,
    onRateMarginChange: (String) -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            Text(
                "⚙️ Sağlayıcı API Yapılandırma Formu",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )

            TourOSTextField(
                value = supplierId,
                onValueChange = onSupplierIdChange,
                label = "Supplier / Merchant ID",
                placeholder = "VIA-MCH-9812",
                modifier = Modifier.fillMaxWidth()
            )

            TourOSTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = "API Key / Public Key",
                placeholder = "pk_live_...",
                modifier = Modifier.fillMaxWidth()
            )

            TourOSTextField(
                value = apiSecret,
                onValueChange = onApiSecretChange,
                label = "API Secret / Private Token",
                placeholder = "sk_live_...",
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            TourOSTextField(
                value = webhookUrl,
                onValueChange = onWebhookUrlChange,
                label = "Webhook Callback Notification URL",
                placeholder = "https://api.touros.io/...",
                modifier = Modifier.fillMaxWidth()
            )

            // Otomatik Sync Sıklığı Seçimi
            Text("Otomatik Senkronizasyon Sıklığı:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("15" to "15 Dk", "30" to "30 Dk", "60" to "1 Saat", "MANUAL" to "Manuel").forEach { (valKey, labelText) ->
                    val isSelected = syncIntervalMinutes == valKey
                    OutlinedButton(
                        onClick = { onSyncIntervalChange(valKey) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                        colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(
                            labelText,
                            style = TourOSTypography.Caption.copy(
                                color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                            )
                        )
                    }
                }
            }

            TourOSTextField(
                value = rateMarginPercent,
                onValueChange = onRateMarginChange,
                label = "Fiyat Marjı / Surge Oranı (%)",
                placeholder = "12.0",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── CONNECT / DISCONNECT BUTONLARI (NET AYRI RENKLERDE) ─────────────────────

@Composable
private fun ConnectDisconnectButtonGroup(
    isConnected: Boolean,
    isLoading: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        // CONNECT / BAĞLAN BUTONU (Primary Viyole Renk) (Strict Rule)
        TourOSButton(
            text = if (isLoading) "İşleniyor..." else "🔌 Kanala Bağlan & Yetkilendir",
            onClick = onConnectClick,
            enabled = !isLoading && !isConnected,
            variant = TourOSButtonVariant.PRIMARY,
            modifier = Modifier.weight(1f)
        )

        // DISCONNECT / KES BUTONU (Kırmızı Error Outlined Renk) (Strict Rule)
        OutlinedButton(
            onClick = onDisconnectClick,
            enabled = !isLoading && isConnected,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TourOSColors.Secondary,
                containerColor = TourOSColors.SecondaryContainer.copy(alpha = 0.3f)
            ),
            border = ButtonDefaults.outlinedToolboxBorder(color = TourOSColors.Secondary)
        ) {
            Text(
                "⚠️ Bağlantıyı Kes",
                style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
            )
        }
    }
}

@Composable
private fun ButtonDefaults.outlinedToolboxBorder(color: Color) = androidx.compose.foundation.BorderStroke(1.5.dp, color)
