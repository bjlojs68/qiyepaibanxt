package com.bwtp.staffService.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bwtp.commonutils.R;
import com.bwtp.staffService.entity.Classes;
import com.bwtp.staffService.entity.vo.queryDay;
import com.bwtp.staffService.service.ClassesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * <p>
 * 班次信息表 前端控制器
 * </p>
 *
 * @author blp
 * @since 2023-02-02
 */
@RestController
@RequestMapping("/shopService/classes")
@CrossOrigin
public class ClassesController {


    @Resource
    private ClassesService classesService;

    //根据日期获取班次信息
    @PostMapping("getClassesByTime/{dateString}/{shopId}")
    public R getClassesByTime(@PathVariable String dateString,@PathVariable String shopId){

        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        targetFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); //设置时区
        Date newDate = null;
        try {
            newDate = targetFormat.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        QueryWrapper<Classes> classesQueryWrapper = new QueryWrapper<>();
        classesQueryWrapper.eq("create_data",newDate);
        classesQueryWrapper.eq("shop_id",shopId);
        classesQueryWrapper.orderByAsc("start_time");

        List<Classes> list = classesService.list(classesQueryWrapper);

        return R.ok().data("classesList",list);
    }
}

