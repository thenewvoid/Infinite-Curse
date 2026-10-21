package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.StoneboundGolem;
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
 * 岩咒傀儡 几何模型（§新增）：庞大、方正、双臂粗壮的岩石守卫轮廓。纯立方体几何 + 深紫色调色，复用原版白图。
 */
public class StoneboundGolemModel extends EntityModel<StoneboundGolem> {

	public static final ModelLayerLocation LAYER = new ModelLayerLocation(
			new ResourceLocation("infinite_curse", "stonebound_golem"), "main");

	private final ModelPart root;

	public StoneboundGolemModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition md = new MeshDefinition();
		PartDefinition root = md.getRoot();
		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -10.0F, -5.0F, 14.0F, 16.0F, 10.0F),
				PartPose.offset(0.0F, 14.0F, 0.0F));
		root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
				PartPose.offset(0.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("arm_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 14.0F, 6.0F),
				PartPose.offset(-9.0F, 6.0F, 0.0F));
		root.addOrReplaceChild("arm_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 14.0F, 6.0F),
				PartPose.offset(9.0F, 6.0F, 0.0F));
		root.addOrReplaceChild("leg_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 8.0F, 7.0F),
				PartPose.offset(-4.0F, 16.0F, 0.0F));
		root.addOrReplaceChild("leg_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 8.0F, 7.0F),
				PartPose.offset(4.0F, 16.0F, 0.0F));
		return LayerDefinition.create(md, 64, 64);
	}

	@Override
	public void setupAnim(StoneboundGolem entity, float limbSwing, float limbSwingAmount, float ageInTicks,
						  float netHeadYaw, float headPitch) {
		this.root.getChild("leg_l").xRot = (float) Math.cos(limbSwing * 0.5F) * 0.7F * limbSwingAmount;
		this.root.getChild("leg_r").xRot = -((float) Math.cos(limbSwing * 0.5F) * 0.7F * limbSwingAmount);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
							   int packedOverlay, float red, float green, float blue, float alpha) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0.32F, 0.16F, 0.42F, 1.0F);
	}
}
