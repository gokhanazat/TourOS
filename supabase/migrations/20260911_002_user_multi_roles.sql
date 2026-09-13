-- ==============================================================================
-- TourOS Migration: 20260911_002_user_multi_roles.sql
-- Description: Kullanıcılara birden fazla rol atama (roles TEXT[]) desteği
-- ==============================================================================

-- 1. users tablosuna çoklu rol dizisi ekleme
ALTER TABLE public.users 
ADD COLUMN IF NOT EXISTS roles TEXT[] DEFAULT '{}';

-- 2. Mevcut kullanıcıların birincil rollerini diziye aktarma (Geriye dönük uyumluluk)
UPDATE public.users 
SET roles = ARRAY[role_id] 
WHERE (roles IS NULL OR roles = '{}') AND role_id IS NOT NULL;

-- 3. Hızlı sorgulama için GIN indeksi
CREATE INDEX IF NOT EXISTS idx_users_roles ON public.users USING GIN (roles);
