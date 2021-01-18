package com.xc.luckysheet.xlsutils.poiutil;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 使用poi导出xls
 * @author Administrator
 */
public class XlsUtil {
    /**
     * 输出文件流
     * @param outputStream 流
     * @param isXlsx  是否是xlsx
     * @param dbObjectList 数据
     */
    public static void exportXlsFile(OutputStream outputStream, Boolean isXlsx,List<JSONObject> dbObjectList) throws IOException {
        Workbook wb=null;
        if(isXlsx){
            wb=new XSSFWorkbook();
        }else{
            wb=new HSSFWorkbook();
        }
        if(dbObjectList!=null&&dbObjectList.size()>0){
            for(int x=0;x<dbObjectList.size();x++){
                XlsSheetUtil.exportSheet(wb,x,dbObjectList.get(x));
            }
        }
        wb.write(outputStream);
    }
}
