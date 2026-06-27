package com.moeread.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库连接工具类
 * 负责获取连接、释放资源
 * 学生写法：每个方法独立处理异常，不做连接池（作业版够用）
 */
public class DBUtil {

    // 数据库连接信息
    private static final String URL = "jdbc:mysql://localhost:3306/moeread?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

    // 加载驱动（类加载时执行一次）
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("MySQL 驱动加载失败", e);
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * 关闭资源（顺序：ResultSet -> PreparedStatement -> Connection）
     */
    public static void close(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (ps != null) ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭资源（无 ResultSet 的场景）
     */
    public static void close(PreparedStatement ps, Connection conn) {
        close(null, ps, conn);
    }
}
