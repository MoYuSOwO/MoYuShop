package io.github.MoYuSOwO.moyushop.guishop;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
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
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new ShopMenu(containerId, playerInventory, page),
                Component.literal("商店 - 第" + page + "页")
        ));
    }
}
