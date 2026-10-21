package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.Archcurse;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** 万咒之主 渲染器（§新增）。 */
public class ArchcurseRenderer extends LivingEntityRenderer<Archcurse, ArchcurseModel> {

	public ArchcurseRenderer(EntityRendererProvider.Context context) {
		super(context, new ArchcurseModel(context.bakeLayer(ArchcurseModel.LAYER)), 1.2F);
	}

	@Override
	public ResourceLocation getTextureLocation(Archcurse entity) {
		return new ResourceLocation("minecraft", "textures/misc/white.png");
	}
}
