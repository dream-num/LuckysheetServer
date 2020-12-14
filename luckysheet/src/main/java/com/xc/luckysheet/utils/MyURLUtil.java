package com.xc.luckysheet.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created with IntelliJ IDEA.
 * User: 1
 * Date: 17-11-16
 * Time: 上午9:38
 * To change this template use File | Settings | File Templates.
 * @author Administrator
 */
public class MyURLUtil {
    /**
     * 解码
     * @param str
     * @return
     */
    public static String urlDecode(String str){
        try {
            String strReturn= URLDecoder.decode(str, "UTF-8");
            return strReturn;
        } catch (UnsupportedEncodingException e) {
           System.out.println("urlDecode error:"+str+" info:"+e.toString());
        }
        return null;
    }

    /**
     * 编码
     * @param str
     * @return
     */
    public static String urlEncode(String str){
        try {
            String strReturn= URLEncoder.encode(str, "UTF-8");
            return strReturn;
        } catch (UnsupportedEncodingException e) {
            System.out.println("urlEncode error:"+str+" info:"+e.toString());
        }
        return null;
    }

    //字符串转字节
    public static byte[] stringTobyte(String str){
        return stringTobyte(str,"ISO-8859-1");
    }
    public static byte[] stringTobyte(String str,String charsetName){
        try {
            return str.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            System.out.println("stringTobyte error:"+str+" info:"+e.toString());
        }
        return null;
    }
    
}
