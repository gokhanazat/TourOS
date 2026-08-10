-- 1. tours tablosunda kapak görseli sütununun varlığından emin olunması
ALTER TABLE public.tours ADD COLUMN IF NOT EXISTS cover_image_url TEXT;

-- 2. Storage Bucket 'tour-covers' kamuya açık olarak tanımlama
INSERT INTO storage.buckets (id, name, public)
VALUES ('tour-covers', 'tour-covers', true)
ON CONFLICT (id) DO NOTHING;

-- 3. Storage RLS Erişim Politikaları
DROP POLICY IF EXISTS "tour_covers_public_read" ON storage.objects;
CREATE POLICY "tour_covers_public_read" ON storage.objects 
    FOR SELECT USING (bucket_id = 'tour-covers');

DROP POLICY IF EXISTS "tour_covers_auth_insert" ON storage.objects;
CREATE POLICY "tour_covers_auth_insert" ON storage.objects 
    FOR INSERT WITH CHECK (bucket_id = 'tour-covers');

DROP POLICY IF EXISTS "tour_covers_auth_update" ON storage.objects;
CREATE POLICY "tour_covers_auth_update" ON storage.objects 
    FOR UPDATE USING (bucket_id = 'tour-covers');

-- 4. Itineraries tablosu RLS politikaları doğrulaması
ALTER TABLE public.itineraries ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "itineraries_all_policy" ON public.itineraries;
CREATE POLICY "itineraries_all_policy" ON public.itineraries
    FOR ALL USING (true) WITH CHECK (true);

-- 5. Departures tablosu RLS politikaları doğrulaması
ALTER TABLE public.departures ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "departures_all_policy" ON public.departures;
CREATE POLICY "departures_all_policy" ON public.departures
    FOR ALL USING (true) WITH CHECK (true);
