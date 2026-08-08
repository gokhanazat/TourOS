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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.OTAHubViewModel

private data class OTASyncLogItem(
    val logId: String,
    val timestamp: String,
    val providerName: String,
    val providerIcon: String,
    val eventName: String,
    val httpStatusCode: Int,
    val isError: Boolean,
    val requestPayloadJson: String,
    val responseBodyJson: String,
    val errorMessage: String? = null
)

private val sampleSyncLogs = listOf(
    OTASyncLogItem(
        logId = "LOG-2026-8801",
        timestamp = "07.08.2026 18:20:14",
        providerName = "Viator / TripAdvisor",
        providerIcon = "🌐",
        eventName = "BOOKING_SYNC",
        httpStatusCode = 400,
        isError = true,
        requestPayloadJson = "{\n  \"event\": \"BOOKING_SYNC\",\n  \"supplierId\": \"VIA-MCH-9812\",\n  \"bookingRef\": \"VIA-99120\"\n}",
        responseBodyJson = "{\n  \"error\": \"INVALID_SIGNATURE\",\n  \"message\": \"API secret signature validation failed for header X-Viator-Sig\"\n}",
        errorMessage = "API secret imza doğrulaması başarısız (HTTP 400)"
    ),
    OTASyncLogItem(
        logId = "LOG-2026-8802",
        timestamp = "07.08.2026 18:18:02",
        providerName = "GetYourGuide",
        providerIcon = "🎯",
        eventName = "AVAILABILITY_UPDATE",
        httpStatusCode = 200,
        isError = false,
        requestPayloadJson = "{\n  \"action\": \"UPDATE_CAPACITY\",\n  \"tourId\": \"t101\",\n  \"availableSlots\": 18\n}",
        responseBodyJson = "{\n  \"status\": \"SUCCESS\",\n  \"syncedSlots\": 18\n}",
        errorMessage = null
    ),
    OTASyncLogItem(
        logId = "LOG-2026-8803",
        timestamp = "07.08.2026 18:12:45",
        providerName = "Booking.com",
        providerIcon = "🏨",
        eventName = "PRICE_UPDATE_PUSH",
        httpStatusCode = 200,
        isError = false,
        requestPayloadJson = "{\n  \"priceRule\": \"HIGH_SEASON\",\n  \"rateAmount\": 2400.00\n}",
        responseBodyJson = "{\n  \"result\": \"ACKNOWLEDGED\",\n  \"updatedAt\": \"2026-08-07T18:12:45Z\"\n}",
        errorMessage = null
    ),
    OTASyncLogItem(
        logId = "LOG-2026-8804",
        timestamp = "07.08.2026 17:55:10",
        providerName = "Expedia Local Expert",
        providerIcon = "✈️",
        eventName = "WEBHOOK_RECEIVE",
        httpStatusCode = 500,
        isError = true,
        requestPayloadJson = "{\n  \"webhookEvent\": \"RESERVATION_CANCELLED\",\n  \"otaBookingId\": \"EXP-77211\"\n}",
        responseBodyJson = "{\n  \"error\": \"INTERNAL_SERVER_ERROR\",\n  \"detail\": \"Database connection timeout while processing cancel notification\"\n}",
        errorMessage = "Sunucu dahili hatası: Veritabanı zaman aşımı (HTTP 500)"
    )
)

/**
 * OTA Log Görüntüleme Ekranı — TourOS 0.3
 *
 * Kronolojik log tablosu.
 * Başarısız kayıtlar Error renkli küçük ikonla işaretli.
 * Satıra tıklayınca detay paneli açılır (Request/Response JSON, Error trace).
 */
