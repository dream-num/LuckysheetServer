package com.xc.luckysheet.mongo.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import com.xc.luckysheet.JfGridConfigModel;
import com.xc.luckysheet.db.IRecordSelectHandle;
import com.xc.luckysheet.util.JfGridFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 查询
 * @author cr
 * @Date: 2021-02-27 14:11
 */
@Slf4j
@Repository(value = "mongoRecordSelectHandle")
public class RecordSelectHandle extends BaseHandle implements IRecordSelectHandle {

    /**
     * 查看指定sheet页 查看第一块是否存在（控制块）
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public Integer getFirstBlockByGridKey(String listId, String index) {
        //默认获取第一块
        Document dbObject = getQueryCondition(listId,index,JfGridConfigModel.FirstBlockID);

        Document fieldsObject=new Document();
        fieldsObject.put("_id",false);
        fieldsObject.put("list_id",true);
        fieldsObject.put("index",true);

        try{
            Query query = new BasicQuery(dbObject, fieldsObject);
            JSONObject jsonObject=(JSONObject)mongoTemplate.findOne(query, JSONObject.class, COLLECTION_NAME);
            if(jsonObject==null){
                return null;
            }
            return 1;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定的xls激活的sheet页的 返回index（控制块）
     *
     * @param listId
     * @return
     */
    @Override
    public String getFirstBlockIndexByGridKey(String listId) {
        Document dbObject = new Document();
        dbObject.put("list_id", listId);
        dbObject.put("block_id", JfGridConfigModel.FirstBlockID);
        dbObject.put("status",1);
        dbObject.put("is_delete",0);

        Document fieldsObject=new Document();
        fieldsObject.put("_id",false);
        fieldsObject.put("list_id",true);
        fieldsObject.put("index",true);

        try{
            Query query = new BasicQuery(dbObject, fieldsObject);
            JSONObject jsonObject=(JSONObject)mongoTemplate.findOne(query, JSONObject.class, COLLECTION_NAME);
            return getStringByKey(jsonObject,"index");
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }

    }

