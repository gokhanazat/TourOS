package com.mgacreative.touros.di

import org.koin.core.module.Module

/**
 * Platform-specific DI modülü.
 * Her platform kendi Ktor engine'ini ve platform bağımlılıklarını sağlar.
 */
expect val platformModule: Module
