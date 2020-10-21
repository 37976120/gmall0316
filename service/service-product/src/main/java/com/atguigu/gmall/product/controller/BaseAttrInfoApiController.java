package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.commom.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class BaseAttrInfoApiController {
    @Autowired
    BaseAttrInfoService baseAttrInfoService;

    @RequestMapping("attrInfoList/{categoryId1}/{categoryId2}/{categoryId3}")
    public Result getAttrInfoList(@PathVariable("categoryId1") String categoryId1,
                                  @PathVariable("categoryId2") String categoryId2,
                                  @PathVariable("categoryId3") String categoryId3
    ) {
        List<BaseAttrInfo> data = baseAttrInfoService.getAttrInfoList(categoryId1, categoryId2, categoryId3);
        return Result.ok(data);
    }

    @RequestMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody() BaseAttrInfo baseAttrInfo) {
        baseAttrInfoService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    @RequestMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable("attrId") String attrId) {
        List<BaseAttrValue> data = baseAttrInfoService.getAttrValueList(attrId);
        return Result.ok(data);
    }
}
