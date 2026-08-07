package com.mgacreative.touros.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

@Composable
fun TourOSEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(TourOSSpacing.xxLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.height(TourOSSpacing.large))
        }

        Text(
            text = title,
            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
        )

        if (!description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(TourOSSpacing.small))
            Text(
                text = description,
                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
            )
        }

        if (!actionButtonText.isNullOrBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.height(TourOSSpacing.large))
            TourOSButton(
                text = actionButtonText,
                onClick = onActionClick,
                variant = TourOSButtonVariant.PRIMARY
            )
        }
    }
}
