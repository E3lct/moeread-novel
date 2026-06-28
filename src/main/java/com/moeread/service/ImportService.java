package com.moeread.service;

import com.moeread.dao.BookDAO;
import com.moeread.dao.ChapterDAO;
import com.moeread.model.Book;
import com.moeread.model.Chapter;
import com.moeread.util.LegadoTxtTocRules;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    // 单章最大字数（超过则按段落断点拆分，借鉴 Legado "拆分超长章节" 思路）
    private static final int MAX_CHAPTER_WORDS = 30000;
    // 选规则时取文件前 512KB 作样本（Legado TextFile 默认值）
    private static final int SAMPLE_LIMIT = 524288;

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
     * 解析章节（移植 Legado TextFile 算法）
     * 1. 取前 512KB 作样本，用 Legado 的 12 条默认规则选出最佳匹配规则
     * 2. 用选中规则对全文做 MULTILINE 匹配，按匹配位置切分章节
     * 3. 第一个匹配前的内容若非空，作为"前言"
     * 4. 超长章节（>3万字）按段落断点自动拆分
     * 5. 无规则命中则整篇作为一章
     */
    private List<Chapter> parseChapters(String content) {
        List<Chapter> rawChapters = new ArrayList<>();

        // 1. 选最佳规则
        String sample = content.length() > SAMPLE_LIMIT ? content.substring(0, SAMPLE_LIMIT) : content;
        int ruleIndex = LegadoTxtTocRules.selectBestRule(sample);

        if (ruleIndex >= 0) {
            // 2. 用选中规则切分全文
            List<int[]> positions = LegadoTxtTocRules.splitChapters(content, ruleIndex);
            if (!positions.isEmpty()) {
                // 3. 第一个匹配前的内容作为"前言"
                int firstStart = positions.get(0)[0];
                if (firstStart > 0) {
                    String preface = content.substring(0, firstStart).trim();
                    if (!preface.isEmpty() && preface.length() > 50) {
                        Chapter ch = new Chapter();
                        ch.setChapterIndex(1);
                        ch.setTitle("前言");
                        ch.setContent(preface);
                        ch.setWordCount(countWords(preface));
                        rawChapters.add(ch);
                    }
                }
                // 4. 各章节
                for (int i = 0; i < positions.size(); i++) {
                    int[] pos = positions.get(i);
                    String title = content.substring(pos[0], pos[1]).trim();
                    String body;
                    if (i < positions.size() - 1) {
                        body = content.substring(pos[1], positions.get(i + 1)[0]).trim();
                    } else {
                        body = content.substring(pos[1]).trim();
                    }
                    Chapter ch = new Chapter();
                    ch.setChapterIndex(rawChapters.size() + 1);
                    ch.setTitle(title);
                    ch.setContent(body);
                    ch.setWordCount(countWords(body));
                    rawChapters.add(ch);
                }
            }
        }

        // 5. 兜底：没识别到章节，整篇作为一章
        if (rawChapters.isEmpty()) {
            Chapter ch = new Chapter();
            ch.setChapterIndex(1);
            ch.setTitle("全文");
            ch.setContent(content.trim());
            ch.setWordCount(countWords(content));
            rawChapters.add(ch);
        }

        // 6. 拆分超长章节
        List<Chapter> result = new ArrayList<>();
        for (Chapter ch : rawChapters) {
            if (ch.getWordCount() > MAX_CHAPTER_WORDS) {
                result.addAll(splitLongChapter(ch));
            } else {
                result.add(ch);
            }
        }
        // 重新编号
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setChapterIndex(i + 1);
        }
        return result;
    }

    /**
     * 拆分超长章节（按段落断点，每段约 MAX_CHAPTER_WORDS 字）
     */
    private List<Chapter> splitLongChapter(Chapter ch) {
        List<Chapter> parts = new ArrayList<>();
        String content = ch.getContent();
        // 按空行分段
        String[] paragraphs = content.split("\\n\\s*\\n");
        StringBuilder buf = new StringBuilder();
        int partNum = 0;
        int bufWords = 0;
        for (String para : paragraphs) {
            String p = para.trim();
            if (p.isEmpty()) continue;
            if (buf.length() > 0) buf.append("\n\n");
            buf.append(p);
            bufWords += countWords(p);
            // 达到上限就切一段
            if (bufWords >= MAX_CHAPTER_WORDS) {
                partNum++;
                Chapter part = new Chapter();
                part.setChapterIndex(0); // 后面统一重新编号
                part.setTitle(ch.getTitle() + "（" + partNum + "）");
                part.setContent(buf.toString());
                part.setWordCount(bufWords);
                parts.add(part);
                buf = new StringBuilder();
                bufWords = 0;
            }
        }
        // 剩余内容
        if (bufWords > 0) {
            partNum++;
            Chapter part = new Chapter();
            part.setChapterIndex(0);
            part.setTitle(partNum == 1 ? ch.getTitle() : ch.getTitle() + "（" + partNum + "）");
            part.setContent(buf.toString());
            part.setWordCount(bufWords);
            parts.add(part);
        }
        return parts;
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
