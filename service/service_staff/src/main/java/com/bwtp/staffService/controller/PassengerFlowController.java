package com.bwtp.staffService.controller;


import com.bwtp.commonutils.R;
import com.bwtp.staffService.entity.PassengerFlow;
import com.bwtp.staffService.service.PassengerFlowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 客流量信息表 前端控制器
 * </p>
 *
 * @author blp
 * @since 2023-01-31
 */
@RestController
@RequestMapping("/shopService/passengerFlow")
public class PassengerFlowController {
    @Resource
    private PassengerFlowService service;

    //获取全部数据
    @GetMapping("getAllList")
    public R getAllList(){
        List<PassengerFlow> list = service.list(null);
        return R.ok().data("list",list);
    }



}

