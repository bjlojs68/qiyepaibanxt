package com.bwtp.staffService.utils;

import com.bwtp.commonbases.exceptionhandler.BwtpException;
import com.bwtp.staffService.entity.Classes;
import com.bwtp.staffService.entity.PassengerFlow;
import com.bwtp.staffService.service.ServiceShopService;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
//一天的班次安排
public class scheduleDayUtil {

    @Resource
    private ServiceShopService shopService;

    //TODO   后面统一用一个service来设置常量
    //上班时间
    private LocalTime workTime;
    //下班时间
    private LocalTime closeTime;
    //门店面积/50 = 人数
    private Integer divisorConstant;


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

    //通过客流量获取初步排班数据
    public void getEmployeeNumber(List<PassengerFlow> passengerShops) {

        PassengerFlow passengerFlow = passengerShops.get(0);
        String shopId = passengerFlow.getShopId();
        LocalDate date = passengerFlow.getDate();
        LocalTime flowStartTime = passengerFlow.getStartTime();

        //班次
        List<Classes> classesArrayList = new ArrayList<>();
        //暂存
        List<Classes> temporaryClassesList = new ArrayList<>();
        //开始时间
        LocalTime startTime;
        //结束时间
        LocalTime endTime;

        //客流规则
        //公式：预测客流/3.8 = 店员需求数  0默认店员为1   3.8可调整，后期在更改   TODO
        List<Integer> number = new ArrayList<>();
        List<Integer> employeeNumber = new ArrayList<>();


        for (PassengerFlow passengerShop : passengerShops) {
            Float forecastPassenger = passengerShop.getForecastPassenger();
            int a = (int) (forecastPassenger / 3.8) + 1;   //0客流量店员为1  3.8  TODO
            number.add(a);
        }

        //取平均值
        for (int i = 0; i < number.size(); i++) {
            employeeNumber.add(((number.get(i) + number.get(i + 1)) + 1) / 2);   //想上取整(x+y-1)/y
            i++;
        }

        //[1, 2, 4, 5, 6, 7, 5, 5, 4, 3, 2, 1, 1]
        for (int i = 0; i < passengerShops.size(); i++) {
            Classes classes = new Classes();
            classes.setStartTime(passengerShops.get(i).getStartTime());
            classes.setEndTime(passengerShops.get(i + 1).getEndTime());
            classes.setEmployeeNumber(employeeNumber.get(i / 2));
            temporaryClassesList.add(classes);
            i++;
        }

        /**开店规则

         a. 每天开店之前需要一小时做准备工作（如做清洁）。1为缺省值，可调整 。

         i. 公式：门店面积/50 = 人数。50为缺省值，可调整*/

        businessHour(date);//调用门店营业时间规则方法

        //计算需多少个人用于开店前准备  门店面积/50 = 人数
        setDivisorConstant(100);//TODO  1000为缺省值，可调整
        int beforeOpenNumber = shopService.getById(shopId).getSize() / getDivisorConstant();
        employeeNumber.add(0, beforeOpenNumber);
        Classes classesOpen = new Classes();
        classesOpen.setStartTime(workTime.minusHours(1));
        classesOpen.setEndTime(workTime);
        classesOpen.setEmployeeNumber(beforeOpenNumber);
        temporaryClassesList.add(0, classesOpen);

/**       **关店规则**

 d. 每天关店之后需要2小时做收尾工作（如盘点、清算、清洁）。==2为缺省值。可调整==

 i. 公式：门店面积/80 + 1= 人数。==80和1为缺省值。可调整==

 ii. 用户可以设置允许执行此类工作的职位。可以设置为所有职位，也可以限制特定职位（比如导购人员、收银，店经理等）*/
        Classes classesClose = new Classes();
        classesClose.setStartTime(closeTime);
        //TODO 2为缺省值   80和1为缺省值。可调整
        classesClose.setEndTime(closeTime.plusHours(2));
        Integer a=(shopService.getById(shopId).getSize() + 80 -1)/80+ 1;  //向上取整
        classesClose.setEmployeeNumber(a);
        employeeNumber.add(a);
        temporaryClassesList.add(classesClose);


        /** 工作时长规则    TODO

         a. 员工每周最多工作40小时

         b. 员工每天最多工作８小时

         c. 单个班次最少2小时，最多4小时。员工可以连续排多个班次

         d. 员工最长连续工作时长：4小时。达到连续工作时长，必须安排休息时间

         e. 必须给工作时间完全覆盖午餐、晚餐时间的员工，安排午餐或晚餐时间*/


        int num = 0, count = 0;
        boolean start;
        if (temporaryClassesList.isEmpty()) {
            throw new BwtpException();
        } else {
            //排班次  先不管偏好规则
            //temporaryClassesList存储着每个小时内所需的店员数量，
//             对temporaryClassesList进行遍历，根据第一个时间段的开始时间作为第一个班次的开始时间，再对

            for (int i = 0; i < temporaryClassesList.size(); i++) {
                start=true;
                Classes classes = new Classes();
                Integer employeeNumberT = temporaryClassesList.get(i).getEmployeeNumber();
                startTime = temporaryClassesList.get(i).getStartTime();
                endTime = startTime.plusHours(4);
                for (int j = i; j < temporaryClassesList.size(); j++) {
                    if (!((temporaryClassesList.get(j).getStartTime().getHour()-startTime.getHour())<=2)) {
                        //该小时段内的人数等于employeeNumberT  直接往下循环
                        //该小时段内的人数大于employeeNumberT
                        if (temporaryClassesList.get(j).getEmployeeNumber() >=(employeeNumberT-count)){
                            num++;
                            if (num == 3) {
                                classes.setStartTime(startTime);
                                classes.setEndTime(endTime);
                                classes.setEmployeeNumber(employeeNumberT);
                                classesArrayList.add(classes);
                                count=employeeNumberT;
                                num=0;
                                //满4退出内循环
                                break;
                            }
                        }else  {
                            Integer b=temporaryClassesList.get(j).getStartTime().getHour()-startTime.getHour();
                            Integer c=temporaryClassesList.get(j).getEmployeeNumber()-(employeeNumberT-count);
                            classes.setStartTime(startTime);
                            classes.setEndTime(startTime.plusHours(b));
                            classes.setEmployeeNumber(c);
                            employeeNumberT=employeeNumberT-c;
                            classesArrayList.add(classes);
                        }
                        //获取下一轮外循环的下标
                        if (start&&temporaryClassesList.get(j).getEmployeeNumber() >=(employeeNumberT-count)){
                            start=false;
                            i=j;
                        }

                    }
                }
            }


//            classes.setEndTime(endTime);
//            classes.setEmployeeNumber();
        }
    }


    /**
     * 门店营业时间规则
     * a. 周一~周五：早9点~晚9点
     * b. 周末：早10点~晚10点
     */
    public void businessHour(LocalDate date) {

        if (ObjectUtils.isEmpty(date)) {
            throw new BwtpException();
        } else {
            //该日期是周几
            DayOfWeek week = date.getDayOfWeek();
            int index = week.getValue();

            if (index != 6 && index != 7) {
                //TODO 不确定，先按8点来
                setWorkTime(LocalTime.of(8, 0));
                setCloseTime(LocalTime.of(21, 0));
            } else {
                setWorkTime(LocalTime.of(10, 0));
                setCloseTime(LocalTime.of(22, 0));
            }
        }
    }


/**   C.休息时间段:    TODO

 a. 午餐时间：时间范围（如11点到14点，一小时）

 b. 晚餐时间：时间范围（如17点到20点，半小时） c. 休息时间：时间范围（不限。半小时）*/


}
