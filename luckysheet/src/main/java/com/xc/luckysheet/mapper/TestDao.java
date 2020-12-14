package com.xc.luckysheet.mapper;

import com.xc.luckysheet.entity.Test;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public interface TestDao {

    @Select({
            "<script>",
            "SELECT ",
            "id,name  ",
            "FROM ",
            "test ",
            "WHERE ",
            "1=1 ",

            "<if test='param.id!=null'>",
            "AND  id=#{param.id} ",
            "</if>",

            "ORDER BY ",
            "id",
            "</script>"
    })
    List<Test> select(@Param("param") Map<String, Object> param);
}
