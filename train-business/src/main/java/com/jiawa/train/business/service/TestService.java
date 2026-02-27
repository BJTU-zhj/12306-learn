package com.jiawa.train.business.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @SentinelResource(value = "hello", blockHandler = "handleException")
    public String hello(){
        return "hello world";
    }

    public String handleException(Throwable e){
        return "hello world 降级";
    }

}
