package net.stonygeist.redbyte.index;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.stonygeist.redbyte.Redbyte;

public class RedbyteCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Redbyte.MOD_ID);
    public static final RegistryObject<CreativeModeTab> REDBYTE_TAB = CREATIVE_TABS.register("redbyte", () -> CreativeModeTab.builder()
            .title(Component.literal("Redbyte"))
            .icon(() -> new ItemStack(RedbyteItems.ROBO_SPAWNER.get()))
            .displayItems((params, output) -> RedbyteItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
