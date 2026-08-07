package com.mgacreative.touros.di

import org.koin.core.context.startKoin
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration

/**
 * Koin başlatma fonksiyonu.
 * Tüm platform entry point'leri bu fonksiyonu çağırır.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) : KoinApplication {
    return startKoin {
        appDeclaration()
        modules(sharedModules + platformModule)
    }
}
