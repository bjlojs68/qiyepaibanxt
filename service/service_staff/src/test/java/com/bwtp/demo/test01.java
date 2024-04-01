package com.bwtp.demo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bwtp.commonbases.exceptionhandler.BwtpException;
import com.bwtp.staffService.entity.Classes;
import com.bwtp.staffService.entity.PassengerFlow;
import com.bwtp.staffService.service.ClassesService;
import com.bwtp.staffService.service.PassengerFlowService;
import com.bwtp.staffService.service.RuleTypeService;
import com.bwtp.staffService.service.ServiceShopService;
import com.bwtp.staffService.staffApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = staffApplication.class)
public class test01 {

    @Resource
    private ServiceShopService shopService;

    //规则类型
    @Resource
    private RuleTypeService ruleTypeService;

    @Resource
    private ClassesService classesService;


    private String shopId="1";
    //TODO   后面统一用一个service来设置常量
    //上班时间
    private LocalTime workTime;
    //下班时间
    private LocalTime closeTime;
    //门店面积/50 = 人数
    private Integer divisorConstant;
    //

    public Integer getDivisorConstant() {
        return divisorConstant;
    }

    public void setDivisorConstant(Integer divisorConstant) {
        this.divisorConstant = divisorConstant;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public LocalTime getWorkTime() {
        return workTime;
    }

    public void setWorkTime(LocalTime workTime) {
        this.workTime = workTime;
    }

    @Resource
    PassengerFlowService service;

    /**
     * 用于处理班次，并将班次存入数据库中
     * */

    public List<Classes> t(LocalDate data) {
        //门店的id
        int id=1;

        //日期
        LocalDate setDate = data;

        Date localToDataUtil= Date.from(data.atStartOfDay(ZoneId.systemDefault()).toInstant());
        //将 java.util.Date 格式转换成 java.sql.Date 格式
        long longTime = localToDataUtil.getTime();
        java.sql.Date localToData = new java.sql.Date(longTime);
        //number存预测店员数
        List<Integer> number = new ArrayList<>();
        //employeeNumber存平均值  一个小时所需店员数
        List<Integer> employeeNumber = new ArrayList<>();
        //以日期来获取数据
        QueryWrapper<PassengerFlow> wrapper = new QueryWrapper<>();
        wrapper.eq("Date", setDate);
        wrapper.orderByAsc("start_time");  //排序，从小到大
        List<PassengerFlow> passengerShops = service.list(wrapper);

        //计算预测店员数
        DecimalFormat format = new DecimalFormat("#.000");  //控制精度
        for (PassengerFlow passengerShop : passengerShops) {
            Float forecastPassenger = passengerShop.getForecastPassenger();

            // x / y + (x % y != 0 ? 1 : 0);
            double c = forecastPassenger / 3.8;  //3.8 TODO
            String s = format.format(c);
            double v = Double.parseDouble(s);
            int ceil = ((v - (int) (c)) == 0 ? (int) (c) : (int) Math.ceil(c));
            number.add(ceil);
        }

        //取平均值
        for (int i = 0; i < number.size(); i++) {
            employeeNumber.add((int) Math.ceil((number.get(i) + number.get(i + 1)) / 2.0));  //想转为double类型才能使用Math.ceil
            i++;
        }


        ArrayList<Classes> temporaryClassesList = new ArrayList<>();
        for (int i = 0; i < passengerShops.size(); i++) {
            Classes classes = new Classes();
            classes.setStartTime(passengerShops.get(i).getStartTime());
            classes.setEndTime(passengerShops.get(i + 1).getEndTime());
            classes.setEmployeeNumber(employeeNumber.get(i / 2));
            temporaryClassesList.add(classes);
            i++;
        }
        PassengerFlow passengerFlow = passengerShops.get(0);
        String shopId = passengerFlow.getShopId();

        LocalDate date = passengerFlow.getDate();

        if (ObjectUtils.isEmpty(date)) {
            throw new BwtpException();
        } else {
            //该日期是周几
            DayOfWeek week = date.getDayOfWeek();
            int index = week.getValue();

            if (index != 6 && index != 7) {
                setWorkTime(LocalTime.of(9, 0));
                setCloseTime(LocalTime.of(21, 0));
            } else {
                setWorkTime(LocalTime.of(10, 0));
                setCloseTime(LocalTime.of(22, 0));
            }
        }
        //计算需多少个人用于开店前准备  门店面积/100 = 人数
        setDivisorConstant(100);//TODO  100为缺省值，可调整

        int beforeOpenNumber = (int) Math.ceil((double) shopService.getById(shopId).getSize() / getDivisorConstant());
        Classes classes1 = new Classes();
        classes1.setStartTime(workTime.minusHours(1));
        classes1.setEndTime(workTime);
        classes1.setEmployeeNumber(beforeOpenNumber);
        temporaryClassesList.add(0, classes1);

        //关店规则
        Integer a = (shopService.getById(shopId).getSize() + 100 - 1) / 100 + 1;  //向上取整  //TODO 2为缺省值   80和1为缺省值。可调整
        employeeNumber.add(a);
        for (int i = 0; i < 2; i++) {
            Classes classesClose = new Classes();
            closeTime = closeTime.plusHours(i);
            classesClose.setStartTime(closeTime);

            classesClose.setEndTime(closeTime.plusHours(1));
            classesClose.setEmployeeNumber(a);
            temporaryClassesList.add(classesClose);
        }
        //班次
        List<Classes> classesArrayList = new ArrayList<>();
        //开始时间
        LocalTime startTime;
        //结束时间
        LocalTime endTime;
        //employeeNumberClasses要存入classesArrayList的店员数  employeeNumberT下个时间段所需店员数
        int num = 0, employeeNumberClasses, employeeNumberT, beforeEmployeeNumberT;
        int i, j, count = -1;
        boolean start;
//        System.out.println(temporaryClassesList);
        if (temporaryClassesList.isEmpty()) {
            throw new BwtpException();
        } else {
            //排班次  先不管偏好规则
            //temporaryClassesList存储着每个小时内所需的店员数量，
//             对temporaryClassesList进行遍历，根据第一个时间段的开始时间作为第一个班次的开始时间，再对

            for (i = 0; i < temporaryClassesList.size(); i++) {
                start = true;
                employeeNumberClasses = temporaryClassesList.get(i).getEmployeeNumber();
                startTime = temporaryClassesList.get(i).getStartTime();
                for (int k = 0; k < classesArrayList.size(); k++) {
                    if (startTime.getHour() >= classesArrayList.get(k).getStartTime().getHour()
                            && startTime.getHour() <=classesArrayList.get(k).getEndTime().minusMinutes(15).getHour()) {
                        employeeNumberClasses = employeeNumberClasses - classesArrayList.get(k).getEmployeeNumber(); }
                }
                endTime = startTime.plusHours(4);
                for (j = i; j < temporaryClassesList.size(); j++) {
                    if (((temporaryClassesList.get(j).getStartTime().getHour() - startTime.getHour()) < 4)
                            && temporaryClassesList.get(j).getStartTime().getHour() != startTime.getHour()) {
                        employeeNumberT = temporaryClassesList.get(j).getEmployeeNumber();
                        if (i != j) {
                            for (int k = 0; k < classesArrayList.size(); k++) {
                                if (temporaryClassesList.get(j).getStartTime().getHour() >= classesArrayList.get(k).getStartTime().getHour()
                                        && temporaryClassesList.get(j).getStartTime().getHour()
                                        <=classesArrayList.get(k).getEndTime().minusMinutes(15).getHour()) {
                                    employeeNumberT = employeeNumberT - classesArrayList.get(k).getEmployeeNumber(); } } }
                        //获取下一轮外循环的下标
                        if (start && employeeNumberT > employeeNumberClasses) {
                            start = false;i = j - 1;count = i;
                        } else if (start && num == 2) { i = j; }
                        if (employeeNumberT >= employeeNumberClasses
                                || ((temporaryClassesList.get(j).getStartTime().getHour() - startTime.getHour()) < 2)) {
                            num++;
                            if (temporaryClassesList.size() - i > 2 && num == 3) {
                                Classes classes = new Classes();
                                classes.setStartTime(startTime);
                                classes.setEndTime(endTime);
                                classes.setEmployeeNumber(employeeNumberClasses);
                                classes.setCreateData(localToData);
                                classesArrayList.add(classes);
                                num = 0;
                                //满4退出内循环
                                break;
                            } else if (temporaryClassesList.size() - i == 2) {  //这个2为打扫店时间 // 2 TODO
                                //下班时间段内店员数减去已存入classesArrayList相应时间段的人数想等的话存入employeeNumberClasses，
                                // 如果不等则存入employeeNumberT，且去除已存入classesArrayList相应的时间段
                                Classes classes = new Classes();
                                classes.setStartTime(startTime);
                                classes.setEndTime(startTime.plusHours(2));  //2 TODO
                                classes.setCreateData(localToData);
//                                System.out.println(employeeNumberT);
//                                System.out.println(employeeNumberClasses);
                                if (employeeNumberT!=employeeNumberClasses){

                                    //去除重复
                                    for (int k = 0; k < classesArrayList.size(); k++) {
                                        if (startTime.getHour() >= classesArrayList.get(k).getStartTime().getHour()
                                                && startTime.getHour() < classesArrayList.get(k).getEndTime().getHour()) {
                                            Classes classesA = classesArrayList.get(k);
                                            classesA.setEndTime(startTime);
                                            classesArrayList.set(k,classesA);
                                        }
                                    }
                                    classes.setEmployeeNumber(employeeNumberT);
                                }else {
                                    classes.setEmployeeNumber(employeeNumberClasses);
                                }
                                classesArrayList.add(classes);

                                num = 0;
                                break;

                            } else if (num == 2 && (count + 1) == (j - 1)) {
                                //获取前一时间段所需店员数
                                beforeEmployeeNumberT = temporaryClassesList.get(j - 1).getEmployeeNumber();

                                for (int k = 0; k < classesArrayList.size(); k++) {
                                    if (temporaryClassesList.get(j - 1).getStartTime().getHour() >= classesArrayList.get(k).getStartTime().getHour()
                                            && temporaryClassesList.get(j-1).getStartTime().getHour()<=classesArrayList.get(k).getEndTime().minusMinutes(15).getHour()) {
                                        beforeEmployeeNumberT = beforeEmployeeNumberT - classesArrayList.get(k).getEmployeeNumber();
                                    }
                                }
                                if ((beforeEmployeeNumberT - employeeNumberClasses) >= employeeNumberT) {
                                    Classes classes = new Classes();
                                    classes.setStartTime(startTime);
                                    classes.setEndTime(temporaryClassesList.get(j - 1).getEndTime());
                                    classes.setEmployeeNumber(employeeNumberClasses);
                                    classes.setCreateData(localToData);
                                    classesArrayList.add(classes);
                                    num = 0;
                                    break;
                                }
                            }
                        } else {
                            Classes classesT = new Classes();
                            Integer b = temporaryClassesList.get(j).getStartTime().getHour() - startTime.getHour();
                            Integer c = employeeNumberClasses - employeeNumberT;
                            classesT.setStartTime(startTime);
                            classesT.setEndTime(startTime.plusHours(b));
                            classesT.setEmployeeNumber(c);
                            classesT.setCreateData(localToData);
                            classesArrayList.add(classesT);
                            employeeNumberClasses = employeeNumberClasses - c;
                            if (employeeNumberT == 0) {
                                i = j;
                                break;
                            } else {
                                //返回上一个数
                                j--;
                            }
                        }

                    }

                }
            }
        }

        System.out.println(employeeNumber);

        return classesArrayList;

    }

    @Test
    public void test(){


        LocalDate b=LocalDate.of(2023,1,30);

        for (int i = 0; i < 35; i++) {
            LocalDate a = b.plusDays(i);
            List<Classes> t = t(a);

            for (Classes classes : t) {
                classes.setShopId(shopId);
                classesService.save(classes);
            }

        }



    }
}
