package com.xc.luckysheet.service;

import com.xc.luckysheet.controller.JfGridFileController;
import com.xc.luckysheet.entity.JfGridConfigModel;
import com.xc.luckysheet.redisserver.RedisMessagePublish;
import com.xc.luckysheet.websocket.MyWebSocketHandler;
import com.xc.luckysheet.websocket.WebSocketConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
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
    @Value("${pgSetUp}")
    public void setPgSetUp(String pgSetUp){
        JfGridFileController.pgSetUp=pgSetUp;
        MyWebSocketHandler.pgSetUp=pgSetUp;
    }
    @Value("${servertype}")
    public void setServerType(String servertype){
        WebSocketConfig.servertype=servertype;
    }

}
