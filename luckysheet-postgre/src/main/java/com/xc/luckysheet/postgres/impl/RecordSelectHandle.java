package com.xc.luckysheet.postgres.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xc.luckysheet.JfGridConfigModel;
import com.xc.luckysheet.db.IRecordSelectHandle;
import com.xc.luckysheet.util.JfGridFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 查询
 * @author Administrator
 */
@Slf4j
@Repository(value = "postgresRecordSelectHandle")
public class RecordSelectHandle extends BaseHandle implements IRecordSelectHandle {
    /**
     * 查看指定sheet页 第一块是否存在（控制块）
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public Integer getFirstBlockByGridKey(String listId, String index) {
        //默认获取第一块
        String sql="select count(1) from "+JfGridConfigModel.TABLENAME +" p where p.list_id=? and p.index=? and p.block_id=? and p.is_delete=0";
        try{
            return  jdbcTemplate_postgresql.queryForObject(sql, new Object[]{listId,index,JfGridConfigModel.FirstBlockID},Integer.class);
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定的xls激活的sheet页的 返回index（控制块）
     *
     * @param listId
     * @return
     */
    @Override
    public String getFirstBlockIndexByGridKey(String listId) {
        //默认获取第一块
        String sql="select p.index from "+JfGridConfigModel.TABLENAME+" p where p.list_id=? and p.block_id=? and p.status=1 and p.is_delete=0 ";
        try{
            return  jdbcTemplate_postgresql.queryForObject(sql, new Object[]{listId,JfGridConfigModel.FirstBlockID},String.class);
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定的xls，sheet 第一块的行列信息（控制块）
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public String getFirstBlockRowColByGridKey(String listId,String index) {
        //默认获取第一块
        String sql="select p.row_col from "+JfGridConfigModel.TABLENAME+" p where p.list_id=? and p.index=? and p.block_id=? and p.is_delete=0";
        try{
            return  jdbcTemplate_postgresql.queryForObject(sql, new Object[]{listId,index,JfGridConfigModel.FirstBlockID},String.class);
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    /**
     * 按指定xls，sheet顺序返回整个xls结构
     * 不返回celldata ,只获取信息块
     *
     * @param listId
     * @return
     */
    @Override
    public List<JSONObject> getByGridKey_NOCelldata(String listId) {
        try{
            String sql="select id,block_id,index,list_id,status,json_data-'celldata' AS json_data,\"order\" from "+JfGridConfigModel.TABLENAME+" p where p.list_id=? and p.block_id=? and p.is_delete=0  order by p.order";
            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql, new Object[]{listId,JfGridConfigModel.FirstBlockID});
            List<JSONObject> result=new ArrayList<JSONObject>();

            for (Map<String, Object> map : list) {
                JSONObject pgd=null;
                try{
                    PGobject pg=(PGobject) map.get("json_data");
                    pgd=JSONObject.parseObject(pg.getValue(),JSONObject.class);
                }catch (Exception e) {
                    pgd=JSONObject.parseObject(map.get("json_data").toString(),JSONObject.class);
                }
                for (String key : map.keySet()) {
                    if("json_data".equals(key)){
                    }else{
                        pgd.put(key.toLowerCase(), map.get(key));
                    }
                }
                result.add(pgd);
            }
            return result;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 按指定xls，sheet获取，返回指定的sheet集合
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public List<JSONObject> getBlockAllByGridKey(String listId, String index) {
        try{
            String sql="select * from "+JfGridConfigModel.TABLENAME+" p where  p.list_id=? and p.index =? and p.is_delete=0 order by p.order asc";
            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql, new Object[]{listId,index});
            List<JSONObject> result=new ArrayList<JSONObject>(4);
            for (Map<String, Object> map : list) {
                result.add(getDBObjectFromMap(map));
            }
            return result;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定xls，sheet，block 的数据
     *
     * @param listId
     * @param index
     * @param blockId
     * @return
     */
    @Override
    public JSONObject getCelldataByGridKey(String listId, String index, String blockId) {
        try{
            String sql="select index,json_data->>'celldata' AS celldata,json_data->>'column' AS column,json_data->>'row' AS row from "+JfGridConfigModel.TABLENAME+" p where  p.list_id=? and p.index=? and p.block_id=? and p.is_delete=0 ORDER BY p.id DESC LIMIT 1 ";
            Map<String, Object> map=jdbcTemplate_postgresql.queryForMap(sql, new Object[]{listId,index,blockId});
            JSONObject db=new JSONObject();

            for (String key : map.keySet()) {
                if("celldata".equals(key)){
                    JSONObject pgd=null;
                    try{
                        PGobject pg=(PGobject) map.get(key);
                        pgd=JSONObject.parseObject(pg.getValue(),JSONObject.class);
                    }catch (Exception e) {
                        pgd=JSONObject.parseObject(map.get(key).toString(),JSONObject.class);
                    }
                    db.put(key.toLowerCase(), pgd);
                }else{
                    db.put(key.toLowerCase(), map.get(key));
                }
            }
            return db;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定xls、sheet中的config中数据 （存放在第一块中）
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public JSONObject getConfigByGridKey(String listId, String index) {
        try{
            String sql="select index,list_id,json_data->>'config' AS config,json_data->>'calcChain' AS calcChain,json_data->>'filter' AS filter from "+JfGridConfigModel.TABLENAME+" p where p.list_id=? and p.index=? and p.block_id=? and p.is_delete=0 ";
            Map<String, Object> map=jdbcTemplate_postgresql.queryForMap(sql, new Object[]{listId,index,JfGridConfigModel.FirstBlockID});
            JSONObject db=new JSONObject();

            for (String key : map.keySet()) {
                if("config".equals(key)|| "calcChain".equals(key)|| "filter".equals(key)){
                    JSONObject pgd=null;
                    try{
                        if(map.get(key)!=null){
                            PGobject pg=(PGobject) map.get(key);
                            pgd=JSONObject.parseObject(pg.getValue(),JSONObject.class);
                        }else{
                            pgd=JSONObject.parseObject("");
                        }
                    }catch (Exception e) {
                        pgd=JSONObject.parseObject(map.get(key).toString(),JSONObject.class);
                    }
                    db.put(key.toLowerCase(), pgd);
                }else{
                    db.put(key.toLowerCase(), map.get(key));
                }
            }
            return db;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 按list_id,index获取，返回指定sheet 当前sheet的全部分块数据（并合并）getMergeByGridKey
     * 返回是DBObject，而下面这个方法返回仅仅只有celldata
     *
     * @param listId
     * @param index
     * @param ids
     * @return
     */
    @Override
    public JSONObject getBlockMergeByGridKey(String listId, String index, List<String> ids) {
        JSONObject _fblock=new JSONObject();
        JSONArray _celldata=new JSONArray();
        //获取全部块
        List<JSONObject> blocks=getBlockAllByGridKey(listId, index);
        if(blocks!=null && blocks.size()>0){
            for(JSONObject _b:blocks){
                if(ids!=null){
                    if(_b.containsKey("id")){
                        ids.add(_b.get("id").toString());
                    }
                }
                if(_b.containsKey("block_id")){
                    if(JfGridConfigModel.FirstBlockID.equals(_b.get("block_id").toString().trim())){
                        //信息块
                        _fblock=_b;
                    }else{
                        //数据块
                        JSONObject db=JfGridFileUtil.getJSONObjectByIndex(_b, "json_data");
                        JSONArray _blockCellData=JfGridFileUtil.getSheetByIndex(db);
                        if(_blockCellData!=null){
                            _celldata.addAll(_blockCellData);
                        }
                    }
                }
            }
        }
        _fblock.put("celldata",_celldata);
        return _fblock;
    }

    /**
     * 按list_id获取（id,index），返回sheet集合
     *
     * @param listId
     * @param flag 是否仅仅获取主要模块
     * @return
     */
    @Override
    public List<JSONObject> getBlocksByGridKey(String listId, boolean flag) {
        try{
            List<Object> _param=new ArrayList<>(2);
            String sql="select id,index from "+JfGridConfigModel.TABLENAME+" p where  p.list_id=? ";
            _param.add(listId);
            if(flag){
                sql=sql+" and block_id=? ";
                _param.add(JfGridConfigModel.FirstBlockID);
            }
            sql=sql+" and p.is_delete=0 ";

            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql, Arrays.asList(_param));
            List<JSONObject> result=new ArrayList<JSONObject>();
            for (Map<String, Object> map : list) {
                result.add(getDBObjectFromMap(map));
            }
            return result;
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定xls，多个sheet的全部分块
     *
     * @param listId
     * @param indexs
     * @return
     */
    @Override
    public List<JSONObject> getAllIndexsByGridKey(String listId, List<String> indexs) {
        try{
            StringBuffer sql=new StringBuffer();
            sql.append("select * from "+JfGridConfigModel.TABLENAME+" p where  p.list_id=? and p.index in (");
            String mockInStatement="";
            int i=0;
            for (String type: indexs){
                if (i < indexs.size()-1){
                    mockInStatement = mockInStatement + "'"+type + "',";
                }
                else {
                    mockInStatement = mockInStatement + "'"+type+"'";
                }
                i++;
            }
            sql.append(mockInStatement);
            sql.append(") and p.is_delete=0 order by p.order asc");
            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql.toString(), new Object[]{listId});
            List<JSONObject> result=new ArrayList<JSONObject>();
            for (Map<String, Object> map : list) {
                result.add(getDBObjectFromMap(map));
            }
            return result;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定xls,sheet全部内容
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public List<JSONObject> getIndexsByGridKey(String listId, String index) {
        try{
            StringBuffer sql=new StringBuffer();
            sql.append("select * from "+JfGridConfigModel.TABLENAME+" p where  p.list_id=? and p.index =? and p.is_delete=0 order by p.id asc ");
            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql.toString(), new Object[]{listId,index});
            List<JSONObject> result=new ArrayList<JSONObject>();
            for (Map<String, Object> map : list) {
                result.add(getDBObjectFromMap(map));
            }
            return result;
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    /**
     * 获取图表数据（第一块）
     *
     * @param listId
     * @param index
     * @return
     */
    @Override
    public JSONObject getChartByGridKey(String listId, String index) {
        //默认获取第一块
        try{
            String sql="select index,list_id,json_data->>'chart' AS chart,block_id from "+JfGridConfigModel.TABLENAME+" p where p.list_id=? and p.index=? and p.block_id=? and p.is_delete=0 ";
            Map<String, Object> map=jdbcTemplate_postgresql.queryForMap(sql, new Object[]{listId,index,JfGridConfigModel.FirstBlockID});
            JSONObject db=new JSONObject();

            for (String key : map.keySet()) {
                if("chart".equals(key)){
                    JSONObject pgd=null;
                    if(map.get(key)!=null){
                        try{
                            PGobject pg=(PGobject) map.get(key);
                            pgd=JSONObject.parseObject(pg.getValue(),JSONObject.class);
                        }catch (Exception e) {
                            pgd=JSONObject.parseObject(map.get(key).toString(),JSONObject.class);
                        }
                        db.put(key.toLowerCase(), pgd);
                    }else{
                        db.put(key.toLowerCase(), null);
                    }

                }else{
                    db.put(key.toLowerCase(), map.get(key));
                }
            }
            return db;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }



    private JSONObject getDBObjectFromMap(Map<String, Object> map){
        JSONObject db=new JSONObject();

        for (String key : map.keySet()) {
            try{
                if("json_data".equals(key)){
                    JSONObject pgd=null;
                    try{
                        PGobject pg=(PGobject) map.get(key);
                        pgd=JSONObject.parseObject(pg.getValue(),JSONObject.class);
                    }catch (Exception e) {
                        pgd=JSONObject.parseObject(map.get(key).toString(),JSONObject.class);
                    }
                    db.put(key.toLowerCase(), pgd);
                }else{
                    db.put(key.toLowerCase(), map.get(key));
                }
            }catch (Exception e) {
                log.error(e.toString());
                continue;
            }
        }
        return db;
    }
}
