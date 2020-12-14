package com.xc.luckysheet.redisserver;

import com.google.gson.Gson;
import com.xc.luckysheet.utils.MyStringUtil;
import com.xc.luckysheet.websocket.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 监听
 * @author Administrator
 */
public class RedisMessageListener implements MessageListener {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 订阅者收到消息
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        //JSON.parse(serializerValue.deserialize(message.getBody()).toString())
        RedisSerializer<?> serializerKey = redisTemplate.getKeySerializer();
        RedisSerializer<?> serializerValue = redisTemplate.getValueSerializer();
        Object channel = serializerKey.deserialize(message.getChannel());
        Object body = serializerValue.deserialize(message.getBody());
        //System.out.println("主题: " + channel);
        //System.out.println("消息内容: " + String.valueOf(body));
        RedisMessageModel bson1=new Gson().fromJson(body.toString(),RedisMessageModel.class);
        System.out.println("得到Redis推送消息："+MyStringUtil.getStringShow(bson1.toString()));
        MyWebSocketHandler.sendMessageToUserByRedis(bson1);
    }

    public RedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
