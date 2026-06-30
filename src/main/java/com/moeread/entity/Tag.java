package com.moeread.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 标签实体
 */
@Data
@TableName("tags")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private String name;
    private String color;
    private Integer isSystem;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
