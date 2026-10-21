package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.SourceDrowned;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** 源咒溺灵 渲染器（§新增）。 */
public class SourceDrownedRenderer extends LivingEntityRenderer<SourceDrowned, SourceDrownedModel> {

	public SourceDrownedRenderer(EntityRendererProvider.Context context) {
		super(context, new SourceDrownedModel(context.bakeLayer(SourceDrownedModel.LAYER)), 0.6F);
	}

	@Override
	public ResourceLocation getTextureLocation(SourceDrowned entity) {
		return new ResourceLocation("minecraft", "textures/misc/white.png");
	}
}
