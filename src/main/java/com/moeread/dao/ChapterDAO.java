package com.moeread.dao;

import com.moeread.model.Chapter;
import com.moeread.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 章节数据访问层 (DAO)
 * 负责 chapters 表读写
 */
public class ChapterDAO {

    /**
     * 批量插入章节（一次一条，事务包裹保证一致性）
     */
    public boolean insertBatch(int bookId, List<Chapter> chapters) {
        String sql = "INSERT INTO chapters (book_id, chapter_index, title, content, word_count) "
                   + "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 开启事务

            ps = conn.prepareStatement(sql);
            for (Chapter ch : chapters) {
                ps.setInt(1, bookId);
                ps.setInt(2, ch.getChapterIndex());
                ps.setString(3, ch.getTitle());
                ps.setString(4, ch.getContent());
                ps.setInt(5, ch.getWordCount());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            success = true;
        } catch (SQLException e) {
            // 回滚
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 查询某本书的某一章
     */
    public Chapter findByBookIdAndIndex(int bookId, int chapterIndex) {
        String sql = "SELECT * FROM chapters WHERE book_id = ? AND chapter_index = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Chapter chapter = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            ps.setInt(2, chapterIndex);
            rs = ps.executeQuery();
            if (rs.next()) {
                chapter = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return chapter;
    }

    /**
     * 查询某本书的全部章节目录（只取 id, chapter_index, title，不取正文，省内存）
     */
    public List<Chapter> findTocByBookId(int bookId) {
        String sql = "SELECT id, book_id, chapter_index, title, word_count FROM chapters WHERE book_id = ? ORDER BY chapter_index";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Chapter> list = new ArrayList<>();

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Chapter ch = new Chapter();
                ch.setId(rs.getInt("id"));
                ch.setBookId(rs.getInt("book_id"));
                ch.setChapterIndex(rs.getInt("chapter_index"));
                ch.setTitle(rs.getString("title"));
                ch.setWordCount(rs.getInt("word_count"));
                list.add(ch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return list;
    }

    /**
     * 统计某本书的章节数
     */
    public int countByBookId(int bookId) {
        String sql = "SELECT COUNT(*) FROM chapters WHERE book_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return count;
    }

    /**
     * ResultSet -> Chapter 映射
     */
    private Chapter mapRow(ResultSet rs) throws SQLException {
        Chapter ch = new Chapter();
        ch.setId(rs.getInt("id"));
        ch.setBookId(rs.getInt("book_id"));
        ch.setChapterIndex(rs.getInt("chapter_index"));
        ch.setTitle(rs.getString("title"));
        ch.setContent(rs.getString("content"));
        ch.setWordCount(rs.getInt("word_count"));
        return ch;
    }
}
