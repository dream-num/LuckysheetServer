package com.xc.luckysheet.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


/**
 * @author Administrator
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    public static String servertype;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //1.注册WebSocket
        //设置websocket的地址
        String websocket_url = "/websocket/luckysheet";
        //注册Handler
        registry.addHandler(getMyWebSocketHandler(), websocket_url).
                //注册Interceptor
                addInterceptors(getMyWebSocketInterceptor())
                //配置*代表允许所有的ip进行调用
                .setAllowedOrigins("*");

        //2.注册SockJS，提供SockJS支持(主要是兼容ie8)
        //设置sockjs的地址
        String sockjs_url = "/sockjs/luckysheet";
        //注册Handler
        registry.addHandler(getMyWebSocketHandler(), sockjs_url).
                //注册Interceptor
                addInterceptors(getMyWebSocketInterceptor())
                //配置*代表允许所有的ip进行调用
                .setAllowedOrigins("*").withSockJS();

        //获取系统ip
        log.info("registerWebSocketHandlers:"+servertype);
        if(servertype!=null && servertype.equals("weblogic")){
            MyWebSocketHandler.ipAndPort= IpAndPortUtil.getIpWeblogic();
        }else{
            MyWebSocketHandler.ipAndPort= IpAndPortUtil.getIpAddressAndPort();
        }

    }

    @Bean
    public MyWebSocketHandler getMyWebSocketHandler(){
        return new MyWebSocketHandler();
    }

    @Bean
    public MyWebSocketInterceptor getMyWebSocketInterceptor(){
        return new MyWebSocketInterceptor();
    }

}
