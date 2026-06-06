package com.geotrack.poi.service;

import org.springframework.stereotype.Component;

/**
 * 球面大圆距离（Haversine）：在 {@link PoiService#checkIn} 中作为 Redis GEO 不可用时的回退实现。
 */
@Component
public class GeoDistanceService {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    public double distanceMeters(double lng1, double lat1, double lng2, double lat2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
