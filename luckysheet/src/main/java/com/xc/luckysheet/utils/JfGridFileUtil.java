package com.xc.luckysheet.utils;

import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
@Slf4j
public class JfGridFileUtil {

    /**
     * 按工作簿index获取对应集合
     * @param dbObject
     * @param index
     * @return
     */
    public static BasicDBList getSheetByIndex( DBObject dbObject,Integer index){
        BasicDBList _resultModel=null;
        if(dbObject!=null && dbObject instanceof List){
            List<DBObject> _list=(List<DBObject>)dbObject;
            for(DBObject _o:_list){
                if(_o.containsField("index")){
                    try{
                        Integer _index=Integer.parseInt(_o.get("index").toString());
                        if(index.equals(_index)){
                            if(_o.containsField("celldata")){
                                _resultModel=(BasicDBList)_o.get("celldata");
                            }
                        }
                    }catch (Exception ex){
                        log.error(ex.toString());
                    }
                }
            }
        }
        return _resultModel;
    }

    /**
     * 从对象中获取数据calldata信息
     * @param dbObject
     * @return
     */
    public static BasicDBList getSheetByIndex(DBObject dbObject){
        BasicDBList _resultModel=null;
        if(dbObject!=null){
            if(dbObject.containsField("celldata")){
                _resultModel=(BasicDBList)dbObject.get("celldata");
            }
        }
        return _resultModel;
    }

    /**
     * 按工作簿数量
     * @param dbObject
     * @return
     */
    public static Integer getSheetCount(DBObject dbObject){
        Integer _resultModel=0;
        if(dbObject!=null){
            if(dbObject.containsField("jfgridfile")){
                DBObject _bs=(DBObject)dbObject.get("jfgridfile");
                return getSheetCount(_bs);
            }else if(dbObject instanceof List){
                List<DBObject> _list=(List<DBObject>)dbObject;
                if(_list!=null){
                    _resultModel=_list.size();
                }
            }
        }
        return _resultModel;
    }

    /**
     * 按工作簿index获取对应集合的序号
     * @param dbObject
     * @param index
     * @return
     */
    public static Integer getSheetPositionByIndex(DBObject dbObject,Integer index){
        Integer _resultModel=null;
        if(dbObject!=null && dbObject instanceof List){
            List<DBObject> _list=(List<DBObject>)dbObject;
            //for(DBObject _o:_list){
            for(int x=0;x<_list.size();x++){
                DBObject _o=_list.get(x);
                if(_o.containsField("index")){
                    try{
                        Integer _index=Integer.parseInt(_o.get("index").toString());
                        if(index.equals(_index)){
                            _resultModel=x;
                            break;
                        }
                    }catch (Exception ex){
                        log.error(ex.toString());
                    }
                }
            }
        }
        return _resultModel;
    }
    public static Integer getSheetPositionByIndex(List<DBObject> _list,String index){
        Integer _resultModel=null;
        if(_list!=null ){
            for(int x=0;x<_list.size();x++){
                DBObject _o=_list.get(x);
                if(_o.containsField("index")){
                    try{
                        Integer _index=Integer.parseInt(_o.get("index").toString());
                        if(index.equals(_index)){
                            _resultModel=x;
                            break;
                        }
                    }catch (Exception ex){
                        log.error(ex.toString());
                    }
                }
            }
        }
        return _resultModel;
    }

    /**
     * 按index值获取mongodb的key
     * @param _list
     * @param index
     * @return
     */
    public static List<String> getSheetKeyByIndex(List<DBObject> _list,Integer index){
        List<String> _resultModel=new ArrayList<String>();
        if(_list!=null ){
            for(int x=0;x<_list.size();x++){
                DBObject _o=_list.get(x);
                if(_o.containsField("index")){
                    try{
                        Integer _index=Integer.parseInt(_o.get("index").toString());
                        if(index.equals(_index)){
                            if(_o.containsField("_id")){
                                //_resultModel=_o.get("_id").toString();
                                _resultModel.add(_o.get("_id").toString());
                            }
                            //break;
                        }
                    }catch (Exception ex){
                        log.error(ex.toString());
                    }
                }
            }
        }
        return _resultModel;
    }

