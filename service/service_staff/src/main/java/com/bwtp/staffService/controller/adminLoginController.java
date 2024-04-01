package com.bwtp.staffService.controller;

import com.bwtp.commonutils.JWT;
import com.bwtp.commonutils.R;
import com.bwtp.staffService.entity.ServiceEmployee;
import com.bwtp.staffService.service.ServiceEmployeeService;
import com.bwtp.staffService.utils.MyUserInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

//管理员登录控制
@RestController
@RequestMapping("/shopService/adminUser")
@CrossOrigin  //解决跨域访问
public class adminLoginController {

    @Resource
    private ServiceEmployeeService employeeService;

    //登录
    @PostMapping("login/{username}/{password}")
    public R login(@PathVariable String username,@PathVariable String password){
       String token= employeeService.getToken(username,password);

       if (token==null){
           return R.error();
       }else {
           return R.ok().data("token",token);
       }
    }

    //获取用户信息   TODO
    @GetMapping("info/{token}")
    public R grtUserInfo(@PathVariable String token){
        String id = JWT.getMemberIdByToken(token);

        //根据id获取员工
        ServiceEmployee byId = employeeService.getById(id);
        MyUserInfo.serviceEmployee=byId;
        String avatar = byId.getAvatar();
        return R.ok().data("roles",byId.getName()).data("name","admin").data("avatar",avatar).data("employee",byId);
    }
}
