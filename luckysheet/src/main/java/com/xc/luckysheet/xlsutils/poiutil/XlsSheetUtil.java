package com.xc.luckysheet.xlsutils.poiutil;

import com.mongodb.DBObject;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

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

    /**
     * 设置列宽
     * 第一个参数代表列id(从0开始),第2个参数代表宽度值  参考 ："2012-08-10"的宽度为2500
     * @param sheet
     */
    private static void setColumnWidth(Sheet sheet){
        sheet.setColumnWidth(0,MSExcelUtil.pixel2WidthUnits(160));
    }
    //单元格样式
    private static void setCellStyle(Workbook wb){
        CellStyle cellStyle= wb.createCellStyle();

        //是否自动换行
        cellStyle.setWrapText(true);

        //设置背景色
        cellStyle.setFillBackgroundColor((short)13);

        //设置边框
        //下边框
        cellStyle.setBorderBottom(BorderStyle.DOUBLE);
        //左边框
        cellStyle.setBorderLeft(BorderStyle.DOUBLE);
        //上边框
        cellStyle.setBorderTop(BorderStyle.DOUBLE);
        //右边框
        cellStyle.setBorderRight(BorderStyle.DOUBLE);

        //设置居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        //设置字体
        Font font = wb.createFont();
        font.setFontName("黑体");
        //字体大小
        font.setFontHeightInPoints((short) 16);
        //是否粗体显示
        font.setBold(true);
        //选择需要用到的字体格式
        cellStyle.setFont(font);

        //合并单元格
        //参数1：起始行 参数2：终止行 参数3：起始列 参数4：终止列
        //CellRangeAddress region1 = new CellRangeAddress(rowNumber, rowNumber, (short) 0, (short) 11);

    }
}
