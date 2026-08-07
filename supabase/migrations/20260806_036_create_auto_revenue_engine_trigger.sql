-- ============================================================
-- TourOS 3.1.2 Auto Revenue Accounting Engine Trigger SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.auto_generate_invoice_on_booking_approval()
RETURNS TRIGGER 
SET search_path = public
AS $$
DECLARE
    v_subtotal NUMERIC(14,2);
    v_tax_amount NUMERIC(14,2);
    v_invoice_code TEXT;
BEGIN
    -- Sadece Onaylandı durumuna geçen rezervasyonlarda çalışır
    IF (NEW.status = 'Onaylandı' OR NEW.status = 'Confirmed') AND 
       (OLD.status IS NULL OR (OLD.status <> 'Onaylandı' AND OLD.status <> 'Confirmed')) THEN

        v_subtotal := ROUND((NEW.total_price / 1.20)::numeric, 2);
        v_tax_amount := NEW.total_price - v_subtotal;
        v_invoice_code := 'INV-' || NEW.booking_code;

        -- Fatura zaten yoksa otomatik oluştur
        IF NOT EXISTS (SELECT 1 FROM public.invoices WHERE booking_id = NEW.id) THEN
            INSERT INTO public.invoices (
                invoice_no,
                booking_id,
                invoice_type,
                customer_name,
                subtotal,
                tax_rate,
                tax_amount,
                total_amount,
                currency,
                status,
                issued_at,
                due_date,
                notes,
                tenant_id
            ) VALUES (
                v_invoice_code,
                NEW.id,
                'sale',
                NEW.customer_name,
                v_subtotal,
                20.00,
                v_tax_amount,
                NEW.total_price,
                NEW.currency,
                'issued',
                NOW(),
                CURRENT_DATE + INTERVAL '7 days',
                'Rezervasyon onayı ile otomatik oluşturan satış faturası (Accounting Engine)',
                NEW.tenant_id
            );
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_auto_revenue_engine ON public.bookings;
CREATE TRIGGER trg_auto_revenue_engine
    AFTER UPDATE OR INSERT ON public.bookings
    FOR EACH ROW EXECUTE FUNCTION public.auto_generate_invoice_on_booking_approval();
