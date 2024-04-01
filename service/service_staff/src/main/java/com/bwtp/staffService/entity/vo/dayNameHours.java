package com.bwtp.staffService.entity.vo;

import lombok.Data;

@Data
public class dayNameHours {
    private String employeeName;
    private int[] isHaveWork; //1为值班，0为休息
    private Integer position;
//    private String sign;
}
