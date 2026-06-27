package com.moeread.controller;

import com.moeread.model.Book;
import com.moeread.model.User;
import com.moeread.service.ImportService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 导入控制器 (Controller)
 * GET  /import  -> 显示导入页
 * POST /import  -> 处理文件上传
 *
 * 使用 commons-fileupload 解析 multipart 请求
 */
@WebServlet(name = "ImportServlet", urlPatterns = {"/import"})
public class ImportServlet extends HttpServlet {

    private ImportService importService = new ImportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/import.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 检查是否是 multipart 请求
        if (!ServletFileUpload.isMultipartContent(request)) {
            request.setAttribute("error", "请选择文件上传");
            request.getRequestDispatcher("/WEB-INF/jsp/import.jsp").forward(request, response);
            return;
        }

        // 获取当前登录用户
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            // 配置文件上传
            DiskFileItemFactory factory = new DiskFileItemFactory();
            // 限制内存缓冲 1MB，超过写临时文件
            factory.setSizeThreshold(1024 * 1024);
            ServletFileUpload upload = new ServletFileUpload(factory);
            // 限制单个文件 50MB（小说一般不会更大）
            upload.setFileSizeMax(50L * 1024 * 1024);
            upload.setHeaderEncoding("UTF-8");

            List<FileItem> items = upload.parseRequest(request);

            int successCount = 0;
            int failCount = 0;
            String lastBookTitle = "";
            String message = "";

            for (FileItem item : items) {
                if (item.isFormField()) {
                    continue; // 跳过普通表单字段
                }
                // 是文件
                String fileName = item.getName();
                if (fileName == null || fileName.isEmpty()) continue;

                byte[] fileBytes = item.get();
                String lowerName = fileName.toLowerCase();

                if (lowerName.endsWith(".txt")) {
                    Book book = importService.importTxt(user.getId(), fileName, fileBytes);
                    if (book != null) {
                        successCount++;
                        lastBookTitle = book.getTitle();
                    } else {
                        failCount++;
                    }
                } else if (lowerName.endsWith(".zip")) {
                    int cnt = importService.importZip(user.getId(), fileBytes);
                    successCount += cnt;
                    if (cnt == 0) failCount++;
                } else {
                    failCount++;
                    message = "不支持的文件类型，仅支持 .txt 和 .zip";
                }
            }

            if (successCount > 0) {
                if (successCount == 1) {
                    message = "导入成功：" + lastBookTitle;
                } else {
                    message = "批量导入完成，共 " + successCount + " 本";
                }
                request.setAttribute("success", message);
            } else {
                request.setAttribute("error", message.isEmpty() ? "导入失败，请检查文件格式" : message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "导入异常：" + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/jsp/import.jsp").forward(request, response);
    }
}
