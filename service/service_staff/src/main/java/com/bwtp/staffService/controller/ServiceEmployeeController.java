package com.bwtp.staffService.controller;
import java.util.Date;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bwtp.commonbases.exceptionhandler.BwtpException;
import com.bwtp.commonutils.MD5;
import com.bwtp.commonutils.R;
import com.bwtp.staffService.entity.ServiceEmployee;
import com.bwtp.staffService.entity.ServiceShop;
import com.bwtp.staffService.entity.vo.StaffShop;
import com.bwtp.staffService.entity.vo.queryEmployee;
import com.bwtp.staffService.service.ServiceEmployeeService;
import com.bwtp.staffService.service.ServiceShopService;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author blp
 * @since 2022-12-27
 */

//员工信息管理
@RestController
@RequestMapping("/shopService/serviceEmployee")
@CrossOrigin
public class ServiceEmployeeController{

    String file="C:\\Users\\99456\\Desktop\\数据.xlsx";

    @Resource
    private ServiceEmployeeService employeeService;

    @Resource
    private ServiceShopService shopService;

    //增加员工
    @PostMapping("addEmployee")
    public R addEmployee(@RequestBody StaffShop staffShop){
        ServiceEmployee serviceEmployee = new ServiceEmployee();

        serviceEmployee.setName(staffShop.getName());
        serviceEmployee.setEmail(staffShop.getEmail());
        serviceEmployee.setAvatar(staffShop.getAvatar());
        serviceEmployee.setPosition(staffShop.getPosition());

        QueryWrapper<ServiceShop> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_name",staffShop.getShopName());
        serviceEmployee.setShopId(shopService.getOne(queryWrapper).getId());
        boolean b = employeeService.save(serviceEmployee);

        if (b){
            return R.ok();
        }else {
            return R.error();
        }
    }

    //删除员工
    @DeleteMapping("deleteEmployee/{id}")
    public R deleteEmployee(@PathVariable String id){
        boolean b = employeeService.removeById(id);
        if (b){
            return R.ok();
        }else {
            return R.error();
        }
    }

    //多条件组合查询
    @PostMapping("queryEmployeeList/{current}/{limit}")
    public R queryEmployeeList(@PathVariable Long current,
                               @PathVariable Long limit,
                               @RequestBody(required = false) queryEmployee queryEmployee){

        Page employeePage=employeeService.getEmployeePage(current,limit,queryEmployee);
        List<StaffShop> staffShopList=shopService.changeShopName(employeePage.getRecords());
        //就是要总数
        long allTotal = employeePage.getTotal();


        return R.ok().data("employees",staffShopList).data("allTotal",allTotal);
    }

    //获取全部数据
    @PostMapping("getAllEmployeeList")
    public R getAllEmployeeList(){
        List<ServiceEmployee> list=employeeService.getAllEmployeeList();

        //获取员工姓名
        QueryWrapper<ServiceEmployee> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("shop_id",1);
        queryWrapper.orderByAsc("position");

        List<ServiceEmployee> list1 =employeeService.list(queryWrapper);
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < list1.size(); i++) {
            names.add(list1.get(i).getName());
        }

        return R.ok().data("EmployeeList",list).data("names",names);
    }

    //根据id获取员工信息
    @PostMapping("getEmployeeShopById/{id}")
    public R getEmployeeById(@PathVariable Long id){
        if (!ObjectUtils.isEmpty(id)) {
            ServiceEmployee employee = employeeService.getById(id);
            StaffShop staffShop=shopService.changeShopName(employee);
            return R.ok().data("staffShop", staffShop);
        }else {
            throw new BwtpException();
        }
    }


    //修改员工信息
    @PostMapping("updateServiceEmployee")
    public R updateServiceEmployeeById(@RequestBody StaffShop staffShop){

        boolean b=employeeService.updateEmployee(staffShop);
        if (b){
            return R.ok();
        }else {
            return R.error();
        }
    }


}

