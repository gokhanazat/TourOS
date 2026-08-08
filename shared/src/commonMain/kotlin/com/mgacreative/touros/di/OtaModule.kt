package com.mgacreative.touros.di

import com.mgacreative.touros.domain.usecase.ota.ConnectOtaAccountUseCase
import com.mgacreative.touros.domain.usecase.ota.GetOtaBookingsUseCase
import com.mgacreative.touros.domain.usecase.ota.SyncOtaChannelUseCase
import com.mgacreative.touros.ui.viewmodel.OTAHubViewModel
import org.koin.dsl.module

val otaModule = module {
    single<com.mgacreative.touros.domain.scheduler.SyncScheduler> { com.mgacreative.touros.domain.scheduler.SyncScheduler() }
    factory { ConnectOtaAccountUseCase(get()) }
    factory { SyncOtaChannelUseCase(get()) }
    factory { GetOtaBookingsUseCase(get()) }

    factory { OTAHubViewModel(get(), get(), get()) }
}
