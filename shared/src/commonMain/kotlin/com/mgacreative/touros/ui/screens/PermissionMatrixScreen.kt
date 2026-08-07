package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.PermissionAction
import com.mgacreative.touros.domain.model.PermissionResource
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSDropdown
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSSnackbarHost
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.PermissionMatrixUiState
import com.mgacreative.touros.ui.viewmodel.PermissionMatrixViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Adaptif Yetki Matrisi Ekranı.
 * - Expanded (Desktop/Web): Satırlar izin kaynakları, sütunlar aksiyonlar olan Matris Tablo + Checkbox hücreleri.
 * - Compact (Mobil): Rol seçici + Tek sütunlu izin kartları listesi.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PermissionMatrixScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: PermissionMatrixViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Yetki Matrisi Düzenleyici",
                subtitle = "Rol bazlı erişim yetkilerini (RBAC) matris veya liste üzerinden yönetin",
                navigationIcon = {
                    TourOSButton(
                        text = "← Geri",
                        onClick = onNavigateBack,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                }
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
            when (val state = uiState) {
                is PermissionMatrixUiState.Loading -> {
                    TourOSLoadingIndicator(message = "Yetki matrisi yükleniyor...")
                }
                is PermissionMatrixUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            style = TourOSTypography.BodyLarge.copy(color = TourOSColors.Error)
                        )
                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                        TourOSButton(
                            text = "Yeniden Yükle",
                            onClick = { viewModel.selectRole(UserRole.TOUR_OPERATOR) },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }
                }
                is PermissionMatrixUiState.Success, is PermissionMatrixUiState.Saving -> {
                    val activeState = state as? PermissionMatrixUiState.Success
                        ?: PermissionMatrixUiState.Success(UserRole.TOUR_OPERATOR, emptyMap())

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(TourOSSpacing.large)
                    ) {
                        val isCompact = maxWidth < 720.dp

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Rol Seçici Alanı
                            Text(
                                text = "Düzenlenecek Rolü Seçin",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                            )
                            Spacer(modifier = Modifier.height(TourOSSpacing.small))

                            if (isCompact) {
                                TourOSDropdown(
                                    items = UserRole.entries,
                                    selectedItem = activeState.selectedRole,
                                    onItemSelected = { viewModel.selectRole(it) },
                                    itemLabel = { it.displayName },
                                    label = "Kullanıcı Rolü",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    UserRole.entries.forEach { role ->
                                        val isSelected = role == activeState.selectedRole
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.selectRole(role) },
                                            label = { Text(role.displayName, style = TourOSTypography.BodyMedium) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                                selectedLabelColor = TourOSColors.Primary
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.large))

                            // MATRİS VE LİSTE GÖRÜNÜMÜ
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                if (isCompact) {
                                    // COMPACT: Tek Sütunlu Kart Listesi
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                    ) {
                                        items(PermissionResource.entries) { resource ->
                                            TourOSCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                backgroundColor = TourOSColors.Background,
                                                borderColor = TourOSColors.Border,
                                                contentPadding = TourOSSpacing.medium
                                            ) {
                                                Text(
                                                    text = resource.key.uppercase(),
                                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                                                )
                                                Spacer(modifier = Modifier.height(TourOSSpacing.small))
                                                HorizontalDivider(color = TourOSColors.Divider)
                                                Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                                FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)
                                                ) {
                                                    PermissionAction.entries.forEach { action ->
                                                        val isChecked = activeState.permissionMap[Pair(resource, action)] ?: false
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Checkbox(
                                                                checked = isChecked,
                                                                onCheckedChange = { checked ->
                                                                    viewModel.togglePermission(resource, action, checked)
                                                                },
                                                                enabled = activeState.selectedRole != UserRole.SYSTEM_ADMIN,
                                                                colors = CheckboxDefaults.colors(
                                                                    checkedColor = TourOSColors.Primary
                                                                )
                                                            )
                                                            Text(
                                                                text = action.key.uppercase(),
                                                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // EXPANDED: Satırları İzinler, Sütunları Eylemler Olan Matris Tablosu
                                    TourOSCard(
                                        modifier = Modifier.fillMaxSize(),
                                        backgroundColor = TourOSColors.Background,
                                        borderColor = TourOSColors.Border,
                                        contentPadding = TourOSSpacing.large
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .horizontalScroll(rememberScrollState())
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            Column {
                                                // Table Header
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(TourOSColors.Surface)
                                                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.medium),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "KAYNAK / MODÜL",
                                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                                        modifier = Modifier.width(220.dp)
                                                    )
                                                    PermissionAction.entries.forEach { action ->
                                                        Text(
                                                            text = action.key.uppercase(),
                                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                                            modifier = Modifier.width(110.dp)
                                                        )
                                                    }
                                                }

                                                HorizontalDivider(color = TourOSColors.Divider)

                                                // Table Rows
                                                PermissionResource.entries.forEach { resource ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.xSmall),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = resource.key.uppercase(),
                                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                                                            modifier = Modifier.width(220.dp)
                                                        )

                                                        PermissionAction.entries.forEach { action ->
                                                            val isChecked = activeState.permissionMap[Pair(resource, action)] ?: false
                                                            Box(
                                                                modifier = Modifier.width(110.dp),
                                                                contentAlignment = Alignment.CenterStart
                                                            ) {
                                                                Checkbox(
                                                                    checked = isChecked,
                                                                    onCheckedChange = { checked ->
                                                                        viewModel.togglePermission(resource, action, checked)
                                                                    },
                                                                    enabled = activeState.selectedRole != UserRole.SYSTEM_ADMIN,
                                                                    colors = CheckboxDefaults.colors(
                                                                        checkedColor = TourOSColors.Primary
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                    HorizontalDivider(color = TourOSColors.Divider)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.large))

                            // Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TourOSButton(
                                    text = "Değişiklikleri Kaydet ✓",
                                    onClick = { viewModel.savePermissions() },
                                    variant = TourOSButtonVariant.PRIMARY,
                                    isLoading = uiState is PermissionMatrixUiState.Saving
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
