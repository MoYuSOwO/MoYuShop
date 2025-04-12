package io.github.MoYuSOwO.moyushop.economy;

import io.github.MoYuSOwO.moyushop.Moyushop;

import java.nio.file.Path;
import java.sql.*;
import java.util.UUID;

public final class EconomyDB {
    private static Connection connection;

    private EconomyDB() {}

    public static void init(Path modDataDir) {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + modDataDir.resolve("economy.db")
            );
            Statement stmt = connection.createStatement();
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS balances (
                    uuid TEXT PRIMARY KEY,
                    balance REAL NOT NULL DEFAULT 0.0
                )
                """);
        } catch (SQLException e) {
            Moyushop.LOGGER.error("初始化失败: ", e);
        }
    }

    public static double getBalance(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT balance FROM balances WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("balance") : 0.0;
        } catch (SQLException e) {
            Moyushop.LOGGER.error("查询余额失败: {}", uuid, e);
            return 0.0;
        }
    }

    public static void updateBalance(UUID uuid, double newBalance) {
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO balances (uuid, balance) VALUES (?, ?)")
            ) {
                ps.setString(1, uuid.toString());
                ps.setDouble(2, newBalance);
                ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
                Moyushop.LOGGER.error("回滚事务失败", ex);
            }
            Moyushop.LOGGER.error("更新余额失败: {} -> {}", uuid, newBalance, e);
        }
    }

    public static void shutdown() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) connection.close();
            } catch (SQLException e) {
                Moyushop.LOGGER.error("关闭数据库连接失败", e);
            }
        }
    }

}
