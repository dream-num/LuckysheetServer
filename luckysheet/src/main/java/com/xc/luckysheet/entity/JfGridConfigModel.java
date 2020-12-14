package com.xc.luckysheet.entity;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;

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
     * 获取块的范围
     * @param r 当前行
     * @param c 当前列
     * @param row_size 行范围
     * @param col_size 列范围
     * @return
     */
    public static String getRange(Integer r,Integer c,Integer row_size,Integer col_size){
        String _r=r/row_size+"";
        String _c=c/col_size+"";
        String _result=_r+"_"+_c;
        //System.out.println(_result);
        return _result;
    }
    public static String getRange(Integer r,Integer c){
        String _r=r/row_size+"";
        String _c=c/col_size+"";
        String _result=_r+"_"+_c;
        //System.out.println(_result);
        return _result;
    }

    /**
     * 获取块的范围
     * @param bson
     * @param row_size
     * @param col_size
     * @return
     */
    private static String getRange(DBObject bson,Integer row_size,Integer col_size){
        if(bson.containsField("r") && bson.containsField("c")){
            try{
                //单元格的行号
                Integer _r=Integer.parseInt(bson.get("r").toString());
                //单元格的列号
                Integer _c=Integer.parseInt(bson.get("c").toString());
                return getRange(_r,_c,row_size,col_size);
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
    public static List<DBObject> toDataSplit(DBObject sheet) {
        return toDataSplit(row_size,col_size,sheet);
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
     * @param row_size 行数量
     * @param col_size 列数量
     * @param sheet 一个sheet
     */
    private static List<DBObject> toDataSplit(Integer row_size,Integer col_size,DBObject sheet){
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
                    String _pos=getRange(bson,row_size,col_size);
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
