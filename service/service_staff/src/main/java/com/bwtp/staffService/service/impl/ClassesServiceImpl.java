package com.bwtp.staffService.service.impl;

import com.bwtp.staffService.entity.Classes;
import com.bwtp.staffService.mapper.ClassesMapper;
import com.bwtp.staffService.service.ClassesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 班次信息表 服务实现类
 * </p>
 *
 * @author blp
 * @since 2023-02-02
 */
@Service
public class ClassesServiceImpl extends ServiceImpl<ClassesMapper, Classes> implements ClassesService {

}
