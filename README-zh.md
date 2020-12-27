# Luckysheet Server

简体中文 | [English](./README.md)

## 介绍
💻[Luckysheet](https://gitee.com/mengshukeji/Luckysheet/) 官方Java版本后台。

## 演示
- [协同编辑Demo](http://luckysheet.lashuju.com/demo/)（注意：请大家别操作频繁，防止搞崩服务器）

## 部署
- [LuckysheetServer Starter](https://github.com/mengshukeji/LuckysheetServerStarter)

## 环境

jdk >= 1.8

postgre >= 10 (支持jsonb的版本)
- [Docker部署postgre](https://www.cnblogs.com/xuchen0117/p/13863509.html)
- [postgre中jsonb字段处理](https://www.cnblogs.com/xuchen0117/p/13890710.html)

redis >= 3
- [Docker部署Redis](https://www.cnblogs.com/xuchen0117/p/12183399.html)
- [Docker部署Redis集群](https://www.cnblogs.com/xuchen0117/p/11678931.html)


nginx >= 1.12
- [Docker部署Nginx](https://www.cnblogs.com/xuchen0117/p/11934202.html)

maven >= 3.6 

IntelliJ IDEA >= 12 (非必须)

## 数据库初始化

创建数据库
```
CREATE DATABASE luckysheetdb
```
创建序列
```
DROP SEQUENCE IF EXISTS "public"."luckysheet_id_seq";
CREATE SEQUENCE "public"."luckysheet_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9999999999999
START 1
CACHE 10;
```
创建表
```
DROP TABLE IF EXISTS "public"."luckysheet";
CREATE TABLE "luckysheet" (
  "id" int8 NOT NULL,
  "block_id" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "row_col" varchar(50),
  "index" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "list_id" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "status" int2 NOT NULL,
  "json_data" jsonb,
  "order" int2,
  "is_delete" int2
);
CREATE INDEX "block_id" ON "public"."luckysheet" USING btree (
  "block_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "list_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "index" ON "public"."luckysheet" USING btree (
  "index" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "list_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "is_delete" ON "public"."luckysheet" USING btree (
  "is_delete" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "list_id" ON "public"."luckysheet" USING btree (
  "list_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "order" ON "public"."luckysheet" USING btree (
  "list_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "order" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "status" ON "public"."luckysheet" USING btree (
  "list_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
ALTER TABLE "public"."luckysheet" ADD CONSTRAINT "luckysheet_pkey" PRIMARY KEY ("id");
```

插入初始化语句
```
INSERT INTO "public"."luckysheet" VALUES (nextval('luckysheet_id_seq'), 'fblock', '', '1', '1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3', 1, '{"row":84,"name":"Sheet1","chart":[],"color":"","index":"1","order":0,"column":60,"config":{},"status":0,"celldata":[],"ch_width":4748,"rowsplit":[],"rh_height":1790,"scrollTop":0,"scrollLeft":0,"visibledatarow":[],"visibledatacolumn":[],"jfgird_select_save":[],"jfgrid_selection_range":{}}', 0, 0);
INSERT INTO "public"."luckysheet" VALUES (nextval('luckysheet_id_seq'), 'fblock', '', '2', '1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3', 0, '{"row":84,"name":"Sheet2","chart":[],"color":"","index":"2","order":1,"column":60,"config":{},"status":0,"celldata":[],"ch_width":4748,"rowsplit":[],"rh_height":1790,"scrollTop":0,"scrollLeft":0,"visibledatarow":[],"visibledatacolumn":[],"jfgird_select_save":[],"jfgrid_selection_range":{}}', 1, 0);
INSERT INTO "public"."luckysheet" VALUES (nextval('luckysheet_id_seq'), 'fblock', '', '3', '1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3', 0, '{"row":84,"name":"Sheet3","chart":[],"color":"","index":"3","order":2,"column":60,"config":{},"status":0,"celldata":[],"ch_width":4748,"rowsplit":[],"rh_height":1790,"scrollTop":0,"scrollLeft":0,"visibledatarow":[],"visibledatacolumn":[],"jfgird_select_save":[],"jfgrid_selection_range":{}}', 2, 0);
```

## nginx配置 
http块配置
```
#websocket配置
map $http_upgrade $connection_upgrade {
    default upgrade;
    ''      close;
}

upstream ws_dataluckysheet {
      server 项目的ip:端口;
}    
```
server块配置
```
#websocket配置
location /luckysheet/websocket/luckysheet {
    proxy_pass http://ws_dataluckysheet/luckysheet/websocket/luckysheet;

    proxy_set_header Host $host;
    proxy_set_header X-real-ip $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

    #websocket
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}       

#动态资源配置
location /luckysheet/ {
    proxy_pass http://ws_dataluckysheet;
}

#静态资源配置，Luckysheet前端代码目录
location / {
    root   /usr/share/nginx/html; # 可修改为自己的资源路径
    index  index.html index.htm;
}
```

### 访问测试

- 通过`项目的ip:端口`访问静态主页
- 通过`项目的ip:端口?share`访问协同编辑主页

## 项目用法 
application.yml 项目配置
```
server:
  port: 项目端口
  servlet:
    context-path: /项目路径
redis.channel: redis通道名称
row_size: 表格中行数 默认500
col_size: 表格中列数 默认500
pgSetUp: 是否启用pgsql作为存储数据（0为是，1为否）目前只能设置为0
```
application-dev.yml 数据库配置
```
spring:
  redis:
    host: ip地址
    port: 端口
    password: 密码
    
db:
  postgre:
    druid:
      url: jdbc:postgresql://ip地址:端口/luckysheetdb?useSSL=false
      driverClassName: org.postgresql.Driver
      username: 用户名
      password: 密码    
```
logback-spring.xml 日志配置
```
 <property name="log.path" value="日志输出目录"/>
```
## 项目说明

### Luckysheet模块主要类说明
com.xc.luckysheet.WebApplication 项目启动类

com.xc.luckysheet.controller
```
JfGridFileController 表格数据加载类
TestController  postgre redis 测试类 
```
com.xc.luckysheet.entity
```
SheetOperationEnum 表格操作类型
JfGridConfigModel 表格块对象
LuckySheetGridModel 表格数据库对象
PgGridDataModel 表格sheet数据库对象
```
com.xc.luckysheet.postgre
```
PostgresGridFileDao postgre数据库操作
PostgresGridFileGetService 记录操作
PostgresJfGridUpdateService 更新处理
```
com.xc.luckysheet.redisserver
```
RedisLock redis锁
RedisMessageListener 管道监听类
RedisMessagePublish 管道发布类
```
com.xc.luckysheet.service
```
ConfigerService 配置类
ScheduleService 对定时数据库初始化
```
com.xc.luckysheet.utils
```
GzipHandle 信息压缩
Pako_GzipUtils WebSocket信息压缩
```
com.xc.luckysheet.websocket
```
IpAndPortUtil 获取当前服务的ip与端口
MyWebSocketHandler Socket处理器(包括发送信息，接收信息，信息错误等方法。)
MyWebSocketInterceptor Socket建立连接（握手）和断开
WebSocketConfig 注册WebSocket，设置WebSocket的地址
WSUserModel WebSocket对象
```

### common模块主要类说明
```
com.xc.common.config.datasource.DataSourceConfig 数据源配置类
com.xc.common.config.redis.RedisConfig redis配置类
```

### websocket 返回数据格式
```
{
    createTime: 命令发送时间
    data:{} 修改的命令
    id: "7a"   websocket的id
    returnMessage: "success"
    status: "0"  0告诉前端需要根据data的命令修改  1无意义
    type: 0：连接成功，1：发送给当前连接的用户，2：发送信息给其他用户，3：发送选区位置信息，999：用户连接断开
    username: 用户名
}
```

## 相关链接
- [Luckysheet官方文档](https://mengshukeji.gitee.io/LuckysheetDocs/)
- [Luckysheet如何把表格里的数据保存到数据库](https://www.cnblogs.com/DuShuSir/p/13857874.html)

## 贡献者和感谢

### 团队
- [@iamxuchen800117](https://github.com/iamxuchen800117)
- [@wpxp123456](https://github.com/wpxp123456)

## 版权信息
有关详细信息，请查阅附件的[LICENSE](./LICENSE)文件。原始作者保留Apache 2.0许可未明确授予的所有权利。
