package com.moeread.dto;

import lombok.Data;

/**
 * 用户信息 VO（脱敏，不含密码）
 */
@Data
public class UserVO {
    private Integer id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer dailyGoal;
    private String mascotImage;
    private Integer mascotOpacity;
    private String themeMode;
    private Integer bgScale;
    private Integer bgMirror;
}
