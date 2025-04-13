package io.github.MoYuSOwO.moyushop.economy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class Economy {

    public static void setBalance(Player player, double amount) {
        double change = amount - getBalance(player);
        String changeString;
        if (change > 0) changeString = "+%.2f".formatted(change);
        else if (change < 0) changeString = "%.2f".formatted(change);
        else return;
        EconomyDB.updateBalance(player.getUUID(), amount);
        notifyPlayer(player, changeString);
    }

    public static double getBalance(Player player) {
        return EconomyDB.getBalance(player.getUUID());
    }

    public static boolean deposit(Player player, double amount) {
        if (amount <= 0) return false;
        double newBalance = getBalance(player) + amount;
        setBalance(player, newBalance);
        return true;
    }

    public static boolean withdraw(Player player, double amount) {
        if (amount <= 0 || getBalance(player) < amount) return false;
        double newBalance = getBalance(player) - amount;
        setBalance(player, newBalance);
        return true;
    }

    public static boolean transfer(Player from, Player to, double amount) {
        if (withdraw(from, amount)) {
            if (deposit(to, amount)) {
                return true;
            } else {
                deposit(from, amount);
            }
        }
        return false;
    }

    private static void notifyPlayer(Player player, String change) {
        player.sendSystemMessage(Component.literal(
                "[经济系统] 余额变动: %s 椰块，当前: %.2f 椰块".formatted(
                        change,
                        getBalance(player)
                )));
    }
}
