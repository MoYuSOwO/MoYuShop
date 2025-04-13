package io.github.MoYuSOwO.moyushop.guishop;

import io.github.MoYuSOwO.moyushop.shop.ShopDB;
import io.github.MoYuSOwO.moyushop.shop.ShopItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BuyMenu extends AbstractContainerMenu {

    private final ShopItem item;
    private final ItemStackHandler itemHandler;
    private final ServerPlayer player;
    private int count;

    public BuyMenu(int containerId, Inventory playerInventory, ServerPlayer player, int itemId) {
        super(MenuType.GENERIC_9x5, containerId);
        this.item = ShopDB.getItem(itemId);
        this.itemHandler = new ItemStackHandler(45);
        this.player = player;
        this.count = 1;
        for(int row = 0; row < 5; ++row) {
            for(int col = 0; col < 9; ++col) {
                this.addSlot(new ShopItemHandler(
                        itemHandler,
                        col + row * 9,
                        8 + col * 18,
                        18 + row * 18
                ));
            }
        }
        for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        122 + row * 18
                ));
            }
        }
        for(int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    180
            ));
        }
        refreshPage();
    }

    private void refreshPage() {
        for (int i = 0; i < 45; i++) {
            if (i == 13) {
                itemHandler.insertItem(i, createDisplay(), false);
            } else if (i == 29) {
                itemHandler.insertItem(i, createMinusFive(), false);
            } else if (i == 30) {
                itemHandler.insertItem(i, createMinusOne(), false);
            } else if (i == 32) {
                itemHandler.insertItem(i, createAddOne(), false);
            } else if (i == 33) {
                itemHandler.insertItem(i, createAddFive(), false);
            } else if (i == 36) {
                itemHandler.insertItem(i, createBack(), false);
            } else if (i == 44) {
                itemHandler.insertItem(i, createAfford(), false);
            } else {
                itemHandler.insertItem(i, createLine(), false);
            }
        }
    }

    private void refreshCount() {
        itemHandler.setStackInSlot(13, createDisplay());
        itemHandler.setStackInSlot(44, createAfford());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    private static ItemStack createLine() {
        ItemStack stack = new ItemStack(Items.YELLOW_STAINED_GLASS_PANE, 1);
        stack.set(DataComponents.ITEM_NAME, Component.empty());
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        return stack;
    }
    private static ItemStack createAddOne() {
        ItemStack stack = new ItemStack(Items.ARROW, 1);
        stack.set(DataComponents.ITEM_NAME, Component.literal("增加一个").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        return stack;
    }
    private static ItemStack createMinusOne() {
        ItemStack stack = new ItemStack(Items.ARROW, 1);
        stack.set(DataComponents.ITEM_NAME, Component.literal("减少一个").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        return stack;
    }
    private static ItemStack createAddFive() {
        ItemStack stack = new ItemStack(Items.SPECTRAL_ARROW, 1);
        stack.set(DataComponents.ITEM_NAME, Component.literal("增加五个").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        return stack;
    }
    private static ItemStack createMinusFive() {
        ItemStack stack = new ItemStack(Items.SPECTRAL_ARROW, 1);
        stack.set(DataComponents.ITEM_NAME, Component.literal("减少五个").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        return stack;
    }
    private static ItemStack createBack() {
        ItemStack stack = new ItemStack(Items.BARRIER, 1);
        stack.set(DataComponents.ITEM_NAME, Component.literal("退出").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        return stack;
    }
    private ItemStack createAfford() {
        ItemStack stack = new ItemStack(Items.GOLD_INGOT, 1);
        List<Component> loreLines = new ArrayList<>();
        loreLines.add(
                Component.empty().append(
                        Component.literal("总价")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withItalic(false))
                ).append(
                        Component.literal(": ")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false))
                ).append(
                        Component.literal(item.price() * count + " 椰块")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                )
        );
        ItemLore newitemLore = new ItemLore(loreLines);
        stack.set(DataComponents.ITEM_NAME, Component.literal("购买").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
        stack.set(DataComponents.LORE, newitemLore);
        return stack;
    }
    private ItemStack createDisplay() {
        ItemStack itemStack = item.item().copy();
        ItemLore itemLore = itemStack.get(DataComponents.LORE);
        List<Component> loreLines = new ArrayList<>();
        if (itemLore != null) {
            loreLines.addAll(itemLore.lines());
        }
        loreLines.add(
                Component.empty().append(
                        Component.literal("单价")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withItalic(false))
                ).append(
                        Component.literal(": ")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false))
                ).append(
                        Component.literal(item.price() + " 椰块")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                )
        );
        ItemLore newitemLore = new ItemLore(loreLines);
        itemStack.set(DataComponents.LORE, newitemLore);
        itemStack.setCount(count);
        return itemStack;
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId == 29) {
            count -= 5;
            if (count < 1) count = 1;
            refreshCount();
        } else if (slotId == 30) {
            count--;
            if (count < 1) count = 1;
            refreshCount();
        } else if (slotId == 32) {
            count++;
            if (count > item.item().getCount()) count = item.item().getCount();
            refreshCount();
        } else if (slotId == 33) {
            count += 5;
            if (count > item.item().getCount()) count = item.item().getCount();
            refreshCount();
        } else if (slotId == 36) {
            ShopCommands.openShop((ServerPlayer) player);
        } else if (slotId == 44) {
            if (ShopCommands.afford((ServerPlayer) player, item, count)) {
                player.closeContainer();
            } else {
                player.closeContainer();
                player.sendSystemMessage(Component.literal("[商店系统] 购买成功！").withStyle(ChatFormatting.RED));
            }
        }
        else {
            super.clicked(slotId, button, clickType, player);
        }
    }
}
