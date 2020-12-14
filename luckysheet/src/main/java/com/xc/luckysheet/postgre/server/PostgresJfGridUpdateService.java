package com.xc.luckysheet.postgre.server;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.xc.common.utils.JsonUtil;
import com.xc.luckysheet.entity.ConfigMergeModel;
import com.xc.luckysheet.entity.JfGridConfigModel;
import com.xc.luckysheet.entity.PgGridDataModel;
import com.xc.luckysheet.entity.LuckySheetGridModel;
import com.xc.luckysheet.entity.enummodel.SheetOperationEnum;
import com.xc.luckysheet.postgre.dao.PostgresGridFileDao;
import com.xc.luckysheet.redisserver.GridFileRedisCacheService;
import com.xc.luckysheet.redisserver.RedisLock;
import com.xc.luckysheet.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class PostgresJfGridUpdateService {

    @Autowired
    private PostgresGridFileDao pgGridFileDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GridFileRedisCacheService gridFileRedisCacheService;


    /**
     * 插入新的表格数据
     *
     * @param dbObject
     * @return
     */
    public String insert(PgGridDataModel dbObject) {
        return pgGridFileDao.insert(dbObject);
    }
    public String insert(List<PgGridDataModel> dbObject) {
        return pgGridFileDao.InsertIntoBatch(dbObject);
    }


    /**
     * 执行更新操作,集合拆分
     *
     * @param gridKey
     * @param bson
     * @return
     */
    public String handleUpdate(String gridKey, DBObject bson) {
        StringBuilder _sb = new StringBuilder();
        if (bson instanceof BasicDBList) {
            List<DBObject> _list = (List<DBObject>) bson;
            //汇聚全部的 3.1单元格操作v
            List<DBObject> _vlist = new ArrayList<DBObject>();
            for (int x = 0; x < _list.size(); x++) {
                if (_list.get(x).containsField("t") && _list.get(x).get("t").equals("v")) {
                    //单元格处理
                    _vlist.add(_list.get(x));
                } else {
                    //其他操作
                    log.info("其他操作--sb.append:chooseOperation");
                    _sb.append(chooseOperation(gridKey, _list.get(x)));
                }
            }
            if (_vlist.size() > 0) {
                //执行单元格批量处理
                if (_vlist.size() == 1) {
                    _sb.append(Operation_v(gridKey, _vlist.get(0)));
                } else {
                    _sb.append(Operation_v(gridKey, _vlist));
                }
            }
        } else if (bson instanceof BasicDBObject) {
            log.info("bson instanceof BasicDBObject--bson");
            _sb.append(chooseOperation(gridKey, bson));
        }
        return _sb.toString();
    }

    //选择操作类型
    private String chooseOperation(String gridKey, DBObject bson) {
        if (bson.containsField("t")) {
            String _result = "";
            if (SheetOperationEnum.contains(bson.get("t").toString())) {
                SheetOperationEnum _e = SheetOperationEnum.valueOf(bson.get("t").toString());

                switch (_e) {
                    case c:
                        //3.9	图表操作 （第一块）
                        _result = Operation_c(gridKey, bson);
                        break;
                    case v:
                        //3.1	单元格操作v  gzip 分片
                        _result = Operation_v(gridKey, bson);
                        break;
                    case cg:
                        //3.2   config操作cg （第一块）
                        _result = Operation_cg(gridKey, bson);
                        break;
                    case all:
                        //3.3 通用保存 （第一块）
                        _result = Operation_all(gridKey, bson);
                        break;
                    case fc:
                        //3.4.1 函数链接 （第一块）
                        _result = Operation_fc(gridKey, bson);
                        break;
                    case f:
                        //3.6.1 选择筛选条件 （第一块）
                        _result = Operation_f(gridKey, bson);
                        break;
                    case fsc:
                        //3.6.2	清除筛 （第一块）
                        _result = Operation_fsc(gridKey, bson);
                        break;
                    case fsr:
                        //3.6.3	恢复筛选 （第一块）
                        _result = Operation_fsr(gridKey, bson);
                        break;
                    case drc:
                        //3.5.1 删除行或列   gzip 分块
                        _result = Operation_drc(gridKey, bson);
                        break;
                    case arc:
                        //3.5.2	增加行或列  gzip 分块
                        _result = Operation_arc(gridKey, bson);
                        break;
                    case sha:
                        //3.7.1	新建sha  sheet操作  gzip 分块
                        _result = Operation_sha(gridKey, bson);
                        break;
                    case shc:
                        //3.7.2	复制shc 分块
                        _result = Operation_shc(gridKey, bson);
                        break;
                    case shd:
                        //3.7.3	删除shd 分块
                        _result = Operation_shd(gridKey, bson);
                        break;
                    case shr:
                        //3.7.4	位置shr 分块
                        _result = Operation_shr(gridKey, bson);
                        break;
                    case shs:
                        //3.7.5	激活shs 分块
                        _result = Operation_shs(gridKey, bson);
                        break;
                    case sh:
                        //3.8.1	隐藏    3.8	sheet属性sh  分块
                        _result = Operation_sh(gridKey, bson);
                        break;
                    case na:
                        //3.10.1	表格名称 修改 数据库
                        _result = Operation_na(gridKey, bson);
                        break;
                    case thumb:
                        //3.10.2	缩略图    修改数据库
                        _result = Operation_thumb(gridKey, bson);
                        break;
                    case rv:
                        //批量单元格操作
                        _result = getIndexRvForThread(gridKey, bson);
                        break;
                    case shre:
                        _result = Operation_shre(gridKey, bson);
                        break;
                    case mv:
                        break;
                    default:
                        _result = "无对应操作符：" + JsonUtil.toJson(bson);
                        break;
                }
            } else {
                _result = "无对应操作符：" + JsonUtil.toJson(bson);
            }
            return _result;
        } else {
            return "无操作符：" + JsonUtil.toJson(bson);
        }
    }

    //3.8.1	隐藏    3.8	sheet属性sh
    /*
    * 	更新“i”对应sheet的根路径hide字段为v，当隐藏时status值为0，当显示时为1，
    * 	如果为隐藏则更新index对应cur的sheet的status状态为1
    *     [{"t":"sh","i":0,"v":1,"op":"hide","cur":1}]
          [{"t":"sh","i":0,"v":0,"op":"hide"}]
    *     隐藏：where index=i set hide=v，status=0
    *           where index=cur set status=1
    * 取消隐藏：where status=1 set status=0         将原来的status标记1的设置为 0
    *           where index=i set hide=v，status=1  将i对应的index的status设置为 1
    **/
    private String Operation_sh(String gridKey, DBObject bson) {
        try {
            String i = bson.get("i").toString();//当前sheet的index值
            String op = bson.get("op").toString();// 	操作选项，有hide、show。
            Integer v = 0;  //如果hide为1则隐藏，为0或者空则为显示
            String cur = null; //	隐藏后设置索引对应cur的sheet为激活状态
            if (bson.containsField("v") && bson.get("v") != null) {
                v = Integer.parseInt(bson.get("v").toString());
            }
            if (bson.containsField("cur") && bson.get("cur") != null) {
                cur = bson.get("cur").toString();
            }
            //1、先获取原数据
            List<DBObject> _dbObject = pgGridFileDao.getBlocksByGridKey(gridKey, true);
            if (_dbObject == null) {
                return "gridKey=" + gridKey + "的数据表格不存在";
            }
            //2、数据所在的sheet的序号
            Integer _sheetPosition = JfGridFileUtil.getSheetPositionByIndex(_dbObject, i);
            if (_sheetPosition == null) {
                return "index=" + i + "的sheet不存在";
            }
            PgGridDataModel model = new PgGridDataModel();
            boolean _result = false;
            if (op.equals("hide")) {
                //隐藏
                //设置i对应文档隐藏，并且status=0
                model.setBlock_id(JfGridConfigModel.FirstBlockID);
                model.setList_id(gridKey);
                _result = pgGridFileDao.updateDataMsgHide(model, v, i, cur);
            } else {
                //取消隐藏

                //设置全部status=0
//                _result=jfGridFileDao.updateOne(query,update);
//                if(!_result){
//                    return "更新失败";
//                }
                model.setBlock_id(JfGridConfigModel.FirstBlockID);
                model.setList_id(gridKey);
                _result = pgGridFileDao.updateDataMsgNoHide(model, v, i);
            }
            if (!_result) {
                return "更新失败";
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.7.5	激活shs
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_shs(String gridKey, DBObject bson) {
        try {
            if (!bson.containsField("v")) {
                return "参数错误";
            }
            //设置Sheet的激活状态，代表sheet的index
            String i = bson.get("v").toString();
            //1、先获取原数据
            List<DBObject> _dbObject = pgGridFileDao.getBlocksByGridKey(gridKey, true);
            if (_dbObject == null) {
                return "gridKey=" + gridKey + "的数据表格不存在";
            }
            //2、数据所在的sheet的序号
            Integer _sheetPosition = JfGridFileUtil.getSheetPositionByIndex(_dbObject, i);
            if (_sheetPosition == null) {
                return "index=" + i + "的sheet不存在";
            }
            PgGridDataModel model = new PgGridDataModel();
            model.setBlock_id(JfGridConfigModel.FirstBlockID);
            model.setIndex(i);
            model.setList_id(gridKey);
            boolean _result = pgGridFileDao.updateDataStatus(model);
            if (!_result) {
                return "更新失败";
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.7.4	位置shr
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_shr(String gridKey, DBObject bson) {
        try {
            if (!bson.containsField("v")) {
                return "参数错误";
            }
            DBObject _v = (DBObject) bson.get("v");
            if (_v != null && _v.keySet().size() > 0) {
                //1、先获取原数据
                List<DBObject> _dbObject = pgGridFileDao.getBlocksByGridKey(gridKey, false);
                if (_dbObject == null) {
                    return "gridKey=" + gridKey + "的数据表格不存在";
                }

                List<PgGridDataModel> models = new ArrayList<>();
                for (String _index : _v.keySet()) {
                    try {
                        // _index 为工作簿index值
                        String _i = _v.get(_index).toString();//要设置的order值
                        if (_i != null) {
                            PgGridDataModel model = new PgGridDataModel();
                            model.setList_id(gridKey);
                            model.setBlock_id(JfGridConfigModel.FirstBlockID);
                            model.setOrder(Integer.valueOf(_i));
                            model.setIndex(_index);
                            models.add(model);
                        }
                    } catch (Exception ex) {
                        log.error(ex.toString());
                    }
                }

                if (models.size() > 0) {
                    boolean _result = pgGridFileDao.batchUpdateForNoJsonbData(models);
                    if (!_result) {
                        return "更新失败";
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.7.3	删除shd
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_shd(String gridKey, DBObject bson) {
        try {
            String deleIndex = null;//	需要删除的sheet索引
            if (bson.containsField("v")) {
                DBObject _v = (DBObject) bson.get("v");
                if (_v.containsField("deleIndex")) {
                    deleIndex = _v.get("deleIndex").toString();
                }
            }
            if (deleIndex == null) {
                return "参数错误";
            }

            //1、先获取原数据
            List<DBObject> _dbObject = pgGridFileDao.getBlocksByGridKey(gridKey, false);
            if (_dbObject == null) {
                return "gridKey=" + gridKey + "的数据表格不存在";
            }
            //2、数据所在的sheet的序号
            PgGridDataModel model = new PgGridDataModel();
            model.setIndex(deleIndex);
            model.setList_id(gridKey);
            model.setIs_delete(1);
            boolean result = pgGridFileDao.updateDataForReDel(model);
            if (!result) {
                return "更新失败";
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.7.2	复制shc
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_shc(String gridKey, DBObject bson) {
        try {
            String i = bson.get("i").toString();//	新建sheet的位置
            String copyindex = null;//复制对象
            String name = null;
            if (bson.containsField("v")) {
                DBObject _v = (DBObject) bson.get("v");
                if (_v.containsField("copyindex")) {
                    copyindex = _v.get("copyindex").toString();
                }
                if (_v.containsField("name")) {
                    name = (String) _v.get("name");
                }
            }
            if (copyindex == null) {
                return "参数错误";
            }
            //1、先获取原数据
            List<DBObject> _dbObjects = pgGridFileDao.getBlockAllByGridKey(gridKey, copyindex);
            if (_dbObjects == null) {
                return "gridKey=" + gridKey + "的数据表格不存在";
            }

            for (DBObject _dbObject : _dbObjects) {
                if (_dbObject.containsField("id")) {
                    _dbObject.removeField("id");
                }
                DBObject jsondata = (DBObject) _dbObject.get("json_data");
                if (jsondata.containsField("name")) {
                    jsondata.put("name", name);
                    _dbObject.put("json_data", jsondata);
                }

                _dbObject.put("index", i);//新建sheet的位置
                _dbObject.put("status", 0);
            }

            String _mongodbKey = pgGridFileDao.InsertBatchDb(_dbObjects);
            if (_mongodbKey == null) {
                return "更新失败";
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.7.1	新建sha  sheet操作
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_sha(String gridKey, DBObject bson) {
        try {
            //Integer i=Integer.parseInt(bson.get("i").toString());// 当前sheet的index值,此处为null
            DBObject v = (DBObject) bson.get("v");   //创建的对象
            log.info("Operation_sha--v:" + v);
            String index = null;// v中Index索引
            if (v.containsField("index")) {
                index = "" + v.get("index").toString();
            }
            if (index == null) {
                return "index不能为null";
            }
            log.info("Operation_sha---" + index);
            //1、先获取原数据
            List<DBObject> _dbObject = pgGridFileDao.getBlocksByGridKey(gridKey.toString(), false);
            log.info("getIndexByGridKey---" + _dbObject);
            if (_dbObject == null) {
                return "gridKey=" + gridKey + "的数据表格不存在";
            }
            //2、数据所在的sheet的序号
            Integer _sheetPosition = JfGridFileUtil.getSheetPositionByIndex(_dbObject, index);
            log.info("_sheetPosition--" + _sheetPosition);
            if (_sheetPosition != null) {
                return "index=" + index + "的sheet已经存在";
            }


            PgGridDataModel model = new PgGridDataModel();
            model.setList_id(gridKey);
            model.setBlock_id(JfGridConfigModel.FirstBlockID);
            model.setIndex(index);
            model.setStatus(0);
            model.setOrder(Integer.valueOf(v.get("order").toString()));
            v.removeField("list_id");
            v.removeField("block_id");
            v.removeField("index");
            v.removeField("status");
            v.removeField("order");
            model.setJson_data(v);
            //GzipHandle.toCompressBySheet(v);
            String _mongodbKey = pgGridFileDao.insert(model);
            if (_mongodbKey == null) {
                return "更新失败";
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.5.2 增加行或列
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_arc(String gridKey, DBObject bson) {
        try {
            String i = bson.get("i").toString();//	当前sheet的index值
            String rc = bson.get("rc").toString();   //行操作还是列操作，值r代表行，c代表列
            Integer index = null;//  		从第几行或者列开始新增
            Integer len = null;// 		增加多少行或者列
            BasicDBList data = null;// 	新增行或者列的内容
            DBObject mc = null;//     	需要修改的合并单元格信息
            String direction = null;//方向
            if (bson.get("v") != null && bson instanceof DBObject) {
                DBObject _v = (DBObject) bson.get("v");
                if (_v.containsField("index")) {
                    index = Integer.parseInt(_v.get("index").toString());
                }
                if (_v.containsField("len")) {
                    len = Integer.parseInt(_v.get("len").toString());
                }
                if (_v.containsField("data") && _v.get("data") instanceof BasicDBList) {
                    data = (BasicDBList) _v.get("data");
                }
                if (_v.containsField("mc")) {
                    mc = (DBObject) _v.get("mc");
                }
                if (_v.containsField("direction")) {
                    direction = _v.get("direction").toString().trim();
                }

            }
            if (index == null || len == null) {
                return "参数错误";
            }

            //1、先获取原数据
            List<String> mongodbKeys = new ArrayList<String>();//mongodb的key，用于删除
            DBObject _dbObject = pgGridFileDao.getBlockMergeByGridKey(gridKey, i, mongodbKeys);
            if (_dbObject == null) {
                return "list_id=" + gridKey + ",index=" + i + "的sheet不存在";
                //return "gridKey="+gridKey+"的数据表格不存在";
            }

            DBObject json_data = JfGridFileUtil.getObjectByIndex(_dbObject, "json_data");
            Integer _column = JfGridFileUtil.getIntegerByIndex(json_data, "column"),
                    _row = JfGridFileUtil.getIntegerByIndex(json_data, "row");

            //获取整个表格
            BasicDBList _celldatas = JfGridFileUtil.getSheetByIndex(_dbObject);
            if (_celldatas != null) {
                for (int x = _celldatas.size() - 1; x >= 0; x--) {
                    DBObject _cell = (DBObject) _celldatas.get(x);
                    Integer _r = Integer.parseInt(_cell.get("r").toString());
                    Integer _c = Integer.parseInt(_cell.get("c").toString());
                    //判断是否添加
                    if (rc.equals("r")) {
                        //行操作
                        if ("lefttop".equals(direction)) {
                            if (_r >= (index)) {
                                //增加之后需要+行号
                                ((DBObject) _celldatas.get(x)).put("r", _r + len);
                            }
                        } else {
                            if (_r > (index)) {
                                //增加之后需要+行号
                                ((DBObject) _celldatas.get(x)).put("r", _r + len);
                            }
                        }
                    } else if (rc.equals("c")) {
                        //列操作
                        //行操作
                        if ("lefttop".equals(direction)) {
                            if (_c >= (index)) {
                                //增加之后需要+行号
                                ((DBObject) _celldatas.get(x)).put("c", _c + len);
                            }
                        } else {
                            if (_c > (index)) {
                                //增加之后需要+行号
                                ((DBObject) _celldatas.get(x)).put("c", _c + len);
                            }
                        }
                    }
                }
                //是否增加新的
                if (data != null) {
                    if (data.size() > 0) {
                        List<Object> _addList = new ArrayList<Object>();
                        if (rc.equals("r")) {
                            //行操作
                            for (int _x = 0; _x < data.size(); _x++) {
                                if (data.get(_x) != null && data.get(_x) instanceof List) {
                                    List _b = (List) data.get(_x);
                                    for (int _x1 = 0; _x1 < _b.size(); _x1++) {
                                        if (_b.get(_x1) != null) {
                                            DBObject _m = new BasicDBObject();
                                            _m.put("r", _x + index);
                                            _m.put("c", _x1);
                                            _m.put("v", _b.get(_x1));
                                            _addList.add(_m);
                                        }
                                    }
                                }
                            }
                        } else if (rc.equals("c")) {
                            //列操作
                            for (int _x = 0; _x < data.size(); _x++) {
                                if (data.get(_x) != null && data.get(_x) instanceof List) {
                                    List _b = (List) data.get(_x);
                                    for (int _x1 = 0; _x1 < _b.size(); _x1++) {
                                        if (_b.get(_x1) != null) {
                                            DBObject _m = new BasicDBObject();
                                            _m.put("r", _x);
                                            _m.put("c", _x1 + index);
                                            _m.put("v", _b.get(_x1));
                                            _addList.add(_m);
                                        }
                                    }
                                }
                            }
                        }

                        _celldatas.addAll(_addList);
                    }
                }

                //更新一下config中的merge信息
                if (mc != null) {
                    if (json_data.containsField("config")) {
                        ((DBObject) json_data.get("config")).put("merge", mc);
                    } else {
                        DBObject _d = new BasicDBObject();
                        _d.put("merge", mc);
                        json_data.put("config", _d);
                    }
                    //对mc进行遍历，更新jfgridfile[1].data中合并单元格(mc)的数据
                    drc_arc_handle_mc(mc, _celldatas);
                }

                if (rc.equals("r")) {
                    json_data.put("row", _row + len);
                } else {
                    json_data.put("column", _column + len);
                }
                _dbObject.put("json_data", json_data);
                //数据分组，删除原有数据，重新保存
                List<DBObject> blocks = JfGridConfigModel.toDataSplit(_dbObject);
                boolean _result = pgGridFileDao.updateMulti2(blocks, mongodbKeys);
                //boolean _result=false;
                if (!_result) {
                    return "更新失败";
                }
            }


        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.5.1 删除行或列
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_drc(String gridKey, DBObject bson) {
        try {
            String i = bson.get("i").toString();//	当前sheet的index值
            String rc = bson.get("rc").toString();   //行操作还是列操作，值r代表行，c代表列
            Integer index = null;//  	从第几行或者列开始删除
            Integer len = null;// 	删除多少行或者列
            DBObject mc = null;//     	需要修改的合并单元格信息
            BasicDBList borderInfo = null;
            if (bson.get("v") != null && bson instanceof DBObject) {
                DBObject _v = (DBObject) bson.get("v");
                if (_v.containsField("index")) {
                    index = Integer.parseInt(_v.get("index").toString());
                }
                if (_v.containsField("len")) {
                    len = Integer.parseInt(_v.get("len").toString());
                }
                if (_v.containsField("mc")) {
                    mc = (DBObject) _v.get("mc");
                }
                if (_v.containsField("borderInfo")) {
                    borderInfo = (BasicDBList) _v.get("borderInfo");
                }
            }
            if (index == null || len == null) {
                return "参数错误";
            }


            //1、先获取原数据
            List<String> mongodbKeys = new ArrayList<String>();//mongodb的key，用于删除
            DBObject _dbObject = pgGridFileDao.getBlockMergeByGridKey(gridKey, i, mongodbKeys);
            if (_dbObject == null) {
                return "list_id=" + gridKey + ",index=" + i + "的sheet不存在";
                //return "gridKey="+gridKey+"的数据表格不存在";
            }

            DBObject json_data = JfGridFileUtil.getObjectByIndex(_dbObject, "json_data");

            //获取整个表格
            BasicDBList _celldatas = JfGridFileUtil.getSheetByIndex(_dbObject);
            if (_celldatas != null) {
                for (int x = _celldatas.size() - 1; x >= 0; x--) {
                    DBObject _cell = (DBObject) _celldatas.get(x);
                    Integer _r = Integer.parseInt(_cell.get("r").toString());
                    Integer _c = Integer.parseInt(_cell.get("c").toString());
                    //判断是否删除
                    if (rc.equals("r")) {
                        //行操作
                        if (_r >= index && _r <= (index + len - 1)) {
                            //删除范围内
                            _celldatas.remove(x);
                        }
                        if (_r >= (index + len)) {
                            //删除之后需要-行号
                            ((DBObject) _celldatas.get(x)).put("r", _r - len);
                        }
                    } else if (rc.equals("c")) {
                        //列操作
                        if (_c >= index && _c <= (index + len - 1)) {
                            //删除范围内
                            _celldatas.remove(x);
                        }
                        if (_c >= (index + len)) {
                            //删除之后需要-行号
                            ((DBObject) _celldatas.get(x)).put("c", _c - len);
                        }
                    }
                }

                // 多块

                //更新一下config中的merge信息
                DBObject _d = new BasicDBObject();
                if (mc != null) {
                    if (json_data.containsField("config")) {
                        ((DBObject) json_data.get("config")).put("merge", mc);
                    } else {
                        _d.put("merge", mc);
                        json_data.put("config", _d);
                    }
                    //对mc进行遍历，更新jfgridfile[1].data中合并单元格(mc)的数据
                    drc_arc_handle_mc(mc, _celldatas);
                }

                //更新一下config中的merge信息
                if (borderInfo != null) {
                    if (json_data.containsField("config")) {
                        ((DBObject) json_data.get("config")).put("borderInfo", borderInfo);
                    } else {
                        _d.put("borderInfo", mc);
                        json_data.put("config", _d);
                    }
                   /* //对mc进行遍历，更新jfgridfile[1].data中合并单元格(mc)的数据
                    drc_arc_handle_mc(mc,_celldatas);*/
                }
                

                /*if(rc.equals("r")){
                	json_data.put("row",_row-len);
                }else{
                	json_data.put("column",_column-len);
                }*/
                _dbObject.put("json_data", json_data);
                //数据分组，删除原有数据，重新保存
                List<DBObject> blocks = JfGridConfigModel.toDataSplit(_dbObject);
                boolean _result = pgGridFileDao.updateMulti2(blocks, mongodbKeys);
                if (!_result) {
                    return "更新失败";
                }
            }


        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 用mc重置data中数据
     *
     * @param mc
     * @param _celldatas
     */
    private void drc_arc_handle_mc(DBObject mc, BasicDBList _celldatas) {
        List<ConfigMergeModel> _list = ConfigMergeModel.getListByDBObject(mc);
        for (int x = _celldatas.size() - 1; x >= 0; x--) {
            try {
                DBObject _cell = (DBObject) _celldatas.get(x);
                Integer _r = Integer.parseInt(_cell.get("r").toString());
                Integer _c = Integer.parseInt(_cell.get("c").toString());
                for (ConfigMergeModel _cmModel : _list) {
                    if (_cmModel.isRange(_r, _c)) {
                        if (_cell.containsField("v")) {
                            DBObject _v = (DBObject) _cell.get("v");
                            if (_v.containsField("mc")) {
                                DBObject _mc = (DBObject) _v.get("mc");
                                _mc.put("r", _cmModel.getR());
                                _mc.put("c", _cmModel.getC());
                            }
                        }
                        break;
                    }
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }
    }

    //3.3 通用保存
    private String Operation_all(String gridKey, DBObject bson) {
        /*
        {
          "t": "all",
          "i": 3,
          "v": "{\"pivot_select_save\":{\"left\":105,\"width\":73,\"top\":0,\"height\":19,\"left_move\":105,\"width_move\":600,\"top_move\":0,\"height_move\":79,\"row\":[0,3],\"column\":[1,6],\"row_focus\":0,\"column_focus\":1},\"pivotDataSheetIndex\":0,\"column\":[{\"index\":0,\"name\":\"分公司\",\"fullname\":\"分公司\"}],\"row\":[],\"filter\":[],\"values\":[{\"index\":1,\"name\":\"年度保费目标\",\"fullname\":\"求和:年度保费目标\",\"sumtype\":\"SUM\",\"nameindex\":0},{\"index\":2,\"name\":\"年度NBV目标\",\"fullname\":\"求和:年度NBV目标\",\"sumtype\":\"SUM\",\"nameindex\":0},{\"index\":3,\"name\":\"期交保费\",\"fullname\":\"求和:期交保费\",\"sumtype\":\"SUM\",\"nameindex\":0},{\"index\":4,\"name\":\"期交达成\",\"fullname\":\"求和:期交达成\",\"sumtype\":\"SUM\",\"nameindex\":0},{\"index\":5,\"name\":\"NBV\",\"fullname\":\"求和:NBV\",\"sumtype\":\"SUM\",\"nameindex\":0}],\"showType\":\"column\",\"drawPivotTable\":false,\"pivotTableBoundary\":[3,17]}",
          "k": "pivotTable",
          "s": true
        }
        * */
        try {
            log.info("start---Operation_all" + bson.toString());
            String i = bson.get("i").toString();//	当前sheet的index值
            String k = bson.get("k").toString();   //	需要保存的key-value中的key
            String s = "true";   //	如果是true则v保存为字符串，否则按照对象进行保存
            log.info("Operation_all:i:" + i + "k:" + k);
            Object _v = null;//需要替换的值 (可能是对象也可能是字符串)
            if (bson.get("v") != null) {
                if (bson.get("v") instanceof String) {
                    log.info("bson.get('v')+string+true");
                    try {
                        _v = (DBObject) JSON.parse(bson.get("v").toString());
                    } catch (Exception e) {
                        log.error("DBObject---error");
                        _v = bson.get("v");
                    }

                } else {
                    log.info("bson.get('v')+false");
                    _v = (DBObject) bson.get("v");
                    s = "false";
                    log.info("Operation_all:_v:false:" + s);
                }
            } else {
                s = "true";
                _v = null;
            }
            //此处如果是数据透视图，改变s为false
            if (_v != null) {
                if (_v.toString().indexOf("{\"pivot_select_save\":") > -1) {
                    s = "false";
                }
            }


            log.info("Operation_all:start+getConfigByGridKey:_v:" + _v);
            //1、先获取原数据
            DBObject _dbObject = pgGridFileDao.getConfigByGridKey(gridKey, i);
            if (_dbObject == null) {
                return "list_id=" + gridKey + ",index=" + i + "的sheet不存在";
                //return "gridKey="+gridKey+"的数据表格不存在";
            }

            Query query = new Query();
            //query.addCriteria(Criteria.where("list_id").is(gridKey));
            query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));
            boolean _result = false;
            String keyName = k;
            log.info("start----update+s:" + s);
            if (s.equals("true")) {
                if (null == _v) {

                } else {
                    _v = "\"" + _v + "\"";
                }

                _result = pgGridFileDao.updateCellDataListTxtValue(query, keyName, null, _v);
            } else {
                try {
                    DBObject _vdb = (DBObject) JSON.parse(_v.toString());
                    //update.set(k,_vdb);
                    _result = pgGridFileDao.updateCellDataListValue(query, keyName, null, _vdb);
                    //update.set("jfgridfile."+_sheetPosition+"."+k,_vdb);
                } catch (Exception ex) {
                    log.error("Operation_all--erorr:" + ex.toString());
                    _v = "\"" + _v + "\"";
                    _result = pgGridFileDao.updateCellDataListTxtValue(query, keyName, null, _v);
                    //update.set("jfgridfile."+_sheetPosition+"."+k,_v);
                }

            }
            log.info("updateOne--start");
            if (!_result) {
                return "更新失败";
            }

        } catch (Exception ex) {
            log.error("Operation_all--err--all:" + ex.getMessage());
        }
        return "";
    }

    /**
     * 3.4.1 函数链接
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_fc(String gridKey, DBObject bson) {
        try {
            //当前sheet的index值
            String i = bson.get("i").toString();
            //对象值，这里对象的内部字段不需要单独更新，所以存为文本即可  2018-11-28 前段需求必须取出时为对象
            DBObject v = null;
            if (bson.get("v") instanceof String) {
                v = (DBObject) JSON.parse(bson.get("v").toString());
            } else {
                v = (DBObject) bson.get("v");
            }

            //操作类型,add为新增，update为更新，del为删除
            String op = bson.get("op").toString();
            //更新或者删除的函数位置
            String pos = bson.get("pos").toString();

            //1、先获取原数据
            DBObject _dbObject = pgGridFileDao.getConfigByGridKey(gridKey, i);
            if (_dbObject == null) {
                return "list_id=" + gridKey + ",index=" + i + "的sheet不存在";
                //return "gridKey="+gridKey+"的数据表格不存在";
            }

            Query query = new Query();
            //query.addCriteria(Criteria.where("list_id").is(gridKey));
            query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));
            boolean _result = false;
            DBObject calcChain = JfGridFileUtil.getObjectByIndex(_dbObject, "calcchain");
            if (calcChain == null) {
                //不存在 (只处理添加)
                if (op.equals("add")) {

                    //update.set("calcChain",_dlist);//添加
                    _result = pgGridFileDao.updateJsonbForSetNull(query, "calcChain", v, 0);
                }
            } else {
                //存在
                if (op.equals("add")) {
                    _result = pgGridFileDao.updateJsonbForElementInsert(query, "calcChain", v, 0);
                } else if (op.equals("update")) {
                    //update.set("calcChain."+pos,v);//修改
                    _result = pgGridFileDao.updateCellDataListValue(query, "calcChain", pos, v);
                } else if (op.equals("del")) {
                    if (calcChain instanceof List) {
                        List<DBObject> _list = (List<DBObject>) calcChain;
                        Integer size = Integer.valueOf(pos);
                        if (size <= _list.size()) {
                            int listindex = size;
                            _list.remove(listindex);
                            //update.set("calcChain",calcChain);//重新赋值
                            _result = pgGridFileDao.updateCellDataListValue(query, "calcChain", null, calcChain);
                        }
                    }
                }
            }
            if (!_result) {
                return "更新失败";
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }


    /**
     * 3.6.1 选择筛选条件
     * 更新jfgridfile[i].filter = { pos : v }， v值为一个JSON格式的字符串。
     * filter为一个键值对，
     * key表示选项位置的索引值（以字符表示），
     * v表示一个json字符串参数。filter代表一个筛选条件的集合
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_f(String gridKey, DBObject bson) {
        try {
            //当前sheet的index值
            String i = bson.get("i").toString();
            //对象值，这里对象的内部字段不需要单独更新，所以存为文本即可
            String v = bson.get("v").toString();
            //操作类型upOrAdd为更新，如果不存在则增加，del为删除
            String op = bson.get("op").toString();
            //更新或者删除的option位置
            String pos = bson.get("pos").toString();

            //1、先获取原数据
            DBObject _dbObject = pgGridFileDao.getConfigByGridKey(gridKey, i);
            if (_dbObject == null) {
                return "list_id=" + gridKey + ",index=" + i + "的sheet不存在";
                //return "gridKey="+gridKey+"的数据表格不存在";
            }

            Query query = new Query();
            //query.addCriteria(Criteria.where("list_id").is(gridKey));
            query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));
            boolean _result = false;
            //不管是否存在，都能直接添加
            DBObject filter = JfGridFileUtil.getObjectByIndex(_dbObject, "filter");
            if (op.equals("upOrAdd")) {
                // update.set("filter."+pos,v);//修改

                _result = pgGridFileDao.updateCellDataListValue(query, "filter", pos, v);
            } else if (op.equals("del")) {
                if (filter != null) {
                    //update.unset("filter."+pos);//删除
                    _result = pgGridFileDao.updateCellDataListValue(query, "filter", pos, v);
                }
            }
            if (!_result) {
                return "更新失败";
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.6.2	清除筛
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_fsc(String gridKey, DBObject bson) {
        try {
            //当前sheet的index值
            String i = bson.get("i").toString();

            //1、先获取原数据
            DBObject _dbObject = pgGridFileDao.getConfigByGridKey(gridKey, i);
            if (_dbObject == null) {
                return "list_id=" + gridKey + ",index=" + i + "的sheet不存在";
                //return "gridKey="+gridKey+"的数据表格不存在";
            }

            Query query = new Query();
            //query.addCriteria(Criteria.where("list_id").is(gridKey));
            query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));

            //不管是否存在，都能直接清除
            /*DBObject v=new BasicDBObject();
            update.set("filter",v);//清除
            update.set("filter_select",v);//清除
            */
            String word = "\"filter\":null,\"filter_select\":null";
            boolean _result = pgGridFileDao.rmJsonbDataForEmpty(query, word);
            if (!_result) {
                return "更新失败";
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.6.3	恢复筛选
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_fsr(String gridKey, DBObject bson) {
        try {
            //当前sheet的index值
            String i = bson.get("i").toString();
            Object filter = null;
            if (bson.get("filter") != null) {
                filter = (Object) bson.get("filter");
            } else {
                filter = new BasicDBObject();
            }

            Object filter_select = null;//
            if (bson.get("filter_select") != null) {
                filter_select = (Object) bson.get("filter_select");
            } else {
                filter_select = new BasicDBList();
            }

            //1、先获取原数据
            DBObject _dbObject = pgGridFileDao.getConfigByGridKey(gridKey, i);
            if (_dbObject == null) {
                return "list_id=" + gridKey + ",index=" + i + "的sheet不存在";
                //return "gridKey="+gridKey+"的数据表格不存在";
            }

            Query query = new Query();
            //query.addCriteria(Criteria.where("list_id").is(gridKey));
            query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));
            DBObject db = new BasicDBObject();
            db.put("filter", filter);
            db.put("filter_select", filter_select);
            boolean _result = pgGridFileDao.updateJsonbDataForKeys(query, db);
            if (!_result) {
                return "更新失败";
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }


    /**
     * 3.2   config操作cg
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_cg(String gridKey, DBObject bson) {
        try {
            //当前sheet的index值
            String i = bson.get("i").toString();
            String k = bson.get("k").toString();

            DBObject _v = null;//需要替换的值
            if (bson.get("v") != null) {
                _v = (DBObject) bson.get("v");
            }
            if (_v == null) {
                //没有要修改的值
                return "";
            }

            //1、先获取原数据
            DBObject _dbObject = pgGridFileDao.getConfigByGridKey(gridKey, i);
            if (_dbObject == null) {
                return "list_id=" + gridKey + ",index=" + i + "的sheet不存在";
                //return "gridKey="+gridKey+"的数据表格不存在";
            }

            Query query = new Query();
            //query.addCriteria(Criteria.where("list_id").is(gridKey));
            //判断_v中是否存在null，则删除该参数
            boolean flag = false;
            String keys = "";
            if (_v.keySet().size() != 0) {
                for (String key : _v.keySet()) {
                    if (_v.get(key) == null) {
                        keys = key;
                        flag = true;
                    }
                }
            } else {
                flag = true;
            }


            query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));
            DBObject _config = JfGridFileUtil.getObjectByIndex(_dbObject, "config");
            String keyName = "";
            boolean _result = false;
            if (_config != null) {
                if (flag) {
                    if ("".equals(keys)) {
                        keyName = "config," + k;
                    } else {
                        keyName = "config," + k + "," + keys;
                    }
                    _result = pgGridFileDao.rmCellDataValue(query, keyName);
                    if (!_result) {
                        return "删除失败";
                    }
                } else {
                    DBObject _k = JfGridFileUtil.getObjectByObject(_config, k);
                    if (_k != null) {
                        //新值覆盖旧值
                        //_k.putAll(_v);
                        keyName = "config," + k;
                        //对jsonb某个元素具体数据具体更新
                        _result = pgGridFileDao.updateCellDataListValue(query, keyName, null, _v);
                        //update.set("jfgridfile."+_sheetPosition+".config."+k,_k);
                    } else {
                        //插入一个
                        //update.set("config."+k,_v);
                        _result = pgGridFileDao.updateJsonbForSetRootNull(query, "config," + k, _v, null, "\"config\":{\"" + k + "\":\"\"}");
                        //update.set("jfgridfile."+_sheetPosition+".config."+k,_v);
                    }
                    if (!_result) {
                        return "更新失败";
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.1	单元格操作v  多个不同的分块,
     *
     * @param gridKey
     * @param dbObject
     * @return
     */
    private String Operation_v(String gridKey, List<DBObject> dbObject) {
        try {
            int _count = dbObject.size();
            log.info("start---Operation_v--list" + dbObject);
            //已存在的块
            HashMap<String, DBObject> _existsBlock = new HashMap<String, DBObject>();
            //不存在的块
            HashMap<String, DBObject> _noExistsBlock = new HashMap<String, DBObject>();

            for (int x = 0; x < _count; x++) {
                DBObject bson = dbObject.get(x);
                String i = bson.get("i").toString();//	当前sheet的index值
                Integer r = Integer.parseInt(bson.get("r").toString());//	单元格的行号
                Integer c = Integer.parseInt(bson.get("c").toString());//	单元格的列号
                Object v = bson.get("v");  //	单元格的值 v=null 删除单元格

                if (x == 0) {
                    //此处假设都是同一个sheet
                    //判断第一个块是否存在
                    Integer isHave = pgGridFileDao.getFirstBlockByGridKey(gridKey, i);
                    if (isHave == null || isHave == 0) {
                        return "list_id=" + gridKey + ",index=" + i + "的sheet不存在;";
                    }
                }
                //获取数据所在块的编号
                String block_id = JfGridConfigModel.getRange(r, c);

                boolean isExists = false;
                DBObject _dbObject = null;
                if (_existsBlock.containsKey(block_id)) {
                    //mongodb已存在的，处理成执行语句
                    isExists = true;
                    _dbObject = _existsBlock.get(block_id);
                } else if (_noExistsBlock.containsKey(block_id)) {
                    //mongodb不存在的
                    _dbObject = _noExistsBlock.get(block_id);
                } else {
                    //已有的中不存在
                    //1、先获取原数据（直接获取到某个sheet）
                    _dbObject = pgGridFileDao.getCelldataByGridKey(gridKey, i, block_id);
                    if (_dbObject == null) {
                        //不存在新建一块处理
                        //集合
                        BasicDBList _celldata = new BasicDBList();
                        //文档
                        DBObject db = new BasicDBObject();
                        db.put("celldata", _celldata);
                        db.put("block_id", block_id);//当前sheet的块编号
                        db.put("index", i); //表格sheet的编号
                        db.put("list_id", gridKey);//表格编号
                        _noExistsBlock.put(block_id, db);
                        _dbObject = db;
                    } else {
                        //已存在
                        isExists = true;
                        _existsBlock.put(block_id, _dbObject);
                    }

                }

                //单元格处理，这一步对象已经获取
                if (v != null) {
                    //修改/添加

                    DBObject _v = new BasicDBObject();
                    _v.put("r", r);
                    _v.put("c", c);
                    _v.put("v", v);

                    if (isExists) {
                        //已存在的
                        int _position = -1;//所在位置，更新使用
                        BasicDBList _celldata = JfGridFileUtil.getSheetByIndex(_dbObject);
                        if (_celldata != null && _celldata.size() > 0) {
                            int _total = _celldata.size();
                            for (int y = 0; y < _total; y++) {
                                DBObject _b = (DBObject) _celldata.get(y);
                                if (_b.get("r").toString().equals(r + "") && _b.get("c").toString().equals(c + "")) {
                                    _b.put("v", v);
                                    _position = y;
                                    break;
                                }
                            }
                        }
                        if (_position == -1) {
                            _celldata.add(_v);
                        }
                    } else {
                        //假定页面提交不存在重复数据，不存在的添加
                        BasicDBList _celldata = JfGridFileUtil.getSheetByIndex(_dbObject);
                        _celldata.add(_v);
                    }
                } else {
                    //删除
                    if (isExists) {
                        //存在的才处理
                        int _position = -1;//所在位置，更新使用
                        BasicDBList _celldata = JfGridFileUtil.getSheetByIndex(_dbObject);
                        if (_celldata != null && _celldata.size() > 0) {
                            int _total = _celldata.size();
                            for (int y = 0; y < _total; y++) {
                                DBObject _b = (DBObject) _celldata.get(y);
                                if (_b.get("r").toString().equals(r + "") && _b.get("c").toString().equals(c + "")) {
                                    _position = y;
                                    break;
                                }
                            }
                        }
                        if (_position != -1) {
                            _celldata.remove(_position);
                        }
                    }
                }
            }

            //postgres处理
            List<PgGridDataModel> models = new ArrayList<>();
            List<String> block_ids = new ArrayList<>();
            if (_existsBlock.size() > 0) {
                for (String _block : _existsBlock.keySet()) {
                    block_ids.add(_block);
                    PgGridDataModel model = new PgGridDataModel();
                    DBObject _bson = _existsBlock.get(_block);
                    BasicDBList _celldata = JfGridFileUtil.getSheetByIndex(_bson);
                    DBObject json_data = new BasicDBObject();
                    json_data.put("celldata", _celldata);
                    model.setJson_data(json_data);
                    model.setBlock_id(_block);
                    model.setIndex(_bson.get("index").toString());
                    model.setList_id(gridKey);
                    model.setStatus(0);
                    model.setIs_delete(0);
                    models.add(model);
                }
            }
            if (models.size() > 0) {
                boolean _result = pgGridFileDao.batchUpdateCellDataValue(block_ids, models);
                if (!_result) {
                    return "更新失败";
                }
            }

            if (_noExistsBlock.size() > 0) {
                for (DBObject _d : _noExistsBlock.values()) {
                    PgGridDataModel model = new PgGridDataModel();
                    model.setBlock_id(_d.get("block_id").toString());
                    model.setIndex(_d.get("index").toString());
                    model.setList_id(_d.get("list_id").toString());
                    DBObject DB = (DBObject) _d.get("celldata");
                    DBObject json_data = new BasicDBObject();
                    json_data.put("celldata", DB);
                    model.setJson_data(json_data);
                    model.setStatus(0);
                    model.setIs_delete(0);
                    models.add(model);
                }

                String _result = pgGridFileDao.InsertIntoBatch(models);
                if (_result == null) {
                    return "更新失败";
                }
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.1	单元格操作v
     *
     * @param gridKey
     * @param bson
     * @return
     */
    private String Operation_v(String gridKey, DBObject bson) {
        if (GzipHandle.runGzip) {
            //压缩处理
            return "";
        }

        //不压缩处理
        try {
            log.info("start---Operation_v" + bson.toString());
            String i = bson.get("i").toString();//	当前sheet的index值
            Integer r = Integer.parseInt(bson.get("r").toString());//	单元格的行号
            Integer c = Integer.parseInt(bson.get("c").toString());//	单元格的列号
            Object v = bson.get("v");  //	单元格的值 v=null 删除单元格

            //判断第一个块是否存在
            Integer isHave = pgGridFileDao.getFirstBlockByGridKey(gridKey, i);
            log.info("isHave---Operation_v" + isHave);
            if (isHave == null || isHave == 0) {
                return "list_id=" + gridKey + ",index=" + i + "的sheet不存在";
            }

            //获取数据所在块的编号
            String block_id = JfGridConfigModel.getRange(r, c);
            log.info("block_id---Operation_v" + block_id);
            //1、先获取原数据（直接获取到某个sheet）
            DBObject _dbObject = pgGridFileDao.getCelldataByGridKey(gridKey, i, block_id);
            if (_dbObject == null) {
                //return "list_id="+gridKey+",index="+i+"的sheet不存在";
                //return "list_id="+gridKey+"的数据表格不存在";
                //不存在，需要创建一个块
                if (v != null) {
                    //必须有值
                    //单元格
                    DBObject _v = new BasicDBObject();
                    _v.put("r", r);
                    _v.put("c", c);
                    _v.put("v", v);
                    //集合
                    BasicDBList _celldata = new BasicDBList();
                    _celldata.add(_v);
                    //文档
                    DBObject db = new BasicDBObject();
                    db.put("celldata", _celldata);
                    PgGridDataModel pg = new PgGridDataModel();
                    pg.setBlock_id(block_id);
                    pg.setIndex(i);
                    pg.setList_id(gridKey);
                    pg.setJson_data(db);
                    pg.setStatus(0);
                    pg.setIs_delete(0);
                    //新增操作
                    String _mongodbKey = pgGridFileDao.insert(pg);
                    if (_mongodbKey == null) {
                        return "更新失败";
                    }
                }
            } else {
                //已经存在块的情况
                //3、查询集合是否存在此数据
                int _position = -1;//所在位置，更新使用
                DBObject _sourceDb = null;//原始对象 (删除时使用)
                //用行、列查询
                //_dbObject=jfGridFileGetService.getCelldataByGridKey(gridKey,i,r,c);
                if (_dbObject != null) {
                    //找到数据，找出位置
                    //BasicDBList _celldata=JfGridFileUtil.getSheetByIndex(_dbObject,i);
                    BasicDBList _celldata = JfGridFileUtil.getSheetByIndex(_dbObject);
                    if (_celldata != null && _celldata.size() > 0) {
                        int _total = _celldata.size();
                        for (int x = 0; x < _total; x++) {
                            DBObject _b = (DBObject) _celldata.get(x);
                            if (_b.get("r").toString().equals(r + "") && _b.get("c").toString().equals(c + "")) {
                                _position = x;
                                _sourceDb = _b;
                                break;
                            }
                        }
                    }
                }

                Query query = new Query();
                //query.addCriteria(Criteria.where("_id").is(gridKey));
                query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(block_id));
                boolean _result = false;
                if (v == null) {
                    if (_sourceDb != null) {
                        //当前设置为null，则表示删除
                        //update.pull("jfgridfile."+_sheetPosition+".celldata",_sourceDb);
                        String keyName = "celldata," + _position;
                        _result = pgGridFileDao.rmCellDataValue(query, keyName);
                        if (!_result) {
                            return "更新失败";
                        }
                    }
                } else {
                    if (_position != -1) {
                        //找到，更新
                        //update.set("jfgridfile."+_sheetPosition+".celldata."+_position+".v",v);
                        //update.set("celldata."+_position+".v",v);
                        //对jsonb某个元素具体数据具体更新
                        String pos = String.valueOf(_position);
                        _result = pgGridFileDao.updateCellDataListValue(query, "celldata," + pos + ",v", null, v);
                    } else {
                        //没找到
                        DBObject _db = new BasicDBObject();
                        _db.put("r", r);
                        _db.put("c", c);
                        _db.put("v", v);
                        //update.push("jfgridfile."+_sheetPosition+".celldata",_db);
                        //update.push("celldata",_db);
                        _result = pgGridFileDao.updateJsonbForElementInsert(query, "celldata", _db, 0);
                    }
                    if (!_result) {
                        return "更新失败";
                    }
                }

            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.10.1	表格名称 修改 数据库
     *
     * @param gridKey
     * @param bson
     * @return
     */
    public String Operation_na(String gridKey, DBObject bson) {
        try {
            String v = null;// 	表格的名称
            if (bson.containsField("v")) {
                v = bson.get("v").toString().trim();
            }
            LuckySheetGridModel model = new LuckySheetGridModel();
            //model.setMongodbkey(gridKey.toString());
            model.setList_id(gridKey);
            model.setGrid_name(v);

            //更新文件名
            int i = 1;
            if (i == 0) {
                return "改名失败";
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 3.10.2	缩略图    修改数据库 与 postgre
     *
     * @param gridKey
     * @param bson
     * @return
     */
    public String Operation_thumb(String gridKey, DBObject bson) {
        try {
            log.info("Operation_thumb----start");
            String curindex = null;//当前表格默认打开的sheet
            String img = null;//	当前表格的缩略图，为base64字符串
            if (bson.containsField("img")) {
                img = bson.get("img").toString();
            }
            log.info("Operation_thumb----img" + img);
            if (bson.containsField("curindex")) {
                curindex = bson.get("curindex").toString();
            }
            log.info("Operation_thumb----curindex" + curindex);
            if (curindex == null || img == null) {
                return "参数错误";
            }

            //1、先获取原数据
            List<DBObject> _dbObject = pgGridFileDao.getBlocksByGridKey(gridKey, false);
            if (_dbObject == null) {
                return "gridKey=" + gridKey + "的数据表格不存在";
            }
            log.info("getSheetPositionByIndex--start");
            //2、数据所在的sheet的序号
            Integer _sheetPosition = JfGridFileUtil.getSheetPositionByIndex(_dbObject, curindex);
            if (_sheetPosition == null) {
                return "index=" + curindex + "的sheet不存在";
            }


            //设置全部status=0
            PgGridDataModel model = new PgGridDataModel();
            model.setBlock_id(JfGridConfigModel.FirstBlockID);
            model.setIndex(curindex);
            model.setList_id(gridKey);
            boolean _result = pgGridFileDao.updateDataStatus(model);
            if (!_result) {
                return "更新失败";
            }

            LuckySheetGridModel models = new LuckySheetGridModel();
            //model.setMongodbkey(gridKey.toString());
            models.setList_id(gridKey);
            models.setGrid_thumb(img.getBytes("UTF-8"));
            log.info("Operation_thumb---updateGridThumbByMongodbKey--start");

            //更新缩略图逻辑
            int i = 1;
            if (i == 0) {
                return "更新缩略图失败";
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }


    /**
     * 3.9	图表操作
     *
     * @param gridKey2
     * @param bson
     * @return
     */
    public String Operation_c(String gridKey2, DBObject bson) {
        return Operation_c2(gridKey2, bson);
    }

    /**
     * 图表操作
     *
     * @param gridKey2
     * @param bson
     * @return
     */
    private String Operation_c2(String gridKey2, DBObject bson) {
        try {
            String i = bson.get("i").toString();//	当前sheet的index值
            String cid = bson.get("cid").toString();//	Chart图表的id
            String op = bson.get("op").toString();//操作选项，有add、xy、wh、del、update。
            DBObject v = null;
            if (bson.containsField("v")) {
                v = (DBObject) bson.get("v");
            }

            //1、先获取原数据（第一块）
            DBObject _dbObject = pgGridFileDao.getChartByGridKey(gridKey2, i);
            if (_dbObject == null) {
                return "list_id=" + gridKey2 + ",index=" + i + "的sheet不存在";
                //return "gridKey="+gridKey+"的数据表格不存在";
            }

            //更新操作（第一块）
            Query query = new Query();
            query.addCriteria(Criteria.where("list_id").is(gridKey2).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));

            //从文档中获取图表对象
            DBObject chart = JfGridFileUtil.getObjectByIndex(_dbObject, "chart");
            boolean _result = false;
            if (chart == null) {
                //不存在 (只处理添加)
                if (op.equals("add")) {
                    _result = pgGridFileDao.updateJsonbForInsertNull(query, "chart", v, 0, "\"chart\":[]");
                }
            } else {
                //存在
                if (op.equals("add")) {
                    _result = pgGridFileDao.updateJsonbForElementInsert(query, "chart", v, 0);
                } else {
                    if (chart instanceof List) {
                        List<DBObject> _list = (List<DBObject>) chart;
                        if (_list != null && _list.size() > 0) {
                            //找出位置
                            int pos = -1;
                            for (int x = 0; x < _list.size(); x++) {
                                if (_list.get(x).containsField("chart_id")) {
                                    if (_list.get(x).get("chart_id").equals(cid)) {
                                        pos = x;
                                        break;
                                    }
                                }
                            }
                            if (pos > -1) {
                                if (op.equals("xy") || op.equals("wh") || op.equals("update")) {
                                    //xy 移动  wh 缩放  更新 update
                                    //按照v中的key循环更新jfgridfile[i].chart[v.key1] = v.value1
                                    if (v != null) {
                                        DBObject _s = _list.get(pos);
                                        _s.putAll(v);
                                        _result = pgGridFileDao.updateCellDataListValue(query, "chart", String.valueOf(pos), _s);
                                    }
                                } else if (op.equals("del")) {
                                    _list.remove(pos);
                                    _result = pgGridFileDao.updateCellDataListValue(query, "chart", null, chart);
                                }
                            }
                        }
                    }

                }
            }
            if (!_result) {
                return "更新失败";
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }


    public void Operation_mv(String gridKey, DBObject bson) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String i = bson.get("i").toString();//	当前sheet的index值
                    String v = bson.get("v").toString();  //	单元格的值 v=null 删除单元格
                    log.info("Operation_mv---v" + v);
                    DBObject db = (DBObject) JSON.parse(v);
                    //更新操作（第一块）
                    Query query = new Query();
                    query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));
                    boolean _result = pgGridFileDao.updateCellDataListValue(query, "jfgird_select_save", null, db);
                    if (!_result) {
                        log.info("更新失败");
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage());
                } finally {

                }
            }
        }).start();
    }


    //3.1	批量单元格操作v
    private String Operation_rv(String gridKey, DBObject bson) {
        if (GzipHandle.runGzip) {

            return "";
        }
        //不压缩处理
        try {
            log.info("start---Operation_bv" + bson.toString());
            String i = bson.get("i").toString();//	当前sheet的index值
            DBObject range = JfGridFileUtil.getObjectByIndex(bson, "range");
            List columns = (List) range.get("column");
            List rows = (List) range.get("row");
            Integer r = Integer.parseInt(rows.get(0).toString());//	单元格的行号
            Integer c = Integer.parseInt(columns.get(0).toString());//	单元格的列号
            Object all = bson.get("v");  //	单元格的值 v=null 删除单元格

            //判断第一个块是否存在
            Integer isHave = pgGridFileDao.getFirstBlockByGridKey(gridKey, i);
            log.info("isHave---Operation_bv" + isHave);
            if (isHave == null || isHave == 0) {
                log.error("list_id=" + gridKey + ",index=" + i + "的sheet不存在");
            }
            //已存在的块
            HashMap<String, DBObject> _existsBlock = new HashMap<String, DBObject>();
            //不存在的块
            HashMap<String, DBObject> _noExistsBlock = new HashMap<String, DBObject>();
            List<ArrayList> data = (List<ArrayList>) all;
            for (ArrayList arrayList : data) {
                int cl = c;
                for (Object v : arrayList) {
                    //获取数据所在块的编号
                    String block_id = JfGridConfigModel.getRange(r, cl);
                    boolean isExists = false;
                    DBObject _dbObject = null;
                    if (_existsBlock.containsKey(block_id)) {
                        //mongodb已存在的，处理成执行语句
                        isExists = true;
                        _dbObject = _existsBlock.get(block_id);
                    } else if (_noExistsBlock.containsKey(block_id)) {
                        //mongodb不存在的
                        _dbObject = _noExistsBlock.get(block_id);
                    } else {
                        //已有的中不存在
                        //1、先获取原数据（直接获取到某个sheet）
                        _dbObject = pgGridFileDao.getCelldataByGridKey(gridKey, i, block_id);
                        if (_dbObject == null) {
                            //不存在新建一块处理
                            //集合
                            BasicDBList _celldata = new BasicDBList();
                            //文档
                            DBObject db = new BasicDBObject();
                            db.put("celldata", _celldata);
                            db.put("block_id", block_id);//当前sheet的块编号
                            db.put("index", i); //表格sheet的编号
                            db.put("list_id", gridKey);//表格编号
                            _noExistsBlock.put(block_id, db);
                            _dbObject = db;
                        } else {
                            //已存在
                            isExists = true;
                            _existsBlock.put(block_id, _dbObject);
                        }

                    }
                    //单元格处理，这一步对象已经获取
                    if (v != null) {
                        //修改/添加

                        DBObject _v = new BasicDBObject();
                        _v.put("r", r);
                        _v.put("c", cl);
                        _v.put("v", v);

                        if (isExists) {
                            //已存在的
                            int _position = -1;//所在位置，更新使用
                            BasicDBList _celldata = JfGridFileUtil.getSheetByIndex(_dbObject);
                            if (_celldata != null && _celldata.size() > 0) {
                                int _total = _celldata.size();
                                for (int y = 0; y < _total; y++) {
                                    DBObject _b = (DBObject) _celldata.get(y);
                                    if (_b.get("r").toString().equals(r + "") && _b.get("c").toString().equals(cl + "")) {
                                        _b.put("v", v);
                                        _position = y;
                                        break;
                                    }
                                }
                            }
                            if (_position == -1) {
                                _celldata.add(_v);
                            }
                        } else {
                            //假定页面提交不存在重复数据，不存在的添加
                            BasicDBList _celldata = JfGridFileUtil.getSheetByIndex(_dbObject);
                            _celldata.add(_v);
                        }
                    } else {
                        //删除
                        if (isExists) {
                            //存在的才处理
                            int _position = -1;//所在位置，更新使用
                            BasicDBList _celldata = JfGridFileUtil.getSheetByIndex(_dbObject);
                            if (_celldata != null && _celldata.size() > 0) {
                                int _total = _celldata.size();
                                for (int y = 0; y < _total; y++) {
                                    DBObject _b = (DBObject) _celldata.get(y);
                                    if (_b.get("r").toString().equals(r + "") && _b.get("c").toString().equals(cl + "")) {
                                        _position = y;
                                        break;
                                    }
                                }
                            }
                            if (_position != -1) {
                                _celldata.remove(_position);
                            }
                        }
                    }
                    cl++;
                }
                r++;
            }

            //处理
            log.info("_existsBlock--" + _existsBlock.size() + ",_noExistsBlock:" + _noExistsBlock.size());
            List<PgGridDataModel> models = new ArrayList<>();
            List<String> block_ids = new ArrayList<>();
            if (_existsBlock.size() > 0) {
                for (String _block : _existsBlock.keySet()) {
                    block_ids.add(_block);
                    PgGridDataModel model = new PgGridDataModel();
                    DBObject _bson = _existsBlock.get(_block);
                    BasicDBList _celldata = JfGridFileUtil.getSheetByIndex(_bson);
                    model.setBlock_id(_block);
                    model.setIndex(i);
                    model.setList_id(gridKey);
                    DBObject json_data = new BasicDBObject();
                    json_data.put("celldata", _celldata);
                    model.setJson_data(json_data);
                    model.setStatus(0);
                    model.setIs_delete(0);
                    models.add(model);
                }
            }
            if (models.size() > 0) {
                boolean _result = pgGridFileDao.batchUpdateCellDataValue(block_ids, models);
                if (!_result) {
                    log.error("更新失败");
                }
            }
            List<PgGridDataModel> isModels = new ArrayList<>();
            if (_noExistsBlock.size() > 0) {
                for (DBObject _d : _noExistsBlock.values()) {
                    PgGridDataModel model = new PgGridDataModel();
                    model.setBlock_id(_d.get("block_id").toString());
                    model.setIndex(_d.get("index").toString());
                    model.setList_id(_d.get("list_id").toString());
                    DBObject DB = (DBObject) _d.get("celldata");
                    DBObject json_data = new BasicDBObject();
                    json_data.put("celldata", DB);
                    model.setJson_data(json_data);
                    model.setStatus(0);
                    model.setIs_delete(0);
                    isModels.add(model);
                }
                DBObject db = pgGridFileDao.getCelldataByGridKey(gridKey, i, "fblock");
                Integer col = Integer.valueOf(db.get("column").toString());
                Integer row = Integer.valueOf(db.get("row").toString());
                if (r < row && c < col) {
                    //修改或插入的行列低于原行列不修改原行列信息
                } else {
                    Integer updateRow = Math.max(r, row);
                    Integer updateCol = Math.max(c, col);
                    Query query = new Query();
                    //query.addCriteria(Criteria.where("list_id").is(gridKey));
                    query.addCriteria(Criteria.where("list_id").is(gridKey).and("index").is(i).and("block_id").is(JfGridConfigModel.FirstBlockID));
                    DBObject b = new BasicDBObject();
                    b.put("row", updateRow);
                    b.put("column", updateCol);
                    boolean result = pgGridFileDao.updateJsonbDataForKeys(query, b);
                    log.info("修改行列数据结果：" + result);
                }
                String _result = pgGridFileDao.InsertIntoBatch(isModels);
                if (_result == null) {
                    log.error("更新失败");
                }
            }
            log.info("修改行列数据结果--end");
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return "";
    }

    //3.7.3	非物理删除恢复shre
    private String Operation_shre(String gridKey, DBObject bson) {
        try {
            String reIndex = null;//	需要删除的sheet索引
            if (bson.containsField("v")) {
                DBObject _v = (DBObject) bson.get("v");
                if (_v.containsField("reIndex")) {
                    reIndex = _v.get("reIndex").toString();
                }
            }
            if (reIndex == null) {
                return "参数错误";
            }

            //1、先获取原数据
            List<DBObject> _dbObject = pgGridFileDao.getBlocksByGridKey(gridKey, false);
            if (_dbObject == null) {
                return "gridKey=" + gridKey + "的数据表格不存在";
            }
            //2、数据所在的sheet的序号
            //Integer _sheetPosition=JfGridFileUtil.getSheetPositionByIndex(_dbObject,deleIndex);
            //if(_sheetPosition==null)
            PgGridDataModel model = new PgGridDataModel();
            model.setIndex(reIndex);
            model.setList_id(gridKey);
            model.setIs_delete(0);
            boolean result = pgGridFileDao.updateDataForReDel(model);
            if (!result) {
                return "更新失败";
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    /**
     * 批量单元格操作
     *
     * @param gridKey
     * @param bson
     * @return
     */
    public String getIndexRvForThread(String gridKey, DBObject bson) {
        log.info("getIndexForRvByThread--start");
        String i = bson.get("i").toString();//	当前sheet的index值
        String key = gridKey + i;
        gridFileRedisCacheService.raddDbContent(key, bson);
        return "";
    }

    public String updateRvDbContent(String gridKey, DBObject bson, String key) {
        List<DBObject> bsons = gridFileRedisCacheService.rgetDbDataContent(key);
        loadRvMsgForLock(gridKey, bsons, key);
        return "";
    }

    private void loadRvMsgForLock(String gridKey, List<DBObject> bsons, String key) {
        RedisLock redisLock = new RedisLock(redisTemplate, key);
        try {
            if (redisLock.lock()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (DBObject dbObject : bsons) {
                            Operation_rv(gridKey, dbObject);
                        }
                    }
                }).start();
            } else {
                Thread.sleep(100);
                loadRvMsgForLock(gridKey, bsons, key);
            }
        } catch (Exception e) {

        } finally {
            redisLock.unlock();
        }
    }




    /**
     * 初始化测试数据
     */
    public void initTestData(){
        List<String> listName=new ArrayList<String>(2){{
            add("1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2xc");
            add("1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3");
        }};
        initTestData(listName);
    }
    public void initTestData(List<String> listName){
        //int delCount=pgGridFileDao.deleteAll();
        //log.info("del row:{}",delCount);
        int[] delCount=pgGridFileDao.delete(listName);
        log.info("del row:{}",delCount);
        List<PgGridDataModel> models=new ArrayList<>(6);
//        List<String> listName=new ArrayList<String>(2){{
//            add("1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2xc");
//            add("1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3");
//        }};
        for(String n:listName) {
            for (int x = 0; x < 3; x++) {
                if(x==0){
                    models.add(strToModel(n, (x+1)+"",1,x));
                }else {
                    models.add(strToModel(n, (x+1)+"",0,x));
                }
            }
        }
        String result=insert(models);
        log.info(result);
    }
    private PgGridDataModel strToModel(String list_id,String index,int status,int order){
        String strSheet="{\"row\":84,\"name\":\"reSheetName\",\"chart\":[],\"color\":\"\",\"index\":\"reIndex\",\"order\":reOrder,\"column\":60,\"config\":{},\"status\":reStatus,\"celldata\":[],\"ch_width\":4748,\"rowsplit\":[],\"rh_height\":1790,\"scrollTop\":0,\"scrollLeft\":0,\"visibledatarow\":[],\"visibledatacolumn\":[],\"jfgird_select_save\":[],\"jfgrid_selection_range\":{}}";
        strSheet=strSheet.replace("reSheetName","Sheet"+index).replace("reIndex",index).replace("reOrder",order+"").replace("reStatus",status+"");

        DBObject bson=(DBObject) JSON.parse(strSheet);
        PgGridDataModel model=new PgGridDataModel();
        model.setBlock_id("fblock");
        model.setIndex(index);
        model.setIs_delete(0);
        model.setJson_data(bson);
        model.setStatus(status);
        model.setOrder(order);
        model.setList_id(list_id);
        return model;
    }

}
