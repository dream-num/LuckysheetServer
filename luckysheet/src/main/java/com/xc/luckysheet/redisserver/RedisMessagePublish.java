package com.xc.luckysheet.redisserver;

import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


/**
 * 发布
 * @author Administrator
 */
@Slf4j
@Data
@Service
public class RedisMessagePublish {
    /**
     * 管道
     */
    public static String channel;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布者
     * @param redisMessageModel  消息内容
     * @return
     */
    public boolean publishMessage(RedisMessageModel redisMessageModel){
        try{
            redisTemplate.convertAndSend(channel,new Gson().toJson(redisMessageModel));
            return true;
        }catch (Exception ex){
            log.error("publishMessage Error:{}",ex);
            return false;
        }
    }

}
