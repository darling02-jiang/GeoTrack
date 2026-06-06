USE geotrack;

-- 普通兑换商品（is_seckill = 0）
INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT '游踪定制贴纸包', 50, 300, 0, NULL, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = '游踪定制贴纸包');

INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT '景区明信片套装', 88, 200, 0, NULL, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = '景区明信片套装');

INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT '古风金属书签', 150, 120, 0, NULL, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = '古风金属书签');

INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT 'POI打卡纪念徽章', 220, 150, 0, NULL, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = 'POI打卡纪念徽章');

INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT '旅行收纳袋三件套', 380, 80, 0, NULL, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = '旅行收纳袋三件套');

INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT '文创随行保温杯', 680, 45, 0, NULL, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = '文创随行保温杯');

INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT '便携充电宝 10000mAh', 1680, 30, 0, NULL, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = '便携充电宝 10000mAh');

INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT '文旅联名遮阳帽', 420, 55, 0, NULL, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = '文旅联名遮阳帽');

-- 补充一条秒杀（与 V5 中联票区分）
INSERT INTO gt_goods (name, points_price, stock, is_seckill, begin_time, end_time, status)
SELECT '早鸟文创礼盒-秒杀', 299, 15, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1
WHERE NOT EXISTS (SELECT 1 FROM gt_goods WHERE name = '早鸟文创礼盒-秒杀');
