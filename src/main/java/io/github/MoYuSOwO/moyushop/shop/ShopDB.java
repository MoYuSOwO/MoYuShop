package io.github.MoYuSOwO.moyushop.shop;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.MoYuSOwO.moyushop.Moyushop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopDB {
    private static Connection connection;

    public record IdWithTime(int id, long createdTime) {}

    private static final Map<UUID, IdWithTime> PENDING_PURCHASES = new ConcurrentHashMap<>();

    public static String generatePurchaseToken(int itemId) {
        UUID token = UUID.randomUUID();
        PENDING_PURCHASES.put(token, new IdWithTime(itemId, System.currentTimeMillis()));
        return token.toString();
    }

    public static IdWithTime consumePurchaseToken(UUID token) {
        if (PENDING_PURCHASES.containsKey(token)) {
            return PENDING_PURCHASES.get(token);
        }
        return null;
    }

    public static void cleanExpiredTokens() {
        PENDING_PURCHASES.entrySet().removeIf(entry ->
                System.currentTimeMillis() - entry.getValue().createdTime() > 60000
        );
    }

    private ShopDB() {}

    public static void init(Path modDataDir) {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + modDataDir.resolve("shop.db")
            );
            Statement stmt = connection.createStatement();
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS shop_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    seller_uuid TEXT NOT NULL,
                    item_stack TEXT NOT NULL,
                    price DOUBLE NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )"""
            );
        } catch (SQLException e) {
            Moyushop.LOGGER.error("初始化失败: ", e);
        }
    }

    public static void addItem(UUID seller, ItemStack item, double price) {
        String sql = "INSERT INTO shop_items (seller_uuid, item_stack, price) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, seller.toString());
            pstmt.setString(2, serializeItemStack(item));
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Moyushop.LOGGER.error("添加物品错误: ", e);
        }
    }

    public static List<ShopItem> getAllItems() {
        List<ShopItem> items = new ArrayList<>();
        String sql = "SELECT * FROM shop_items";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new ShopItem(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("seller_uuid")),
                        deserializeItemStack(rs.getString("item_stack")),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            Moyushop.LOGGER.error("读取物品s错误: ", e);
        }
        return items;
    }

    public static void removeItem(int itemId) {
        String sql = "DELETE FROM shop_items WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Moyushop.LOGGER.error("删除物品错误: ", e);
        }
    }

    public static ShopItem getItem(int id) {
        String sql = "SELECT * FROM shop_items WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new ShopItem(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("seller_uuid")),
                        deserializeItemStack(rs.getString("item_stack")),
                        rs.getDouble("price")
                );
            }
        } catch (SQLException e) {
            Moyushop.LOGGER.error("读取物品错误: ", e);
        }
        return null;
    }

    private static String serializeItemStack(ItemStack stack) {
        CompoundTag tag;
        tag = (CompoundTag) stack.save(ServerLifecycleHooks.getCurrentServer().registryAccess());
        return tag.toString();
    }

    private static ItemStack deserializeItemStack(String data) {
        try {
            CompoundTag tag = TagParser.parseTag(data);
            return ItemStack.parse(ServerLifecycleHooks.getCurrentServer().registryAccess(), tag).get();
        } catch (CommandSyntaxException e) {
            throw new RuntimeException("Failed to parse item stack", e);
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
