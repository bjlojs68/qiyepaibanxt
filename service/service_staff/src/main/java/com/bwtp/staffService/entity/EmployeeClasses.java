package com.bwtp.staffService.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 员工班次信息表
 * </p>
 *
 * @author blp
 * @since 2023-03-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="EmployeeClasses对象", description="员工班次信息表")
public class EmployeeClasses implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    @ApiModelProperty(value = "员工id")
    @TableField("EmployeeId")
    private String EmployeeId;

    @ApiModelProperty(value = "班次id")
    @TableField("classesId")
    private String classesId;


    @ApiModelProperty(value = "创建时间")
//    @TableField(fill = FieldFill.INSERT)
    private LocalDate createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDate updateTime;


}
