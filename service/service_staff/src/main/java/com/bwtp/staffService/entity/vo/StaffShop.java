package com.bwtp.staffService.entity.vo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
public class StaffShop {

    @ApiModelProperty(value = "主键")
    private String id;

    @ApiModelProperty(value = "用户名")
    private String name;

    @ApiModelProperty(value = "电子邮件")
    private String email;

    @ApiModelProperty(value = "职位，1-经理  2-副经理  3-小组长  4-店员 ")
    private Integer position;

    @ApiModelProperty(value = "门店名")
    private String shopName;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
