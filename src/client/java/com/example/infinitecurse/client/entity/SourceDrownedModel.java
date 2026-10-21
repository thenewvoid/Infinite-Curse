package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.SourceDrowned;
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
 * 源咒溺灵 几何模型（§新增）：人形 + 脊背鱼鳍的溺亡诅咒轮廓。纯立方体几何 + 青紫色调色，复用原版白图。
 */
public class SourceDrownedModel extends EntityModel<SourceDrowned> {

	public static final ModelLayerLocation LAYER = new ModelLayerLocation(
			new ResourceLocation("infinite_curse", "source_drowned"), "main");

	private final ModelPart root;

	public SourceDrownedModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition md = new MeshDefinition();
		PartDefinition root = md.getRoot();
		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -7.0F, -2.5F, 7.0F, 11.0F, 5.0F),
				PartPose.offset(0.0F, 13.0F, 0.0F));
		root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
				PartPose.offset(0.0F, 5.0F, 0.0F));
		root.addOrReplaceChild("fin",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 6.0F),
				PartPose.offset(0.0F, 9.0F, 1.0F));
		root.addOrReplaceChild("arm_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 9.0F, 3.0F),
				PartPose.offset(-4.5F, 7.0F, 0.0F));
		root.addOrReplaceChild("arm_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 9.0F, 3.0F),
				PartPose.offset(4.5F, 7.0F, 0.0F));
		root.addOrReplaceChild("leg_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F),
				PartPose.offset(-2.0F, 16.0F, 0.0F));
		root.addOrReplaceChild("leg_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F),
				PartPose.offset(2.0F, 16.0F, 0.0F));
		return LayerDefinition.create(md, 32, 32);
	}

	@Override
	public void setupAnim(SourceDrowned entity, float limbSwing, float limbSwingAmount, float ageInTicks,
						  float netHeadYaw, float headPitch) {
		this.root.getChild("leg_l").xRot = (float) Math.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
		this.root.getChild("leg_r").xRot = -((float) Math.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
							   int packedOverlay, float red, float green, float blue, float alpha) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0.30F, 0.40F, 0.85F, 1.0F);
	}
}
