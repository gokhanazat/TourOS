package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.repository.AuthRepository
import com.mgacreative.touros.domain.repository.CompanySettingsRepository
import com.mgacreative.touros.domain.model.CompanySettings
import com.mgacreative.touros.domain.model.PromoBannerItem
import com.mgacreative.touros.domain.model.ServiceCardItem
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.utils.rememberFilePickerLauncher
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

/**
 * Ana Web Yönetimi & Canlı Önizleme (CMS).
 * Web sitesi footbar ve iletişim bilgileri Acente resmi fatura kayıtlarından %100 BAĞIMSIZDIR.
 * Tüm footbar iletişim, adres ve yasal lisans bilgileri doğrudan bu panelden yönetilir.
 */
@Composable
fun GlobalWebCmsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val authRepository: AuthRepository = koinInject()
    val companySettingsRepository: CompanySettingsRepository = koinInject()
    val currentUser by authRepository.observeAuthState().collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    var saveNotification by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Form State'leri (Web'e Özel Yönetim - Tamamen Dinamik, Hardcoded İçerik Yok)
    var siteTitle by remember { mutableStateOf("") }
    var customLogoUrl by remember { mutableStateOf("") }
    var heroSlogan by remember { mutableStateOf("") }
    var headerImageUrl by remember { mutableStateOf("") }
    var callCenterPhone by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var supportEmail by remember { mutableStateOf("") }
    var webAddress by remember { mutableStateOf("") }
    var webMersisNo by remember { mutableStateOf("") }
    var webTaxOffice by remember { mutableStateOf("") }
    var webTaxNumber by remember { mutableStateOf("") }
    var defaultCommissionMargin by remember { mutableStateOf("% 12.5") }
    var agencyReferralCode by remember { mutableStateOf("") }
    var metaDescription by remember { mutableStateOf("") }
    var footerText by remember { mutableStateOf("") }
    var promoBannersList by remember { mutableStateOf<List<PromoBannerItem>>(emptyList()) }
    var serviceCardsList by remember { mutableStateOf<List<ServiceCardItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
        val settings = companySettingsRepository.getCompanySettings(tid).getOrNull()
        if (settings != null) {
            val effBanners = settings.getEffectivePromoBanners()
            if (effBanners.isNotEmpty()) {
                promoBannersList = effBanners
            }
            serviceCardsList = settings.getEffectiveServiceCards()
            siteTitle = settings.name
            customLogoUrl = settings.logoUrl ?: ""
            heroSlogan = settings.heroSubtitle
            headerImageUrl = settings.headerImageUrl ?: ""
            callCenterPhone = settings.webPhone
            whatsappNumber = settings.webWhatsapp
            supportEmail = settings.webEmail
            webAddress = settings.webAddress
            webMersisNo = settings.webMersisNo
            webTaxOffice = settings.webTaxOffice
            webTaxNumber = settings.webTaxNumber
            footerText = settings.footerText
            agencyReferralCode = settings.defaultMasterAgencyCode ?: ""
        }
    }

    var activePickingSlideIndex by remember { mutableStateOf<Int?>(null) }

    fun formatFilePickerPath(path: String?): String {
        if (path.isNullOrBlank()) return ""
        val trimmed = path.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("file://") -> trimmed
            else -> "file:///$trimmed".replace("\\", "/")
        }
    }

    fun saveAllCmsSettings() {
        coroutineScope.launch {
            isSaving = true
            val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
            val currentSettings = companySettingsRepository.getCompanySettings(tid).getOrNull()
                ?: CompanySettings(id = tid, name = "TourOS Travels")
            
            val updatedSettings = currentSettings.copy(
                name = siteTitle,
                logoUrl = customLogoUrl,
                heroSubtitle = heroSlogan,
                headerImageUrl = headerImageUrl,
                webPhone = callCenterPhone,
                webWhatsapp = whatsappNumber,
                webEmail = supportEmail,
                webAddress = webAddress,
                webMersisNo = webMersisNo,
                webTaxOffice = webTaxOffice,
                webTaxNumber = webTaxNumber,
                footerText = footerText,
                defaultMasterAgencyCode = agencyReferralCode,
                promoBanners = promoBannersList,
                serviceCards = serviceCardsList
            )
            companySettingsRepository.updateCompanySettings(updatedSettings)
            isSaving = false
            saveNotification = "✅ Tüm Web ve Footbar ayarları başarıyla kaydedildi! Canlı sitede anında güncellendi."
        }
    }

    val logoPickerLauncher = rememberFilePickerLauncher(mimeType = "image/*") { fileName, bytes ->
        if (bytes != null && bytes.isNotEmpty()) {
            coroutineScope.launch {
                val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
                companySettingsRepository.uploadLogo(tid, bytes, fileName.ifBlank { "logo.png" })
                    .onSuccess { url ->
                        customLogoUrl = url
                        saveNotification = "✅ Kurumsal Logo görseli Supabase depolamasına yüklendi!"
                    }
                    .onFailure {
                        val formatted = formatFilePickerPath(fileName)
                        customLogoUrl = formatted
                    }
            }
        } else if (!fileName.isNullOrBlank()) {
            val formatted = formatFilePickerPath(fileName)
            customLogoUrl = formatted
        }
    }

    val headerPickerLauncher = rememberFilePickerLauncher(mimeType = "image/*") { fileName, bytes ->
        if (bytes != null && bytes.isNotEmpty()) {
            coroutineScope.launch {
                val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
                companySettingsRepository.uploadHeaderBanner(tid, bytes, fileName.ifBlank { "header.png" })
                    .onSuccess { url ->
                        headerImageUrl = url
                        saveNotification = "✅ Manşet & Hero Slider görseli Supabase bulut deposuna yüklendi!"
                    }
            }
        } else if (!fileName.isNullOrBlank()) {
            val formatted = formatFilePickerPath(fileName)
            headerImageUrl = formatted
        }
    }

    val slidePickerLauncher = rememberFilePickerLauncher(mimeType = "image/*") { fileName, bytes ->
        val idx = activePickingSlideIndex
        if (idx != null) {
            coroutineScope.launch {
                val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
                val uploadedUrlResult = if (bytes != null && bytes.isNotEmpty()) {
                    companySettingsRepository.uploadPromoBannerImage(tid, bytes, fileName.ifBlank { "promo.png" })
                } else {
                    Result.success(formatFilePickerPath(fileName))
                }
                val publicUrl = uploadedUrlResult.getOrNull()
                if (!publicUrl.isNullOrBlank()) {
                    val list = promoBannersList.toMutableList()
                    if (idx in list.indices) {
                        list[idx] = list[idx].copy(imageUrl = publicUrl)
                        promoBannersList = list
                        saveNotification = "✅ Promosyon görseli Supabase bulut deposuna yüklendi!"
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "🌐 Ana Web Yönetimi & Canlı Önizleme (CMS)",
                subtitle = "Sistem Yöneticisi Paneli • Web Sayfası İçerik, Görsel ve Footbar Yönetimi"
            )
        },
        containerColor = TourOSColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 2 Ana Sekmeli Üst Başlık (Tabs)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TourOSColors.Surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val tabs = listOf(
                        "⚙️ Web Sayfası Ayarları (Tüm Bloklar)",
                        "👁️ Canlı Web Önizleme"
                    )

                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TourOSColors.Primary else Color(0xFFE2E8F0))
                                .clickable { selectedTab = index }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = TourOSTypography.TitleMedium.copy(
                                    color = if (isSelected) Color.White else TourOSColors.TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            if (!saveNotification.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF10B981))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        saveNotification ?: "",
                        style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when (selectedTab) {
                    0 -> {
                        // ⚙️ TEK AKICI SAYFADA TÜM CMS AYAR BLOKLARI
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Üst Sabit Kaydet Butonu Banner
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "⚙️ Web Sayfası Yönetim Paneli",
                                            style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                                        )
                                        Text(
                                            "Web footbar ve site ayarları acente resmi kayıtlarından bağımsızdır. Aşağıdan düzenleyip tek tıkla kaydedebilirsiniz.",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    TourOSButton(
                                        text = "Değişiklikleri Kaydet 💾",
                                        onClick = { saveAllCmsSettings() },
                                        variant = TourOSButtonVariant.PRIMARY,
                                        isLoading = isSaving
                                    )
                                }
                            }

                            // BLOK 1: Manşet & Hero Slider
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text("🖼️ 1. Manşet & Hero Slider Yönetimi", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                                    Text("Web sitesinin ana sayfasında ilk girişte görünecek manşet görselini, sloganını ve promosyon slaytlarını ayarlayın.", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = heroSlogan,
                                        onValueChange = { heroSlogan = it },
                                        label = { Text("Hero Slogan Metni") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = headerImageUrl,
                                            onValueChange = { headerImageUrl = it },
                                            label = { Text("Manşet & Hero Slider Görsel URL / Dosya Yolu") },
                                            placeholder = { Text("https://... veya C:/Gorseller/banner.jpg") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = { headerPickerLauncher() },
                                            colors = ButtonDefaults.buttonColors(containerColor = TourOSColors.Primary, contentColor = Color.White)
                                        ) {
                                            Text("📁 Dosya Seç", color = Color.White, style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = TourOSColors.Border)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🛥️ Sol Promosyon Kartı Slider Listesi", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
                                        Button(
                                            onClick = {
                                                val newSlide = PromoBannerItem(
                                                    id = (promoBannersList.size + 1).toString(),
                                                    title = "Yeni Promosyon Turu #${promoBannersList.size + 1}",
                                                    imageUrl = "https://images.unsplash.com/photo-1569263979104-865ab7cd8d13?w=1200",
                                                    targetUrl = null
                                                )
                                                promoBannersList = promoBannersList + newSlide
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TourOSColors.Success, contentColor = Color.White)
                                        ) {
                                            Text("➕ Yeni Slayt Ekle", color = Color.White, style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                        }
                                    }

                                    promoBannersList.forEachIndexed { index, slide ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(1.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Slayt #${index + 1}: ${slide.title}", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
                                                    TextButton(onClick = { promoBannersList = promoBannersList.filterIndexed { i, _ -> i != index } }) {
                                                        Text("🗑️ Sil", color = TourOSColors.Error)
                                                    }
                                                }
                                                OutlinedTextField(
                                                    value = slide.title,
                                                    onValueChange = { newTitle ->
                                                        val list = promoBannersList.toMutableList()
                                                        list[index] = list[index].copy(title = newTitle)
                                                        promoBannersList = list
                                                    },
                                                    label = { Text("Promosyon Başlığı") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    OutlinedTextField(
                                                        value = slide.imageUrl,
                                                        onValueChange = { newImg ->
                                                            val list = promoBannersList.toMutableList()
                                                            list[index] = list[index].copy(imageUrl = newImg)
                                                            promoBannersList = list
                                                        },
                                                        label = { Text("Promosyon Görsel URL / Dosya Yolu") },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Button(
                                                        onClick = {
                                                            activePickingSlideIndex = index
                                                            slidePickerLauncher()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = TourOSColors.Primary, contentColor = Color.White)
                                                    ) {
                                                        Text("📁 Görsel Seç", color = Color.White, style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                                    }
                                                }
                                                OutlinedTextField(
                                                    value = slide.targetUrl ?: "",
                                                    onValueChange = { newLink ->
                                                        val list = promoBannersList.toMutableList()
                                                        list[index] = list[index].copy(targetUrl = newLink.ifBlank { null })
                                                        promoBannersList = list
                                                    },
                                                    label = { Text("Hedef Link URL (Opsiyonel)") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // BLOK 2: Logo & Marka Başlığı
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text("🎨 2. Logo & Kurumsal Marka Ayarları", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                                    Text("Web sitesinin en üst bandındaki marka logosunu, marka ismini ve banner görsellerini düzenleyin.", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = siteTitle,
                                        onValueChange = { siteTitle = it },
                                        label = { Text("Web Sitesi Marka Başlığı / İsmi (Header Üst Bant)") },
                                        placeholder = { Text("Örn: MGA Creative / TourOS Travels") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = customLogoUrl,
                                            onValueChange = { customLogoUrl = it },
                                            label = { Text("Kurumsal Logo Görsel URL / Dosya Yolu") },
                                            placeholder = { Text("https://... veya C:/Gorseller/logo.png") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = { logoPickerLauncher() },
                                            colors = ButtonDefaults.buttonColors(containerColor = TourOSColors.Primary, contentColor = Color.White)
                                        ) {
                                            Text("📁 Logo Seç", color = Color.White, style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                            }

                            // BLOK 3: Fiyatlama & Marj Kuralları
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text("🏷️ 3. Fiyatlama & Marj Kuralları", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                                    Text("Web sitesinde gösterilecek turların varsayılan acente ve müşteri kâr marjları.", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = defaultCommissionMargin,
                                        onValueChange = { defaultCommissionMargin = it },
                                        label = { Text("B2B Varsayılan Komisyon Marjı") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // BLOK 4: Web İletişim & Footbar Adres Bilgileri
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text("📞 4. Web İletişim & Footbar Adres Bilgileri (Acentadan Bağımsız)", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                                    Text("Web sitesi footbarında gösterilecek Kurumsal İletişim ve Adres bilgileri (Acente resmi fatura profilinden bağımsızdır).", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = callCenterPhone,
                                        onValueChange = { callCenterPhone = it },
                                        label = { Text("Web Çağrı Merkezi / Sabit Telefon Numarası") },
                                        placeholder = { Text("Örn: +90 (242) 555 0199") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = whatsappNumber,
                                        onValueChange = { whatsappNumber = it },
                                        label = { Text("WhatsApp Canlı Destek Numarası") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = supportEmail,
                                        onValueChange = { supportEmail = it },
                                        label = { Text("Web Destek E-Posta Adresi") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = webAddress,
                                        onValueChange = { webAddress = it },
                                        label = { Text("Web Footbar Görünecek İletişim Adresi") },
                                        placeholder = { Text("Örn: Lara Cad. No:142, Muratpaşa / Antalya") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // BLOK 5: SEO Bilgileri
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text("🔍 5. SEO & Google Meta Bilgileri", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                                    Text("Google arama motoru indekslemesi ve sosyal medya paylaşım kartları.", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = metaDescription,
                                        onValueChange = { metaDescription = it },
                                        label = { Text("Meta Description (Açıklama)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )
                                }
                            }

                            // BLOK 6: FOOTBAR YASAL & LİSANS BİLGİLERİ (Yalnızca Admin)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🦶 6. Footbar Yasal & Lisans Bilgileri (Acentadan Bağımsız)", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = TourOSColors.Secondary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "🔒 Yalnızca Admin",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                    Text(
                                        "Web footbarında 'Yasal & Lisans' alanında gösterilecek MERSİS, Vergi Dairesi, Vergi No ve Telif Hakkı metni.",
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                    )

                                    OutlinedTextField(
                                        value = webMersisNo,
                                        onValueChange = { webMersisNo = it },
                                        label = { Text("Web Footbar MERSİS Numarası") },
                                        placeholder = { Text("Örn: 0859012345600001") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = webTaxOffice,
                                            onValueChange = { webTaxOffice = it },
                                            label = { Text("Web Footbar Vergi Dairesi") },
                                            placeholder = { Text("Örn: Antalya Kurumlar V.D.") },
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = webTaxNumber,
                                            onValueChange = { webTaxNumber = it },
                                            label = { Text("Web Footbar Vergi Numarası") },
                                            placeholder = { Text("Örn: 8590123456") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = footerText,
                                        onValueChange = { footerText = it },
                                        label = { Text("Footbar Telif Hakkı & Alt Bilgi Dipnot Metni") },
                                        placeholder = { Text("Örn: © 2026 MGA Creative B2B Tur Operatörü. Tüm hakları saklıdır.") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )
                                }
                            }

                            // BLOK 7: HİZMET KARTLARI YÖNETİMİ (6 Adet Görsel Banner Kartı)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text("🎴 7. Hizmet Kartları Yönetimi (6 Adet Görsel Banner Kartı)", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                                    Text("Web ana sayfasında gösterilecek 6 adet dinamik hizmet banner kartı. Her kart için görsel URL, başlık, açıklama ve hedef link belirleyebilirsiniz.", style = TourOSTypography.BodyMedium)

                                    val currentCards: List<ServiceCardItem> = if (serviceCardsList.size >= 6) serviceCardsList.take(6) else {
                                        val defaults = listOf(
                                            ServiceCardItem("1", "Paket Turlar / Tour Packages", "Gezginler için özel seçilmiş her şey dahil paket tur seçenekleri.", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800", "PACKAGE_TOUR"),
                                            ServiceCardItem("2", "Otel Rezervasyonları / Hotel Reservations", "En uygun fiyat garantili seçkin 5 yıldızlı oteller ve tatil köyleri.", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800", "HOTEL"),
                                            ServiceCardItem("3", "Macera Turları / Adventure Tours", "Safari, trekking, kültür turları ve heyecan dolu özel tatil rotaları.", "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=800", "ADVENTURE"),
                                            ServiceCardItem("4", "Seyahat Desteği / Travel Assistance", "Sorunsuz bir seyahat deneyimi için 7/24 canlı müşteri desteği.", "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800", "ASSISTANCE"),
                                            ServiceCardItem("5", "Uçuş Rezervasyonu / Flight Booking", "Hızlı, uygun fiyatlı yurt içi ve yurt dışı charter ve tarifeli uçuşlar.", "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800", "FLIGHT"),
                                            ServiceCardItem("6", "Mavi Yolculuk & Cruise / Cruise Trips", "Lüks cruise gemileri ve büyüleyici koyları keşfedeceğiniz mavi yolculuk.", "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800", "CRUISE")
                                        )
                                        val mutable = serviceCardsList.toMutableList()
                                        for (i in mutable.size until 6) {
                                            mutable.add(defaults[i])
                                        }
                                        mutable
                                    }

                                    for (cardIdx in currentCards.indices) {
                                        val cardItem = currentCards[cardIdx]
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFF1F5F9))
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Kart ${cardIdx + 1}: ${cardItem.title.ifBlank { "Hizmet Kartı" }}",
                                                style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = cardItem.title,
                                                    onValueChange = { newTitle ->
                                                        val newList = currentCards.toMutableList()
                                                        newList[cardIdx] = newList[cardIdx].copy(title = newTitle)
                                                        serviceCardsList = newList
                                                    },
                                                    label = { Text("Kart Başlığı") },
                                                    modifier = Modifier.weight(1f)
                                                )

                                                OutlinedTextField(
                                                    value = cardItem.targetUrl,
                                                    onValueChange = { newTarget ->
                                                        val newList = currentCards.toMutableList()
                                                        newList[cardIdx] = newList[cardIdx].copy(targetUrl = newTarget)
                                                        serviceCardsList = newList
                                                    },
                                                    label = { Text("Tıklama Linki / Kategori") },
                                                    placeholder = { Text("Örn: PACKAGE_TOUR veya https://...") },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            OutlinedTextField(
                                                value = cardItem.subtitle,
                                                onValueChange = { newSub ->
                                                    val newList = currentCards.toMutableList()
                                                    newList[cardIdx] = newList[cardIdx].copy(subtitle = newSub)
                                                    serviceCardsList = newList
                                                },
                                                label = { Text("Açıklama Metni") },
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            OutlinedTextField(
                                                value = cardItem.imageUrl,
                                                onValueChange = { newImg ->
                                                    val newList = currentCards.toMutableList()
                                                    newList[cardIdx] = newList[cardIdx].copy(imageUrl = newImg)
                                                    serviceCardsList = newList
                                                },
                                                label = { Text("Arka Plan Görsel URL / Dosya Yolu") },
                                                placeholder = { Text("https://images.unsplash.com/... veya Supabase Storage URL") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }

                            // Alt Kaydet Butonu
                            TourOSButton(
                                text = "Değişiklikleri Canlı Web Sitesine Kaydet 💾",
                                onClick = { saveAllCmsSettings() },
                                variant = TourOSButtonVariant.PRIMARY,
                                isLoading = isSaving
                            )
                        }
                    }

                    1 -> {
                        // 🖥️ CANLI WEB ÖNİZLEME (LIVE PREVIEW)
                        Box(modifier = Modifier.fillMaxSize()) {
                            GlobalWebPublicScreen(referralCode = agencyReferralCode)
                        }
                    }
                }
            }
        }
    }
}
