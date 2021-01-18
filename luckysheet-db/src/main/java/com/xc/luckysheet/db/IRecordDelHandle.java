package com.xc.luckysheet.db;

import com.xc.luckysheet.entity.GridRecordDataModel;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 删除
 * @author Administrator
 */
public interface IRecordDelHandle {
    /**
     * 删除sheet（非物理删除）
     * @param model
     * @return
     */
    boolean updateDataForReDel(GridRecordDataModel model);

    /**
     * 按ID 删除多个文档 （物理删除）
     * @param ids
     * @return
     */
    String delDocuments(List<String> ids);

    /**
     * 按list_id 删除记录 （物理删除）
     * @param listIds
     * @return
     */
    int[] delete(List<String> listIds);




}
