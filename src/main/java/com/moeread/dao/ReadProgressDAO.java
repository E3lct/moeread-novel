package com.moeread.dao;

import com.moeread.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 阅读进度数据访问层 (DAO)
 * 负责 read_progress 表读写
 */
public class ReadProgressDAO {

    /**
     * 查询某用户某本书的阅读进度，没有则返回 null
     */
    public int[] find(int userId, int bookId) {
        // 返回 [chapterIndex, scrollPercent]，null 表示无记录
        String sql = "SELECT chapter_index, scroll_percent FROM read_progress WHERE user_id = ? AND book_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new int[]{ rs.getInt("chapter_index"), rs.getInt("scroll_percent") };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return null;
    }

    /**
     * 保存进度（不存在则插入，存在则更新）
     */
    public boolean upsert(int userId, int bookId, int chapterIndex, int scrollPercent) {
        String sql = "INSERT INTO read_progress (user_id, book_id, chapter_index, scroll_percent) "
                   + "VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE chapter_index = VALUES(chapter_index), scroll_percent = VALUES(scroll_percent)";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ps.setInt(3, chapterIndex);
            ps.setInt(4, scrollPercent);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }
}
