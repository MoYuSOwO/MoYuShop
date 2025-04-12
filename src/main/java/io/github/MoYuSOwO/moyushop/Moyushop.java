package io.github.MoYuSOwO.moyushop;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.logging.LogUtils;
import io.github.MoYuSOwO.moyushop.economy.Economy;
import io.github.MoYuSOwO.moyushop.economy.EconomyDB;
import io.github.MoYuSOwO.moyushop.shop.ShopCommands;
import io.github.MoYuSOwO.moyushop.shop.ShopDB;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.nio.file.Path;

@Mod(Moyushop.MODID)
public class Moyushop {
    public static final String MODID = "moyushop";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Moyushop(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(Moyushop.class);
        NeoForge.EVENT_BUS.register(Economy.class);
        NeoForge.EVENT_BUS.register(Util.class);
        NeoForge.EVENT_BUS.register(ShopCommands.class);
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("欢迎使用 MoYuShop Mod!");
        Path dataDir = event.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data").normalize();
        EconomyDB.init(dataDir);
        ShopDB.init(dataDir);
        LOGGER.info("数据库初始化成功！");
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 400 == 0) {
            ShopDB.cleanExpiredTokens();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        EconomyDB.shutdown();
        ShopDB.shutdown();
        LOGGER.info("ByeBye OwO!");
    }
}
