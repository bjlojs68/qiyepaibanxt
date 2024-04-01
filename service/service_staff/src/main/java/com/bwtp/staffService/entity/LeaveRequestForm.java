package com.bwtp.staffService.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 请假申请表
 * </p>
 *
 * @author blp
 * @since 2023-08-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="LeaveRequestForm对象", description="请假申请表")
public class LeaveRequestForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "唯一标识符，使用自增主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "员工编号")
    private String employeeId;

    @ApiModelProperty(value = "请假类型：1表示事假，0表示病假")
    private Integer leaveType;

    @ApiModelProperty(value = "请假起始日期")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date startDate;

    @ApiModelProperty(value = "请假结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date endDate;

    @ApiModelProperty(value = "请假事由")
    private String reason;


    @ApiModelProperty(value = "创建时间和日期")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最后更新时间和日期")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Integer isDeleted;


}
