-- ============================================================================
-- TourOS Migration: 20260821_004_rate_limiting_and_bot_shield.sql
-- DESCRIPTION: Sunucu Katmanında Rate Limiting & Bot İstismar Koruması
-- HEDEF: Acentelerin veya botların sisteme dakikada 30'dan fazla arama yapmasını engelleyerek
-- dış API maliyet patlamalarını ve DDoS yükünü önler.
-- ============================================================================

-- 1. Rate Limiting Takip Tablosu
CREATE TABLE IF NOT EXISTS public.api_rate_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rate_key TEXT UNIQUE NOT NULL, -- IP veya UserID + Endpoint
    request_count INTEGER NOT NULL DEFAULT 1,
    window_start TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_api_rate_limits_key ON public.api_rate_limits(rate_key);
CREATE INDEX IF NOT EXISTS idx_api_rate_limits_window ON public.api_rate_limits(window_start);

-- 2. Atomik Rate Limit Kontrol ve Artırma RPC Fonksiyonu
CREATE OR REPLACE FUNCTION public.check_and_increment_rate_limit(
    p_rate_key TEXT,
    p_max_requests INTEGER DEFAULT 30,
    p_window_seconds INTEGER DEFAULT 60
)
RETURNS JSONB
SET search_path = public
AS $$
DECLARE
    v_record RECORD;
    v_now TIMESTAMPTZ := NOW();
    v_elapsed INTEGER;
BEGIN
    SELECT * INTO v_record FROM public.api_rate_limits WHERE rate_key = p_rate_key FOR UPDATE;

    IF NOT FOUND THEN
        INSERT INTO public.api_rate_limits (rate_key, request_count, window_start, updated_at)
        VALUES (p_rate_key, 1, v_now, v_now);

        RETURN jsonb_build_object(
            'allowed', true,
            'current_requests', 1,
            'max_requests', p_max_requests,
            'retry_after', 0
        );
    END IF;

    v_elapsed := EXTRACT(EPOCH FROM (v_now - v_record.window_start))::INTEGER;

    -- Pencere süresi (60 sn) dolduysa sayacı sıfırla
    IF v_elapsed >= p_window_seconds THEN
        UPDATE public.api_rate_limits
        SET request_count = 1,
            window_start = v_now,
            updated_at = v_now
        WHERE rate_key = p_rate_key;

        RETURN jsonb_build_object(
            'allowed', true,
            'current_requests', 1,
            'max_requests', p_max_requests,
            'retry_after', 0
        );
    END IF;

    -- Limit aşıldıysa engelle
    IF v_record.request_count >= p_max_requests THEN
        RETURN jsonb_build_object(
            'allowed', false,
            'current_requests', v_record.request_count,
            'max_requests', p_max_requests,
            'retry_after', (p_window_seconds - v_elapsed),
            'message', format('Çok fazla istek yapıldı. Lütfen %s saniye bekleyin.', (p_window_seconds - v_elapsed))
        );
    END IF;

    -- Limit uygunsa sayacı +1 artır
    UPDATE public.api_rate_limits
    SET request_count = request_count + 1,
        updated_at = v_now
    WHERE rate_key = p_rate_key;

    RETURN jsonb_build_object(
        'allowed', true,
        'current_requests', v_record.request_count + 1,
        'max_requests', p_max_requests,
        'retry_after', 0
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Eski Rate Limit Kayıtlarını Temizleyen Fonksiyon (Her gün çalışır)
CREATE OR REPLACE FUNCTION public.cleanup_old_rate_limits()
RETURNS VOID
SET search_path = public
AS $$
BEGIN
    DELETE FROM public.api_rate_limits 
    WHERE window_start < NOW() - INTERVAL '1 hour';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
