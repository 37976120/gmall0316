package com.atguigu.gmall.item.service;

import com.atguigu.gmall.commom.result.Result;

import java.util.HashMap;
import java.util.Map;

public interface ItemService {

    Map<String, Object> getItem(String skuid);
}
