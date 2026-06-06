package com.geotrack.poi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geotrack.poi.entity.CheckInRecordEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

public interface CheckInRecordMapper extends BaseMapper<CheckInRecordEntity> {

    @Select("SELECT COUNT(DISTINCT poi_id) FROM gt_checkin_record WHERE user_id = #{userId} AND result_code = 'SUCCESS'")
    Long countDistinctPoiByUser(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT checkin_day FROM gt_checkin_record
            WHERE user_id = #{userId} AND result_code = 'SUCCESS'
              AND checkin_day >= #{start} AND checkin_day <= #{end}
            ORDER BY checkin_day
            """)
    List<LocalDate> listDistinctCheckinDaysInRange(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Select("SELECT COUNT(1) FROM gt_checkin_record WHERE user_id = #{userId} AND result_code = 'SUCCESS'")
    Long countSuccessByUser(@Param("userId") Long userId);
}
