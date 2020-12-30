package com.xc.luckysheet.controller;

import com.mongodb.DBObject;
import com.xc.common.api.ResponseVO;
import com.xc.common.config.redis.RedisCacheService;
import com.xc.common.utils.JsonUtil;
import com.xc.luckysheet.postgre.server.PostgresGridFileGetService;
import com.xc.luckysheet.postgre.server.PostgresJfGridUpdateService;
import com.xc.luckysheet.xlsutils.poiutil.XlsUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

/**
 * jar启动
 * java -jar luckysheet.jar
 * 测试类
 * http://localhost:9004/luckysheet/doc.html#/home
 * http://luckysheet.lashuju.com/demo/qkIndex.html
 * http://127.0.0.1:85/luckysheet/demo/
 * http://localhost:9004/luckysheet/test/constant?param=123
 * http://localhost:9004/luckysheet/test/down?listId=xc79500%23-8803%237c45f52b7d01486d88bc53cb17dcd2c3&fileName=list.xls
 * http://localhost:9004/luckysheet/test/down?listId=1079500%23-8803%237c45f52b7d01486d88bc53cb17dcd2c3&fileName=list.xls
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

    @Autowired
    private PostgresGridFileGetService postgresGridFileGetService;

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

    @ApiOperation(value = "获取整个xls结构",notes = "初始化db单个")
    @GetMapping("get/LuckySheetJson")
    public ResponseVO getLuckySheetJson(String listId){
        List<DBObject> list=postgresGridFileGetService.getAllSheetByGridKey(listId);
        return ResponseVO.successInstance(list);
    }

    @ApiOperation(value = "下载xls",notes = "下载xls")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileName", value = "文档名称", paramType = "query", required = true, dataType = "String"),
            @ApiImplicitParam(name = "listId", value = "文档id", paramType = "query", required = true, dataType = "String")
    })
    @GetMapping("down")
    public ResponseVO down(HttpServletResponse response,String fileName,String listId){
        if(fileName==null||listId==null){
            return ResponseVO.errorInstance("参数错误");
        }
        if(!fileName.endsWith(".xls")&&!fileName.endsWith(".xlsx")){
            return ResponseVO.errorInstance("文件扩展名错误");
        }
        boolean isXlsx=false;
        String zipFileName="";
        if(fileName.endsWith(".xlsx")){
            isXlsx=true;
            zipFileName=fileName.substring(0,fileName.length()-5);
        }else{
            zipFileName=fileName.substring(0,fileName.length()-4);
        }
        List<DBObject> lists=postgresGridFileGetService.getAllSheetByGridKey(listId);
        //输出的文件名
        String _fileName= null;
        try {
            _fileName = new String(zipFileName.getBytes("gb2312"), "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            return ResponseVO.errorInstance(e.getMessage());
        }
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;fileName=" +_fileName+".zip" );


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            XlsUtil.exportXlsFile(baos,isXlsx,lists);
        } catch (FileNotFoundException e) {
            return ResponseVO.errorInstance(e.getMessage());
        } catch (IOException e) {
            return ResponseVO.errorInstance(e.getMessage());
        }
        byte [] data = baos.toByteArray();
        try{
            ZipOutputStream out=new ZipOutputStream(response.getOutputStream());
            ZipEntry zipEntry=new ZipEntry(fileName);
            zipEntry.setSize(data.length);
            zipEntry.setTime(System.currentTimeMillis());
            out.putNextEntry(zipEntry);
            out.setEncoding("GBK");
            out.write(data);
            out.closeEntry();
            out.finish();
            System.out.println("down ok");
            return null;
        }catch (Exception ex){
            return ResponseVO.errorInstance(ex.getMessage());
        }


    }






}
