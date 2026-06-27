package com.moeread.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登录拦截器
 * 未登录用户访问受保护路径时，跳转到登录页
 */
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        // 检查 session 中是否有用户信息
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        if (isLoggedIn) {
            chain.doFilter(req, resp);
        } else {
            // 未登录，跳转到登录页，并带上原请求路径
            String redirect = req.getContextPath() + "/login";
            resp.sendRedirect(redirect);
        }
    }

    @Override
    public void destroy() {
    }
}
