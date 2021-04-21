mongodb版本 >= 3.6.1

## 数据库初始化
## 创建数据库
### 方法一导入cursor.json文件
### 方法二配置完成后执行初始化
```
地址：
http://localhost:9004/luckysheet/test/dbInit
类：
src/com/xc/luckysheet/controller/TestController
```


## 项目中使用
1、luckysheet中的pom.xml 注释postgre，引入mongo
```aidl
       <!-- 数据库接口以及实现 -->
       <dependency>
           <groupId>com.xc</groupId>
           <artifactId>luckysheet-db</artifactId>
           <version>${project.version}</version>
       </dependency>
       <dependency>
           <groupId>com.xc</groupId>
           <artifactId>luckysheet-mongo</artifactId>
           <version>${project.version}</version>
       </dependency>
       <!--<dependency>-->
           <!--<groupId>com.xc</groupId>-->
           <!--<artifactId>luckysheet-mysql</artifactId>-->
           <!--<version>${project.version}</version>-->
       <!--</dependency>-->
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
mongo实现
```aidl
@Resource(name = "mongoRecordDataInsertHandle")
private IRecordDataInsertHandle recordDataInsertHandle;

@Resource(name = "mongoRecordDataUpdataHandle")
private IRecordDataUpdataHandle recordDataUpdataHandle;

@Resource(name = "mongoRecordDelHandle")
private IRecordDelHandle recordDelHandle;

@Resource(name = "mongoRecordSelectHandle")
private IRecordSelectHandle recordSelectHandle;
```