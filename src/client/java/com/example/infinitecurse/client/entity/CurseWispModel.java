package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.CurseWisp;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.ResourceLocation;

/**
 * 咒丝游魂 几何模型（§新增）：瘦高、悬浮、双臂飘散的幽魂轮廓。纯立方体几何 + 紫色调色，复用原版白图。
 */
public class CurseWispModel extends EntityModel<CurseWisp> {

	public static final ModelLayerLocation LAYER = new ModelLayerLocation(
			new ResourceLocation("infinite_curse", "curse_wisp"), "main");

	private final ModelPart root;

	public CurseWispModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition md = new MeshDefinition();
		PartDefinition root = md.getRoot();
		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 12.0F, 6.0F),
				PartPose.offset(0.0F, 12.0F, 0.0F));
		root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F),
				PartPose.offset(0.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("arm_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 8.0F, 3.0F),
				PartPose.offset(-4.5F, 6.0F, 0.0F));
		root.addOrReplaceChild("arm_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 8.0F, 3.0F),
				PartPose.offset(4.5F, 6.0F, 0.0F));
		return LayerDefinition.create(md, 32, 32);
	}

	@Override
	public void setupAnim(CurseWisp entity, float limbSwing, float limbSwingAmount, float ageInTicks,
						  float netHeadYaw, float headPitch) {
		this.root.y = 12.0F + (float) Math.sin(ageInTicks * 0.1F) * 0.6F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
							   int packedOverlay, float red, float green, float blue, float alpha) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0.62F, 0.30F, 0.92F, 1.0F);
	}
}
