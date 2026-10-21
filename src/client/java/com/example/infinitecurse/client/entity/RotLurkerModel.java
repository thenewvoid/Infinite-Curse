package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.RotLurker;
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
 * 腐咒潜伏者 几何模型（§新增）：佝偻、臃肿、拖尾的腐化轮廓。纯立方体几何 + 暗红色调色，复用原版白图。
 */
public class RotLurkerModel extends EntityModel<RotLurker> {

	public static final ModelLayerLocation LAYER = new ModelLayerLocation(
			new ResourceLocation("infinite_curse", "rot_lurker"), "main");

	private final ModelPart root;

	public RotLurkerModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition md = new MeshDefinition();
		PartDefinition root = md.getRoot();
		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.0F, -3.0F, 8.0F, 9.0F, 6.0F),
				PartPose.offset(0.0F, 13.0F, 0.0F));
		root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
				PartPose.offset(0.0F, 5.0F, 1.0F));
		root.addOrReplaceChild("leg_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
				PartPose.offset(-2.5F, 16.0F, 0.0F));
		root.addOrReplaceChild("leg_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
				PartPose.offset(2.5F, 16.0F, 0.0F));
		root.addOrReplaceChild("tail",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 4.0F, 8.0F),
				PartPose.offsetAndRotation(0.0F, 12.0F, 1.0F, -0.5F, 0.0F, 0.0F));
		return LayerDefinition.create(md, 32, 32);
	}

	@Override
	public void setupAnim(RotLurker entity, float limbSwing, float limbSwingAmount, float ageInTicks,
						  float netHeadYaw, float headPitch) {
		this.root.getChild("leg_l").xRot = (float) Math.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
		this.root.getChild("leg_r").xRot = -((float) Math.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
							   int packedOverlay, float red, float green, float blue, float alpha) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0.45F, 0.22F, 0.30F, 1.0F);
	}
}
