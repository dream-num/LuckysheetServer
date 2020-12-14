package com.xc.luckysheet.entity;


import lombok.Data;


/**
 * Created with IntelliJ IDEA.
 * User: 1
 * Date: 17-12-15
 * Time: 上午11:34
 * To change this template use File | Settings | File Templates.
 * @author cr
 */
@Data
public class LuckySheetGridModel implements BaseModel{
    private String list_id;

    /**
     * 表格名称
     */
    private String grid_name;
    /**
     *  '缩略图'
     */
    private byte[] grid_thumb;

}
