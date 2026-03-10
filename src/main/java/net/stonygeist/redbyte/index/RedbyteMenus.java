package net.stonygeist.redbyte.index;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.menu.robo_docs.RoboDocs;
import net.stonygeist.redbyte.menu.robo_inventory.RoboInventory;
import net.stonygeist.redbyte.menu.robo_terminal.RoboTerminal;

public enum RedbyteMenus {
    ;
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Redbyte.MOD_ID);
    public static final RegistryObject<MenuType<RoboTerminal>> ROBO_TERMINAL = MENU_TYPES.register("robo_terminal_menu", () -> IForgeMenuType.create(RoboTerminal::new));
    public static final RegistryObject<MenuType<RoboInventory>> ROBO_INVENTORY = MENU_TYPES.register("robo_inventory_menu", () -> IForgeMenuType.create(RoboInventory::new));
    public static final RegistryObject<MenuType<RoboDocs>> ROBO_DOCS = MENU_TYPES.register("robo_docs_menu", () -> IForgeMenuType.create(RoboDocs::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
