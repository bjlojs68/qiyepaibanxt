package com.bwtp.staffService.listener;
import java.util.Date;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.bwtp.commonbases.exceptionhandler.BwtpException;
import com.bwtp.staffService.entity.PassengerFlow;
import com.bwtp.staffService.service.PassengerFlowService;

public class PassengerExcelListener extends AnalysisEventListener<PassengerFlow> {


    private PassengerFlowService passengerFlowService;
    public PassengerExcelListener() {}
    public PassengerExcelListener(PassengerFlowService passengerFlowService) {
        this.passengerFlowService = passengerFlowService;
    }

    //监听获取的数据  在这里进行添加数据到数据库中
    @Override
    public void invoke(PassengerFlow data, AnalysisContext analysisContext) {

        System.out.println(data);

//        passengerFlowService.save(data);
//        //判断excel表中是否有值
//        if (data==null){
//            throw new BwtpException(20001,"excel为空");
//        }else {
//            passengerShop shop = new passengerShop();
//            shop.setShopId(data.getShopId());
//            shop.setDate(data.getDate());
//            shop.setStartTime(data.getStartTime());
//            shop.setEndTime(data.getEndTime());
//            shop.setForecastPassenger(data.getForecastPassenger());
//            System.out.println(shop);
//            passengerFlowService.save(shop);
//        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
