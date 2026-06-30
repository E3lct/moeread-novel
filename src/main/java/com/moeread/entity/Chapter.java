package com.moeread.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 章节实体
 */
@Data
@TableName("chapters")
public class Chapter {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer bookId;
    private Integer chapterIndex;
    private String title;
    private String content;
    private Integer wordCount;
}
