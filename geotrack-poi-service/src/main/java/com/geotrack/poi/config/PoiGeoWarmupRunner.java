package com.geotrack.poi.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geotrack.poi.entity.PoiEntity;
import com.geotrack.poi.mapper.PoiMapper;
import com.geotrack.poi.service.PoiGeoRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动预热：把 DB 中 {@code status=1} 的 POI 写入 Redis GEO，避免依赖「首次访问再写入」导致附近查询/打卡 GEO 缺数据。
 * 与运行时 {@link com.geotrack.poi.service.PoiService#createPoi}、{@code updatePoiStatus} 的增量更新配合使用。
 */
@Component
public class PoiGeoWarmupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PoiGeoWarmupRunner.class);

    private final PoiMapper poiMapper;
    private final PoiGeoRedisService poiGeoRedisService;

    public PoiGeoWarmupRunner(PoiMapper poiMapper, PoiGeoRedisService poiGeoRedisService) {
        this.poiMapper = poiMapper;
        this.poiGeoRedisService = poiGeoRedisService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 全量对齐仅针对启用 POI；停用项不应出现在 GEO 中（与 updatePoiStatus 删除逻辑一致）
        List<PoiEntity> enabled = poiMapper.selectList(new LambdaQueryWrapper<PoiEntity>()
                .eq(PoiEntity::getStatus, 1));
        int ok = 0;
        for (PoiEntity p : enabled) {
            try {
                poiGeoRedisService.savePoiLocation(p);
                ok++;
            } catch (Exception e) {
                log.warn("Redis GEO 预热跳过单条 poiId={}: {}", p.getId(), e.getMessage());
            }
        }
        log.info("Redis GEO 预热：成功写入 {}/{} 个启用 POI 至 {}", ok, enabled.size(), poiGeoRedisService.getGeoKey());
    }
}
