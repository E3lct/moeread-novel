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
 * 图书数据访问层 (DAO)
 * 只负责 books 表的读写
 */
public class BookDAO {

    /**
     * 新增图书（返回自增主键 id）
     */
    public int insert(Book book) {
        String sql = "INSERT INTO books (user_id, title, author, description, cover_color, source_type, status, is_favorite, chapter_count, total_words) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        int bookId = -1;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, book.getUserId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getDescription());
            ps.setString(5, book.getCoverColor());
            ps.setString(6, book.getSourceType());
            ps.setString(7, book.getStatus());
            ps.setInt(8, book.getIsFavorite());
            ps.setInt(9, book.getChapterCount());
            ps.setInt(10, book.getTotalWords());
            ps.executeUpdate();

            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                bookId = keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(keys, ps, conn);
        }
        return bookId;
    }

    /**
     * 根据 ID 查询
     */
    public Book findById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Book book = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                book = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return book;
    }

    /**
     * 查询某用户的所有图书
     */
    public List<Book> findByUserId(int userId) {
        String sql = "SELECT * FROM books WHERE user_id = ? ORDER BY create_time DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Book> list = new ArrayList<>();

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
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
     * 更新图书统计信息（章节数、总字数）
     */
    public boolean updateStats(int bookId, int chapterCount, int totalWords) {
        String sql = "UPDATE books SET chapter_count = ?, total_words = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, chapterCount);
            ps.setInt(2, totalWords);
            ps.setInt(3, bookId);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 更新阅读状态
     */
    public boolean updateStatus(int bookId, String status) {
        String sql = "UPDATE books SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, bookId);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 更新喜欢状态
     */
    public boolean updateFavorite(int bookId, int isFavorite) {
        String sql = "UPDATE books SET is_favorite = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, isFavorite);
            ps.setInt(2, bookId);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 更新图书元信息（书名 + 封面色）
     */
    public boolean updateMeta(int bookId, String title, String coverColor) {
        String sql = "UPDATE books SET title = ?, cover_color = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, title);
            ps.setString(2, coverColor);
            ps.setInt(3, bookId);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 更新封面图片路径
     */
    public boolean updateCoverImage(int bookId, String coverImagePath) {
        String sql = "UPDATE books SET cover_image_path = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, coverImagePath);
            ps.setInt(2, bookId);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 删除图书
     */
    public boolean delete(int bookId) {
        String sql = "DELETE FROM books WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * ResultSet -> Book 映射
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
