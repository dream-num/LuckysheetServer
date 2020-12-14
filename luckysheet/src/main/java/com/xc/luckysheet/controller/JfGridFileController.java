package com.xc.luckysheet.controller;


import com.mongodb.DBObject;
import com.xc.common.utils.JsonUtil;
import com.xc.luckysheet.entity.LuckySheetGridModel;
import com.xc.luckysheet.entity.enummodel.OperationTypeEnum;
import com.xc.luckysheet.postgre.server.PostgresGridFileGetService;
import com.xc.luckysheet.utils.Pako_GzipUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *  图表格页面调用
 * @author Administrator
 */
@Slf4j
@RestController
@Api(description = "luckysheet测试接口")
@RequestMapping(value = {"/api"})
public class JfGridFileController {
    /*
        /load       加载默认表格   （）
        /loadsheet  加载指定表格   （）

        打开表格
        /tu/api?id=listid
    */

    /**
     * 是否启用pgsql的开关
     */
    public static String pgSetUp="";

    @Autowired
    private PostgresGridFileGetService pgGridFileGetService;

    /**
     * 默认加载表格 分块
     * @param request
     * @param response
     * @param gridKey
     * @return
     */
    @ApiOperation(value = "默认加载表格",notes = "默认加载表格")
    @PostMapping("/load")
    public String load(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "") String gridKey) {
        //告诉浏览器，当前发送的是gzip格式的内容
        response.setHeader("Content-Encoding", "gzip");
        response.setContentType("text/html");
        String resultStr="";
        if(gridKey.trim().length()!=0){
            try {
                String _checkStr=check(request,gridKey.toString(),null,OperationTypeEnum.Read);
                if(_checkStr.length()>0){
                    return null;
                }
                List<DBObject> dbObject=null;
                if("0".equals(pgSetUp)){
                    //postgre
                    dbObject=pgGridFileGetService.getDefaultByGridKey(gridKey);
                }else{
                    //其它数据库
                }
                if(dbObject!=null){
                    resultStr=JsonUtil.toJson(dbObject);
                }
            } catch (Exception e) {
                log.error(gridKey+e.getMessage());
            }


        }
        log.info("load");
        try {
            byte dest[]= Pako_GzipUtils.compress2(resultStr);
            OutputStream out=response.getOutputStream();
            out.write(dest);
            out.close();
            out.flush();
        } catch (Exception e) {
            log.error("load---ioerror:"+e);
        }
        return null;
    }

    /**
     * 加载指定表格
     * @param map
     * @param request
     * @param response
     * @param gridKey
     * @param index
     * @return
     */
    @ApiOperation(value = "加载指定表格",notes = "加载指定表格")
    @PostMapping("/loadsheet")
    public byte[] loadsheet(Map map, HttpServletRequest request, HttpServletResponse response,
                            @RequestParam(defaultValue = "") String gridKey,
                            @RequestParam(defaultValue = "") String[] index) {
        log.info("loadsheet--gridKey:"+gridKey+" index:"+ Arrays.toString(index));
        ////告诉浏览器，当前发送的是gzip格式的内容
        response.setHeader("Content-Encoding", "gzip");
        response.setContentType("text/html");
        String resultStr="";
        if(gridKey.trim().length()!=0){
            try {
                String _id=gridKey;
                String _checkStr=check(request,_id,null,OperationTypeEnum.Read);
                log.info(_checkStr);
                if(_checkStr.length()>0){
                    return null;
                }
                LinkedHashMap dbObject=null;
                if("0".equals(pgSetUp)){
                    //postgre
                    dbObject=pgGridFileGetService.getByGridKeys(_id, Arrays.asList(index));
                }else{
                    //mongo
                }
                log.info("loadsheet--dbObject--");
                if(dbObject!=null){
                    resultStr=JsonUtil.toJson(dbObject);
                }
            } catch (Exception e) {
                log.info(gridKey+e.getMessage());
            }
        }

        byte dest[]= Pako_GzipUtils.compress2(resultStr);
        log.info("loadsheet");
        OutputStream out=null;
        try {
            out = response.getOutputStream();
            out.write(dest);
            out.close();
            out.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error("loadsheet---ioerror:"+e);
        }catch (Exception ex){
            log.error("loadsheet---error:"+ex);
        }

        return null;
    }

    /**
     * 文档权限的检查
     * @param request
     * @param _id
     * @param curmodel
     * @param operationTypeEnum
     * @return
     */
    private String check(HttpServletRequest request, String _id, LuckySheetGridModel curmodel, OperationTypeEnum operationTypeEnum){
        //校验代码
        return "";
    }
}
