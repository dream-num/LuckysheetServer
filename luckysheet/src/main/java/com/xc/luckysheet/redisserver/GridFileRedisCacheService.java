package com.xc.luckysheet.redisserver;

import com.mongodb.DBObject;
import com.xc.common.config.redis.RedisCacheService;
import com.xc.common.config.redis.RedisQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author Administrator
 */
@Slf4j
@Service
public class GridFileRedisCacheService {
    @Autowired
    private RedisCacheService redisCache;
    @Autowired
    private RedisQueueService redisQueueService;


    /**
     * 批量更新数据
     */
    private String dbdata_content="lk:dbdata:";

    /**
     * 收集指令信息
     */
    private String hand_flag_content="lk:upflag:";

    /**
     * 收集指令信息内容
     */
    private String qk_handle_content="lk:handler:";


    public List<DBObject> rgetDbDataContent(String key){
        try{
            String redisKey=dbdata_content+key;
            List<DBObject> lists=redisCache.getListBySize(redisKey, -1);
            if(lists!=null && lists.size()>0){
                //从redis中删除
                redisCache.delList(redisKey, lists.size());
                return lists;
            }
        }catch(Exception ex){
            return null;
        }
        return null;
    }


    /**
     *
     * @param key
     * @param db
     */
    public void raddDbContent(String key,DBObject db){
        String redisKey=dbdata_content+key;
        redisCache.addList(redisKey, db);
    }


    /**
     * 存入启用存储指令信息
     * @param key
     * @param val
     */
    public void raddFlagContent(String key, Object val) {
        String redisKey = hand_flag_content + key;
        log.info("raddFlagContent---redisKey="+redisKey+"val="+val);
        redisCache.addCache(redisKey, val, 240);
    }

    /**
     * 根据key 获得email验证码信息
     * @param key
     */
    public Boolean rgetFlagContent(String key) {
        Boolean flag=false;
        try{
            String redisKey = hand_flag_content + key;
            log.info("rgetFlagContent---redisKey="+redisKey);
            flag=(Boolean) redisCache.getCache(redisKey);
        }catch (Exception e) {
            // TODO: handle exception
        }
        return flag;
    }


    /**
     * 获取数据
     * @param key
     * @return
     */
    public List<String> rgetHandlerContent(String key){
        try{
            String redisKey=qk_handle_content+key;
            //多节点使用
            List<String> lists=redisQueueService.popList(redisKey,String.class,500);
            return lists;
            //单节点使用
//            List<String> lists=redisCache.getListBySize(redisKey, -1);
//            if(lists!=null && lists.size()>0){
//                //从redis中删除
//                redisCache.delList(redisKey, lists.size());
//                return lists;
//            }
        }catch(Exception ex){
            return null;
        }
    }

}
