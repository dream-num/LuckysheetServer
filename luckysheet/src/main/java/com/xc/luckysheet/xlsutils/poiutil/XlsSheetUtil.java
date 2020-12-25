package com.xc.luckysheet.xlsutils.poiutil;

import com.mongodb.DBObject;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * sheet操作
 * @author Administrator
 */
public class XlsSheetUtil {
    /**
     * 导出sheet
     * @param wb
     * @param sheetNum
     * @param dbObject
     */
    public static void exportSheet(Workbook wb, int sheetNum, DBObject dbObject){
        Sheet sheet=wb.createSheet();
        wb.setSheetName(sheetNum,"sheet"+sheetNum);

    }
}
