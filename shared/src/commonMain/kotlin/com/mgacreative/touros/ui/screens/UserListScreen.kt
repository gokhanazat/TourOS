package com.mgacreative.touros.ui.screens

import com.mgacreative.touros.ui.localization.AppLanguageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSColumn
import com.mgacreative.touros.ui.components.TourOSDataTable
import com.mgacreative.touros.ui.components.TourOSDropdown
import com.mgacreative.touros.ui.components.TourOSEmptyState
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.UserListUiState
import com.mgacreative.touros.ui.viewmodel.UserListViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Adaptif Kullanıcı Listesi Ekranı.
 * - Üstte Arama Kutusu + Rol Filtre Dropdown.
 * - Expanded: TourOSDataTable (Sütunlar: Avatar/İsim, E-posta, Rol Rozeti, Durum Switch).
 * - Compact: Kullanıcı Kartları Listesi (Avatar, İsim, Rol rozeti, Aktif/Pasif switch).
 */
@Composable
fun UserListScreen(
    onNavigateToInviteUser: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: UserListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Kullanıcı Yönetimi",
                subtitle = "Şirket kullanıcılarını listeleyin, arayın ve durumlarını yönetin",
                actions = {
                    TourOSButton(
                        text = AppLanguageManager.translate("+ Kullanıcı Davet Et", currentLanguage.code),
                        onClick = onNavigateToInviteUser,
                        variant = TourOSButtonVariant.PRIMARY
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
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // Arama & Filtre Kartı
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                val successState = uiState as? UserListUiState.Success
                val roleOptions = listOf("Tüm Roller") + UserRole.entries.map { it.displayName }
                val currentRoleLabel = successState?.selectedRoleFilter?.displayName ?: "Tüm Roller"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TourOSTextField(
                        value = successState?.searchQuery ?: "",
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = AppLanguageManager.translate("🔍 İsim veya e-posta ile ara...", currentLanguage.code),
                        modifier = Modifier.weight(1f)
                    )

                    Box(modifier = Modifier.width(220.dp)) {
                        TourOSDropdown(
                            items = roleOptions,
                            selectedItem = currentRoleLabel,
                            onItemSelected = { label ->
                                val selectedRole = UserRole.entries.firstOrNull { it.displayName == label }
                                viewModel.onRoleFilterSelected(selectedRole)
                            },
                            itemLabel = { AppLanguageManager.translate(it, currentLanguage.code) },
                            label = AppLanguageManager.translate("Rol Filtresi", currentLanguage.code)
                        )
                    }
                }
            }

            // Adaptif Tablo / Kart Listesi Görünümü
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val isCompact = maxWidth < 720.dp

                when (val state = uiState) {
                    is UserListUiState.Loading -> {
                        TourOSLoadingIndicator(message = AppLanguageManager.translate("Kullanıcılar yükleniyor...", currentLanguage.code))
                    }
                    is UserListUiState.Error -> {
                        TourOSEmptyState(
                            title = AppLanguageManager.translate("Bir Hata Oluştu", currentLanguage.code),
                            description = state.message,
                            actionButtonText = AppLanguageManager.translate("Yeniden Dene", currentLanguage.code),
                            onActionClick = { viewModel.onSearchQueryChanged("") }
                        )
                    }
                    is UserListUiState.Success -> {
                        if (state.users.isEmpty()) {
                            TourOSEmptyState(
                                title = AppLanguageManager.translate("Kullanıcı Bulunamadı", currentLanguage.code),
                                description = AppLanguageManager.translate("Kriterlerinize uygun aktif kullanıcı bulunmuyor.", currentLanguage.code),
                                actionButtonText = AppLanguageManager.translate("+ Yeni Kullanıcı Davet Et", currentLanguage.code),
                                onActionClick = onNavigateToInviteUser
                            )
                        } else {
                            val userColumns = listOf(
                                TourOSColumn<User>(title = AppLanguageManager.translate("KULLANICI", currentLanguage.code), weight = 2.5f) { user ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        UserAvatar(name = user.fullName)
                                        Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                        Column {
                                            Text(text = user.fullName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                            Text(text = user.email, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                        }
                                    }
                                },
                                TourOSColumn<User>(title = AppLanguageManager.translate("ROL", currentLanguage.code), weight = 2.0f) { user ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                            .clickable { viewModel.onEditRolesClicked(user) }
                                            .padding(vertical = 4.dp, horizontal = 6.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            user.roles.forEach { role ->
                                                TourOSStatusBadge(
                                                    text = AppLanguageManager.translate(role.displayName, currentLanguage.code),
                                                    backgroundColor = TourOSColors.PrimaryContainer,
                                                    textColor = TourOSColors.Primary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "✏️",
                                            style = TourOSTypography.Caption
                                        )
                                    }
                                },
                                TourOSColumn<User>(title = AppLanguageManager.translate("DURUM", currentLanguage.code), weight = 1.2f) { user ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(
                                            checked = user.isActive,
                                            onCheckedChange = { viewModel.toggleUserStatus(user) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = TourOSColors.Background,
                                                checkedTrackColor = TourOSColors.Primary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                        Text(
                                            text = if (user.isActive) AppLanguageManager.translate("Aktif", currentLanguage.code) else AppLanguageManager.translate("Pasif", currentLanguage.code),
                                            style = TourOSTypography.BodyMedium.copy(
                                                color = if (user.isActive) TourOSColors.Success else TourOSColors.TextDisabled
                                            )
                                        )
                                    }
                                }
                            )

                            TourOSDataTable(
                                items = state.users,
                                columns = userColumns,
                                isCompact = isCompact,
                                modifier = Modifier.fillMaxSize(),
                                compactCardContent = { user ->
                                    // Compact Mobil Kart İçeriği
                                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                UserAvatar(name = user.fullName)
                                                Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                                Column {
                                                    Text(text = user.fullName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                                    Text(text = user.email, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                                    .clickable { viewModel.onEditRolesClicked(user) }
                                                    .padding(2.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    user.roles.forEach { role ->
                                                        TourOSStatusBadge(
                                                            text = AppLanguageManager.translate(role.displayName, currentLanguage.code),
                                                            backgroundColor = TourOSColors.PrimaryContainer,
                                                            textColor = TourOSColors.Primary
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "✏️", style = TourOSTypography.Caption)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (user.isActive) AppLanguageManager.translate("Hesap Durumu: Aktif", currentLanguage.code) else AppLanguageManager.translate("Hesap Durumu: Pasif", currentLanguage.code),
                                                style = TourOSTypography.BodyMedium.copy(
                                                    color = if (user.isActive) TourOSColors.Success else TourOSColors.Error
                                                )
                                            )

                                            Switch(
                                                checked = user.isActive,
                                                onCheckedChange = { viewModel.toggleUserStatus(user) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = TourOSColors.Background,
                                                    checkedTrackColor = TourOSColors.Primary
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        val successState = uiState as? UserListUiState.Success
        if (successState?.editingRolesUser != null) {
            EditRolesDialog(
                user = successState.editingRolesUser,
                currentLanguageCode = currentLanguage.code,
                onDismiss = { viewModel.onEditRolesClicked(null) },
                onSave = { newRoles ->
                    viewModel.updateUserRoles(successState.editingRolesUser, newRoles)
                }
            )
        }
    }
}

@Composable
private fun EditRolesDialog(
    user: User,
    currentLanguageCode: String,
    onDismiss: () -> Unit,
    onSave: (List<UserRole>) -> Unit
) {
    var selectedRoles by remember(user) { mutableStateOf(user.roles.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        TourOSCard(
            modifier = Modifier
                .widthIn(min = 340.dp, max = 460.dp)
                .padding(TourOSSpacing.medium),
            backgroundColor = TourOSColors.Background,
            borderColor = TourOSColors.Border,
            contentPadding = TourOSSpacing.large
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = AppLanguageManager.translate("Rolleri Düzenle", currentLanguageCode),
                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${user.fullName} • ${user.email}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                HorizontalDivider(color = TourOSColors.Divider, thickness = TourOSSpacing.borderWidth)

                Text(
                    text = AppLanguageManager.translate("Kullanıcıya atanacak rolleri seçin:", currentLanguageCode),
                    style = TourOSTypography.BodyMedium.copy(
                        color = TourOSColors.TextPrimary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                )

                Text(
                    text = AppLanguageManager.translate("Birden fazla rol seçilebilir.", currentLanguageCode),
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )

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
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedRoles = if (isChecked) {
                                        selectedRoles - role
                                    } else {
                                        selectedRoles + role
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedRoles = if (checked) {
                                        selectedRoles + role
                                    } else {
                                        selectedRoles - role
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = TourOSColors.Primary
                                )
                            )
                            Spacer(modifier = Modifier.width(TourOSSpacing.small))
                            Text(
                                text = AppLanguageManager.translate(role.displayName, currentLanguageCode),
                                style = TourOSTypography.BodyMedium.copy(
                                    color = if (isChecked) TourOSColors.Primary else TourOSColors.TextPrimary,
                                    fontWeight = if (isChecked) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                if (selectedRoles.isEmpty()) {
                    Text(
                        text = AppLanguageManager.translate("En az bir rol seçilmelidir", currentLanguageCode),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Error)
                    )
                }

                Spacer(modifier = Modifier.height(TourOSSpacing.small))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TourOSButton(
                        text = AppLanguageManager.translate("İptal", currentLanguageCode),
                        onClick = onDismiss,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                    Spacer(modifier = Modifier.width(TourOSSpacing.small))
                    TourOSButton(
                        text = AppLanguageManager.translate("Kaydet", currentLanguageCode),
                        onClick = {
                            if (selectedRoles.isNotEmpty()) {
                                onSave(selectedRoles.toList())
                            }
                        },
                        variant = TourOSButtonVariant.PRIMARY,
                        enabled = selectedRoles.isNotEmpty()
                    )
                }
            }
        }
    }
}

@Composable
private fun UserAvatar(name: String) {
    val initial = name.firstOrNull()?.uppercase() ?: "U"
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(TourOSColors.PrimaryContainer)
            .border(TourOSSpacing.borderWidth, TourOSColors.Border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
        )
    }
}
