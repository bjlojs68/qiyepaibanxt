package com.bwtp.staffService.entity.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.sf.cglib.core.Local;

import java.time.LocalDate;
import java.util.Date;

@Data
public class leaveRequestFront {
    @ApiModelProperty(value = "唯一标识符，使用自增主键")
    private Integer id;

    @ApiModelProperty(value = "员工姓名")
    private String name;

    @ApiModelProperty(value = "请假类型：1表示事假，0表示病假")
    private Integer leaveType;

    @ApiModelProperty(value = "请假起始日期")
    private LocalDate startDate;

    @ApiModelProperty(value = "请假结束日期")
    private LocalDate endDate;

    @ApiModelProperty(value = "请假天数")
    private Integer leaveDays;

    @ApiModelProperty(value = "请假事由")
    private String reason;

    @ApiModelProperty(value = "联系电话")
    private String contactNumber;

    @ApiModelProperty(value = "创建时间和日期")
    @TableField(fill = FieldFill.INSERT)
    private Date startTime;

    @ApiModelProperty(value = "审批结果：1表示同意，2表示不同意，0待审核，待审核状态可以撤销申请")
    private Integer approvalResult;
}
