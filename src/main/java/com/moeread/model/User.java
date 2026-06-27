package com.moeread.model;

import java.util.Date;

/**
 * 用户实体类 (JavaBean)
 * 对应数据库 users 表
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private int dailyGoal;
    private String mascotImage;
    private int mascotOpacity;
    private String themeMode;
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getDailyGoal() {
        return dailyGoal;
    }

    public void setDailyGoal(int dailyGoal) {
        this.dailyGoal = dailyGoal;
    }

    public String getMascotImage() {
        return mascotImage;
    }

    public void setMascotImage(String mascotImage) {
        this.mascotImage = mascotImage;
    }

    public int getMascotOpacity() {
        return mascotOpacity;
    }

    public void setMascotOpacity(int mascotOpacity) {
        this.mascotOpacity = mascotOpacity;
    }

    public String getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(String themeMode) {
        this.themeMode = themeMode;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
