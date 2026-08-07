package com.mgacreative.touros.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mgacreative.touros.domain.model.SharedMapPoint

/**
 * 4.4.3 Otel Konumları, Tur Rotaları ve Canlı Araç Konumunu Gösteren Ortak Harita Expect Bileşeni.
 */
@Composable
expect fun SharedMapView(
    modifier: Modifier = Modifier,
    points: List<SharedMapPoint>,
    selectedLayer: String = "ALL"
)
