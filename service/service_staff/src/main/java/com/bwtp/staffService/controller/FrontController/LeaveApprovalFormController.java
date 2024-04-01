package com.bwtp.staffService.controller.FrontController;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bwtp.commonbases.exceptionhandler.BwtpException;
import com.bwtp.commonutils.R;
import com.bwtp.staffService.entity.LeaveApprovalForm;
import com.bwtp.staffService.service.LeaveApprovalFormService;
import lombok.experimental.PackagePrivate;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 请假审批表 前端控制器
 * </p>
 *
 * @author blp
 * @since 2023-08-06
 */
@RestController
@RequestMapping("/shopService/leaveApprovalForm")
@CrossOrigin
public class LeaveApprovalFormController {

    @Resource
    private LeaveApprovalFormService leaveApprovalFormService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    //修改审批状态，根据请假id和approvalResult
    @PostMapping("updateApprovalResult/{id}/{approvalResult}")
    public R updateApprovalResult(@PathVariable int id,@PathVariable("approvalResult") int approvalResult){

        try {
            String sql="update corporate_schedul.leave_approval_form set approval_result=? where request_id=?";
            int update = jdbcTemplate.update(sql, approvalResult, id);
            if (update>0){
                return R.ok();
            }else {
                return R.error();
            }

        }catch (BwtpException ex){
            return R.error();
        }
    }
}

