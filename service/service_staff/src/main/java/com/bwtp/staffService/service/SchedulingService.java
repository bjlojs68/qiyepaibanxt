package com.bwtp.staffService.service;

import com.bwtp.staffService.entity.Scheduling;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bwtp.staffService.entity.vo.scheduleVo;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 排班表 服务类
 * </p>
 *
 * @author blp
 * @since 2023-04-03
 */
public interface SchedulingService extends IService<Scheduling> {

//    List<scheduleVo> getMouthClassesList();

    List<scheduleVo> getWeekClassesList(int current,String shopId);

    List<scheduleVo> getDayClassesList(Date creatDate,String shopId);

    String getDataTimeRedis();
}
