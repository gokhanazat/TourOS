package com.mgacreative.touros.di

import com.mgacreative.touros.data.repository.AuthRepositoryImpl
import com.mgacreative.touros.data.repository.CompanySettingsRepositoryImpl
import com.mgacreative.touros.data.repository.RolePermissionRepositoryImpl
import com.mgacreative.touros.data.repository.TenantRepositoryImpl
import com.mgacreative.touros.data.repository.TourMediaRepositoryImpl
import com.mgacreative.touros.data.repository.TourRepositoryImpl
import com.mgacreative.touros.data.repository.UserRepositoryImpl
import com.mgacreative.touros.domain.repository.AuthRepository
import com.mgacreative.touros.domain.repository.CompanySettingsRepository
import com.mgacreative.touros.domain.repository.RolePermissionRepository
import com.mgacreative.touros.domain.repository.TenantRepository
import com.mgacreative.touros.domain.repository.TourMediaRepository
import com.mgacreative.touros.domain.repository.TourRepository
import com.mgacreative.touros.domain.repository.UserRepository
import com.mgacreative.touros.domain.usecase.CheckPermissionUseCase
import com.mgacreative.touros.domain.usecase.CheckSessionUseCase
import com.mgacreative.touros.domain.usecase.CreateOrUpdateTourUseCase
import com.mgacreative.touros.domain.usecase.ForgotPasswordUseCase
import com.mgacreative.touros.domain.usecase.GetCompanySettingsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetTourMediaUseCase
import com.mgacreative.touros.domain.usecase.GetTourDetailUseCase
import com.mgacreative.touros.domain.usecase.GetToursUseCase
import com.mgacreative.touros.domain.usecase.GetUsersUseCase
import com.mgacreative.touros.domain.usecase.InviteUserUseCase
import com.mgacreative.touros.domain.usecase.LoginUseCase
import com.mgacreative.touros.domain.usecase.LogoutUseCase
import com.mgacreative.touros.domain.usecase.OnboardTenantUseCase
import com.mgacreative.touros.domain.usecase.RegisterUseCase
import com.mgacreative.touros.domain.usecase.ToggleTourStatusUseCase
import com.mgacreative.touros.domain.usecase.ToggleUserStatusUseCase
import com.mgacreative.touros.domain.usecase.UpdateCompanySettingsUseCase
import com.mgacreative.touros.domain.usecase.UpdateRolePermissionsUseCase
import com.mgacreative.touros.domain.usecase.UploadCompanyLogoUseCase
import com.mgacreative.touros.domain.usecase.UploadTourMediaUseCase
import com.mgacreative.touros.domain.usecase.VerifyEmailUseCase
import com.mgacreative.touros.network.SupabaseClientProvider
import com.mgacreative.touros.ui.viewmodel.AuthViewModel
import com.mgacreative.touros.ui.viewmodel.CompanySettingsViewModel
import com.mgacreative.touros.ui.viewmodel.ForgotPasswordViewModel
import com.mgacreative.touros.ui.viewmodel.InviteUserViewModel
import com.mgacreative.touros.ui.viewmodel.PermissionMatrixViewModel
import com.mgacreative.touros.ui.viewmodel.SplashViewModel
import com.mgacreative.touros.ui.viewmodel.TourFormViewModel
import com.mgacreative.touros.ui.viewmodel.TourListViewModel
import com.mgacreative.touros.ui.viewmodel.TourMediaGalleryViewModel
import com.mgacreative.touros.ui.viewmodel.UserListViewModel
import org.koin.dsl.module

/**
 * Uygulama geneli Koin modülleri.
 * Her katman kendi modülünü tanımlar, appModule bunları birleştirir.
 */

