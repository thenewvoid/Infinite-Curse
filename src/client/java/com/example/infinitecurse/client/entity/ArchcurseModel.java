package com.example.infinitecurse.client.entity;

import com.example.infinitecurse.entity.Archcurse;
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
 * 万咒之主 几何模型（§新增）：庞大、暗紫、生有双角与双臂的诅咒主宰，环绕两枚漂浮诅咒球。
 * 纯立方体几何 + 暗紫色调色，复用原版白图。
 */
public class ArchcurseModel extends EntityModel<Archcurse> {

	public static final ModelLayerLocation LAYER = new ModelLayerLocation(
			new ResourceLocation("infinite_curse", "archcurse"), "main");

	private final ModelPart root;

	public ArchcurseModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition md = new MeshDefinition();
		PartDefinition root = md.getRoot();
		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -16.0F, -3.0F, 10.0F, 18.0F, 6.0F),
				PartPose.offset(0.0F, 23.0F, 0.0F));
		root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
				PartPose.offset(0.0F, 3.0F, 0.0F));
		root.addOrReplaceChild("horn_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F),
				PartPose.offset(-4.0F, -1.0F, 0.0F));
		root.addOrReplaceChild("horn_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F),
				PartPose.offset(4.0F, -1.0F, 0.0F));
		root.addOrReplaceChild("arm_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -12.0F, -2.5F, 5.0F, 14.0F, 5.0F),
				PartPose.offset(-8.0F, 18.0F, 0.0F));
		root.addOrReplaceChild("arm_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -12.0F, -2.5F, 5.0F, 14.0F, 5.0F),
				PartPose.offset(8.0F, 18.0F, 0.0F));
		root.addOrReplaceChild("leg_l",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 9.0F, 4.0F),
				PartPose.offset(-3.0F, 23.0F, 0.0F));
		root.addOrReplaceChild("leg_r",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 9.0F, 4.0F),
				PartPose.offset(3.0F, 23.0F, 0.0F));
		root.addOrReplaceChild("orb1",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
				PartPose.offset(0.0F, 14.0F, -9.0F));
		root.addOrReplaceChild("orb2",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
				PartPose.offset(0.0F, 8.0F, 9.0F));
		return LayerDefinition.create(md, 64, 64);
	}

	@Override
	public void setupAnim(Archcurse entity, float limbSwing, float limbSwingAmount, float ageInTicks,
						  float netHeadYaw, float headPitch) {
		this.root.getChild("leg_l").xRot = (float) Math.cos(limbSwing * 0.6F) * 0.6F * limbSwingAmount;
		this.root.getChild("leg_r").xRot = -((float) Math.cos(limbSwing * 0.6F) * 0.6F * limbSwingAmount);
		this.root.getChild("orb1").yRot = ageInTicks * 0.05F;
		this.root.getChild("orb2").yRot = -ageInTicks * 0.05F;
		this.root.getChild("orb1").y = 14.0F + (float) Math.sin(ageInTicks * 0.06F) * 0.6F;
		this.root.getChild("orb2").y = 8.0F + (float) Math.cos(ageInTicks * 0.06F) * 0.6F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
							   int packedOverlay, float red, float green, float blue, float alpha) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0.35F, 0.15F, 0.50F, 1.0F);
	}
}
