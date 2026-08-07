package com.mgacreative.touros.ui.theme

import androidx.compose.runtime.Composable

/**
 * Geriye dönük uyumluluk için TourOSTheme yönlendirmesi.
 */
@Composable
fun TourOSTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    com.mgacreative.touros.ui.theme.TourOSTheme(content = content)
}
