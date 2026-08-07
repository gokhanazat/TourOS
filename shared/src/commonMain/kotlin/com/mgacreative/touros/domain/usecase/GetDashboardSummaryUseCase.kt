package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.DashboardSummary
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.repository.DashboardRepository

class GetDashboardSummaryUseCase(
    private val dashboardRepository: DashboardRepository
) {
    suspend operator fun invoke(tenantId: String, userRole: UserRole = UserRole.TOUR_OPERATOR): Result<DashboardSummary> {
        val effectiveTenant = if (userRole == UserRole.SYSTEM_ADMIN && tenantId.isBlank()) {
            "ALL"
        } else {
            tenantId
        }
        return dashboardRepository.getDashboardSummary(effectiveTenant)
    }
}
