package com.moeread.dto;

import lombok.Data;

@Data
public class BookSourceDTO {
    private Integer sourceId;
    private String name;
    private String sourceKey;
    private String sourceType;
    private String baseUrl;
    private String description;
    private String language;
    private Integer enabled;
    private Integer isPreset;
}
