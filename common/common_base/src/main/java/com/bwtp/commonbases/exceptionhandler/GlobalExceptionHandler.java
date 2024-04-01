package com.bwtp.commonbases.exceptionhandler;

import com.bwtp.commonutils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //指定出现什么异常执行这个方法
    @ExceptionHandler(Exception.class)
    @ResponseBody //为了返回数据
    public R error(Exception e) {
        e.printStackTrace();
        return R.error().message("执行了全局异常处理..");
    }

    //特殊异常处理
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody //为了返回数据
    public R ownError(Exception e) {
        e.printStackTrace();
        return R.error().message("执行了HttpMessageNotReadableException全局异常处理..");
    }

    //自定义异常处理
    @ExceptionHandler(BwtpException.class)
    @ResponseBody //为了返回数据
    public R blpError(BwtpException e) {
        log.error(e.getMsg());
        e.printStackTrace();
        return R.error().code(e.getCode()).message(e.getMsg());
    }

}
