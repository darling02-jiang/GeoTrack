package com.geotrack.poi.service;

import com.geotrack.common.exception.BizException;
import com.geotrack.poi.entity.PoiEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Redis GEO 关键实现（需求文档 6.2 / 10.2）：
 * <ul>
 *   <li>所有启用 POI 的中心点写入同一 key（底层为 ZSET + geohash）；member 固定为 {@code poi:{id}}</li>
 *   <li>附近查询：{@code GEORADIUS}，以用户坐标为圆心、按米过滤</li>
 *   <li>打卡距离：临时写入用户坐标为 member，{@code GEODIST} 与目标 POI 求米级距离后删除临时 member</li>
 * </ul>
 * 与 Java Haversine 在米级可能略有差异，业务侧已留容差；Redis 不可用时由 {@link PoiService} 回退本地计算。
 */
@Service
public class PoiGeoRedisService {

    private static final Logger log = LoggerFactory.getLogger(PoiGeoRedisService.class);

    private final StringRedisTemplate redisTemplate;

    @Value("${geotrack.poi.geo-key:geotrack:poi:geo}")
    private String geoKey;

    public PoiGeoRedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** GEO member 命名，与 {@link com.geotrack.poi.service.PoiService#listNearbyPois} 解析规则一致 */
    public static String poiMember(Long poiId) {
        return "poi:" + poiId;
    }

    /** 对应 Redis {@code GEOADD}：更新或插入 POI 中心坐标 */
    public void savePoiLocation(PoiEntity poi) {
        if (poi == null || poi.getId() == null) {
            return;
        }
        if (poi.getLongitude() == null || poi.getLatitude() == null) {
            log.warn("POI {} 缺少坐标，跳过 GEO 写入", poi.getId());
            return;
        }
        try {
            Point p = new Point(poi.getLongitude(), poi.getLatitude());
            Long added = redisTemplate.opsForGeo().add(geoKey, p, poiMember(poi.getId()));
            log.debug("GEOADD {} member={} lon={} lat={} added={}", geoKey, poiMember(poi.getId()), poi.getLongitude(), poi.getLatitude(), added);
        } catch (Exception e) {
            log.warn("Redis GEOADD 失败 poiId={}: {}", poi.getId(), e.getMessage());
        }
    }

    /** POI 停用或删除同步：对应 {@code ZREM}，从 GEO 集合移除 member */
    public void removePoi(Long poiId) {
        if (poiId == null) {
            return;
        }
        try {
            redisTemplate.opsForZSet().remove(geoKey, poiMember(poiId));
        } catch (Exception e) {
            log.warn("Redis GEO 删除失败 poiId={}: {}", poiId, e.getMessage());
        }
    }

    /**
     * 打卡校验用：用户坐标到 POI 中心的球面距离（米）。
     * 实现要点：临时 member 避免把用户坐标长期写入业务 GEO；{@code finally} 中必须删除以免 ZSET 膨胀。
     */
    public Optional<Double> distanceUserToPoiMeters(double userLng, double userLat, Long poiId) {
        if (poiId == null) {
            return Optional.empty();
        }
        String tempMember = "_u:" + UUID.randomUUID();
        try {
            redisTemplate.opsForGeo().add(geoKey, new Point(userLng, userLat), tempMember);
            Distance dist = redisTemplate.opsForGeo().distance(
                    geoKey, poiMember(poiId), tempMember, RedisGeoCommands.DistanceUnit.METERS);
            if (dist == null) {
                return Optional.empty();
            }
            return Optional.of(dist.getValue());
        } catch (Exception e) {
            log.warn("Redis GEO 距离计算失败 poiId={}: {}", poiId, e.getMessage());
            return Optional.empty();
        } finally {
            try {
                redisTemplate.opsForZSet().remove(geoKey, tempMember);
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    /**
     * 附近 POI：以 (longitude, latitude) 为圆心做 {@code GEORADIUS}（Spring 封装为 {@code radius(circle)}），
     * 仅反映「POI 中心点是否落在圆内」，启用状态需由业务层再查库过滤。
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> searchNearbyCenters(
            double longitude,
            double latitude,
            double radiusMeters,
            int limit
    ) {
        if (radiusMeters <= 0 || limit <= 0) {
            throw new BizException("查询半径与条数必须大于 0");
        }
        int cap = Math.min(limit, 200);
        Circle circle = new Circle(
                new Point(longitude, latitude),
                new Distance(radiusMeters, RedisGeoCommands.DistanceUnit.METERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(cap);
        try {
            return redisTemplate.opsForGeo().radius(geoKey, circle, args);
        } catch (Exception e) {
            log.warn("Redis GEORADIUS 失败: {}", e.getMessage());
            throw new BizException("附近查询暂不可用，请确认 Redis 已启动");
        }
    }

    public String getGeoKey() {
        return geoKey;
    }
}
