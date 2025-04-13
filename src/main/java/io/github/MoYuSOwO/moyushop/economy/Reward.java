package io.github.MoYuSOwO.moyushop.economy;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.concurrent.ThreadLocalRandom;

public class Reward {

    private static double coinPrice = 5;
    private static final ResourceLocation itemId = ResourceLocation.parse("libraryferret:gold_coins_jtl");
    private static final Item targetItem = BuiltInRegistries.ITEM.get(itemId);

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) return;
        BlockState blockState = event.getState();
        BlockPos pos = event.getPos();
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        if (player.gameMode.isCreative()) return;
        ItemStack coin = new ItemStack(targetItem, 1);
        if (blockState.is(BlockTags.CROPS)) {
            dropAccordingToRatio(player, pos, 0.01, coin);
        }
        else if (blockState.is(BlockTags.COAL_ORES)) {
            dropAccordingToRatio(player, pos, 0.05, coin);
        }
        else if (blockState.is(BlockTags.COPPER_ORES)) {
            dropAccordingToRatio(player, pos, 0.05, coin);
        }
        else if (blockState.is(BlockTags.IRON_ORES)) {
            dropAccordingToRatio(player, pos, 0.15, coin);
        }
        else if (blockState.is(BlockTags.REDSTONE_ORES)) {
            dropAccordingToRatio(player, pos, 0.20, coin);
        }
        else if (blockState.is(BlockTags.GOLD_ORES)) {
            dropAccordingToRatio(player, pos, 0.25, coin);
        }
        else if (blockState.is(BlockTags.DIAMOND_ORES)) {
            dropAccordingToRatio(player, pos, 0.50, coin);
        }
        else if (blockState.is(BlockTags.LAPIS_ORES)) {
            dropAccordingToRatio(player, pos, 0.20, coin);
        }
        else if (blockState.is(BlockTags.EMERALD_ORES)) {
            dropAccordingToRatio(player, pos, 0.40, coin);
        }
        else if (blockState.is(Blocks.ANCIENT_DEBRIS)) {
            dropAccordingToRatio(player, pos, 0.75, coin);
        }
    }

    private static void dropAccordingToRatio(Player player, BlockPos pos, double ratio, ItemStack item) {
        if (ThreadLocalRandom.current().nextDouble() < ratio) {
            player.level().addFreshEntity(
                    new ItemEntity(
                            player.level(),
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            item
                    )
            );
        }
    }

    @SubscribeEvent
    public static void sellCoin(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("sellcoin")
                .executes(
                        ctx -> {
                            ItemStack coin = new ItemStack(targetItem, 1);
                            ServerPlayer target = ctx.getSource().getPlayer();
                            if (target == null) return 0;
                            if (target.getInventory().contains(coin)) {
                                Economy.deposit(target, coinPrice);
                                removeItems(target.getInventory(), targetItem, 1);
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("[经济系统] 成功卖出了一个金币！"),
                                        false
                                );
                                return 1;
                            } else {
                                ctx.getSource().sendFailure(
                                        Component.literal("[经济系统] 你没有金币可以卖！")
                                );
                                return 0;
                            }
                        }
                )
                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                        .executes(
                                ctx -> {
                                    int amount = IntegerArgumentType.getInteger(ctx, "count");
                                    ItemStack coin = new ItemStack(targetItem, amount);
                                    ServerPlayer target = ctx.getSource().getPlayer();
                                    if (target == null) return 0;
                                    if (target.getInventory().contains(coin)) {
                                        int realAmount = countItems(target.getInventory(), targetItem);
                                        if (realAmount >= amount) {
                                            Economy.deposit(target, coinPrice * (double) amount);
                                            removeItems(target.getInventory(), targetItem, amount);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("[经济系统] 成功卖出了 " + amount + " 个金币！"),
                                                    false
                                            );
                                            return 1;
                                        } else {
                                            ctx.getSource().sendFailure(
                                                    Component.literal("[经济系统] 你没有那么多金币可以卖！")
                                            );
                                            return 0;
                                        }
                                    } else {
                                        ctx.getSource().sendFailure(
                                                Component.literal("[经济系统] 你没有那么多金币可以卖！")
                                        );
                                        return 0;
                                    }
                                }
                        )
                )
        );
        event.getDispatcher().register(Commands.literal("setCoinPrice")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("price", DoubleArgumentType.doubleArg(1.0))
                        .executes(
                                ctx -> {
                                    coinPrice = DoubleArgumentType.getDouble(ctx, "price");
                                    return 1;
                                }
                        )
                )
        );
    }

    private static int countItems(Inventory inv, Item item) {
        int total = 0;
        for(int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == item) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void removeItems(Inventory inv, Item item, int amount) {
        for(int i = 0; i < inv.getContainerSize(); i++) {
            if (amount == 0) return;
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == item) {
                if (stack.getCount() >= amount) {
                    stack.shrink(amount);
                } else {
                    amount -= stack.getCount();
                    stack.setCount(0);
                }
            }
        }
    }
}
