package com.xc.luckysheet.service;

import com.xc.luckysheet.postgre.server.PostgresJfGridUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 定时任务
 * @author cr
 */
@Slf4j
@Service
public class ScheduleService {

    @Autowired
    private PostgresJfGridUpdateService postgresJfGridUpdateService;

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = "0 */5 * * * *")
    public void test(){
        System.out.println(format.format(new Date()));
    }



    @Scheduled(cron = "0 0 1 * * *")
    public void pgInit(){
        postgresJfGridUpdateService.initTestData();
        System.out.println(format.format(new Date())+" luckysheet table init!!!");
    }

}
