USE geotrack;

CREATE TABLE IF NOT EXISTS gt_order_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    from_status VARCHAR(32) NULL,
    to_status VARCHAR(32) NOT NULL,
    reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_order_status_log_order_no (order_no),
    KEY idx_order_status_log_created_at (created_at)
);
