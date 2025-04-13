package io.github.MoYuSOwO.moyushop.guishop;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.MoYuSOwO.moyushop.economy.Economy;
import io.github.MoYuSOwO.moyushop.economy.EconomyDB;
import io.github.MoYuSOwO.moyushop.shop.ShopDB;
import io.github.MoYuSOwO.moyushop.shop.ShopItem;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ShopCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shopgui")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayer();
                    if (player == null) return 0;
                    openShop(player);
                    return 1;
                })
        );
    }

    public static void openShop(ServerPlayer player) {
        openShop(player, 1);
    }

    public static void replaceShop(ServerPlayer player, int newPage) {
        player.closeContainer();
        openShop(player, newPage);
    }

    public static void openShop(ServerPlayer player, int page) {
        player.closeContainer();
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new ShopMenu(containerId, playerInventory, (ServerPlayer) p, page),
                Component.literal("商店 - 第" + page + "页")
        ));
    }

    public static void openBuy(ServerPlayer player, int itemId) {
        player.closeContainer();
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new BuyMenu(containerId, playerInventory, (ServerPlayer) p, itemId),
                Component.literal("商品购买")
        ));
    }

    public static boolean afford(ServerPlayer player, ShopItem item, int count) {
        double price = count * item.price();
        if (Economy.withdraw(player, price)) {
            ItemStack giveItem = item.item().copy();
            giveItem.setCount(count);
            player.getInventory().add(giveItem);
            if (count == item.item().getCount()) ShopDB.removeItem(item.id());
            else {
                ItemStack itemStack = item.item().copy();
                itemStack.setCount(itemStack.getCount() - count);
                ShopDB.replaceItem(item.id(), itemStack);
            }
            player.sendSystemMessage(Component.literal("[商店系统] 购买成功！").withStyle(ChatFormatting.GREEN));
            if (player.getServer() == null) return false;
            Player seller = player.getServer().getPlayerList().getPlayer(item.sellerUuid());
            if (seller != null) {
                Economy.deposit(seller, price);
                seller.sendSystemMessage(Component.literal("§a你的商品 ").
                        append(item.item().getDisplayName().copy().withStyle(ChatFormatting.YELLOW).setStyle(
                                Style.EMPTY.withHoverEvent(
                                        new HoverEvent(
                                                HoverEvent.Action.SHOW_ITEM,
                                                new HoverEvent.ItemStackInfo(item.item())
                                        )
                                )
                        )).
                        append(" §a已售出！收入: §6" + price + " 椰块")
                );
            } else {
                EconomyDB.updateBalance(item.sellerUuid(), EconomyDB.getBalance(item.sellerUuid()) + price);
            }
            return true;
        }
        return false;
    }
}
