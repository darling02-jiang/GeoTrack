package com.geotrack.common.mq;

public final class RocketMqTopics {

    public static final String CHECKIN_POINTS_TOPIC = "geotrack-checkin-points";
    public static final String CHECKIN_FEED_TOPIC = "geotrack-checkin-feed";
    public static final String SECKILL_ORDER_CREATE_TOPIC = "geotrack-seckill-order-create";
    public static final String MALL_POINTS_DEDUCT_TOPIC = "geotrack-mall-points-deduct";
    public static final String MALL_POINTS_DEDUCT_RESULT_TOPIC = "geotrack-mall-points-deduct-result";

    public static final String POI_CHECKIN_PRODUCER_GROUP = "geotrack-poi-checkin-producer";
    public static final String MALL_SECKILL_PRODUCER_GROUP = "geotrack-mall-seckill-producer";
    public static final String AUTH_POINTS_CONSUMER_GROUP = "geotrack-auth-points-consumer";
    public static final String CONTENT_FEED_CONSUMER_GROUP = "geotrack-content-feed-consumer";
    public static final String MALL_SECKILL_ORDER_CONSUMER_GROUP = "geotrack-mall-seckill-order-consumer";
    public static final String AUTH_MALL_POINTS_CONSUMER_GROUP = "geotrack-auth-mall-points-consumer";
    public static final String MALL_POINTS_RESULT_CONSUMER_GROUP = "geotrack-mall-points-result-consumer";

    private RocketMqTopics() {
    }
}
