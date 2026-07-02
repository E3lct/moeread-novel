package com.moeread.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

@Data
public class BookSourceDTO {
    private Integer sourceId;
    @JsonAlias({"sourceName", "bookSourceName"})
    private String name;
    private String sourceKey;
    private String sourceType;
    @JsonAlias({"sourceUrl", "bookSourceUrl"})
    private String baseUrl;
    @JsonAlias({"ruleSearchUrl"})
    private String searchUrl;
    @JsonAlias({"ruleContentUrl", "contentUrl"})
    private String contentUrlTemplate;
    private String ruleConfig;
    @JsonAlias({"sourceComment", "bookSourceComment", "comment", "desc"})
    private String description;
    private String language;
    private Integer enabled;
    private Integer isPreset;

    @JsonSetter("enabled")
    public void setEnabledValue(Object value) {
        this.enabled = toFlag(value, 1);
    }

    @JsonSetter("isPreset")
    public void setIsPresetValue(Object value) {
        this.isPreset = toFlag(value, 0);
    }

    private Integer toFlag(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Boolean bool) return bool ? 1 : 0;
        if (value instanceof Number number) return number.intValue();
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return defaultValue;
        if ("true".equalsIgnoreCase(text)) return 1;
        if ("false".equalsIgnoreCase(text)) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
