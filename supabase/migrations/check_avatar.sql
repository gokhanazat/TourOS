SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'member_travel_preferences';

SELECT proname, proargnames 
FROM pg_proc 
WHERE proname IN ('save_member_avatar', 'save_member_travel_preferences', 'get_member_vip_dashboard_summary');
