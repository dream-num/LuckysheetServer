package com.xc.common.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Bean;

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
    @Resource(name = "postgreDataSource")
    private DataSource postgreDataSource;

    @Bean(name="postgreJdbcTemplate")
    public JdbcTemplate createPostgreJdbcTemplate(){
        return new JdbcTemplate(postgreDataSource);
    }

}
