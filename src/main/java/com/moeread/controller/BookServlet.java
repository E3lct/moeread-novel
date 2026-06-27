package com.moeread.controller;

import com.moeread.dao.BookDAO;
import com.moeread.model.Book;
import com.moeread.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 书架控制器 (Controller)
 * GET /book -> 显示书架（支持筛选和搜索）
 *
 * 筛选参数：
 *   status = all / reading / prepare / finished
 *   favorite = 1 (显示喜欢的书)
 *   q = 关键词（搜书名）
 */
@WebServlet(name = "BookServlet", urlPatterns = {"/book"})
public class BookServlet extends HttpServlet {

    private BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");

        // 取筛选参数
        String status = request.getParameter("status");
        String favorite = request.getParameter("favorite");
        String q = request.getParameter("q");

        // 查全部书
        List<Book> allBooks = bookDAO.findByUserId(user.getId());

        // 内存筛选（作业版数据量小，不需要 SQL 筛选）
        List<Book> books = new ArrayList<>();
        for (Book b : allBooks) {
            // 状态筛选
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                if (!status.equals(b.getStatus())) continue;
            }
            // 喜欢筛选
            if ("1".equals(favorite)) {
                if (b.getIsFavorite() != 1) continue;
            }
            // 搜索
            if (q != null && !q.trim().isEmpty()) {
                String title = b.getTitle() == null ? "" : b.getTitle();
                if (!title.toLowerCase().contains(q.toLowerCase().trim())) continue;
            }
            books.add(b);
        }

        // 统计各状态数量（显示在筛选标签上）
        int countAll = allBooks.size();
        int countReading = 0, countPrepare = 0, countFinished = 0, countFavorite = 0;
        for (Book b : allBooks) {
            if ("reading".equals(b.getStatus())) countReading++;
            if ("prepare".equals(b.getStatus())) countPrepare++;
            if ("finished".equals(b.getStatus())) countFinished++;
            if (b.getIsFavorite() == 1) countFavorite++;
        }

        request.setAttribute("books", books);
        request.setAttribute("currentStatus", status == null ? "all" : status);
        request.setAttribute("currentFavorite", favorite);
        request.setAttribute("currentQ", q == null ? "" : q);
        request.setAttribute("countAll", countAll);
        request.setAttribute("countReading", countReading);
        request.setAttribute("countPrepare", countPrepare);
        request.setAttribute("countFinished", countFinished);
        request.setAttribute("countFavorite", countFavorite);

        request.getRequestDispatcher("/WEB-INF/jsp/book.jsp").forward(request, response);
    }
}
