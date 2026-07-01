package com.moeread.dto;

import lombok.Data;

@Data
public class SourceBookVO {
    private Integer sourceId;
    private String sourceName;
    private String title;
    private String author;
    private String description;
    private String coverUrl;
    private String contentUrl;
}
