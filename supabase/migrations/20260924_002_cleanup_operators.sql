UPDATE public.canonical_operators 
SET aliases = array_append(aliases, 'biblio-globus') 
WHERE id = 1 AND NOT ('biblio-globus' = ANY(aliases));

UPDATE public.marketplace_products 
SET operator_name = public.normalize_operator_name(operator_name, NULL) 
WHERE public.normalize_operator_name(operator_name, NULL) IS NOT NULL;

DELETE FROM public.marketplace_products 
WHERE operator_name NOT IN ('Bibloglobus', 'Anex', 'Coral Travel', 'Sunmar', 'Fun&Sun (Ru)', 'Kazunion', 'Loti', 'Pegas Touristik', 'Интурист')
  AND product_type != 'FLIGHT';

SELECT operator_name, count(*) 
FROM public.marketplace_products 
GROUP BY operator_name 
ORDER BY count(*) DESC;
