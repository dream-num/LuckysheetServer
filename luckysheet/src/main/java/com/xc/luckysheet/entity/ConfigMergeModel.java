package com.xc.luckysheet.entity;

import com.mongodb.DBObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * config 中 merge 对象
 * @author Administrator
 */
@Slf4j
@Data
public class ConfigMergeModel {
    /**
     * 行
     */
    private int r;
    /**
     * 列
     */
    private int c;
    /**
     * 行数
     */
    private int rs;
    /**
     * 列数
     */
    private int cs;

    /**
     * 最大行
     */
    private int maxr;
    /**
     * 最大列
     */
    private int maxc;

    /**
     * 将config的merge对象转换为对象
     * @param mc
     * @return
     */
    public static List<ConfigMergeModel>getListByDBObject(DBObject mc){
        List<ConfigMergeModel> _list=new ArrayList<ConfigMergeModel>();
        if(mc!=null){
            for(String k:mc.keySet()){
                try{
                    ConfigMergeModel c=getByDBObject((DBObject)mc.get(k));
                    if(c!=null){
                        _list.add(c);
                    }
                }catch (Exception ex){

                }
            }
        }
        return _list;
    }
    private static ConfigMergeModel getByDBObject(DBObject mc){
        if(mc!=null){
            if(mc.containsField("r") && mc.containsField("c") && mc.containsField("rs") && mc.containsField("cs")){
               try{
                   ConfigMergeModel c=new ConfigMergeModel();
                   c.r=Integer.parseInt(mc.get("r").toString());
                   c.c=Integer.parseInt(mc.get("c").toString());
                   c.rs=Integer.parseInt(mc.get("rs").toString());
                   c.cs=Integer.parseInt(mc.get("cs").toString());

                   //   { "2_1" : { "rs" : 5 , "cs" : 2 , "r" : 2 , "c" : 1}}
                   //   1~2   c=1  cs=2
                   //   2~6   r=2  rs=5
                   c.maxc=c.c+c.cs-1;
                   c.maxr=c.r+c.rs-1;

                   return c;
               }catch (Exception ex){
                   return null;
               }
            }
        }
        return null;
    }

    /**
     * 是否在范围内
     * @param r
     * @param c
     * @return
     */
    public boolean isRange(int r,int c){
        if(r>=this.r && r<=this.maxr && c>=this.c && c<=this.maxc){
            return true;
        }
        return false;
    }

}
