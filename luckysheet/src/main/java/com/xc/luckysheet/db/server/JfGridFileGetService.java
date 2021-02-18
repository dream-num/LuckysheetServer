package com.xc.luckysheet.db.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xc.luckysheet.JfGridConfigModel;
import com.xc.luckysheet.db.IRecordDataInsertHandle;
import com.xc.luckysheet.db.IRecordDataUpdataHandle;
import com.xc.luckysheet.db.IRecordDelHandle;
import com.xc.luckysheet.db.IRecordSelectHandle;
import com.xc.luckysheet.redisserver.GridFileRedisCacheService;
import com.xc.luckysheet.util.JfGridFileUtil;
import com.xc.luckysheet.utils.GzipHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class JfGridFileGetService {

//    @Resource(name = "postgresRecordDataInsertHandle")
//    private IRecordDataInsertHandle recordDataInsertHandle;
//
//    @Resource(name = "postgresRecordDataUpdataHandle")
//    private IRecordDataUpdataHandle recordDataUpdataHandle;
//
//    @Resource(name = "postgresRecordDelHandle")
//    private IRecordDelHandle recordDelHandle;
//
//    @Resource(name = "postgresRecordSelectHandle")
//    private IRecordSelectHandle recordSelectHandle;

    @Resource(name = "mysqlRecordDataInsertHandle")
    private IRecordDataInsertHandle recordDataInsertHandle;

    @Resource(name = "mysqlRecordDataUpdataHandle")
    private IRecordDataUpdataHandle recordDataUpdataHandle;

    @Resource(name = "mysqlRecordDelHandle")
    private IRecordDelHandle recordDelHandle;

    @Resource(name = "mysqlRecordSelectHandle")
    private IRecordSelectHandle recordSelectHandle;

    @Autowired
    private GridFileRedisCacheService redisService;


    /**
     * 1.3.3	获取表格数据 按gridKey获取,默认载入status为1
     * @param listId
     * @return
     */
    public List<JSONObject> getDefaultByGridKey(String listId){
        String i=recordSelectHandle.getFirstBlockIndexByGridKey(listId);
        redisService.raddFlagContent(listId+i, true);
        List<JSONObject>  dbObject=recordSelectHandle.getByGridKey_NOCelldata(listId);
        log.info("getDefaultByGridKey--dbObjectList:start");
        if(dbObject!=null && dbObject.size()>0){
            log.info("getDefaultByGridKey--start---dbObject");
            for(int x=0;x<dbObject.size();x++){
                JSONObject _o =dbObject.get(x);
                if(_o.containsKey("status") && _o.get("status").toString().equals("1")){
                    //获取当前显示的数据
                    //DBObject n=jfGridFileDao.getByGridKey(gridKey,Integer.parseInt(_o.get("index").toString()));
                    //dbObject.set(x,n);
                    String index=_o.get("index").toString();
                    //覆盖当前对象的数据信息
                    JSONArray _celldata=getCelldataBlockMergeByGridKey(listId,index);
                    _o.put("celldata",_celldata);
                }

                if(_o.containsKey("calcChain")){
                    Object calcChain=JfGridFileUtil.getObjectByIndex(_o, "calcChain");

                    log.info("calcChain--"+calcChain);
                    _o.put("calcChain", calcChain);
                }

            }
        }
        log.info("dbObject:true");
        return dbObject;
    }

    /**
     * 按list_id获取，返回指定sheet 当前sheet的全部分块数据（并合并）
     * @param listId
     * @param index
     * @return
     */
    public JSONArray getCelldataBlockMergeByGridKey(String listId,String index){
        //每一个分块数据合并后的对象
        JSONArray _celldata=new JSONArray();
        //获取全部块
        List<JSONObject> blocks=getBlocksByGridKey(listId, index);
        if(blocks!=null && blocks.size()>0){
            for(JSONObject _b:blocks){
                if(_b.containsKey("block_id")){
                    if(JfGridConfigModel.FirstBlockID.equals(_b.get("block_id"))){
                        //信息块
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
        return _celldata;
    }

    /**
     * 按list_id获取，返回指定sheet分块组
     * @param listId
     * @param index
     * @return
     */
    public List<JSONObject> getBlocksByGridKey(String listId,String index){
        return recordSelectHandle.getBlockAllByGridKey(listId, index);
    }

    /**
     * 1.3.4	获取sheet数据  参数为gridKey（表格主键） 和 index（sheet主键合集
     * @param listId
     * @param indexs
     * @return
     */
    public LinkedHashMap getByGridKeys(String listId, List<String> indexs){
        LinkedHashMap _resultModel=null;
        if(indexs==null || indexs.size()==0){
            return _resultModel;
        }
        //获取全部多个sheet的分块
        List<JSONObject> dbObject=null;
        if(indexs.size()==1){
            dbObject=recordSelectHandle.getIndexsByGridKey(listId, indexs.get(0));
        }else{
            dbObject= recordSelectHandle.getAllIndexsByGridKey(listId.toString(),indexs);
        }

        if(dbObject!=null && dbObject.size()>0){
            if(_resultModel==null){
                _resultModel=new LinkedHashMap<Integer,Object>();
            }
            log.info("getByGridKeys--dbObject-start");
            for(JSONObject _o:dbObject){
                if(!_o.containsKey("block_id")){
                    continue;
                }
                if(JfGridConfigModel.FirstBlockID.equals(_o.get("block_id"))){
                    continue;
                }
                //数据块处理
                if(_o.containsKey("index")){
                    try{
                        GzipHandle.toUncompressBySheet(_o);
                        String _index=_o.get("index").toString();
                        if(indexs.contains(_index)){
                            //数据块
                            JSONObject data=JfGridFileUtil.getJSONObjectByIndex(_o,"json_data");
                            JSONArray _cellData=JfGridFileUtil.getSheetByIndex(data);
                            if(_cellData!=null){
                                if(_resultModel.containsKey(_index)){
                                     JSONArray _blockCellData=(JSONArray)_resultModel.get(_index);
                                    _blockCellData.addAll(_cellData);
                                }else {
                                    _resultModel.put(_index, _cellData);
                                }
                            }
                        }
                    }catch (Exception ex){
                        log.error(ex.toString());
                    }
                }
            }
        }
        return _resultModel;
    }

    /**
     * 获取全部结构以及数据
     * @param gridKey
     * @return
     */
    public List<JSONObject> getAllSheetByGridKey(String gridKey) {
        //返回全部结构
        List<JSONObject>  dbObject=recordSelectHandle.getByGridKey_NOCelldata(gridKey);
        if(dbObject!=null&&dbObject.size()>0){
            //获取全部的index
            List<String> indexs=dbObject.stream().map(k->k.get("index").toString()).collect(Collectors.toList());
            //获取数据
            LinkedHashMap celldatas=getByGridKeys(gridKey,indexs);
            if(celldatas!=null&&celldatas.size()>0) {
                for (JSONObject _o : dbObject) {
                    _o.put("celldata", celldatas.get(_o.get("index")));
                }
            }
            return dbObject;
        }
        return null;
    }

    /**
     * 获取指定的xls激活的sheet页的 返回index（控制块）
     * @param gridKey
     * @return
     */
    public String getFirstBlockIndexByGridKey(String gridKey){
        return recordSelectHandle.getFirstBlockIndexByGridKey(gridKey);
    }
}
