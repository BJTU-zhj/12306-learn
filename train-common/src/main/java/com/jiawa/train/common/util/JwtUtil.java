package com.jiawa.train.common.util;

import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWTUtil;
import com.jiawa.train.common.properties.JwtUtilProperties;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger LOG= LoggerFactory.getLogger(JwtUtil.class);
    @Resource
    private JwtUtilProperties jwtUtilProperties;

    public  String createToken(Long id, String mobile){
        Map<String, Object> map=new HashMap<>();
        map.put("id",id);
        map.put("mobile",mobile);
        map.put("expire_time",System.currentTimeMillis() + jwtUtilProperties.getExpire());
        String token=JWTUtil.createToken(map,jwtUtilProperties.getSecret().getBytes());
        LOG.info("用户{}，JWT生成token：{}",id,token);
        return token;
    }

    public  boolean verify(String token){
        boolean verifyResult=JWTUtil.verify(token,jwtUtilProperties.getSecret().getBytes());
        LOG.info("JWT校验结果：{}",verifyResult);
        return verifyResult;
    }

    //根据token获取用户信息
    public JSONObject getInfoByToken(String token){
        JSONObject infoByToken = JWTUtil.parseToken(token).setKey(jwtUtilProperties.getSecret().getBytes()).getPayloads();
        LOG.info("JWT解析后所得用户信息：{}",infoByToken);
        return infoByToken;
    }
}
