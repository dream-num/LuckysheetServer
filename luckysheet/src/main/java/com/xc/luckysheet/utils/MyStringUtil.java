package com.xc.luckysheet.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Administrator
 */
public class MyStringUtil {

    private final static Pattern p = Pattern.compile("\\s*|\t|\r|\n");
    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
    public static String getStringShow(String str,int limit){
        if(StringUtils.isEmpty(str)){
            return "";
        }else{
            return str.length()<=limit?str:str.substring(0,limit);
        }
    }
    public static String getStringShow(String str){
        return getStringShow(str,50);
    }
}
