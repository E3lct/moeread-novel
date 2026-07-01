package com.moeread.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("book_sources")
public class BookSource {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private String name;
    private String sourceKey;
    private String sourceType;
    private String baseUrl;
    private String description;
    private String language;
    private Integer enabled;
    private Integer isPreset;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
