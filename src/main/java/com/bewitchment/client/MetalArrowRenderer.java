package com.bewitchment.client;

import com.bewitchment.entity.MetalArrowEntity;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;

public final class MetalArrowRenderer extends ArrowRenderer<MetalArrowEntity> {
    private static final ResourceLocation SILVER = new ResourceLocation("bewitchment", "textures/entity/silver_arrow.png");
    private static final ResourceLocation COLD_IRON = new ResourceLocation("bewitchment", "textures/entity/cold_iron_arrow.png");
    public MetalArrowRenderer(EntityRendererManager manager) { super(manager); }
    @Override public ResourceLocation getTextureLocation(MetalArrowEntity arrow) { return arrow.isColdIron() ? COLD_IRON : SILVER; }
}
