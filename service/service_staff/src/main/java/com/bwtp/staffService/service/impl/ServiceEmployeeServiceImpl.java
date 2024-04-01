package com.bwtp.staffService.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bwtp.commonbases.exceptionhandler.BwtpException;
import com.bwtp.commonutils.JWT;
import com.bwtp.commonutils.MD5;
import com.bwtp.staffService.entity.PassengerFlow;
import com.bwtp.staffService.entity.ServiceEmployee;

import com.bwtp.staffService.entity.ServiceShop;
import com.bwtp.staffService.entity.vo.StaffShop;
import com.bwtp.staffService.entity.vo.queryEmployee;
import com.bwtp.staffService.listener.PassengerExcelListener;
import com.bwtp.staffService.mapper.ServiceEmployeeMapper;
import com.bwtp.staffService.service.ServiceEmployeeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bwtp.staffService.service.ServiceShopService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author blp
 * @since 2022-12-27
 */
@Service
public class ServiceEmployeeServiceImpl extends ServiceImpl<ServiceEmployeeMapper, ServiceEmployee> implements ServiceEmployeeService {
    private String loginToken;

    @Resource
    private ServiceShopService shopService;

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public String getLoginToken() {
        return loginToken;
    }
    @Override
    public String getToken(String username, String password) {

        //通过查寻用户数据库判断用户名及密码是否正确
        //不为空则查询数据库
        if (!ObjectUtils.isEmpty(username) && !ObjectUtils.isEmpty(password)) {
            //根据用户名查询密码
            QueryWrapper<ServiceEmployee> wrapper = new QueryWrapper<>();
            wrapper.eq("email", username);

            List<ServiceEmployee> employees = baseMapper.selectList(wrapper);
            if (employees.size() != 0 && !ObjectUtils.isEmpty(employees.get(0).getPwd())) {
//                System.out.println(employees.get(0).getId());

                String encrypt = MD5.encrypt(password);
                if (encrypt.equals(employees.get(0).getPwd())) {
                    //验证成功返回token
                    //id加用户名，即电子邮箱
                    String token = JWT.getJwtToken(employees.get(0).getId(), username);
                    setLoginToken(token);
                    return token;
                }
            }
        }
        return null;
    }

    @Override
    public Page getEmployeePage(Long current, Long limit, queryEmployee queryEmployee) {


        QueryWrapper<ServiceEmployee> queryWrapper = new QueryWrapper<>();
        Page<ServiceEmployee> employeePage = new Page<>(current, limit);

        String name = queryEmployee.getName();
        String position = queryEmployee.getPosition();
        String shopId = queryEmployee.getShopId();

        if (!ObjectUtils.isEmpty(name)){
            queryWrapper.like("name",name);
        }
        if (!ObjectUtils.isEmpty(position)){
            queryWrapper.eq("position",position);
        }
        if (!ObjectUtils.isEmpty(shopId)){
            queryWrapper.eq("shop_id",shopId);
        }

        //根据员工id查询门店id
        String s = getLoginToken();
        String id = JWT.getMemberIdByToken(s);
        //TODO 要改


        baseMapper.selectPage(employeePage,queryWrapper);
        return employeePage;
    }

    @Override
    public List<ServiceEmployee> getAllEmployeeList() {
        List<ServiceEmployee> list = baseMapper.selectList(null);
        return list;
    }


    @Override
    public boolean updateEmployee(StaffShop staffShop) {
        ServiceEmployee serviceEmployee = new ServiceEmployee();

        serviceEmployee.setId(staffShop.getId());
        serviceEmployee.setName(staffShop.getName());
        serviceEmployee.setEmail(staffShop.getEmail());
        serviceEmployee.setAvatar(staffShop.getAvatar());
        serviceEmployee.setPosition(staffShop.getPosition());

        QueryWrapper<ServiceShop> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_name",staffShop.getShopName());
        serviceEmployee.setShopId(shopService.getOne(queryWrapper).getId());

        int i = baseMapper.updateById(serviceEmployee);

        return i>0;
    }


    @Override
    public void addPassenger(String file) {
            try {
                //调用方法进行读取
                EasyExcel.read(file, PassengerFlow.class,new PassengerExcelListener()).sheet().doRead();
            }catch(Exception e){
                e.printStackTrace();
            }
    }


}
