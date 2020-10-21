package com.atguigu.gmall.list.service.Impl;

import com.alibaba.fastjson.JSON;

import com.atguigu.gmall.list.repository.GoodsElasticsearchRepository;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    GoodsElasticsearchRepository goodsElasticsearchRepository;
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public void onSale(Long skuId) {
        //上架es
        Goods goods = new Goods();
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId.toString());
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id().toString());
        BaseTrademark baseTrademark = productFeignClient.getTrademark(skuInfo.getTmId());
        List<SearchAttr> list = productFeignClient.getSearchAttrList(skuId.toString());
        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        //TODO 查数据库
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setCreateTime(new Date());
        goods.setTmId(baseTrademark.getId());
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        goods.setAttrs(list);
        BeanCopier beanCopier = BeanCopier.create(BaseCategoryView.class, Goods.class, false);
        beanCopier.copy(categoryView, goods, null);
        //在拷贝属性后重置id
        goods.setId(skuInfo.getId());
        goodsElasticsearchRepository.save(goods);
    }

    @Override
    public void cancelSale(Long skuId) {
        //下架es
        goodsElasticsearchRepository.deleteById(skuId);
    }

    @Override
    public void incHot(String skuid) {
        long hotScore = redisTemplate.opsForZSet().incrementScore("hotScore", "sku" + skuid, 1).longValue();
        if (hotScore % 10 == 0) {
            Optional<Goods> byId = goodsElasticsearchRepository.findById(Long.parseLong(skuid));
            Goods goods = byId.get();
            goods.setHotScore(hotScore);
            goodsElasticsearchRepository.save(goods);
        }
    }

    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        //构建dsl语句
        SearchRequest searchRequest = getSearchRequest(searchParam);
        //执行查询
        SearchResponse rs = null;
        try {
            rs = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //返回结果
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = rs.getHits();
        ArrayList<Goods> goods = new ArrayList<>();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            Goods good = JSON.parseObject(sourceAsString, Goods.class);
            goods.add(good);
            //解析高亮内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            if (title != null) {
                String highlightTitleString = title.getFragments()[0].string();
                good.setTitle(highlightTitleString);
            }
        }
        searchResponseVo.setGoodsList(goods);
        //**商标聚合解析**
        ParsedLongTerms tmIdAggr = (ParsedLongTerms) rs.getAggregations().asMap().get("tmIdAggr");
        List<SearchResponseTmVo> tmIdVos = tmIdAggr.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //商标ID
            long tmid = bucket.getKeyAsNumber().longValue();
            searchResponseTmVo.setTmId(tmid);
            //商标名字(在桶聚合中,一个商标ID没有其它名字了,可直接get(0))
            ParsedStringTerms tmNameAggr = (ParsedStringTerms) bucket.getAggregations().asMap().get("tmNameAggr");
            String nameString = tmNameAggr.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(nameString);
            //商标URL
            ParsedStringTerms tmLogoUrlAggr = (ParsedStringTerms) bucket.getAggregations().asMap().get("tmLogoUrlAggr");
            String tmLogString = tmLogoUrlAggr.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogString);

            return searchResponseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(tmIdVos);
        //**属性聚合解析**

        ParsedNested attrsAggr = (ParsedNested) rs.getAggregations().asMap().get("attrsAggr");
        ParsedLongTerms attrIdAggr = (ParsedLongTerms) attrsAggr.getAggregations().asMap().get("attrIdAggr");
        List<SearchResponseAttrVo> searchResponseAttrVoList = attrIdAggr.getBuckets().stream().map(bucket -> {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //当前桶中的attrid
            long attrId = bucket.getKeyAsNumber().longValue();
            searchResponseAttrVo.setAttrId(attrId);

            //它桶中的attrName,只有一个
            Map<String, Aggregation> stringAggregationMap = bucket.getAggregations().asMap();
            ParsedStringTerms attrNameAggr = (ParsedStringTerms) stringAggregationMap.get("attrNameAggr");
            List<String> attrNameList = attrNameAggr.getBuckets().stream().map(bucket1 -> {
                String attrNameString = bucket1.getKeyAsString();
                return attrNameString;
            }).collect(Collectors.toList());
            searchResponseAttrVo.setAttrName(attrNameList.get(0));

            //attrValue
            ParsedStringTerms attrValueAggr = (ParsedStringTerms) stringAggregationMap.get("attrValueAggr");
            List<String> attrValueList = attrValueAggr.getBuckets().stream().map(bucket1 -> {

                String keyAsString = bucket1.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(attrValueList);

            return searchResponseAttrVo;
        }).collect(Collectors.toList());

        searchResponseVo.setAttrsList(searchResponseAttrVoList);
        return searchResponseVo;
        //todo
    }

    private SearchRequest getSearchRequest(SearchParam searchParam) {
        Long category3Id = searchParam.getCategory3Id();
        //与keywords互斥 ==>
        String keyword = searchParam.getKeyword();
        //与属性互斥  ==>title
        String[] props = searchParam.getProps();
        String trademark = searchParam.getTrademark();
        String order = searchParam.getOrder();

        //构造DSL语句 query-->bool/filter
        //                              -->must-->match
        //                              -->filter-->term
        //queryBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //    bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (StringUtils.isNoneBlank(keyword)) {//有关键字时
            boolQueryBuilder.must(new MatchQueryBuilder("title", keyword));
            //----------高亮显示-----------------
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red;font-weight:bolder;'>");
            highlightBuilder.field("title");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
            //开启高亮搜索
            //----------高亮显示-----------------
        }
        if (null != category3Id && category3Id > 0) {//有分类
            boolQueryBuilder.filter(new TermQueryBuilder("category3Id", category3Id));
        }
        //----------------属性的nested搜索【beg】-----------------
        if (null != props && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                String attrId = split[0];
                String attrValue = split[1];
                String attrName = split[2];

                BoolQueryBuilder boolQueryBuilderAttrs = new BoolQueryBuilder();
                boolQueryBuilderAttrs.filter(new TermQueryBuilder("attrs.attrId", attrId));
                boolQueryBuilderAttrs.must(new TermQueryBuilder("attrs.attrValue", attrValue));
                boolQueryBuilderAttrs.must(new TermQueryBuilder("attrs.attrName", attrName));
                // 第二层query，nested的query
                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs", boolQueryBuilderAttrs, ScoreMode.None);
                boolQueryBuilder.must(nestedQueryBuilder);
            }
        }
        //----------------属性的nested搜索【end】-----------------
        //-----------------品牌搜索--------------------
        if (StringUtils.isNoneBlank(trademark)) {
            String[] split = trademark.split(":");//tmId在es中类型为long
            boolQueryBuilder.filter(new TermQueryBuilder("tmId", split[0]));
        }
        //-----------------排序beg--------------------
        String defOrder = "hotScore";
        SortOrder defSort = SortOrder.DESC;
        if (StringUtils.isNoneBlank(order)) {
            String[] split = order.split(":");
            if (split[0].equals("2")) {
                defOrder = "price";
            }
            if (split[1].equals("asc")) {
                defSort = SortOrder.ASC;
            }
        }
        searchSourceBuilder.sort(defOrder, defSort);
        //-----------------排序end--------------------

        //query
        SearchSourceBuilder query = searchSourceBuilder.query(boolQueryBuilder);
        //----------聚合搜索-----------------
        // 商标聚合函数aggregation
        AggregationBuilder termsIdAggregationBuilder = AggregationBuilders.terms("tmIdAggr").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAggr").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAggr").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(termsIdAggregationBuilder);
        //平台属性及值聚合函数
        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("attrsAggr", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAggr").field("attrs.attrId")//【注意：nested与属性点】
                        .subAggregation(AggregationBuilders.terms("attrValueAggr").field("attrs.attrValue"))
                        .subAggregation(AggregationBuilders.terms("attrNameAggr").field("attrs.attrName")));
        searchSourceBuilder.aggregation(nestedAggregationBuilder);
        //----------聚合搜索-----------------
        System.out.println(query.toString());

        //封装searchRequest查询请求
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);

        return searchRequest;
    }
}
