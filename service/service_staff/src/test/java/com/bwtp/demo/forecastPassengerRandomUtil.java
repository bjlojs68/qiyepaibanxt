package com.bwtp.demo;

import com.bwtp.commonbases.exceptionhandler.BwtpException;
import com.bwtp.staffService.entity.PassengerFlow;
import com.bwtp.staffService.service.PassengerFlowService;
import com.bwtp.staffService.staffApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;

//用于客流量生成
@RunWith(SpringRunner.class)
@SpringBootTest(classes = staffApplication.class)
public class forecastPassengerRandomUtil {

    @Resource
    private PassengerFlowService flowService;

    private LocalTime startTime;
    private LocalTime endTime;

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Test
    public void addData(){
        //从2月1号开始
        LocalDate date=LocalDate.of(2023,1,30);
        //已存1,2
        for (int i = 0; i < 35; i++) {
            int a=0,c=10;
            LocalDate plusDays = date.plusDays(i);
            int day = plusDays.getDayOfWeek().getValue();
//            System.out.println(day);
            double forecastPassenger=0;
            //中间大两边小，先试试不
            //判断周几
            //     * a. 周一~周五：早9点~晚9点
            //     * b. 周末：早10点~晚10点
            if (day!=6&&day!=7){
                setStartTime(LocalTime.of(9, 0));
                setEndTime(LocalTime.of(21, 0));
                LocalTime startTime = getStartTime();
                LocalTime endTime = getEndTime();
                while(endTime!=startTime){
                    PassengerFlow passengerFlow = new PassengerFlow();
                    //10点到15点  16到22之间
                    if (startTime.getHour()>=10&&startTime.getHour()<=15){
                        forecastPassenger=16+Math.random()*6;
                    }else if (startTime.getHour()<10){
                        forecastPassenger=a+Math.random()*2;
                        a++;
                    }else if (startTime.getHour()>15){
                        forecastPassenger=c+Math.random()*2;
                        c--;
                    }

                    BigDecimal b = new BigDecimal(forecastPassenger);
                    float floatValue = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();  //精度为一 四舍五入
                    passengerFlow.setShopId("3");   //门店id
                    passengerFlow.setDate(plusDays);
                    passengerFlow.setStartTime(startTime);
                    passengerFlow.setEndTime(startTime.plusMinutes(30));
                    passengerFlow.setForecastPassenger(floatValue);
                    startTime=startTime.plusMinutes(30);
                    flowService.save(passengerFlow);
                }
            }else {
                setStartTime(LocalTime.of(10, 0));
                setEndTime(LocalTime.of(22, 0));

                while(endTime!=startTime){
                    PassengerFlow passengerFlow = new PassengerFlow();
                    //12点到16点  17到27之间
                    if (startTime.getHour()>=12&&startTime.getHour()<=16){
                        forecastPassenger=17+Math.random()*10;
                    }else if (startTime.getHour()<12){
                        forecastPassenger=a+Math.random()*2;
                        a++;
                    }else if (startTime.getHour()>16){
                        forecastPassenger=c+Math.random()*2;
                        c--;
                    }

                    BigDecimal b = new BigDecimal(forecastPassenger);
                    float floatValue = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();  //精度为一 四舍五入
                    passengerFlow.setShopId("3");
                    passengerFlow.setDate(plusDays);
                    passengerFlow.setStartTime(startTime);
                    passengerFlow.setEndTime(startTime.plusMinutes(30));
                    passengerFlow.setForecastPassenger(floatValue);
                    startTime=startTime.plusMinutes(30);
                    flowService.save(passengerFlow);
                }
            }


        }

    }
}
