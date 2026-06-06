USE geotrack;

ALTER TABLE gt_feed
    ADD COLUMN check_in_record_id BIGINT NULL AFTER id,
    ADD UNIQUE KEY uk_check_in_record (check_in_record_id);