@Composable
fun SyncLogsScreen(
    viewModel: OTAHubViewModel,
    providerIdFilter: String = "ALL",
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var logsList by remember { mutableStateOf(sampleSyncLogs) }
    var selectedFilterProvider by remember { mutableStateOf(providerIdFilter) }
    var selectedLogForDetail by remember { mutableStateOf<OTASyncLogItem?>(null) }
    var onlyErrorsFilter by remember { mutableStateOf(false) }

    val filteredLogs = remember(logsList, selectedFilterProvider, onlyErrorsFilter) {
        logsList.filter { log ->
            val providerMatch = selectedFilterProvider == "ALL" || log.providerName.lowercase().contains(selectedFilterProvider.lowercase())
            val errorMatch = !onlyErrorsFilter || log.isError
            providerMatch && errorMatch
        }
    }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "OTA Senkronizasyon Logları",
                subtitle = "API istek, yanıt ve canlı senkronizasyon hata geçmişi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                },
                actions = {
                    TourOSButton(
                        text = "🔄 Logları Yenile",
                        onClick = { viewModel.loadBookings(tenantId = "tenant-001") },
                        variant = TourOSButtonVariant.SECONDARY,
                        modifier = Modifier.padding(end = TourOSSpacing.small)
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
            // ── 1. KANAL & DURUM FİLTRE ÇUBUĞU ───────────────────────────────
            LogFilterBar(
                selectedProvider = selectedFilterProvider,
                onProviderSelect = { selectedFilterProvider = it },
                onlyErrors = onlyErrorsFilter,
                onOnlyErrorsToggle = { onlyErrorsFilter = !onlyErrorsFilter }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📜 Kronolojik Log Kayıtları (${filteredLogs.size})",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )

                TourOSStatusBadge(
                    text = "${filteredLogs.count { it.isError }} Hata Kaydı",
                    backgroundColor = if (filteredLogs.any { it.isError }) TourOSColors.SecondaryContainer else TourOSColors.SuccessContainer,
                    textColor = if (filteredLogs.any { it.isError }) TourOSColors.Secondary else TourOSColors.Success
                )
            }

            // ── 2. KRONOLOJİK LOG TABLOSU ────────────────────────────────────
            if (filteredLogs.isEmpty()) {
                TourOSEmptyState(
                    title = "Kayıtlı Log Bulunmuyor",
                    description = "Seçilen filtrelere uygun herhangi bir senkronizasyon log kaydı bulunamadı.",
                    icon = { Text("📜", style = TourOSTypography.DisplaySmall) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(filteredLogs) { log ->
                        ChronologicalLogRowItem(
                            log = log,
                            onClick = { selectedLogForDetail = log }
                        )
                    }
                }
            }
        }

        // ── 3. SATIRA TIKLAYINCA AÇILAN LOG DETAY PANELİ ────────────────────
        selectedLogForDetail?.let { log ->
            LogDetailModalDialog(
                log = log,
                onDismiss = { selectedLogForDetail = null }
            )
        }
    }
}

// ─── LOG FİLTRE ÇUBUĞU BİLEŞENİ ───────────────────────────────────────────────

@Composable
private fun LogFilterBar(
    selectedProvider: String,
    onProviderSelect: (String) -> Unit,
    onlyErrors: Boolean,
    onOnlyErrorsToggle: () -> Unit
) {
    val providers = listOf(
        "ALL" to "Tüm Kanallar",
        "viator" to "Viator",
        "getyourguide" to "GetYourGuide",
        "booking" to "Booking.com",
        "expedia" to "Expedia"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall),
            modifier = Modifier.weight(1f)
        ) {
            providers.forEach { (code, label) ->
                val isSelected = selectedProvider == code
                OutlinedButton(
                    onClick = { onProviderSelect(code) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                    colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(
                        label,
                        style = TourOSTypography.Caption.copy(
                            color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                        )
                    )
                }
            }
        }

        // SADESE HATALAR FİLTRE ROZETİ
        FilterChip(
            selected = onlyErrors,
            onClick = onOnlyErrorsToggle,
            label = {
                Text(
                    "⚠️ Sadece Hatalar",
                    style = TourOSTypography.Caption.copy(
                        color = if (onlyErrors) TourOSColors.Secondary else TourOSColors.TextSecondary
                    )
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TourOSColors.SecondaryContainer
            )
        )
    }
}

// ─── KRONOLOJİK LOG SATIR İTEMİ (Strict Rule: Başarısız Kayıtlar Error İkonlu) ───

@Composable
private fun ChronologicalLogRowItem(
    log: OTASyncLogItem,
    onClick: () -> Unit
) {
    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = if (log.isError) TourOSColors.SecondaryContainer.copy(alpha = 0.25f) else TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // BAŞARISIZ KAYITLAR ERROR RENKLİ KÜÇÜK İKONLA İŞARETLİ (Strict Rule)
            Text(
                text = if (log.isError) "⚠️" else "✅",
                style = TourOSTypography.TitleLarge
            )

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
                        "${log.providerIcon} ${log.providerName}  ·  ${log.eventName}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                    )

                    // STATUS CODE BADGE
                    TourOSStatusBadge(
                        text = "HTTP ${log.httpStatusCode}",
                        backgroundColor = if (log.isError) TourOSColors.SecondaryContainer else TourOSColors.SuccessContainer,
                        textColor = if (log.isError) TourOSColors.Secondary else TourOSColors.Success
                    )
                }

                if (log.isError && !log.errorMessage.isNullOrBlank()) {
                    Text(
                        "Hata: ${log.errorMessage}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Secondary)
                    )
                } else {
                    Text(
                        "Kanal Senkronizasyon İsteği Başarılı (200 OK)",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                Text(
                    "🕒 ${log.timestamp}  ·  ID: ${log.logId}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            Text("→", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextSecondary))
        }
    }
}

// ─── SATIRA TIKLAYINCA AÇILAN LOG DETAY PANELİ DIALOGU ───────────────────────

@Composable
private fun LogDetailModalDialog(
    log: OTASyncLogItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (log.isError) "⚠️" else "✅", style = TourOSTypography.TitleLarge)
                Text(
                    "Log Detayı: ${log.logId}",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kanal: ${log.providerIcon} ${log.providerName}", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                    Text("Saat: ${log.timestamp}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Event: ${log.eventName}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                    Text("Status: HTTP ${log.httpStatusCode}", style = TourOSTypography.Label.copy(color = if (log.isError) TourOSColors.Secondary else TourOSColors.Success))
                }

                HorizontalDivider(color = TourOSColors.Divider)

                // REQUEST PAYLOAD JSON
                Text("📤 Request Payload JSON:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(Color(0xFF0F172A))
                        .padding(TourOSSpacing.medium)
                ) {
                    Text(
                        log.requestPayloadJson,
                        style = TourOSTypography.Caption.copy(fontFamily = FontFamily.Monospace, color = Color(0xFF38BDF8))
                    )
                }

                // RESPONSE / ERROR STACK TRACE
                Text("📥 Response Body / Error Trace:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(if (log.isError) Color(0xFF450A0A) else Color(0xFF0F172A))
                        .padding(TourOSSpacing.medium)
                ) {
                    Text(
                        log.responseBodyJson,
                        style = TourOSTypography.Caption.copy(
                            fontFamily = FontFamily.Monospace,
                            color = if (log.isError) Color(0xFFFCA5A5) else Color(0xFF4ADE80)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TourOSButton(
                text = "📋 Log'u Kopyala",
                onClick = onDismiss,
                variant = TourOSButtonVariant.PRIMARY
            )
        },
        dismissButton = {
            TourOSButton(
                text = "Kapat",
                onClick = onDismiss,
                variant = TourOSButtonVariant.TERTIARY
            )
        },
        containerColor = TourOSColors.Surface,
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusLarge)
    )
}
