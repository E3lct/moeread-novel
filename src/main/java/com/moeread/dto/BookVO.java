package com.moeread.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 书籍列表 VO
 */
@Data
public class BookVO {
    private Integer id;
    private String title;
    private String author;
    private String description;
    private String coverColor;
    private String coverImagePath;
    private String sourceType;
    private String status;
    private Integer isFavorite;
    private Integer seriesId;
    private Integer chapterCount;
    private Integer totalWords;
    private LocalDateTime createTime;
    /** 阅读进度百分比 0-100 */
    private Integer readProgress;
    /** 当前阅读章节索引 */
    private Integer currentChapter;
    /** 标签列表 */
    private java.util.List<String> tags;
}
