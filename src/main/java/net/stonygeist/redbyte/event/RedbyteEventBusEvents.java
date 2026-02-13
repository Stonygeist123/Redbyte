package net.stonygeist.redbyte.event;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.entity.robo.RoboModel;
import net.stonygeist.redbyte.index.RedbyteEntities;

@Mod.EventBusSubscriber(modid = Redbyte.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RedbyteEventBusEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RoboModel.LAYER_LOCATION, RoboModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(RedbyteEntities.ROBO.get(), RoboEntity.createAttributes().build());
    }
}
