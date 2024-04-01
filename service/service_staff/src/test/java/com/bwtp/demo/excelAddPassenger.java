package com.bwtp.demo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bwtp.staffService.entity.RuleType;
import com.bwtp.staffService.service.PassengerFlowService;
import com.bwtp.staffService.service.RuleTypeService;
import com.bwtp.staffService.service.impl.PassengerFlowServiceImpl;
import com.bwtp.staffService.staffApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;

import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = staffApplication.class)
public class excelAddPassenger {
    @Resource
    private PassengerFlowService service;
    @Resource
    private RuleTypeService ruleTypeService;;
    @Test
    public void test() throws IOException {

        List<RuleType> ruleTypes = ruleTypeService.list(null);
        for (int i = 0; i < ruleTypes.size(); i++) {
            RuleType ruleType = ruleTypes.get(i);
            String defaultValue = ruleType.getDefaultValue();
            JSONObject jsonObject = JSON.parseObject(defaultValue);
            Set<String> strings = jsonObject.keySet();
            Object add = jsonObject.getInteger("add");
            System.out.println(add);
        }
    }
}
