package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.BlindEye;
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
 * 瞽咒魔眼 几何模型（§新增）：悬浮的眼瞳轮廓——核心眼体 + 前凸瞳孔 + 两侧羽翼。
 * 纯立方体几何 + 淡紫色调色，复用原版白图。
 */
public class BlindEyeModel extends EntityModel<BlindEye> {

	public static final ModelLayerLocation LAYER = new ModelLayerLocation(
			new ResourceLocation("infinite_curse", "blind_eye"), "main");

	private final ModelPart root;

	public BlindEyeModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition md = new MeshDefinition();
		PartDefinition root = md.getRoot();
		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
				PartPose.offset(0.0F, 14.0F, 0.0F));
		root.addOrReplaceChild("pupil",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
				PartPose.offset(0.0F, 14.0F, 4.0F));
		root.addOrReplaceChild("wing_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F),
				PartPose.offset(-5.0F, 14.0F, 0.0F));
		root.addOrReplaceChild("wing_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F),
				PartPose.offset(5.0F, 14.0F, 0.0F));
		return LayerDefinition.create(md, 32, 32);
	}

	@Override
	public void setupAnim(BlindEye entity, float limbSwing, float limbSwingAmount, float ageInTicks,
						  float netHeadYaw, float headPitch) {
		this.root.y = 14.0F + (float) Math.sin(ageInTicks * 0.08F) * 0.5F;
		float flutter = (float) Math.sin(ageInTicks * 0.2F) * 0.4F;
		this.root.getChild("wing_l").zRot = flutter;
		this.root.getChild("wing_r").zRot = -flutter;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
							   int packedOverlay, float red, float green, float blue, float alpha) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0.70F, 0.60F, 0.95F, 1.0F);
	}
}
