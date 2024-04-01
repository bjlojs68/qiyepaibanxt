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
import springfox.documentation.spring.web.json.Json;

/**
 * <p>
 * 规则种类表
 * </p>
 *
 * @author blp
 * @since 2023-02-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="RuleType对象", description="规则种类表")
//@TableName(value = "rule_type",autoResultMap = true)
public class RuleType implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty(value = "规则")
    private String rule;

    @ApiModelProperty(value = "规则默认值")
//    @TableField(typeHandler = JacksonTypeHandler.class)
    private String defaultValue;


}
