-- 手动插入测试打卡点（已有库可执行本文件；重复执行会再插入一批，可先按下方删除）
-- mysql -h127.0.0.1 -uroot -p geotrack < scripts/mysql/insert_test_pois.sql

USE geotrack;

-- 可选：删除历史测试数据后再插入
-- DELETE FROM gt_poi WHERE description LIKE '测试数据%';

INSERT INTO gt_poi (name, longitude, latitude, radius_meters, reward_points, description, status) VALUES
('断桥观景平台', 120.147600, 30.259800, 500, 20, '测试数据-杭州西湖', 1),
('雷峰塔景区', 120.148500, 30.231100, 400, 30, '测试数据-杭州', 1),
('文峰塔', 118.063154, 24.440716, 500, 50, '测试数据-厦门', 1),
('鼓浪屿龙头路', 118.065800, 24.447200, 350, 25, '测试数据-厦门', 1),
('环岛路木栈道', 118.132000, 24.448500, 600, 15, '测试数据-厦门', 1),
('南普陀寺', 118.098500, 24.441800, 450, 35, '测试数据-厦门', 1);
