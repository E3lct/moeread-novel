package com.moeread.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 书籍实体
 */
@Data
@TableName("books")
public class Book {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 封面色系 */
    public static final String[] COVER_COLORS = {
        "#F59E0B", "#D97706", "#B45309", "#FBBF24", "#92400E"
    };

    public static String randomCoverColor() {
        return COVER_COLORS[(int) (Math.random() * COVER_COLORS.length)];
    }
}
