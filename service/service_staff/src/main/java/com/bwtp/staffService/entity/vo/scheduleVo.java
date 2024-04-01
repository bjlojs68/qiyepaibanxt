package com.bwtp.staffService.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

//import java.util.Date;
import java.sql.Date;
import java.time.LocalTime;

@Data
public class scheduleVo {
    private String employeeName;
    private Integer position;
    private LocalTime startTime;
    private LocalTime endTime;
    private Date scheduleDate;  //哪天的班次
    private String sign;
}
