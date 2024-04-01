//package com.bwtp.staffService.controller;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.bwtp.commonbases.exceptionhandler.BwtpException;
//import com.bwtp.commonutils.MD5;
//import com.bwtp.commonutils.R;
//import com.bwtp.staffService.entity.Classes;
//import com.bwtp.staffService.entity.Scheduling;
//import com.bwtp.staffService.entity.ServiceEmployee;
//import com.bwtp.staffService.entity.ServiceShop;
//import com.bwtp.staffService.entity.vo.*;
//import com.bwtp.staffService.service.ClassesService;
//import com.bwtp.staffService.service.SchedulingService;
//import com.bwtp.staffService.service.ServiceEmployeeService;
//import com.bwtp.staffService.service.ServiceShopService;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.util.ObjectUtils;
//import org.springframework.web.bind.annotation.*;
//import sun.util.resources.LocaleData;
//
//import javax.annotation.Resource;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.time.ZoneId;
//import java.util.*;
//
//
///**
// * <p>
// * 排班表 前端控制器
// * </p>
// *
// * @author blp
// * @since 2023-04-03
// */
//@RestController
//@RequestMapping("/shopService/scheduling")
//@CrossOrigin
//public class SchedulingController {
//
//    @Resource
//    private SchedulingService schedulingService;
//
//    @Resource
//    private ServiceShopService serviceShopService;
//
//    @Resource
//    private ServiceEmployeeService serviceEmployeeService;
//
//    @Resource
//    private ClassesService classesService;
//
//    //获取周排班数据
////    @PostMapping("getWeekSchedule/{current}")
////    public R getWeekSchedule(@PathVariable int current) {
////        List<scheduleVo> weekClassesList = schedulingService.getWeekClassesList(current,"1");
////
////        return R.ok().data("classesList", weekClassesList).data("total", weekClassesList.size());
////    }
//
//    //获取月排班数据
///*    @PostMapping("getMouthSchedule")
//    public R getMouthSchedule() {
//        List<scheduleVo> mouthClassesList = schedulingService.getMouthClassesList();
//
////
////        ObjectMapper objectMapper = new ObjectMapper();
////        String s = null;
////        try {
////            s = objectMapper.writeValueAsString(mouthClassesList);
////        } catch (JsonProcessingException e) {
////            e.printStackTrace();
////        }
//        System.out.println(mouthClassesList);
//
//        return R.ok().data("mouthClassesList", mouthClassesList).data("total", mouthClassesList.size());
//    }*/
//
//    //获取日排班数据
//    @PostMapping("getDaySchedule")
//    public R getDaySchedule(@RequestBody queryDay queryDay) {
//
////        System.out.println(queryDay.getPosition().length);
//
//        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        targetFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); //设置时区
//        Date newDate = null;
//        try {
//            newDate = targetFormat.parse(queryDay.getDateString());
////            System.out.println(newDate); // 输出转换后的Date对象
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        List<scheduleVo> list = schedulingService.getDayClassesList(newDate,queryDay.getShopId());
//
//        for (int i = 0; i < list.size(); i++) {
//            System.out.println(list.get(i));
//        }
//
//
//        //根据岗位条件获取相应的排班数组
//        List<scheduleVo> scheduleVos = new ArrayList<>();
//        int[] position = queryDay.getPosition();
//        if (position.length != 0) {
//            for (int i = 0; i < list.size(); i++) {
//                for (int j = 0; j < position.length; j++) {
//                    if (position[j]==0){
//                        scheduleVos = list;
//                    }else {
//                        if (list.get(i).getPosition() == position[j]) {
//                            scheduleVos.add(list.get(i));
//                        }
//                    }
//
//                }
//            }
//        } else {
//            scheduleVos = list;
//        }
//
//        /**---------------------------------------------------------*/
//
//        QueryWrapper<Classes> classesQueryWrapper = new QueryWrapper<>();
//
//        classesQueryWrapper.eq("create_data",newDate);
//        classesQueryWrapper.eq("shop_id",queryDay.getShopId());
//        classesQueryWrapper.orderByAsc("start_time");
//
//        List<Classes> list2 = classesService.list(classesQueryWrapper);
//
//        String[] timeB = new String[list.size()];
//        for (int i = 0; i < list2.size(); i++) {
//            String s = "";
//            //拼接
//            Classes classes = list2.get(i);
//            s = s + classes.getStartTime().toString() + "-" + classes.getEndTime().toString();
//            timeB[i] = s;
//        }
//
//        /**---------------------------------------------------------*/
//
//        //传给前端一个时间字符串数组  格式：开始时间-结束时间
//        String[] time = new String[list.size()];
//        for (int i = 0; i < list.size(); i++) {
//            String s = "";
//            //拼接
//            scheduleVo scheduleVo = list.get(i);
//            s = s + scheduleVo.getStartTime().toString() + "-" + scheduleVo.getEndTime().toString();
//            time[i] = s;
//        }
//
//        HashSet<String> strings = new HashSet<>();
//        for (int i = 0; i < time.length; i++) {
//            strings.add(time[i]);
//        }
//
//        String[] times = strings.toArray(new String[0]);//将set集合转为字符串数组
//
//        for (int i = 0; i < times.length; i++) {
//            System.out.println(times[i]);
//        }
//        //排序 根据开始时间和结束时间
//        String a;
//        for (int i = 0; i < times.length; i++) {
//            for (int j = 0; j < times.length; j++) {
//                String time1 = times[i].substring(0, 5);
//                String time3= times[i].substring(6, 8);
//
//                String time2 = times[j].substring(0, 5);
//                String time4= times[j].substring(6, 8);
//
//                if (time1.compareTo(time2) < 0) {
//                    a = times[i];
//                    times[i] = times[j];
//                    times[j] = a;
//                }
//            }
//        }
//
//        for (int i = 0; i < times.length; i++) {
//            System.out.println(times[i]);
//        }
//
//        int size = list.size();
//
//        //获取员工姓名
//        QueryWrapper<ServiceEmployee> queryWrapper = new QueryWrapper<>();
//
//        queryWrapper.eq("shop_id", queryDay.getShopId());
//        queryWrapper.orderByAsc("position");
//
//        List<ServiceEmployee> list1 = serviceEmployeeService.list(queryWrapper);
//        ArrayList<String> names = new ArrayList<>();
//        for (int i = 0; i < list1.size(); i++) {
//            names.add(list1.get(i).getName());
//        }
//
//        ArrayList<dayNameHours> dayNameHours = new ArrayList<>();
//        for (int i = 0; i < names.size(); i++) {
//            dayNameHours dayNameHour = new dayNameHours();
//            dayNameHour.setEmployeeName(names.get(i)); //设置姓名
//            int[] isHave = new int[times.length];
//            for (int j = 0; j < scheduleVos.size(); j++) {
//                if (dayNameHour.getEmployeeName().equals(scheduleVos.get(j).getEmployeeName())) {
//                    if (dayNameHour.getPosition() == null) { //设置岗位
//                        dayNameHour.setPosition(scheduleVos.get(j).getPosition());
//                    }
//                    LocalTime startTime = scheduleVos.get(j).getStartTime();
//                    LocalTime endTime = scheduleVos.get(j).getEndTime();
//
//                    for (int k = 0; k < times.length; k++) {
//                        //切割转localTime数据类型
//                        String[] timeArray = times[k].split("-");
//                        LocalTime startTime1 = LocalTime.parse(timeArray[0]);
//                        LocalTime endTime1 = LocalTime.parse(timeArray[1]);
//                        if (startTime.equals(startTime1) && endTime.equals(endTime1)) {  //如果该位置有值则设置为1
//                            isHave[k] = 1;
//
//                        } else { //没有则存入
//                            if (isHave[k]==0){
//                                isHave[k] = 0;
//                            }
//
//                        }
//                    }
//
//                }
//
//            }
//            dayNameHour.setIsHaveWork(isHave);
//            dayNameHours.add(dayNameHour);
//        }
//
//
//        //人麻了，为什么用dayClassesList作为key不行
//        return R.ok().data("total", size).data("times", times).data("dayNameHours", dayNameHours).data("names", names).data("list", list);
//    }
//
//    //根据weekI获取排班，周排班
//    @PostMapping("getNameHours")
//    public R getNameHours(@RequestBody querySchedule querySchedule) {
//        Integer weekI = querySchedule.getWeekI();
//        //获取日期集合
//        LocalDate localDate = LocalDate.of(2023, 1, 30);//固定
//        LocalDate startDays = localDate.plusDays((weekI - 1) * 7); //周一
//        LocalDate endDays = localDate.plusDays(weekI * 7-1); //周日
//        ArrayList<LocalDate> dateArrayList = new ArrayList<>();
//        for (int i = (weekI - 1) * 7; i < weekI * 7; i++) {
//            LocalDate localDate1 = localDate.plusDays(i);
//            dateArrayList.add(localDate1);
//        }
//
//        //获取员工姓名
//        QueryWrapper<ServiceEmployee> queryWrapper = new QueryWrapper<>();
//
//        queryWrapper.eq("shop_id", querySchedule.getShopId());
//        queryWrapper.orderByAsc("position");
//
//        List<ServiceEmployee> list1 = serviceEmployeeService.list(queryWrapper);
//        ArrayList<String> names = new ArrayList<>();
//        for (int i = 0; i < list1.size(); i++) {
//            names.add(list1.get(i).getName());
//        }
//
//        List<scheduleVo> weekClassesList = schedulingService.getWeekClassesList(weekI,querySchedule.getShopId());
//
//        //根据岗位条件获取相应的排班数组
//        List<scheduleVo> scheduleVos = new ArrayList<>();
//
//        if (querySchedule.getPosition().length != 0) {
//            for (int i = 0; i < weekClassesList.size(); i++) {
//                for (int j = 0; j < querySchedule.getPosition().length; j++) {
//                    if (querySchedule.getPosition()[j] == 0) {
//                        scheduleVos = weekClassesList;
//                    } else if (weekClassesList.get(i).getPosition() == querySchedule.getPosition()[j]) {
//                        scheduleVos.add(weekClassesList.get(i));
//                    }
//                }
//
//            }
//        } else {
//            scheduleVos = weekClassesList;
//        }
//
//        //排序
//        ArrayList<nameHours> hoursArrayList = new ArrayList<>();
//        for (int i = 0; i < names.size(); i++) {
//            nameHours nameHours = new nameHours();
//            nameHours.setEmployeeName(names.get(i)); //设置姓名
//            String[] strings = new String[7];      //每周时间段字符串
//            for (int j = 0; j < scheduleVos.size(); j++) {
//                java.util.Date Date = new java.util.Date(scheduleVos.get(j).getScheduleDate().getTime());
//                LocalDate localDateF = Date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//
////                System.out.println(localDateF);
//                if (endDays.compareTo(localDateF) >= 0 && startDays.compareTo(localDateF) <= 0) {  //7天
//                    if (nameHours.getEmployeeName().equals(scheduleVos.get(j).getEmployeeName())) {
//                        if (nameHours.getPosition() == null) { //设置岗位
//                            nameHours.setPosition(scheduleVos.get(j).getPosition());
//                        }
//                        java.util.Date utilDate = new java.util.Date(scheduleVos.get(j).getScheduleDate().getTime());
//                        LocalDate loDate = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//                        DayOfWeek week = loDate.getDayOfWeek();
//                        int index = week.getValue();  //1到7
//
//                        if (strings[index - 1] != null) {  //如果该位置有值则拼接
//                            strings[index - 1] = strings[index - 1] + " " + scheduleVos.get(j).getStartTime() + "-" + scheduleVos.get(j).getEndTime();
//                        } else { //没有则存入
//                            strings[index - 1] = scheduleVos.get(j).getStartTime() + "-" + scheduleVos.get(j).getEndTime();
//                        }
//                    }
//                }
//            }
//            nameHours.setWorkTime(strings);
//            hoursArrayList.add(nameHours);
//        }
//
//        for (int i = 0; i < hoursArrayList.size(); i++) {
//            for (int j = 0; j < hoursArrayList.get(i).getWorkTime().length; j++) {
//                if (hoursArrayList.get(i).getWorkTime()[j]!=null&&hoursArrayList.get(i).getWorkTime()[j].length()==23){
//                    String[] split = hoursArrayList.get(i).getWorkTime()[j].split(" ");
//
//                    String time1 =split[0].substring(0, 5);
//                    String time2 = split[1].substring(0, 5);
//
//                    String time3= split[0].substring(6,11);
//                    String time4=split[1].substring(6, 11);
//
//                    String n="";
//                    if (time1.equals(time4)){
//                        n=time1.replace(time1.substring(3,5),"30");
//                    }else {
//                        n=time1;
//                    }
//                    String s="";
//                    if (time2.equals(time3)){
//                        s=time2.replace(time1.substring(3,5),"30");
//                    }else {
//                        s=time2;
//                    }
//
//                    String f=n+"-"+time3+" "+s+"-"+time4;
//
//                    hoursArrayList.get(i).getWorkTime()[j]=f;
//                }
//            }
//
//        }
//
//        return R.ok().data("total", hoursArrayList.size()).data("hoursList", hoursArrayList);
//    }
//
//
//    //修改排班
//    @PostMapping("updateScheduleList/{selectDateString}/{shopId}")
//    public R updateScheduleList(@RequestBody List<dayNameHours> dayNameHours,@PathVariable String selectDateString,@PathVariable String shopId){
//        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        targetFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); //设置时区
//        Date newDate = null;
//        try {
//            newDate = targetFormat.parse(selectDateString);
////            System.out.println(newDate); // 输出转换后的Date对象
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        LocalDate localDateF = newDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//        //定位到哪段数据
//        int numUp=0;  //最后一个索引
//        int numDown=0;//第一个索引
//        LocalDate l=LocalDate.of(2023,1,30);
//        for (int i = 0; i < 35; i++) { //用于判断b的值
//            LocalDate date=l.plusDays(i);
//            QueryWrapper<Classes> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("create_data",date);
//            queryWrapper.eq("shop_id",shopId);
//            queryWrapper.orderByAsc("start_time");
//            int size = classesService.list(queryWrapper).size();
//            numDown=numUp;
//            numUp=size+numUp;
//            if (localDateF.equals(date)){
//                break;
//            }
//        }
//
//        //根据员工姓名  首先确保员工姓名唯一 以后再看看要不要使用id
//        for (int i = 0; i < dayNameHours.size(); i++) {
//            //将集合转为字符串
//            String newStr="";
//            for (int j = 0; j < dayNameHours.get(i).getIsHaveWork().length; j++) {
//                newStr=newStr+dayNameHours.get(i).getIsHaveWork()[j];
//            }
//
//            //根据姓名查询id
//            QueryWrapper<ServiceEmployee> employeeQueryWrapper = new QueryWrapper<>();
//            employeeQueryWrapper.eq("name",dayNameHours.get(i).getEmployeeName());
//            employeeQueryWrapper.eq("shop_id",shopId);
//            String id = serviceEmployeeService.getOne(employeeQueryWrapper).getId();
//
//            //根据id查询排班字符数组
//            QueryWrapper<Scheduling> schedulingQueryWrapper = new QueryWrapper<>();
//            schedulingQueryWrapper.eq("employee_id",id);
//            schedulingQueryWrapper.eq("sign",shopId);
//            String rosterString = schedulingService.getOne(schedulingQueryWrapper).getRosterString();
//            String oldStr =rosterString.substring(numDown,numUp);
//            if (!newStr.equals(oldStr)){
//                rosterString = rosterString.replaceAll(oldStr, newStr);
//
//                Scheduling scheduling = schedulingService.getOne(schedulingQueryWrapper).setRosterString(rosterString);
//
//                //更新数据
//                boolean b = schedulingService.updateById(scheduling);
//                if (!b){
//                    return R.error();
//                }
//            }
//        }
//        return R.ok();
//    }
//
//
//}
//
