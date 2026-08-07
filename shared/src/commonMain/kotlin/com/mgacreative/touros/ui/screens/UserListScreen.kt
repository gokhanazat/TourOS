package com.mgacreative.touros.ui.screens

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Kullanıcı Yönetimi",
                subtitle = "Şirket kullanıcılarını listeleyin, arayın ve durumlarını yönetin",
                actions = {
                    TourOSButton(
                        text = "+ Kullanıcı Davet Et",
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
                        placeholder = "🔍 İsim veya e-posta ile ara...",
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
                            itemLabel = { it },
                            label = "Rol Filtresi"
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
                        TourOSLoadingIndicator(message = "Kullanıcılar yükleniyor...")
                    }
                    is UserListUiState.Error -> {
                        TourOSEmptyState(
                            title = "Bir Hata Oluştu",
                            description = state.message,
                            actionButtonText = "Yeniden Dene",
                            onActionClick = { viewModel.onSearchQueryChanged("") }
                        )
                    }
                    is UserListUiState.Success -> {
                        if (state.users.isEmpty()) {
                            TourOSEmptyState(
                                title = "Kullanıcı Bulunamadı",
                                description = "Kriterlerinize uygun aktif kullanıcı bulunmuyor.",
                                actionButtonText = "+ Yeni Kullanıcı Davet Et",
                                onActionClick = onNavigateToInviteUser
                            )
                        } else {
                            val userColumns = listOf(
                                TourOSColumn<User>(title = "KULLANICI", weight = 2.5f) { user ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        UserAvatar(name = user.fullName)
                                        Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                        Column {
                                            Text(text = user.fullName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                            Text(text = user.email, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                        }
                                    }
                                },
                                TourOSColumn<User>(title = "ROL", weight = 1.5f) { user ->
                                    TourOSStatusBadge(
                                        text = user.role.displayName,
                                        backgroundColor = TourOSColors.PrimaryContainer,
                                        textColor = TourOSColors.Primary
                                    )
                                },
                                TourOSColumn<User>(title = "DURUM", weight = 1.2f) { user ->
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
                                            text = if (user.isActive) "Aktif" else "Pasif",
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

                                            TourOSStatusBadge(
                                                text = user.role.displayName,
                                                backgroundColor = TourOSColors.PrimaryContainer,
                                                textColor = TourOSColors.Primary
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (user.isActive) "Hesap Durumu: Aktif" else "Hesap Durumu: Pasif",
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
