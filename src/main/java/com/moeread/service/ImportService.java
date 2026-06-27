package com.moeread.service;

import com.moeread.dao.BookDAO;
import com.moeread.dao.ChapterDAO;
import com.moeread.model.Book;
import com.moeread.model.Chapter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 导入解析服务 (Service)
 * 负责解析 txt 和 zip 文件，提取章节，存入数据库
 *
 * 章节标题识别规则（常见的几种）：
 *   第1章 标题 / 第一章 标题 / 第1回 标题 / 第1节 标题
 *   Chapter 1 Title / CHAPTER 1
 *   也可以只有 "第X章" 没有标题
 */
public class ImportService {

    private BookDAO bookDAO = new BookDAO();
    private ChapterDAO chapterDAO = new ChapterDAO();

    // 章节标题正则（支持：第一章 / 第1章 / 第123回 / 第一节 / 卷一 等）
    private static final Pattern CHAPTER_PATTERN = Pattern.compile(
        "^\\s*(第[零一二三四五六七八九十百千万0-9]+[章回节卷])\\s*[\\s:：、\\.\\-]*(.*)$"
    );

    // 英文章节：Chapter 1 / CHAPTER 1
    private static final Pattern CHAPTER_EN_PATTERN = Pattern.compile(
        "^\\s*(?:Chapter|CHAPTER)\\s*([0-9]+)\\s*[:：\\.\\-]*(.*)$"
    );

    /**
     * 导入单个 txt 文件
     * @param userId 用户ID
     * @param fileName 文件名（作为书名）
     * @param fileBytes 文件内容字节数组
     * @return 成功返回 Book 对象，失败返回 null
     */
    public Book importTxt(int userId, String fileName, byte[] fileBytes) {
        // 1. 解析文件内容
        String content = decodeContent(fileBytes);
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        // 2. 书名 = 文件名去掉 .txt 后缀
        String title = fileName;
        int dotIdx = title.lastIndexOf('.');
        if (dotIdx > 0) {
            title = title.substring(0, dotIdx);
        }

        // 3. 解析章节
        List<Chapter> chapters = parseChapters(content);
        if (chapters.isEmpty()) {
            // 没识别到章节，整篇作为一章
            Chapter ch = new Chapter();
            ch.setChapterIndex(1);
            ch.setTitle("全文");
            ch.setContent(content.trim());
            ch.setWordCount(countWords(content));
            chapters.add(ch);
        }

        // 4. 存入数据库
        return saveBook(userId, title, chapters);
    }

