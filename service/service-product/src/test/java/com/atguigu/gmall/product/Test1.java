package com.atguigu.gmall.product;


import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.transform.Source;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Test1 {

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Test
    public void test1() {
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(1l, 1l);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrListCheckBySku) {
            System.out.println("spuSaleAttr = " + spuSaleAttr);
        }

        List<Map<String, Object>> skuValueIdsMap = skuAttrValueMapper.getSkuValueIdsMap(1L);

        Map<String, String> valuleIdsMap = new HashMap<>();
        for (Map<String, Object> map : skuValueIdsMap) {
            String skuId = map.get("sku_id").toString();
            String valueId = map.get("value_ids").toString();
            valuleIdsMap.put(valueId, skuId);
        }
        System.out.println(valuleIdsMap);
    }


}
