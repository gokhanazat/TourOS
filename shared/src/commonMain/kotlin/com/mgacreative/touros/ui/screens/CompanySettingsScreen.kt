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
import androidx.compose.material3.Surface
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import com.mgacreative.touros.ui.viewmodel.CompanySettingsUiState
import com.mgacreative.touros.ui.viewmodel.CompanySettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

enum class SettingsCategory(val title: String, val icon: ImageVector) {
    GENEL("Genel", Icons.Default.Business),
    MARKA("Marka", Icons.Default.Palette),
    ODEME_SISTEMLERI("Banka & PayPal", Icons.Default.CreditCard),
    VERGI_SEZON("Vergi & KDV Oranı", Icons.Default.ReceiptLong),
    DIL_PARA_BIRIMI("Dil / Para Birimi", Icons.Default.Language)
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
    var selectedCategory by remember { mutableStateOf(SettingsCategory.GENEL) }

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
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Şirket Ayarları"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kurumsal firma bilgileri, marka, ödeme sistemleri ve finansal ayarları yönetin")
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
                    var newSeasonCommission by remember { mutableStateOf("12.5") }

                    // Eksiksiz Tüm Web & Görsel Dinamik Değişiklik State'leri
                    var webCustomLogo by remember(settings.id) { mutableStateOf(settings.logoUrl ?: "") }
                    var webHeaderImage by remember(settings.id) { mutableStateOf(settings.headerImageUrl ?: "") }
                    var webHeroTitle by remember(settings.id) { mutableStateOf(settings.name) }
                    var webHeroSubtitle by remember(settings.id) { mutableStateOf(settings.heroSubtitle) }
                    var webFooterText by remember(settings.id) { mutableStateOf(settings.footerText) }
                    var webPromoTitle by remember(settings.id) { mutableStateOf(settings.promoBannerTitle ?: "") }
                    var webPromoImageUrl by remember(settings.id) { mutableStateOf(settings.promoBannerImageUrl ?: "") }
                    var webPromoTargetUrl by remember(settings.id) { mutableStateOf(settings.promoBannerTargetUrl ?: "") }

                    // Acente Web Sayfasına Özel İletişim Bilgileri (Resmi Fatura Bilgilerinden Bağımsız)
                    var webEmail by remember(settings.id) { mutableStateOf(settings.webEmail) }
                    var webPhone by remember(settings.id) { mutableStateOf(settings.webPhone) }
                    var webWhatsapp by remember(settings.id) { mutableStateOf(settings.webWhatsapp) }
                    var webAddress by remember(settings.id) { mutableStateOf(settings.webAddress) }

                    // Banka & PayPal Ödeme Ayarları
                    var bankName by remember(settings.id) { mutableStateOf(settings.bankName ?: "") }
                    var iban by remember(settings.id) { mutableStateOf(settings.iban ?: "") }
                    var accountHolder by remember(settings.id) { mutableStateOf(settings.accountHolder ?: "") }
                    var paypalEmail by remember(settings.id) { mutableStateOf(settings.paypalEmail ?: "") }
                    var paypalMeUrl by remember(settings.id) { mutableStateOf(settings.paypalMeUrl ?: "") }

                    // Dış Müşteri Varsayılan Acente Yönlendirmesi
                    var defaultMasterAgencyId by remember(settings.id) { mutableStateOf(settings.defaultMasterAgencyId ?: "00000000-0000-0000-0000-000000000001") }
                    var defaultMasterAgencyCode by remember(settings.id) { mutableStateOf(settings.defaultMasterAgencyCode ?: "AGN-MASTER") }

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
                            webEmail = settings.webEmail
                            webPhone = settings.webPhone
                            webWhatsapp = settings.webWhatsapp
                            webAddress = settings.webAddress
                            bankName = settings.bankName ?: ""
                            iban = settings.iban ?: ""
                            accountHolder = settings.accountHolder ?: ""
                            paypalEmail = settings.paypalEmail ?: ""
                            paypalMeUrl = settings.paypalMeUrl ?: ""
                            defaultMasterAgencyId = settings.defaultMasterAgencyId ?: "00000000-0000-0000-0000-000000000001"
                            defaultMasterAgencyCode = settings.defaultMasterAgencyCode ?: "AGN-MASTER"
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

                                SettingsCategory.entries.filter { it != SettingsCategory.MARKA }.forEach { category ->
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
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                                    .background(
                                                        if (isSelected) TourOSColors.Primary.copy(alpha = 0.15f)
                                                        else TourOSColors.Surface
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = category.icon,
                                                    contentDescription = category.title,
                                                    tint = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
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
                                        Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Genel Firma Bilgileri"), style = TourOSTypography.TitleLarge)
                                        Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Fatura ve resmi yazışmalarda görünecek kurum ve iletişim bilgileri."), style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        TourOSTextField(
                                            value = name,
                                            onValueChange = { name = it },
                                            label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Firma Marka Adı"),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        TourOSTextField(
                                            value = legalTitle,
                                            onValueChange = { legalTitle = it },
                                            label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Resmi Ticari Unvan"),
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
                                                    label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Vergi Dairesi")
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = taxNumber,
                                                    onValueChange = { taxNumber = it },
                                                    label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Vergi Numarası")
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
                                                    label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ticaret Sicil No")
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = mersisNo,
                                                    onValueChange = { mersisNo = it },
                                                    label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("MERSİS No")
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
                                                    label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kurumsal E-Posta")
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = companyPhone,
                                                    onValueChange = { companyPhone = it },
                                                    label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Telefon Numarası")
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        TourOSTextField(
                                            value = companyAddress,
                                            onValueChange = { companyAddress = it },
                                            label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Firma Merkez Adresi"),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Acente Kodu (B2B SaaS / Master Referans Kodu)"),
                                                    style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFF1F5F9),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                                                ) {
                                                    Text(
                                                        text = "🔒 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Admin Tarafından Kilitlendi"),
                                                        style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            TourOSTextField(
                                                value = defaultMasterAgencyCode,
                                                onValueChange = { /* Kilitli - Değiştirilemez */ },
                                                readOnly = true,
                                                enabled = false,
                                                placeholder = "Örn: ACT-001 / AGN-MASTER-8492",
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Text(
                                                text = "ℹ️ " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Bu acente referans kodu sistem yöneticisi (Admin) tarafından tahsis edilmiştir ve acente tarafından değiştirilemez."),
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                            )
                                        }
                                    }

                                    SettingsCategory.MARKA -> {
                                        Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Marka ve Kurumsal Renkler"), style = TourOSTypography.TitleLarge)
                                        Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Acente kimliğinizi yansıtan logo ve kurumsal renk teması."), style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kurumsal Logo"), style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
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
                                                         contentDescription = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kurumsal Logo Görseli"),
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
                                                         Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("📁 Dosyadan Görsel Seçin"), style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                                                         Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                                         Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("PNG, JPG veya SVG (Maksimum 1MB)"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                                     }
                                                 }
                                                 if (webCustomLogo.isNotBlank()) {
                                                     Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                                     Text(
                                                         text = "✓ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Seçilen Dosya:")} ${webCustomLogo.substringAfterLast("/").substringAfterLast("\\")}",
                                                         style = TourOSTypography.Caption.copy(color = TourOSColors.Success, fontWeight = FontWeight.Bold)
                                                     )
                                                 }
                                             }
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ana Kurumsal Rengi"), style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TourOSTextField(
                                                value = themeColor,
                                                onValueChange = { themeColor = it },
                                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hex Renk Kodu (örn. #1F4E5F)"),
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

                                    SettingsCategory.VERGI_SEZON -> {
                                        Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Vergi & KDV Oranı"), style = TourOSTypography.TitleLarge)
                                        Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Fatura, fiş ve voucher hesaplamalarında geçerli resmi KDV oranını belirleyin."), style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        TourOSTextField(
                                            value = taxRateStr,
                                            onValueChange = { taxRateStr = it },
                                            label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Varsayılan Fatura KDV Oranı (%)"),
                                            placeholder = "20.0",
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        // Operatör Komisyon Bilgilendirme Kartı
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text("ℹ️", style = TourOSTypography.TitleMedium)
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Operatör Komisyonları Hakkında"),
                                                        style = TourOSTypography.TitleSmall.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Bold)
                                                    )
                                                    Text(
                                                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tur operatörleri bazlı komisyon ve net fiyat anlaşmaları, ana menüdeki 'Tur Operatörü > Operatör Bağlantıları' ekranından yönetilmektedir. Buradaki KDV oranı şirketinizin kestiği faturalar ve finansal hesaplamalar için baz alınır."),
                                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                     SettingsCategory.ODEME_SISTEMLERI -> {
                                         Text(
                                             text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("💳 Banka & PayPal Ödeme Ayarları"),
                                             style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                         )
                                         Text(
                                             text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşterilerinizin havale/EFT ve PayPal ile ödeme yapabilmesi için banka ve PayPal hesap bilgilerinizi girin."),
                                             style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                         )


                                         Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                         Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("🏦 Banka Hesap Bilgileri (Havale / EFT)"), style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                         Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                         TourOSTextField(
                                             value = bankName,
                                             onValueChange = { bankName = it },
                                             label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Banka Adı & Şube"),
                                             placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Örn: Türkiye İş Bankası - Kadıköy Şubesi"),
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                         Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                         TourOSTextField(
                                             value = accountHolder,
                                             onValueChange = { accountHolder = it },
                                             label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hesap Sahibi (Alıcı Adı)"),
                                             placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Örn: TourOS Turizm Seyahat A.Ş."),
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                         Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                         TourOSTextField(
                                             value = iban,
                                             onValueChange = { iban = it },
                                             label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("IBAN Numarası"),
                                             placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Örn: TR92 0006 4000 0011 2233 4455 66"),
                                             modifier = Modifier.fillMaxWidth()
                                         )

                                         Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))
                                         HorizontalDivider(color = TourOSColors.Divider)
                                         Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                         Text(text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("🅿️ PayPal Ödeme Ayarları"), style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                         Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                         TourOSTextField(
                                             value = paypalEmail,
                                             onValueChange = { paypalEmail = it },
                                             label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("PayPal Kurumsal E-Posta Adresi"),
                                             placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Örn: payment@acente.com"),
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                         Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                         TourOSTextField(
                                             value = paypalMeUrl,
                                             onValueChange = { paypalMeUrl = it },
                                             label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("PayPal.Me Ödeme Bağlantısı (URL)"),
                                             placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Örn: https://paypal.me/acenteisminiz"),
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                     }

                                    SettingsCategory.DIL_PARA_BIRIMI -> {
                                        Text(
                                            text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Dil ve Para Birimi Tercihleri"),
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Text(
                                            text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Acente panelinde ve web sitesinde aktif desteklenecek para birimleri ve diller."),
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        // ── 1. CANLI ARAYÜZ DİLİ (TEKLİ SEÇİM) ────────────────────
                                        Text(
                                            text = "🌐 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Canlı Panel Arayüz Dili (Anlık Çeviri)")}",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tüm yönetim panelinin anlık olarak görüntüleneceği dili seçiniz (Tekli seçim)."),
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        val activeSystemLanguage = currentLanguage.code
                                        val allLanguages = mapOf("tr" to "Türkçe 🇹🇷", "en" to "English 🇬🇧", "ru" to "Русский 🇷🇺", "de" to "Deutsch 🇩🇪", "ar" to "العربية 🇸🇦", "es" to "Español 🇪🇸")

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                        ) {
                                            allLanguages.forEach { (code, langName) ->
                                                val isActive = activeSystemLanguage == code
                                                FilterChip(
                                                    selected = isActive,
                                                    onClick = {
                                                        com.mgacreative.touros.ui.localization.AppLanguageManager.setLanguage(code)
                                                        if (code !in selectedLanguages) {
                                                            selectedLanguages = selectedLanguages + code
                                                        }
                                                    },
                                                    label = {
                                                        Text(
                                                            text = if (isActive) "✓ $langName (${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Aktif")})" else langName,
                                                            style = TourOSTypography.BodyMedium.copy(
                                                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        )
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = TourOSColors.SuccessContainer,
                                                        selectedLabelColor = TourOSColors.Success,
                                                        containerColor = TourOSColors.Surface,
                                                        labelColor = TourOSColors.TextSecondary
                                                    ),
                                                    border = FilterChipDefaults.filterChipBorder(
                                                        enabled = true,
                                                        selected = isActive,
                                                        borderColor = TourOSColors.Border,
                                                        selectedBorderColor = TourOSColors.Success,
                                                        borderWidth = if (isActive) 2.dp else 1.dp
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))
                                        HorizontalDivider(color = TourOSColors.Divider)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        HorizontalDivider(color = TourOSColors.Divider)
                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                        // ── 3. DESTEKLENEN PARA BİRİMLERİ (ÇOKLU SEÇİM) ────────────
                                        Text(
                                            text = "💵 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Desteklenen Para Birimleri (Çoklu Seçim)")}",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Sistemde ve web sitesinde müşterilere sunulacak satış ve fiyatlama birimleri."),
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
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
                                                        selectedCurrencies = if (isSelected && selectedCurrencies.size > 1) selectedCurrencies - code else selectedCurrencies + code
                                                    },
                                                    label = {
                                                        Text(
                                                            text = if (isSelected) "☑ $curr" else "☐ $curr",
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
                                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Değişiklikleri Kaydet ✓"),
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
                                                promoBannerTitle = webPromoTitle.ifBlank { null },
                                                promoBannerImageUrl = webPromoImageUrl.ifBlank { null },
                                                promoBannerTargetUrl = webPromoTargetUrl.ifBlank { null },
                                                webEmail = webEmail,
                                                webPhone = webPhone,
                                                webWhatsapp = webWhatsapp,
                                                webAddress = webAddress,
                                                bankName = bankName.ifBlank { null },
                                                iban = iban.ifBlank { null },
                                                accountHolder = accountHolder.ifBlank { null },
                                                paypalEmail = paypalEmail.ifBlank { null },
                                                paypalMeUrl = paypalMeUrl.ifBlank { null },
                                                defaultMasterAgencyId = defaultMasterAgencyId.ifBlank { "00000000-0000-0000-0000-000000000001" },
                                                defaultMasterAgencyCode = defaultMasterAgencyCode.ifBlank { "AGN-MASTER" },
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
