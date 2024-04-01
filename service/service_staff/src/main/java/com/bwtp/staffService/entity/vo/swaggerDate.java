package com.bwtp.staffService.entity.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static javax.xml.bind.DatatypeConverter.parseDate;

@Data
public class swaggerDate {
//    @ApiModelProperty(required = true,example = "1997-01-01 00:00:00",value = "日期")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

//    @JsonCreator
//    public static swaggerDate fromString(String dateString) {
//        return new swaggerDate(parseDate(dateString));
//    }
//
//    private static Date parseDate(String dateString) {
//        // 解析字符串并返回Date对象
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        format.setTimeZone(TimeZone.getTimeZone("UTC")); // 设置时区为UTC
//        try {
//            return format.parse(dateString);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public swaggerDate(Date time) {
//        this.time = time;
//    }
}
