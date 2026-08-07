-- ============================================================
-- TourOS 4.4.1 Multi-Language (TR, EN, DE, RU, AR, FR) & RTL RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.app_translations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_code VARCHAR(10) NOT NULL, -- tr, en, de, ru, ar, fr
    translation_key TEXT NOT NULL,
    translation_value TEXT NOT NULL,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_lang_key UNIQUE (language_code, translation_key)
);

-- RPC 1: Supported Languages List
CREATE OR REPLACE FUNCTION public.get_supported_languages()
RETURNS TABLE (
    code VARCHAR(10),
    name TEXT,
    is_rtl BOOLEAN,
    flag_emoji TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 'tr'::VARCHAR(10), 'Türkçe'::TEXT, FALSE, '🇹🇷'::TEXT
    UNION ALL SELECT 'en'::VARCHAR(10), 'English'::TEXT, FALSE, '🇬🇧'::TEXT
    UNION ALL SELECT 'de'::VARCHAR(10), 'Deutsch'::TEXT, FALSE, '🇩🇪'::TEXT
    UNION ALL SELECT 'ru'::VARCHAR(10), 'Русский'::TEXT, FALSE, '🇷🇺'::TEXT
    UNION ALL SELECT 'ar'::VARCHAR(10), 'العربية'::TEXT, TRUE, '🇸🇦'::TEXT
    UNION ALL SELECT 'fr'::VARCHAR(10), 'Français'::TEXT, FALSE, '🇫🇷'::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC 2: Get Translations by Language
CREATE OR REPLACE FUNCTION public.get_app_translations(
    p_language_code VARCHAR(10) DEFAULT 'tr',
    p_tenant_id UUID DEFAULT NULL
)
RETURNS TABLE (
    language_code VARCHAR(10),
    translation_key TEXT,
    translation_value TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 'tr'::VARCHAR(10), 'welcome_title'::TEXT, 'TourOS Seyahat Sistemine Hoş Geldiniz'::TEXT
    UNION ALL SELECT 'en'::VARCHAR(10), 'welcome_title'::TEXT, 'Welcome to TourOS Travel System'::TEXT
    UNION ALL SELECT 'de'::VARCHAR(10), 'welcome_title'::TEXT, 'Willkommen beim TourOS Reisesystem'::TEXT
    UNION ALL SELECT 'ru'::VARCHAR(10), 'welcome_title'::TEXT, 'Добро пожаловать в TourOS'::TEXT
    UNION ALL SELECT 'ar'::VARCHAR(10), 'welcome_title'::TEXT, 'مرحبا بكم في نظام توروس للسياحة'::TEXT
    UNION ALL SELECT 'fr'::VARCHAR(10), 'welcome_title'::TEXT, 'Bienvenue dans le système TourOS'::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
