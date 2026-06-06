package com.geotrack.poi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geotrack.common.exception.BizException;
import com.geotrack.poi.dto.CheckInRequest;
import com.geotrack.poi.dto.PoiCreateRequest;
import com.geotrack.poi.entity.CheckInRecordEntity;
import com.geotrack.poi.entity.PoiEntity;
import com.geotrack.poi.mapper.CheckInRecordMapper;
import com.geotrack.poi.mapper.PoiMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.geotrack.poi.dto.CheckInMySummaryDto;
import com.geotrack.poi.dto.CheckInRecentItemDto;
import com.geotrack.poi.dto.NearbyPoiDto;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * POI 与打卡核心业务：含 Redis GEO 附近查询/打卡距离（优先）及数据库行锁防超卖式重复（当日唯一约束在表 uk）。
 */
@Service
public class PoiService {

    private final PoiMapper poiMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final GeoDistanceService geoDistanceService;
    private final PoiGeoRedisService poiGeoRedisService;
    private final CheckInEventPublisher checkInEventPublisher;

    public PoiService(
            PoiMapper poiMapper,
            CheckInRecordMapper checkInRecordMapper,
            GeoDistanceService geoDistanceService,
            PoiGeoRedisService poiGeoRedisService,
            CheckInEventPublisher checkInEventPublisher
    ) {
        this.poiMapper = poiMapper;
        this.checkInRecordMapper = checkInRecordMapper;
        this.geoDistanceService = geoDistanceService;
        this.poiGeoRedisService = poiGeoRedisService;
        this.checkInEventPublisher = checkInEventPublisher;
    }