val networkModule = module {
    single { SupabaseClientProvider.create() }
    single { com.mgacreative.touros.data.cache.SystemCacheManager(get()) }
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<TenantRepository> { TenantRepositoryImpl(get()) }
    single<CompanySettingsRepository> { CompanySettingsRepositoryImpl(get()) }
    single<RolePermissionRepository> { RolePermissionRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<TourRepository> { TourRepositoryImpl(get()) }
    single<TourMediaRepository> { TourMediaRepositoryImpl(get()) }
    single<com.mgacreative.touros.domain.repository.HotelRepository> { com.mgacreative.touros.data.repository.HotelRepositoryImpl(get()) }
    single<com.mgacreative.touros.domain.repository.BookingRepository> { com.mgacreative.touros.data.repository.BookingRepositoryImpl(get()) }
    single<com.mgacreative.touros.domain.repository.DashboardRepository> { com.mgacreative.touros.data.repository.DashboardRepositoryImpl(get()) }
    single<com.mgacreative.touros.domain.repository.FinanceRepository> { com.mgacreative.touros.data.repository.FinanceRepositoryImpl(get()) }
    single<com.mgacreative.touros.domain.repository.OTARepository> { com.mgacreative.touros.data.repository.OTARepositoryImpl(get()) }
    single<com.mgacreative.touros.domain.repository.RecommendationRepository> { com.mgacreative.touros.data.repository.RecommendationRepositoryImpl(get()) }
    single<com.mgacreative.touros.domain.repository.CustomerSegmentationRepository> { com.mgacreative.touros.data.repository.CustomerSegmentationRepositoryImpl(get()) }
    single<com.mgacreative.touros.domain.engine.SalesForecastEngine> { com.mgacreative.touros.data.engine.HeuristicHistoricalForecastEngineImpl(get()) }
    single { com.mgacreative.touros.domain.factory.ForecastEngineFactory(get()) }
    single<com.mgacreative.touros.domain.repository.SalesForecastRepository> { com.mgacreative.touros.data.repository.SalesForecastRepositoryImpl(get(), getOrNull()) }
    single { com.mgacreative.touros.domain.engine.LowOccupancyAlertRuleEngine() }
    single { com.mgacreative.touros.domain.engine.EmailDraftGeneratorEngine() }
    single<com.mgacreative.touros.domain.repository.EmailDraftRepository> { com.mgacreative.touros.data.repository.EmailDraftRepositoryImpl(get(), getOrNull()) }
    single { com.mgacreative.touros.domain.engine.FeedbackPatternAnalysisEngine() }
    single<com.mgacreative.touros.domain.repository.FeedbackPatternRepository> { com.mgacreative.touros.data.repository.FeedbackPatternRepositoryImpl(get(), getOrNull()) }
    single { com.mgacreative.touros.domain.engine.ComplaintClassifierEngine() }
    single<com.mgacreative.touros.domain.repository.ComplaintClassificationRepository> { com.mgacreative.touros.data.repository.ComplaintClassificationRepositoryImpl(get(), getOrNull()) }
    single<com.mgacreative.touros.domain.repository.ComplaintTrendPerformanceRepository> { com.mgacreative.touros.data.repository.ComplaintTrendPerformanceRepositoryImpl(getOrNull()) }
}

val useCaseModule = module {
    factory { com.mgacreative.touros.domain.usecase.recommendation.GetPersonalizedRecommendationsUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.segmentation.AnalyzeCustomerSegmentationUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.forecast.GetTourSalesForecastUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.forecast.GetDashboardSalesForecastUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.forecast.CheckLowOccupancyAlertsUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.email.GenerateEmailDraftUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.email.SendEmailDraftUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.feedback.AnalyzeFeedbackPatternsUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.feedback.ClassifyComplaintUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.feedback.GetComplaintTrendPerformanceUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { OnboardTenantUseCase(get()) }
    factory { GetCompanySettingsUseCase(get()) }
    factory { UpdateCompanySettingsUseCase(get()) }
    factory { UploadCompanyLogoUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.UploadCompanyHeaderBannerUseCase(get()) }
    factory { CheckSessionUseCase(get()) }
    factory { ForgotPasswordUseCase(get()) }
    factory { VerifyEmailUseCase(get()) }
    factory { CheckPermissionUseCase() }
    factory { InviteUserUseCase(get()) }
    factory { UpdateRolePermissionsUseCase(get()) }
    factory { GetUsersUseCase(get()) }
    factory { ToggleUserStatusUseCase(get()) }
    factory { CreateOrUpdateTourUseCase(get()) }
    factory { GetToursUseCase(get()) }
    factory { GetTourDetailUseCase(get()) }
    factory { ToggleTourStatusUseCase(get()) }
    factory { UploadTourMediaUseCase(get()) }
    factory { GetTourMediaUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetHotelsUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetRoomTypesUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetHotelContractsUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.CreateHotelContractUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetHotelSeasonRatesUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.CreateHotelSeasonRateUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetHotelStopSalesUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.CreateHotelStopSaleUseCase(get()) }
    single { com.mgacreative.touros.domain.engine.AutoSupplierExpenseEngine(get(), get()) }
    single { com.mgacreative.touros.domain.engine.InvoicePdfExportEngine(get()) }
    single { com.mgacreative.touros.domain.engine.CommissionCalculationEngine() }
    single { com.mgacreative.touros.domain.engine.CurrencyConverterEngine() }
    single { com.mgacreative.touros.domain.engine.ReportExportEngine() }
    single { com.mgacreative.touros.domain.engine.VoucherContractTemplateEngine() }
    single { com.mgacreative.touros.data.adapter.InternalOperatorAdapter(get()) }
    single { com.mgacreative.touros.domain.engine.MarketplaceBookingRoutingEngine(get(), get()) }
    single { com.mgacreative.touros.domain.engine.MarketplaceCancellationReceiver(get(), get()) }
    single { com.mgacreative.touros.domain.engine.MarketplaceCommissionEngine(get()) }
    single { com.mgacreative.touros.domain.engine.OTASyncManager(get(), get()) }
    single { com.mgacreative.touros.domain.engine.OTAWebhookManager(get(), get(), get()) }
    factory { com.mgacreative.touros.domain.usecase.GetExchangeRatesUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.UpdateExchangeRatesUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetFinancialReportUseCase(get(), get()) }
    factory { com.mgacreative.touros.domain.usecase.GetAnalyticsChartsUseCase(get(), get()) }
    factory { com.mgacreative.touros.domain.usecase.GetPerformanceReportsUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.ExportReportUseCase(get(), get()) }
    factory { com.mgacreative.touros.domain.usecase.GenerateVoucherOrContractPdfUseCase(get(), get()) }
    factory { com.mgacreative.touros.domain.usecase.GetSupportedLanguagesUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetAppTranslationsUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.ConvertCurrencyUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.ProcessSupplierExpenseUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetCurrentAccountsUseCase(get(), get()) }
    factory { com.mgacreative.touros.domain.usecase.CreateInvoiceUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.ExportInvoicePdfUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.CreateBookingUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetBookingsUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.UpdateBookingStatusUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetBookingDetailUseCase(get()) }
    factory { com.mgacreative.touros.domain.usecase.GetDashboardSummaryUseCase(get()) }
}

val viewModelModule = module {
    factory { AuthViewModel(get(), get(), get(), get()) }
    factory { CompanySettingsViewModel(get(), get(), get(), get()) }
    factory { SplashViewModel(get()) }
    factory { ForgotPasswordViewModel(get()) }
    factory { InviteUserViewModel(get()) }
    factory { PermissionMatrixViewModel(get()) }
    factory { UserListViewModel(get(), get(), get()) }
    factory { TourFormViewModel(get(), get(), get(), get(), get()) }
    factory { TourListViewModel(get(), get(), get()) }
    factory { TourMediaGalleryViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.TourDetailViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.CreateBookingWizardViewModel(get(), get(), get(), get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.BookingListViewModel(get(), get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.ReportsViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.OperatorCurrentAccountReportViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.CustomerSegmentationViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.BookingDetailViewModel(get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.DashboardViewModel(get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.DepartureFormViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.HotelListViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.HotelFormViewModel(get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.RoomTypeManagementViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.HotelContractViewModel(get(), get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.SupplierExpenseViewModel(get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.CurrentAccountViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.InvoiceManagementViewModel(get(), get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.ExchangeRatesViewModel(get(), get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.FinancialReportsViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.AnalyticsChartsViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.PerformanceReportsViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.ReportFilterExportViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.VoucherContractPdfViewModel(get(), get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.MultiLanguageViewModel(get(), get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.CurrencyConverterViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.PersonalizedRecommendationsViewModel(get(), get()) }
    factory { com.mgacreative.touros.ui.viewmodel.AgencyOperatorConnectionsViewModel(get()) }
    single { com.mgacreative.touros.ui.viewmodel.AgencyProductPublishingViewModel(get(), getOrNull()) }
    single { com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel(get(), get(), getOrNull(), getOrNull()) }
    factory { com.mgacreative.touros.ui.viewmodel.AdminDataManagementViewModel(get()) }
}

/**
 * Tüm shared modülleri birleştiren liste.
 * Platform entry point'lerinde `initKoin { modules(sharedModules) }` ile kullanılır.
 */
val sharedModules = listOf(
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
    otaModule
)
