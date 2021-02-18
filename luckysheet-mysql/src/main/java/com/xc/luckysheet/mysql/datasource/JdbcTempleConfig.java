package com.xc.luckysheet.mysql.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;


/**
 * @author Administrator
 */
@Configuration
@Slf4j
public class JdbcTempleConfig {

    /**
     * postgre数据源
     */
    @Resource(name = "mysqlDataSource")
    private DataSource dataSource;

    @Bean(name="mysqlJdbcTemplate")
    public JdbcTemplate createJdbcTemplate(){
        return new JdbcTemplate(dataSource);
    }

}
