package com.bwtp.staffService.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class queryLeaveRequest {

    @ApiModelProperty(value = "员工姓名")
    private String name;

    @ApiModelProperty(value = "请假类型：1表示事假，0表示病假")
    private Boolean leaveType;

    @ApiModelProperty(value = "审批结果：1表示同意，2表示不同意，0待审核，待审核状态可以撤销申请")
    private Integer approvalResult;

    @ApiModelProperty(value = "请假天数")
    private Integer leaveDays;
}
