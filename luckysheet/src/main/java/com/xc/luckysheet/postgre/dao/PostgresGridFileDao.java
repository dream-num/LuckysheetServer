package com.xc.luckysheet.postgre.dao;

import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.xc.luckysheet.utils.JfGridFileUtil;
import org.springframework.data.mongodb.core.query.Query;
import com.xc.luckysheet.entity.JfGridConfigModel;
import com.xc.luckysheet.entity.PgGridDataModel;
import com.xc.luckysheet.utils.JSONParse;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.postgresql.util.PGobject;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.object.BatchSqlUpdate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * @author Administrator
 */
@Slf4j
@Repository
public class PostgresGridFileDao {

    /**
     * 表名
     */
    private static final String TableName="luckysheet";

    /**
     * 默认第一块的编号
     */
    private static String FirstBlockId="";

    static {
        try {
            //获取默认第一块的编号
            FirstBlockId=JfGridConfigModel.FirstBlockID;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Resource(name = "postgreJdbcTemplate")
    private JdbcTemplate jdbcTemplate_postgresql;

    /**
     * 新增数据,并返回刚刚插入的_id
     * @param pgModel
     * @return
     */
    public String insert(PgGridDataModel pgModel){
        DBObject bson=pgModel.getJson_data();
        PGobject pg=new PGobject();
        pg.setType("json");
        try {
            //pg.setValue("{\"key\":\"value\"}");
            pg.setValue(new Gson().toJson(bson));
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        String sql = "insert into "+TableName+" (id,block_id,index,list_id,status,json_data,\"order\",is_delete) values " +
                " (nextval('luckysheet_id_seq'),?,?,?,?,?,?,0)";
        try{
            jdbcTemplate_postgresql.update(sql,pgModel.getBlock_id().trim(),pgModel.getIndex(),pgModel.getList_id(),pgModel.getStatus(),pg,pgModel.getOrder());
            return "";
        }catch (Exception e){
            log.warn(e.getMessage());
        }
        return null;
    }
    public int addJsonb(Integer index,String list_id,boolean error){
        String model="{\"name\":\"Sheet1\",\"color\":\"\",\"index\":0,\"chart\":[],\"status\":1,\"order\":0,\"column\":60,\"row\":84,\"celldata\":[{\"c\":1,\"r\":1,\"v\":\"v1\"},{\"c\":1,\"r\":2,\"v\":\"v2\"},{\"c\":1,\"r\":3,\"v\":\"v3\"},{\"c\":1,\"r\":4,\"v\":\"v4\"}],\"visibledatarow\":[],\"visibledatacolumn\":[],\"rowsplit\":[],\"ch_width\":4748,\"rh_height\":1790,\"jfgird_select_save\":{},\"jfgrid_selection_range\":{},\"scrollLeft\":0,\"scrollTop\":0,\"config\":{}}";

        DBObject bson=(DBObject) JSONParse.parse(model);
        bson.put("block_id","block_id");
        String block_id="block_id";
        PGobject pg=new PGobject();
        pg.setType("json");
        try {
            pg.setValue(new Gson().toJson(bson));
        } catch (SQLException e) {
            log.error(e.getMessage());
        }


        String sql = "insert into "+TableName+" (id,block_id,index,list_id,status,json_data) values " +
                " (?,?,?,?,?,?)";
        try{
            long l=getMaxId()+1;
            if(error){
                l=l-1;
            }
            return jdbcTemplate_postgresql.update(sql,l,block_id.trim(),index,list_id,0,pg
            );
        }catch (Exception e){
            log.warn(e.getMessage());
        }
        return 0;
    }
    public int addJsonb2(String username,String password,String remark){
        HashMap<String,Object> _map=new HashMap<String,Object>();
        _map.put("number",1);
        _map.put("string","str");
        ArrayList<String> _list=new ArrayList<String>();
        _list.add("list1");
        _list.add("list2");
        _list.add("list3");
        _list.add("list4");
        _map.put("list",_list);


        PGobject pg=new PGobject();
        pg.setType("json");
        try {
            //pg.setValue("{\"key\":\"value\"}");
            pg.setValue(new Gson().toJson(_map));
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        String sql = "insert into "+TableName+" (id,block_id,index,list_id,status,json_data) values " +
                " (?,?,?,?,?,?)";
        try{
            long l=getMaxId()+1;
            return jdbcTemplate_postgresql.update(sql,l,username,password,l,remark,new Date(),pg
            );
        }catch (Exception e){
            log.warn(e.getMessage());
        }
        return 0;
    }

    private long getMaxId(){
        String sql="select max(id) from "+TableName;
        try{
            return jdbcTemplate_postgresql.queryForObject(sql,new Object[]{},Long.class);
        }catch (Exception e){
            log.warn(e.getMessage());
        }
        return 0l;
    }

    //查看第一块是否存在（控制块）
    public Integer getFirstBlockByGridKey(String gridKey,String index){
        //默认获取第一块
        String sql="select count(1) from "+TableName+" p where p.block_id='fblock' and p.list_id=? and p.index=? ";
        try{

            return  jdbcTemplate_postgresql.queryForObject(sql, new Object[]{gridKey,index},Integer.class);
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    //查看第一块是否存在（控制块）
    public String getFirstBlockIndexByGridKey(String gridKey){
        //默认获取第一块
        String sql="select p.index from "+TableName+" p where p.block_id='fblock' and p.list_id=? and p.status=1 ";
        try{

            return  jdbcTemplate_postgresql.queryForObject(sql, new Object[]{gridKey},String.class);
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }


    /**
     * 添加jsonb
     * 批量添加
     * @param models
     * @return
     */
    public String InsertIntoBatch(List<PgGridDataModel> models){
        String sql = "insert into "+TableName+" (id,block_id,index,list_id,status,\"order\",json_data,is_delete) values " +
                " (nextval('luckysheet_id_seq'),?,?,?,?,?,?,0)";
        List<Object[]>batch=new ArrayList<Object[]>();
        for(PgGridDataModel b : models){
            List<Object> objectList=new ArrayList<Object>();
            objectList.add(b.getBlock_id().trim());
            objectList.add(b.getIndex());
            objectList.add(b.getList_id());
            objectList.add(b.getStatus());
            objectList.add(b.getOrder());
            PGobject pg=new PGobject();
            pg.setType("json");
            try {
                pg.setValue(new Gson().toJson(b.getJson_data()));
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
            objectList.add(pg);

            Object[] params=(Object[])objectList.toArray(new Object[objectList.size()]);
            batch.add(params);
        }
        log.info("sqls"+sql);
        try{
            //long sttime = System.currentTimeMillis();
            jdbcTemplate_postgresql.batchUpdate(sql,batch);
            log.info("InsertIntoBatch--end");
            return "";
            //long endtime = System.currentTimeMillis();
            //System.out.println("保存:"+(endtime - sttime) / 1000.0 + "");
        }catch (Exception ex){
            log.error(ex.toString());
            return null;
        }
    }


    /**
     * 不返回celldata ,只获取信息块
     * @param gridKey
     * @return
     */
    public List<DBObject> getByGridKey_NOCelldata(String gridKey){
        try{
            String sql="select id,block_id,index,list_id,status,json_data-'celldata' AS json_data,\"order\" from "+TableName+" p where p.block_id='fblock' and p.list_id=? and p.is_delete=0";
            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql, new Object[]{gridKey});
            List<DBObject> result=new ArrayList<DBObject>();

            for (Map<String, Object> map : list) {
                DBObject pgd=null;
                try{
                    PGobject pg=(PGobject) map.get("json_data");
                    pgd= (DBObject) JSONParse.parse(pg.getValue());
                }catch (Exception e) {
                    pgd= (DBObject) JSONParse.parse(map.get("json_data").toString());
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


    //数据中插入数据
    public boolean updateOne(Query query,Query update){
        String condition="";
        try{
            log.info("select:"+query.getQueryObject().toString()+" \r\n update:"+update.getQueryObject().toString());
            DBObject queryDB=query.getQueryObject();
            DBObject updateDB=update.getQueryObject();
            for (String key : queryDB.keySet()) {
                condition=condition+"and "+key+"='"+queryDB.get(key)+"'";;
            }
            String updateResult="";
            for (String key : updateDB.keySet()) {
                updateResult=updateResult+"\""+key+"\""+":"+updateDB.get(key).toString()+",";
            }
            updateResult=updateResult.substring(0, updateResult.length()-1);
            String updateSql="update "+TableName+" set json_data=json_data||'{"+updateResult+"}' where 1=1 " +condition;
            log.info("updateSql:"+updateSql);
            jdbcTemplate_postgresql.update(updateSql);
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }

    //清除指定层级下某条数据
    public boolean rmCellDataValue(Query query,String keyName){
        String condition="";
        try{
            log.info("select:"+query.getQueryObject().toString());
            DBObject queryDB=query.getQueryObject();
            for (String key : queryDB.keySet()) {
                condition=condition+"and "+key+"='"+queryDB.get(key)+"'";
            }
            String updateSql="update "+TableName+" set json_data=json_data #- '{"+keyName+"}'  where 1=1 " +condition;
            log.info("rmCellDataValue--"+updateSql);
            jdbcTemplate_postgresql.update(updateSql);
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }

    //更新jsonb中某条文本数据
    public boolean updateCellDataListTxtValue(Query query,String keyName,Integer position,Object v){
        String condition="";
        try{
            log.info("select:"+query.getQueryObject().toString());
            DBObject queryDB=query.getQueryObject();
            for (String key : queryDB.keySet()) {
                condition=condition+"and "+key+"='"+queryDB.get(key)+"'";
            }
            if(position!=null){
                keyName=keyName+","+position;
            }
            StringBuffer updateSql=new StringBuffer();
            updateSql.append("update "+TableName+" set json_data=jsonb_set(json_data,'{"+keyName);
            updateSql.append("}'::text[],'"+v+"',true) where 1=1 "+condition);

            jdbcTemplate_postgresql.update(updateSql.toString());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }

    //更新jsonb中某条文本数据
    public boolean updateCellDataListValue(Query query,String keyName,String position,Object v){
        String condition="";
        try{
            log.info("select:"+query.getQueryObject().toString());
            DBObject queryDB=query.getQueryObject();
            for (String key : queryDB.keySet()) {
                condition=condition+"and "+key+"='"+queryDB.get(key)+"'";
            }
            if(position!=null){
                keyName=keyName+","+position;
            }
            StringBuffer updateSql=new StringBuffer();
            updateSql.append("update "+TableName+" set json_data=jsonb_set(json_data,'{"+keyName);
            updateSql.append("}','"+v+"',true) where 1=1 "+condition);
            log.info("updateSql:"+updateSql);
            jdbcTemplate_postgresql.update(updateSql.toString());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }

    //执行多个文档
    public Boolean updateMulti(List<Pair<Query,Query>> _updates){
        try{

            for (Pair<Query, Query> pair : _updates) {
                List paramters=new ArrayList<>();
                List paramter=new ArrayList<>();
                String condition="";
                DBObject db=null;
                Query query=pair.getFirst();
                Query update=pair.getSecond();
                log.info("select:"+query.getQueryObject().toString()+" \r\n update:"+update.getQueryObject().toString());
                paramter.add("");
                DBObject queryDB=query.getQueryObject();
                for (String key : queryDB.keySet()) {
                    paramters.add(queryDB.get(key));
                    paramter.add(queryDB.get(key));
                    condition=condition+" and "+key+"=? ";
                }
                String sql="select json_data from "+TableName+" p where 1=1"+condition;
                Map<String, Object> map=jdbcTemplate_postgresql.queryForMap(sql, paramters.toArray());
                db=(DBObject) JSONParse.parse(map.get("json_data").toString());
                if(db!=null && condition!=null){
                    db.putAll(update.getQueryObject());
                }
                paramter.add(0, db);
                String updateSql="update "+TableName+" set json_data=? where 1=1 " +condition;
                jdbcTemplate_postgresql.update(updateSql,paramter.toArray());
            }
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return false;
    }

    //按list_id获取，返回sheet集合
    public List<DBObject> getBlockAllByGridKey(String list_id,String index){
        try{
            String sql="select * from "+TableName+" p where  p.list_id=? and p.index =? order by p.order asc";
            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql, new Object[]{list_id,index});
            List<DBObject> result=new ArrayList<DBObject>();
            for (Map<String, Object> map : list) {
                result.add(getDBObjectFromMap(map));
            }
            return result;
            //return mongoTemplate.find(query, DBObject.class, NEW_COLLECTION_NAME);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    //按list_id获取，返回sheet集合
    public List<PgGridDataModel> getByGridKey(String list_id){
        try{
            String sql="select * from "+TableName+" p where p.block_id='fblock' and p.list_id=? ";
            List<PgGridDataModel> list=jdbcTemplate_postgresql.query(sql, new Object[]{list_id},modelRowMapper);
            return list;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    RowMapper<PgGridDataModel> modelRowMapper = new RowMapper()
    {
        public PgGridDataModel mapRow(ResultSet rs, int rowNum)
                throws SQLException
        {
            PgGridDataModel model = new PgGridDataModel();

            model.setBlock_id(rs.getString("block_id"));
            model.setId(rs.getInt("id"));
            model.setIndex(rs.getString("index"));
            model.setJson_data((DBObject)JSONParse.parse(rs.getString("json_data")));
            model.setList_id(rs.getString("list_id"));
            model.setStatus(rs.getInt("status"));
            model.setOrder(rs.getInt("order"));
            return model;
        }
    };

    public DBObject getCelldataByGridKey(String gridKey,String index,String block_id){
        try{

            String sql="select index,json_data->>'celldata' AS celldata,json_data->>'column' AS column,json_data->>'row' AS row from "+TableName+" p where  p.block_id='"+block_id+"' and p.list_id=? and p.index=? ORDER BY p.id DESC LIMIT 1 ";
            Map<String, Object> map=jdbcTemplate_postgresql.queryForMap(sql, new Object[]{gridKey,index});
            DBObject db=new BasicDBObject();

            for (String key : map.keySet()) {
                if("celldata".equals(key)){
                    DBObject pgd=null;
                    try{
                        PGobject pg=(PGobject) map.get(key);
                        pgd=(DBObject) JSONParse.parse(pg.getValue());
                    }catch (Exception e) {
                        pgd=(DBObject) JSONParse.parse(map.get(key).toString());
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

    public List<DBObject> getBlocksByGridKey(String gridKey,String index){
        try{

            String sql="select list_id,block_id,index,json_data->>'celldata' AS celldata,json_data->>'column' AS column,json_data->>'row' AS row from "+TableName+" p where p.list_id=? and p.index=?";
            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql, new Object[]{gridKey,index});
            List<DBObject> result=new ArrayList<DBObject>();

            for (Map<String, Object> map : list) {
                DBObject db=new BasicDBObject();
                for (String key : map.keySet()) {
                    if("celldata".equals(key)){
                        DBObject pgd=null;
                        try{
                            PGobject pg=(PGobject) map.get(key);
                            pgd=(DBObject) JSONParse.parse(pg.getValue());
                        }catch (Exception e) {
                            pgd=(DBObject) JSONParse.parse(map.get(key).toString());
                        }
                        db.put(key.toLowerCase(), pgd);
                    }else{
                        db.put(key.toLowerCase(), map.get(key));
                    }
                }
                result.add(db);
            }
            return result;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    public DBObject getCelldataByGridKey(String gridKey){
        try{

            String sql="select index,json_data->>'celldata' AS celldata,json_data->>'column' AS column,json_data->>'row' AS row from "+TableName+" p where   p.id =? ";
            Map<String, Object> map=jdbcTemplate_postgresql.queryForMap(sql, new Object[]{gridKey});
            DBObject db=new BasicDBObject();

            for (String key : map.keySet()) {
                if("celldata".equals(key)){
                    DBObject pgd=null;
                    try{
                        PGobject pg=(PGobject) map.get(key);
                        pgd=(DBObject) JSONParse.parse(pg.getValue());
                    }catch (Exception e) {
                        pgd=(DBObject) JSONParse.parse(map.get(key).toString());
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


    //删除文档
    public String delDocument(String id){
        try{
            String delsql="DELETE from "+TableName+" where id=? ";
            jdbcTemplate_postgresql.update(delsql,new Object[]{id});
            return "";
        }catch (Exception ex){
            log.error(ex.toString());
            return ex.toString();
        }
    }


    //删除多个文档
    public String delDocuments(List<String> ids){
        try{
            String id="";
            for (String str : ids) {
                id=id+str+",";
            }
            id=id.substring(0, id.length()-1);
            String delsql="DELETE from "+TableName+" where id in ("+id+")";
            jdbcTemplate_postgresql.update(delsql);
            return "";
        }catch (Exception ex){
            log.error(ex.toString());
            return ex.toString();
        }
    }

    //删除多个文档
    public String delDocumentsByDel(){
        try{
            String delsql="DELETE from "+TableName+" where is_delete=1";
            jdbcTemplate_postgresql.update(delsql);
            return "";
        }catch (Exception ex){
            log.error(ex.toString());
            return ex.toString();
        }
    }
    public String delByGridKeys(List<String> ids) {
        String delsql="DELETE from "+TableName+" where list_id in (?)";
        jdbcTemplate_postgresql.update(delsql,ids.toArray());
        return "";
    }


    //按gridKey获取，返回sheet集合
    public DBObject getByPgId(ObjectId gridKey){
        try{
            String sql="select * from "+TableName+" p where and p.id=? ";
            Map<String, Object> map=jdbcTemplate_postgresql.queryForMap(sql, new Object[]{gridKey});
            return getDBObjectFromMap(map);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    //查看第一块是否存在（控制块）
    public String getKeyByGridKeyAndIndex(String gridKey,String index,String block_id){
        String sql="select id from "+TableName+" p where p.list_id=? and p.index=? and p.block_id=?";
        try{

            return  jdbcTemplate_postgresql.queryForObject(sql, new Object[]{gridKey,index,block_id},String.class);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    //jsonb数据中元素添加元素
    public boolean updateJsonbForElementInsert(Query query,String word,DBObject db,Integer position){
        String condition="";
        try{
            log.info("select:"+query.getQueryObject().toString());
            DBObject queryDB=query.getQueryObject();
            if(position!=null){
                word=word+","+position;
            }
            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            //(jsonb_v,'{myinfo,celldata,0}','{"c":1,"r":1,"v":{"con":"str"}}',false)
            String updateSql="update "+TableName+" set json_data=jsonb_insert(json_data,'{"+word+"}','"+db.toString()+"',false) where 1=1 " +condition;
            log.info("updateSql:"+updateSql);
            jdbcTemplate_postgresql.update(updateSql,arr.toArray());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }


    public DBObject getConfigByGridKey(String gridKey,String index){
        //默认获取第一块
        try{
            String sql="select index,list_id,json_data->>'config' AS config,json_data->>'calcChain' AS calcChain,json_data->>'filter' AS filter from "+TableName+" p where p.list_id=? and p.index=? and p.block_id=? ";
            Map<String, Object> map=jdbcTemplate_postgresql.queryForMap(sql, new Object[]{gridKey,index,FirstBlockId});
            DBObject db=new BasicDBObject();

            for (String key : map.keySet()) {
                if("config".equals(key)|| "calcChain".equals(key)|| "filter".equals(key)){
                    DBObject pgd=null;
                    try{
                        if(map.get(key)!=null){
                            PGobject pg=(PGobject) map.get(key);
                            pgd=(DBObject) JSONParse.parse(pg.getValue());
                        }else{
                            pgd=(DBObject) JSONParse.parse("");
                        }
                    }catch (Exception e) {
                        pgd=(DBObject) JSONParse.parse(map.get(key).toString());
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


    public boolean  rmJsonbDataForEmpty(Query query,String word){
        String condition="";
        try{
            log.info("select:"+query.getQueryObject().toString());
            DBObject queryDB=query.getQueryObject();

            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            String sql="update "+TableName+" set json_data=json_data||'{"+word+"}'::jsonb where 1=1 "+condition;
            log.info("updateSql:"+sql);
            jdbcTemplate_postgresql.update(sql,arr.toArray());
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    public boolean  updateJsonbDataForKeys(Query query,DBObject word){
        String condition="";
        try{
            log.info("select:"+query.getQueryObject().toString());
            DBObject queryDB=query.getQueryObject();

            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            String sql="update "+TableName+" set json_data=json_data||'"+word.toString()+"'::jsonb where 1=1 "+condition;
            log.info("updateSql:"+sql);
            jdbcTemplate_postgresql.update(sql,arr.toArray());
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    //按list_id获取，返回指定sheet 当前sheet的全部分块数据（并合并）
    //返回是DBObject，而下面这个方法返回仅仅只有celldata
    public DBObject getBlockMergeByGridKey(String gridKey,String index,List<String> mongodbKeys){
        DBObject _fblock=new BasicDBObject();
        BasicDBList _celldata=new BasicDBList();
        //获取全部块
        List<DBObject> blocks=getBlockAllByGridKey(gridKey, index);
        if(blocks!=null && blocks.size()>0){
            for(DBObject _b:blocks){
                if(mongodbKeys!=null){
                    if(_b.containsField("id")){
                        mongodbKeys.add(_b.get("id").toString());
                    }
                }
                if(_b.containsField("block_id")){
                    if(JfGridConfigModel.FirstBlockID.equals(_b.get("block_id").toString().trim())){
                        //信息块
                        _fblock=_b;
                    }else{
                        //数据块
                        DBObject db=JfGridFileUtil.getObjectByIndex(_b, "json_data");
                        BasicDBList _blockCellData=JfGridFileUtil.getSheetByIndex(db);
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

    //sheet多块更新（先删除后添加）
    @Transactional(value = "postgreTxManager",rollbackFor = Exception.class)
    public Boolean updateMulti2(List<DBObject> blocks,List<String> ids){
        try{
            if(ids!=null && ids.size()>0){
                delDocuments(ids);
            }
            String _mongodbKey = InsertBatchDb(blocks);
            if (_mongodbKey == null) {
                throw new RuntimeException("插入报错");
            }
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    //添加jsonb
    //批量添加
    public String InsertBatchDb(List<DBObject>models){
        String sql = "insert into "+TableName+" (id,block_id,index,list_id,status,\"order\",json_data,is_delete) values " +
                " (nextval('luckysheet_id_seq'),?,?,?,?,?,?,0)";
        List<Object[]>batch=new ArrayList<Object[]>();
        int order=0;
        for(DBObject b : models){
            List<Object> objectList=new ArrayList<Object>();
            objectList.add(b.get("block_id").toString().trim());
            objectList.add(b.get("index"));
            objectList.add(b.get("list_id"));
            if(b.containsField("status") && b.get("status")!=null){
                objectList.add(b.get("status"));
            }else{
                objectList.add(0);
            }

            if(b.containsField("order") && b.get("order")!=null){
                objectList.add(b.get("order"));
                order=Integer.valueOf(b.get("order").toString());
            }else{
                objectList.add(order);
            }

            PGobject pg=new PGobject();
            pg.setType("json");
            try {
                DBObject db=new BasicDBObject();
                if(b.containsField("json_data")){
                    db=(DBObject) b.get("json_data");
                }else{
                    db.put("celldata", b.get("celldata"));
                }

                pg.setValue(new Gson().toJson(db));
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
            objectList.add(pg);

            Object[] params=(Object[])objectList.toArray(new Object[objectList.size()]);
            batch.add(params);
        }

        try{
            //long sttime = System.currentTimeMillis();
            jdbcTemplate_postgresql.batchUpdate(sql,batch);
            return "";
            //long endtime = System.currentTimeMillis();
            //System.out.println("保存:"+(endtime - sttime) / 1000.0 + "");
        }catch (Exception ex){
            log.error(ex.getMessage());
            return null;
        }
    }

    /**
     * 按list_id获取，返回sheet集合
     * @param list_id
     * @param flag
     * @return
     */
    public List<DBObject> getBlocksByGridKey(String list_id,boolean flag){
        try{
            String condition="";
            if(flag){
                condition=condition+"and block_id='fblock'";
            }
            String sql="select id,index from "+TableName+" p where  p.list_id=? "+condition;
            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql, new Object[]{list_id});
            List<DBObject> result=new ArrayList<DBObject>();
            for (Map<String, Object> map : list) {
                result.add(getDBObjectFromMap(map));
            }
            return result;
            //return mongoTemplate.find(query, DBObject.class, NEW_COLLECTION_NAME);
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    public boolean  batchUpdateForNoJsonbData(List<PgGridDataModel> models){
        try{
            String sql="update "+TableName+" set \"order\"=?  where  list_id=? and index=? and block_id=?";
            log.info("batchUpdateForNoJsonbData:"+sql);
            jdbcTemplate_postgresql.batchUpdate(sql, new BatchPreparedStatementSetter() {
                public int getBatchSize() {
                    return models.size();
                    //这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
                }
                public void setValues(PreparedStatement ps, int i)throws SQLException {
                    PgGridDataModel linkset =  models.get(i);
                    ps.setInt(1, linkset.getOrder());
                    ps.setString(2, linkset.getList_id());
                    ps.setString(3, linkset.getIndex());
                    ps.setString(4, linkset.getBlock_id());
                }
            });
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    @Transactional(value = "postgreTxManager",rollbackFor = Exception.class)
    public boolean  updateDataStatus(PgGridDataModel model){
        try{

            String sql1="update "+TableName+" set status=0  where  list_id=? and status=1 and block_id=?";
            log.info("updateSql1:"+sql1);
            jdbcTemplate_postgresql.update(sql1,new Object[]{model.getList_id(),model.getBlock_id()});

            String sql2="update "+TableName+" set status=1  where  list_id=? and index=? and block_id=?";
            log.info("updateSql2:"+sql2);
            jdbcTemplate_postgresql.update(sql2,new Object[]{model.getList_id(),model.getIndex(),model.getBlock_id()});
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional(value = "postgreTxManager",rollbackFor = Exception.class)
    public boolean  updateDataMsgHide(PgGridDataModel model,Integer hide,String index1,String index2){
        try{
            String sql1="update "+TableName+" set status=0 ,json_data=jsonb_set(json_data,'{hide}'::text[],'"+hide+"',true) where  list_id='"+model.getList_id()+"' and index='"+index1+"' and block_id='fblock'";
            log.info("updateSql1:"+sql1);
            jdbcTemplate_postgresql.update(sql1);
            String sql2="update "+TableName+" set status=1  where  list_id=? and index=? and block_id=?";
            log.info("updateSql2:"+sql2);
            jdbcTemplate_postgresql.update(sql2,new Object[]{model.getList_id(),index2,model.getBlock_id()});
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional(value = "postgreTxManager",rollbackFor = Exception.class)
    public boolean  updateDataMsgNoHide(PgGridDataModel model,Integer hide,String index){
        try{

            String sql1="update "+TableName+" set status=0  where  list_id=?  and block_id=?";
            log.info("updateSql1:"+sql1);
            jdbcTemplate_postgresql.update(sql1,new Object[]{model.getList_id(),model.getBlock_id()});

            String sql2="update "+TableName+" set status=1 ,json_data=jsonb_set(json_data,'{hide}'::text[],'"+hide+"',true) where  list_id='"+model.getList_id()+"' and index='"+index+"' and block_id='"+model.getBlock_id()+"'";
            log.info("updateSql2:"+sql2);
            jdbcTemplate_postgresql.update(sql2);
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public List<DBObject> getAllIndexsByGridKey(String list_id, List<String> indexs) {
        try{
            StringBuffer sql=new StringBuffer();
            sql.append("select * from "+TableName+" p where  p.list_id=? and p.index in (");
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
            sql.append(") order by p.order asc");
            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql.toString(), new Object[]{list_id});
            List<DBObject> result=new ArrayList<DBObject>();
            for (Map<String, Object> map : list) {
                result.add(getDBObjectFromMap(map));
            }
            return result;
            //return mongoTemplate.find(query, DBObject.class, NEW_COLLECTION_NAME);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    public List<DBObject> getIndexsByGridKey(String list_id, String indexs) {
        try{
            StringBuffer sql=new StringBuffer();
            sql.append("select * from "+TableName+" p where  p.list_id=? and p.index =? order by p.id asc ");
            List<Map<String, Object>> list=jdbcTemplate_postgresql.queryForList(sql.toString(), new Object[]{list_id,indexs});
            List<DBObject> result=new ArrayList<DBObject>();
            for (Map<String, Object> map : list) {
                result.add(getDBObjectFromMap(map));
            }
            return result;
            //return mongoTemplate.find(query, DBObject.class, NEW_COLLECTION_NAME);
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    private DBObject getDBObjectFromMap(Map<String, Object> map){
        DBObject db=new BasicDBObject();

        for (String key : map.keySet()) {
            try{
                if("json_data".equals(key)){
                    DBObject pgd=null;
                    try{
                        PGobject pg=(PGobject) map.get(key);
                        pgd=(DBObject) JSONParse.parse(pg.getValue());
                    }catch (Exception e) {
                        pgd=(DBObject) JSONParse.parse(map.get(key).toString());
                    }
                    db.put(key.toLowerCase(), pgd);
                }else{
                    db.put(key.toLowerCase(), map.get(key));
                }
            }catch (Exception e) {
                continue;
            }
        }
        return db;
    }

    /*fieldsObject.put("list_id",true);
    fieldsObject.put("index",true);
    fieldsObject.put("chart",true);
    fieldsObject.put("block_id",true);*/
    public DBObject getChartByGridKey(String gridKey2, String i) {
        //默认获取第一块
        try{
            String sql="select index,list_id,json_data->>'chart' AS chart,block_id from "+TableName+" p where p.list_id=? and p.index=? and p.block_id=? ";
            Map<String, Object> map=jdbcTemplate_postgresql.queryForMap(sql, new Object[]{gridKey2,i,FirstBlockId});
            DBObject db=new BasicDBObject();

            for (String key : map.keySet()) {
                if("chart".equals(key)){
                    DBObject pgd=null;
                    if(map.get(key)!=null){
                        try{
                            PGobject pg=(PGobject) map.get(key);
                            pgd=(DBObject) JSONParse.parse(pg.getValue());
                        }catch (Exception e) {
                            pgd=(DBObject) JSONParse.parse(map.get(key).toString());
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

    public boolean  updateDataForReDel(PgGridDataModel model){
        try{

            String sql1="update "+TableName+"  set is_delete=?  where  list_id=? and index=? ";
            log.info("updateSql1:"+sql1);
            jdbcTemplate_postgresql.update(sql1,new Object[]{model.getIs_delete(),model.getList_id(),model.getIndex()});
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    //更新jsonb中某条文本数据
    public boolean batchUpdateCellDataValue(List<String> block_ids, List<PgGridDataModel> models){
        try{
            String id="";
            for (String str : block_ids) {
                id=id+"'"+str+"',";
            }
            id=id.substring(0, id.length()-1);
            log.info("id:"+id);
            String delsql="DELETE from "+TableName+" where list_id=? and block_id in ("+id+") and index=? ";
            jdbcTemplate_postgresql.update(delsql,new Object[]{models.get(0).getList_id(),models.get(0).getIndex()});
            String sql = "insert into "+TableName+" (id,block_id,index,list_id,status,\"order\",json_data,is_delete) values " +
                    " (nextval('luckysheet_id_seq'),?,?,?,?,?,?,0)";
            List<Object[]>batch=new ArrayList<Object[]>();
            for(PgGridDataModel b : models){
                List<Object> objectList=new ArrayList<Object>();
                objectList.add(b.getBlock_id().trim());
                objectList.add(b.getIndex());
                objectList.add(b.getList_id());
                objectList.add(b.getStatus());
                objectList.add(b.getOrder());
                PGobject pg=new PGobject();
                pg.setType("json");
                try {
                    pg.setValue(new Gson().toJson(b.getJson_data()));
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }
                objectList.add(pg);

                Object[] params=(Object[])objectList.toArray(new Object[objectList.size()]);
                batch.add(params);
            }
            log.info("sqls"+sql);
            //long sttime = System.currentTimeMillis();
            jdbcTemplate_postgresql.batchUpdate(sql,batch);
            log.info("batchUpdateCellDataValue--end");
            return true;


        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }

    public boolean testUpdate(){
        String condition="";
        try{
            StringBuffer updateSql=new StringBuffer();
            updateSql.append("update "+TableName+" set status=0 ,json_data=jsonb_set(json_data,'{hide}'::text[],'1',true) where  list_id='9783810#9488#1685fa3d441a454d9f521409ebdb8cf3' and index='Sheet_e1aelleeG8oW_1553673432973' and block_id='fblock'");

            jdbcTemplate_postgresql.update(updateSql.toString());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            return false;
        }
    }

    //jsonb数据中元素添加元素
    @Transactional(value = "postgreTxManager",rollbackFor = Exception.class)
    public boolean updateJsonbForInsertNull(Query query,String word,DBObject db,Integer position,String words){
        String condition="";
        try{
            log.info("select:"+query.getQueryObject().toString());
            DBObject queryDB=query.getQueryObject();
            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            String createSql="update "+TableName+" set json_data=json_data||'{"+words+"}'::jsonb where 1=1 " +condition;
            log.info("createSql:"+createSql);
            jdbcTemplate_postgresql.update(createSql,arr.toArray());
            if(position!=null){
                word=word+","+position;
            }
            //(jsonb_v,'{myinfo,celldata,0}','{"c":1,"r":1,"v":{"con":"str"}}',false)
            String updateSql="update "+TableName+" set json_data=jsonb_insert(json_data,'{"+word+"}','"+db.toString()+"',false) where 1=1 " +condition;
            log.info("updateSql:"+updateSql);
            jdbcTemplate_postgresql.update(updateSql,arr.toArray());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    //jsonb数据中元素添加元素
    @Transactional(value = "postgreTxManager",rollbackFor = Exception.class)
    public boolean updateJsonbForSetNull(Query query,String word,DBObject db,Integer position){
        String condition="";
        try{
            log.info("select:"+query.getQueryObject().toString());
            DBObject queryDB=query.getQueryObject();
            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            log.info("arr:"+arr);
            String createSql="update "+TableName+" set json_data=jsonb_set(json_data,'{"+word+"}'::text[],'[]',true)  where 1=1 " +condition;
            log.info("createSql:"+createSql);
            jdbcTemplate_postgresql.update(createSql,arr.toArray());
            if(position!=null){
                word=word+","+position;
            }
            //(jsonb_v,'{myinfo,celldata,0}','{"c":1,"r":1,"v":{"con":"str"}}',false)
            String updateSql="update "+TableName+" set json_data=jsonb_set(json_data,'{"+word+"}'::text[],'"+db.toString()+"',true) where 1=1 " +condition;
            log.info("updateSql:"+updateSql);
            jdbcTemplate_postgresql.update(updateSql,arr.toArray());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * jsonb数据中元素添加元素
     * @param query
     * @param word
     * @param db
     * @param position
     * @param words
     * @return
     */
    @Transactional(value = "postgreTxManager",rollbackFor = Exception.class)
    public boolean updateJsonbForSetRootNull(Query query,String word,DBObject db,Integer position,String words){
        String condition="";
        try{
            log.info("select:"+query.getQueryObject().toString());
            DBObject queryDB=query.getQueryObject();
            List arr=new ArrayList<>();
            for (String key : queryDB.keySet()) {
                arr.add(queryDB.get(key));
                condition=condition+"and "+key+"=? ";
            }
            String createSql="update "+TableName+" set json_data=json_data||'{"+words+"}'::jsonb where 1=1 " +condition;
            log.info("createSql:"+createSql);
            jdbcTemplate_postgresql.update(createSql,arr.toArray());
            if(position!=null){
                word=word+","+position;
            }
            //(jsonb_v,'{myinfo,celldata,0}','{"c":1,"r":1,"v":{"con":"str"}}',false)
            String updateSql="update "+TableName+" set json_data=jsonb_set(json_data,'{"+word+"}','"+db.toString()+"',false) where 1=1 " +condition;
            log.info("updateSql:"+updateSql);
            jdbcTemplate_postgresql.update(updateSql,arr.toArray());
            return true;
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    public int deleteAll(){
        String sql="delete from "+TableName;
        try{
            return jdbcTemplate_postgresql.update(sql,new Object[]{});
        }catch (Exception e){
            log.warn(e.getMessage());
        }
        return 0;
    }

    public int[] delete(List<String> listIds ){
        if(listIds==null && listIds.size()==0){
            return new int[]{};
        }
        DataSource ds = jdbcTemplate_postgresql.getDataSource();
        BatchSqlUpdate bsu = new BatchSqlUpdate(ds, " delete  from "+TableName +" where list_id = ? ");
        bsu.setBatchSize(4);
        bsu.setTypes(new int[]{Types.VARCHAR});
        for(int i = 0; i < listIds.size(); i++){
            log.info(bsu.update(new Object[]{listIds.get(i)})+"");
        }
        return bsu.flush();
    }
}
