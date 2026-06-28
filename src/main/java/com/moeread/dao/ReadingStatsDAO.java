package com.moeread.dao;

import com.moeread.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 阅读统计数据访问层 (DAO)
 * 负责 reading_stats 表读写
 */
public class ReadingStatsDAO {

    /**
     * 获取用户今日阅读分钟数
     */
    public int getTodayMinutes(int userId) {
        String sql = "SELECT read_minutes FROM reading_stats WHERE user_id = ? AND stat_date = CURDATE()";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("read_minutes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return 0;
    }

    /**
     * 累加今日阅读时长（不存在则插入，存在则累加）
     * @param minutes 新增的阅读分钟数
     */
    public boolean addTodayMinutes(int userId, int minutes) {
        if (minutes <= 0) return false;
        String sql = "INSERT INTO reading_stats (user_id, stat_date, read_minutes, is_checked_in) "
                   + "VALUES (?, CURDATE(), ?, 1) "
                   + "ON DUPLICATE KEY UPDATE read_minutes = read_minutes + VALUES(read_minutes), is_checked_in = 1";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, minutes);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 获取连续打卡天数（从今天往回数，连续 is_checked_in=1 的天数）
     */
    public int getStreakDays(int userId) {
        String sql = "SELECT stat_date, is_checked_in FROM reading_stats "
                   + "WHERE user_id = ? AND stat_date <= CURDATE() "
                   + "ORDER BY stat_date DESC LIMIT 365";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int streak = 0;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            java.sql.Date expected = java.sql.Date.valueOf(java.time.LocalDate.now());
            while (rs.next()) {
                java.sql.Date d = rs.getDate("stat_date");
                int checked = rs.getInt("is_checked_in");
                if (checked == 1 && d.equals(expected)) {
                    streak++;
                    expected = java.sql.Date.valueOf(expected.toLocalDate().minusDays(1));
                } else if (!d.equals(expected)) {
                    // 有间断，停止
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return streak;
    }
}
