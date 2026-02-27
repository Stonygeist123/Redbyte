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
import net.stonygeist.redbyte.server.C2SFunctionsPaket;
import net.stonygeist.redbyte.server.C2SRoboCodePacket;
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
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        RedbyteCreativeTabs.register(modEventBus);
        RedbyteItems.register(modEventBus);
        RedbyteEntities.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        int networkID = 0;
        CHANNEL.messageBuilder(C2SRoboCodePacket.class, networkID++)
                .encoder(C2SRoboCodePacket::encode)
                .decoder(C2SRoboCodePacket::decode)
                .consumerMainThread(C2SRoboCodePacket::handle)
                .add();
        CHANNEL.messageBuilder(C2SFunctionsPaket.WalkFunction.class, networkID++)
                .encoder(C2SFunctionsPaket.WalkFunction::encode)
                .decoder(C2SFunctionsPaket.WalkFunction::decode)
                .consumerMainThread(C2SFunctionsPaket.WalkFunction::handle)
                .add();
        CHANNEL.messageBuilder(C2SFunctionsPaket.WalkToFunction.class, networkID++)
                .encoder(C2SFunctionsPaket.WalkToFunction::encode)
                .decoder(C2SFunctionsPaket.WalkToFunction::decode)
                .consumerMainThread(C2SFunctionsPaket.WalkToFunction::handle)
                .add();
        CHANNEL.messageBuilder(C2SFunctionsPaket.JumpFunction.class, networkID++)
                .encoder(C2SFunctionsPaket.JumpFunction::encode)
                .decoder(C2SFunctionsPaket.JumpFunction::decode)
                .consumerMainThread(C2SFunctionsPaket.JumpFunction::handle)
                .add();
        CHANNEL.messageBuilder(C2SFunctionsPaket.FollowFunction.class, networkID++)
                .encoder(C2SFunctionsPaket.FollowFunction::encode)
                .decoder(C2SFunctionsPaket.FollowFunction::decode)
                .consumerMainThread(C2SFunctionsPaket.FollowFunction::handle)
                .add();
        CHANNEL.messageBuilder(C2SFunctionsPaket.StopFollowFunction.class, networkID++)
                .encoder(C2SFunctionsPaket.StopFollowFunction::encode)
                .decoder(C2SFunctionsPaket.StopFollowFunction::decode)
                .consumerMainThread(C2SFunctionsPaket.StopFollowFunction::handle)
                .add();
        CHANNEL.messageBuilder(C2SFunctionsPaket.AttackFunction.class, networkID++)
                .encoder(C2SFunctionsPaket.AttackFunction::encode)
                .decoder(C2SFunctionsPaket.AttackFunction::decode)
                .consumerMainThread(C2SFunctionsPaket.AttackFunction::handle)
                .add();
    }

    // Add the example block item to the building blocks tab
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
