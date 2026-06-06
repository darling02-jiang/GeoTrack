package com.geotrack.common.mq;

public class CheckInFeedCreateMessage {

    private Long checkInRecordId;
    private Long userId;
    private Long poiId;
    private String content;
    private String imageUrl;

    public CheckInFeedCreateMessage() {
    }

    public CheckInFeedCreateMessage(Long checkInRecordId, Long userId, Long poiId, String content, String imageUrl) {
        this.checkInRecordId = checkInRecordId;
        this.userId = userId;
        this.poiId = poiId;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public Long getCheckInRecordId() {
        return checkInRecordId;
    }

    public void setCheckInRecordId(Long checkInRecordId) {
        this.checkInRecordId = checkInRecordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPoiId() {
        return poiId;
    }

    public void setPoiId(Long poiId) {
        this.poiId = poiId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
