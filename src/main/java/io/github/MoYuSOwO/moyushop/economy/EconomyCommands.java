package io.github.MoYuSOwO.moyushop.economy;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class EconomyCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("balance")
                .executes(
                        ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayer();
                            if (player != null) {
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("[经济系统] 你的余额: " + Economy.getBalance(player) + " 椰块"),
                                        false
                                );
                                return 1;
                            }
                            return 0;
                        }
                )
        );
        event.getDispatcher().register(Commands.literal("balance")
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(
                                                ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                                    double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                                    Economy.setBalance(target, amount);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal("[经济系统] 已设置玩家 " + target.getScoreboardName() + " 的余额为: " + amount + " 椰块"),
                                                            false
                                                    );
                                                    return 1;
                                                }
                                        )
                                )
                        )
                )
        );
        event.getDispatcher().register(Commands.literal("balance")
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                .executes(
                                        ctx -> {
                                            ServerPlayer target = ctx.getSource().getPlayer();
                                            if (target == null) return 0;
                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                            Economy.setBalance(target, amount);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("[经济系统] 已设置玩家 " + target.getScoreboardName() + " 的余额为: " + amount + " 椰块"),
                                                    false
                                            );
                                            return 1;
                                        }
                                )
                        )
                )
        );
        event.getDispatcher().register(Commands.literal("pay")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                .executes(
                                        ctx -> {
                                            ServerPlayer from = ctx.getSource().getPlayer();
                                            ServerPlayer to = EntityArgument.getPlayer(ctx, "target");
                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                            if (Economy.transfer(from, to, amount)) {
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.literal("[经济系统] 成功转账 %.2f 椰块给 %s".formatted(amount, to.getScoreboardName())),
                                                        false
                                                );
                                            } else {
                                                ctx.getSource().sendFailure(Component.literal("[经济系统] 转账失败！"));
                                            }
                                            return 1;
                                        }
                                )
                        )
                )
        );
    }
}
