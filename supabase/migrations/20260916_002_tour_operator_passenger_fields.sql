-- 20260916_002_tour_operator_passenger_fields.sql
-- Tour Operatörleri için gerekli yolcu alanları

ALTER TABLE public.passengers
ADD COLUMN IF NOT EXISTS passport_series TEXT,
ADD COLUMN IF NOT EXISTS document_issued_by TEXT,
ADD COLUMN IF NOT EXISTS citizenship TEXT;
