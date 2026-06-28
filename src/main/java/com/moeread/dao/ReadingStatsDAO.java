package com.moeread.dao;

import com.moeread.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

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
     * 使用 UPDATE 优先 + INSERT IGNORE 方式，避免 VALUES() 废弃语法
     * @param minutes 新增的阅读分钟数
     */
    public boolean addTodayMinutes(int userId, int minutes) {
        if (minutes <= 0) return false;
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DBUtil.getConnection();
            // 先尝试 UPDATE
            ps = conn.prepareStatement(
                "UPDATE reading_stats SET read_minutes = read_minutes + ?, is_checked_in = 1 " +
                "WHERE user_id = ? AND stat_date = CURDATE()"
            );
            ps.setInt(1, minutes);
            ps.setInt(2, userId);
            int updated = ps.executeUpdate();
            ps.close();
            if (updated > 0) {
                success = true;
            } else {
                // 没有今天记录，INSERT
                ps = conn.prepareStatement(
                    "INSERT IGNORE INTO reading_stats (user_id, stat_date, read_minutes, is_checked_in) " +
                    "VALUES (?, CURDATE(), ?, 1)"
                );
                ps.setInt(1, userId);
                ps.setInt(2, minutes);
                success = ps.executeUpdate() > 0;
            }
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

    /**
     * 获取用户总阅读分钟数
     */
    public int getTotalMinutes(int userId) {
        String sql = "SELECT SUM(read_minutes) FROM reading_stats WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return 0;
    }

    /**
     * 获取用户某年的每日阅读数据（用于热力图）
     * @return Map<"MM-dd", minutes> 按日期排序
     */
    public Map<String, Integer> getYearlyData(int userId, int year) {
        String sql = "SELECT stat_date, read_minutes FROM reading_stats "
                   + "WHERE user_id = ? AND YEAR(stat_date) = ? "
                   + "ORDER BY stat_date ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Integer> data = new LinkedHashMap<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, year);
            rs = ps.executeQuery();
            while (rs.next()) {
                java.sql.Date d = rs.getDate("stat_date");
                // 存 "MM-dd" 格式作为 key
                String key = String.format("%02d-%02d",
                    d.toLocalDate().getMonthValue(),
                    d.toLocalDate().getDayOfMonth());
                data.put(key, rs.getInt("read_minutes"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return data;
    }
}
