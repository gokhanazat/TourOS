package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mgacreative.touros.ui.navigation.LocalNavController
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourOSTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val navController = LocalNavController.current
    val canPop = navController?.previousBackStackEntry != null
    val effectiveBackAction: (() -> Unit)? = onNavigateBack ?: (if (canPop) {
        {
            navController?.popBackStack()
            Unit
        }
    } else null)

    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            },
            navigationIcon = {
                if (navigationIcon != null) {
                    navigationIcon()
                } else if (effectiveBackAction != null) {
                    IconButton(onClick = effectiveBackAction) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri Dön",
                            tint = TourOSColors.Primary
                        )
                    }
                }
            },
            actions = {
                actions()
                LanguageSelector()
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = TourOSColors.Background,
                titleContentColor = TourOSColors.TextPrimary,
                actionIconContentColor = TourOSColors.Primary
            )
        )
        HorizontalDivider(color = TourOSColors.Divider, thickness = TourOSSpacing.borderWidth)
    }
}
