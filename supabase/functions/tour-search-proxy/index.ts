// ============================================================================
// TourOS Supabase Edge Function: tour-search-proxy
// DESCRIPTION: Güvenli Dış Operatör (TourVisor/Paximum) API Köprüsü (Proxy).
// Dış operatör şifreleri ve anahtarları istemciden gizlenir, sadece burada saklanır.
// ============================================================================

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

interface ProxySearchRequest {
  action: "LIST_METADATA" | "SEARCH_TOURS" | "HOTEL_DETAIL";
  type?: string; // 'departure', 'country', 'operator', etc.
  departureCityId?: string;
  countryId?: string;
  nightsFrom?: number;
  nightsTo?: number;
  adults?: number;
  children?: number;
  stars?: number[];
  hotelId?: string;
}

serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    // 1. Supabase Client & Kullanıcı Oturumu Doğrulama
    const supabaseUrl = Deno.env.get("SUPABASE_URL") || "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "";
    const authHeader = req.headers.get("Authorization");

    if (!authHeader) {
      return new Response(
        JSON.stringify({ error: true, message: "Yetkisiz istek: Oturum token'ı bulunamadı." }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const supabase = createClient(supabaseUrl, supabaseServiceKey);
    const token = authHeader.replace("Bearer ", "");
    const { data: { user }, error: userError } = await supabase.auth.getUser(token);

    if (userError || !user) {
      return new Response(
        JSON.stringify({ error: true, message: "Geçersiz veya süresi dolmuş oturum." }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 2. Acente Şirket ID'sini Bul
    const { data: userData } = await supabase
      .from("users")
      .select("company_id, role")
      .eq("id", user.id)
      .single();

    const companyId = userData?.company_id;

    // 3. Atomik Kota ve Borç Kontrolü (Güvenlik Kalkanı)
    if (companyId) {
      const { data: quotaResult, error: quotaError } = await supabase.rpc(
        "check_and_increment_agency_quota",
        { p_company_id: companyId }
      );

      if (quotaError || (quotaResult && quotaResult.allowed === false)) {
        return new Response(
          JSON.stringify({
            error: true,
            quota_exceeded: true,
            reason: quotaResult?.reason || "QUOTA_OR_DEBT_BLOCKED",
            message: quotaResult?.message || "Sorgu kotanız dolmuş veya ödenmemiş borcunuz bulunmaktadır.",
          }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }
    }

    // 4. Rate Limiting & Bot İstismar Kalkanı (Dakikada Maksimum 30 İstek)
    const clientIp = req.headers.get("x-forwarded-for") || user.id;
    const rateKey = `rate:search:${user.id || clientIp}`;
    
    const { data: rateResult } = await supabase.rpc(
      "check_and_increment_rate_limit",
      { p_rate_key: rateKey, p_max_requests: 30, p_window_seconds: 60 }
    );

    if (rateResult && rateResult.allowed === false) {
      return new Response(
        JSON.stringify({
          error: true,
          rate_limited: true,
          retry_after: rateResult.retry_after,
          message: rateResult.message || "Çok hızlı arama yaptınız. Lütfen birkaç saniye bekleyin.",
        }),
        {
          status: 429,
          headers: {
            ...corsHeaders,
            "Content-Type": "application/json",
            "Retry-After": String(rateResult.retry_after || 10),
          },
        }
      );
    }

    // 5. Gizli Sunucu API Kimlik Bilgileri (Environment Secrets)
    const tourvisorLogin = Deno.env.get("TOURVISOR_AUTH_LOGIN") || "Mabit23@gmail.com";
    const tourvisorPass = Deno.env.get("TOURVISOR_AUTH_PASS") || "FFytMvSU0ZHr";

    const body: ProxySearchRequest = await req.json();

    // 5. İşlem Türüne Göre Dış Operatör Çağrısı (Maskelenmiş & Güvenli)
    if (body.action === "LIST_METADATA") {
      const listType = body.type || "departure";
      const targetUrl = `http://tourvisor.ru/xml/list.php?authlogin=${encodeURIComponent(tourvisorLogin)}&authpass=${encodeURIComponent(tourvisorPass)}&type=${encodeURIComponent(listType)}&format=json`;

      const response = await fetch(targetUrl, { method: "GET" });
      const data = await response.text();

      return new Response(data, {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    if (body.action === "SEARCH_TOURS") {
      const targetUrl = `http://tourvisor.ru/xml/search.php?authlogin=${encodeURIComponent(tourvisorLogin)}&authpass=${encodeURIComponent(tourvisorPass)}&format=json`;

      const response = await fetch(targetUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      const data = await response.text();

      return new Response(data, {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    return new Response(
      JSON.stringify({ error: true, message: "Geçersiz işlem parametresi." }),
      { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    return new Response(
      JSON.stringify({ error: true, message: err.message || "Sunucu proxy hatası oluştu." }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
