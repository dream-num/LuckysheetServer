package com.xc.luckysheet.xlsutils.poiutil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


/**
 * 测试导出poi 导出 xls
 * @author Administrator
 */
public class Test {
    public static void main(String[] args){
        String str="[{\"row\":199,\"name\":\"Sheet1\",\"chart\":[],\"color\":\"\",\"index\":\"1\",\"order\":0,\"column\":70,\"config\":{\"merge\":{},\"rowlen\":{\"118\":19},\"colhidden\":{},\"columnlen\":{},\"customHeight\":{\"3\":1}},\"images\":{},\"status\":1,\"ch_width\":4748,\"rowsplit\":[],\"calcChain\":[],\"hyperlink\":{},\"rh_height\":1790,\"scrollTop\":0,\"scrollLeft\":0,\"visibledatarow\":[],\"dataVerification\":{},\"visibledatacolumn\":[],\"jfgird_select_save\":[{\"row\":[3,3],\"top\":60,\"left\":222,\"width\":73,\"column\":[3,3],\"height\":19,\"top_move\":60,\"left_move\":222,\"row_focus\":3,\"width_move\":73,\"height_move\":19,\"column_focus\":3}],\"jfgrid_selection_range\":{},\"luckysheet_alternateformat_save\":[],\"luckysheet_conditionformat_save\":[],\"id\":4852,\"block_id\":\"fblock\",\"list_id\":\"xc79500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3\",\"celldata\":[{\"c\":2,\"r\":2,\"v\":{\"m\":\"3\",\"v\":3,\"ct\":{\"t\":\"n\",\"fa\":\"General\"}}},{\"c\":1,\"r\":1,\"v\":{\"m\":\"2\",\"v\":2,\"ct\":{\"t\":\"n\",\"fa\":\"General\"}}},{\"c\":0,\"r\":0,\"v\":{\"m\":\"1\",\"v\":1,\"ct\":{\"t\":\"n\",\"fa\":\"General\"}}},{\"c\":2,\"r\":101,\"v\":{\"m\":\"1111\",\"v\":1111,\"ct\":{\"t\":\"n\",\"fa\":\"General\"}}}]},{\"row\":84,\"name\":\"Sheet2\",\"chart\":[],\"color\":\"\",\"index\":\"2\",\"order\":1,\"column\":60,\"config\":{},\"status\":0,\"ch_width\":4748,\"rowsplit\":[],\"rh_height\":1790,\"scrollTop\":0,\"scrollLeft\":0,\"visibledatarow\":[],\"visibledatacolumn\":[],\"jfgird_select_save\":[{\"row\":[1,1],\"top\":20,\"left\":74,\"width\":73,\"column\":[1,1],\"height\":19,\"top_move\":20,\"left_move\":74,\"row_focus\":1,\"width_move\":73,\"height_move\":19,\"column_focus\":1}],\"jfgrid_selection_range\":{},\"id\":4712,\"block_id\":\"fblock\",\"list_id\":\"xc79500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3\",\"celldata\":[{\"c\":1,\"r\":3,\"v\":{\"m\":\"qwe\",\"v\":\"qwe\",\"ct\":{\"t\":\"g\",\"fa\":\"General\"}}},{\"c\":1,\"r\":1,\"v\":{\"m\":\"qwe\",\"v\":\"qwe\",\"ct\":{\"t\":\"g\",\"fa\":\"General\"}}}]},{\"row\":84,\"name\":\"Sheet3\",\"chart\":[],\"color\":\"\",\"index\":\"3\",\"order\":2,\"column\":60,\"config\":{},\"status\":0,\"ch_width\":4748,\"rowsplit\":[],\"rh_height\":1790,\"scrollTop\":0,\"scrollLeft\":0,\"visibledatarow\":[],\"visibledatacolumn\":[],\"jfgird_select_save\":[{\"row\":[13,13],\"top\":260,\"left\":296,\"width\":73,\"column\":[4,4],\"height\":19,\"top_move\":260,\"left_move\":296,\"row_focus\":13,\"width_move\":73,\"height_move\":19,\"column_focus\":4}],\"jfgrid_selection_range\":{},\"id\":4713,\"block_id\":\"fblock\",\"list_id\":\"xc79500#-8803#7c45f52b7d01486d88bc53cb17dcd2c3\",\"celldata\":[{\"c\":5,\"r\":2,\"v\":{\"m\":\"请问王企鹅全文\",\"v\":\"请问王企鹅全文\",\"bl\":1,\"ct\":{\"t\":\"g\",\"fa\":\"General\"}}},{\"c\":2,\"r\":8,\"v\":{\"m\":\"请问请问\",\"v\":\"请问请问\",\"ct\":{\"t\":\"g\",\"fa\":\"General\"},\"it\":1}}]}]";
        //DBObject dbObject=(DBObject) JSON.parse(str);
        List<JSONObject> lists=(List<JSONObject> )JSON.parse(str);

        OutputStream out = null;
        try {
            out = new FileOutputStream("/Users/cr/test.xls");
            XlsUtil.exportXlsFile(out,false,lists);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("start");
    }
}
