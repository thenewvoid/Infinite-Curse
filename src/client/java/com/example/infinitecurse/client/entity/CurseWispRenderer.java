package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.CurseWisp;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** 咒丝游魂 渲染器（§新增）：复用几何模型，贴图用原版白图由模型自身着色为紫色。 */
public class CurseWispRenderer extends LivingEntityRenderer<CurseWisp, CurseWispModel> {

	public CurseWispRenderer(EntityRendererProvider.Context context) {
		super(context, new CurseWispModel(context.bakeLayer(CurseWispModel.LAYER)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(CurseWisp entity) {
		return new ResourceLocation("minecraft", "textures/misc/white.png");
	}
}
