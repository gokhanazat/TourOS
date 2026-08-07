package com.mgacreative.touros.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // JVM/Desktop-specific dependencies
    // Ktor CIO engine is auto-resolved via classpath
}
