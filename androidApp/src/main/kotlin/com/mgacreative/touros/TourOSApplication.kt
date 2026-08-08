package com.mgacreative.touros

import android.app.Application
import com.mgacreative.touros.di.initKoin
import org.koin.android.ext.koin.androidContext

class TourOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@TourOSApplication)
        }
    }
}
