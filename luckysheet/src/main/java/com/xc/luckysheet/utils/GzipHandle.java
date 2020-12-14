package com.xc.luckysheet.utils;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
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
    public static void toCompressBySheet(DBObject sheet){
        if(runGzip){
            if(sheet!=null && sheet.containsField("celldata")){
                List<DBObject> _celldata=(List<DBObject>)sheet.get("celldata");
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
    public static String toCompressByCelldata(List<DBObject> _celldata){
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
    public static String toCompressByCelldata(BasicDBList _celldata){
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
    public static void toUncompressBySheet(DBObject sheet){
        if(sheet!=null && sheet.containsField("celldata")){
            if(sheet.get("celldata") instanceof String){
                String celldataStr=sheet.get("celldata").toString();
                sheet.put("celldata",toUncompressBySheet(celldataStr));
            }
        }
    }
    public static BasicDBList toUncompressBySheet(String celldataStr){
        BasicDBList list=new BasicDBList();
        if(celldataStr!=null){
            String _gzipStr= Pako_GzipUtils.uncompress(celldataStr);
            try{
                list=(BasicDBList)JSON.parse(_gzipStr);
            }catch (Exception ex){
                log.error(ex.getMessage());
            }
        }
        return list;
    }

}
