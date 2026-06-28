package com.moeread.controller;

import com.moeread.dao.BookDAO;
import com.moeread.dao.ChapterDAO;
import com.moeread.model.Book;
import com.moeread.model.Chapter;
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
 * 摸鱼模式控制器 (Controller)
 * GET  /stealth?bookId=xxx[&chapter=xxx]  显示 VS Code 伪装界面
 * 阅读内容藏在"代码注释"里
 */
@WebServlet(name = "StealthServlet", urlPatterns = {"/stealth"})
public class StealthServlet extends HttpServlet {

    private BookDAO bookDAO = new BookDAO();
    private ChapterDAO chapterDAO = new ChapterDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        String ctx = request.getContextPath();

        int bookId = parseInt(request.getParameter("bookId"));
        Book book = bookDAO.findById(bookId);

        if (book == null || book.getUserId() != user.getId()) {
            response.sendRedirect(ctx + "/book");
            return;
        }

        // 章节信息
        int totalChapters = chapterDAO.countByBookId(bookId);
        int chapterIndex = parseInt(request.getParameter("chapter"));
        if (chapterIndex <= 0) {
            chapterIndex = 1; // 默认显示第1章
        }
        if (chapterIndex > totalChapters) chapterIndex = totalChapters;

        Chapter chapter = chapterDAO.findByBookIdAndIndex(bookId, chapterIndex);
        if (chapter == null) {
            response.sendRedirect(ctx + "/book");
            return;
        }

        // 获取目录（用于侧边文件列表）
        List<Chapter> toc = chapterDAO.findTocByBookId(bookId);

        request.setAttribute("book", book);
        request.setAttribute("chapter", chapter);
        request.setAttribute("toc", toc);
        request.setAttribute("chapterIndex", chapterIndex);
        request.setAttribute("totalChapters", totalChapters);

        request.getRequestDispatcher("/WEB-INF/jsp/stealth.jsp").forward(request, response);
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }
}
