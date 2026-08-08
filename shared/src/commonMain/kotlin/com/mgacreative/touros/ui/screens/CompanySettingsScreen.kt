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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.CompanySeason
import com.mgacreative.touros.domain.model.CompanySettings
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSDropdown
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSSnackbarHost
import com.mgacreative.touros.ui.components.TourOSTabs
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
 * TourOS 0.3 Tasarım Sistemi ile Yenilenmiş Şirket Ayarları Ekranı.
 * Sol tarafta dikey kategori menüsü, sağ tarafta seçili kategori formu.
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
                title = "Şirket Ayarları",
                subtitle = "Firma bilgileri, marka görsel dili, sezon ve sistem tercihlerini yönetin"
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
                        Text(
                            text = errorMsg,
                            style = TourOSTypography.BodyLarge.copy(color = TourOSColors.Error)
                        )
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

                    var name by remember(settings) { mutableStateOf(settings.name) }
                    var legalTitle by remember(settings) { mutableStateOf(settings.name + " Turizm A.Ş.") }
                    var themeColor by remember(settings) { mutableStateOf(settings.themeColor.ifBlank { "#1F4E5F" }) }
                    var taxRateStr by remember(settings) { mutableStateOf(settings.taxRate.toString()) }
                    var selectedCurrencies by remember(settings) { mutableStateOf(settings.supportedCurrencies.toSet()) }
                    var selectedLanguages by remember(settings) { mutableStateOf(settings.supportedLanguages.toSet()) }
                    var seasons by remember(settings) { mutableStateOf(settings.seasons) }

                    // Responsive Split View (Sol: Kategoriler, Sağ: Form)
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(TourOSSpacing.large)
                    ) {
                        // Sol Menü / Kategori Listesi (Fixed 240dp on desktop/tablet)
                        Column(
                            modifier = Modifier
                                .width(240.dp)
                                .padding(end = TourOSSpacing.large),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            Text(
                                text = "KATEGORİLER",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

                            SettingsCategory.entries.forEach { category ->
                                val isSelected = selectedCategory == category
                                val rowBg = if (isSelected) TourOSColors.PrimaryContainer else Color.Transparent
                                val txtColor = if (isSelected) TourOSColors.Primary else TourOSColors.TextPrimary

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                        .background(rowBg)
                                        .clickable { selectedCategory = category }
                                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.medium),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = category.icon, style = TourOSTypography.TitleMedium)
                                    Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                    Text(
                                        text = category.title,
                                        style = TourOSTypography.TitleMedium.copy(color = txtColor)
                                    )
                                }
                            }
                        }

                        // Dikey Bölme Çizgisi
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxSize()
                                .background(TourOSColors.Border)
                        )

                        Spacer(modifier = Modifier.width(TourOSSpacing.large))

                        // Sağ Taraf: Form İçeriği
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                        ) {
                            TourOSCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = TourOSColors.Background,
                                borderColor = TourOSColors.Border,
                                contentPadding = TourOSSpacing.xLarge
                            ) {
                                when (selectedCategory) {
                                    SettingsCategory.GENEL -> {
                                        Text(
                                            text = "Genel Firma Bilgileri",
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Text(
                                            text = "Fatura ve resmi yazışmalarda görünecek kurum bilgileri.",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        TourOSTextField(
                                            value = name,
                                            onValueChange = { name = it },
                                            label = "Firma Ünvanı / Marka Adı",
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

                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            TourOSTextField(
                                                value = "Karaköy V.D.",
                                                onValueChange = {},
                                                label = "Vergi Dairesi",
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                            TourOSTextField(
                                                value = "9876543210",
                                                onValueChange = {},
                                                label = "Vergi Numarası",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    SettingsCategory.MARKA -> {
                                        Text(
                                            text = "Marka Kimliği & Logo",
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Text(
                                            text = "Sistem raporları ve voucher belgelerinde görünecek amblem ve renkler.",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        // Logo Yükleme Alanı (Kare önizleme + Sürükle-Bırak)
                                        Text(
                                            text = "Kurumsal Logo",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                                        ) {
                                            // Kare Önizleme Kartı
                                            Box(
                                                modifier = Modifier
                                                    .size(110.dp)
                                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                                                    .background(TourOSColors.PrimaryContainer)
                                                    .border(TourOSSpacing.borderWidth, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadius)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "LOGO",
                                                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                                                )
                                            }

                                            // Sürükle - Bırak Yükleme Alanı
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(110.dp)
                                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                                                    .background(TourOSColors.Surface)
                                                    .border(
                                                        width = TourOSSpacing.borderWidth,
                                                        color = TourOSColors.Primary,
                                                        shape = RoundedCornerShape(TourOSSpacing.cornerRadius)
                                                    )
                                                    .clickable { /* Upload File */ }
                                                    .padding(TourOSSpacing.medium),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(text = "📁 Dosya Seç veya Sürükleyip Bırakın", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                                                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                                    Text(text = "PNG, JPG veya SVG (Maksimum 2MB)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        // Kurumsal Tema Rengi
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TourOSTextField(
                                                value = themeColor,
                                                onValueChange = { themeColor = it },
                                                label = "Ana Kurumsal Rengi (Hex)",
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
                                        var webHeroTitle by remember { mutableStateOf(settings.name.ifEmpty { "Hayalinizdeki Turu Keşfedin" }) }
                                        var webHeroSubtitle by remember { mutableStateOf("En iyi tur operatörlerinden karşılaştırmalı teklifler ve fırsatlar") }
                                        var webContactPhone by remember { mutableStateOf("0850 300 00 00") }
                                        var webCustomLogo by remember { mutableStateOf(settings.logoUrl ?: "") }
                                        var webHeaderImage by remember(settings) { mutableStateOf(settings.headerImageUrl ?: "") }
                                        var headerImageError by remember { mutableStateOf<String?>(null) }
                                        var webFooterText by remember { mutableStateOf("© 2026 Tüm Hakları Saklıdır") }

                                        Text(
                                            text = "Acente Web Sayfası Ayarları (Sletat.ru Konsepti)",
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Text(
                                            text = "Müşterilerinize sunulan web sitesinin başlığı, header görseli, logo, iletişim ve ürün yayınlama ayarlarını düzenleyin.",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        // ── Header Banner Resmi Seçimi (Maks 1 MB) ──────────────────
                                        Text(
                                            text = "Web Sitesi Header Banner Görseli",
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
                                                    label = "Header Resim URL / Dosya Yolu"
                                                )
                                            }

                                            TourOSButton(
                                                text = "🖼️ Dosyadan Seç (Max 1MB)",
                                                onClick = {
                                                    // 1 MB Dosya Boyut Kontrolü (Simülasyon / Doğrulama)
                                                    headerImageError = null
                                                    webHeaderImage = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1600"
                                                },
                                                variant = TourOSButtonVariant.SECONDARY
                                            )
                                        }

                                        if (headerImageError != null) {
                                            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                            Text(
                                                text = headerImageError!!,
                                                style = TourOSTypography.Caption,
                                                color = TourOSColors.Error
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                            Text(
                                                text = "📌 İpucu: Seçilecek görsel boyutu maksimum 1 MB olmalıdır. (JPG, PNG, WEBP)",
                                                style = TourOSTypography.Caption,
                                                color = TourOSColors.TextSecondary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

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

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = webContactPhone,
                                                    onValueChange = { webContactPhone = it },
                                                    label = "Müşteri Destek Telefonu"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = webCustomLogo,
                                                    onValueChange = { webCustomLogo = it },
                                                    label = "Özel Logo URL"
                                                )
                                            }
                                        }

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
                                        Text(
                                            text = "Vergi Oranları ve Operasyonel Sezonlar",
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Text(
                                            text = "Otomatik fiyatlama ve KDV hesaplama kuralları.",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        TourOSTextField(
                                            value = taxRateStr,
                                            onValueChange = { taxRateStr = it },
                                            label = "Varsayılan KDV Oranı (%)",
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(
                                            text = "Tanımlı Sezonlar",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                        )

                                        seasons.forEach { season ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
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
                                                    Text(text = season.name, style = TourOSTypography.TitleMedium)
                                                    Text(text = "${season.startDate} - ${season.endDate}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                                                }
                                            }
                                        }
                                    }

                                    SettingsCategory.DIL_PARA_BIRIMI -> {
                                        Text(
                                            text = "Dil ve Para Birimi Tercihleri",
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                        )
                                        Text(
                                            text = "Acente panelinde aktif desteklenecek para birimleri ve diller.",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(text = "Desteklenen Para Birimleri", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                        ) {
                                            val allCurrencies = listOf("TRY (₺)", "EUR (€)", "USD ($)", "GBP (£)", "AED", "SAR")
                                            allCurrencies.forEach { curr ->
                                                val code = curr.take(3)
                                                val isSelected = code in selectedCurrencies
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        selectedCurrencies = if (isSelected) selectedCurrencies - code else selectedCurrencies + code
                                                    },
                                                    label = { Text(curr, style = TourOSTypography.BodyMedium) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = TourOSColors.PrimaryContainer,
                                                        selectedLabelColor = TourOSColors.Primary
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                        Text(text = "Desteklenen Panel Dilleri", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                        Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                        ) {
                                            val allLanguages = mapOf("tr" to "Türkçe", "en" to "English", "de" to "Deutsch", "ar" to "العربية")
                                            allLanguages.forEach { (code, langName) ->
                                                val isSelected = code in selectedLanguages
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        selectedLanguages = if (isSelected) selectedLanguages - code else selectedLanguages + code
                                                    },
                                                    label = { Text(langName, style = TourOSTypography.BodyMedium) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = TourOSColors.PrimaryContainer,
                                                        selectedLabelColor = TourOSColors.Primary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(TourOSSpacing.xxLarge))
                                HorizontalDivider(color = TourOSColors.Divider)
                                Spacer(modifier = Modifier.height(TourOSSpacing.large))

                                // Kaydet Butonu
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TourOSButton(
                                        text = "Değişiklikleri Kaydet ✓",
                                        onClick = {
                                            val updated = settings.copy(
                                                name = name,
                                                themeColor = themeColor,
                                                taxRate = taxRateStr.toDoubleOrNull() ?: settings.taxRate,
                                                supportedCurrencies = selectedCurrencies.toList(),
                                                supportedLanguages = selectedLanguages.toList(),
                                                seasons = seasons,
                                                headerImageUrl = settings.headerImageUrl
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
