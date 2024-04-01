package com.bwtp.staffService.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
//import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
//import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//Spring boot方式
@Configuration
public class employeeConfig {
    /*
     *逻辑删除插件
     */
//    @Bean
//    public ISqlInjector injector(){
//        return new LogicSqlInjector();
//    }

        /**
         * mybatis-plus分页插件
         */
        @Bean
        public PaginationInterceptor paginationInterceptor() {
            return new PaginationInterceptor();
        }
}
