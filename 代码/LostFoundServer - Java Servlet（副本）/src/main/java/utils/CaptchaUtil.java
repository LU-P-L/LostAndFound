package utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtil {
    private static final String CHAR_SET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final int LINE_COUNT = 20;
    private static final Random random = new Random();

    public static Captcha generateCaptcha() {
        // 生成验证码字符串
        StringBuilder captchaCode = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            captchaCode.append(CHAR_SET.charAt(random.nextInt(CHAR_SET.length())));
        }

        // 创建图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 24));
        
        // 绘制验证码字符
        for (int i = 0; i < CODE_LENGTH; i++) {
            g.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
            g.drawString(String.valueOf(captchaCode.charAt(i)), 20 + i * 25, 28);
        }
        
        // 绘制干扰线
        for (int i = 0; i < LINE_COUNT; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), 
                      random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }
        
        g.dispose();

        // 转换为Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new Captcha(captchaCode.toString(), "data:image/png;base64," + base64Image);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码失败", e);
        }
    }

    public static class Captcha {
        private final String code;
        private final String imageBase64;

        public Captcha(String code, String imageBase64) {
            this.code = code;
            this.imageBase64 = imageBase64;
        }

        public String getCode() {
            return code;
        }

        public String getImageBase64() {
            return imageBase64;
        }
    }
}