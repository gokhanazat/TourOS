package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.AppLanguageItem
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.MultiLanguageViewModel

private data class CurrencyOption(val code: String, val name: String, val symbol: String, val flag: String)

private val currencyOptions = listOf(
    CurrencyOption("TRY", "Türk Lirası", "₺", "🇹🇷"),
    CurrencyOption("EUR", "Euro", "€", "🇪🇺"),
    CurrencyOption("USD", "Amerikan Doları", "$", "🇺🇸"),
    CurrencyOption("RUB", "Rus Rublesi", "₽", "🇷🇺"),
    CurrencyOption("GBP", "İngiliz Sterlini", "£", "🇬🇧"),
    CurrencyOption("AED", "BAE Dirhemi", "AED", "🇦🇪"),
    CurrencyOption("SAR", "Suudi Riyali", "SAR", "🇸🇦")
)

/**
 * Dil/Para Birimi Ayarları Ekranı — TourOS 0.3
 *
 * Basit iki dropdown (Dil, Para Birimi) içeren küçük bir ayar kartı.
 * Uygulama genelinde üst bar'daki hızlı erişim menüsünden de ulaşılabilir.
 */
@Composable
fun MultiLanguageScreen(
    viewModel: MultiLanguageViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var selectedLang by remember(state.selectedLanguage) { mutableStateOf(state.selectedLanguage) }
    var selectedCurr by remember { mutableStateOf(currencyOptions.first()) }
    var showQuickAccessMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Dil & Para Birimi Ayarları",
                subtitle = "Uygulama genel yerelleştirme ve para birimi tercihleri",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                },
                actions = {
                    // UYGULAMA GENELİNDE ÜST BAR'DAKİ HIZLI ERİŞİM MENÜSÜ (Strict Rule)
                    Box {
                        TourOSStatusBadge(
                            text = "${selectedLang.flagEmoji} ${selectedLang.code.uppercase()} | ${selectedCurr.symbol} ${selectedCurr.code}",
                            backgroundColor = TourOSColors.PrimaryContainer,
                            textColor = TourOSColors.Primary,
                            modifier = Modifier
                                .padding(end = TourOSSpacing.medium)
                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                .clickable { showQuickAccessMenu = true }
                        )

                        DropdownMenu(
                            expanded = showQuickAccessMenu,
                            onDismissRequest = { showQuickAccessMenu = false },
                            modifier = Modifier.background(TourOSColors.Surface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "⚡ Hızlı Dil & Para Birimi Değiştir",
                                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                                    )
                                },
                                onClick = { showQuickAccessMenu = false }
                            )

                            HorizontalDivider(color = TourOSColors.Divider)

                            currencyOptions.forEach { curr ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${curr.flag} ${curr.name} (${curr.symbol})",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                                        )
                                    },
                                    onClick = {
                                        selectedCurr = curr
                                        showQuickAccessMenu = false
                                    }
                                )
                            }
                        }
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
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(TourOSSpacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // Bildirim Mesajı
                if (state.notificationMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SuccessContainer)
                            .padding(TourOSSpacing.medium)
                    ) {
                        Text(
                            state.notificationMessage!!,
                            style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                        )
                    }
                }

                // ── BASİT İKİ DROPDOWN (DİL, PARA BİRİMİ) İÇEREN KÜÇÜK AYAR KARTI ────────
                LanguageAndCurrencySettingsCard(
                    supportedLanguages = state.supportedLanguages,
                    selectedLanguage = selectedLang,
                    onLanguageSelect = { lang ->
                        selectedLang = lang
                        viewModel.selectLanguage(lang)
                    },
                    currencyOptions = currencyOptions,
                    selectedCurrency = selectedCurr,
                    onCurrencySelect = { curr -> selectedCurr = curr }
                )

                // ── CANLI YERELLEŞTİRME VE PARA BİRİMİ ÖNİZLEME KARTI ────────────────────
                LivePreviewCard(
                    selectedLang = selectedLang,
                    selectedCurr = selectedCurr,
                    translations = state.translations
                )
            }
        }
    }
}

// ─── BASİT İKİ DROPDOWN İÇEREN KÜÇÜK AYAR KARTI BİLEŞENİ ─────────────────────

@Composable
private fun LanguageAndCurrencySettingsCard(
    supportedLanguages: List<AppLanguageItem>,
    selectedLanguage: AppLanguageItem,
    onLanguageSelect: (AppLanguageItem) -> Unit,
    currencyOptions: List<CurrencyOption>,
    selectedCurrency: CurrencyOption,
    onCurrencySelect: (CurrencyOption) -> Unit
) {
    // KÜÇÜK AYAR KARTI (Strict Rule: Derli toplu 480dp kart)
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🌐 Bölgesel Ayarlar",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )

                TourOSStatusBadge(
                    text = "AYAR KARTI",
                    backgroundColor = TourOSColors.PrimaryContainer,
                    textColor = TourOSColors.Primary
                )
            }

            // 1. DROPDOWN: DİL SEÇİMİ (Strict Rule)
            TourOSDropdown(
                items = supportedLanguages,
                selectedItem = selectedLanguage,
                onItemSelected = onLanguageSelect,
                itemLabel = { "${it.flagEmoji} ${it.name}" },
                label = "Uygulama Dili (Language)",
                placeholder = "Dil Seçiniz...",
                modifier = Modifier.fillMaxWidth()
            )

            // 2. DROPDOWN: PARA BİRİMİ SEÇİMİ (Strict Rule)
            TourOSDropdown(
                items = currencyOptions,
                selectedItem = selectedCurrency,
                onItemSelected = onCurrencySelect,
                itemLabel = { "${it.flag} ${it.name} (${it.symbol} ${it.code})" },
                label = "Para Birimi (Currency)",
                placeholder = "Para Birimi Seçiniz...",
                modifier = Modifier.fillMaxWidth()
            )

            TourOSButton(
                text = "💾 Tercihleri Uygula & Kaydet",
                onClick = { },
                variant = TourOSButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── CANLI YERELLEŞTİRME VE PARA BİRİMİ ÖNİZLEME KARTI ────────────────────────

@Composable
private fun LivePreviewCard(
    selectedLang: AppLanguageItem,
    selectedCurr: CurrencyOption,
    translations: Map<String, String>
) {

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.35f),
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "👁️ Canlı Arayüz Önizleme",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )

                TourOSStatusBadge(
                    text = if (selectedLang.isRtl) "🇸🇦 RTL DÜZEN" else "LTR DÜZEN",
                    backgroundColor = TourOSColors.SecondaryContainer,
                    textColor = TourOSColors.Secondary
                )
            }

            Text(
                translations["welcome_title"] ?: "TourOS Seyahat Sistemine Hoş Geldiniz",
                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Örnek Tur Fiyatı:",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
                Text(
                    "${selectedCurr.symbol} ${formatCurrencyPreview(14500.0, selectedCurr.code)}",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )
            }
        }
    }
}

private fun formatCurrencyPreview(amount: Double, code: String): String {
    return when (code) {
        "EUR" -> (amount / 36.0).toInt().toString()
        "USD" -> (amount / 33.0).toInt().toString()
        "GBP" -> (amount / 42.0).toInt().toString()
        else -> (amount).toInt().toString()
    }
}
