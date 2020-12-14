package com.xc.luckysheet.redisserver;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
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
    public RedisMessageModel(DBObject jsonObject){
        if(jsonObject!=null) {
            if (jsonObject.containsField("ipandport")) {
                this.ipandport = jsonObject.get("ipandport").toString();
            }
            if (jsonObject.containsField("gridkey")) {
                this.gridkey = jsonObject.get("gridkey").toString();
            }
            if (jsonObject.containsField("content")) {
                this.content = jsonObject.get("content").toString();
            }
        }
    }

    public DBObject toDBObject(){
        DBObject jsonObject=new BasicDBObject();
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
