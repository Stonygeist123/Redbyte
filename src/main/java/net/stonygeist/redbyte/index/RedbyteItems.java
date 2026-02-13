package net.stonygeist.redbyte.index;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.item.RoboSpawner;

public class RedbyteItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Redbyte.MOD_ID);
    public static final RegistryObject<RoboSpawner> ROBO_SPAWNER = ITEMS.register("robo_spawner", () -> new RoboSpawner(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
