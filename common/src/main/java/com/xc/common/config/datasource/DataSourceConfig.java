package com.xc.common.config.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;


/**
 * 数据源配置
 * @author Administrator
 */
@Configuration
@Slf4j
public class DataSourceConfig {

    @Bean(name = "postgreDataSource")
    @ConfigurationProperties(prefix = "db.postgre.druid")
    public DataSource postgreDataSource(){
        DataSource postgre = DataSourceBuilder.create().type(DruidDataSource.class).build();
        log.debug("数据源postgre",postgre);
        return postgre;
    }


}
