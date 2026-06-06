USE geotrack;

INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT '文创帆布袋', 500, 100, 0, NULL, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = '文创帆布袋');

INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT '限量景区联票-秒杀', 100, 20, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = '限量景区联票-秒杀');
