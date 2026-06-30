package com.moeread.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private Integer dailyGoal;
    private String mascotImage;
    private Integer mascotOpacity;
    private String themeMode;
    private Integer bgScale;
    private Integer bgMirror;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
