package com.xc.common.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class RedisCacheService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 获取值
     * @param name
     * @param key
     * @return
     */
    public Object getMapCache(String name,String key){
        log.info("获取Map {},{}",name,key);
        return redisTemplate.opsForHash().get(name,key);
    }

    /**
     * 保存值
     * @param key
     * @param name
     * @param value
     */
    public void setMapCache(String name,String key,Object value){
        log.info("保存Map {},{},{}",name,key,value);
        redisTemplate.opsForHash().put(name,key,value);
    }
    /**
     * 删除值
     * @param name
     * @param key
     */
    public void delMapCache(String name,String key){
        log.info("删除Map {},{}",name,key);
        redisTemplate.opsForHash().delete(name,key);
    }

    /**
     * 数据更新时间 默认30分钟
     * @param key
     * @param val
     */
    public void addCache(String key,Object val){
        addCache(key,val,30);
    }
    /**
     * 数据更新时间
     * @param key
     * @param val
     * @param time 过期时间 以分钟为单位
     */
    public void addCache(String key,Object val,long time) {
        ValueOperations opsForValue=redisTemplate.opsForValue();
        opsForValue.set(key,val,time,TimeUnit.MINUTES);
    }

    /**
     * 根据key查询对应缓存值
     * @param key
     * @return
     */
    public Object getCache(String key){
        return redisTemplate.opsForValue().get(key);
    }



    /**
     * 根据前缀批量删除redisKey
     * @param prex 前缀
     * @return 删除条数
     */
    public int deleteCaches(String prex){
        String key=prex+"*";
        Set<String> keys = redisTemplate.keys(key);
        if(keys!=null && keys.size()>0){
            redisTemplate.delete(keys);
            return keys.size();
        }
        return 0;
    }

    /**
     * 删除单条
     * @param key 删除的key
     * @return
     */
    public int deleteCacheByKey(String key){
        Set<String> keys = redisTemplate.keys(key);
        if(keys!=null && keys.size()>0){
            redisTemplate.delete(keys);
            return keys.size();
        }
        return 0;
    }

    /**
     * 按数量获取
     * @param key
     * @param size
     * @return
     */
    public List getListBySize(String key, long size){
        List liststr=redisTemplate.opsForList().range(key, 0, size);
        return liststr;
    }
    /**
     * 从头部删除指定数量的key
     * @param key
     * @param size
     */
    public void delList(String key,int size){
        for(int x=0;x<size;x++){
            try{
                redisTemplate.opsForList().leftPop(key);
            }catch(Exception ex){

            }
        }
    }
    /**
     * 添加队列(位置尾部)
     * @param key
     * @param val
     * @return  返回val所在队列的序号
     */
    public long addList(String key,Object val){
        return redisTemplate.opsForList().rightPush(key, val);
    }
}
