package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.BlindEye;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** 瞽咒魔眼 渲染器（§新增）。 */
public class BlindEyeRenderer extends LivingEntityRenderer<BlindEye, BlindEyeModel> {

	public BlindEyeRenderer(EntityRendererProvider.Context context) {
		super(context, new BlindEyeModel(context.bakeLayer(BlindEyeModel.LAYER)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(BlindEye entity) {
		return new ResourceLocation("minecraft", "textures/misc/white.png");
	}
}
