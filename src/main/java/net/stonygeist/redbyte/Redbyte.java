package net.stonygeist.redbyte;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import net.stonygeist.redbyte.entity.robo.RoboEntityRenderer;
import net.stonygeist.redbyte.index.RedbyteCreativeTabs;
import net.stonygeist.redbyte.index.RedbyteEntities;
import net.stonygeist.redbyte.index.RedbyteItems;
import net.stonygeist.redbyte.server.C2SBuildCodePacket;
import net.stonygeist.redbyte.server.C2SDiagnosticsPacket;
import net.stonygeist.redbyte.server.C2SEvaluateCodePacket;
import net.stonygeist.redbyte.server.C2SStoreRoboCodePacket;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Redbyte.MOD_ID)
public class Redbyte {
    public static final String MOD_ID = "redbyte";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = ChannelBuilder.named(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main")).simpleChannel();

    public Redbyte() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        RedbyteCreativeTabs.register(bus);
        RedbyteItems.register(bus);
        RedbyteEntities.register(bus);

        bus.addListener(this::addCreative);

        int networkID = 0;
        CHANNEL.messageBuilder(C2SStoreRoboCodePacket.class, networkID++)
                .encoder(C2SStoreRoboCodePacket::encode)
                .decoder(C2SStoreRoboCodePacket::decode)
                .consumerMainThread(C2SStoreRoboCodePacket::handle)
                .add();
        CHANNEL.messageBuilder(C2SEvaluateCodePacket.class, networkID++)
                .encoder(C2SEvaluateCodePacket::encode)
                .decoder(C2SEvaluateCodePacket::decode)
                .consumerMainThread(C2SEvaluateCodePacket::handle)
                .add();
        CHANNEL.messageBuilder(C2SBuildCodePacket.class, networkID++)
                .encoder(C2SBuildCodePacket::encode)
                .decoder(C2SBuildCodePacket::decode)
                .consumerMainThread(C2SBuildCodePacket::handle)
                .add();
        CHANNEL.messageBuilder(C2SDiagnosticsPacket.class, networkID++)
                .encoder(C2SDiagnosticsPacket::encode)
                .decoder(C2SDiagnosticsPacket::decode)
                .consumerMainThread(C2SDiagnosticsPacket::handle)
                .add();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == RedbyteCreativeTabs.REDBYTE_TAB.get())
            event.accept(RedbyteItems.ROBO_SPAWNER);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(RedbyteEntities.ROBO.get(), RoboEntityRenderer::new);
        }
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
