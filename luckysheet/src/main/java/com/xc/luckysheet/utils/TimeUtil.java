package com.xc.luckysheet.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author zhouhang
 * @description TimeUtil
 * @date 2021/5/10
 */
public class TimeUtil {

    public static int getTodayBeginTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return (int) (calendar.getTime().getTime() / 1000);
    }

}
