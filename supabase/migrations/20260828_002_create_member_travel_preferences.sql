-- ============================================================================
-- TourOS Migration: 20260828_002_create_member_travel_preferences.sql
-- Description: Axileto Club Member Profile, Travel Tendency Survey & Photo Gallery
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.member_travel_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    email TEXT NOT NULL,
    phone TEXT,
    full_name TEXT,
    passport_no TEXT,
    birth_date DATE,
    city TEXT,
    holiday_concepts TEXT[] DEFAULT '{}',
    transport_preferences TEXT[] DEFAULT '{}',
    favorite_destinations TEXT[] DEFAULT '{}',
    budget_range TEXT DEFAULT '₺45.000 - ₺90.000',
    travel_group TEXT DEFAULT 'Aile (Çocuklu)',
    special_requests TEXT[] DEFAULT '{}',
    inspiration_photos TEXT[] DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (tenant_id, email)
);

ALTER TABLE public.member_travel_preferences ENABLE ROW LEVEL SECURITY;

DO $$ BEGIN
    DROP POLICY IF EXISTS "Public can view own member travel preferences" ON public.member_travel_preferences;
    DROP POLICY IF EXISTS "Users can upsert own member travel preferences" ON public.member_travel_preferences;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

CREATE POLICY "Public can view own member travel preferences"
    ON public.member_travel_preferences FOR SELECT
    USING (true);

CREATE POLICY "Users can upsert own member travel preferences"
    ON public.member_travel_preferences FOR ALL
    USING (true)
    WITH CHECK (true);

-- Upsert Member Preferences RPC
CREATE OR REPLACE FUNCTION public.save_member_travel_preferences(
    p_email TEXT,
    p_phone TEXT DEFAULT NULL,
    p_full_name TEXT DEFAULT NULL,
    p_passport_no TEXT DEFAULT NULL,
    p_city TEXT DEFAULT NULL,
    p_holiday_concepts TEXT[] DEFAULT '{}',
    p_transport_preferences TEXT[] DEFAULT '{}',
    p_favorite_destinations TEXT[] DEFAULT '{}',
    p_budget_range TEXT DEFAULT '₺45.000 - ₺90.000',
    p_travel_group TEXT DEFAULT 'Aile (Çocuklu)',
    p_special_requests TEXT[] DEFAULT '{}',
    p_inspiration_photos TEXT[] DEFAULT '{}'
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_clean_email TEXT := LOWER(TRIM(COALESCE(p_email, '')));
    v_result JSONB;
BEGIN
    IF v_clean_email = '' THEN
        RETURN jsonb_build_object('success', false, 'error', 'Email is required');
    END IF;

    INSERT INTO public.member_travel_preferences (
        email, phone, full_name, passport_no, city,
        holiday_concepts, transport_preferences, favorite_destinations,
        budget_range, travel_group, special_requests, inspiration_photos,
        updated_at
    )
    VALUES (
        v_clean_email, p_phone, p_full_name, p_passport_no, p_city,
        p_holiday_concepts, p_transport_preferences, p_favorite_destinations,
        p_budget_range, p_travel_group, p_special_requests, p_inspiration_photos,
        now()
    )
    ON CONFLICT (tenant_id, email) DO UPDATE SET
        phone = EXCLUDED.phone,
        full_name = EXCLUDED.full_name,
        passport_no = EXCLUDED.passport_no,
        city = EXCLUDED.city,
        holiday_concepts = EXCLUDED.holiday_concepts,
        transport_preferences = EXCLUDED.transport_preferences,
        favorite_destinations = EXCLUDED.favorite_destinations,
        budget_range = EXCLUDED.budget_range,
        travel_group = EXCLUDED.travel_group,
        special_requests = EXCLUDED.special_requests,
        inspiration_photos = EXCLUDED.inspiration_photos,
        updated_at = now();

    RETURN jsonb_build_object('success', true, 'email', v_clean_email);
END;
$$;

GRANT EXECUTE ON FUNCTION public.save_member_travel_preferences TO anon, authenticated, service_role;
