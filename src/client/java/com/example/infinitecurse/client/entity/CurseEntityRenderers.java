package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.Archcurse;
import com.example.infinitecurse.entity.BlindEye;
import com.example.infinitecurse.entity.CurseWisp;
import com.example.infinitecurse.entity.RotLurker;
import com.example.infinitecurse.entity.SourceDrowned;
import com.example.infinitecurse.entity.StarvingGhoul;
import com.example.infinitecurse.entity.StoneboundGolem;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * 诅咒系列怪物的客户端注册（§新增）：登记 4 个几何模型层与对应的实体渲染器。
 * 由 InfiniteCurseClient.onInitializeClient 调用。
 */
public final class CurseEntityRenderers {

	private CurseEntityRenderers() {
	}

	public static void register() {
		EntityModelLayerRegistry.registerModelLayer(CurseWispModel.LAYER, CurseWispModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(RotLurkerModel.LAYER, RotLurkerModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(StoneboundGolemModel.LAYER, StoneboundGolemModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(SourceDrownedModel.LAYER, SourceDrownedModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(BlindEyeModel.LAYER, BlindEyeModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(StarvingGhoulModel.LAYER, StarvingGhoulModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ArchcurseModel.LAYER, ArchcurseModel::createBodyLayer);

		EntityRendererRegistry.register(CurseWisp.TYPE, CurseWispRenderer::new);
		EntityRendererRegistry.register(RotLurker.TYPE, RotLurkerRenderer::new);
		EntityRendererRegistry.register(StoneboundGolem.TYPE, StoneboundGolemRenderer::new);
		EntityRendererRegistry.register(SourceDrowned.TYPE, SourceDrownedRenderer::new);
		EntityRendererRegistry.register(BlindEye.TYPE, BlindEyeRenderer::new);
		EntityRendererRegistry.register(StarvingGhoul.TYPE, StarvingGhoulRenderer::new);
		EntityRendererRegistry.register(Archcurse.TYPE, ArchcurseRenderer::new);
	}
}
