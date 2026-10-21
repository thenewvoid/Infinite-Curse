package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.StarvingGhoul;
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
 * 饥馑噬魂 几何模型（§新增）：骨瘦如柴、大首长臂的噬魂轮廓。纯立方体几何 + 病态黄绿色调色，复用原版白图。
 */
public class StarvingGhoulModel extends EntityModel<StarvingGhoul> {

	public static final ModelLayerLocation LAYER = new ModelLayerLocation(
			new ResourceLocation("infinite_curse", "starving_ghoul"), "main");

	private final ModelPart root;

	public StarvingGhoulModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition md = new MeshDefinition();
		PartDefinition root = md.getRoot();
		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -6.0F, -2.0F, 5.0F, 8.0F, 4.0F),
				PartPose.offset(0.0F, 15.0F, 0.0F));
		root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
				PartPose.offset(0.0F, 6.0F, 0.0F));
		root.addOrReplaceChild("jaw",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -3.0F, 4.0F, 2.0F, 4.0F),
				PartPose.offset(0.0F, 9.0F, 0.0F));
		root.addOrReplaceChild("arm_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 9.0F, 2.0F),
				PartPose.offset(-4.0F, 13.0F, 0.0F));
		root.addOrReplaceChild("arm_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 9.0F, 2.0F),
				PartPose.offset(4.0F, 13.0F, 0.0F));
		root.addOrReplaceChild("leg_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
				PartPose.offset(-2.0F, 17.0F, 0.0F));
		root.addOrReplaceChild("leg_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
				PartPose.offset(2.0F, 17.0F, 0.0F));
		return LayerDefinition.create(md, 32, 32);
	}

	@Override
	public void setupAnim(StarvingGhoul entity, float limbSwing, float limbSwingAmount, float ageInTicks,
						  float netHeadYaw, float headPitch) {
		this.root.getChild("leg_l").xRot = (float) Math.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
		this.root.getChild("leg_r").xRot = -((float) Math.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
							   int packedOverlay, float red, float green, float blue, float alpha) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0.55F, 0.65F, 0.30F, 1.0F);
	}
}
