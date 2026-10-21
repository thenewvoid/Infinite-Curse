package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.RotLurker;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** 腐咒潜伏者 渲染器（§新增）。 */
public class RotLurkerRenderer extends LivingEntityRenderer<RotLurker, RotLurkerModel> {

	public RotLurkerRenderer(EntityRendererProvider.Context context) {
		super(context, new RotLurkerModel(context.bakeLayer(RotLurkerModel.LAYER)), 0.6F);
	}

	@Override
	public ResourceLocation getTextureLocation(RotLurker entity) {
		return new ResourceLocation("minecraft", "textures/misc/white.png");
	}
}
