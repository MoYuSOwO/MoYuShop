package io.github.MoYuSOwO.moyushop.util;

import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

public class AntiMobGriefing {
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getDirectSourceEntity() != null) {
            var directSource = event.getExplosion().getDirectSourceEntity();
            var indirectSource = event.getExplosion().getIndirectSourceEntity();
            if (directSource instanceof Creeper || directSource instanceof Ghast || indirectSource instanceof Creeper || indirectSource instanceof Ghast) {
                event.getAffectedBlocks().clear();
            }
        }
    }

    @SubscribeEvent
    public static void onMobGriefing(EntityMobGriefingEvent event) {
        if (event.getEntity() instanceof EnderMan) {
            event.setCanGrief(false);
        }
    }
}
