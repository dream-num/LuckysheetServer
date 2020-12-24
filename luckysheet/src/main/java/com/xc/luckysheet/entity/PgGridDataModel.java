package com.xc.luckysheet.entity;

import com.mongodb.DBObject;
import lombok.Data;

/**
 *
 * @author Administrator
 */
@Data
public class PgGridDataModel {
	Integer id;
    String list_id;
    //本记录的行_列
    String row_col;
    String index;

    Integer status;
    String block_id;
    DBObject json_data;
    Integer order;
    Integer is_delete;

}
