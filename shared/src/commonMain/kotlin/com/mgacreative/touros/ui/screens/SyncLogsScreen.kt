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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.ota.OTASyncLog
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.OTAHubViewModel

/**
 * OTA Senkronizasyon Log Görüntüleme Ekranı (Dinamik & Hardcoded Olmayan)
 */
@Composable
fun SyncLogsScreen(
    viewModel: OTAHubViewModel,
    providerIdFilter: String = "ALL",
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedLogForDetail by remember { mutableStateOf<OTASyncLog?>(null) }
    var currentFilter by remember { mutableStateOf(providerIdFilter) }

    LaunchedEffect(currentFilter) {
        viewModel.filterLogs(currentFilter)
    }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Entegrasyon & Senkronizasyon Logları"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("OTA ve webhook veri aktarım hareketleri ve hata kayıtları"),
                onNavigateBack = onNavigateBack,
                actions = {
                    TourOSButton(
                        text = "🔄 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Yenile"),
                        onClick = { viewModel.filterLogs(currentFilter) },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // SOL: LOG TABLOSU VE FİLTRELEME
            Column(
                modifier = Modifier
                    .weight(if (selectedLogForDetail != null) 0.55f else 1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // KANAL FİLTRE BUTONLARI
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "ALL" to ("🌐 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tümü")),
                        "viator" to "Viator",
                        "getyourguide" to "GetYourGuide",
                        "booking" to "Booking.com",
                        "expedia" to "Expedia",
                        "airbnb" to "Airbnb"
                    ).forEach { (fKey, fLabel) ->
                        val isSelected = currentFilter.equals(fKey, ignoreCase = true)
                        OutlinedButton(
                            onClick = { currentFilter = fKey },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                            colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                fLabel,
                                style = TourOSTypography.Caption.copy(
                                    color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                // LOG LİSTESİ KARTI
                TourOSCard(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    backgroundColor = TourOSColors.Surface,
                    contentPadding = TourOSSpacing.medium
                ) {
                    if (uiState.syncLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Seçilen filtrede henüz log kaydı bulunmuyor."), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)
                        ) {
                            items(uiState.syncLogs) { log ->
                                val isSelected = selectedLogForDetail?.logId == log.logId
                                OTASyncLogRowItem(
                                    log = log,
                                    isSelected = isSelected,
                                    onClick = { selectedLogForDetail = log }
                                )
                            }
                        }
                    }
                }
            }

            // SAĞ: SEÇİLİ LOG DETAY PANELİ
            selectedLogForDetail?.let { log ->
                OTASyncLogDetailPanel(
                    log = log,
                    onClose = { selectedLogForDetail = null },
                    modifier = Modifier.weight(0.45f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun OTASyncLogRowItem(
    log: OTASyncLog,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
        color = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.5f) else TourOSColors.SurfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(log.providerIcon, fontSize = 16.sp)
                Column {
                    Text(
                        "${log.providerName} • ${log.eventName}",
                        style = TourOSTypography.Label.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(log.timestamp, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 10.sp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                TourOSStatusBadge(
                    text = "HTTP ${log.httpStatusCode}",
                    backgroundColor = if (log.isError) TourOSColors.SecondaryContainer else TourOSColors.SuccessContainer,
                    textColor = if (log.isError) TourOSColors.Secondary else TourOSColors.Success
                )
            }
        }
    }
}

@Composable
private fun OTASyncLogDetailPanel(
    log: OTASyncLog,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    TourOSCard(
        modifier = modifier,
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Log Ayrıntısı")}: ${log.logId}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                IconButton(onClick = onClose) {
                    Text("✕", style = TourOSTypography.TitleMedium)
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            Text("${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İşlem")}: ${log.eventName}", fontWeight = FontWeight.Bold)
            Text("${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Zaman")}: ${log.timestamp}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
            Text("${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Sağlayıcı")}: ${log.providerName}", style = TourOSTypography.Caption)

            log.errorMessage?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.SecondaryContainer)
                        .padding(TourOSSpacing.small)
                ) {
                    Text("❌ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hata")}: $err", style = TourOSTypography.Caption.copy(color = TourOSColors.Secondary))
                }
            }

            Text("📤 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İstek Payload (JSON)")}:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Surface(
                color = TourOSColors.SurfaceVariant,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    log.requestPayloadJson,
                    modifier = Modifier.padding(8.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            Text("📥 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Yanıt Body (JSON)")}:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Surface(
                color = TourOSColors.SurfaceVariant,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    log.responseBodyJson,
                    modifier = Modifier.padding(8.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }
    }
}
