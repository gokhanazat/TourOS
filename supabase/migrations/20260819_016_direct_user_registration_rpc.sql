-- ============================================================================
-- ACENTE KAYIT (SIGNUP) MAILER BYPASS & EVRENSEL DOĞRUDAN RPC FONKSİYONU
-- Dosya: supabase/migrations/20260819_016_direct_user_registration_rpc.sql
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Eski fonksiyon imzalarını temizle
DROP FUNCTION IF EXISTS public.register_new_user(jsonb);
DROP FUNCTION IF EXISTS public.register_new_user(text, text, text);
DROP FUNCTION IF EXISTS public.register_new_user(text, text);
DROP FUNCTION IF EXISTS public.register_new_user;

-- 1. PostgREST Evrensel JSONB Parametreli Kayıt Fonksiyonu
CREATE OR REPLACE FUNCTION public.register_new_user(payload jsonb)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, auth, extensions
AS $$
DECLARE
    v_email text := LOWER(TRIM(COALESCE(payload->>'p_email', payload->>'email')));
    v_full_name text := COALESCE(NULLIF(TRIM(COALESCE(payload->>'p_full_name', payload->>'full_name', payload->>'fullName')), ''), SPLIT_PART(v_email, '@', 1));
    v_password text := COALESCE(payload->>'p_password', payload->>'password');
    v_user_id uuid := gen_random_uuid();
    v_company_id uuid := gen_random_uuid();
    v_encrypted_pw text;
BEGIN
    -- E-posta kontrolü
    IF v_email IS NULL OR v_email = '' THEN
        RAISE EXCEPTION 'E-posta adresi boş olamaz.';
    END IF;

    -- Şifre kontrolü
    IF v_password IS NULL OR LENGTH(v_password) < 6 THEN
        RAISE EXCEPTION 'Şifreniz en az 6 karakter olmalıdır.';
    END IF;

    -- Mevcut kullanıcı kontrolü
    IF EXISTS (SELECT 1 FROM auth.users WHERE email = v_email) THEN
        RAISE EXCEPTION 'Bu e-posta adresiyle zaten kayıtlı bir hesap mevcut.';
    END IF;

    -- Bcrypt Hash
    v_encrypted_pw := crypt(v_password, gen_salt('bf', 10));

    -- auth.users doğrudan ekle
    INSERT INTO auth.users (
        instance_id,
        id,
        aud,
        role,
        email,
        encrypted_password,
        email_confirmed_at,
        last_sign_in_at,
        raw_app_meta_data,
        raw_user_meta_data,
        is_super_admin,
        created_at,
        updated_at
    ) VALUES (
        '00000000-0000-0000-0000-000000000000',
        v_user_id,
        'authenticated',
        'authenticated',
        v_email,
        v_encrypted_pw,
        now(),
        now(),
        '{"provider":"email","providers":["email"]}'::jsonb,
        json_build_object(
            'full_name', v_full_name,
            'role', 'AGENT',
            'tenant_id', v_company_id::text,
            'is_approved', false
        )::jsonb,
        false,
        now(),
        now()
    );

    -- public.companies tablosuna ekle
    INSERT INTO public.companies (
        id,
        name,
        company_type,
        email,
        is_active,
        created_at,
        updated_at
    ) VALUES (
        v_company_id,
        v_full_name || ' (Acente Başvurusu)',
        'AGENCY',
        v_email,
        false,
        now(),
        now()
    ) ON CONFLICT DO NOTHING;

    -- public.users tablosuna ekle
    INSERT INTO public.users (
        id,
        auth_id,
        email,
        full_name,
        is_active,
        tenant_id,
        created_at,
        updated_at
    ) VALUES (
        v_user_id,
        v_user_id,
        v_email,
        v_full_name,
        false,
        v_company_id,
        now(),
        now()
    ) ON CONFLICT (auth_id) DO UPDATE 
    SET full_name = EXCLUDED.full_name,
        tenant_id = EXCLUDED.tenant_id,
        updated_at = now();

    RETURN json_build_object(
        'success', true,
        'user_id', v_user_id,
        'email', v_email,
        'tenant_id', v_company_id
    )::jsonb;
END;
$$;

-- 2. Çoklu Parametre İmza Desteği (p_email, p_full_name, p_password)
CREATE OR REPLACE FUNCTION public.register_new_user(
    p_email text,
    p_full_name text DEFAULT '',
    p_password text DEFAULT ''
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, auth, extensions
AS $$
BEGIN
    RETURN public.register_new_user(
        json_build_object(
            'p_email', p_email,
            'p_full_name', p_full_name,
            'p_password', p_password
        )::jsonb
    );
END;
$$;

-- 3. Yetkilendirme
GRANT EXECUTE ON FUNCTION public.register_new_user(jsonb) TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.register_new_user(text, text, text) TO anon, authenticated, service_role;

-- 4. PostgREST Schema Cache Yenileme
NOTIFY pgrst, 'reload schema';
NOTIFY pgrst, 'reload config';
