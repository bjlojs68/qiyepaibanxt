package com.bwtp.staffService.service;

import com.bwtp.staffService.entity.LeaveApprovalForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 请假审批表 服务类
 * </p>
 *
 * @author blp
 * @since 2023-08-06
 */
public interface LeaveApprovalFormService extends IService<LeaveApprovalForm> {

    boolean removeUseSql(int id);
}
