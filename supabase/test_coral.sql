SELECT public.atomic_swap_marketplace_products();

SELECT operator_name, count(*) 
FROM public.marketplace_products 
WHERE product_type = 'PACKAGE_TOUR' 
GROUP BY operator_name 
ORDER BY count(*) DESC;
