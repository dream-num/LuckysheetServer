package com.xc.luckysheet.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: 1
 * Date: 17-11-16
 * Time: 下午1:08
 * To change this template use File | Settings | File Templates.
 * @author Administrator
 */
@Slf4j
public class Pako_GzipUtils {
   // private static final Logger logger = Logger.getLogger(MyGzipUtils.class);

    /**
     * @param str：正常的字符串
     * @return 压缩字符串 类型为：  ³)°K,NIc i£_`Çe#  c¦%ÂXHòjyIÅÖ`
     * @throws IOException
     */
    public static String compress(String str){
        try{
            if (str == null || str.length() == 0) {
                return str;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
            gzip.close();
            return out.toString("ISO-8859-1");
        }catch (Exception e){
            log.error("gzip compress:"+str, e);
        }
        return "";
    }

    public static byte[] compress2(String str){
        try{
            if (str == null || str.length() == 0) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes("UTF-8"));
            gzip.close();
            return out.toByteArray();
        }catch (Exception e){
            log.error("gzip compress2:", e);
        }
        return null;
    }

    public static void compress2(String str,OutputStream _out){
        try{
            if (str == null || str.length() == 0) {
                return;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes("UTF-8"));
            gzip.close();
            out.writeTo(_out);
        }catch (Exception e){
            log.error("gzip compress2:", e);
        }
    }




