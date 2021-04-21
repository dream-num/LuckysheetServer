package com.xc.luckysheet.mongo.impl;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.WriteResult;
import com.mongodb.client.result.UpdateResult;
import com.xc.luckysheet.entity.GridRecordDataModel;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库连接
 *
 * @author cr
 * @Date: 2021-02-27 14:06
 */
@Slf4j
public class BaseHandle {

    /**
     * 注入MongoTemplate
     */
    @Autowired
    protected MongoTemplate mongoTemplate;

    /**
     * 表名
     */
    protected static String COLLECTION_NAME = "luckysheet";

    /**
     * 数据中插入数据
     * @param query
     * @param update
     * @return
     */
    public boolean updateOne(Query query,Update update){
        try{
            log.info("select:"+query.getQueryObject().toString()+" \r\n update:"+update.getUpdateObject().toString());
        }catch (Exception ex){
            log.error(ex.getMessage());
        }
        try{
            UpdateResult writeResult=mongoTemplate.updateFirst(query,update,COLLECTION_NAME);
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }
    /**
     * 批量更新
     * @param query
     * @param update
     * @return
     */
    protected boolean updateMulti(Query query, Update update){
        try{
            log.info("select:"+query.getQueryObject().toString()+" \r\n update:"+update.getUpdateObject().toString());
        }catch (Exception ex){
            log.error(ex.getMessage());
        }
        try{
            UpdateResult writeResult=mongoTemplate.updateMulti(query,update,COLLECTION_NAME);
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }

    /**
     * 执行多个文档
     * @param _updates
     * @return
     */
    public Boolean updateMulti(List<Pair<Query,Update>> _updates){
        try{
            BulkOperations ops=mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, COLLECTION_NAME);
            ops.updateMulti(_updates);
            ops.execute();
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 组装文档查询条件
     * @param listId 文档ID
     * @param index  文档序号
     * @param block  块编号
     * @return
     */
    protected Document getQueryCondition(String listId, String index,String block){
        Document dbObject = new Document();
        dbObject.put("list_id", listId);
        dbObject.put("index", index);
        dbObject.put("block_id", block);
        dbObject.put("is_delete",0);
        return dbObject;
    }


    /**
     * 获取指定key的值
     * @param jsonObject
     * @param key
     * @return
     */
    protected String getStringByKey(JSONObject jsonObject,String key){
        Object obj=getByKey(jsonObject, key);
        if(obj==null){
            return null;
        }
        return obj.toString();
    }
    protected Object getByKey(JSONObject jsonObject,String key){
        if(jsonObject!=null&&jsonObject.containsKey(key)){
            return jsonObject.get(key);
        }
        return null;
    }

    /**
     * 将对象转换成JSONObject
     * @param m
     * @return
     */
    protected JSONObject tranToJSONObject(GridRecordDataModel m){
        //TODO: mongo的主键是字符串 _id 是否有使用
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("list_id",m.getList_id());
        jsonObject.put("row_col",m.getRow_col());
        jsonObject.put("index",m.getIndex());
        jsonObject.put("status",m.getStatus());
        jsonObject.put("block_id",m.getBlock_id());
        jsonObject.put("order",m.getOrder());
        jsonObject.put("is_delete",m.getIs_delete()==null?0:m.getIs_delete());

        jsonObject.put("json_data",m.getJson_data());
        return jsonObject;
    }

    /**
     * 查询条件转换
     * query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));
     *
     * @param query
     * @return
     */
    protected Criteria tranToCriteria(JSONObject query){
        Criteria criteria=null;
        if(query.containsKey("list_id")) {
            criteria=Criteria.where("list_id").is(query.get("list_id"));
            for (String k : query.keySet()) {
                if(!k.equals("list_id")){
                    criteria.and(k).is(query.get(k));
                }
            }
        }
        return criteria;
    }


    /**
     * 字符串位置处理
     * x,y,z  -> x.y.z
     * x,1,2  -> x.1.z
     * @param str
     * @return
     */
    protected String positionHandle(String str){
        if(str==null||str.indexOf(",")==-1){
            return str;
        }

        String[] strs=str.split(",");
        String result="";
        for(String s:strs){
            if(result.length()==0){
                result=s;
            }else{
                result+="."+s;
//                if(isNumeric(s)){
//                    result+="["+s+"]";
//                }else{
//                    result+="."+s;
//                }
            }
        }
        return "json_data."+result;
    }
    private Pattern patternIsNumeric = Pattern.compile("[0-9]*");
    /**
     * 利用正则表达式判断字符串是否是数字
     * @param str
     * @return
     */
    protected  boolean isNumeric(String str){
        Matcher isNum = patternIsNumeric.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }


    /**
     * 处理celldata中的数据(移动到顶级)
     * @param jsonObject
     */
    protected void cellDataHandle(JSONObject jsonObject){
        String cellDataKey="json_data";
        if(jsonObject.containsKey(cellDataKey)){
            boolean isDelCelldata=true;
            JSONObject data=new JSONObject();
            JSONObject jsonObject1=jsonObject.getJSONObject(cellDataKey);
            for(String key:jsonObject1.keySet()){
                if(cellDataKey.equals(key)){
                    isDelCelldata=false;
                }
                data.put(key,jsonObject1.get(key));
            }
            jsonObject.putAll(data);
            if(isDelCelldata){
                jsonObject.remove(cellDataKey);
            }
        }
    }


}
