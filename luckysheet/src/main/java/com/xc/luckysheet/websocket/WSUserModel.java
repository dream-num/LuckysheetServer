package com.xc.luckysheet.websocket;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * @author Administrator
 */
@Data
public class WSUserModel {

    /**
     * 用户key
     */
    public static final String USER_TOKEN="t";
    /**
     * 文档key
     */
    public static final String USER_GRIDKEY="g";

    /**
     * ws-session
     */
    private WebSocketSession ws;
    /**
     * session id
     */
    private String id;
    /**
     * 接收token
     */
    private String token;
    /**
     * 文档id
     */
    private String gridKey;
    /**
     * 连接的用户名
     */
    private String userName;

    public WSUserModel(WebSocketSession ws){
        this.id=ws.getId();
        this.userName="testUser-"+ws.getId();
        if(ws.getAttributes().get(USER_TOKEN)!=null){
            this.token=ws.getAttributes().get(USER_TOKEN).toString();
        }else{
            this.token="i";
        }
        if(ws.getAttributes().get(USER_GRIDKEY)!=null){
            this.gridKey=ws.getAttributes().get(USER_GRIDKEY).toString();
        }else{
            this.gridKey="1";
        }

        this.ws=ws;
    }


    /**
     * 外层key gridKey（文档id），内层key session ID（用户id）
     * @param maps
     * @param wm
     */
    public static void webSocketMapAdd(Hashtable<String,Hashtable<String,WSUserModel>> maps,WSUserModel wm){
        if(maps.containsKey(wm.getGridKey())){
            maps.get(wm.getGridKey()).put(wm.getId(),wm);
        }else{
            Hashtable<String,WSUserModel> _map=new Hashtable<String,WSUserModel>();
            _map.put(wm.getId(),wm);
            maps.put(wm.getGridKey(),_map);
        }
    }
    public static void webSocketMapRemove(Hashtable<String,Hashtable<String,WSUserModel>> maps, WSUserModel wm){
        if(maps.containsKey(wm.getGridKey())){
            if(maps.get(wm.getGridKey())!=null){
                Hashtable<String,WSUserModel> _map=maps.get(wm.getGridKey());
                if(_map!=null && _map.containsKey(wm.getId())){
                    _map.remove(wm.getId());
                }
            }
        }
    }

}
