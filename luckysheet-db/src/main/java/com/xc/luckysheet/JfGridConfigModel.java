package com.xc.luckysheet;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Slf4j
public class JfGridConfigModel {

    /**
     * 表名
     */
    public static final String TABLENAME="luckysheet";

    /**
     * 每一块的行、列范围
     */
    public static Integer row_size;
    public static Integer col_size;
    /**
     * 第一块只保存二维数据以外的东西，其他“列号_行号”
     */
    public static final String FirstBlockID="fblock";


    /**
     * 默认第一块的编号
     */
    private static String FirstBlockId="";

    static {
        try {
            //获取默认第一块的编号
            FirstBlockId=JfGridConfigModel.FirstBlockID;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 返回设置的块范围
     * @return
     */
    public static String getRowCol(){
        return row_size+"_"+col_size;
    }
    private static Integer getRow(String rowCol){
        if(StringUtils.isBlank(rowCol)){
            return row_size;
        }
        try{
            return Integer.parseInt(rowCol.split("_")[0]);
        }catch (Exception ex){
            return row_size;
        }
    }
    private static Integer getCol(String rowCol){
        if(StringUtils.isBlank(rowCol)){
            return col_size;
        }
        try{
            return Integer.parseInt(rowCol.split("_")[1]);
        }catch (Exception ex){
            return col_size;
        }
    }



    /**
     * 获取块的范围
     * @param r 当前行
     * @param c 当前列
     * @param rowSize 行范围
     * @param colSize 列范围
     * @return
     */
    public static String getRange(Integer r,Integer c,Integer rowSize,Integer colSize){
        String _r=r/rowSize+"";
        String _c=c/colSize+"";
        String _result=_r+"_"+_c;
        return _result;
    }
    public static String getRange(Integer r,Integer c,String rowCol){
        return getRange(r,c,getRow(rowCol),getCol(rowCol));
    }

    /**
     * 获取块的范围
     * @param bson
     * @param rowSize
     * @param colSize
     * @return
     */
    private static String getRange(JSONObject bson, Integer rowSize, Integer colSize){
        if(bson.containsKey("r") && bson.containsKey("c")){
            try{
                //单元格的行号
                Integer _r=Integer.parseInt(bson.get("r").toString());
                //单元格的列号
                Integer _c=Integer.parseInt(bson.get("c").toString());
                return getRange(_r,_c,rowSize,colSize);
            }catch (Exception ex){
                log.error(ex.toString());
                return null;
            }
        }
        return null;
    }

    /**
     * 单个sheet数据拆分成多个(使用默认块大小)
     * @param sheet 一个sheet
     */
    public static List<JSONObject> toDataSplit(String rowCol, JSONObject sheet) {
        return toDataSplit(getRow(rowCol),getCol(rowCol),sheet);
    }

    public static Integer getSheetCount(List<JSONObject> dbObject){
        int i=0;
        if(dbObject!=null && dbObject.size()>0){
            for(JSONObject b:dbObject){
                if(b.containsKey("block_id") && FirstBlockID.equals(b.get("block_id"))){
                    i++;
                }
            }
        }
        return i;
    }

    /**
     * 单个sheet数据拆分成多个
     * @param rowSize 行数量
     * @param colSize 列数量
     * @param sheet 一个sheet
     */
    private static List<JSONObject> toDataSplit(Integer rowSize,Integer colSize,JSONObject sheet){
        List<JSONObject> list=new ArrayList<JSONObject>();
        if(sheet!=null && sheet.containsKey("celldata")){
            //单元格数据
            List<JSONObject> celldata=(List<JSONObject>)sheet.get("celldata");
            //相同的索引
            Object index=sheet.get("index");
            //序号
            Object list_id=null;
            if(sheet.containsKey("list_id")){
                list_id=sheet.get("list_id");
            }
            //Object order=sheet.get("order");//相同的位置

            //k 行号+列号 v 位置_datas下标
            Map<String,Integer> pos=new HashMap<String, Integer>();
            //分组的数据
            List<List<JSONObject>> datas=new ArrayList<List<JSONObject>>();

            if(celldata!=null && celldata.size()>0){
                for(JSONObject bson:celldata){
                    //获取到位置
                    String _pos=getRange(bson,rowSize,colSize);
                    if(_pos!=null){
                        //获取到数据集合
                        List<JSONObject> _data=null;
                        if(pos.containsKey(_pos)){
                            //获取对应集合
                            _data=datas.get(pos.get(_pos));
                        }else{
                            _data=new ArrayList<JSONObject>();
                            //保存位置信息
                            pos.put(_pos,datas.size());
                            //添加集合
                            datas.add(_data);
                        }
                        //添加新数据
                        _data.add(bson);
                    }
                }
            }

            //替换原始的数据
            //if(pos.containsKey(FirstBlockID)){
            //    sheet.put("celldata",datas.get(pos.get(FirstBlockID)));
            //}
            if(sheet.containsKey("_id")){
                sheet.remove("_id");
            }
            sheet.put("celldata",new ArrayList());
            list.add(sheet);

            for(String _pos:pos.keySet()){
                //if(_pos.equals(FirstBlockID)){
                //    continue;
                //}
                //获取对应集合
                List<JSONObject> _data=datas.get(pos.get(_pos));
                JSONObject _sheet=new JSONObject();
                _sheet.put("block_id",_pos);
                _sheet.put("celldata",_data);
                _sheet.put("index",index);
                if(list_id!=null){
                    _sheet.put("list_id",list_id);
                }
                list.add(_sheet);
                //_sheet.put("order",order);
            }

        }else{
            list.add(sheet);
        }
        return  list;

    }


}
