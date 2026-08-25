package com.mgacreative.touros

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.mgacreative.touros.ui.navigation.AppNavigation
import com.mgacreative.touros.ui.theme.TourOSTheme

/**
 * Ana uygulama composable.
 * TourOS teması, Coil 3 network görsel yükleyicisi ve navigasyon grafiğini başlatır.
 */
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .crossfade(true)
            .build()
    }

    TourOSTheme {
        AppNavigation()
    }
}