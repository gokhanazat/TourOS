package com.mgacreative.touros

import androidx.compose.runtime.Composable
import com.mgacreative.touros.ui.navigation.AppNavigation
import com.mgacreative.touros.ui.theme.TourOSTheme

/**
 * Ana uygulama composable.
 * TourOS teması ve navigasyon grafiğini başlatır.
 */
@Composable
fun App() {
    TourOSTheme {
        AppNavigation()
    }
}