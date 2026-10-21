package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.StarvingGhoul;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** 饥馑噬魂 渲染器（§新增）。 */
public class StarvingGhoulRenderer extends LivingEntityRenderer<StarvingGhoul, StarvingGhoulModel> {

	public StarvingGhoulRenderer(EntityRendererProvider.Context context) {
		super(context, new StarvingGhoulModel(context.bakeLayer(StarvingGhoulModel.LAYER)), 0.55F);
	}

	@Override
	public ResourceLocation getTextureLocation(StarvingGhoul entity) {
		return new ResourceLocation("minecraft", "textures/misc/white.png");
	}
}
