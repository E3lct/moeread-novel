package com.moeread.model;

import java.sql.Timestamp;

/**
 * 图书实体类 (JavaBean)
 * 对应数据库 books 表
 */
public class Book {

    private int id;
    private int userId;
    private String title;
    private String author;
    private String description;
    private String coverColor;
    private String coverImagePath;
    private String sourceType;   // import / public
    private String status;       // reading / prepare / finished
    private int isFavorite;      // 0 否 / 1 是
    private Integer seriesId;    // 套书ID，可空
    private int chapterCount;
    private int totalWords;
    private Timestamp createTime;

    // 封面色系（导入时随机分配琥珀色系渐变）
    private static final String[] COVER_COLORS = {
        "#F59E0B", "#D97706", "#B45309", "#FBBF24", "#92400E"
    };

    /**
     * 随机取一个封面色（导入新书时用）
     */
    public static String randomCoverColor() {
        int idx = (int) (Math.random() * COVER_COLORS.length);
        return COVER_COLORS[idx];
    }

    // ---------- getters / setters ----------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverColor() { return coverColor; }
    public void setCoverColor(String coverColor) { this.coverColor = coverColor; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getIsFavorite() { return isFavorite; }
    public void setIsFavorite(int isFavorite) { this.isFavorite = isFavorite; }

    public Integer getSeriesId() { return seriesId; }
    public void setSeriesId(Integer seriesId) { this.seriesId = seriesId; }

    public int getChapterCount() { return chapterCount; }
    public void setChapterCount(int chapterCount) { this.chapterCount = chapterCount; }

    public int getTotalWords() { return totalWords; }
    public void setTotalWords(int totalWords) { this.totalWords = totalWords; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}
