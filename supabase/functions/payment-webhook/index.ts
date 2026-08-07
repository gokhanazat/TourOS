// Supabase Edge Function: payment-webhook/index.ts
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

serve(async (req) => {
  try {
    const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? ''
    const supabaseServiceKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    const supabase = createClient(supabaseUrl, supabaseServiceKey)

    const payload = await req.json()
    const gateway = req.headers.get("x-gateway-provider") ?? "stripe"
    const eventType = payload.type ?? "payment_intent.succeeded"
    const linkCode = payload.data?.object?.metadata?.link_code ?? payload.link_code ?? "cs_live_981238"
    const txId = payload.data?.object?.id ?? payload.transaction_id ?? `tx_${Date.now()}`

    const { data, error } = await supabase.rpc('handle_payment_webhook_callback', {
      p_payment_link_code: linkCode,
      p_transaction_id: txId,
      p_gateway_provider: gateway,
      p_event_type: eventType,
      p_payload_json: payload
    })

    if (error) throw error

    return new Response(JSON.stringify({ success: true, result: data }), {
      headers: { "Content-Type": "application/json" },
      status: 200,
    })
  } catch (err) {
    return new Response(JSON.stringify({ success: false, error: err.message }), {
      headers: { "Content-Type": "application/json" },
      status: 400,
    })
  }
})
