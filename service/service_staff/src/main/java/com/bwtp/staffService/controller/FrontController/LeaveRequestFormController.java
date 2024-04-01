package com.bwtp.staffService.controller.FrontController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bwtp.commonutils.R;
import com.bwtp.staffService.entity.LeaveApprovalForm;
import com.bwtp.staffService.entity.LeaveRequestForm;
import com.bwtp.staffService.entity.vo.leaveRequestFront;
import com.bwtp.staffService.entity.vo.queryLeaveRequest;
import com.bwtp.staffService.service.LeaveApprovalFormService;
import com.bwtp.staffService.service.LeaveRequestFormService;
import com.bwtp.staffService.service.ServiceEmployeeService;
import com.bwtp.staffService.utils.MyUserInfo;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 请假申请表 前端控制器
 * </p>
 *
 * @author blp
 * @since 2023-08-06
 */
@RestController
@RequestMapping("/shopService/leaveRequestForm")
@CrossOrigin
public class LeaveRequestFormController {

    @Resource
    private LeaveRequestFormService leaveRequestFormService;
    @Resource
    private LeaveApprovalFormService leaveApprovalFormService;
    @Resource
    private ServiceEmployeeService serviceEmployeeService;


    //添加  需要从token中获取id
    @PostMapping("addLeaveRequest")
    public R addLeaveRequest(@RequestBody LeaveRequestForm leaveRequestForm){

        System.out.println(leaveRequestForm);

        boolean b=leaveRequestFormService.addLeaveForm(leaveRequestForm);
        if (b){
            return R.ok();
        }else {
            return R.error();
        }
    }

    //删除请假记录  前台是逻辑删除  后台物理删除
    //前台是逻辑删除
    @DeleteMapping("removeFormLogic/{id}")
    public R removeFormByIdLogic(@PathVariable int id){
        //根据用户id获取审批数据
        QueryWrapper<LeaveApprovalForm> leaveApprovalFormQueryWrapper = new QueryWrapper<>();
        leaveApprovalFormQueryWrapper.eq("request_id",MyUserInfo.serviceEmployee.getId());
        boolean remove = leaveApprovalFormService.remove(leaveApprovalFormQueryWrapper);
        boolean b = leaveRequestFormService.removeById(id);
        if (remove&&b){
            return R.ok();
        }else {
            return R.error();
        }
    }
    //后台物理删除
    @DeleteMapping("removeForm/{id}")
    public R removeFormById(@PathVariable int id){
       boolean b= leaveApprovalFormService.removeUseSql(id);
        if (b){
            return R.ok();
        }else {
            return R.error();
        }
    }

    //修改  后台
    @PostMapping("updateLeaveForm")
    public R updateLeaveForm(@RequestBody LeaveRequestForm leaveRequestForm){
        boolean b = leaveRequestFormService.updateById(leaveRequestForm);
        if (b){
            return R.ok();
        }else {
            return R.error();
        }
    }

    //查询请假记录 前台是请假申请历史记录   后台是每个员工的请假申请表单
    //前台 数据及状态  通过员工id获取申请表数据，通过申请表id获取审批状态 不分页
    @PostMapping("frontGetData/{id}")
    public R frontGetDataById(@PathVariable String id){
        QueryWrapper<LeaveRequestForm> leaveRequestFormQueryWrapper = new QueryWrapper<>();
        leaveRequestFormQueryWrapper.eq("employee_id",id);

        String name = serviceEmployeeService.getById(id).getName();

        ArrayList<leaveRequestFront> leaveRequestFronts= new ArrayList<>();

        for (int i = leaveRequestFormService.list(leaveRequestFormQueryWrapper).size() - 1; i >= 0; i--) {
            //复制除updateTime
            leaveRequestFront leaveRequestFront = new leaveRequestFront();
            leaveRequestFront.setName(name);
            BeanUtils.copyProperties(leaveRequestFormService.list(leaveRequestFormQueryWrapper).get(i),leaveRequestFront,"updateTime");
            QueryWrapper<LeaveApprovalForm> leaveApprovalFormQueryWrapper = new QueryWrapper<>();
            leaveApprovalFormQueryWrapper.eq("request_id",leaveRequestFormService.list(leaveRequestFormQueryWrapper).get(i).getId());
            leaveRequestFront.setApprovalResult(leaveApprovalFormService.getOne(leaveApprovalFormQueryWrapper).getApprovalResult());
            leaveRequestFronts.add(leaveRequestFront);
        }
        return R.ok().data("leaveRequestFronts",leaveRequestFronts);
    }

    //后台 分页
    @PostMapping("getAllLeaveRequestData/{current}/{limit}")
    public R getAllLeaveRequestDataPage(@PathVariable Long current,
                                        @PathVariable Long limit,
                                        @RequestBody(required = false) queryLeaveRequest queryLeaveRequest){
        QueryWrapper<LeaveRequestForm> leaveRequestFormQueryWrapper = new QueryWrapper<>();
        Page<LeaveRequestForm> leaveRequestFormPage = new Page<>(current,limit);


        List<leaveRequestFront> leaveRequestFrontPage= leaveRequestFormService.getLeaveRequestPage(current,limit,queryLeaveRequest);
        long total = leaveRequestFormService.page(leaveRequestFormPage, leaveRequestFormQueryWrapper).getTotal();
        return R.ok().data("leaveRequestFormList",leaveRequestFrontPage).data("total",total);
    }
}

