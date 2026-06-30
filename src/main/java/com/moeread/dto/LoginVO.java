package com.moeread.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginVO {
    private String token;
    private Integer userId;
    private String username;
    private String nickname;

    public LoginVO(String token, Integer userId, String username, String nickname) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
    }
}
