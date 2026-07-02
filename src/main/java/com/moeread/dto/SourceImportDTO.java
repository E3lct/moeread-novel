package com.moeread.dto;

import lombok.Data;

@Data
public class SourceImportDTO {
    private Integer sourceId;
    private String title;
    private String author;
    private String contentUrl;
    private String coverUrl;
    private String description;
}
