package com.xc.luckysheet.entity;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
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
     * 每一块的行、列范围
     */
    public static Integer row_size;
    public static Integer col_size;
    /**
     * 第一块只保存二维数据以外的东西，其他“列号_行号”
     */
    public static final String FirstBlockID="fblock";
    /**
     * 文件保存在mongodb中  目前模式为保存到磁盘中
     */
    public static final Boolean isFileSaveMongodb=false;

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
    private static String getRange(DBObject bson,Integer rowSize,Integer colSize){
        if(bson.containsField("r") && bson.containsField("c")){
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
    public static List<DBObject> toDataSplit(String rowCol,DBObject sheet) {
        return toDataSplit(getRow(rowCol),getCol(rowCol),sheet);
    }

    public static Integer getSheetCount(List<DBObject> dbObject){
        int i=0;
        if(dbObject!=null && dbObject.size()>0){
            for(DBObject b:dbObject){
                if(b.containsField("block_id") && FirstBlockID.equals(b.get("block_id"))){
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
    private static List<DBObject> toDataSplit(Integer rowSize,Integer colSize,DBObject sheet){
        List<DBObject> list=new ArrayList<DBObject>();
        if(sheet!=null && sheet.containsField("celldata")){
            //单元格数据
            List<DBObject> celldata=(List<DBObject>)sheet.get("celldata");
            //相同的索引
            Object index=sheet.get("index");
            //序号
            Object list_id=null;
            if(sheet.containsField("list_id")){
                list_id=sheet.get("list_id");
            }
            //Object order=sheet.get("order");//相同的位置

            //k 行号+列号 v 位置_datas下标
            Map<String,Integer> pos=new HashMap<String, Integer>();
            //分组的数据
            List<List<DBObject>> datas=new ArrayList<List<DBObject>>();

            if(celldata!=null && celldata.size()>0){
                for(DBObject bson:celldata){
                    //获取到位置
                    String _pos=getRange(bson,rowSize,colSize);
                    if(_pos!=null){
                        //获取到数据集合
                        List<DBObject> _data=null;
                        if(pos.containsKey(_pos)){
                            //获取对应集合
                            _data=datas.get(pos.get(_pos));
                        }else{
                            _data=new ArrayList<DBObject>();
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
            if(sheet.containsField("_id")){
                sheet.removeField("_id");
            }
            sheet.put("celldata",new BasicDBList());
            list.add(sheet);

            for(String _pos:pos.keySet()){
                //if(_pos.equals(FirstBlockID)){
                //    continue;
                //}
                //获取对应集合
                List<DBObject> _data=datas.get(pos.get(_pos));
                DBObject _sheet=new BasicDBObject();
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
