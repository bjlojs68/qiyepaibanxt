package com.bwtp.staffService.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 请假审批表
 * </p>
 *
 * @author blp
 * @since 2023-08-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="LeaveApprovalForm对象", description="请假审批表")
public class LeaveApprovalForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "唯一标识符，使用自增主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "关联的请假申请表ID")
    private Integer requestId;

    @ApiModelProperty(value = "审批结果：1表示同意，0表示不同意，2待审核，待审核状态可以撤销申请")
    private Integer approvalResult;

    @ApiModelProperty(value = "审批日期和时间")
    @TableField(fill = FieldFill.INSERT)
    private Date startTime;

    @TableLogic
    private Integer isDeleted;
}
