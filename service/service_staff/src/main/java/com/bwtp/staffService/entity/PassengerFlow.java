package com.bwtp.staffService.entity;

import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 客流量信息表
 * </p>
 *
 * @author blp
 * @since 2023-01-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="PassengerFlow对象", description="客流量信息表")
public class PassengerFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    @ApiModelProperty(value = "门店id")
    @TableField("shopId")
    private String shopId;

    @ApiModelProperty(value = "日期")
    @TableField("Date")
    private LocalDate Date;

    @ApiModelProperty(value = "开始时间")
    private LocalTime startTime;

    @ApiModelProperty(value = "结束时间")
    private LocalTime endTime;

    @ApiModelProperty(value = "预测客流量")
    private Float forecastPassenger;


}