    @Transactional
    public Long createPoi(PoiCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.name())) {
            throw new BizException("POI 名称不能为空");
        }
        if (request.longitude() == null || request.latitude() == null) {
            throw new BizException("经纬度不能为空");
        }
        if (request.longitude() < -180 || request.longitude() > 180
                || request.latitude() < -90 || request.latitude() > 90) {
            throw new BizException("经纬度超出合法范围");
        }
        if (request.radiusMeters() != null && request.radiusMeters() <= 0) {
            throw new BizException("打卡半径必须大于 0");
        }
        if (request.rewardPoints() != null && request.rewardPoints() < 0) {
            throw new BizException("奖励积分不能为负数");
        }
        if (request.status() != null && request.status() != 0 && request.status() != 1) {
            throw new BizException("状态仅支持 0(停用) 或 1(启用)");
        }
        PoiEntity poi = new PoiEntity();
        poi.setName(request.name());
        poi.setLongitude(request.longitude());
        poi.setLatitude(request.latitude());
        poi.setRadiusMeters(request.radiusMeters() == null ? 500 : request.radiusMeters());
        poi.setRewardPoints(request.rewardPoints() == null ? 10 : request.rewardPoints());
        poi.setDescription(request.description());
        poi.setStatus(request.status() == null ? 1 : request.status());
        poiMapper.insert(poi);
        // 仅启用 POI 进入 GEO，保证 /nearby 与 GEODIST 能找到 member
        if (poi.getStatus() != null && poi.getStatus() == 1) {
            poiGeoRedisService.savePoiLocation(poi);
        }
        return poi.getId();
    }

    public List<PoiEntity> list() {
        return poiMapper.selectList(new LambdaQueryWrapper<PoiEntity>().orderByDesc(PoiEntity::getId));
    }

    public CheckInMySummaryDto getMyCheckInSummary(Long userId, int year, int month) {
        if (userId == null) {
            throw new BizException("用户未登录");
        }
        if (month < 1 || month > 12 || year < 2000 || year > 2100) {
            throw new BizException("年月参数不合法");
        }
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<LocalDate> days = checkInRecordMapper.listDistinctCheckinDaysInRange(userId, start, end);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        List<String> checkedDates = days.stream().map(d -> d.format(fmt)).collect(Collectors.toList());
        Long distinct = checkInRecordMapper.countDistinctPoiByUser(userId);
        Long total = checkInRecordMapper.countSuccessByUser(userId);
        return new CheckInMySummaryDto(
                checkedDates,
                distinct == null ? 0L : distinct,
                total == null ? 0L : total
        );
    }

    public List<CheckInRecentItemDto> listMyRecentCheckIns(Long userId, int limit) {
        if (userId == null) {
            throw new BizException("用户未登录");
        }
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<CheckInRecordEntity> rows = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecordEntity>()
                        .eq(CheckInRecordEntity::getUserId, userId)
                        .orderByDesc(CheckInRecordEntity::getCheckedAt)
                        .last("limit " + safeLimit));
        return rows.stream().map(r -> new CheckInRecentItemDto(
                r.getId(),
                r.getUserId(),
                r.getPoiId(),
                "SUCCESS".equalsIgnoreCase(r.getResultCode()) ? "success" : "failed",
                r.getCheckedAt() == null ? "" : r.getCheckedAt().toString()
        )).collect(Collectors.toList());
    }

    @Transactional
    public void updatePoiStatus(Long id, Integer status) {
        if (id == null) {
            throw new BizException("POI id 不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("状态仅支持 0(停用) 或 1(启用)");
        }
        PoiEntity existing = poiMapper.selectById(id);
        if (existing == null) {
            throw new BizException("POI 不存在");
        }
        existing.setStatus(status);
        poiMapper.updateById(existing);
        // GEO 与启用状态一致：启用则覆盖坐标，停用则移除避免脏读
        if (status == 1) {
            poiGeoRedisService.savePoiLocation(poiMapper.selectById(id));
        } else {
            poiGeoRedisService.removePoi(id);
        }
    }

    /**
     * 以用户坐标为圆心，查询半径内 POI 中心点（Redis GEO），再与数据库对齐仅返回启用中的 POI。
     */
    public List<NearbyPoiDto> listNearbyPois(double longitude, double latitude, double radiusMeters, int limit) {
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            throw new BizException("经纬度超出合法范围");
        }
        GeoResults<RedisGeoCommands.GeoLocation<String>> raw =
                poiGeoRedisService.searchNearbyCenters(longitude, latitude, radiusMeters, limit);
        if (raw == null) {
            return List.of();
        }
        // 解析 GEORADIUS 结果：distance 为用户坐标到各 POI 中心的球面距离（米）
        Map<Long, Double> idToDistMeters = new LinkedHashMap<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> gr : raw) {
            RedisGeoCommands.GeoLocation<String> loc = gr.getContent();
            if (loc == null || loc.getName() == null || gr.getDistance() == null) {
                continue;
            }
            String name = loc.getName();
            if (!name.startsWith("poi:")) {
                continue;
            }
            try {
                Long pid = Long.parseLong(name.substring(4));
                idToDistMeters.putIfAbsent(pid, gr.getDistance().getValue());
            } catch (NumberFormatException ignored) {
                // 非法 member 名跳过
            }
        }
        if (idToDistMeters.isEmpty()) {
            return List.of();
        }
        List<PoiEntity> rows = poiMapper.selectList(new LambdaQueryWrapper<PoiEntity>()
                .in(PoiEntity::getId, idToDistMeters.keySet())
                .eq(PoiEntity::getStatus, 1));
        List<NearbyPoiDto> out = new ArrayList<>();
        for (PoiEntity p : rows) {
            Double d = idToDistMeters.get(p.getId());
            if (d == null || p.getLongitude() == null || p.getLatitude() == null) {
                continue;
            }
            int rm = p.getRadiusMeters() == null ? 500 : p.getRadiusMeters();
            int rp = p.getRewardPoints() == null ? 0 : p.getRewardPoints();
            int st = p.getStatus() == null ? 0 : p.getStatus();
            out.add(new NearbyPoiDto(
                    p.getId(),
                    p.getName(),
                    d,
                    p.getLongitude(),
                    p.getLatitude(),
                    rm,
                    rp,
                    st,
                    p.getDescription() == null ? "" : p.getDescription()
            ));
        }
        out.sort((a, b) -> Double.compare(a.distanceMeters(), b.distanceMeters()));
        return out;
    }

    @Transactional
    public String checkIn(Long userId, CheckInRequest request) {
        validateRequest(userId, request);
        PoiEntity poi = poiMapper.selectById(request.poiId());
        if (poi == null || poi.getStatus() == null || poi.getStatus() != 1) {
            throw new BizException("POI 不存在或不可用");
        }

        LocalDate today = LocalDate.now();
        Long existed = checkInRecordMapper.selectCount(new LambdaQueryWrapper<CheckInRecordEntity>()
                .eq(CheckInRecordEntity::getUserId, userId)
                .eq(CheckInRecordEntity::getPoiId, request.poiId())
                .eq(CheckInRecordEntity::getCheckinDay, today));
        if (existed != null && existed > 0) {
            throw new BizException("今日已完成打卡");
        }

        // 打卡范围：优先 Redis GEO（GEODIST）；Redis 无结果或异常时回退 Haversine，避免单点故障挡业务
        Optional<Double> geoDist = poiGeoRedisService.distanceUserToPoiMeters(
                request.longitude(), request.latitude(), request.poiId());
        double distance = geoDist.orElseGet(() -> geoDistanceService.distanceMeters(
                request.longitude(), request.latitude(),
                poi.getLongitude(), poi.getLatitude()));
        int radiusM = poi.getRadiusMeters() == null ? 500 : poi.getRadiusMeters();
        // 少量容差：减轻浮点误差与消费级 GPS 短时漂移导致的边界误杀
        double allowedMeters = radiusM + 5.0;
        if (distance > allowedMeters) {
            throw new BizException(String.format(
                    "未在打卡范围内（直线距离约 %.0f 米，允许半径 %d 米；请确认经纬度未颠倒，并尽量靠近打卡点中心）",
                    distance, radiusM));
        }

        CheckInRecordEntity record = new CheckInRecordEntity();
        record.setUserId(userId);
        record.setPoiId(request.poiId());
        record.setCheckinDay(today);
        record.setLongitude(request.longitude());
        record.setLatitude(request.latitude());
        record.setImageUrl(request.imageUrl());
        record.setResultCode("SUCCESS");
        record.setCheckedAt(LocalDateTime.now());
        checkInRecordMapper.insert(record);
        checkInEventPublisher.publishPointsGrantEvent(record, poi);
        checkInEventPublisher.publishFeedCreateEvent(record, request.content());

        int reward = poi.getRewardPoints() == null ? 0 : poi.getRewardPoints();
        if (reward > 0) {
            return "打卡成功，获得 " + reward + " 积分";
        }
        return "打卡成功";
    }

    private void validateRequest(Long userId, CheckInRequest request) {
        if (userId == null || request == null || request.poiId() == null) {
            throw new BizException("用户和 POI 参数不能为空");
        }
        if (request.longitude() == null || request.latitude() == null) {
            throw new BizException("坐标参数不能为空");
        }
        if (!StringUtils.hasText(request.content())) {
            throw new BizException("打卡文案不能为空");
        }
        if (request.content().length() > 500) {
            throw new BizException("打卡文案长度不能超过 500 字");
        }
        if (!StringUtils.hasText(request.imageUrl())) {
            throw new BizException("打卡图片不能为空");
        }
        if (request.imageUrl().length() > 255) {
            throw new BizException("图片地址长度不能超过 255 字符");
        }
    }
}
