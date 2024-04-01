package com.bwtp.staffService.utils;
import com.bwtp.staffService.entity.ServiceEmployee;

import java.time.LocalDate;

/**
 * 获取用户信息供全局使用
 */

public class MyUserInfo {

    public static ServiceEmployee serviceEmployee;
    public static LocalDate currentDate = LocalDate.parse("2023-02-09"); // 获取当前日期


}
