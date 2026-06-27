package com.moeread.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 字符编码过滤器
 * 统一设置请求和响应为 UTF-8，解决中文乱码问题
 */
public class EncodingFilter implements Filter {

    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String encoding = filterConfig.getInitParameter("encoding");
        if (encoding != null && !encoding.isEmpty()) {
            this.encoding = encoding;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 设置编码
        req.setCharacterEncoding(encoding);
        resp.setCharacterEncoding(encoding);
        resp.setContentType("text/html;charset=" + encoding);

        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}