    /**
     * @param str：类型为：  ³)°K,NIc i£_`Çe#  c¦%ÂXHòjyIÅÖ`
     * @return 解压字符串  生成正常字符串。
     * @throws IOException
     */
    public static String uncompress(String str)  {
        try{
            if (str == null || str.length() == 0) {
                return str;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(str
                    .getBytes("ISO-8859-1"));
            GZIPInputStream gunzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = gunzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            // toString()使用平台默认编码，也可以显式的指定如toString("GBK")
            return out.toString();
        }catch (Exception e){
            log.error("gzip uncompress:"+str, e);
        }
        return "";

    }

    /**
     * @param jsUriStr :字符串类型为：%1F%C2%8B%08%00%00%00%00%00%00%03%C2%B3)%C2%B0K%2CNI%03c%20i%C2%A3_%60%C3%87e%03%11%23%C2%82%0Dc%C2%A6%25%C3%82XH%C3%B2jyI%C3%85%05%C3%96%60%1E%00%17%C2%8E%3Dvf%00%00%00
     * @return 生成正常字符串
     * @throws IOException
     */
    public static String  unCompressURI(String jsUriStr) throws IOException {
        String decodeJSUri= URLDecoder.decode(jsUriStr, "UTF-8");
        String gzipCompress=uncompress(decodeJSUri);
        return gzipCompress;
    }
    /**
     * @param strData :字符串类型为： 正常字符串
     * @return 生成字符串类型为：%1F%C2%8B%08%00%00%00%00%00%00%03%C2%B3)%C2%B0K%2CNI%03c%20i%C2%A3_%60%C3%87e%03%11%23%C2%82%0Dc%C2%A6%25%C3%82XH%C3%B2jyI%C3%85%05%C3%96%60%1E%00%17%C2%8E%3Dvf%00%00%00
     * @throws IOException
     */
    public static String  compress2URI(String strData) throws IOException {
        String encodeGzip=compress(strData);
        String jsUriStr= URLEncoder.encode(encodeGzip, "UTF-8");
        return jsUriStr;
    }


    /**
     * @param jsUriStr :字符串类型为：%1F%C2%8B%08%00%00%00%00%00%00%03%C2%B3)%C2%B0K%2CNI%03c%20i%C2%A3_%60%C3%87e%03%11%23%C2%82%0Dc%C2%A6%25%C3%82XH%C3%B2jyI%C3%85%05%C3%96%60%1E%00%17%C2%8E%3Dvf%00%00%00
     * @return 生成正常字符串
     * @throws IOException
     */
    public static String  unCompressToURI(String jsUriStr)  {
        if(jsUriStr==null){
            return "";
        }
        try {
            String gzipCompress=uncompress(jsUriStr);
            String decodeJSUri= URLDecoder.decode(gzipCompress, "UTF-8");
            return decodeJSUri;
        } catch (Exception e) {
            log.error("gzip unCompressToURI:"+jsUriStr, e);
        }
        return "";
    }
    /**
     * @param strData :字符串类型为： 正常字符串
     * @return 生成字符串类型为：%1F%C2%8B%08%00%00%00%00%00%00%03%C2%B3)%C2%B0K%2CNI%03c%20i%C2%A3_%60%C3%87e%03%11%23%C2%82%0Dc%C2%A6%25%C3%82XH%C3%B2jyI%C3%85%05%C3%96%60%1E%00%17%C2%8E%3Dvf%00%00%00
     * @throws IOException
     */
    public static String  compressToURI(String strData){
        if(strData==null){
            return "";
        }
        try {
            String jsUriStr = URLEncoder.encode(strData, "UTF-8");
            String encodeGzip=compress(jsUriStr);
            return encodeGzip;
        } catch (Exception e) {
            log.error("gzip compressToURI:"+strData, e);
        }
        return "";
    }

    public static void main(String[] args) {
        String  str="[{\"t\":\"v\",\"i\":1,\"v\":\"运输设备\",\"r\":1,\"c\":2}]";
//        System.out.println(compress2URI(str));
//
//        String str1="%1F%C2%8B%08%00%00%00%00%00%00%00%C2%8B%C2%AEV*Q%C2%B2R*S%C3%92Q%C3%8AT%C2%B22%C3%94%01%C2%B2%C2%AC%C2%94%5E%C3%AC%C2%9F%C3%B0b%C3%9F%C3%A4%17%C3%AB%C3%B6%3D%5D%C3%92%0E%C2%94%28%02K%24%2BY%19%C3%95%C3%86%02%00%C3%93%C2%AD%C2%96%C3%920%00%00%00";
//        System.out.println(unCompressURI(str1));

        String _result1=compressToURI(str);
        System.out.println(_result1);
        System.out.println(unCompressToURI(_result1));

        File outputFile = new File("C:\\Users\\1\\Downloads\\test1 (2).tu");
        readGZip(outputFile);

        //File outputFile1 = new File("C:\\Users\\1\\Downloads\\test1 (1).tu");
        //readGZip(outputFile1);

        File outputFile2 = new File("C:\\Users\\1\\Downloads\\test1_byte.tu");
        readGZip(outputFile2);
    }

    private static final int BUFFER_SIZE = 1024;
    public static void readGZip(File file) {

        GZIPInputStream gzipInputStream = null;
        ByteArrayOutputStream baos = null;
        try {
            gzipInputStream = new GZIPInputStream(new FileInputStream(file));

            baos = new ByteArrayOutputStream();

            byte[] buf = new byte[BUFFER_SIZE];
            int len = 0;
            while((len=gzipInputStream.read(buf, 0, BUFFER_SIZE))!=-1){
                baos.write(buf, 0, len);
            }

            baos.toByteArray();

            String result = baos.toString("UTF-8");

            System.out.println("result="+result);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(gzipInputStream!=null){
                try {
                    gzipInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(baos!=null){
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readGZip(MultipartFile fileUpload) {

        GZIPInputStream gzipInputStream = null;
        ByteArrayOutputStream baos = null;
        try {
            gzipInputStream = new GZIPInputStream(fileUpload.getInputStream());

            baos = new ByteArrayOutputStream();

            byte[] buf = new byte[BUFFER_SIZE];
            int len = 0;
            while((len=gzipInputStream.read(buf, 0, BUFFER_SIZE))!=-1){
                baos.write(buf, 0, len);
            }

            baos.toByteArray();

            String result = baos.toString("UTF-8");
            return result;
            //System.out.println("result="+result);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(gzipInputStream!=null){
                try {
                    gzipInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(baos!=null){
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }





}
