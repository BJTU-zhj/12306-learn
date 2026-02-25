package com.jiawa.train.member.controller;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class TestController {

    @Value("${test.zhj}")
    private String nacosConfig;

    @Resource
    private Environment environment;

    @GetMapping("/test")
    public String test() {
        return "配置："+ nacosConfig+","+"端口："+ environment.getProperty("local.server.port");
    }
}
