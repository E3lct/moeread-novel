package com.moeread.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * 阅读统计实体
 */
@Data
@TableName("reading_stats")
public class ReadingStats {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private LocalDate statDate;
    private Integer readMinutes;
    private Integer isCheckedIn;
}
