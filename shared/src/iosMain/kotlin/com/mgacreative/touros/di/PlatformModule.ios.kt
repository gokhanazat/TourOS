package com.mgacreative.touros.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // iOS-specific dependencies
    // Ktor Darwin engine is auto-resolved via classpath
}
