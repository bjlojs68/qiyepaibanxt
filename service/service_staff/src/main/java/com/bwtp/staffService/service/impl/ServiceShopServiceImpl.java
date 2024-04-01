package com.bwtp.staffService.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bwtp.commonbases.exceptionhandler.BwtpException;
import com.bwtp.staffService.entity.ServiceEmployee;
import com.bwtp.staffService.entity.ServiceShop;
import com.bwtp.staffService.entity.vo.StaffShop;
import com.bwtp.staffService.mapper.ServiceShopMapper;
import com.bwtp.staffService.service.ServiceShopService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author blp
 * @since 2022-12-17
 */
@Service
public class ServiceShopServiceImpl extends ServiceImpl<ServiceShopMapper, ServiceShop> implements ServiceShopService {

    @Override
    public boolean deleteShopById(String id) {
        return false;
    }

    @Override
    public List<StaffShop> changeShopName(List<ServiceEmployee> employees) {
        List<StaffShop> staffShopList=new ArrayList<>();
//        调用shop中的方法
        for (int i = 0; i < employees.size(); i++) {
            StaffShop staffShop = new StaffShop();
            ServiceEmployee list = employees.get(i);
            String id = list.getShopId();
            if (id==null){
                throw new BwtpException();
            }else {
                String name = baseMapper.selectById(id).getShopName();
                staffShop.setId(list.getId());
                staffShop.setName(list.getName());
                staffShop.setEmail(list.getEmail());
                staffShop.setPosition(list.getPosition());
                staffShop.setShopName(name);
                staffShop.setCreateTime(list.getCreateTime());
                staffShop.setAvatar(list.getAvatar());
                staffShopList.add(staffShop);
            }
        }
        return staffShopList;
    }

    @Override
    public StaffShop changeShopName(ServiceEmployee employee) {
        StaffShop staffShop = new StaffShop();
        String id = employee.getId();
        String name = employee.getName();
        String email = employee.getEmail();
        String avatar = employee.getAvatar();
        Integer position = employee.getPosition();
        String shopId = employee.getShopId();
        Date createTime = employee.getCreateTime();

        String shopName = baseMapper.selectById(shopId).getShopName();

        staffShop.setId(id);
        staffShop.setName(name);
        staffShop.setEmail(email);
        staffShop.setPosition(position);
        staffShop.setShopName(shopName);
        staffShop.setAvatar(avatar);
        staffShop.setCreateTime(createTime);
        return staffShop;
    }

}
