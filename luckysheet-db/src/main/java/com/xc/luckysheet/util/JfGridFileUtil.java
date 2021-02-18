package com.xc.luckysheet.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public static JSONArray getSheetByIndex(JSONObject dbObject, Integer index){
        JSONArray _resultModel=null;
        if(dbObject!=null && dbObject instanceof List){
            List<JSONObject> _list=(List<JSONObject>)dbObject;
            for(JSONObject _o:_list){
                if(_o.containsKey("index")){
                    try{
                        Integer _index=Integer.parseInt(_o.get("index").toString());
                        if(index.equals(_index)){
                            if(_o.containsKey("celldata")){
                                _resultModel=_o.getJSONArray("celldata");
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
    public static JSONArray getSheetByIndex(JSONObject dbObject){
        JSONArray _resultModel=null;
        if(dbObject!=null){
            if(dbObject.containsKey("celldata")){
                _resultModel=dbObject.getJSONArray("celldata");
            }
        }
        return _resultModel;
    }

    /**
     * 按工作簿数量
     * @param dbObject
     * @return
     */
    public static Integer getSheetCount(JSONObject dbObject){
        Integer _resultModel=0;
        if(dbObject!=null){
            if(dbObject.containsKey("jfgridfile")){
                JSONObject _bs=dbObject.getJSONObject("jfgridfile");
                return getSheetCount(_bs);
            }else if(dbObject instanceof List){
                List<JSONObject> _list=(List<JSONObject>)dbObject;
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
    public static Integer getSheetPositionByIndex(JSONObject dbObject,Integer index){
        Integer _resultModel=null;
        if(dbObject!=null && dbObject instanceof List){
            List<JSONObject> _list=(List<JSONObject>)dbObject;
            //for(DBObject _o:_list){
            for(int x=0;x<_list.size();x++){
                JSONObject _o=_list.get(x);
                if(_o.containsKey("index")){
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
    public static Integer getSheetPositionByIndex(List<JSONObject> _list,String index){
        Integer _resultModel=null;
        if(_list!=null ){
            for(int x=0;x<_list.size();x++){
                JSONObject _o=_list.get(x);
                if(_o.containsKey("index")){
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
    public static List<String> getSheetKeyByIndex(List<JSONObject> _list,Integer index){
        List<String> _resultModel=new ArrayList<String>();
        if(_list!=null ){
            for(int x=0;x<_list.size();x++){
                JSONObject _o=_list.get(x);
                if(_o.containsKey("index")){
                    try{
                        Integer _index=Integer.parseInt(_o.get("index").toString());
                        if(index.equals(_index)){
                            if(_o.containsKey("_id")){
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
    public static JSONObject getObjectByIndex(JSONObject dbObject,Integer index,String k){
        JSONObject _resultModel=null;
        if(dbObject!=null && dbObject instanceof List){
            List<JSONObject> _list=(List<JSONObject>)dbObject;
            for(JSONObject _o:_list){
                if(_o.containsKey("index")){
                    try{
                        Integer _index=Integer.parseInt(_o.get("index").toString());
                        if(index.equals(_index)){
                            if(_o.containsKey(k)){
                                _resultModel=_o.getJSONObject(k);
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
    public static Object getObjectByIndex(JSONObject dbObject,String k){
        Object _resultModel=null;
        if(dbObject!=null){
            if(dbObject.containsKey(k)){
                _resultModel=dbObject.get(k);
            }
        }
        return _resultModel;
    }
    public static JSONObject getJSONObjectByIndex(JSONObject dbObject,String k){
        JSONObject _resultModel=null;
        if(dbObject!=null){
            if(dbObject.containsKey(k)&& dbObject.get(k) instanceof JSONObject){
                _resultModel=dbObject.getJSONObject(k);
            }
        }
        return _resultModel;
    }
    public static JSONArray getJSONArrayByIndex(JSONObject dbObject,String k){
        JSONArray _resultModel=null;
        if(dbObject!=null){
            if(dbObject.containsKey(k)&& dbObject.get(k) instanceof JSONArray){
                _resultModel=dbObject.getJSONArray(k);
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
    public static Integer getIntegerByIndex(JSONObject dbObject,Integer index,String k){
        Integer _resultModel=null;
        if(dbObject!=null && dbObject instanceof List){
            List<JSONObject> _list=(List<JSONObject>)dbObject;
            for(JSONObject _o:_list){
                if(_o.containsKey("index")){
                    try{
                        Integer _index=Integer.parseInt(_o.get("index").toString());
                        if(index.equals(_index)){
                            if(_o.containsKey(k)){
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
    public static Integer getIntegerByIndex(JSONObject dbObject,String k){
        Integer _resultModel=null;
        if(dbObject!=null){
            if(dbObject.containsKey(k)){
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
    public static JSONObject getObjectByObject(JSONObject dbObject,String k){
        if(dbObject!=null){
            if(dbObject.containsKey(k)){
                return dbObject.getJSONObject(k);
            }
        }
        return null;
    }



    /**
     * /获取测试用二维数组
     * @param rc
     * @param len
     * @return
     */
    public static JSONArray getTestData(String rc,int len){
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
        System.out.println(JSONArray.toJSONString(strs));
        List<Object> strs1=JSONObject.parseObject(JSONArray.toJSONString(strs),ArrayList.class);

        JSONArray _db=new JSONArray();
        _db.addAll(strs1);
        return _db;
    }

    /**
     * 给出查询条件
     * @param query
     * @return
     */
    public static String getCondition(JSONObject query){
        String condition="";
        if(query!=null) {
            Map<String, Object> queryDB = query.getInnerMap();
            for (String key : queryDB.keySet()) {
                condition = condition + " and t." + key + "='" + queryDB.get(key) + "'";
            }
        }
        return condition;
    }
    /**
     * 给出查询条件
     * @param query
     * @return
     */
    public static String getCondition(JSONObject query,List arr){
        String condition="";
        if(query!=null&&arr!=null) {
            Map<String,Object> queryDB=query.getInnerMap();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+" and t."+key+"=? ";
            }
        }
        return condition;
    }

    /**
     * 从jsonObject中找到第一个key的名字
     * @param jsonObject
     * @return
     */
    public static String getKeyName(JSONObject jsonObject){
        try{
            if(jsonObject!=null&&jsonObject.size()>0) {
                Map<String, Object> map = jsonObject.getInnerMap();
                return map.keySet().stream().findFirst().get();
//                for(String key:map.keySet()){
//                    return key;
//                }
            }
        }catch (Exception ex){
            log.error("jsonObject:{};{}",jsonObject.toString(SerializerFeature.WriteMapNullValue),ex.toString());
        }
        return "";
    }
}
