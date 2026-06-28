package com.moeread.dao;

import com.moeread.model.Book;
import com.moeread.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 阅读进度数据访问层 (DAO)
 * 负责 read_progress 表读写
 */
public class ReadProgressDAO {

    /**
     * 查询某用户某本书的阅读进度，没有则返回 null
     */
    public int[] find(int userId, int bookId) {
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

    /**
     * 查询最近阅读的图书（按阅读进度更新时间排序，JOIN books）
     * @return List<Book>，每本书的进度信息可通过 find() 单独查
     */
    public List<Book> findRecentBooks(int userId, int limit) {
        String sql = "SELECT b.* FROM books b "
                   + "INNER JOIN read_progress p ON b.id = p.book_id "
                   + "WHERE p.user_id = ? "
                   + "ORDER BY p.update_time DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Book> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return list;
    }

    /**
     * ResultSet -> Book 映射（与 BookDAO.mapRow 一致）
     */
    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setUserId(rs.getInt("user_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setDescription(rs.getString("description"));
        book.setCoverColor(rs.getString("cover_color"));
        book.setCoverImagePath(rs.getString("cover_image_path"));
        book.setSourceType(rs.getString("source_type"));
        book.setStatus(rs.getString("status"));
        book.setIsFavorite(rs.getInt("is_favorite"));
        Object seriesId = rs.getObject("series_id");
        if (seriesId != null) {
            book.setSeriesId(rs.getInt("series_id"));
        }
        book.setChapterCount(rs.getInt("chapter_count"));
        book.setTotalWords(rs.getInt("total_words"));
        book.setCreateTime(rs.getTimestamp("create_time"));
        return book;
    }
}
