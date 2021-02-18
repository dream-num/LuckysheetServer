package com.xc.luckysheet.mysql.test;


import com.xc.luckysheet.mysql.impl.BaseHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 注解在类上，如果方法上有注解，覆盖
 */
@Slf4j
@Service
@Transactional(value = "mysqlTxManager",rollbackFor = Exception.class)
public class TestTransaction2 extends BaseHandle {


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
