<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.moeread.model.User" %>
<%
    User _u = (session != null) ? (User) session.getAttribute("user") : null;
    if (_u != null && _u.getMascotImage() != null && !_u.getMascotImage().isEmpty()) {
        int _opacity = _u.getMascotOpacity() > 0 ? _u.getMascotOpacity() : 80;
        int _scale = _u.getBgScale() > 0 ? _u.getBgScale() : 100;
        boolean _mirror = _u.getBgMirror() == 1;
%>
<div class="bg-layer" id="bgLayer"
     style="background-image:url('<%=_u.getMascotImage()%>');opacity:<%=_opacity/100.0%>;background-size:<%=_scale%>% <%=_scale%>%;<%=_mirror ? "transform:scaleX(-1);" : ""%>"></div>
<% } %>