    /**
     * 获取指定的xls，sheet 第一块的行列信息（控制块）
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public String getFirstBlockRowColByGridKey(String listId, String index) {
        Document dbObject = getQueryCondition(listId,index,JfGridConfigModel.FirstBlockID);

        Document fieldsObject=new Document();
        fieldsObject.put("_id",false);
        fieldsObject.put("list_id",true);
        fieldsObject.put("index",true);
        fieldsObject.put("row_col",true);

        try{
            Query query = new BasicQuery(dbObject, fieldsObject);
            JSONObject jsonObject=(JSONObject)mongoTemplate.findOne(query, JSONObject.class, COLLECTION_NAME);
            return getStringByKey(jsonObject,"row_col");
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 按指定xls，sheet顺序返回整个xls结构
     * 不返回celldata ,只获取信息块
     *
     * @param listId
     * @return
     */
    @Override
    public List<JSONObject> getByGridKey_NOCelldata(String listId) {
        try{
            Document fieldsObject=new Document();
            fieldsObject.put("json_data",false);
            Document searchObject1=new Document();
            Query query=new BasicQuery(searchObject1,fieldsObject);
            //Query query=new Query();
            query.addCriteria(
                    Criteria.where("list_id").is(listId)
                            .and("block_id").is(JfGridConfigModel.FirstBlockID)
                            .and("is_delete").is(0))
                    .with(new Sort(Sort.Direction.ASC, "order"));
            log.info("query "+query+" COLLECTION_NAME:"+COLLECTION_NAME);
            return mongoTemplate.find(query, JSONObject.class, COLLECTION_NAME);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 按指定xls，sheet获取，返回指定的sheet集合
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public List<JSONObject> getBlockAllByGridKey(String listId, String index) {
        try{
            Query query=new Query();
            query.addCriteria(Criteria.where("list_id").is(listId)
                    .and("index").in(index)
                    .and("is_delete").is(0));
            List<JSONObject> jsonObjects= mongoTemplate.find(query, JSONObject.class, COLLECTION_NAME);
            return jsonObjects;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定xls，sheet，block的数据
     *
     * @param listId
     * @param index
     * @param blockId
     * @return
     */
    @Override
    public JSONObject getCelldataByGridKey(String listId, String index, String blockId) {
        try{
            Document dbObject = getQueryCondition(listId,index,blockId);

            Document fieldsObject=new Document();
            fieldsObject.put("_id",true);
            fieldsObject.put("list_id",true);
            fieldsObject.put("index",true);
            fieldsObject.put("json_data.celldata",true);
            fieldsObject.put("json_data.column",true);
            fieldsObject.put("json_data.row",true);

            Query query = new BasicQuery(dbObject, fieldsObject);
            JSONObject jsonObject=  mongoTemplate.findOne(query, JSONObject.class, COLLECTION_NAME);
            cellDataHandle(jsonObject);
            return jsonObject;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定xls、sheet中的config中数据 （存放在第一块中）
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public JSONObject getConfigByGridKey(String listId, String index) {
        //默认获取第一块
        try{
            Document dbObject =getQueryCondition(listId,index,JfGridConfigModel.FirstBlockID);

            Document fieldsObject=new Document();
            fieldsObject.put("_id",false);
            fieldsObject.put("list_id",true);
            fieldsObject.put("index",true);
            fieldsObject.put("json_data.config",true);
            fieldsObject.put("json_data.calcChain",true);
            fieldsObject.put("json_data.filter",true);
            fieldsObject.put("block_id",true);

            Query query = new BasicQuery(dbObject, fieldsObject);
            JSONObject jsonObject=mongoTemplate.findOne(query, JSONObject.class, COLLECTION_NAME);
            cellDataHandle(jsonObject);
            return jsonObject;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 按list_id获取，返回指定sheet 当前sheet的全部分块数据（并合并）getMergeByGridKey
     * 返回是DBObject，而下面这个方法返回仅仅只有celldata
     *
     * @param listId
     * @param index
     * @param ids    返回记录存在数据库的ID
     * @return
     */
    @Override
    public JSONObject getBlockMergeByGridKey(String listId, String index, List<String> ids) {
        JSONObject _fblock=new JSONObject();
        JSONArray _celldata=new JSONArray();
        //获取全部块
        List<JSONObject> blocks=getBlockAllByGridKey(listId, index);
        if(blocks!=null && blocks.size()>0){
            for(JSONObject _b:blocks){
                if(ids!=null){
                    if(_b.containsKey("_id")){
                        ids.add(_b.get("_id").toString());
                    }
                }
                if(_b.containsKey("block_id")){
                    if(JfGridConfigModel.FirstBlockID.equals(_b.get("block_id"))){
                        //信息块
                        _fblock=_b;
                    }else{
                        //数据块
                        //注意：此处与MySql不同
                        JSONArray _blockCellData=JfGridFileUtil.getSheetByIndex(_b);
                        if(_blockCellData!=null){
                            _celldata.addAll(_blockCellData);
                        }
                    }
                }
            }
        }
        _fblock.put("celldata",_celldata);
        return _fblock;
    }

    /**
     * 按list_id获取（id,index），返回sheet集合
     *
     * @param listId
     * @param flag   是否仅仅获取主要模块
     * @return
     */
    @Override
    public List<JSONObject> getBlocksByGridKey(String listId, boolean flag) {
        try{
//            Query query=new Query();
//            query.addCriteria(Criteria.where("list_id").is(list_id).and("index").is(index));
//            return mongoTemplate.find(query, DBObject.class, NEW_COLLECTION_NAME);

            Document dbObject = new Document();
            dbObject.put("list_id", listId);
            if(flag){
                dbObject.put("block_id", JfGridConfigModel.FirstBlockID);
            }
            dbObject.put("is_delete",0);

            Document fieldsObject=new Document();
            fieldsObject.put("_id",true);
            fieldsObject.put("list_id",true);
            fieldsObject.put("index",true);

            Query query = new BasicQuery(dbObject, fieldsObject);
            return  mongoTemplate.find(query, JSONObject.class, COLLECTION_NAME);
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定xls，多个sheet的全部分块
     *
     * @param listId
     * @param indexs
     * @return
     */
    @Override
    public List<JSONObject> getAllIndexsByGridKey(String listId, List<String> indexs) {
        try{
            Query query=new Query();
            query.addCriteria(Criteria.where("list_id").is(listId)
                    .and("index").in(indexs)
                    .and("is_delete").is(0))
                    .with(new Sort(Sort.Direction.ASC, "order"));
            log.info("getByGridKey--"+query);
            return mongoTemplate.find(query, JSONObject.class, COLLECTION_NAME);
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定xls,sheet全部内容
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public List<JSONObject> getIndexsByGridKey(String listId, String index) {
        try{
            Query query=new Query();
            query.addCriteria(Criteria.where("list_id").is(listId)
                    .and("index").in(index)
                    .and("is_delete").is(0))
                    .with(new Sort(Sort.Direction.ASC, "order"));
            return mongoTemplate.find(query, JSONObject.class, COLLECTION_NAME);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取图表数据（第一块）
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public JSONObject getChartByGridKey(String listId, String index) {
        try{
            Document dbObject = getQueryCondition(listId,index,JfGridConfigModel.FirstBlockID);

            Document fieldsObject=new Document();
            fieldsObject.put("_id",false);
            fieldsObject.put("list_id",true);
            fieldsObject.put("index",true);
            fieldsObject.put("json_data.chart",true);
            fieldsObject.put("block_id",true);

            Query query = new BasicQuery(dbObject, fieldsObject);
            JSONObject jsonObject= mongoTemplate.findOne(query, JSONObject.class, COLLECTION_NAME);
            cellDataHandle(jsonObject);
            return jsonObject;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }
}
