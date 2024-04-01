package com.bwtp.staffService.controller.FrontController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bwtp.commonutils.R;
import com.bwtp.staffService.entity.Classes;
import com.bwtp.staffService.entity.Scheduling;
import com.bwtp.staffService.service.ClassesService;
import com.bwtp.staffService.service.SchedulingService;
import com.bwtp.staffService.utils.MyUserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 返回前台排班数据  周排班数据，就是当前周的7天排班
 * 格式：scheduleJson: '{"2023-09-07":"12:00-11:30","2023-09-20":"12:00-11:30"}'
 */

@RestController
@RequestMapping("/shopService/SchedulingFront")
@CrossOrigin
public class SchedulingFrontController {

    @Resource
    private SchedulingService schedulingService;

//    //缓存管理器
//    @Resource
//    private RedisCacheManager redisCacheManager;

    /**
     * 班次
     */
    @Resource
    private ClassesService classesService;

    //判断缓存中存不存在当前对应的排班数据，如果存在就从缓存中获取，如果不存在则执行getScheduleData
//    public R isGetScheduleData(){
//        String scheduleData="";
//        Cache scheduleString = redisCacheManager.getCache("ScheduleString");
//        Cache.ValueWrapper dateTimeHashMap = scheduleString.get("dateTimeHashMapa");
//        Object o = dateTimeHashMap.get();
//        if (o.equals(null)){
//            scheduleData= o.toString();
//
//        }else {
//             scheduleData = this.getScheduleData();
//        }
//        return R.ok().data("dateTimeHashMap",scheduleData);
//
//    }


    /**
     * 根据用户id获取数据
     */
    @PostMapping("getScheduleData")
    public R getScheduleData() {
       String dateTimeHashMap=schedulingService.getDataTimeRedis();
        return R.ok().data("dateTimeHashMap",dateTimeHashMap);
    }



}
