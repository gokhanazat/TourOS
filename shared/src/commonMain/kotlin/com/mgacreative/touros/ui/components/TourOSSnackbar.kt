package com.mgacreative.touros.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing

@Composable
fun TourOSSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    containerColor: Color = TourOSColors.Primary,
    contentColor: Color = TourOSColors.OnPrimary
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data: SnackbarData ->
        Snackbar(
            snackbarData = data,
            shape = RoundedCornerShape(TourOSSpacing.cornerRadius),
            containerColor = containerColor,
            contentColor = contentColor,
            actionColor = TourOSColors.Secondary
        )
    }
}
