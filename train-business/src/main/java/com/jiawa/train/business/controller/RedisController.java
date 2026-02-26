package com.jiawa.train.business.controller;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@RestController
public class RedisController {

    private static final Logger LOG = Logger.getLogger(RedisController.class.getName());

    @Resource
    private RedisTemplate redisTemplate;


    @GetMapping("/redis/set/{key}/{value}")
    public  String set(@PathVariable String key, @PathVariable String value){
        LOG.info("设置redis key: " + key + " value: " + value);
        redisTemplate.opsForValue().set(key, value,60, TimeUnit.SECONDS);
        return "success";
    }

    @GetMapping("/redis/get/{key}")
    public Object get(@PathVariable String key){
        LOG.info("获取redis key: " + key);
        return  redisTemplate.opsForValue().get(key);
    }

}
