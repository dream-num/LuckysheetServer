package com.xc.luckysheet.mysql.test;


import com.xc.luckysheet.mysql.impl.BaseHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 测试方法调用方法
 */
@Slf4j
@Service
@Transactional(value = "mysqlTxManager",rollbackFor = Exception.class)
public class TestTransaction52 extends BaseHandle {

    public String adderror(){
        try{
            //类型错误
            String sql="insert into test(id,jsontest,updatetime) values(?,?,?)";
            luckySheetJdbcTemplate.update(sql,"qewqeqw","{}",new Date());
            return "success";
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
