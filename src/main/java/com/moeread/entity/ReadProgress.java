package com.moeread.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 阅读进度实体
 */
@Data
@TableName("read_progress")
public class ReadProgress {

    private Integer userId;
    private Integer bookId;
    private Integer chapterIndex;
    private Integer scrollPercent;
    private LocalDateTime updateTime;
}
