package com.bwtp.staffService.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class queryEmployee {

    private String name;


    @ApiModelProperty(value = "职位")
    private String position;

    @ApiModelProperty(value = "门店")
    private String shopId;
}
