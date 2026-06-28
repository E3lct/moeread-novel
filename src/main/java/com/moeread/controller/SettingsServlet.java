package com.moeread.controller;

import com.moeread.dao.BookDAO;
import com.moeread.dao.UserDAO;
import com.moeread.model.Book;
import com.moeread.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

@WebServlet(name = "SettingsServlet", urlPatterns = {"/settings"})
@MultipartConfig
public class SettingsServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");

        User freshUser = userDAO.findById(user.getId());
        if (freshUser != null) {
            session.setAttribute("user", freshUser);
            user = freshUser;
        }

        List<Book> importHistory = bookDAO.findByUserId(user.getId());

        request.setAttribute("user", user);
        request.setAttribute("importHistory", importHistory);
        request.getRequestDispatcher("/WEB-INF/jsp/settings.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        String ctx = request.getContextPath();

        String action = request.getParameter("action");

        if ("update_profile".equals(action)) {
            String nickname = request.getParameter("nickname");
            if (nickname != null && !nickname.trim().isEmpty()) {
                user.setNickname(nickname.trim());
            }

            try {
                int goal = Integer.parseInt(request.getParameter("dailyGoal"));
                if (goal >= 5 && goal <= 240) {
                    user.setDailyGoal(goal);
                }
            } catch (NumberFormatException ignored) {}

            try {
                int opacity = Integer.parseInt(request.getParameter("mascotOpacity"));
                if (opacity >= 10 && opacity <= 100) {
                    user.setMascotOpacity(opacity);
                }
            } catch (NumberFormatException ignored) {}

            // 背景缩放
            try {
                int scale = Integer.parseInt(request.getParameter("bgScale"));
                if (scale >= 20 && scale <= 300) {
                    user.setBgScale(scale);
                }
            } catch (NumberFormatException ignored) {}

            // 背景镜像
            user.setBgMirror("1".equals(request.getParameter("bgMirror")) ? 1 : 0);

            // 看板娘图片上传
            Part mascotPart = request.getPart("mascotFile");
            if (mascotPart != null && mascotPart.getSize() > 0 && mascotPart.getSize() < 2 * 1024 * 1024) {
                String ct = mascotPart.getContentType();
                if (ct != null && ct.startsWith("image/")) {
                    InputStream is = mascotPart.getInputStream();
                    byte[] imgBytes = new byte[(int) mascotPart.getSize()];
                    is.read(imgBytes);
                    is.close();
                    String base64 = "data:" + ct + ";base64," + Base64.getEncoder().encodeToString(imgBytes);
                    user.setMascotImage(base64);
                }
            }

            // 移除看板娘
            if ("1".equals(request.getParameter("removeMascot"))) {
                user.setMascotImage(null);
            }

            // 头像上传
            Part avatarPart = request.getPart("avatarFile");
            if (avatarPart != null && avatarPart.getSize() > 0 && avatarPart.getSize() < 2 * 1024 * 1024) {
                String ct = avatarPart.getContentType();
                if (ct != null && ct.startsWith("image/")) {
                    InputStream is = avatarPart.getInputStream();
                    byte[] imgBytes = new byte[(int) avatarPart.getSize()];
                    is.read(imgBytes);
                    is.close();
                    String base64 = "data:" + ct + ";base64," + Base64.getEncoder().encodeToString(imgBytes);
                    user.setAvatar(base64);
                }
            }

            // 移除头像
            if ("1".equals(request.getParameter("removeAvatar"))) {
                user.setAvatar(null);
            }

            userDAO.updateSettings(user);
            session.setAttribute("user", user);
            response.sendRedirect(ctx + "/settings?saved=1");
            return;
        }

        if ("change_password".equals(action)) {
            String oldPwd = request.getParameter("oldPassword");
            String newPwd = request.getParameter("newPassword");

            String oldMd5 = com.moeread.util.MD5Util.md5(oldPwd);
            if (!oldMd5.equals(user.getPassword())) {
                response.sendRedirect(ctx + "/settings?err=pwd");
                return;
            }
            if (newPwd == null || newPwd.length() < 6) {
                response.sendRedirect(ctx + "/settings?err=short");
                return;
            }

            String newMd5 = com.moeread.util.MD5Util.md5(newPwd);
            userDAO.updatePassword(user.getId(), newMd5);
            user.setPassword(newMd5);
            session.setAttribute("user", user);
            response.sendRedirect(ctx + "/settings?saved=1");
            return;
        }

        response.sendRedirect(ctx + "/settings");
    }
}
