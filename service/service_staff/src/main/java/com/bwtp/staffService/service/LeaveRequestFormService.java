package com.bwtp.staffService.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bwtp.staffService.entity.LeaveRequestForm;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bwtp.staffService.entity.vo.leaveRequestFront;
import com.bwtp.staffService.entity.vo.queryLeaveRequest;

import java.util.List;

/**
 * <p>
 * 请假申请表 服务类
 * </p>
 *
 * @author blp
 * @since 2023-08-06
 */
public interface LeaveRequestFormService extends IService<LeaveRequestForm> {

    boolean addLeaveForm(LeaveRequestForm leaveRequestForm);

    List<leaveRequestFront> getLeaveRequestPage(Long current, Long limit, queryLeaveRequest queryLeaveRequest);

    boolean updateBySql(LeaveRequestForm leaveRequestForm);
}
