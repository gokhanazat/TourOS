package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSDropdown
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.InviteUserUiState
import com.mgacreative.touros.ui.viewmodel.InviteUserViewModel
import org.koin.compose.viewmodel.koinViewModel

data class PendingInvitation(
    val email: String,
    val fullName: String,
    val role: UserRole,
    val sentAt: String
)

/**
 * TourOS 0.3 Tasarım Sistemine uygun Kullanıcı Davet Etme Ekranı.
 * - Sol Taraf: Bekleyen Davetler Listesi ('Bekliyor' status badge ile)
 * - Sağ Taraf: Side Sheet Form Paneli (E-posta + Rol Dropdown)
 */
@Composable
fun InviteUserScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: InviteUserViewModel = koinViewModel()
) {
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRoles by remember { mutableStateOf(setOf(UserRole.TOUR_OPERATOR)) }
    var selectedMenus by remember {
        mutableStateOf(setOf("B2B_SALES", "TOUR_OPERATOR", "ACCOUNTING", "ANALYTICS", "LOCAL", "SETTINGS"))
    }

    val uiState by viewModel.uiState.collectAsState()

    // Mock Bekleyen Davetler Verisi
    val pendingInvitations = remember {
        listOf(
            PendingInvitation("ahmet.yilmaz@touros.com", "Ahmet Yılmaz", UserRole.SALES, "Bugün, 09:30"),
            PendingInvitation("zeynep.kaya@touros.com", "Zeynep Kaya", UserRole.GUIDE, "Dün, 14:15"),
            PendingInvitation("mehmet.demir@touros.com", "Mehmet Demir", UserRole.ACCOUNTING, "05 Ağu 2026")
        )
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Kullanıcı Davet Etme",
                subtitle = "Ekibinize e-posta ile davet gönderin ve bekleyen davetleri izleyin",
                navigationIcon = {
                    TourOSButton(
                        text = "← ${AppLanguageManager.translate("Geri", currentLanguage.code)}",
                        onClick = onNavigateBack,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                }
            )
        },
        containerColor = TourOSColors.Surface
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(TourOSSpacing.large)
        ) {
            // SOL TARAF: Bekleyen Davetler Listesi
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Text(
                    text = "${AppLanguageManager.translate("Bekleyen Davetler")} (${pendingInvitations.size})",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    items(pendingInvitations) { invitation ->
                        TourOSCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TourOSColors.Background,
                            borderColor = TourOSColors.Border,
                            contentPadding = TourOSSpacing.medium
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = invitation.fullName,
                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                    )
                                    Text(
                                        text = invitation.email,
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                    )
                                    Text(
                                        text = "Rol: ${AppLanguageManager.translate(invitation.role.displayName)} • ${invitation.sentAt}",
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextDisabled)
                                    )
                                }

                                TourOSStatusBadge(
                                    text = AppLanguageManager.translate("Bekliyor"),
                                    backgroundColor = TourOSColors.WarningContainer,
                                    textColor = TourOSColors.Warning
                                )
                            }
                        }
                    }
                }
            }

            // DİKEY SEPARATÖR
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxSize()
                    .background(TourOSColors.Border)
            )

            Spacer(modifier = Modifier.width(TourOSSpacing.large))

            // SAĞ TARAF: Side Sheet Davet Formu Paneli (Fixed 400dp)
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.Background,
                    borderColor = TourOSColors.Border,
                    contentPadding = TourOSSpacing.xLarge
                ) {
                    Text(
                        text = AppLanguageManager.translate("Yeni Davet Gönder"),
                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                    )
                    Text(
                        text = AppLanguageManager.translate("Kullanıcıya e-posta ile davet bağlantısı iletilecektir."),
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(TourOSSpacing.large))

                    // Status Banners
                    when (val state = uiState) {
                        is InviteUserUiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                    .background(TourOSColors.ErrorContainer)
                                    .padding(TourOSSpacing.medium)
                            ) {
                                Text(
                                    text = state.message,
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error)
                                )
                            }
                            Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                        }
                        is InviteUserUiState.Success -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                    .background(TourOSColors.SuccessContainer)
                                    .padding(TourOSSpacing.medium)
                            ) {
                                Text(
                                    text = "${state.email} adresine davet e-postası başarıyla gönderildi.",
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Success)
                                )
                            }
                            Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                        }
                        else -> {}
                    }

                    TourOSTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = AppLanguageManager.translate("Ad Soyad"),
                        placeholder = "Örn: Canan Yıldız",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                    TourOSTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = AppLanguageManager.translate("E-posta Adresi"),
                        placeholder = "canan@touros.com",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                    // Kullanıcı Rolleri (Çoklu Seçim)
                    Text(
                        text = AppLanguageManager.translate("Kullanıcı Rolleri", currentLanguage.code),
                        style = TourOSTypography.TitleSmall.copy(color = TourOSColors.TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(TourOSSpacing.xxSmall))
                    Text(
                        text = AppLanguageManager.translate("Birden fazla rol seçilebilir.", currentLanguage.code),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(TourOSSpacing.small))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xxSmall),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UserRole.entries.forEach { role ->
                            val isChecked = selectedRoles.contains(role)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = TourOSSpacing.xxSmall)
                            ) {
                                androidx.compose.material3.Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedRoles = if (checked) {
                                            selectedRoles + role
                                        } else {
                                            if (selectedRoles.size > 1) selectedRoles - role else selectedRoles
                                        }
                                    },
                                    colors = androidx.compose.material3.CheckboxDefaults.colors(
                                        checkedColor = TourOSColors.Primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                Text(
                                    text = AppLanguageManager.translate(role.displayName, currentLanguage.code),
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                    // İzin Verilen Menüler (Erişim Kısıtlaması)
                    Text(
                        text = AppLanguageManager.translate("Erişebileceği Menüler:", currentLanguage.code),
                        style = TourOSTypography.TitleSmall.copy(color = TourOSColors.TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(TourOSSpacing.xxSmall))
                    Text(
                        text = AppLanguageManager.translate("İşaretlenmeyen menüler kullanıcının sol menüsünde gizlenir.", currentLanguage.code),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(TourOSSpacing.small))

                    val availableMenuOptions = remember {
                        listOf(
                            "B2B_SALES" to "B2B Satış & Rezervasyon",
                            "TOUR_OPERATOR" to "Tur Operatörü & PNR",
                            "ACCOUNTING" to "Muhasebe & Cari Hesap",
                            "ANALYTICS" to "Analitik & Raporlar",
                            "PREMIUM" to "Premium (OTA & Entegrasyon)",
                            "LOCAL" to "Yerel Tur & Otel",
                            "SETTINGS" to "Ayarlar & Dil"
                        )
                    }

                    availableMenuOptions.forEach { (key, title) ->
                        val isChecked = selectedMenus.contains(key)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = TourOSSpacing.xxSmall)
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedMenus = selectedMenus + key
                                    } else {
                                        selectedMenus = selectedMenus - key
                                    }
                                },
                                colors = androidx.compose.material3.CheckboxDefaults.colors(
                                    checkedColor = TourOSColors.Primary
                                )
                            )
                            Spacer(modifier = Modifier.width(TourOSSpacing.small))
                            Text(
                                text = AppLanguageManager.translate(title, currentLanguage.code),
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(TourOSSpacing.xxLarge))

                    TourOSButton(
                        text = AppLanguageManager.translate("Davet Gönder ✉️", currentLanguage.code),
                        onClick = {
                            viewModel.inviteUser(email, selectedRoles.first(), fullName)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        variant = TourOSButtonVariant.PRIMARY,
                        enabled = email.isNotBlank() && fullName.isNotBlank() && selectedRoles.isNotEmpty(),
                        isLoading = uiState is InviteUserUiState.Loading
                    )
                }
            }
        }
    }
}
