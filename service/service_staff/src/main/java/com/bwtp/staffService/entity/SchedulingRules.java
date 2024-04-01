package com.bwtp.staffService.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;

//import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.json.JSONArray;

/**
 * <p>
 * 门店规则表
 * </p>
 *
 * @author blp
 * @since 2023-02-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="SchedulingRules对象", description="门店规则表")
//@TableName(value = "scheduling_rules",autoResultMap = true)
public class SchedulingRules implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "门店id")
    private String shopId;

    @ApiModelProperty(value = "规则id")
    private String ruleId;

    @ApiModelProperty(value = "规则值")
//    @TableField(typeHandler= JacksonTypeHandler.class)
    private String ruleValue;


}
