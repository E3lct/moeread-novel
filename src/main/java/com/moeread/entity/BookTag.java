package com.moeread.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 书籍标签关联实体
 */
@Data
@TableName("book_tags")
public class BookTag {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private Integer bookId;
    private String tagName;
    private LocalDateTime createTime;
}
