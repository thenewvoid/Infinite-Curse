package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.StoneboundGolem;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** 岩咒傀儡 渲染器（§新增）。 */
public class StoneboundGolemRenderer extends LivingEntityRenderer<StoneboundGolem, StoneboundGolemModel> {

	public StoneboundGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new StoneboundGolemModel(context.bakeLayer(StoneboundGolemModel.LAYER)), 0.9F);
	}

	@Override
	public ResourceLocation getTextureLocation(StoneboundGolem entity) {
		return new ResourceLocation("minecraft", "textures/misc/white.png");
	}
}
