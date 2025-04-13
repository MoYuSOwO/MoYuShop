package io.github.MoYuSOwO.moyushop.guishop;

import io.github.MoYuSOwO.moyushop.shop.ShopDB;
import io.github.MoYuSOwO.moyushop.shop.ShopItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ShopMenu extends AbstractContainerMenu {

    private static final int itemsPerPage = 28;
    private final List<ShopItem> items;
    private final int page;
    private final int maxPage;
    private final IItemHandler itemHandler;

    public ShopMenu(int containerId, Inventory playerInventory) { // 如果需要从服务器读取数据，可选FriendlyByteBuf参数
        this(containerId, playerInventory, 1);
    }

    public ShopMenu(int containerId, Inventory playerInventory, int page) {
        super(MenuType.GENERIC_9x6, containerId);
        this.page = page;
        this.items = ShopDB.getAllItems();
        this.itemHandler = new ItemStackHandler(54);
        this.maxPage = Math.max(1, (int) Math.ceil(items.size() / (double) itemsPerPage));
        for(int row = 0; row < 6; ++row) {
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
                        140 + row * 18
                ));
            }
        }
        for(int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    198
            ));
        }
        refreshPage();
    }

    private void refreshPage() {
        for (int i = 0; i < 9; i++) {
            itemHandler.insertItem(i, createLine(), false);
            if (i == 2 && page > 1) {
                itemHandler.insertItem(45 + i, createLastPage(), false);
            } else if (i == 6 && page < maxPage) {
                itemHandler.insertItem(45 + i, createNextPage(), false);
            } else {
                itemHandler.insertItem(45 + i, createLine(), false);
            }
        }
        for (int i = 1; i < 5; i++) {
            itemHandler.insertItem(i * 9, createLine(), false);
            itemHandler.insertItem(i * 9 + 8, createLine(), false);
        }
        int itemId = (page - 1) * itemsPerPage;
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                if (itemId >= items.size()) return;
                ItemStack item = items.get(itemId).item().copy();
                ItemLore itemLore = item.get(DataComponents.LORE);
                List<Component> loreLines = new ArrayList<>();
                if (itemLore != null) {
                    loreLines.addAll(itemLore.lines());
                }
                loreLines.add(
                        Component.empty().append(
                                        Component.literal("价格")
                                                .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withItalic(false))
                                ).append(
                                        Component.literal(": ")
                                                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false))
                                ).append(
                                        Component.literal(items.get(itemId).price() + " 椰块")
                                                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                                )
                );
                loreLines.add(
                        Component.literal("鼠标左键进入购买页面")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(false))
                );
                ItemLore newitemLore = new ItemLore(loreLines);
                item.set(DataComponents.LORE, newitemLore);
                itemHandler.insertItem(row * 9 + col, item, false);
                itemId++;
            }
        }
    }

    private static ItemStack createLine() {
        ItemStack stack = new ItemStack(Items.YELLOW_STAINED_GLASS_PANE, 1);
        stack.set(DataComponents.ITEM_NAME, Component.empty());
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        return stack;
    }
    private static ItemStack createNextPage() {
        ItemStack stack = new ItemStack(Items.ARROW, 1);
        stack.set(DataComponents.ITEM_NAME, Component.literal("下一页"));
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        return stack;
    }
    private static ItemStack createLastPage() {
        ItemStack stack = new ItemStack(Items.ARROW, 1);
        stack.set(DataComponents.ITEM_NAME, Component.literal("上一页"));
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        return stack;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId == 47 && page > 1) {
            ShopCommands.replaceShop((ServerPlayer) player, page - 1);
        } else if (slotId == 51 && page < maxPage) {
            ShopCommands.replaceShop((ServerPlayer) player, page + 1);
        } else {
            super.clicked(slotId, button, clickType, player);
        }
    }
}
