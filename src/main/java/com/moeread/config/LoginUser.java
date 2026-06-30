package com.moeread.config;

import java.lang.annotation.*;

/**
 * 标记需要登录的接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginUser {
}
