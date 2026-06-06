package com.geotrack.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geotrack.common.exception.BizException;
import com.geotrack.mall.dto.MallGoodsDto;
import com.geotrack.mall.entity.GoodsEntity;
import com.geotrack.mall.mapper.GoodsMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallCatalogService {

    private final GoodsMapper goodsMapper;

    public MallCatalogService(GoodsMapper goodsMapper) {
        this.goodsMapper = goodsMapper;
    }

    public List<MallGoodsDto> listOnShelf() {
        List<GoodsEntity> list = goodsMapper.selectList(new LambdaQueryWrapper<GoodsEntity>()
                .eq(GoodsEntity::getStatus, 1)
                .orderByDesc(GoodsEntity::getId));
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public MallGoodsDto getDetail(Long id) {
        GoodsEntity g = goodsMapper.selectById(id);
        if (g == null || g.getStatus() == null || g.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }
        return toDto(g);
    }

    private MallGoodsDto toDto(GoodsEntity g) {
        boolean seckill = g.getIsSeckill() != null && g.getIsSeckill() != 0;
        return new MallGoodsDto(
                g.getId(),
                g.getName(),
                g.getPointsPrice(),
                g.getStock(),
                seckill,
                g.getBeginTime(),
                g.getEndTime()
        );
    }
}
