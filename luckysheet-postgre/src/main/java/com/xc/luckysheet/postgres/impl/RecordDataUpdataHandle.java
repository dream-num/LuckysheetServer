package com.xc.luckysheet.postgres.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.xc.luckysheet.JfGridConfigModel;
import com.xc.luckysheet.db.IRecordDataInsertHandle;
import com.xc.luckysheet.db.IRecordDataUpdataHandle;
import com.xc.luckysheet.db.IRecordDelHandle;
import com.xc.luckysheet.entity.GridRecordDataModel;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 更新
 * @author Administrator
 */
@Slf4j
@Repository(value ="postgresRecordDataUpdataHandle" )
public class RecordDataUpdataHandle extends BaseHandle implements IRecordDataUpdataHandle {

    @Resource
    @Qualifier("postgresRecordDataInsertHandle")
    private IRecordDataInsertHandle RecordDataInsertHandle;

    @Resource
    @Qualifier("postgresRecordDelHandle")
    private IRecordDelHandle recordDelHandle;

    /**
     * sheet多块更新（先删除后添加）
     * 按IDS删除一组，然后新加处理后的
     * @param blocks
     * @param ids
     * @return
     */
    @Transactional(value = "postgresTxManager",rollbackFor = Exception.class)
    @Override
    public Boolean updateMulti2(List<JSONObject> blocks, List<String> ids) {
        try{
            if(ids!=null && ids.size()>0){
                recordDelHandle.delDocuments(ids);
            }
            String _mongodbKey = RecordDataInsertHandle.InsertBatchDb(blocks);
            if (_mongodbKey == null) {
                throw new RuntimeException("插入报错");
            }
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 批量更新order 按listId，index，首块
     *
     * @param models
     * @return
     */
    @Override
    public boolean batchUpdateForNoJsonbData(List<GridRecordDataModel> models) {
        try{
            String sql="update "+JfGridConfigModel.TABLENAME+" set \"order\"=?  where  list_id=? and index=? and block_id=?";
            log.info("batchUpdateForNoJsonbData:"+sql);
            jdbcTemplate_postgresql.batchUpdate(sql, new BatchPreparedStatementSetter() {
                public int getBatchSize() {
                    return models.size();
                    //这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
                }
                public void setValues(PreparedStatement ps, int i)throws SQLException {
                    GridRecordDataModel linkset =  models.get(i);
                    ps.setInt(1, linkset.getOrder());
                    ps.setString(2, linkset.getList_id());
                    ps.setString(3, linkset.getIndex());
                    ps.setString(4, JfGridConfigModel.FirstBlockID);
                }
            });
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
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
        String condition="";
        try{
            log.info("select:"+query.toString(SerializerFeature.WriteMapNullValue));
            Map<String,Object> queryDB=query.getInnerMap();
            for (String key : queryDB.keySet()) {
                condition=condition+"and "+key+"='"+queryDB.get(key)+"'";
            }
            String updateSql="update "+JfGridConfigModel.TABLENAME+" set json_data=json_data #- '{"+keyName+"}'  where 1=1 " +condition;
            log.info("rmCellDataValue--"+updateSql);
            jdbcTemplate_postgresql.update(updateSql);
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
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
    public boolean updateCellDataListTxtValue(JSONObject query, String keyName, Integer position, Object v) {
        String condition="";
        try{
            log.info("select:"+query.toString(SerializerFeature.WriteMapNullValue));
            Map<String,Object> queryDB=query.getInnerMap();
            for (String key : queryDB.keySet()) {
                condition=condition+"and "+key+"='"+queryDB.get(key)+"'";
            }
            if(position!=null){
                keyName=keyName+","+position;
            }
            StringBuffer updateSql=new StringBuffer();
            updateSql.append("update "+JfGridConfigModel.TABLENAME+" set json_data=jsonb_set(json_data,'{"+keyName);
            updateSql.append("}'::text[],'"+v+"',true) where 1=1 "+condition);

            jdbcTemplate_postgresql.update(updateSql.toString());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
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
        String condition="";
        try{
            log.info("select:"+query.toString(SerializerFeature.WriteMapNullValue));
            Map<String,Object> queryDB=query.getInnerMap();
            for (String key : queryDB.keySet()) {
                condition=condition+"and "+key+"='"+queryDB.get(key)+"'";
            }
            if(position!=null){
                keyName=keyName+","+position;
            }
            StringBuffer updateSql=new StringBuffer();
            updateSql.append("update "+JfGridConfigModel.TABLENAME+" set json_data=jsonb_set(json_data,'{"+keyName);
            updateSql.append("}','"+v+"',true) where 1=1 "+condition);
            log.info("updateSql:"+updateSql);
            jdbcTemplate_postgresql.update(updateSql.toString());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
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
        String condition="";
        try{
            log.info("select:"+query.toString(SerializerFeature.WriteMapNullValue));
            Map<String,Object> queryDB=query.getInnerMap();
            if(position!=null){
                word=word+","+position;
            }
            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            //(jsonb_v,'{myinfo,celldata,0}','{"c":1,"r":1,"v":{"con":"str"}}',false)
            String updateSql="update "+JfGridConfigModel.TABLENAME+" set json_data=jsonb_insert(json_data,'{"+word+"}','"+db.toString()+"',false) where 1=1 " +condition;
            log.info("updateSql:"+updateSql);
            jdbcTemplate_postgresql.update(updateSql,arr.toArray());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }

    /**
     * 更新
     *
     * @param query
     * @param word
     * @return
     */
    @Override
    public boolean rmJsonbDataForEmpty(JSONObject query, String word) {
        String condition="";
        try{
            log.info("select:"+query.toString(SerializerFeature.WriteMapNullValue));
            Map<String,Object> queryDB=query.getInnerMap();
            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            String sql="update "+JfGridConfigModel.TABLENAME+" set json_data=json_data||'{"+word+"}'::jsonb where 1=1 "+condition;
            log.info("updateSql:"+sql);
            jdbcTemplate_postgresql.update(sql,arr.toArray());
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 更新
     *
     * @param query
     * @param word
     * @return
     */
    @Override
    public boolean updateJsonbDataForKeys(JSONObject query, JSONObject word) {
        String condition="";
        try{
            log.info("select:"+query.toString(SerializerFeature.WriteMapNullValue));
            Map<String,Object> queryDB=query.getInnerMap();

            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            String sql="update "+JfGridConfigModel.TABLENAME+" set json_data=json_data||'"+word.toString()+"'::jsonb where 1=1 "+condition;
            log.info("updateSql:"+sql);
            jdbcTemplate_postgresql.update(sql,arr.toArray());
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 更新status状态
     *
     * @param model
     * @return
     */
    @Transactional(value = "postgresTxManager",rollbackFor = Exception.class)
    @Override
    public boolean updateDataStatus(GridRecordDataModel model) {
        try{
            String sql1="update "+JfGridConfigModel.TABLENAME+" set status=0  where  list_id=? and status=1 and block_id=?";
            log.info("updateSql1:"+sql1);
            jdbcTemplate_postgresql.update(sql1,new Object[]{model.getList_id(),model.getBlock_id()});

            String sql2="update "+JfGridConfigModel.TABLENAME+" set status=1  where  list_id=? and index=? and block_id=?";
            log.info("updateSql2:"+sql2);
            jdbcTemplate_postgresql.update(sql2,new Object[]{model.getList_id(),model.getIndex(),model.getBlock_id()});
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
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
    @Transactional(value = "postgresTxManager",rollbackFor = Exception.class)
    @Override
    public boolean updateDataMsgHide(GridRecordDataModel model, Integer hide, String index1, String index2) {
        try{
            String sql1="update "+JfGridConfigModel.TABLENAME+" set status=0 ,json_data=jsonb_set(json_data,'{hide}'::text[],'"+hide+"',true) where  list_id='"+model.getList_id()+"' and index='"+index1+"' and block_id='fblock'";
            log.info("updateSql1:"+sql1);
            jdbcTemplate_postgresql.update(sql1);
            String sql2="update "+JfGridConfigModel.TABLENAME+" set status=1  where  list_id=? and index=? and block_id=?";
            log.info("updateSql2:"+sql2);
            jdbcTemplate_postgresql.update(sql2,new Object[]{model.getList_id(),index2,model.getBlock_id()});
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 更新sheet隐藏状态
     *
     * @param model
     * @param hide
     * @param index
     * @return
     */
    @Transactional(value = "postgresTxManager",rollbackFor = Exception.class)
    @Override
    public boolean updateDataMsgNoHide(GridRecordDataModel model, Integer hide, String index) {
        try{
            String sql1="update "+JfGridConfigModel.TABLENAME+" set status=0  where  list_id=?  and block_id=?";
            log.info("updateSql1:"+sql1);
            jdbcTemplate_postgresql.update(sql1,new Object[]{model.getList_id(),model.getBlock_id()});

            String sql2="update "+JfGridConfigModel.TABLENAME+" set status=1 ,json_data=jsonb_set(json_data,'{hide}'::text[],'"+hide+"',true) where  list_id='"+model.getList_id()+"' and index='"+index+"' and block_id='"+model.getBlock_id()+"'";
            log.info("updateSql2:"+sql2);
            jdbcTemplate_postgresql.update(sql2);
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 更新jsonb中某条文本数据
     *
     * @param block_ids
     * @param models
     * @return
     */
    @Override
    public boolean batchUpdateCellDataValue(List<String> block_ids, List<GridRecordDataModel> models) {
        try{
            String id="";
            for (String str : block_ids) {
                id=id+"'"+str+"',";
            }
            id=id.substring(0, id.length()-1);
            log.info("id:"+id);
            String delsql="DELETE from "+JfGridConfigModel.TABLENAME+" where list_id=? and block_id in ("+id+") and index=? ";
            jdbcTemplate_postgresql.update(delsql,new Object[]{models.get(0).getList_id(),models.get(0).getIndex()});
            String sql = "insert into "+JfGridConfigModel.TABLENAME+" (id,block_id,index,list_id,status,\"order\",json_data,is_delete) values " +
                    " (nextval('luckysheet_id_seq'),?,?,?,?,?,?,0)";
            List<Object[]>batch=new ArrayList<Object[]>();
            for(GridRecordDataModel b : models){
                List<Object> objectList=new ArrayList<Object>();
                objectList.add(b.getBlock_id().trim());
                objectList.add(b.getIndex());
                objectList.add(b.getList_id());
                objectList.add(b.getStatus());
                objectList.add(b.getOrder());
                PGobject pg=new PGobject();
                pg.setType("json");
                try {
                    pg.setValue(b.getJson_data().toString(SerializerFeature.WriteMapNullValue));
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }
                objectList.add(pg);

                Object[] params=(Object[])objectList.toArray(new Object[objectList.size()]);
                batch.add(params);
            }
            log.info("sqls:{}",sql);
            jdbcTemplate_postgresql.batchUpdate(sql,batch);
            log.info("batchUpdateCellDataValue--end");
            return true;


        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }

    /**
     * jsonb数据中元素添加元素（集合插入）
     *
     * @param query
     * @param word
     * @param db
     * @param position
     * @param words
     * @return
     */
    @Transactional(value = "postgresTxManager",rollbackFor = Exception.class)
    @Override
    public boolean updateJsonbForInsertNull(JSONObject query, String word, JSONObject db, Integer position, String words) {
        String condition="";
        try{
            log.info("select:"+query.toString(SerializerFeature.WriteMapNullValue));
            Map<String,Object> queryDB=query.getInnerMap();
            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            String createSql="update "+JfGridConfigModel.TABLENAME+" set json_data=json_data||'{"+words+"}'::jsonb where 1=1 " +condition;
            log.info("createSql:{}",createSql);
            jdbcTemplate_postgresql.update(createSql,arr.toArray());
            if(position!=null){
                word=word+","+position;
            }
            //(jsonb_v,'{myinfo,celldata,0}','{"c":1,"r":1,"v":{"con":"str"}}',false)
            String updateSql="update "+JfGridConfigModel.TABLENAME+" set json_data=jsonb_insert(json_data,'{"+word+"}','"+db.toString()+"',false) where 1=1 " +condition;
            log.info("updateSql:{}",updateSql);
            jdbcTemplate_postgresql.update(updateSql,arr.toArray());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
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
    @Transactional(value = "postgresTxManager",rollbackFor = Exception.class)
    @Override
    public boolean updateJsonbForSetNull(JSONObject query, String word, JSONObject db, Integer position) {
        String condition="";
        try{
            log.info("select:"+query.toString(SerializerFeature.WriteMapNullValue));
            Map<String,Object> queryDB=query.getInnerMap();
            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            log.info("arr:"+arr);
            String createSql="update "+JfGridConfigModel.TABLENAME+" set json_data=jsonb_set(json_data,'{"+word+"}'::text[],'[]',true)  where 1=1 " +condition;
            log.info("createSql:"+createSql);
            jdbcTemplate_postgresql.update(createSql,arr.toArray());
            if(position!=null){
                word=word+","+position;
            }
            //(jsonb_v,'{myinfo,celldata,0}','{"c":1,"r":1,"v":{"con":"str"}}',false)
            String updateSql="update "+JfGridConfigModel.TABLENAME+" set json_data=jsonb_set(json_data,'{"+word+"}'::text[],'"+db.toString()+"',true) where 1=1 " +condition;
            log.info("updateSql:"+updateSql);
            jdbcTemplate_postgresql.update(updateSql,arr.toArray());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * jsonb数据中元素添加元素(根节点)
     *
     * @param query
     * @param word
     * @param db
     * @param position
     * @param words
     * @return
     */
    @Transactional(value = "postgresTxManager",rollbackFor = Exception.class)
    @Override
    public boolean updateJsonbForSetRootNull(JSONObject query, String word, JSONObject db, Integer position, String words) {
        String condition="";
        try{
            log.info("select:{}",query.toString(SerializerFeature.WriteMapNullValue));
            Map<String,Object> queryDB=query.getInnerMap();
            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            String createSql="update "+JfGridConfigModel.TABLENAME+" set json_data=json_data||'{"+words+"}'::jsonb where 1=1 " +condition;
            log.info("createSql:"+createSql);
            jdbcTemplate_postgresql.update(createSql,arr.toArray());
            if(position!=null){
                word=word+","+position;
            }
            //(jsonb_v,'{myinfo,celldata,0}','{"c":1,"r":1,"v":{"con":"str"}}',false)
            String updateSql="update "+JfGridConfigModel.TABLENAME+" set json_data=jsonb_set(json_data,'{"+word+"}','"+db.toString()+"',false) where 1=1 " +condition;
            log.info("updateSql:"+updateSql);
            jdbcTemplate_postgresql.update(updateSql,arr.toArray());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }
}
