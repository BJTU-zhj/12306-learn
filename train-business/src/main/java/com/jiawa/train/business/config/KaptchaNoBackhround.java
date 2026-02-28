package com.jiawa.train.business.config;

import com.google.code.kaptcha.BackgroundProducer;
import com.google.code.kaptcha.util.Configurable;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/*
实现 BackgroundProducer 接口：
这是 Kaptcha 留出的扩展接口。Kaptcha 在生成验证码时，会先生成文字，然后调用这个接口的 addBackground 方法把文字图层（baseImage）叠加上背景图层。

创建新画布：
new BufferedImage(width, height, 1)：这里的数字 1 对应的是 BufferedImage.TYPE_INT_RGB。这意味着它创建了一个不支持透明度的 RGB 画布。

填充背景：
graph.fill(new Rectangle2D.Double(...))：

在未指定 Paint（颜色）的情况下，Graphics2D 默认使用黑色进行填充。

因此，它实际上是在画布上刷了一层纯黑色的底。

绘制文字：
graph.drawImage(baseImage, 0, 0, null)：将包含验证码字符的原始图片叠加到刚才刷好的黑色背景上。
 */

public class KaptchaNoBackhround extends Configurable implements BackgroundProducer {

    public KaptchaNoBackhround(){
    }
    @Override
    public BufferedImage addBackground(BufferedImage baseImage) {
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();
        BufferedImage imageWithBackground = new BufferedImage(width, height, 1);
        Graphics2D graph = (Graphics2D)imageWithBackground.getGraphics();
        graph.fill(new Rectangle2D.Double(0.0D, 0.0D, (double)width, (double)height));
        graph.drawImage(baseImage, 0, 0, null);
        return imageWithBackground;
    }
}
