package com.xc.luckysheet.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 压缩处理类
 * @author Administrator
 */
@Slf4j
public class GzipHandle {
    /**
     * 是否对celldata的数据压缩(分块存储，不再使用压缩)
     */
    public static boolean runGzip=false;

    /**
     * 加密 单个sheet，celldata处理
     * @param sheet
     */
    public static void toCompressBySheet(JSONObject sheet){
        if(runGzip){
            if(sheet!=null && sheet.containsKey("celldata")){
                List<JSONObject> _celldata=(List<JSONObject>)sheet.get("celldata");
                //String _gzipStr= Pako_GzipUtils.compress(_celldata.toString());
                sheet.put("celldata",toCompressByCelldata(_celldata));
            }
        }
    }

    /**
     * 压缩  celldata
     * @param _celldata
     * @return
     */
    public static String toCompressByCelldata(List<JSONObject> _celldata){
        if(_celldata!=null){
            String _gzipStr= Pako_GzipUtils.compress(_celldata.toString());
            return _gzipStr;
        }else{
            return "";
        }
    }

    /**
     * 压缩  celldata
     * @param _celldata
     * @return
     */
    public static String toCompressByCelldata(JSONArray _celldata){
        if(_celldata!=null){
            String _gzipStr= Pako_GzipUtils.compress(_celldata.toString());
            return _gzipStr;
        }else{
            return "";
        }
    }

    /**
     * 将celldata中数据解压
     * @param sheet
     */
    public static void toUncompressBySheet(JSONObject sheet){
        if(sheet!=null && sheet.containsKey("celldata")){
            if(sheet.get("celldata") instanceof String){
                String celldataStr=sheet.get("celldata").toString();
                sheet.put("celldata",toUncompressBySheet(celldataStr));
            }
        }
    }
    public static JSONArray toUncompressBySheet(String celldataStr){
        JSONArray list=new JSONArray();
        if(celldataStr!=null){
            String _gzipStr= Pako_GzipUtils.uncompress(celldataStr);
            try{
                list=JSONArray.parseArray(_gzipStr);
            }catch (Exception ex){
                log.error(ex.getMessage());
            }
        }
        return list;
    }

}
