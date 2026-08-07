package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.DepartureFormViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Kalkış Ekleme/Düzenleme Ekranı.
 * - Modal / Form düzeninde tarih seçici + fiyat/kontenjan alanları.
 * - 'Tekrarlayan tarih' anahtarı açıldığında ek form alanları dinamik genişleyerek açılır.
 */
@Composable
fun DepartureFormScreen(
    viewModel: DepartureFormViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val daysOfWeekMap = listOf(
        1 to "Pazartesi",
        2 to "Salı",
        3 to "Çarşamba",
        4 to "Perşembe",
        5 to "Cuma",
        6 to "Cumartesi",
        7 to "Pazar"
    )

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Kalkış Tarihi Ekle / Düzenle",
                subtitle = "Sefer bazlı kontenjan, tarih ve özel fiyatlandırma tanımlayın",
                actions = {
                    TourOSButton(
                        text = "Vazgeç",
                        onClick = onNavigateBack,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                }
            )
        },
        containerColor = TourOSColors.Surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(TourOSSpacing.large)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // Seçili Tur Kartı
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.PrimaryContainer,
                borderColor = TourOSColors.Primary.copy(alpha = 0.2f),
                contentPadding = TourOSSpacing.medium
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Seçili Tur", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        Text(text = uiState.tourTitle, style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                    }
                    TourOSStatusBadge(text = "Aktif Katalog", backgroundColor = TourOSColors.Surface, textColor = TourOSColors.Primary)
                }
            }

            // 1. Tarih ve Kontenjan Kartı
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(text = "🗓️ Tarih & Kontenjan Tanımları", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        TourOSTextField(
                            value = uiState.departureDate,
                            onValueChange = { viewModel.updateDepartureDate(it) },
                            label = "Kalkış Tarihi (YYYY-AA-GG)",
                            placeholder = "2026-08-15",
                            modifier = Modifier.weight(1f)
                        )

                        TourOSTextField(
                            value = uiState.returnDate,
                            onValueChange = { viewModel.updateReturnDate(it) },
                            label = "Dönüş Tarihi (YYYY-AA-GG)",
                            placeholder = "2026-08-22",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TourOSTextField(
                        value = uiState.capacity,
                        onValueChange = { viewModel.updateCapacity(it) },
                        label = "Kontenjan (Max Pax Kapasite)",
                        placeholder = "45",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 2. Fiyatlandırma İstisnaları (Price Overrides)
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(text = "💵 Sefer Bazlı Özel Fiyatlar (Override)", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                    Text(text = "Standart tur fiyatlarını sadece bu kalkış tarihi için değiştirebilirsiniz.", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                    ) {
                        TourOSTextField(
                            value = uiState.priceOverride,
                            onValueChange = { viewModel.updatePriceOverride(it) },
                            label = "Yetişkin (₺)",
                            placeholder = "4500",
                            modifier = Modifier.weight(1f)
                        )

                        TourOSTextField(
                            value = uiState.childPriceOverride,
                            onValueChange = { viewModel.updateChildPriceOverride(it) },
                            label = "Çocuk (₺)",
                            placeholder = "3200",
                            modifier = Modifier.weight(1f)
                        )

                        TourOSTextField(
                            value = uiState.infantPriceOverride,
                            onValueChange = { viewModel.updateInfantPriceOverride(it) },
                            label = "Bebek (₺)",
                            placeholder = "800",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Garanti Sefer & Tekrarlayan Tarih Ayarları (Genişleyen Alan)
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    // Kesin Kalkış (Garanti) Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Garanti Sefer (Kesin Kalkış)", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                            Text(text = "Minimum sayı şartı aranmaksızın tur kesin hareket eder", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                        Switch(
                            checked = uiState.isGuaranteed,
                            onCheckedChange = { viewModel.updateIsGuaranteed(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TourOSColors.Surface,
                                checkedTrackColor = TourOSColors.Primary
                            )
                        )
                    }

                    HorizontalDivider(color = TourOSColors.Divider)

                    // Tekrarlayan Seferler Switch & Genişleyen Form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "🔄 Tekrarlayan Tarihler Oluştur", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                            Text(text = "Seçilen periyoda göre otomatik toplu kalkış seferleri üretir", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                        Switch(
                            checked = uiState.isRecurring,
                            onCheckedChange = { viewModel.updateIsRecurring(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TourOSColors.Surface,
                                checkedTrackColor = TourOSColors.Primary
                            )
                        )
                    }

                    // GENİŞLEYEN TEKRARLAYAN TARİH FORM ALANLARI
                    AnimatedVisibility(
                        visible = uiState.isRecurring,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = TourOSSpacing.small),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            Text(text = "Tekrarlama Günü Seçimi:", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Primary))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)
                            ) {
                                daysOfWeekMap.forEach { (dayCode, name) ->
                                    val isSelected = uiState.selectedDayOfWeek == dayCode
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateSelectedDayOfWeek(dayCode) },
                                        label = { Text(name.take(3), style = TourOSTypography.Caption) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TourOSColors.PrimaryContainer,
                                            selectedLabelColor = TourOSColors.Primary,
                                            containerColor = TourOSColors.Surface,
                                            labelColor = TourOSColors.TextSecondary
                                        )
                                    )
                                }
                            }

                            TourOSTextField(
                                value = uiState.recurrenceEndDate,
                                onValueChange = { viewModel.updateRecurrenceEndDate(it) },
                                label = "Bitiş Tarihi (Bu tarihe kadar üret)",
                                placeholder = "2026-10-31",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Başarılı Mesajı
            if (uiState.isSavedSuccess) {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.SuccessContainer,
                    borderColor = TourOSColors.Success.copy(alpha = 0.3f),
                    contentPadding = TourOSSpacing.medium
                ) {
                    Text(
                        text = "✅ ${uiState.generatedCount} adet kalkış seferi başarıyla kaydedildi!",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Success)
                    )
                }
            }

            // Kaydet Butonu
            TourOSButton(
                text = if (uiState.isRecurring) "🚀 Toplu Seferleri Üret ve Kaydet" else "💾 Kalkış Seferini Kaydet",
                onClick = { viewModel.saveDeparture() },
                variant = TourOSButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
