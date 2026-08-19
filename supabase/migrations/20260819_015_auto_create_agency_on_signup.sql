-- ============================================================================
-- ACENTE KAYIT (SIGNUP) OTOMATİK İZOLE ŞİRKET (TENANT) OLUŞTURMA TRİGGERI
-- Dosya: supabase/migrations/20260819_015_auto_create_agency_on_signup.sql
-- ============================================================================

CREATE OR REPLACE FUNCTION public.handle_new_user_registration()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, auth
AS $$
DECLARE
    v_user_email text := LOWER(TRIM(NEW.email));
    v_full_name text := COALESCE(NULLIF(TRIM(NEW.raw_user_meta_data->>'full_name'), ''), SPLIT_PART(v_user_email, '@', 1));
    v_company_id uuid;
    v_is_admin boolean := (v_user_email = 'gkhnazat@gmail.com');
BEGIN
    IF v_is_admin THEN
        v_company_id := '00000000-0000-0000-0000-000000000001'::uuid;
        
        INSERT INTO public.users (id, auth_id, email, full_name, is_active, tenant_id, created_at, updated_at)
        VALUES (
            NEW.id,
            NEW.id,
            v_user_email,
            'Sistem Yöneticisi (Super Admin)',
            true,
            v_company_id,
            now(),
            now()
        )
        ON CONFLICT (auth_id) DO UPDATE 
        SET is_active = true, updated_at = now();

        NEW.raw_user_meta_data := jsonb_set(
            jsonb_set(COALESCE(NEW.raw_user_meta_data, '{}'::jsonb), '{role}', '"SYSTEM_ADMIN"'),
            '{tenant_id}', to_jsonb(v_company_id::text)
        );
    ELSE
        -- Yeni Acente Şirketi Oluştur (İzole SaaS Tenant)
        v_company_id := gen_random_uuid();
        
        INSERT INTO public.companies (
            id,
            tenant_id,
            slug,
            name,
            company_type,
            email,
            is_active,
            default_master_agency_code,
            created_at,
            updated_at
        ) VALUES (
            v_company_id,
            v_company_id,
            'agency-' || substr(v_company_id::text, 1, 8),
            v_full_name || ' Seyahat Acentesi',
            'acente',
            v_user_email,
            true,
            NULL,
            now(),
            now()
        );

        -- Kullanıcı Profilini Ekle
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
            NEW.id,
            NEW.id,
            v_user_email,
            v_full_name,
            true,
            v_company_id,
            now(),
            now()
        )
        ON CONFLICT (auth_id) DO UPDATE 
        SET tenant_id = EXCLUDED.tenant_id,
            updated_at = now();

        -- Meta Veriyi Güncelle
        NEW.raw_user_meta_data := jsonb_set(
            jsonb_set(
                jsonb_set(COALESCE(NEW.raw_user_meta_data, '{}'::jsonb), '{role}', '"AGENT"'),
                '{tenant_id}', to_jsonb(v_company_id::text)
            ),
            '{is_approved}', 'true'::jsonb
        );
    END IF;

    RETURN NEW;
EXCEPTION WHEN OTHERS THEN
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
    BEFORE INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user_registration();
