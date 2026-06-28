package com.moeread.controller;

import com.moeread.dao.BookDAO;
import com.moeread.dao.ChapterDAO;
import com.moeread.dao.ReadProgressDAO;
import com.moeread.dao.ReadingStatsDAO;
import com.moeread.model.Book;
import com.moeread.model.Chapter;
import com.moeread.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 阅读器控制器 (Controller)
 * GET  /reader?bookId=xxx[&chapter=N]  显示某本书某章节
 * POST /reader?action=save_progress    保存阅读进度 (AJAX)
 */
@WebServlet(name = "ReaderServlet", urlPatterns = {"/reader"})
@MultipartConfig
public class ReaderServlet extends HttpServlet {

    private BookDAO bookDAO = new BookDAO();
    private ChapterDAO chapterDAO = new ChapterDAO();
    private ReadProgressDAO progressDAO = new ReadProgressDAO();
    private ReadingStatsDAO statsDAO = new ReadingStatsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        String ctx = request.getContextPath();

        int bookId = parseInt(request.getParameter("bookId"));
        Book book = bookDAO.findById(bookId);

        // 书不存在或不属于当前用户
        if (book == null || book.getUserId() != user.getId()) {
            response.sendRedirect(ctx + "/book");
            return;
        }

        // 章节数 + 目录
        int totalChapters = chapterDAO.countByBookId(bookId);
        List<Chapter> toc = chapterDAO.findTocByBookId(bookId);

        // 当前章节：优先 URL 参数 -> 进度记录 -> 1
        int chapterIndex = parseInt(request.getParameter("chapter"));
        int scrollPercent = 0;
        if (chapterIndex <= 0) {
            int[] p = progressDAO.find(user.getId(), bookId);
            if (p != null && p[0] > 0) {
                chapterIndex = p[0];
                scrollPercent = p[1];  // 恢复上次滚动位置
            } else {
                chapterIndex = 1;
            }
        }
        if (chapterIndex > totalChapters) chapterIndex = totalChapters;
        if (chapterIndex < 1) chapterIndex = 1;

        Chapter chapter = chapterDAO.findByBookIdAndIndex(bookId, chapterIndex);
        if (chapter == null) {
            // 没有章节内容，回书架
            response.sendRedirect(ctx + "/book");
            return;
        }

        // 如果是第一次打开且状态还是 prepare，自动改为 reading
        if (!"reading".equals(book.getStatus()) && !"finished".equals(book.getStatus())) {
            bookDAO.updateStatus(bookId, "reading");
            book.setStatus("reading");
        }

        request.setAttribute("book", book);
        request.setAttribute("chapter", chapter);
        request.setAttribute("toc", toc);
        request.setAttribute("chapterIndex", chapterIndex);
        request.setAttribute("totalChapters", totalChapters);
        request.setAttribute("hasPrev", chapterIndex > 1);
        request.setAttribute("hasNext", chapterIndex < totalChapters);
        request.setAttribute("scrollPercent", scrollPercent);

        request.getRequestDispatcher("/WEB-INF/jsp/reader.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        String action = request.getParameter("action");

        boolean ok = false;
        if ("save_progress".equals(action)) {
            int bookId = parseInt(request.getParameter("bookId"));
            int chapterIndex = parseInt(request.getParameter("chapterIndex"));
            int scrollPercent = parseInt(request.getParameter("scrollPercent"));
            Book b = bookDAO.findById(bookId);
            if (b != null && b.getUserId() == user.getId() && chapterIndex > 0) {
                ok = progressDAO.upsert(user.getId(), bookId, chapterIndex, scrollPercent);
                if (ok) {
                    // 每次保存进度累加 1 分钟阅读时长
                    statsDAO.addTodayMinutes(user.getId(), 1);
                }
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write("{\"success\":" + ok + "}");
        out.flush();
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }
}
