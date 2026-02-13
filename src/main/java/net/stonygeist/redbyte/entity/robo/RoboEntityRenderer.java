package net.stonygeist.redbyte.entity.robo;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.stonygeist.redbyte.Redbyte;
import org.jetbrains.annotations.NotNull;

public class RoboEntityRenderer extends MobRenderer<RoboEntity, RoboModel<RoboEntity>> {
    private static final ResourceLocation TEXTURE = Redbyte.asResource("textures/entity/robo.png");

    public RoboEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new RoboModel<>(pContext.bakeLayer(RoboModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull RoboEntity pEntity) {
        return TEXTURE;
    }
}