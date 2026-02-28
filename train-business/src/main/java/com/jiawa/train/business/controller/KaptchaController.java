package com.jiawa.train.business.controller;


import com.google.code.kaptcha.impl.DefaultKaptcha;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/kaptcha")
public class KaptchaController {

    @Qualifier("getDefaultKaptcha")
    @Resource
    DefaultKaptcha defaultKaptcha;

    @Resource
    private RedissonClient redissonClient;

    @GetMapping("/image-code/{imageCodeToken}")
    public void imageCode(@PathVariable(value = "imageCodeToken") String imageCodeToken, HttpServletResponse response) throws Exception {
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        try {
            //生成验证码字符串
            String createText=defaultKaptcha.createText();
            //将验证码字符串保存到redis中
            redissonClient.getBucket(imageCodeToken).set(createText,300, TimeUnit.SECONDS);
            //使用验证码字符串生成验证吗图片
            BufferedImage challenge = defaultKaptcha.createImage(createText);
            ImageIO.write(challenge, "jpg", jpegOutputStream);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
        byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream = response.getOutputStream();
        responseOutputStream.write(captchaChallengeAsJpeg);
        responseOutputStream.flush();
        responseOutputStream.close();
    }


}
