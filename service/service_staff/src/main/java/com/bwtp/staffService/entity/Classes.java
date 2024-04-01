package com.bwtp.staffService.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 班次信息表
 * </p>
 *
 * @author blp
 * @since 2023-02-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Classes对象", description="班次信息表")
public class Classes implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    private String shopId;

    @ApiModelProperty(value = "开始时间")
    private LocalTime startTime;

    @ApiModelProperty(value = "结束时间")
    private LocalTime endTime;

    @ApiModelProperty(value = "该时间段店员数量")
    private Integer employeeNumber;


    @ApiModelProperty(value = "创建时间")
//    @TableField(fill = FieldFill.INSERT)
    private Date createData;

}
