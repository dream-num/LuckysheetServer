package com.xc.luckysheet.mongo.impl;

import com.alibaba.fastjson.JSONObject;
import com.xc.luckysheet.db.IRecordDataInsertHandle;
import com.xc.luckysheet.entity.GridRecordDataModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加
 * @author cr
 * @Date: 2021-02-27 14:08
 */
@Slf4j
@Repository(value = "mongoRecordDataInsertHandle")
public class RecordDataInsertHandle extends BaseHandle implements IRecordDataInsertHandle {

    /**
     * 新增Sheet页,并返回刚刚插入的_id
     *
     * @param pgModel
     * @return
     */
    @Override
    public String insert(GridRecordDataModel pgModel) {
        try{
            JSONObject jsonObject=tranToJSONObject(pgModel);
            mongoTemplate.insert(jsonObject,COLLECTION_NAME);
            if(jsonObject.containsKey("_id")){
                return jsonObject.get("_id").toString();
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 批量添加 添加jsonb
     *
     * @param models
     * @return
     */
    @Override
    public String InsertIntoBatch(List<GridRecordDataModel> models) {
        List<JSONObject> jsonObjects=new ArrayList<>(5);
        for(GridRecordDataModel m:models){
            jsonObjects.add(tranToJSONObject(m));
        }
        return InsertBatchDb(jsonObjects);
    }

    /**
     * 批量添加 添加jsonb
     *
     * @param models
     * @return
     */
    @Override
    public String InsertBatchDb(List<JSONObject> models) {
        try{
            mongoTemplate.insert(models,COLLECTION_NAME);
            return "";
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }
}
