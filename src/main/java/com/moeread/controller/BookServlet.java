package com.moeread.controller;

import com.moeread.dao.BookDAO;
import com.moeread.dao.BookTagDAO;
import com.moeread.model.Book;
import com.moeread.model.User;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 书架控制器 (Controller)
 * GET  /book          显示书架（网格/分组视图，支持状态/喜欢/标签/搜索筛选）
 * POST /book?action=  编辑操作：update_meta / add_tag / remove_tag / delete
 */
@WebServlet(name = "BookServlet", urlPatterns = {"/book"})
public class BookServlet extends HttpServlet {

    private BookDAO bookDAO = new BookDAO();
    private BookTagDAO tagDAO = new BookTagDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");

        String status = request.getParameter("status");
        String favorite = request.getParameter("favorite");
        String q = request.getParameter("q");
        String tag = request.getParameter("tag");
        String view = request.getParameter("view");   // grid | grouped

        List<Book> allBooks = bookDAO.findByUserId(user.getId());

        // 内存筛选
        List<Book> filtered = new ArrayList<>();
        for (Book b : allBooks) {
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                if (!status.equals(b.getStatus())) continue;
            }
            if ("1".equals(favorite)) {
                if (b.getIsFavorite() != 1) continue;
            }
            if (q != null && !q.trim().isEmpty()) {
                String title = b.getTitle() == null ? "" : b.getTitle();
                if (!title.toLowerCase().contains(q.toLowerCase().trim())) continue;
            }
            filtered.add(b);
        }

        // 统计
        int countAll = allBooks.size();
        int countReading = 0, countPrepare = 0, countFinished = 0, countFavorite = 0;
        for (Book b : allBooks) {
            if ("reading".equals(b.getStatus())) countReading++;
            if ("prepare".equals(b.getStatus())) countPrepare++;
            if ("finished".equals(b.getStatus())) countFinished++;
            if (b.getIsFavorite() == 1) countFavorite++;
        }

        // 标签数据
        List<String> allTags = tagDAO.findTagsByUserId(user.getId());
        Map<Integer, List<String>> tagMap = tagDAO.findTagMapByUserId(user.getId());
        // 当前选中标签过滤
        if (tag != null && !tag.trim().isEmpty()) {
            List<Book> tagFiltered = new ArrayList<>();
            for (Book b : filtered) {
                List<String> tags = tagMap.get(b.getId());
                if (tags != null && tags.contains(tag)) tagFiltered.add(b);
            }
            filtered = tagFiltered;
        }

        // 分组视图数据：tagName -> List<Book>
        Map<String, List<Book>> groupedBooks = new LinkedHashMap<>();
        if ("grouped".equals(view)) {
            Map<String, List<Integer>> raw = tagDAO.findBooksGroupedByTag(user.getId());
            // 用 filtered 列表里的书构建 id->book 映射
            Map<Integer, Book> idMap = new HashMap<>();
            for (Book b : filtered) idMap.put(b.getId(), b);
            for (Map.Entry<String, List<Integer>> e : raw.entrySet()) {
                List<Book> g = new ArrayList<>();
                for (Integer bid : e.getValue()) {
                    Book b = idMap.get(bid);
                    if (b != null) g.add(b);
                }
                if (!g.isEmpty()) groupedBooks.put(e.getKey(), g);
            }
            // 未分组
            List<Book> untagged = new ArrayList<>();
            for (Book b : filtered) {
                List<String> ts = tagMap.get(b.getId());
                if (ts == null || ts.isEmpty()) untagged.add(b);
            }
            if (!untagged.isEmpty()) groupedBooks.put("未分组", untagged);
        }

        request.setAttribute("books", filtered);
        request.setAttribute("groupedBooks", groupedBooks);
        request.setAttribute("currentStatus", status == null ? "all" : status);
        request.setAttribute("currentFavorite", favorite);
        request.setAttribute("currentQ", q == null ? "" : q);
        request.setAttribute("currentTag", tag == null ? "" : tag);
        request.setAttribute("currentView", view == null ? "grid" : view);
        request.setAttribute("allTags", allTags);
        request.setAttribute("tagMap", tagMap);
        request.setAttribute("countAll", countAll);
        request.setAttribute("countReading", countReading);
        request.setAttribute("countPrepare", countPrepare);
        request.setAttribute("countFinished", countFinished);
        request.setAttribute("countFavorite", countFavorite);

        request.getRequestDispatcher("/WEB-INF/jsp/book.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        String action = request.getParameter("action");
        String ctx = request.getContextPath();

        boolean ok = false;
        if ("update_meta".equals(action)) {
            int bookId = parseInt(request.getParameter("bookId"));
            String title = request.getParameter("title");
            String coverColor = request.getParameter("coverColor");
            Book b = bookDAO.findById(bookId);
            if (b != null && b.getUserId() == user.getId() && title != null && !title.trim().isEmpty()) {
                ok = bookDAO.updateMeta(bookId, title.trim(), coverColor);
            }
        } else if ("add_tag".equals(action)) {
            int bookId = parseInt(request.getParameter("bookId"));
            String tagName = request.getParameter("tagName");
            Book b = bookDAO.findById(bookId);
            if (b != null && b.getUserId() == user.getId() && tagName != null && !tagName.trim().isEmpty()) {
                ok = tagDAO.addTag(user.getId(), bookId, tagName.trim());
            }
        } else if ("remove_tag".equals(action)) {
            int bookId = parseInt(request.getParameter("bookId"));
            String tagName = request.getParameter("tagName");
            Book b = bookDAO.findById(bookId);
            if (b != null && b.getUserId() == user.getId()) {
                ok = tagDAO.removeTag(user.getId(), bookId, tagName);
            }
        } else if ("delete".equals(action)) {
            int bookId = parseInt(request.getParameter("bookId"));
            Book b = bookDAO.findById(bookId);
            if (b != null && b.getUserId() == user.getId()) {
                tagDAO.removeAllTags(bookId);
                ok = bookDAO.delete(bookId);
            }
        } else if ("upload_cover".equals(action)) {
            // 封面图片上传
            ok = handleCoverUpload(request, response, user, ctx);
            return; // upload_cover 自己处理响应
        }

        // 简单 JSON 响应（AJAX 用）
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write("{\"success\":" + ok + "}");
        out.flush();
    }

    /**
     * 处理封面上传（multipart）
     */
    private boolean handleCoverUpload(HttpServletRequest request, HttpServletResponse response,
                                        User user, String ctx) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (!ServletFileUpload.isMultipartContent(request)) {
            out.write("{\"success\":false,\"error\":\"not_multipart\"}");
            out.flush();
            return false;
        }

        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(2 * 1024 * 1024); // 2MB

            List<FileItem> items = upload.parseRequest(request);
            int bookId = -1;
            FileItem fileItem = null;

            for (FileItem item : items) {
                if (item.isFormField()) {
                    if ("bookId".equals(item.getFieldName())) {
                        bookId = Integer.parseInt(item.getString("UTF-8"));
                    }
                } else {
                    if (item.getSize() > 0) {
                        fileItem = item;
                    }
                }
            }

            if (bookId <= 0 || fileItem == null) {
                out.write("{\"success\":false,\"error\":\"missing_params\"}");
                out.flush();
                return false;
            }

            Book b = bookDAO.findById(bookId);
            if (b == null || b.getUserId() != user.getId()) {
                out.write("{\"success\":false,\"error\":\"not_owner\"}");
                out.flush();
                return false;
            }

            // 保存到 /uploads/covers/ 目录
            String uploadDir = getServletContext().getRealPath("/uploads/covers");
            new File(uploadDir).mkdirs();
            String ext = fileItem.getName().lastIndexOf('.') > 0
                    ? fileItem.getName().substring(fileItem.getName().lastIndexOf('.'))
                    : ".jpg";
            String fileName = "cover_" + bookId + "_" + System.currentTimeMillis() + ext;
            File uploadedFile = new File(uploadDir, fileName);
            fileItem.write(uploadedFile);

            // 更新数据库路径
            String relativePath = ctx + "/uploads/covers/" + fileName;
            boolean ok = bookDAO.updateCoverImage(bookId, relativePath);
            out.write("{\"success\":" + ok + ",\"url\":\"" + relativePath + "\"}");
            out.flush();
            return ok;

        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"success\":false,\"error\":\"upload_failed\"}");
            out.flush();
            return false;
        }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }
}
