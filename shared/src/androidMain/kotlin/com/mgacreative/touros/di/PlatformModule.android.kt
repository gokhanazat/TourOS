package com.mgacreative.touros.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Android-specific dependencies
    // Ktor OkHttp engine is auto-resolved via classpath
}
