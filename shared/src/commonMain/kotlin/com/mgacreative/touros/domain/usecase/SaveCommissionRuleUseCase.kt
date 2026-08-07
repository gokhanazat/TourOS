package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.CommissionRule
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 3.1.6 Komisyon Kuralı Ekleme / Güncelleme Use Case.
 */
class SaveCommissionRuleUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(rule: CommissionRule): Result<CommissionRule> {
        if (rule.ruleName.isBlank()) {
            return Result.failure(IllegalArgumentException("Kural adı boş olamaz."))
        }
        return runCatching {
            if (rule.id.isBlank()) {
                val created = supabaseClient.postgrest.from("commission_rules")
                    .insert(rule) { select() }
                    .decodeSingle<CommissionRule>()
                created
            } else {
                supabaseClient.postgrest.from("commission_rules")
                    .update(rule) { filter { eq("id", rule.id) } }
                rule
            }
        }
    }
}
