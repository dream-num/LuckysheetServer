package com.xc.luckysheet.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.Gson;
import com.xc.common.constant.SysConstant;

import java.util.ArrayList;
import java.util.Map;

public class TestUtil {
    public static void main(String[] args){
        ArrayList<Object> array=new ArrayList<>();
        array.add("abc");
        array.add("123");

        JSONObject test=new JSONObject();
        test.put("test1","1");
        test.put("list",array);

        System.out.println(new Gson().toJson(test));
        System.out.println(test.toString());
        System.out.println(test.toJSONString());


        JSONObject test1=new JSONObject();
        test1.put("day1","2019-10-10");
        test1.put("day2",null);
        System.out.println(test1.toString(SerializerFeature.WriteMapNullValue));
        System.out.println(test1.toJSONString(SerializerFeature.WriteMapNullValue));


        String str="{\"day1\":\"2019-10-10\",\"day2\":null}";
        JSONObject db=JSONObject.parseObject(str);
        System.out.println(db.toString());
        System.out.println(db.toJSONString());
        Map<String,Object> queryDB=db.getInnerMap();
        for (String key : queryDB.keySet()) {
            System.out.println(key+"='"+queryDB.get(key)+"'");
        }
    }
}
