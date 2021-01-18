package com.xc.luckysheet.postgres.impl;

import com.alibaba.fastjson.JSONObject;
import com.xc.luckysheet.JfGridConfigModel;
import com.xc.luckysheet.db.IRecordDataInsertHandle;
import com.xc.luckysheet.entity.GridRecordDataModel;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 添加
 * @author Administrator
 */
@Slf4j
@Repository(value = "postgresRecordDataInsertHandle")
public class RecordDataInsertHandle extends BaseHandle implements IRecordDataInsertHandle{

    /**
     * 新增Sheet页,并返回刚刚插入的_id
     *
     * @param pgModel
     * @return
     */
    @Override
    public String insert(GridRecordDataModel pgModel) {
        JSONObject bson=pgModel.getJson_data();
        PGobject pg=new PGobject();
        pg.setType("json");
        try {
            pg.setValue(bson.toString());
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        String sql = "insert into "+JfGridConfigModel.TABLENAME +" (id,block_id,index,list_id,status,json_data,\"order\",is_delete) values " +
                " (nextval('luckysheet_id_seq'),?,?,?,?,?,?,0)";
        try{
            jdbcTemplate_postgresql.update(sql,pgModel.getBlock_id().trim(),pgModel.getIndex(),pgModel.getList_id(),pgModel.getStatus(),pg,pgModel.getOrder());
            return "";
        }catch (Exception e){
            log.warn(e.getMessage());
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
        String sql = "insert into "+JfGridConfigModel.TABLENAME +" (id,block_id,row_col,index,list_id,status,\"order\",json_data,is_delete) values " +
                " (nextval('luckysheet_id_seq'),?,?,?,?,?,?,?,0)";
        List<Object[]>batch=new ArrayList<Object[]>();
        for(GridRecordDataModel b : models){
            List<Object> objectList=new ArrayList<Object>();
            objectList.add(b.getBlock_id().trim());
            objectList.add(b.getRow_col());
            objectList.add(b.getIndex());
            objectList.add(b.getList_id());
            objectList.add(b.getStatus());
            objectList.add(b.getOrder());
            PGobject pg=new PGobject();
            pg.setType("json");
            try {
                pg.setValue(b.getJson_data().toString());
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
            objectList.add(pg);

            Object[] params=(Object[])objectList.toArray(new Object[objectList.size()]);
            batch.add(params);
        }
        try{
            log.info("InsertIntoBatch sql{}",sql);
            int[] i=jdbcTemplate_postgresql.batchUpdate(sql,batch);
            log.info("InsertIntoBatch count {}",i);
            return "";
        }catch (Exception ex){
            log.error(ex.toString());
            return null;
        }
    }

    /**
     * 批量添加 添加jsonb
     *
     * @param models
     * @return
     */
    @Override
    public String InsertBatchDb(List<JSONObject> models) {
        String sql = "insert into "+JfGridConfigModel.TABLENAME+" (id,block_id,row_col,index,list_id,status,\"order\",json_data,is_delete) values " +
                " (nextval('luckysheet_id_seq'),?,?,?,?,?,?,?,0)";
        List<Object[]>batch=new ArrayList<Object[]>();
        int order=0;
        for(JSONObject b : models){
            List<Object> objectList=new ArrayList<Object>();
            objectList.add(b.get("block_id").toString().trim());
            if(b.containsKey("row_col") && b.get("row_col")!=null){
                objectList.add(b.get("row_col"));
            }else{
                objectList.add(null);
            }
            objectList.add(b.get("index"));
            objectList.add(b.get("list_id"));
            if(b.containsKey("status") && b.get("status")!=null){
                objectList.add(b.get("status"));
            }else{
                objectList.add(0);
            }

            if(b.containsKey("order") && b.get("order")!=null){
                objectList.add(b.get("order"));
                order=Integer.valueOf(b.get("order").toString());
            }else{
                objectList.add(order);
            }

            PGobject pg=new PGobject();
            pg.setType("json");
            try {
                JSONObject db=new JSONObject();
                if(b.containsKey("json_data")){
                    db=(JSONObject) b.get("json_data");
                }else{
                    db.put("celldata", b.get("celldata"));
                }

                pg.setValue(db.toString());
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
            objectList.add(pg);

            Object[] params=(Object[])objectList.toArray(new Object[objectList.size()]);
            batch.add(params);
        }

        try{
            log.info("InsertBatchDb sql{}",sql);
            int[] i=jdbcTemplate_postgresql.batchUpdate(sql,batch);
            log.info("InsertBatchDb count {}",i);
            return "";
        }catch (Exception ex){
            log.error(ex.getMessage());
            return null;
        }
    }
}

