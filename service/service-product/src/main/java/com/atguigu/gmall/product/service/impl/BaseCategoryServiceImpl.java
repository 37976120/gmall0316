package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.model.product.BaseCategory1;

import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.service.BaseCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BaseCategoryServiceImpl implements BaseCategoryService {

    @Autowired
    BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;

    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(String Category1Id) {
        QueryWrapper<BaseCategory2> wrapper = new QueryWrapper<>();
        wrapper.eq("category1_id", Category1Id);
        return baseCategory2Mapper.selectList(wrapper);
    }

    @Override
    public List<BaseCategory3> getCategory3(String category2Id) {
        QueryWrapper<BaseCategory3> wrapper = new QueryWrapper<>();
        wrapper.eq("category2_id", category2Id);
        return baseCategory3Mapper.selectList(wrapper);
    }

    @Override
    public BaseCategoryView getCategoryView(String category3Id) {

        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    public List<JSONObject> getBaseCatogory() {
        //首页JSON树的制做
        List<BaseCategoryView> all = baseCategoryViewMapper.selectList(null);
        List<JSONObject> allCategory1Item = new ArrayList<>();
        Map<Long, List<BaseCategoryView>> category1IdMap = all.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        for (Map.Entry<Long, List<BaseCategoryView>> category1Entry : category1IdMap.entrySet()) {
            Long categoryId = category1Entry.getKey();
            String categoryName = category1Entry.getValue().get(0).getCategory1Name();
            JSONObject jsonObjectCategory1Id = new JSONObject();
            jsonObjectCategory1Id.put("categoryId", categoryId);
            jsonObjectCategory1Id.put("categoryName", categoryName);
            List<BaseCategoryView> category1List = category1Entry.getValue();
            List<JSONObject> allCategory2Item = new ArrayList<>();
            Map<Long, List<BaseCategoryView>> category2Map = category1List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> category2Entry : category2Map.entrySet()) {
                JSONObject jsonObject2 = new JSONObject();
                Long category2Id = category2Entry.getKey();
                String category2Name = category2Entry.getValue().get(0).getCategory2Name();
                jsonObject2.put("categoryId", category2Id);
                jsonObject2.put("categoryName", category2Name);
                List<BaseCategoryView> category3List = category2Entry.getValue();
                List<JSONObject> allCategory3Item = new ArrayList<>();
                for (BaseCategoryView category3 : category3List) {
                    JSONObject jsonObject3 = new JSONObject();
                    Long category3Id = category3.getCategory3Id();
                    String category3Name = category3.getCategory3Name();
                    jsonObject3.put("categoryId", category3Id);
                    jsonObject3.put("categoryName", category3Name);
                    allCategory3Item.add(jsonObject3);
                }
                jsonObject2.put("categoryChild", allCategory3Item);
                allCategory2Item.add(jsonObject2);
            }
            jsonObjectCategory1Id.put("categoryChild", allCategory2Item);
            allCategory1Item.add(jsonObjectCategory1Id);
        }
        return allCategory1Item;
    }

    public List<JSONObject> recursive(List<BaseCategoryView> allItem, int i) {
        i++;
        ArrayList<JSONObject> objects = new ArrayList<>();
        Map<Long, List<BaseCategoryView>> collect = allItem.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        for (Map.Entry<Long, List<BaseCategoryView>> entry : collect.entrySet()) {
            Long categoryId = entry.getKey();
            //getName
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("categoryId", categoryId);
            List<BaseCategoryView> nextList = entry.getValue();
            List<JSONObject> rs = new ArrayList<>();
            if (i < 3) {
                rs = recursive(nextList, i);
                jsonObject.put("categoryChild", rs);
            }

            objects.add(jsonObject);
        }
        return objects;
    }

}
