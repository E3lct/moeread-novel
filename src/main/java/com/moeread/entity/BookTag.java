package com.moeread.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 书籍标签实体
 */
@Data
@TableName("book_tags")
public class BookTag {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer bookId;
    private Integer tagId;
}
