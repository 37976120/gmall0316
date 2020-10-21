package com.atguigu.gmall.all.controller;

import ch.qos.logback.core.status.StatusUtil;
import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.list.client.ListFeign;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class ListController {
    @Autowired
    ListFeign listFeign;

    @RequestMapping({"list.html", "search.html"})
    public String list(SearchParam searchParam, Model model) {
        Result<Map> data = listFeign.list(searchParam);
        model.addAllAttributes(data.getData());
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("urlParam", getUrlParam(searchParam));
        model.addAttribute("orderMap", getOrderMap(searchParam));
        model.addAttribute("propsParamList", getPropsParamList(searchParam));
        String trademark = searchParam.getTrademark();
        if (null != trademark) {
            String[] split = trademark.split(":");
            model.addAttribute("trademarkParam", split[1]);
        }
        return "list/index.html";
    }

    private ArrayList<SearchAttr> getPropsParamList(SearchParam searchParam) {
        String[] props = searchParam.getProps();
        ArrayList<SearchAttr> propsParamList = new ArrayList<>();
        if (null != props && props.length > 0) {
            for (String prop : props) {
                SearchAttr searchAttr = new SearchAttr();
                String[] split = prop.split(":");
                searchAttr.setAttrId(Long.parseLong(split[0]));
                searchAttr.setAttrValue(split[1]);
                searchAttr.setAttrName(split[2]);
                propsParamList.add(searchAttr);
            }
        }
        return propsParamList;
    }

    private Object getOrderMap(SearchParam searchParam) {
        HashMap<String, String> orderMap = new HashMap<>();
        orderMap.put("type", "1");
        orderMap.put("sort", "desc");
        String order = searchParam.getOrder();
        if (StringUtils.isNoneBlank(order)) {
            String[] split = order.split(":");
            orderMap.put("type", split[0]);
            orderMap.put("sort", split[1]);
        }
        return orderMap;
    }

    private String getUrlParam(SearchParam searchParam) {
        Long category3Id = searchParam.getCategory3Id();
        String keyword = searchParam.getKeyword();
        String[] props = searchParam.getProps();
        String trademark = searchParam.getTrademark();
        String order = searchParam.getOrder();
        StringBuffer stringBuffer = new StringBuffer("list.html?");
        if (null != category3Id && category3Id > 0) {
            stringBuffer.append("category3Id=").append(category3Id);
        }
        if (null != keyword) {
            stringBuffer.append("keyword=").append(keyword);
        }
        if (null != props && props.length > 0) {
            for (String prop : props) {
                stringBuffer.append("&props=").append(prop);
            }
        }
        if (null != trademark) {
            stringBuffer.append("&trademark=").append(trademark);
        }
//        if (order != null) {
//            stringBuffer.append("&order=").append(order);
//        }

        return stringBuffer.toString();
    }
}
