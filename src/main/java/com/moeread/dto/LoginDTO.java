package com.moeread.dto;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginDTO {
    private String username;
    private String password;
}
