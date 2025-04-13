package io.github.MoYuSOwO.moyushop.shop;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.MoYuSOwO.moyushop.economy.Economy;
import io.github.MoYuSOwO.moyushop.economy.EconomyDB;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.UUID;

public class ShopCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("sell")
                .then(Commands.argument("price", DoubleArgumentType.doubleArg(1))
                        .executes(
                                ctx -> {
                                    Player player = ctx.getSource().getPlayer();
                                    if (player == null) return 0;
                                    double price = DoubleArgumentType.getDouble(ctx, "price");
                                    ItemStack heldItem = player.getMainHandItem();
                                    if (heldItem.isEmpty()) {
                                        ctx.getSource().sendFailure(Component.literal("[商店系统] 你不能上架空气！"));
                                        return 0;
                                    }
                                    ShopDB.addItem(player.getUUID(), heldItem.copy(), price);
                                    heldItem.setCount(0);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("[商店系统] 成功上架物品！价格: " + price + " 椰块"),
                                            false
                                    );
                                    return 1;
                                }
                        )
                )
        );
        event.getDispatcher().register(Commands.literal("shop")
                .executes(ctx -> {
                    showShopPage(ctx.getSource(), 1);
                    return 1;
                })
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int page = IntegerArgumentType.getInteger(ctx, "page");
                            showShopPage(ctx.getSource(), page);
                            return 1;
                        })
                )
        );
        event.getDispatcher().register(Commands.literal("buy")
                .then(Commands.argument("token", StringArgumentType.string())
                        .executes(ctx -> {
                            if (!(ctx.getSource().getEntity() instanceof Player)) {
                                ctx.getSource().sendFailure(Component.literal("[商店系统] 只有玩家可以执行"));
                                return 0;
                            }
                            UUID token;
                            try {
                                token = UUID.fromString(StringArgumentType.getString(ctx, "token"));
                            } catch (IllegalArgumentException e) {
                                ctx.getSource().sendFailure(Component.literal(("[商店系统] 无效的购买令牌")));
                                return 0;
                            }
                            ShopDB.IdWithTime result = ShopDB.consumePurchaseToken(token);
                            if (result == null) {
                                ctx.getSource().sendFailure(Component.literal("[商店系统] 你打开商店之后太久没买东西啦！重新打开吧！"));
                                return 0;
                            }
                            if (buy(ctx.getSource(), result.id())) return 1;
                            else return 0;
                        })
                )
        );
    }

    private static boolean buy(CommandSourceStack source, int id) {
        Player player = source.getPlayer();
        ShopItem shopItem = ShopDB.getItem(id);
        if (player == null) return false;
        if (shopItem == null) {
            source.sendFailure(Component.literal("[商店系统] id 不存在！"));
            return false;
        }
        if (Economy.withdraw(player, shopItem.price())) {
            player.getInventory().add(shopItem.item().copy());
            ShopDB.removeItem(id);
            source.sendSuccess(
                    () -> Component.literal("[商店系统] 购买成功！"),
                    false
            );
            if (player.getServer() == null) return false;
            Player seller = player.getServer().getPlayerList().getPlayer(shopItem.sellerUuid());
            if (seller != null) {
                Economy.deposit(seller, shopItem.price());
                seller.sendSystemMessage(Component.literal("§a你的商品 ").
                        append(shopItem.item().getDisplayName().copy().withStyle(ChatFormatting.YELLOW).setStyle(
                                Style.EMPTY.withHoverEvent(
                                        new HoverEvent(
                                                HoverEvent.Action.SHOW_ITEM,
                                                new HoverEvent.ItemStackInfo(shopItem.item())
                                        )
                                )
                        )).
                        append(" §a已售出！收入: §6" + shopItem.price() + " 椰块")
                );
            } else {
                EconomyDB.updateBalance(shopItem.sellerUuid(), EconomyDB.getBalance(shopItem.sellerUuid()) + shopItem.price());
            }
            return true;
        } else {
            source.sendFailure(Component.literal("[商店系统] 椰块余额不足！"));
            return false;
        }
    }

    private static void showShopPage(CommandSourceStack source, int page) {
        List<ShopItem> items = ShopDB.getAllItems();
        int itemsPerPage = 5;
        int maxPage = Math.max(1, (int) Math.ceil(items.size() / (double) itemsPerPage));
        if (page < 1 || page > maxPage) {
            source.sendFailure(Component.literal("§c无效页码 (可用范围: 1-" + maxPage + ")"));
            return;
        }
        source.sendSystemMessage(Component.literal(
                "§6===" + " 商店 §7(第 " + page + "/" + maxPage + " 页) " + "§6==="
        ));
        items.stream()
                .skip((long) (page - 1) * itemsPerPage)
                .limit(itemsPerPage)
                .forEach(
                    item -> {
                        String token = ShopDB.generatePurchaseToken(item.id());
                        MutableComponent buyClick = Component.literal(" [点击购买]").setStyle(
                                Style.EMPTY.withClickEvent(
                                        new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/buy " + token
                                        )
                                ).withHoverEvent(
                                        new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("买买买！")
                                        )
                                ).withColor(ChatFormatting.GREEN)
                        );
                        MutableComponent show = Component.literal("§7- [§b" + items.indexOf(item) + "§7] ")
                                .append(item.item().getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
                                .append(" §7x§b" + item.item().getCount() + " §7| 价格: §6" + item.price() + " 椰块")
                                        .setStyle(
                                                Style.EMPTY.withHoverEvent(
                                                        new HoverEvent(
                                                                HoverEvent.Action.SHOW_ITEM,
                                                                new HoverEvent.ItemStackInfo(item.item())
                                                        )
                                                )
                                        );
                        source.sendSystemMessage(show.append(buyClick));
                    }
                );
        if (maxPage > 1) {
            MutableComponent navigation = Component.literal("§7导航: ");
            if (page > 1) {
                navigation.append(
                        Component.literal("§a<上一页")
                                .setStyle(Style.EMPTY
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/shop " + (page - 1)
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("跳转到第 " + (page - 1) + "页")
                                        ))
                                )
                );
                navigation.append(Component.literal(" "));
            }
            navigation.append(Component.literal("§e" + page + "/" + maxPage));
            if (page < maxPage) {
                navigation.append(Component.literal(" "));
                navigation.append(
                        Component.literal("§a下一页>")
                                .setStyle(Style.EMPTY
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/shop " + (page + 1)
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("跳转到第 " + (page + 1) + "页")
                                        ))
                                )
                );
            }
            source.sendSystemMessage(navigation);
        }
    }
}
