package com.xc.luckysheet.postgres.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Slf4j
@Component
public class BaseHandle {

    @Resource(name = "postgreJdbcTemplate")
    protected JdbcTemplate jdbcTemplate_postgresql;
}
