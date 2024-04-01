package com.bwtp.staffService.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bwtp.staffService.entity.ServiceEmployee;
import com.bwtp.staffService.entity.ServiceShop;
import com.bwtp.staffService.entity.vo.StaffShop;
import com.bwtp.staffService.entity.vo.queryEmployee;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author blp
 * @since 2022-12-27
 */
public interface ServiceEmployeeService extends IService<ServiceEmployee> {

    String getToken(String username, String password);

    Page getEmployeePage(Long current, Long limit, queryEmployee queryEmployee);

    List<ServiceEmployee> getAllEmployeeList();


    boolean updateEmployee(StaffShop staffShop);

    void addPassenger(String file);
}