    /**
     * 导入 zip 文件（批量）
     * @param userId 用户ID
     * @param fileBytes zip 文件内容
     * @return 成功导入的书数量
     */
    public int importZip(int userId, byte[] fileBytes) {
        int successCount = 0;
        try {
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(fileBytes), Charset.forName("GBK"));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (!name.toLowerCase().endsWith(".txt")) continue;

                // 读取这个 txt 的内容
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int len;
                while ((len = zis.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                byte[] txtBytes = baos.toByteArray();

                Book book = importTxt(userId, name, txtBytes);
                if (book != null) {
                    successCount++;
                }
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return successCount;
    }

    /**
     * 解析章节
     * 逐行扫描，遇到 "第X章" 就开新章节
     */
    private List<Chapter> parseChapters(String content) {
        List<Chapter> chapters = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");

        int chapterIndex = 0;
        StringBuilder currentContent = new StringBuilder();
        String currentTitle = null;

        for (String line : lines) {
            String trimmed = line.trim();
            Matcher mZh = CHAPTER_PATTERN.matcher(trimmed);
            Matcher mEn = CHAPTER_EN_PATTERN.matcher(trimmed);

            if (mZh.matches() || mEn.matches()) {
                // 遇到新章节标题，保存上一章
                if (chapterIndex > 0) {
                    Chapter ch = new Chapter();
                    ch.setChapterIndex(chapterIndex);
                    ch.setTitle(currentTitle != null ? currentTitle : "第" + chapterIndex + "章");
                    String body = currentContent.toString().trim();
                    ch.setContent(body);
                    ch.setWordCount(countWords(body));
                    chapters.add(ch);
                }
                // 开始新章节
                chapterIndex++;
                if (mZh.matches()) {
                    currentTitle = mZh.group(1) + (mZh.group(2).isEmpty() ? "" : " " + mZh.group(2));
                } else {
                    currentTitle = mEn.group(0).trim();
                }
                currentContent = new StringBuilder();
            } else {
                // 正文行
                if (currentContent.length() > 0) {
                    currentContent.append("\n");
                }
                currentContent.append(line);
            }
        }

        // 保存最后一章
        if (chapterIndex > 0) {
            Chapter ch = new Chapter();
            ch.setChapterIndex(chapterIndex);
            ch.setTitle(currentTitle != null ? currentTitle : "第" + chapterIndex + "章");
            String body = currentContent.toString().trim();
            ch.setContent(body);
            ch.setWordCount(countWords(body));
            chapters.add(ch);
        }

        return chapters;
    }

    /**
     * 保存图书和章节到数据库
     */
    private Book saveBook(int userId, String title, List<Chapter> chapters) {
        // 统计总字数
        int totalWords = 0;
        for (Chapter ch : chapters) {
            totalWords += ch.getWordCount();
        }

        // 创建 Book
        Book book = new Book();
        book.setUserId(userId);
        book.setTitle(title);
        book.setAuthor("未知");
        book.setDescription("");
        book.setCoverColor(Book.randomCoverColor());
        book.setSourceType("import");
        book.setStatus("prepare");
        book.setIsFavorite(0);
        book.setChapterCount(chapters.size());
        book.setTotalWords(totalWords);

        int bookId = bookDAO.insert(book);
        if (bookId <= 0) {
            return null;
        }

        // 批量插入章节
        boolean ok = chapterDAO.insertBatch(bookId, chapters);
        if (!ok) {
            // 章节插入失败，删除书
            bookDAO.delete(bookId);
            return null;
        }

        book.setId(bookId);
        return book;
    }

    /**
     * 自动识别文件编码（UTF-8 / GBK）
     */
    private String decodeContent(byte[] bytes) {
        // 先试 UTF-8
        try {
            String utf8 = new String(bytes, StandardCharsets.UTF_8);
            // UTF-8 BOM
            if (utf8.startsWith("\uFEFF")) {
                utf8 = utf8.substring(1);
            }
            // 简单判断：如果不含乱码字符，认为 UTF-8 正确
            if (!hasReplacementChar(utf8)) {
                return utf8;
            }
        } catch (Exception e) {
            // ignore
        }
        // 退回 GBK
        try {
            return new String(bytes, "GBK");
        } catch (Exception e) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * 检查是否包含替换字符（UTF-8 解码失败的标志）
     */
    private boolean hasReplacementChar(String s) {
        return s.indexOf('\uFFFD') >= 0;
    }

    /**
     * 统计字数（中文字符算1个，英文按空格分词）
     */
    private int countWords(String text) {
        if (text == null || text.isEmpty()) return 0;
        // 简单算法：去掉空白后的字符数
        return text.replaceAll("\\s+", "").length();
    }

    // 内部用：ByteArrayOutputStream
    private static class ByteArrayOutputStream {
        private byte[] buf = new byte[8192];
        private int count = 0;

        public void write(byte[] b, int off, int len) {
            ensureCapacity(count + len);
            System.arraycopy(b, off, buf, count, len);
            count += len;
        }

        public byte[] toByteArray() {
            byte[] result = new byte[count];
            System.arraycopy(buf, 0, result, 0, count);
            return result;
        }

        private void ensureCapacity(int minCapacity) {
            if (minCapacity > buf.length) {
                int newCapacity = buf.length * 2;
                while (newCapacity < minCapacity) {
                    newCapacity *= 2;
                }
                byte[] newBuf = new byte[newCapacity];
                System.arraycopy(buf, 0, newBuf, 0, count);
                buf = newBuf;
            }
        }
    }
}
