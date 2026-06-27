package com.moeread.model;

/**
 * 章节实体类 (JavaBean)
 * 对应数据库 chapters 表
 */
public class Chapter {

    private int id;
    private int bookId;
    private int chapterIndex;  // 第几章，从 1 开始
    private String title;      // 章节标题
    private String content;    // 正文
    private int wordCount;     // 字数

    // ---------- getters / setters ----------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getChapterIndex() { return chapterIndex; }
    public void setChapterIndex(int chapterIndex) { this.chapterIndex = chapterIndex; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }
}
