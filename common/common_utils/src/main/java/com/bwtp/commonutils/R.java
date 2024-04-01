package com.bwtp.commonutils;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

//自定义数据输出格式
@Data
public class R {
    private Boolean success;
    private Integer code;
    private String message;
    private Map<String,Object> data=new HashMap<>();

    private R(){}   //无参构造私有化，为了避免外界随意new对象

    //提供固定的静态方法，类名即可调用

    public static R ok(){
        R r = new R();
        r.setSuccess(true);
        r.setCode(RestCode.SUCCESS);
        r.setMessage("成功");
        return r;
    }

    public static R error(){
        R r = new R();
        r.setSuccess(false);
        r.setCode(RestCode.ERROR);
        r.setMessage("失败");
        return r;
    }

    //重写set方法，返回this便于链式编程
    public R success(Boolean success){
        this.setSuccess(success);
        return this;
    }

    public R message(String message){
        this.setMessage(message);
        return this;
    }

    public R code(Integer code){
        this.setCode(code);
        return this;
    }

    public R data(String key, Object value){
        this.data.put(key, value);
        return this;
    }

    public R data(Map<String, Object> map){
        this.setData(map);
        return this;
    }
}
