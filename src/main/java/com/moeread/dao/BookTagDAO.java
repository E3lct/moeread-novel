package com.moeread.dao;

import com.moeread.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义标签数据访问层 (DAO)
 * 负责 book_tags 表读写
 */
public class BookTagDAO {

    /**
     * 查询某用户的所有标签名（去重，按使用频率降序）
     */
    public List<String> findTagsByUserId(int userId) {
        String sql = "SELECT tag_name, COUNT(*) AS cnt FROM book_tags WHERE user_id = ? GROUP BY tag_name ORDER BY cnt DESC, tag_name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("tag_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return list;
    }

    /**
     * 查询某本书的所有标签
     */
    public List<String> findTagsByBookId(int bookId) {
        String sql = "SELECT tag_name FROM book_tags WHERE book_id = ? ORDER BY tag_name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("tag_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return list;
    }

    /**
     * 查询某用户所有书的标签映射 (bookId -> tags)
     */
    public Map<Integer, List<String>> findTagMapByUserId(int userId) {
        String sql = "SELECT book_id, tag_name FROM book_tags WHERE user_id = ? ORDER BY tag_name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<Integer, List<String>> map = new HashMap<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String tag = rs.getString("tag_name");
                map.computeIfAbsent(bookId, k -> new ArrayList<>()).add(tag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return map;
    }

    /**
     * 按标签分组查询某用户的书籍 (tagName -> bookIdList)，保留插入顺序
     * 仅返回有标签的书，"未分组"由上层补充
     */
    public Map<String, List<Integer>> findBooksGroupedByTag(int userId) {
        String sql = "SELECT tag_name, book_id FROM book_tags WHERE user_id = ? ORDER BY tag_name, id";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, List<Integer>> map = new LinkedHashMap<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                String tag = rs.getString("tag_name");
                int bookId = rs.getInt("book_id");
                map.computeIfAbsent(tag, k -> new ArrayList<>()).add(bookId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return map;
    }

    /**
     * 添加标签（已存在则忽略）
     */
    public boolean addTag(int userId, int bookId, String tagName) {
        String sql = "INSERT IGNORE INTO book_tags (user_id, book_id, tag_name) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ps.setString(3, tagName);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 删除某本书的某个标签
     */
    public boolean removeTag(int userId, int bookId, String tagName) {
        String sql = "DELETE FROM book_tags WHERE user_id = ? AND book_id = ? AND tag_name = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ps.setString(3, tagName);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(ps, conn);
        }
        return success;
    }

    /**
     * 删除某本书的所有标签（删书时调用）
     */
    public boolean removeAllTags(int bookId) {
        String sql = "DELETE FROM book_tags WHERE book_id = ?";
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
}
