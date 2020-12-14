package com.xc.luckysheet.websocket;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import com.xc.luckysheet.postgre.dao.PostgresGridFileDao;
import com.xc.luckysheet.postgre.server.PostgresJfGridUpdateService;
import com.xc.luckysheet.redisserver.GridFileRedisCacheService;
import com.xc.luckysheet.redisserver.RedisLock;
import com.xc.luckysheet.redisserver.RedisMessageModel;
import com.xc.luckysheet.redisserver.RedisMessagePublish;
import com.xc.luckysheet.utils.JSONParse;
import com.xc.luckysheet.utils.MyStringUtil;
import com.xc.luckysheet.utils.MyURLUtil;
import com.xc.luckysheet.utils.Pako_GzipUtils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

/**
 * Socket处理器(包括发送信息，接收信息，信息错误等方法。)
 *
 * 代码中rv，rv_end说明
 * 因为websocket传输大小限制
 * 批量更新范围单元格的时候
 * 一次最多1000个单元格
 * 要求 每次传'rv'  最后一次传他'rv_end'
 * rv_end就是个信号
 * 表示这次范围更新数据全部传输完,它自身这次不带数据过去的
 * @author Administrator
 */
@Slf4j
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private RedisMessagePublish redisMessagePublish;
    @Autowired
    private PostgresJfGridUpdateService pgGridUpdateService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private GridFileRedisCacheService redisService;
    @Autowired
    private PostgresGridFileDao pgGridFileDao;

    /**
     * 先注册一个websocket服务器，将连接上的所有用户放进去
     * 外层key gridKey（文档id），内层key session ID（用户id）
     */
    private static final Hashtable<String, Hashtable<String, WSUserModel>> USER_SOCKET_SESSION_MAP;
    /**
     * 启用pgsql的开关
     */
    public static String pgSetUp = "";
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;

    public static String ipAndPort;

    static {
        USER_SOCKET_SESSION_MAP = new Hashtable<String, Hashtable<String, WSUserModel>>(12);
    }

    /**
     * 前台连接并且注册了账户
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.setTextMessageSizeLimit(2048000);
        session.setBinaryMessageSizeLimit(2048000);
        openConn(session);
    }

    /**
     * 接受消息
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message.getPayloadLength() == 0) {
            //无消息直接返回
            return;
        }
        //返回消息类型type :0：连接成功，1.发送给发送信息用户，2.发送信息给其他用户，3.发送选区位置信息 999、用户连接断开
        Map map = new HashMap<>();
        boolean _b = true;
        boolean s = true;
        ObjectMapper obj = new ObjectMapper();
        WSUserModel wsUserModel = new WSUserModel(session);
        String content = message.getPayload().toString();
        if ("rub".equals(content)) {
            log.info("保持连接状态信息");
        } else {
            log.info("消息解压前：" + MyStringUtil.getStringShow(content));
            String contentReal = Pako_GzipUtils.unCompressToURI(content);
            log.info("消息解压后：" + MyStringUtil.getStringShow(contentReal));
            //content=contentReal;
            DBObject bson = null;
            try {
                bson = (DBObject) JSONParse.parse(contentReal);
            } catch (Exception ex) {
                log.error("json字符串转换错误str:" + JSONObject.toJSONString(contentReal));
                return;
            }
            String _id = wsUserModel.getGridKey();
            String gridKey = MyURLUtil.urlDecode(_id);
            if (bson != null && !StringUtil.isNullOrEmpty(_id)) {
                if (bson.get("t").equals("mv")) {
                    //记录光标位置
                    s = false;
                    map.put("type", 3);
                    map.put("username", wsUserModel.getUserName());
                    map.put("id", "" + wsUserModel.getWs().getId());
                    if ("0".equals(pgSetUp)) {
                        pgGridUpdateService.Operation_mv(gridKey, bson);
                    } else {
                        //其它实现
                    }
                } else if (bson.get("t").equals("rv_end")) {
                    //当前sheet的index值
                    String i = bson.get("i").toString();
                    String key = gridKey + wsUserModel.getWs().getId();
                    key = key + i;
                    pgGridUpdateService.updateRvDbContent(gridKey, bson, key);
                } else if (bson.get("t").equals("rv")) {
                    String key = gridKey + wsUserModel.getWs().getId();
                    pgGridUpdateService.getIndexRvForThread(key, bson);
                } else {
                    //其它操作
                    RedisLock redisLock = new RedisLock(redisTemplate, gridKey);
                    try {
                        if (redisLock.lock()) {
                            String _str = "";
                            if ("0".equals(pgSetUp)) {
                                _str = pgGridUpdateService.handleUpdate(gridKey, bson);
                            } else {
                                //其它实现
                            }

                            if (_str.length() == 0) {

                            } else {
                                log.info("handleUpdate--error:{}" ,_str);
                                _b = false;
                            }
                        } else {
                            log.info("handleUpdate--:redisLock---lock");
                            _b = false;
                        }
                    } catch (Exception e) {
                        log.error("handleUpdate--:redisLock--error:{}",e);
                        _b = false;
                    } finally {
                        redisLock.unlock();
                    }
                }

            } else {
                _b = false;
            }
            Map maps = new HashMap<>();
            //执行其他操作成功后，可调用消息发送给窗口
            String returnMessage = "error";
            if (_b) {
                //表示发送其他共享编辑者收到更新信息（0:更新）
                returnMessage = "success";
                map.put("status", "0");
                maps.put("status", "0");
                map.put("returnMessage", returnMessage);
                map.put("createTime", System.currentTimeMillis());

                //发送消息给本机其他用户
                if (s) {
                    map.put("type", 2);
                }
                map.put("data", contentReal);
                String param = obj.writeValueAsString(map);
                //消息发送到redis
                redisMessagePublish.publishMessage(new RedisMessageModel(ipAndPort, wsUserModel.getGridKey(), param));
                sendMessageToUserByCurrent(wsUserModel, param);
            } else {
                maps.put("status", "1");
                maps.put("data", contentReal);
            }
            //只给发送此信息的用户发送信息
            maps.put("returnMessage", returnMessage);
            maps.put("createTime", System.currentTimeMillis());
            maps.put("type", 1);
            String params = obj.writeValueAsString(maps);
            sendMessageToUser(session, params);

        }
    }

    /**
     * 消息传输错误处理，如果出现错误直接断开连接
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("handleTransportError:{};exception:{}" ,session,exception);
        if (session.isOpen()) {
            session.close();
        }
        closeConn(session, true);
    }

    /**
     * 关闭连接后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        closeConn(session, false);
    }

    private void openConn(WebSocketSession session) {
        //创建一个连接窗口，并加入的队列中
        WSUserModel ws = new WSUserModel(session);
        WSUserModel.webSocketMapAdd(USER_SOCKET_SESSION_MAP, ws);
        addOnlineCount();           //在线数加1
        log.info("有新窗口开始监听:" + ws.getId() + ",当前在线人数为" + getOnlineCount());
        try {
            Map map = new HashMap<>();
            map.put("message", "连接成功");
            map.put("status", "0");
            map.put("type", "0");
            ObjectMapper obj = new ObjectMapper();
            String params = obj.writeValueAsString(map);
            sendMessageToUser(session, params);
            //建立type为4的指令将所有加载期间其他人发送的指令进行收集

            String gridkey = ws.getGridKey();
            String index = pgGridFileDao.getFirstBlockIndexByGridKey(gridkey);
            String key = gridkey + index;
            Boolean flag = redisService.rgetFlagContent(key);
            //判断是否又存在sheet信息被重新加载
            if (flag) {
                List<String> arr = redisService.rgetHandlerContent(key);
                //如未收集到任何指令，不发送type为4信息
                if (arr == null) {
                    redisService.raddFlagContent(key, false);
                    return;
                }
                String data = "";
                for (String str : arr) {
                    data = data + "&*&" + str;
                }
                Map map1 = new HashMap<>();
                map1.put("message", "反馈以前操作信息");
                map1.put("status", "0");
                map1.put("type", "4");
                map1.put("data", data);
                String param = obj.writeValueAsString(map1);
                redisService.raddFlagContent(key, false);
                sendMessageToUser(session, param);
            }

        } catch (Exception e) {
            log.error("openConn--Exception:" + e);
        }
    }

    private void closeConn(WebSocketSession session, boolean isError) {
        WSUserModel wsUserModel = new WSUserModel(session);
        WSUserModel.webSocketMapRemove(USER_SOCKET_SESSION_MAP, wsUserModel);

        if (isError) {
            log.info("窗口关闭(Error):{},当前在线人数为{}" ,wsUserModel.getId() ,getOnlineCount());
        } else {
            subOnlineCount();              //在线数减1
            log.info("窗口关闭:{},当前在线人数为:{}",wsUserModel.getId(),getOnlineCount());
            try{
                Map map = new HashMap<>(2);
                map.put("message", "用户退出");
                map.put("type", 999);
                map.put("username", wsUserModel.getUserName());
                map.put("id", "" + wsUserModel.getWs().getId());
                String param =new ObjectMapper().writeValueAsString(map);
                //消息发送到redis
                redisMessagePublish.publishMessage(new RedisMessageModel(ipAndPort, wsUserModel.getGridKey(), param));
                sendMessageToUserByCurrent(wsUserModel, param);
            }catch (Exception ex){
                log.error("用户下线群发失败:{}",ex);
            }
        }

    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


    /**
     * 群发消息（本节点，其他窗口）
     *
     * @param _wsUserModel 消息提供者
     * @param _content     内容
     */
    public void sendMessageToUserByCurrent(WSUserModel _wsUserModel, String _content) {
        if (USER_SOCKET_SESSION_MAP != null && _wsUserModel != null && _content != null) {
            if (USER_SOCKET_SESSION_MAP.containsKey(_wsUserModel.getGridKey())) {
                //必须是同一个文档
                Hashtable<String, WSUserModel> _userMap = USER_SOCKET_SESSION_MAP.get(_wsUserModel.getGridKey());
                sendMessageToUser(_userMap, _content, _wsUserModel.getWs().getId());
            }
        }
    }


    /**
     * 群发消息（redis消息订阅）
     **/
    public static void sendMessageToUserByRedis(RedisMessageModel model) {
        sendMessageToUserByRedis(model.getIpandport(), model.getGridkey(), model.getContent());
    }

    /**
     * 群发消息（redis消息订阅）
     *
     * @param _ipAndPort 提供数据的节点
     * @param _gridkey   文档id
     * @param _content   内容
     */
    private static void sendMessageToUserByRedis(String _ipAndPort, String _gridkey, String _content) {
        if (USER_SOCKET_SESSION_MAP != null && _ipAndPort != null && _gridkey != null && _content != null) {
            if (!_ipAndPort.equals(ipAndPort)) {
                //不是本机的
                if (USER_SOCKET_SESSION_MAP.containsKey(_gridkey)) {
                    //必须是同一个文档
                    Hashtable<String, WSUserModel> _userMap = USER_SOCKET_SESSION_MAP.get(_gridkey);
                    sendMessageToUser(_userMap, _content, null);
                }
            }
        }
    }

    /**
     * 给同一个文档的全部用户发消息（除了提供消息的用户）
     *
     * @param _userMap 用户组
     * @param _content 内容
     * @param _uid     数据提供的用户sessionid（redis订阅设定为null）
     */
    private static void sendMessageToUser(Hashtable<String, WSUserModel> _userMap, String _content, String _uid) {
        if (_userMap != null && _content != null) {
            TextMessage message = new TextMessage(_content);
            //BinaryMessage message= new BinaryMessage(_content.getBytes());
            _userMap.forEach((k, v) -> {
                if (null == _uid || !k.equals(_uid)) {
                    //给（非消息提供者）打开改文档的用户发消息
                    sendMessageToUser(v.getWs(), message);
                }
            });
        }
    }

    /**
     * 给单个用户发消息
     *
     * @param session
     * @param message
     */
    private static void sendMessageToUser(WebSocketSession session, WebSocketMessage message) {
        try {
            log.info("sendMessageToUser--WebSocketSession");
            synchronized (session) {
                session.sendMessage(message);
            }
        } catch (Exception ex) {
            log.error(ex.toString() + ";WebSocketSession:" + session + "message" + message);
        }
    }

    private static void sendMessageToUser(WebSocketSession session, String message) {

        log.info("sendMessageToUser--onlyForUser");
        sendMessageToUser(session, new TextMessage(message));
    }


    //全局的在线人数
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        onlineCount--;
    }
}
