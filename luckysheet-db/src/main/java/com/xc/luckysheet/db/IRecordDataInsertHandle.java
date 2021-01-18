package com.xc.luckysheet.db;

import com.alibaba.fastjson.JSONObject;
import com.xc.luckysheet.entity.GridRecordDataModel;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 插入数据
 * @author Administrator
 */
public interface IRecordDataInsertHandle {
    /**
     * 新增Sheet页,并返回刚刚插入的_id
     * @param pgModel
     * @return
     */
    String insert(GridRecordDataModel pgModel);
    /**
     * 批量添加 添加jsonb
     * @param models
     * @return
     */
    String InsertIntoBatch(List<GridRecordDataModel> models);

    /**
     * 批量添加 添加jsonb
     * @param models
     * @return
     */
    String InsertBatchDb(List<JSONObject> models);
}
