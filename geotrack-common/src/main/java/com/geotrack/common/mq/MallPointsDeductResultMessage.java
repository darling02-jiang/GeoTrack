package com.geotrack.common.mq;

public class MallPointsDeductResultMessage {

    private String messageKey;
    private String orderNo;
    private Long userId;
    private Long goodsId;
    private Boolean success;
    private String reason;

    public MallPointsDeductResultMessage() {
    }

    public MallPointsDeductResultMessage(String messageKey, String orderNo, Long userId, Long goodsId, Boolean success, String reason) {
        this.messageKey = messageKey;
        this.orderNo = orderNo;
        this.userId = userId;
        this.goodsId = goodsId;
        this.success = success;
        this.reason = reason;
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

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
