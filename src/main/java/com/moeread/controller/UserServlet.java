package com.moeread.controller;

import com.moeread.model.User;
import com.moeread.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 用户控制器 (Controller)
 * 处理 /login /register /logout 三个路径
 * MVC: Controller 负责接收请求 -> 调 Service -> 跳转页面
 */
@WebServlet(name = "UserServlet", urlPatterns = {"/login", "/register", "/logout"})
public class UserServlet extends HttpServlet {

    private UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/login".equals(path)) {
            // 已登录直接跳首页
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);

        } else if ("/register".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);

        } else if ("/logout".equals(path)) {
            // 退出登录：销毁 session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/login".equals(path)) {
            handleLogin(request, response);
        } else if ("/register".equals(path)) {
            handleRegister(request, response);
        }
    }

    /**
     * 处理登录表单提交
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 基本校验
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "用户名和密码不能为空");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        User user = userService.login(username.trim(), password);
        if (user != null) {
            // 登录成功：存 session，跳首页
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            // 登录失败
            request.setAttribute("error", "用户名或密码错误");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        }
    }

    /**
     * 处理注册表单提交
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String nickname = request.getParameter("nickname");

        // 基本校验
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "用户名和密码不能为空");
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        if (username.trim().length() < 3 || username.trim().length() > 20) {
            request.setAttribute("error", "用户名长度需要 3-20 个字符");
            request.setAttribute("username", username);
            request.setAttribute("nickname", nickname);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        if (password.length() < 6) {
            request.setAttribute("error", "密码至少 6 位");
            request.setAttribute("username", username);
            request.setAttribute("nickname", nickname);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "两次密码不一致");
            request.setAttribute("username", username);
            request.setAttribute("nickname", nickname);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
            return;
        }

        User user = userService.register(username.trim(), password, nickname);
        if (user != null) {
            // 注册成功：自动登录，跳首页
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            // 注册失败：用户名已存在
            request.setAttribute("error", "用户名已被占用");
            request.setAttribute("username", username);
            request.setAttribute("nickname", nickname);
            request.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(request, response);
        }
    }
}
