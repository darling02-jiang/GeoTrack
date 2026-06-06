package com.geotrack.common.mq;

public class MallPointsDeductMessage {

    private String messageKey;
    private String orderNo;
    private Long userId;
    private Long goodsId;
    private Integer points;

    public MallPointsDeductMessage() {
    }

    public MallPointsDeductMessage(String messageKey, String orderNo, Long userId, Long goodsId, Integer points) {
        this.messageKey = messageKey;
        this.orderNo = orderNo;
        this.userId = userId;
        this.goodsId = goodsId;
        this.points = points;
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

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
