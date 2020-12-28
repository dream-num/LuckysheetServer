package com.xc.luckysheet.xlsutils.poiutil;

import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sheet操作
 * @author Administrator
 */
@Slf4j
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
        //是否隐藏
        if(dbObject.containsField("hide") && dbObject.get("hide").toString().equals("1")){
            wb.setSheetHidden(sheetNum,true);
        }
        //是否当前选中页
        if(dbObject.containsField("status") && dbObject.get("status").toString().equals("1")){
            sheet.setSelected(true);
        }


        //循环数据
        if(dbObject.containsField("celldata")&&dbObject.get("celldata")!=null){
            //取到所有单元格集合
            List<DBObject> cells_json = ( List<DBObject> )dbObject.get("celldata");
            Map<Integer,List<DBObject>> cellMap=cellGroup(cells_json);
            //循环每一行
            for(Integer r:cellMap.keySet()){
                Row row=sheet.createRow(r);
                //循环每一列
                for(DBObject col:cellMap.get(r)){
                    createCell(wb,row,col);
                }
            }
        }

    }

    /**
     * 每一个单元格
     * @param row
     * @param dbObject
     */
    private static void createCell(Workbook wb,Row row,DBObject dbObject){
        if(dbObject.containsField("c")) {
            Integer c = getStrToInt(dbObject.get("c"));
            if (c != null) {
                Cell cell=row.createCell(c);
                //取单元格中的v_json
                if(dbObject.containsField("v")) {
                    //获取v对象
                    Object obj = dbObject.get("v");
                    if (obj == null) {
                        //没有内容
                        return;
                    }
                    //如果v对象直接是字符串
                    if(obj instanceof String){
                        if(((String) obj).length()>0){
                            cell.setCellValue(obj.toString());
                        }
                        return;
                    }

                    //转换v为对象(v是一个对象)
                    DBObject v_json = (DBObject)obj;
                    //样式
                    CellStyle style= wb.createCellStyle();
                    cell.setCellStyle(style);

                    //bs 边框样式 //bc 边框颜色
                    setBorderStyle(style,v_json,"bs","bc");
                    //bs_t 上边框样式   bc_t  上边框颜色
                    setBorderStyle(style,v_json,"bs_t","bc_t");
                    //bs_b 下边框样式   bc_b  下边框颜色
                    setBorderStyle(style,v_json,"bs_b","bc_b");
                    //bs_l 左边框样式   bc_l  左边框颜色
                    setBorderStyle(style,v_json,"bs_l","bc_l");
                    //bs_r 右边框样式   bc_r  右边框颜色
                    setBorderStyle(style,v_json,"bs_r","bc_r");

                }

            }
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
    private static void setCellStyle(CellStyle cellStyle,DBObject dbObject){
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
//        Font font = wb.createFont();
//        font.setFontName("黑体");
//        //字体大小
//        font.setFontHeightInPoints((short) 16);
//        //是否粗体显示
//        font.setBold(true);
//        //选择需要用到的字体格式
//        cellStyle.setFont(font);

        //合并单元格
        //参数1：起始行 参数2：终止行 参数3：起始列 参数4：终止列
        //CellRangeAddress region1 = new CellRangeAddress(rowNumber, rowNumber, (short) 0, (short) 11);
    }

    /**
     * 设置cell边框颜色样式
     * @param style 样式
     * @param dbObject json对象
     * @param bs 样式
     * @param bc 样式
     */
    private static void setBorderStyle(CellStyle style,DBObject dbObject,String bs,String bc ){
        //bs 边框样式
        if(dbObject.containsField(bs)){
            Short _v=getStrToShort(getByDBObject(dbObject,bs));
            if(_v!=null){
                //边框没有，不作改变
                if(bs.equals("bs") || bs.equals("bs_t")){
                    style.setBorderTop(BorderStyle.valueOf(_v));
                }
                if(bs.equals("bs") || bs.equals("bs_b")){
                    style.setBorderBottom(BorderStyle.valueOf(_v));
                }
                if(bs.equals("bs") || bs.equals("bs_l")){
                    style.setBorderLeft(BorderStyle.valueOf(_v));
                }
                if(bs.equals("bs") || bs.equals("bs_r")){
                    style.setBorderRight(BorderStyle.valueOf(_v));
                }

                //bc 边框颜色
                String _vcolor=getByDBObject(dbObject,bc);
                if(_vcolor!=null){
                    Short _color=ColorUtil.getColorByStr(_vcolor);
                    if(_color!=null){
                        if(bc.equals("bc") || bc.equals("bc_t")){
                            style.setTopBorderColor(_color);
                        }
                        if(bc.equals("bc") || bc.equals("bc_b")){
                            style.setBottomBorderColor(_color);
                        }
                        if(bc.equals("bc") || bc.equals("bc_l")){
                            style.setLeftBorderColor(_color);
                        }
                        if(bc.equals("bc") || bc.equals("bc_r")){
                            style.setRightBorderColor(_color);
                        }
                    }
                }
            }
        }
    }

    /**
     * 内容按行分组
     * @param cells
     * @return
     */
    private static Map<Integer,List<DBObject>> cellGroup(List<DBObject> cells){
        Map<Integer,List<DBObject>> cellMap=new HashMap<>(100);
        for(DBObject dbObject:cells){
            //行号
            if(dbObject.containsField("r")){
                Integer r =getStrToInt(dbObject.get("r"));
                if(r!=null){
                    if(cellMap.containsKey(r)){
                        cellMap.get(r).add(dbObject);
                    }else{
                        List<DBObject> list=new ArrayList<>(10);
                        list.add(dbObject);
                        cellMap.put(r,list);
                    }
                }
            }

        }
        return cellMap;
    }


    /**
     * 获取一个k的值
     * @param b
     * @param k
     * @return
     */
    public static String getByDBObject(DBObject b,String k){
        if(b.containsField(k)){
            if(b.get(k)!=null&&b.get(k)instanceof String){
                return b.get(k).toString();
            }
        }
        return null;
    }

    /**
     * 获取一个k的值
     * @param b
     * @param k
     * @return
     */
    public static Object getObjectByDBObject(DBObject b,String k){
        if(b.containsField(k)){
            if(b.get(k)!=null){
                return b.get(k);
            }
        }
        return "";
    }

    /**
     * 没有/无法转换 返回null
     * @param b
     * @param k
     * @return
     */
    public static Integer getIntByDBObject(DBObject b,String k){
        if(b.containsField(k)){
            if(b.get(k)!=null){
                try{
                    String _s=b.get(k).toString().replace("px", "");
                    Double _d=Double.parseDouble(_s);
                    return _d.intValue();
                }catch (Exception ex){
                    System.out.println(ex.toString());
                    return null;
                }
            }
        }
        return null;
    }
    /**
     * 转int
     * @param str
     * @return
     */
    private static Integer getStrToInt(Object str){
        try{
            if(str!=null&& str instanceof String) {
                return Integer.parseInt(str.toString());
            }
            return null;
        }catch (Exception ex){
            log.error("String:{};Error:{}",str,ex.getMessage());
            return null;
        }
    }
    private static Short getStrToShort(Object str){
        try{
            if(str!=null&& str instanceof String) {
                return Short.parseShort(str.toString());
            }
            return null;
        }catch (Exception ex){
            log.error("String:{};Error:{}",str,ex.getMessage());
            return null;
        }
    }
}
