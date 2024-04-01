package com.bwtp.staffService.service.impl;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bwtp.commonutils.JWT;
import com.bwtp.staffService.entity.LeaveApprovalForm;
import com.bwtp.staffService.entity.LeaveRequestForm;
import com.bwtp.staffService.entity.ServiceEmployee;
import com.bwtp.staffService.entity.vo.leaveRequestFront;
import com.bwtp.staffService.entity.vo.queryLeaveRequest;
import com.bwtp.staffService.mapper.LeaveRequestFormMapper;
import com.bwtp.staffService.service.LeaveApprovalFormService;
import com.bwtp.staffService.service.LeaveRequestFormService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bwtp.staffService.service.ServiceEmployeeService;
import com.bwtp.staffService.utils.MyUserInfo;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

/**
 * <p>
 * 请假申请表 服务实现类
 * </p>
 *
 * @author blp
 * @since 2023-08-06
 */
@Service
public class LeaveRequestFormServiceImpl extends ServiceImpl<LeaveRequestFormMapper, LeaveRequestForm> implements LeaveRequestFormService {

    @Resource
    private ServiceEmployeeService employeeService;

    @Resource
    private LeaveApprovalFormService leaveApprovalFormService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    //添加申请
    @Override
    public boolean addLeaveForm(LeaveRequestForm leaveRequestForm) {
            leaveRequestForm.setEmployeeId(MyUserInfo.serviceEmployee.getId());

        int insert = baseMapper.insert(leaveRequestForm);
        return  insert>0;
    }


    //查询请假审批信息表
    @Override
    public List<leaveRequestFront> getLeaveRequestPage(Long current, Long limit, queryLeaveRequest queryLeaveRequest) {

        String sql = "SELECT lrf.id, se.name, lrf.leave_type, lrf.start_date, lrf.end_date, lrf.reason,  laf.start_time, laf.approval_result" +
                " FROM corporate_schedul.leave_request_form lrf" +
                " JOIN corporate_schedul.service_employee se ON lrf.employee_id = se.id" +
                " JOIN corporate_schedul.leave_approval_form laf ON lrf.id = laf.request_id";

        String employeeName = queryLeaveRequest.getName();
        Boolean leaveType = queryLeaveRequest.getLeaveType();
        Integer approvalResult = queryLeaveRequest.getApprovalResult();
        Integer leaveDays = queryLeaveRequest.getLeaveDays();

        List<Object> params = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder();

        if (!ObjectUtils.isEmpty(employeeName)) {
            whereClause.append("se.name like ?");
            params.add("%"+employeeName+"%");
        }

        if (!ObjectUtils.isEmpty(leaveType)) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            whereClause.append("lrf.leave_type = ?");
            params.add(leaveType);
        }

        if (!ObjectUtils.isEmpty(approvalResult)) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            whereClause.append("laf.approval_result = ?");
            params.add(approvalResult);
        }

        if (!ObjectUtils.isEmpty(leaveDays)) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            whereClause.append("lrf.leave_days = ?");
            params.add(leaveDays);
        }

        if (whereClause.length() > 0) {
            sql += " WHERE " + whereClause;
        }

        sql += " ORDER BY lrf.id LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(current-1);
        List<leaveRequestFront> list = jdbcTemplate.query(sql, params.toArray(), new BeanPropertyRowMapper<>(leaveRequestFront.class));

        System.out.println(list);
        return list;

    }

    @Override
    public boolean updateBySql(LeaveRequestForm leaveRequestForm) {

        Integer id = leaveRequestForm.getId();
        String employeeId = leaveRequestForm.getEmployeeId();
        Integer leaveType = leaveRequestForm.getLeaveType();
        Date startDate = leaveRequestForm.getStartDate();
        Date endDate = leaveRequestForm.getEndDate();
        String reason = leaveRequestForm.getReason();
        Date updateTime = leaveRequestForm.getUpdateTime();
        Integer isDeleted = leaveRequestForm.getIsDeleted();


        //使用map加sql字符串拼接

        return false;
    }
}
