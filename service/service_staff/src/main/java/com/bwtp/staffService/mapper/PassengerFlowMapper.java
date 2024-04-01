package com.bwtp.staffService.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bwtp.staffService.entity.PassengerFlow;

import java.util.List;

/**
 * <p>
 * 客流量信息表 Mapper 接口
 * </p>
 *
 * @author blp
 * @since 2023-01-27
 */
public interface PassengerFlowMapper extends BaseMapper<PassengerFlow> {

    List<PassengerFlow> getPList();

}
