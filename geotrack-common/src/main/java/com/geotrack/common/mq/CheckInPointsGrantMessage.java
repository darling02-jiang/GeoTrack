package com.geotrack.common.mq;

public class CheckInPointsGrantMessage {

    private Long userId;
    private Long checkInRecordId;
    private Integer points;

    public CheckInPointsGrantMessage() {
    }

    public CheckInPointsGrantMessage(Long userId, Long checkInRecordId, Integer points) {
        this.userId = userId;
        this.checkInRecordId = checkInRecordId;
        this.points = points;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCheckInRecordId() {
        return checkInRecordId;
    }

    public void setCheckInRecordId(Long checkInRecordId) {
        this.checkInRecordId = checkInRecordId;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
