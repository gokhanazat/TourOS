package com.mgacreative.touros.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * KMP Google Maps Entegrasyonu Expect Köprüsü.
 */
@Composable
expect fun GoogleMapView(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    title: String = "Pickup Noktası"
)
