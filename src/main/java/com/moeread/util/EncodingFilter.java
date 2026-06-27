package com.moeread.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 字符编码过滤器
 * 统一设置请求和响应为 UTF-8，解决中文乱码问题
 * 注意：不对静态资源（css/js/img）设置 Content-Type，避免覆盖默认 MIME 类型
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

        // 统一设置字符编码
        req.setCharacterEncoding(encoding);
        resp.setCharacterEncoding(encoding);

        // 只对 JSP/Servlet 页面设置 Content-Type，静态资源由 Tomcat 默认 Servlet 处理
        String uri = req.getRequestURI();
        if (!isStaticResource(uri)) {
            resp.setContentType("text/html;charset=" + encoding);
        }

        chain.doFilter(req, resp);
    }

    /**
     * 判断是否为静态资源（不强制设置 text/html）
     */
    private boolean isStaticResource(String uri) {
        String lower = uri.toLowerCase();
        return lower.endsWith(".css")
            || lower.endsWith(".js")
            || lower.endsWith(".png")
            || lower.endsWith(".jpg")
            || lower.endsWith(".jpeg")
            || lower.endsWith(".gif")
            || lower.endsWith(".svg")
            || lower.endsWith(".ico")
            || lower.endsWith(".woff")
            || lower.endsWith(".woff2")
            || lower.endsWith(".ttf")
            || lower.endsWith(".eot");
    }

    @Override
    public void destroy() {
    }
}
