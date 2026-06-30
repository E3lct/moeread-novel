package com.moeread.dto;

import lombok.Data;

@Data
public class SaveProgressDTO {
    private Integer bookId;
    private Integer chapterIndex;
    private Integer scrollPosition;
}
