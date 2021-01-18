package com.xc.luckysheet.db;

import com.alibaba.fastjson.JSONObject;
import com.xc.luckysheet.entity.GridRecordDataModel;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * json数据更新处理
 * @author Administrator
 */
public interface IRecordDataUpdataHandle {

    /**
     * sheet多块更新（先删除后添加）
     * 按IDS删除一组，然后新加处理后的
     * @param blocks
     * @param ids
     * @return
     */
    Boolean updateMulti2(List<JSONObject> blocks, List<String> ids);


    /**
     * 批量更新
     * @param models
     * @return
     */
    boolean batchUpdateForNoJsonbData(List<GridRecordDataModel> models);

    /**
     * 清除指定层级下某条数据
     * @param query   键值对
     * @param keyName
     * @return
     */
    boolean rmCellDataValue(JSONObject query, String keyName);

    /**
     * 更新jsonb中某条文本数据
     * @param query    键值对
     * @param keyName
     * @param position
     * @param v
     * @return
     */
    boolean updateCellDataListTxtValue(JSONObject query, String keyName, Integer position, Object v);

    /**
     * 更新jsonb中某条文本数据
     * @param query    键值对
     * @param keyName
     * @param position
     * @param v
     * @return
     */
    boolean updateCellDataListValue(JSONObject query, String keyName, String position, Object v);

    /**
     * jsonb数据中元素添加元素
     * @param query
     * @param word
     * @param db
     * @param position
     * @return
     */
    boolean updateJsonbForElementInsert(JSONObject query, String word, JSONObject db, Integer position);

    /**
     * 更新
     * @param query
     * @param word
     * @return
     */
    boolean rmJsonbDataForEmpty(JSONObject query, String word);

    /**
     * 更新
     * @param query
     * @param word
     * @return
     */
    boolean updateJsonbDataForKeys(JSONObject query, JSONObject word);

    /**
     * 更新status状态
     * @param model
     * @return
     */
    boolean updateDataStatus(GridRecordDataModel model);

    /**
     * 更新sheet隐藏状态
     * @param model
     * @param hide
     * @param index1
     * @param index2
     * @return
     */
    boolean updateDataMsgHide(GridRecordDataModel model, Integer hide, String index1, String index2);

    /**
     * 更新sheet隐藏状态
     * @param model
     * @param hide
     * @param index
     * @return
     */
    boolean updateDataMsgNoHide(GridRecordDataModel model, Integer hide, String index);

    /**
     * 更新jsonb中某条文本数据
     * @param block_ids
     * @param models
     * @return
     */
    boolean batchUpdateCellDataValue(List<String> block_ids, List<GridRecordDataModel> models);

    /**
     * jsonb数据中元素添加元素（集合插入）
     * @param query
     * @param word
     * @param db
     * @param position
     * @param words
     * @return
     */
    boolean updateJsonbForInsertNull(JSONObject query, String word, JSONObject db, Integer position, String words);

    /**
     * jsonb数据中元素添加元素
     * @param query
     * @param word
     * @param db
     * @param position
     * @return
     */
    boolean updateJsonbForSetNull(JSONObject query, String word, JSONObject db, Integer position);


    /**
     * jsonb数据中元素添加元素(根节点)
     * @param query
     * @param word
     * @param db
     * @param position
     * @param words
     * @return
     */
    boolean updateJsonbForSetRootNull(JSONObject query, String word, JSONObject db, Integer position, String words);


}
