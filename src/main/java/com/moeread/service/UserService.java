package com.moeread.service;

import com.moeread.dao.UserDAO;
import com.moeread.model.User;
import com.moeread.util.MD5Util;

/**
 * 用户业务逻辑层 (Service)
 * 封装登录验证、注册等业务逻辑
 * Controller 层只调 Service，不直接调 DAO
 */
public class UserService {

    private UserDAO userDAO = new UserDAO();

    /**
     * 用户登录验证
     * @param username 用户名
     * @param password 原始密码（未加密）
     * @return 登录成功返回 User 对象，失败返回 null
     */
    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return null; // 用户不存在
        }
        // 比对 MD5
        String md5Password = MD5Util.md5(password);
        if (user.getPassword().equals(md5Password)) {
            return user; // 密码正确
        }
        return null; // 密码错误
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param password 原始密码
     * @param nickname 昵称（可选）
     * @return 注册成功返回 User 对象，用户名已存在返回 null
     */
    public User register(String username, String password, String nickname) {
        // 检查用户名是否已存在
        User existing = userDAO.findByUsername(username);
        if (existing != null) {
            return null; // 用户名已被占用
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(MD5Util.md5(password));
        // 昵称为空时用用户名代替
        user.setNickname((nickname == null || nickname.isEmpty()) ? username : nickname);

        boolean success = userDAO.insert(user);
        if (success) {
            // 重新查一次，拿到带 id 的完整对象
            return userDAO.findByUsername(username);
        }
        return null;
    }

    /**
     * 根据 ID 获取用户信息
     */
    public User getUserById(int id) {
        return userDAO.findById(id);
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码（原始）
     * @param newPassword 新密码（原始）
     * @return 0=成功, 1=旧密码错误, 2=系统错误
     */
    public int changePassword(int userId, String oldPassword, String newPassword) {
        User user = userDAO.findById(userId);
        if (user == null) {
            return 2;
        }
        String oldMd5 = MD5Util.md5(oldPassword);
        if (!user.getPassword().equals(oldMd5)) {
            return 1; // 旧密码错误
        }
        boolean success = userDAO.updatePassword(userId, MD5Util.md5(newPassword));
        return success ? 0 : 2;
    }

    /**
     * 更新用户设置
     */
    public boolean updateSettings(User user) {
        return userDAO.updateSettings(user);
    }
}
