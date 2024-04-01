package com.bwtp.staffService;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.bwtp.staffService.mapper")
@ComponentScan("com.bwtp")
@EnableCaching //开启全局注解缓存
public class staffApplication {
    public static void main(String[] args) {
        SpringApplication.run(staffApplication.class,args);
    }
}
