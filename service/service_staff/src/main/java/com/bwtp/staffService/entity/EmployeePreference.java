package com.bwtp.staffService.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 员工偏好表
 * </p>
 *
 * @author blp
 * @since 2023-02-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="EmployeePreference对象", description="员工偏好表")
public class EmployeePreference implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "员工id")
    @TableId(value = "EmployeeId", type = IdType.AUTO)
    private String EmployeeId;

    @ApiModelProperty(value = "偏好类型id")
    private String preferenceTypeId;

    @ApiModelProperty(value = "偏好值")
    private String preferenceValue;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
