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
import java.util.Map;

/**
 * 统计页控制器 (Controller)
 * GET /stats  显示统计：年度热力图 + 统计卡片 + 已读完列表
 */
@WebServlet(name = "StatsServlet", urlPatterns = {"/stats"})
public class StatsServlet extends HttpServlet {

    private BookDAO bookDAO = new BookDAO();
    private ReadProgressDAO progressDAO = new ReadProgressDAO();
    private ReadingStatsDAO statsDAO = new ReadingStatsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");

        // 顶部三卡片数据
        int totalMinutes = statsDAO.getTotalMinutes(user.getId());
        int totalBooks = bookDAO.countByUserId(user.getId());
        int streakDays = statsDAO.getStreakDays(user.getId());

        // 当前年份热力图数据
        int currentYear = java.time.Year.now().getValue();
        Map<String, Integer> yearlyData = statsDAO.getYearlyData(user.getId(), currentYear);

        // 已读完书籍
        List<Book> finishedBooks = bookDAO.findFinishedByUserId(user.getId());

        // 阅读进度（用于计算每本书的阅读进度百分比）
        Map<Integer, Integer> progressMap = progressDAO.findProgressMap(user.getId());

        request.setAttribute("user", user);
        request.setAttribute("totalMinutes", totalMinutes);
        request.setAttribute("totalBooks", totalBooks);
        request.setAttribute("streakDays", streakDays);
        request.setAttribute("currentYear", currentYear);
        request.setAttribute("yearlyData", yearlyData);
        request.setAttribute("finishedBooks", finishedBooks);
        request.setAttribute("progressMap", progressMap);

        request.getRequestDispatcher("/WEB-INF/jsp/stats.jsp").forward(request, response);
    }
}
