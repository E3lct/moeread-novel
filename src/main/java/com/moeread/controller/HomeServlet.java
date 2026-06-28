package com.moeread.controller;

import com.moeread.dao.BookDAO;
import com.moeread.dao.ReadProgressDAO;
import com.moeread.dao.ReadingStatsDAO;
import com.moeread.model.Book;
import com.moeread.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 首页控制器 (Controller)
 * GET /home  显示首页：今日阅读环 + 推荐位 + 最近在读 + 喜爱书籍
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private BookDAO bookDAO = new BookDAO();
    private ReadProgressDAO progressDAO = new ReadProgressDAO();
    private ReadingStatsDAO statsDAO = new ReadingStatsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");

        // 今日阅读时长 + 目标
        int todayMinutes = statsDAO.getTodayMinutes(user.getId());
        int dailyGoal = user.getDailyGoal() > 0 ? user.getDailyGoal() : 30;
        int progressPercent = Math.min(100, todayMinutes * 100 / dailyGoal);
        int streakDays = statsDAO.getStreakDays(user.getId());

        // 最近在读（按阅读进度更新时间排序，最多 6 本）
        List<Book> recentBooks = progressDAO.findRecentBooks(user.getId(), 6);

        // 喜爱书籍（最多 6 本）
        List<Book> favoriteBooks = bookDAO.findFavorites(user.getId(), 6);

        // 推荐位：取最近添加且有简介的书，最多取 3 本轮换
        List<Book> recommendBooks = bookDAO.findRecentAdded(user.getId(), 3);
        Book recommend = null;
        for (Book b : recommendBooks) {
            if (b.getDescription() != null && !b.getDescription().trim().isEmpty()) {
                recommend = b;
                break;
            }
        }
        if (recommend == null && !recommendBooks.isEmpty()) {
            recommend = recommendBooks.get(0);
        }

        // 总书数（用于空状态判断）
        int totalBooks = bookDAO.findByUserId(user.getId()).size();

        request.setAttribute("user", user);
        request.setAttribute("todayMinutes", todayMinutes);
        request.setAttribute("dailyGoal", dailyGoal);
        request.setAttribute("progressPercent", progressPercent);
        request.setAttribute("streakDays", streakDays);
        request.setAttribute("recentBooks", recentBooks);
        request.setAttribute("favoriteBooks", favoriteBooks);
        request.setAttribute("recommend", recommend);
        request.setAttribute("totalBooks", totalBooks);

        request.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(request, response);
    }
}
