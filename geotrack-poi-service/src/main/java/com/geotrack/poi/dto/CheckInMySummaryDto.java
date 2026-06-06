package com.geotrack.poi.dto;

import java.util.List;

public record CheckInMySummaryDto(List<String> checkedDates, long distinctPoiCount, long totalSuccessCount) {
}
