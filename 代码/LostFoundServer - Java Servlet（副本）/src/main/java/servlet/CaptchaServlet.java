package servlet;

import utils.CaptchaUtil;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/captcha")
public class CaptchaServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 生成验证码
        CaptchaUtil.Captcha captcha = CaptchaUtil.generateCaptcha();
        
        // 将验证码存入session
        HttpSession session = request.getSession();
        session.setAttribute("captchaCode", captcha.getCode());
        
        // 设置响应头
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // 返回验证码图片和session ID
        response.getWriter().write(String.format(
            "{\"image\":\"%s\",\"sessionId\":\"%s\"}", 
            captcha.getImageBase64(), 
            session.getId()
        ));
    }
}