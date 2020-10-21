package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {
    List<Map<String, Object>> getSkuValueIdsMap(Long spuId);
}
