package com.xc.luckysheet.service;


import com.xc.luckysheet.JfGridConfigModel;
import com.xc.luckysheet.redisserver.RedisMessagePublish;
import com.xc.luckysheet.websocket.WebSocketConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


/**
 * @author Administrator
 */
@Slf4j
@Configuration
@Service
public class ConfigerService {

    @Value("${redis.channel}")
    public void setRedisChannel(String path){
        RedisMessagePublish.channel=path;
    }
    @Value("${row_size}")
    public void setRowSize(Integer rowSize){
        JfGridConfigModel.row_size=rowSize;
    }
    @Value("${col_size}")
    public void setColSize(Integer colSize){
        JfGridConfigModel.col_size=colSize;
    }
    @Value("${servertype}")
    public void setServerType(String servertype){
        WebSocketConfig.servertype=servertype;
    }

}
