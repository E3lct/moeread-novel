<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.User" %>
<%
    // 访问根路径时的重定向逻辑
    // 已登录 -> 跳首页
    // 未登录 -> 跳登录页
    User user = (User) session.getAttribute("user");
    String contextPath = request.getContextPath();
    if (user != null) {
        response.sendRedirect(contextPath + "/home");
    } else {
        response.sendRedirect(contextPath + "/login");
    }
%>
