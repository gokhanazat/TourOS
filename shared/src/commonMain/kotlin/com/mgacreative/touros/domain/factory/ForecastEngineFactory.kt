package com.mgacreative.touros.domain.factory

import com.mgacreative.touros.domain.engine.SalesForecastEngine
import com.mgacreative.touros.domain.model.forecast.ForecastModelType

class ForecastEngineFactory(
    private val historicalEngine: SalesForecastEngine
) {
    fun getEngine(type: ForecastModelType = ForecastModelType.HEURISTIC_HISTORICAL): SalesForecastEngine {
        return when (type) {
            ForecastModelType.HEURISTIC_HISTORICAL -> historicalEngine
            ForecastModelType.MACHINE_LEARNING_REGRESSION -> historicalEngine
            ForecastModelType.HYBRID -> historicalEngine
        }
    }
}
