package com.moeread.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类
 * 负责获取连接、释放资源
 * 学生写法：每个方法独立处理异常，不做连接池（作业版够用）
 *
 * 配置从 config.properties 读取（该文件不上传 GitHub，保护密码）
 */
public class DBUtil {

    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    // 加载驱动和配置（类加载时执行一次）
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 从 classpath 读取配置文件
            Properties props = new Properties();
            InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("config.properties");
            if (is != null) {
                props.load(is);
                URL = props.getProperty("db.url");
                USERNAME = props.getProperty("db.username");
                PASSWORD = props.getProperty("db.password");
                is.close();
            } else {
                // 配置文件不存在时用默认值（方便测试）
                URL = "jdbc:mysql://localhost:3306/moeread?useSSL=false&useUnicode=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";
                USERNAME = "root";
                PASSWORD = "123456";
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据库配置加载失败", e);
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
