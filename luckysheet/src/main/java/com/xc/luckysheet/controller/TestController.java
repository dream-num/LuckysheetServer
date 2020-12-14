package com.xc.luckysheet.controller;

import com.xc.common.api.ResponseVO;
import com.xc.common.config.redis.RedisCacheService;
import com.xc.common.utils.JsonUtil;
import com.xc.luckysheet.postgre.server.PostgresJfGridUpdateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * jar启动
 * java -jar luckysheet.jar
 * 测试类
 * http://localhost:9004/luckysheet/doc.html#/home
 * http://luckysheet.lashuju.com/demo/qkIndex.html
 * http://127.0.0.1:85/luckysheet/demo/
 * http://localhost:9004/luckysheet/test/constant?param=123
 * @author Administrator
 */
@Slf4j
@RestController
@Api(description = "测试接口")
@RequestMapping("test")
public class TestController {

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private PostgresJfGridUpdateService postgresJfGridUpdateService;


    @GetMapping("constant")
    public String getConstant(String param){
        Map<String,String> map=new HashMap<>();
        map.put("threadName",Thread.currentThread().getName());
        map.put("SUCCESS","true");
        map.put("param",param);

        log.info(JsonUtil.toJson(map));
        return JsonUtil.toJson(map);
    }

    @ApiOperation(value = "redis添加",notes = "保存到redis")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "key", value = "键", paramType = "query", required = true, dataType = "String"),
            @ApiImplicitParam(name = "value", value = "值", paramType = "query", required = true, dataType = "String")
    })
    @GetMapping("redis/addCache")
    public ResponseVO addCache(String key, String value){
        redisCacheService.addCache(key,value);
        return ResponseVO.successInstance("ok");
    }
    @ApiOperation(value = "redis查询",notes = "从redis获取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "key", value = "键", paramType = "query", required = true, dataType = "String")
    })
    @GetMapping("redis/getCache")
    public ResponseVO getCache(String key){
        return ResponseVO.successInstance(redisCacheService.getCache(key));
    }

    @ApiOperation(value = "初始化db",notes = "初始化db")
    @GetMapping("dbInit")
    public ResponseVO dbInit(){
        postgresJfGridUpdateService.initTestData();
        return ResponseVO.successInstance("success");
    }
    @ApiOperation(value = "初始化db单个",notes = "初始化db单个")
    @GetMapping("dbInit/one")
    public ResponseVO dbInit(String listId){
        List<String> listName=new ArrayList<String>();
        listName.add(listId);
        postgresJfGridUpdateService.initTestData(listName);
        return ResponseVO.successInstance("success");
    }


}
