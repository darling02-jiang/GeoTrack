USE geotrack;

ALTER TABLE gt_point_flow
    ADD UNIQUE KEY uk_user_biz (user_id, biz_type, biz_no);
