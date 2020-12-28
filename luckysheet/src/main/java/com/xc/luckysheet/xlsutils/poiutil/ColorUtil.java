package com.xc.luckysheet.xlsutils.poiutil;



import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;


/**
 * @author Administrator
 */
public class ColorUtil {

    public static Short getColorByStr(String colorStr){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFPalette palette = workbook.getCustomPalette();

        if(colorStr.equals("#000")){
            colorStr="#000000";
        }
        if(colorStr!=null && colorStr.length()>=6){
            try{
                if(colorStr.length()==8){
                    colorStr=colorStr.substring(2);
                }
                if(colorStr.length()==7){
                    colorStr=colorStr.substring(1);
                }
                String str2 = colorStr.substring(0,2);
                String str3 = colorStr.substring(2,4);
                String str4 = colorStr.substring(4,6);
                int red = Integer.parseInt(str2,16);
                int green = Integer.parseInt(str3,16);
                int blue = Integer.parseInt(str4,16);

                HSSFColor hssfColor=palette.findSimilarColor(red,green,blue);
                return hssfColor.getIndex();
            }catch (Exception ex){
                System.out.println(ex.toString());
                return null;
            }
        }
        return null;
    }
}
