package com.bwtp.staffService.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bwtp.commonutils.R;
import com.bwtp.staffService.entity.ServiceShop;
import com.bwtp.staffService.entity.vo.QueryShop;
import com.bwtp.staffService.service.ServiceShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author blp
 * @since 2022-12-17
 */
@RestController
@RequestMapping("/shopService/serviceShop")
@CrossOrigin
public class ServiceShopController {

    @Autowired
    private ServiceShopService shopService;


    //多条件组和分页查询
    //增加一个实体类方便查询，因为如果直接用ServiceShop的话，除了地址其他都不能为空
    @PostMapping("pageShop/{current}/{limit}")
    public R pageShop(@PathVariable Long current,
                      @PathVariable Long limit,
                      @RequestBody(required = false) QueryShop queryShop){
        QueryWrapper<ServiceShop> wrapper = new QueryWrapper<>();
        Page<ServiceShop> page = new Page<>(current, limit);

        String id = queryShop.getId();
        String name = queryShop.getName();
        String address = queryShop.getAddress();

        if (!ObjectUtils.isEmpty(id)) {
            wrapper.eq("id",id);
        }
        if (!ObjectUtils.isEmpty(name)){
            wrapper.eq("name",name);
        }
        //地址模糊查询
        if (!ObjectUtils.isEmpty(address)){
            wrapper.like("address",address);
        }

        shopService.page(page,wrapper);

        long total = page.getTotal();
        List<ServiceShop> shops = page.getRecords();
        return R.ok().data("shopList",shops).data("total",total);
    }

//    //根据id返回门店名
//    @GetMapping("getShopNameById/{id}")
//    public R getShopNameById(@PathVariable Long id){
//        if (ObjectUtils.isEmpty(id)){
//            throw new BwtpException();
//        }else {
//            ServiceShop service = shopService.getById(id);
//            String shopName = service.getShopName();
//            return R.ok().data("shopName",shopName);
//        }
//    }


    //删除
    @DeleteMapping("deleteShop/{id}")
    public R deleteShopByID(@PathVariable String id){
        boolean b=shopService.deleteShopById(id);
        if (b){
            return R.ok();
        }else {
            return R.error();
        }
    }

    //添加门店
    @PostMapping("addShop")
    public R addShop(@RequestBody ServiceShop shop){
        boolean b = shopService.save(shop);
        if (b){
            return R.ok();
        }else {
            return R.error();
        }
    }

    //修改门店信息
    @PostMapping("updateShopByID")
    public R updateByID(@RequestBody ServiceShop shop){
        boolean b = shopService.updateById(shop);
        if (b){
            return R.ok();
        }else {
            return R.error();
        }
    }


    //获取门店全部信息
    @PostMapping("getAllShop")
    public R getAllShop(){
        List<ServiceShop> list = shopService.list(null);
        return R.ok().data("shopList",list);
    }

}

