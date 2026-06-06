USE geotrack;

CREATE TABLE IF NOT EXISTS gt_feed_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    feed_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_feed_user (feed_id, user_id),
    KEY idx_feed (feed_id)
);
