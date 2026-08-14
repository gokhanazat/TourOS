package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    actions: @Composable RowScope.() -> Unit = {}
) {
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
            navigationIcon = { navigationIcon?.invoke() },
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

