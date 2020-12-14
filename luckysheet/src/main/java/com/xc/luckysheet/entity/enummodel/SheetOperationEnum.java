package com.xc.luckysheet.entity.enummodel;

/**
 * 各类操作
 */
public enum SheetOperationEnum {
    fc, //3.4.1 函数链接
    f,//3.6.1 选择筛选条件
    fsc,//3.6.2	清除筛
    fsr,//3.6.3	恢复筛选

    thumb, //3.10.2	缩略图    修改数据库
    na,   //3.10.1	表格名称 修改数据库

    c,     //3.9	图表操作 全部转向rails

    sh,    //3.8.1	隐藏    3.8	sheet属性sh
    shs,   //3.7.5	激活shs
    shr,   //3.7.4	位置shr
    shd,   //3.7.3	删除shd
    shc,   //3.7.2	复制shc
    sha,   //3.7.1	新建sha  3.7 sheet操作

    arc,   //3.5.2	增加行或列
    drc,   //3.5.1 删除行或列
    all, //3.3 通用保存
    cg, //3.2   config操作cg
    v,  //3.1	单元格操作v
    rv,//批量单元格操作
    shre,//撤销删除
	mv;//记录光标位置不保存


    public static boolean contains(String _name){
        SheetOperationEnum[] season = values();
        for(SheetOperationEnum s:season){
            if(s.name().equals(_name)){
                return true;
            }
        }
        return false;

    }
}
