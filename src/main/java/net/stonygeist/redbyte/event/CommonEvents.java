package net.stonygeist.redbyte.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonygeist.redbyte.manager.RoboRegistry;

@Mod.EventBusSubscriber
public enum CommonEvents {
    ;

    @SubscribeEvent
    public static void onServerWorldTick(TickEvent.LevelTickEvent.Post event) {
        Level level = event.level;
        if (level instanceof ServerLevel serverLevel)
            RoboRegistry.get(serverLevel).tick(serverLevel);
    }
}
