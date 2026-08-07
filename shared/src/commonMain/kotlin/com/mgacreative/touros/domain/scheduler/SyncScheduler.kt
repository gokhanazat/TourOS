package com.mgacreative.touros.domain.scheduler

/**
 * 4.5.9 Background Worker ve SyncScheduler expect sınıfı.
 * Android WorkManager ve iOS BGTaskScheduler için ortak zamanlayıcı arayüzü.
 */
expect class SyncScheduler() {
    fun schedulePeriodicSync(intervalMinutes: Long, tenantId: String)
    fun scheduleRetrySync(accountId: String, tenantId: String)
    fun triggerManualSync(accountId: String, tenantId: String)
    fun cancelAllSync()
}
