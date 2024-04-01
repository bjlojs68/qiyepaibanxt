package com.bwtp.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bwtp.staffService.entity.Classes;
import com.bwtp.staffService.entity.Scheduling;
import com.bwtp.staffService.service.ClassesService;
import com.bwtp.staffService.service.SchedulingService;
import com.bwtp.staffService.service.ServiceEmployeeService;
import com.bwtp.staffService.staffApplication;
import com.bwtp.staffService.utils.MyUserInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = staffApplication.class)
public class m {

    @Resource
    private ClassesService classesService;
    @Resource
    private SchedulingService schedulingService;
    @Resource
    private ServiceEmployeeService serviceEmployeeService;

    @Test
    public void c() {

        Scheduling scheduling = schedulingService.getById("1653258981960331266");
        String rosterString = scheduling.getRosterString();

        /**rosterString是一个二进制字符串，他是根据classes班次模板得到的，0为有排班，1位无排班*/
        //获取当前周
        LocalDate currentDate = MyUserInfo.currentDate; // 获取当前日期

//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 创建自定义格式的日期时间格式化器
//        String formattedDate = currentDate.format(formatter); // 格式化日期为字符串
        //获取当前为周几
        int dayOfWeek = currentDate.getDayOfWeek().getValue();
        //根据当前周获取周班次
        /**
         * 7做闭环，直到
         * */

        //日期集合
        List<String> strings = new ArrayList<>();

        int num = 0;

        int dayValue = currentDate.getDayOfWeek().getValue();

        LocalDate currentDateM;
        LocalDate currentDateP;
        LocalDate startLocalDate = currentDate.minusDays(dayValue);

        Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        //获取日期集合
        while (num != 7) {


            //获取当天的日期后，后面的日期与7比较，
            //添加日期
            for (int j = dayValue - 1; j >= 0; j--) {
                num++;
                currentDateM = currentDate.minusDays(j);
                strings.add(currentDateM.toString());
            }

            for (int i = 1; i < 8 - dayValue; i++) {
                num++;
                currentDateP = currentDate.plusDays(i);
                strings.add(currentDateP.toString());
            }

        }


        //获取固定班次
        QueryWrapper<Classes> classesQueryWrapper = new QueryWrapper<>();
        classesQueryWrapper.eq("shop_id", 1);
        classesQueryWrapper.orderByAsc("create_data");
        List<Classes> classesList = classesService.list(classesQueryWrapper);
        //获取周固定班次范围
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format);


        int pre = 0, next = 0;
        try {
            Date parseStart = sdf.parse(strings.get(0));

            for (int i = 0; i < classesList.size(); i++) {
                if (classesList.get(i).getCreateData().compareTo(parseStart) == 0) {
                    pre = i;
                    break;
                }
            }
            Date parseEnd = sdf.parse(strings.get(strings.size() - 1));
            for (int i = 0; i < classesList.size(); i++) {
                if (classesList.get(i).getCreateData().compareTo(parseEnd) > 0) {
                    next = i;
                    break;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //范围是pre<= d < next
        String s = "";
        LinkedHashMap<String, String> dateTimeHashMap = new LinkedHashMap<>();
        System.out.println(classesList.get(118));
        for (int i = 0; i < strings.size(); i++) {
            s = "";
            for (int j = pre; j < next; j++) {



                try {
                    if (sdf.parse(strings.get(i)).compareTo(classesList.get(j).getCreateData()) == 0) {
                        if (rosterString.getBytes()[j] == '1') {
                            if (!s.isEmpty()){
                                s=s+"--";
                            }
                            s = s+classesList.get(j).getStartTime().toString()+"-"+ classesList.get(j).getEndTime().toString();
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            dateTimeHashMap.put(strings.get(i), s);
        }

    }


}
