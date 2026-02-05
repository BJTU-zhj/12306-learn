package com.jiawa.train.common.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
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

    public String createToken(Long id, String mobile) {
        DateTime now = DateTime.now();
        DateTime expTime = now.offsetNew(DateField.SECOND, Math.toIntExact(jwtUtilProperties.getExpire()));
        Map<String, Object> payload = new HashMap<>();
        // 签发时间
        payload.put(JWTPayload.ISSUED_AT, now);
        // 过期时间
        payload.put(JWTPayload.EXPIRES_AT, expTime);
        // 生效时间
        payload.put(JWTPayload.NOT_BEFORE, now);
        // 内容
        payload.put("id", id);
        payload.put("mobile", mobile);
        String token = JWTUtil.createToken(payload, jwtUtilProperties.getSecret().getBytes());
        LOG.info("用户{}，JWT生成token：{}", id, token);
        return token;
    }
    public boolean verify(String token) {
        try {
            // 1. 校验签名（验证密钥是否匹配，数据是否被篡改）
            JWT jwt = JWTUtil.parseToken(token).setKey(jwtUtilProperties.getSecret().getBytes());
            boolean verifyResult = jwt.validate(0);
            if (!verifyResult) {
                LOG.warn("JWT 签名校验失败！");
                return false;
            }
            LOG.info("JWT 校验通过");
            return true;
        } catch (Exception e) {
            LOG.error("JWT 解析异常", e);
            return false;
        }
    }

    //根据token获取用户信息
    public JSONObject getInfoByToken(String token){
        JSONObject infoByToken = JWTUtil.parseToken(token).setKey(jwtUtilProperties.getSecret().getBytes()).getPayloads();
        LOG.info("JWT解析后所得用户信息：{}",infoByToken);
        return infoByToken;
    }
}
