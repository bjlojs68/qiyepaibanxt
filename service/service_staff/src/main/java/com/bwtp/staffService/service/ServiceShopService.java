package com.bwtp.staffService.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bwtp.staffService.entity.ServiceEmployee;
import com.bwtp.staffService.entity.ServiceShop;
import com.bwtp.staffService.entity.vo.StaffShop;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author blp
 * @since 2022-12-17
 */
public interface ServiceShopService extends IService<ServiceShop> {

    boolean deleteShopById(String id);

    List<StaffShop> changeShopName(List<ServiceEmployee> employees);

    StaffShop changeShopName(ServiceEmployee employee);
}
