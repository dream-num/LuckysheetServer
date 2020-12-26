package com.xc.luckysheet.xlsutils.poiutil;

import com.mongodb.DBObject;
import org.apache.poi.ss.usermodel.Row;
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

        //设置sheet位置，名称
        if(dbObject.containsField("name")&&dbObject.get("name")!=null){
            wb.setSheetName(sheetNum,dbObject.get("name").toString());
        }else{
            wb.setSheetName(sheetNum,"sheet"+sheetNum);
        }


        //循环数据
        if(dbObject.containsField("celldata")&&dbObject.get("celldata")!=null){

        }

    }

    //设置行高
    private static void setRowHeight(Sheet sheet){
        Row row=sheet.getRow(0);
        row.setHeightInPoints(30);
    }
    //设置列宽
    private static void setColumnWidth(Sheet sheet){
        sheet.setColumnWidth(0,MSExcelUtil.pixel2WidthUnits(160));
    }
}
