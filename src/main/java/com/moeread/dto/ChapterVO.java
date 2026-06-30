package com.moeread.dto;

import lombok.Data;

@Data
public class ChapterVO {
    private Integer id;
    private Integer bookId;
    private Integer chapterIndex;
    private String title;
    private String content;
    private Integer wordCount;
}
