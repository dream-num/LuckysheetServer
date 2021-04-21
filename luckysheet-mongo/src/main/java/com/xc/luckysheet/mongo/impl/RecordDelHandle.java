package com.xc.luckysheet.mongo.impl;

import com.mongodb.client.result.DeleteResult;
import com.xc.luckysheet.db.IRecordDelHandle;
import com.xc.luckysheet.entity.GridRecordDataModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 删除
 * @author cr
 * @Date: 2021-02-27 14:10
 */
@Slf4j
@Repository(value = "mongoRecordDelHandle")
public class RecordDelHandle extends BaseHandle implements IRecordDelHandle {

    /**
     * 删除sheet（非物理删除）
     *
     * @param model
     * @return
     */
    @Override
    public boolean updateDataForReDel(GridRecordDataModel model) {
        Query query=new Query();
        query.addCriteria(Criteria.where("list_id").is(model.getList_id())
        .and("index").is(model.getIndex()));

        Update update=new Update();
        update.set("is_delete",model.getIs_delete());
        return updateMulti(query,update);
    }

    /**
     * 按ID 删除多个文档 （物理删除）
     *
     * @param ids
     * @return
     */
    @Override
    public String delDocuments(List<String> ids) {
        if(ids==null || ids.size()==0){
            return "";
        }
        Query query=new Query();
        query.addCriteria(Criteria.where("_id").in(ids.toArray()));
        try{
            mongoTemplate.remove(query,COLLECTION_NAME);
            return "";
        }catch (Exception ex){
            log.error(ex.toString());
            return ex.toString();
        }
    }

    /**
     * 按list_id 删除记录 （物理删除）
     *
     * @param listIds
     * @return
     */
    @Override
    public int[] delete(List<String> listIds) {
        if(listIds==null || listIds.size()==0){
            return null;
        }
        Query query=new Query();
        query.addCriteria(Criteria.where("list_id").in(listIds.toArray()));
        try{
            DeleteResult result=mongoTemplate.remove(query,COLLECTION_NAME);
            int[] i=new int[0];
            i[0]=(int)result.getDeletedCount();
            return i;
        }catch (Exception ex){
            log.error(ex.toString());
            return null;
        }
    }
}