    /**
     * 按工作簿index 获取对象每一个工作簿的第一层对象
     * @param dbObject
     * @param index
     * @param k
     * @return
     */
    public static DBObject getObjectByIndex(DBObject dbObject,Integer index,String k){
        DBObject _resultModel=null;
        if(dbObject!=null && dbObject instanceof List){
            List<DBObject> _list=(List<DBObject>)dbObject;
            for(DBObject _o:_list){
                if(_o.containsField("index")){
                    try{
                        Integer _index=Integer.parseInt(_o.get("index").toString());
                        if(index.equals(_index)){
                            if(_o.containsField(k)){
                                _resultModel=(DBObject)_o.get(k);
                            }
                            break;
                        }
                    }catch (Exception ex){
                        log.error(ex.toString());
                    }
                }
            }
        }
        return _resultModel;
    }

    /**
     * 按工作簿index 获取对象每一个工作簿的第一层对象(传入为单sheet)
     * @param dbObject
     * @param k
     * @return
     */
    public static DBObject getObjectByIndex(DBObject dbObject,String k){
        DBObject _resultModel=null;
        if(dbObject!=null){
            if(dbObject.containsField(k)){
                _resultModel=(DBObject)dbObject.get(k);
            }
        }
        return _resultModel;
    }


    /**
     * 按工作簿index 获取对象每一个工作簿的第一层对象
     * @param dbObject
     * @param index
     * @param k
     * @return
     */
    public static Integer getIntegerByIndex(DBObject dbObject,Integer index,String k){
        Integer _resultModel=null;
        if(dbObject!=null && dbObject instanceof List){
            List<DBObject> _list=(List<DBObject>)dbObject;
            for(DBObject _o:_list){
                if(_o.containsField("index")){
                    try{
                        Integer _index=Integer.parseInt(_o.get("index").toString());
                        if(index.equals(_index)){
                            if(_o.containsField(k)){
                                _resultModel=(Integer)_o.get(k);
                            }
                            break;
                        }
                    }catch (Exception ex){
                        log.error(ex.toString());
                    }
                }
            }
        }
        return _resultModel;
    }

    /**
     * 按工作簿index 获取对象每一个工作簿的第一层对象(单个工作簿)
     * @param dbObject
     * @param k
     * @return
     */
    public static Integer getIntegerByIndex(DBObject dbObject,String k){
        Integer _resultModel=null;
        if(dbObject!=null){
            if(dbObject.containsField(k)){
                _resultModel=(Integer)dbObject.get(k);
            }
        }
        return _resultModel;
    }

    /**
     * 从对象中获取一个指定的对象
     * @param dbObject
     * @param k
     * @return
     */
    public static DBObject getObjectByObject(DBObject dbObject,String k){
        if(dbObject!=null && dbObject instanceof DBObject){
            DBObject _d=(DBObject)dbObject;
            if(_d.containsField(k)){
                return (DBObject)_d.get(k);
            }
        }
        return null;
    }

    public static ObjectId getObjectId(String gridKey)throws Exception{
        try{
           return  new ObjectId(gridKey);
        }catch (Exception ex){
            throw new RuntimeException("ObjectId 转换错误");
        }
    }

    /**
     * /获取测试用二维数组
     * @param rc
     * @param len
     * @return
     */
    public static BasicDBList getTestData(String rc,int len){
        int row=0;
        int column=0;
        if(rc.equals("r")){
            //增加行
            row=len;
            column=5;
        }else{
            //增加列
            row=5;
            column=len;
        }
        Object[][] strs=new String[row][column];
        for(int x=0;x<row;x++){
            for(int x1=0;x1<column;x1++){
                if(x1==x){
                    //continue;
                }
                strs[x][x1]=x+""+x1;
            }
        }
        System.out.println(new Gson().toJson(strs));
        List<Object> strs1=new Gson().fromJson(new Gson().toJson(strs),ArrayList.class);

        BasicDBList _db=new BasicDBList();
        _db.addAll(strs1);
        return _db;
    }
}
