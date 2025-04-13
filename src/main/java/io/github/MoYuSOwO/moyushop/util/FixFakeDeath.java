package io.github.MoYuSOwO.moyushop.util;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class FixFakeDeath {

    @SubscribeEvent
    public static void setHealth(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("setHealth")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(1.0F))
                                .executes(
                                        ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            float amount = FloatArgumentType.getFloat(ctx, "amount");
                                            target.setHealth(amount);
                                            return 1;
                                        }
                                )
                        )
                )
        );
    }
}
