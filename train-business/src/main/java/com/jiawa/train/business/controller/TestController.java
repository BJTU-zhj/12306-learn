package com.jiawa.train.business.controller;

import com.jiawa.train.business.service.TestService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private static final Logger LOG = LoggerFactory.getLogger(TestController.class);

    @Resource
    private TestService testService;

    @GetMapping("/hello")
    public String hello(){
        return "hello world business";
    }


//    @SentinelResource(value = "hello1", blockHandler = "handleException1")
    @GetMapping("/hello1")
    public String hello1(){
        return testService.hello();
    }

//    @SentinelResource(value = "hello2", blockHandler = "handleException2")
    @GetMapping("hello2")
    public String hello2(){
        return testService.hello();
    }

    //降级函数的返回值和参数都有要求才行
    public String handleException1(Throwable e){
        LOG.info("我是hello1的降级函数");
        return "hello1 降级";
    }

    public String handleException2(Throwable e){
        LOG.info("我是hello2的降级函数");
        return "hello2 降级";
    }

}
