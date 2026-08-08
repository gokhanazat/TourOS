-- ============================================================
-- TourOS Migration: 20260808_014_operator_code_and_tour_code_sequence.sql
-- Prompt 4.6.2: operator_code ve Otomatik Tur Kodu Üretimi
-- companies tablosuna operator_code kolonu ve tur_operatoru tipi için CHECK kısıtı eklenir.
-- Otomatik PREFIX-00001 formatında tur kodu üreten fonksiyon ve trigger.
-- ============================================================

-- 1. operator_code Kolonunu Ekle
ALTER TABLE public.companies 
ADD COLUMN IF NOT EXISTS operator_code TEXT;

-- 2. Tur Operatörleri için operator_code zorunluluğu
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.constraint_column_usage 
        WHERE constraint_name = 'chk_operator_code_for_operator' AND table_name = 'companies'
    ) THEN
        ALTER TABLE public.companies 
        ADD CONSTRAINT chk_operator_code_for_operator 
        CHECK (company_type != 'tur_operatoru' OR (operator_code IS NOT NULL AND length(trim(operator_code)) > 0));
    END IF;
END $$;

-- 3. Otomatik Tur Kodu Üreten Fonksiyon (PREFIX-00001)
CREATE OR REPLACE FUNCTION public.generate_tour_code(target_tenant_id UUID)
RETURNS TEXT AS $$
DECLARE
    prefix_code TEXT;
    next_seq INT;
    generated_code TEXT;
BEGIN
    -- Şirketin operator_code bilgisini al, yoksa fallback 'TUR' kullan
    SELECT COALESCE(UPPER(TRIM(operator_code)), 'TUR') INTO prefix_code
    FROM public.companies
    WHERE id = target_tenant_id;
    
    IF prefix_code IS NULL OR length(prefix_code) = 0 THEN
        prefix_code := 'TUR';
    END IF;

    -- O firmaya ait tur sayısının bir fazlası
    SELECT COUNT(*) + 1 INTO next_seq
    FROM public.tours
    WHERE tenant_id = target_tenant_id;

    generated_code := prefix_code || '-' || LPAD(next_seq::text, 5, '0');
    RETURN generated_code;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- 4. Tours tablosu için BEFORE INSERT Trigger (Kod girilmediyse otomatik üretir)
CREATE OR REPLACE FUNCTION public.trg_auto_set_tour_code()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.code IS NULL OR length(trim(NEW.code)) = 0 THEN
        NEW.code := public.generate_tour_code(NEW.tenant_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_tours_auto_code ON public.tours;
CREATE TRIGGER trg_tours_auto_code
    BEFORE INSERT ON public.tours
    FOR EACH ROW EXECUTE FUNCTION public.trg_auto_set_tour_code();
