package io.github.MoYuSOwO.moyushop.shop;

import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record ShopItem(
        int id,
        UUID sellerUuid,
        ItemStack item,
        double price
) {
}
