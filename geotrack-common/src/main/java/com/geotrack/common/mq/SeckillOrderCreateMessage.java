package com.geotrack.common.mq;

public class SeckillOrderCreateMessage {

    private String messageKey;
    private String orderNo;
    private Long userId;
    private Long goodsId;
    private String idempotencyKey;

    public SeckillOrderCreateMessage() {
    }

    public SeckillOrderCreateMessage(String messageKey, String orderNo, Long userId, Long goodsId, String idempotencyKey) {
        this.messageKey = messageKey;
        this.orderNo = orderNo;
        this.userId = userId;
        this.goodsId = goodsId;
        this.idempotencyKey = idempotencyKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
