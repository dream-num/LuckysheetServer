package com.xc.luckysheet.mongo.impl;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.result.UpdateResult;
import com.xc.luckysheet.JfGridConfigModel;
import com.xc.luckysheet.db.IRecordDataUpdataHandle;
import com.xc.luckysheet.entity.GridRecordDataModel;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * 更新
 * @author cr
 * @Date: 2021-02-27 14:09
 */
@Slf4j
@Repository(value ="mongoRecordDataUpdataHandle" )
public class RecordDataUpdataHandle extends BaseHandle implements IRecordDataUpdataHandle {

    /**
     * sheet多块更新（先删除后添加）
     * 按IDS删除一组，然后新加处理后的
     * @param blocks
     * @param ids
     * @return
     */
    @Override
    public Boolean updateMulti2(List<JSONObject> blocks, List<String> ids) {
        try{
            List<ObjectId> objectIdList=new ArrayList<ObjectId>();
            if(ids!=null && ids.size()>0){
                for(String s:ids){
                    objectIdList.add(new ObjectId(s));
                }
            }

            Query del=new Query();
            del.addCriteria(Criteria.where("_id").in(objectIdList.toArray()));

            BulkOperations ops=mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,COLLECTION_NAME);
            ops.remove(del);
            ops.insert(blocks);
            ops.execute();
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 批量更新order 按listId，index，首块
     *
     * @param models
     * @return
     */
    @Override
    public boolean batchUpdateForNoJsonbData(List<GridRecordDataModel> models) {
        List<Pair<Query,Update>> _updates=new ArrayList<Pair<Query,Update>>(10);
        for(GridRecordDataModel dataModel:models){
            Query query=new Query();
            // 只修改信息块
            query.addCriteria(Criteria
                    .where("list_id").is(dataModel.getList_id())
                    .and("index").is(Integer.parseInt(dataModel.getIndex()))
                    .and("block_id").is(JfGridConfigModel.FirstBlockID));
            Update update=new Update();
            update.set("order",dataModel.getOrder());
            Pair<Query,Update> pair=Pair.of(query, update);
            _updates.add(pair);
        }

        if(_updates.size()>0){
            return updateMulti(_updates);
        }
        return false;
    }

    /**
     * 清除指定层级下某条数据
     *
     * @param query   键值对
     * @param keyName
     * @return
     */
    @Override
    public boolean rmCellDataValue(JSONObject query, String keyName) {
        try{
            Query q = new Query();
            q.addCriteria(tranToCriteria(query));
            //query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));

            Update u=new Update();
            u.unset("json_data."+positionHandle(keyName));
            return updateOne(q, u);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 更新jsonb中某条文本数据
     *
     * @param query    键值对
     * @param keyName
     * @param position 目前传入都是null
     * @param v
     * @return
     */
    @Override
    public boolean updateCellDataListTxtValue(JSONObject query, String keyName, Integer position, Object v) {
        try{
            Query q = new Query();
            q.addCriteria(tranToCriteria(query));
            //query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));
            keyName=positionHandle(keyName);
            Update u=new Update();
            if(position==null){
                u.set("json_data."+keyName,v);
            }else{
                u.set("json_data."+keyName+"."+position,v);
            }
            return updateOne(q, u);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 更新jsonb中某条文本数据
     *
     * @param query    键值对
     * @param keyName
     * @param position
     * @param v
     * @return
     */
    @Override
    public boolean updateCellDataListValue(JSONObject query, String keyName, String position, Object v) {
        //ToDo 与上一个方法一样
        try{
            Query q = new Query();
            q.addCriteria(tranToCriteria(query));
            //query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));
            keyName=positionHandle(keyName);
            Update u=new Update();
            if(position==null){
                u.set("json_data."+keyName,v);
            }else{
                u.set("json_data."+keyName+"."+position,v);
            }

            return updateOne(q, u);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * jsonb数据中元素添加元素
     *
     * @param query
     * @param word
     * @param db
     * @param position
     * @return
     */
    @Override
    public boolean updateJsonbForElementInsert(JSONObject query, String word, JSONObject db, Integer position) {
        try{
            Query q = new Query();
            q.addCriteria(tranToCriteria(query));
            word=positionHandle(word);
            Update u=new Update();
            if(position==0){
                u.push("json_data."+word, db);
            }else {
                u.push("json_data."+word + "." + position, db);
            }
            return updateOne(q, u);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 更新 ,将key设置NULL
     * @param query
     * @param word json字符串
     * @return
     */
    @Override
    public boolean rmJsonbDataForEmpty(JSONObject query, String word) {
        DBObject v=new BasicDBObject();
        try{
            Query q = new Query();
            q.addCriteria(tranToCriteria(query));
            Update u=new Update();
            JSONObject wordJson=JSONObject.parseObject("{"+word+"}");
            for(String k:wordJson.keySet()){
                u.set("json_data."+k,v);
            }
            return updateOne(q, u);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 更新 ,按key设置值
     * @param query
     * @param word json字符串
     * @return
     */
    @Override
    public boolean updateJsonbDataForKeys(JSONObject query, JSONObject word) {
        try{
            Query q = new Query();
            q.addCriteria(tranToCriteria(query));
            Update u=new Update();
            for(String k:word.keySet()){
                u.set("json_data."+k,word.get(k));
            }
            return updateOne(q, u);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 更新status状态
     *
     * @param model
     * @return
     */
    @Override
    public boolean updateDataStatus(GridRecordDataModel model) {
        List<Pair<Query,Update>> _updates=new ArrayList<Pair<Query,Update>>();
        //设置全部status=0
        Query query=new Query();
        query.addCriteria(Criteria.where("list_id").is(model.getList_id()).and("status").is(1).and("block_id").is(JfGridConfigModel.FirstBlockID));
        Update update=new Update();
        update.set("status",0);
        Pair<Query,Update> pair=Pair.of(query, update);
        _updates.add(pair);
        //设置激活的文档
        Query query2=new Query();
        query2.addCriteria(Criteria.where("list_id").is(model.getList_id()).and("index").is(model.getIndex()).and("block_id").is(JfGridConfigModel.FirstBlockID));
        Update update2=new Update();
        update2.set("status",1);
        Pair<Query,Update> pair2=Pair.of(query2, update2);
        _updates.add(pair2);

        return updateMulti(_updates);

    }

    /**
     * 更新sheet隐藏状态
     *
     * @param model
     * @param hide
     * @param index1
     * @param index2
     * @return
     */
    @Override
    public boolean updateDataMsgHide(GridRecordDataModel model, Integer hide, String index1, String index2) {
        List<Pair<Query,Update>> _updates=new ArrayList<Pair<Query,Update>>();
        //设置隐藏
        //设置i对应文档隐藏，并且status=0
        Query query=new Query();
        query.addCriteria(Criteria.where("list_id").is(model.getList_id()).and("index").is(index1).and("block_id").is(JfGridConfigModel.FirstBlockID));
        Update update=new Update();
        update.set("hide",hide);
        update.set("status",0);
        Pair<Query,Update> pair=Pair.of(query, update);
        _updates.add(pair);

        if(index2!=null) {
            //设置cur对应文档status=1
            Query query2 = new Query();
            query2.addCriteria(Criteria.where("list_id").is(model.getList_id()).and("index").is(index2).and("block_id").is(JfGridConfigModel.FirstBlockID));
            Update update2 = new Update();
            update2.set("status", 1);
            Pair<Query, Update> pair2 = Pair.of(query2, update2);
            _updates.add(pair2);
        }

        return updateMulti(_updates);

    }

    /**
     * 更新sheet隐藏状态
     *
     * @param model
     * @param hide
     * @param index
     * @return
     */
    @Override
    public boolean updateDataMsgNoHide(GridRecordDataModel model, Integer hide, String index) {
        List<Pair<Query,Update>> _updates=new ArrayList<Pair<Query,Update>>();
        //设置全部status=0
        Query query=new Query();
        query.addCriteria(Criteria.where("list_id").is(model.getList_id()).and("block_id").is(JfGridConfigModel.FirstBlockID));
        Update update=new Update();
        update.set("status",0);
        Pair<Query,Update> pair=Pair.of(query, update);
        _updates.add(pair);

        //设置取消隐藏的文档
        Query query2=new Query();
        query2.addCriteria(Criteria.where("list_id").is(model.getList_id()).and("index").is(index).and("block_id").is(JfGridConfigModel.FirstBlockID));
        Update update2=new Update();
        update2.set("hide",hide);
        update2.set("status",1);
        Pair<Query,Update> pair2=Pair.of(query2, update2);
        _updates.add(pair2);
        return updateMulti(_updates);

    }

    /**
     * 更新jsonb中某条文本数据
     *
     * @param block_ids 要删除的
     * @param models    新增加的
     * @return
     */
    @Override
    public boolean batchUpdateCellDataValue(List<String> block_ids, List<GridRecordDataModel> models) {
        try{
            Query del=new Query();
            del.addCriteria(Criteria.where("list_id").is(models.get(0).getList_id())
                    .and("index").is(models.get(0).getIndex())
                    .and("block_id").in(block_ids.toArray()));

            List<JSONObject> jsonObjects=new ArrayList<>(5);
            for(GridRecordDataModel m:models){
                jsonObjects.add(tranToJSONObject(m));
            }

            BulkOperations ops=mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, COLLECTION_NAME);
            ops.remove(del);
            ops.insert(jsonObjects);
            ops.execute();
            return true;
        }catch (Exception ex){
            log.error(ex.toString());
            return false;
        }
    }

    /**
     * jsonb数据中元素添加元素（集合插入）
     * @param query 查询条件
     * @param word  路径
     * @param db    内容
     * @param position  位置   (无效)
     * @param words  初始化的内容 (无效)
     * @return
     */
    @Override
    public boolean updateJsonbForInsertNull(JSONObject query, String word, JSONObject db, Integer position, String words) {
        Query q=new Query();
        q.addCriteria(tranToCriteria(query));

        Update update=new Update();
        BasicDBList _dlist=new BasicDBList();
        _dlist.add(db);
        update.set("json_data."+word, _dlist);

        return updateOne(q,update);

    }

    /**
     * jsonb数据中元素添加元素（集合插入）,不存在创建一个空集合  (同上一个方法)
     * @param query 查询条件
     * @param word  路径
     * @param db    内容
     * @param position  位置  （无效）
     * @return
     */
    @Override
    public boolean updateJsonbForSetNull(JSONObject query, String word, JSONObject db, Integer position) {
//        Query q=new Query();
//        q.addCriteria(tranToCriteria(query));
//
//        Update update=new Update();
//        BasicDBList _dlist=new BasicDBList();
//        _dlist.add(db);
//        update.set(word, _dlist);
//
//        return updateOne(q,update);
        return updateJsonbForInsertNull(query,word,db,null,null);
    }

    /**
     * jsonb数据中元素添加元素(根节点)
     *
     * @param query
     * @param word
     * @param db
     * @param position （无效）
     * @param words （无效）
     * @return
     */
    @Override
    public boolean updateJsonbForSetRootNull(JSONObject query, String word, JSONObject db, Integer position, String words) {
        Query q=new Query();
        q.addCriteria(tranToCriteria(query));

        Update update=new Update();
        update.set("json_data."+positionHandle(word), db);

        return updateOne(q,update);
    }
}
