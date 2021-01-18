package com.xc.luckysheet.redisserver;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * redis消息
 * @author Administrator
 */
@Data
public class RedisMessageModel {

    /**
     * 节点端口与ip （或者其他唯一标示）
     */
    private String ipandport;
    /**
     * 文档id
     */
    private String gridkey;
    /**
     * 内容
     */
    private String content;

    public RedisMessageModel(String _ipandport,String _gridkey,String _content){
        this.ipandport=_ipandport;
        this.gridkey=_gridkey;
        this.content=_content;
    }
    public RedisMessageModel(JSONObject jsonObject){
        if(jsonObject!=null) {
            if (jsonObject.containsKey("ipandport")) {
                this.ipandport = jsonObject.getString("ipandport");
            }
            if (jsonObject.containsKey("gridkey")) {
                this.gridkey = jsonObject.getString("gridkey");
            }
            if (jsonObject.containsKey("content")) {
                this.content = jsonObject.getString("content");
            }
        }
    }

    public JSONObject toDBObject(){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("ipandport",ipandport);
        jsonObject.put("gridkey",gridkey);
        jsonObject.put("content",content);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "RedisMessageModel{" +
                "ipandport='" + ipandport + '\'' +
                ", gridkey='" + gridkey + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
