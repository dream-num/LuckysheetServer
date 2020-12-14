package com.xc.luckysheet.postgre.server;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.xc.luckysheet.entity.JfGridConfigModel;
import com.xc.luckysheet.postgre.dao.PostgresGridFileDao;
import com.xc.luckysheet.redisserver.GridFileRedisCacheService;
import com.xc.luckysheet.utils.GzipHandle;
import com.xc.luckysheet.utils.JfGridFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class PostgresGridFileGetService {

    @Autowired
    private PostgresGridFileDao pgGridFileDao;
    @Autowired
    private GridFileRedisCacheService redisService;


    /**
     * 1.3.3	获取表格数据 按gridKey获取,默认载入status为1
     * @param gridKey
     * @return
     */
    public List<DBObject> getDefaultByGridKey(String gridKey){
        String i=pgGridFileDao.getFirstBlockIndexByGridKey(gridKey);
        redisService.raddFlagContent(gridKey+i, true);
        List<DBObject>  dbObject=pgGridFileDao.getByGridKey_NOCelldata(gridKey);
        log.info("getDefaultByGridKey--dbObjectList:start");
        if(dbObject!=null && dbObject.size()>0){
            log.info("getDefaultByGridKey--start---dbObject");
            for(int x=0;x<dbObject.size();x++){
                DBObject _o =dbObject.get(x);
                if(_o.containsField("status") && _o.get("status").toString().equals("1")){
                    //获取当前显示的数据
                    //DBObject n=jfGridFileDao.getByGridKey(gridKey,Integer.parseInt(_o.get("index").toString()));
                    //dbObject.set(x,n);
                    String index=_o.get("index").toString();
                    //覆盖当前对象的数据信息
                    BasicDBList _celldata=getCelldataBlockMergeByGridKey(gridKey,index);
                    _o.put("celldata",_celldata);
                }

                if(_o.containsField("calcChain")){
                    DBObject calcChain=JfGridFileUtil.getObjectByIndex(_o, "calcChain");

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
     * @param gridKey
     * @param index
     * @return
     */
    public BasicDBList getCelldataBlockMergeByGridKey(String gridKey,String index){
        //每一个分块数据合并后的对象
        BasicDBList _celldata=new BasicDBList();
        //获取全部块
        List<DBObject> blocks=getBlocksByGridKey(gridKey, index);
        if(blocks!=null && blocks.size()>0){
            for(DBObject _b:blocks){
                if(_b.containsField("block_id")){
                    if(JfGridConfigModel.FirstBlockID.equals(_b.get("block_id"))){
                        //信息块
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
        return _celldata;
    }

    /**
     * 按list_id获取，返回指定sheet分块组
     * @param list_id
     * @param index
     * @return
     */
    public List<DBObject> getBlocksByGridKey(String list_id,String index){
        return pgGridFileDao.getBlockAllByGridKey(list_id, index);
    }

    /**
     * 1.3.4	获取sheet数据  参数为gridKey（表格主键） 和 index（sheet主键合集
     * @param gridKey
     * @param indexs
     * @return
     */
    public LinkedHashMap getByGridKeys(String gridKey, List<String> indexs){
        LinkedHashMap _resultModel=null;
        if(indexs==null || indexs.size()==0){
            return _resultModel;
        }
        //获取全部多个sheet的分块
        List<DBObject> dbObject=null;
        if(indexs.size()==1){
            dbObject=pgGridFileDao.getIndexsByGridKey(gridKey, indexs.get(0));
        }else{
            dbObject= pgGridFileDao.getAllIndexsByGridKey(gridKey.toString(),indexs);
        }

        if(dbObject!=null && dbObject.size()>0){
            if(_resultModel==null){
                _resultModel=new LinkedHashMap<Integer,Object>();
            }
            log.info("getByGridKeys--dbObject-start");
            for(DBObject _o:dbObject){
                if(!_o.containsField("block_id")){
                    continue;
                }
                if(JfGridConfigModel.FirstBlockID.equals(_o.get("block_id"))){
                    continue;
                }
                //数据块处理
                if(_o.containsField("index")){
                    try{
                        GzipHandle.toUncompressBySheet(_o);
                        String _index=_o.get("index").toString();
                        if(indexs.contains(_index)){
                            //数据块
                            DBObject data=JfGridFileUtil.getObjectByIndex(_o,"json_data");
                            BasicDBList _cellData=JfGridFileUtil.getSheetByIndex(data);
                            if(_cellData!=null){
                                if(_resultModel.containsKey(_index)){
                                    BasicDBList _blockCellData=(BasicDBList)_resultModel.get(_index);
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

}
