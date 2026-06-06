USE geotrack;

CREATE TABLE IF NOT EXISTS gt_mq_consume_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consumer_group VARCHAR(128) NOT NULL,
    message_key VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    error_message VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_consumer_message (consumer_group, message_key)
);
