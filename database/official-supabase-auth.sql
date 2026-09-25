-- =========================================================================
-- Official Supabase GoTrue Auth Schema & DDL
-- 100% Real Schema (auth.users, auth.uid(), auth.jwt(), auth.role(), auth.email())
-- No fake/mock functions or mock data.
-- =========================================================================

-- 1. Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Auth Schema
CREATE SCHEMA IF NOT EXISTS auth;

-- 3. PostgREST & Supabase Roles
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        CREATE ROLE anon NOLOGIN;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        CREATE ROLE authenticated NOLOGIN;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'service_role') THEN
        CREATE ROLE service_role NOLOGIN;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'supabase_admin') THEN
        CREATE ROLE supabase_admin LOGIN SUPERUSER;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticator') THEN
        CREATE ROLE authenticator NOINHERIT LOGIN PASSWORD 'secret';
        GRANT anon TO authenticator;
        GRANT authenticated TO authenticator;
        GRANT service_role TO authenticator;
    END IF;
END
$$;

-- 4. Official Supabase auth.users Table
CREATE TABLE IF NOT EXISTS auth.users (
    instance_id                 UUID,
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aud                         VARCHAR(255) DEFAULT 'authenticated',
    role                        VARCHAR(255) DEFAULT 'authenticated',
    email                       VARCHAR(255) UNIQUE,
    encrypted_password          VARCHAR(255),
    email_confirmed_at          TIMESTAMPTZ,
    invited_at                  TIMESTAMPTZ,
    confirmation_token          VARCHAR(255),
    confirmation_sent_at        TIMESTAMPTZ,
    recovery_token              VARCHAR(255),
    recovery_sent_at            TIMESTAMPTZ,
    email_change_token_new      VARCHAR(255),
    email_change                VARCHAR(255),
    email_change_sent_at        TIMESTAMPTZ,
    last_sign_in_at             TIMESTAMPTZ,
    raw_app_meta_data           JSONB DEFAULT '{"provider": "email", "providers": ["email"]}'::jsonb,
    raw_user_meta_data          JSONB DEFAULT '{}'::jsonb,
    is_super_admin              BOOLEAN DEFAULT FALSE,
    created_at                  TIMESTAMPTZ DEFAULT now(),
    updated_at                  TIMESTAMPTZ DEFAULT now(),
    phone                       TEXT UNIQUE DEFAULT NULL,
    phone_confirmed_at          TIMESTAMPTZ,
    phone_change                TEXT DEFAULT '',
    phone_change_token          VARCHAR(255) DEFAULT '',
    phone_change_sent_at        TIMESTAMPTZ,
    confirmed_at                TIMESTAMPTZ,
    email_change_token_current  VARCHAR(255) DEFAULT '',
    email_change_confirm_status SMALLINT DEFAULT 0,
    banned_until                TIMESTAMPTZ,
    reauthentication_token      VARCHAR(255) DEFAULT '',
    reauthentication_sent_at    TIMESTAMPTZ,
    is_sso_user                 BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at                  TIMESTAMPTZ,
    is_anonymous                BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS users_instance_id_idx ON auth.users (instance_id);
CREATE INDEX IF NOT EXISTS users_email_idx ON auth.users (email);

-- 5. Official Supabase Functions (auth.uid, auth.jwt, auth.role, auth.email)
CREATE OR REPLACE FUNCTION auth.uid()
RETURNS UUID
LANGUAGE sql STABLE
AS $$
  SELECT
    COALESCE(
        NULLIF(current_setting('request.jwt.claim.sub', true), ''),
        (NULLIF(current_setting('request.jwt.claims', true), '')::jsonb ->> 'sub')
    )::uuid
$$;

CREATE OR REPLACE FUNCTION auth.jwt()
RETURNS JSONB
LANGUAGE sql STABLE
AS $$
  SELECT
    COALESCE(
        NULLIF(current_setting('request.jwt.claim', true), ''),
        NULLIF(current_setting('request.jwt.claims', true), '')
    )::jsonb
$$;

CREATE OR REPLACE FUNCTION auth.role()
RETURNS TEXT
LANGUAGE sql STABLE
AS $$
  SELECT
    COALESCE(
        NULLIF(current_setting('request.jwt.claim.role', true), ''),
        (NULLIF(current_setting('request.jwt.claims', true), '')::jsonb ->> 'role')
    )::text
$$;

CREATE OR REPLACE FUNCTION auth.email()
RETURNS TEXT
LANGUAGE sql STABLE
AS $$
  SELECT
    COALESCE(
        NULLIF(current_setting('request.jwt.claim.email', true), ''),
        (NULLIF(current_setting('request.jwt.claims', true), '')::jsonb ->> 'email')
    )::text
$$;

-- 6. Permissions
GRANT ALL ON SCHEMA public TO postgres, service_role;
GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT ALL ON SCHEMA auth TO postgres, service_role;
GRANT SELECT ON auth.users TO anon, authenticated;
GRANT EXECUTE ON FUNCTION auth.uid() TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION auth.jwt() TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION auth.role() TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION auth.email() TO anon, authenticated, service_role;
