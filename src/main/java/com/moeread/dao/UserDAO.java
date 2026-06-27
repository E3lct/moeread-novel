package com.moeread.dao;

import com.moeread.model.User;
import com.moeread.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户数据访问层 (DAO)
 * 只负责数据库读写，不处理业务逻辑
 */
public class UserDAO {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return User 对象，找不到返回 null
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();

            if (rs.next()) {
                user = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return user;
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return User 对象，找不到返回 null
     */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                user = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return user;
    }

    /**
     * 新增用户
     * @param user 用户对象
     * @return 成功返回 true，失败返回 false
     */
    public boolean insert(User user) {
        String sql = "INSERT INTO users (username, password, nickname) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNickname());
            int rows = ps.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 更新用户设置
     * @param user 用户对象
     * @return 成功返回 true
     */
    public boolean updateSettings(User user) {
        String sql = "UPDATE users SET nickname = ?, daily_goal = ?, mascot_image = ?, mascot_opacity = ?, theme_mode = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getNickname());
            ps.setInt(2, user.getDailyGoal());
            ps.setString(3, user.getMascotImage());
            ps.setInt(4, user.getMascotOpacity());
            ps.setString(5, user.getThemeMode());
            ps.setInt(6, user.getId());
            int rows = ps.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param newPassword MD5加密后的新密码
     * @return 成功返回 true
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 将 ResultSet 映射为 User 对象
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setNickname(rs.getString("nickname"));
        user.setAvatar(rs.getString("avatar"));
        user.setDailyGoal(rs.getInt("daily_goal"));
        user.setMascotImage(rs.getString("mascot_image"));
        user.setMascotOpacity(rs.getInt("mascot_opacity"));
        user.setThemeMode(rs.getString("theme_mode"));
        user.setCreateTime(rs.getTimestamp("create_time"));
        return user;
    }
}
