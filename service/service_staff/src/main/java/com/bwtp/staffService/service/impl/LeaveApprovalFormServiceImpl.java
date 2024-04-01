package com.bwtp.staffService.service.impl;

import com.bwtp.staffService.entity.LeaveApprovalForm;
import com.bwtp.staffService.mapper.LeaveApprovalFormMapper;
import com.bwtp.staffService.service.LeaveApprovalFormService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bwtp.staffService.service.LeaveRequestFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 请假审批表 服务实现类
 * </p>
 *
 * @author blp
 * @since 2023-08-06
 */
@Service
public class LeaveApprovalFormServiceImpl extends ServiceImpl<LeaveApprovalFormMapper, LeaveApprovalForm> implements LeaveApprovalFormService {

    @Resource
    private LeaveRequestFormService leaveRequestFormService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    //物理删除
    @Override
    public boolean removeUseSql(int id) {
        String deleteApprovalFormSql = "DELETE FROM corporate_schedul.leave_approval_form WHERE request_id = ?";
        int approvalFormRowsAffected = jdbcTemplate.update(deleteApprovalFormSql, id);
        // Delete leave_request_form records
        String deleteRequestFormSql = "DELETE FROM corporate_schedul.leave_request_form WHERE id = ?";
        int requestFormRowsAffected = jdbcTemplate.update(deleteRequestFormSql, id);
        return approvalFormRowsAffected>0&&requestFormRowsAffected>0;
    }
}
