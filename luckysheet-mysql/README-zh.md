mysql版本 >= 8 

## 数据库初始化

创建数据库
```
CREATE DATABASE luckysheetdb
```

创建表 id采用雪花算法
```
CREATE TABLE `luckysheet` (
  `id` bigint(30) NOT NULL,
  `block_id` varchar(200) NOT NULL,
  `row_col` varchar(50) DEFAULT NULL,
  `index` varchar(200) NOT NULL,
  `list_id` varchar(200) NOT NULL,
  `status` int(1) DEFAULT NULL,
  `json_data` json DEFAULT NULL,
  `order` int(3) DEFAULT NULL,
  `is_delete` int(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lib` (`list_id`,`index`,`block_id`),
  KEY ```order``` (`order`),
  KEY ```status``` (`status`),
  KEY ```is_delete``` (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=armscii8
```

插入初始化语句
```
INSERT INTO luckysheet VALUES (139400313311449087, 'fblock', '', '1', '1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3', 1, '{"row":84,"name":"Sheet1","chart":[],"color":"","index":"1","order":0,"column":60,"config":{},"status":0,"celldata":[],"ch_width":4748,"rowsplit":[],"rh_height":1790,"scrollTop":0,"scrollLeft":0,"visibledatarow":[],"visibledatacolumn":[],"jfgird_select_save":[],"jfgrid_selection_range":{}}', 0, 0);
INSERT INTO luckysheet VALUES (139400313311449088, 'fblock', '', '2', '1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3', 0, '{"row":84,"name":"Sheet2","chart":[],"color":"","index":"2","order":1,"column":60,"config":{},"status":0,"celldata":[],"ch_width":4748,"rowsplit":[],"rh_height":1790,"scrollTop":0,"scrollLeft":0,"visibledatarow":[],"visibledatacolumn":[],"jfgird_select_save":[],"jfgrid_selection_range":{}}', 1, 0);
INSERT INTO luckysheet VALUES (139400313311449089, 'fblock', '', '3', '1079500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3', 0, '{"row":84,"name":"Sheet3","chart":[],"color":"","index":"3","order":2,"column":60,"config":{},"status":0,"celldata":[],"ch_width":4748,"rowsplit":[],"rh_height":1790,"scrollTop":0,"scrollLeft":0,"visibledatarow":[],"visibledatacolumn":[],"jfgird_select_save":[],"jfgrid_selection_range":{}}', 2, 0);
```

注意：
表中 index、order 是关键字


## 项目中使用
1、luckysheet中的pom.xml 注释postgre，引入mysql
```aidl
        <!-- 数据库接口以及实现 -->
        <dependency>
            <groupId>com.xc</groupId>
            <artifactId>luckysheet-db</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.xc</groupId>
            <artifactId>luckysheet-mysql</artifactId>
            <version>${project.version}</version>
        </dependency>
        <!--<dependency>-->
            <!--<groupId>com.xc</groupId>-->
            <!--<artifactId>luckysheet-postgre</artifactId>-->
            <!--<version>${project.version}</version>-->
        <!--</dependency>-->
```

2、com.xc.luckysheet.db.server 文件夹中
JfGridFileGetService类
JfGridUpdateService类

mysql实现
```$xslt
@Resource(name = "mysqlRecordDataInsertHandle")
private IRecordDataInsertHandle recordDataInsertHandle;

@Resource(name = "mysqlRecordDataUpdataHandle")
private IRecordDataUpdataHandle recordDataUpdataHandle;

@Resource(name = "mysqlRecordDelHandle")
private IRecordDelHandle recordDelHandle;

@Resource(name = "mysqlRecordSelectHandle")
private IRecordSelectHandle recordSelectHandle;
```
postgres实现
```
@Resource(name = "postgresRecordDataInsertHandle")
private IRecordDataInsertHandle recordDataInsertHandle;

@Resource(name = "postgresRecordDataUpdataHandle")
private IRecordDataUpdataHandle recordDataUpdataHandle;

@Resource(name = "postgresRecordDelHandle")
private IRecordDelHandle recordDelHandle;

@Resource(name = "postgresRecordSelectHandle")
private IRecordSelectHandle recordSelectHandle;
```
