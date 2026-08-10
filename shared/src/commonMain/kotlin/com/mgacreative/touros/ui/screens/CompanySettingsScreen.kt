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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mgacreative.touros.utils.MAX_IMAGE_SIZE_BYTES
import com.mgacreative.touros.utils.onFileDrop
import com.mgacreative.touros.utils.rememberFilePickerLauncher
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
    ODEME_SISTEMLERI("Banka & PayPal", "💳"),
    VERGI_SEZON("Vergi / Sezon", "📊"),
    DIL_PARA_BIRIMI("Dil / Para Birimi", "🌐")
}

/**
 * TourOS Şirket ve Acente Web Sayfası Ayarları Ekranı.
 * Eksiksiz Tüm Dinamik Ayar Alanları ve Görsel Seçici.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompanySettingsScreen(
    companyId: String = "00000000-0000-0000-0000-000000000001",
    viewModel: CompanySettingsViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()

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
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Şirket & Web Sayfası Ayarları"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Acente web sitesi logo, header banner, başlık, slogan ve kurumsal tercihleri yönetin")
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
                    val settings = when (val state = uiState) {
                        is CompanySettingsUiState.Success -> state.settings
                        is CompanySettingsUiState.Saving -> state.settings
                        else -> CompanySettings(id = companyId, name = "")
                    }

                    // ── DİNAMİK FORM DURUM YÖNETİMİ (STATE HOISTING) ─────────────────────────
                    var name by remember(settings.id) { mutableStateOf(settings.name) }
                    var legalTitle by remember(settings.id) { mutableStateOf(settings.legalTitle) }
                    var taxOffice by remember(settings.id) { mutableStateOf(settings.taxOffice) }
                    var taxNumber by remember(settings.id) { mutableStateOf(settings.taxNumber) }
                    var tradeRegistryNo by remember(settings.id) { mutableStateOf(settings.tradeRegistryNo) }
                    var mersisNo by remember(settings.id) { mutableStateOf(settings.mersisNo) }
                    var companyAddress by remember(settings.id) { mutableStateOf(settings.address) }
                    var companyEmail by remember(settings.id) { mutableStateOf(settings.email) }
                    var companyPhone by remember(settings.id) { mutableStateOf(settings.phone) }

                    var themeColor by remember(settings.id) { mutableStateOf(settings.themeColor.ifBlank { "#1F4E5F" }) }
                    var taxRateStr by remember(settings.id) { mutableStateOf(settings.taxRate.toString()) }
                    var selectedCurrencies by remember(settings.id) { mutableStateOf(settings.supportedCurrencies.toSet()) }
                    var selectedLanguages by remember(settings.id) { mutableStateOf(settings.supportedLanguages.toSet()) }
                    var seasons by remember(settings.id) { mutableStateOf(settings.seasons) }

                    LaunchedEffect(settings.id, settings.supportedLanguages) {
                        val activeLang = settings.supportedLanguages.firstOrNull { it.isNotBlank() } ?: "tr"
                        com.mgacreative.touros.ui.localization.AppLanguageManager.setLanguage(activeLang)
                    }

                    // Yeni Sezon Ekleme State'leri
                    var newSeasonName by remember { mutableStateOf("") }
                    var newSeasonStart by remember { mutableStateOf("2026-06-01") }
                    var newSeasonEnd by remember { mutableStateOf("2026-09-30") }

                    // Eksiksiz Tüm Web & Görsel Dinamik Değişiklik State'leri
                    var webCustomLogo by remember(settings.id) { mutableStateOf(settings.logoUrl ?: "") }
                    var webHeaderImage by remember(settings.id) { mutableStateOf(settings.headerImageUrl ?: "") }
                    var webHeroTitle by remember(settings.id) { mutableStateOf(settings.name) }
                    var webHeroSubtitle by remember(settings.id) { mutableStateOf(settings.heroSubtitle) }
                    var webFooterText by remember(settings.id) { mutableStateOf(settings.footerText) }

                    // Acente Web Sayfasına Özel İletişim Bilgileri (Resmi Fatura Bilgilerinden Bağımsız)
                    var webEmail by remember(settings.id) { mutableStateOf(settings.webEmail.ifBlank { settings.email }) }
                    var webPhone by remember(settings.id) { mutableStateOf(settings.webPhone.ifBlank { settings.phone }) }
                    var webWhatsapp by remember(settings.id) { mutableStateOf(settings.webWhatsapp) }
                    var webAddress by remember(settings.id) { mutableStateOf(settings.webAddress.ifBlank { settings.address }) }

                    // Banka & PayPal Ödeme Ayarları
                    var bankName by remember(settings.id) { mutableStateOf(settings.bankName ?: "") }
                    var iban by remember(settings.id) { mutableStateOf(settings.iban ?: "") }
                    var accountHolder by remember(settings.id) { mutableStateOf(settings.accountHolder ?: "") }
                    var paypalEmail by remember(settings.id) { mutableStateOf(settings.paypalEmail ?: "") }
                    var paypalMeUrl by remember(settings.id) { mutableStateOf(settings.paypalMeUrl ?: "") }

                    var isFormInitialized by remember(settings.id) { mutableStateOf(false) }

                    LaunchedEffect(settings.id) {
                        if (!isFormInitialized) {
                            name = settings.name
                            legalTitle = settings.legalTitle
                            taxOffice = settings.taxOffice
                            taxNumber = settings.taxNumber
                            tradeRegistryNo = settings.tradeRegistryNo
                            mersisNo = settings.mersisNo
                            companyAddress = settings.address
                            companyEmail = settings.email
                            companyPhone = settings.phone
                            themeColor = settings.themeColor.ifBlank { "#1F4E5F" }
                            taxRateStr = settings.taxRate.toString()
                            selectedCurrencies = settings.supportedCurrencies.toSet()
                            selectedLanguages = settings.supportedLanguages.toSet()
                            seasons = settings.seasons
                            webCustomLogo = settings.logoUrl ?: ""
                            webHeaderImage = settings.headerImageUrl ?: ""
                            webHeroTitle = settings.name
                            webHeroSubtitle = settings.heroSubtitle
                            webFooterText = settings.footerText
                            webEmail = settings.webEmail.ifBlank { settings.email }
                            webPhone = settings.webPhone.ifBlank { settings.phone }
                            webWhatsapp = settings.webWhatsapp
                            webAddress = settings.webAddress.ifBlank { settings.address }
                            bankName = settings.bankName ?: ""
                            iban = settings.iban ?: ""
                            accountHolder = settings.accountHolder ?: ""
                            paypalEmail = settings.paypalEmail ?: ""
                            paypalMeUrl = settings.paypalMeUrl ?: ""
                            isFormInitialized = true
                        }
                    }

                    LaunchedEffect(settings.logoUrl) {
                        if (!settings.logoUrl.isNullOrBlank()) {
                            webCustomLogo = settings.logoUrl
                        }
                    }

                    LaunchedEffect(settings.headerImageUrl) {
                        if (!settings.headerImageUrl.isNullOrBlank()) {
                            webHeaderImage = settings.headerImageUrl
                        }
                    }

                    var headerImageError by remember { mutableStateOf<String?>(null) }
                    var logoImageError by remember { mutableStateOf<String?>(null) }

                    val logoPickerLauncher = rememberFilePickerLauncher(mimeType = "image/*") { fileName, bytes ->
                        if (bytes.size > MAX_IMAGE_SIZE_BYTES) {
                            logoImageError = "Logo boyutu 1MB'ı aşamaz."
                        } else {
                            logoImageError = null
                            viewModel.uploadLogo(companyId, bytes, fileName)
                        }
                    }

                    val headerPickerLauncher = rememberFilePickerLauncher(mimeType = "image/*") { fileName, bytes ->
                        if (bytes.size > MAX_IMAGE_SIZE_BYTES) {
                            headerImageError = "Header görsel boyutu 1MB'ı aşamaz."
                        } else {
                            headerImageError = null
                            viewModel.uploadHeaderBanner(companyId, bytes, "header_$fileName")
                        }
                    }

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
                                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ayar Kategorileri"),
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
                                                text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(category.title),
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

                        // ── SAĞ PANEL: FORM VE DİNAMİK GÜNCELLEME İÇERİĞİ ────────────────────
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
                                        Text(text = "Genel Firma Bilgileri", style = TourOSTypography.TitleLarge)
                                        Text(text = "Fatura ve resmi yazışmalarda görünecek kurum ve iletişim bilgileri.", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        TourOSTextField(
                                            value = name,
                                            onValueChange = { name = it },
                                            label = "Firma Marka Adı",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        TourOSTextField(
                                            value = legalTitle,
                                            onValueChange = { legalTitle = it },
                                            label = "Resmi Ticari Unvan",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = taxOffice,
                                                    onValueChange = { taxOffice = it },
                                                    label = "Vergi Dairesi"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = taxNumber,
                                                    onValueChange = { taxNumber = it },
                                                    label = "Vergi Numarası"
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = tradeRegistryNo,
                                                    onValueChange = { tradeRegistryNo = it },
                                                    label = "Ticaret Sicil No"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = mersisNo,
                                                    onValueChange = { mersisNo = it },
                                                    label = "MERSİS No"
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = companyEmail,
                                                    onValueChange = { companyEmail = it },
                                                    label = "Kurumsal E-Posta"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = companyPhone,
                                                    onValueChange = { companyPhone = it },
                                                    label = "Telefon Numarası"
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        TourOSTextField(
                                            value = companyAddress,
                                            onValueChange = { companyAddress = it },
                                            label = "Firma Merkez Adresi",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    SettingsCategory.MARKA -> {
                                        Text(text = "Marka ve Kurumsal Renkler", style = TourOSTypography.TitleLarge)
                                        Text(text = "Acente kimliğinizi yansıtan logo ve kurumsal renk teması.", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(text = "Kurumsal Logo", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                                        ) {
                                             Box(
                                                 modifier = Modifier
                                                     .size(110.dp)
                                                     .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                                                     .background(if (webCustomLogo.isNotBlank()) TourOSColors.Surface else TourOSColors.PrimaryContainer)
                                                     .border(TourOSSpacing.borderWidth, if (webCustomLogo.isNotBlank()) TourOSColors.Success else TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadius))
                                                     .padding(TourOSSpacing.xSmall),
                                                 contentAlignment = Alignment.Center
                                             ) {
                                                 if (webCustomLogo.isNotBlank()) {
                                                     val imageModel = remember(webCustomLogo) {
                                                         val trimmed = webCustomLogo.trim()
                                                         if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://")) {
                                                             trimmed
                                                         } else {
                                                             "file://$trimmed"
                                                         }
                                                     }
                                                     AsyncImage(
                                                         model = imageModel,
                                                         contentDescription = "Kurumsal Logo Görseli",
                                                         modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(TourOSSpacing.cornerRadius)),
                                                         contentScale = ContentScale.Fit
                                                     )
                                                 } else {
                                                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                         Text(
                                                             text = "LOGO",
                                                             style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                                                         )
                                                     }
                                                 }
                                             }

                                             Column(modifier = Modifier.weight(1f)) {
                                                 Box(
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .height(110.dp)
                                                         .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                                                         .background(TourOSColors.Surface)
                                                         .border(
                                                             width = TourOSSpacing.borderWidth,
                                                             color = TourOSColors.Primary,
                                                             shape = RoundedCornerShape(TourOSSpacing.cornerRadius)
                                                         )
                                                         .onFileDrop { droppedFiles ->
                                                             droppedFiles.firstOrNull()?.let { filePath ->
                                                                 webCustomLogo = filePath
                                                             }
                                                         }
                                                         .clickable {
                                                             logoPickerLauncher()
                                                         }
                                                         .padding(TourOSSpacing.medium),
                                                     contentAlignment = Alignment.Center
                                                 ) {
                                                     Column(
                                                         horizontalAlignment = Alignment.CenterHorizontally,
                                                         verticalArrangement = Arrangement.Center
                                                     ) {
                                                         Text(text = "📁 Dosyadan Görsel Seçin", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                                                         Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                                         Text(text = "PNG, JPG veya SVG (Maksimum 1MB)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                                     }
                                                 }
                                                 if (webCustomLogo.isNotBlank()) {
                                                     Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                                     Text(
                                                         text = "✓ Seçilen Dosya: ${webCustomLogo.substringAfterLast("/").substringAfterLast("\\")}",
                                                         style = TourOSTypography.Caption.copy(color = TourOSColors.Success, fontWeight = FontWeight.Bold)
                                                     )
                                                 }
                                             }
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(text = "Ana Kurumsal Rengi", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TourOSTextField(
                                                value = themeColor,
                                                onValueChange = { themeColor = it },
                                                label = "Hex Renk Kodu (örn. #1F4E5F)",
                                                modifier = Modifier.weight(1f)
                                            )

                                            val parsedColor = runCatching {
                                                val hex = themeColor.removePrefix("#")
                                                Color(hex.toLong(16) or 0xFF000000)
                                            }.getOrDefault(TourOSColors.Primary)

                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(parsedColor)
                                                    .border(TourOSSpacing.borderWidth, TourOSColors.Border, CircleShape)
                                            )
                                        }
                                    }

                                    SettingsCategory.WEB_SITESI -> {
                                        Text(
                                            text = "Acente Web Sayfası Ayarları (Sletat.ru Mimarisi)",
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Text(
                                            text = "Acente web sitenizin logosunu, header resmini, başlığını, sloganını ve iletişim detaylarını buradan güncelleyin.",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        // ── 1. ÖZEL LOGO YÜKLEME ALANI ─────────────────────────────
                                        Text(
                                            text = "1. Özel Logo Görseli (Sol Üst Header Logosu)",
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
                                                    label = "Logo Dosya Yolu veya Resim URL (Max 1MB)"
                                                )
                                            }

                                            TourOSButton(
                                                text = "📁 Dosyadan Seç",
                                                onClick = {
                                                    logoImageError = null
                                                    logoPickerLauncher()
                                                },
                                                variant = TourOSButtonVariant.SECONDARY
                                            )
                                        }
                                        if (logoImageError != null) {
                                            Text(text = logoImageError!!, style = TourOSTypography.Caption, color = TourOSColors.Error)
                                        } else {
                                            Text(text = "📌 Özel logo PNG/SVG formatında maksimum 1 MB olmalıdır.", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
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
                                                    label = "Header Banner Dosya Yolu veya Resim URL (Max 1MB)"
                                                )
                                            }

                                            TourOSButton(
                                                text = "📁 Dosyadan Seç",
                                                onClick = {
                                                    headerImageError = null
                                                    headerPickerLauncher()
                                                },
                                                variant = TourOSButtonVariant.SECONDARY
                                            )
                                        }

                                        if (headerImageError != null) {
                                            Text(text = headerImageError!!, style = TourOSTypography.Caption, color = TourOSColors.Error)
                                        } else {
                                            Text(text = "📌 Header banner çözünürlüğü 1600x400 px, maksimum 1 MB olmalıdır.", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
                                        }

                                        if (webHeaderImage.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                                                    .border(TourOSSpacing.borderWidth, TourOSColors.Success, RoundedCornerShape(TourOSSpacing.cornerRadius)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val imageModel = remember(webHeaderImage) {
                                                    val trimmed = webHeaderImage.trim()
                                                    if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://")) {
                                                        trimmed
                                                    } else {
                                                        "file://$trimmed"
                                                    }
                                                }
                                                AsyncImage(
                                                    model = imageModel,
                                                    contentDescription = "Header Banner Önizleme",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                            Text(
                                                text = "✓ Aktif Banner Yolu: $webHeaderImage",
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.Success, fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        // ── 3. EKSİKSİZ TÜM DİNAMİK İÇERİK VE İLETİŞİM ALANLARI ─────
                                        Text(
                                            text = "3. Acente Web İletişim & WhatsApp Bilgileri (Web Sayfasına Özel)",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                        Text(
                                            text = "Resmi şirket fatura bilgilerinden bağımsız olarak web sitenizde ve müşteri taleplerinde kullanılacak iletişim bilgileri.",
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = webEmail,
                                                    onValueChange = { webEmail = it },
                                                    label = "Web İletişim E-Postası (Rezervasyon Talepleri Buraya Düşer) *"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = webPhone,
                                                    onValueChange = { webPhone = it },
                                                    label = "Web İletişim Telefon Numarası *"
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = webWhatsapp,
                                                    onValueChange = { webWhatsapp = it },
                                                    label = "WhatsApp Hattı Telefon Numarası (Örn. 905320000000) *"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = webAddress,
                                                    onValueChange = { webAddress = it },
                                                    label = "Web İletişim Merkez Adresi *"
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(
                                            text = "4. Web Sitesi Başlık, Slogan ve Telif Metni",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        TourOSTextField(
                                            value = webHeroTitle,
                                            onValueChange = { webHeroTitle = it },
                                            label = "Web Site Başlığı / Acente Adı",
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        TourOSTextField(
                                            value = webHeroSubtitle,
                                            onValueChange = { webHeroSubtitle = it },
                                            label = "Web Site Alt Başlığı (Slogan)",
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        TourOSTextField(
                                            value = webFooterText,
                                            onValueChange = { webFooterText = it },
                                            label = "Footer / Telif Hakkı Metni",
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        // Entegrasyon Bilgi Kartı
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                                .background(TourOSColors.PrimaryContainer.copy(alpha = 0.4f))
                                                .border(TourOSSpacing.borderWidth, TourOSColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                                .padding(TourOSSpacing.medium)
                                        ) {
                                            Column {
                                                Text(text = "🌐 OTA & Canlı Ürün Yayınlama Entegrasyonu", style = TourOSTypography.TitleMedium, color = TourOSColors.Primary)
                                                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                                Text(
                                                    text = "OTA Hub ve Pazaryeri kataloğundan 'Yayınla' (is_published=true) olarak işaretlenen tüm turlar, kar marjı hesaplanarak acente web sitenizde canlı olarak görünür.",
                                                    style = TourOSTypography.BodyMedium,
                                                    color = TourOSColors.TextPrimary
                                                )
                                            }
                                        }
                                    }

                                    SettingsCategory.VERGI_SEZON -> {
                                        Text(text = "Vergi Oranları ve Operasyonel Sezonlar", style = TourOSTypography.TitleLarge)
                                        Text(text = "Otomatik fiyatlama, KDV hesaplama ve sezonsal çalışma kuralları.", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        TourOSTextField(
                                            value = taxRateStr,
                                            onValueChange = { taxRateStr = it },
                                            label = "Varsayılan KDV Oranı (%)",
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(text = "Tanımlı Operasyonel Sezonlar", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        if (seasons.isEmpty()) {
                                            Text(text = "Henüz tanımlanmış bir sezon bulunmamaktadır.", style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                                        } else {
                                            seasons.forEach { season ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = TourOSSpacing.small)
                                                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                                        .background(TourOSColors.Surface)
                                                        .border(TourOSSpacing.borderWidth, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                                        .padding(TourOSSpacing.medium)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(text = season.name, style = TourOSTypography.TitleMedium)
                                                            Text(text = "${season.startDate} - ${season.endDate}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                                                        }
                                                        TourOSButton(
                                                            text = "Sil",
                                                            onClick = { seasons = seasons.filter { it.id != season.id } },
                                                            variant = TourOSButtonVariant.SECONDARY
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(text = "Yeni Sezon Ekle", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = newSeasonName,
                                                    onValueChange = { newSeasonName = it },
                                                    label = "Sezon Adı (örn. Yaz 2026)"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = newSeasonStart,
                                                    onValueChange = { newSeasonStart = it },
                                                    label = "Başlangıç Tarihi"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = newSeasonEnd,
                                                    onValueChange = { newSeasonEnd = it },
                                                    label = "Bitiş Tarihi"
                                                )
                                            }
                                            TourOSButton(
                                                text = "➕ Ekle",
                                                onClick = {
                                                    if (newSeasonName.isNotBlank()) {
                                                        val newSeason = com.mgacreative.touros.domain.model.CompanySeason(
                                                            id = "season_${seasons.size + 1}",
                                                            name = newSeasonName,
                                                            startDate = newSeasonStart,
                                                            endDate = newSeasonEnd
                                                        )
                                                        seasons = seasons + newSeason
                                                        newSeasonName = ""
                                                    }
                                                },
                                                variant = TourOSButtonVariant.PRIMARY
                                            )
                                        }
                                    }

                                     SettingsCategory.ODEME_SISTEMLERI -> {
                                         Text(
                                             text = "💳 Banka & PayPal Ödeme Ayarları",
                                             style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                         )
                                         Text(
                                             text = "Müşterilerinizin havale/EFT ve PayPal ile ödeme yapabilmesi için banka ve PayPal hesap bilgilerinizi girin.",
                                             style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                         )

                                         Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                         Text(text = "🏦 Banka Hesap Bilgileri (Havale / EFT)", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                         Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                         TourOSTextField(
                                             value = bankName,
                                             onValueChange = { bankName = it },
                                             label = "Banka Adı & Şube",
                                             placeholder = "Örn: Türkiye İş Bankası - Kadıköy Şubesi",
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                         Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                         TourOSTextField(
                                             value = accountHolder,
                                             onValueChange = { accountHolder = it },
                                             label = "Hesap Sahibi (Alıcı Adı)",
                                             placeholder = "Örn: TourOS Turizm Seyahat A.Ş.",
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                         Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                         TourOSTextField(
                                             value = iban,
                                             onValueChange = { iban = it },
                                             label = "IBAN Numarası",
                                             placeholder = "Örn: TR92 0006 4000 0011 2233 4455 66",
                                             modifier = Modifier.fillMaxWidth()
                                         )

                                         Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))
                                         HorizontalDivider(color = TourOSColors.Divider)
                                         Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                         Text(text = "🅿️ PayPal Ödeme Ayarları", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                         Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                         TourOSTextField(
                                             value = paypalEmail,
                                             onValueChange = { paypalEmail = it },
                                             label = "PayPal Kurumsal E-Posta Adresi",
                                             placeholder = "Örn: payment@acente.com",
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                         Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                         TourOSTextField(
                                             value = paypalMeUrl,
                                             onValueChange = { paypalMeUrl = it },
                                             label = "PayPal.Me Ödeme Bağlantısı (URL)",
                                             placeholder = "Örn: https://paypal.me/acenteisminiz",
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                     }

                                    SettingsCategory.DIL_PARA_BIRIMI -> {
                                        Text(
                                            text = "Dil ve Para Birimi Tercihleri",
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Text(
                                            text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Acente panelinde ve web sitesinde aktif desteklenecek para birimleri ve diller."),
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Desteklenen Para Birimleri"), style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                        ) {
                                            val allCurrencies = listOf("TRY (₺)", "EUR (€)", "USD ($)", "RUB (₽)", "GBP (£)", "AED", "SAR")
                                            allCurrencies.forEach { curr ->
                                                val code = curr.take(3)
                                                val isSelected = code in selectedCurrencies
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        selectedCurrencies = if (isSelected) selectedCurrencies - code else selectedCurrencies + code
                                                    },
                                                    label = {
                                                        Text(
                                                            text = if (isSelected) "✓ $curr" else curr,
                                                            style = TourOSTypography.BodyMedium.copy(
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        )
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = TourOSColors.PrimaryContainer,
                                                        selectedLabelColor = TourOSColors.Primary,
                                                        containerColor = TourOSColors.Surface,
                                                        labelColor = TourOSColors.TextSecondary
                                                    ),
                                                    border = FilterChipDefaults.filterChipBorder(
                                                        enabled = true,
                                                        selected = isSelected,
                                                        borderColor = TourOSColors.Border,
                                                        selectedBorderColor = TourOSColors.Primary,
                                                        borderWidth = if (isSelected) 2.dp else 1.dp
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Desteklenen Panel Dilleri"), style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                        ) {
                                            val allLanguages = mapOf("tr" to "Türkçe", "en" to "English", "ru" to "Русский", "de" to "Deutsch", "ar" to "العربية", "es" to "Español")
                                            allLanguages.forEach { (code, langName) ->
                                                val isSelected = code in selectedLanguages
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        val newSet = if (isSelected) selectedLanguages - code else selectedLanguages + code
                                                        selectedLanguages = newSet
                                                        com.mgacreative.touros.ui.localization.AppLanguageManager.setLanguage(code)
                                                    },
                                                    label = {
                                                        Text(
                                                            text = if (isSelected) "✓ $langName" else langName,
                                                            style = TourOSTypography.BodyMedium.copy(
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        )
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = TourOSColors.PrimaryContainer,
                                                        selectedLabelColor = TourOSColors.Primary,
                                                        containerColor = TourOSColors.Surface,
                                                        labelColor = TourOSColors.TextSecondary
                                                    ),
                                                    border = FilterChipDefaults.filterChipBorder(
                                                        enabled = true,
                                                        selected = isSelected,
                                                        borderColor = TourOSColors.Border,
                                                        selectedBorderColor = TourOSColors.Primary,
                                                        borderWidth = if (isSelected) 2.dp else 1.dp
                                                    )
                                                )
                                            }
                                        }
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
                                                legalTitle = legalTitle,
                                                taxOffice = taxOffice,
                                                taxNumber = taxNumber,
                                                tradeRegistryNo = tradeRegistryNo,
                                                mersisNo = mersisNo,
                                                address = companyAddress,
                                                phone = companyPhone,
                                                email = companyEmail,
                                                logoUrl = webCustomLogo.ifBlank { null },
                                                headerImageUrl = webHeaderImage.ifBlank { null },
                                                heroSubtitle = webHeroSubtitle,
                                                footerText = webFooterText,
                                                webEmail = webEmail,
                                                webPhone = webPhone,
                                                webWhatsapp = webWhatsapp,
                                                webAddress = webAddress,
                                                bankName = bankName.ifBlank { null },
                                                iban = iban.ifBlank { null },
                                                accountHolder = accountHolder.ifBlank { null },
                                                paypalEmail = paypalEmail.ifBlank { null },
                                                paypalMeUrl = paypalMeUrl.ifBlank { null },
                                                themeColor = themeColor,
                                                taxRate = taxRateStr.toDoubleOrNull() ?: settings.taxRate,
                                                supportedCurrencies = selectedCurrencies.toList(),
                                                supportedLanguages = selectedLanguages.toList(),
                                                seasons = seasons
                                            )
                                            viewModel.saveSettings(updated)
                                            val savedLang = selectedLanguages.firstOrNull { it.isNotBlank() } ?: "tr"
                                            com.mgacreative.touros.ui.localization.AppLanguageManager.setLanguage(savedLang)
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
