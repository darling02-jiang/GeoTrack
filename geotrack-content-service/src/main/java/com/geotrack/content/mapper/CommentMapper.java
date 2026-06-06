package com.geotrack.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geotrack.content.entity.CommentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {
}
