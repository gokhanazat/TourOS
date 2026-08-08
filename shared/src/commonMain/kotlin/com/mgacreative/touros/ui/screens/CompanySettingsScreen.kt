package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.CompanySettings
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSSnackbarHost
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.CompanySettingsUiState
import com.mgacreative.touros.ui.viewmodel.CompanySettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

enum class SettingsCategory(val title: String, val icon: String) {
    GENEL("Genel", "🏢"),
    MARKA("Marka", "🎨"),
    WEB_SITESI("Acente Web Sitesi", "🖥️"),
    VERGI_SEZON("Vergi / Sezon", "📊"),
    DIL_PARA_BIRIMI("Dil / Para Birimi", "🌐")
}

/**
 * TourOS Şirket Ayarları Ekranı.
 * Firma bilgileri, marka görsel dili, web sitesi header resmi, logo yükleme ve kaydetme işlemleri.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompanySettingsScreen(
    companyId: String = "00000000-0000-0000-0000-000000000001",
    viewModel: CompanySettingsViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedCategory by remember { mutableStateOf(SettingsCategory.WEB_SITESI) }

    LaunchedEffect(companyId) {
        viewModel.loadSettings(companyId)
    }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Şirket & Web Sayfası Ayarları",
                subtitle = "Firma bilgileri, acente web sitesi logo/header resmi ve sistem tercihlerini yönetin"
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
            when (uiState) {
                is CompanySettingsUiState.Loading, is CompanySettingsUiState.Idle -> {
                    TourOSLoadingIndicator(message = "Ayarlar yükleniyor...")
                }
                is CompanySettingsUiState.Error -> {
                    val errorMsg = (uiState as CompanySettingsUiState.Error).message
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = errorMsg, style = TourOSTypography.BodyLarge.copy(color = TourOSColors.Error))
                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                        TourOSButton(
                            text = "Tekrar Deneyin",
                            onClick = { viewModel.loadSettings(companyId) },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }
                }
                is CompanySettingsUiState.Success, is CompanySettingsUiState.Saving -> {
                    val settings = when (uiState) {
                        is CompanySettingsUiState.Success -> (uiState as CompanySettingsUiState.Success).settings
                        else -> CompanySettings(id = companyId, name = "Anatolia Travel Operasyon")
                    }

                    // ── FORM STATE HOISTING (Tüm kategoriler için ortak durum yönetimi) ──────
                    var name by remember(settings) { mutableStateOf(settings.name) }
                    var themeColor by remember(settings) { mutableStateOf(settings.themeColor.ifBlank { "#1F4E5F" }) }
                    var taxRateStr by remember(settings) { mutableStateOf(settings.taxRate.toString()) }
                    var selectedCurrencies by remember(settings) { mutableStateOf(settings.supportedCurrencies.toSet()) }
                    var selectedLanguages by remember(settings) { mutableStateOf(settings.supportedLanguages.toSet()) }
                    var seasons by remember(settings) { mutableStateOf(settings.seasons) }

                    // Web & Marka State'leri
                    var webCustomLogo by remember(settings) { mutableStateOf(settings.logoUrl ?: "") }
                    var webHeaderImage by remember(settings) { mutableStateOf(settings.headerImageUrl ?: "") }
                    var headerImageError by remember { mutableStateOf<String?>(null) }
                    var logoImageError by remember { mutableStateOf<String?>(null) }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(TourOSSpacing.large),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                    ) {
                        // ── SOL PANEL: KATEGORİ MENÜSÜ ──────────────────────────────────────
                        TourOSCard(
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(TourOSSpacing.medium)
                            ) {
                                Text(
                                    text = "Ayar Kategorileri",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                )
                                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                SettingsCategory.entries.forEach { category ->
                                    val isSelected = category == selectedCategory
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                            .background(
                                                if (isSelected) TourOSColors.PrimaryContainer
                                                else TourOSColors.Surface
                                            )
                                            .clickable { selectedCategory = category }
                                            .padding(
                                                horizontal = TourOSSpacing.medium,
                                                vertical = TourOSSpacing.small
                                            )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                        ) {
                                            Text(text = category.icon, style = TourOSTypography.TitleMedium)
                                            Text(
                                                text = category.title,
                                                style = TourOSTypography.BodyMedium.copy(
                                                    color = if (isSelected) TourOSColors.Primary else TourOSColors.TextPrimary
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                }
                            }
                        }

                        // ── SAĞ PANEL: FORM VE GÜNCELLEME İÇERİĞİ ────────────────────────────
                        TourOSCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(TourOSSpacing.large)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                when (selectedCategory) {
                                    SettingsCategory.GENEL -> {
                                        Text(text = "Genel Şirket Bilgileri", style = TourOSTypography.TitleLarge)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                                        TourOSTextField(
                                            value = name,
                                            onValueChange = { name = it },
                                            label = "Firma Ticari Unvanı",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    SettingsCategory.MARKA -> {
                                        Text(text = "Marka ve Kurumsal Renkler", style = TourOSTypography.TitleLarge)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                                        TourOSTextField(
                                            value = themeColor,
                                            onValueChange = { themeColor = it },
                                            label = "Ana Kurumsal Tema Rengi (Hex)",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    SettingsCategory.WEB_SITESI -> {
                                        Text(
                                            text = "Acente Web Sayfası Ayarları",
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Text(
                                            text = "Web sitenizin üst bölümünde görünecek Özel Logo ve Header Banner görsellerini yükleyin.",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        // ── 1. ÖZEL LOGO YÜKLEME ALANI (SOL ÜST LOGO) ──────────────
                                        Text(
                                            text = "1. Özel Logo Görseli (Sol Üst Köşe Header Logosu)",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = webCustomLogo,
                                                    onValueChange = { input ->
                                                        webCustomLogo = input
                                                        logoImageError = null
                                                    },
                                                    label = "Logo Resim URL / Dosya Yolu"
                                                )
                                            }

                                            TourOSButton(
                                                text = "🖼️ Logoyu Dosyadan Seç (Max 1MB)",
                                                onClick = {
                                                    logoImageError = null
                                                    webCustomLogo = "https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=300"
                                                },
                                                variant = TourOSButtonVariant.SECONDARY
                                            )
                                        }
                                        if (logoImageError != null) {
                                            Text(text = logoImageError!!, style = TourOSTypography.Caption, color = TourOSColors.Error)
                                        } else {
                                            Text(text = "📌 Özel logo PNG/SVG şeffaf formatta maksimum 1 MB olmalıdır.", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        // ── 2. HEADER BANNER RESMİ YÜKLEME ALANI (MAKS 1 MB) ───────
                                        Text(
                                            text = "2. Header Banner Görseli (Web Sitesi Üst Arka Plan Resmi)",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = webHeaderImage,
                                                    onValueChange = { input ->
                                                        webHeaderImage = input
                                                        headerImageError = null
                                                    },
                                                    label = "Header Banner Resim URL / Dosya Yolu"
                                                )
                                            }

                                            TourOSButton(
                                                text = "🖼️ Banner'ı Dosyadan Seç (Max 1MB)",
                                                onClick = {
                                                    headerImageError = null
                                                    webHeaderImage = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1600"
                                                },
                                                variant = TourOSButtonVariant.SECONDARY
                                            )
                                        }

                                        if (headerImageError != null) {
                                            Text(text = headerImageError!!, style = TourOSTypography.Caption, color = TourOSColors.Error)
                                        } else {
                                            Text(text = "📌 Header banner çözünürlüğü 1600x400 px, maksimum 1 MB olmalıdır.", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
                                        }
                                    }

                                    SettingsCategory.VERGI_SEZON -> {
                                        Text(text = "Vergi ve Sezon Ayarları", style = TourOSTypography.TitleLarge)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                                        TourOSTextField(
                                            value = taxRateStr,
                                            onValueChange = { taxRateStr = it },
                                            label = "Varsayılan KDV Oranı (%)",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    SettingsCategory.DIL_PARA_BIRIMI -> {
                                        Text(text = "Dil ve Para Birimi Tercihleri", style = TourOSTypography.TitleLarge)
                                    }
                                }

                                Spacer(modifier = Modifier.height(TourOSSpacing.xxLarge))
                                HorizontalDivider(color = TourOSColors.Divider)
                                Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                // ── GÜNCELLEME VE KAYDETME BUTONU ─────────────────────────────────
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TourOSButton(
                                        text = "Değişiklikleri Kaydet ✓",
                                        onClick = {
                                            val updated = settings.copy(
                                                name = name,
                                                logoUrl = webCustomLogo.ifBlank { null },
                                                headerImageUrl = webHeaderImage.ifBlank { null },
                                                themeColor = themeColor,
                                                taxRate = taxRateStr.toDoubleOrNull() ?: settings.taxRate,
                                                supportedCurrencies = selectedCurrencies.toList(),
                                                supportedLanguages = selectedLanguages.toList(),
                                                seasons = seasons
                                            )
                                            viewModel.saveSettings(updated)
                                        },
                                        variant = TourOSButtonVariant.PRIMARY,
                                        isLoading = uiState is CompanySettingsUiState.Saving
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
