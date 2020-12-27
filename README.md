# Luckysheet Server

English| [简体中文](./README-zh.md)

## Introduction
💻[Luckysheet](https://github.com/mengshukeji/Luckysheet/) official Java version backend.

## Demo
- [Cooperative editing demo](http://luckysheet.lashuju.com/demo/)(Note: Please do not operate frequently to prevent the server from crashing)

## Deploy
- [LuckysheetServer Starter](https://github.com/mengshukeji/LuckysheetServerStarter)

## Requirements

jdk >= 1.8

postgre >= 10 (Support jsonb version)
- [Docker deploys postgre](https://www.cnblogs.com/xuchen0117/p/13863509.html)
- [Jsonb field processing in postgre](https://www.cnblogs.com/xuchen0117/p/13890710.html)

redis >= 3
- [Docker deploys Redis](https://www.cnblogs.com/xuchen0117/p/12183399.html)
- [Docker deploys Redis cluster](https://www.cnblogs.com/xuchen0117/p/11678931.html)


nginx >= 1.12
- [Docker deploys Nginx](https://www.cnblogs.com/xuchen0117/p/11934202.html)

maven >= 3.6 

IntelliJ IDEA >= 12 (not necessary)

## Database initialization

Create database
```
CREATE DATABASE luckysheetdb
```
Create sequence
```
DROP SEQUENCE IF EXISTS "public"."luckysheet_id_seq";
CREATE SEQUENCE "public"."luckysheet_id_seq"
INCREMENT 1
MINVALUE  1
MAXVALUE 9999999999999
START 1
CACHE 10;
```
Create table
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

Insert initialization statement
```
INSERT INTO "public"."luckysheet" VALUES (nextval('luckysheet_id_seq'), 'fblock', '', '1', '1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3', 1, '{"row":84,"name":"Sheet1","chart":[],"color":"","index":"1","order":0,"column":60,"config":{},"status":0,"celldata":[],"ch_width":4748,"rowsplit":[],"rh_height":1790,"scrollTop":0,"scrollLeft":0,"visibledatarow":[],"visibledatacolumn":[],"jfgird_select_save":[],"jfgrid_selection_range":{}}', 0, 0);
INSERT INTO "public"."luckysheet" VALUES (nextval('luckysheet_id_seq'), 'fblock', '', '2', '1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3', 0, '{"row":84,"name":"Sheet2","chart":[],"color":"","index":"2","order":1,"column":60,"config":{},"status":0,"celldata":[],"ch_width":4748,"rowsplit":[],"rh_height":1790,"scrollTop":0,"scrollLeft":0,"visibledatarow":[],"visibledatacolumn":[],"jfgird_select_save":[],"jfgrid_selection_range":{}}', 1, 0);
INSERT INTO "public"."luckysheet" VALUES (nextval('luckysheet_id_seq'), 'fblock', '', '3', '1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3', 0, '{"row":84,"name":"Sheet3","chart":[],"color":"","index":"3","order":2,"column":60,"config":{},"status":0,"celldata":[],"ch_width":4748,"rowsplit":[],"rh_height":1790,"scrollTop":0,"scrollLeft":0,"visibledatarow":[],"visibledatacolumn":[],"jfgird_select_save":[],"jfgrid_selection_range":{}}', 2, 0);
```

## nginx configuration 
http block configuration
```
#websocket configuration
map $http_upgrade $connection_upgrade {
    default upgrade;
    ''      close;
}

upstream ws_dataluckysheet {
      server [Project ip]: [port];
}    
```
server block configuration
```
#websocket configuration
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

#Dynamic resource configuration
location /luckysheet/ {
    proxy_pass http://ws_dataluckysheet;
}

#Static resource configuration
location /luckysheet/demo/ {
    root /usr/share/nginx/html;
    index  index.html index.htm;
}
```

### Access test

- Access the static homepage through `[project ip]:[port]`
- Access the collaborative editing homepage through `[project ip]:[port]?share`

## Project usage 
application.yml Project configuration
```
server:
  port: [Project port]
  servlet:
    context-path: /[Project path]
redis.channel: [redis channel name]
row_size: [The number of rows in the table, default 500]
col_size: [Number of columns in the table, default 500]
pgSetUp: [Whether to enable pgsql as storage data (0 for yes, 1 for no), currently can only be set to 0]
```
application-dev.yml Database configuration
```
spring:
  redis:
    host: [ip address]
    port: [port]
    password: [password]
    
db:
  postgre:
    druid:
      url: jdbc:postgresql://[ip address]: [port]/luckysheetdb?useSSL=false
      driverClassName: org.postgresql.Driver
      username: [username]
      password: [password]    
```
logback-spring.xml Log configuration
```
 <property name="log.path" value="[Log output directory]"/>
```
## project instruction

### Luckysheet module main class description
com.xc.luckysheet.WebApplication Project startup

com.xc.luckysheet.controller
```
JfGridFileController Table data loading class
TestController  postgre redis Test class 
```
com.xc.luckysheet.entity
```
SheetOperationEnum Table operation type
JfGridConfigModel Table block object
LuckySheetGridModel Tabular database objects
PgGridDataModel Table database object
```
com.xc.luckysheet.postgre
```
PostgresGridFileDao postgre database operation
PostgresGridFileGetService Record operation
PostgresJfGridUpdateService Update processing
```
com.xc.luckysheet.redisserver
```
RedisLock redis lock
RedisMessageListener Pipeline monitoring class
RedisMessagePublish Pipeline release class
```
com.xc.luckysheet.service
```
ConfigerService Configuration class
ScheduleService Initialize the timing database
```
com.xc.luckysheet.utils
```
GzipHandle Information compression
Pako_GzipUtils WebSocket information compression
```
com.xc.luckysheet.websocket
```
IpAndPortUtil Get the IP and port of the current service
MyWebSocketHandler Socket processor (including methods for sending information, receiving information, and information errors.)
MyWebSocketInterceptor Socket connection (handshake) and disconnection
WebSocketConfig Register WebSocket, Set the address of WebSocket
WSUserModel WebSocket object
```

### Main class description of common module
```
com.xc.common.config.datasource.DataSourceConfig Data source configuration class
com.xc.common.config.redis.RedisConfig redis configuration class
```

## Links
- [Luckysheet Documentation](https://mengshukeji.github.io/LuckysheetDocs/)
- [How Luckysheet saves the data in the table to the database](https://www.cnblogs.com/DuShuSir/p/13857874.html)

## Authors and acknowledgment

### Team
- [@iamxuchen800117](https://github.com/iamxuchen800117)
- [@wpxp123456](https://github.com/wpxp123456)

## License
Please consult the attached [LICENSE](./LICENSE) file for details. All rights not explicitly granted by the Apache 2.0 License are reserved by the Original Author.
