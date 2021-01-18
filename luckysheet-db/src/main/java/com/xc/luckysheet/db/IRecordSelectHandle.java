package com.xc.luckysheet.db;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 记录处理
 * @author Administrator
 */
public interface IRecordSelectHandle {

    /**
     * 查看指定sheet页 查看第一块是否存在（控制块）
     * @param listId
     * @param index
     * @return
     */
     Integer getFirstBlockByGridKey(String listId, String index);

    /**
     * 获取指定的xls激活的sheet页的 返回index（控制块）
     * @param listId
     * @return
     */
     String getFirstBlockIndexByGridKey(String listId);

    /**
     * 获取指定的xls，sheet 第一块的行列信息（控制块）
     * @param listId
     * @param index
     * @return
     */
     String getFirstBlockRowColByGridKey(String listId,String index);

    /**
     * 按指定xls，sheet顺序返回整个xls结构
     * 不返回celldata ,只获取信息块
     * @param listId
     * @return
     */
     List<JSONObject> getByGridKey_NOCelldata(String listId);

    /**
     * 按指定xls，sheet获取，返回指定的sheet集合
     * @param listId
     * @param index
     * @return
     */
     List<JSONObject> getBlockAllByGridKey(String listId, String index);

    /**
     * 获取指定xls，sheet，block的数据
     * @param listId
     * @param index
     * @param blockId
     * @return
     */
     JSONObject getCelldataByGridKey(String listId, String index, String blockId);

    /**
     * 获取指定xls、sheet中的config中数据 （存放在第一块中）
     * @param listId
     * @param index
     * @return
     */
     JSONObject getConfigByGridKey(String listId, String index);

    /**
     * 按list_id获取，返回指定sheet 当前sheet的全部分块数据（并合并）getMergeByGridKey
     * 返回是DBObject，而下面这个方法返回仅仅只有celldata
     * @param listId
     * @param index
     * @param ids 返回记录存在数据库的ID
     * @return
     */
     JSONObject getBlockMergeByGridKey(String listId, String index, List<String> ids);

    /**
     * 按list_id获取（id,index），返回sheet集合
     *
     * @param listId
     * @param flag 是否仅仅获取主要模块
     * @return
     */
     List<JSONObject> getBlocksByGridKey(String listId, boolean flag);

    /**
     *  获取指定xls，多个sheet的全部分块
     * @param listId
     * @param indexs
     * @return
     */
     List<JSONObject> getAllIndexsByGridKey(String listId, List<String> indexs);

    /**
     * 获取指定xls,sheet全部内容
     * @param listId
     * @param index
     * @return
     */
     List<JSONObject> getIndexsByGridKey(String listId, String index);

    /**
     * 获取图表数据（第一块）
     * @param listId
     * @param index
     * @return
     */
     JSONObject getChartByGridKey(String listId, String index);



}