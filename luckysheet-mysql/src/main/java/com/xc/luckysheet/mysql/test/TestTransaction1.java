package com.xc.luckysheet.mysql.test;


import com.xc.luckysheet.mysql.impl.BaseHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Spring使用声明式事务处理，默认情况下，
 * 如果被注解的数据库操作方法中发生了unchecked异常，所有的数据库操作将rollback；
 * 如果发生的异常是checked异常，默认情况下数据库操作还是会提交的。
 *
 * 默认配置下，spring只有在抛出的异常为运行时unchecked异常时才回滚该事务，
 * 也就是抛出的异常为RuntimeException的子类(Errors也会导致事务回滚)，而抛出checked异常则不会导致事务回滚。
 *
 * Spring的AOP即声明式事务管理默认是针对unchecked exception回滚。Spring的事务边界是在调用业务方法之前开始的，业务方法执行完毕之后来执行commit or rollback(Spring默认取决于是否抛出runtimeException)。
 *
 * 如果你在方法中有try{}catch(Exception e){}处理，那么try里面的代码块就脱离了事务的管理，
 * 若要事务生效需要在catch中throw new RuntimeException ("xxxxxx");
 *
 *
 CREATE TABLE `test` (
 `id` bigint(120) unsigned NOT NULL,
 `jsontest` json DEFAULT NULL,
 `updatetime` datetime DEFAULT NULL,
 PRIMARY KEY (`id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=armscii8
 */
@Slf4j
@Service
public class TestTransaction1 extends BaseHandle {

    public Integer clear(){
        String sql="delete from test";
        try{
            return luckySheetJdbcTemplate.update(sql);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }

    public Integer add1(){
        String sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
        try{
            return luckySheetJdbcTemplate.update(sql,snowFlake.nextId().longValue(),"{}",new Date());
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }

    public String add2(){
        try{
            String sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,snowFlake.nextId().longValue(),"{}",new Date());

            //类型错误
            sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,"qewqeqw","{}",new Date());

            sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,snowFlake.nextId().longValue(),"{}",new Date());

            return "success";
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }


    //回滚
    @Transactional(value = "mysqlTxManager",rollbackFor = Exception.class)
    public String add3(){
        try{
            String sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,snowFlake.nextId().longValue(),"{}",new Date());

            //类型错误
            sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,"qewqeqw","{}",new Date());

            sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,snowFlake.nextId().longValue(),"{}",new Date());

            return "success";
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional(value = "mysqlTxManager")
    public String add4() throws Exception {
        try{
            String sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,snowFlake.nextId().longValue(),"{}",new Date());

            //类型错误
            sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,"qewqeqw","{}",new Date());

            sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,snowFlake.nextId().longValue(),"{}",new Date());

            return "success";
        }catch (Exception e){
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    //回滚
    //rollbackFor这属性指定了，既使你出现了checked这种例外，那么它也会对事务进行回滚
    @Transactional(value = "mysqlTxManager",rollbackFor = Exception.class)
    public String add5() throws Exception {
        try{
            String sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,snowFlake.nextId().longValue(),"{}",new Date());

            //类型错误
            sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,"qewqeqw","{}",new Date());

            sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,snowFlake.nextId().longValue(),"{}",new Date());

            return "success";
        }catch (Exception e){
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
